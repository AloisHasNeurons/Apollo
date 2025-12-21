package com.example.mycalisthenics.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycalisthenics.data.local.ExerciseRecord
import com.example.mycalisthenics.data.model.WorkoutConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapScreen(
    workout: WorkoutConfig,
    results: Map<String, List<Int>>,
    isReadOnly: Boolean = false,
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    viewModel: WorkoutViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isReadOnly) "Session Summary" else "Workout Recap") },
                actions = {
                    if (isReadOnly) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!isReadOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(workout.exercises) { exercise ->
                val sets = results[exercise.id] ?: listOf(0, 0, 0)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (viewModel.isFrench()) exercise.nameFr else exercise.nameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            sets.forEachIndexed { index, value ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Set ${index + 1}", style = MaterialTheme.typography.labelSmall)
                                    if (isReadOnly) {
                                        Text(
                                            text = value.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        var textValue by remember { mutableStateOf(value.toString()) }
                                        OutlinedTextField(
                                            value = textValue,
                                            onValueChange = {
                                                textValue = it
                                                it.toIntOrNull()?.let { newVal ->
                                                    viewModel.updateResult(exercise.id, index, newVal)
                                                }
                                            },
                                            modifier = Modifier.width(70.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
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

@Composable
fun ReadOnlyRecapScreen(
    sessionId: Long,
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    var records by remember { mutableStateOf<List<ExerciseRecord>>(emptyList()) }
    val workouts by viewModel.availableWorkouts.collectAsState()
    val history by viewModel.history.collectAsState()
    
    val session = history.find { it.id == sessionId }
    val workoutConfig = workouts.find { it.id == session?.workoutId }

    LaunchedEffect(sessionId) {
        records = viewModel.getSessionDetails(sessionId)
    }

    if (workoutConfig != null && session != null) {
        val resultsMap = records.associate { it.exerciseId to it.sets }
        RecapScreen(
            workout = workoutConfig,
            results = resultsMap,
            isReadOnly = true,
            onCancel = onBack,
            viewModel = viewModel
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
