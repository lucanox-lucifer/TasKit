package com.arcanox.taskit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.arcanox.taskit.ui.calendar.CalendarScreen
import com.arcanox.taskit.ui.category.CategoriesScreen
import com.arcanox.taskit.ui.components.TasKitBottomNavBar
import com.arcanox.taskit.ui.components.TasKitDrawer
import com.arcanox.taskit.ui.settings.DeveloperScreen
import com.arcanox.taskit.ui.settings.SettingsScreen
import com.arcanox.taskit.ui.settings.SettingsViewModel
import com.arcanox.taskit.ui.settings.VersionScreen
import com.arcanox.taskit.ui.splash.CustomSplashScreen
import com.arcanox.taskit.ui.task.TaskEditScreen
import com.arcanox.taskit.ui.task.TaskScreen
import com.arcanox.taskit.ui.task.TaskViewModel
import com.arcanox.taskit.ui.theme.TasKitTheme
import com.arcanox.taskit.ui.update.ForceUpdateScreen
import com.arcanox.taskit.ui.update.UpdateViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                Log.d("MainActivity", if (task.isSuccessful) "Subscribed to 'all'" else "Subscription failed")
            }

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsData by settingsViewModel.settingsData.collectAsState()
            val darkTheme = settingsData?.appTheme == "Dark"

            TasKitTheme(darkTheme = darkTheme) {
                if (showSplash) {
                    CustomSplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    val navController = rememberNavController()
                    val viewModel: TaskViewModel = hiltViewModel()
                    val updateViewModel: UpdateViewModel = hiltViewModel()
                    val updateState by updateViewModel.uiState.collectAsState()
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
                    val categories by viewModel.categories.collectAsState()

                    // Responsive sizing
                    val configuration = LocalConfiguration.current
                    val isTablet = configuration.screenWidthDp > 600

                    LaunchedEffect(Unit) { updateViewModel.checkUpdate() }

                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) updateViewModel.checkUpdate()
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }

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
                        ) { updateViewModel.startDownload() }
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            scrimColor = Color.Black.copy(alpha = 0.5f),
                            drawerContent = {
                                TasKitDrawer(
                                    categories = categories,
                                    isTablet = isTablet,
                                    onNavigate = { route -> 
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onCloseDrawer = { scope.launch { drawerState.close() } }
                                )
                            }
                        ) {
                            Scaffold(
                                containerColor = MaterialTheme.colorScheme.background,
                                bottomBar = {
                                    if (currentRoute != "task_edit/{taskId}") {
                                        TasKitBottomNavBar(
                                            currentRoute = currentRoute,
                                            isTablet = isTablet,
                                            onNavigate = { route -> 
                                                navController.navigate(route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            onAddTask = { navController.navigate("task_edit/-1") }
                                        )
                                    }
                                }
                            ) { paddingValues ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(paddingValues)
                                ) {
                                    NavHost(
                                        navController = navController, 
                                        startDestination = "home",
                                        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally() },
                                        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally() }
                                    ) {
                                        composable("home") {
                                            viewModel.selectCategory(null)
                                            TaskScreen(
                                                viewModel = viewModel,
                                                isTablet = isTablet,
                                                onEditTask = { taskId -> navController.navigate("task_edit/$taskId") },
                                                onOpenDrawer = { scope.launch { drawerState.open() } }
                                            )
                                        }
                                        composable(
                                            "category/{categoryName}",
                                            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                                        ) { backStackEntry ->
                                            val categoryName = backStackEntry.arguments?.getString("categoryName")
                                            LaunchedEffect(categoryName) {
                                                viewModel.selectCategory(categoryName)
                                            }
                                            TaskScreen(
                                                viewModel = viewModel,
                                                isTablet = isTablet,
                                                onEditTask = { taskId -> navController.navigate("task_edit/$taskId") },
                                                onOpenDrawer = { scope.launch { drawerState.open() } }
                                            )
                                        }
                                        composable("categories") {
                                            CategoriesScreen(
                                                viewModel = viewModel,
                                                isTablet = isTablet,
                                                onCategoryClick = { categoryName ->
                                                    if (categoryName == "All Tasks") {
                                                        navController.navigate("home")
                                                    } else {
                                                        navController.navigate("category/$categoryName")
                                                    }
                                                },
                                                onOpenDrawer = { scope.launch { drawerState.open() } }
                                            )
                                        }
                                        composable("calendar") {
                                            CalendarScreen()
                                        }
                                        composable("settings") {
                                            SettingsScreen(
                                                onBack = { navController.popBackStack() },
                                                onNavigateToDeveloper = { navController.navigate("developer") },
                                                onNavigateToVersion = { navController.navigate("version") }
                                            )
                                        }
                                        composable("developer") {
                                            DeveloperScreen(onBack = { navController.popBackStack() })
                                        }
                                        composable("version") {
                                            VersionScreen(onBack = { navController.popBackStack() })
                                        }
                                        composable(
                                            "task_edit/{taskId}",
                                            arguments = listOf(navArgument("taskId") { type = NavType.IntType }),
                                            enterTransition = { slideInVertically(initialOffsetY = { it }) },
                                            exitTransition = { slideOutVertically(targetOffsetY = { it }) }
                                        ) { backStackEntry ->
                                            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
                                            TaskEditScreen(
                                                taskId = taskId,
                                                viewModel = viewModel
                                            ) { navController.popBackStack() }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
