package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.GoalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: GoalViewModel = viewModel()
                val profileState by viewModel.userProfile.collectAsState()
                val goalsState by viewModel.allGoals.collectAsState()
                val entriesState by viewModel.allEntries.collectAsState()
                
                Box(modifier = Modifier.fillMaxSize()) {
                    val profile = profileState
                    
                    if (profile == null) {
                        // User is not onboarded, display wizard
                        OnboardingWizard(
                            onComplete = { nickname, age, gender, occupation, language, style, sleep, wake, sleepRem, wakeRem, reminder, sound, vibe ->
                                viewModel.completeOnboarding(
                                    nickname = nickname,
                                    age = age,
                                    gender = gender,
                                    occupation = occupation,
                                    language = language,
                                    motivationStyle = style,
                                    sleepTime = sleep,
                                    wakeTime = wake,
                                    sleepReminders = sleepRem,
                                    wakeReminders = wakeRem,
                                    reminderMode = reminder,
                                    sound = sound,
                                    vibration = vibe
                                )
                            }
                        )
                    } else {
                        // Active Main Habit Tracker
                        val currentTab by viewModel.currentTab.collectAsState()

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("app_navigation_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.TODAY,
                                        onClick = { viewModel.currentTab.value = AppTab.TODAY },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                                        label = { Text("Today") },
                                        modifier = Modifier.testTag("nav_tab_today")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.GOALS,
                                        onClick = { viewModel.currentTab.value = AppTab.GOALS },
                                        icon = { Icon(Icons.Default.List, contentDescription = "Goals") },
                                        label = { Text("Goals") },
                                        modifier = Modifier.testTag("nav_tab_goals")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.INSIGHTS,
                                        onClick = { viewModel.currentTab.value = AppTab.INSIGHTS },
                                        icon = { Icon(Icons.Default.Star, contentDescription = "Insights") },
                                        label = { Text("Insights") },
                                        modifier = Modifier.testTag("nav_tab_insights")
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == AppTab.MORE,
                                        onClick = { viewModel.currentTab.value = AppTab.MORE },
                                        icon = { Icon(Icons.Default.MoreVert, contentDescription = "More") },
                                        label = { Text("More") },
                                        modifier = Modifier.testTag("nav_tab_more")
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .windowInsetsPadding(WindowInsets.statusBars)
                            ) {
                                when (currentTab) {
                                    AppTab.TODAY -> {
                                        TodayTabScreen(
                                            viewModel = viewModel,
                                            profile = profile,
                                            goals = goalsState,
                                            entries = entriesState
                                        )
                                    }
                                    AppTab.GOALS -> {
                                        GoalsTabScreen(
                                            viewModel = viewModel,
                                            goals = goalsState
                                        )
                                    }
                                    AppTab.INSIGHTS -> {
                                        InsightsTabScreen(
                                            viewModel = viewModel,
                                            goals = goalsState,
                                            entries = entriesState
                                        )
                                    }
                                    AppTab.MORE -> {
                                        MoreTabScreen(
                                            viewModel = viewModel,
                                            profile = profile,
                                            achievements = viewModel.allAchievements.collectAsState().value
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Simulated Interventions overlay drawer (active on any simulation trigger)
                    val showOverlay by viewModel.showAvoidanceOverlay.collectAsState()
                    val activeAvoidanceGoal by viewModel.activeAvoidanceGoal.collectAsState()
                    
                    AnimatedVisibility(
                        visible = showOverlay && activeAvoidanceGoal != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        activeAvoidanceGoal?.let { goal ->
                            AppAvoidanceOverlay(viewModel = viewModel, goal = goal)
                        }
                    }
                }
            }
        }
    }
}
