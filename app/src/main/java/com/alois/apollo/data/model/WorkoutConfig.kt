package com.alois.apollo.data.model

import kotlinx.serialization.Serializable

/**
 * Configuration for a full workout routine.
 * @property id Unique string ID (e.g., "greek_statue_a").
 * @property name Display name (English default).
 * @property exercises Ordered list of exercises.
 */
@Serializable
data class WorkoutConfig(
    val id: String,
    val name: String,
    val nameFr: String? = null,
    val focus: String,
    val focusFr: String? = null,
    val notes: String? = null,
    val exercises: List<ExerciseConfig>
)

/**
 * Configuration for a single exercise.
 * @property id Unique exercise ID.
 * @property targetReps Baseline target reps for a beginner.
 * @property unit Whether it's rep-based or time-based.
 * @property upgradeCriteria Description of when to move to a harder variation.
 */
@Serializable
data class ExerciseConfig(
    val id: String,
    val nameEn: String,
    val nameFr: String,
    val descriptionEn: String,
    val descriptionFr: String,
    val repRange: String, // e.g., "3-5"
    val targetReps: Int,
    val unit: ExerciseUnit,
    val restSeconds: Int,
    val harderVariation: String? = null,
    val upgradeCriteria: String? = null,
    val postureTips: List<String> = emptyList()
)

@Serializable
enum class ExerciseUnit {
    REPS,
    TIME
}
