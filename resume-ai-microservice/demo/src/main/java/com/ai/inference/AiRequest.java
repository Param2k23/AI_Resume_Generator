package com.ai.inference;

/**
 * Data Transfer Object (DTO) for the incoming JSON request body.
 * This class represents the data the client sends to the /generate endpoint.
 *
 * @param userDescription The raw text provided by the user describing their job history, skills, etc.
 */
public record AiRequest(String userDescription) {
    // This is a Java Record (since Java 16), which automatically provides
    // a constructor, getters, equals(), hashCode(), and toString()
}