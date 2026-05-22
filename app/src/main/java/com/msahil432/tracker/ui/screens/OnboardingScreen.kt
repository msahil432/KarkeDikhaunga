package com.msahil432.tracker.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingWizard(
    onComplete: (
        name: String, age: Int, gender: String, occupation: String, language: String,
        motivationStyle: String, sleepTime: String, wakeTime: String,
        sleepReminders: Boolean, wakeReminders: Boolean, reminderMode: String,
        sound: String, vibration: Boolean
    ) -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Phase 1 inputs
    var nickname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("24") }
    var gender by remember { mutableStateOf("Male") }
    var occupation by remember { mutableStateOf("Engineer") }
    var motivationStyle by remember { mutableStateOf("Balanced") } // gentle, tough, balanced

    // Phase 2 inputs  
    var sleepTime by remember { mutableStateOf("22:30") }
    var wakeTime by remember { mutableStateOf("06:30") }
    var sleepReminders by remember { mutableStateOf(true) }
    var wakeReminders by remember { mutableStateOf(true) }

    // Phase 3 inputs
    var preferredLanguage by remember { mutableStateOf("English") }
    var defaultReminderMode by remember { mutableStateOf("notification") } // notification, popup, none
    var defaultVibration by remember { mutableStateOf(true) }
    var defaultSound by remember { mutableStateOf("Default Chime") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo
            Text(
                text = "Karke Dikhaunga 🎯",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "I'll prove it by doing - Your dynamic habits companion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Step Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..4) {
                    val isActive = i <= step
                    val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (step) {
                1 -> {
                    Text(
                        text = "Bataaie, Aap Kon Hain? 🙌",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("What should we call you? (Nickname)") },
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                        singleLine = true,
                        placeholder = { Text("e.g. Sahil") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Your Age") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation / Lifestyle") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. Student, Homemaker, Designer") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Gender Selection",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Male", "Female", "Other").forEach { g ->
                            val isSel = gender == g
                            FilterChip(
                                selected = isSel,
                                onClick = { gender = g },
                                label = { Text(g) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                2 -> {
                    Text(
                        text = "Personal Inspiration Style 🧠",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "How do you want your Companion Gemini feedback prompts to speech/nudge you?",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    listOf(
                        "Tough" to "🔥 Tough Love: Direct, high-energy, kicks procrastination!",
                        "Gentle" to "🌸 Gentle Encouragement: Kind support, peaceful mindfulness.",
                        "Balanced" to "⚖️ Balanced: Consistency structured motivation with dynamic logic."
                    ).forEach { (styleKey, label) ->
                        val selected = motivationStyle.lowercase() == styleKey.lowercase()
                        Card(
                            onClick = { motivationStyle = styleKey },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                3 -> {
                    Text(
                        text = "Sleep & Routine Timers 💤",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = sleepTime,
                        onValueChange = { sleepTime = it },
                        label = { Text("What time do you usually sleep?") },
                        placeholder = { Text("e.g. 22:30") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text("What time do you usually wake up?") },
                        placeholder = { Text("e.g. 06:30") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Morning Reminders Overview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("See today's goals on waking up", style = MaterialTheme.typography.labelSmall)
                        }
                        Switch(checked = wakeReminders, onCheckedChange = { wakeReminders = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Night Review Catch-ups", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Log any missed entries before bedtime", style = MaterialTheme.typography.labelSmall)
                        }
                        Switch(checked = sleepReminders, onCheckedChange = { sleepReminders = it })
                    }
                }

                4 -> {
                    Text(
                        text = "Companion Defaults & Preferences ⚙️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Default Reminder Format", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "notification" to "Push Dialog",
                            "popup" to "Full Popup",
                            "none" to "Manual Log"
                        ).forEach { (key, display) ->
                            val isSel = defaultReminderMode == key
                            FilterChip(
                                selected = isSel,
                                onClick = { defaultReminderMode = key },
                                label = { Text(display) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = preferredLanguage,
                        onValueChange = { preferredLanguage = it },
                        label = { Text("Preferred Language for Motivations") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("English, Hindi, Hinglish...") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vibration patterns enabled", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = defaultVibration, onCheckedChange = { defaultVibration = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Prev")
                    }
                }

                val canProceed = step < 4
                Button(
                    onClick = {
                        if (canProceed) {
                            if (step == 1 && nickname.isBlank()) {
                                nickname = "Mitra" // Default
                            }
                            step++
                        } else {
                            // Completed!
                            onComplete(
                                nickname.ifBlank { "Mitra" },
                                age.toIntOrNull() ?: 24,
                                gender,
                                occupation.ifBlank { "Achiever" },
                                preferredLanguage.ifBlank { "English" },
                                motivationStyle,
                                sleepTime,
                                wakeTime,
                                sleepReminders,
                                wakeReminders,
                                defaultReminderMode,
                                defaultSound,
                                defaultVibration
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("onboarding_next_button")
                ) {
                    Text(if (canProceed) "Next" else "Finish! 🚀")
                }
            }
        }
    }
}
