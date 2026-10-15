package com.direkjames.sophames.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.direkjames.sophames.data.ThemeMode
import com.direkjames.sophames.ui.screens.WelcomeScreen
import com.direkjames.sophames.ui.theme.SophamesTheme

@Composable
fun SophamesRoot(vm: MainViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val s = settings ?: return

    val dark = when (s.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    SophamesTheme(darkTheme = dark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Crossfade(targetState = s.onboarded, animationSpec = tween(500), label = "root") { onboarded ->
                if (onboarded) {
                    MainNav(vm = vm, isDark = dark)
                } else {
                    WelcomeScreen(settings = s, today = today, onEnter = { vm.setOnboarded(true) })
                }
            }
        }
    }
}
