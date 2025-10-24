package com.emulsify.prescriptionreader.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emulsify.prescriptionreader.ui.theme.PrescriptionReaderTheme
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit = {}
) {
    var loadingProgress by remember { mutableStateOf(0f) }
    
    // Rotation animation for medical cross
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse animation for the glow effect
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Progress animation
    LaunchedEffect(Unit) {
        for (i in 0..100) {
            loadingProgress = i / 100f
            delay(30)
        }
        delay(500) // Small delay before completing
        onLoadingComplete()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Medical Cross with glow and rotation
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size((100 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                )
                
                // Rotating medical cross
                MedicalCrossIcon(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(rotation),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App title
            Text(
                text = "Prescription Reader",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Analyzing your medical prescriptions",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = loadingProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${(loadingProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading messages
            val loadingMessages = listOf(
                "Initializing OCR engine...",
                "Loading medical database...",
                "Preparing analysis tools...",
                "Ready to scan prescriptions!"
            )
            
            val currentMessageIndex = (loadingProgress * (loadingMessages.size - 1)).toInt()
            
            Text(
                text = loadingMessages[currentMessageIndex],
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MedicalCrossIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Vertical bar
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(60.dp)
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.small
                )
        )
        
        // Horizontal bar
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(20.dp)
                .background(
                    color = color,
                    shape = MaterialTheme.shapes.small
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingScreenPreview() {
    PrescriptionReaderTheme {
        LoadingScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingScreenDarkPreview() {
    PrescriptionReaderTheme {
        LoadingScreen()
    }
}