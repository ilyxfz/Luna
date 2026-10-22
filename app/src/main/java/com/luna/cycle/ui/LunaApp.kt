package com.luna.cycle.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.biometric.BiometricManager
import androidx.activity.compose.BackHandler
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.luna.cycle.data.CycleLog
import com.luna.cycle.data.CycleHistory
import com.luna.cycle.data.CalendarSync
import com.luna.cycle.data.analyzeCycleHistory
import com.luna.cycle.data.EncryptedBackup
import com.luna.cycle.data.symptomIntensity
import com.luna.cycle.data.wellbeingCorrelations
import com.luna.cycle.reminders.DailyReminderWorker
import com.luna.cycle.reminders.RetentionWorker
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Muted: androidx.compose.ui.graphics.Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val Mint: androidx.compose.ui.graphics.Color
    @Composable get() = MaterialTheme.colorScheme.secondaryContainer
private val Lilac: androidx.compose.ui.graphics.Color
    @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
private val Symptoms = listOf(
    "Cramps" to "♡", "Bloating" to "◌", "Headache" to "☼", "Acne" to "✳",
    "Fatigue" to "☾", "Tender breasts" to "⌁", "Backache" to "⌇"
)
private val Moods = listOf("Calm", "Happy", "Anxious", "Irritable", "Low energy", "Moody")
private val Flows = listOf("Spotting", "Light", "Medium", "Heavy")
private val ActivityOptions = listOf("Rest", "Walk", "Yoga", "Workout")
private val ProtectionOptions = listOf("Protected", "Unprotected", "Other")
private val MucusOptions = listOf("Dry", "Sticky", "Creamy", "Watery", "Egg white")
private val Rose: androidx.compose.ui.graphics.Color @Composable get() = LocalLunaAccent.current
private val Blush: androidx.compose.ui.graphics.Color @Composable get() = LocalLunaHighlight.current

private enum class Tab(val title: String) {
    HOME("Today"), CALENDAR("Calendar"), INSIGHTS("Insights"), SETTINGS("You")
}

@Composable
fun LunaApp(cycleViewModel: CycleViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val preferences = remember { context.getSharedPreferences("luna_settings", Context.MODE_PRIVATE) }
    var themeName by rememberSaveable { mutableStateOf(preferences.getString("theme_name", "Pastel") ?: "Pastel") }
    var accentIndex by rememberSaveable { mutableIntStateOf(preferences.getInt("accent_index", 0)) }
    var darkMode by rememberSaveable { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
    var appLanguage by rememberSaveable { mutableStateOf(preferences.getString("app_language", "en") ?: "en") }
    var fontScale by rememberSaveable { mutableStateOf(preferences.getFloat("font_scale", 1f)) }
    var boldText by rememberSaveable { mutableStateOf(preferences.getBoolean("bold_text", false)) }
    var reduceMotion by rememberSaveable { mutableStateOf(preferences.getBoolean("reduce_motion", false)) }
    var captureProtection by rememberSaveable { mutableStateOf(preferences.getBoolean("screen_capture_protection", true)) }
    LunaTheme(themeName, accentIndex, darkMode) {
        SideEffect {
            activity?.window?.let { window ->
                if (captureProtection) window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, fontScale),
            LocalLunaLanguage provides appLanguage,
            LocalLayoutDirection provides if (appLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr,
            androidx.compose.material3.LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (boldText) FontWeight.SemiBold else FontWeight.Normal
            )
        ) {
            val logs by cycleViewModel.logs.collectAsState()
        var tab by rememberSaveable { mutableStateOf(Tab.HOME.name) }
        var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
        val today = LocalDate.now()
        var lastPeriodValue by rememberSaveable {
            mutableStateOf(preferences.getString("last_period", null) ?: today.minusDays(8).toString())
        }
        val lastPeriod = LocalDate.parse(lastPeriodValue)
        var customLength by rememberSaveable { mutableIntStateOf(preferences.getInt("custom_cycle_length", 28)) }
        var averagingCycles by rememberSaveable { mutableIntStateOf(preferences.getInt("cycle_average_window", 3)) }
        var useAverage by rememberSaveable { mutableStateOf(preferences.getBoolean("use_cycle_average", false)) }
        val cycleHistory = remember(logs, lastPeriodValue, customLength, averagingCycles, useAverage) {
            analyzeCycleHistory(logs, lastPeriod, if (useAverage) 0 else customLength, averagingCycles)
        }
        val cycleLength = cycleHistory.averageCycleLength?.takeIf { useAverage } ?: customLength
        val cycleStart = cycleHistory.periodStarts.lastOrNull { !it.isAfter(today) } ?: lastPeriod
        val cycleDay = ((ChronoUnit.DAYS.between(cycleStart, today).toInt() % cycleLength) + cycleLength) % cycleLength + 1
        val ovulationDay = (cycleLength - 14).coerceAtLeast(10)
        val phase = when {
            cycleDay <= 5 -> "Menstrual phase"
            cycleDay < ovulationDay -> "Follicular phase"
            cycleDay == ovulationDay -> "Ovulation day"
            else -> "Luteal phase"
        }
        val remaining = cycleLength - cycleDay + 1
        var showLog by remember { mutableStateOf(false) }
        var showDecoy by rememberSaveable { mutableStateOf(false) }
        var showOnboarding by rememberSaveable {
            mutableStateOf(!preferences.getBoolean("onboarding_complete", false))
        }
        BackHandler(showDecoy) { showDecoy = false }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Tab.entries.forEach { item ->
                        val icon = when (item) {
                            Tab.HOME -> Icons.Default.Home
                            Tab.CALENDAR -> Icons.Default.CalendarMonth
                            Tab.INSIGHTS -> Icons.Default.Insights
                            Tab.SETTINGS -> Icons.Default.FavoriteBorder
                        }
                        NavigationBarItem(
                            selected = tab == item.name,
                            onClick = { tab = item.name },
                            icon = { Icon(icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Rose,
                                selectedTextColor = Rose,
                                indicatorColor = Blush
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (tab != Tab.SETTINGS.name) {
                    Button(
                        onClick = {
                            playHaptic(context)
                            selectedDate = today.toString()
                            showLog = true
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose),
                        elevation = ButtonDefaults.buttonElevation(6.dp)
                    ) {
                        Text("＋  Log today", modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                }
            },
        ) { padding ->
            if (showDecoy) {
                DecoyScreen(Modifier.padding(padding), onReturn = { showDecoy = false })
            } else AnimatedContent(
                tab,
                label = "main-tab",
                modifier = Modifier.padding(padding),
                transitionSpec = {
                    if (reduceMotion) androidx.compose.animation.EnterTransition.None togetherWith androidx.compose.animation.ExitTransition.None
                    else androidx.compose.animation.fadeIn() togetherWith androidx.compose.animation.fadeOut()
                }
            ) { page ->
                when (Tab.valueOf(page)) {
                    Tab.HOME -> HomeScreen(
                        cycleDay,
                        phase,
                        remaining,
                        logs.count {
                            val date = LocalDate.parse(it.date)
                            !date.isBefore(cycleStart) && !date.isAfter(today)
                        },
                        onLog = { showLog = true },
                        onPanic = { if (preferences.getBoolean("panic_hide", true)) showDecoy = true },
                        cycleLength = cycleLength,
                        cards = preferences.getString("dashboard_cards", "dial,insight,log,stats") ?: "dial,insight,log,stats"
                    )
                    Tab.CALENDAR -> CalendarScreen(
                        selectedDate = selectedDate,
                        logs = logs,
                        lastPeriod = cycleStart,
                        cycleLength = cycleLength,
                        periodLength = cycleHistory.averagePeriodLength ?: 5,
                        ovulationDay = ovulationDay,
                        onDateSelected = { selectedDate = it; showLog = true }
                    )
                    Tab.INSIGHTS -> InsightsScreen(logs, cycleDay, remaining, cycleHistory)
                                        Tab.SETTINGS -> SettingsScreen(
                        lastPeriod = lastPeriod,
                        logs = logs,
                        history = cycleHistory,
                        viewModel = cycleViewModel,
                        themeName = themeName,
                        accentIndex = accentIndex,
                        darkMode = darkMode,
                        appLanguage = appLanguage,
                        onThemeChanged = { name, accent ->
                            themeName = name
                            accentIndex = accent
                            preferences.edit().putString("theme_name", name).putInt("accent_index", accent).apply()
                        },
                        onDarkModeChanged = { enabled ->
                            darkMode = enabled
                            preferences.edit().putBoolean("dark_mode", enabled).apply()
                        },
                        onLanguageChanged = { language ->
                            appLanguage = language
                            check(preferences.edit().putString("app_language", language).commit()) {
                                "Unable to save the selected app language."
                            }
                            activity?.recreate()
                        },
                        useAverage = useAverage,
                        averagingCycles = averagingCycles,
                        customLength = customLength,
                        onCycleSettingsChanged = { average, window, length ->
                            useAverage = average
                            averagingCycles = window
                            customLength = length
                            preferences.edit()
                                .putBoolean("use_cycle_average", average)
                                .putInt("cycle_average_window", window)
                                .putInt("custom_cycle_length", length)
                                .apply()
                        },
                        fontScale = fontScale,
                        boldText = boldText,
                        reduceMotion = reduceMotion,
                        captureProtection = captureProtection,
                        onCaptureProtectionChanged = {
                            captureProtection = it
                            preferences.edit().putBoolean("screen_capture_protection", it).apply()
                        },
                        onAccessibilityChanged = { scale, bold, reduced ->
                            fontScale = scale
                            boldText = bold
                            reduceMotion = reduced
                            preferences.edit().putFloat("font_scale", scale)
                                .putBoolean("bold_text", bold)
                                .putBoolean("reduce_motion", reduced)
                                .apply()
                        },
                        onLastPeriodChanged = { lastPeriodValue = it.toString() },
                        onRestore = { restoredLogs, restoredPeriod ->
                            cycleViewModel.restore(restoredLogs)
                            restoredPeriod?.let {
                                lastPeriodValue = it.toString()
                                preferences.edit().putString("last_period", it.toString()).apply()
                            }
                        },
                        onDashboardCardsChanged = { preferences.edit().putString("dashboard_cards", it).apply() }
                    )
                }
            }
            }

        if (showOnboarding && !showDecoy) {
            OnboardingDialog(
                onFinish = {
                        preferences.edit().putBoolean("onboarding_complete", true).apply()
                        showOnboarding = false
                }
            )
        }
        if (showLog) {
            LogDialog(
                date = LocalDate.parse(selectedDate),
                existingLog = logs.firstOrNull { it.date == selectedDate },
                onDismiss = { showLog = false },
                onSave = { cycleViewModel.save(it); showLog = false }
            )
        }
    }
    }
}

@Composable
private fun HomeScreen(
    cycleDay: Int,
    phase: String,
    remaining: Int,
    logCount: Int,
    onLog: () -> Unit,
    onPanic: () -> Unit,
    cycleLength: Int,
    cards: String
) {
    val today = LocalDate.now()
    val cardOrder = cards.split(",").filter { it in setOf("dial", "insight", "log", "stats") }
    val tips = when {
        phase.startsWith("Menstrual") -> "Rest when you need to. A warm compress and gentle stretching may help you feel more comfortable."
        phase.startsWith("Follicular") -> "Energy may begin to return. Try a new recipe, a walk, or a movement routine that feels good."
        phase.startsWith("Ovulation") -> "Stay hydrated and choose movement that feels right for you. Cycle predictions are estimates, not contraception."
        else -> "A steady bedtime and nourishing meals can support your wellbeing in the days before your next period."
    }
    val phaseName = when {
        phase.startsWith("Menstrual") -> "Menstrual"
        phase.startsWith("Follicular") -> "Follicular"
        phase.startsWith("Ovulation") -> "Ovulation"
        else -> "Luteal"
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
                )
            )
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures(onDoubleTap = { onPanic() }) }
                    .semantics {
                        customActions = listOf(CustomAccessibilityAction("Quick hide") {
                            onPanic()
                            true
                        })
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), color = Muted, fontSize = 13.sp)
                    Text("Good morning", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
                    Text("A little more in tune with you", color = Muted, fontSize = 12.sp)
                }
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(
                        Brush.linearGradient(listOf(Blush, LocalLunaAccent.current.copy(alpha = 0.24f)))
                    ),
                    contentAlignment = Alignment.Center
                ) { Text("✦", color = Rose, fontSize = 24.sp) }
            }
        }
        items(cardOrder.size) { index ->
            when (cardOrder[index]) {
                "dial" -> CycleRing(cycleDay, phase, remaining, cycleLength)
                "stats" -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStat("Next period", "In $remaining days", "◷", Modifier.weight(1f))
                    MiniStat("Cycle length", "$cycleLength days", "↻", Modifier.weight(1f))
                }
                "insight" -> {
                    SectionTitle("Today's insight", "A little note for you")
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Mint),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                            Text("🌿", fontSize = 23.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("$phaseName phase tip", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(5.dp))
                                Text(tips, color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                            }
                        }
                    }
                }
                "log" -> {
                    SectionTitle("Your day", "$logCount ${if (logCount == 1) "entry" else "entries"} this cycle")
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onLog)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(42.dp).clip(CircleShape).background(Blush), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Rose)
                                }
                                Spacer(Modifier.width(13.dp))
                                Column {
                                    Text("How are you feeling?", fontWeight = FontWeight.Medium)
                                    Text("Add symptoms, mood or a note", color = Muted, fontSize = 12.sp)
                                }
                            }
                            Text("＋", color = Rose, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(58.dp)) }
    }
}

