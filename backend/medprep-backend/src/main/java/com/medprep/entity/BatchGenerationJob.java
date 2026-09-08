package com.medprep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "batch_generation_jobs")
public class BatchGenerationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String batchName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchGenerationJobStatus status;

    @Column(nullable = false)
    private int requestCount;

    @Column(nullable = false)
    private int completedRequests;

    @Column(nullable = false)
    private int failedRequests;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(length = 2000)
    private String lastError;

    protected BatchGenerationJob() {
        // JPA
    }

    public BatchGenerationJob(
            String batchName,
            int requestCount) {

        this.batchName = batchName;
        this.requestCount = requestCount;
        this.status = BatchGenerationJobStatus.CREATED;
        this.completedRequests = 0;
        this.failedRequests = 0;

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    // ==========================================================
    // STATE TRANSITIONS
    // ==========================================================

    public void markProcessing() {

        this.status =
                BatchGenerationJobStatus.PROCESSING;

        this.updatedAt =
                Instant.now();
    }

    public void markCompleted(
            int completedRequests,
            int failedRequests) {

        this.status =
                BatchGenerationJobStatus.COMPLETED;

        this.completedRequests =
                completedRequests;

        this.failedRequests =
                failedRequests;

        this.lastError = null;

        this.updatedAt =
                Instant.now();
    }

    public void markFailed(
            String errorMessage) {

        this.status =
                BatchGenerationJobStatus.FAILED;

        this.lastError =
                errorMessage;

        this.updatedAt =
                Instant.now();
    }

    // ==========================================================
    // GETTERS
    // ==========================================================

    public Long getId() {
        return id;
    }

    public String getBatchName() {
        return batchName;
    }

    public BatchGenerationJobStatus getStatus() {
        return status;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getCompletedRequests() {
        return completedRequests;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getLastError() {
        return lastError;
    }
}