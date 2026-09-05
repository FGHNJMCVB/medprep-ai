package com.medprep.repository;

import com.medprep.entity.MockTestConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MockTestConfigRepository
        extends JpaRepository<MockTestConfig, Long> {

    Optional<MockTestConfig> findByName(String name);
}