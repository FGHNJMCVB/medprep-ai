package com.medprep.repository;

import com.medprep.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findBySubjectIdOrderByDisplayOrderAsc(Long subjectId);

    boolean existsBySubjectIdAndName(Long subjectId, String name);
}