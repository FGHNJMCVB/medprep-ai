package com.medprep.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.medprep.dto.AdminQuestionRequest;
import com.medprep.dto.GeneratedQuestionResponse;

import org.springframework.stereotype.Component;

@Component
public class GeneratedQuestionParser {

    private final ObjectMapper objectMapper;

    public GeneratedQuestionParser() {

        this.objectMapper =
                new ObjectMapper();
    }

    // ==========================================================
    // PARSE AI RESPONSE
    // ==========================================================

    public GeneratedQuestionResponse parse(
            String aiResponse) {

        if(aiResponse == null ||
           aiResponse.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "AI returned an empty response"
            );
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            aiResponse
                    );

            if(root == null ||
               !root.isObject()) {

                throw new IllegalArgumentException(
                        "AI response must be a JSON object"
                );
            }

            if(!root.has("questions") ||
               !root.get("questions").isArray()) {

                throw new IllegalArgumentException(
                        "AI response does not contain a valid questions array"
                );
            }

            // --------------------------------------------------
            // Validate the raw JSON structure BEFORE mapping.
            // --------------------------------------------------

            validateRawResponse(
                    root
            );

            // --------------------------------------------------
            // Convert validated JSON into DTO.
            // --------------------------------------------------

            GeneratedQuestionResponse response =
                    objectMapper.treeToValue(
                            root,
                            GeneratedQuestionResponse.class
                    );

            // --------------------------------------------------
            // Validate DTO-level fields too.
            // --------------------------------------------------

            validate(
                    response
            );

