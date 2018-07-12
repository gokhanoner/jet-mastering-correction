package com.axiomapoc.model;

import lombok.Data;

import java.util.Collection;

@Data(staticConstructor = "of")
public class MCJobResult {
    private final MCJob job;
    private final Collection<BiTemporalDoc> result;
}
