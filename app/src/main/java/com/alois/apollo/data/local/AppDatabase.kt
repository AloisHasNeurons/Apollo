package com.alois.apollo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** Main database definition for the application. Stores workout sessions and exercise records. */
@Database(entities = [WorkoutSession::class, ExerciseRecord::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
