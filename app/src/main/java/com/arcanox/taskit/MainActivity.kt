package com.arcanox.taskit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arcanox.taskit.ui.task.TaskEditScreen
import com.arcanox.taskit.ui.task.TaskScreen
import com.arcanox.taskit.ui.task.TaskViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.arcanox.taskit.ui.update.UpdateViewModel
import com.arcanox.taskit.ui.update.ForceUpdateScreen
import com.arcanox.taskit.ui.theme.TasKitTheme
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Subscribe to global topic "all" for general notifications
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) "Subscribed to 'all' topic" else "Subscription to 'all' failed"
                Log.d("MainActivity", msg)
            }

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDark) }

            TasKitTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val viewModel: TaskViewModel = hiltViewModel()
                val updateViewModel: UpdateViewModel = hiltViewModel()
                val updateState by updateViewModel.uiState.collectAsState()
                val lifecycleOwner = LocalLifecycleOwner.current

                // Check for updates on startup
                LaunchedEffect(Unit) {
                    updateViewModel.checkUpdate()
                }

                // Check for updates on foreground
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            updateViewModel.checkUpdate()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // Force Update Logic
                val isForceLocked = remember(updateState) {
                    if ((!updateState.isUpToDate) && updateState.isMandatory && (updateState.firstDetectedTime > 0)) {
                        val daysRemaining = 7 - ((System.currentTimeMillis() - updateState.firstDetectedTime) / (1000 * 60 * 60 * 24))
                        daysRemaining <= 0
                    } else false
                }

                if (isForceLocked) {
                    ForceUpdateScreen(
                        versionName = updateState.latestVersionName,
                        changelog = updateState.changelog,
                    ) {
                        updateViewModel.startDownload()
                    }
                } else {
                    NavHost(navController = navController, startDestination = "tasks") {
                        composable("tasks") {
                            TaskScreen(
                                viewModel = viewModel,
                                updateViewModel = updateViewModel,
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                onAddTask = { navController.navigate("task_edit/-1") },
                            ) { taskId ->
                                navController.navigate("task_edit/$taskId")
                            }
                        }
                        composable(
                            "task_edit/{taskId}",
                            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                            TaskEditScreen(
                                taskId = taskId,
                                viewModel = viewModel
                            ) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
