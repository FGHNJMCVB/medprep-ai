package com.medprep.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medprep.dto.QuestionGenerationRequest;

import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class OpenAiQuestionGenerator
        implements AiQuestionGenerator {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final String apiKey;
    private final String model;

    public OpenAiQuestionGenerator(
            Environment environment) {

        this.objectMapper =
                new ObjectMapper();

        this.apiKey =
                environment.getProperty(
                        "openai.api-key",
                        ""
                );

        this.model =
                environment.getProperty(
                        "openai.model",
                        "gpt-5.6-luna"
                );

        if(apiKey.trim().isEmpty()) {

            this.webClient = null;

            return;
        }

        this.webClient =
                WebClient.builder()
                        .baseUrl(
                                "https://api.openai.com/v1"
                        )
                        .defaultHeader(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .defaultHeader(
                                "Content-Type",
                                MediaType.APPLICATION_JSON_VALUE
                        )
                        .build();
    }

    // ==========================================================
    // GENERATE QUESTIONS
    // ==========================================================

    @Override
    public String generateQuestions(
            QuestionGenerationRequest request,
            String promptContext) {

        if(apiKey.trim().isEmpty()) {

            throw new IllegalStateException(
                    "OpenAI API key is not configured. "
                            + "Set OPENAI_API_KEY before using "
                            + "AI question generation."
            );
        }

        String prompt =
                buildPrompt(
                        request,
                        promptContext
                );

        Map<String, Object> requestBody =
                new HashMap<>();

        requestBody.put(
                "model",
                model
        );

        requestBody.put(
                "input",
                prompt
        );

        String response =
                webClient
                        .post()
                        .uri("/responses")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .bodyValue(
                                requestBody
                        )
                        .retrieve()
                        .bodyToMono(
                                String.class
                        )
                        .block();

        if(response == null ||
           response.trim().isEmpty()) {

            throw new IllegalStateException(
                    "OpenAI returned an empty response"
            );
        }

        return extractOutputText(
                response
        );
    }

    // ==========================================================
    // EXTRACT OPENAI OUTPUT TEXT
    // ==========================================================

    private String extractOutputText(
            String response) {

        try {

            Map<?, ?> root =
                    objectMapper.readValue(
                            response,
                            Map.class
                    );

            Object output =
                    root.get("output");

            if(!(output instanceof Iterable<?>)) {

                throw new IllegalStateException(
                        "OpenAI response did not contain output"
                );
            }

            StringBuilder text =
                    new StringBuilder();

            for(Object outputItem :
                    (Iterable<?>) output) {

                if(!(outputItem instanceof Map<?, ?>)) {
                    continue;
                }

                Map<?, ?> item =
                        (Map<?, ?>) outputItem;

                Object content =
                        item.get("content");

                if(!(content instanceof Iterable<?>)) {
                    continue;
                }

                for(Object contentItem :
                        (Iterable<?>) content) {

                    if(!(contentItem instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<?, ?> contentMap =
                            (Map<?, ?>) contentItem;

                    Object type =
                            contentMap.get("type");

                    if("output_text".equals(type)) {

                        Object itemText =
                                contentMap.get("text");

                        if(itemText != null) {

                            text.append(
                                    itemText.toString()
                            );
                        }
                    }
                }
            }

            if(text.length() == 0) {

                throw new IllegalStateException(
                        "OpenAI response contained no output text"
                );
            }

            return text.toString();

        } catch(Exception exception) {

            throw new IllegalStateException(
                    "Failed to parse OpenAI response: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    // ==========================================================
    // BUILD PROMPT
    // ==========================================================

    private String buildPrompt(
            QuestionGenerationRequest request,
            String promptContext) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append(
                "You are an FMGE medical education question generator."
        );

        prompt.append(
                "\nGenerate ORIGINAL multiple-choice questions."
        );

        prompt.append(
                "\nDo not reproduce memorized question wording."
        );

        prompt.append(
                "\nUse historical FMGE concepts and trends "
                        + "only as topic-selection signals."
        );

        prompt.append(
                "\nEvery question must have exactly four options."
        );

        prompt.append(
                "\nExactly one option must be correct."
        );

        prompt.append(
                "\nProvide a clear medically accurate explanation."
        );

        prompt.append(
                "\nReturn JSON only."
        );

        prompt.append(
                "\nDo not add markdown fences."
        );

        prompt.append(
                "\nDo not add commentary outside the JSON."
        );

        prompt.append(
                "\nDo not invent claims that a question "
                        + "was officially asked."
        );

        prompt.append(
                "\n\nRequested subject ID: "
                        + request.getSubjectId()
        );

        prompt.append(
                "\nRequested topic ID: "
                        + request.getTopicId()
        );

        prompt.append(
                "\nNumber of questions: "
                        + request.getCount()
        );

        prompt.append(
                "\nDifficulty: "
                        + request.getDifficulty()
        );

        prompt.append(
                "\nHigh yield: "
                        + request.getHighYield()
        );

        prompt.append(
                "\nClinical case: "
                        + request.getClinicalCase()
        );

        prompt.append(
                "\nImage based: "
                        + request.getImageBased()
        );

        prompt.append(
                "\nFocus: "
                        + request.getFocus()
        );

        prompt.append(
                "\n\nTREND / RESEARCH CONTEXT:\n"
        );

        prompt.append(
                promptContext
        );

        prompt.append(
                "\n\nReturn exactly this JSON shape:"
        );

        prompt.append(
                "\n{"
                        + "\"questions\":["
                        + "{"
                        + "\"questionText\":\"...\","
                        + "\"explanation\":\"...\","
                        + "\"difficulty\":\"MEDIUM\","
                        + "\"questionType\":\"PYQ_INSPIRED\","
                        + "\"conceptTag\":\"...\","
                        + "\"sourceYear\":\"...\","
                        + "\"sourceSession\":\"...\","
                        + "\"sourceReference\":\"...\","
                        + "\"highYield\":true,"
                        + "\"clinicalCase\":false,"
                        + "\"imageBased\":false,"
                        + "\"options\":["
                        + "{"
                        + "\"optionLabel\":\"A\","
                        + "\"optionText\":\"...\","
                        + "\"displayOrder\":1,"
                        + "\"correct\":false"
                        + "},"
                        + "{"
                        + "\"optionLabel\":\"B\","
                        + "\"optionText\":\"...\","
                        + "\"displayOrder\":2,"
                        + "\"correct\":true"
                        + "},"
                        + "{"
                        + "\"optionLabel\":\"C\","
                        + "\"optionText\":\"...\","
                        + "\"displayOrder\":3,"
                        + "\"correct\":false"
                        + "},"
                        + "{"
                        + "\"optionLabel\":\"D\","
                        + "\"optionText\":\"...\","
                        + "\"displayOrder\":4,"
                        + "\"correct\":false"
                        + "}"
                        + "]"
                        + "}"
                        + "]"
                        + "}"
        );

        return prompt.toString();
    }
}