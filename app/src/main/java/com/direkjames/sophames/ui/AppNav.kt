package com.direkjames.sophames.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.direkjames.sophames.ui.components.FloatingNavBar
import com.direkjames.sophames.ui.components.ScreenBackground
import com.direkjames.sophames.ui.screens.CountdownsScreen
import com.direkjames.sophames.ui.screens.HomeScreen
import com.direkjames.sophames.ui.screens.MemoriesScreen
import com.direkjames.sophames.ui.screens.MemoryDetailScreen
import com.direkjames.sophames.ui.screens.MemoryEditorScreen
import com.direkjames.sophames.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val COUNTDOWNS = "countdowns"
    const val MEMORIES = "memories"
    const val SETTINGS = "settings"
    const val NEW_MEMORY = "new_memory"
    const val DETAIL = "memory/{id}"
    const val EDIT = "memory/{id}/edit"

    fun detail(id: String) = "memory/$id"
    fun edit(id: String) = "memory/$id/edit"
}

@Composable
fun MainNav(vm: MainViewModel, isDark: Boolean) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showBar = route == Routes.HOME || route == Routes.COUNTDOWNS || route == Routes.MEMORIES

    val goTab: (String) -> Unit = { destination ->
        nav.navigate(destination) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(300)) { it / 14 } },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(240)) },
            popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(260)) { it / 14 } },
        ) {
            composable(Routes.HOME) {
                ScreenBackground {
                    HomeScreen(
                        vm = vm,
                        isDark = isDark,
                        onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                        onOpenCountdowns = { goTab(Routes.COUNTDOWNS) },
                        onOpenMemories = { goTab(Routes.MEMORIES) },
                        onOpenMemory = { id -> nav.navigate(Routes.detail(id)) },
                        onNewMemory = { nav.navigate(Routes.NEW_MEMORY) },
                    )
                }
            }
            composable(Routes.COUNTDOWNS) {
                ScreenBackground { CountdownsScreen(vm) }
            }
            composable(Routes.MEMORIES) {
                ScreenBackground {
                    MemoriesScreen(
                        vm = vm,
                        onOpenMemory = { id -> nav.navigate(Routes.detail(id)) },
                        onNewMemory = { nav.navigate(Routes.NEW_MEMORY) },
                    )
                }
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(vm = vm, onBack = { nav.popBackStack() })
            }
            composable(Routes.NEW_MEMORY) {
                MemoryEditorScreen(vm = vm, memoryId = null, onClose = { nav.popBackStack() })
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                MemoryDetailScreen(
                    vm = vm,
                    memoryId = id,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(Routes.edit(id)) },
                )
            }
            composable(
                route = Routes.EDIT,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                MemoryEditorScreen(vm = vm, memoryId = id, onClose = { nav.popBackStack() })
            }
        }

        AnimatedVisibility(
            visible = showBar,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200)),
        ) {
            FloatingNavBar(current = route, onSelect = goTab)
        }
    }
}
