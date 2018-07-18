package com.axiomapoc.jobengine;

import com.axiomapoc.model.MCJob;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;

import java.util.UUID;

public class JobListener implements EntryAddedListener<String, MCJob> {

    @Override
    public void entryAdded(EntryEvent<String, MCJob> event) {
        JobProcessor.submitJob(event.getValue());
    }

}
