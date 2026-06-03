# Project Guide Plan: Karke Dikhaunga — Android Habit Tracker

A local-first Android habit/goal tracking app built with Kotlin + Jetpack Compose, Material Expressive design with dynamic theming, on-device AI motivation (Gemini Nano), app usage blocking, health data integration, and rich analytics - with optional Google Drive backup.

---

## 1. Core Concept

**App Name:** Karke Dikhaunga ("I'll prove it by doing")
**Platform:** Android 14+ (API 34)
**Tech Stack:** Kotlin, Jetpack Compose, Room DB, Health Connect, UsageStats API, Gemini Nano (ML Kit)
**Data:** Local-first (Room/SQLite) with optional Google Drive sync
**Auth:** No user accounts — purely local

---

## 2. Feature Breakdown

### 2.1 Goal Management

**Goal Types:**
- **Do-it goals:** Track regular habits (exercise, read, drink water, custom)
- **Avoid-it goals:** Avoid specific apps or behaviors (with cooldown interventions)
- **Health-linked goals:** Auto-populated from Health Connect (steps, workouts, etc.)
- **One-time goals:** Deadline-based tasks
- **Measurable goals:** Numeric targets (e.g., run 100km this month)

**Goal Attributes:**
- Name, description, icon/emoji
- Category (predefined: Health, Productivity, Learning, Finance, Social, Mindfulness + user-defined custom categories)
- Created date, last modified date
- Frequency: daily, weekly, monthly, specific days, custom intervals
- Reminder configuration (time, days, smart triggers)
- Analytics type preference (average, min, max, total, streak, completion %)
- Status (active, paused, archived, completed)
- XP value assigned
- Gamification weight (default: equal to all goals, user-overridable)
- Partial goal inclusion in gamification (toggle: count partial progress toward XP or not)

**Completion Stats (per goal):**
- User defines completion levels for each goal
- Prefilled defaults: Very Low (0%), Low (20%), Middle (40%), High (60%), Very High (80%), Super Se Upar Duper Awesome (90%)
- User can rename labels, adjust percentages, or add custom levels
- Used for quick entry (tap a level instead of precise input)

**Goal Creation:**
- Full-screen step-by-step creation wizard
- Steps: Type → Name/Category → Frequency → Reminders → Analytics preferences → Confirm

**Past Entry Logging:**
- Calendar view (tap any past date to log)
- Goal detail screen (scroll back to past dates)
- Both methods available

---

### 2.2 App Avoidance System

**Detection:**
- UsageStats API (permission-based) to automatically detect target app launches

**Cooldown Intervention:**
- Dismissible overlay notification with:
    - Countdown timer (user-defined duration per goal)
    - Alternative app suggestions (user-configurable list)
    - Google Image search launcher (user-defined search terms like "cat memes", "dog pics", "nature wallpapers" — opens a quick image grid as distraction)
    - Motivational message (Gemini Nano generated)
- User can dismiss and continue or accept the cooldown

**Configurable per goal:**
- List of apps to monitor
- Cooldown duration
- Alternative suggestions
- Intervention frequency (every launch, once per hour, etc.)

---

### 2.3 Health Data Integration

**Source:** Google Health Connect API
**Supported data types:**
- Steps
- Workouts (type, duration, calories)
- Sleep
- Heart rate
- Custom metrics as Health Connect expands

**Behavior:**
- Auto-sync health data into relevant goals
- User maps a goal to a health metric
- Progress updates automatically without manual entry
- Manual entry option always available (user can override or supplement auto-synced data)
- For goals not linked to Health Connect, manual entry is the primary input

---

### 2.4 Analytics & Insights

**Per-Goal Analytics (user selects type per goal):**
- Streak tracking (consecutive days/periods completed)
- Completion percentage (daily/weekly/monthly)
- Average, minimum, maximum, total values
- Trend graphs (line/bar charts)
- Comparison with previous periods

**Overall Analytics (Insights tab):**
- Category-wise breakdown (pie/bar chart)
- Weekly/monthly summary
- Best performing goals
- Goals needing attention
- Comparison with previous weeks

**Analytics Scheduling:**
- User sets per-goal analytics frequency (daily, weekly, monthly, custom)
- App checks daily for due analytics → sends notification with summary
- Dedicated Insights screen for on-demand viewing

