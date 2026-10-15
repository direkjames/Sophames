package com.direkjames.sophames

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.direkjames.sophames.ui.AppViewModelProvider
import com.direkjames.sophames.ui.MainViewModel
import com.direkjames.sophames.ui.SophamesRoot

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Hold the splash until settings have loaded so the first frame is already the right theme.
        splash.setKeepOnScreenCondition { viewModel.settings.value == null }
        setContent { SophamesRoot(viewModel) }
    }
}
