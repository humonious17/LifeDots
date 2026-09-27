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
import com.example.lifedots.ui.theme.LifeDotsTheme
import com.example.lifedots.wallpaper.LifeDotsWallpaperService
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeDotsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnboardingScreen(
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
    onSetWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val totalDays = remember { Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Preview dots visualization
        DotsPreview(
            dayOfYear = dayOfYear,
            totalDays = 365,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = stringResource(R.string.onboarding_title),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = stringResource(R.string.onboarding_description),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Days counter with percentage
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

        Spacer(modifier = Modifier.height(48.dp))

        // Buttons
        Button(
            onClick = onSetWallpaper,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
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
                .height(56.dp),
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
