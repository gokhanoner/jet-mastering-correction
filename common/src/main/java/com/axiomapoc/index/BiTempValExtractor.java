package com.axiomapoc.index;

import com.axiomapoc.model.BiTemporalDoc;
import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;

public class BiTempValExtractor extends ValueExtractor<BiTemporalDoc, Integer> {

    @Override
    public void extract(BiTemporalDoc target, Integer argument, ValueCollector collector) {
        BiTemporalIdx idx = new BiTemporalIdx();
        idx.setTransactionDate(target.getTransactionTime());
        idx.setValidFrom(target.getValidityRange().getValidFrom());
        idx.setValidTo(target.getValidityRange().getValidTo());

        collector.addObject(idx);
    }
}
