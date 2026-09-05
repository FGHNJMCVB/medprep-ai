package com.medprep.controller;

import com.medprep.dto.TopicResponse;
import com.medprep.service.TopicService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/subjects/{subjectId}/topics")
    public List<TopicResponse> getTopicsBySubject(
            @PathVariable Long subjectId) {

        return topicService.getTopicsBySubject(subjectId);
    }
}