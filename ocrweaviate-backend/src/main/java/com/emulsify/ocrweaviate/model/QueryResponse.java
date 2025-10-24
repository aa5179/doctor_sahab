package com.emulsify.ocrweaviate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response model for query operations
 */
public class QueryResponse {
    private String answer;
    private String explanation;
    private List<String> sources;
    
    @JsonProperty("cross_document_analysis")
    private String crossDocumentAnalysis;

    public QueryResponse() {}

    public QueryResponse(String answer, String explanation, List<String> sources, String crossDocumentAnalysis) {
        this.answer = answer;
        this.explanation = explanation;
        this.sources = sources;
        this.crossDocumentAnalysis = crossDocumentAnalysis;
    }

    // Getters and setters
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public String getCrossDocumentAnalysis() {
        return crossDocumentAnalysis;
    }

    public void setCrossDocumentAnalysis(String crossDocumentAnalysis) {
        this.crossDocumentAnalysis = crossDocumentAnalysis;
    }
}