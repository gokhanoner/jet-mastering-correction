package com.axiomapoc.model;

import lombok.Data;

import java.io.Serializable;

@Data(staticConstructor = "of")
public class LogicalId implements Serializable {
    private final String source;
    private final long axiomaDataId;
}