@Composable
private fun DecoyScreen(modifier: Modifier = Modifier, onReturn: () -> Unit) {
    Column(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Daily Notes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onReturn))
        Text("A quiet place for your thoughts.", color = Muted)
        repeat(3) { index ->
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(listOf("A little reminder", "Things to remember", "Today")[index], fontWeight = FontWeight.Medium)
                    Text("Tap the title twice to return.", color = Muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun OnboardingDialog(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val content = listOf(
        "A softer way to track" to "Log your cycle, symptoms, mood and daily wellbeing at your own pace.",
        "Private by design" to "Your journal is encrypted on this device. Optional app lock and private reminders are in Settings.",
        "Your cycle, your way" to "Explore your calendar, patterns and reminders. Predictions are estimates and never a substitute for medical advice."
    )
    AlertDialog(
        onDismissRequest = onFinish,
        title = { Text(content[page].first) },
        text = { Text(content[page].second, color = Muted) },
        confirmButton = {
            TextButton(onClick = { if (page == content.lastIndex) onFinish() else page++ }) {
                Text(if (page == content.lastIndex) "Get started" else "Continue", color = Rose)
            }
        },
        dismissButton = { TextButton(onClick = onFinish) { Text("Skip") } }
    )
}

@Composable
private fun CycleRing(cycleDay: Int, phase: String, remaining: Int, cycleLength: Int) {
    val progress by animateFloatAsState(cycleDay / cycleLength.toFloat(), label = "cycle-progress")
    val heroStart = LocalLunaHeroStart.current
    val heroEnd = LocalLunaHeroEnd.current
    Card(
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(34.dp))
    ) {
        Box(
            Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(heroStart, heroEnd)))
        ) {
            Canvas(Modifier.fillMaxWidth().height(360.dp)) {
                drawCircle(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.055f),
                    radius = size.width * 0.36f,
                    center = Offset(size.width * 0.94f, size.height * 0.05f)
                )
                drawCircle(
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.045f),
                    radius = size.width * 0.24f,
                    center = Offset(size.width * 0.02f, size.height * 0.97f)
                )
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("YOUR CYCLE", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
                    Box(
                        Modifier.clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.14f))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(phase, color = androidx.compose.ui.graphics.Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Box(Modifier.size(224.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        val diameter = size.minDimension - stroke
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        drawArc(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.19f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = androidx.compose.ui.graphics.Color(0xFFFFD5B9),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        drawCircle(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.13f),
                            radius = 3.dp.toPx(),
                            center = Offset(size.width / 2f, stroke / 2f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DAY", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 2.sp)
                        Text("$cycleDay", color = androidx.compose.ui.graphics.Color.White, fontSize = 76.sp, fontWeight = FontWeight.Light, lineHeight = 80.sp)
                        Text("of $cycleLength", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                    }
                    Text("✦", color = androidx.compose.ui.graphics.Color(0xFFFFD5B9), fontSize = 18.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 2.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (cycleDay <= 5) "Your period is expected in about $remaining days"
                    else "$remaining days until your next period",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.11f))
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✧  CYCLE PROGRESS", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.74f), fontSize = 10.sp, letterSpacing = 1.sp)
                    Text("${(progress * 100).toInt()}%", color = androidx.compose.ui.graphics.Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MiniStat(title: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Lilac)
    ) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            Text(icon, color = Rose, fontSize = 18.sp)
            Spacer(Modifier.height(9.dp))
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(title, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun CalendarScreen(
    selectedDate: String,
    logs: List<CycleLog>,
    lastPeriod: LocalDate,
    cycleLength: Int,
    periodLength: Int,
    ovulationDay: Int,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val language = LocalLunaLanguage.current
    var month by remember { mutableStateOf(YearMonth.now()) }
    val firstDay = month.atDay(1)
    val weekStartsOn = when (language) {
        "fr" -> 1
        "ar" -> 6
        else -> 0
    }
    val leading = ((firstDay.dayOfWeek.value % 7) - weekStartsOn + 7) % 7
    val days = month.lengthOfMonth()
    val loggedDates = remember(logs) { logs.map { it.date }.toSet() }
    val weekdayHeaders = when (language) {
        "fr" -> listOf("D", "L", "M", "M", "J", "V", "S").drop(weekStartsOn) + listOf("D", "L", "M", "M", "J", "V", "S").take(weekStartsOn)
        "ar" -> listOf("ا", "ن", "ث", "ر", "خ", "ج", "س").drop(weekStartsOn) + listOf("ا", "ن", "ث", "ر", "خ", "ج", "س").take(weekStartsOn)
        else -> listOf("S", "M", "T", "W", "T", "F", "S")
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            )
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Your calendar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("A gentle view of your cycle", color = Muted, fontSize = 13.sp)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("‹", Modifier.clickable { month = month.minusMonths(1) }.padding(8.dp), color = Rose, fontSize = 26.sp)
                        Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                        Text("›", Modifier.clickable { month = month.plusMonths(1) }.padding(8.dp), color = Rose, fontSize = 26.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        weekdayHeaders.forEach {
                            Text(it, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center, color = Muted, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val totalCells = ((leading + days + 6) / 7) * 7
                    (0 until totalCells step 7).forEach { start ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            (start until start + 7).forEach { cell ->
                                val day = cell - leading + 1
                                if (day in 1..days) {
                                    val date = month.atDay(day)
                                    val cycleDay = ((ChronoUnit.DAYS.between(lastPeriod, date).toInt() % cycleLength) + cycleLength) % cycleLength + 1
                                    val periodDay = cycleDay <= periodLength
                                    val fertileDay = cycleDay in (ovulationDay - 5)..(ovulationDay + 1)
                                    val selected = selectedDate == date.toString()
                                    val bg = when {
                                        selected -> Rose
                                        periodDay -> Blush
                                        fertileDay -> Mint
                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                    }
                                    Column(
                                        Modifier.size(38.dp).clip(CircleShape).background(bg)
                                            .clickable {
                                                playHaptic(context)
                                                onDateSelected(date.toString())
                                            }
                                            .semantics {
                                                contentDescription = buildString {
                                                    append(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")))
                                                    when {
                                                        periodDay -> append(", predicted period")
                                                        fertileDay -> append(", estimated fertile window")
                                                    }
                                                    if (date.toString() in loggedDates) append(", logged")
                                                }
                                                role = Role.Button
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("$day", color = if (selected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        if (date.toString() in loggedDates) {
                                            Box(Modifier.padding(top = 1.dp).size(3.dp).clip(CircleShape).background(if (selected) androidx.compose.ui.graphics.Color.White else Rose))
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.size(38.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                    }
                    Spacer(Modifier.height(13.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        LegendDot("Period", Blush)
                        LegendDot("Fertile", Mint)
                        LegendDot("Logged", Rose)
                    }
                }
            }
        }
        item {
            SectionTitle("Cycle overview", "Estimated")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStat("Cycle length", "$cycleLength days", "↻", Modifier.weight(1f))
                MiniStat("Period length", "$periodLength days", "◷", Modifier.weight(1f))
            }
        }
        item {
            SectionTitle("Logged days", "${logs.size} total")
            if (logs.isEmpty()) {
                Text("Your daily check-ins will appear here.", color = Muted, fontSize = 13.sp)
            } else {
                logs.take(5).forEach { log ->
                    Text(
                        "${LocalDate.parse(log.date).format(DateTimeFormatter.ofPattern("MMM d"))}  ·  " +
                            listOfNotNull(log.flow.takeIf(String::isNotBlank), log.mood.takeIf(String::isNotBlank)).joinToString(" · ")
                                .ifBlank { "Daily check-in" },
                        Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onDateSelected(log.date) },
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun InsightsScreen(logs: List<CycleLog>, cycleDay: Int, remaining: Int, history: CycleHistory) {
    val context = LocalContext.current
    val symptomCounts = remember(logs) {
        logs.flatMap { it.symptomSet() }.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.take(4)
    }
    val correlations = remember(logs) { wellbeingCorrelations(logs) }
    val metrics = remember(history) {
        CycleMetrics(history.averageCycleLength, history.averagePeriodLength, history.regularityScore)
    }
    val missedCycle = history.periodStarts.lastOrNull()?.let {
        ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() > (history.averageCycleLength ?: 28) + 7
    } == true
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            )
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        item {
            Text("Your insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("Getting to know your rhythm", color = Muted, fontSize = 13.sp)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Lilac)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("CYCLE REGULARITY", color = Muted, fontSize = 11.sp, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(metrics.regularity?.toString() ?: "—", fontSize = 44.sp, fontWeight = FontWeight.Light)
                        Text(
                            if (metrics.regularity != null) " / 100" else "  Log period days across cycles to see your score",
                            color = Muted,
                            modifier = Modifier.padding(bottom = 9.dp),
                            fontSize = 12.sp
                        )
                    }
                    Text("Based on the start dates you have logged", color = Muted, fontSize = 12.sp)
                }
            }
        }
        item {
            SectionTitle("Cycle at a glance", "This cycle")
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricRow(
                        "Average cycle length",
                        metrics.averageCycleLength?.let { "$it days" } ?: "Not enough data",
                        (metrics.averageCycleLength ?: 0).toFloat() / 35f
                    )
                    MetricRow(
                        "Period duration",
                        metrics.averagePeriodLength?.let { "$it days" } ?: "Not enough data",
                        (metrics.averagePeriodLength ?: 0).toFloat() / 10f
                    )
                    MetricRow("Current cycle", "Day $cycleDay", cycleDay / 35f)
                    MetricRow("Next period", "$remaining days", remaining / 35f)
                }
            }
        }
        if (history.irregularCycleLengths.isNotEmpty() || missedCycle) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Your cycle pattern may be changing", fontWeight = FontWeight.SemiBold)
                        Text(
                            buildString {
                                if (history.irregularCycleLengths.isNotEmpty()) append("Some logged cycles were shorter than 21 days or longer than 35 days. ")
                                if (missedCycle) append("Your next period is later than the estimate. ")
                                append("Cycle variation can be common. Consider discussing persistent changes with a healthcare professional.")
                            },
                            color = Muted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
        item {
            SectionTitle("Cycle comparison", "Symptom intensity by cycle day")
            CycleComparisonChart(logs, history)
        }
        item {
            SectionTitle("Symptom patterns", "From your check-ins")
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    if (symptomCounts.isEmpty()) {
                        Text("Log a few days to discover patterns in how you feel.", color = Muted, fontSize = 13.sp)
                    } else {
                        symptomCounts.forEach { (symptom, count) ->
                            MetricRow(symptom, "$count ${if (count == 1) "day" else "days"}", (count / (logs.size.coerceAtLeast(1).toFloat())).coerceIn(0.05f, 1f))
                        }
                    }
                }
            }
        }
        item {
            SectionTitle("Wellbeing patterns", "Local estimates from your logs")
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (correlations.isEmpty()) {
                        Text("Log water, sleep and symptoms on at least 8 days to explore possible associations.", color = Muted, fontSize = 13.sp)
                    } else {
                        correlations.forEach { pattern ->
                            Text(
                                "${pattern.symptom} appeared on ${pattern.affectedGroupRate}% of ${pattern.factor.lowercase()} days versus ${pattern.comparisonGroupRate}% on comparison days (${pattern.observedDays} and ${pattern.comparisonDays} days).",
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                        Text("These are descriptive associations, not evidence that one factor causes another.", color = Muted, fontSize = 10.sp)
                    }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { ExportReport.export(context, logs) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Export health summary") }
        }
        item { Spacer(Modifier.height(54.dp)) }
    }
}

@Composable
private fun CycleComparisonChart(logs: List<CycleLog>, history: CycleHistory) {
    val accentColor = Rose
    val highlightColor = Blush
    val mutedColor = Muted
    val length = history.averageCycleLength ?: history.cycleLengths.lastOrNull() ?: 28
    val starts = history.periodStarts
    val cycleSeries = starts.mapIndexed { index, start ->
        val end = starts.getOrNull(index + 1) ?: LocalDate.now().plusDays(1)
        val values = logs.filter {
            val date = LocalDate.parse(it.date)
            !date.isBefore(start) && date.isBefore(end)
        }.groupBy { ChronoUnit.DAYS.between(start, LocalDate.parse(it.date)).toInt() + 1 }
        (1..length).map { day -> values[day]?.map { symptomIntensity(it).toFloat() }?.average()?.toFloat() }
    }
    val current = cycleSeries.lastOrNull()
    val previous = if (cycleSeries.size > 1) {
        (0 until length).map { day ->
            cycleSeries.dropLast(1).mapNotNull { it.getOrNull(day) }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
        }
    } else null
    val hasCurrentData = current?.any { it != null } == true
    val hasPreviousData = previous?.any { it != null } == true
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            if (!hasCurrentData && !hasPreviousData) {
                Text("Log flow or symptoms to compare cycles over time.", color = Muted, fontSize = 12.sp)
            } else {
                androidx.compose.foundation.Canvas(
                    Modifier.fillMaxWidth().height(155.dp).semantics {
                        role = Role.Image
                        contentDescription = "Chart comparing daily symptom intensity for the latest cycle and previous cycles"
                    }
                ) {
                    val left = 10.dp.toPx()
                    val top = 10.dp.toPx()
                    val width = size.width - left * 2
                    val height = size.height - top * 2
                    repeat(4) { line ->
                        val y = top + height * line / 3f
                        drawLine(
                            color = highlightColor.copy(alpha = 0.7f),
                            start = Offset(left, y),
                            end = Offset(left + width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    fun drawSeries(points: List<Float?>?, color: androidx.compose.ui.graphics.Color) {
                        if (points == null || points.size < 2) return
                        for (index in 0 until points.lastIndex) {
                            val first = points[index] ?: continue
                            val second = points[index + 1] ?: continue
                            val x1 = left + width * index / (points.size - 1f)
                            val x2 = left + width * (index + 1) / (points.size - 1f)
                            val y1 = top + height * (1f - first.coerceIn(0f, 10f) / 10f)
                            val y2 = top + height * (1f - second.coerceIn(0f, 10f) / 10f)
                            drawLine(color.copy(alpha = 0.62f), Offset(x1, y1), Offset(x2, y2), 2.dp.toPx(), cap = StrokeCap.Round)
                        }
                    }
                    drawSeries(previous, mutedColor)
                    drawSeries(current, accentColor)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (hasCurrentData) LegendDot("Current cycle", Rose)
                    if (hasPreviousData) LegendDot("Previous-cycle average", Muted)
                }
                if (!hasCurrentData) Text("No symptoms logged in the current cycle yet.", color = Muted, fontSize = 10.sp)
                Text("Intensity combines reported symptoms and flow. Empty days are not treated as zero.", color = Muted, fontSize = 10.sp)
            }
        }
    }
}

private data class CycleMetrics(
    val averageCycleLength: Int?,
    val averagePeriodLength: Int?,
    val regularity: Int?
)

@Composable
private fun MetricRow(label: String, value: String, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp)
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = Rose,
            trackColor = Blush
        )
    }
}

@Composable
private fun SettingsScreen(
    lastPeriod: LocalDate,
    logs: List<CycleLog>,
    history: com.luna.cycle.data.CycleHistory,
    viewModel: CycleViewModel,
    themeName: String,
    accentIndex: Int,
    darkMode: Boolean,
    appLanguage: String,
    onThemeChanged: (String, Int) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    useAverage: Boolean,
    averagingCycles: Int,
    customLength: Int,
    onCycleSettingsChanged: (Boolean, Int, Int) -> Unit,
    fontScale: Float,
    boldText: Boolean,
    reduceMotion: Boolean,
    captureProtection: Boolean,
    onCaptureProtectionChanged: (Boolean) -> Unit,
    onAccessibilityChanged: (Float, Boolean, Boolean) -> Unit,
    onLastPeriodChanged: (LocalDate) -> Unit,
    onRestore: (List<CycleLog>, LocalDate?) -> Unit,
    onDashboardCardsChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferences = remember { context.getSharedPreferences("luna_settings", Context.MODE_PRIVATE) }
    val currentPeriodStart = history.periodStarts.lastOrNull { !it.isAfter(LocalDate.now()) } ?: lastPeriod
    var biometric by remember { mutableStateOf(preferences.getBoolean("biometric_lock", false)) }
    var reminders by remember { mutableStateOf(preferences.getBoolean("daily_reminder", false)) }
    var cycleAlerts by remember { mutableStateOf(preferences.getBoolean("cycle_alerts", false)) }
    var medicationReminder by remember { mutableStateOf(preferences.getBoolean("medication_reminder", false)) }
    var periodDate by remember { mutableStateOf(lastPeriod) }
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var backupPassphrase by remember { mutableStateOf("") }
    var restorePassphrase by remember { mutableStateOf("") }
    var panicHide by remember { mutableStateOf(preferences.getBoolean("panic_hide", true)) }
    var haptics by remember { mutableStateOf(preferences.getBoolean("haptics_enabled", true)) }
    var hapticStrength by remember { mutableIntStateOf(preferences.getInt("haptic_strength", 80)) }
    var discreet by remember { mutableStateOf(preferences.getBoolean("discreet_notifications", true)) }
    var calendarSync by remember { mutableStateOf(preferences.getBoolean("calendar_sync", false)) }
    var notificationTitle by remember { mutableStateOf(preferences.getString("notification_title", "Daily Check-in") ?: "") }
    var notificationMessage by remember { mutableStateOf(preferences.getString("notification_message", "Take a moment for yourself.") ?: "") }
    var alertTime by remember { mutableStateOf(preferences.getString("alert_time", "20:00") ?: "20:00") }
    var quietStart by remember { mutableStateOf(preferences.getString("quiet_start", "22:00") ?: "22:00") }
    var quietEnd by remember { mutableStateOf(preferences.getString("quiet_end", "08:00") ?: "08:00") }
    var fontScale by rememberSaveable { mutableStateOf(preferences.getFloat("font_scale", 1f)) }
    var boldText by remember { mutableStateOf(preferences.getBoolean("bold_text", false)) }
    var reduceMotion by remember { mutableStateOf(preferences.getBoolean("reduce_motion", false)) }
    var retentionYears by remember { mutableIntStateOf(preferences.getInt("retention_years", 0)) }
    var dashboardCards by remember { mutableStateOf(preferences.getString("dashboard_cards", "dial,insight,log,stats") ?: "") }
    var showRotateDialog by remember { mutableStateOf(false) }
    var launcherProfile by remember { mutableStateOf(preferences.getString("launcher_profile", "Luna") ?: "Luna") }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        EncryptedBackup.write(context, uri, logs, periodDate.toString(), backupPassphrase)
                    }
                    showLunaToast(context, "Encrypted backup created.")
                } catch (error: Exception) {
                    showLunaToast(context, "Backup failed: ${error.localizedMessage ?: "Unable to write file."}")
                } finally {
                    backupPassphrase = ""
                }
            }
        } else {
            backupPassphrase = ""
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        restoreUri = uri
        if (uri != null) restorePassphrase = ""
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            reminders = true
            preferences.edit().putBoolean("daily_reminder", true).apply()
            scheduleReminder(context, discreet)
        }
    }
    val cycleNotificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cycleAlerts = true
            preferences.edit().putBoolean("cycle_alerts", true).apply()
            scheduleReminder(context, discreet)
        }
    }
    val medicationNotificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            medicationReminder = true
            preferences.edit().putBoolean("medication_reminder", true).apply()
            scheduleReminder(context, discreet)
        }
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            calendarSync = true
            preferences.edit().putBoolean("calendar_sync", true).apply()
            try {
                val count = CalendarSync.sync(context, currentPeriodStart, history.averageCycleLength ?: customLength, discreet)
                showLunaToast(context, "Added $count cycle dates to your calendar.")
            } catch (error: Exception) {
                calendarSync = false
                preferences.edit().putBoolean("calendar_sync", false).apply()
                showLunaToast(context, "Calendar sync failed: ${error.localizedMessage ?: "Unable to sync."}")
            }
        } else {
            showLunaToast(context, "Calendar access is needed to add cycle dates.")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            )
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(22.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Text("A little about you", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("Your settings, always private", color = Muted, fontSize = 13.sp)
        }
        item {
            NotificationControlsCard(
                title = notificationTitle,
                message = notificationMessage,
                alertTime = alertTime,
                quietStart = quietStart,
                quietEnd = quietEnd,
                onTitle = {
                    val value = it.take(48)
                    notificationTitle = value
                    preferences.edit().putString("notification_title", value).apply()
                },
                onMessage = {
                    val value = it.take(120)
                    notificationMessage = value
                    preferences.edit().putString("notification_message", value).apply()
                },
                onAlertTime = {
                    alertTime = it
                    if (isValidClock(it)) {
                        preferences.edit().putString("alert_time", it).apply()
                        if (reminders || cycleAlerts || medicationReminder) scheduleReminder(context, discreet)
                    }
                },
                onQuietHours = { start, end ->
                    quietStart = start
                    quietEnd = end
                    if (isValidClock(start) && isValidClock(end)) {
                        preferences.edit().putString("quiet_start", start).putString("quiet_end", end).apply()
                    }
                }
            )
        }
        item {
            CycleAndDashboardSettings(
                themeName = themeName,
                accentIndex = accentIndex,
                darkMode = darkMode,
                appLanguage = appLanguage,
                onThemeChanged = onThemeChanged,
                onDarkModeChanged = onDarkModeChanged,
                onLanguageChanged = onLanguageChanged,
                useAverage = useAverage,
                averagingCycles = averagingCycles,
                customLength = customLength,
                onCycleSettingsChanged = onCycleSettingsChanged,
                cards = dashboardCards,
                onCardsChanged = {
                    dashboardCards = it
                    onDashboardCardsChanged(it)
                },
                calendarSync = calendarSync,
                onCalendarEnabled = { enabled ->
                    if (!enabled) {
                        calendarSync = false
                        preferences.edit().putBoolean("calendar_sync", false).apply()
                    } else if (hasCalendarPermissions(context)) {
                        calendarSync = true
                        preferences.edit().putBoolean("calendar_sync", true).apply()
                        syncPredictedCalendar(context, currentPeriodStart, history.averageCycleLength ?: customLength, discreet)
                    } else {
                        calendarPermissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                        )
                    }
                },
                onCalendarSync = {
                    if (calendarSync) {
                        syncPredictedCalendar(context, currentPeriodStart, history.averageCycleLength ?: customLength, discreet)
                    }
                }
            )
        }
        item {
            PrivacyAndSystemSettings(
                context = context,
                panicHide = panicHide,
                onPanicHide = {
                    panicHide = it
                    preferences.edit().putBoolean("panic_hide", it).apply()
                },
                haptics = haptics,
                onHaptics = {
                    haptics = it
                    preferences.edit().putBoolean("haptics_enabled", it).apply()
                },
                hapticStrength = hapticStrength,
                onHapticStrength = {
                    hapticStrength = it
                    preferences.edit().putInt("haptic_strength", it).apply()
                },
                captureProtection = captureProtection,
                onCaptureProtection = {
                    onCaptureProtectionChanged(it)
                },
                discreetNotifications = discreet,
                onDiscreetNotifications = { enabled ->
                    discreet = enabled
                    preferences.edit().putBoolean("discreet_notifications", enabled).apply()
                    if (reminders || cycleAlerts || medicationReminder) scheduleReminder(context, enabled)
                },
                launcherProfile = launcherProfile,
                onLauncherProfile = {
                    setLauncherProfile(context, it)
                    launcherProfile = it
                    preferences.edit().putString("launcher_profile", it).apply()
                }
            )
        }
        item {
            AccessibilityAndRetentionSettings(
                fontScale = fontScale,
                boldText = boldText,
                reduceMotion = reduceMotion,
                retentionYears = retentionYears,
                onAccessibilityChanged = onAccessibilityChanged,
                onRetentionChanged = { years ->
                    retentionYears = years
                    preferences.edit().putInt("retention_years", years).apply()
                    if (years == 0) {
                        WorkManager.getInstance(context).cancelUniqueWork("luna-data-retention")
                    } else {
                        viewModel.purgeBefore(LocalDate.now().minusYears(years.toLong()).toString())
                        scheduleRetention(context)
                    }
                }
            )
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(horizontal = 18.dp)) {
                    SettingRow("Rotate database encryption key", "Re-encrypt your local journal with a new key", "⟳") {
                        showRotateDialog = true
                    }
                    SettingDivider()
                    SettingRow("Encrypted device-to-device transfer", "Use the passphrase-protected backup above", "⇧") {
                        showBackupDialog = true
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(horizontal = 18.dp)) {
                    SettingRow("Last period started", periodDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), "◷") { showPeriodPicker = true }
                    SettingDivider()
                    SettingSwitch("Daily check-in reminder", "A gentle nudge each day", "♧", reminders) { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= 33) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            reminders = enabled
                            preferences.edit().putBoolean("daily_reminder", enabled).apply()
                            updateReminderSchedule(context, enabled || cycleAlerts || medicationReminder, discreet)
                        }
                    }
                    SettingDivider()
                    SettingSwitch("Cycle & fertile window alerts", "A quiet heads-up before your period", "◌", cycleAlerts) { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= 33) {
                            cycleNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            cycleAlerts = enabled
                            preferences.edit().putBoolean("cycle_alerts", enabled).apply()
                            updateReminderSchedule(context, reminders || enabled || medicationReminder, discreet)
                        }
                    }
                    SettingDivider()
                    SettingSwitch("Daily medication reminder", "A private nudge for your pill", "＋", medicationReminder) { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= 33) {
                            medicationNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            medicationReminder = enabled
                            preferences.edit().putBoolean("medication_reminder", enabled).apply()
                            updateReminderSchedule(context, reminders || cycleAlerts || enabled, discreet)
                        }
                    }
                    SettingDivider()
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(horizontal = 18.dp)) {
                    SettingSwitch("Biometric lock", "Unlock with your fingerprint", "⌑", biometric) { enabled ->
                        val available = BiometricManager.from(context)
                            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                        if (!enabled || available == BiometricManager.BIOMETRIC_SUCCESS) {
                            biometric = enabled
                            preferences.edit().putBoolean("biometric_lock", enabled).apply()
                        } else {
                            showLunaToast(context, "Set up a fingerprint or face unlock first.")
                        }
                    }
                    SettingDivider()
                    SettingRow("Private by design", "Encrypted on this device", "♧") { }
                    SettingDivider()
                    SettingRow("Create encrypted backup", "Save a private, portable copy", "↓") { showBackupDialog = true }
                    SettingDivider()
                    SettingRow("Restore encrypted backup", "Merge saved check-ins", "↑") {
                        restoreLauncher.launch(arrayOf("application/octet-stream"))
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Blush.copy(alpha = 0.5f))) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Rose, modifier = Modifier.padding(end = 12.dp))
                    Column {
                        Text("Your body, your data", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(5.dp))
                        Text("Your journal is stored on this device in an encrypted database. Luna never sells or shares your health data.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
        item {
            Text("Luna is for cycle awareness and is not a contraceptive or a substitute for medical advice.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        }
        item { Spacer(Modifier.height(50.dp)) }
    }

    if (showPeriodPicker) {
        DatePickerDialog(
            initialDate = periodDate,
            onDismiss = { showPeriodPicker = false },
            onConfirm = {
                periodDate = it
                preferences.edit().putString("last_period", it.toString()).apply()
                onLastPeriodChanged(it)
                showPeriodPicker = false
            }
        )
    }
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false; backupPassphrase = "" },
            title = { Text("Create encrypted backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose a passphrase with at least 8 characters. You’ll need it to restore this backup.", color = Muted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = backupPassphrase,
                        onValueChange = { backupPassphrase = it },
                        label = { Text("Backup passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (backupPassphrase.length < 8) {
                        showLunaToast(context, "Use at least 8 characters for the passphrase.")
                    } else {
                        showBackupDialog = false
                        backupLauncher.launch("luna-private-backup.lunabackup")
                    }
                }) { Text("Choose file", color = Rose) }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false; backupPassphrase = "" }) { Text("Cancel") }
            }
        )
    }
    if (restoreUri != null) {
        AlertDialog(
            onDismissRequest = { restoreUri = null; restorePassphrase = "" },
            title = { Text("Restore encrypted backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the passphrase used when this backup was created. Matching dates will be replaced.", color = Muted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = restorePassphrase,
                        onValueChange = { restorePassphrase = it },
                        label = { Text("Backup passphrase") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = restoreUri ?: return@TextButton
                    scope.launch {
                        try {
                            val backup = withContext(Dispatchers.IO) {
                                EncryptedBackup.read(context, uri, restorePassphrase)
                            }
                            val period = backup.lastPeriod?.let(LocalDate::parse)
                            onRestore(backup.logs, period)
                            showLunaToast(context, "Restored ${backup.logs.size} check-ins.")
                            restoreUri = null
                            restorePassphrase = ""
                        } catch (error: Exception) {
                            showLunaToast(context, "Restore failed: ${error.localizedMessage ?: "Check the backup and passphrase."}")
                        }
                    }
                }) { Text("Restore", color = Rose) }
            },
            dismissButton = {
                TextButton(onClick = { restoreUri = null; restorePassphrase = "" }) { Text("Cancel") }
            }
        )
    }
    if (showRotateDialog) {
        AlertDialog(
            onDismissRequest = { showRotateDialog = false },
            title = { Text("Rotate encryption key?") },
            text = {
                Text(
                    "Luna will re-encrypt the local database with a new device-protected key. Your logs remain in place. Create an encrypted backup first as a precaution.",
                    color = Muted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRotateDialog = false
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) { viewModel.rotateEncryptionKey(context) }
                            showLunaToast(context, "Database encryption key rotated.")
                        } catch (error: Exception) {
                            showLunaToast(context, "Key rotation failed: ${error.localizedMessage ?: "The existing key was retained."}")
                        }
                    }
                }) { Text("Rotate key", color = Rose) }
            },
            dismissButton = { TextButton(onClick = { showRotateDialog = false }) { Text("Cancel") } }
        )
    }
}

