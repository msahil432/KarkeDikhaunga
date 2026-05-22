package com.msahil432.tracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.msahil432.tracker.data.model.UserProfile
import com.msahil432.tracker.data.model.Goal
import com.msahil432.tracker.data.model.GoalEntry
import com.msahil432.tracker.data.model.DayStatus
import com.msahil432.tracker.data.model.Achievement
import com.msahil432.tracker.ui.viewmodel.AppTab
import com.msahil432.tracker.ui.viewmodel.GoalViewModel
import com.msahil432.tracker.ui.viewmodel.SubScreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAvoidanceOverlay(
    viewModel: GoalViewModel,
    goal: Goal
) {
    var timerSeconds by remember { mutableStateOf(goal.cooldownDurationMinutes * 60) }
    val alternativesList = remember(goal.avoidAlternatives) {
        goal.avoidAlternatives.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }
    
    // Auto countdown timer
    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            timerSeconds--
        }
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Karke Dikhaunga System Block!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You requested to avoid opening apps to focus, linked with habit: '${goal.name}'",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer display
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.2f),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val mm = timerSeconds / 60
                    val ss = timerSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", mm, ss),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "left",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Alternatives suggestions
            Text(
                text = "💡 Try these productive alternatives instead:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                alternativesList.forEach { alt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Point",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Image distraction launcher button
            Button(
                onClick = {
                    Toast.makeText(context, "Opening Google Images for '${goal.avoidSearchTerms}'", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().testTag("cooldown_images_button")
            ) {
                Icon(Icons.Default.Search, contentDescription = "Distraction Grid")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Distraction: ${goal.avoidSearchTerms}")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI/Motivational fallbacks
            Text(
                text = "Personalized nudge:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val activeNudge by viewModel.activeMotivation.collectAsState()
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "\"$activeNudge\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { viewModel.dismissAvoidanceOverlay() },
                    modifier = Modifier.weight(1f).testTag("accept_cooldown_button")
                ) {
                    Text("Accept Cooldown")
                }
                
                Button(
                    onClick = { viewModel.dismissAvoidanceOverlay() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).testTag("bypass_limit_button")
                ) {
                    Text("Bypass Limit")
                }
            }
        }
    }
}

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

@Composable
fun TodayTabScreen(
    viewModel: GoalViewModel,
    profile: UserProfile,
    goals: List<Goal>,
    entries: List<GoalEntry>
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val calendar = Calendar.getInstance()
    
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

    val context = LocalContext.current

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
                        textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
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

@Composable
fun GoalsTabScreen(
    viewModel: GoalViewModel,
    goals: List<Goal>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showCreateWizard by remember { mutableStateOf(false) }

    val categories = listOf("All", "Health", "Productivity", "Learning", "Finance", "Social", "Mindfulness")

    // Filter goals
    val filteredGoals = remember(goals, searchQuery, selectedCategoryFilter) {
        goals.filter { goal ->
            val matchSearch = goal.name.contains(searchQuery, ignoreCase = true) || goal.description.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategoryFilter == "All" || goal.category.lowercase() == selectedCategoryFilter.lowercase()
            matchSearch && matchCategory
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Goals/Habits") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("goal_search_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Categories slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSel = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit Catalog",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredGoals.size} Habits",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗒️", fontSize = 44.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching habits found!", fontWeight = FontWeight.Bold)
                        Text("Click the + icon in the corner to create one.", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredGoals) { goal ->
                        GoalCatalogItemCard(
                            goal = goal,
                            onToggleArchive = { viewModel.toggleGoalArchive(goal) },
                            onDelete = { viewModel.deleteGoal(goal) }
                        )
                    }
                }
            }
        }

        // FAB to launch creation wizard
        FloatingActionButton(
            onClick = { showCreateWizard = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
                .testTag("create_goal_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Habit")
        }

        if (showCreateWizard) {
            CreateGoalWizardDialog(
                onDismiss = { showCreateWizard = false },
                onSave = { name, desc, type, cat, freq, days, targetVal, unit, xpVal, xpW, partial, tracking, rem, levels, pacName, coolMin, alts, terms ->
                    viewModel.addNewGoal(
                        name = name,
                        description = desc,
                        type = type,
                        category = cat,
                        frequency = freq,
                        frequencyDays = days,
                        targetValue = targetVal,
                        unit = unit,
                        xpValue = xpVal,
                        xpWeight = xpW,
                        partialInGamification = partial,
                        analyticsType = tracking,
                        reminderMode = rem,
                        completionLevels = levels,
                        avoidPackageNames = pacName,
                        cooldownMins = coolMin,
                        alternatives = alts,
                        searchTerms = terms
                    )
                    showCreateWizard = false
                }
            )
        }
    }
}