---

### 2.5 Busy Day System

**Toggle:** Mark any day as "busy/stressful"
- Excluded from standard analytics calculations
- Soft limit: gentle nudge after 3 days marked in a week ("You've marked 3 days this week as busy")
- If goal is still completed on a busy day → special metric tracked
- Highlighted in analytics as "bonus completions" with extra motivation

---

### 2.6 On-Device AI (Gemini Nano)

**Use Cases:**
- Generate personalized motivational line per goal based on current analytics/streak
- Post-reminder feedback (on task completion or failure)
- Weekly summary narration
- Cooldown intervention messages for app avoidance

**Implementation:**
- Google AI Edge SDK (Gemini Nano, ML Kit)
- Runs entirely on-device (no network needed)
- Fallback: pre-written motivational templates if AI unavailable

---

### 2.7 Gamification

**Badges/Achievements:**
- Streak-based: 7-day, 14-day, 30-day, 60-day, 90-day, 365-day
- Milestone-based: First goal created, 10 goals, 50 completions, etc.
- Category-based: Health champion, Productivity pro, etc.
- Special: Completed on busy day, Perfect week, etc.

**XP/Points System:**
- Each goal completion earns XP (configurable per goal)
- Bonus XP for streaks, busy-day completions, perfect weeks
- XP accumulates over time
- **Weightage:** All goals have equal weight by default; user can assign custom weights per goal
- **Partial goals:** Per-goal toggle to include/exclude partial progress (e.g., 60% of weekly step target) from XP calculations

**Levels/Tiers:**
- Level progression based on total XP
- Tier names (e.g., Beginner → Consistent → Dedicated → Master → Legend)
- Level-up celebrations with AI-generated message

**Daily/Weekly Challenges:**
- System-generated mini-challenges (e.g., "Complete all goals today", "Try a new category")
- Bonus XP for completing challenges

---

### 2.8 Reminders & Notifications

**Types (per goal, user-configurable):**
- Scheduled push notifications (specific times)
- Recurring daily summary (morning/evening configurable)
- Smart reminders (triggered when incomplete tasks detected near end of day)
- Multiple reminders per goal (same day, different times, or across days)

**Reminder Display Modes (per goal):**
- **Notification:** Standard push notification
    - Simple goals (toggle/boolean): action buttons directly in notification (Done / Skip)
    - Numeric goals: quick-entry button in notification (opens mini input)
    - Completion stats goals: buttons for each level in notification (expandable)
- **Full-page popup:** Opens a full-screen entry page when triggered
    - User configures per goal: blocking (must act) or auto-dismiss after timeout
    - Useful for goals needing reflection or detailed input
- **None:** Don't send any reminder, let the user add details manually only

**Sleep Cycle Reminders (configurable per user):**
- User chooses: before sleep, after waking, both, or neither
- **Morning reminder:** Today's goals overview + goals with no data entered yesterday
- **Night reminder:** End-of-day review, incomplete goals, quick catch-up entry
- Sleep/wake times: auto-detected from Health Connect sleep data; user can override manually
- Configured during app setup (onboarding), editable in settings

**Sound & Vibration:**
- Global defaults set during onboarding
- Per-goal override available (custom sound, vibration pattern, silent)

**Post-Action Feedback:**
- On completion/failure, show quick motivational AI feedback
- Toast/snackbar with Gemini Nano-generated message

---

### 2.9 Data & Sync

**Local Storage:**
- Room database (SQLite)
- All data stored on-device by default

**Google Drive Backup:**
- No account system — uses Google Drive API with Google Sign-In (scoped to Drive only)
- Auto-sync: configurable frequency (daily, weekly, on-change)
- Manual export/import available
- Encrypted backup file
- Multi-device sync via shared Drive file

---

### 2.10 App Setup & Onboarding

**First Launch Flow (step-by-step wizard):**

1. **Welcome Screen** — App intro, value proposition
2. **Personal Info:**
    - Name (used in AI motivational messages, e.g., "Great job, [Name]!")
    - Age
    - Gender
    - Occupation/lifestyle (student, working, retired, homemaker, etc.)
    - Preferred language for motivational messages
    - Motivation style preference (gentle encouragement vs tough love vs balanced)
