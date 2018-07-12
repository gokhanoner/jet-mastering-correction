package com.axiomapoc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Dummy implements Serializable {
    private int id;
    private DT dt;

    @Data(staticConstructor = "of")
    public static class DT implements Serializable {
        private final LocalDateTime dt;
    }

}
