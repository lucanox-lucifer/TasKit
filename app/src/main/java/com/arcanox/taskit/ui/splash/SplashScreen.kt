package com.arcanox.taskit.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcanox.taskit.R
import com.arcanox.taskit.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun CustomSplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "Alpha"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F10)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim).scale(scaleAnim)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "TasKit Logo",
                modifier = Modifier.size(160.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "TasKit",
                style = MaterialTheme.typography.headlineLarge,
                color = Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            // Subtitle
            Text(
                text = "Organize your productivity",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
        }
        
        // Bottom Branding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Powered by Lucanox",
                style = MaterialTheme.typography.labelSmall,
                color = Primary.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp
            )
        }
    }
}
