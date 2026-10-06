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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.R
import com.example.lifedots.preferences.*
import com.example.lifedots.ui.components.ColorButton
import kotlin.math.roundToInt

private val SectionGap = 24.dp

// region Entry point

@Composable
fun AppearanceSettingsSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onShowBgColorPicker: () -> Unit,
    onShowFilledColorPicker: () -> Unit,
    onShowEmptyColorPicker: () -> Unit,
    onShowTodayColorPicker: () -> Unit
) {
    // One root Column so the parent treats this whole section as a single child.
    Column {
        ThemeSection(
            preferences = preferences,
            settings = settings,
            onShowBgColorPicker = onShowBgColorPicker,
            onShowFilledColorPicker = onShowFilledColorPicker,
            onShowEmptyColorPicker = onShowEmptyColorPicker,
            onShowTodayColorPicker = onShowTodayColorPicker
        )
        Gap(SectionGap)
        DotShapeSection(preferences, settings)
        Gap(SectionGap)
        DotSizeSection(preferences, settings)
        Gap(SectionGap)
        GridDensitySection(preferences, settings)
        Gap(SectionGap)
        TransparencySection(preferences, settings)
        Gap(SectionGap)
        HighlightTodayCard(preferences, settings)
        Gap(SectionGap)
        DotStyleSection(preferences, settings)
        Gap(SectionGap)
        ViewModeSection(preferences, settings)
    }
}

// endregion

// region Theme

private class ThemeChoice(
    val theme: ThemeOption,
    val label: String,
    val backgroundColor: Color,
    val dotColor: Color
)

