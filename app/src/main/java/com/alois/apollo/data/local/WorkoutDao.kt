package com.alois.apollo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertExerciseRecords(records: List<ExerciseRecord>)

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
