package com.axiomapoc.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.Data;

import java.io.IOException;
import java.time.LocalDateTime;

@Data
public class BiTemporalDoc implements DataSerializable {
    private ValidityRange validityRange;
    private long axiomaDataId;
    private String id;
    private String source;
    private LocalDateTime transactionTime;

    private String currency;
    private LocalDateTime maturityDate;
    private double currentCoupon;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(validityRange);
        out.writeLong(axiomaDataId);
        out.writeUTF(id);
        out.writeUTF(source);
        out.writeObject(transactionTime);

        out.writeUTF(currency);
        out.writeObject(maturityDate);
        out.writeDouble(currentCoupon);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        validityRange = in.readObject();
        axiomaDataId = in.readLong();
        id = in.readUTF();
        source = in.readUTF();
        transactionTime = in.readObject();

        currency = in.readUTF();
        maturityDate = in.readObject();
        currentCoupon = in.readDouble();
    }
}

