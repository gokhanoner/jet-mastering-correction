package com.axiomapoc.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BiTemporalDoc implements Serializable {
    private ValidityRange validityRange;
    private long axiomaDataId;
    private String id;
    private String source;
    private LocalDateTime transactionTime;

    private String currency;
    private LocalDateTime maturityDate;
    private double currentCoupon;


    public boolean isValid(LocalDateTime asAtDate) {
        return asAtDate.isAfter(getValidityRange().getValidFrom()) && asAtDate.isBefore(getValidityRange().getValidTo());
    }

    public boolean isValidTransaction(LocalDateTime asAtDate) {
        return getTransactionTime().compareTo(asAtDate) >= 0;
    }
}

