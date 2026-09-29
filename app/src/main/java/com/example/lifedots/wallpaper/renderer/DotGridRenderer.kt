package com.example.lifedots.wallpaper.renderer

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.lifedots.preferences.AnimationSettings
import com.example.lifedots.preferences.AnimationType
import com.example.lifedots.preferences.DotShape
import com.example.lifedots.preferences.DotSize
import com.example.lifedots.preferences.DotStyle
import com.example.lifedots.preferences.GridDensity
import com.example.lifedots.preferences.WallpaperSettings
import java.util.Calendar
import kotlin.math.min
import kotlin.math.sin

class DotGridRenderer {

    private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val monthLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val diamondPath = Path()
    private val rectF = RectF()

    private var currentDotIndex = 0
    private var totalDotsInView = 365

    private val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val shortMonthNames = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun setupPaints(colors: ThemeColors, settings: WallpaperSettings) {
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

    fun drawContinuousView(
        canvas: Canvas,
        settings: WallpaperSettings,
        colors: ThemeColors,
        dayOfYear: Int,
        totalDays: Int,
        topOffset: Float,
        bottomOffset: Float,
        isTodayInRange: Boolean = true,
        animationTime: Long = 0L
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
                    dotIndex + 1 == dayOfYear && settings.highlightToday && isTodayInRange -> DotType.TODAY
                    dotIndex + 1 <= dayOfYear -> DotType.FILLED
                    else -> DotType.EMPTY
                }

                drawStyledDot(canvas, cx, cy, gridConfig.dotRadius, dotType, settings, colors, animationTime)
                dotIndex++
            }
            if (dotIndex >= totalDays) break
        }
    }

    fun drawMonthlyView(
        canvas: Canvas,
        settings: WallpaperSettings,
        colors: ThemeColors,
        dayOfYear: Int,
        totalDays: Int,
        topOffset: Float,
        bottomOffset: Float,
        isTodayInRange: Boolean = true,
        animationTime: Long = 0L
    ) {
        val monthsList = mutableListOf<Pair<Int, Int>>() // Pair(year, month 0..11)
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        if (settings.customYearSettings.enabled) {
            val scanCal = Calendar.getInstance().apply {
                timeInMillis = settings.customYearSettings.startDate
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = settings.customYearSettings.endDate
                set(Calendar.DAY_OF_MONTH, 1)
            }
            while (!scanCal.after(endCal) && monthsList.size < 24) {
                monthsList.add(Pair(scanCal.get(Calendar.YEAR), scanCal.get(Calendar.MONTH)))
                scanCal.add(Calendar.MONTH, 1)
            }
        }
        if (monthsList.isEmpty()) {
            for (m in 0..11) monthsList.add(Pair(currentYear, m))
        }

        // Reset animation counters
        currentDotIndex = 0
        totalDotsInView = totalDays

        val availableHeight = canvas.height - topOffset - bottomOffset
        val monthSectionHeight = availableHeight / monthsList.size.toFloat()

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

        for (monthIdx in monthsList.indices) {
            val (year, month) = monthsList[monthIdx]
            val tempCal = Calendar.getInstance()
            tempCal.set(year, month, 1)
            val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val monthTop = topOffset + monthIdx * monthSectionHeight
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
                        absoluteDay == dayOfYear && settings.highlightToday && isTodayInRange -> DotType.TODAY
                        absoluteDay <= dayOfYear -> DotType.FILLED
                        else -> DotType.EMPTY
                    }

                    drawStyledDot(canvas, cx, cy, dotRadius, dotType, settings, colors, animationTime)
                    dayIndex++
                }
            }
            cumulativeDayOfYear += daysInMonth
        }
    }

    fun drawCalendarView(
        canvas: Canvas,
        settings: WallpaperSettings,
        colors: ThemeColors,
        dayOfYear: Int,
        totalDays: Int,
        topOffset: Float,
        bottomOffset: Float,
        isTodayInRange: Boolean = true,
        animationTime: Long = 0L
    ) {
        val monthsList = mutableListOf<Pair<Int, Int>>() // Pair(year, month 0..11)
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        if (settings.customYearSettings.enabled) {
            val scanCal = Calendar.getInstance().apply {
                timeInMillis = settings.customYearSettings.startDate
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = settings.customYearSettings.endDate
                set(Calendar.DAY_OF_MONTH, 1)
            }
            while (!scanCal.after(endCal) && monthsList.size < 24) {
                monthsList.add(Pair(scanCal.get(Calendar.YEAR), scanCal.get(Calendar.MONTH)))
                scanCal.add(Calendar.MONTH, 1)
            }
        }
        if (monthsList.isEmpty()) {
            for (m in 0..11) monthsList.add(Pair(currentYear, m))
        }

        // Reset animation counters
        currentDotIndex = 0
        totalDotsInView = totalDays

        val columnsPerRow = settings.calendarViewSettings.columnsPerRow
        val rowsOfMonths = (monthsList.size + columnsPerRow - 1) / columnsPerRow

        val availableWidth = canvas.width.toFloat()
        val availableHeight = canvas.height - topOffset - bottomOffset

        val cellWidth = availableWidth / columnsPerRow
        val cellHeight = availableHeight / rowsOfMonths

        val padding = 8f

        var cumulativeDayOfYear = 0

        for (monthIdx in monthsList.indices) {
            val (year, month) = monthsList[monthIdx]
            val gridRow = monthIdx / columnsPerRow
            val gridCol = monthIdx % columnsPerRow

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

            val tempCal = Calendar.getInstance()
            tempCal.set(year, month, 1)
            val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
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
                        absoluteDay == dayOfYear && settings.highlightToday && isTodayInRange -> DotType.TODAY
                        absoluteDay <= dayOfYear -> DotType.FILLED
                        else -> DotType.EMPTY
                    }

                    drawStyledDot(canvas, cx, cy, dotRadius, dotType, settings, colors, animationTime)
                    dayIndex++
                }
            }
            cumulativeDayOfYear += daysInMonth
        }
    }

    fun drawLifeInWeeksView(
        canvas: Canvas,
        settings: WallpaperSettings,
        colors: ThemeColors,
        topOffset: Float,
        bottomOffset: Float,
        animationTime: Long = 0L
    ) {
        val lifeSettings = settings.lifeSettings
        val progress = lifeSettings.calculateProgress()
        val rows = lifeSettings.lifeExpectancyYears
        val cols = 52

        val showYearLabels = lifeSettings.showYearLabels
        val splitHalves = lifeSettings.splitHalves

        val yearLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.emptyDot
            alpha = 180
            typeface = Typeface.DEFAULT
        }

        val horizontalMargin = canvas.width * 0.04f
        val labelMargin = if (showYearLabels) 40f else 0f

        val availableWidth = canvas.width - (2 * horizontalMargin) - labelMargin
        val availableHeight = canvas.height - topOffset - bottomOffset

        val gapMultiplier = if (splitHalves) 1.2f else 0f
        val totalColSlots = 52f + gapMultiplier

        val cellSizeByWidth = availableWidth / totalColSlots
        val cellSizeByHeight = availableHeight / rows.toFloat()
        val cellSize = minOf(cellSizeByWidth, cellSizeByHeight)

        val gapWidth = if (splitHalves) cellSize * gapMultiplier else 0f

        val dotSizeMultiplier = when (settings.dotSize) {
            DotSize.TINY -> 0.55f
            DotSize.SMALL -> 0.65f
            DotSize.MEDIUM -> 0.75f
            DotSize.LARGE -> 0.85f
            DotSize.HUGE -> 0.95f
        }
        val dotRadius = (cellSize / 2f) * dotSizeMultiplier

        val gridWidth = (52 * cellSize) + gapWidth
        val gridHeight = rows * cellSize

        val startX = (canvas.width - gridWidth - labelMargin) / 2f
        val startY = topOffset + (availableHeight - gridHeight) / 2f

        yearLabelPaint.textSize = (cellSize * 0.75f).coerceIn(12f, 26f)

        // Reset animation counters
        currentDotIndex = 0
        totalDotsInView = rows * cols

        for (r in 0 until rows) {
            val cy = startY + (r * cellSize) + (cellSize / 2f)

            // Draw year label on the right (e.g. for year 5, 10, 15, ..., 80)
            if (showYearLabels && (r + 1) % 5 == 0) {
                val labelText = "${r + 1}"
                val labelX = startX + gridWidth + 8f
                val labelY = cy + (yearLabelPaint.textSize / 3f)
                canvas.drawText(labelText, labelX, labelY, yearLabelPaint)
            }

            for (c in 0 until cols) {
                val dotIndex = r * 52 + c
                val colOffset = if (splitHalves && c >= 26) gapWidth else 0f
                val cx = startX + (c * cellSize) + colOffset + (cellSize / 2f)

                val dotType = when {
                    dotIndex == progress.currentDotIndex && settings.highlightToday -> DotType.TODAY
                    dotIndex < progress.currentDotIndex -> DotType.FILLED
                    else -> DotType.EMPTY
                }

                drawStyledDot(canvas, cx, cy, dotRadius, dotType, settings, colors, animationTime)
            }
        }
    }

    fun drawStyledDot(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        dotType: DotType,
        settings: WallpaperSettings,
        colors: ThemeColors,
        animationTime: Long = 0L
    ) {
        val baseColor = when (dotType) {
            DotType.TODAY -> colors.todayDot
            DotType.FILLED -> colors.filledDot
            DotType.EMPTY -> colors.emptyDot
        }

        // Apply animation effects
        val animAlpha = getAnimationAlpha(currentDotIndex, totalDotsInView, settings.animationSettings, animationTime)
        val animScale = getAnimationScale(currentDotIndex, totalDotsInView, settings.animationSettings, animationTime)
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

    fun calculateGridConfigWithOffset(
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

    fun calculateGridConfig(
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

    private fun getAnimationAlpha(
        dotIndex: Int,
        totalDots: Int,
        settings: AnimationSettings,
        animationTime: Long
    ): Float {
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

    private fun getAnimationScale(
        dotIndex: Int,
        totalDots: Int,
        settings: AnimationSettings,
        animationTime: Long
    ): Float {
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
}
