package com.example.lifedots

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.lifedots.preferences.AnimationType
import com.example.lifedots.preferences.CustomYearSettings
import com.example.lifedots.preferences.DotShape
import com.example.lifedots.preferences.DotSize
import com.example.lifedots.preferences.DotStyle
import com.example.lifedots.preferences.FluidStyle
import com.example.lifedots.preferences.GlassStyle
import com.example.lifedots.preferences.Goal
import com.example.lifedots.preferences.GoalPosition
import com.example.lifedots.preferences.GridDensity
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.LifeProgress
import com.example.lifedots.preferences.LifeSettings
import com.example.lifedots.preferences.PositionSettings
import com.example.lifedots.preferences.ProgressPosition
import com.example.lifedots.preferences.ProgressSettings
import com.example.lifedots.preferences.TextAlignment
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.preferences.TimeScale
import com.example.lifedots.preferences.TreeStyle
import com.example.lifedots.preferences.ViewMode
import com.example.lifedots.preferences.VisualTheme
import com.example.lifedots.ui.components.ColorButton
import com.example.lifedots.ui.components.ColorPickerDialog
import com.example.lifedots.ui.components.DatePickerDialog
import com.example.lifedots.ui.components.GoalEditorDialog
import com.example.lifedots.ui.theme.LifeDotsTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {

    private lateinit var preferences: LifeDotsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = LifeDotsPreferences.getInstance(this)

        setContent {
            LifeDotsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsScreen(
                        preferences = preferences,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    preferences: LifeDotsPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settingsFlow.collectAsState()
    val context = LocalContext.current

    var showBgColorPicker by remember { mutableStateOf(false) }
    var showFilledColorPicker by remember { mutableStateOf(false) }
    var showEmptyColorPicker by remember { mutableStateOf(false) }
    var showTodayColorPicker by remember { mutableStateOf(false) }
    var showFooterColorPicker by remember { mutableStateOf(false) }
    var showProgressColorPicker by remember { mutableStateOf(false) }
    var showGoalEditor by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    var showGlassTintPicker by remember { mutableStateOf(false) }
    var showTreeTrunkColorPicker by remember { mutableStateOf(false) }
    var showTreeLeafColorPicker by remember { mutableStateOf(false) }
    var showTreeBloomColorPicker by remember { mutableStateOf(false) }
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showCustomYearStartDatePicker by remember { mutableStateOf(false) }
    var showCustomYearEndDatePicker by remember { mutableStateOf(false) }

    // Permission state for image picker
    var hasImagePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Take persistable permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Permission not persistable, that's okay
            }
            preferences.setBackgroundUri(it.toString())
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasImagePermission = isGranted
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Time Scale Section
        SettingsSection(title = "Time Scale") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { preferences.setTimeScale(TimeScale.YEAR) },
                    color = if (settings.timeScale == TimeScale.YEAR) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    border = if (settings.timeScale == TimeScale.YEAR) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Year in Days",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.timeScale == TimeScale.YEAR) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "365 days of year",
                            fontSize = 12.sp,
                            color = (if (settings.timeScale == TimeScale.YEAR) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { preferences.setTimeScale(TimeScale.LIFE) },
                    color = if (settings.timeScale == TimeScale.LIFE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    border = if (settings.timeScale == TimeScale.LIFE) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Life in Weeks",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.timeScale == TimeScale.LIFE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Memento Mori",
                            fontSize = 12.sp,
                            color = (if (settings.timeScale == TimeScale.LIFE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Custom Year / Academic Year Settings (visible when Year mode selected)
        AnimatedVisibility(
            visible = settings.timeScale == TimeScale.YEAR,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            val customYear = settings.customYearSettings
            val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
            val startDateString = remember(customYear.startDate) {
                dateFormat.format(Date(customYear.startDate))
            }
            val endDateString = remember(customYear.endDate) {
                dateFormat.format(Date(customYear.endDate))
            }
            val progress = remember(customYear) { customYear.calculateProgress() }

            Column {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSection(title = stringResource(R.string.custom_year_section)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.custom_year_enable),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.custom_year_desc),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Switch(
                                    checked = customYear.enabled,
                                    onCheckedChange = { preferences.setCustomYearEnabled(it) }
                                )
                            }

                            AnimatedVisibility(visible = customYear.enabled) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Summary Card
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Custom Year Progress",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${String.format(Locale.getDefault(), "%.1f", progress.percentage)}%",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            LinearProgressIndicator(
                                                progress = (progress.percentage / 100.0).toFloat().coerceIn(0f, 1f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Day Count",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                    Text(
                                                        text = "Day ${progress.dayIndex} of ${progress.totalDays}",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "Days Remaining",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                    )
                                                    Text(
                                                        text = "${progress.daysRemaining} left",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Quick Presets
                                    Text(
                                        text = "Quick Presets",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val (start, end) = CustomYearSettings.createAcademicYearRange()
                                                preferences.setCustomYearRange(start, end)
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Sep – May",
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val (start, end) = CustomYearSettings.createSchoolYearRange()
                                                preferences.setCustomYearRange(start, end)
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Aug – Jun",
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val (start, end) = CustomYearSettings.createCalendarYearRange()
                                                preferences.setCustomYearRange(start, end)
                                            },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Jan – Dec",
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Start Date
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.custom_year_start),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = startDateString,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showCustomYearStartDatePicker = true }
                                        ) {
                                            Text("Change")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // End Date
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(R.string.custom_year_end),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = endDateString,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { showCustomYearEndDatePicker = true }
                                        ) {
                                            Text("Change")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Life in Weeks Settings (visible when Life mode selected)
        AnimatedVisibility(
            visible = settings.timeScale == TimeScale.LIFE,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            val lifeSettings = settings.lifeSettings
            val progress = remember(lifeSettings) { lifeSettings.calculateProgress() }
            val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }
            val birthDateString = remember(lifeSettings.birthDate) {
                dateFormat.format(Date(lifeSettings.birthDate))
            }

            Column {
                Spacer(modifier = Modifier.height(24.dp))
                SettingsSection(title = "Life in Weeks (Memento Mori)") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Summary Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Memento Mori Summary",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Current Age",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = "${progress.ageYears} yrs (${progress.weekInCurrentYear} wks)",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Life Lived",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", progress.percentLived)}%",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Weeks Lived",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = "${progress.weeksLived} / ${progress.totalWeeks}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Weeks Remaining",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = "${progress.weeksRemaining} left",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Date of Birth
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Date of Birth", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(birthDateString, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                                OutlinedButton(
                                    onClick = { showBirthDatePicker = true }
                                ) {
                                    Text("Change")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Life Expectancy Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Life Expectancy", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${lifeSettings.lifeExpectancyYears} years (${lifeSettings.lifeExpectancyYears * 52} weeks)", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            }
                            Slider(
                                value = lifeSettings.lifeExpectancyYears.toFloat(),
                                onValueChange = { preferences.setLifeExpectancy(it.roundToInt()) },
                                valueRange = 50f..100f,
                                steps = 49
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Title toggle & input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Top Header Title", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Classic 'MEMENTO MORI' poster title", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Switch(
                                    checked = lifeSettings.showTitle,
                                    onCheckedChange = { preferences.setLifeShowTitle(it) }
                                )
                            }

                            AnimatedVisibility(visible = lifeSettings.showTitle) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = lifeSettings.titleText,
                                        onValueChange = { preferences.setLifeTitleText(it) },
                                        label = { Text("Title Text") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Split Halves (Mid-Year Gap) toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Mid-Year Split", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Divide weeks into two 26-week columns", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Switch(
                                    checked = lifeSettings.splitHalves,
                                    onCheckedChange = { preferences.setLifeSplitHalves(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Year Labels toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Year Markers", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Show 5, 10, 15... markers on edge", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Switch(
                                    checked = lifeSettings.showYearLabels,
                                    onCheckedChange = { preferences.setLifeShowYearLabels(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quote toggle & inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Footer Quote", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Philosophical reflection at wallpaper bottom", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Switch(
                                    checked = lifeSettings.showQuote,
                                    onCheckedChange = { preferences.setLifeShowQuote(it) }
                                )
                            }

                            AnimatedVisibility(visible = lifeSettings.showQuote) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = lifeSettings.quoteText,
                                        onValueChange = { preferences.setLifeQuoteText(it) },
                                        label = { Text("Quote Text") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 5
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = lifeSettings.quoteAuthor,
                                        onValueChange = { preferences.setLifeQuoteAuthor(it) },
                                        label = { Text("Author") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Theme Section
        SettingsSection(title = stringResource(R.string.theme_section)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionButton(
                    label = stringResource(R.string.theme_light),
                    backgroundColor = Color(0xFFF5F5F5),
                    dotColor = Color(0xFF2C2C2C),
                    isSelected = settings.theme == ThemeOption.LIGHT,
                    onClick = { preferences.setTheme(ThemeOption.LIGHT) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    label = stringResource(R.string.theme_dark),
                    backgroundColor = Color(0xFF1A1A1A),
                    dotColor = Color(0xFFE0E0E0),
                    isSelected = settings.theme == ThemeOption.DARK,
                    onClick = { preferences.setTheme(ThemeOption.DARK) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    label = stringResource(R.string.theme_amoled),
                    backgroundColor = Color(0xFF000000),
                    dotColor = Color(0xFFFFFFFF),
                    isSelected = settings.theme == ThemeOption.AMOLED,
                    onClick = { preferences.setTheme(ThemeOption.AMOLED) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    label = "Custom",
                    backgroundColor = Color(settings.customColors.backgroundColor),
                    dotColor = Color(settings.customColors.filledDotColor),
                    isSelected = settings.theme == ThemeOption.CUSTOM,
                    onClick = { preferences.setTheme(ThemeOption.CUSTOM) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Custom Colors Section (visible when Custom theme selected)
        AnimatedVisibility(
            visible = settings.theme == ThemeOption.CUSTOM,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "Custom Colors") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ColorButton(
                            color = settings.customColors.backgroundColor,
                            label = "Background",
                            onClick = { showBgColorPicker = true }
                        )
                        ColorButton(
                            color = settings.customColors.filledDotColor,
                            label = "Filled Dots",
                            onClick = { showFilledColorPicker = true }
                        )
                        ColorButton(
                            color = settings.customColors.emptyDotColor,
                            label = "Empty Dots",
                            onClick = { showEmptyColorPicker = true }
                        )
                        ColorButton(
                            color = settings.customColors.todayDotColor,
                            label = "Today's Dot",
                            onClick = { showTodayColorPicker = true }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dot Shape Section
        SettingsSection(title = "Dot Shape") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotShapeOption(
                    shape = DotShape.CIRCLE,
                    label = "Circle",
                    isSelected = settings.dotShape == DotShape.CIRCLE,
                    onClick = { preferences.setDotShape(DotShape.CIRCLE) },
                    modifier = Modifier.weight(1f)
                )
                DotShapeOption(
                    shape = DotShape.SQUARE,
                    label = "Square",
                    isSelected = settings.dotShape == DotShape.SQUARE,
                    onClick = { preferences.setDotShape(DotShape.SQUARE) },
                    modifier = Modifier.weight(1f)
                )
                DotShapeOption(
                    shape = DotShape.ROUNDED_SQUARE,
                    label = "Rounded",
                    isSelected = settings.dotShape == DotShape.ROUNDED_SQUARE,
                    onClick = { preferences.setDotShape(DotShape.ROUNDED_SQUARE) },
                    modifier = Modifier.weight(1f)
                )
                DotShapeOption(
                    shape = DotShape.DIAMOND,
                    label = "Diamond",
                    isSelected = settings.dotShape == DotShape.DIAMOND,
                    onClick = { preferences.setDotShape(DotShape.DIAMOND) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dot Size Section
        SettingsSection(title = stringResource(R.string.dot_size_section)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotSizeOption(
                    label = "Tiny",
                    dotSize = 6.dp,
                    isSelected = settings.dotSize == DotSize.TINY,
                    onClick = { preferences.setDotSize(DotSize.TINY) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = stringResource(R.string.dot_size_small),
                    dotSize = 8.dp,
                    isSelected = settings.dotSize == DotSize.SMALL,
                    onClick = { preferences.setDotSize(DotSize.SMALL) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = stringResource(R.string.dot_size_medium),
                    dotSize = 12.dp,
                    isSelected = settings.dotSize == DotSize.MEDIUM,
                    onClick = { preferences.setDotSize(DotSize.MEDIUM) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = stringResource(R.string.dot_size_large),
                    dotSize = 16.dp,
                    isSelected = settings.dotSize == DotSize.LARGE,
                    onClick = { preferences.setDotSize(DotSize.LARGE) },
                    modifier = Modifier.weight(1f)
                )
                DotSizeOption(
                    label = "Huge",
                    dotSize = 20.dp,
                    isSelected = settings.dotSize == DotSize.HUGE,
                    onClick = { preferences.setDotSize(DotSize.HUGE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid Density Section
        SettingsSection(title = stringResource(R.string.grid_density_section)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GridDensityOption(
                    label = stringResource(R.string.grid_compact),
                    columns = 6,
                    isSelected = settings.gridDensity == GridDensity.COMPACT,
                    onClick = { preferences.setGridDensity(GridDensity.COMPACT) },
                    modifier = Modifier.weight(1f)
                )
                GridDensityOption(
                    label = "Normal",
                    columns = 5,
                    isSelected = settings.gridDensity == GridDensity.NORMAL,
                    onClick = { preferences.setGridDensity(GridDensity.NORMAL) },
                    modifier = Modifier.weight(1f)
                )
                GridDensityOption(
                    label = stringResource(R.string.grid_relaxed),
                    columns = 4,
                    isSelected = settings.gridDensity == GridDensity.RELAXED,
                    onClick = { preferences.setGridDensity(GridDensity.RELAXED) },
                    modifier = Modifier.weight(1f)
                )
                GridDensityOption(
                    label = "Spacious",
                    columns = 3,
                    isSelected = settings.gridDensity == GridDensity.SPACIOUS,
                    onClick = { preferences.setGridDensity(GridDensity.SPACIOUS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transparency Section
        SettingsSection(title = "Transparency") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Filled dots alpha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filled Dots",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(settings.filledDotAlpha * 100).roundToInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Slider(
                        value = settings.filledDotAlpha,
                        onValueChange = { preferences.setFilledDotAlpha(it) },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Empty dots alpha
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Empty Dots",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(settings.emptyDotAlpha * 100).roundToInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Slider(
                        value = settings.emptyDotAlpha,
                        onValueChange = { preferences.setEmptyDotAlpha(it) },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Highlight Today Toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.highlight_today),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.highlight_today_desc),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = settings.highlightToday,
                    onCheckedChange = { preferences.setHighlightToday(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Feature 3: Dot Style Section =====
        SettingsSection(title = "Dot Style") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotStyleOption(
                    label = "Flat",
                    style = DotStyle.FLAT,
                    isSelected = settings.dotEffectSettings.style == DotStyle.FLAT,
                    onClick = { preferences.setDotStyle(DotStyle.FLAT) },
                    modifier = Modifier.weight(1f)
                )
                DotStyleOption(
                    label = "Gradient",
                    style = DotStyle.GRADIENT,
                    isSelected = settings.dotEffectSettings.style == DotStyle.GRADIENT,
                    onClick = { preferences.setDotStyle(DotStyle.GRADIENT) },
                    modifier = Modifier.weight(1f)
                )
                DotStyleOption(
                    label = "Outlined",
                    style = DotStyle.OUTLINED,
                    isSelected = settings.dotEffectSettings.style == DotStyle.OUTLINED,
                    onClick = { preferences.setDotStyle(DotStyle.OUTLINED) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotStyleOption(
                    label = "Glow",
                    style = DotStyle.SOFT_GLOW,
                    isSelected = settings.dotEffectSettings.style == DotStyle.SOFT_GLOW,
                    onClick = { preferences.setDotStyle(DotStyle.SOFT_GLOW) },
                    modifier = Modifier.weight(1f)
                )
                DotStyleOption(
                    label = "Neon",
                    style = DotStyle.NEON,
                    isSelected = settings.dotEffectSettings.style == DotStyle.NEON,
                    onClick = { preferences.setDotStyle(DotStyle.NEON) },
                    modifier = Modifier.weight(1f)
                )
                DotStyleOption(
                    label = "Embossed",
                    style = DotStyle.EMBOSSED,
                    isSelected = settings.dotEffectSettings.style == DotStyle.EMBOSSED,
                    onClick = { preferences.setDotStyle(DotStyle.EMBOSSED) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Dot Effect Sliders (visible for certain styles)
        AnimatedVisibility(
            visible = settings.dotEffectSettings.style in listOf(DotStyle.SOFT_GLOW, DotStyle.NEON),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Glow Radius", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${settings.dotEffectSettings.glowRadius.roundToInt()}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.dotEffectSettings.glowRadius,
                            onValueChange = { preferences.setGlowRadius(it) },
                            valueRange = 2f..20f
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = settings.dotEffectSettings.style == DotStyle.OUTLINED,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Outline Width", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${settings.dotEffectSettings.outlineWidth.roundToInt()}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.dotEffectSettings.outlineWidth,
                            onValueChange = { preferences.setOutlineWidth(it) },
                            valueRange = 1f..5f
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Features 4 & 5: View Mode Section =====
        SettingsSection(title = "View Mode") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewModeOption(
                    label = "Continuous",
                    mode = ViewMode.CONTINUOUS,
                    isSelected = settings.viewModeSettings.mode == ViewMode.CONTINUOUS,
                    onClick = { preferences.setViewMode(ViewMode.CONTINUOUS) },
                    modifier = Modifier.weight(1f)
                )
                ViewModeOption(
                    label = "Monthly",
                    mode = ViewMode.MONTHLY,
                    isSelected = settings.viewModeSettings.mode == ViewMode.MONTHLY,
                    onClick = { preferences.setViewMode(ViewMode.MONTHLY) },
                    modifier = Modifier.weight(1f)
                )
                ViewModeOption(
                    label = "Calendar",
                    mode = ViewMode.CALENDAR,
                    isSelected = settings.viewModeSettings.mode == ViewMode.CALENDAR,
                    onClick = { preferences.setViewMode(ViewMode.CALENDAR) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // View Mode Options
        AnimatedVisibility(
            visible = settings.viewModeSettings.mode != ViewMode.CONTINUOUS,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Month Labels", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = settings.viewModeSettings.showMonthLabels,
                                onCheckedChange = { preferences.setShowMonthLabels(it) }
                            )
                        }
                    }
                }
            }
        }

        // Calendar columns option
        AnimatedVisibility(
            visible = settings.viewModeSettings.mode == ViewMode.CALENDAR,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Months Per Row", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalendarColumnsOption(
                                label = "3x4",
                                columns = 3,
                                isSelected = settings.calendarViewSettings.columnsPerRow == 3,
                                onClick = { preferences.setCalendarColumns(3) },
                                modifier = Modifier.weight(1f)
                            )
                            CalendarColumnsOption(
                                label = "4x3",
                                columns = 4,
                                isSelected = settings.calendarViewSettings.columnsPerRow == 4,
                                onClick = { preferences.setCalendarColumns(4) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Feature 2: Footer Text Section =====
        SettingsSection(title = "Footer Text") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Footer Text", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = settings.footerTextSettings.enabled,
                            onCheckedChange = { preferences.setFooterEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.footerTextSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = settings.footerTextSettings.text,
                                onValueChange = { preferences.setFooterText(it) },
                                label = { Text("Footer Text") },
                                placeholder = { Text("e.g., Make every day count") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tip: Use {percent}, {remaining}, {passed}, or {total} for live stats",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Font Size", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${settings.footerTextSettings.fontSize.roundToInt()}sp", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.footerTextSettings.fontSize,
                                onValueChange = { preferences.setFooterFontSize(it) },
                                valueRange = 10f..24f
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Alignment", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextAlignmentOption(
                                    label = "Left",
                                    alignment = TextAlignment.LEFT,
                                    isSelected = settings.footerTextSettings.alignment == TextAlignment.LEFT,
                                    onClick = { preferences.setFooterAlignment(TextAlignment.LEFT) },
                                    modifier = Modifier.weight(1f)
                                )
                                TextAlignmentOption(
                                    label = "Center",
                                    alignment = TextAlignment.CENTER,
                                    isSelected = settings.footerTextSettings.alignment == TextAlignment.CENTER,
                                    onClick = { preferences.setFooterAlignment(TextAlignment.CENTER) },
                                    modifier = Modifier.weight(1f)
                                )
                                TextAlignmentOption(
                                    label = "Right",
                                    alignment = TextAlignment.RIGHT,
                                    isSelected = settings.footerTextSettings.alignment == TextAlignment.RIGHT,
                                    onClick = { preferences.setFooterAlignment(TextAlignment.RIGHT) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Color", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(settings.footerTextSettings.color))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable { showFooterColorPicker = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Year Progress & Countdown Section =====
        SettingsSection(title = "Year Progress & Countdown") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show Year Progress", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Display percentage and remaining days", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = settings.progressSettings.enabled,
                            onCheckedChange = { preferences.setProgressEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.progressSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Live Preview Box
                            val (dayOfYear, totalDays) = remember(settings.customYearSettings) {
                                if (settings.customYearSettings.enabled) {
                                    val prog = settings.customYearSettings.calculateProgress()
                                    Pair(prog.dayIndex, prog.totalDays)
                                } else {
                                    val calendar = Calendar.getInstance()
                                    Pair(calendar.get(Calendar.DAY_OF_YEAR), calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                                }
                            }
                            val previewText = remember(settings.progressSettings, dayOfYear, totalDays) {
                                settings.progressSettings.formatText(dayOfYear, totalDays)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(16.dp),
                                contentAlignment = when (settings.progressSettings.alignment) {
                                    TextAlignment.LEFT -> Alignment.CenterStart
                                    TextAlignment.CENTER -> Alignment.Center
                                    TextAlignment.RIGHT -> Alignment.CenterEnd
                                }
                            ) {
                                Text(
                                    text = previewText,
                                    fontSize = settings.progressSettings.fontSize.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(settings.progressSettings.color)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Display Options", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Percentage", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = settings.progressSettings.showPercentage,
                                    onCheckedChange = { preferences.setProgressShowPercentage(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Remaining Days", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = settings.progressSettings.showRemainingDays,
                                    onCheckedChange = { preferences.setProgressShowRemaining(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Day Count", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = settings.progressSettings.showDaysPassed,
                                    onCheckedChange = { preferences.setProgressShowPassed(it) }
                                )
                            }

                            if (settings.progressSettings.showPercentage) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Percentage Decimals", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DecimalPlacesOption(
                                        label = "0 (24%)",
                                        places = 0,
                                        isSelected = settings.progressSettings.decimalPlaces == 0,
                                        onClick = { preferences.setProgressDecimalPlaces(0) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DecimalPlacesOption(
                                        label = "1 (24.5%)",
                                        places = 1,
                                        isSelected = settings.progressSettings.decimalPlaces == 1,
                                        onClick = { preferences.setProgressDecimalPlaces(1) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DecimalPlacesOption(
                                        label = "2 (24.45%)",
                                        places = 2,
                                        isSelected = settings.progressSettings.decimalPlaces == 2,
                                        onClick = { preferences.setProgressDecimalPlaces(2) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Position", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ProgressPositionOption(
                                    label = "Top",
                                    position = ProgressPosition.TOP,
                                    isSelected = settings.progressSettings.position == ProgressPosition.TOP,
                                    onClick = { preferences.setProgressPosition(ProgressPosition.TOP) },
                                    modifier = Modifier.weight(1f)
                                )
                                ProgressPositionOption(
                                    label = "Bottom",
                                    position = ProgressPosition.BOTTOM,
                                    isSelected = settings.progressSettings.position == ProgressPosition.BOTTOM,
                                    onClick = { preferences.setProgressPosition(ProgressPosition.BOTTOM) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Font Size", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${settings.progressSettings.fontSize.roundToInt()}sp", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.progressSettings.fontSize,
                                onValueChange = { preferences.setProgressFontSize(it) },
                                valueRange = 10f..24f
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Alignment", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextAlignmentOption(
                                    label = "Left",
                                    alignment = TextAlignment.LEFT,
                                    isSelected = settings.progressSettings.alignment == TextAlignment.LEFT,
                                    onClick = { preferences.setProgressAlignment(TextAlignment.LEFT) },
                                    modifier = Modifier.weight(1f)
                                )
                                TextAlignmentOption(
                                    label = "Center",
                                    alignment = TextAlignment.CENTER,
                                    isSelected = settings.progressSettings.alignment == TextAlignment.CENTER,
                                    onClick = { preferences.setProgressAlignment(TextAlignment.CENTER) },
                                    modifier = Modifier.weight(1f)
                                )
                                TextAlignmentOption(
                                    label = "Right",
                                    alignment = TextAlignment.RIGHT,
                                    isSelected = settings.progressSettings.alignment == TextAlignment.RIGHT,
                                    onClick = { preferences.setProgressAlignment(TextAlignment.RIGHT) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Color", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(settings.progressSettings.color))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable { showProgressColorPicker = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Feature 1: Background Photo Section =====
        SettingsSection(title = "Background Photo") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Background", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = settings.backgroundSettings.enabled,
                            onCheckedChange = { preferences.setBackgroundEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.backgroundSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = {
                                    if (hasImagePermission) {
                                        imagePickerLauncher.launch("image/*")
                                    } else {
                                        permissionLauncher.launch(
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                Manifest.permission.READ_MEDIA_IMAGES
                                            } else {
                                                Manifest.permission.READ_EXTERNAL_STORAGE
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (settings.backgroundSettings.imageUri != null) "Change Image" else "Select Image"
                                )
                            }

                            if (settings.backgroundSettings.imageUri != null) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Opacity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${(settings.backgroundSettings.opacity * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Slider(
                                    value = settings.backgroundSettings.opacity,
                                    onValueChange = { preferences.setBackgroundOpacity(it) },
                                    valueRange = 0.1f..1f
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Blur", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${settings.backgroundSettings.blurRadius.roundToInt()}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Slider(
                                    value = settings.backgroundSettings.blurRadius,
                                    onValueChange = { preferences.setBackgroundBlur(it) },
                                    valueRange = 0f..25f
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Position & Scale Section =====
        SettingsSection(title = "Position & Scale") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Horizontal Offset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horizontal Offset",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${settings.positionSettings.horizontalOffset.roundToInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Slider(
                        value = settings.positionSettings.horizontalOffset,
                        onValueChange = { preferences.setHorizontalOffset(it) },
                        valueRange = -50f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vertical Offset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vertical Offset",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${settings.positionSettings.verticalOffset.roundToInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Slider(
                        value = settings.positionSettings.verticalOffset,
                        onValueChange = { preferences.setVerticalOffset(it) },
                        valueRange = -50f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scale
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scale",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(settings.positionSettings.scale * 100).roundToInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Slider(
                        value = settings.positionSettings.scale,
                        onValueChange = { preferences.setScale(it) },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Animation Section =====
        SettingsSection(title = "Animation") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Animation", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = settings.animationSettings.enabled,
                            onCheckedChange = { preferences.setAnimationEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.animationSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Animation Type", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimationTypeOption(
                                    label = "Fade",
                                    type = AnimationType.FADE_IN,
                                    isSelected = settings.animationSettings.type == AnimationType.FADE_IN,
                                    onClick = { preferences.setAnimationType(AnimationType.FADE_IN) },
                                    modifier = Modifier.weight(1f)
                                )
                                AnimationTypeOption(
                                    label = "Pulse",
                                    type = AnimationType.PULSE,
                                    isSelected = settings.animationSettings.type == AnimationType.PULSE,
                                    onClick = { preferences.setAnimationType(AnimationType.PULSE) },
                                    modifier = Modifier.weight(1f)
                                )
                                AnimationTypeOption(
                                    label = "Wave",
                                    type = AnimationType.WAVE,
                                    isSelected = settings.animationSettings.type == AnimationType.WAVE,
                                    onClick = { preferences.setAnimationType(AnimationType.WAVE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimationTypeOption(
                                    label = "Breathe",
                                    type = AnimationType.BREATHE,
                                    isSelected = settings.animationSettings.type == AnimationType.BREATHE,
                                    onClick = { preferences.setAnimationType(AnimationType.BREATHE) },
                                    modifier = Modifier.weight(1f)
                                )
                                AnimationTypeOption(
                                    label = "Ripple",
                                    type = AnimationType.RIPPLE,
                                    isSelected = settings.animationSettings.type == AnimationType.RIPPLE,
                                    onClick = { preferences.setAnimationType(AnimationType.RIPPLE) },
                                    modifier = Modifier.weight(1f)
                                )
                                AnimationTypeOption(
                                    label = "Cascade",
                                    type = AnimationType.CASCADE,
                                    isSelected = settings.animationSettings.type == AnimationType.CASCADE,
                                    onClick = { preferences.setAnimationType(AnimationType.CASCADE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Speed
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Speed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${(settings.animationSettings.speed * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.animationSettings.speed,
                                onValueChange = { preferences.setAnimationSpeed(it) },
                                valueRange = 0.5f..2f
                            )

                            // Intensity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Intensity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${(settings.animationSettings.intensity * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.animationSettings.intensity,
                                onValueChange = { preferences.setAnimationIntensity(it) },
                                valueRange = 0.1f..1f
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Glass Effect Section =====
        SettingsSection(title = "Glass Effect") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Glass Effect", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = settings.glassEffectSettings.enabled,
                            onCheckedChange = { preferences.setGlassEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.glassEffectSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Glass Style", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlassStyleOption(
                                    label = "Light",
                                    style = GlassStyle.LIGHT_FROST,
                                    isSelected = settings.glassEffectSettings.style == GlassStyle.LIGHT_FROST,
                                    onClick = { preferences.setGlassStyle(GlassStyle.LIGHT_FROST) },
                                    modifier = Modifier.weight(1f)
                                )
                                GlassStyleOption(
                                    label = "Heavy",
                                    style = GlassStyle.HEAVY_FROST,
                                    isSelected = settings.glassEffectSettings.style == GlassStyle.HEAVY_FROST,
                                    onClick = { preferences.setGlassStyle(GlassStyle.HEAVY_FROST) },
                                    modifier = Modifier.weight(1f)
                                )
                                GlassStyleOption(
                                    label = "Acrylic",
                                    style = GlassStyle.ACRYLIC,
                                    isSelected = settings.glassEffectSettings.style == GlassStyle.ACRYLIC,
                                    onClick = { preferences.setGlassStyle(GlassStyle.ACRYLIC) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlassStyleOption(
                                    label = "Crystal",
                                    style = GlassStyle.CRYSTAL,
                                    isSelected = settings.glassEffectSettings.style == GlassStyle.CRYSTAL,
                                    onClick = { preferences.setGlassStyle(GlassStyle.CRYSTAL) },
                                    modifier = Modifier.weight(1f)
                                )
                                GlassStyleOption(
                                    label = "Ice",
                                    style = GlassStyle.ICE,
                                    isSelected = settings.glassEffectSettings.style == GlassStyle.ICE,
                                    onClick = { preferences.setGlassStyle(GlassStyle.ICE) },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Blur
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Blur", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${settings.glassEffectSettings.blur.roundToInt()}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.glassEffectSettings.blur,
                                onValueChange = { preferences.setGlassBlur(it) },
                                valueRange = 0f..25f
                            )

                            // Opacity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Opacity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${(settings.glassEffectSettings.opacity * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.glassEffectSettings.opacity,
                                onValueChange = { preferences.setGlassOpacity(it) },
                                valueRange = 0.1f..0.9f
                            )

                            // Tint Color
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tint Color", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(settings.glassEffectSettings.tint))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable { showGlassTintPicker = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Tree Effect Section =====
        SettingsSection(title = "Tree Growth Effect") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Tree Effect", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Replaces dot grid with tree", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = settings.treeEffectSettings.enabled,
                            onCheckedChange = { preferences.setTreeEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.treeEffectSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tree Style", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TreeStyleOption(
                                    label = "Simple",
                                    style = TreeStyle.SIMPLE,
                                    isSelected = settings.treeEffectSettings.style == TreeStyle.SIMPLE,
                                    onClick = { preferences.setTreeStyle(TreeStyle.SIMPLE) },
                                    modifier = Modifier.weight(1f)
                                )
                                TreeStyleOption(
                                    label = "Detailed",
                                    style = TreeStyle.DETAILED,
                                    isSelected = settings.treeEffectSettings.style == TreeStyle.DETAILED,
                                    onClick = { preferences.setTreeStyle(TreeStyle.DETAILED) },
                                    modifier = Modifier.weight(1f)
                                )
                                TreeStyleOption(
                                    label = "Bonsai",
                                    style = TreeStyle.BONSAI,
                                    isSelected = settings.treeEffectSettings.style == TreeStyle.BONSAI,
                                    onClick = { preferences.setTreeStyle(TreeStyle.BONSAI) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TreeStyleOption(
                                    label = "Sakura",
                                    style = TreeStyle.SAKURA,
                                    isSelected = settings.treeEffectSettings.style == TreeStyle.SAKURA,
                                    onClick = { preferences.setTreeStyle(TreeStyle.SAKURA) },
                                    modifier = Modifier.weight(1f)
                                )
                                TreeStyleOption(
                                    label = "Willow",
                                    style = TreeStyle.WILLOW,
                                    isSelected = settings.treeEffectSettings.style == TreeStyle.WILLOW,
                                    onClick = { preferences.setTreeStyle(TreeStyle.WILLOW) },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tree Colors
                            Text("Colors", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(settings.treeEffectSettings.trunkColor))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            .clickable { showTreeTrunkColorPicker = true }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Trunk", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(settings.treeEffectSettings.leafColor))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            .clickable { showTreeLeafColorPicker = true }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Leaf", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(settings.treeEffectSettings.bloomColor))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                            .clickable { showTreeBloomColorPicker = true }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Bloom", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Ground", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Switch(
                                    checked = settings.treeEffectSettings.showGround,
                                    onCheckedChange = { preferences.setTreeShowGround(it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Fluid Effect Section =====
        SettingsSection(title = "Fluid Effect") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Fluid Effect", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Animated background effect", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = settings.fluidEffectSettings.enabled,
                            onCheckedChange = { preferences.setFluidEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.fluidEffectSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Fluid Style", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FluidStyleOption(
                                    label = "Water",
                                    style = FluidStyle.WATER,
                                    isSelected = settings.fluidEffectSettings.style == FluidStyle.WATER,
                                    onClick = { preferences.setFluidStyle(FluidStyle.WATER) },
                                    modifier = Modifier.weight(1f)
                                )
                                FluidStyleOption(
                                    label = "Lava",
                                    style = FluidStyle.LAVA,
                                    isSelected = settings.fluidEffectSettings.style == FluidStyle.LAVA,
                                    onClick = { preferences.setFluidStyle(FluidStyle.LAVA) },
                                    modifier = Modifier.weight(1f)
                                )
                                FluidStyleOption(
                                    label = "Mercury",
                                    style = FluidStyle.MERCURY,
                                    isSelected = settings.fluidEffectSettings.style == FluidStyle.MERCURY,
                                    onClick = { preferences.setFluidStyle(FluidStyle.MERCURY) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FluidStyleOption(
                                    label = "Plasma",
                                    style = FluidStyle.PLASMA,
                                    isSelected = settings.fluidEffectSettings.style == FluidStyle.PLASMA,
                                    onClick = { preferences.setFluidStyle(FluidStyle.PLASMA) },
                                    modifier = Modifier.weight(1f)
                                )
                                FluidStyleOption(
                                    label = "Aurora",
                                    style = FluidStyle.AURORA,
                                    isSelected = settings.fluidEffectSettings.style == FluidStyle.AURORA,
                                    onClick = { preferences.setFluidStyle(FluidStyle.AURORA) },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Flow Speed
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Flow Speed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${(settings.fluidEffectSettings.flowSpeed * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.fluidEffectSettings.flowSpeed,
                                onValueChange = { preferences.setFluidFlowSpeed(it) },
                                valueRange = 0.1f..2f
                            )

                            // Turbulence
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Turbulence", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${(settings.fluidEffectSettings.turbulence * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.fluidEffectSettings.turbulence,
                                onValueChange = { preferences.setFluidTurbulence(it) },
                                valueRange = 0.1f..1f
                            )

                            // Color Intensity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Color Intensity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${(settings.fluidEffectSettings.colorIntensity * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Slider(
                                value = settings.fluidEffectSettings.colorIntensity,
                                onValueChange = { preferences.setFluidColorIntensity(it) },
                                valueRange = 0.1f..1f
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Feature 6: Goal Tracking Section =====
        SettingsSection(title = "Goal Countdown") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Goals", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = settings.goalSettings.enabled,
                            onCheckedChange = { preferences.setGoalsEnabled(it) }
                        )
                    }

                    AnimatedVisibility(
                        visible = settings.goalSettings.enabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Position", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GoalPositionOption(
                                    label = "Top",
                                    position = GoalPosition.TOP,
                                    isSelected = settings.goalSettings.position == GoalPosition.TOP,
                                    onClick = { preferences.setGoalsPosition(GoalPosition.TOP) },
                                    modifier = Modifier.weight(1f)
                                )
                                GoalPositionOption(
                                    label = "Bottom",
                                    position = GoalPosition.BOTTOM,
                                    isSelected = settings.goalSettings.position == GoalPosition.BOTTOM,
                                    onClick = { preferences.setGoalsPosition(GoalPosition.BOTTOM) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Goals list
                            if (settings.goalSettings.goals.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    settings.goalSettings.goals.forEach { goal ->
                                        GoalItem(
                                            goal = goal,
                                            onEdit = {
                                                editingGoal = goal
                                                showGoalEditor = true
                                            },
                                            onDelete = { preferences.deleteGoal(goal.id) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Button(
                                onClick = {
                                    editingGoal = null
                                    showGoalEditor = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add Goal")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Color Picker Dialogs
    if (showBgColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.backgroundColor,
            title = "Background Color",
            onColorSelected = {
                preferences.setCustomBackgroundColor(it)
                showBgColorPicker = false
            },
            onDismiss = { showBgColorPicker = false }
        )
    }

    if (showFilledColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.filledDotColor,
            title = "Filled Dots Color",
            onColorSelected = {
                preferences.setCustomFilledDotColor(it)
                showFilledColorPicker = false
            },
            onDismiss = { showFilledColorPicker = false }
        )
    }

    if (showEmptyColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.emptyDotColor,
            title = "Empty Dots Color",
            onColorSelected = {
                preferences.setCustomEmptyDotColor(it)
                showEmptyColorPicker = false
            },
            onDismiss = { showEmptyColorPicker = false }
        )
    }

    if (showTodayColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.todayDotColor,
            title = "Today's Dot Color",
            onColorSelected = {
                preferences.setCustomTodayDotColor(it)
                showTodayColorPicker = false
            },
            onDismiss = { showTodayColorPicker = false }
        )
    }

    // Footer color picker
    if (showFooterColorPicker) {
        ColorPickerDialog(
            initialColor = settings.footerTextSettings.color,
            title = "Footer Text Color",
            onColorSelected = {
                preferences.setFooterColor(it)
                showFooterColorPicker = false
            },
            onDismiss = { showFooterColorPicker = false }
        )
    }

    // Progress color picker
    if (showProgressColorPicker) {
        ColorPickerDialog(
            initialColor = settings.progressSettings.color,
            title = "Progress Text Color",
            onColorSelected = {
                preferences.setProgressColor(it)
                showProgressColorPicker = false
            },
            onDismiss = { showProgressColorPicker = false }
        )
    }

    // Goal editor dialog
    if (showGoalEditor) {
        GoalEditorDialog(
            goal = editingGoal,
            onSave = { goal ->
                if (editingGoal != null) {
                    preferences.updateGoal(goal)
                } else {
                    preferences.addGoal(goal)
                }
            },
            onDelete = editingGoal?.let { { preferences.deleteGoal(it.id) } },
            onDismiss = {
                showGoalEditor = false
                editingGoal = null
            }
        )
    }

    // Glass tint color picker
    if (showGlassTintPicker) {
        ColorPickerDialog(
            initialColor = settings.glassEffectSettings.tint,
            title = "Glass Tint Color",
            onColorSelected = {
                preferences.setGlassTint(it)
                showGlassTintPicker = false
            },
            onDismiss = { showGlassTintPicker = false }
        )
    }

    // Tree trunk color picker
    if (showTreeTrunkColorPicker) {
        ColorPickerDialog(
            initialColor = settings.treeEffectSettings.trunkColor,
            title = "Trunk Color",
            onColorSelected = {
                preferences.setTreeTrunkColor(it)
                showTreeTrunkColorPicker = false
            },
            onDismiss = { showTreeTrunkColorPicker = false }
        )
    }

    // Tree leaf color picker
    if (showTreeLeafColorPicker) {
        ColorPickerDialog(
            initialColor = settings.treeEffectSettings.leafColor,
            title = "Leaf Color",
            onColorSelected = {
                preferences.setTreeLeafColor(it)
                showTreeLeafColorPicker = false
            },
            onDismiss = { showTreeLeafColorPicker = false }
        )
    }

    // Tree bloom color picker
    if (showTreeBloomColorPicker) {
        ColorPickerDialog(
            initialColor = settings.treeEffectSettings.bloomColor,
            title = "Bloom Color",
            onColorSelected = {
                preferences.setTreeBloomColor(it)
                showTreeBloomColorPicker = false
            },
            onDismiss = { showTreeBloomColorPicker = false }
        )
    }

    // Birth date picker dialog
    if (showBirthDatePicker) {
        DatePickerDialog(
            initialDate = settings.lifeSettings.birthDate,
            onDateSelected = {
                preferences.setLifeBirthDate(it)
                showBirthDatePicker = false
            },
            onDismiss = { showBirthDatePicker = false }
        )
    }

    // Custom year start date picker dialog
    if (showCustomYearStartDatePicker) {
        DatePickerDialog(
            initialDate = settings.customYearSettings.startDate,
            onDateSelected = {
                preferences.setCustomYearStartDate(it)
                showCustomYearStartDatePicker = false
            },
            onDismiss = { showCustomYearStartDatePicker = false }
        )
    }

    // Custom year end date picker dialog
    if (showCustomYearEndDatePicker) {
        DatePickerDialog(
            initialDate = settings.customYearSettings.endDate,
            onDateSelected = {
                preferences.setCustomYearEndDate(it)
                showCustomYearEndDatePicker = false
            },
            onDismiss = { showCustomYearEndDatePicker = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
fun ThemeOptionButton(
    label: String,
    backgroundColor: Color,
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DotShapeOption(
    shape: DotShape,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val dotColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    when (shape) {
                        DotShape.CIRCLE -> {
                            drawCircle(color = dotColor)
                        }
                        DotShape.SQUARE -> {
                            drawRect(color = dotColor)
                        }
                        DotShape.ROUNDED_SQUARE -> {
                            drawRoundRect(
                                color = dotColor,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                            )
                        }
                        DotShape.DIAMOND -> {
                            val path = Path().apply {
                                moveTo(size.width / 2, 0f)
                                lineTo(size.width, size.height / 2)
                                lineTo(size.width / 2, size.height)
                                lineTo(0f, size.height / 2)
                                close()
                            }
                            drawPath(path, dotColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DotSizeOption(
    label: String,
    dotSize: Dp,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GridDensityOption(
    label: String,
    columns: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(columns) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DotStyleOption(
    label: String,
    style: DotStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val dotColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    when (style) {
                        DotStyle.FLAT -> {
                            drawCircle(color = dotColor)
                        }
                        DotStyle.GRADIENT -> {
                            drawCircle(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(dotColor.copy(alpha = 0.5f), dotColor),
                                    center = androidx.compose.ui.geometry.Offset(
                                        size.width * 0.3f,
                                        size.height * 0.3f
                                    )
                                )
                            )
                        }
                        DotStyle.OUTLINED -> {
                            drawCircle(
                                color = dotColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                        DotStyle.SOFT_GLOW -> {
                            drawCircle(color = dotColor.copy(alpha = 0.3f), radius = size.minDimension / 2 * 1.3f)
                            drawCircle(color = dotColor)
                        }
                        DotStyle.NEON -> {
                            drawCircle(color = dotColor.copy(alpha = 0.2f), radius = size.minDimension / 2 * 1.5f)
                            drawCircle(color = dotColor.copy(alpha = 0.4f), radius = size.minDimension / 2 * 1.2f)
                            drawCircle(color = dotColor)
                        }
                        DotStyle.EMBOSSED -> {
                            drawCircle(color = dotColor.copy(alpha = 0.5f), center = androidx.compose.ui.geometry.Offset(size.width / 2 + 2, size.height / 2 + 2))
                            drawCircle(color = dotColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ViewModeOption(
    label: String,
    mode: ViewMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                when (mode) {
                    ViewMode.CONTINUOUS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(4) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(5) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ViewMode.MONTHLY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(2) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(2.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(4) {
                                            Box(
                                                modifier = Modifier
                                                    .size(3.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ViewMode.CALENDAR -> {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CalendarColumnsOption(
    label: String,
    columns: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = 12 / columns
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(columns) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TextAlignmentOption(
    label: String,
    alignment: TextAlignment,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp, 20.dp),
                contentAlignment = when (alignment) {
                    TextAlignment.LEFT -> Alignment.CenterStart
                    TextAlignment.CENTER -> Alignment.Center
                    TextAlignment.RIGHT -> Alignment.CenterEnd
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GoalPositionOption(
    label: String,
    position: GoalPosition,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp, 40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                contentAlignment = if (position == GoalPosition.TOP) Alignment.TopCenter else Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .width(20.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GoalItem(
    goal: Goal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val daysRemaining = remember(goal.targetDate) {
        goal.calculateDaysRemaining()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(goal.color))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        daysRemaining > 1 -> "$daysRemaining days left"
                        daysRemaining == 1 -> "1 day left"
                        daysRemaining == 0 -> "Today!"
                        daysRemaining == -1 -> "1 day ago"
                        else -> "${-daysRemaining} days ago"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = dateFormatter.format(Date(goal.targetDate)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun AnimationTypeOption(
    label: String,
    type: AnimationType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animation preview icons
                when (type) {
                    AnimationType.FADE_IN -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                        }
                    }
                    AnimationType.PULSE -> {
                        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                    }
                    AnimationType.WAVE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                            }
                        }
                    }
                    AnimationType.BREATHE -> {
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                    }
                    AnimationType.RIPPLE -> {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                    }
                    AnimationType.CASCADE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(3) { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    repeat(3) { col ->
                                        val alpha = if (row * 3 + col < 5) 1f else 0.3f
                                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)))
                                    }
                                }
                            }
                        }
                    }
                    AnimationType.NONE -> {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GlassStyleOption(
    label: String,
    style: GlassStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (style) {
                            GlassStyle.LIGHT_FROST -> Color.White.copy(alpha = 0.4f)
                            GlassStyle.HEAVY_FROST -> Color.White.copy(alpha = 0.7f)
                            GlassStyle.ACRYLIC -> Color(0xFFE0E8F0).copy(alpha = 0.6f)
                            GlassStyle.CRYSTAL -> Color(0xFFE8F4FF).copy(alpha = 0.5f)
                            GlassStyle.ICE -> Color(0xFFD0E8FF).copy(alpha = 0.6f)
                            GlassStyle.NONE -> Color.Transparent
                        }
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TreeStyleOption(
    label: String,
    style: TreeStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.size(32.dp)) {
                    val trunkColor = Color(0xFF8B4513)
                    val leafColor = when (style) {
                        TreeStyle.SAKURA -> Color(0xFFFF69B4)
                        else -> Color(0xFF228B22)
                    }

                    // Draw trunk
                    drawRect(
                        color = trunkColor,
                        topLeft = androidx.compose.ui.geometry.Offset(size.width / 2 - 2, size.height * 0.5f),
                        size = androidx.compose.ui.geometry.Size(4f, size.height * 0.5f)
                    )

                    // Draw foliage based on style
                    when (style) {
                        TreeStyle.SIMPLE -> {
                            val path = Path().apply {
                                moveTo(size.width / 2, size.height * 0.1f)
                                lineTo(size.width * 0.2f, size.height * 0.5f)
                                lineTo(size.width * 0.8f, size.height * 0.5f)
                                close()
                            }
                            drawPath(path, leafColor)
                        }
                        TreeStyle.DETAILED, TreeStyle.WILLOW -> {
                            drawCircle(leafColor, radius = size.width * 0.35f, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.3f))
                        }
                        TreeStyle.BONSAI -> {
                            drawOval(leafColor, topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.15f), size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.35f))
                        }
                        TreeStyle.SAKURA -> {
                            drawCircle(leafColor, radius = size.width * 0.3f, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.3f))
                            // Add small dots for blossoms
                            for (i in 0 until 5) {
                                drawCircle(Color.White.copy(alpha = 0.8f), radius = 2f, center = androidx.compose.ui.geometry.Offset(size.width * (0.3f + i * 0.1f), size.height * (0.2f + (i % 2) * 0.15f)))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FluidStyleOption(
    label: String,
    style: FluidStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (style) {
                            FluidStyle.WATER -> Color(0xFF4488CC)
                            FluidStyle.LAVA -> Color(0xFFFF6600)
                            FluidStyle.MERCURY -> Color(0xFFB8B8C8)
                            FluidStyle.PLASMA -> Color(0xFF8844FF)
                            FluidStyle.AURORA -> Color(0xFF44FF88)
                            FluidStyle.NONE -> Color.Transparent
                        }
                    )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProgressPositionOption(
    label: String,
    position: ProgressPosition,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp, 40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                contentAlignment = if (position == ProgressPosition.TOP) Alignment.TopCenter else Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .width(20.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DecimalPlacesOption(
    label: String,
    places: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

