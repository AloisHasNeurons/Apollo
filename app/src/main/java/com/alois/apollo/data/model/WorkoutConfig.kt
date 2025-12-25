package com.alois.apollo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutConfig(
    val id: String,
    // Keep raw name/focus for backward compatibility or serialization if needed, 
    // but we will primarily use the localized versions.
    // Actually, let's just use name/focus as English default and add Fr.
    val name: String,
    val nameFr: String? = null,
    val focus: String,
    val focusFr: String? = null,
    val notes: String? = null,
    val exercises: List<ExerciseConfig>
)

@Serializable
data class ExerciseConfig(
    val id: String,
    val nameEn: String,
    val nameFr: String,
    val descriptionEn: String,
    val descriptionFr: String,
    val repRange: String, // e.g., "3-5"
    val targetedReps: Int,
    val unit: ExerciseUnit,
    val restSeconds: Int,
    val harderVariation: String? = null,
    val upgradeCriteria: String? = null,
    val postureTips: List<String> = emptyList()
)

@Serializable
enum class ExerciseUnit {
    REPS, TIME
}
