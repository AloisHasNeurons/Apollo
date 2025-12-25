package com.alois.apollo.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.alois.apollo.data.local.AppDatabase
import com.alois.apollo.data.local.ExerciseRecord
import com.alois.apollo.data.local.WorkoutSession
import com.alois.apollo.data.model.ExerciseConfig
import com.alois.apollo.data.model.ExerciseUnit
import com.alois.apollo.data.model.WorkoutConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        getApplication(),
        AppDatabase::class.java, "calisthenics-db"
    )
        .fallbackToDestructiveMigration()
    .build()

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

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

    private val _workoutResults = MutableStateFlow<Map<String, MutableList<Int>>>(emptyMap())
    val workoutResults: StateFlow<Map<String, List<Int>>> = _workoutResults as StateFlow<Map<String, List<Int>>>

    private val _showRecap = MutableStateFlow(false)
    val showRecap: StateFlow<Boolean> = _showRecap

    private val _history = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val history: StateFlow<List<WorkoutSession>> = _history

    private val _timerSeconds = MutableStateFlow(0f)
    val timerSeconds: StateFlow<Float> = _timerSeconds

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning

    private val _isDelaying = MutableStateFlow(false)
    val isDelaying: StateFlow<Boolean> = _isDelaying

    private var timerJob: Job? = null

    init {
        loadAvailableWorkouts()
        loadHistory()
        // Auto-seed test data for development (clears and repopulates)
        seedTestData()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = db.workoutDao().getWorkoutHistory()
        }
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
        _showRecap.value = false
        _workoutResults.value = workout.exercises.associate { it.id to MutableList(3) { ex -> 0 } }
        resetTimer()
        
        viewModelScope.launch {
            val suggestions = mutableMapOf<String, Int>()
            workout.exercises.forEach { exercise ->
                suggestions[exercise.id] = calculateSuggestedReps(exercise)
            }
            _suggestedReps.value = suggestions
            
            val initialResults = workout.exercises.associate { ex -> 
                ex.id to MutableList(3) { suggestions[ex.id] ?: ex.targetedReps } 
            }
            _workoutResults.value = initialResults
        }
    }

    fun updateResult(exerciseId: String, setIndex: Int, value: Int) {
        val current = _workoutResults.value.toMutableMap()
        val list = current[exerciseId]?.toMutableList() ?: return
        if (setIndex in list.indices) {
            list[setIndex] = value
            current[exerciseId] = list
            _workoutResults.value = current
        }
    }

    fun nextStep() {
        val workout = _activeWorkout.value ?: return
        stopTimer()
        
        val totalSets = 3 
        val totalExercises = workout.exercises.size

        if (_currentExerciseIndex.value < totalExercises - 1) {
            _currentExerciseIndex.value += 1
        } else if (_currentSetIndex.value < totalSets - 1) {
            _currentExerciseIndex.value = 0
            _currentSetIndex.value += 1
        } else {
            _showRecap.value = true
        }
        resetTimer()
    }

    fun previousStep() {
        val workout = _activeWorkout.value ?: return
        stopTimer()

        val totalExercises = workout.exercises.size

        if (_currentExerciseIndex.value > 0) {
            _currentExerciseIndex.value -= 1
        } else if (_currentSetIndex.value > 0) {
            _currentSetIndex.value -= 1
            _currentExerciseIndex.value = totalExercises - 1
        }
        resetTimer()
    }

    fun saveWorkout() {
        val workout = _activeWorkout.value ?: return
        viewModelScope.launch {
            // Only count reps from rep-based exercises (exclude timed exercises)
            val repBasedExerciseIds = workout.exercises
                .filter { it.unit == ExerciseUnit.REPS }
                .map { it.id }
                .toSet()

            val totalReps = _workoutResults.value
                .filterKeys { it in repBasedExerciseIds }
                .values
                .sumOf { it.sum() }
            
            val bodyweightKg = 68 // Baseline bodyweight for volume calculation
            val totalVolume = totalReps * bodyweightKg
            val sessionId = db.workoutDao().insertSession(
                WorkoutSession(
                    date = System.currentTimeMillis(),
                    workoutId = workout.id,
                    workoutName = workout.name,
                    volumeLoad = totalVolume
                )
            )
            
            val records = _workoutResults.value.map { (exId, sets) ->
                ExerciseRecord(
                    sessionId = sessionId,
                    exerciseId = exId,
                    sets = sets,
                    targetReps = _suggestedReps.value[exId] ?: 0,
                    isCompleted = true
                )
            }
            db.workoutDao().insertExerciseRecords(records)
            _activeWorkout.value = null
            _showRecap.value = false
            loadHistory()
        }
    }

    fun cancelWorkout() {
        _activeWorkout.value = null
        _showRecap.value = false
        stopTimer()
    }

    fun exitWorkout() {
        cancelWorkout()
    }

    fun deleteSession(session: WorkoutSession) {
        viewModelScope.launch {
            db.workoutDao().deleteSession(session)
            db.workoutDao().deleteExerciseRecordsForSession(session.id)
            loadHistory()
        }
    }

    suspend fun getSessionDetails(sessionId: Long): List<ExerciseRecord> {
        return db.workoutDao().getExerciseRecordsForSession(sessionId)
    }

    private fun resetTimer() {
        val exercise = getCurrentExercise()
        if (exercise?.unit == ExerciseUnit.TIME) {
            val suggested = _suggestedReps.value[exercise.id] ?: exercise.targetedReps
            _timerSeconds.value = suggested.toFloat()
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
        val lastRecord = lastRecords.firstOrNull() // Order is DESC locally, so first is latest
        return com.alois.apollo.logic.GoalCalculator.calculateSuggestedReps(
            lastRecord,
            exercise.targetedReps,
            exercise.unit
        )
    }

    fun seedTestData() {
        viewModelScope.launch {
            // Clear existing data first
            db.workoutDao().clearAllExerciseRecords()
            db.workoutDao().clearAllSessions()
            
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L

            // Realistic workout history over ~2 months
            // - 2-3 workouts per week (realistic schedule)
            // - Natural variation in performance (±10-15%)
            // - Gradual progression over time
            // - Some "off" days where performance dipped
            val testSessions = listOf(
                // Recent week (Christmas week - lighter)
                WorkoutSession(
                    date = now - (dayMillis * 2),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3672  // 54 reps × 68kg - good session
                ),

                // Week 1 (current)
                WorkoutSession(
                    date = now - (dayMillis * 5),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3468  // 51 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 7),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3536  // 52 reps × 68kg
                ),

                // Week 2
                WorkoutSession(
                    date = now - (dayMillis * 10),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3400  // 50 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 12),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3128  // 46 reps × 68kg - tired day
                ),
                WorkoutSession(
                    date = now - (dayMillis * 14),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3332  // 49 reps × 68kg
                ),

                // Week 3
                WorkoutSession(
                    date = now - (dayMillis * 17),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3264  // 48 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 19),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3196  // 47 reps × 68kg
                ),

                // Week 4
                WorkoutSession(
                    date = now - (dayMillis * 22),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3060  // 45 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 24),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2992  // 44 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 26),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 3128  // 46 reps × 68kg - good energy
                ),

                // Week 5-6 (earlier - building up)
                WorkoutSession(
                    date = now - (dayMillis * 30),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2856  // 42 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 33),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2788  // 41 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 36),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2924  // 43 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 40),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2720  // 40 reps × 68kg
                ),

                // Week 7-8 (starting phase)
                WorkoutSession(
                    date = now - (dayMillis * 45),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2584  // 38 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 49),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2448  // 36 reps × 68kg
                ),
                WorkoutSession(
                    date = now - (dayMillis * 52),
                    workoutId = "greek_statue_a",
                    workoutName = "Greek Statue Protocol - A",
                    volumeLoad = 2380  // 35 reps × 68kg - first tracked
                )
            )

            testSessions.forEach { session ->
                val sessionId = db.workoutDao().insertSession(session)
                // Vary the reps per exercise based on total volume
                val totalReps = session.volumeLoad / 68
                val pullupReps = totalReps * 40 / 100  // ~40% from pullups
                val pushupReps = totalReps - pullupReps  // ~60% from pushups
                
                val records = listOf(
                    ExerciseRecord(
                        sessionId = sessionId,
                        exerciseId = "pullup_01",
                        sets = listOf(
                            pullupReps / 3,
                            pullupReps / 3,
                            pullupReps - 2 * (pullupReps / 3)
                        ),
                        targetReps = 5,
                        isCompleted = true
                    ),
                    ExerciseRecord(
                        sessionId = sessionId,
                        exerciseId = "pike_pushup_01",
                        sets = listOf(
                            pushupReps / 3,
                            pushupReps / 3,
                            pushupReps - 2 * (pushupReps / 3)
                        ),
                        targetReps = 10,
                        isCompleted = true
                    )
                )
                db.workoutDao().insertExerciseRecords(records)
            }
            loadHistory()
        }
    }

    fun isFrench(): Boolean = Locale.getDefault().language == "fr"
}
