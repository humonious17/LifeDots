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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.lifedots.preferences.DotShape
import com.example.lifedots.preferences.DotSize
import com.example.lifedots.preferences.DotStyle
import com.example.lifedots.preferences.Goal
import com.example.lifedots.preferences.GoalPosition
import com.example.lifedots.preferences.GridDensity
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.TextAlignment
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.preferences.ViewMode
import com.example.lifedots.ui.components.ColorButton
import com.example.lifedots.ui.components.ColorPickerDialog
import com.example.lifedots.ui.components.GoalEditorDialog
import com.example.lifedots.ui.theme.LifeDotsTheme
import java.text.SimpleDateFormat
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
    var showGoalEditor by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }

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

        Spacer(modifier = Modifier.height(32.dp))

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
        val now = System.currentTimeMillis()
        ((goal.targetDate - now) / (1000 * 60 * 60 * 24)).toInt()
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
                        daysRemaining > 0 -> "$daysRemaining days left"
                        daysRemaining == 0 -> "Today!"
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
