package com.alois.apollo.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alois.apollo.data.model.WorkoutConfig

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
