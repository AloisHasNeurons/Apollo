package com.alois.apollo.logic

import com.alois.apollo.data.local.ExerciseRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalCalculatorTest {

    @Test
    fun `returns default target when no history exists`() {
        val result =
            GoalCalculator.calculateSuggestedReps(
                null,
                10,
                com.alois.apollo.data.model.ExerciseUnit.REPS
            )
        assertEquals(10, result)
    }

    @Test
    fun `increases goal by 1 if previous goal reached in all sets (REPS)`() {
        val record = createRecord(target = 10, sets = listOf(10, 10, 10))
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                10,
                com.alois.apollo.data.model.ExerciseUnit.REPS
            )
        assertEquals(11, result)
    }

    @Test
    fun `increases goal by 5 if previous goal reached in all sets (TIME)`() {
        val record = createRecord(target = 30, sets = listOf(30, 30, 30))
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                30,
                com.alois.apollo.data.model.ExerciseUnit.TIME
            )
        assertEquals(35, result)
    }

    @Test
    fun `increases goal by 1 if previous goal exceeded (REPS)`() {
        val record = createRecord(target = 10, sets = listOf(11, 10, 10))
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                10,
                com.alois.apollo.data.model.ExerciseUnit.REPS
            )
        assertEquals(12, result)
    }

    @Test
    fun `increases goal by 5 if previous goal exceeded (TIME)`() {
        val record = createRecord(target = 30, sets = listOf(35, 30, 30)) // Max 35
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                30,
                com.alois.apollo.data.model.ExerciseUnit.TIME
            )
        assertEquals(40, result) // 35 (max) + 5
    }

    @Test
    fun `increases goal based on max reps if significantly exceeded (REPS)`() {
        val record = createRecord(target = 10, sets = listOf(13, 13, 13))
        // Max is 13. Target 10. Result should be 13 + 1 = 14.
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                10,
                com.alois.apollo.data.model.ExerciseUnit.REPS
            )
        assertEquals(14, result)
    }

    @Test
    fun `keeps previous goal if failed in at least one set (REPS)`() {
        val record = createRecord(target = 10, sets = listOf(10, 9, 10))
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                10,
                com.alois.apollo.data.model.ExerciseUnit.REPS
            )
        assertEquals(10, result)
    }

    @Test
    fun `keeps previous goal if failed in at least one set (TIME)`() {
        val record = createRecord(target = 30, sets = listOf(30, 25, 30))
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                30,
                com.alois.apollo.data.model.ExerciseUnit.TIME
            )
        assertEquals(30, result)
    }

    @Test
    fun `keeps previous goal if failed even if max exceeded in other sets`() {
        val record = createRecord(target = 10, sets = listOf(15, 8, 10))
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                10,
                com.alois.apollo.data.model.ExerciseUnit.REPS
            )
        assertEquals(10, result)
    }

    @Test
    fun `mixed success with one set exceeding target`() {
        val record = createRecord(target = 10, sets = listOf(10, 12, 10))
        // Met goal in all? Yes (10, 12, 10 >= 10).
        // Max (12) > Target (10). Return Max + 1 = 13.
        val result =
            GoalCalculator.calculateSuggestedReps(
                record,
                10,
                com.alois.apollo.data.model.ExerciseUnit.REPS
            )
        assertEquals(13, result)
    }

    private fun createRecord(target: Int, sets: List<Int>): ExerciseRecord {
        return ExerciseRecord(
            sessionId = 1L,
            exerciseId = "ex_1",
            repsPerSet = sets,
            targetReps = target,
            isCompleted = true
        )
    }
}
