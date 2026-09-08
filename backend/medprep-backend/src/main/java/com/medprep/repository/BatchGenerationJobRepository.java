package com.medprep.repository;

import com.medprep.entity.BatchGenerationJob;
import com.medprep.entity.BatchGenerationJobStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchGenerationJobRepository
        extends JpaRepository<BatchGenerationJob, Long> {

    Optional<BatchGenerationJob> findByBatchName(
            String batchName
    );

    List<BatchGenerationJob> findByStatusOrderByIdAsc(
            BatchGenerationJobStatus status
    );

    List<BatchGenerationJob> findAllByOrderByIdAsc();
}