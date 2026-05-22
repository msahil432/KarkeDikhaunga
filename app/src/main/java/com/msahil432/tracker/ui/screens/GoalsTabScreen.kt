package com.msahil432.tracker.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.msahil432.tracker.data.model.Goal
import com.msahil432.tracker.ui.viewmodel.GoalViewModel

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
