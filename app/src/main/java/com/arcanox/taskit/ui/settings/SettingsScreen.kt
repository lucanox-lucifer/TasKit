package com.arcanox.taskit.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcanox.taskit.ui.theme.*
import com.arcanox.taskit.ui.update.UpdateViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToVersion: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val settingsData by settingsViewModel.settingsData.collectAsState()
    val updateState by updateViewModel.uiState.collectAsState()
    var showDownloadConfirm by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        updateViewModel.checkUpdate()
    }

    if (showDownloadConfirm) {
        AlertDialog(
            onDismissRequest = { showDownloadConfirm = false },
            title = { Text("Confirm Download") },
            text = { Text("Do you want to download TasKit v${updateState.latestVersionName}?") },
            confirmButton = {
                TextButton(onClick = {
                    updateViewModel.startDownload()
                    showDownloadConfirm = false
                }) { Text("Download") }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadConfirm = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            settingsData?.let { data ->
                // Appearance Section
                item {
                    AnimatedSettingsGroup(title = "Appearance", index = 0) {
                        SettingsToggleItem(
                            title = "Dark Mode",
                            subtitle = if (data.appTheme == "Dark") "Premium Dark UI enabled" else "High-contrast Light UI enabled",
                            icon = Icons.Default.Palette,
                            checked = data.appTheme == "Dark",
                            onCheckedChange = { 
                                settingsViewModel.setAppTheme(if (it) "Dark" else "Light")
                            }
                        )
                    }
                }

                // Update & OTA Section
                item {
                    AnimatedSettingsGroup(title = "Update & OTA", index = 1) {
                        val updateColor = if (!updateState.isUpToDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        
                        SettingsItem(
                            title = if (updateState.isChecking) "Checking for updates..." else "Check for OTA Updates",
                            subtitle = when {
                                updateState.error != null -> "Connection error"
                                updateState.isUpToDate -> "Your app is up to date"
                                else -> "New version available: ${updateState.latestVersionName}"
                            },
                            icon = Icons.Default.SystemUpdate,
                            contentColor = updateColor,
                            onClick = { 
                                if (!updateState.isChecking) {
                                    if (!updateState.isUpToDate) {
                                        onNavigateToVersion() // Open version screen for details
                                    } else {
                                        updateViewModel.checkUpdate()
                                    }
                                }
                            }
                        )
                        
                        if (!updateState.isUpToDate) {
                            SettingsItem(
                                title = if (updateState.isDownloading) "Downloading... ${updateState.downloadProgress}%" else "Download available update",
                                subtitle = if (updateState.isDownloading) "Check notification for progress" else "Click to start background download",
                                icon = Icons.Default.CloudDownload,
                                contentColor = MaterialTheme.colorScheme.primary,
                                onClick = { 
                                    if (!updateState.isDownloading) {
                                        showDownloadConfirm = true
                                    }
                                }
                            )
                        }
                    }
                }

                // About Section
                item {
                    AnimatedSettingsGroup(title = "About", index = 2) {
                        SettingsItem(
                            title = "App Version",
                            subtitle = updateState.currentVersionName,
                            icon = Icons.Default.Info,
                            onClick = onNavigateToVersion
                        )
                        SettingsItem(
                            title = "Developer",
                            subtitle = "LucaBox Studio",
                            icon = Icons.Default.Code,
                            onClick = onNavigateToDeveloper
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun AnimatedSettingsGroup(title: String, index: Int, content: @Composable ColumnScope.() -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally() + fadeIn(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (contentColor == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.primary else contentColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = contentColor, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
