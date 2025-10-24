package com.emulsify.prescriptionreader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Medical color palette
private val MedicalPrimary = Color(0xFF00BCD4)          // Light teal
private val MedicalPrimaryDark = Color(0xFF0097A7)      // Darker teal
private val MedicalSecondary = Color(0xFF4CAF50)        // Medical green
private val MedicalSecondaryDark = Color(0xFF388E3C)    // Darker green
private val MedicalAccent = Color(0XFFFF5722)           // Medical orange

private val MedicalBackground = Color(0xFFFAFAFA)       // Soft white
private val MedicalSurface = Color(0xFFFFFFFF)          // Pure white
private val MedicalSurfaceVariant = Color(0xFFF5F5F5)   // Light grey

private val MedicalTextPrimary = Color(0xFF212121)      // Dark grey
private val MedicalTextSecondary = Color(0xFF757575)    // Medium grey

private val DarkMedicalPrimary = Color(0xFF4DD0E1)      // Lighter teal for dark
private val DarkMedicalSecondary = Color(0xFF81C784)    // Lighter green for dark
private val DarkMedicalBackground = Color(0xFF121212)   // Dark background
private val DarkMedicalSurface = Color(0xFF1E1E1E)      // Dark surface

private val LightColorScheme = lightColorScheme(
    primary = MedicalPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = MedicalPrimaryDark,
    
    secondary = MedicalSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = MedicalSecondaryDark,
    
    tertiary = MedicalAccent,
    onTertiary = Color.White,
    
    error = Color(0xFFB00020),
    onError = Color.White,
    
    background = MedicalBackground,
    onBackground = MedicalTextPrimary,
    
    surface = MedicalSurface,
    onSurface = MedicalTextPrimary,
    surfaceVariant = MedicalSurfaceVariant,
    onSurfaceVariant = MedicalTextSecondary,
    
    outline = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkMedicalPrimary,
    onPrimary = Color.Black,
    primaryContainer = MedicalPrimaryDark,
    onPrimaryContainer = Color.White,
    
    secondary = DarkMedicalSecondary,
    onSecondary = Color.Black,
    secondaryContainer = MedicalSecondaryDark,
    onSecondaryContainer = Color.White,
    
    tertiary = MedicalAccent,
    onTertiary = Color.White,
    
    error = Color(0xFFCF6679),
    onError = Color.Black,
    
    background = DarkMedicalBackground,
    onBackground = Color.White,
    
    surface = DarkMedicalSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
    
    outline = Color(0xFF424242)
)

@Composable
fun PrescriptionReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}