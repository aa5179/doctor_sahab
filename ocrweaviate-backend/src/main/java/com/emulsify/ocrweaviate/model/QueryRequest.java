package com.emulsify.ocrweaviate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request model for query operations
 */
public class QueryRequest {
    
    @NotBlank(message = "Query cannot be empty")
    private String query;

    public QueryRequest() {}

    public QueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}