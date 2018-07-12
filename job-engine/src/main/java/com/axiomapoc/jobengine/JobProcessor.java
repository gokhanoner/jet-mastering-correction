package com.axiomapoc.jobengine;

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
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.query.Predicate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

import static com.axiomapoc.util.Constants.INSTRUMENT_MAP;
import static com.axiomapoc.util.Constants.JOB_COUNTER;
import static com.axiomapoc.util.Constants.JOB_MAP;
import static com.hazelcast.jet.aggregate.AggregateOperations.counting;
import static com.hazelcast.jet.aggregate.AggregateOperations.maxBy;
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
        IMapJet<UUID, MCJob> jobMap = jetInstance.getMap(JOB_MAP);
        mcJob.setJobStatus(jobStatus);

        switch (jobStatus) {
            case RUNNING:
                mcJob.setStartTime(LocalDateTime.now());
                break;
            case COMPLETED:
                mcJob.setEndTime(LocalDateTime.now());
                break;
        }

        jobMap.put(mcJob.getRequestId(), mcJob);
    }

    private static Pipeline buildPipeline(UUID requestId, MCRequest mcRequest) {
        Pipeline p = Pipeline.create();
        long asAtDate = mcRequest.getAsAtDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Predicate<MapKey, BiTemporalDoc> pr = and(in("souces", mcRequest.getSources()));

        BatchStage<BiTemporalDoc> data = p
                .drawFrom(Sources.<BiTemporalDoc, MapKey, BiTemporalDoc>remoteMap(INSTRUMENT_MAP, dataClientConfig, alwaysTrue(), Map.Entry::getValue));

        //Mastering
        BatchStage<BiTemporalDoc> aggregate = data
                //.map(Map.Entry::getValue)
                .filter(e -> e.isValid(mcRequest.getAsAtDate().atStartOfDay()))
                .groupingKey(e -> LogicalId.of(e.getSource(), e.getAxiomaDataId()))
                .aggregate(maxBy(comparing(BiTemporalDoc::getTransactionTime)))
                .map(Map.Entry::getValue)
                .filter(e -> e.getCurrency().equals("USD"));

        //.groupingKey(e -> e.getAxiomaDataId())
        //.aggregate(toList());
        // Correction Step
        //.filter(e -> e.getCurrency().equals("USD") && e.getMaturityDate().toLocalDate().isEqual(LocalDate.now()))

        aggregate.aggregate(counting()).drainTo(Sinks.logger());

        aggregate.drainTo(Sinks.list(requestId.toString()));

        return p;
    }
}