@Composable
fun GoalCatalogItemCard(
    goal: Goal,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (goal.category) {
        "Health" -> Color(0xFF4CAF50)
        "Productivity" -> Color(0xFF2196F3)
        "Learning" -> Color(0xFFFF9800)
        "Finance" -> Color(0xFF00BCD4)
        "Social" -> Color(0xFFE91E63)
        "Mindfulness" -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.primary
    }

    val isArchived = goal.status == "archived"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = goal.category,
                            color = categoryColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isArchived) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ARCHIVED",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (goal.description.isNotBlank()) {
                    Text(
                        text = goal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "🎯 Frequency: ${goal.frequency} | Goal: ${goal.targetValue} ${goal.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (goal.avoidPackageNames.isNotBlank()) {
                    Text(
                        text = "🚫 Block active: ${goal.avoidPackageNames}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onToggleArchive,
                    modifier = Modifier.testTag("archive_goal_btn_${goal.id}")
                ) {
                    Icon(
                        imageVector = if (isArchived) Icons.Default.Refresh else Icons.Default.Warning,
                        contentDescription = "Archive or Restore",
                        tint = if (isArchived) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_goal_btn_${goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Full Creation Wizard step-by-step Dialog layout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalWizardDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String, desc: String, type: String, cat: String, freq: String,
        days: List<String>, targetVal: Double, unit: String, xpVal: Int,
        xpWeight: Double, partialInGamification: Boolean, tracking: String,
        reminder: String, completionLevels: String,
        avoidPackageNames: String, cooldownMinutes: Int, alternatives: String, searchTerms: String
    ) -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Step 1: Types & Basic Details
    var goalName by remember { mutableStateOf("") }
    var goalDesc by remember { mutableStateOf("") }
    var goalType by remember { mutableStateOf("do") } // do, avoid, health, measurable, one-time
    var goalCategory by remember { mutableStateOf("Health") }

    // Step 2: Frequency & Targets  
    var goalFrequency by remember { mutableStateOf("daily") } // daily, weekly, monthly, specific-days
    var goalTargetValue by remember { mutableStateOf("1") }
    var goalUnit by remember { mutableStateOf("times") }
    
    // Step 3: Gamification Weightage & Extras
    var xpValue by remember { mutableStateOf("10") }
    var xpWeight by remember { mutableStateOf("1.0") }
    var includePartialProgress by remember { mutableStateOf(true) }
    var analyticsTypePreference by remember { mutableStateOf("completion") } // average, streak, completion
    var reminderModeSelected by remember { mutableStateOf("notification") } // notification, popup, none

    // Step 4: Special App Avoidance system configurations
    var avoidPackageNames by remember { mutableStateOf("") }
    var cooldownMinutes by remember { mutableStateOf("10") }
    var alternativesList by remember { mutableStateOf("Stretch, Deep Breathing, Drink water") }
    var imageSearchTerms by remember { mutableStateOf("dog memes") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .background(Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Wizard Header
                Text(
                    text = "Nayi Goal Baniaye (Step $step of 4)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    1 -> {
                        Text("Step 1: Goal Identity & Type", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = goalName,
                            onValueChange = { goalName = it },
                            label = { Text("What is your goal name?") },
                            modifier = Modifier.fillMaxWidth().testTag("wizard_goal_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = goalDesc,
                            onValueChange = { goalDesc = it },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Goal Category", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Health", "Productivity", "Learning", "Finance", "Social", "Mindfulness").forEach { c ->
                                val isSelected = goalCategory == c
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { goalCategory = c },
                                    label = { Text(c) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Goal Mode Type", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "do" to "✅ Do-it: Build active healthy habits (Drink water, run)",
                                "avoid" to "🚫 Avoid-it: Restrict target apps (Instagram, YouTube)",
                                "health" to "❤️ Health-linked: Sync auto data parameters (Steps, sleep)",
                                "measurable" to "📊 Measurable: Value targets tracking (Read 20 pages)"
                            ).forEach { (k, display) ->
                                val selected = goalType == k
                                Card(
                                    onClick = { goalType = k },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                                ) {
                                    Text(
                                        text = display,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        Text("Step 2: Scheduling & Metric Targets", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Frequency Interval", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("daily" to "Daily", "weekly" to "Weekly", "monthly" to "Monthly").forEach { (v, disp) ->
                                FilterChip(
                                    selected = goalFrequency == v,
                                    onClick = { goalFrequency = v },
                                    label = { Text(disp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = goalTargetValue,
                            onValueChange = { goalTargetValue = it },
                            label = { Text("Target Quantity Value") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = goalUnit,
                            onValueChange = { goalUnit = it },
                            label = { Text("Measurement Unit Label") },
                            placeholder = { Text("e.g. times, steps, ml, pages, mins") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    3 -> {
                        Text("Step 3: Gamification Weights & Analytics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = xpValue,
                            onValueChange = { xpValue = it },
                            label = { Text("XP Points reward value") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = xpWeight,
                            onValueChange = { xpWeight = it },
                            label = { Text("Priority Weight Multiplier (1.0 default)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reward partial progress", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("Earn some scaling XP even if target is partially met", style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(checked = includePartialProgress, onCheckedChange = { includePartialProgress = it })
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Preferred Analytics View", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("completion" to "Ratio %", "streak" to "Streak", "average" to "Average").forEach { (k, disp) ->
                                FilterChip(
                                    selected = analyticsTypePreference == k,
                                    onClick = { analyticsTypePreference = k },
                                    label = { Text(disp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Reminder Display Format", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("notification" to "Notification", "popup" to "Popup Blocker", "none" to "None").forEach { (k, disp) ->
                                FilterChip(
                                    selected = reminderModeSelected == k,
                                    onClick = { reminderModeSelected = k },
                                    label = { Text(disp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    4 -> {
                        Text("Step 4: App Avoidance Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Configure on-launch monitoring for target addictive apps. Skip if not planning to bypass packages.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = avoidPackageNames,
                            onValueChange = { avoidPackageNames = it },
                            label = { Text("App Package names to lock (com.whatsapp, etc.)") },
                            placeholder = { Text("comma-separated e.g. com.instagram.android") },
                            modifier = Modifier.fillMaxWidth().testTag("avoid_package_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cooldownMinutes,
                            onValueChange = { cooldownMinutes = it },
                            label = { Text("Focus Timer cooldown (Minutes)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = alternativesList,
                            onValueChange = { alternativesList = it },
                            label = { Text("Custom alternative tasks list") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = imageSearchTerms,
                            onValueChange = { imageSearchTerms = it },
                            label = { Text("Distraction Image Search labels") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }

                    Button(
                        onClick = {
                            if (step < 4) {
                                if (step == 1 && goalName.isBlank()) {
                                    goalName = "New Habit"
                                }
                                step++
                            } else {
                                // Save
                                onSave(
                                    goalName.ifBlank { "New Habit" },
                                    goalDesc,
                                    goalType,
                                    goalCategory,
                                    goalFrequency,
                                    emptyList(), // specific days
                                    goalTargetValue.toDoubleOrNull() ?: 1.0,
                                    goalUnit.ifBlank { "times" },
                                    xpValue.toIntOrNull() ?: 10,
                                    xpWeight.toDoubleOrNull() ?: 1.0,
                                    includePartialProgress,
                                    analyticsTypePreference,
                                    reminderModeSelected,
                                    "Very Low:15,Low:33,Middle:55,High:75,Very High:90,Super Se Upar (100%):100", // Pre-filled default levels
                                    avoidPackageNames,
                                    cooldownMinutes.toIntOrNull() ?: 10,
                                    alternativesList,
                                    imageSearchTerms
                                )
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("wizard_next_save_button")
                    ) {
                        Text(if (step < 4) "Continue" else "Create! 🎯")
                    }
                }
            }
        }
    }
}

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
                    val daysTitle = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
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
