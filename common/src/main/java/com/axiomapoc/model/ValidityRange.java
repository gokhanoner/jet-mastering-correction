package com.axiomapoc.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.Data;

import java.io.IOException;
import java.time.LocalDateTime;

@Data
public class ValidityRange implements DataSerializable {

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(validFrom);
        out.writeObject(validTo);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        validFrom = in.readObject();
        validTo = in.readObject();
    }
}
