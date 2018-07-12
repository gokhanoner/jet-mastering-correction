package com.axiomapoc.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data(staticConstructor = "of")
public class MCJob implements Serializable {
    private final UUID requestId;
    private final MCRequest request;
    private final LocalDateTime submitTime;

    private long jetJobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private JobStatus jobStatus;


    public enum JobStatus {
        SUBMITTED,
        RUNNING,
        COMPLETED;
    }
}