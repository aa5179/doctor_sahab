package com.emulsify.ocrweaviate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Main Spring Boot application class for OCRWeaviate Backend
 * 
 * This application provides:
 * - PDF document upload and OCR processing
 * - Vector embedding storage in Weaviate
 * - Semantic search and query processing
 * - AI-powered responses using Gemini API
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class OcrWeaviateApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcrWeaviateApplication.class, args);
        System.out.println("🚀 OCRWeaviate Backend started successfully!");
        System.out.println("📄 Ready to process PDF documents with OCR");
        System.out.println("🔍 Weaviate vector search enabled");
        System.out.println("🤖 Gemini AI integration active");
    }

    /**
     * RestTemplate bean for making HTTP requests to external APIs
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}