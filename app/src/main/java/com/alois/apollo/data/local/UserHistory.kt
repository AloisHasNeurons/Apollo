package com.alois.apollo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a completed workout session.
 * @property id Unique identifier.
 * @property date Timestamp of completion.
 * @property workoutId ID of the workout configuration used.
 * @property workoutName Name of the workout at the time of completion.
 * @property volumeLoad Total volume load in kg (reps * bodyweight/weight).
 */
@Entity(tableName = "workout_history")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Timestamp
    val workoutId: String,
    val workoutName: String,
    val volumeLoad: Int
)

/**
 * Represents the results of a specific exercise within a session.
 * @property id Unique identifier.
 * @property sessionId FK to the parent [WorkoutSession].
 * @property exerciseId ID of the exercise.
 * @property repsPerSet List of reps performed for each set.
 * @property targetReps The goal that was set for this exercise.
 * @property isCompleted Whether the exercise was fully completed (informational).
 */
@Entity(tableName = "exercise_history")
data class ExerciseRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val repsPerSet: List<Int>, // Number of reps per set
    val targetReps: Int,
    val isCompleted: Boolean
)
