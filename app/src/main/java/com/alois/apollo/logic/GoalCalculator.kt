package com.alois.apollo.logic

import com.alois.apollo.data.local.ExerciseRecord

object GoalCalculator {

    fun calculateSuggestedReps(
        lastRecord: ExerciseRecord?,
        defaultTarget: Int,
        unit: com.alois.apollo.data.model.ExerciseUnit
    ): Int {
        if (lastRecord == null) return defaultTarget

        val previousTarget = lastRecord.targetReps
        val sets = lastRecord.sets

        // Requirement 3: Keep the last goal if it was not reached for at least one set of the previous session
        val anySetFailed = sets.any { it < previousTarget }
        if (anySetFailed) {
            return previousTarget
        }

        val increment = if (unit == com.alois.apollo.data.model.ExerciseUnit.TIME) 5 else 1

        // Requirement 2: Set the goal to +increment of the max n° of reps reached during the last session, 
        // if more reps were done than the suggested goal
        val maxRepsPerformed = sets.maxOrNull() ?: 0
        if (maxRepsPerformed > previousTarget) {
            return maxRepsPerformed + increment
        }

        // Requirement 1: Set the goal to +increment if the previous goal was reached for every set
        return previousTarget + increment
    }
}
