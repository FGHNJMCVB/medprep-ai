package com.medprep.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medprep.dto.QuestionGenerationRequest;

import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@Primary
public class GeminiQuestionGenerator
        implements AiQuestionGenerator {

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    private final String apiKey;

    private final String model;

    public GeminiQuestionGenerator(
            Environment environment) {

        this.objectMapper = new ObjectMapper();

        this.apiKey = environment.getProperty(
                "gemini.api-key",
                ""
        );

        this.model = environment.getProperty(
                "gemini.model",
                "gemini-3.6-flash"
        );

        this.webClient = WebClient.builder()
                .baseUrl(
                        "https://generativelanguage.googleapis.com/v1beta"
                )
                .build();
    }

    @Override
    public String generateQuestions(
            QuestionGenerationRequest request,
            String promptContext) {

        if (apiKey.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Gemini API key is not configured. "
                            + "Set GEMINI_API_KEY before using "
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

        // ------------------------------------------------------
        // contents
        // ------------------------------------------------------

        Map<String, Object> textPart =
                new HashMap<>();

        textPart.put(
                "text",
                prompt
        );

        Map<String, Object> content =
                new HashMap<>();

        content.put(
                "parts",
                new Object[]{
                        textPart
                }
        );

        requestBody.put(
                "contents",
                new Object[]{
                        content
                }
        );

        // ------------------------------------------------------
        // generationConfig
        // ------------------------------------------------------

        Map<String, Object> generationConfig =
                new HashMap<>();

        generationConfig.put(
                "response_mime_type",
                "application/json"
        );

        generationConfig.put(
                "response_schema",
                buildQuestionSchema()
        );

        requestBody.put(
                "generationConfig",
                generationConfig
        );

        String response =
                webClient
                        .post()
                        .uri(uriBuilder -> uriBuilder
                                .path(
                                        "/models/"
                                                + model
                                                + ":generateContent"
                                )
                                .queryParam(
                                        "key",
                                        apiKey
                                )
                                .build()
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .bodyValue(requestBody)
                        .exchangeToMono(
                                clientResponse ->
                                        clientResponse
                                                .bodyToMono(
                                                        String.class
                                                )
                                                .flatMap(
                                                        body -> {

                                                            if (clientResponse
                                                                    .statusCode()
                                                                    .isError()) {

                                                                return reactor.core.publisher.Mono
                                                                        .error(
                                                                                new IllegalStateException(
                                                                                        "Gemini API returned "
                                                                                                + clientResponse.statusCode()
                                                                                                + ": "
                                                                                                + body
                                                                                )
                                                                        );
                                                            }

                                                            return reactor.core.publisher.Mono
                                                                    .just(body);
                                                        }
                                                )
                        )
                        .block();

        return extractGeneratedText(
                response
        );
    }

    // ==========================================================
    // GEMINI STRUCTURED JSON SCHEMA
    // ==========================================================

    private Map<String, Object> buildQuestionSchema() {

        Map<String, Object> optionSchema =
                new HashMap<>();

        optionSchema.put(
                "type",
                "OBJECT"
        );

        Map<String, Object> optionProperties =
                new HashMap<>();

        optionProperties.put(
                "optionLabel",
                Map.of(
                        "type",
                        "STRING",
                        "enum",
                        new String[]{
                                "A",
                                "B",
                                "C",
                                "D"
                        }
                )
        );

        optionProperties.put(
                "optionText",
                Map.of(
                        "type",
                        "STRING"
                )
        );

        optionProperties.put(
                "displayOrder",
                Map.of(
                        "type",
                        "INTEGER"
                )
        );

        optionProperties.put(
                "correct",
                Map.of(
                        "type",
                        "BOOLEAN"
                )
        );

        optionSchema.put(
                "properties",
                optionProperties
        );

        optionSchema.put(
                "required",
                new String[]{
                        "optionLabel",
                        "optionText",
                        "displayOrder",
                        "correct"
                }
        );

        // ------------------------------------------------------
        // Question schema
        // ------------------------------------------------------

        Map<String, Object> questionSchema =
                new HashMap<>();

        questionSchema.put(
                "type",
                "OBJECT"
        );

        Map<String, Object> questionProperties =
                new HashMap<>();

        questionProperties.put(
                "questionText",
                Map.of(
                        "type",
                        "STRING"
                )
        );

        questionProperties.put(
                "explanation",
                Map.of(
                        "type",
                        "STRING"
                )
        );

        questionProperties.put(
                "difficulty",
                Map.of(
                        "type",
                        "STRING",
                        "enum",
                        new String[]{
                                "EASY",
                                "MEDIUM",
                                "HARD"
                        }
                )
        );

        questionProperties.put(
                "questionType",
                Map.of(
                        "type",
                        "STRING",
                        "enum",
                        new String[]{
                                "ORIGINAL",
                                "PYQ_INSPIRED",
                                "MEMORY_BASED_RECALL"
                        }
                )
        );

        questionProperties.put(
                "conceptTag",
                Map.of(
                        "type",
                        "STRING"
                )
        );

        questionProperties.put(
                "sourceYear",
                Map.of(
                        "type",
                        "STRING"
                )
        );

        questionProperties.put(
                "sourceSession",
                Map.of(
                        "type",
                        "STRING"
                )
        );

        questionProperties.put(
                "sourceReference",
                Map.of(
                        "type",
                        "STRING"
                )
        );

        questionProperties.put(
                "highYield",
                Map.of(
                        "type",
                        "BOOLEAN"
                )
        );

        questionProperties.put(
                "clinicalCase",
                Map.of(
                        "type",
                        "BOOLEAN"
                )
        );

        questionProperties.put(
                "imageBased",
                Map.of(
                        "type",
                        "BOOLEAN"
                )
        );

        questionProperties.put(
                "options",
                Map.of(
                        "type",
                        "ARRAY",
                        "items",
                        optionSchema
                )
        );

        questionSchema.put(
                "properties",
                questionProperties
        );

        questionSchema.put(
                "required",
                new String[]{
                        "questionText",
                        "explanation",
                        "difficulty",
                        "questionType",
                        "conceptTag",
                        "sourceYear",
                        "sourceSession",
                        "sourceReference",
                        "highYield",
                        "clinicalCase",
                        "imageBased",
                        "options"
                }
        );

        // ------------------------------------------------------
        // Root schema
        // ------------------------------------------------------

        Map<String, Object> rootSchema =
                new HashMap<>();

        rootSchema.put(
                "type",
                "OBJECT"
        );

        rootSchema.put(
                "properties",
                Map.of(
                        "questions",
                        Map.of(
                                "type",
                                "ARRAY",
                                "items",
                                questionSchema
                        )
                )
        );

        rootSchema.put(
                "required",
                new String[]{
                        "questions"
                }
        );

        return rootSchema;
    }

    // ==========================================================
    // EXTRACT GENERATED JSON
    // ==========================================================

    private String extractGeneratedText(
            String response) {

        try {

            JsonNode root =
                    objectMapper.readTree(
                            response
                    );

            JsonNode candidates =
                    root.path(
                            "candidates"
                    );

            if (!candidates.isArray() ||
                    candidates.isEmpty()) {

                throw new IllegalStateException(
                        "Gemini returned no candidates"
                );
            }

            JsonNode text =
                    candidates
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text");

            if (text.isMissingNode() ||
                    text.isNull() ||
                    text.asText()
                            .trim()
                            .isEmpty()) {

                throw new IllegalStateException(
                        "Gemini returned no generated text"
                );
            }

            return text.asText();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to parse Gemini response: "
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

        // ======================================================
        // ROLE
        // ======================================================

        prompt.append(
                "You are an FMGE medical education question generator."
        );

        prompt.append(
                "\nGenerate high-quality ORIGINAL multiple-choice "
                        + "questions for FMGE preparation."
        );

        prompt.append(
                "\nDo not copy recalled or historical question wording."
        );

        prompt.append(
                "\nThe goal is exam preparation, not prediction "
                        + "of exact future questions."
        );

        // ======================================================
        // FUNDAMENTAL QUESTION RULES
        // ======================================================

        prompt.append(
                "\nEvery question must have exactly four options."
        );

        prompt.append(
                "\nExactly one option must be correct."
        );

        prompt.append(
                "\nThe explanation must be medically accurate "
                        + "and directly justify the correct answer."
        );

        prompt.append(
                "\nDo not invent official NBEMS/PYQ claims."
        );

        prompt.append(
                "\nIf inspired by a recurring concept, mark "
                        + "questionType as PYQ_INSPIRED."
        );

        prompt.append(
                "\nIf it is an original conceptual question, mark "
                        + "questionType as ORIGINAL."
        );

        // ======================================================
        // AUTHORITATIVE SUBJECT / TOPIC SCOPE
        // ======================================================

        prompt.append(
                "\n\n=========================================================="
        );

        prompt.append(
                "\nAUTHORITATIVE SUBJECT / TOPIC SCOPE"
        );

        prompt.append(
                "\n=========================================================="
        );

        prompt.append(
                "\nRequested subject ID: "
                        + request.getSubjectId()
        );

        prompt.append(
                "\nRequested topic ID: "
                        + request.getTopicId()
        );

        prompt.append(
                "\nGeneration focus: "
                        + request.getFocus()
        );

        prompt.append(
                "\n\nThe database-resolved subject and topic "
                        + "provided in the generation context below "
                        + "are authoritative."
        );

        prompt.append(
                "\nUse the database-resolved topic NAME as the "
                        + "actual content boundary."
        );

        prompt.append(
                "\nNever infer the topic meaning from the numeric ID."
        );

        prompt.append(
                "\nNever replace the database-resolved topic with "
                        + "a related topic from the same subject."
        );

        prompt.append(
                "\nNever broaden a topic request into the whole subject."
        );

        prompt.append(
                "\nEvery generated question must primarily test "
                        + "the database-resolved topic."
        );

        prompt.append(
                "\nThe clinical scenario may mention another organ, "
                        + "disease, structure, or body region when "
                        + "clinically necessary, but the answer must "
                        + "depend primarily on the requested topic."
        );

        // ======================================================
        // DATABASE SCOPE CONTEXT
        // ======================================================

        prompt.append(
                "\n\nThe following context contains the "
                        + "database-resolved generation scope and "
                        + "supporting trend information:"
        );

        prompt.append(
                promptContext
        );

        // ======================================================
        // TOPIC EXAMPLE
        // ======================================================

        prompt.append(
                "\n\nFor example, when the database-resolved "
                        + "topic is \"General Anatomy\":"
        );

        prompt.append(
                "\n- Test general anatomical principles."
        );

        prompt.append(
                "\n- Acceptable areas include terminology, "
                        + "anatomical planes, positions, movements, "
                        + "tissue basics, connective tissue, cartilage, "
                        + "general bone structure, ossification, "
                        + "epiphyses, general joint classification, "
                        + "general muscle architecture, fascia, skin, "
                        + "and general vascular/lymphatic principles."
        );

        prompt.append(
                "\n- Do NOT make Upper Limb, Lower Limb, Thorax, "
                        + "Abdomen, Pelvis, Head and Neck, "
                        + "Neuroanatomy, or Embryology the primary "
                        + "topic when General Anatomy is requested."
        );

        prompt.append(
                "\n- If a more specific anatomical topic becomes "
                        + "the main knowledge required to answer the "
                        + "question, the question is considered OFF-TOPIC."
        );

        // ======================================================
        // DIFFICULTY
        // ======================================================

        prompt.append(
                "\n\nRequested number of questions: "
                        + request.getCount()
        );

        prompt.append(
                "\nRequested difficulty: "
                        + request.getDifficulty()
        );

        prompt.append(
                "\nEvery generated question MUST use exactly "
                        + "the requested difficulty."
        );

        // ======================================================
        // QUESTION CHARACTERISTICS
        // ======================================================

        prompt.append(
                "\nHigh yield: "
                        + request.getHighYield()
        );

        prompt.append(
                "\nClinical case preference: "
                        + request.getClinicalCase()
        );

        prompt.append(
                "\nImage based preference: "
                        + request.getImageBased()
        );

        // ======================================================
        // QUESTION DIVERSITY
        // ======================================================

        prompt.append(
                "\n\nEvery question must test a distinct clinical "
                        + "or conceptual angle."
        );

        prompt.append(
                "\nDo not generate two questions that test the "
                        + "same fact using slightly different wording."
        );

        prompt.append(
                "\nAvoid repeating the same diagnosis, mechanism, "
                        + "nerve, drug, investigation, anatomical fact, "
                        + "or management fact within the same batch."
        );

        prompt.append(
                "\nUse different concepts within the requested "
                        + "topic whenever enough concepts are available."
        );

        // ======================================================
        // TREND RULE
        // ======================================================

        prompt.append(
                "\n\nTrend information is supportive only."
        );

        prompt.append(
                "\nIt may help prioritize high-yield concepts."
        );

        prompt.append(
                "\nIt must NEVER override the database-resolved "
                        + "subject/topic boundary."
        );

        prompt.append(
                "\nIf trend information refers to another topic, "
                        + "ignore it for this generation request."
        );

        // ======================================================
        // FINAL SELF-CHECK
        // ======================================================

        prompt.append(
                "\n\nBefore returning each question, internally verify:"
        );

        prompt.append(
                "\n1. Is the primary tested concept inside the "
                        + "database-resolved requested topic?"
        );

        prompt.append(
                "\n2. Is the requested difficulty respected?"
        );

        prompt.append(
                "\n3. Is exactly one option correct?"
        );

        prompt.append(
                "\n4. Is the question meaningfully different "
                        + "from the other questions in this batch?"
        );

        prompt.append(
                "\n5. Would this question still belong to the "
                        + "requested topic if the clinical scenario "
                        + "were removed?"
        );

        prompt.append(
                "\n6. Does the question primarily test the exact "
                        + "database-resolved topic name?"
        );

        prompt.append(
                "\nIf the answer to the topic-scope check is NO, "
                        + "replace that question with a different "
                        + "question before returning the JSON."
        );

        return prompt.toString();
    }
}