3. **Notification Defaults:**
    - Default reminder type: notification, full-page popup or none
    - Sound preference (select tone or silent)
    - Vibration pattern (on/off, intensity)
    - These become defaults; overridable per goal later
4. **Sleep Cycle Setup:**
    - Auto-detect from Health Connect sleep data (if available)
    - Manual input: usual sleep time & wake time
    - Choose sleep-cycle reminders: before sleep / after waking / both / neither
5. **Permissions:**
    - Health Connect access
    - Notification permission
    - Usage Stats (can skip, prompted later when needed)
    - Overlay permission (can skip, prompted later)
6. **Optional:** Create first goal (guided)

**All onboarding settings editable later in Settings.**

---

## 3. UI/UX Design

### 3.1 Design System
- **Material Expressive** (latest Android design language)
- **Dynamic Color (Material You):** Theme adapts to wallpaper colors
- **Dark & Light mode:** System-following + manual override
- **Typography:** Material 3 type scale
- **Motion:** Expressive transitions, meaningful animations

### 3.2 Navigation
**Bottom Navigation Bar (4 tabs):**
1. **Today** — Today's goals checklist, quick check-off, daily progress ring
2. **Goals** — All goals list/grid, categories, search/filter
3. **Insights** — Analytics dashboard, charts, trends, weekly summary
4. **More** — Settings, achievements/badges, levels, backup, about

### 3.3 Key Screens
1. **Onboarding Wizard:** Personal info, notification defaults, sleep cycle, permissions
2. **Today Screen:** Daily goal cards with check/fail actions, progress indicator, busy day toggle, daily challenge
3. **Goals List:** Filterable by category/status, grid or list view
4. **Goal Detail:** Full history, calendar view for past entries, per-goal analytics, edit
5. **Goal Creation Wizard:** Step-by-step (Type → Details → Completion Stats → Frequency → Reminders → Analytics → Gamification → Confirm)
6. **Insights Dashboard:** Summary cards, charts (streak, completion %, category breakdown), period selector
7. **Achievements Screen:** Badge gallery, XP progress bar, level indicator
8. **Settings:** Theme, notifications, sleep cycle, personal info, Google Drive sync, permissions, data management
9. **Calendar View:** Month view with day indicators, tap to log past entries
10. **Full-Page Reminder Popup:** Goal entry screen triggered by reminder (blocking or auto-dismiss)

---

## 4. Technical Architecture

### 4.1 Architecture Pattern
- **MVVM + Clean Architecture** (Presentation → Domain → Data layers)
- Unidirectional data flow with Compose state management

### 4.2 Key Libraries/Dependencies
| Purpose | Library |
|---------|---------|
| UI | Jetpack Compose + Material 3 Expressive |
| Navigation | Compose Navigation (type-safe) |
| Database | Room |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Health | Health Connect SDK |
| App Usage | UsageStatsManager (system API) |
| AI | Google AI Edge SDK (Gemini Nano) |
| Charts | Vico or compose-charts |
| Drive | Google Drive API v3 |
| Notifications | WorkManager + NotificationCompat |
| Background | WorkManager (periodic sync, analytics checks, reminders) |
| Overlay | WindowManager (TYPE_APPLICATION_OVERLAY for cooldown) |

### 4.3 Data Model (Core Entities)
- **Goal:** id, name, description, type (do/avoid/health/measurable), category, frequency, target, xp_value, xp_weight (default 1.0), include_partial_in_gamification (bool), analytics_type, reminder_display_mode (notification/popup/none), popup_behavior (blocking/auto-dismiss/timeout_seconds), status, created_at, modified_at
- **CompletionLevel:** id, goal_id, label, percentage, sort_order (prefilled defaults + user custom)
- **GoalEntry:** id, goal_id, date, value, completion_level_id, completed (bool), notes, is_busy_day, source (manual/health_connect/auto)
- **Reminder:** id, goal_id, time, days[], type (scheduled/smart/summary/sleep_cycle), enabled, sound, vibration_enabled
- **Achievement:** id, type, title, description, icon, unlocked_at, xp_reward
- **UserProfile:** name, age, gender, occupation, preferred_language, motivation_style, total_xp, level, current_streak, longest_streak, sleep_time, wake_time, sleep_reminder_enabled, wake_reminder_enabled, default_reminder_mode, default_sound, default_vibration
- **DayStatus:** date, is_busy (bool)
- **AppAvoidanceConfig:** id, goal_id, package_names[], cooldown_duration, alternatives[], image_search_terms[]
- **AnalyticsPreference:** id, goal_id, metric_type, frequency, last_sent

