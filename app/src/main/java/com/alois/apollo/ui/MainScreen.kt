package com.alois.apollo.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alois.apollo.data.local.WorkoutSession
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartWorkout: () -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val workouts by viewModel.availableWorkouts.collectAsState()
    val history by viewModel.history.collectAsState()
    val isFrench by viewModel.isFrench.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importWorkout(context, it) }
    }

    var showSettingsDialog by remember { mutableStateOf(false) }

    if (showSettingsDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(if (isFrench) "Paramètres" else "Settings") },
            text = {
                Column {
                    Text(
                        text = if (isFrench) "Langue / Language" else "Language / Langue",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setLanguage(false) } // English
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = !isFrench,
                            onClick = { viewModel.setLanguage(false) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("English")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setLanguage(true) } // French
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = isFrench,
                            onClick = { viewModel.setLanguage(true) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Français")
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showSettingsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text(if (isFrench) "Historique" else "History") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenHistory()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                    label = { Text(if (isFrench) "Importer un entraînement" else "Import Workout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        launcher.launch("application/json")
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(if (isFrench) "Paramètres" else "Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSettingsDialog = true
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Apollo") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { launcher.launch("application/json") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Workout")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SectionHeader(
                        if (isFrench) "Entraînements disponibles" else "Available Workouts",
                        Icons.Default.PlayArrow
                    )
                }

                itemsIndexed(workouts) { index, workout ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 50L) // Stagger delay
                        visible = true
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = visible,
                        enter = androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(300)
                        ) + androidx.compose.animation.slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = androidx.compose.animation.core.tween(300)
                        )
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.startWorkout(workout)
                                    onStartWorkout()
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isFrench && workout.nameFr != null) workout.nameFr!! else workout.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (isFrench && workout.focusFr != null) workout.focusFr!! else workout.focus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isFrench) "Statistiques" else "Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    WorkoutHeatmap(history, isFrench)
                }

                item {
                    VolumeLoadGraph(history, isFrench)
                }
            }
        }
    }
}

