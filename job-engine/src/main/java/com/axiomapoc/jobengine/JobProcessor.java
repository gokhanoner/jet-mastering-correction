package com.axiomapoc.jobengine;

import com.axiomapoc.index.BiTemporalIdx;
import com.axiomapoc.model.BiTemporalDoc;
import com.axiomapoc.model.LogicalId;
import com.axiomapoc.model.MCJob;
import com.axiomapoc.model.MCJob.JobStatus;
import com.axiomapoc.model.MCRequest;
import com.axiomapoc.model.MapKey;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.crdt.pncounter.PNCounter;
import com.hazelcast.jet.IMapJet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.axiomapoc.util.Constants.INSTRUMENT_MAP;
import static com.axiomapoc.util.Constants.JOB_COUNTER;
import static com.axiomapoc.util.Constants.JOB_MAP;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.aggregate.AggregateOperations.maxBy;
import static com.hazelcast.jet.aggregate.AggregateOperations.toList;
import static com.hazelcast.jet.function.DistributedComparator.comparing;
import static com.hazelcast.query.Predicates.*;

public class JobProcessor {

    private static JetInstance jetInstance;
    private static ClientConfig dataClientConfig;

    public static void init(JetInstance jetInstance, ClientConfig dataClientConfig) {
        JobProcessor.jetInstance = jetInstance;
        JobProcessor.dataClientConfig = dataClientConfig;
    }

    public static void submitJob(MCJob mcJob) {
        Pipeline p = buildPipeline(mcJob.getRequestId(), mcJob.getRequest());

        //Submit Job
        Job job = jetInstance.newJob(p);

        mcJob.setJetJobId(job.getId());

        updateJobStatus(mcJob, JobStatus.RUNNING);
        updateRunningJobCount(1);

        //Wait for Job completion
        job.join();

        updateRunningJobCount(-1);
        updateJobStatus(mcJob, JobStatus.COMPLETED);
        informJobCompletion(mcJob.getRequestId());
    }

    private static void informJobCompletion(UUID requstId) {
        jetInstance.getHazelcastInstance().getCountDownLatch(requstId.toString()).countDown();
    }

    private static void updateRunningJobCount(int increment) {
        PNCounter pnCounter = jetInstance.getHazelcastInstance().getPNCounter(JOB_COUNTER);
        pnCounter.addAndGet(increment);
    }

    private static void updateJobStatus(MCJob mcJob, JobStatus jobStatus) {
        IMapJet<String, MCJob> jobMap = jetInstance.getMap(JOB_MAP);
        mcJob.setJobStatus(jobStatus);

        switch (jobStatus) {
            case RUNNING:
                mcJob.setStartTime(LocalDateTime.now());
                break;
            case COMPLETED:
                mcJob.setEndTime(LocalDateTime.now());
                break;
        }

        jobMap.put(mcJob.getRequestId().toString(), mcJob);
    }

    private static Pipeline buildPipeline(UUID requestId, MCRequest mcRequest) {
        Pipeline p = Pipeline.create();

        LocalDateTime asAtDate = mcRequest.getAsAtDate().atStartOfDay();
        Predicate sources = in("source", mcRequest.getSources());

        //Predicate predicate = lessEqual("bitempidx", new BiTemporalIdx(asAtDate));

        Predicate predicate = and(lessEqual("validityRange.validFrom", asAtDate),
                greaterThan("validityRange.validTo", asAtDate),
                lessEqual("transactionTime", asAtDate));

        BatchStage<BiTemporalDoc> data = p
                .drawFrom(Sources.<BiTemporalDoc, MapKey, BiTemporalDoc>remoteMap(INSTRUMENT_MAP, dataClientConfig, predicate, Projections.singleAttribute("this"))).setName("filter-by-temporal");


        //Mastering
        BatchStage<BiTemporalDoc> aggregate = data
                .filter(e -> Arrays.asList(mcRequest.getSources()).contains(e.getSource())).setName("filter-sources")
                .groupingKey(e -> LogicalId.of(e.getSource(), e.getAxiomaDataId()))
                .aggregate(maxBy(comparing(BiTemporalDoc::getTransactionTime))).setName("find-doc-with-max-tran-time")
                .map(Map.Entry::getValue).setName("map-2-value")
                //Enrichment steps
                //.addKey(BiTemporalDoc::getAxiomaDataId)
                //.aggregate(toList());
                // Correction Step
                .filter(e -> Objects.isNull(mcRequest.getCurrency()) ? true : Objects.equals(e.getCurrency(), mcRequest.getCurrency())).setName("filter-currency")
                .filter(e -> Objects.isNull(mcRequest.getMaturityDate()) ? true : Objects.equals(e.getMaturityDate(), mcRequest.getMaturityDate())).setName("filter-mat-date");


        aggregate.drainTo(Sinks.logger());
        aggregate.drainTo(Sinks.list(requestId.toString())).setName("write-to-list");

        aggregate.aggregate(counting()).drainTo(Sinks.logger());

        return p;
    }
}
