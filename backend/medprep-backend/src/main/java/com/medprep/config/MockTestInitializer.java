package com.medprep.config;

import com.medprep.entity.MockTestConfig;
import com.medprep.repository.MockTestConfigRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockTestInitializer {

    @Bean
    CommandLineRunner initializeMockTestConfig(
            MockTestConfigRepository mockTestConfigRepository) {

        return args -> {

            if(mockTestConfigRepository
                    .findByName("FMGE")
                    .isPresent()) {

                return;
            }

            MockTestConfig config =
                    new MockTestConfig(
                            "FMGE",
                            300,
                            150,
                            150,
                            150,
                            false
                    );

            mockTestConfigRepository.save(config);
        };
    }
}