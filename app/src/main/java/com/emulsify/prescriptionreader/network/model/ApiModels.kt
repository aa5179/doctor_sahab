package com.emulsify.prescriptionreader.network.model

import com.google.gson.annotations.SerializedName

// API Request Models
data class QueryRequest(
    val query: String
)

data class AskRequest(
    val query: String,
    val context: String = ""
)

// API Response Models
data class UploadResponse(
    val documents: List<DocumentInfo> = emptyList(),
    val message: String = "",
    @SerializedName("total_documents")
    val totalDocuments: Int = 0,
    // Backward compatibility
    val text: String = "",
    val success: Boolean = true
)

data class DocumentInfo(
    val filename: String = "",
    val content: String = "",
    @SerializedName("upload_time")
    val uploadTime: String = "",
    val id: String = ""
)

data class QueryResponse(
    val results: List<QueryResult>,
    val context: String,
    val success: Boolean = true
)

data class QueryResult(
    val content: String,
    val source: String? = null,
    val score: Double? = null
)

data class AskResponse(
    val response: String,
    val reasoning: List<String> = emptyList(),
    val context: String? = null,
    val success: Boolean = true
)

data class HealthResponse(
    val status: String,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// Error Response Model
data class ErrorResponse(
    val error: String,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)