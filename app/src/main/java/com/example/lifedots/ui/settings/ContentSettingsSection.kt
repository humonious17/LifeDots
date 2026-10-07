package com.example.lifedots.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.preferences.*
import java.util.Calendar
import kotlin.math.roundToInt

private val SectionGap = 24.dp

// region Entry point

@Composable
fun ContentSettingsSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onSelectImage: () -> Unit,
    onShowFooterColorPicker: () -> Unit,
    onShowProgressColorPicker: () -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (Goal) -> Unit
) {
    // One root Column so the parent treats this whole section as a single child.
    Column {
        FooterTextSection(preferences, settings, onShowFooterColorPicker)
        Gap(SectionGap)
        ProgressSection(preferences, settings, onShowProgressColorPicker)
        Gap(SectionGap)
        BackgroundPhotoSection(preferences, settings, onSelectImage)
        Gap(SectionGap)
        GoalCountdownSection(preferences, settings, onAddGoal, onEditGoal)
    }
}

// endregion

// region Footer text

@Composable
private fun FooterTextSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onShowColorPicker: () -> Unit
) {
    val footer = settings.footerTextSettings

    SettingsSection(title = "Footer Text") {
        ToggleCard(
            label = "Enable Footer Text",
            checked = footer.enabled,
            onCheckedChange = { preferences.setFooterEnabled(it) }
        ) {
            OutlinedTextField(
                value = footer.text,
                onValueChange = { preferences.setFooterText(it) },
                label = { Text("Footer Text") },
                placeholder = { Text("e.g., Make every day count") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Gap(4.dp)
            Text(
                text = "Tip: Use {percent}, {remaining}, {passed}, or {total} for live stats",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Gap(16.dp)

            TextStyleControls(
                fontSize = footer.fontSize,
                onFontSizeChange = { preferences.setFooterFontSize(it) },
                alignment = footer.alignment,
                onAlignmentChange = { preferences.setFooterAlignment(it) },
                color = Color(footer.color),
                onColorClick = onShowColorPicker
            )
        }
    }
}

// endregion

// region Year progress

@Composable
private fun ProgressSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onShowColorPicker: () -> Unit
) {
    val progress = settings.progressSettings

    val decimalChoices = listOf(
        0 to "0 (24%)",
        1 to "1 (24.5%)",
        2 to "2 (24.45%)"
    )
    val positionChoices = listOf(
        ProgressPosition.TOP to "Top",
        ProgressPosition.BOTTOM to "Bottom"
    )

    SettingsSection(title = "Year Progress & Countdown") {
        ToggleCard(
            label = "Show Year Progress",
            subtitle = "Display percentage and remaining days",
            checked = progress.enabled,
            onCheckedChange = { preferences.setProgressEnabled(it) }
        ) {
            ProgressPreview(settings)

            Gap(16.dp)

            Text(
                text = "Display Options",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Gap(8.dp)
            SwitchRow(
                label = "Show Percentage",
                checked = progress.showPercentage,
                onCheckedChange = { preferences.setProgressShowPercentage(it) }
            )
            Gap(8.dp)
            SwitchRow(
                label = "Show Remaining Days",
                checked = progress.showRemainingDays,
                onCheckedChange = { preferences.setProgressShowRemaining(it) }
            )
            Gap(8.dp)
            SwitchRow(
                label = "Show Day Count",
                checked = progress.showDaysPassed,
                onCheckedChange = { preferences.setProgressShowPassed(it) }
            )

            if (progress.showPercentage) {
                Gap(16.dp)
                OptionGroup("Percentage Decimals") {
                    decimalChoices.forEach { (places, label) ->
                        DecimalPlacesOption(
                            label = label,
                            places = places,
                            isSelected = progress.decimalPlaces == places,
                            onClick = { preferences.setProgressDecimalPlaces(places) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Gap(16.dp)

            OptionGroup("Position") {
                positionChoices.forEach { (position, label) ->
                    ProgressPositionOption(
                        label = label,
                        position = position,
                        isSelected = progress.position == position,
                        onClick = { preferences.setProgressPosition(position) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Gap(16.dp)

            TextStyleControls(
                fontSize = progress.fontSize,
                onFontSizeChange = { preferences.setProgressFontSize(it) },
                alignment = progress.alignment,
                onAlignmentChange = { preferences.setProgressAlignment(it) },
                color = Color(progress.color),
                onColorClick = onShowColorPicker
            )
        }
    }
}

/** Shows the progress text the way it will appear, using the current day or custom year. */
@Composable
private fun ProgressPreview(settings: WallpaperSettings) {
    val progress = settings.progressSettings

    val (dayOfYear, totalDays) = remember(settings.customYearSettings) {
        if (settings.customYearSettings.enabled) {
            val prog = settings.customYearSettings.calculateProgress()
            Pair(prog.dayIndex, prog.totalDays)
        } else {
            val calendar = Calendar.getInstance()
            Pair(calendar.get(Calendar.DAY_OF_YEAR), calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        }
    }
    val previewText = remember(progress, dayOfYear, totalDays) {
        progress.formatText(dayOfYear, totalDays)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        contentAlignment = when (progress.alignment) {
            TextAlignment.LEFT -> Alignment.CenterStart
            TextAlignment.CENTER -> Alignment.Center
            TextAlignment.RIGHT -> Alignment.CenterEnd
        }
    ) {
        Text(
            text = previewText,
            fontSize = progress.fontSize.sp,
            fontWeight = FontWeight.Medium,
            color = Color(progress.color)
        )
    }
}

// endregion

// region Background photo

@Composable
private fun BackgroundPhotoSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onSelectImage: () -> Unit
) {
    val background = settings.backgroundSettings
    val hasImage = background.imageUri != null

    SettingsSection(title = "Background Photo") {
        ToggleCard(
            label = "Enable Background",
            checked = background.enabled,
            onCheckedChange = { preferences.setBackgroundEnabled(it) }
        ) {
            OutlinedButton(
                onClick = onSelectImage,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (hasImage) "Change Image" else "Select Image")
            }

            if (hasImage) {
                Gap(16.dp)
                LabeledSlider(
                    label = "Opacity",
                    valueText = "${(background.opacity * 100).roundToInt()}%",
                    value = background.opacity,
                    valueRange = 0.1f..1f,
                    onValueChange = { preferences.setBackgroundOpacity(it) }
                )
                Gap(8.dp)
                LabeledSlider(
                    label = "Blur",
                    valueText = "${background.blurRadius.roundToInt()}",
                    value = background.blurRadius,
                    valueRange = 0f..25f,
                    onValueChange = { preferences.setBackgroundBlur(it) }
                )
            }
        }
    }
}

// endregion

// region Goal countdown

@Composable
private fun GoalCountdownSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onAddGoal: () -> Unit,
    onEditGoal: (Goal) -> Unit
) {
    val goals = settings.goalSettings
    val positionChoices = listOf(
        GoalPosition.TOP to "Top",
        GoalPosition.BOTTOM to "Bottom"
    )

    SettingsSection(title = "Goal Countdown") {
        ToggleCard(
            label = "Enable Goals",
            checked = goals.enabled,
            onCheckedChange = { preferences.setGoalsEnabled(it) }
        ) {
            OptionGroup("Position") {
                positionChoices.forEach { (position, label) ->
                    GoalPositionOption(
                        label = label,
                        position = position,
                        isSelected = goals.position == position,
                        onClick = { preferences.setGoalsPosition(position) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Gap(16.dp)

            if (goals.goals.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.goals.forEach { goal ->
                        GoalItem(
                            goal = goal,
                            onEdit = { onEditGoal(goal) },
                            onDelete = { preferences.deleteGoal(goal.id) }
                        )
                    }
                }
                Gap(12.dp)
            }

            Button(
                onClick = onAddGoal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Goal")
            }
        }
    }
}

// endregion

// region Shared by footer and progress

private val AlignmentChoices = listOf(
    TextAlignment.LEFT to "Left",
    TextAlignment.CENTER to "Center",
    TextAlignment.RIGHT to "Right"
)

/** Font size slider, alignment picker and color swatch, used by both footer and progress text. */
@Composable
private fun TextStyleControls(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    alignment: TextAlignment,
    onAlignmentChange: (TextAlignment) -> Unit,
    color: Color,
    onColorClick: () -> Unit
) {
    LabeledSlider(
        label = "Font Size",
        valueText = "${fontSize.roundToInt()}sp",
        value = fontSize,
        valueRange = 10f..24f,
        onValueChange = onFontSizeChange
    )

    Gap(8.dp)

    OptionGroup("Alignment") {
        AlignmentChoices.forEach { (option, label) ->
            TextAlignmentOption(
                label = label,
                alignment = option,
                isSelected = alignment == option,
                onClick = { onAlignmentChange(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    Gap(12.dp)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Color", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable { onColorClick() }
        )
    }
}

// endregion