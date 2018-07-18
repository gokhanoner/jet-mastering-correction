package com.axiomapoc.index;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

@Data
@NoArgsConstructor
public class BiTemporalIdx implements Comparable<BiTemporalIdx>, Serializable {

    private LocalDateTime transactionDate;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    public BiTemporalIdx(LocalDateTime dateTime) {
        this.transactionDate = dateTime;
        this.validTo = dateTime;
        this.validFrom = dateTime;
    }

    @Override
    public int compareTo(BiTemporalIdx o) {
        int compareTran = comparing(BiTemporalIdx::getTransactionDate, nullsLast(naturalOrder())).compare(this, o);
        int compareTo = comparing(BiTemporalIdx::getValidTo, nullsLast(naturalOrder())).compare(this, o);
        int compareFrom = comparing(BiTemporalIdx::getValidFrom, nullsLast(naturalOrder())).compare(this, o);

        if (compareTo == 0 && compareFrom == 0 && compareTran == 0) return 0;

        if (compareTran <= 0 && compareTo > 0 && compareFrom <= 0) return -1;

        return 1;
    }
}
