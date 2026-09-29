package com.example.lifedots.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.R
import com.example.lifedots.preferences.CustomYearSettings
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.TimeScale
import com.example.lifedots.preferences.WallpaperSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TimeScaleSettingsSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onShowCustomYearStartDatePicker: () -> Unit,
    onShowCustomYearEndDatePicker: () -> Unit,
    onShowBirthDatePicker: () -> Unit
) {
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
                                            progress = { (progress.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
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
                                        onClick = onShowCustomYearStartDatePicker
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
                                        onClick = onShowCustomYearEndDatePicker
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
                                onClick = onShowBirthDatePicker
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
}
