package com.medprep.service;

import com.medprep.dto.SubjectResponse;
import com.medprep.entity.Subject;
import com.medprep.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<SubjectResponse> getAllSubjects() {

        return subjectRepository
                .findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SubjectResponse toResponse(Subject subject) {

        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDisplayOrder(),
                subject.getWeightageMarks(),
                subject.getSection()
        );
    }
}