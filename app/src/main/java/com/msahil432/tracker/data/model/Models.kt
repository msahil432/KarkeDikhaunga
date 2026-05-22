package com.msahil432.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single-row user profile
    val name: String,
    val age: Int,
    val gender: String,
    val occupation: String,
    val preferredLanguage: String,
    val motivationStyle: String, // "gentle", "tough", "balanced"
    val totalXp: Int = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val sleepTime: String = "22:00",
    val wakeTime: String = "06:00",
    val sleepReminderEnabled: Boolean = true,
    val wakeReminderEnabled: Boolean = true,
    val defaultReminderMode: String = "notification", // notification, popup, none
    val defaultSound: String = "Default",
    val defaultVibration: Boolean = true
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val type: String, // "do" (habit), "avoid" (app limit), "health", "measurable", "one-time"
    val category: String, // "Health", "Productivity", "Learning", "Finance", "Social", "Mindfulness" or Custom
    val frequency: String, // "daily", "weekly", "monthly", "specific-days"
    val frequencyDays: String = "", // comma separated "Mon,Tue" etc.
    val targetValue: Double = 1.0, // Target numeric value (e.g. 10000 steps, or 1 time)
    val unit: String = "times", // "times", "steps", "mins", "ml", "km"
    val xpValue: Int = 10,
    val xpWeight: Double = 1.0,
    val includePartialInGamification: Boolean = true,
    val analyticsType: String = "completion", // "average", "min", "max", "total", "streak", "completion"
    val reminderDisplayMode: String = "notification", // "notification", "popup", "none"
    val status: String = "active", // "active", "paused", "archived", "completed"
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    // Completion quick levels string format: "LevelName:PercentageRatio,LevelName:PercentageRatio"
    val completionLevels: String = "Very Low:10,Low:30,Middle:50,High:70,Very High:85,Super Se Upar (100%):100",
    // App Avoidance Details
    val avoidPackageNames: String = "", // Comma-separated list of target apps to monitor
    val cooldownDurationMinutes: Int = 10,
    val avoidAlternatives: String = "Read a book, Meditate, Back-straight stretch, Drink water",
    val avoidSearchTerms: String = "cute dog memes"
)

@Entity(tableName = "goal_entries")
data class GoalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val date: String, // "YYYY-MM-DD"
    val value: Double, // The numerical value tracked
    val completionLevelLabel: String = "", // E.g., "Middle (50%)"
    val completed: Boolean = false,
    val notes: String = "",
    val isBusyDay: Boolean = false,
    val isBonus: Boolean = false, // Completed on a busy day
    val logTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "day_status")
data class DayStatus(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val isBusy: Boolean = false
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "streak", "milestone", "category", "busy_day"
    val title: String,
    val description: String,
    val icon: String, // e.g. "🏆", "🔥", "🌱"
    val unlockedAt: Long = System.currentTimeMillis(),
    val xpReward: Int = 50
)
