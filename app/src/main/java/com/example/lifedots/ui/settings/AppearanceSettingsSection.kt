package com.example.lifedots.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.R
import com.example.lifedots.preferences.*
import com.example.lifedots.ui.components.ColorButton
import kotlin.math.roundToInt

@Composable
fun AppearanceSettingsSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onShowBgColorPicker: () -> Unit,
    onShowFilledColorPicker: () -> Unit,
    onShowEmptyColorPicker: () -> Unit,
    onShowTodayColorPicker: () -> Unit
) {
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
                        onClick = onShowBgColorPicker
                    )
                    ColorButton(
                        color = settings.customColors.filledDotColor,
                        label = "Filled Dots",
                        onClick = onShowFilledColorPicker
                    )
                    ColorButton(
                        color = settings.customColors.emptyDotColor,
                        label = "Empty Dots",
                        onClick = onShowEmptyColorPicker
                    )
                    ColorButton(
                        color = settings.customColors.todayDotColor,
                        label = "Today's Dot",
                        onClick = onShowTodayColorPicker
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

    // ===== Dot Style Section =====
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

    // ===== View Mode Section =====
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
}
