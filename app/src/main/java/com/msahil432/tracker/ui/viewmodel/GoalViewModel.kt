package com.msahil432.tracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.msahil432.tracker.data.local.AppDatabase
import com.msahil432.tracker.data.model.UserProfile
import com.msahil432.tracker.data.model.Goal
import com.msahil432.tracker.data.model.GoalEntry
import com.msahil432.tracker.data.model.DayStatus
import com.msahil432.tracker.data.model.Achievement
import com.msahil432.tracker.data.repository.GoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab {
    TODAY, GOALS, INSIGHTS, MORE
}

sealed class SubScreen {
    object Main : SubScreen()
    data class GoalDetail(val goalId: Long) : SubScreen()
    object CreateGoalWizard : SubScreen()
}

class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GoalRepository

    // --- State Streams ---
    val userProfile: StateFlow<UserProfile?>
    val allGoals: StateFlow<List<Goal>>
    val activeGoals: StateFlow<List<Goal>>
    val allEntries: StateFlow<List<GoalEntry>>
    val allAchievements: StateFlow<List<Achievement>>
    val allDayStatuses: StateFlow<List<DayStatus>>

    // --- Screen Control States ---
    val currentTab = MutableStateFlow(AppTab.TODAY)
    val currentSubScreen = MutableStateFlow<SubScreen>(SubScreen.Main)
    
    // Default selected date for calendar-based logging (supports scrollback)
    val selectedDate = MutableStateFlow(getFormattedDate(Date()))

    // Gemini Motivation states
    val activeMotivation = MutableStateFlow("Tap any goal to get instant motivational feedback!")
    val isGeneratingMotivation = MutableStateFlow(false)

    // App Avoidance Overlay Simulation State (cooldown blocker)
    val activeAvoidanceGoal = MutableStateFlow<Goal?>(null)
    val showAvoidanceOverlay = MutableStateFlow(false)

    // System Challenge Generator
    val dailyChallenge = MutableStateFlow("")

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GoalRepository(database.goalDao())

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allGoals = repository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeGoals = repository.activeGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allEntries = repository.allEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allAchievements = repository.allAchievements.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allDayStatuses = repository.allDayStatuses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Generate daily system challenge on start
        generateDailyChallenge()
    }

    // --- User Actions & Operations ---

    fun completeOnboarding(
        nickname: String,
        age: Int,
        gender: String,
        occupation: String,
        language: String,
        motivationStyle: String,
        sleepTime: String,
        wakeTime: String,
        sleepReminders: Boolean,
        wakeReminders: Boolean,
        reminderMode: String,
        sound: String,
        vibration: Boolean
    ) {
        viewModelScope.launch {
            val defaultProfile = UserProfile(
                name = nickname,
                age = age,
                gender = gender,
                occupation = occupation,
                preferredLanguage = language,
                motivationStyle = motivationStyle,
                sleepTime = sleepTime,
                wakeTime = wakeTime,
                sleepReminderEnabled = sleepReminders,
                wakeReminderEnabled = wakeReminders,
                defaultReminderMode = reminderMode,
                defaultSound = sound,
                defaultVibration = vibration,
                totalXp = 0,
                level = 1
            )
            repository.saveProfile(defaultProfile)
            
            // Seed 3 starter achievements so the user does not start completely empty
            repository.checkAndUnlockMilestones()
        }
    }

    fun updateProfile(updated: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(updated)
        }
    }

    fun addNewGoal(
        name: String,
        description: String,
        type: String,
        category: String,
        frequency: String,
        frequencyDays: List<String> = emptyList(),
        targetValue: Double,
        unit: String,
        xpValue: Int,
        xpWeight: Double,
        partialInGamification: Boolean,
        analyticsType: String,
        reminderMode: String,
        completionLevels: String = "",
        avoidPackageNames: String = "",
        cooldownMins: Int = 10,
        alternatives: String = "",
        searchTerms: String = ""
    ) {
        viewModelScope.launch {
            val levels = if (completionLevels.isNotBlank()) completionLevels else {
                "Very Low:15,Low:33,Middle:55,High:75,Very High:90,Completed (100%):100"
            }
            val newGoal = Goal(
                name = name,
                description = description,
                type = type,
                category = category,
                frequency = frequency,
                frequencyDays = frequencyDays.joinToString(","),
                targetValue = targetValue,
                unit = unit,
                xpValue = xpValue,
                xpWeight = xpWeight,
                includePartialInGamification = partialInGamification,
                analyticsType = analyticsType,
                reminderDisplayMode = reminderMode,
                completionLevels = levels,
                avoidPackageNames = avoidPackageNames,
                cooldownDurationMinutes = cooldownMins,
                avoidAlternatives = if (alternatives.isNotBlank()) alternatives else "Read, Meditate, Back stretch",
                avoidSearchTerms = if (searchTerms.isNotBlank()) searchTerms else "funny panda memes"
            )
            repository.createGoal(newGoal)
        }
    }

    fun updateGoalDetails(goal: Goal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun toggleGoalArchive(goal: Goal) {
        viewModelScope.launch {
            val currentStatus = goal.status
            val newStatus = if (currentStatus == "archived") "active" else "archived"
            repository.updateGoal(goal.copy(status = newStatus))
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun logGoalEntry(
        goalId: Long,
        date: String,
        value: Double,
        levelLabel: String = "",
        notes: String = "",
        completed: Boolean = true
    ) {
        viewModelScope.launch {
            repository.logProgress(goalId, date, value, levelLabel, notes, completed)
        }
    }

    fun deleteGoalEntry(goalId: Long, date: String) {
        viewModelScope.launch {
            repository.deleteProgress(goalId, date)
        }
    }

    fun toggleBusyDay(date: String) {
        viewModelScope.launch {
            val currentStatus = repository.getDayStatusSync(date)
            val newIsBusy = !(currentStatus?.isBusy ?: false)
            repository.saveDayStatus(date, newIsBusy)
            
            // Re-apply exclusions / recalculate achievements
            val profile = repository.getProfile()
            if (profile != null) {
                repository.checkAndUnlockMilestones()
            }
        }
    }

    // --- Gemini Interactive Motivator API Trigger ---
    fun fetchMotivation(goalName: String) {
        viewModelScope.launch {
            isGeneratingMotivation.value = true
            val feedback = repository.getMotivationalFeedback(goalName)
            activeMotivation.value = feedback
            isGeneratingMotivation.value = false
        }
    }

    // --- App Avoidance System Simulator ---
    fun simulateAppLaunch(packageName: String, avoidanceGoal: Goal) {
        activeAvoidanceGoal.value = avoidanceGoal
        showAvoidanceOverlay.value = true
        fetchMotivation("Avoiding spending bad time on ${avoidanceGoal.name}")
    }

    fun dismissAvoidanceOverlay() {
        showAvoidanceOverlay.value = false
        activeAvoidanceGoal.value = null
    }

    // --- System Daily Challenges generator ---
    private fun generateDailyChallenge() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        dailyChallenge.value = when (dayOfWeek) {
            Calendar.MONDAY -> "🎯 pehla Kadam: Log any physical exercise first thing today!"
            Calendar.TUESDAY -> "💡 gyan Vardhak: Complete at least 20 minutes of your Learning goal."
            Calendar.WEDNESDAY -> "⚡ super Charger: Record progress on 3 different goals before 6 PM."
            Calendar.THURSDAY -> "🌊 shanti Prem: Add 10 minutes of Mindfulness or meditation."
            Calendar.FRIDAY -> "💸 Dhan Rakshak: Log a financial saving check-off or budget entry."
            Calendar.SATURDAY -> "🌱 Toofan Me Diya: Log a goal on time, keeping excuses aside!"
            else -> "✨ weekend Reset: Do a review check-off and maintain your daily streak!"
        }
    }

    // --- Helper date calculations ---
    fun getFormattedDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }

    fun changeSelectedDate(daysOffset: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentParsed = sdf.parse(selectedDate.value) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = currentParsed
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        selectedDate.value = sdf.format(cal.time)
    }

    fun getBusyDaysCountInWeek(): Int {
        val statuses = allDayStatuses.value
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        // Match current week's dates
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekDates = mutableListOf<String>()
        for (i in 0 until 7) {
            weekDates.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return statuses.count { it.date in weekDates && it.isBusy }
    }
}