private fun scheduleReminder(context: Context, discreet: Boolean) {
    val preferences = context.getSharedPreferences("luna_settings", Context.MODE_PRIVATE)
    val alertTime = java.time.LocalTime.parse(preferences.getString("alert_time", "20:00") ?: "20:00")
    val now = java.time.LocalDateTime.now()
    val nextAlert = now.toLocalDate().atTime(alertTime).let { if (it.isAfter(now)) it else it.plusDays(1) }
    val initialDelay = java.time.Duration.between(now, nextAlert).toMillis().coerceAtLeast(0)
    val work = OneTimeWorkRequestBuilder<DailyReminderWorker>()
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .addTag(if (discreet) "discreet" else "normal")
        .build()
    WorkManager.getInstance(context).cancelUniqueWork("luna-daily-reminder")
    WorkManager.getInstance(context).enqueueUniqueWork(
        "luna-daily-reminder",
        ExistingWorkPolicy.REPLACE,
        work
    )
}

private fun updateReminderSchedule(context: Context, enabled: Boolean, discreet: Boolean) {
    if (enabled) scheduleReminder(context, discreet)
    else WorkManager.getInstance(context).cancelUniqueWork("luna-daily-reminder")
}

@Composable
private fun SettingRow(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, color = Rose, fontSize = 20.sp, modifier = Modifier.width(34.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
        Text("›", color = Muted, fontSize = 22.sp)
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, icon: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, color = Rose, fontSize = 20.sp, modifier = Modifier.width(34.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = {
            playHaptic(context)
            onCheckedChange(it)
        })
    }
}

