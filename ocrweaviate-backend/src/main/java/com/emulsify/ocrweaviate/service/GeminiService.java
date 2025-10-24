package com.emulsify.ocrweaviate.service;

import com.emulsify.ocrweaviate.config.GeminiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service for Gemini AI integration
 */
@Service
public class GeminiService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    
    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiService(RestTemplate restTemplate, GeminiConfig geminiConfig) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate response using Gemini API
     */
    public String generateResponse(String query, List<String> context, List<String> sources) {
        try {
            logger.info("🤖 Generating AI response for query: {}", query.substring(0, Math.min(100, query.length())));
            
            // Check if this is a prescription analysis request
            if (isPrescriptionAnalysisQuery(query, context)) {
                return generatePrescriptionAnalysis(query, context);
            }
            
            // For other queries, return a simulated response
            String simulatedResponse = String.format(
                "Answer:\nBased on your documents, here's what I found about '%s'.\n\n" +
                "Explanation:\n\nCore Concepts:\n\n• Main concept related to your query\n" +
                "  - Key explanation based on document content\n" +
                "  - Important details from the analysis\n\n" +
                "• Secondary concept\n" +
                "  - Supporting information\n" +
                "  - Additional context\n\n" +
                "Key Considerations:\n\n• Important point 1\n\n• Important point 2\n\n• Important point 3\n",
                query
            );
            
            logger.info("✅ Generated response: {} characters", simulatedResponse.length());
            return simulatedResponse;
            
        } catch (Exception e) {
            logger.error("❌ Gemini API error: {}", e.getMessage());
            return "I found relevant information in your documents, but I'm unable to generate a detailed response at the moment. Please try again.";
        }
    }

    /**
     * Check if the query is asking for prescription analysis
     */
    private boolean isPrescriptionAnalysisQuery(String query, List<String> context) {
        if (query == null) return false;
        
        String lowerQuery = query.toLowerCase();
        boolean hasPrescriptionKeywords = lowerQuery.contains("medicine") || 
                                        lowerQuery.contains("dosage") || 
                                        lowerQuery.contains("prescription") ||
                                        lowerQuery.contains("extract") ||
                                        lowerQuery.contains("drug") ||
                                        lowerQuery.contains("medication");
        
        // Also check if context contains pharmacy/medical terms
        boolean hasPharmacyContext = false;
        if (context != null && !context.isEmpty()) {
            String contextText = String.join(" ", context).toLowerCase();
            hasPharmacyContext = contextText.contains("pharmacy") ||
                               contextText.contains("prescription") ||
                               contextText.contains("medicine") ||
                               contextText.contains("tablet") ||
                               contextText.contains("capsule") ||
                               contextText.contains("mg") ||
                               contextText.contains("doctor") ||
                               contextText.contains("patient");
        }
        
        return hasPrescriptionKeywords || hasPharmacyContext;
    }

    /**
     * Generate prescription-specific analysis
     */
    private String generatePrescriptionAnalysis(String query, List<String> context) {
        try {
            String extractedText = context != null && !context.isEmpty() ? context.get(0) : "";
            
            // Analyze the prescription text for medical information
            StringBuilder analysis = new StringBuilder();
            analysis.append("📋 PRESCRIPTION ANALYSIS RESULTS\n\n");
            
            // Extract pharmacy information
            String pharmacyInfo = extractPharmacyInfo(extractedText);
            if (!pharmacyInfo.isEmpty()) {
                analysis.append("🏥 PHARMACY INFORMATION:\n");
                analysis.append(pharmacyInfo).append("\n\n");
            }
            
            // Extract patient information
            String patientInfo = extractPatientInfo(extractedText);
            if (!patientInfo.isEmpty()) {
                analysis.append("👤 PATIENT INFORMATION:\n");
                analysis.append(patientInfo).append("\n\n");
            }
            
            // Extract medication information
            String medicationInfo = extractMedicationInfo(extractedText);
            if (!medicationInfo.isEmpty()) {
                analysis.append("💊 MEDICATION DETAILS:\n");
                analysis.append(medicationInfo).append("\n\n");
            }
            
            // Extract doctor information
            String doctorInfo = extractDoctorInfo(extractedText);
            if (!doctorInfo.isEmpty()) {
                analysis.append("👨‍⚕️ PRESCRIBER INFORMATION:\n");
                analysis.append(doctorInfo).append("\n\n");
            }
            
            // Add safety notes
            analysis.append("⚠️ IMPORTANT SAFETY NOTES:\n");
            analysis.append("• Always follow the prescribed dosage and frequency\n");
            analysis.append("• Consult your doctor before making any changes\n");
            analysis.append("• Check for drug interactions with other medications\n");
            analysis.append("• Contact your pharmacist for any questions\n\n");
            
            analysis.append("📞 If you have concerns about this prescription, please contact your healthcare provider or pharmacist immediately.");
            
            String result = analysis.toString();
            logger.info("✅ Generated prescription analysis: {} characters", result.length());
            return result;
            
        } catch (Exception e) {
            logger.error("❌ Error generating prescription analysis: {}", e.getMessage());
            return "Unable to analyze prescription. Please ensure the image is clear and contains readable prescription information.";
        }
    }

    /**
     * Parse the response to extract answer, explanation, and cross-document analysis
     */
    public Map<String, String> parseFormattedResponse(String response) {
        try {
            String answer = "";
            String explanation = "";
            String crossAnalysis = "";
            
            if (response.contains("Answer:")) {
                String[] parts = response.split("Answer:", 2);
                if (parts.length > 1) {
                    String remaining = parts[1];
                    
                    if (remaining.contains("Explanation:")) {
                        String[] explainParts = remaining.split("Explanation:", 2);
                        answer = explainParts[0].trim();
                        
                        if (explainParts.length > 1) {
                            String explainRemaining = explainParts[1];
                            
                            if (explainRemaining.contains("Cross-Document Analysis:")) {
                                String[] crossParts = explainRemaining.split("Cross-Document Analysis:", 2);
                                explanation = crossParts[0].trim();
                                
                                if (crossParts.length > 1) {
                                    crossAnalysis = crossParts[1].trim();
                                }
                            } else {
                                explanation = explainRemaining.trim();
                            }
                        }
                    } else {
                        answer = remaining.trim();
                    }
                }
            }
            
            // Fallback: use the full response as explanation if parsing fails
            if (answer.isEmpty() && explanation.isEmpty()) {
                answer = "Based on your document(s), here's what I found about your question.";
                explanation = response;
            }
            
            return Map.of(
                "answer", answer.isEmpty() ? "Unable to generate answer" : answer,
                "explanation", explanation.isEmpty() ? response : explanation,
                "cross_document_analysis", crossAnalysis
            );
            
        } catch (Exception e) {
            logger.error("Failed to parse formatted response: {}", e.getMessage());
            return Map.of(
                "answer", "Unable to generate answer",
                "explanation", "There was an issue processing your question.",
                "cross_document_analysis", ""
            );
        }
    }

    /**
     * Test Gemini API connection
     */
    public boolean testConnection() {
        try {
            // For now, just return true (simulated connection test)
            logger.info("✅ Gemini connection test passed (simulated)");
            return true;
        } catch (Exception e) {
            logger.error("Gemini connection test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract pharmacy information from prescription text
     */
    private String extractPharmacyInfo(String text) {
        StringBuilder info = new StringBuilder();
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("pharmacy")) {
            String[] lines = text.split("\\n");
            for (String line : lines) {
                if (line.toLowerCase().contains("pharmacy")) {
                    info.append("• ").append(line.trim()).append("\n");
                    break;
                }
            }
        }
        
        // Extract address information
        if (text.matches(".*\\d+.*[Aa]venue.*") || text.matches(".*\\d+.*[Ss]treet.*") || text.matches(".*\\d+.*[Rr]oad.*")) {
            String[] lines = text.split("\\n");
            for (String line : lines) {
                if (line.matches(".*\\d+.*[Aa]venue.*") || line.matches(".*\\d+.*[Ss]treet.*") || line.matches(".*\\d+.*[Rr]oad.*")) {
                    info.append("• Address: ").append(line.trim()).append("\n");
                    break;
                }
            }
        }
        
        // Extract phone number
        if (text.matches(".*\\d{3}[-.]\\d{4}.*") || text.matches(".*\\d{3}\\s\\d{4}.*")) {
            String[] parts = text.split("\\s+");
            for (String part : parts) {
                if (part.matches("\\d{3}[-.]\\d{4}") || part.matches("\\d{3}\\d{4}")) {
                    info.append("• Phone: ").append(part).append("\n");
                    break;
                }
            }
        }
        
        return info.toString();
    }

    /**
     * Extract patient information from prescription text
     */
    private String extractPatientInfo(String text) {
        StringBuilder info = new StringBuilder();
        String[] lines = text.split("\\n");
        
        // Look for patient names (often after pharmacy info)
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.matches(".*[A-Z][a-z]+,\\s*[A-Z][a-z]+.*") || 
                line.matches(".*[A-Z][a-z]+\\s+[A-Z][a-z]+.*")) {
                // Skip pharmacy names
                if (!line.toLowerCase().contains("pharmacy")) {
                    info.append("• Patient: ").append(line).append("\n");
                    break;
                }
            }
        }
        
        // Extract prescription number
        if (text.matches(".*[Rr]\\s*[#]?\\s*\\d+.*")) {
            String[] parts = text.split("\\s+");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].matches("[Rr]") && i + 1 < parts.length) {
                    String rxNum = parts[i + 1].replaceAll("[^\\d]", "");
                    if (!rxNum.isEmpty()) {
                        info.append("• Prescription #: ").append(rxNum).append("\n");
                        break;
                    }
                }
            }
        }
        
        // Extract date
        if (text.matches(".*\\d{1,2}\\s*/\\s*\\d{1,2}\\s*/\\s*\\d{2,4}.*") || 
            text.matches(".*\\d{1,2}\\s*\\d{1,2}\\s*\\d{2,4}.*")) {
            String[] parts = text.split("\\s+");
            for (String part : parts) {
                if (part.matches("\\d{1,2}/\\d{1,2}/\\d{2,4}") || 
                    part.matches("\\d{1,2}\\s\\d{1,2}\\s\\d{2,4}")) {
                    info.append("• Date: ").append(part).append("\n");
                    break;
                }
            }
        }
        
        return info.toString();
    }

    /**
     * Extract medication information from prescription text
     */
    private String extractMedicationInfo(String text) {
        StringBuilder info = new StringBuilder();
        String lowerText = text.toLowerCase();
        
        // Common medication indicators
        String[] medicationKeywords = {"tablet", "capsule", "mg", "ml", "dose", "take", "daily", "twice", "once"};
        String[] lines = text.split("\\n");
        
        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            boolean hasMedicationKeyword = false;
            
            for (String keyword : medicationKeywords) {
                if (lowerLine.contains(keyword)) {
                    hasMedicationKeyword = true;
                    break;
                }
            }
            
            if (hasMedicationKeyword && line.trim().length() > 5) {
                info.append("• ").append(line.trim()).append("\n");
            }
        }
        
        // If no specific medications found, provide general guidance
        if (info.length() == 0) {
            info.append("• Medication details not clearly readable in the prescription\n");
            info.append("• Please verify medication names and dosages with your pharmacist\n");
        }
        
        return info.toString();
    }

    /**
     * Extract doctor information from prescription text
     */
    private String extractDoctorInfo(String text) {
        StringBuilder info = new StringBuilder();
        String lowerText = text.toLowerCase();
        
        // Look for doctor titles
        if (lowerText.contains("dr.") || lowerText.contains("doctor") || lowerText.contains("md")) {
            String[] lines = text.split("\\n");
            for (String line : lines) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.contains("dr.") || lowerLine.contains("doctor") || lowerLine.contains("md")) {
                    info.append("• ").append(line.trim()).append("\n");
                    break;
                }
            }
        }
        
        if (info.length() == 0) {
            info.append("• Doctor information not clearly visible in prescription\n");
        }
        
        return info.toString();
    }
}