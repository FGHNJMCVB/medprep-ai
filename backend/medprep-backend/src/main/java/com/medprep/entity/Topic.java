package com.medprep.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "topics",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_topic_subject_name",
            columnNames = {"subject_id", "name"}
        )
    }
)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    public Topic() {
    }

    public Topic(String name, Integer displayOrder, Subject subject) {
        this.name = name;
        this.displayOrder = displayOrder;
        this.subject = subject;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}