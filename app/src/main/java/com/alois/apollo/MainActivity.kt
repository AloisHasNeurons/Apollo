package com.alois.apollo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alois.apollo.ui.HistoryScreen
import com.alois.apollo.ui.MainScreen
import com.alois.apollo.ui.ReadOnlyRecapScreen
import com.alois.apollo.ui.RecapScreen
import com.alois.apollo.ui.WorkoutScreen
import com.alois.apollo.ui.WorkoutViewModel
import com.alois.apollo.ui.theme.ApolloTheme

/**
 * Entry point of the application. Sets up the high refresh rate and manages the top-level
 * navigation state between screens.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestHighRefreshRate()
        setContent {
            ApolloTheme {
                val viewModel: WorkoutViewModel = viewModel()
                val activeWorkout by viewModel.activeWorkout.collectAsState()
                val showRecap by viewModel.showRecap.collectAsState()
                val workoutResults by viewModel.workoutResults.collectAsState()

                var viewSessionId by remember { mutableStateOf<Long?>(null) }
                var showHistory by remember { mutableStateOf(false) }

                when {
                    viewSessionId != null -> {
                        ReadOnlyRecapScreen(
                            sessionId = viewSessionId!!,
                            onBack = { viewSessionId = null },
                            viewModel = viewModel
                        )
                    }
                    showHistory -> {
                        HistoryScreen(
                            onBack = { showHistory = false },
                            onViewSession = { id -> viewSessionId = id },
                            viewModel = viewModel
                        )
                    }
                    showRecap && activeWorkout != null -> {
                        RecapScreen(
                            workout = activeWorkout!!,
                            results = workoutResults,
                            onSave = { viewModel.saveWorkout() },
                            onCancel = { viewModel.cancelWorkout() },
                            viewModel = viewModel
                        )
                    }
                    activeWorkout != null -> {
                        WorkoutScreen(
                            onBack = { viewModel.exitWorkout() },
                            onShowRecap = { /* Handled by state */ },
                            viewModel = viewModel
                        )
                    }
                    else -> {
                        MainScreen(
                            onStartWorkout = { /* Handled by state */ },
                            onOpenHistory = { showHistory = true },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun requestHighRefreshRate() {
        val display =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                display
            } else {
                windowManager.defaultDisplay
            }
                ?: return

        val supportedModes = display.supportedModes

        // Find the mode with highest refresh rate
        val highestRefreshMode = supportedModes.maxByOrNull { it.refreshRate }

        highestRefreshMode?.let { mode ->
            val params = window.attributes
            params.preferredDisplayModeId = mode.modeId
            window.attributes = params
        }
    }
}