@Composable
private fun SettingDivider() {
    Spacer(Modifier.fillMaxWidth().height(1.dp).background(Blush.copy(alpha = 0.7f)))
}

@Composable
private fun NotificationControlsCard(
    title: String,
    message: String,
    alertTime: String,
    quietStart: String,
    quietEnd: String,
    onTitle: (String) -> Unit,
    onMessage: (String) -> Unit,
    onAlertTime: (String) -> Unit,
    onQuietHours: (String, String) -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Notification privacy", "Text and timing")
            Text("Custom text replaces cycle-specific details on notifications.", color = Muted, fontSize = 12.sp)
            OutlinedTextField(title, onTitle, label = { Text("Notification title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(message, onMessage, label = { Text("Notification text") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            ClockField("Alert time (24-hour, HH:mm)", alertTime, onAlertTime)
            Text("Quiet hours", fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ClockField("Start", quietStart) { onQuietHours(it, quietEnd) }
                ClockField("End", quietEnd) { onQuietHours(quietStart, it) }
            }
        }
    }
}

@Composable
private fun ClockField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { updated ->
            if (updated.length <= 5 && updated.all { it.isDigit() || it == ':' }) onValueChange(updated)
        },
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        modifier = Modifier.width(160.dp)
    )
}

private fun isValidClock(value: String): Boolean =
    runCatching { java.time.LocalTime.parse(value) }.isSuccess

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CycleAndDashboardSettings(
    themeName: String,
    accentIndex: Int,
    darkMode: Boolean,
    appLanguage: String,
    onThemeChanged: (String, Int) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    useAverage: Boolean,
    averagingCycles: Int,
    customLength: Int,
    onCycleSettingsChanged: (Boolean, Int, Int) -> Unit,
    cards: String,
    onCardsChanged: (String) -> Unit,
    calendarSync: Boolean,
    onCalendarEnabled: (Boolean) -> Unit,
    onCalendarSync: () -> Unit
) {
    val context = LocalContext.current
    val themeNames = listOf("Pastel", "Warm earth", "High contrast", "Monochrome")
    val accentNames = listOf("Rose", "Terracotta", "Violet", "Sage", "Blue", "Graphite")
    val labels = mapOf("dial" to "Cycle dial", "insight" to "Daily tip", "log" to "Daily log", "stats" to "Cycle stats")
    val order = cards.split(",").filter { it in labels }.distinct().toMutableList()
    listOf("dial", "insight", "log", "stats").filter { it !in order }.forEach(order::add)
    val enabledOrder = order.filter { it in cards.split(",") }

    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Your look", "Theme and accent")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                themeNames.forEach { name -> ChoiceChip(name, themeName == name) { onThemeChanged(name, accentIndex) } }
            }
            Text("Accent color", color = Muted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Dark mode", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                Switch(checked = darkMode, onCheckedChange = onDarkModeChanged)
            }
            Text("Language", color = Muted, fontSize = 12.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("English" to "en", "French" to "fr", "Arabic" to "ar").forEach { (label, code) ->
                    ChoiceChip(label, appLanguage == code) { onLanguageChanged(code) }
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                accentNames.forEachIndexed { index, name ->
                    ChoiceChip(name, accentIndex == index) { onThemeChanged(themeName, index) }
                }
            }
            SettingDivider()
            SectionTitle("Cycle estimates", "For awareness only")
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ChoiceChip("Custom", !useAverage) {
                    onCycleSettingsChanged(false, averagingCycles, customLength)
                }
                ChoiceChip("Moving average", useAverage) {
                    onCycleSettingsChanged(true, averagingCycles, customLength)
                }
            }
            if (useAverage) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ChoiceChip("Last 3 cycles", averagingCycles == 3) { onCycleSettingsChanged(true, 3, customLength) }
                    ChoiceChip("Last 6 cycles", averagingCycles == 6) { onCycleSettingsChanged(true, 6, customLength) }
                }
                Text("Average from logged period starts: ${if (averagingCycles == 3) "3" else "6"} cycles (as available).", color = Muted, fontSize = 11.sp)
            } else {
                Text("Custom cycle length: $customLength days", color = Muted, fontSize = 12.sp)
                Slider(
                    value = customLength.toFloat(),
                    onValueChange = { onCycleSettingsChanged(false, averagingCycles, it.roundToInt()) },
                    onValueChangeFinished = { playHaptic(context) },
                    valueRange = 21f..45f,
                    steps = 23
                )
            }
            SettingDivider()
            SectionTitle("Home screen", "Show and reorder cards")
            order.forEachIndexed { index, card ->
                val enabledIndex = enabledOrder.indexOf(card)
                val enabled = card in cards.split(",")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(labels.getValue(card), Modifier.weight(1f), fontSize = 13.sp)
                    TextButton(enabled = enabled && enabledIndex > 0, onClick = {
                        val reordered = enabledOrder.toMutableList().apply { add(enabledIndex - 1, removeAt(enabledIndex)) }
                        onCardsChanged(reordered.joinToString(","))
                    }) { Text("↑") }
                    TextButton(enabled = enabled && enabledIndex < enabledOrder.lastIndex, onClick = {
                        val reordered = enabledOrder.toMutableList().apply { add(enabledIndex + 1, removeAt(enabledIndex)) }
                        onCardsChanged(reordered.joinToString(","))
                    }) { Text("↓") }
                    Switch(checked = enabled, onCheckedChange = { isEnabled ->
                        val updated = cards.split(",").filter { it in labels }.toMutableList()
                        if (isEnabled && card !in updated) updated.add(card)
                        if (!isEnabled) updated.remove(card)
                        onCardsChanged(updated.joinToString(","))
                    })
                }
            }
            SettingDivider()
            SettingSwitch("Calendar predictions", "Opt in to add dates to Android Calendar", "▦", calendarSync, onCalendarEnabled)
            if (calendarSync) {
                OutlinedButton(onClick = onCalendarSync, modifier = Modifier.fillMaxWidth()) {
                    Text("Sync next 3 predicted cycles")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrivacyAndSystemSettings(
    context: Context,
    panicHide: Boolean,
    onPanicHide: (Boolean) -> Unit,
    haptics: Boolean,
    onHaptics: (Boolean) -> Unit,
    hapticStrength: Int,
    onHapticStrength: (Int) -> Unit,
    captureProtection: Boolean,
    onCaptureProtection: (Boolean) -> Unit,
    discreetNotifications: Boolean,
    onDiscreetNotifications: (Boolean) -> Unit,
    launcherProfile: String,
    onLauncherProfile: (String) -> Unit
) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            SectionTitle("Privacy shortcuts", "Choose how Luna appears")
            SettingSwitch("Quick hide gesture", "Double-tap the Today heading to show a neutral notes screen", "⌑", panicHide, onPanicHide)
            SettingDivider()
            SettingSwitch("Discreet notifications", "Replace cycle details with your custom text", "◉", discreetNotifications, onDiscreetNotifications)
            SettingDivider()
            SettingSwitch("Protect app preview", "Hide the journal from screenshots and app switcher previews", "▣", captureProtection, onCaptureProtection)
            SettingDivider()
            Text("Launcher name and icon", Modifier.padding(top = 13.dp), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("Changing this adds the selected app name to your launcher.", color = Muted, fontSize = 11.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(vertical = 10.dp)) {
                listOf("Luna", "Notes", "Planner").forEach { profile ->
                    ChoiceChip(profile, launcherProfile == profile) { onLauncherProfile(profile) }
                }
            }
            SettingDivider()
            SettingSwitch("Haptic feedback", "Gentle tactile feedback on controls", "⌁", haptics, onHaptics)
            if (haptics) {
                Text("Haptic strength: $hapticStrength%", color = Muted, fontSize = 12.sp)
                Slider(
                    value = hapticStrength.toFloat(),
                    onValueChange = { onHapticStrength(it.roundToInt()) },
                    valueRange = 1f..100f,
                    steps = 98
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccessibilityAndRetentionSettings(
    fontScale: Float,
    boldText: Boolean,
    reduceMotion: Boolean,
    retentionYears: Int,
    onAccessibilityChanged: (Float, Boolean, Boolean) -> Unit,
    onRetentionChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Accessibility", "Reading and motion")
            Text("Text size: ${(fontScale * 100).roundToInt()}%", color = Muted, fontSize = 12.sp)
            Slider(
                value = fontScale,
                onValueChange = { onAccessibilityChanged(it, boldText, reduceMotion) },
                onValueChangeFinished = { playHaptic(context) },
                valueRange = 0.85f..1.35f,
                steps = 9
            )
            SettingSwitch("Bold text", "Increase text weight across the app", "B", boldText) {
                onAccessibilityChanged(fontScale, it, reduceMotion)
            }
            SettingSwitch("Reduce motion", "Use minimal screen transitions", "◫", reduceMotion) {
                onAccessibilityChanged(fontScale, boldText, it)
            }
            SettingDivider()
            SectionTitle("Automatic data purge", "On-device history")
            Text("Older daily logs are removed automatically. Export an encrypted backup first if you want to keep them.", color = Muted, fontSize = 11.sp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(0 to "Never", 1 to "1 year", 3 to "3 years", 5 to "5 years").forEach { (years, label) ->
                    ChoiceChip(label, retentionYears == years) { onRetentionChanged(years) }
                }
            }
        }
    }
}

private fun setLauncherProfile(context: Context, profile: String) {
    val aliases = mapOf(
        "Luna" to ".LunaLauncher",
        "Notes" to ".NotesLauncher",
        "Planner" to ".PlannerLauncher"
    )
    require(profile in aliases) { "Unknown launcher profile." }
    val packageManager = context.packageManager
    val enabled = ComponentName(context.packageName, context.packageName + aliases.getValue(profile))
    packageManager.setComponentEnabledSetting(
        enabled,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )
    aliases.filterKeys { it != profile }.values.forEach { className ->
        packageManager.setComponentEnabledSetting(
            ComponentName(context.packageName, context.packageName + className),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}

private fun hasCalendarPermissions(context: Context): Boolean =
    androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
        androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED

private fun syncPredictedCalendar(context: Context, start: LocalDate, cycleLength: Int, discreet: Boolean) {
    try {
        val count = CalendarSync.sync(context, start, cycleLength, discreet)
        showLunaToast(context, "Added $count cycle dates to your calendar.")
    } catch (error: Exception) {
        showLunaToast(context, "Calendar sync failed: ${error.localizedMessage ?: "Unable to sync."}")
    }
}

private fun scheduleRetention(context: Context) {
    val work = PeriodicWorkRequestBuilder<RetentionWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(24, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "luna-data-retention",
        ExistingPeriodicWorkPolicy.UPDATE,
        work
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogDialog(date: LocalDate, existingLog: CycleLog?, onDismiss: () -> Unit, onSave: (CycleLog) -> Unit) {
    val context = LocalContext.current
    var flow by remember(date) { mutableStateOf(existingLog?.flow.orEmpty()) }
    var mood by remember(date) { mutableStateOf(existingLog?.mood.orEmpty()) }
    var symptoms by remember(date) { mutableStateOf(existingLog?.symptomSet() ?: emptySet()) }
    var water by remember(date) { mutableIntStateOf(existingLog?.waterGlasses ?: 0) }
    var sleep by remember(date) { mutableIntStateOf(existingLog?.sleepQuality ?: 0) }
    var activity by remember(date) { mutableStateOf(existingLog?.activity.orEmpty()) }
    var sexualActivity by remember(date) { mutableStateOf(existingLog?.sexualActivity.orEmpty()) }
    var protection by remember(date) { mutableStateOf(existingLog?.protection.orEmpty()) }
    var cervicalMucus by remember(date) { mutableStateOf(existingLog?.cervicalMucus.orEmpty()) }
    var note by remember(date) { mutableStateOf(existingLog?.note.orEmpty()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Daily check-in", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")), color = Muted, fontSize = 12.sp)
                        }
                        Text("×", Modifier.clickable(onClick = onDismiss).padding(6.dp), color = Muted, fontSize = 24.sp)
                    }
                }
                item { SectionTitle("Flow", "How would you describe it?") }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Flows.forEach { option -> ChoiceChip(option, flow == option) { flow = if (flow == option) "" else option } }
                    }
                }
                item { SectionTitle("Symptoms", "Select all that apply") }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Symptoms.forEach { (name, icon) ->
                            ChoiceChip("$icon  $name", name in symptoms) {
                                symptoms = if (name in symptoms) symptoms - name else symptoms + name
                            }
                        }
                    }
                }
                item { SectionTitle("Mood", "What's your emotional weather?") }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Moods.forEach { option -> ChoiceChip(option, mood == option) { mood = if (mood == option) "" else option } }
                    }
                }
                item { SectionTitle("Water", "$water glasses") }
                item {
                    Slider(
                        value = water.toFloat(),
                        onValueChange = { water = it.roundToInt() },
                        onValueChangeFinished = { playHaptic(context) },
                        valueRange = 0f..12f,
                        steps = 11
                    )
                }
                item { SectionTitle("Sleep quality", if (sleep == 0) "Not rated" else "$sleep / 5") }
                item {
                    Slider(
                        value = sleep.toFloat(),
                        onValueChange = { sleep = it.roundToInt() },
                        onValueChangeFinished = { playHaptic(context) },
                        valueRange = 0f..5f,
                        steps = 4
                    )
                }
                item { SectionTitle("Movement", "What felt good today?") }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        ActivityOptions.forEach { option ->
                            ChoiceChip(option, activity == option) { activity = if (activity == option) "" else option }
                        }
                    }
                }
                item { SectionTitle("Cervical mucus", "Optional") }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        MucusOptions.forEach { option ->
                            ChoiceChip(option, cervicalMucus == option) { cervicalMucus = if (cervicalMucus == option) "" else option }
                        }
                    }
                }
                item { SectionTitle("Sexual activity", "Optional and private") }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("No", "Yes").forEach { option ->
                            ChoiceChip(option, sexualActivity == option) {
                                sexualActivity = if (sexualActivity == option) "" else option
                                if (sexualActivity != "Yes") protection = ""
                            }
                        }
                        if (sexualActivity == "Yes") {
                            ProtectionOptions.forEach { option ->
                                ChoiceChip(option, protection == option) { protection = if (protection == option) "" else option }
                            }
                        }
                    }
                }
                item {
                    androidx.compose.material3.OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("A note to yourself") },
                        placeholder = { Text("Anything else on your mind?") },
                        minLines = 2,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                item {
                    Button(
                        onClick = {
                            onSave(
                                CycleLog(
                                    date = date.toString(),
                                    flow = flow,
                                    symptoms = symptoms.sorted().joinToString("|"),
                                    mood = mood,
                                    note = note.trim(),
                                    waterGlasses = water,
                                    sleepQuality = sleep,
                                    activity = activity,
                                    sexualActivity = sexualActivity,
                                    protection = protection,
                                    cervicalMucus = cervicalMucus
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose)
                    ) { Text("Save check-in") }
                }
            }
        }
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    androidx.compose.material3.Surface(
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable {
            playHaptic(context)
            onClick()
        }.semantics { role = Role.Button },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Blush else MaterialTheme.colorScheme.background
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            color = if (selected) Rose else MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun playHaptic(context: Context) {
    val preferences = context.getSharedPreferences("luna_settings", Context.MODE_PRIVATE)
    if (!preferences.getBoolean("haptics_enabled", true)) return
    val strength = preferences.getInt("haptic_strength", 80).coerceIn(1, 100)
    val vibrator = if (Build.VERSION.SDK_INT >= 31) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (vibrator.hasVibrator()) {
        vibrator.vibrate(VibrationEffect.createOneShot(18L, (strength * 255 / 100).coerceIn(1, 255)))
    }
}

@Composable
private fun DatePickerDialog(initialDate: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    var selected by remember { mutableStateOf(initialDate) }
    var month by remember { mutableStateOf(YearMonth.from(initialDate)) }
    val language = LocalLunaLanguage.current
    val weekStartsOn = when (language) {
        "fr" -> 1
        "ar" -> 6
        else -> 0
    }
    val leading = ((month.atDay(1).dayOfWeek.value % 7) - weekStartsOn + 7) % 7
    val weekdayHeaders = when (language) {
        "fr" -> listOf("D", "L", "M", "M", "J", "V", "S").drop(weekStartsOn) + listOf("D", "L", "M", "M", "J", "V", "S").take(weekStartsOn)
        "ar" -> listOf("ا", "ن", "ث", "ر", "خ", "ج", "س").drop(weekStartsOn) + listOf("ا", "ن", "ث", "ر", "خ", "ج", "س").take(weekStartsOn)
        else -> listOf("S", "M", "T", "W", "T", "F", "S")
    }
    val total = ((leading + month.lengthOfMonth() + 6) / 7) * 7
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Last period started") },
        text = {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹", fontSize = 22.sp) }
                    Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { month = month.plusMonths(1) }) { Text("›", fontSize = 22.sp) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    weekdayHeaders.forEach { Text(it, color = Muted, fontSize = 12.sp) }
                }
                (0 until total step 7).forEach { start ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        (start until start + 7).forEach { cell ->
                            val day = cell - leading + 1
                            if (day in 1..month.lengthOfMonth()) {
                                val date = month.atDay(day)
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape)
                                        .background(if (selected == date) Rose else androidx.compose.ui.graphics.Color.Transparent)
                                        .clickable { selected = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$day", color = if (selected == date) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                }
                            } else Spacer(Modifier.size(36.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selected) }) { Text("Save", color = Rose) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        if (subtitle != null) Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}
