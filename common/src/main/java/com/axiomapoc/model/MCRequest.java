package com.axiomapoc.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@NoArgsConstructor
@Data
@Setter(AccessLevel.NONE)
public class MCRequest implements Serializable {
    private LocalDate asAtDate;
    private String[] sources;
    //Other request params
}
