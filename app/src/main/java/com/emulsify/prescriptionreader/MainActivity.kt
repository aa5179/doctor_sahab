package com.emulsify.prescriptionreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emulsify.prescriptionreader.ui.screens.LoadingScreen
import com.emulsify.prescriptionreader.ui.screens.DashboardScreen
import com.emulsify.prescriptionreader.ui.screens.ProfileScreen
import com.emulsify.prescriptionreader.ui.screens.EditProfileScreen
import com.emulsify.prescriptionreader.ui.screens.UploadScreen
import com.emulsify.prescriptionreader.ui.screens.MedicalRecord
import com.emulsify.prescriptionreader.ui.theme.PrescriptionReaderTheme
import com.emulsify.prescriptionreader.ui.viewmodel.PrescriptionViewModel
import com.emulsify.prescriptionreader.data.model.UserProfile
import com.emulsify.prescriptionreader.data.model.Prescription
import com.emulsify.prescriptionreader.data.model.Medicine
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PrescriptionReaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrescriptionReaderApp()
                }
            }
        }
    }
}

@Composable
fun PrescriptionReaderApp() {
    var isLoading by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("dashboard") }
    val prescriptionViewModel: PrescriptionViewModel = viewModel()
    var userProfile by remember { mutableStateOf(
        UserProfile(
            name = "John Doe",
            age = 35,
            gender = "Male",
            bloodGroup = "O+",
            allergies = listOf("Peanuts", "Shellfish", "Dust mites"),
            chronicConditions = listOf("Hypertension", "Type 2 Diabetes")
        )
    ) }
    
    val sampleMedicalHistory = listOf(
        MedicalRecord(
            title = "Annual Health Checkup",
            description = "Complete physical examination with blood work and vitals assessment. All parameters within normal range.",
            date = System.currentTimeMillis() - 86400000 * 30, // 30 days ago
            doctor = "Smith",
            hospital = "City General Hospital",
            type = "Routine Checkup"
        ),
        MedicalRecord(
            title = "Blood Pressure Monitoring",
            description = "Follow-up appointment for hypertension management. Blood pressure readings stable.",
            date = System.currentTimeMillis() - 86400000 * 15, // 15 days ago
            doctor = "Johnson",
            hospital = "Heart Care Clinic",
            type = "Follow-up"
        ),
        MedicalRecord(
            title = "Diabetes Management Consultation",
            description = "HbA1c levels reviewed. Medication dosage adjusted. Diet plan updated.",
            date = System.currentTimeMillis() - 86400000 * 45, // 45 days ago
            doctor = "Williams",
            hospital = "Diabetes Care Center",
            type = "Specialist Consultation"
        ),
        MedicalRecord(
            title = "Eye Examination",
            description = "Routine diabetic eye screening. No signs of diabetic retinopathy detected.",
            date = System.currentTimeMillis() - 86400000 * 90, // 90 days ago
            doctor = "Brown",
            hospital = "Vision Care Institute",
            type = "Screening"
        )
    )
    
    val samplePrescriptions = listOf(
        Prescription(
            doctorName = "Dr. Smith",
            hospitalName = "City General Hospital",
            date = System.currentTimeMillis() - 86400000 * 5,
            diagnosis = "Hypertension management",
            medicines = listOf(
                Medicine(
                    name = "Lisinopril",
                    dosage = "10mg",
                    frequency = "Once daily",
                    duration = "30 days"
                ),
                Medicine(
                    name = "Metformin",
                    dosage = "500mg",
                    frequency = "Twice daily",
                    duration = "30 days"
                )
            )
        ),
        Prescription(
            doctorName = "Dr. Johnson",
            hospitalName = "Heart Care Clinic",
            date = System.currentTimeMillis() - 86400000 * 20,
            diagnosis = "Diabetes management",
            medicines = listOf(
                Medicine(
                    name = "Insulin Glargine",
                    dosage = "20 units",
                    frequency = "Once daily at bedtime",
                    duration = "Ongoing"
                )
            )
        )
    )
    
    when {
        isLoading -> {
            LoadingScreen(
                onLoadingComplete = {
                    isLoading = false
                }
            )
        }
        currentScreen == "dashboard" -> {
            DashboardScreen(
                userProfile = userProfile,
                prescriptions = prescriptionViewModel.prescriptions + samplePrescriptions,
                onPrescriptionClick = { prescription ->
                    // TODO: Navigate to prescription details
                },
                onScanPrescription = {
                    // TODO: Open camera for scanning
                },
                onUploadPrescription = {
                    currentScreen = "upload"
                },
                onProfileClick = {
                    currentScreen = "profile"
                }
            )
        }
        currentScreen == "profile" -> {
            ProfileScreen(
                userProfile = userProfile,
                medicalHistory = sampleMedicalHistory,
                recentPrescriptions = prescriptionViewModel.prescriptions + samplePrescriptions,
                onBackClick = {
                    currentScreen = "dashboard"
                },
                onEditProfile = {
                    currentScreen = "edit_profile"
                },
                onAddMedicalRecord = {
                    // TODO: Open add medical record screen
                }
            )
        }
        currentScreen == "edit_profile" -> {
            EditProfileScreen(
                userProfile = userProfile,
                onBackClick = {
                    currentScreen = "profile"
                },
                onSaveProfile = { updatedProfile ->
                    userProfile = updatedProfile
                    currentScreen = "profile"
                }
            )
        }
        currentScreen == "upload" -> {
            UploadScreen(
                onBackClick = {
                    currentScreen = "dashboard"
                },
                prescriptionViewModel = prescriptionViewModel
            )
        }
    }
}