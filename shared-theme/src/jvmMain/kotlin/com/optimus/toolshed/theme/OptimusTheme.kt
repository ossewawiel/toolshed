/*
 * Optimus Toolshed - Shared Theme
 * 
 * Material Design 3 theme definitions and design system contracts
 * for consistent visual appearance across all application components.
 */

package com.optimus.toolshed.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Optimus Toolshed color palette following Material Design 3 guidelines
 */
object OptimusColors {
    // Primary brand colors
    val Primary = Color(0xFF1976D2)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFBBDEFB)
    val OnPrimaryContainer = Color(0xFF0D47A1)
    
    // Secondary colors
    val Secondary = Color(0xFF03A9F4)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFE1F5FE)
    val OnSecondaryContainer = Color(0xFF006064)
}

/**
 * Light color scheme for Optimus Toolshed
 */
private val LightColorScheme = lightColorScheme(
    primary = OptimusColors.Primary,
    onPrimary = OptimusColors.OnPrimary,
    primaryContainer = OptimusColors.PrimaryContainer,
    onPrimaryContainer = OptimusColors.OnPrimaryContainer,
    secondary = OptimusColors.Secondary,
    onSecondary = OptimusColors.OnSecondary,
    secondaryContainer = OptimusColors.SecondaryContainer,
    onSecondaryContainer = OptimusColors.OnSecondaryContainer
)

/**
 * Dark color scheme for Optimus Toolshed
 */
private val DarkColorScheme = darkColorScheme(
    primary = OptimusColors.PrimaryContainer,
    onPrimary = OptimusColors.OnPrimaryContainer,
    primaryContainer = OptimusColors.Primary,
    onPrimaryContainer = OptimusColors.OnPrimary,
    secondary = OptimusColors.SecondaryContainer,
    onSecondary = OptimusColors.OnSecondaryContainer,
    secondaryContainer = OptimusColors.Secondary,
    onSecondaryContainer = OptimusColors.OnSecondary
)

/**
 * Main theme composable for Optimus Toolshed application
 */
@Composable
fun OptimusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // Default Material 3 typography
        content = content
    )
}