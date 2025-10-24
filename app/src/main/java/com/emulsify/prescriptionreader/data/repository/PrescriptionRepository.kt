package com.emulsify.prescriptionreader.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.emulsify.prescriptionreader.data.model.DoctorInfo
import com.emulsify.prescriptionreader.data.model.Medicine
import com.emulsify.prescriptionreader.data.model.MedicineType
import com.emulsify.prescriptionreader.data.model.Prescription
import com.emulsify.prescriptionreader.data.model.PrescriptionAnalysis
import com.emulsify.prescriptionreader.network.NetworkConfig
import com.emulsify.prescriptionreader.network.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class PrescriptionRepository {
    
    private val apiService = NetworkConfig.apiService
    private val TAG = "PrescriptionRepository"
    
    suspend fun uploadPrescription(context: Context, fileUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 TESTING REAL OCR - Starting file upload for URI: $fileUri")
            
            // Convert URI to File
            val file = uriToFile(context, fileUri)
            Log.d(TAG, "📄 File prepared: ${file.name}, size: ${file.length()} bytes")
            
            val mimeType = when {
                file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                file.name.endsWith(".jpg", ignoreCase = true) || 
                file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                file.name.endsWith(".png", ignoreCase = true) -> "image/png"
                else -> "application/octet-stream"
            }
            
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("files", file.name, requestFile)
            
            // 🎯 TRY REAL OCR FIRST
            try {
                Log.d(TAG, "🔬 Attempting REAL OCR processing via /upload endpoint...")
                val response = apiService.uploadPrescription(body)
                
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    Log.d(TAG, "📊 OCR Response received: $uploadResponse")
                    
                    if (uploadResponse?.documents?.isNotEmpty() == true) {
                        val extractedText = uploadResponse.documents.first().content
                        Log.d(TAG, "✅ REAL OCR SUCCESS! Extracted ${extractedText.length} characters")
                        
                        // Clean up temp file
                        if (file.exists()) {
                            file.delete()
                            Log.d(TAG, "Temporary file cleaned up")
                        }
                        
                        val result = """
                            🔬 OCR EXTRACTION SUCCESSFUL!
                            
                            📋 Extracted Text (${extractedText.length} characters):
                            
                            $extractedText
                            
                            📄 File: ${file.name}
                            📊 Processing: Tesseract OCR Engine
                            ✅ Status: Real OCR Processing Complete
                        """.trimIndent()
                        
                        return@withContext Result.success(result)
                    } else {
                        Log.w(TAG, "⚠️ OCR response empty or no documents")
                        val message = uploadResponse?.message ?: "No documents processed"
                        throw Exception("OCR processed but no text extracted: $message")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ OCR failed: ${response.code()} - $errorBody")
                    throw Exception("OCR HTTP ${response.code()}: $errorBody")
                }
            } catch (ocrError: Exception) {
                Log.w(TAG, "🔄 Real OCR failed: ${ocrError.message}, trying text extraction...")
                
                // 📝 TRY BASIC TEXT EXTRACTION
                try {
                    // Recreate file for text extraction
                    val tempFile2 = uriToFile(context, fileUri)
                    val requestFile2 = tempFile2.asRequestBody(mimeType.toMediaTypeOrNull())
                    val body2 = MultipartBody.Part.createFormData("files", tempFile2.name, requestFile2)
                    
                    val textResponse = apiService.extractTextOnly(body2)
                    
                    // Clean up temp files
                    if (file.exists()) file.delete()
                    if (tempFile2.exists()) tempFile2.delete()
                    
                    if (textResponse.isSuccessful) {
                        val textResult = textResponse.body()
                        Log.d(TAG, "📝 Text extraction success: $textResult")
                        
                        if (textResult?.documents?.isNotEmpty() == true) {
                            // Text is in the message field for this endpoint
                            val extractedText = textResult.message
                            val textExtractionResult = """
                                📝 TEXT EXTRACTION SUCCESSFUL!
                                
                                📋 Extracted Content:
                                
                                $extractedText
                                
                                📄 File: ${file.name}
                                📊 Processing: Basic PDF Text Extraction
                                ℹ️ Note: OCR was unavailable, used text-based extraction
                            """.trimIndent()
                            
                            return@withContext Result.success(textExtractionResult)
                        } else {
                            Log.w(TAG, "⚠️ Text extraction found no content")
                            // Check if message contains extracted text
                            val message = textResult?.message ?: ""
                            if (message.isNotEmpty() && !message.contains("No text found")) {
                                val textExtractionResult = """
                                    📝 TEXT EXTRACTION SUCCESSFUL!
                                    
                                    📋 Extracted Content:
                                    
                                    $message
                                    
                                    📄 File: ${file.name}
                                    📊 Processing: Basic PDF Text Extraction
                                """.trimIndent()
                                
                                return@withContext Result.success(textExtractionResult)
                            }
                            throw Exception("No text found in PDF - may require OCR for scanned images")
                        }
                    } else {
                        Log.e(TAG, "❌ Text extraction failed: ${textResponse.code()}")
                        throw Exception("Text extraction failed: ${textResponse.code()}")
                    }
                } catch (textError: Exception) {
                    Log.w(TAG, "🔄 Text extraction failed: ${textError.message}, trying test endpoint...")
                    
                    // 🔄 FINAL FALLBACK TO TEST ENDPOINT
                    try {
                        // Recreate file for test endpoint
                        val tempFile3 = uriToFile(context, fileUri)
                        val requestFile3 = tempFile3.asRequestBody(mimeType.toMediaTypeOrNull())
                        val body3 = MultipartBody.Part.createFormData("files", tempFile3.name, requestFile3)
                        
                        val testResponse = apiService.testUploadPrescription(body3)
                        
                        // Clean up temp files
                        if (file.exists()) file.delete()
                        if (tempFile3.exists()) tempFile3.delete()
                        
                        if (testResponse.isSuccessful) {
                            val testResult = testResponse.body()
                            Log.d(TAG, "🧪 Test endpoint success: $testResult")
                            
                            val fallbackResult = """
                                🧪 TEST MODE (Text Extraction Unavailable)
                                
                                ⚠️ Processing Issues:
                                • OCR: ${ocrError.message}
                                • Text: ${textError.message}
                                
                                📄 File: ${file.name} (${file.length()} bytes)
                                🔄 Fallback: Test endpoint successful
                                📝 Message: ${testResult?.message ?: "File received successfully"}
                                
                                🔧 Possible Solutions:
                                - Try a text-based PDF instead of scanned image
                                - Check backend Tesseract installation
                                - Verify PDF is not corrupted or password-protected
                            """.trimIndent()
                            
                            return@withContext Result.success(fallbackResult)
                        } else {
                            Log.e(TAG, "❌ All endpoints failed: ${testResponse.code()}")
                            throw Exception("All extraction methods failed: OCR, Text, and Test endpoints")
                        }
                    } catch (testError: Exception) {
                        Log.e(TAG, "❌ Complete failure: ${testError.message}")
                        throw Exception("Complete failure - OCR: ${ocrError.message}, Text: ${textError.message}, Test: ${testError.message}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Complete upload failure: ${e.message}", e)
            
            // Final fallback with detailed error info
            val errorResult = """
                ❌ UPLOAD FAILED
                
                Error: ${e.message}
                
                📄 File: ${fileUri.lastPathSegment}
                🔧 Troubleshooting:
                
                1. Check backend is running (localhost:8000)
                2. Verify file permissions and size
                3. Check network connectivity
                4. Review backend logs for Tesseract errors
                
                Error Details: ${e.javaClass.simpleName}
            """.trimIndent()
            
            Result.success(errorResult) // Return as success with error info for user
        }
    }
    
    suspend fun checkBackendHealth(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Checking backend health...")
            val response = apiService.checkHealth()
            
            if (response.isSuccessful) {
                val health = response.body()
                Log.d(TAG, "Backend health: ${health?.status}")
                Result.success(health?.status ?: "Unknown")
            } else {
                Log.e(TAG, "Health check failed: ${response.code()}")
                Result.failure(Exception("Backend not available: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Health check error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        try {
            Log.d(TAG, "Converting URI to file: $uri")
            
            // Get the actual filename from URI
            val fileName = getFileNameFromUri(context, uri) ?: "temp_prescription_${System.currentTimeMillis()}.pdf"
            Log.d(TAG, "Using filename: $fileName")
            
            // Create cache directory if it doesn't exist
            val cacheDir = context.cacheDir
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
                Log.d(TAG, "Created cache directory: ${cacheDir.absolutePath}")
            }
            
            val tempFile = File(cacheDir, fileName)
            Log.d(TAG, "Creating temp file: ${tempFile.absolutePath}")
            
            // Open input stream and copy content
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open input stream for URI: $uri")
            
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    val bytesCopied = input.copyTo(output)
                    Log.d(TAG, "Copied $bytesCopied bytes to temp file")
                }
            }
            
            // Verify file was created and has content
            if (!tempFile.exists()) {
                throw IOException("Failed to create temp file: ${tempFile.absolutePath}")
            }
            
            if (tempFile.length() == 0L) {
                throw IOException("Temp file is empty: ${tempFile.absolutePath}")
            }
            
            Log.d(TAG, "Successfully created temp file: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
            return tempFile
            
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file: ${e.message}", e)
            throw e
        }
    }
    
    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get filename from URI: ${e.message}")
            null
        }
    }

    suspend fun analyzePrescription(extractedText: String): Result<PrescriptionAnalysis> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🤖 Starting prescription analysis...")
            
            val analysisRequest = AskRequest(
                query = "Parse this prescription and extract: 1) Medicine names and generic names, 2) Dosage information, 3) Frequency and duration, 4) Doctor information, 5) Safety warnings",
                context = extractedText
            )
            
            val response = apiService.askQuestion(analysisRequest)
            
            if (response.isSuccessful) {
                val askResponse = response.body()
                Log.d(TAG, "✅ AI Analysis response received: ${askResponse?.response?.length} characters")
                
                if (askResponse?.response != null) {
                    // Parse the AI response into structured data
                    val analysis = parseAnalysisResponse(askResponse.response, extractedText)
                    Log.d(TAG, "✅ Prescription analysis completed successfully")
                    return@withContext Result.success(analysis)
                } else {
                    throw Exception("Empty analysis response from AI")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Analysis failed: ${response.code()} - $errorBody")
                throw Exception("Analysis HTTP ${response.code()}: $errorBody")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Analysis failure: ${e.message}", e)
            
            // Provide fallback analysis with basic parsing
            val fallbackAnalysis = createFallbackAnalysis(extractedText)
            return@withContext Result.success(fallbackAnalysis)
        }
    }

    private fun parseAnalysisResponse(aiResponse: String, extractedText: String): PrescriptionAnalysis {
        val medicines = mutableListOf<Medicine>()
        val warnings = mutableListOf<String>()
        val interactions = mutableListOf<String>()
        
        // Extract doctor information
        val doctorInfo = extractDoctorInformation(extractedText)
        
        // Parse medicines from AI response and extracted text
        medicines.addAll(extractMedicinesFromText(extractedText))
        medicines.addAll(extractMedicinesFromAIResponse(aiResponse))
        
        // Extract warnings from AI response
        warnings.addAll(extractWarningsFromAIResponse(aiResponse))
        
        // Extract drug interactions
        interactions.addAll(extractInteractionsFromAIResponse(aiResponse))
        
        return PrescriptionAnalysis(
            prescriptionId = System.currentTimeMillis().toString(),
            extractedText = extractedText,
            confidence = calculateConfidence(extractedText),
            medicines = medicines.distinctBy { it.name }, // Remove duplicates
            doctorInfo = doctorInfo,
            warnings = warnings,
            interactions = interactions,
            analysisDate = System.currentTimeMillis()
        )
    }

    private fun createFallbackAnalysis(extractedText: String): PrescriptionAnalysis {
        Log.d(TAG, "📋 Creating fallback analysis for extracted text")
        
        val medicines = extractMedicinesFromText(extractedText)
        val doctorInfo = extractDoctorInformation(extractedText)
        
        val basicWarnings = listOf(
            "Always follow the prescribed dosage and frequency",
            "Consult your doctor before making any changes",
            "Check for drug interactions with other medications",
            "Contact your pharmacist for any questions"
        )
        
        return PrescriptionAnalysis(
            prescriptionId = System.currentTimeMillis().toString(),
            extractedText = extractedText,
            confidence = 0.7f, // Moderate confidence for basic extraction
            medicines = medicines,
            doctorInfo = doctorInfo,
            warnings = basicWarnings,
            interactions = listOf("Check for drug interactions with other medications"),
            analysisDate = System.currentTimeMillis()
        )
    }

    private fun extractDoctorInformation(text: String): DoctorInfo {
        var doctorName = ""
        var hospital = ""
        var specialization = ""
        var licenseNumber = ""
        var phoneNumber = ""

        // Extract doctor name
        val doctorPattern = Regex("Dr\\.?\\s+([A-Za-z\\s]+)", RegexOption.IGNORE_CASE)
        doctorPattern.find(text)?.let { match ->
            doctorName = match.groupValues[1].trim()
        }

        // Extract hospital/clinic name
        val clinicPattern = Regex("(\\w+\\s+(?:Clinic|Hospital|Care|Medical|Center))", RegexOption.IGNORE_CASE)
        clinicPattern.find(text)?.let { match ->
            hospital = match.groupValues[1].trim()
        }

        // Extract specialization
        val specializationPattern = Regex("(MBBS|M\\.D|M\\.S|MD|MS)", RegexOption.IGNORE_CASE)
        specializationPattern.findAll(text).joinToString(", ") { it.value }.let { spec ->
            if (spec.isNotEmpty()) specialization = spec
        }

        // Extract phone number
        val phonePattern = Regex("(\\d{3}[-.\\s]?\\d{4}|\\d{10})")
        phonePattern.find(text)?.let { match ->
            phoneNumber = match.value
        }

        // Extract license/registration number
        val licensePattern = Regex("(?:No|Reg|License)[:.\\s]+(\\d+)", RegexOption.IGNORE_CASE)
        licensePattern.find(text)?.let { match ->
            licenseNumber = match.groupValues[1]
        }

        return DoctorInfo(
            name = doctorName.ifEmpty { "Doctor information not found" },
            specialization = specialization.ifEmpty { "Not specified" },
            hospital = hospital.ifEmpty { "Hospital/Clinic not specified" },
            licenseNumber = licenseNumber.ifEmpty { "Not available" },
            phoneNumber = phoneNumber.ifEmpty { "Not available" }
        )
    }

    private fun extractMedicinesFromText(text: String): List<Medicine> {
        val medicines = mutableListOf<Medicine>()
        val lines = text.split("\n")

        for (line in lines) {
            val lowerLine = line.toLowerCase()
            
            // Look for lines that might contain medicine information
            if (lowerLine.contains("tablet") || lowerLine.contains("capsule") || 
                lowerLine.contains("mg") || lowerLine.contains("ml") ||
                lowerLine.contains("dose") || lowerLine.contains("take") ||
                lowerLine.contains("daily") || lowerLine.contains("twice")) {
                
                val medicine = parseMedicineFromLine(line.trim())
                if (medicine.name.isNotEmpty()) {
                    medicines.add(medicine)
                }
            }
        }

        return medicines
    }

    private fun parseMedicineFromLine(line: String): Medicine {
        // Basic parsing logic - can be enhanced
        val name = extractMedicineName(line)
        val dosage = extractDosage(line)
        val frequency = extractFrequency(line)
        val type = determineMedicineType(line)

        return Medicine(
            name = name,
            genericName = "",
            dosage = dosage,
            frequency = frequency,
            duration = extractDuration(line),
            instructions = line,
            sideEffects = emptyList(),
            type = type
        )
    }

    private fun extractMedicineName(line: String): String {
        // Extract potential medicine name (first word or before dosage)
        val words = line.split(" ")
        return words.firstOrNull { word ->
            word.length > 3 && !word.lowercase().contains("take") && 
            !word.lowercase().contains("daily") && !word.lowercase().contains("mg")
        } ?: ""
    }

    private fun extractDosage(line: String): String {
        val dosagePattern = Regex("(\\d+\\s*mg|\\d+\\s*ml|\\d+\\s*tablet|\\d+\\s*capsule)", RegexOption.IGNORE_CASE)
        return dosagePattern.find(line)?.value ?: ""
    }

    private fun extractFrequency(line: String): String {
        return when {
            line.lowercase().contains("twice") || line.lowercase().contains("2") -> "Twice daily"
            line.lowercase().contains("thrice") || line.lowercase().contains("3") -> "Three times daily"
            line.lowercase().contains("once") || line.lowercase().contains("1") -> "Once daily"
            line.lowercase().contains("daily") -> "Daily"
            else -> "As directed"
        }
    }

    private fun extractDuration(line: String): String {
        val durationPattern = Regex("(\\d+\\s*days?|\\d+\\s*weeks?|\\d+\\s*months?)", RegexOption.IGNORE_CASE)
        return durationPattern.find(line)?.value ?: "Not specified"
    }

    private fun determineMedicineType(line: String): MedicineType {
        return when {
            line.lowercase().contains("tablet") -> MedicineType.TABLET
            line.lowercase().contains("capsule") -> MedicineType.CAPSULE
            line.lowercase().contains("syrup") -> MedicineType.SYRUP
            line.lowercase().contains("injection") -> MedicineType.INJECTION
            line.lowercase().contains("drops") -> MedicineType.DROPS
            line.lowercase().contains("cream") -> MedicineType.CREAM
            else -> MedicineType.OTHER
        }
    }

    private fun extractMedicinesFromAIResponse(aiResponse: String): List<Medicine> {
        // Parse structured information from AI response
        val medicines = mutableListOf<Medicine>()
        
        // This is a simplified parser - can be enhanced based on AI response format
        if (aiResponse.contains("MEDICATION DETAILS:")) {
            val medicationSection = aiResponse.substringAfter("MEDICATION DETAILS:")
                .substringBefore("PRESCRIBER INFORMATION:")
            
            medicationSection.split("•").forEach { item ->
                val trimmed = item.trim()
                if (trimmed.isNotEmpty() && trimmed.length > 10) {
                    val medicine = parseMedicineFromLine(trimmed)
                    if (medicine.name.isNotEmpty()) {
                        medicines.add(medicine)
                    }
                }
            }
        }
        
        return medicines
    }

    private fun extractWarningsFromAIResponse(aiResponse: String): List<String> {
        val warnings = mutableListOf<String>()
        
        if (aiResponse.contains("SAFETY NOTES:")) {
            val safetySection = aiResponse.substringAfter("SAFETY NOTES:")
            safetySection.split("•").forEach { item ->
                val trimmed = item.trim()
                if (trimmed.isNotEmpty() && trimmed.length > 5) {
                    warnings.add(trimmed)
                }
            }
        }
        
        return warnings
    }

    private fun extractInteractionsFromAIResponse(aiResponse: String): List<String> {
        val interactions = mutableListOf<String>()
        
        // Look for drug interaction information in AI response
        if (aiResponse.lowercase().contains("interaction")) {
            interactions.add("Check for drug interactions with other medications")
        }
        
        return interactions
    }

    private fun calculateConfidence(text: String): Float {
        var confidence = 0.5f // Base confidence
        
        // Increase confidence based on content quality
        if (text.contains("Dr.", ignoreCase = true)) confidence += 0.1f
        if (text.contains("mg", ignoreCase = true)) confidence += 0.1f
        if (text.contains("tablet", ignoreCase = true) || text.contains("capsule", ignoreCase = true)) confidence += 0.1f
        if (text.contains("daily", ignoreCase = true)) confidence += 0.1f
        if (text.length > 200) confidence += 0.1f
        
        return minOf(1.0f, confidence)
    }
}