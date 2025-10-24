package com.emulsify.prescriptionreader.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val profileImageUrl: String = "",
    val bloodGroup: String = "",
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList()
) : Parcelable

@Parcelize
data class Prescription(
    val id: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val hospitalName: String = "",
    val date: Long = System.currentTimeMillis(),
    val medicines: List<Medicine> = emptyList(),
    val instructions: String = "",
    val diagnosis: String = "",
    val imageUrl: String = "",
    val isProcessed: Boolean = false
) : Parcelable

@Parcelize
data class Medicine(
    val name: String = "",
    val genericName: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = "",
    val sideEffects: List<String> = emptyList(),
    val type: MedicineType = MedicineType.TABLET
) : Parcelable

enum class MedicineType {
    TABLET,
    CAPSULE,
    SYRUP,
    INJECTION,
    DROPS,
    CREAM,
    OINTMENT,
    INHALER,
    OTHER
}

@Parcelize
data class PrescriptionAnalysis(
    val prescriptionId: String = "",
    val extractedText: String = "",
    val confidence: Float = 0f,
    val medicines: List<Medicine> = emptyList(),
    val doctorInfo: DoctorInfo = DoctorInfo(),
    val warnings: List<String> = emptyList(),
    val interactions: List<String> = emptyList(),
    val analysisDate: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class DoctorInfo(
    val name: String = "",
    val specialization: String = "",
    val hospital: String = "",
    val licenseNumber: String = "",
    val phoneNumber: String = ""
) : Parcelable