package com.example.mycalisthenics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycalisthenics.ui.*
import com.example.mycalisthenics.ui.theme.MyCalisthenicsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCalisthenicsTheme {
                val viewModel: WorkoutViewModel = viewModel()
                val activeWorkout by viewModel.activeWorkout.collectAsState()
                val showRecap by viewModel.showRecap.collectAsState()
                val workoutResults by viewModel.workoutResults.collectAsState()
                
                var viewSessionId by remember { mutableStateOf<Long?>(null) }

                when {
                    viewSessionId != null -> {
                        ReadOnlyRecapScreen(
                            sessionId = viewSessionId!!,
                            onBack = { viewSessionId = null },
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
                            onViewSession = { id -> viewSessionId = id },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
