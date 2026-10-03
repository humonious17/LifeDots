package com.example.lifedots

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.LifeProgress
import com.example.lifedots.preferences.TimeScale
import com.example.lifedots.ui.theme.LifeDotsTheme
import com.example.lifedots.ui.theme.lifeDotsBackground
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.wallpaper.LifeDotsWallpaperService
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var preferences: LifeDotsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = LifeDotsPreferences.getInstance(this)

        setContent {
            val appearance by preferences.settingsFlow.collectAsState()
            LifeDotsTheme(liquidGlass = appearance.theme == ThemeOption.LIQUID_GLASS) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnboardingScreen(
                        preferences = preferences,
                        onSetWallpaper = { openWallpaperPicker() },
                        onOpenSettings = { openSettings() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun openWallpaperPicker() {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@MainActivity, LifeDotsWallpaperService::class.java)
            )
        }
        startActivity(intent)
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}

@Composable
fun OnboardingScreen(
    preferences: LifeDotsPreferences,
    onSetWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settingsFlow.collectAsState()
    val isLifeMode = settings.timeScale == TimeScale.LIFE
    val customYearProgress = remember(settings.customYearSettings) {
        if (settings.customYearSettings.enabled) {
            settings.customYearSettings.calculateProgress()
        } else null
    }
    val calendar = remember { Calendar.getInstance() }
    val dayOfYear = customYearProgress?.dayIndex ?: calendar.get(Calendar.DAY_OF_YEAR)
    val totalDays = customYearProgress?.totalDays ?: calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
    val lifeProgress = remember(settings.lifeSettings) { settings.lifeSettings.calculateProgress() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .lifeDotsBackground()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Preview dots visualization
        if (isLifeMode) {
            LifeDotsPreview(
                progress = lifeProgress,
                lifeExpectancyYears = settings.lifeSettings.lifeExpectancyYears,
                splitHalves = settings.lifeSettings.splitHalves,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(horizontal = 8.dp)
            )
        } else {
            DotsPreview(
                dayOfYear = dayOfYear,
                totalDays = totalDays,
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = if (isLifeMode) "Memento Mori" else stringResource(R.string.onboarding_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = if (isLifeMode) "Your Entire Life in Weeks" else stringResource(R.string.onboarding_subtitle),
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        val descriptionText = when {
            isLifeMode -> "Every dot represents one week of your life. Filled dots are weeks passed, the highlighted dot is this week, and empty dots are weeks remaining."
            settings.customYearSettings.enabled -> "$totalDays dots. One fills each day. A quiet reminder that time is moving forward."
            else -> stringResource(R.string.onboarding_description)
        }

        // Description
        Text(
            text = descriptionText,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Days / Weeks counter with percentage
        if (isLifeMode) {
            Text(
                text = "${String.format(Locale.getDefault(), "%.1f%%", lifeProgress.percentLived)} of life lived",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${lifeProgress.weeksLived} weeks lived • ${lifeProgress.weeksRemaining} weeks remaining",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        } else {
            val percentage = (dayOfYear.toFloat() / totalDays.toFloat()) * 100f
            val remainingDays = totalDays - dayOfYear

            Text(
                text = stringResource(R.string.year_percentage, String.format(Locale.getDefault(), "%.1f%%", percentage)),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.days_passed, dayOfYear)} • ${stringResource(R.string.days_remaining, remainingDays)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Buttons
        Button(
            onClick = onSetWallpaper,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.set_wallpaper),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.open_settings),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LifeDotsPreview(
    progress: LifeProgress,
    lifeExpectancyYears: Int,
    splitHalves: Boolean,
    modifier: Modifier = Modifier
) {
    val filledColor = MaterialTheme.colorScheme.onBackground
    val emptyColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)
    val todayColor = Color(0xFF4A90D9)

    Canvas(modifier = modifier) {
        val rows = lifeExpectancyYears
        val cols = 52
        val gapRatio = if (splitHalves) 1.2f else 0f
        val totalCols = cols.toFloat() + gapRatio

        val cellWidth = size.width / totalCols
        val cellHeight = size.height / rows.toFloat()
        val cellSize = minOf(cellWidth, cellHeight)
        val gapWidth = if (splitHalves) cellSize * gapRatio else 0f
        val dotRadius = (cellSize / 2f) * 0.75f

        val totalGridWidth = (cols * cellSize) + gapWidth
        val totalGridHeight = rows * cellSize
        val startX = (size.width - totalGridWidth) / 2f
        val startY = (size.height - totalGridHeight) / 2f

        for (r in 0 until rows) {
            val cy = startY + (r * cellSize) + (cellSize / 2f)
            for (c in 0 until cols) {
                val dotIndex = r * 52 + c
                val colOffset = if (splitHalves && c >= 26) gapWidth else 0f
                val cx = startX + (c * cellSize) + colOffset + (cellSize / 2f)

                val color = when {
                    dotIndex == progress.currentDotIndex -> todayColor
                    dotIndex < progress.currentDotIndex -> filledColor
                    else -> emptyColor
                }

                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

@Composable
fun DotsPreview(
    dayOfYear: Int,
    totalDays: Int,
    modifier: Modifier = Modifier
) {
    val filledColor = MaterialTheme.colorScheme.onBackground
    val emptyColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
    val todayColor = Color(0xFF4A90D9)

    Canvas(modifier = modifier) {
        val cols = 15
        val rows = (totalDays + cols - 1) / cols

        val cellSize = minOf(size.width / cols, size.height / rows)
        val dotRadius = cellSize * 0.35f

        val gridWidth = cols * cellSize
        val gridHeight = rows * cellSize
        val startX = (size.width - gridWidth) / 2
        val startY = (size.height - gridHeight) / 2

        var dotIndex = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (dotIndex >= totalDays) break

                val cx = startX + col * cellSize + cellSize / 2
                val cy = startY + row * cellSize + cellSize / 2

                val color = when {
                    dotIndex + 1 == dayOfYear -> todayColor
                    dotIndex + 1 < dayOfYear -> filledColor
                    else -> emptyColor
                }

                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(cx, cy)
                )
                dotIndex++
            }
            if (dotIndex >= totalDays) break
        }
    }
}
