package com.arcanox.taskit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun TasKitBottomNavBar(
    currentRoute: String,
    isTablet: Boolean,
    onNavigate: (String) -> Unit,
    onAddTask: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = if (isTablet) 32.dp else 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .height(72.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = if (currentRoute == "home" || currentRoute.startsWith("category/")) Icons.Filled.Home else Icons.Outlined.Home,
                    label = "Home",
                    selected = currentRoute == "home" || currentRoute.startsWith("category/"),
                    onClick = { onNavigate("home") }
                )
                NavItem(
                    icon = if (currentRoute == "categories") Icons.Filled.GridView else Icons.Outlined.GridView,
                    label = "Categories",
                    selected = currentRoute == "categories",
                    onClick = { onNavigate("categories") }
                )

                // Space for FAB
                Spacer(Modifier.width(64.dp))

                NavItem(
                    icon = if (currentRoute == "calendar") Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    label = "Calendar",
                    selected = currentRoute == "calendar",
                    onClick = { onNavigate("calendar") }
                )
                NavItem(
                    icon = if (currentRoute == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Settings",
                    selected = currentRoute == "settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }

        // Floating Center Add Button
        Box(
            modifier = Modifier
                .offset(y = (-36).dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onAddTask),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (selected) 1.2f else 1.0f, label = "Scale")
    val tint by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "Color")
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )
        if (selected) {
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
