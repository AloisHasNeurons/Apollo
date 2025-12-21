package com.example.mycalisthenics.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Dao
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
}

@Database(entities = [WorkoutSession::class, ExerciseRecord::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