@Composable
private fun ThemeSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onShowBgColorPicker: () -> Unit,
    onShowFilledColorPicker: () -> Unit,
    onShowEmptyColorPicker: () -> Unit,
    onShowTodayColorPicker: () -> Unit
) {
    val choices = listOf(
        ThemeChoice(
            ThemeOption.LIGHT, stringResource(R.string.theme_light),
            Color(0xFFF5F5F5), Color(0xFF2C2C2C)
        ),
        ThemeChoice(
            ThemeOption.DARK, stringResource(R.string.theme_dark),
            Color(0xFF1A1A1A), Color(0xFFE0E0E0)
        ),
        ThemeChoice(
            ThemeOption.AMOLED, stringResource(R.string.theme_amoled),
            Color(0xFF000000), Color(0xFFFFFFFF)
        ),
        ThemeChoice(
            ThemeOption.CUSTOM, "Custom",
            Color(settings.customColors.backgroundColor), Color(settings.customColors.filledDotColor)
        )
    )

    SettingsSection(title = stringResource(R.string.theme_section)) {
        OptionRow {
            choices.forEach { choice ->
                ThemeOptionButton(
                    label = choice.label,
                    backgroundColor = choice.backgroundColor,
                    dotColor = choice.dotColor,
                    isSelected = settings.theme == choice.theme,
                    onClick = { preferences.setTheme(choice.theme) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Gap(8.dp)
    ThemeOptionButton(
        label = stringResource(R.string.theme_liquid_glass),
        backgroundColor = Color(0xFF193953),
        dotColor = Color(0xFF83F4DC),
        isSelected = settings.theme == ThemeOption.LIQUID_GLASS,
        onClick = { preferences.setTheme(ThemeOption.LIQUID_GLASS) },
        modifier = Modifier.fillMaxWidth()
    )

    // Custom colors, only visible when the Custom theme is selected
    Expandable(visible = settings.theme == ThemeOption.CUSTOM) {
        Gap(16.dp)
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

// endregion

// region Dot shape, size and grid

@Composable
private fun DotShapeSection(preferences: LifeDotsPreferences, settings: WallpaperSettings) {
    val choices = listOf(
        DotShape.CIRCLE to "Circle",
        DotShape.SQUARE to "Square",
        DotShape.ROUNDED_SQUARE to "Rounded",
        DotShape.DIAMOND to "Diamond"
    )

    SettingsSection(title = "Dot Shape") {
        OptionRow {
            choices.forEach { (shape, label) ->
                DotShapeOption(
                    shape = shape,
                    label = label,
                    isSelected = settings.dotShape == shape,
                    onClick = { preferences.setDotShape(shape) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private class DotSizeChoice(val size: DotSize, val label: String, val preview: Dp)

@Composable
private fun DotSizeSection(preferences: LifeDotsPreferences, settings: WallpaperSettings) {
    val choices = listOf(
        DotSizeChoice(DotSize.TINY, "Tiny", 6.dp),
        DotSizeChoice(DotSize.SMALL, stringResource(R.string.dot_size_small), 8.dp),
        DotSizeChoice(DotSize.MEDIUM, stringResource(R.string.dot_size_medium), 12.dp),
        DotSizeChoice(DotSize.LARGE, stringResource(R.string.dot_size_large), 16.dp),
        DotSizeChoice(DotSize.HUGE, "Huge", 20.dp)
    )

    SettingsSection(title = stringResource(R.string.dot_size_section)) {
        OptionRow {
            choices.forEach { choice ->
                DotSizeOption(
                    label = choice.label,
                    dotSize = choice.preview,
                    isSelected = settings.dotSize == choice.size,
                    onClick = { preferences.setDotSize(choice.size) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private class GridDensityChoice(val density: GridDensity, val label: String, val columns: Int)

@Composable
private fun GridDensitySection(preferences: LifeDotsPreferences, settings: WallpaperSettings) {
    val choices = listOf(
        GridDensityChoice(GridDensity.COMPACT, stringResource(R.string.grid_compact), 6),
        GridDensityChoice(GridDensity.NORMAL, "Normal", 5),
        GridDensityChoice(GridDensity.RELAXED, stringResource(R.string.grid_relaxed), 4),
        GridDensityChoice(GridDensity.SPACIOUS, "Spacious", 3)
    )

    SettingsSection(title = stringResource(R.string.grid_density_section)) {
        OptionRow {
            choices.forEach { choice ->
                GridDensityOption(
                    label = choice.label,
                    columns = choice.columns,
                    isSelected = settings.gridDensity == choice.density,
                    onClick = { preferences.setGridDensity(choice.density) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// endregion

// region Transparency and highlight

@Composable
private fun TransparencySection(preferences: LifeDotsPreferences, settings: WallpaperSettings) {
    SettingsSection(title = "Transparency") {
        SettingsCard {
            LabeledSlider(
                label = "Filled Dots",
                valueText = "${(settings.filledDotAlpha * 100).roundToInt()}%",
                value = settings.filledDotAlpha,
                valueRange = 0.1f..1f,
                onValueChange = { preferences.setFilledDotAlpha(it) }
            )
            Gap(8.dp)
            LabeledSlider(
                label = "Empty Dots",
                valueText = "${(settings.emptyDotAlpha * 100).roundToInt()}%",
                value = settings.emptyDotAlpha,
                valueRange = 0.1f..1f,
                onValueChange = { preferences.setEmptyDotAlpha(it) }
            )
        }
    }
}

@Composable
private fun HighlightTodayCard(preferences: LifeDotsPreferences, settings: WallpaperSettings) {
    SettingsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
}

// endregion

// region Dot style

private class DotStyleChoice(val style: DotStyle, val label: String)

private val DotStyleChoices = listOf(
    DotStyleChoice(DotStyle.FLAT, "Flat"),
    DotStyleChoice(DotStyle.GRADIENT, "Gradient"),
    DotStyleChoice(DotStyle.OUTLINED, "Outlined"),
    DotStyleChoice(DotStyle.SOFT_GLOW, "Glow"),
    DotStyleChoice(DotStyle.NEON, "Neon"),
    DotStyleChoice(DotStyle.EMBOSSED, "Embossed")
)

private const val DOT_STYLES_PER_ROW = 3
private val GlowStyles = setOf(DotStyle.SOFT_GLOW, DotStyle.NEON)

@Composable
private fun DotStyleSection(preferences: LifeDotsPreferences, settings: WallpaperSettings) {
    val currentStyle = settings.dotEffectSettings.style

    SettingsSection(title = "Dot Style") {
        DotStyleChoices.chunked(DOT_STYLES_PER_ROW).forEachIndexed { index, rowChoices ->
            if (index > 0) Gap(8.dp)
            OptionRow {
                rowChoices.forEach { choice ->
                    DotStyleOption(
                        label = choice.label,
                        style = choice.style,
                        isSelected = currentStyle == choice.style,
                        onClick = { preferences.setDotStyle(choice.style) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Extra controls that only apply to some styles
    ExpandableCard(visible = currentStyle in GlowStyles) {
        LabeledSlider(
            label = "Glow Radius",
            valueText = "${settings.dotEffectSettings.glowRadius.roundToInt()}",
            value = settings.dotEffectSettings.glowRadius,
            valueRange = 2f..20f,
            onValueChange = { preferences.setGlowRadius(it) }
        )
    }

    ExpandableCard(visible = currentStyle == DotStyle.OUTLINED) {
        LabeledSlider(
            label = "Outline Width",
            valueText = "${settings.dotEffectSettings.outlineWidth.roundToInt()}",
            value = settings.dotEffectSettings.outlineWidth,
            valueRange = 1f..5f,
            onValueChange = { preferences.setOutlineWidth(it) }
        )
    }
}

// endregion

// region View mode

private class ViewModeChoice(val mode: ViewMode, val label: String)

private val ViewModeChoices = listOf(
    ViewModeChoice(ViewMode.CONTINUOUS, "Continuous"),
    ViewModeChoice(ViewMode.MONTHLY, "Monthly"),
    ViewModeChoice(ViewMode.CALENDAR, "Calendar")
)

private class CalendarColumnsChoice(val label: String, val columns: Int)

private val CalendarColumnsChoices = listOf(
    CalendarColumnsChoice("3x4", 3),
    CalendarColumnsChoice("4x3", 4)
)

@Composable
private fun ViewModeSection(preferences: LifeDotsPreferences, settings: WallpaperSettings) {
    val currentMode = settings.viewModeSettings.mode

    SettingsSection(title = "View Mode") {
        OptionRow {
            ViewModeChoices.forEach { choice ->
                ViewModeOption(
                    label = choice.label,
                    mode = choice.mode,
                    isSelected = currentMode == choice.mode,
                    onClick = { preferences.setViewMode(choice.mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    ExpandableCard(visible = currentMode != ViewMode.CONTINUOUS) {
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

    ExpandableCard(visible = currentMode == ViewMode.CALENDAR) {
        Text("Months Per Row", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Gap(12.dp)
        OptionRow {
            CalendarColumnsChoices.forEach { choice ->
                CalendarColumnsOption(
                    label = choice.label,
                    columns = choice.columns,
                    isSelected = settings.calendarViewSettings.columnsPerRow == choice.columns,
                    onClick = { preferences.setCalendarColumns(choice.columns) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// endregion