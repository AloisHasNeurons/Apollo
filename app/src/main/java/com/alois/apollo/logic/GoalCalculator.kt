package com.alois.apollo.logic

import com.alois.apollo.data.local.ExerciseRecord

object GoalCalculator {

    /**
     * Calculates the suggested recommended reps or time for the next session. Uses a progressive
     * overload algorithm based on previous performance.
     *
     * @param previousRecord The record of the last completed session for this exercise.
     * @param fallbackTarget The default target to use if no history exists (e.g. from config).
     * @param unit Whether the exercise is measured in reps or time (seconds).
     * @return The new target value (reps or seconds).
     */
    fun calculateSuggestedReps(
        previousRecord: ExerciseRecord?,
        fallbackTarget: Int,
        unit: com.alois.apollo.data.model.ExerciseUnit
    ): Int {
        if (previousRecord == null) return fallbackTarget

        val previousTarget = previousRecord.targetReps
        val repsPerSet = previousRecord.repsPerSet

        // Rule 1: Regression (Maintain)
        // If the user failed to reach the target in ANY set, we maintain the same goal.
        // This ensures they master the current volume before progressing.
        val anySetFailed = repsPerSet.any { it < previousTarget }
        if (anySetFailed) {
            return previousTarget
        }

        // Determine increment step based on unit (+5s for time, +1 rep for strength)
        val increment = if (unit == com.alois.apollo.data.model.ExerciseUnit.TIME) 5 else 1

        // Rule 2: Double Progression (Jump)
        // If the user significantly exceeded the goal (e.g. did AMRAP and got way more),
        // we set the new goal based on their MAX performance + increment, rather than just previous
        // target + increment.
        // This allows faster progression for easy exercises.
        val maxRepsPerformed = repsPerSet.maxOrNull() ?: 0
        if (maxRepsPerformed > previousTarget) {
            return maxRepsPerformed + increment
        }

        // Rule 3: Standard Progression
        // If they hit the exact target on all sets, we bump the target by the increment.
        return previousTarget + increment
    }
}
