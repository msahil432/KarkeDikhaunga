package com.example.data.repository

import com.example.data.local.GoalDao
import com.example.data.model.UserProfile
import com.example.data.model.Goal
import com.example.data.model.GoalEntry
import com.example.data.model.DayStatus
import com.example.data.model.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GoalRepository(private val goalDao: GoalDao) {

    // --- Reactive Flows ---
    val userProfile: Flow<UserProfile?> = goalDao.getUserProfile()
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    val activeGoals: Flow<List<Goal>> = goalDao.getActiveGoals()
    val allEntries: Flow<List<GoalEntry>> = goalDao.getAllEntries()
    val allAchievements: Flow<List<Achievement>> = goalDao.getAllAchievements()
    val allDayStatuses: Flow<List<DayStatus>> = goalDao.getAllDayStatuses()

    // --- Core Database CRUD ---
    suspend fun getGoalById(id: Long): Goal? = goalDao.getGoalById(id)

    suspend fun getProfile(): UserProfile? = goalDao.getUserProfileSync()

    suspend fun saveProfile(profile: UserProfile) = goalDao.insertProfile(profile)

    suspend fun createGoal(goal: Goal): Long {
        val id = goalDao.insertGoal(goal)
        // Check for "First Goal Created" milestone
        checkAndUnlockMilestones()
        return id
    }

    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)

    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    suspend fun saveDayStatus(date: String, isBusy: Boolean) {
        goalDao.insertDayStatus(DayStatus(date, isBusy))
    }

    suspend fun getDayStatusSync(date: String): DayStatus? = goalDao.getDayStatusSync(date)

    // --- Past and Current Logging + XP/Gamification Processing ---
    suspend fun logProgress(
        goalId: Long,
        date: String,
        value: Double,
        levelLabel: String = "",
        notes: String = "",
        completed: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val goal = goalDao.getGoalById(goalId) ?: return@withContext
        val profile = goalDao.getUserProfileSync() ?: return@withContext

        // Check if date is designated as a busy day
        val dayStatus = goalDao.getDayStatusSync(date)
        val isBusy = dayStatus?.isBusy == true

        // Check if there was an existing log for this goal on this date
        // Delete any existing log so we replace it (avoid duplicates on same day)
        goalDao.deleteEntriesForGoalAndDate(goalId, date)

        // Calculate XP reward
        var xpEarned = 0
        if (completed) {
            // XP formula based on goal settings: goal XP value * goal weight
            xpEarned = (goal.xpValue * goal.xpWeight).toInt()
            
            // Streak bonuses or busy day bonuses
            if (isBusy) {
                xpEarned = (xpEarned * 1.5).toInt() // 50% bonus on busy days (bonus completion!)
            }
        } else if (goal.includePartialInGamification && value > 0) {
            // Partial progress XP
            val ratio = (value / goal.targetValue).coerceAtMost(1.0)
            xpEarned = (goal.xpValue * goal.xpWeight * ratio).toInt()
        }

        // Save Goal Entry
        val entry = GoalEntry(
            goalId = goalId,
            date = date,
            value = value,
            completionLevelLabel = levelLabel,
            completed = completed,
            notes = notes,
            isBusyDay = isBusy,
            isBonus = isBusy && completed
        )
        goalDao.insertEntry(entry)

        // Re-calculate Profile XP & Streaks
        if (xpEarned > 0) {
            val newXp = profile.totalXp + xpEarned
            // Simple level progression: level increments every 150 XP
            val newLevel = (newXp / 150) + 1
            
            val updatedProfile = profile.copy(
                totalXp = newXp,
                level = newLevel
            )
            goalDao.insertProfile(updatedProfile)
        }

        // Recalculate streaks and achievements
        recalculateStreaks(profile)
        checkAndUnlockMilestones()
    }

    suspend fun deleteProgress(goalId: Long, date: String) = withContext(Dispatchers.IO) {
        goalDao.deleteEntriesForGoalAndDate(goalId, date)
        val profile = goalDao.getUserProfileSync()
        if (profile != null) {
            recalculateStreaks(profile)
        }
    }

    // --- Streak Engine ---
    private suspend fun recalculateStreaks(profile: UserProfile) {
        // Scans entries of active goals to compute current active streaks
        val allEntriesList = goalDao.getAllEntries().firstOrNull() ?: emptyList()
        if (allEntriesList.isEmpty()) return

        // Group active completions by date
        val completedDatesList = allEntriesList
            .filter { it.completed }
            .map { it.date }
            .distinct()
            .sortedDescending() // newest first

        if (completedDatesList.isEmpty()) {
            val updatedProfile = profile.copy(currentStreak = 0)
            goalDao.insertProfile(updatedProfile)
            return
        }

        // Calculate current streak
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        var currentStreak = 0
        var checkDate = todayStr
        var isYesterdayChecked = false

        // If today is completed or yesterday is completed, we count backwards
        if (completedDatesList.contains(todayStr)) {
            currentStreak = 1
            var daysAgo = 1
            while (true) {
                val tempCal = Calendar.getInstance()
                tempCal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                val dateToCheck = sdf.format(tempCal.time)
                if (completedDatesList.contains(dateToCheck)) {
                    currentStreak++
                    daysAgo++
                } else {
                    break
                }
            }
        } else if (completedDatesList.contains(yesterdayStr)) {
            currentStreak = 1
            var daysAgo = 2
            while (true) {
                val tempCal = Calendar.getInstance()
                tempCal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                val dateToCheck = sdf.format(tempCal.time)
                if (completedDatesList.contains(dateToCheck)) {
                    currentStreak++
                    daysAgo++
                } else {
                    break
                }
            }
        }

        val longestStreak = if (currentStreak > profile.longestStreak) currentStreak else profile.longestStreak

        val updatedProfile = profile.copy(
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )
        goalDao.insertProfile(updatedProfile)
    }

    // --- Milestone Badge Unlocks ---
    suspend fun checkAndUnlockMilestones() = withContext(Dispatchers.IO) {
        val profile = goalDao.getUserProfileSync() ?: return@withContext
        val currentAchievements = goalDao.getAllAchievements().firstOrNull() ?: emptyList()
        val goals = goalDao.getAllGoals().firstOrNull() ?: emptyList()
        val entries = goalDao.getAllEntries().firstOrNull() ?: emptyList()

        val unlockedTitles = currentAchievements.map { it.title }.toSet()

        // 1. First goal created!
        if (!unlockedTitles.contains("Pehlwaan") && goals.isNotEmpty()) {
            goalDao.insertAchievement(Achievement(
                type = "milestone",
                title = "Pehlwaan",
                description = "Created your very first goal. Keep pushing!",
                icon = "🎯",
                xpReward = 50
            ))
            awardAchievementXp(profile, 50)
        }

        // 2. 10 goals in list!
        if (!unlockedTitles.contains("Goal Collector") && goals.size >= 10) {
            goalDao.insertAchievement(Achievement(
                type = "milestone",
                title = "Goal Collector",
                description = "Added 10 active goals in your catalog.",
                icon = "📚",
                xpReward = 100
            ))
            awardAchievementXp(profile, 100)
        }

        // 3. Completed first activity!
        val completedEntriesCount = entries.count { it.completed }
        if (!unlockedTitles.contains("Shuruaat") && completedEntriesCount >= 1) {
            goalDao.insertAchievement(Achievement(
                type = "milestone",
                title = "Shuruaat",
                description = "Earned your very first habit completion!",
                icon = "🔥",
                xpReward = 30
            ))
            awardAchievementXp(profile, 30)
        }

        // 4. Completed 15 habits!
        if (!unlockedTitles.contains("Agni Pariksha") && completedEntriesCount >= 15) {
            goalDao.insertAchievement(Achievement(
                type = "milestone",
                title = "Agni Pariksha",
                description = "Logged 15 habit completions successfully.",
                icon = "⚡",
                xpReward = 150
            ))
            awardAchievementXp(profile, 150)
        }

        // 5. Streaks milestones
        if (!unlockedTitles.contains("7-Day Warrior") && profile.currentStreak >= 7) {
            goalDao.insertAchievement(Achievement(
                type = "streak",
                title = "7-Day Warrior",
                description = "Completed at least one goal 7 days in a row!",
                icon = "🛡️",
                xpReward = 100
            ))
            awardAchievementXp(profile, 100)
        }

        if (!unlockedTitles.contains("30-Day Legend") && profile.longestStreak >= 30) {
            goalDao.insertAchievement(Achievement(
                type = "streak",
                title = "30-Day Legend",
                description = "Maintained a staggering streak of 30 days!",
                icon = "👑",
                xpReward = 300
            ))
            awardAchievementXp(profile, 300)
        }

        // 6. Busy day hero!
        val busyDayBonusCompletions = entries.count { it.isBusyDay && it.completed }
        if (!unlockedTitles.contains("Toofan Me Diya") && busyDayBonusCompletions >= 3) {
            goalDao.insertAchievement(Achievement(
                type = "busy_day",
                title = "Toofan Me Diya",
                description = "Completed 3 major goals on extreme busy days!",
                icon = "🌊",
                xpReward = 80
            ))
            awardAchievementXp(profile, 80)
        }
    }

    private suspend fun awardAchievementXp(profile: UserProfile, xp: Int) {
        val currentProfile = goalDao.getUserProfileSync() ?: return
        val newXp = currentProfile.totalXp + xp
        val newLevel = (newXp / 150) + 1
        goalDao.insertProfile(currentProfile.copy(totalXp = newXp, level = newLevel))
    }

    // --- On-Device AI: Contextual Motivation Generator ---
    suspend fun getMotivationalFeedback(goalName: String): String = withContext(Dispatchers.IO) {
        val profile = goalDao.getUserProfileSync()
        val name = profile?.name ?: "Mitra"
        val style = profile?.motivationStyle ?: "balanced"


        // Using on-device logic for motivation generation
        return@withContext getOfflineMotivation(name, style, goalName)
    }

    private fun getOfflineMotivation(name: String, style: String, goalName: String): String {
        return when (style.lowercase().trim()) {
            "tough" -> {
                val toughTemplates = listOf(
                    "Hey $name, excuses don't build muscles or status. Lock in on '$goalName' now!",
                    "You said you'd do it! Don't let '$goalName' beat you today, $name.",
                    "Time is slipping away, $name. Show some grit and complete '$goalName'!",
                    "Are you a talker or a doer, $name? Prove it by handling '$goalName'!"
                )
                toughTemplates.random()
            }
            "gentle" -> {
                val gentleTemplates = listOf(
                    "You're doing wonderful, $name. Even a small step on '$goalName' is a beautiful victory.",
                    "Be kind to yourself today, $name. A gentle focus on '$goalName' will make you smile.",
                    "No pressure, $name. Take a deep breath and give a tiny bit of love to '$goalName'.",
                    "You are capable of amazing things, $name! Let's work on '$goalName' in your pace."
                )
                gentleTemplates.random()
            }
            else -> { // Balanced
                val balancedTemplates = listOf(
                    "Consistent actions shape your future, $name. Let's conquer '$goalName' today!",
                    "Great things take time, $name. Complete '$goalName' and keep your momentum alive!",
                    "Your streaks are looking great, $name! Time to log '$goalName' and stay cataloged.",
                    "Karke Dikhaunga, $name! You've got this. Let's score '$goalName'."
                )
                balancedTemplates.random()
            }
        }
    }
}
