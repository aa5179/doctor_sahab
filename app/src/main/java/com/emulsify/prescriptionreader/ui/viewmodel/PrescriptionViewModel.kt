package com.emulsify.prescriptionreader.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emulsify.prescriptionreader.data.model.Prescription
import com.emulsify.prescriptionreader.data.model.PrescriptionAnalysis
import com.emulsify.prescriptionreader.data.repository.PrescriptionRepository
import kotlinx.coroutines.launch

class PrescriptionViewModel : ViewModel() {
    
    private val repository = PrescriptionRepository()
    private val TAG = "PrescriptionViewModel"
    
    // UI State
    var isLoading by mutableStateOf(false)
        private set
    
    var backendConnected by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var uploadedText by mutableStateOf<String?>(null)
        private set
    
    var prescriptionAnalysis by mutableStateOf<PrescriptionAnalysis?>(null)
        private set
    
    var prescriptions by mutableStateOf<List<Prescription>>(emptyList())
        private set
    
    init {
        checkBackendConnection()
    }
    
    fun checkBackendConnection() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                Log.d(TAG, "Checking backend connection...")
                repository.checkBackendHealth()
                    .onSuccess { status ->
                        backendConnected = true
                        Log.d(TAG, "Backend connected: $status")
                    }
                    .onFailure { error ->
                        backendConnected = false
                        errorMessage = "Backend connection failed: ${error.message}"
                        Log.e(TAG, "Backend connection failed", error)
                    }
            } finally {
                isLoading = false
            }
        }
    }
    
    fun uploadPrescription(context: Context, fileUri: Uri) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                uploadedText = null
                prescriptionAnalysis = null
                
                Log.d(TAG, "Starting prescription upload and analysis...")
                
                // Step 1: Upload and extract text
                repository.uploadPrescription(context, fileUri)
                    .onSuccess { extractedText ->
                        uploadedText = extractedText
                        Log.d(TAG, "Text extracted successfully")
                        
                        // Step 2: Analyze the extracted text
                        analyzePrescriptionText(extractedText)
                    }
                    .onFailure { error ->
                        errorMessage = "Upload failed: ${error.message}"
                        Log.e(TAG, "Upload failed", error)
                        isLoading = false
                    }
                
            } catch (e: Exception) {
                errorMessage = "Unexpected error: ${e.message}"
                Log.e(TAG, "Unexpected error during upload", e)
                isLoading = false
            }
        }
    }
    
    private suspend fun analyzePrescriptionText(extractedText: String) {
        try {
            Log.d(TAG, "Analyzing extracted text...")
            
            repository.analyzePrescription(extractedText)
                .onSuccess { analysis ->
                    prescriptionAnalysis = analysis
                    Log.d(TAG, "Analysis completed successfully")
                    
                    // Create a prescription record
                    val prescription = Prescription(
                        id = System.currentTimeMillis().toString(),
                        patientName = "Current User", // You can extract this from analysis
                        doctorName = analysis.doctorInfo.name,
                        hospitalName = analysis.doctorInfo.hospital,
                        date = System.currentTimeMillis(),
                        medicines = analysis.medicines,
                        instructions = extractedText,
                        diagnosis = "",
                        imageUrl = "",
                        isProcessed = true
                    )
                    
                    // Add to prescriptions list
                    prescriptions = prescriptions + prescription
                }
                .onFailure { error ->
                    errorMessage = "Analysis failed: ${error.message}"
                    Log.e(TAG, "Analysis failed", error)
                }
        } catch (e: Exception) {
            errorMessage = "Analysis error: ${e.message}"
            Log.e(TAG, "Analysis error", e)
        } finally {
            isLoading = false
        }
    }
    
    fun clearError() {
        errorMessage = null
    }
    
    fun clearResults() {
        uploadedText = null
        prescriptionAnalysis = null
        errorMessage = null
    }
    
    fun retryConnection() {
        checkBackendConnection()
    }
}