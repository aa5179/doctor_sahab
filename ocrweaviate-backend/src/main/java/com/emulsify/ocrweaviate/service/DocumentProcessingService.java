package com.emulsify.ocrweaviate.service;

import com.emulsify.ocrweaviate.model.QueryResponse;
import com.emulsify.ocrweaviate.model.UploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main service for document processing workflow
 */
@Service
public class DocumentProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);
    
    private final OCRService ocrService;
    private final WeaviateService weaviateService;
    private final GeminiService geminiService;
    
    @Value("${text.chunk.size:500}")
    private int chunkSize;
    
    @Value("${text.chunk.overlap:50}")
    private int chunkOverlap;
    
    @Value("${text.min.chunk.length:30}")
    private int minChunkLength;

    @Autowired
    public DocumentProcessingService(OCRService ocrService, WeaviateService weaviateService, GeminiService geminiService) {
        this.ocrService = ocrService;
        this.weaviateService = weaviateService;
        this.geminiService = geminiService;
    }

    /**
     * Extract basic text from PDF without OCR (for text-based PDFs)
     */
    public String extractBasicTextFromPDF(MultipartFile file) {
        try {
            logger.info("📝 Attempting basic text extraction from: {}", file.getOriginalFilename());
            
            // Create temp file
            java.io.File tempFile = java.io.File.createTempFile("text_extract_", ".pdf");
            file.transferTo(tempFile);
            
            try {
                // Use PDFBox to extract text directly
                org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(tempFile);
                org.apache.pdfbox.text.PDFTextStripper textStripper = new org.apache.pdfbox.text.PDFTextStripper();
                String extractedText = textStripper.getText(document);
                document.close();
                
                if (extractedText != null && !extractedText.trim().isEmpty()) {
                    logger.info("✅ Basic text extraction successful: {} characters", extractedText.length());
                    return extractedText.trim();
                } else {
                    logger.warn("⚠️ No text found - PDF may contain only images");
                    return "";
                }
                
            } finally {
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
            
        } catch (Exception e) {
            logger.error("❌ Basic text extraction failed: {}", e.getMessage());
            return "Error extracting text: " + e.getMessage();
        }
    }
    
    /**
     * Extract text from image files using OCR
     */
    public String extractTextFromImage(MultipartFile file) {
        try {
            logger.info("🖼️ Attempting OCR text extraction from: {}", file.getOriginalFilename());
            
            String extractedText = ocrService.extractTextFromImage(file);
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                logger.info("✅ Image text extraction successful: {} characters", extractedText.length());
                return extractedText.trim();
            } else {
                logger.warn("⚠️ No text found in image");
                return "";
            }
            
        } catch (Exception e) {
            logger.error("❌ Image text extraction failed: {}", e.getMessage());
            return "Unable to extract text from image: " + e.getMessage();
        }
    }

    /**
     * Process uploaded PDF documents
     */
    public UploadResponse processDocuments(List<MultipartFile> files) {
        logger.info("📁 Processing {} document(s)", files.size());
        
        List<UploadResponse.DocumentInfo> processedDocs = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                UploadResponse.DocumentInfo docInfo = processSingleDocument(file);
                processedDocs.add(docInfo);
                
            } catch (Exception e) {
                logger.error("❌ Failed to process {}: {}", file.getOriginalFilename(), e.getMessage());
                throw new RuntimeException("Failed to process " + file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        return new UploadResponse(
            String.format("Successfully processed %d document(s)", processedDocs.size()),
            processedDocs,
            processedDocs.size()
        );
    }

    private UploadResponse.DocumentInfo processSingleDocument(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        logger.info("📄 Processing document: {}", filename);
        
        // Validate file type
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        
        String lowerFilename = filename.toLowerCase();
        boolean isPDF = lowerFilename.endsWith(".pdf");
        boolean isImage = lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg") || 
                         lowerFilename.endsWith(".png") || lowerFilename.endsWith(".bmp") || 
                         lowerFilename.endsWith(".tiff") || lowerFilename.endsWith(".tif");
        
        if (!isPDF && !isImage) {
            throw new IllegalArgumentException("Unsupported file type: " + filename + ". Supported formats: PDF, JPG, JPEG, PNG, BMP, TIFF");
        }
        
        // Extract text based on file type
        String extractedText;
        if (isPDF) {
            logger.info("📄 Processing PDF document: {}", filename);
            extractedText = ocrService.extractTextFromPDF(file);
        } else {
            logger.info("🖼️ Processing image document: {}", filename);
            extractedText = ocrService.extractTextFromImage(file);
        }
        
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new RuntimeException("No text could be extracted from: " + filename);
        }
        
        // Split into chunks
        List<String> chunks = ocrService.splitTextIntoChunks(extractedText, chunkSize, chunkOverlap);
        
        // Filter chunks by minimum length
        chunks = chunks.stream()
                .filter(chunk -> chunk.length() >= minChunkLength)
                .collect(Collectors.toList());
        
        if (chunks.isEmpty()) {
            throw new RuntimeException("No valid text chunks could be created from: " + filename);
        }
        
        // Store in Weaviate
        int storedChunks = weaviateService.storeDocumentChunks(chunks, filename);
        
        if (storedChunks == 0) {
            throw new RuntimeException("Failed to store any chunks for: " + filename);
        }
        
        logger.info("✅ Successfully processed {}: {} chunks stored", filename, storedChunks);
        
        return new UploadResponse.DocumentInfo(filename, extractedText, storedChunks, extractedText.length());
    }

    /**
     * Query documents and generate AI response
     */
    public QueryResponse queryDocuments(String query) {
        logger.info("🔍 Processing query: {}", query.substring(0, Math.min(100, query.length())));
        
        // Search for relevant documents
        List<Map<String, Object>> searchResults = weaviateService.searchDocuments(query, 5);
        
        if (searchResults.isEmpty()) {
            return new QueryResponse(
                "No relevant information found",
                "I couldn't find any information in your uploaded documents that relates to your question. Please make sure you've uploaded PDF documents and try again.",
                null,
                null
            );
        }
        
        // Group results by source for cross-document analysis
        Map<String, List<String>> sourceContent = groupResultsBySource(searchResults);
        List<String> sources = new ArrayList<>(sourceContent.keySet());
        List<String> context = prepareContext(sourceContent);
        
        // Generate AI response
        String aiResponse = geminiService.generateResponse(query, context, sources);
        Map<String, String> parsedResponse = geminiService.parseFormattedResponse(aiResponse);
        
        // Build final response
        QueryResponse response = new QueryResponse(
            parsedResponse.get("answer"),
            parsedResponse.get("explanation"),
            sources,
            sources.size() > 1 ? parsedResponse.get("cross_document_analysis") : null
        );
        
        logger.info("✅ Query processed successfully. Found {} sources", sources.size());
        return response;
    }

    private Map<String, List<String>> groupResultsBySource(List<Map<String, Object>> results) {
        Map<String, List<String>> sourceContent = new LinkedHashMap<>();
        
        for (Map<String, Object> result : results) {
            String content = (String) result.get("content");
            String source = (String) result.get("source");
            
            if (content != null && source != null) {
                sourceContent.computeIfAbsent(source, k -> new ArrayList<>()).add(content);
            }
        }
        
        return sourceContent;
    }

    private List<String> prepareContext(Map<String, List<String>> sourceContent) {
        List<String> context = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : sourceContent.entrySet()) {
            String source = entry.getKey();
            List<String> contents = entry.getValue();
            
            // Take the most relevant (first) chunk from each document
            String bestContent = contents.isEmpty() ? "" : contents.get(0);
            context.add("From " + source + ": " + bestContent);
        }
        
        return context;
    }

    /**
     * Get all uploaded documents
     */
    public Map<String, Object> getAllDocuments() {
        // This would require additional Weaviate API calls to list all documents
        // For now, return a simple response
        return Map.of(
            "message", "Use specific endpoints to query documents",
            "available_operations", Arrays.asList(
                "POST /upload - Upload documents",
                "POST /query - Query documents",
                "DELETE /documents - Clear all documents"
            )
        );
    }

    /**
     * Clear all documents
     */
    public Map<String, Object> clearAllDocuments() {
        boolean success = weaviateService.clearAllDocuments();
        
        if (success) {
            logger.info("✅ All documents cleared successfully");
            return Map.of("message", "All documents cleared successfully");
        } else {
            logger.error("❌ Failed to clear documents");
            throw new RuntimeException("Failed to clear documents");
        }
    }

    /**
     * Health check for all services
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Check OCR
        boolean ocrAvailable = ocrService.isOCRAvailable();
        status.put("ocr", ocrAvailable ? "available" : "unavailable");
        
        // Check Weaviate
        boolean weaviateConnected = weaviateService.testConnection();
        status.put("weaviate", weaviateConnected ? "connected" : "disconnected");
        
        // Check Gemini
        boolean geminiConnected = geminiService.testConnection();
        status.put("gemini", geminiConnected ? "connected" : "disconnected");
        
        // Overall status
        boolean allHealthy = ocrAvailable && weaviateConnected && geminiConnected;
        status.put("status", allHealthy ? "healthy" : "degraded");
        status.put("message", "OCRWeaviate Backend");
        
        return status;
    }
    
    /**
     * Generate AI response for given query and context
     */
    public String generateAIResponse(String query, String context) {
        try {
            logger.info("🤖 Generating AI response for query: {}", query.substring(0, Math.min(50, query.length())));
            
            // Convert string context to List<String> and create empty sources list
            List<String> contextList = Arrays.asList(context);
            List<String> sources = Arrays.asList();
            
            // Use Gemini service to generate response
            String response = geminiService.generateResponse(query, contextList, sources);
            
            if (response != null && !response.trim().isEmpty()) {
                logger.info("✅ AI response generated: {} characters", response.length());
                return response;
            } else {
                logger.warn("⚠️ AI response was empty, using fallback");
                return generateFallbackResponse(query, context);
            }
            
        } catch (Exception e) {
            logger.error("❌ AI response generation failed: {}", e.getMessage());
            return generateFallbackResponse(query, context);
        }
    }
    
    /**
     * Generate fallback response when AI is unavailable
     */
    private String generateFallbackResponse(String query, String context) {
        logger.info("📝 Generating fallback response...");
        
        // If query is about prescription analysis, provide structured fallback
        if (query.toLowerCase().contains("prescription") || query.toLowerCase().contains("medicine")) {
            return String.format("""
                **Prescription Analysis (Fallback Mode)**
                
                **Extracted Content:**
                %s
                
                **Analysis Request:** %s
                
                **Note:** AI processing is currently unavailable. Please review the extracted text above for:
                - Medicine names and dosages
                - Frequency and duration instructions  
                - Doctor information
                - Special warnings or notes
                
                **Status:** Manual review required - AI analysis offline
                """, 
                context.length() > 500 ? context.substring(0, 500) + "..." : context,
                query
            );
        } else {
            return String.format("""
                **Analysis Result (Fallback Mode)**
                
                **Query:** %s
                
                **Content:** %s
                
                **Note:** AI processing is currently unavailable. The extracted content is provided above for manual review.
                """,
                query,
                context.length() > 500 ? context.substring(0, 500) + "..." : context
            );
        }
    }
}