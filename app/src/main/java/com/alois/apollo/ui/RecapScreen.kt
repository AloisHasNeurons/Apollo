package com.alois.apollo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alois.apollo.data.local.ExerciseRecord
import com.alois.apollo.data.model.WorkoutConfig

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Screen displaying the summary of a workout session. Used for both editing the results before
 * saving (Recap) and viewing past sessions (ReadOnly).
 *
 * @param workout The workout configuration.
 * @param results The recorded reps for each exercise.
 * @param isReadOnly Whether the screen is for viewing history only.
 * @param onSave Callback to save the session.
 */
@Composable
fun RecapScreen(
    workout: WorkoutConfig,
    results: Map<String, List<Int>>,
    isReadOnly: Boolean = false,
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    viewModel: WorkoutViewModel = viewModel()
) {
    // Handle system back gesture
    androidx.activity.compose.BackHandler { onCancel() }

    // Observe isFrench state for immediate updates if ever reachable, or just consistency
    val isFrench by viewModel.isFrench.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title =
                        if (isReadOnly) {
                            if (isFrench) "Résumé de la séance" else "Session Summary"
                        } else {
                            if (isFrench) "Récapitulatif" else "Workout Recap"
                        }
                    Text(title)
                },
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
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text(if (isFrench) "Annuler" else "Cancel")
                    }
                    Button(onClick = onSave, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isFrench) "Enregistrer" else "Save")
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
                val repsPerSet = results[exercise.id] ?: listOf(0, 0, 0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f
                                )
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text =
                                if (viewModel.isFrench()) exercise.nameFr
                                else exercise.nameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            repsPerSet.forEachIndexed { index, value ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Set ${index + 1}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    if (isReadOnly) {
                                        Text(
                                            text = value.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        var textValue by remember {
                                            mutableStateOf(value.toString())
                                        }
                                        OutlinedTextField(
                                            value = textValue,
                                            onValueChange = {
                                                textValue = it
                                                it.toIntOrNull()?.let { newVal ->
                                                    viewModel.updateResult(
                                                        exercise.id,
                                                        index,
                                                        newVal
                                                    )
                                                }
                                            },
                                            modifier = Modifier.width(70.dp),
                                            keyboardOptions =
                                                KeyboardOptions(
                                                    keyboardType = KeyboardType.Number
                                                ),
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
    // Handle system back gesture
    androidx.activity.compose.BackHandler { onBack() }

    var records by remember { mutableStateOf<List<ExerciseRecord>>(emptyList()) }
    val workouts by viewModel.availableWorkouts.collectAsState()
    val history by viewModel.history.collectAsState()

    val session = history.find { it.id == sessionId }
    val workoutConfig = workouts.find { it.id == session?.workoutId }

    LaunchedEffect(sessionId) { records = viewModel.getSessionDetails(sessionId) }

    if (workoutConfig != null && session != null) {
        val resultsMap = records.associate { it.exerciseId to it.repsPerSet }
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
