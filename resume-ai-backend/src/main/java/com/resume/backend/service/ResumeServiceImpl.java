package com.resume.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    // 1. Define the URL for the external AI Microservice
    @Value("${ai.service.url:http://localhost:8081/api/ai/generate}")
    private String aiServiceUrl;

    private final WebClient webClient;

    public ResumeServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
    }

    @Override
    public   Map<String, Object> generateResumeResponse(String userResumeDescription) throws IOException {
        Map<String, String> requestBody = Map.of("userDescription", userResumeDescription);
        String response = webClient.post()
                .uri("") // The base URL is already set in the WebClient
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        Map<String, Object> stringObjectMap = parseMultipleResponses(response);
        return stringObjectMap;
    }


    // String loadPromptFromFile(String filename) throws IOException {
    //     try (var inputStream = new ClassPathResource(filename).getInputStream()) {
    //         return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    //     }
    // }

    // String putValuesToTemplate(String template, Map<String, String> values) {
    //     for (Map.Entry<String, String> entry : values.entrySet()) {

    //         template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());

    //     }
    //     return template;
    // }


    public static Map<String, Object> parseMultipleResponses(String response) {
        Map<String, Object> jsonResponse = new HashMap<>();

        // Extract content inside <think> tags
        int thinkStart = response.indexOf("<think>") + 7;
        int thinkEnd = response.indexOf("</think>");
        if (thinkStart != -1 && thinkEnd != -1) {
            String thinkContent = response.substring(thinkStart, thinkEnd).trim();
            jsonResponse.put("think", thinkContent);
        } else {
            jsonResponse.put("think", null); // Handle missing <think> tags
        }

        // Extract content that is in JSON format
        int jsonStart = response.indexOf("```json") + 7; // Start after ```json
        int jsonEnd = response.lastIndexOf("```");       // End before ```
        if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
            String jsonContent = response.substring(jsonStart, jsonEnd).trim();
            try {
                // Convert JSON string to Map using Jackson ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> dataContent = objectMapper.readValue(jsonContent, Map.class);
                jsonResponse.put("data", dataContent);
            } catch (Exception e) {
                jsonResponse.put("data", null); // Handle invalid JSON
                System.err.println("Invalid JSON format in the response: " + e.getMessage());
            }
        } else {
            jsonResponse.put("data", null); // Handle missing JSON
        }

        return jsonResponse;
    }
}


