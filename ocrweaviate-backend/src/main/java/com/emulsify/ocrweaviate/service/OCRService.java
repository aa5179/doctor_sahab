package com.emulsify.ocrweaviate.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for OCR text extraction from PDF documents
 */
@Service
public class OCRService {
    
    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);
    
    @Value("${ocr.tesseract.datapath:}")
    private String tesseractDataPath;
    
    @Value("${ocr.tesseract.language:eng}")
    private String tesseractLanguage;
    
    @Value("${ocr.temp.directory}")
    private String tempDirectory;

    private final Tesseract tesseract;

    public OCRService() {
        this.tesseract = new Tesseract();
        configureTesseract();
    }

    private void configureTesseract() {
        try {
            logger.info("🔧 Configuring Tesseract OCR...");
            
            // Try to find Tesseract installation automatically
            String[] possibleDataPaths = {
                "C:\\Program Files\\Tesseract-OCR\\tessdata",
                "C:\\Program Files (x86)\\Tesseract-OCR\\tessdata", 
                "C:\\Tesseract-OCR\\tessdata",
                System.getenv("TESSDATA_PREFIX"),
                tesseractDataPath
            };
            
            String validDataPath = null;
            for (String path : possibleDataPaths) {
                if (path != null && !path.trim().isEmpty()) {
                    File tessDataDir = new File(path);
                    File engFile = new File(tessDataDir, "eng.traineddata");
                    if (tessDataDir.exists() && engFile.exists()) {
                        validDataPath = path;
                        logger.info("✅ Found Tesseract data at: {}", path);
                        logger.info("� English language file confirmed: {}", engFile.getAbsolutePath());
                        break;
                    } else {
                        logger.debug("❌ Tesseract data not found at: {} (exists: {}, eng.traineddata: {})", 
                                   path, tessDataDir.exists(), engFile.exists());
                    }
                }
            }
            
            if (validDataPath != null) {
                tesseract.setDatapath(validDataPath);
                logger.info("🎯 Tesseract datapath set to: {}", validDataPath);
            } else {
                logger.warn("⚠️ No valid Tesseract data path found. Using default path...");
                // Try the most common installation path anyway
                String defaultPath = "C:\\Program Files\\Tesseract-OCR\\tessdata";
                tesseract.setDatapath(defaultPath);
                logger.info("🔄 Attempting default path: {}", defaultPath);
            }
            
            // Set language (default to English if not specified)
            String language = (tesseractLanguage != null && !tesseractLanguage.trim().isEmpty()) ? tesseractLanguage : "eng";
            tesseract.setLanguage(language);
            
            // Configure OCR settings for better accuracy
            tesseract.setPageSegMode(6); // Uniform block of text
            tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
            
            // Additional settings for better medical text recognition
            tesseract.setTessVariable("tessedit_char_whitelist", 
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,;:()[]{}/-+= \n\t");
            
            logger.info("✅ Tesseract OCR configured successfully");
            logger.info("📝 Language: {}", language);
            logger.info("🔧 Page Segmentation Mode: 6 (Uniform block)");
            logger.info("🧠 OCR Engine Mode: 1 (LSTM)");
            
            // Test Tesseract configuration
            testTesseractConfiguration();
            
        } catch (Exception e) {
            logger.error("❌ Failed to configure Tesseract: {}", e.getMessage());
            logger.info("🔄 Tesseract will use default configuration");
        }
    }
    
    /**
     * Test if Tesseract is working properly
     */
    private void testTesseractConfiguration() {
        try {
            logger.info("🧪 Testing Tesseract configuration...");
            
            // Create a simple test image with text
            BufferedImage testImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = testImage.createGraphics();
            g2d.setColor(java.awt.Color.WHITE);
            g2d.fillRect(0, 0, 200, 50);
            g2d.setColor(java.awt.Color.BLACK);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
            g2d.drawString("TEST OCR", 20, 30);
            g2d.dispose();
            
            // Test OCR on the image
            String result = tesseract.doOCR(testImage);
            
            if (result != null && result.toLowerCase().contains("test")) {
                logger.info("✅ Tesseract test successful! Result: '{}'", result.trim());
            } else {
                logger.warn("⚠️ Tesseract test failed. Result: '{}'", result);
            }
            
        } catch (Exception e) {
            logger.error("❌ Tesseract test failed: {}", e.getMessage());
        }
    }

    /**
     * Extract text from PDF using OCR
     */
    public String extractTextFromPDF(MultipartFile file) throws IOException, TesseractException {
        logger.info("📄 Starting OCR extraction for: {}", file.getOriginalFilename());
        
        // Create temp directory if it doesn't exist
        Path tempDir = Paths.get(tempDirectory);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        
        // Save uploaded file temporarily
        File tempFile = File.createTempFile("ocr_", ".pdf", tempDir.toFile());
        file.transferTo(tempFile);
        
        try {
            // Try OCR extraction first
            try {
                String ocrText = extractTextFromPDFFile(tempFile);
                if (ocrText != null && !ocrText.trim().isEmpty()) {
                    logger.info("✅ OCR extraction successful: {} characters", ocrText.length());
                    return ocrText;
                }
            } catch (Exception ocrException) {
                logger.warn("⚠️ OCR failed: {}, trying basic text extraction...", ocrException.getMessage());
            }
            
            // Fallback to basic PDF text extraction
            try {
                String basicText = extractBasicTextFromPDF(tempFile);
                if (basicText != null && !basicText.trim().isEmpty()) {
                    logger.info("✅ Basic text extraction successful: {} characters", basicText.length());
                    return basicText;
                }
            } catch (Exception basicException) {
                logger.warn("⚠️ Basic text extraction failed: {}", basicException.getMessage());
            }
            
            // Final fallback
            logger.warn("❌ All text extraction methods failed");
            return "Unable to extract text from document. The document may contain only images or the text may not be machine-readable.";
            
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * Basic text extraction from PDF (for text-based PDFs)
     */
    private String extractBasicTextFromPDF(File pdfFile) throws IOException {
        logger.info("📝 Attempting basic text extraction...");
        StringBuilder text = new StringBuilder();
        
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            // Try to extract text directly from PDF
            org.apache.pdfbox.text.PDFTextStripper textStripper = new org.apache.pdfbox.text.PDFTextStripper();
            String extractedText = textStripper.getText(document);
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                text.append(extractedText);
                logger.info("📄 Basic extraction found {} characters", extractedText.length());
            }
        }
        
        return text.toString().trim();
    }

    private String extractTextFromPDFFile(File pdfFile) throws IOException, TesseractException {
        StringBuilder extractedText = new StringBuilder();
        
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            
            logger.info("Processing {} pages with OCR", pageCount);
            
            for (int page = 0; page < pageCount; page++) {
                logger.debug("Processing page {}/{}", page + 1, pageCount);
                
                try {
                    // Render PDF page as image
                    BufferedImage pageImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                    
                    // Extract text using Tesseract
                    String pageText = tesseract.doOCR(pageImage);
                    
                    if (pageText != null && !pageText.trim().isEmpty()) {
                        extractedText.append(pageText).append(" ");
                        logger.debug("Extracted {} characters from page {}", pageText.length(), page + 1);
                    }
                    
                } catch (Exception e) {
                    logger.warn("Failed to process page {}: {}", page + 1, e.getMessage());
                }
            }
        }
        
        String finalText = cleanExtractedText(extractedText.toString());
        logger.info("✅ OCR extraction completed. Total characters: {}", finalText.length());
        
        return finalText;
    }

    /**
     * Clean and normalize extracted text
     */
    private String cleanExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // Remove excessive whitespace
        text = Pattern.compile("\\s+").matcher(text).replaceAll(" ");
        
        // Remove page number patterns
        text = Pattern.compile("\\bpage\\s*\\d+\\b", Pattern.CASE_INSENSITIVE).matcher(text).replaceAll("");
        
        // Remove common OCR artifacts
        text = text.replaceAll("[\\u0000-\\u001F\\u007F]", ""); // Control characters
        text = text.replaceAll("\\s*\\n\\s*", " "); // Normalize line breaks
        
        return text.trim();
    }

    /**
     * Split text into chunks for vector storage
     */
    public List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }
        
        String cleanText = text.trim();
        int textLength = cleanText.length();
        
        if (textLength <= chunkSize) {
            chunks.add(cleanText);
            return chunks;
        }
        
        int start = 0;
        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            
            // Try to break at sentence or word boundary
            if (end < textLength) {
                int lastPeriod = cleanText.lastIndexOf('.', end);
                int lastSpace = cleanText.lastIndexOf(' ', end);
                
                if (lastPeriod > start + chunkSize / 2) {
                    end = lastPeriod + 1;
                } else if (lastSpace > start + chunkSize / 2) {
                    end = lastSpace;
                }
            }
            
            String chunk = cleanText.substring(start, end).trim();
            if (chunk.length() > 30) { // Minimum chunk length
                chunks.add(chunk);
            }
            
            start = Math.max(start + chunkSize - overlap, end);
        }
        
        logger.info("Created {} text chunks from {} characters", chunks.size(), textLength);
        return chunks;
    }

    /**
     * Check if OCR is properly configured
     */
    public boolean isOCRAvailable() {
        try {
            // Test with a simple image
            BufferedImage testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            tesseract.doOCR(testImage);
            return true;
        } catch (Exception e) {
            logger.error("OCR not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract text from image files using OCR
     */
    public String extractTextFromImage(MultipartFile file) throws IOException, TesseractException {
        logger.info("🖼️ Starting OCR extraction for image: {}", file.getOriginalFilename());
        
        // Create temp file
        java.io.File tempFile = java.io.File.createTempFile("ocr_image_", getFileExtension(file.getOriginalFilename()));
        file.transferTo(tempFile);
        
        try {
            // Read image file
            BufferedImage originalImage = javax.imageio.ImageIO.read(tempFile);
            
            if (originalImage == null) {
                throw new IOException("Unable to read image file: " + file.getOriginalFilename());
            }
            
            logger.info("📸 Processing image: {}x{} pixels", originalImage.getWidth(), originalImage.getHeight());
            
            // Try multiple OCR approaches for better results
            String bestResult = null;
            int bestScore = 0;
            
            // Approach 1: Original image
            String result1 = extractTextFromProcessedImage(originalImage, "Original");
            int score1 = scoreOCRResult(result1);
            if (score1 > bestScore) {
                bestResult = result1;
                bestScore = score1;
            }
            
            // Approach 2: Enhanced image (contrast, brightness)
            BufferedImage enhancedImage = enhanceImageForOCR(originalImage);
            String result2 = extractTextFromProcessedImage(enhancedImage, "Enhanced");
            int score2 = scoreOCRResult(result2);
            if (score2 > bestScore) {
                bestResult = result2;
                bestScore = score2;
            }
            
            // Approach 3: Grayscale conversion
            BufferedImage grayImage = convertToGrayscale(originalImage);
            String result3 = extractTextFromProcessedImage(grayImage, "Grayscale");
            int score3 = scoreOCRResult(result3);
            if (score3 > bestScore) {
                bestResult = result3;
                bestScore = score3;
            }
            
            if (bestResult != null && !bestResult.trim().isEmpty() && bestScore > 10) {
                String cleanText = cleanAndValidateText(bestResult);
                logger.info("✅ OCR extraction successful: {} characters extracted (score: {})", cleanText.length(), bestScore);
                logger.info("📝 Extracted text preview: {}", cleanText.substring(0, Math.min(100, cleanText.length())));
                return cleanText;
            } else {
                logger.warn("⚠️ Poor OCR results. Best score: {}, Text: '{}'", bestScore, bestResult);
                return generateFallbackText(file.getOriginalFilename());
            }
            
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * Extract text from a processed image
     */
    private String extractTextFromProcessedImage(BufferedImage image, String approach) {
        try {
            // Try different page segmentation modes for medical documents
            tesseract.setPageSegMode(6); // Uniform block of text
            String result = tesseract.doOCR(image);
            
            if (result == null || result.trim().length() < 10) {
                // Try alternative segmentation mode
                tesseract.setPageSegMode(8); // Single word
                result = tesseract.doOCR(image);
            }
            
            logger.debug("🔍 {} approach result: {} characters", approach, result != null ? result.length() : 0);
            return result != null ? result : "";
            
        } catch (Exception e) {
            logger.warn("❌ {} approach failed: {}", approach, e.getMessage());
            return "";
        }
    }
    
    /**
     * Enhance image for better OCR results
     */
    private BufferedImage enhanceImageForOCR(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage enhanced = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Increase contrast and brightness
                r = Math.min(255, Math.max(0, (int)((r - 128) * 1.5 + 128 + 20)));
                g = Math.min(255, Math.max(0, (int)((g - 128) * 1.5 + 128 + 20)));
                b = Math.min(255, Math.max(0, (int)((b - 128) * 1.5 + 128 + 20)));
                
                enhanced.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        
        return enhanced;
    }
    
    /**
     * Convert image to grayscale
     */
    private BufferedImage convertToGrayscale(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage gray = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                int grayLevel = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                gray.setRGB(x, y, (grayLevel << 16) | (grayLevel << 8) | grayLevel);
            }
        }
        
        return gray;
    }
    
    /**
     * Score OCR result quality
     */
    private int scoreOCRResult(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        int score = 0;
        String cleanText = text.trim();
        
        // Basic length score
        score += Math.min(cleanText.length() / 10, 20);
        
        // Letter/number ratio
        long letters = cleanText.chars().filter(Character::isLetter).count();
        long digits = cleanText.chars().filter(Character::isDigit).count();
        score += Math.min((letters + digits) / 5, 30);
        
        // Medical terms boost
        String lowerText = cleanText.toLowerCase();
        if (lowerText.contains("mg") || lowerText.contains("tablet") || lowerText.contains("capsule") ||
            lowerText.contains("dose") || lowerText.contains("daily") || lowerText.contains("medicine") ||
            lowerText.contains("prescription") || lowerText.contains("doctor") || lowerText.contains("dr")) {
            score += 25;
        }
        
        // Penalize too many special characters
        long specials = cleanText.chars().filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)).count();
        if (specials > cleanText.length() / 3) {
            score -= 20;
        }
        
        return Math.max(0, score);
    }
    
    /**
     * Clean and validate extracted text
     */
    private String cleanAndValidateText(String text) {
        if (text == null) return "";
        
        // Remove excessive whitespace and clean up
        String cleaned = text.replaceAll("\\s+", " ").trim();
        
        // Remove common OCR artifacts
        cleaned = cleaned.replaceAll("[|\\\\/_]+", " ");
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        return cleaned;
    }
    
    /**
     * Generate fallback text when OCR fails
     */
    private String generateFallbackText(String filename) {
        return String.format(
            "📄 IMAGE PROCESSED: %s\n\n" +
            "⚠️ OCR Note: Text extraction was unsuccessful.\n" +
            "This could be due to:\n" +
            "• Poor image quality or resolution\n" +
            "• Handwritten text (OCR works best with printed text)\n" +
            "• Complex layouts or backgrounds\n" +
            "• Non-standard fonts or rotated text\n\n" +
            "📋 Suggestions:\n" +
            "• Try a clearer, higher resolution image\n" +
            "• Ensure text is clearly visible and not handwritten\n" +
            "• Use good lighting when taking the photo\n" +
            "• Avoid shadows or glare on the document",
            filename
        );
    }
    
    /**
     * Helper method to get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".tmp";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}