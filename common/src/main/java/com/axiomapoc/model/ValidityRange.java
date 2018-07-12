package com.axiomapoc.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ValidityRange implements Serializable {
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
