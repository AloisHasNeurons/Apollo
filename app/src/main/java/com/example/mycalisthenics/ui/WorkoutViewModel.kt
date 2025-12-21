package com.example.mycalisthenics.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycalisthenics.data.local.AppDatabase
import com.example.mycalisthenics.data.model.ExerciseConfig
import com.example.mycalisthenics.data.model.ExerciseUnit
import com.example.mycalisthenics.data.model.WorkoutConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import androidx.room.Room
import java.io.File
import java.util.Locale

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        getApplication(),
        AppDatabase::class.java, "calisthenics-db"
    ).build()

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

    private val _availableWorkouts = MutableStateFlow<List<WorkoutConfig>>(emptyList())
    val availableWorkouts: StateFlow<List<WorkoutConfig>> = _availableWorkouts

    private val _activeWorkout = MutableStateFlow<WorkoutConfig?>(null)
    val activeWorkout: StateFlow<WorkoutConfig?> = _activeWorkout

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex

    private val _currentSetIndex = MutableStateFlow(0)
    val currentSetIndex: StateFlow<Int> = _currentSetIndex

    private val _suggestedReps = MutableStateFlow<Map<String, Int>>(emptyMap())
    val suggestedReps: StateFlow<Map<String, Int>> = _suggestedReps

    // Timer state
    private val _timerSeconds = MutableStateFlow(0f)
    val timerSeconds: StateFlow<Float> = _timerSeconds

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private val _isDelaying = MutableStateFlow(false)
    val isDelaying: StateFlow<Boolean> = _isDelaying

    private var timerJob: Job? = null

    init {
        loadAvailableWorkouts()
    }

    private fun loadAvailableWorkouts() {
        viewModelScope.launch {
            val workouts = mutableListOf<WorkoutConfig>()
            try {
                getApplication<Application>().assets.list("")?.filter { it.endsWith(".json") }?.forEach { fileName ->
                    val jsonString = getApplication<Application>().assets.open(fileName).bufferedReader().use { it.readText() }
                    workouts.add(Json.decodeFromString<WorkoutConfig>(jsonString))
                }
            } catch (e: Exception) {}

            val filesDir = getApplication<Application>().filesDir
            filesDir.listFiles { _, name -> name.endsWith(".json") }?.forEach { file ->
                try {
                    val jsonString = file.readText()
                    workouts.add(Json.decodeFromString<WorkoutConfig>(jsonString))
                } catch (e: Exception) {}
            }
            _availableWorkouts.value = workouts.distinctBy { it.id }
        }
    }

    fun importWorkout(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (jsonString != null) {
                    val workout = Json.decodeFromString<WorkoutConfig>(jsonString)
                    val file = File(context.filesDir, "${workout.id}.json")
                    file.writeText(jsonString)
                    loadAvailableWorkouts()
                }
            } catch (e: Exception) {}
        }
    }

    fun startWorkout(workout: WorkoutConfig) {
        _activeWorkout.value = workout
        _currentExerciseIndex.value = 0
        _currentSetIndex.value = 0
        resetTimer()
        
        viewModelScope.launch {
            val suggestions = mutableMapOf<String, Int>()
            workout.exercises.forEach { exercise ->
                suggestions[exercise.id] = calculateSuggestedReps(exercise)
            }
            _suggestedReps.value = suggestions
        }
    }

    fun exitWorkout() {
        _activeWorkout.value = null
        stopTimer()
    }

    fun nextStep() {
        val workout = _activeWorkout.value ?: return
        stopTimer()
        
        val totalSets = 3 // Standard for this app as per requirements
        
        if (_currentSetIndex.value < totalSets - 1) {
            _currentSetIndex.value += 1
        } else if (_currentExerciseIndex.value < workout.exercises.size - 1) {
            _currentExerciseIndex.value += 1
            _currentSetIndex.value = 0
        } else {
            _activeWorkout.value = null
        }
        resetTimer()
    }

    private fun resetTimer() {
        val exercise = getCurrentExercise()
        if (exercise?.unit == ExerciseUnit.TIME) {
            _timerSeconds.value = exercise.targetedReps.toFloat()
        } else {
            _timerSeconds.value = 0f
        }
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            stopTimer()
        } else {
            startTimerWithDelay()
        }
    }

    private fun startTimerWithDelay() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _isDelaying.value = true
            repeat(2) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                delay(1000)
            }
            _isDelaying.value = false
            _isTimerRunning.value = true
            
            val startTime = _timerSeconds.value
            while (_timerSeconds.value > 0 && _isTimerRunning.value) {
                delay(100)
                _timerSeconds.value -= 0.1f
            }
            
            if (_timerSeconds.value <= 0) {
                _timerSeconds.value = 0f
                _isTimerRunning.value = false
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 500)
            }
        }
    }

    private fun stopTimer() {
        _isTimerRunning.value = false
        _isDelaying.value = false
        timerJob?.cancel()
    }

    fun getCurrentExercise(): ExerciseConfig? {
        val workout = _activeWorkout.value ?: return null
        return workout.exercises.getOrNull(_currentExerciseIndex.value)
    }

    private suspend fun calculateSuggestedReps(exercise: ExerciseConfig): Int {
        val lastRecords = db.workoutDao().getLastTwoRecordsForExercise(exercise.id)
        if (lastRecords.size < 2) return exercise.targetedReps
        val hitTargetInAllSets = lastRecords.all { record -> record.sets.all { it >= record.targetReps } }
        return if (hitTargetInAllSets) exercise.targetedReps + 1 else exercise.targetedReps
    }

    fun isFrench(): Boolean = Locale.getDefault().language == "fr"
}
