package com.medprep.service;

import com.medprep.dto.TopicResponse;
import com.medprep.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<TopicResponse> getTopicsBySubject(Long subjectId) {

        return topicRepository
                .findBySubjectIdOrderByDisplayOrderAsc(subjectId)
                .stream()
                .map(topic -> new TopicResponse(
                        topic.getId(),
                        topic.getName(),
                        topic.getDisplayOrder(),
                        topic.getSubject().getId()
                ))
                .toList();
    }
}