package com.msahil432.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.msahil432.tracker.data.model.UserProfile
import com.msahil432.tracker.data.model.Goal
import com.msahil432.tracker.data.model.GoalEntry
import com.msahil432.tracker.data.model.DayStatus
import com.msahil432.tracker.data.model.Achievement

@Database(
    entities = [
        UserProfile::class,
        Goal::class,
        GoalEntry::class,
        DayStatus::class,
        Achievement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "karke_dikhaunga_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
