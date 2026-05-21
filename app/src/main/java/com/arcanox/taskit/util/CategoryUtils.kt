package com.arcanox.taskit.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryUtils {
    fun getIconByName(name: String?): ImageVector {
        return when (name) {
            "WorkOutline" -> Icons.Outlined.WorkOutline
            "PersonOutline" -> Icons.Outlined.PersonOutline
            "Book" -> Icons.Outlined.Book
            "ShoppingCart" -> Icons.Outlined.ShoppingCart
            "FitnessCenter" -> Icons.Outlined.FitnessCenter
            "Lightbulb" -> Icons.Outlined.Lightbulb
            "Checklist" -> Icons.Outlined.Checklist
            "Favorite" -> Icons.Outlined.FavoriteBorder
            "Home" -> Icons.Outlined.Home
            "School" -> Icons.Outlined.School
            "Star" -> Icons.Outlined.StarOutline
            else -> Icons.Outlined.Category
        }
    }

    fun getColor(colorInt: Int?): Color {
        return if (colorInt != null) Color(colorInt) else Color.Gray
    }
}
