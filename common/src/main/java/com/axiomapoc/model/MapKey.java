package com.axiomapoc.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class MapKey implements DataSerializable {
    private String source;
    private long axiomaDataId;
    private ValidityRange validityRange;
    private LocalDateTime transactionTime;

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(source);
        out.writeLong(axiomaDataId);
        out.writeObject(validityRange);
        out.writeObject(transactionTime);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        source = in.readUTF();
        axiomaDataId = in.readLong();
        validityRange = in.readObject();
        transactionTime = in.readObject();
    }
}
