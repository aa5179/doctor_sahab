package com.emulsify.ocrweaviate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response model for document upload operations
 */
public class UploadResponse {
    private String message;
    private List<DocumentInfo> documents;
    
    @JsonProperty("total_documents")
    private int totalDocuments;

    public UploadResponse() {}

    public UploadResponse(String message, List<DocumentInfo> documents, int totalDocuments) {
        this.message = message;
        this.documents = documents;
        this.totalDocuments = totalDocuments;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DocumentInfo> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentInfo> documents) {
        this.documents = documents;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(int totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    /**
     * Document information model
     */
    public static class DocumentInfo {
        private String filename;
        private String content; // Added content field for extracted text
        
        @JsonProperty("chunks_created")
        private int chunksCreated;
        
        @JsonProperty("text_length")
        private int textLength;

        public DocumentInfo() {}

        public DocumentInfo(String filename, int chunksCreated, int textLength) {
            this.filename = filename;
            this.chunksCreated = chunksCreated;
            this.textLength = textLength;
        }

        public DocumentInfo(String filename, String content, int chunksCreated, int textLength) {
            this.filename = filename;
            this.content = content;
            this.chunksCreated = chunksCreated;
            this.textLength = textLength;
        }

        // Getters and setters
        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getChunksCreated() {
            return chunksCreated;
        }

        public void setChunksCreated(int chunksCreated) {
            this.chunksCreated = chunksCreated;
        }

        public int getTextLength() {
            return textLength;
        }

        public void setTextLength(int textLength) {
            this.textLength = textLength;
        }
    }
}