### 4.4 Permissions Required
- `PACKAGE_USAGE_STATS` — app usage detection
- `SYSTEM_ALERT_WINDOW` — cooldown overlay
- `POST_NOTIFICATIONS` — reminders
- `health connect permissions` — health data
- `INTERNET` — Google Drive sync
- `RECEIVE_BOOT_COMPLETED` — restart reminders after reboot

---

## 5. Implementation Phases

### Phase 1: Foundation (MVP)
1. Project setup (Kotlin, Compose, Hilt, Room, Material 3 Expressive)
2. Data models & Room database (all entities including CompletionLevel, UserProfile)
3. App setup/onboarding wizard (personal info, notification defaults, sleep cycle, permissions)
4. Goal CRUD (creation wizard with completion stats step, list, detail, edit, delete)
5. Today screen with daily checklist & check-off (using completion levels)
6. Basic bottom navigation (Today, Goals, placeholder Insights, More)
7. Dark/Light mode + Dynamic Color theming

### Phase 2: Reminders & Notifications
8. Scheduled reminders via WorkManager
9. Notification with action buttons (toggle/numeric based on goal type)
10. Full-page popup reminder mode (blocking + auto-dismiss variants)
11. Multiple reminders per goal
12. Sleep cycle-based reminders (morning/night)
13. Daily summary notification
14. Smart reminders (incomplete tasks near end of day)
15. Sound & vibration per-goal overrides

### Phase 3: Analytics & History
16. Past entry logging (calendar view + goal detail scroll-back)
17. Per-goal analytics engine (streak, completion %, avg/min/max/total)
18. Insights dashboard with charts
19. Period comparison (this week vs last week)
20. Busy day toggle + exclusion logic + bonus completion tracking
21. Analytics notification scheduling

### Phase 4: Integrations
22. Health Connect integration (auto-sync health metrics + manual entry)
23. Sleep data detection for sleep cycle reminders
24. UsageStats API integration (app avoidance detection)
25. Cooldown overlay system (timer + alternatives + Google Image search)
26. Google Drive backup (auto-sync + manual export/import)

### Phase 5: AI & Gamification
27. Gemini Nano integration (on-device motivational messages, personalized with user name/style)
28. AI-powered post-action feedback
29. Achievement/badge system
30. XP/points + weighted goals + partial progress toggle
31. Level progression
32. Daily/weekly challenges
33. Level-up celebrations

### Phase 6: Polish
34. Animations & transitions (Material Expressive motion)
35. Edge cases & error handling
36. Performance optimization
37. Accessibility (TalkBack, content descriptions)
38. Testing (unit, UI, integration)

---

## 6. Verification Plan

1. **Unit tests:** ViewModels, use cases, analytics calculations, streak logic
2. **UI tests:** Compose testing for key flows (goal creation, check-off, navigation)
3. **Integration tests:** Room DB queries, Health Connect data flow, Drive sync
4. **Manual QA:**
    - Test dynamic theming with different wallpapers
    - Test app avoidance overlay on various apps
    - Test busy day exclusion in analytics
    - Test Gemini Nano on supported devices
    - Verify notifications fire correctly
5. **Device testing:** Multiple Android 14+ devices (Pixel, Samsung, etc.)

---

## 7. Decisions & Scope

**Included:**
- Full goal lifecycle (create, track, analyze, archive)
- App avoidance with cooldown overlay
- Health Connect auto-tracking
- On-device AI motivational messages
- Gamification (badges, XP, levels, challenges)
- Google Drive backup/sync
- Material Expressive with dynamic color

**Excluded (for now):**
- Home screen widget
- Social/sharing features
- Web/iOS companion app
- Cloud backend/server
- User accounts beyond Google Drive scope
- Custom themes beyond dynamic color

**Key Decisions:**
- Android 14+ only (for Gemini Nano native support)
- No user accounts — purely local with Drive backup
- Overlay-based cooldown (dismissible, non-aggressive)
- Soft busy-day limit (nudge, not block)
- Dynamic color by default (wallpaper-based)
- Do not create any tests, skip them all for now
