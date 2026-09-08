package com.medprep.controller;

import com.medprep.entity.BatchGenerationJob;
import com.medprep.service.GeminiBatchService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/gemini/batch")
public class GeminiBatchController {

    private final GeminiBatchService
            geminiBatchService;

    public GeminiBatchController(
            GeminiBatchService geminiBatchService) {

        this.geminiBatchService =
                geminiBatchService;
    }

    // ==========================================================
    // CREATE SMALL TEST BATCH
    // ==========================================================

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>>
    createTestBatch() {

        return ResponseEntity.ok(
                geminiBatchService.createTestBatch()
        );
    }

    // ==========================================================
    // GET GEMINI BATCH STATUS
    // ==========================================================

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>>
    getBatchStatus(
            @RequestParam String batchName) {

        return ResponseEntity.ok(
                geminiBatchService
                        .getBatchStatus(batchName)
        );
    }

    // ==========================================================
    // GET STORED BATCH JOBS
    // ==========================================================

    @GetMapping("/jobs")
    public ResponseEntity<List<BatchGenerationJob>>
    getStoredBatchJobs() {

        return ResponseEntity.ok(
                geminiBatchService
                        .getStoredBatchJobs()
        );
    }

    // ==========================================================
    // EXCEPTION HANDLING
    // ==========================================================

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<Map<String, String>>
    handleBadRequest(
            IllegalArgumentException exception) {

        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(
            IllegalStateException.class
    )
    public ResponseEntity<Map<String, String>>
    handleGenerationFailure(
            IllegalStateException exception) {

        Map<String, String> body =
                new LinkedHashMap<>();

        body.put(
                "message",
                exception.getMessage()
        );

        return ResponseEntity
                .status(502)
                .body(body);
    }
}