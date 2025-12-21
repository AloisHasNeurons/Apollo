package com.example.mycalisthenics.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Timestamp
    val workoutId: String,
    val workoutName: String,
    val volumeLoad: Int
)

@Entity(tableName = "exercise_history")
data class ExerciseRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val sets: List<Int>, // Number of reps per set
    val targetReps: Int,
    val isCompleted: Boolean
)
