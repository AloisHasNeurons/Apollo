package com.alois.apollo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkoutDao {
    /**
     * Inserts a completed workout session into the history.
     * @param session The session data.
     * @return The ID of the inserted session.
     */
    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    /**
     * Inserts detailed records for each exercise in a session.
     * @param records List of exercise records.
     */
    @Insert
    suspend fun insertExerciseRecords(records: List<ExerciseRecord>)

    /**
     * Retrieves the last two records for a given exercise to calculate future goals. We fetch two
     * to verify consistency or progression trends if needed.
     * @param exerciseId The ID of the exercise.
     * @return List of at most 2 records, ordered by ID descending (newest first).
     */
    @Query("SELECT * FROM exercise_history WHERE exerciseId = :exerciseId ORDER BY id DESC LIMIT 2")
    suspend fun getLastTwoRecordsForExercise(exerciseId: String): List<ExerciseRecord>

    @Query("SELECT * FROM workout_history ORDER BY date DESC")
    suspend fun getWorkoutHistory(): List<WorkoutSession>

    @Query("SELECT * FROM exercise_history WHERE sessionId = :sessionId")
    suspend fun getExerciseRecordsForSession(sessionId: Long): List<ExerciseRecord>

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    @Query("DELETE FROM exercise_history WHERE sessionId = :sessionId")
    suspend fun deleteExerciseRecordsForSession(sessionId: Long)

    @Query("DELETE FROM workout_history")
    suspend fun clearAllSessions()

    @Query("DELETE FROM exercise_history")
    suspend fun clearAllExerciseRecords()
}
