package com.arcanox.taskit.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arcanox.taskit.ui.theme.Background
import com.arcanox.taskit.ui.theme.TextSecondary

@Composable
fun CalendarScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = TextSecondary.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(24.dp))
        Text("Calendar Coming Soon", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
    }
}
