package com.emulsify.prescriptionreader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emulsify.prescriptionreader.data.model.*
import com.emulsify.prescriptionreader.ui.theme.PrescriptionReaderTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile = UserProfile(),
    medicalHistory: List<MedicalRecord> = emptyList(),
    recentPrescriptions: List<Prescription> = emptyList(),
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onAddMedicalRecord: () -> Unit = {}
) {
    var expandedSections by remember { mutableStateOf(setOf<String>()) }

    fun toggleSection(sectionId: String) {
        expandedSections = if (expandedSections.contains(sectionId)) {
            expandedSections - sectionId
        } else {
            expandedSections + sectionId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medical Profile",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Main Profile Card
                ProfileHeaderCard(
                    userProfile = userProfile,
                    onEditClick = onEditProfile
                )
            }

            item {
                // Basic Information Section
                ProfileSection(
                    title = "Basic Information",
                    icon = Icons.Default.Person,
                    isExpanded = expandedSections.contains("basic"),
                    onToggle = { toggleSection("basic") }
                ) {
                    BasicInfoContent(userProfile = userProfile)
                }
            }

            item {
                // Medical Information Section
                ProfileSection(
                    title = "Medical Information",
                    icon = Icons.Default.MedicalServices,
                    isExpanded = expandedSections.contains("medical"),
                    onToggle = { toggleSection("medical") }
                ) {
                    MedicalInfoContent(userProfile = userProfile)
                }
            }

            item {
                // Emergency Contact Section
                ProfileSection(
                    title = "Emergency Contact",
                    icon = Icons.Default.ContactEmergency,
                    isExpanded = expandedSections.contains("emergency"),
                    onToggle = { toggleSection("emergency") }
                ) {
                    EmergencyContactContent(userProfile = userProfile)
                }
            }

            item {
                // Medical History Section
                ProfileSection(
                    title = "Medical History",
                    icon = Icons.Default.History,
                    isExpanded = expandedSections.contains("history"),
                    onToggle = { toggleSection("history") }
                ) {
                    MedicalHistoryContent(
                        medicalHistory = medicalHistory,
                        onAddRecord = onAddMedicalRecord
                    )
                }
            }

            item {
                // Recent Prescriptions Section
                ProfileSection(
                    title = "Recent Prescriptions",
                    icon = Icons.Default.Receipt,
                    isExpanded = expandedSections.contains("prescriptions"),
                    onToggle = { toggleSection("prescriptions") }
                ) {
                    RecentPrescriptionsContent(prescriptions = recentPrescriptions)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    userProfile: UserProfile,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (userProfile.profileImageUrl.isNotEmpty()) {
                    // TODO: Add Coil image loading
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = userProfile.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userProfile.name.ifEmpty { "Unknown User" },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            if (userProfile.age > 0) {
                Text(
                    text = "${userProfile.age} years old",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    label = "Gender",
                    value = userProfile.gender.ifEmpty { "Not specified" }
                )
                QuickStatItem(
                    label = "Blood Group",
                    value = userProfile.bloodGroup.ifEmpty { "Unknown" }
                )
                QuickStatItem(
                    label = "Allergies",
                    value = "${userProfile.allergies.size}"
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Section Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    Divider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun BasicInfoContent(userProfile: UserProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow(label = "Full Name", value = userProfile.name.ifEmpty { "Not provided" })
        InfoRow(label = "Age", value = if (userProfile.age > 0) "${userProfile.age} years" else "Not provided")
        InfoRow(label = "Gender", value = userProfile.gender.ifEmpty { "Not specified" })
        InfoRow(label = "Date of Birth", value = "Not provided") // TODO: Add DOB to model
        InfoRow(label = "Phone", value = "Not provided") // TODO: Add phone to model
        InfoRow(label = "Email", value = "Not provided") // TODO: Add email to model
    }
}

@Composable
private fun MedicalInfoContent(userProfile: UserProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow(label = "Blood Group", value = userProfile.bloodGroup.ifEmpty { "Unknown" })
        InfoRow(label = "Height", value = "Not provided") // TODO: Add to model
        InfoRow(label = "Weight", value = "Not provided") // TODO: Add to model
        
        // Allergies
        if (userProfile.allergies.isNotEmpty()) {
            Text(
                text = "Allergies:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            userProfile.allergies.forEach { allergy ->
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Allergy",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = allergy,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            InfoRow(label = "Allergies", value = "No known allergies")
        }

        // Chronic Conditions
        if (userProfile.chronicConditions.isNotEmpty()) {
            Text(
                text = "Chronic Conditions:",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            userProfile.chronicConditions.forEach { condition ->
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Condition",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = condition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            InfoRow(label = "Chronic Conditions", value = "None reported")
        }
    }
}

@Composable
private fun EmergencyContactContent(userProfile: UserProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow(label = "Primary Contact", value = "Not provided")
        InfoRow(label = "Relationship", value = "Not provided")
        InfoRow(label = "Phone Number", value = "Not provided")
        InfoRow(label = "Alternative Contact", value = "Not provided")
        
        OutlinedButton(
            onClick = { /* TODO: Add emergency contact */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Emergency Contact")
        }
    }
}

@Composable
private fun MedicalHistoryContent(
    medicalHistory: List<MedicalRecord>,
    onAddRecord: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (medicalHistory.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = "No history",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No medical history recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            medicalHistory.forEach { record ->
                MedicalRecordItem(record = record)
            }
        }

        OutlinedButton(
            onClick = onAddRecord,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Medical Record")
        }
    }
}

@Composable
private fun RecentPrescriptionsContent(prescriptions: List<Prescription>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (prescriptions.isEmpty()) {
            Text(
                text = "No recent prescriptions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            prescriptions.take(3).forEach { prescription ->
                PrescriptionSummaryItem(prescription = prescription)
            }
            
            if (prescriptions.size > 3) {
                TextButton(
                    onClick = { /* TODO: Navigate to all prescriptions */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View All ${prescriptions.size} Prescriptions")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MedicalRecordItem(record: MedicalRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(record.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (record.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (record.doctor.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dr. ${record.doctor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PrescriptionSummaryItem(prescription: Prescription) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = "Prescription",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prescription.doctorName.ifEmpty { "Unknown Doctor" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${prescription.medicines.size} medicines",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = SimpleDateFormat("MMM dd", Locale.getDefault())
                    .format(Date(prescription.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Sample data class for medical records
data class MedicalRecord(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val doctor: String = "",
    val hospital: String = "",
    val type: String = "" // Surgery, Checkup, Lab Test, etc.
)

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    PrescriptionReaderTheme {
        ProfileScreen(
            userProfile = UserProfile(
                name = "John Doe",
                age = 35,
                gender = "Male",
                bloodGroup = "O+",
                allergies = listOf("Peanuts", "Shellfish"),
                chronicConditions = listOf("Hypertension", "Diabetes Type 2")
            ),
            medicalHistory = listOf(
                MedicalRecord(
                    title = "Annual Checkup",
                    description = "Routine health examination",
                    date = System.currentTimeMillis() - 86400000 * 30,
                    doctor = "Smith",
                    type = "Checkup"
                ),
                MedicalRecord(
                    title = "Blood Test",
                    description = "Complete blood count and lipid profile",
                    date = System.currentTimeMillis() - 86400000 * 60,
                    doctor = "Johnson",
                    type = "Lab Test"
                )
            ),
            recentPrescriptions = listOf(
                Prescription(
                    doctorName = "Dr. Smith",
                    date = System.currentTimeMillis(),
                    medicines = listOf(
                        Medicine(name = "Lisinopril"),
                        Medicine(name = "Metformin")
                    )
                )
            )
        )
    }
}