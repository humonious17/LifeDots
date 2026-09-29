package com.example.lifedots.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.lifedots.preferences.BackgroundSettings
import com.example.lifedots.preferences.GoalPosition
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.ProgressPosition
import com.example.lifedots.preferences.TimeScale
import com.example.lifedots.preferences.ViewMode
import com.example.lifedots.preferences.WallpaperSettings
import com.example.lifedots.util.ImageUtils
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
        private val glassEffectRenderer = GlassEffectRenderer()
        private val fluidEffectRenderer = FluidEffectRenderer()
        private val treeEffectRenderer = TreeEffectRenderer()
        private val dotGridRenderer = DotGridRenderer()
        private val overlayTextRenderer = OverlayTextRenderer()

        // Animation state
        private var animationTime = 0L
        private val animationFrameRate = 60 // FPS
        private val animationFrameDelay = 1000L / animationFrameRate

        // Animation loop
        private val animationRunner = object : Runnable {
            override fun run() {
                if (visible && preferences.settings.animationSettings.enabled) {
                    animationTime = System.currentTimeMillis()
                    draw()
                    handler.postDelayed(this, animationFrameDelay)
                }
            }
        }

        // Fluid effect state - for continuous motion
        private var fluidPhase = 0f
        private val fluidRunner = object : Runnable {
            override fun run() {
                if (visible && preferences.settings.fluidEffectSettings.enabled) {
                    fluidPhase += 0.02f * preferences.settings.fluidEffectSettings.flowSpeed
                    if (fluidPhase > 2 * Math.PI) fluidPhase = 0f
                    draw()
                    handler.postDelayed(this, 50)
                }
            }
        }

        // Background image caching
        private var cachedBackgroundBitmap: Bitmap? = null
        private var cachedBackgroundUri: String? = null
        private var cachedScreenWidth = 0
        private var cachedScreenHeight = 0

        private val settingsChangeListener: () -> Unit = {
            lastDrawnDay = -1
            handler.post { draw() }
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
            handler.removeCallbacks(animationRunner)
            handler.removeCallbacks(fluidRunner)
            handler.removeCallbacksAndMessages(null)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                draw()
                scheduleNextMidnightCheck()
                // Start animation loop if animations are enabled
                if (preferences.settings.animationSettings.enabled) {
                    animationTime = System.currentTimeMillis()
                    handler.post(animationRunner)
                }
                // Start fluid loop if fluid effects are enabled
                if (preferences.settings.fluidEffectSettings.enabled) {
                    handler.post(fluidRunner)
                }
            } else {
                handler.removeCallbacks(animationRunner)
                handler.removeCallbacks(fluidRunner)
            }
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
            drawBackgroundImage(canvas, settings.backgroundSettings, colors.background)

            // Draw glass effect background if enabled
            if (settings.glassEffectSettings.enabled) {
                glassEffectRenderer.drawGlassBackground(canvas, settings.glassEffectSettings, colors)
            }

            // Draw fluid effect background if enabled
            if (settings.fluidEffectSettings.enabled) {
                fluidEffectRenderer.drawFluidBackground(canvas, settings.fluidEffectSettings, colors, fluidPhase)
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

        private fun drawBackgroundImage(canvas: Canvas, bgSettings: BackgroundSettings, fallbackColor: Int) {
            if (!bgSettings.enabled || bgSettings.imageUri == null) return

            try {
                val bitmap = loadBackgroundBitmap(bgSettings.imageUri!!, canvas.width, canvas.height)
                if (bitmap != null) {
                    val finalBitmap = if (bgSettings.blurRadius > 0) {
                        applyBlur(bitmap, bgSettings.blurRadius)
                    } else {
                        bitmap
                    }

                    val paint = Paint()
                    paint.alpha = (bgSettings.opacity * 255).toInt()
                    canvas.drawBitmap(finalBitmap, 0f, 0f, paint)

                    val overlayPaint = Paint()
                    overlayPaint.color = fallbackColor
                    overlayPaint.alpha = ((1 - bgSettings.opacity) * 200).toInt()
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), overlayPaint)
                }
            } catch (e: Exception) {
                // Silently fail - background is optional
            }
        }

        private fun loadBackgroundBitmap(uriString: String, targetWidth: Int, targetHeight: Int): Bitmap? {
            if (cachedBackgroundBitmap != null &&
                cachedBackgroundUri == uriString &&
                cachedScreenWidth == targetWidth &&
                cachedScreenHeight == targetHeight) {
                return cachedBackgroundBitmap
            }

            return try {
                val uri = Uri.parse(uriString)
                val scaledBitmap = ImageUtils.loadScaledBitmap(applicationContext, uri, targetWidth, targetHeight)
                    ?: return null

                cachedBackgroundBitmap?.recycle()
                cachedBackgroundBitmap = scaledBitmap
                cachedBackgroundUri = uriString
                cachedScreenWidth = targetWidth
                cachedScreenHeight = targetHeight

                scaledBitmap
            } catch (e: Exception) {
                null
            }
        }

        private fun applyBlur(bitmap: Bitmap, radius: Float): Bitmap {
            return ImageUtils.applyBlur(applicationContext, bitmap, radius)
        }
    }
}
