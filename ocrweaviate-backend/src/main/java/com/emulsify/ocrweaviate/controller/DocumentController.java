package com.emulsify.ocrweaviate.controller;

import com.emulsify.ocrweaviate.model.QueryRequest;
import com.emulsify.ocrweaviate.model.QueryResponse;
import com.emulsify.ocrweaviate.model.UploadResponse;
import com.emulsify.ocrweaviate.service.DocumentProcessingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Main REST controller for OCRWeaviate backend
 * Provides endpoints for document upload, querying, and management
 */
@RestController
@RequestMapping("/")
public class DocumentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    
    private final DocumentProcessingService documentService;

    @Autowired
    public DocumentController(DocumentProcessingService documentService) {
        this.documentService = documentService;
    }
    
    /**
     * Check if file is supported (PDF or common image formats)
     */
    private boolean isSupportedFileType(String filename) {
        if (filename == null) return false;
        
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".pdf") || 
               lowerFilename.endsWith(".jpg") || 
               lowerFilename.endsWith(".jpeg") || 
               lowerFilename.endsWith(".png") || 
               lowerFilename.endsWith(".bmp") || 
               lowerFilename.endsWith(".tiff") || 
               lowerFilename.endsWith(".tif");
    }
    
    /**
     * Check if file is an image
     */
    private boolean isImageFile(String filename) {
        if (filename == null) return false;
        
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".jpg") || 
               lowerFilename.endsWith(".jpeg") || 
               lowerFilename.endsWith(".png") || 
               lowerFilename.endsWith(".bmp") || 
               lowerFilename.endsWith(".tiff") || 
               lowerFilename.endsWith(".tif");
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = Map.of(
            "message", "PDF Policy Query System",
            "status", "running",
            "version", "1.0.0"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            Map<String, Object> healthStatus = documentService.getHealthStatus();
            
            boolean isHealthy = "healthy".equals(healthStatus.get("status"));
            HttpStatus status = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            
            return ResponseEntity.status(status).body(healthStatus);
            
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Health check failed: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Simple file upload test (without OCR processing)
     */
    @PostMapping(value = "/test-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> testUpload(@RequestParam("files") List<MultipartFile> files) {
        try {
            logger.info("🧪 Test upload request with {} file(s)", files.size());
            
            if (files.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new UploadResponse("No files provided", null, 0)
                );
            }
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body(
                        new UploadResponse("Empty file detected", null, 0)
                    );
                }
                
                String filename = file.getOriginalFilename();
                logger.info("📄 Received file: {} (size: {} bytes)", filename, file.getSize());
            }
            
            // Simple success response without OCR processing
            UploadResponse response = new UploadResponse(
                "Test upload successful - OCR processing skipped",
                null,
                files.size()
            );
            
            logger.info("✅ Test upload completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Test upload failed: {}", e.getMessage());
            UploadResponse errorResponse = new UploadResponse(
                "Test upload error: " + e.getMessage(),
                null,
                0
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Text-only extraction endpoint (bypasses OCR completely)
     */
    @PostMapping(value = "/extract-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> extractTextOnly(@RequestParam("files") List<MultipartFile> files) {
        try {
            logger.info("📝 Text extraction request with {} file(s)", files.size());
            
            if (files.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new UploadResponse("No files provided", null, 0)
                );
            }
            
            // Process only the first file for now
            MultipartFile file = files.get(0);
            String filename = file.getOriginalFilename();
            
            if (!isSupportedFileType(filename)) {
                return ResponseEntity.badRequest().body(
                    new UploadResponse("Unsupported file type: " + filename + ". Supported formats: PDF, JPG, JPEG, PNG, BMP, TIFF", null, 0)
                );
            }
            
            logger.info("📄 Processing file: {} (size: {} bytes)", filename, file.getSize());
            
            // Extract text based on file type
            String extractedText;
            if (isImageFile(filename)) {
                logger.info("🖼️ Attempting image text extraction from: {}", filename);
                extractedText = documentService.extractTextFromImage(file);
            } else {
                logger.info("📄 Attempting basic text extraction from: {}", filename);
                extractedText = documentService.extractBasicTextFromPDF(file);
            }
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                // Create document info with available fields
                UploadResponse.DocumentInfo doc = new UploadResponse.DocumentInfo();
                doc.setFilename(filename);
                doc.setContent(extractedText); // Add the extracted text content
                doc.setTextLength(extractedText.length());
                doc.setChunksCreated(1); // Basic extraction creates one "chunk"
                
                // Create response with extracted text in message
                UploadResponse response = new UploadResponse(
                    "✅ Text extraction successful (no OCR):\n\n" + extractedText,
                    List.of(doc),
                    1
                );
                
                logger.info("✅ Text extraction completed: {} characters", extractedText.length());
                return ResponseEntity.ok(response);
            } else {
                UploadResponse response = new UploadResponse(
                    "⚠️ No text found in PDF - may require OCR for scanned images",
                    null,
                    0
                );
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            logger.error("❌ Text extraction failed: {}", e.getMessage());
            UploadResponse errorResponse = new UploadResponse(
                "Text extraction error: " + e.getMessage(),
                null,
                0
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Upload and process multiple PDF documents (up to 3)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadDocuments(@RequestParam("files") List<MultipartFile> files) {
        try {
            logger.info("📁 Received upload request with {} file(s)", files.size());
            
            // Validate file count
            if (files.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new UploadResponse("At least 1 file required", null, 0)
                );
            }
            
            if (files.size() > 3) {
                return ResponseEntity.badRequest().body(
                    new UploadResponse("Maximum 3 files allowed", null, 0)
                );
            }
            
            // Validate file types
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body(
                        new UploadResponse("Empty file detected", null, 0)
                    );
                }
                
                String filename = file.getOriginalFilename();
                if (!isSupportedFileType(filename)) {
                    return ResponseEntity.badRequest().body(
                        new UploadResponse("Unsupported file type: " + filename + ". Supported formats: PDF, JPG, JPEG, PNG, BMP, TIFF", null, 0)
                    );
                }
            }
            
            // Process documents
            UploadResponse response = documentService.processDocuments(files);
            
            logger.info("✅ Upload completed successfully: {} documents processed", response.getTotalDocuments());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Upload failed: {}", e.getMessage());
            UploadResponse errorResponse = new UploadResponse(
                "Processing error: " + e.getMessage(),
                null,
                0
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Upload single PDF document (backward compatibility)
     */
    @PostMapping(value = "/upload-single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadSingleDocument(@RequestParam("file") MultipartFile file) {
        try {
            logger.info("📄 Received single file upload: {}", file.getOriginalFilename());
            
            List<MultipartFile> files = List.of(file);
            UploadResponse response = documentService.processDocuments(files);
            
            // Convert to single document response format
            UploadResponse.DocumentInfo doc = response.getDocuments().get(0);
            Map<String, Object> singleResponse = Map.of(
                "message", "Successfully processed " + doc.getFilename(),
                "chunks_created", doc.getChunksCreated(),
                "text_length", doc.getTextLength()
            );
            
            return ResponseEntity.ok(singleResponse);
            
        } catch (Exception e) {
            logger.error("❌ Single upload failed: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "message", "Processing error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Query documents with cross-document analysis
     */
    @PostMapping("/query")
    public ResponseEntity<QueryResponse> queryDocuments(@Valid @RequestBody QueryRequest request) {
        try {
            logger.info("🔍 Received query: {}", request.getQuery().substring(0, Math.min(100, request.getQuery().length())));
            
            QueryResponse response = documentService.queryDocuments(request.getQuery());
            
            logger.info("✅ Query processed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Query failed: {}", e.getMessage());
            QueryResponse errorResponse = new QueryResponse(
                "Query failed",
                "An error occurred while processing your query: " + e.getMessage(),
                null,
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Ask AI to analyze content with context
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askAI(@Valid @RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            String context = request.get("context");
            
            logger.info("🤖 Received AI ask request: {}", query != null ? query.substring(0, Math.min(100, query.length())) : "null");
            
            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> errorResponse = Map.of(
                    "response", "Query cannot be empty",
                    "reasoning", List.of("No query provided"),
                    "success", false
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Use the Gemini service to generate AI response
            String aiResponse = documentService.generateAIResponse(query, context != null ? context : "");
            
            Map<String, Object> response = Map.of(
                "response", aiResponse,
                "reasoning", List.of("AI analysis completed", "Used provided context", "Generated structured response"),
                "success", true
            );
            
            logger.info("✅ AI ask processed successfully: {} characters", aiResponse.length());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ AI ask failed: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "response", "AI analysis failed: " + e.getMessage(),
                "reasoning", List.of("Error occurred during AI processing", e.getMessage()),
                "success", false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all uploaded documents
     */
    @GetMapping("/documents")
    public ResponseEntity<Map<String, Object>> listDocuments() {
        try {
            Map<String, Object> response = documentService.getAllDocuments();
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to list documents: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to retrieve document list: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Clear all uploaded documents
     */
    @DeleteMapping("/documents")
    public ResponseEntity<Map<String, Object>> clearAllDocuments() {
        try {
            Map<String, Object> response = documentService.clearAllDocuments();
            
            logger.info("✅ All documents cleared");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Failed to clear documents: {}", e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                "error", "Failed to clear documents: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        Map<String, Object> errorResponse = Map.of(
            "error", "Validation failed",
            "message", ex.getBindingResult().getFieldError().getDefaultMessage()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
}