package com.example.lifedots.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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

@Composable
fun ContentSettingsSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    hasImagePermission: Boolean,
    onSelectImage: () -> Unit,
    onShowFooterColorPicker: () -> Unit,
    onShowProgressColorPicker: () -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (Goal) -> Unit
) {
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
                                    .clickable { onShowFooterColorPicker() }
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
                                    .clickable { onShowProgressColorPicker() }
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
                            onClick = onSelectImage,
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

    // ===== Feature 6: Goal Countdown Section =====
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
                                        onEdit = { onEditGoal(goal) },
                                        onDelete = { preferences.deleteGoal(goal.id) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
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
        }
    }
}
