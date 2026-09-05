package com.medprep.config;

import com.medprep.entity.QuestionTrend;
import com.medprep.entity.Subject;
import com.medprep.entity.Topic;

import com.medprep.repository.QuestionTrendRepository;
import com.medprep.repository.SubjectRepository;
import com.medprep.repository.TopicRepository;

import com.medprep.service.QuestionTrendService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class QuestionTrendInitializer {

    @Bean
    CommandLineRunner initializeQuestionTrends(
            QuestionTrendRepository questionTrendRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            QuestionTrendService questionTrendService) {

        return args -> {

            /*
             * We only insert these development trend records
             * when the table is empty.
             *
             * Later, these records should come from our
             * researched FMGE trend dataset.
             */

            if(questionTrendRepository.count() > 0) {
                return;
            }

            // ======================================================
            // ANATOMY - UPPER LIMB
            // ======================================================

            Optional<Subject> anatomyOptional =
                    subjectRepository.findById(1L);

            Optional<Topic> upperLimbOptional =
                    topicRepository.findById(2L);

            if(anatomyOptional.isPresent() &&
               upperLimbOptional.isPresent()) {

                Subject anatomy =
                        anatomyOptional.get();

                Topic upperLimb =
                        upperLimbOptional.get();

                double score =
                        questionTrendService.calculateTrendScore(
                                12,
                                6,
                                4,
                                0.90,
                                0.30,
                                0.70
                        );

                QuestionTrend trend =
                        new QuestionTrend(
                                anatomy,
                                upperLimb,
                                "Peripheral nerve injuries",
                                12,
                                6,
                                4,
                                0.90,
                                0.30,
                                0.70,
                                score,
                                questionTrendService
                                        .isHighYield(score),
                                "Development trend seed"
                        );

                questionTrendRepository.save(trend);
            }

            // ======================================================
            // ANATOMY - NEUROANATOMY
            // ======================================================

            Optional<Topic> neuroanatomyOptional =
                    topicRepository.findById(8L);

            if(anatomyOptional.isPresent() &&
               neuroanatomyOptional.isPresent()) {

                Subject anatomy =
                        anatomyOptional.get();

                Topic neuroanatomy =
                        neuroanatomyOptional.get();

                double score =
                        questionTrendService.calculateTrendScore(
                                16,
                                8,
                                5,
                                0.95,
                                0.50,
                                0.85
                        );

                QuestionTrend trend =
                        new QuestionTrend(
                                anatomy,
                                neuroanatomy,
                                "Cranial nerves and lesions",
                                16,
                                8,
                                5,
                                0.95,
                                0.50,
                                0.85,
                                score,
                                questionTrendService
                                        .isHighYield(score),
                                "Development trend seed"
                        );

                questionTrendRepository.save(trend);
            }

            // ======================================================
            // PHYSIOLOGY - CARDIOVASCULAR
            // ======================================================

            Optional<Subject> physiologyOptional =
                    subjectRepository.findById(2L);

            Optional<Topic> cardiovascularOptional =
                    topicRepository.findById(13L);

            if(physiologyOptional.isPresent() &&
               cardiovascularOptional.isPresent()) {

                Subject physiology =
                        physiologyOptional.get();

                Topic cardiovascular =
                        cardiovascularOptional.get();

                double score =
                        questionTrendService.calculateTrendScore(
                                18,
                                9,
                                5,
                                0.95,
                                0.40,
                                0.90
                        );

                QuestionTrend trend =
                        new QuestionTrend(
                                physiology,
                                cardiovascular,
                                "Cardiac cycle and hemodynamics",
                                18,
                                9,
                                5,
                                0.95,
                                0.40,
                                0.90,
                                score,
                                questionTrendService
                                        .isHighYield(score),
                                "Development trend seed"
                        );

                questionTrendRepository.save(trend);
            }
        };
    }
}