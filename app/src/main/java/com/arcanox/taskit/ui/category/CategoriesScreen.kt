package com.arcanox.taskit.ui.category

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcanox.taskit.ui.task.TaskViewModel
import com.arcanox.taskit.ui.task.CategoryWithCount
import com.arcanox.taskit.data.local.entity.CategoryEntity
import com.arcanox.taskit.util.CategoryUtils
import com.arcanox.taskit.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun CategoriesScreenPreview() {
    TasKitTheme {
        CategoriesScreenContent(
            totalTasks = 12,
            categories = emptyList(),
            searchQuery = "",
            isTablet = false,
            onCategoryClick = {},
            onOpenDrawer = {},
            onAddCategory = { _, _ -> },
            onUpdateCategory = {},
            onDeleteCategory = {},
            onSearchQueryChange = {}
        )
    }
}

@Composable
fun CategoriesScreen(
    viewModel: TaskViewModel,
    isTablet: Boolean,
    onCategoryClick: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val categories by viewModel.categoriesWithCounts.collectAsState()
    val searchQuery by viewModel.categorySearchQuery.collectAsState()
    
    CategoriesScreenContent(
        totalTasks = stats.totalTasks,
        categories = categories,
        searchQuery = searchQuery,
        isTablet = isTablet,
        onCategoryClick = onCategoryClick,
        onOpenDrawer = onOpenDrawer,
        onAddCategory = { name, color -> viewModel.addCategory(name, color) },
        onUpdateCategory = { viewModel.updateCategory(it) },
        onDeleteCategory = { viewModel.deleteCategory(it) },
        onSearchQueryChange = { viewModel.onCategorySearchQueryChange(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreenContent(
    totalTasks: Int,
    categories: List<CategoryWithCount>,
    searchQuery: String,
    isTablet: Boolean,
    onCategoryClick: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onAddCategory: (String, Int) -> Unit,
    onUpdateCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onSearchQueryChange: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                onAddCategory(name, color)
                showAddDialog = false
            }
        )
    }

    if (categoryToEdit != null) {
        EditCategoryDialog(
            category = categoryToEdit!!,
            onDismiss = { categoryToEdit = null },
            onConfirm = { updatedCategory ->
                onUpdateCategory(updatedCategory)
                categoryToEdit = null
            },
            onDelete = {
                onDeleteCategory(categoryToEdit!!)
                categoryToEdit = null
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
                }
                
                if (isSearchExpanded) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        placeholder = { Text("Search categories...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { 
                                onSearchQueryChange("")
                                isSearchExpanded = false 
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    )
                } else {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                    IconButton(onClick = { isSearchExpanded = true }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(if (isTablet) 200.dp else 160.dp),
                    modifier = Modifier.widthIn(max = 1000.dp),
                    contentPadding = PaddingValues(if (isTablet) 32.dp else 16.dp, 16.dp, if (isTablet) 32.dp else 16.dp, 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // All Tasks card
                    if (!isSearchExpanded || "All Tasks".contains(searchQuery, ignoreCase = true)) {
                        item(key = "all_tasks") {
                            CategoryCard(
                                title = "All Tasks",
                                icon = Icons.Outlined.Checklist,
                                taskCount = totalTasks,
                                color = MaterialTheme.colorScheme.primary,
                                index = 0
                            ) { onCategoryClick("All Tasks") }
                        }
                    }

                    // Add Category card
                    item(key = "add_category_card") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clickable { showAddDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Category",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "New Category",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // User categories
                    items(categories, key = { it.category.id }) { categoryWithCount ->
                        val index = categories.indexOf(categoryWithCount) + 2
                        CategoryCard(
                            title = categoryWithCount.category.name,
                            icon = CategoryUtils.getIconByName(categoryWithCount.category.icon),
                            taskCount = categoryWithCount.taskCount,
                            color = CategoryUtils.getColor(categoryWithCount.category.color),
                            index = index,
                            onLongClick = { categoryToEdit = categoryWithCount.category },
                            onClick = { onCategoryClick(categoryWithCount.category.name) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCard(
    title: String,
    icon: ImageVector,
    taskCount: Int,
    color: Color,
    index: Int,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.8f) + fadeIn(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("$taskCount Tasks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private val CATEGORY_COLORS = listOf(
    CatWork, CatPersonal, CatStudy, CatShopping, CatFitness, CatIdeas, Primary,
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5),
    Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFFFEB3B),
    Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFF795548)
)

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(CATEGORY_COLORS[0]) }
    var r by remember { mutableFloatStateOf(selectedColor.red) }
    var g by remember { mutableFloatStateOf(selectedColor.green) }
    var b by remember { mutableFloatStateOf(selectedColor.blue) }
    var useCustomColor by remember { mutableStateOf(false) }

    val currentColor = if (useCustomColor) Color(r, g, b) else selectedColor

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Category Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Select Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Box(modifier = Modifier.height(110.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CATEGORY_COLORS) { color ->
                            ColorCircle(
                                color = color,
                                isSelected = !useCustomColor && selectedColor == color,
                                onClick = {
                                    useCustomColor = false
                                    selectedColor = color
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useCustomColor, onCheckedChange = { useCustomColor = it })
                    Text("Custom Color Maker", style = MaterialTheme.typography.bodyMedium)
                }

                if (useCustomColor) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorSlider(label = "Red", value = r, onValueChange = { r = it }, color = Color.Red)
                        ColorSlider(label = "Green", value = g, onValueChange = { g = it }, color = Color.Green)
                        ColorSlider(label = "Blue", value = b, onValueChange = { b = it }, color = Color.Blue)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(currentColor))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Hex: #${Integer.toHexString(currentColor.toArgb()).uppercase().takeLast(6)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name, currentColor.toArgb()) },
                enabled = name.isNotBlank()
            ) {
                Text("Add", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun EditCategoryDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (CategoryEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    val initialColor = CategoryUtils.getColor(category.color)
    var selectedColor by remember { mutableStateOf(initialColor) }
    var r by remember { mutableFloatStateOf(initialColor.red) }
    var g by remember { mutableFloatStateOf(initialColor.green) }
    var b by remember { mutableFloatStateOf(initialColor.blue) }
    var useCustomColor by remember { mutableStateOf(false) }

    val currentColor = if (useCustomColor) Color(r, g, b) else selectedColor

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Category", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Category Name") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Select Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Box(modifier = Modifier.height(110.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CATEGORY_COLORS) { color ->
                            ColorCircle(
                                color = color,
                                isSelected = !useCustomColor && selectedColor == color,
                                onClick = {
                                    useCustomColor = false
                                    selectedColor = color
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useCustomColor, onCheckedChange = { useCustomColor = it })
                    Text("Custom Color Maker", style = MaterialTheme.typography.bodyMedium)
                }

                if (useCustomColor) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorSlider(label = "Red", value = r, onValueChange = { r = it }, color = Color.Red)
                        ColorSlider(label = "Green", value = g, onValueChange = { g = it }, color = Color.Green)
                        ColorSlider(label = "Blue", value = b, onValueChange = { b = it }, color = Color.Blue)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(currentColor))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Hex: #${Integer.toHexString(currentColor.toArgb()).uppercase().takeLast(6)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Category")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(category.copy(name = name, color = currentColor.toArgb())) },
                enabled = name.isNotBlank()
            ) {
                Text("Update", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Ensure it's a perfect square for the clip
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ColorSlider(label: String, value: Float, onValueChange: (Float) -> Unit, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                (value * 255).toInt().toString(),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}
