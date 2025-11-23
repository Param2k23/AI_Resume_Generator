package com.ai.inference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the AI Inference Microservice.
 * This service is responsible ONLY for handling LLM calls via Ollama.
 * It must be run on a separate port (e.g., 8081) from the main backend.
 */
@SpringBootApplication
public class AiInferenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiInferenceApplication.class, args);
    }
}