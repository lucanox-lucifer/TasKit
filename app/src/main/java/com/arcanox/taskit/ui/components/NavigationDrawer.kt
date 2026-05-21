package com.arcanox.taskit.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.arcanox.taskit.R
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.ui.update.UpdateViewModel
import com.arcanox.taskit.util.CategoryUtils
import com.arcanox.taskit.ui.theme.*

@Composable
fun TasKitDrawer(
    categories: List<CategoryEntity>,
    isTablet: Boolean,
    updateViewModel: UpdateViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val updateState by updateViewModel.uiState.collectAsState()
    var isCategoriesExpanded by remember { mutableStateOf(false) }
    
    val rotationState by animateFloatAsState(
        targetValue = if (isCategoriesExpanded) 180f else 0f,
        label = "Rotation"
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        modifier = Modifier.width(if (isTablet) 340.dp else 280.dp),
        drawerTonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header with Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "TasKit ✨",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Profile Section
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hi, welcome", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Guest", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Navigation Items
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DrawerItemRow(
                    title = "Home",
                    icon = Icons.Outlined.Home,
                    color = MaterialTheme.colorScheme.onSurface,
                    onClick = { onNavigate("home"); onCloseDrawer() }
                )
                DrawerItemRow(
                    title = "Categories",
                    icon = Icons.Outlined.GridView,
                    color = MaterialTheme.colorScheme.onSurface,
                    onClick = { onNavigate("categories"); onCloseDrawer() }
                )
                DrawerItemRow(
                    title = "Calendar",
                    icon = Icons.Outlined.CalendarMonth,
                    color = MaterialTheme.colorScheme.onSurface,
                    onClick = { onNavigate("calendar"); onCloseDrawer() }
                )
                DrawerItemRow(
                    title = "Settings",
                    icon = Icons.Outlined.Settings,
                    color = MaterialTheme.colorScheme.onSurface,
                    onClick = { onNavigate("settings"); onCloseDrawer() }
                )
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            // Expandable Categories Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isCategoriesExpanded = !isCategoriesExpanded }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "QUICK ACCESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp).rotate(rotationState)
                )
            }

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    AnimatedVisibility(
                        visible = isCategoriesExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            categories.forEach { category ->
                                DrawerItemRow(
                                    title = category.name,
                                    icon = CategoryUtils.getIconByName(category.icon),
                                    color = CategoryUtils.getColor(category.color),
                                    onClick = { onNavigate("category/${category.name}"); onCloseDrawer() }
                                )
                            }
                        }
                    }
                }
            }

            // About Section (Integrated into Sidebar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate("settings"); onCloseDrawer() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("About TasKit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("by LucaBox Studio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    updateState.currentVersionName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun DrawerItemRow(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}
