package com.emulsify.ocrweaviate.model;

import java.util.Map;

/**
 * Model for Weaviate document storage
 */
public class WeaviateDocument {
    private String content;
    private String source;

    public WeaviateDocument() {}

    public WeaviateDocument(String content, String source) {
        this.content = content;
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Convert to Map for Weaviate API
     */
    public Map<String, Object> toMap() {
        return Map.of(
            "content", content,
            "source", source
        );
    }
}