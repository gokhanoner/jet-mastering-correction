package com.axiomapoc.web.service;

import com.axiomapoc.model.MCJob;
import com.axiomapoc.model.MCJobResult;
import com.axiomapoc.model.MCRequest;
import com.axiomapoc.util.Constants;
import com.hazelcast.core.ICountDownLatch;
import com.hazelcast.jet.IMapJet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.util.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.axiomapoc.model.MCJob.JobStatus.*;
import static com.axiomapoc.util.Constants.*;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JetInstance jetInstance;


    public MCJobResult submitJob(MCRequest mcRequest) {
        MCJob job = MCJob.of(UUID.randomUUID(), mcRequest, LocalDateTime.now());
        job.setJobStatus(SUBMITTED);

        IMapJet<String, MCJob> jobMap = jetInstance.getMap(JOB_MAP);

        initiateWait(job.getRequestId());

        //Post Job
        jobMap.put(job.getRequestId().toString(), job);

        //Wait until job-engine finishes the execution
        waitJobCompletion(job.getRequestId());

        //Get Job Result
        job = jobMap.get(job.getRequestId().toString());

        //Return Job Results
        return MCJobResult.of(job, jetInstance.getList(job.getRequestId().toString()), jetInstance.getList(job.getRequestId().toString()).size());
    }

    private void initiateWait(UUID requestId) {
        ICountDownLatch countDownLatch = jetInstance.getHazelcastInstance().getCountDownLatch(requestId.toString());
        countDownLatch.trySetCount(1);
    }

    private void waitJobCompletion(UUID requestId) {
        ICountDownLatch countDownLatch = jetInstance.getHazelcastInstance().getCountDownLatch(requestId.toString());
        try {
            countDownLatch.await(30, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
            ExceptionUtil.peel(e);
        } finally {
            countDownLatch.destroy();
        }
    }
}
