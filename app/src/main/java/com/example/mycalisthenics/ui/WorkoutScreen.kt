package com.example.mycalisthenics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mycalisthenics.data.model.ExerciseUnit
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val workout by viewModel.activeWorkout.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val currentSetIndex by viewModel.currentSetIndex.collectAsState()
    val isFrench = viewModel.isFrench()

    val exercise = workout?.exercises?.getOrNull(currentExerciseIndex)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${currentSetIndex + 1} / 3",
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
        exercise?.let { ex ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
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

                if (ex.postureTips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tip: ${ex.postureTips.first()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (ex.unit == ExerciseUnit.TIME) {
                    TimerSection(viewModel)
                } else {
                    RepsSection(ex.targetedReps)
                }
            }
        }
    }
}

@Composable
fun RepsSection(targetReps: Int) {
    var actualReps by remember { mutableStateOf(targetReps.toString()) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = targetReps.toString(),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 100.sp),
            fontWeight = FontWeight.Black
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = actualReps,
            onValueChange = { actualReps = it },
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
