package com.axiomapoc.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data(staticConstructor = "of")
public class MapKey implements Serializable {
    private final String source;
    private final long axiomaDataId;
    private final ValidityRange validityRange;
    private final LocalDateTime transactionTime;
}
