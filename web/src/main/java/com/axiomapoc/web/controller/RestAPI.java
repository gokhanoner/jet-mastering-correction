package com.axiomapoc.web.controller;

import com.axiomapoc.model.BiTemporalDoc;
import com.axiomapoc.model.MCJob;
import com.axiomapoc.model.MCJobResult;
import com.axiomapoc.model.MCRequest;
import com.axiomapoc.web.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestAPI {

    private final JobService jobService;

    @PostMapping("/docs")
    public MCJobResult getDocs(@RequestBody MCRequest mcRequest) {
        System.out.println(mcRequest);
        //return MCJobResult.of(MCJob.of(UUID.randomUUID(), mcRequest, LocalDateTime.now()), Collections.emptyList());
        return jobService.submitJob(mcRequest);
    }

}