@Composable
fun WorkoutHeatmap(history: List<WorkoutSession>, isFrench: Boolean) {
    val today = Calendar.getInstance()
    val cellSize = 14.dp
    val cellSpacing = 2.dp

    // Build a calendar-accurate grid: list of months with their days
    data class DayCell(val date: Long, val dayOfMonth: Int)
    data class MonthColumn(
        val month: Int,
        val year: Int,
        val monthName: String,
        val days: List<DayCell>
    )

    val months = mutableListOf<MonthColumn>()
    val startCal = Calendar.getInstance().apply {
        add(Calendar.MONTH, -3)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Process up to 4 months (current + 3 previous)
    repeat(4) {
        if (startCal.after(today)) return@repeat

        val monthDays = mutableListOf<DayCell>()
        val currentMonth = startCal.get(Calendar.MONTH)
        val currentYear = startCal.get(Calendar.YEAR)
        val monthName =
            java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(startCal.time)

        while (startCal.get(Calendar.MONTH) == currentMonth &&
            startCal.get(Calendar.YEAR) == currentYear
        ) {
            if (startCal.after(today)) break
            monthDays.add(
                DayCell(
                    date = startCal.timeInMillis,
                    dayOfMonth = startCal.get(Calendar.DAY_OF_MONTH)
                )
            )
            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        if (monthDays.isNotEmpty()) {
            months.add(MonthColumn(currentMonth, currentYear, monthName, monthDays))
        }
    }

    // Count workouts per day
    val workoutCounts = history.groupBy { session ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = session.date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }.mapValues { it.value.size }

    val dateFormat =
        remember { java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault()) }
    var selectedCell by remember { mutableStateOf<Pair<Long, Int>?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (isFrench) "Fréquence d'entraînement" else "Workout Frequency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Box {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    months.forEachIndexed { monthIdx, monthColumn ->
                        // Month separator (smaller spacing)
                        if (monthIdx > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Month block
                        Column {
                            // Month label above the grid
                            Text(
                                text = monthColumn.monthName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // Days grid - chunks of 7 days per row (week-like columns)
                            Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
                                val columns = monthColumn.days.chunked(7)
                                columns.forEach { columnDays ->
                                    Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
                                        columnDays.forEach { dayCell ->
                                            val workoutCount = workoutCounts[dayCell.date] ?: 0
                                            val intensity = when {
                                                workoutCount == 0 -> 0f
                                                workoutCount == 1 -> 0.5f
                                                workoutCount >= 2 -> 1f
                                                else -> 0f
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(cellSize)
                                                    .background(
                                                        when {
                                                            intensity == 0f -> MaterialTheme.colorScheme.surfaceVariant
                                                            intensity < 1f -> MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.5f
                                                            )

                                                            else -> MaterialTheme.colorScheme.primary
                                                        },
                                                        RoundedCornerShape(2.dp)
                                                    )
                                                    .pointerInput(dayCell.date) {
                                                        detectTapGestures(
                                                            onLongPress = {
                                                                selectedCell =
                                                                    dayCell.date to workoutCount
                                                            },
                                                            onPress = {
                                                                awaitRelease()
                                                                selectedCell = null
                                                            }
                                                        )
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tooltip overlay - positioned at top center
                selectedCell?.let { (date, count) ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-8).dp)
                            .background(
                                MaterialTheme.colorScheme.inverseSurface,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dateFormat.format(Date(date)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (count == 0) (if (isFrench) "Pas d'entraînement" else "No workout") else "$count " + (if (isFrench) "entraînement" else "workout") + if (count > 1) "s" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VolumeLoadGraph(history: List<WorkoutSession>, isFrench: Boolean) {
    val sortedHistory = history.sortedBy { it.date }
    if (sortedHistory.size < 2) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isFrench) "Terminez plus d'entraînements pour voir votre progression" else "Complete more workouts to see your progression",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // Normalize old volumeLoad values (if < 500, assume it's raw reps and multiply by 68kg)
    fun normalizeVolume(rawVolume: Int): Int {
        return if (rawVolume < 500) rawVolume * 68 else rawVolume
    }

    val maxVolume = sortedHistory.maxOf { normalizeVolume(it.volumeLoad) }.toFloat()
    val minVolume = sortedHistory.minOf { normalizeVolume(it.volumeLoad) }.toFloat()
    val range = (maxVolume - minVolume).coerceAtLeast(1f)

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var points by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val dateFormat = remember { java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    LocalDensity.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (isFrench) "Progression de la charge" else "Volume Load Progression",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxSize()) {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${maxVolume.toInt()}kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceColor,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${((maxVolume + minVolume) / 2).toInt()}kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceColor,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${minVolume.toInt()}kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceColor,
                        fontSize = 9.sp
                    )
                }

                // Graph area
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(sortedHistory) {
                                    detectTapGestures(
                                        onLongPress = { offset ->
                                            // Find closest point
                                            val closest = points.withIndex().minByOrNull {
                                                abs(it.value.x - offset.x)
                                            }
                                            if (closest != null) {
                                                selectedPointIndex = closest.index
                                            }
                                        },
                                        onPress = {
                                            awaitRelease()
                                            selectedPointIndex = null
                                        }
                                    )
                                }
                        ) {
                            val width = size.width
                            val height = size.height
                            val spacing = width / (sortedHistory.size - 1)
                            val axisColor = onSurfaceColor.copy(alpha = 0.3f)

                            // Draw horizontal grid lines
                            for (i in 0..4) {
                                val y = height * i / 4
                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, y),
                                    end = Offset(width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Calculate and store points
                            points = sortedHistory.mapIndexed { index, session ->
                                val x = index * spacing
                                val normalizedVolume = normalizeVolume(session.volumeLoad)
                                val y = height - ((normalizedVolume - minVolume) / range * height)
                                Offset(x, y)
                            }

                            // Draw the line path
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                points.forEach { lineTo(it.x, it.y) }
                            }

                            drawPath(
                                path = path,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // Draw points
                            points.forEachIndexed { index, point ->
                                val isSelected = index == selectedPointIndex
                                drawCircle(
                                    color = if (isSelected) surfaceColor else primaryColor,
                                    radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                                    center = point
                                )
                                if (isSelected) {
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 8.dp.toPx(),
                                        center = point,
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                            }
                        }

                        // Tooltip overlay - positioned at top center
                        selectedPointIndex?.let { idx ->
                            if (idx in sortedHistory.indices && idx in points.indices) {
                                val session = sortedHistory[idx]

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .offset(y = (-8).dp)
                                        .background(
                                            MaterialTheme.colorScheme.inverseSurface,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dateFormat.format(Date(session.date)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.inverseOnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${normalizeVolume(session.volumeLoad)} kg",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.inverseOnSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // X-axis date labels
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labelCount = minOf(5, sortedHistory.size)
                        val step = (sortedHistory.size - 1) / (labelCount - 1).coerceAtLeast(1)

                        for (i in 0 until labelCount) {
                            val idx = (i * step).coerceIn(0, sortedHistory.lastIndex)
                            Text(
                                text = dateFormat.format(Date(sortedHistory[idx].date)),
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
