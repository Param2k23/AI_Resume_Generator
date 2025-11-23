package com.ai.inference;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the AI Microservice.
 * Exposes a single endpoint for generating resume content.
 */
@RestController
@RequestMapping("/api/ai")
public class AiInferenceController {

    private final AiInferenceService aiInferenceService;

    // Spring Boot automatically injects the AiInferenceService instance here
    public AiInferenceController(AiInferenceService aiInferenceService) {
        this.aiInferenceService = aiInferenceService;
    }

    /**
     * Endpoint to receive the user's request and generate the resume JSON.
     *
     * @param request The DTO containing the user's description.
     * @return A raw string containing the JSON-formatted resume data from the LLM.
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateResume(@RequestBody AiRequest request) {
        if (request.userDescription() == null || request.userDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("User description cannot be empty.");
        }

        try {
            // The service handles the API call and returns the raw JSON string
            String resumeJson = aiInferenceService.generateResume(request.userDescription());
            return ResponseEntity.ok(resumeJson);
        } catch (Exception e) {
            System.err.println("Error during AI generation: " + e.getMessage());
            // Log the error and return a 500 status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("AI inference failed: " + e.getMessage());
        }
    }
}