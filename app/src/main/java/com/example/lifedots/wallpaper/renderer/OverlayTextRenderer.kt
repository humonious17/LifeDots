package com.example.lifedots.wallpaper.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.example.lifedots.preferences.CustomYearSettings
import com.example.lifedots.preferences.FooterTextSettings
import com.example.lifedots.preferences.GoalPosition
import com.example.lifedots.preferences.GoalSettings
import com.example.lifedots.preferences.LifeProgress
import com.example.lifedots.preferences.LifeSettings
import com.example.lifedots.preferences.ProgressPosition
import com.example.lifedots.preferences.ProgressSettings
import com.example.lifedots.preferences.TextAlignment
import com.example.lifedots.preferences.TimeScale
import com.example.lifedots.preferences.WallpaperSettings
import java.util.Calendar
import java.util.Locale

class OverlayTextRenderer {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun calculateTopOffset(width: Int, height: Int, settings: WallpaperSettings): Float {
        var offset = height * 0.05f
        if (settings.timeScale == TimeScale.LIFE && settings.lifeSettings.showTitle) {
            offset += 70f
        }
        if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.TOP) {
            offset += 80f + (settings.goalSettings.goals.size * 30f)
        }
        if (settings.progressSettings.enabled && settings.progressSettings.position == ProgressPosition.TOP) {
            offset += settings.progressSettings.fontSize * 3 + 30f
        }
        return offset
    }

    fun calculateBottomOffset(width: Int, height: Int, settings: WallpaperSettings): Float {
        var offset = height * 0.05f
        if (settings.timeScale == TimeScale.LIFE && settings.lifeSettings.showQuote) {
            offset += 160f
        }
        if (settings.footerTextSettings.enabled && settings.footerTextSettings.text.isNotEmpty()) {
            offset += 60f
        }
        if (settings.progressSettings.enabled && settings.progressSettings.position == ProgressPosition.BOTTOM) {
            offset += settings.progressSettings.fontSize * 3 + 30f
        }
        if (settings.goalSettings.enabled && settings.goalSettings.position == GoalPosition.BOTTOM) {
            offset += 80f + (settings.goalSettings.goals.size * 30f)
        }
        return offset
    }

    fun calculateLifeQuoteHeight(canvas: Canvas, lifeSettings: LifeSettings): Float {
        if (!lifeSettings.showQuote || lifeSettings.quoteText.isEmpty()) return 0f
        textPaint.textSize = 21f
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        val maxTextWidth = canvas.width * 0.86f
        val words = lifeSettings.quoteText.split(" ")
        var lineCount = 0
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (textPaint.measureText(testLine) <= maxTextWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lineCount++
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lineCount++
        val lineHeight = textPaint.textSize * 1.35f
        val authorHeight = if (lifeSettings.quoteAuthor.isNotEmpty()) lineHeight + 10f else 0f
        return (lineCount * lineHeight) + authorHeight
    }

    fun drawLifeTitle(canvas: Canvas, lifeSettings: LifeSettings, colors: ThemeColors, y: Float) {
        if (!lifeSettings.showTitle || lifeSettings.titleText.isEmpty()) return

        textPaint.color = colors.filledDot
        textPaint.textSize = 42f
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        textPaint.letterSpacing = 0.22f

        val textWidth = textPaint.measureText(lifeSettings.titleText)
        val x = (canvas.width - textWidth) / 2f

        canvas.drawText(lifeSettings.titleText, x, y, textPaint)
        textPaint.letterSpacing = 0f
    }

    fun drawLifeQuote(canvas: Canvas, lifeSettings: LifeSettings, colors: ThemeColors, y: Float) {
        if (!lifeSettings.showQuote || lifeSettings.quoteText.isEmpty()) return

        textPaint.color = colors.emptyDot
        textPaint.textSize = 21f
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)

        val maxTextWidth = canvas.width * 0.86f
        val words = lifeSettings.quoteText.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (textPaint.measureText(testLine) <= maxTextWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        var lineY = y
        val lineHeight = textPaint.textSize * 1.35f

        for (line in lines) {
            val textWidth = textPaint.measureText(line)
            val x = (canvas.width - textWidth) / 2f
            canvas.drawText(line, x, lineY, textPaint)
            lineY += lineHeight
        }

        if (lifeSettings.quoteAuthor.isNotEmpty()) {
            lineY += 6f
            textPaint.textSize = 19f
            textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textPaint.letterSpacing = 0.15f
            val authorText = "— ${lifeSettings.quoteAuthor.uppercase()} —"
            val textWidth = textPaint.measureText(authorText)
            val x = (canvas.width - textWidth) / 2f
            canvas.drawText(authorText, x, lineY, textPaint)
            textPaint.letterSpacing = 0f
        }
    }

    fun drawFooterText(
        canvas: Canvas,
        footerSettings: FooterTextSettings,
        customYearSettings: CustomYearSettings,
        dayOfYear: Int,
        totalDays: Int,
        lifeProgress: LifeProgress?,
        y: Float
    ) {
        if (footerSettings.text.isEmpty()) return

        val remainingDays = (totalDays - dayOfYear).coerceAtLeast(0)
        val year = if (customYearSettings.enabled) {
            val startYr = Calendar.getInstance().apply { timeInMillis = customYearSettings.startDate }.get(Calendar.YEAR)
            val endYr = Calendar.getInstance().apply { timeInMillis = customYearSettings.endDate }.get(Calendar.YEAR)
            if (startYr == endYr) "$startYr" else "$startYr-$endYr"
        } else {
            Calendar.getInstance().get(Calendar.YEAR).toString()
        }

        val percent = if (lifeProgress != null) {
            String.format(Locale.US, "%.1f", lifeProgress.percentLived)
        } else {
            String.format(Locale.US, "%.1f", (dayOfYear.toFloat() / totalDays.toFloat()) * 100f)
        }

        val remaining = if (lifeProgress != null) lifeProgress.weeksRemaining.toString() else remainingDays.toString()
        val passed = if (lifeProgress != null) lifeProgress.weeksLived.toString() else dayOfYear.toString()
        val total = if (lifeProgress != null) lifeProgress.totalWeeks.toString() else totalDays.toString()
        val age = lifeProgress?.ageYears?.toString() ?: ""

        val resolvedText = footerSettings.text
            .replace("{percent}", percent)
            .replace("{remaining}", remaining)
            .replace("{passed}", passed)
            .replace("{total}", total)
            .replace("{year}", year)
            .replace("{age}", age)

        textPaint.color = footerSettings.color
        textPaint.textSize = footerSettings.fontSize * 3  // Scale for wallpaper
        textPaint.typeface = Typeface.DEFAULT

        val textWidth = textPaint.measureText(resolvedText)
        val x = when (footerSettings.alignment) {
            TextAlignment.LEFT -> 40f
            TextAlignment.CENTER -> (canvas.width - textWidth) / 2
            TextAlignment.RIGHT -> canvas.width - textWidth - 40f
        }

        canvas.drawText(resolvedText, x, y, textPaint)
    }

    fun drawProgress(
        canvas: Canvas,
        progressSettings: ProgressSettings,
        dayOfYear: Int,
        totalDays: Int,
        lifeProgress: LifeProgress?,
        y: Float
    ) {
        if (!progressSettings.enabled) return

        val text = progressSettings.formatText(dayOfYear, totalDays, lifeProgress)
        if (text.isEmpty()) return

        textPaint.color = progressSettings.color
        textPaint.textSize = progressSettings.fontSize * 3  // Scale for wallpaper
        textPaint.typeface = Typeface.DEFAULT

        val textWidth = textPaint.measureText(text)
        val x = when (progressSettings.alignment) {
            TextAlignment.LEFT -> 40f
            TextAlignment.CENTER -> (canvas.width - textWidth) / 2
            TextAlignment.RIGHT -> canvas.width - textWidth - 40f
        }

        canvas.drawText(text, x, y, textPaint)
    }

    fun drawGoals(
        canvas: Canvas,
        goalSettings: GoalSettings,
        colors: ThemeColors,
        startY: Float,
        width: Float
    ) {
        if (goalSettings.goals.isEmpty()) return

        val now = System.currentTimeMillis()
        var yOffset = startY + 50f

        for (goal in goalSettings.goals) {
            val daysRemaining = goal.calculateDaysRemaining(now)

            val text = when {
                daysRemaining > 1 -> "$daysRemaining days until ${goal.title}"
                daysRemaining == 1 -> "1 day until ${goal.title}"
                daysRemaining == 0 -> "Today: ${goal.title}!"
                daysRemaining == -1 -> "1 day since ${goal.title}"
                else -> "${-daysRemaining} days since ${goal.title}"
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
}
