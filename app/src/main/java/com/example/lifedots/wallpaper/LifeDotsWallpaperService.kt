package com.example.lifedots.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.lifedots.preferences.GoalPosition
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.ProgressPosition
import com.example.lifedots.preferences.TimeScale
import com.example.lifedots.preferences.ViewMode
import com.example.lifedots.preferences.WallpaperSettings
import com.example.lifedots.wallpaper.renderer.DotGridRenderer
import com.example.lifedots.wallpaper.renderer.FluidEffectRenderer
import com.example.lifedots.wallpaper.renderer.GlassEffectRenderer
import com.example.lifedots.wallpaper.renderer.OverlayTextRenderer
import com.example.lifedots.wallpaper.renderer.ThemeColors
import com.example.lifedots.wallpaper.renderer.TreeEffectRenderer
import java.util.Calendar

class LifeDotsWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LifeDotsEngine()
    }

    inner class LifeDotsEngine : Engine() {

        private val preferences by lazy { LifeDotsPreferences.getInstance(applicationContext) }
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var lastDrawnDay = -1

        // Specialized Renderers
        private val liquidGlassRenderer = com.example.lifedots.wallpaper.renderer.LiquidGlassRenderer()
        private val glassEffectRenderer = GlassEffectRenderer()
        private val fluidEffectRenderer = FluidEffectRenderer()
        private val treeEffectRenderer = TreeEffectRenderer()
        private val dotGridRenderer = DotGridRenderer()
        private val overlayTextRenderer = OverlayTextRenderer()

        private var animationTime = 0L
        private var fluidPhase = 0f
        private var lastFrameTime = 0L
        private val backgroundRenderer = BackgroundImageRenderer(applicationContext)

        // One loop prevents duplicate draws when both effects are enabled.
        private val frameRunner = object : Runnable {
            override fun run() {
                if (!visible) return
                val settings = preferences.settings
                val now = android.os.SystemClock.uptimeMillis()
                val elapsed = if (lastFrameTime == 0L) 0L else (now - lastFrameTime).coerceAtMost(100L)
                lastFrameTime = now
                animationTime = System.currentTimeMillis()
                if (settings.fluidEffectSettings.enabled) {
                    fluidPhase = (fluidPhase + elapsed / 50f * 0.02f *
                        settings.fluidEffectSettings.flowSpeed) % (2f * Math.PI.toFloat())
                }
                draw()
                if (settings.animationSettings.enabled || settings.fluidEffectSettings.enabled) {
                    handler.postDelayed(this, if (settings.animationSettings.enabled) 16L else 50L)
                }
            }
        }

        private fun restartRendering() {
            handler.removeCallbacks(frameRunner)
            lastFrameTime = 0L
            if (visible) handler.post(frameRunner)
        }

        private val settingsChangeListener: () -> Unit = {
            handler.post { restartRendering() }
        }

        private val dateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                lastDrawnDay = -1
                handler.post { draw() }
                scheduleNextMidnightCheck()
            }
        }

        private val midnightChecker = object : Runnable {
            override fun run() {
                val currentDay = getActiveDayOfYear(preferences.settings)
                if (currentDay != lastDrawnDay) {
                    lastDrawnDay = currentDay
                    draw()
                }
                scheduleNextMidnightCheck()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            LifeDotsPreferences.addWallpaperChangeListener(settingsChangeListener)
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(dateChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(dateChangeReceiver, filter)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            try {
                unregisterReceiver(dateChangeReceiver)
            } catch (e: Exception) {
                // Receiver was not registered or already unregistered
            }
            LifeDotsPreferences.removeWallpaperChangeListener(settingsChangeListener)
            handler.removeCallbacks(midnightChecker)
            backgroundRenderer.clear()
            handler.removeCallbacksAndMessages(null)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            restartRendering()
            if (visible) scheduleNextMidnightCheck()
            else handler.removeCallbacks(midnightChecker)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            draw()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacksAndMessages(null)
        }

        private fun scheduleNextMidnightCheck() {
            handler.removeCallbacks(midnightChecker)
            if (!visible) return
            val now = Calendar.getInstance()
            val midnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 1)
                set(Calendar.MILLISECOND, 0)
            }
            val delay = midnight.timeInMillis - now.timeInMillis
            handler.postDelayed(midnightChecker, delay)
        }

        private fun getActiveDayOfYear(settings: WallpaperSettings): Int {
            return if (settings.customYearSettings.enabled) {
                settings.customYearSettings.calculateProgress().dayIndex
            } else {
                Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            }
        }

        private fun getCurrentDayOfYear(): Int {
            return Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        }

        private fun getTotalDaysInYear(): Int {
            val calendar = Calendar.getInstance()
            return calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
        }

        private fun draw() {
            if (!visible) return

            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawDots(canvas)
                    lastDrawnDay = getActiveDayOfYear(preferences.settings)
                }
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: IllegalArgumentException) {
                        // Surface was destroyed
                    }
                }
            }
        }

        private fun drawDots(canvas: Canvas) {
            val settings = preferences.settings
            val colors = ThemeColors.fromSettings(settings)

            // Draw background color first
            canvas.drawColor(colors.background)

            // Feature 1: Draw background image if enabled
            backgroundRenderer.draw(canvas, settings.backgroundSettings, colors.background)

            // Draw glass effect background if enabled
            if (settings.glassEffectSettings.enabled) {
                glassEffectRenderer.drawGlassBackground(canvas, settings.glassEffectSettings, colors)
            }

            // Draw fluid effect background if enabled
            if (settings.fluidEffectSettings.enabled) {
                fluidEffectRenderer.drawFluidBackground(canvas, settings.fluidEffectSettings, colors, fluidPhase)
            }

            if (settings.theme == com.example.lifedots.preferences.ThemeOption.LIQUID_GLASS) {
                liquidGlassRenderer.draw(canvas)
            }

            dotGridRenderer.setupPaints(colors, settings)

            val customRangeProgress = if (settings.customYearSettings.enabled) {
                settings.customYearSettings.calculateProgress()
            } else null

            val dayOfYear = customRangeProgress?.dayIndex ?: getCurrentDayOfYear()
            val totalDays = customRangeProgress?.totalDays ?: getTotalDaysInYear()
            val isTodayInRange = customRangeProgress?.isTodayInRange ?: true

            // Calculate available height considering goals, progress, and footer
            val topOffset = overlayTextRenderer.calculateTopOffset(canvas.width, canvas.height, settings)
            val bottomOffset = overlayTextRenderer.calculateBottomOffset(canvas.width, canvas.height, settings)

            var topY = canvas.height * 0.04f

            // Life in Weeks Title ("MEMENTO MORI")
            if (settings.timeScale == TimeScale.LIFE && settings.lifeSettings.showTitle) {
                overlayTextRenderer.drawLifeTitle(canvas, settings.lifeSettings, colors, topY + 40f)
                topY += 60f
            }

            // Feature 6: Draw goals at top if enabled and positioned there
            if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.TOP) {
                overlayTextRenderer.drawGoals(canvas, settings.goalSettings, colors, topY, canvas.width.toFloat())
                topY += 50f + (settings.goalSettings.goals.size * 40f)
            }

            val lifeProgress = if (settings.timeScale == TimeScale.LIFE) settings.lifeSettings.calculateProgress() else null

            // Year / Life Progress & Countdown at top if enabled
            if (settings.progressSettings.enabled && settings.progressSettings.position == ProgressPosition.TOP) {
                val progressY = if (topY > 0f) topY + 10f else canvas.height * 0.06f
                overlayTextRenderer.drawProgress(canvas, settings.progressSettings, dayOfYear, totalDays, lifeProgress, progressY)
            }

            // Apply position and scale transformations
            val positionSettings = settings.positionSettings
            canvas.save()

            // Calculate offset based on screen size
            val offsetX = canvas.width * (positionSettings.horizontalOffset / 100f)
            val offsetY = canvas.height * (positionSettings.verticalOffset / 100f)

            // Apply transformations
            canvas.translate(offsetX, offsetY)
            canvas.scale(
                positionSettings.scale,
                positionSettings.scale,
                canvas.width / 2f,
                canvas.height / 2f
            )

            // Check visualization mode
            if (settings.timeScale == TimeScale.LIFE) {
                dotGridRenderer.drawLifeInWeeksView(canvas, settings, colors, topOffset, bottomOffset, animationTime)
            } else if (settings.treeEffectSettings.enabled) {
                treeEffectRenderer.drawTreeEffect(canvas, settings, colors, dayOfYear, totalDays, topOffset, bottomOffset)
            } else {
                // Draw based on view mode
                when (settings.viewModeSettings.mode) {
                    ViewMode.CONTINUOUS -> {
                        dotGridRenderer.drawContinuousView(canvas, settings, colors, dayOfYear, totalDays, topOffset, bottomOffset, isTodayInRange, animationTime)
                    }
                    ViewMode.MONTHLY -> {
                        dotGridRenderer.drawMonthlyView(canvas, settings, colors, dayOfYear, totalDays, topOffset, bottomOffset, isTodayInRange, animationTime)
                    }
                    ViewMode.CALENDAR -> {
                        dotGridRenderer.drawCalendarView(canvas, settings, colors, dayOfYear, totalDays, topOffset, bottomOffset, isTodayInRange, animationTime)
                    }
                }
            }

            canvas.restore()

            var bottomY = canvas.height - (canvas.height * 0.04f)

            // Life Quote (Seneca) at bottom
            if (settings.timeScale == TimeScale.LIFE && settings.lifeSettings.showQuote) {
                val quoteHeight = overlayTextRenderer.calculateLifeQuoteHeight(canvas, settings.lifeSettings)
                overlayTextRenderer.drawLifeQuote(canvas, settings.lifeSettings, colors, bottomY - quoteHeight)
                bottomY -= (quoteHeight + 20f)
            }

            // Feature 2: Draw footer text if enabled
            if (settings.footerTextSettings.enabled && settings.footerTextSettings.text.isNotEmpty()) {
                overlayTextRenderer.drawFooterText(canvas, settings.footerTextSettings, settings.customYearSettings, dayOfYear, totalDays, lifeProgress, bottomY)
                bottomY -= (settings.footerTextSettings.fontSize * 3 + 20f)
            }

            // Year / Life Progress & Countdown at bottom if enabled
            if (settings.progressSettings.enabled && settings.progressSettings.position == ProgressPosition.BOTTOM) {
                overlayTextRenderer.drawProgress(canvas, settings.progressSettings, dayOfYear, totalDays, lifeProgress, bottomY)
                bottomY -= (settings.progressSettings.fontSize * 3 + 20f)
            }

            // Feature 6: Draw goals at bottom if enabled and positioned there
            if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.BOTTOM) {
                val goalY = bottomY - (settings.goalSettings.goals.size * 40f)
                overlayTextRenderer.drawGoals(canvas, settings.goalSettings, colors, goalY, canvas.width.toFloat())
            }
        }

    }
}
