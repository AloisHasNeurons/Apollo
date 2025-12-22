package com.alois.apollo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alois.apollo.data.model.ExerciseUnit
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    onShowRecap: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val workout by viewModel.activeWorkout.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val currentSetIndex by viewModel.currentSetIndex.collectAsState()
    val suggestedReps by viewModel.suggestedReps.collectAsState()
    val workoutResults by viewModel.workoutResults.collectAsState()
    val showRecap by viewModel.showRecap.collectAsState()
    val isFrench = viewModel.isFrench()

    LaunchedEffect(showRecap) {
        if (showRecap) {
            onShowRecap()
        }
    }

    val exercise = workout?.exercises?.getOrNull(currentExerciseIndex)
    val totalSets = 3 
    val totalExercises = workout?.exercises?.size ?: 1

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isFrench) "Série ${currentSetIndex + 1} / $totalSets" else "Set ${currentSetIndex + 1} / $totalSets",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = { viewModel.nextStep() }) {
                    Text("Next")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LinearProgressIndicator(
                progress = { (currentExerciseIndex + 1).toFloat() / totalExercises },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                strokeCap = StrokeCap.Round
            )
            
            Text(
                text = if (isFrench) "Exercice ${currentExerciseIndex + 1} / $totalExercises" else "Exercise ${currentExerciseIndex + 1} / $totalExercises",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.secondary
            )

            exercise?.let { ex ->
                val suggested = suggestedReps[ex.id] ?: ex.targetedReps
                val currentVal = workoutResults[ex.id]?.getOrNull(currentSetIndex) ?: suggested
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isFrench) ex.nameFr else ex.nameEn,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isFrench) ex.descriptionFr else ex.descriptionEn,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    if (ex.unit == ExerciseUnit.TIME) {
                        TimerSection(viewModel)
                    } else {
                        key(ex.id, currentSetIndex) {
                            RepsSection(
                                initialReps = currentVal,
                                onRepsChanged = { viewModel.updateResult(ex.id, currentSetIndex, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepsSection(initialReps: Int, onRepsChanged: (Int) -> Unit) {
    var actualReps by remember { mutableStateOf(initialReps.toString()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = initialReps.toString(),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 100.sp),
            fontWeight = FontWeight.Black
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = actualReps,
            onValueChange = { 
                actualReps = it
                it.toIntOrNull()?.let { reps -> onRepsChanged(reps) }
            },
            label = { Text("Reps performed") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(150.dp),
            singleLine = true
        )
    }
}

@Composable
fun TimerSection(viewModel: WorkoutViewModel) {
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val isDelaying by viewModel.isDelaying.collectAsState()
    
    val currentExercise = viewModel.getCurrentExercise()
    val totalTime = (currentExercise?.targetedReps ?: 30).toFloat()
    val progress = if (totalTime > 0) timerSeconds / totalTime else 0f

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isDelaying) "Get Ready!" else "${ceil(timerSeconds).toInt()}s",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            IconButton(
                onClick = { viewModel.toggleTimer() },
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors()
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
