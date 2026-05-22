package com.msahil432.tracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.msahil432.tracker.data.model.UserProfile
import com.msahil432.tracker.data.model.Goal
import com.msahil432.tracker.data.model.GoalEntry
import com.msahil432.tracker.data.model.DayStatus
import com.msahil432.tracker.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)


    // --- Goals ---
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE status = 'active' ORDER BY id DESC")
    fun getActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)


    // --- Goal Entries ---
    @Query("SELECT * FROM goal_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<GoalEntry>>

    @Query("SELECT * FROM goal_entries WHERE goalId = :goalId ORDER BY date DESC")
    fun getEntriesForGoal(goalId: Long): Flow<List<GoalEntry>>

    @Query("SELECT * FROM goal_entries WHERE date = :date")
    fun getEntriesForDate(date: String): Flow<List<GoalEntry>>

    @Query("SELECT * FROM goal_entries WHERE date = :date")
    suspend fun getEntriesForDateSync(date: String): List<GoalEntry>

    @Query("SELECT * FROM goal_entries WHERE goalId = :goalId")
    suspend fun getEntriesForGoalSync(goalId: Long): List<GoalEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: GoalEntry): Long

    @Query("DELETE FROM goal_entries WHERE goalId = :goalId AND date = :date")
    suspend fun deleteEntriesForGoalAndDate(goalId: Long, date: String)

    @Delete
    suspend fun deleteEntry(entry: GoalEntry)


    // --- Day Status ---
    @Query("SELECT * FROM day_status WHERE date = :date")
    fun getDayStatus(date: String): Flow<DayStatus?>

    @Query("SELECT * FROM day_status WHERE date = :date")
    suspend fun getDayStatusSync(date: String): DayStatus?

    @Query("SELECT * FROM day_status")
    fun getAllDayStatuses(): Flow<List<DayStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayStatus(dayStatus: DayStatus)


    // --- Achievements ---
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)
}
