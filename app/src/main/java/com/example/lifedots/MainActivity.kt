package com.example.lifedots

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.LifeProgress
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.preferences.TimeScale
import com.example.lifedots.ui.theme.LifeDotsTheme
import com.example.lifedots.ui.theme.lifeDotsBackground
import com.example.lifedots.wallpaper.LifeDotsWallpaperService
import java.util.Calendar
import java.util.Locale

// region Activity

class MainActivity : ComponentActivity() {

    private lateinit var preferences: LifeDotsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = LifeDotsPreferences.getInstance(this)

        setContent {
            val settings by preferences.settingsFlow.collectAsState()
            LifeDotsTheme(liquidGlass = settings.theme == ThemeOption.LIQUID_GLASS) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnboardingScreen(
                        preferences = preferences,
                        onSetWallpaper = ::openWallpaperPicker,
                        onOpenSettings = ::openSettings,
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
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (_: ActivityNotFoundException) {
                android.widget.Toast.makeText(
                    this, "This device does not provide a live wallpaper picker.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}

// endregion

// region Constants

private val TodayColor = Color(0xFF4A90D9)

private object LifeGrid {
    const val COLUMNS = 52 // one dot per week
    const val HALF_YEAR_COLUMN = 26
    const val GAP_RATIO = 1.2f
    const val DOT_SCALE = 0.75f
    const val EMPTY_ALPHA = 0.18f
}

private object YearGrid {
    const val COLUMNS = 15
    const val DOT_RADIUS_RATIO = 0.35f
    const val EMPTY_ALPHA = 0.15f
}

private object Copy {
    const val LIFE_TITLE = "Memento Mori"
    const val LIFE_SUBTITLE = "Your Entire Life in Weeks"
    const val LIFE_DESCRIPTION =
        "Every dot represents one week of your life. Filled dots are weeks passed, " +
            "the highlighted dot is this week, and empty dots are weeks remaining."

    fun customYearDescription(totalDays: Int) =
        "$totalDays dots. One fills each day. A quiet reminder that time is moving forward."
}

// endregion

// region UI state

private sealed interface OnboardingUiState {

    data class Life(
        val progress: LifeProgress,
        val lifeExpectancyYears: Int,
        val splitHalves: Boolean
    ) : OnboardingUiState

    data class Year(
        val dayOfYear: Int,
        val totalDays: Int,
        val isCustomYear: Boolean
    ) : OnboardingUiState {
        val percentage: Float get() = dayOfYear.toFloat() / totalDays.toFloat() * 100f
        val remainingDays: Int get() = totalDays - dayOfYear
    }
}

// endregion

// region Screen

@Composable
fun OnboardingScreen(
    preferences: LifeDotsPreferences,
    onSetWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settingsFlow.collectAsState()

    val uiState = remember(settings.timeScale, settings.lifeSettings, settings.customYearSettings) {
        if (settings.timeScale == TimeScale.LIFE) {
            OnboardingUiState.Life(
                progress = settings.lifeSettings.calculateProgress(),
                lifeExpectancyYears = settings.lifeSettings.lifeExpectancyYears,
                splitHalves = settings.lifeSettings.splitHalves
            )
        } else {
            val custom = settings.customYearSettings
                .takeIf { it.enabled }
                ?.calculateProgress()
            val calendar = Calendar.getInstance()
            OnboardingUiState.Year(
                dayOfYear = custom?.dayIndex ?: calendar.get(Calendar.DAY_OF_YEAR),
                totalDays = custom?.totalDays ?: calendar.getActualMaximum(Calendar.DAY_OF_YEAR),
                isCustomYear = custom != null
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .lifeDotsBackground()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PreviewSection(uiState)
        Gap(32.dp)
        HeaderSection(uiState)
        Gap(16.dp)
        DescriptionSection(uiState)
        Gap(16.dp)
        ProgressSummary(uiState)
        Gap(36.dp)
        ActionButtons(onSetWallpaper = onSetWallpaper, onOpenSettings = onOpenSettings)
    }
}

// endregion

// region Sections

@Composable
private fun PreviewSection(state: OnboardingUiState) {
    when (state) {
        is OnboardingUiState.Life -> LifeDotsPreview(
            progress = state.progress,
            lifeExpectancyYears = state.lifeExpectancyYears,
            splitHalves = state.splitHalves,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 8.dp)
        )
        is OnboardingUiState.Year -> DotsPreview(
            dayOfYear = state.dayOfYear,
            totalDays = state.totalDays,
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
private fun HeaderSection(state: OnboardingUiState) {
    val isLife = state is OnboardingUiState.Life

    Text(
        text = if (isLife) Copy.LIFE_TITLE else stringResource(R.string.onboarding_title),
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Gap(8.dp)
    Text(
        text = if (isLife) Copy.LIFE_SUBTITLE else stringResource(R.string.onboarding_subtitle),
        fontSize = 17.sp,
        color = onBackground(alpha = 0.7f)
    )
}

@Composable
private fun DescriptionSection(state: OnboardingUiState) {
    val text = when (state) {
        is OnboardingUiState.Life -> Copy.LIFE_DESCRIPTION
        is OnboardingUiState.Year ->
            if (state.isCustomYear) Copy.customYearDescription(state.totalDays)
            else stringResource(R.string.onboarding_description)
    }

    Text(
        text = text,
        fontSize = 15.sp,
        color = onBackground(alpha = 0.6f),
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
    )
}

@Composable
private fun ProgressSummary(state: OnboardingUiState) {
    val (headline, detail) = when (state) {
        is OnboardingUiState.Life -> Pair(
            "${formatPercent(state.progress.percentLived)} of life lived",
            "${state.progress.weeksLived} weeks lived • ${state.progress.weeksRemaining} weeks remaining"
        )
        is OnboardingUiState.Year -> Pair(
            stringResource(R.string.year_percentage, formatPercent(state.percentage)),
            "${stringResource(R.string.days_passed, state.dayOfYear)} • " +
                stringResource(R.string.days_remaining, state.remainingDays)
        )
    }

    Text(
        text = headline,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
    Gap(4.dp)
    Text(
        text = detail,
        fontSize = 14.sp,
        color = onBackground(alpha = 0.6f)
    )
}

@Composable
private fun ActionButtons(
    onSetWallpaper: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
    val buttonShape = RoundedCornerShape(16.dp)

    Button(
        onClick = onSetWallpaper,
        modifier = buttonModifier,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        ButtonLabel(stringResource(R.string.set_wallpaper))
    }

    Gap(12.dp)

    OutlinedButton(
        onClick = onOpenSettings,
        modifier = buttonModifier,
        shape = buttonShape
    ) {
        ButtonLabel(stringResource(R.string.open_settings))
    }
}

// endregion

// region Dot previews

@Composable
fun LifeDotsPreview(
    progress: LifeProgress,
    lifeExpectancyYears: Int,
    splitHalves: Boolean,
    modifier: Modifier = Modifier
) {
    val filledColor = MaterialTheme.colorScheme.onBackground
    val emptyColor = onBackground(alpha = LifeGrid.EMPTY_ALPHA)

    Canvas(modifier = modifier) {
        val rows = lifeExpectancyYears
        val cols = LifeGrid.COLUMNS
        val gapRatio = if (splitHalves) LifeGrid.GAP_RATIO else 0f

        val cellSize = minOf(size.width / (cols + gapRatio), size.height / rows)
        val gapWidth = cellSize * gapRatio
        val dotRadius = cellSize / 2f * LifeGrid.DOT_SCALE

        val startX = (size.width - (cols * cellSize + gapWidth)) / 2f
        val startY = (size.height - rows * cellSize) / 2f

        for (row in 0 until rows) {
            val cy = startY + row * cellSize + cellSize / 2f
            for (col in 0 until cols) {
                val dotIndex = row * cols + col
                val halfOffset = if (splitHalves && col >= LifeGrid.HALF_YEAR_COLUMN) gapWidth else 0f
                val cx = startX + col * cellSize + halfOffset + cellSize / 2f

                drawCircle(
                    color = dotColor(
                        isToday = dotIndex == progress.currentDotIndex,
                        isPast = dotIndex < progress.currentDotIndex,
                        filled = filledColor,
                        empty = emptyColor
                    ),
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
    val emptyColor = onBackground(alpha = YearGrid.EMPTY_ALPHA)

    Canvas(modifier = modifier) {
        val cols = YearGrid.COLUMNS
        val rows = (totalDays + cols - 1) / cols

        val cellSize = minOf(size.width / cols, size.height / rows)
        val dotRadius = cellSize * YearGrid.DOT_RADIUS_RATIO

        val startX = (size.width - cols * cellSize) / 2
        val startY = (size.height - rows * cellSize) / 2

        for (dotIndex in 0 until totalDays) {
            val row = dotIndex / cols
            val col = dotIndex % cols
            val day = dotIndex + 1

            drawCircle(
                color = dotColor(
                    isToday = day == dayOfYear,
                    isPast = day < dayOfYear,
                    filled = filledColor,
                    empty = emptyColor
                ),
                radius = dotRadius,
                center = Offset(
                    x = startX + col * cellSize + cellSize / 2,
                    y = startY + row * cellSize + cellSize / 2
                )
            )
        }
    }
}

// endregion

// region Helpers

@Composable
private fun Gap(height: Dp) = Spacer(modifier = Modifier.height(height))

@Composable
private fun ButtonLabel(text: String) = Text(
    text = text,
    fontSize = 16.sp,
    fontWeight = FontWeight.Medium
)

@Composable
private fun onBackground(alpha: Float): Color =
    MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)

private fun dotColor(isToday: Boolean, isPast: Boolean, filled: Color, empty: Color): Color =
    when {
        isToday -> TodayColor
        isPast -> filled
        else -> empty
    }

private fun formatPercent(value: Float): String =
    String.format(Locale.getDefault(), "%.1f%%", value)

// endregion