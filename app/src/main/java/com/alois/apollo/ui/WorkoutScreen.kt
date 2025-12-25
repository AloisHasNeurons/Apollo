package com.alois.apollo.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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

    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back gesture
    androidx.activity.compose.BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(if (isFrench) "Quitter l'entraînement ?" else "Quit Workout?") },
            text = {
                Text(
                    if (isFrench)
                        "Votre progression actuelle sera perdue. Êtes-vous sûr de vouloir quitter ?"
                    else
                        "Your current progress will be lost. Are you sure you want to quit?"
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.cancelWorkout()
                        onBack()
                    }
                ) {
                    Text(
                        if (isFrench) "Quitter" else "Quit",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showExitDialog = false }) {
                    Text(if (isFrench) "Continuer" else "Resume")
                }
            }
        )
    }

    LaunchedEffect(showRecap) {
        if (showRecap) {
            onShowRecap()
        }
    }

    val exercise = workout?.exercises?.getOrNull(currentExerciseIndex)
    val totalSets = 3 
    val totalExercises = workout?.exercises?.size ?: 1

    // Animate progress smoothly
    val animatedProgress by animateFloatAsState(
        targetValue = (currentExerciseIndex + 1).toFloat() / totalExercises,
        label = "progress"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isFrench) "Série ${currentSetIndex + 1} / $totalSets" else "Set ${currentSetIndex + 1} / $totalSets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val canGoBack = currentSetIndex > 0 || currentExerciseIndex > 0

                if (canGoBack) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (isFrench) "Précédent" else "Previous",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = { viewModel.nextStep() },
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (isFrench) "Suivant" else "Next",
                        style = MaterialTheme.typography.titleMedium
                    )
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
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp) // Thicker progress bar
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = if (isFrench) "Exercice ${currentExerciseIndex + 1} / $totalExercises" else "Exercise ${currentExerciseIndex + 1} / $totalExercises",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp
            )

            // Animated content transition
            androidx.compose.animation.AnimatedContent(
                targetState = exercise,
                transitionSpec = {
                    androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally { it } togetherWith
                            androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutHorizontally { -it }
                },
                label = "exercise_transition"
            ) { targetExercise ->
                targetExercise?.let { ex ->
                    val suggested = suggestedReps[ex.id] ?: ex.targetedReps
                    val currentVal = workoutResults[ex.id]?.getOrNull(currentSetIndex) ?: suggested

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isFrench) ex.nameFr else ex.nameEn,
                            style = MaterialTheme.typography.headlineLarge, // Bigger headline
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = if (isFrench) ex.descriptionFr else ex.descriptionEn,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        if (ex.unit == ExerciseUnit.TIME) {
                            TimerSection(viewModel)
                        } else {
                            key(ex.id, currentSetIndex) {
                                RepsSection(
                                    initialReps = currentVal,
                                    targetReps = suggested,
                                    onRepsChanged = {
                                        viewModel.updateResult(
                                            ex.id,
                                            currentSetIndex,
                                            it
                                        )
                                    }
                                )
                            }
                        }

                        // Spacer to push content up slightly from bottom bar
                        Spacer(modifier = Modifier.height(80.dp)) 
                    }
                }
            }
        }
    }
}

@Composable
fun RepsSection(initialReps: Int, targetReps: Int, onRepsChanged: (Int) -> Unit) {
    // Use targetReps as the starting value, not initialReps (which may be 0 from empty results)
    var reps by remember { mutableStateOf(if (initialReps > 0) initialReps else targetReps) }

    // Sync reps when initialReps changes (e.g., switching exercises or sets)
    LaunchedEffect(initialReps, targetReps) {
        reps = if (initialReps > 0) initialReps else targetReps
    }

    // Update parent when local state changes
    LaunchedEffect(reps) {
        onRepsChanged(reps)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.animation.AnimatedContent(
            targetState = reps,
            transitionSpec = {
                if (targetState > initialState) {
                    androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn() togetherWith
                            androidx.compose.animation.slideOutVertically { -it } + androidx.compose.animation.fadeOut()
                } else {
                    androidx.compose.animation.slideInVertically { -it } + androidx.compose.animation.fadeIn() togetherWith
                            androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut()
                }.using(androidx.compose.animation.SizeTransform(clip = false))
            },
            label = "reps_counter"
        ) { count ->
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 120.sp,
                    lineHeight = 120.sp
                ),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "Goal: $targetReps",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Control buttons
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrease Button
            androidx.compose.material3.FilledIconButton(
                onClick = { if (reps > 0) reps-- },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Remove, // Need to make sure Remove is imported or use a different icon
                    contentDescription = "Decrease reps",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Increase Button
            androidx.compose.material3.FilledIconButton(
                onClick = { reps++ },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase reps",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun TimerSection(viewModel: WorkoutViewModel) {
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val isDelaying by viewModel.isDelaying.collectAsState()
    val suggestedReps by viewModel.suggestedReps.collectAsState()
    
    val currentExercise = viewModel.getCurrentExercise()
    val totalTime = if (currentExercise != null) {
        (suggestedReps[currentExercise.id] ?: currentExercise.targetedReps).toFloat()
    } else {
        30f
    }
    val rawProgress = if (totalTime > 0) timerSeconds / totalTime else 0f

    // Smooth animated progress for high refresh rate displays
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(
            durationMillis = 100,
            easing = androidx.compose.animation.core.LinearEasing
        ),
        label = "timer_progress"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 20.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            strokeCap = StrokeCap.Round,
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isDelaying) "Get Ready!" else "${ceil(timerSeconds).toInt()}",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (isDelaying) 40.sp else 80.sp
                ),
                fontWeight = FontWeight.Bold,
                color = if (timerSeconds <= 5 && isRunning && !isDelaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            IconButton(
                onClick = { viewModel.toggleTimer() },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isRunning) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                )
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
