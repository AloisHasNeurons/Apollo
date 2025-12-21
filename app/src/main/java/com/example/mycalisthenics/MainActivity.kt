package com.example.mycalisthenics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycalisthenics.ui.WorkoutListScreen
import com.example.mycalisthenics.ui.WorkoutScreen
import com.example.mycalisthenics.ui.WorkoutViewModel
import com.example.mycalisthenics.ui.theme.MyCalisthenicsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCalisthenicsTheme {
                val viewModel: WorkoutViewModel = viewModel()
                val activeWorkout by viewModel.activeWorkout.collectAsState()

                if (activeWorkout == null) {
                    WorkoutListScreen(
                        onWorkoutSelected = { workout ->
                            viewModel.startWorkout(workout)
                        },
                        viewModel = viewModel
                    )
                } else {
                    WorkoutScreen(
                        onBack = {
                            viewModel.exitWorkout()
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
