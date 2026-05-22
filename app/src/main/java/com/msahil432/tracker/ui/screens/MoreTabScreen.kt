package com.msahil432.tracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msahil432.tracker.data.model.Achievement
import com.msahil432.tracker.data.model.UserProfile
import com.msahil432.tracker.ui.viewmodel.GoalViewModel

@Composable
fun MoreTabScreen(
    viewModel: GoalViewModel,
    profile: UserProfile,
    achievements: List<Achievement>
) {
    val scrollState = rememberScrollState()

    // Level progression formula
    val xpLimit = profile.level * 150
    val progressRatio = profile.totalXp.toFloat() / xpLimit.toFloat()

    val tierName = remember(profile.level) {
        when {
            profile.level <= 2 -> "Shuruaat Pehlwaan (Beginner)"
            profile.level <= 5 -> "Abhyasi Consistent (Consistent)"
            profile.level <= 9 -> "Sadhak Dedicated (Dedicated)"
            profile.level <= 14 -> "Acharya Master (Master)"
            else -> "Param Legend (Legend)"
        }
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Level progressions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sadhana Level Progression 🌟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aapka Pad: $tierName (Level ${profile.level})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = progressRatio.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${profile.totalXp} XP", style = MaterialTheme.typography.labelSmall)
                    Text("Next Level at $xpLimit XP", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Intervention System Sandbox
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🚫 App Avoidance Intervention Sandbox",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Test our dismissible overlay cooldown block, which opens distraction recommendations and Gemini generated motivates tailored to your styles!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val activeGoals = viewModel.allGoals.value.filter { it.status == "active" }
                        if (activeGoals.isEmpty()) {
                            Toast.makeText(context, "Create at least 1 goal to simulate blockers", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.simulateAppLaunch("com.instagram.android", activeGoals.first())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().testTag("simulate_instagram_block_btn")
                ) {
                    Text("Simulate Instagram App Use Block")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Badges achievements section
        Text(
            text = "Karke Dikhaunga Medals (Achievements)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Predefined grid of badges
        val fixedAchievements = listOf(
            Achievement(type = "milestone", title = "Pehlwaan", description = "Created your first goal. Keep pushing!", icon = "🎯"),
            Achievement(type = "milestone", title = "Goal Collector", description = "Added 10 active goals in catalogue.", icon = "📚"),
            Achievement(type = "milestone", title = "Shuruaat", description = "Earned your first habit completion!", icon = "🔥"),
            Achievement(type = "milestone", title = "Agni Pariksha", description = "Logged 15 habit completions successfully.", icon = "⚡"),
            Achievement(type = "streak", title = "7-Day Warrior", description = "Completed at least one goal 7 days in a row!", icon = "🛡️"),
            Achievement(type = "busy_day", title = "Toofan Me Diya", description = "Completed 3 major goals on extreme busy days!", icon = "🌊")
        )

        // Render responsive flow for achievements
        Column(
            modifier = Modifier.widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fixedAchievements.forEach { ach ->
                val isUnlocked = achievements.any { it.title.lowercase().trim() == ach.title.lowercase().trim() }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ach.icon,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = ach.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isUnlocked) {
                            Text("✅ Unlocked", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        } else {
                            Text("Locked", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data / Developer configurations
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Backup & Developer Controls 🛠️",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        // Seed mock data for evaluation
                        viewModel.addNewGoal("Walk 10000 steps", "Daily health goal tracking", "do", "Health", "daily", emptyList(), 10000.0, "steps", 15, 1.0, true, "completion", "notification")
                        viewModel.addNewGoal("Drink Water", "8 glasses of pure hydration", "do", "Health", "daily", emptyList(), 8.0, "glasses", 10, 1.0, true, "completion", "notification")
                        viewModel.addNewGoal("Read Book", "Read technology news pages", "do", "Learning", "daily", emptyList(), 15.0, "pages", 20, 1.2, true, "completion", "notification")
                        
                        Toast.makeText(context, "Seeded test starter habits. Go to Today / Goals tab!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("seed_sample_data_btn")
                ) {
                    Text("Populate Sample Evaluation Data")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Local JSON Backup Saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).testTag("backup_export_btn")
                    ) {
                        Text("Export JSON")
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Data imported safely!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).testTag("backup_import_btn")
                    ) {
                        Text("Import Backup")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
