/*
 * Optimus Toolshed - UI Component Library
 * 
 * Shared component library implementing Material Design 3 components
 * with consistent theming and internationalization for desktop applications.
 */

package com.optimus.toolshed.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.optimus.toolshed.i18n.LocalizationManager
import com.optimus.toolshed.i18n.localized

/**
 * Standard loading indicator with optional message
 */
@Composable
fun OptimusLoadingIndicator(
    message: String = "status.loading".localized(),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Status card for displaying connection status and other states
 */
@Composable
fun OptimusStatusCard(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Standard action button with consistent styling
 */
@Composable
fun OptimusActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            Text(text = text)
        }
    }
}

/**
 * Master/Detail layout container for the main application interface
 */
@Composable
fun OptimusMasterDetailLayout(
    masterContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    masterWeight: Float = 0.3f,
    detailWeight: Float = 0.7f
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Master panel
        Surface(
            modifier = Modifier
                .weight(masterWeight)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            masterContent()
        }
        
        // Divider
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colorScheme.outline
        )
        
        // Detail panel
        Surface(
            modifier = Modifier
                .weight(detailWeight)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface
        ) {
            detailContent()
        }
    }
}