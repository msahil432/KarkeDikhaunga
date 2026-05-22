package com.msahil432.tracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msahil432.tracker.data.model.Goal
import com.msahil432.tracker.data.model.GoalEntry
import com.msahil432.tracker.data.model.UserProfile
import com.msahil432.tracker.ui.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayTabScreen(
    viewModel: GoalViewModel,
    profile: UserProfile,
    goals: List<Goal>,
    entries: List<GoalEntry>
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    // Calculate week day titles (e.g. Wednesday, May 22)
    val todayFormatter = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formattedDisplayDate = remember(selectedDate) {
        try {
            val parsed = sdf.parse(selectedDate) ?: Date()
            todayFormatter.format(parsed)
        } catch (e: Exception) {
            todayFormatter.format(Date())
        }
    }

    // Filter goals active for selected day
    val activeGoalsToday = remember(goals, selectedDate) {
        goals.filter { it.status == "active" } // In a complex scenario, filter by frequency. Simple lists show all active goals.
    }

    // Filter logs recorded for selectedDate
    val entriesOnSelectedDate = remember(entries, selectedDate) {
        entries.filter { it.date == selectedDate }
    }

    val scaffoldState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scaffoldState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bento-Theme Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Namaste, ${profile.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "Karke Dikhaunga",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
            Surface(
                color = Color(0xFF4F46E5),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LVL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${profile.level}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Date Control Bar with past scrolling support
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.changeSelectedDate(-1) },
                modifier = Modifier.testTag("date_prev_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Date", tint = Color(0xFF4F46E5))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (selectedDate == viewModel.getFormattedDate(Date())) "Aaj (Today)" else formattedDisplayDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F46E5)
                )
                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { viewModel.changeSelectedDate(1) },
                modifier = Modifier.testTag("date_next_button")
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Date", tint = Color(0xFF4F46E5))
            }
        }

        val completionRatio = remember(activeGoalsToday, entriesOnSelectedDate) {
            if (activeGoalsToday.isEmpty()) 0f else {
                val completedCount = activeGoalsToday.count { goal ->
                    entriesOnSelectedDate.any { it.goalId == goal.id && it.completed }
                }
                completedCount.toFloat() / activeGoalsToday.size.toFloat()
            }
        }

        // Primary Progress Bento Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4F46E5)),
            shape = RoundedCornerShape(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Background decorative overlapping canvas circles
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = 110.dp.toPx(),
                            center = Offset(size.width * 0.9f, size.height * 0.1f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.04f),
                            radius = 150.dp.toPx(),
                            center = Offset(size.width * 0.9f, size.height * 0.1f)
                        )
                    }
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TODAY'S PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "${(completionRatio * 100).toInt()}",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp),
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val completedCount = activeGoalsToday.count { goal ->
                            entriesOnSelectedDate.any { it.goalId == goal.id && it.completed }
                        }
                        Text(
                            text = "$completedCount of ${activeGoalsToday.size} goals done",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        // Streak and XP Bento Card Grid (Side-by-Side Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Day Streak Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5)),
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 26.sp,
                        color = Color(0xFFEA580C)
                    )
                    Column {
                        Text(
                            text = "${profile.currentStreak}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF9A3412)
                        )
                        Text(
                            text = "DAY STREAK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEA580C).copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Total XP Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 26.sp,
                        color = Color(0xFF059669)
                    )
                    Column {
                        Text(
                            text = String.format("%,d", profile.totalXp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "TOTAL XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669).copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Dynamic Motivation Assistant Board (Gemini API Nudge Display styled Bento Card)
        val liveMotivation by viewModel.activeMotivation.collectAsState()
        val isGenerating by viewModel.isGeneratingMotivation.collectAsState()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF6366F1), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("🤖", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Gemini AI Saathi",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF818CF8),
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            ) {
                                append("Gemini: ")
                            }
                            withStyle(style = SpanStyle(color = Color(0xFFCBD5E1))) {
                                append("\"$liveMotivation\"")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to Refresh Insight 🔄",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF818CF8),
                        modifier = Modifier
                            .clickable { viewModel.fetchMotivation("General") }
                            .padding(vertical = 4.dp).testTag("refresh_insight_btn")
                    )
                }
            }
        }

        // System Challenge Banner
        val challengeText by viewModel.dailyChallenge.collectAsState()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎯", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "DAILY CHALLENGE MISSION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = challengeText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Busy / Stressful day toggler
        val isSelectedDateBusy = remember(viewModel.allDayStatuses.value, selectedDate) {
            viewModel.allDayStatuses.value.any { it.date == selectedDate && it.isBusy }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .clickable { viewModel.toggleBusyDay(selectedDate) },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelectedDateBusy) Color(0xFFFEE2E2) else Color.White
            ),
            border = BorderStroke(
                1.dp,
                if (isSelectedDateBusy) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🌪️ Mark Today as Busy/Stressful",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelectedDateBusy) Color(0xFF991B1B) else Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Protects your streaks from breaking and boosts XP by 1.5x on completed tasks!",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelectedDateBusy) Color(0xFF991B1B).copy(alpha = 0.8f) else Color.Gray
                    )
                }
                Checkbox(
                    checked = isSelectedDateBusy,
                    onCheckedChange = { viewModel.toggleBusyDay(selectedDate) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFDC2626),
                        uncheckedColor = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier.testTag("busy_day_checkbox")
                )
            }
        }

        // Stressful limit nudge: warned if user marks 3 busy days in a week
        val weeklyBusyCount = remember(viewModel.allDayStatuses.value) {
            viewModel.getBusyDaysCountInWeek()
        }
        if (weeklyBusyCount >= 3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Dhyan De: You've marked $weeklyBusyCount days as busy this week. Avoid habitual dodging or burnout!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF991B1B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Goals List headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Habit Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${activeGoalsToday.size} Active Goals",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }

        if (activeGoalsToday.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No habits configured yet!", fontWeight = FontWeight.Bold)
                    Text("Tap the 'Goals' tab below to add your first habits.", style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeGoalsToday.forEach { goal ->
                    val entryForGoal = entriesOnSelectedDate.find { it.goalId == goal.id }
                    val isChecked = entryForGoal?.completed ?: false

                    TodayGoalItemCard(
                        goal = goal,
                        isChecked = isChecked,
                        entry = entryForGoal,
                        onCheckChanged = { check ->
                            val defaultCompletedVal = if (check) goal.targetValue else 0.0
                            viewModel.logGoalEntry(
                                goalId = goal.id,
                                date = selectedDate,
                                value = defaultCompletedVal,
                                completed = check
                            )
                        },
                        onCustomValueLogged = { value, label ->
                            viewModel.logGoalEntry(
                                goalId = goal.id,
                                date = selectedDate,
                                value = value,
                                levelLabel = label,
                                completed = value >= goal.targetValue
                            )
                        },
                        onTriggerGeminiMotivation = {
                            viewModel.fetchMotivation(goal.name)
                        },
                        onTriggerSimulatedAvoidLaunch = {
                            viewModel.simulateAppLaunch(goal.avoidPackageNames, goal)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayGoalItemCard(
    goal: Goal,
    isChecked: Boolean,
    entry: GoalEntry?,
    onCheckChanged: (Boolean) -> Unit,
    onCustomValueLogged: (Double, String) -> Unit,
    onTriggerGeminiMotivation: () -> Unit,
    onTriggerSimulatedAvoidLaunch: () -> Unit
) {
    var expandedLevels by remember { mutableStateOf(false) }

    val categoryColor = when (goal.category) {
        "Health" -> Color(0xFF4CAF50)
        "Productivity" -> Color(0xFF2196F3)
        "Learning" -> Color(0xFFFF9800)
        "Finance" -> Color(0xFF00BCD4)
        "Social" -> Color(0xFFE91E63)
        "Mindfulness" -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.primary
    }

    val categoryEmoji = when (goal.category) {
        "Health" -> "💧"
        "Productivity" -> "🏃"
        "Learning" -> "📚"
        "Finance" -> "💰"
        "Social" -> "🤝"
        "Mindfulness" -> "🧘"
        else -> "🎯"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_today_card_${goal.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) Color(0xFFF1F5F9).copy(alpha = 0.6f)
            else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isChecked) Color(0xFFE2E8F0)
            else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Tag Left Side
                Box(
                    modifier = Modifier
                        .background(categoryColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = goal.category,
                        color = categoryColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Ask Gemini Button
                    IconButton(
                        onClick = onTriggerGeminiMotivation,
                        modifier = Modifier.size(32.dp).testTag("ask_ai_btn_${goal.id}")
                    ) {
                        Text("🤖", fontSize = 16.sp)
                    }

                    // Simulated block button if avoidance goal
                    if (goal.avoidPackageNames.isNotBlank()) {
                        IconButton(
                            onClick = onTriggerSimulatedAvoidLaunch,
                            modifier = Modifier.size(32.dp).testTag("avoid_simulate_btn_${goal.id}")
                        ) {
                            Text("🚫", fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Emoji Icon Container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isChecked) Color(0xFFE2E8F0) else categoryColor.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(categoryEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Main Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                        color = if (isChecked) Color.Gray else Color(0xFF1E293B)
                    )
                    if (goal.description.isNotBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Target: ${goal.targetValue} ${goal.unit} | Type: ${goal.type.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    if (entry != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val progressLabel = if (entry.completionLevelLabel.isNotBlank()) entry.completionLevelLabel else "${entry.value} ${goal.unit}"
                        Text(
                            text = "Progress: $progressLabel",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }

                // Check button
                IconButton(
                    onClick = { onCheckChanged(!isChecked) },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isChecked) Color(0xFF10B981) else Color(0xFFE2E8F0),
                            CircleShape
                        ).testTag("check_goal_toggle_button_${goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check Goal",
                        tint = if (isChecked) Color.White else Color(0xFF64748B)
                    )
                }
            }

            // Quick Levels Accordion Toggle for customized logging
            val levelsList = remember(goal.completionLevels) {
                goal.completionLevels.split(",").map { item ->
                    val pair = item.split(":")
                    val label = pair.getOrNull(0) ?: "Log"
                    val percentageStr = pair.getOrNull(1) ?: "100"
                    val percentage = percentageStr.toDoubleOrNull() ?: 100.0
                    label to (goal.targetValue * (percentage / 100.0))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                onClick = { expandedLevels = !expandedLevels },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ Log Custom Completion Level",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Icon(
                        imageVector = if (expandedLevels) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand levels"
                    )
                }
            }

            AnimatedVisibility(visible = expandedLevels) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Tap a level below for quick logging:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Wrap levels in responsive row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        levelsList.forEach { (label, value) ->
                            val isCurrentLevel = entry != null && entry.value == value
                            
                            SuggestionChip(
                                onClick = {
                                    onCustomValueLogged(value, label)
                                    expandedLevels = false
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("level_chip_${goal.id}_$label"),
                                border = if (isCurrentLevel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            )
                        }
                    }
                }
            }
        }
    }
}

// FlowRow standard backwards-supported helper
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Simple wrapped representation for layouts
        content()
    }
}
