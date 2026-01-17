package com.example.lifedots.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
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
import com.example.lifedots.preferences.BackgroundSettings
import com.example.lifedots.preferences.DotEffectSettings
import com.example.lifedots.preferences.DotShape
import com.example.lifedots.preferences.DotSize
import com.example.lifedots.preferences.DotStyle
import com.example.lifedots.preferences.FooterTextSettings
import com.example.lifedots.preferences.GoalPosition
import com.example.lifedots.preferences.GoalSettings
import com.example.lifedots.preferences.GridDensity
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.TextAlignment
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.preferences.ViewMode
import com.example.lifedots.preferences.WallpaperSettings
import java.util.Calendar
import kotlin.math.min

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

        private val diamondPath = Path()
        private val rectF = RectF()

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
            handler.removeCallbacksAndMessages(null)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                draw()
                scheduleNextMidnightCheck()
            } else {
                handler.removeCallbacks(midnightChecker)
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

            val alpha = when (dotType) {
                DotType.TODAY -> 255
                DotType.FILLED -> (settings.filledDotAlpha * 255).toInt()
                DotType.EMPTY -> (settings.emptyDotAlpha * 255).toInt()
            }

            val effectSettings = settings.dotEffectSettings

            when (effectSettings.style) {
                DotStyle.FLAT -> {
                    val paint = when (dotType) {
                        DotType.TODAY -> todayPaint
                        DotType.FILLED -> filledPaint
                        DotType.EMPTY -> emptyPaint
                    }
                    drawDot(canvas, cx, cy, radius, paint, settings.dotShape)
                }

                DotStyle.GRADIENT -> {
                    val lightColor = lightenColor(baseColor, 0.3f)
                    val darkColor = darkenColor(baseColor, 0.3f)

                    val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    gradientPaint.shader = RadialGradient(
                        cx - radius * 0.3f, cy - radius * 0.3f, radius * 1.5f,
                        lightColor, darkColor, Shader.TileMode.CLAMP
                    )
                    gradientPaint.alpha = alpha
                    drawDot(canvas, cx, cy, radius, gradientPaint, settings.dotShape)
                }

                DotStyle.OUTLINED -> {
                    // Draw outline only
                    outlinePaint.color = baseColor
                    outlinePaint.style = Paint.Style.STROKE
                    outlinePaint.strokeWidth = effectSettings.outlineWidth
                    outlinePaint.alpha = alpha
                    drawDot(canvas, cx, cy, radius - effectSettings.outlineWidth / 2, outlinePaint, settings.dotShape)
                }

                DotStyle.SOFT_GLOW -> {
                    // Draw glow behind
                    glowPaint.color = baseColor
                    glowPaint.alpha = (alpha * 0.3f).toInt()
                    glowPaint.maskFilter = BlurMaskFilter(effectSettings.glowRadius, BlurMaskFilter.Blur.NORMAL)
                    drawDot(canvas, cx, cy, radius + effectSettings.glowRadius / 2, glowPaint, settings.dotShape)

                    // Draw main dot
                    val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    mainPaint.color = baseColor
                    mainPaint.alpha = alpha
                    drawDot(canvas, cx, cy, radius, mainPaint, settings.dotShape)
                }

                DotStyle.NEON -> {
                    // Multiple glow layers for neon effect
                    for (i in 3 downTo 1) {
                        val glowAlpha = (alpha * 0.15f * i).toInt()
                        val glowSize = radius + (effectSettings.glowRadius * i / 2)

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
                    drawDot(canvas, cx, cy, radius * 0.7f, centerPaint, settings.dotShape)
                }

                DotStyle.EMBOSSED -> {
                    // Shadow behind (offset)
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    shadowPaint.color = darkenColor(baseColor, 0.5f)
                    shadowPaint.alpha = (alpha * 0.5f).toInt()
                    drawDot(canvas, cx + 2f, cy + 2f, radius, shadowPaint, settings.dotShape)

                    // Highlight on top-left
                    val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    highlightPaint.color = lightenColor(baseColor, 0.3f)
                    highlightPaint.alpha = alpha
                    drawDot(canvas, cx, cy, radius, highlightPaint, settings.dotShape)

                    // Main dot slightly inset
                    val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    mainPaint.color = baseColor
                    mainPaint.alpha = alpha
                    drawDot(canvas, cx, cy, radius * 0.9f, mainPaint, settings.dotShape)
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
