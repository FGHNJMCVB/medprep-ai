package com.medprep.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String optionLabel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean correct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    public QuestionOption() {
    }

    public QuestionOption(String optionLabel,
                           String optionText,
                           Integer displayOrder,
                           Boolean correct,
                           Question question) {

        this.optionLabel = optionLabel;
        this.optionText = optionText;
        this.displayOrder = displayOrder;
        this.correct = correct;
        this.question = question;
    }

    public Long getId() {
        return id;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}