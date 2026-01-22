package com.example.lifedots.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.lifedots.preferences.AnimationSettings
import com.example.lifedots.preferences.AnimationType
import com.example.lifedots.preferences.BackgroundSettings
import com.example.lifedots.preferences.DotEffectSettings
import com.example.lifedots.preferences.DotShape
import com.example.lifedots.preferences.DotSize
import com.example.lifedots.preferences.DotStyle
import com.example.lifedots.preferences.FluidEffectSettings
import com.example.lifedots.preferences.FluidStyle
import com.example.lifedots.preferences.FooterTextSettings
import com.example.lifedots.preferences.GlassEffectSettings
import com.example.lifedots.preferences.GlassStyle
import com.example.lifedots.preferences.GoalPosition
import com.example.lifedots.preferences.GoalSettings
import com.example.lifedots.preferences.GridDensity
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.PositionSettings
import com.example.lifedots.preferences.TextAlignment
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.preferences.TreeEffectSettings
import com.example.lifedots.preferences.TreeStyle
import com.example.lifedots.preferences.ViewMode
import com.example.lifedots.preferences.WallpaperSettings
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class LifeDotsWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LifeDotsEngine()
    }

    inner class LifeDotsEngine : Engine() {

        private val preferences by lazy { LifeDotsPreferences.getInstance(applicationContext) }
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var lastDrawnDay = -1

        private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val monthLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val treePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val fluidPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private val diamondPath = Path()
        private val rectF = RectF()
        private val treePath = Path()

        // Animation state
        private var animationTime = 0L
        private var lastAnimationFrame = 0L
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

        // Random seed for tree branches
        private val treeRandom = Random(42)

        // Background image caching
        private var cachedBackgroundBitmap: Bitmap? = null
        private var cachedBackgroundUri: String? = null
        private var cachedScreenWidth = 0
        private var cachedScreenHeight = 0

        // Month names for labels
        private val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        private val shortMonthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        private val settingsChangeListener: () -> Unit = {
            handler.post { draw() }
        }

        private val midnightChecker = object : Runnable {
            override fun run() {
                val currentDay = getCurrentDayOfYear()
                if (currentDay != lastDrawnDay) {
                    draw()
                }
                scheduleNextMidnightCheck()
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            LifeDotsPreferences.addWallpaperChangeListener(settingsChangeListener)
        }

        override fun onDestroy() {
            super.onDestroy()
            LifeDotsPreferences.removeWallpaperChangeListener(settingsChangeListener)
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
                handler.removeCallbacks(midnightChecker)
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
                    lastDrawnDay = getCurrentDayOfYear()
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
            val colors = getThemeColors(settings)

            // Draw background color first
            canvas.drawColor(colors.background)

            // Feature 1: Draw background image if enabled
            drawBackgroundImage(canvas, settings.backgroundSettings, colors.background)

            // Draw glass effect background if enabled
            if (settings.glassEffectSettings.enabled) {
                drawGlassBackground(canvas, settings.glassEffectSettings, colors)
            }

            // Draw fluid effect background if enabled
            if (settings.fluidEffectSettings.enabled) {
                drawFluidBackground(canvas, settings.fluidEffectSettings, colors)
            }

            setupPaints(colors, settings)

            val dayOfYear = getCurrentDayOfYear()
            val totalDays = getTotalDaysInYear()

            // Calculate available height considering goals and footer
            val topOffset = calculateTopOffset(canvas.width, canvas.height, settings)
            val bottomOffset = calculateBottomOffset(canvas.width, canvas.height, settings)

            // Feature 6: Draw goals at top if enabled and positioned there
            if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.TOP) {
                drawGoals(canvas, settings.goalSettings, colors, 0f, canvas.width.toFloat())
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

            // Check if tree effect should be drawn instead of dots
            if (settings.treeEffectSettings.enabled) {
                drawTreeEffect(canvas, settings, colors, dayOfYear, totalDays, topOffset, bottomOffset)
            } else {
                // Draw based on view mode
                when (settings.viewModeSettings.mode) {
                    ViewMode.CONTINUOUS -> {
                        drawContinuousView(canvas, settings, colors, dayOfYear, totalDays, topOffset, bottomOffset)
                    }
                    ViewMode.MONTHLY -> {
                        drawMonthlyView(canvas, settings, colors, dayOfYear, topOffset, bottomOffset)
                    }
                    ViewMode.CALENDAR -> {
                        drawCalendarView(canvas, settings, colors, dayOfYear, topOffset, bottomOffset)
                    }
                }
            }

            canvas.restore()

            // Feature 6: Draw goals at bottom if enabled and positioned there
            if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.BOTTOM) {
                val goalY = canvas.height - bottomOffset + 20f
                drawGoals(canvas, settings.goalSettings, colors, goalY, canvas.width.toFloat())
            }

            // Feature 2: Draw footer text if enabled
            if (settings.footerTextSettings.enabled) {
                drawFooterText(canvas, settings.footerTextSettings, canvas.height - 40f)
            }
        }

        private fun calculateTopOffset(width: Int, height: Int, settings: WallpaperSettings): Float {
            var offset = height * 0.06f
            if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.TOP) {
                offset += 80f + (settings.goalSettings.goals.size * 30f)
            }
            return offset
        }

        private fun calculateBottomOffset(width: Int, height: Int, settings: WallpaperSettings): Float {
            var offset = height * 0.06f
            if (settings.footerTextSettings.enabled && settings.footerTextSettings.text.isNotEmpty()) {
                offset += 60f
            }
            if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.BOTTOM) {
                offset += 80f + (settings.goalSettings.goals.size * 30f)
            }
            return offset
        }

        private fun drawContinuousView(
            canvas: Canvas,
            settings: WallpaperSettings,
            colors: ThemeColors,
            dayOfYear: Int,
            totalDays: Int,
            topOffset: Float,
            bottomOffset: Float
        ) {
            val availableHeight = canvas.height - topOffset - bottomOffset
            val gridConfig = calculateGridConfigWithOffset(
                canvas.width, availableHeight.toInt(), settings, totalDays, topOffset
            )

            // Reset animation counters
            currentDotIndex = 0
            totalDotsInView = totalDays

            var dotIndex = 0
            for (row in 0 until gridConfig.rows) {
                for (col in 0 until gridConfig.cols) {
                    if (dotIndex >= totalDays) break

                    val cx = gridConfig.startX + col * gridConfig.cellSize + gridConfig.cellSize / 2
                    val cy = gridConfig.startY + row * gridConfig.cellSize + gridConfig.cellSize / 2

                    val dotType = when {
                        dotIndex + 1 == dayOfYear && settings.highlightToday -> DotType.TODAY
                        dotIndex + 1 <= dayOfYear -> DotType.FILLED
                        else -> DotType.EMPTY
                    }

                    drawStyledDot(canvas, cx, cy, gridConfig.dotRadius, dotType, settings, colors)
                    dotIndex++
                }
                if (dotIndex >= totalDays) break
            }
        }

        private fun drawMonthlyView(
            canvas: Canvas,
            settings: WallpaperSettings,
            colors: ThemeColors,
            dayOfYear: Int,
            topOffset: Float,
            bottomOffset: Float
        ) {
            // Reset animation counters
            currentDotIndex = 0
            totalDotsInView = getTotalDaysInYear()

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val availableHeight = canvas.height - topOffset - bottomOffset
            val monthSectionHeight = availableHeight / 12f

            val cols = when (settings.gridDensity) {
                GridDensity.COMPACT -> 21
                GridDensity.NORMAL -> 19
                GridDensity.RELAXED -> 15
                GridDensity.SPACIOUS -> 12
            }

            val paddingPercent = when (settings.gridDensity) {
                GridDensity.COMPACT -> 0.06f
                GridDensity.NORMAL -> 0.08f
                GridDensity.RELAXED -> 0.10f
                GridDensity.SPACIOUS -> 0.12f
            }

            val horizontalPadding = canvas.width * paddingPercent

            var cumulativeDayOfYear = 0

            for (month in 0..11) {
                val tempCal = Calendar.getInstance()
                tempCal.set(currentYear, month, 1)
                val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                val monthTop = topOffset + month * monthSectionHeight
                val labelHeight = if (settings.viewModeSettings.showMonthLabels) 25f else 0f
                val dotsTop = monthTop + labelHeight

                // Draw month label
                if (settings.viewModeSettings.showMonthLabels) {
                    monthLabelPaint.color = settings.viewModeSettings.monthLabelColor
                    monthLabelPaint.textSize = 16f
                    monthLabelPaint.typeface = Typeface.DEFAULT_BOLD
                    canvas.drawText(monthNames[month], horizontalPadding, monthTop + 18f, monthLabelPaint)
                }

                // Calculate rows needed for this month
                val rows = (daysInMonth + cols - 1) / cols
                val dotAreaHeight = monthSectionHeight - labelHeight - 5f
                val cellSize = min(
                    (canvas.width - 2 * horizontalPadding) / cols,
                    dotAreaHeight / rows
                )

                val dotSizeMultiplier = when (settings.dotSize) {
                    DotSize.TINY -> 0.4f
                    DotSize.SMALL -> 0.55f
                    DotSize.MEDIUM -> 0.7f
                    DotSize.LARGE -> 0.85f
                    DotSize.HUGE -> 0.95f
                }
                val dotRadius = (cellSize / 2) * dotSizeMultiplier

                val gridWidth = cols * cellSize
                val startX = (canvas.width - gridWidth) / 2

                var dayIndex = 0
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        if (dayIndex >= daysInMonth) break

                        val cx = startX + col * cellSize + cellSize / 2
                        val cy = dotsTop + row * cellSize + cellSize / 2

                        val absoluteDay = cumulativeDayOfYear + dayIndex + 1
                        val dotType = when {
                            absoluteDay == dayOfYear && settings.highlightToday -> DotType.TODAY
                            absoluteDay <= dayOfYear -> DotType.FILLED
                            else -> DotType.EMPTY
                        }

                        drawStyledDot(canvas, cx, cy, dotRadius, dotType, settings, colors)
                        dayIndex++
                    }
                }
                cumulativeDayOfYear += daysInMonth
            }
        }

        private fun drawCalendarView(
            canvas: Canvas,
            settings: WallpaperSettings,
            colors: ThemeColors,
            dayOfYear: Int,
            topOffset: Float,
            bottomOffset: Float
        ) {
            // Reset animation counters
            currentDotIndex = 0
            totalDotsInView = getTotalDaysInYear()

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)

            val columnsPerRow = settings.calendarViewSettings.columnsPerRow
            val rowsOfMonths = (12 + columnsPerRow - 1) / columnsPerRow

            val availableWidth = canvas.width.toFloat()
            val availableHeight = canvas.height - topOffset - bottomOffset

            val cellWidth = availableWidth / columnsPerRow
            val cellHeight = availableHeight / rowsOfMonths

            val padding = 8f

            var cumulativeDayOfYear = 0
            val daysPerMonth = IntArray(12)
            for (m in 0..11) {
                val tempCal = Calendar.getInstance()
                tempCal.set(currentYear, m, 1)
                daysPerMonth[m] = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }

            for (month in 0..11) {
                val gridRow = month / columnsPerRow
                val gridCol = month % columnsPerRow

                val cellLeft = gridCol * cellWidth + padding
                val cellTop = topOffset + gridRow * cellHeight + padding
                val cellInnerWidth = cellWidth - 2 * padding
                val cellInnerHeight = cellHeight - 2 * padding

                // Draw month label
                val labelHeight = 20f
                monthLabelPaint.color = settings.viewModeSettings.monthLabelColor
                monthLabelPaint.textSize = 12f
                monthLabelPaint.typeface = Typeface.DEFAULT_BOLD
                if (settings.viewModeSettings.showMonthLabels) {
                    canvas.drawText(shortMonthNames[month], cellLeft + 4f, cellTop + 14f, monthLabelPaint)
                }

                val daysInMonth = daysPerMonth[month]
                val dotsAreaTop = cellTop + labelHeight
                val dotsAreaHeight = cellInnerHeight - labelHeight

                // Use 7 columns for a week-like layout in calendar view
                val cols = 7
                val rows = (daysInMonth + cols - 1) / cols

                val dotCellSize = min(cellInnerWidth / cols, dotsAreaHeight / rows)
                val dotSizeMultiplier = when (settings.dotSize) {
                    DotSize.TINY -> 0.35f
                    DotSize.SMALL -> 0.45f
                    DotSize.MEDIUM -> 0.55f
                    DotSize.LARGE -> 0.65f
                    DotSize.HUGE -> 0.75f
                }
                val dotRadius = (dotCellSize / 2) * dotSizeMultiplier

                val gridWidth = cols * dotCellSize
                val startX = cellLeft + (cellInnerWidth - gridWidth) / 2

                var dayIndex = 0
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        if (dayIndex >= daysInMonth) break

                        val cx = startX + col * dotCellSize + dotCellSize / 2
                        val cy = dotsAreaTop + row * dotCellSize + dotCellSize / 2

                        val absoluteDay = cumulativeDayOfYear + dayIndex + 1
                        val dotType = when {
                            absoluteDay == dayOfYear && settings.highlightToday -> DotType.TODAY
                            absoluteDay <= dayOfYear -> DotType.FILLED
                            else -> DotType.EMPTY
                        }

                        drawStyledDot(canvas, cx, cy, dotRadius, dotType, settings, colors)
                        dayIndex++
                    }
                }
                cumulativeDayOfYear += daysInMonth
            }
        }

        private var currentDotIndex = 0
        private var totalDotsInView = 365

        private fun drawStyledDot(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            dotType: DotType,
            settings: WallpaperSettings,
            colors: ThemeColors
        ) {
            val baseColor = when (dotType) {
                DotType.TODAY -> colors.todayDot
                DotType.FILLED -> colors.filledDot
                DotType.EMPTY -> colors.emptyDot
            }

            // Apply animation effects
            val animAlpha = getAnimationAlpha(currentDotIndex, totalDotsInView, settings.animationSettings)
            val animScale = getAnimationScale(currentDotIndex, totalDotsInView, settings.animationSettings)
            currentDotIndex++

            val baseAlpha = when (dotType) {
                DotType.TODAY -> 255
                DotType.FILLED -> (settings.filledDotAlpha * 255).toInt()
                DotType.EMPTY -> (settings.emptyDotAlpha * 255).toInt()
            }

            val alpha = (baseAlpha * animAlpha).toInt().coerceIn(0, 255)
            val animatedRadius = radius * animScale

            val effectSettings = settings.dotEffectSettings

            when (effectSettings.style) {
                DotStyle.FLAT -> {
                    val paint = when (dotType) {
                        DotType.TODAY -> todayPaint
                        DotType.FILLED -> filledPaint
                        DotType.EMPTY -> emptyPaint
                    }
                    paint.alpha = alpha
                    drawDot(canvas, cx, cy, animatedRadius, paint, settings.dotShape)
                }

                DotStyle.GRADIENT -> {
                    val lightColor = lightenColor(baseColor, 0.3f)
                    val darkColor = darkenColor(baseColor, 0.3f)

                    val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    gradientPaint.shader = RadialGradient(
                        cx - animatedRadius * 0.3f, cy - animatedRadius * 0.3f, animatedRadius * 1.5f,
                        lightColor, darkColor, Shader.TileMode.CLAMP
                    )
                    gradientPaint.alpha = alpha
                    drawDot(canvas, cx, cy, animatedRadius, gradientPaint, settings.dotShape)
                }

                DotStyle.OUTLINED -> {
                    // Draw outline only
                    outlinePaint.color = baseColor
                    outlinePaint.style = Paint.Style.STROKE
                    outlinePaint.strokeWidth = effectSettings.outlineWidth
                    outlinePaint.alpha = alpha
                    drawDot(canvas, cx, cy, animatedRadius - effectSettings.outlineWidth / 2, outlinePaint, settings.dotShape)
                }

                DotStyle.SOFT_GLOW -> {
                    // Draw glow behind
                    glowPaint.color = baseColor
                    glowPaint.alpha = (alpha * 0.3f).toInt()
                    glowPaint.maskFilter = BlurMaskFilter(effectSettings.glowRadius, BlurMaskFilter.Blur.NORMAL)
                    drawDot(canvas, cx, cy, animatedRadius + effectSettings.glowRadius / 2, glowPaint, settings.dotShape)

                    // Draw main dot
                    val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    mainPaint.color = baseColor
                    mainPaint.alpha = alpha
                    drawDot(canvas, cx, cy, animatedRadius, mainPaint, settings.dotShape)
                }

                DotStyle.NEON -> {
                    // Multiple glow layers for neon effect
                    for (i in 3 downTo 1) {
                        val glowAlpha = (alpha * 0.15f * i).toInt()
                        val glowSize = animatedRadius + (effectSettings.glowRadius * i / 2)

                        val neonGlow = Paint(Paint.ANTI_ALIAS_FLAG)
                        neonGlow.color = baseColor
                        neonGlow.alpha = glowAlpha
                        neonGlow.maskFilter = BlurMaskFilter(effectSettings.glowRadius * i, BlurMaskFilter.Blur.NORMAL)
                        drawDot(canvas, cx, cy, glowSize, neonGlow, settings.dotShape)
                    }

                    // Bright center
                    val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    centerPaint.color = lightenColor(baseColor, 0.5f)
                    centerPaint.alpha = alpha
                    drawDot(canvas, cx, cy, animatedRadius * 0.7f, centerPaint, settings.dotShape)
                }

                DotStyle.EMBOSSED -> {
                    // Shadow behind (offset)
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    shadowPaint.color = darkenColor(baseColor, 0.5f)
                    shadowPaint.alpha = (alpha * 0.5f).toInt()
                    drawDot(canvas, cx + 2f, cy + 2f, animatedRadius, shadowPaint, settings.dotShape)

                    // Highlight on top-left
                    val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    highlightPaint.color = lightenColor(baseColor, 0.3f)
                    highlightPaint.alpha = alpha
                    drawDot(canvas, cx, cy, animatedRadius, highlightPaint, settings.dotShape)

                    // Main dot slightly inset
                    val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    mainPaint.color = baseColor
                    mainPaint.alpha = alpha
                    drawDot(canvas, cx, cy, animatedRadius * 0.9f, mainPaint, settings.dotShape)
                }
            }
        }

        private fun lightenColor(color: Int, factor: Float): Int {
            val r = min(255, ((Color.red(color) * (1 - factor) + 255 * factor).toInt()))
            val g = min(255, ((Color.green(color) * (1 - factor) + 255 * factor).toInt()))
            val b = min(255, ((Color.blue(color) * (1 - factor) + 255 * factor).toInt()))
            return Color.rgb(r, g, b)
        }

        private fun darkenColor(color: Int, factor: Float): Int {
            val r = (Color.red(color) * (1 - factor)).toInt()
            val g = (Color.green(color) * (1 - factor)).toInt()
            val b = (Color.blue(color) * (1 - factor)).toInt()
            return Color.rgb(r, g, b)
        }

        private fun drawBackgroundImage(canvas: Canvas, bgSettings: BackgroundSettings, fallbackColor: Int) {
            if (!bgSettings.enabled || bgSettings.imageUri == null) return

            try {
                val bitmap = loadBackgroundBitmap(bgSettings.imageUri!!, canvas.width, canvas.height)
                if (bitmap != null) {
                    // Apply blur if needed
                    val finalBitmap = if (bgSettings.blurRadius > 0) {
                        applyBlur(bitmap, bgSettings.blurRadius)
                    } else {
                        bitmap
                    }

                    // Draw with opacity
                    val paint = Paint()
                    paint.alpha = (bgSettings.opacity * 255).toInt()
                    canvas.drawBitmap(finalBitmap, 0f, 0f, paint)

                    // Draw overlay for better dot visibility
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
            // Return cached bitmap if available and size matches
            if (cachedBackgroundBitmap != null &&
                cachedBackgroundUri == uriString &&
                cachedScreenWidth == targetWidth &&
                cachedScreenHeight == targetHeight) {
                return cachedBackgroundBitmap
            }

            try {
                val uri = Uri.parse(uriString)
                val inputStream = applicationContext.contentResolver.openInputStream(uri)
                    ?: return null

                // Decode bounds first
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()

                // Calculate sample size
                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                options.inJustDecodeBounds = false

                // Decode actual bitmap
                val inputStream2 = applicationContext.contentResolver.openInputStream(uri)
                    ?: return null
                val bitmap = BitmapFactory.decodeStream(inputStream2, null, options)
                inputStream2.close()

                if (bitmap == null) return null

                // Scale to fit screen
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                }

                // Cache the result
                cachedBackgroundBitmap?.recycle()
                cachedBackgroundBitmap = scaledBitmap
                cachedBackgroundUri = uriString
                cachedScreenWidth = targetWidth
                cachedScreenHeight = targetHeight

                return scaledBitmap
            } catch (e: Exception) {
                return null
            }
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        @Suppress("DEPRECATION")
        private fun applyBlur(bitmap: Bitmap, radius: Float): Bitmap {
            val clampedRadius = min(25f, radius)
            if (clampedRadius <= 0) return bitmap

            return try {
                val rs = RenderScript.create(applicationContext)
                val input = Allocation.createFromBitmap(rs, bitmap)
                val output = Allocation.createTyped(rs, input.type)
                val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
                script.setRadius(clampedRadius)
                script.setInput(input)
                script.forEach(output)
                val blurredBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
                output.copyTo(blurredBitmap)
                rs.destroy()
                blurredBitmap
            } catch (e: Exception) {
                bitmap
            }
        }

        private fun drawFooterText(canvas: Canvas, footerSettings: FooterTextSettings, y: Float) {
            if (footerSettings.text.isEmpty()) return

            textPaint.color = footerSettings.color
            textPaint.textSize = footerSettings.fontSize * 3  // Scale for wallpaper
            textPaint.typeface = Typeface.DEFAULT

            val textWidth = textPaint.measureText(footerSettings.text)
            val x = when (footerSettings.alignment) {
                TextAlignment.LEFT -> 40f
                TextAlignment.CENTER -> (canvas.width - textWidth) / 2
                TextAlignment.RIGHT -> canvas.width - textWidth - 40f
            }

            canvas.drawText(footerSettings.text, x, y, textPaint)
        }

        private fun drawGoals(canvas: Canvas, goalSettings: GoalSettings, colors: ThemeColors, startY: Float, width: Float) {
            if (goalSettings.goals.isEmpty()) return

            val now = System.currentTimeMillis()
            var yOffset = startY + 50f

            for (goal in goalSettings.goals) {
                val daysRemaining = ((goal.targetDate - now) / (1000 * 60 * 60 * 24)).toInt()

                val text = if (daysRemaining > 0) {
                    "$daysRemaining days until ${goal.title}"
                } else if (daysRemaining == 0) {
                    "Today: ${goal.title}!"
                } else {
                    "${-daysRemaining} days since ${goal.title}"
                }

                textPaint.color = goal.color
                textPaint.textSize = 36f
                textPaint.typeface = Typeface.DEFAULT_BOLD

                val textWidth = textPaint.measureText(text)
                val x = (width - textWidth) / 2

                canvas.drawText(text, x, yOffset, textPaint)
                yOffset += 40f
            }
        }

        private fun calculateGridConfigWithOffset(
            width: Int,
            height: Int,
            settings: WallpaperSettings,
            totalDots: Int,
            topOffset: Float
        ): GridConfig {
            val cols = when (settings.gridDensity) {
                GridDensity.COMPACT -> 21
                GridDensity.NORMAL -> 19
                GridDensity.RELAXED -> 15
                GridDensity.SPACIOUS -> 12
            }

            val rows = (totalDots + cols - 1) / cols

            val dotSizeMultiplier = when (settings.dotSize) {
                DotSize.TINY -> 0.4f
                DotSize.SMALL -> 0.55f
                DotSize.MEDIUM -> 0.7f
                DotSize.LARGE -> 0.85f
                DotSize.HUGE -> 0.95f
            }

            val paddingPercent = when (settings.gridDensity) {
                GridDensity.COMPACT -> 0.06f
                GridDensity.NORMAL -> 0.08f
                GridDensity.RELAXED -> 0.10f
                GridDensity.SPACIOUS -> 0.12f
            }

            val horizontalPadding = width * paddingPercent
            val verticalPadding = height * paddingPercent

            val availableWidth = width - (2 * horizontalPadding)
            val availableHeight = height - (2 * verticalPadding)

            val cellSizeByWidth = availableWidth / cols
            val cellSizeByHeight = availableHeight / rows
            val cellSize = minOf(cellSizeByWidth, cellSizeByHeight)

            val gridWidth = cols * cellSize
            val gridHeight = rows * cellSize

            val startX = (width - gridWidth) / 2
            val startY = topOffset + (height - gridHeight) / 2

            val dotRadius = (cellSize / 2) * dotSizeMultiplier

            return GridConfig(
                cols = cols,
                rows = rows,
                cellSize = cellSize,
                dotRadius = dotRadius,
                startX = startX,
                startY = startY
            )
        }

        private fun drawDot(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint, shape: DotShape) {
            when (shape) {
                DotShape.CIRCLE -> {
                    canvas.drawCircle(cx, cy, radius, paint)
                }
                DotShape.SQUARE -> {
                    rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)
                    canvas.drawRect(rectF, paint)
                }
                DotShape.ROUNDED_SQUARE -> {
                    rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)
                    val cornerRadius = radius * 0.3f
                    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
                }
                DotShape.DIAMOND -> {
                    diamondPath.reset()
                    diamondPath.moveTo(cx, cy - radius)
                    diamondPath.lineTo(cx + radius, cy)
                    diamondPath.lineTo(cx, cy + radius)
                    diamondPath.lineTo(cx - radius, cy)
                    diamondPath.close()
                    canvas.drawPath(diamondPath, paint)
                }
            }
        }

        private fun setupPaints(colors: ThemeColors, settings: WallpaperSettings) {
            filledPaint.color = colors.filledDot
            filledPaint.style = Paint.Style.FILL
            filledPaint.alpha = (settings.filledDotAlpha * 255).toInt()

            emptyPaint.color = colors.emptyDot
            emptyPaint.style = Paint.Style.FILL
            emptyPaint.alpha = (settings.emptyDotAlpha * 255).toInt()

            todayPaint.color = colors.todayDot
            todayPaint.style = Paint.Style.FILL
            todayPaint.alpha = 255
        }

        private fun calculateGridConfig(
            width: Int,
            height: Int,
            settings: WallpaperSettings,
            totalDots: Int
        ): GridConfig {
            val cols = when (settings.gridDensity) {
                GridDensity.COMPACT -> 21
                GridDensity.NORMAL -> 19
                GridDensity.RELAXED -> 15
                GridDensity.SPACIOUS -> 12
            }

            val rows = (totalDots + cols - 1) / cols

            val dotSizeMultiplier = when (settings.dotSize) {
                DotSize.TINY -> 0.4f
                DotSize.SMALL -> 0.55f
                DotSize.MEDIUM -> 0.7f
                DotSize.LARGE -> 0.85f
                DotSize.HUGE -> 0.95f
            }

            val paddingPercent = when (settings.gridDensity) {
                GridDensity.COMPACT -> 0.06f
                GridDensity.NORMAL -> 0.08f
                GridDensity.RELAXED -> 0.10f
                GridDensity.SPACIOUS -> 0.12f
            }

            val horizontalPadding = width * paddingPercent
            val verticalPadding = height * paddingPercent

            val availableWidth = width - (2 * horizontalPadding)
            val availableHeight = height - (2 * verticalPadding)

            val cellSizeByWidth = availableWidth / cols
            val cellSizeByHeight = availableHeight / rows
            val cellSize = minOf(cellSizeByWidth, cellSizeByHeight)

            val gridWidth = cols * cellSize
            val gridHeight = rows * cellSize

            val startX = (width - gridWidth) / 2
            val startY = (height - gridHeight) / 2

            val dotRadius = (cellSize / 2) * dotSizeMultiplier

            return GridConfig(
                cols = cols,
                rows = rows,
                cellSize = cellSize,
                dotRadius = dotRadius,
                startX = startX,
                startY = startY
            )
        }

        // ===== GLASS EFFECT =====
        private fun drawGlassBackground(canvas: Canvas, glassSettings: GlassEffectSettings, colors: ThemeColors) {
            if (glassSettings.style == GlassStyle.NONE) return

            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val centerX = width / 2
            val centerY = height / 2

            glassPaint.reset()
            glassPaint.isAntiAlias = true

            when (glassSettings.style) {
                GlassStyle.LIGHT_FROST -> {
                    // Light frosted glass effect
                    glassPaint.color = Color.argb(
                        (glassSettings.opacity * 255).toInt(),
                        255, 255, 255
                    )
                    glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur, BlurMaskFilter.Blur.NORMAL)
                    canvas.drawRect(0f, 0f, width, height, glassPaint)

                    // Add subtle gradient overlay
                    val gradient = LinearGradient(
                        0f, 0f, 0f, height,
                        Color.argb(40, 255, 255, 255),
                        Color.argb(10, 255, 255, 255),
                        Shader.TileMode.CLAMP
                    )
                    glassPaint.shader = gradient
                    glassPaint.maskFilter = null
                    canvas.drawRect(0f, 0f, width, height, glassPaint)
                    glassPaint.shader = null
                }

                GlassStyle.HEAVY_FROST -> {
                    // Heavy frosted glass with multiple layers
                    for (i in 3 downTo 1) {
                        glassPaint.color = Color.argb(
                            (glassSettings.opacity * 80 / i).toInt(),
                            255, 255, 255
                        )
                        glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur * i, BlurMaskFilter.Blur.NORMAL)
                        canvas.drawRect(0f, 0f, width, height, glassPaint)
                    }
                }

                GlassStyle.ACRYLIC -> {
                    // Windows 11 Acrylic-style effect
                    // Tinted blur layer
                    val tintR = Color.red(glassSettings.tint)
                    val tintG = Color.green(glassSettings.tint)
                    val tintB = Color.blue(glassSettings.tint)

                    glassPaint.color = Color.argb(
                        (glassSettings.opacity * 200).toInt(),
                        tintR, tintG, tintB
                    )
                    glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur, BlurMaskFilter.Blur.NORMAL)
                    canvas.drawRect(0f, 0f, width, height, glassPaint)

                    // Noise texture simulation with dots
                    glassPaint.maskFilter = null
                    glassPaint.color = Color.argb(15, 255, 255, 255)
                    val noiseRandom = Random(System.currentTimeMillis() / 1000)
                    for (i in 0 until 200) {
                        val x = noiseRandom.nextFloat() * width
                        val y = noiseRandom.nextFloat() * height
                        canvas.drawCircle(x, y, 1f, glassPaint)
                    }
                }

                GlassStyle.CRYSTAL -> {
                    // Crystal clear glass with refraction-like effect
                    val gradient = RadialGradient(
                        centerX, centerY,
                        maxOf(width, height) / 2,
                        intArrayOf(
                            Color.argb((glassSettings.opacity * 100).toInt(), 255, 255, 255),
                            Color.argb((glassSettings.opacity * 50).toInt(), 200, 220, 255),
                            Color.argb((glassSettings.opacity * 30).toInt(), 180, 200, 255)
                        ),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    glassPaint.shader = gradient
                    canvas.drawRect(0f, 0f, width, height, glassPaint)
                    glassPaint.shader = null

                    // Add light streaks
                    glassPaint.color = Color.argb(30, 255, 255, 255)
                    glassPaint.strokeWidth = 2f
                    glassPaint.style = Paint.Style.STROKE
                    for (i in 0 until 5) {
                        val startX = width * (0.2f + i * 0.15f)
                        canvas.drawLine(startX, 0f, startX - 50, height, glassPaint)
                    }
                    glassPaint.style = Paint.Style.FILL
                }

                GlassStyle.ICE -> {
                    // Ice effect with blue tint and crystalline patterns
                    glassPaint.color = Color.argb(
                        (glassSettings.opacity * 150).toInt(),
                        200, 230, 255
                    )
                    glassPaint.maskFilter = BlurMaskFilter(glassSettings.blur, BlurMaskFilter.Blur.NORMAL)
                    canvas.drawRect(0f, 0f, width, height, glassPaint)
                    glassPaint.maskFilter = null

                    // Draw ice crystal patterns
                    glassPaint.color = Color.argb(40, 255, 255, 255)
                    glassPaint.strokeWidth = 1.5f
                    glassPaint.style = Paint.Style.STROKE
                    val iceRandom = Random(42)
                    for (i in 0 until 20) {
                        val x = iceRandom.nextFloat() * width
                        val y = iceRandom.nextFloat() * height
                        drawIceCrystal(canvas, x, y, 30f + iceRandom.nextFloat() * 40f, glassPaint)
                    }
                    glassPaint.style = Paint.Style.FILL
                }

                GlassStyle.NONE -> { /* No effect */ }
            }
        }

        private fun drawIceCrystal(canvas: Canvas, cx: Float, cy: Float, size: Float, paint: Paint) {
            // Draw a 6-pointed ice crystal
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60).toDouble())
                val endX = cx + (size * cos(angle)).toFloat()
                val endY = cy + (size * sin(angle)).toFloat()
                canvas.drawLine(cx, cy, endX, endY, paint)

                // Add small branches
                val midX = cx + (size * 0.6f * cos(angle)).toFloat()
                val midY = cy + (size * 0.6f * sin(angle)).toFloat()
                val branchAngle1 = angle + Math.PI / 6
                val branchAngle2 = angle - Math.PI / 6
                val branchLen = size * 0.3f
                canvas.drawLine(
                    midX, midY,
                    midX + (branchLen * cos(branchAngle1)).toFloat(),
                    midY + (branchLen * sin(branchAngle1)).toFloat(),
                    paint
                )
                canvas.drawLine(
                    midX, midY,
                    midX + (branchLen * cos(branchAngle2)).toFloat(),
                    midY + (branchLen * sin(branchAngle2)).toFloat(),
                    paint
                )
            }
        }

        // ===== FLUID EFFECT =====
        private fun drawFluidBackground(canvas: Canvas, fluidSettings: FluidEffectSettings, colors: ThemeColors) {
            if (fluidSettings.style == FluidStyle.NONE) return

            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()

            fluidPaint.reset()
            fluidPaint.isAntiAlias = true

            when (fluidSettings.style) {
                FluidStyle.WATER -> {
                    drawWaterEffect(canvas, width, height, fluidSettings, colors)
                }
                FluidStyle.LAVA -> {
                    drawLavaEffect(canvas, width, height, fluidSettings, colors)
                }
                FluidStyle.MERCURY -> {
                    drawMercuryEffect(canvas, width, height, fluidSettings, colors)
                }
                FluidStyle.PLASMA -> {
                    drawPlasmaEffect(canvas, width, height, fluidSettings, colors)
                }
                FluidStyle.AURORA -> {
                    drawAuroraEffect(canvas, width, height, fluidSettings, colors)
                }
                FluidStyle.NONE -> { /* No effect */ }
            }
        }

        private fun drawWaterEffect(canvas: Canvas, width: Float, height: Float, settings: FluidEffectSettings, colors: ThemeColors) {
            // Animated water waves
            val waveCount = 5
            val baseAlpha = (settings.colorIntensity * 60).toInt()

            for (i in 0 until waveCount) {
                val phase = fluidPhase + i * 0.5f
                val waveHeight = height * 0.05f * settings.turbulence

                fluidPaint.color = Color.argb(
                    baseAlpha - i * 10,
                    100, 150 + i * 20, 255
                )

                val path = Path()
                path.moveTo(0f, height)

                for (x in 0..width.toInt() step 10) {
                    val y = height * (0.6f + i * 0.08f) +
                            sin(x * 0.02 + phase.toDouble()).toFloat() * waveHeight +
                            sin(x * 0.01 + phase * 0.5).toFloat() * waveHeight * 0.5f
                    if (x == 0) path.moveTo(x.toFloat(), y)
                    else path.lineTo(x.toFloat(), y)
                }
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()

                canvas.drawPath(path, fluidPaint)
            }
        }

        private fun drawLavaEffect(canvas: Canvas, width: Float, height: Float, settings: FluidEffectSettings, colors: ThemeColors) {
            // Animated lava bubbles and flow
            val baseAlpha = (settings.colorIntensity * 100).toInt()

            // Background lava glow
            val gradient = LinearGradient(
                0f, height, 0f, 0f,
                Color.argb(baseAlpha, 255, 100, 0),
                Color.argb(baseAlpha / 3, 255, 50, 0),
                Shader.TileMode.CLAMP
            )
            fluidPaint.shader = gradient
            canvas.drawRect(0f, height * 0.5f, width, height, fluidPaint)
            fluidPaint.shader = null

            // Lava bubbles
            fluidPaint.color = Color.argb(baseAlpha, 255, 150, 50)
            val bubbleRandom = Random((fluidPhase * 10).toLong())
            for (i in 0 until 15) {
                val x = bubbleRandom.nextFloat() * width
                val baseY = height * 0.7f + bubbleRandom.nextFloat() * height * 0.25f
                val y = baseY - (sin(fluidPhase + i.toFloat()).toFloat() + 1) * 30f * settings.turbulence
                val radius = 10f + bubbleRandom.nextFloat() * 20f

                fluidPaint.maskFilter = BlurMaskFilter(radius * 0.5f, BlurMaskFilter.Blur.NORMAL)
                canvas.drawCircle(x, y, radius, fluidPaint)
            }
            fluidPaint.maskFilter = null
        }

        private fun drawMercuryEffect(canvas: Canvas, width: Float, height: Float, settings: FluidEffectSettings, colors: ThemeColors) {
            // Metallic liquid mercury effect
            val baseAlpha = (settings.colorIntensity * 150).toInt()

            // Mercury pools
            fluidPaint.color = Color.argb(baseAlpha, 180, 180, 200)

            val poolRandom = Random(42)
            for (i in 0 until 8) {
                val cx = poolRandom.nextFloat() * width
                val cy = height * 0.5f + poolRandom.nextFloat() * height * 0.4f
                val rx = 30f + poolRandom.nextFloat() * 60f
                val ry = 15f + poolRandom.nextFloat() * 30f

                // Animate position slightly
                val animCx = cx + sin(fluidPhase + i.toDouble()).toFloat() * 10f * settings.turbulence
                val animCy = cy + cos(fluidPhase * 0.7 + i.toDouble()).toFloat() * 5f * settings.turbulence

                // Metallic gradient
                val gradient = RadialGradient(
                    animCx - rx * 0.3f, animCy - ry * 0.3f, rx,
                    Color.argb(baseAlpha, 240, 240, 255),
                    Color.argb(baseAlpha, 120, 120, 140),
                    Shader.TileMode.CLAMP
                )
                fluidPaint.shader = gradient

                val rect = RectF(animCx - rx, animCy - ry, animCx + rx, animCy + ry)
                canvas.drawOval(rect, fluidPaint)
            }
            fluidPaint.shader = null
        }

        private fun drawPlasmaEffect(canvas: Canvas, width: Float, height: Float, settings: FluidEffectSettings, colors: ThemeColors) {
            // Colorful plasma effect
            val baseAlpha = (settings.colorIntensity * 100).toInt()

            // Create plasma-like color bands
            for (y in 0..height.toInt() step 20) {
                for (x in 0..width.toInt() step 20) {
                    val value = sin(x * 0.01 + fluidPhase.toDouble()) +
                            sin(y * 0.01 + fluidPhase * 0.5) +
                            sin((x + y) * 0.01 + fluidPhase * 0.3) +
                            sin(sqrt((x * x + y * y).toDouble()) * 0.01)

                    val normalizedValue = ((value + 4) / 8).toFloat()

                    val r = (sin(normalizedValue * Math.PI * 2).toFloat() * 127 + 128).toInt()
                    val g = (sin(normalizedValue * Math.PI * 2 + 2).toFloat() * 127 + 128).toInt()
                    val b = (sin(normalizedValue * Math.PI * 2 + 4).toFloat() * 127 + 128).toInt()

                    fluidPaint.color = Color.argb(baseAlpha / 2, r, g, b)
                    canvas.drawRect(x.toFloat(), y.toFloat(), x + 20f, y + 20f, fluidPaint)
                }
            }
        }

        private fun drawAuroraEffect(canvas: Canvas, width: Float, height: Float, settings: FluidEffectSettings, colors: ThemeColors) {
            // Northern lights aurora effect
            val baseAlpha = (settings.colorIntensity * 80).toInt()

            val auroraColors = intArrayOf(
                Color.argb(baseAlpha, 0, 255, 100),
                Color.argb(baseAlpha, 0, 200, 255),
                Color.argb(baseAlpha, 150, 0, 255),
                Color.argb(baseAlpha, 255, 0, 150)
            )

            for (band in 0 until 4) {
                val path = Path()
                val baseY = height * (0.1f + band * 0.15f)

                path.moveTo(0f, baseY)

                for (x in 0..width.toInt() step 5) {
                    val wave1 = sin(x * 0.005 + fluidPhase + band).toFloat() * 50f * settings.turbulence
                    val wave2 = sin(x * 0.01 + fluidPhase * 1.5 + band * 0.5).toFloat() * 30f * settings.turbulence
                    val y = baseY + wave1 + wave2

                    path.lineTo(x.toFloat(), y)
                }

                path.lineTo(width, baseY + 100f)
                path.lineTo(0f, baseY + 100f)
                path.close()

                fluidPaint.color = auroraColors[band]
                fluidPaint.maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
                canvas.drawPath(path, fluidPaint)
            }
            fluidPaint.maskFilter = null
        }

        // ===== TREE GROWTH EFFECT =====
        private fun drawTreeEffect(
            canvas: Canvas,
            settings: WallpaperSettings,
            colors: ThemeColors,
            dayOfYear: Int,
            totalDays: Int,
            topOffset: Float,
            bottomOffset: Float
        ) {
            val treeSettings = settings.treeEffectSettings
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val availableHeight = height - topOffset - bottomOffset

            // Progress through the year (0 to 1)
            val progress = dayOfYear.toFloat() / totalDays.toFloat()

            // Draw ground if enabled
            if (treeSettings.showGround) {
                treePaint.color = Color.argb(255, 60, 40, 20)
                val groundHeight = 50f
                canvas.drawRect(0f, height - bottomOffset - groundHeight, width, height - bottomOffset, treePaint)

                // Grass on top
                treePaint.color = Color.argb(200, 50, 120, 50)
                canvas.drawRect(0f, height - bottomOffset - groundHeight, width, height - bottomOffset - groundHeight + 10f, treePaint)
            }

            val treeCenterX = width / 2
            val treeBaseY = height - bottomOffset - 50f
            val maxTreeHeight = availableHeight * 0.8f

            when (treeSettings.style) {
                TreeStyle.SIMPLE -> drawSimpleTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
                TreeStyle.DETAILED -> drawDetailedTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
                TreeStyle.BONSAI -> drawBonsaiTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
                TreeStyle.SAKURA -> drawSakuraTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
                TreeStyle.WILLOW -> drawWillowTree(canvas, treeCenterX, treeBaseY, maxTreeHeight, progress, treeSettings, colors, dayOfYear)
            }
        }

        private fun drawSimpleTree(
            canvas: Canvas,
            centerX: Float,
            baseY: Float,
            maxHeight: Float,
            progress: Float,
            settings: TreeEffectSettings,
            colors: ThemeColors,
            dayOfYear: Int
        ) {
            val trunkHeight = maxHeight * 0.3f * progress
            val trunkWidth = 20f + progress * 15f

            // Draw trunk
            treePaint.color = settings.trunkColor
            val trunkRect = RectF(
                centerX - trunkWidth / 2,
                baseY - trunkHeight,
                centerX + trunkWidth / 2,
                baseY
            )
            canvas.drawRoundRect(trunkRect, 5f, 5f, treePaint)

            // Draw foliage layers (triangular)
            if (progress > 0.2f) {
                val foliageProgress = (progress - 0.2f) / 0.8f
                treePaint.color = settings.leafColor

                val layers = 3
                for (i in 0 until layers) {
                    val layerProgress = minOf(1f, foliageProgress * layers - i)
                    if (layerProgress <= 0) continue

                    val layerTop = baseY - trunkHeight - (maxHeight * 0.6f) * ((layers - i).toFloat() / layers) * layerProgress
                    val layerBottom = baseY - trunkHeight * 0.5f - (maxHeight * 0.2f * i)
                    val layerWidth = (80f + i * 40f) * layerProgress

                    treePath.reset()
                    treePath.moveTo(centerX, layerTop)
                    treePath.lineTo(centerX - layerWidth, layerBottom)
                    treePath.lineTo(centerX + layerWidth, layerBottom)
                    treePath.close()

                    canvas.drawPath(treePath, treePaint)
                }

                // Add dots/fruits as day indicators
                drawTreeDots(canvas, centerX, baseY - trunkHeight, 100f * foliageProgress, dayOfYear, settings, colors)
            }
        }

        private fun drawDetailedTree(
            canvas: Canvas,
            centerX: Float,
            baseY: Float,
            maxHeight: Float,
            progress: Float,
            settings: TreeEffectSettings,
            colors: ThemeColors,
            dayOfYear: Int
        ) {
            // Draw trunk with branches
            treePaint.color = settings.trunkColor
            treePaint.strokeWidth = 8f + progress * 10f
            treePaint.strokeCap = Paint.Cap.ROUND
            treePaint.style = Paint.Style.STROKE

            val trunkHeight = maxHeight * 0.4f * progress

            // Main trunk
            canvas.drawLine(centerX, baseY, centerX, baseY - trunkHeight, treePaint)

            // Branches
            if (progress > 0.3f) {
                val branchProgress = (progress - 0.3f) / 0.7f
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.4f, -45f, trunkHeight * 0.3f * branchProgress, treePaint, 2)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.4f, 45f, trunkHeight * 0.3f * branchProgress, treePaint, 2)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.6f, -35f, trunkHeight * 0.35f * branchProgress, treePaint, 2)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.6f, 35f, trunkHeight * 0.35f * branchProgress, treePaint, 2)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.8f, -25f, trunkHeight * 0.25f * branchProgress, treePaint, 1)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.8f, 25f, trunkHeight * 0.25f * branchProgress, treePaint, 1)
            }

            treePaint.style = Paint.Style.FILL

            // Leaf clusters
            if (progress > 0.4f) {
                val leafProgress = (progress - 0.4f) / 0.6f
                treePaint.color = settings.leafColor

                drawLeafCluster(canvas, centerX, baseY - trunkHeight, 60f * leafProgress, treePaint)
                drawLeafCluster(canvas, centerX - 40f, baseY - trunkHeight * 0.7f, 45f * leafProgress, treePaint)
                drawLeafCluster(canvas, centerX + 40f, baseY - trunkHeight * 0.7f, 45f * leafProgress, treePaint)
                drawLeafCluster(canvas, centerX - 60f, baseY - trunkHeight * 0.5f, 35f * leafProgress, treePaint)
                drawLeafCluster(canvas, centerX + 60f, baseY - trunkHeight * 0.5f, 35f * leafProgress, treePaint)

                // Day indicator dots
                drawTreeDots(canvas, centerX, baseY - trunkHeight * 0.6f, 80f * leafProgress, dayOfYear, settings, colors)
            }
        }

        private fun drawBranch(canvas: Canvas, startX: Float, startY: Float, angle: Float, length: Float, paint: Paint, depth: Int) {
            val radAngle = Math.toRadians(angle.toDouble() - 90)
            val endX = startX + (length * cos(radAngle)).toFloat()
            val endY = startY + (length * sin(radAngle)).toFloat()

            paint.strokeWidth = (depth * 3f + 2f)
            canvas.drawLine(startX, startY, endX, endY, paint)

            if (depth > 0) {
                drawBranch(canvas, endX, endY, angle - 25f, length * 0.6f, paint, depth - 1)
                drawBranch(canvas, endX, endY, angle + 25f, length * 0.6f, paint, depth - 1)
            }
        }

        private fun drawLeafCluster(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
            paint.maskFilter = BlurMaskFilter(radius * 0.3f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(cx, cy, radius, paint)
            paint.maskFilter = null
        }

        private fun drawBonsaiTree(
            canvas: Canvas,
            centerX: Float,
            baseY: Float,
            maxHeight: Float,
            progress: Float,
            settings: TreeEffectSettings,
            colors: ThemeColors,
            dayOfYear: Int
        ) {
            // Compact bonsai style
            val trunkHeight = maxHeight * 0.25f * progress
            val trunkWidth = 15f + progress * 20f

            // Curved trunk
            treePaint.color = settings.trunkColor
            treePaint.strokeWidth = trunkWidth
            treePaint.strokeCap = Paint.Cap.ROUND
            treePaint.style = Paint.Style.STROKE

            treePath.reset()
            treePath.moveTo(centerX, baseY)
            treePath.quadTo(
                centerX - 30f * progress, baseY - trunkHeight * 0.5f,
                centerX - 20f * progress, baseY - trunkHeight
            )
            canvas.drawPath(treePath, treePaint)

            treePaint.style = Paint.Style.FILL

            // Compact foliage pads
            if (progress > 0.3f) {
                val foliageProgress = (progress - 0.3f) / 0.7f
                treePaint.color = settings.leafColor

                // Multiple foliage pads
                val padPositions = listOf(
                    Pair(centerX - 20f * progress, baseY - trunkHeight),
                    Pair(centerX - 50f * progress, baseY - trunkHeight * 0.7f),
                    Pair(centerX + 10f * progress, baseY - trunkHeight * 0.8f)
                )

                for ((x, y) in padPositions) {
                    treePaint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
                    canvas.drawOval(
                        RectF(x - 40f * foliageProgress, y - 20f * foliageProgress,
                            x + 40f * foliageProgress, y + 15f * foliageProgress),
                        treePaint
                    )
                }
                treePaint.maskFilter = null

                drawTreeDots(canvas, centerX - 20f, baseY - trunkHeight * 0.8f, 50f * foliageProgress, dayOfYear, settings, colors)
            }

            // Draw pot
            treePaint.color = Color.argb(255, 120, 60, 30)
            val potWidth = 80f
            val potHeight = 30f
            canvas.drawRoundRect(
                RectF(centerX - potWidth / 2, baseY, centerX + potWidth / 2, baseY + potHeight),
                10f, 10f, treePaint
            )
        }

        private fun drawSakuraTree(
            canvas: Canvas,
            centerX: Float,
            baseY: Float,
            maxHeight: Float,
            progress: Float,
            settings: TreeEffectSettings,
            colors: ThemeColors,
            dayOfYear: Int
        ) {
            // Japanese cherry blossom tree
            val trunkHeight = maxHeight * 0.35f * progress
            val trunkWidth = 12f + progress * 8f

            // Curved trunk
            treePaint.color = settings.trunkColor
            treePaint.strokeWidth = trunkWidth
            treePaint.strokeCap = Paint.Cap.ROUND
            treePaint.style = Paint.Style.STROKE

            treePath.reset()
            treePath.moveTo(centerX, baseY)
            treePath.cubicTo(
                centerX + 20f, baseY - trunkHeight * 0.3f,
                centerX - 10f, baseY - trunkHeight * 0.6f,
                centerX, baseY - trunkHeight
            )
            canvas.drawPath(treePath, treePaint)

            // Branches
            if (progress > 0.2f) {
                treePaint.strokeWidth = trunkWidth * 0.5f
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.5f, -60f, trunkHeight * 0.4f, treePaint, 1)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.5f, 50f, trunkHeight * 0.35f, treePaint, 1)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.7f, -40f, trunkHeight * 0.3f, treePaint, 1)
                drawBranch(canvas, centerX, baseY - trunkHeight * 0.7f, 45f, trunkHeight * 0.35f, treePaint, 1)
            }

            treePaint.style = Paint.Style.FILL

            // Cherry blossoms
            if (progress > 0.3f) {
                val bloomProgress = (progress - 0.3f) / 0.7f
                treePaint.color = settings.bloomColor

                // Blossom clusters
                val blossomRandom = Random(dayOfYear)
                for (i in 0 until (50 * bloomProgress).toInt()) {
                    val angle = blossomRandom.nextFloat() * 360f
                    val distance = blossomRandom.nextFloat() * 100f * bloomProgress
                    val bx = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
                    val by = (baseY - trunkHeight * 0.7f) + sin(Math.toRadians(angle.toDouble())).toFloat() * distance * 0.6f

                    val size = 3f + blossomRandom.nextFloat() * 5f
                    treePaint.alpha = 180 + blossomRandom.nextInt(75)
                    canvas.drawCircle(bx, by, size, treePaint)
                }

                // Falling petals
                treePaint.alpha = 150
                val petalCount = (20 * bloomProgress).toInt()
                val time = System.currentTimeMillis() / 50f
                for (i in 0 until petalCount) {
                    val px = (centerX - 80f + blossomRandom.nextFloat() * 160f + sin(time * 0.01 + i).toFloat() * 20f)
                    val py = (baseY - trunkHeight + ((time + i * 50) % (trunkHeight + 100)).toFloat())
                    canvas.drawCircle(px, py, 3f, treePaint)
                }

                treePaint.alpha = 255
                drawTreeDots(canvas, centerX, baseY - trunkHeight * 0.6f, 70f * bloomProgress, dayOfYear, settings, colors)
            }
        }

        private fun drawWillowTree(
            canvas: Canvas,
            centerX: Float,
            baseY: Float,
            maxHeight: Float,
            progress: Float,
            settings: TreeEffectSettings,
            colors: ThemeColors,
            dayOfYear: Int
        ) {
            // Weeping willow tree
            val trunkHeight = maxHeight * 0.3f * progress
            val trunkWidth = 18f + progress * 12f

            // Main trunk
            treePaint.color = settings.trunkColor
            treePaint.strokeWidth = trunkWidth
            treePaint.strokeCap = Paint.Cap.ROUND
            treePaint.style = Paint.Style.STROKE
            canvas.drawLine(centerX, baseY, centerX, baseY - trunkHeight, treePaint)

            treePaint.style = Paint.Style.FILL

            // Drooping branches with leaves
            if (progress > 0.25f) {
                val branchProgress = (progress - 0.25f) / 0.75f
                treePaint.color = settings.leafColor
                treePaint.strokeWidth = 2f
                treePaint.style = Paint.Style.STROKE

                val branchCount = (30 * branchProgress).toInt()
                val time = System.currentTimeMillis() / 1000f

                for (i in 0 until branchCount) {
                    val startAngle = -150f + (i.toFloat() / branchCount) * 120f
                    val startX = centerX + cos(Math.toRadians(startAngle.toDouble())).toFloat() * 20f
                    val startY = baseY - trunkHeight + sin(Math.toRadians(startAngle.toDouble())).toFloat() * 10f

                    val branchLength = 80f + (i % 5) * 30f * branchProgress
                    val swayAmount = sin(time + i * 0.5).toFloat() * 10f

                    treePath.reset()
                    treePath.moveTo(startX, startY)
                    treePath.cubicTo(
                        startX + swayAmount, startY + branchLength * 0.3f,
                        startX + swayAmount * 1.5f, startY + branchLength * 0.6f,
                        startX + swayAmount * 2f, startY + branchLength
                    )

                    canvas.drawPath(treePath, treePaint)
                }

                treePaint.style = Paint.Style.FILL
                drawTreeDots(canvas, centerX, baseY - trunkHeight * 0.5f, 60f * branchProgress, dayOfYear, settings, colors)
            }
        }

        private fun drawTreeDots(
            canvas: Canvas,
            centerX: Float,
            centerY: Float,
            radius: Float,
            dayOfYear: Int,
            settings: TreeEffectSettings,
            colors: ThemeColors
        ) {
            // Draw small dots representing days passed as fruits/leaves
            val dotRandom = Random(42)
            val dotsToShow = minOf(dayOfYear, 50)

            for (i in 0 until dotsToShow) {
                val angle = dotRandom.nextFloat() * 360f
                val distance = dotRandom.nextFloat() * radius
                val dx = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
                val dy = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance * 0.6f

                treePaint.color = if (i == dayOfYear - 1) colors.todayDot else colors.filledDot
                treePaint.alpha = if (i == dayOfYear - 1) 255 else 180
                canvas.drawCircle(dx, dy, 4f, treePaint)
            }
            treePaint.alpha = 255
        }

        // ===== ANIMATION HELPERS =====
        private fun getAnimationAlpha(dotIndex: Int, totalDots: Int, settings: AnimationSettings): Float {
            if (!settings.enabled) return 1f

            val time = animationTime / 1000f * settings.speed
            val normalizedIndex = dotIndex.toFloat() / totalDots

            return when (settings.type) {
                AnimationType.NONE -> 1f

                AnimationType.FADE_IN -> {
                    val fadeProgress = (time % 5f) / 5f
                    if (normalizedIndex <= fadeProgress) 1f else 0.2f
                }

                AnimationType.PULSE -> {
                    val pulse = (sin(time * 3 + normalizedIndex * 10) + 1) / 2
                    0.5f + pulse.toFloat() * 0.5f * settings.intensity
                }

                AnimationType.WAVE -> {
                    val wave = sin(time * 2 + normalizedIndex * Math.PI * 4)
                    (0.6f + wave.toFloat() * 0.4f * settings.intensity)
                }

                AnimationType.BREATHE -> {
                    val breathe = (sin(time * 1.5) + 1) / 2
                    0.4f + breathe.toFloat() * 0.6f * settings.intensity
                }

                AnimationType.RIPPLE -> {
                    val distance = normalizedIndex
                    val ripple = sin(time * 3 - distance * 20)
                    (0.5f + ripple.toFloat() * 0.5f * settings.intensity)
                }

                AnimationType.CASCADE -> {
                    val cascadeTime = (time % 3f) / 3f
                    val threshold = cascadeTime
                    if (normalizedIndex <= threshold) 1f else 0.3f
                }
            }
        }

        private fun getAnimationScale(dotIndex: Int, totalDots: Int, settings: AnimationSettings): Float {
            if (!settings.enabled) return 1f

            val time = animationTime / 1000f * settings.speed
            val normalizedIndex = dotIndex.toFloat() / totalDots

            return when (settings.type) {
                AnimationType.PULSE -> {
                    val pulse = (sin(time * 3 + normalizedIndex * 10) + 1) / 2
                    0.8f + pulse.toFloat() * 0.4f * settings.intensity
                }

                AnimationType.WAVE -> {
                    val wave = sin(time * 2 + normalizedIndex * Math.PI * 4)
                    0.9f + wave.toFloat() * 0.2f * settings.intensity
                }

                AnimationType.RIPPLE -> {
                    val distance = normalizedIndex
                    val ripple = sin(time * 3 - distance * 20)
                    0.9f + ripple.toFloat() * 0.2f * settings.intensity
                }

                else -> 1f
            }
        }

        private fun getThemeColors(settings: WallpaperSettings): ThemeColors {
            return when (settings.theme) {
                ThemeOption.LIGHT -> ThemeColors(
                    background = Color.parseColor("#F5F5F5"),
                    filledDot = Color.parseColor("#2C2C2C"),
                    emptyDot = Color.parseColor("#D0D0D0"),
                    todayDot = Color.parseColor("#4A90D9")
                )
                ThemeOption.DARK -> ThemeColors(
                    background = Color.parseColor("#1A1A1A"),
                    filledDot = Color.parseColor("#E0E0E0"),
                    emptyDot = Color.parseColor("#3A3A3A"),
                    todayDot = Color.parseColor("#5BA0E9")
                )
                ThemeOption.AMOLED -> ThemeColors(
                    background = Color.parseColor("#000000"),
                    filledDot = Color.parseColor("#FFFFFF"),
                    emptyDot = Color.parseColor("#2A2A2A"),
                    todayDot = Color.parseColor("#6AB0F9")
                )
                ThemeOption.CUSTOM -> ThemeColors(
                    background = settings.customColors.backgroundColor,
                    filledDot = settings.customColors.filledDotColor,
                    emptyDot = settings.customColors.emptyDotColor,
                    todayDot = settings.customColors.todayDotColor
                )
            }
        }
    }

    private data class ThemeColors(
        val background: Int,
        val filledDot: Int,
        val emptyDot: Int,
        val todayDot: Int
    )

    private data class GridConfig(
        val cols: Int,
        val rows: Int,
        val cellSize: Float,
        val dotRadius: Float,
        val startX: Float,
        val startY: Float
    )

    private enum class DotType {
        FILLED, EMPTY, TODAY
    }
}
