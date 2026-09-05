package com.medprep.controller;

import com.medprep.dto.QuestionTrendResponse;
import com.medprep.entity.QuestionTrend;
import com.medprep.service.QuestionTrendService;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/question-trends")
public class QuestionTrendController {

    private final QuestionTrendService questionTrendService;

    public QuestionTrendController(
            QuestionTrendService questionTrendService) {

        this.questionTrendService =
                questionTrendService;
    }

    // ==========================================================
    // GET ALL TRENDS
    // ==========================================================

    @GetMapping
    public List<QuestionTrendResponse> getAllTrends() {

        return toResponseList(
                questionTrendService.getAllTrends()
        );
    }

    // ==========================================================
    // GET SUBJECT TRENDS
    // ==========================================================

    @GetMapping("/subject/{subjectId}")
    public List<QuestionTrendResponse> getSubjectTrends(
            @PathVariable Long subjectId) {

        return toResponseList(
                questionTrendService.getSubjectTrends(
                        subjectId
                )
        );
    }

    // ==========================================================
    // GET TOPIC TRENDS
    // ==========================================================

    @GetMapping("/topic/{topicId}")
    public List<QuestionTrendResponse> getTopicTrends(
            @PathVariable Long topicId) {

        return toResponseList(
                questionTrendService.getTopicTrends(
                        topicId
                )
        );
    }

    // ==========================================================
    // ENTITY -> DTO
    // ==========================================================

    private List<QuestionTrendResponse> toResponseList(
            List<QuestionTrend> trends) {

        List<QuestionTrendResponse> responses =
                new ArrayList<>();

        for(QuestionTrend trend : trends) {

            responses.add(
                    new QuestionTrendResponse(
                            trend.getId(),

                            trend.getSubject().getId(),
                            trend.getSubject().getName(),

                            trend.getTopic() != null
                                    ? trend.getTopic().getId()
                                    : null,

                            trend.getTopic() != null
                                    ? trend.getTopic().getName()
                                    : null,

                            trend.getConceptTag(),

                            trend.getHistoricalFrequency(),
                            trend.getRecentFrequency(),
                            trend.getRecurrenceYears(),

                            trend.getClinicalWeight(),
                            trend.getImageWeight(),
                            trend.getIntegratedWeight(),

                            trend.getTrendScore(),
                            trend.getHighYield(),

                            trend.getSourceReference()
                    )
            );
        }

        return responses;
    }
}