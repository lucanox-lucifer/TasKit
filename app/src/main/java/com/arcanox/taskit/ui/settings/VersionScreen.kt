package com.arcanox.taskit.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcanox.taskit.ui.theme.*
import com.arcanox.taskit.ui.update.UpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionScreen(
    onBack: () -> Unit,
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val updateState by updateViewModel.uiState.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Download") },
            text = { Text("Are you sure you want to download TasKit v${updateState.latestVersionName}?") },
            confirmButton = {
                TextButton(onClick = {
                    updateViewModel.startDownload()
                    showConfirmDialog = false
                }) { Text("Download") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Version", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // ABOUT APP SECTION
            Text(
                "ABOUT APP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("Current Version", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(updateState.currentVersionName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Build: ${updateState.currentVersionCode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            ChangelogItem(
                version = "v${updateState.currentVersionName} (Current)",
                changes = listOf(
                    "Improved category color picker with RGB Maker",
                    "Fixed task reminder notifications",
                    "Redesigned Settings and Sidebar UI",
                    "Added support for light/dark theme persistence",
                    "Implemented robust OTA update system"
                )
            )

            Spacer(Modifier.height(32.dp))

            // UPCOMING UPDATE SECTION
            if (!updateState.isUpToDate) {
                Text(
                    "UPCOMING UPDATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("New Version Available: v${updateState.latestVersionName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        Text("What's New:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            updateState.changelog.ifEmpty { "Performance improvements and bug fixes." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(20.dp))

                        if (updateState.isDownloading) {
                            Column {
                                LinearProgressIndicator(
                                    progress = { updateState.downloadProgress / 100f },
                                    modifier = Modifier.fillMaxWidth().clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Downloading... ${updateState.downloadProgress}%", style = MaterialTheme.typography.labelSmall)
                                    TextButton(
                                        onClick = { updateViewModel.cancelDownload() },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Cancel") }
                                }
                            }
                        } else {
                            Button(
                                onClick = { showConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Download v${updateState.latestVersionName}")
                            }
                        }
                    }
                }
            } else {
                Text(
                    "No updates available at this time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ChangelogItem(version: String, changes: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(version, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            changes.forEach { change ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                    Text(change, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
