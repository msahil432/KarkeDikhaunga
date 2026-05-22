package com.msahil432.tracker.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msahil432.tracker.data.model.Goal
import com.msahil432.tracker.data.model.GoalEntry
import com.msahil432.tracker.ui.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InsightsTabScreen(
    viewModel: GoalViewModel,
    goals: List<Goal>,
    entries: List<GoalEntry>
) {
    val scrollState = rememberScrollState()

    // Analytics processing
    val totalXp = remember(entries, goals) {
        // Compute dynamically or fetch from profile
        val profile = viewModel.userProfile.value
        profile?.totalXp ?: 15
    }

    val categoryCounts = remember(goals) {
        val distribution = mutableMapOf<String, Float>()
        goals.forEach { g ->
            distribution[g.category] = (distribution[g.category] ?: 0f) + 1f
        }
        distribution
    }

    // Weekly summary calculations
    val weeklyCompletions = remember(entries) {
        val completions = IntArray(7) { 0 }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        // Match current week's dates
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekDates = mutableListOf<String>()
        for (i in 0 until 7) {
            weekDates.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        entries.forEach { entry ->
            val idx = weekDates.indexOf(entry.date)
            if (idx != -1 && entry.completed) {
                completions[idx]++
            }
        }
        completions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pragati Dashboard",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Total analytics cards rows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total XP Earned", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalXp Points", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Completion % (Avg)", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    val averagePercent = remember(entries, goals) {
                        if (entries.isEmpty()) "0%" else {
                            val completed = entries.count { it.completed }
                            val totalRatio = (completed.toFloat() / entries.size.toFloat()) * 100f
                            "${totalRatio.toInt()}%"
                        }
                    }
                    Text(averagePercent, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas Custom Native Bar Chart drawing for Weekly completions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Activity Completions 📈",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Completions count for current week days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val maxVal = (weeklyCompletions.maxOrNull() ?: 1).coerceAtLeast(3).toFloat()

                    val blockWidth = size.width / 7f
                    val chartHeight = size.height - 30.dp.toPx()

                    for (i in 0 until 7) {
                        val valPercent = weeklyCompletions[i] / maxVal
                        val barHeight = chartHeight * valPercent
                        val xOffset = blockWidth * i + (blockWidth * 0.2f)
                        val yOffset = chartHeight - barHeight

                        // Draw background track
                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.15f),
                            topLeft = Offset(xOffset, 0f),
                            size = Size(blockWidth * 0.6f, chartHeight)
                        )

                        // Draw foreground Bar
                        drawRect(
                            color = Color(0xFF2196F3),
                            topLeft = Offset(xOffset, yOffset),
                            size = Size(blockWidth * 0.6f, barHeight)
                        )
                    }
                }

                // Days Text footer label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Native Canvas Category Pie Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Category Distribution Pie 🎨",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (categoryCounts.isEmpty()) {
                    Text("No category distribution logs yet", style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            var currentAngle = 0f
                            val totalVal = categoryCounts.values.sum().coerceAtLeast(1f)
                            
                            val categoryColorsMap = mapOf(
                                "Health" to Color(0xFF4CAF50),
                                "Productivity" to Color(0xFF2196F3),
                                "Learning" to Color(0xFFFF9800),
                                "Finance" to Color(0xFF00BCD4),
                                "Social" to Color(0xFFE91E63),
                                "Mindfulness" to Color(0xFF9C27B0)
                            )

                            categoryCounts.forEach { (cat, count) ->
                                val sweep = (count / totalVal) * 360f
                                drawArc(
                                    color = categoryColorsMap[cat] ?: Color.LightGray,
                                    startAngle = currentAngle,
                                    sweepAngle = sweep,
                                    useCenter = true
                                )
                                currentAngle += sweep
                            }
                        }

                        // Labels list on right  
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val colorsMap = mapOf(
                                "Health" to Color(0xFF4CAF50),
                                "Productivity" to Color(0xFF2196F3),
                                "Learning" to Color(0xFFFF9800),
                                "Finance" to Color(0xFF00BCD4),
                                "Social" to Color(0xFFE91E63),
                                "Mindfulness" to Color(0xFF9C27B0)
                            )
                            categoryCounts.forEach { (cat, count) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(colorsMap[cat] ?: Color.LightGray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$cat (${count.toInt()})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goals Needing Attention list (completions under 30%)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Habit attention trackers ⚠️",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))

                val needsAttentionGoals = remember(goals, entries) {
                    goals.filter { goal ->
                        val goalLogs = entries.filter { it.goalId == goal.id }
                        if (goalLogs.isEmpty()) true else {
                            val completes = goalLogs.count { it.completed }
                            (completes.getRatioPercent(goalLogs.size)) < 40f
                        }
                    }
                }

                if (needsAttentionGoals.isEmpty()) {
                    Text(
                        text = "Everything is balanced! All habits on check.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    needsAttentionGoals.take(3).forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = goal.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Attention needed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

fun Int.getRatioPercent(total: Int): Float {
    if (total == 0) return 0f
    return (this.toFloat() / total.toFloat()) * 100f
}