            return response;

        } catch(Exception exception) {

            if(exception instanceof
                    IllegalArgumentException) {

                throw (IllegalArgumentException)
                        exception;
            }

            throw new IllegalArgumentException(
                    "Failed to parse AI-generated questions: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    // ==========================================================
    // VALIDATE RAW JSON RESPONSE
    // ==========================================================

    private void validateRawResponse(
            JsonNode root) {

        JsonNode questions =
                root.get("questions");

        if(questions == null ||
           !questions.isArray() ||
           questions.isEmpty()) {

            throw new IllegalArgumentException(
                    "AI generated no questions"
            );
        }

        for(int i = 0;
            i < questions.size();
            i++) {

            JsonNode question =
                    questions.get(i);

            validateRawQuestion(
                    question,
                    i
            );
        }
    }

    // ==========================================================
    // VALIDATE RAW QUESTION
    // ==========================================================

    private void validateRawQuestion(
            JsonNode question,
            int index) {

        if(question == null ||
           !question.isObject()) {

            throw new IllegalArgumentException(
                    "Generated question at index "
                            + index
                            + " is not a valid object"
            );
        }

        // ------------------------------------------------------
        // Question text
        // ------------------------------------------------------

        validateRequiredTextField(
                question,
                "questionText",
                index
        );

        // ------------------------------------------------------
        // Explanation
        // ------------------------------------------------------

        validateRequiredTextField(
                question,
                "explanation",
                index
        );

        // ------------------------------------------------------
        // Difficulty
        // ------------------------------------------------------

        validateRequiredTextField(
                question,
                "difficulty",
                index
        );

        // ------------------------------------------------------
        // Question type
        // ------------------------------------------------------

        validateRequiredTextField(
                question,
                "questionType",
                index
        );

        // ------------------------------------------------------
        // Options
        // ------------------------------------------------------

        JsonNode options =
                question.get("options");

        if(options == null ||
           !options.isArray()) {

            throw new IllegalArgumentException(
                    "Generated question at index "
                            + index
                            + " does not contain a valid options array"
            );
        }

        if(options.size() != 4) {

            throw new IllegalArgumentException(
                    "Generated question at index "
                            + index
                            + " must contain exactly 4 options"
            );
        }

        // ------------------------------------------------------
        // Validate all four options.
        // ------------------------------------------------------

        int correctCount = 0;

        for(int optionIndex = 0;
            optionIndex < options.size();
            optionIndex++) {

            JsonNode option =
                    options.get(optionIndex);

            if(option == null ||
               !option.isObject()) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + index
                                + " has an invalid option at position "
                                + optionIndex
                );
            }

            // --------------------------------------------------
            // Option label
            // --------------------------------------------------

            String optionLabel =
                    getRequiredText(
                            option,
                            "optionLabel"
                    );

            if(optionLabel == null) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + index
                                + " has an option without optionLabel"
                );
            }

            // --------------------------------------------------
            // Option text
            // --------------------------------------------------

            String optionText =
                    getRequiredText(
                            option,
                            "optionText"
                    );

            if(optionText == null) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + index
                                + " has an option without optionText"
                );
            }

            // --------------------------------------------------
            // Correct flag
            // --------------------------------------------------

            JsonNode correct =
                    option.get("correct");

            if(correct == null ||
               !correct.isBoolean()) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + index
                                + " has an option with invalid correct flag"
                );
            }

            if(correct.booleanValue()) {

                correctCount++;
            }
        }

        // ------------------------------------------------------
        // EXACTLY ONE correct answer.
        // ------------------------------------------------------

        if(correctCount != 1) {

            throw new IllegalArgumentException(
                    "Generated question at index "
                            + index
                            + " must have exactly one correct option, "
                            + "but found "
                            + correctCount
            );
        }

        // ------------------------------------------------------
        // Option labels must be unique.
        // ------------------------------------------------------

        validateUniqueOptionLabels(
                options,
                index
        );
    }

    // ==========================================================
    // VALIDATE OPTION LABELS
    // ==========================================================

    private void validateUniqueOptionLabels(
            JsonNode options,
            int questionIndex) {

        for(int i = 0;
            i < options.size();
            i++) {

            String currentLabel =
                    options.get(i)
                            .get("optionLabel")
                            .asText()
                            .trim();

            for(int j = i + 1;
                j < options.size();
                j++) {

                String otherLabel =
                        options.get(j)
                                .get("optionLabel")
                                .asText()
                                .trim();

                if(currentLabel.equalsIgnoreCase(
                        otherLabel)) {

                    throw new IllegalArgumentException(
                            "Generated question at index "
                                    + questionIndex
                                    + " contains duplicate option label: "
                                    + currentLabel
                    );
                }
            }
        }
    }

    // ==========================================================
    // REQUIRED TEXT FIELD
    // ==========================================================

    private void validateRequiredTextField(
            JsonNode object,
            String fieldName,
            int questionIndex) {

        String value =
                getRequiredText(
                        object,
                        fieldName
                );

        if(value == null) {

            throw new IllegalArgumentException(
                    "Generated question at index "
                            + questionIndex
                            + " has no "
                            + fieldName
            );
        }
    }

    // ==========================================================
    // GET REQUIRED TEXT
    // ==========================================================

    private String getRequiredText(
            JsonNode object,
            String fieldName) {

        JsonNode value =
                object.get(fieldName);

        if(value == null ||
           !value.isTextual()) {

            return null;
        }

        String text =
                value.asText().trim();

        if(text.isEmpty()) {

            return null;
        }

        return text;
    }

    // ==========================================================
    // DTO VALIDATION
    // ==========================================================

    private void validate(
            GeneratedQuestionResponse response) {

        if(response == null ||
           response.getQuestions() == null ||
           response.getQuestions().isEmpty()) {

            throw new IllegalArgumentException(
                    "AI generated no questions"
            );
        }

        for(int i = 0;
            i < response.getQuestions().size();
            i++) {

            AdminQuestionRequest question =
                    response.getQuestions().get(i);

            if(question == null) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + i
                                + " is null"
                );
            }

            if(question.getQuestionText() == null ||
               question.getQuestionText()
                       .trim()
                       .isEmpty()) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + i
                                + " has no question text"
                );
            }

            if(question.getExplanation() == null ||
               question.getExplanation()
                       .trim()
                       .isEmpty()) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + i
                                + " has no explanation"
                );
            }

            if(question.getDifficulty() == null) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + i
                                + " has no difficulty"
                );
            }

            if(question.getQuestionType() == null) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + i
                                + " has no question type"
                );
            }

            if(question.getOptions() == null ||
               question.getOptions().size() != 4) {

                throw new IllegalArgumentException(
                        "Generated question at index "
                                + i
                                + " must contain exactly 4 options"
                );
            }
        }
    }
}