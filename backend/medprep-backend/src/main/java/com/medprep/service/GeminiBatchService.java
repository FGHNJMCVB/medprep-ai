package com.medprep.service;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.types.BatchJob;
import com.google.genai.types.BatchJobSource;
import com.google.genai.types.Content;
import com.google.genai.types.CreateBatchJobConfig;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.InlinedRequest;
import com.google.genai.types.Part;

import com.medprep.entity.BatchGenerationJob;
import com.medprep.entity.BatchGenerationJobStatus;
import com.medprep.repository.BatchGenerationJobRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GeminiBatchService {

    private final BatchGenerationJobRepository
            batchGenerationJobRepository;

    private final Client client;

    private final String model;

    public GeminiBatchService(
            BatchGenerationJobRepository batchGenerationJobRepository,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-3.6-flash}") String model) {

        this.batchGenerationJobRepository =
                batchGenerationJobRepository;

        this.model = model;

        if (apiKey == null ||
                apiKey.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Gemini API key is not configured. "
                            + "Set GEMINI_API_KEY before using "
                            + "Batch API."
            );
        }

        /*
         * Gemini Developer API client.
         *
         * The existing application already uses
         * gemini.api-key, so reuse that same configuration.
         */
        this.client =
                Client.builder()
                        .apiKey(apiKey)
                        .build();
    }

    // ==========================================================
    // TEST BATCH
    // ==========================================================
    //
    // Creates a very small 2-request batch.
    //
    // This is intentionally NOT the full question-bank
    // generation yet.
    //
    public Map<String, Object> createTestBatch() {

        String displayName =
                "medprep-test-batch-"
                        + UUID.randomUUID();

        InlinedRequest request1 =
                InlinedRequest.builder()
                        .contents(
                                Content.builder()
                                        .parts(
                                                Part.fromText(
                                                        "Return the following text exactly: "
                                                                + "MEDPREP_BATCH_TEST_1"
                                                )
                                        )
                                        .build()
                        )
                        .config(
                                GenerateContentConfig.builder()
                                        .temperature(0.1f)
                                        .build()
                        )
                        .build();

        InlinedRequest request2 =
                InlinedRequest.builder()
                        .contents(
                                Content.builder()
                                        .parts(
                                                Part.fromText(
                                                        "Return the following text exactly: "
                                                                + "MEDPREP_BATCH_TEST_2"
                                                )
                                        )
                                        .build()
                        )
                        .config(
                                GenerateContentConfig.builder()
                                        .temperature(0.1f)
                                        .build()
                        )
                        .build();

        BatchJobSource batchJobSource =
                BatchJobSource.builder()
                        .inlinedRequests(
                                ImmutableList.of(
                                        request1,
                                        request2
                                )
                        )
                        .build();

        CreateBatchJobConfig config =
                CreateBatchJobConfig.builder()
                        .displayName(displayName)
                        .build();

        // BatchJob batchJob =
        //         client.batches.create(
        //                 model,
        //                 batchJobSource,
        //                 config
        //         );
        BatchJob batchJob;

try {
    System.out.println("========== GEMINI BATCH TEST ==========");
    System.out.println("Model: " + model);
    System.out.println("API key configured: " +
            (System.getenv("GEMINI_API_KEY") != null));

    batchJob = client.batches.create(
            model,
            batchJobSource,
            config
    );

    System.out.println("========== GEMINI BATCH CREATED ==========");
    System.out.println("Batch: " +
            batchJob.name().orElse("NO NAME"));

} catch (Exception e) {

    System.err.println("========== GEMINI BATCH ERROR ==========");
    e.printStackTrace();
    System.err.println("=========================================");

    throw e;
}

        String batchName =
                batchJob.name()
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Gemini created the batch "
                                                + "but returned no batch name"
                                )
                        );

        BatchGenerationJob databaseJob =
                new BatchGenerationJob(
                        batchName,
                        2
                );

        batchGenerationJobRepository.save(
                databaseJob
        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "success",
                true
        );

        response.put(
                "batchName",
                batchName
        );

        response.put(
                "displayName",
                displayName
        );

        response.put(
                "model",
                model
        );

        response.put(
                "requestCount",
                2
        );

        response.put(
                "status",
                databaseJob
                        .getStatus()
                        .name()
        );

        return response;
    }

    // ==========================================================
    // GET BATCH
    // ==========================================================

    public Map<String, Object> getBatchStatus(
            String batchName) {

        if (batchName == null ||
                batchName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Batch name is required"
            );
        }

        BatchJob batchJob =
                client.batches.get(
                        batchName,null
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "batchName",
                batchJob.name().orElse(batchName)
        );

        response.put(
                "state",
                batchJob.state() != null
                        ? batchJob.state().toString()
                        : null
        );

        response.put(
                "displayName",
                batchJob.displayName().orElse(null)
        );

        return response;
    }

    // ==========================================================
    // GET DATABASE BATCH
    // ==========================================================

    public List<BatchGenerationJob>
    getStoredBatchJobs() {

        return batchGenerationJobRepository
                .findAllByOrderByIdAsc();
    }
}