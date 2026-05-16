package com.arcanox.taskit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arcanox.taskit.ui.task.TaskEditScreen
import com.arcanox.taskit.ui.task.TaskScreen
import com.arcanox.taskit.ui.task.TaskViewModel
import com.arcanox.taskit.ui.theme.TasKitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDark) }

            TasKitTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val viewModel: TaskViewModel = hiltViewModel()

                NavHost(navController = navController, startDestination = "tasks") {
                    composable("tasks") {
                        TaskScreen(
                            viewModel = viewModel,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it },
                            onAddTask = { navController.navigate("task_edit/-1") },
                            onEditTask = { taskId -> navController.navigate("task_edit/$taskId") }
                        )
                    }
                    composable(
                        "task_edit/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                        TaskEditScreen(
                            taskId = taskId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
