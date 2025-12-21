package com.example.mycalisthenics.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycalisthenics.data.model.WorkoutConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onWorkoutSelected: (WorkoutConfig) -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val workouts by viewModel.availableWorkouts.collectAsState()
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importWorkout(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Workouts") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { launcher.launch("application/json") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(workouts) { workout ->
                ListItem(
                    headlineContent = { Text(workout.name) },
                    supportingContent = { Text(workout.focus) },
                    modifier = Modifier.clickable { onWorkoutSelected(workout) }
                )
            }
        }
    }
}
