package com.example.lifedots.preferences

import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

// Time Scale: Year vs Life
enum class TimeScale {
    YEAR,  // 365/366 days of current year (default)
    LIFE   // Life in Weeks (Memento Mori)
}

data class LifeProgress(
    val ageYears: Int,
    val weekInCurrentYear: Int,
    val currentDotIndex: Int,
    val totalWeeks: Int,
    val weeksLived: Int,
    val weeksRemaining: Int,
    val percentLived: Float
)

data class LifeSettings(
    val birthDate: Long = defaultBirthDate(),
    val lifeExpectancyYears: Int = 80,
    val showTitle: Boolean = true,
    val titleText: String = "MEMENTO MORI",
    val showYearLabels: Boolean = true,
    val splitHalves: Boolean = true,
    val showQuote: Boolean = true,
    val quoteText: String = DEFAULT_SENECA_QUOTE,
    val quoteAuthor: String = "SENECA"
) {
    companion object {
        const val DEFAULT_SENECA_QUOTE = "It is not that we have a short time to live, but that we waste much of it. Life is long enough, and it has been given in sufficiently generous measure to allow the accomplishment of the very greatest things if the whole of it is well invested."

        fun defaultBirthDate(): Long {
            return Calendar.getInstance().apply {
                set(2000, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }

    fun calculateProgress(): LifeProgress {
        val birthCal = Calendar.getInstance().apply { timeInMillis = birthDate }
        val nowCal = Calendar.getInstance()

        var ageYears = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        val hasHadBirthdayThisYear = (nowCal.get(Calendar.MONTH) > birthCal.get(Calendar.MONTH)) ||
            (nowCal.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH) && nowCal.get(Calendar.DAY_OF_MONTH) >= birthCal.get(Calendar.DAY_OF_MONTH))

        if (!hasHadBirthdayThisYear) {
            ageYears--
        }
        ageYears = ageYears.coerceAtLeast(0)

        val lastBirthdayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, birthCal.get(Calendar.YEAR) + ageYears)
            set(Calendar.MONTH, birthCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, birthCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val daysSinceLastBirthday = ((nowCal.timeInMillis - lastBirthdayCal.timeInMillis) / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        val weekInCurrentYear = (daysSinceLastBirthday / 7).coerceIn(0, 51)

        val totalWeeks = lifeExpectancyYears * 52
        val currentDotIndex = (ageYears * 52 + weekInCurrentYear).coerceIn(0, totalWeeks)
        val weeksLived = currentDotIndex
        val weeksRemaining = (totalWeeks - currentDotIndex - 1).coerceAtLeast(0)
        val percentLived = if (totalWeeks > 0) (currentDotIndex.toFloat() / totalWeeks.toFloat()) * 100f else 0f

        return LifeProgress(
            ageYears = ageYears,
            weekInCurrentYear = weekInCurrentYear,
            currentDotIndex = currentDotIndex,
            totalWeeks = totalWeeks,
            weeksLived = weeksLived,
            weeksRemaining = weeksRemaining,
            percentLived = percentLived
        )
    }
}

// Custom Year / Academic Year
data class CustomRangeProgress(
    val dayIndex: Int,
    val totalDays: Int,
    val daysRemaining: Int,
    val percentCompleted: Float,
    val isBeforeStart: Boolean,
    val isAfterEnd: Boolean,
    val isTodayInRange: Boolean
) {
    val percentage: Float get() = percentCompleted
}

data class CustomYearSettings(
    val enabled: Boolean = false,
    val startDate: Long = defaultStartDate(),
    val endDate: Long = defaultEndDate()
) {
    companion object {
        fun defaultStartDate(): Long {
            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)
            val currentMonth = now.get(Calendar.MONTH) // 0-indexed, Sep is 8
            val startYear = if (currentMonth < Calendar.SEPTEMBER) currentYear - 1 else currentYear
            return Calendar.getInstance().apply {
                set(startYear, Calendar.SEPTEMBER, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        fun defaultEndDate(): Long {
            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)
            val currentMonth = now.get(Calendar.MONTH)
            val endYear = if (currentMonth >= Calendar.SEPTEMBER) currentYear + 1 else currentYear
            return Calendar.getInstance().apply {
                set(endYear, Calendar.MAY, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        }

        fun createAcademicYearRange(): Pair<Long, Long> {
            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)
            val currentMonth = now.get(Calendar.MONTH)
            val startYear = if (currentMonth < Calendar.SEPTEMBER) currentYear - 1 else currentYear
            val start = Calendar.getInstance().apply {
                set(startYear, Calendar.SEPTEMBER, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = Calendar.getInstance().apply {
                set(startYear + 1, Calendar.MAY, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            return Pair(start, end)
        }

        fun createSchoolYearRange(): Pair<Long, Long> {
            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)
            val currentMonth = now.get(Calendar.MONTH)
            val startYear = if (currentMonth < Calendar.AUGUST) currentYear - 1 else currentYear
            val start = Calendar.getInstance().apply {
                set(startYear, Calendar.AUGUST, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = Calendar.getInstance().apply {
                set(startYear + 1, Calendar.JUNE, 30, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            return Pair(start, end)
        }

        fun createCalendarYearRange(): Pair<Long, Long> {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val start = Calendar.getInstance().apply {
                set(currentYear, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = Calendar.getInstance().apply {
                set(currentYear, Calendar.DECEMBER, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            return Pair(start, end)
        }
    }

    fun calculateProgress(nowMillis: Long = System.currentTimeMillis()): CustomRangeProgress {
        val todayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startCal = Calendar.getInstance().apply {
            timeInMillis = startDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = endDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val totalDiffMillis = endCal.timeInMillis - startCal.timeInMillis
        val totalDays = (kotlin.math.round(totalDiffMillis.toDouble() / (1000.0 * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(1)

        val daysSinceStart = kotlin.math.round((todayCal.timeInMillis - startCal.timeInMillis).toDouble() / (1000.0 * 60 * 60 * 24)).toInt()

        val isBeforeStart = todayCal.timeInMillis < startCal.timeInMillis
        val isAfterEnd = todayCal.timeInMillis > endCal.timeInMillis

        val dayIndex = when {
            isBeforeStart -> 0
            isAfterEnd -> totalDays
            else -> daysSinceStart + 1
        }

        val daysRemaining = (totalDays - dayIndex).coerceAtLeast(0)
        val percent = if (totalDays > 0) ((dayIndex.toFloat() / totalDays.toFloat()) * 100f).coerceIn(0f, 100f) else 0f

        return CustomRangeProgress(
            dayIndex = dayIndex,
            totalDays = totalDays,
            daysRemaining = daysRemaining,
            percentCompleted = percent,
            isBeforeStart = isBeforeStart,
            isAfterEnd = isAfterEnd,
            isTodayInRange = !isBeforeStart && !isAfterEnd
        )
    }
}

// Feature: Year Progress & Remaining Days
enum class ProgressPosition {
    TOP, BOTTOM
}

data class ProgressSettings(
    val enabled: Boolean = false,
    val showPercentage: Boolean = true,
    val showRemainingDays: Boolean = true,
    val showDaysPassed: Boolean = false,
    val decimalPlaces: Int = 1,
    val fontSize: Float = 14f,
    val color: Int = 0xFFFFFFFF.toInt(),
    val alignment: TextAlignment = TextAlignment.CENTER,
    val position: ProgressPosition = ProgressPosition.BOTTOM
) {
    fun formatText(dayOfYear: Int, totalDays: Int, lifeProgress: LifeProgress? = null): String {
        if (lifeProgress != null) {
            val percentage = lifeProgress.percentLived
            val formattedPercent = if (decimalPlaces <= 0) {
                "${percentage.roundToInt()}%"
            } else {
                String.format(Locale.US, "%.${decimalPlaces}f%%", percentage)
            }

            val parts = mutableListOf<String>()

            if (showDaysPassed && !showPercentage && !showRemainingDays) {
                parts.add("Week ${lifeProgress.weeksLived + 1} of ${lifeProgress.totalWeeks}")
            } else if (showDaysPassed) {
                parts.add("Wk ${lifeProgress.weeksLived + 1}/${lifeProgress.totalWeeks}")
            }

            if (showPercentage) {
                if (!showRemainingDays && !showDaysPassed) {
                    parts.add("$formattedPercent lived")
                } else {
                    parts.add(formattedPercent)
                }
            }

            if (showRemainingDays) {
                if (!showPercentage && !showDaysPassed) {
                    val weeksText = if (lifeProgress.weeksRemaining == 1) "1 week remaining" else "${lifeProgress.weeksRemaining} weeks remaining"
                    parts.add(weeksText)
                } else {
                    val weeksText = if (lifeProgress.weeksRemaining == 1) "1 week left" else "${lifeProgress.weeksRemaining} weeks left"
                    parts.add(weeksText)
                }
            }

            if (parts.isEmpty()) {
                return "$formattedPercent • ${if (lifeProgress.weeksRemaining == 1) "1 week left" else "${lifeProgress.weeksRemaining} weeks left"}"
            }

            return parts.joinToString(" • ")
        }

        val remainingDays = (totalDays - dayOfYear).coerceAtLeast(0)
        val percentage = (dayOfYear.toFloat() / totalDays.toFloat()) * 100f
        val formattedPercent = if (decimalPlaces <= 0) {
            "${percentage.roundToInt()}%"
        } else {
            String.format(Locale.US, "%.${decimalPlaces}f%%", percentage)
        }

        val parts = mutableListOf<String>()

        if (showDaysPassed && !showPercentage && !showRemainingDays) {
            parts.add("Day $dayOfYear of $totalDays")
        } else if (showDaysPassed) {
            parts.add("Day $dayOfYear/$totalDays")
        }

        if (showPercentage) {
            if (!showRemainingDays && !showDaysPassed) {
                parts.add("$formattedPercent completed")
            } else {
                parts.add(formattedPercent)
            }
        }

        if (showRemainingDays) {
            if (!showPercentage && !showDaysPassed) {
                val daysText = if (remainingDays == 1) "1 day remaining" else "$remainingDays days remaining"
                parts.add(daysText)
            } else {
                val daysText = if (remainingDays == 1) "1 day left" else "$remainingDays days left"
                parts.add(daysText)
            }
        }

        if (parts.isEmpty()) {
            return "$formattedPercent • ${if (remainingDays == 1) "1 day left" else "$remainingDays days left"}"
        }

        return parts.joinToString(" • ")
    }
}

enum class ThemeOption {
    LIGHT, DARK, AMOLED, CUSTOM
}

enum class DotSize {
    TINY, SMALL, MEDIUM, LARGE, HUGE
}

enum class DotShape {
    CIRCLE, SQUARE, ROUNDED_SQUARE, DIAMOND
}

enum class GridDensity {
    COMPACT, NORMAL, RELAXED, SPACIOUS
}

// Feature 3: Dot Effects
enum class DotStyle {
    FLAT, GRADIENT, OUTLINED, SOFT_GLOW, NEON, EMBOSSED
}

data class DotEffectSettings(
    val style: DotStyle = DotStyle.FLAT,
    val glowRadius: Float = 8f,
    val outlineWidth: Float = 2f
)

// Feature 2: Footer Text
enum class TextAlignment {
    LEFT, CENTER, RIGHT
}

data class FooterTextSettings(
    val enabled: Boolean = false,
    val text: String = "",
    val fontSize: Float = 14f,
    val color: Int = 0xFFFFFFFF.toInt(),
    val alignment: TextAlignment = TextAlignment.CENTER
)

// Features 4 & 5: View Modes
enum class ViewMode {
    CONTINUOUS, MONTHLY, CALENDAR
}

data class ViewModeSettings(
    val mode: ViewMode = ViewMode.CONTINUOUS,
    val showMonthLabels: Boolean = true,
    val monthLabelColor: Int = 0xFFFFFFFF.toInt()
)

data class CalendarViewSettings(
    val columnsPerRow: Int = 3  // 3x4 or 4x3 grid
)

// Feature 1: Background Photo
data class BackgroundSettings(
    val enabled: Boolean = false,
    val imageUri: String? = null,
    val opacity: Float = 0.3f,
    val blurRadius: Float = 0f
)

// Feature 6: Goal Tracking
enum class GoalPosition {
    TOP, BOTTOM
}

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetDate: Long,
    val color: Int = 0xFF5BA0E9.toInt()
) {
    fun calculateDaysRemaining(nowMillis: Long = System.currentTimeMillis()): Int {
        val todayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
        return kotlin.math.round(diffMillis.toDouble() / (1000.0 * 60 * 60 * 24)).toInt()
    }
}

data class GoalSettings(
    val enabled: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val position: GoalPosition = GoalPosition.TOP
)

data class CustomColors(
    val backgroundColor: Int = 0xFF1A1A1A.toInt(),
    val filledDotColor: Int = 0xFFE0E0E0.toInt(),
    val emptyDotColor: Int = 0xFF3A3A3A.toInt(),
    val todayDotColor: Int = 0xFF5BA0E9.toInt()
)

// Custom Positioning & Scaling
data class PositionSettings(
    val horizontalOffset: Float = 0f,  // -50 to 50 percent
    val verticalOffset: Float = 0f,    // -50 to 50 percent
    val scale: Float = 1.0f            // 0.5 to 1.5
)

// Animation Types
enum class AnimationType {
    NONE,
    FADE_IN,
    PULSE,
    WAVE,
    BREATHE,
    RIPPLE,
    CASCADE
}

data class AnimationSettings(
    val enabled: Boolean = false,
    val type: AnimationType = AnimationType.NONE,
    val speed: Float = 1.0f,           // 0.5 to 2.0
    val intensity: Float = 0.5f        // 0.1 to 1.0
)

// Glass/Frosted Effect
enum class GlassStyle {
    NONE,
    LIGHT_FROST,
    HEAVY_FROST,
    ACRYLIC,
    CRYSTAL,
    ICE
}

data class GlassEffectSettings(
    val enabled: Boolean = false,
    val style: GlassStyle = GlassStyle.NONE,
    val blur: Float = 10f,             // 0 to 25
    val opacity: Float = 0.3f,         // 0.1 to 0.9
    val tint: Int = 0x80FFFFFF.toInt() // Tint color with alpha
)

// Tree Growth Effect
enum class TreeStyle {
    SIMPLE,
    DETAILED,
    BONSAI,
    SAKURA,
    WILLOW
}

data class TreeEffectSettings(
    val enabled: Boolean = false,
    val style: TreeStyle = TreeStyle.SIMPLE,
    val trunkColor: Int = 0xFF8B4513.toInt(),
    val leafColor: Int = 0xFF228B22.toInt(),
    val bloomColor: Int = 0xFFFF69B4.toInt(),
    val showGround: Boolean = true
)

// Fluid/Liquid Effects
enum class FluidStyle {
    NONE,
    WATER,
    LAVA,
    MERCURY,
    PLASMA,
    AURORA
}

data class FluidEffectSettings(
    val enabled: Boolean = false,
    val style: FluidStyle = FluidStyle.NONE,
    val flowSpeed: Float = 1.0f,
    val turbulence: Float = 0.5f,
    val colorIntensity: Float = 0.7f
)

// Special Visual Mode combining multiple effects
enum class VisualTheme {
    CLASSIC,           // Default dot grid
    MINIMALIST,        // Clean, simple
    CYBERPUNK,         // Neon, glowing
    NATURE,            // Tree growth
    FLUID,             // Liquid effects
    GLASS,             // Frosted glass
    COSMIC             // Space-themed
}

data class WallpaperSettings(
    val timeScale: TimeScale = TimeScale.YEAR,
    val lifeSettings: LifeSettings = LifeSettings(),
    val theme: ThemeOption = ThemeOption.DARK,
    val dotSize: DotSize = DotSize.MEDIUM,
    val dotShape: DotShape = DotShape.CIRCLE,
    val gridDensity: GridDensity = GridDensity.COMPACT,
    val highlightToday: Boolean = true,
    val filledDotAlpha: Float = 1.0f,
    val emptyDotAlpha: Float = 1.0f,
    val customColors: CustomColors = CustomColors(),
    // Feature settings
    val dotEffectSettings: DotEffectSettings = DotEffectSettings(),
    val footerTextSettings: FooterTextSettings = FooterTextSettings(),
    val viewModeSettings: ViewModeSettings = ViewModeSettings(),
    val calendarViewSettings: CalendarViewSettings = CalendarViewSettings(),
    val backgroundSettings: BackgroundSettings = BackgroundSettings(),
    val goalSettings: GoalSettings = GoalSettings(),
    val progressSettings: ProgressSettings = ProgressSettings(),
    // Advanced feature settings
    val positionSettings: PositionSettings = PositionSettings(),
    val animationSettings: AnimationSettings = AnimationSettings(),
    val glassEffectSettings: GlassEffectSettings = GlassEffectSettings(),
    val treeEffectSettings: TreeEffectSettings = TreeEffectSettings(),
    val fluidEffectSettings: FluidEffectSettings = FluidEffectSettings(),
    val visualTheme: VisualTheme = VisualTheme.CLASSIC,
    val customYearSettings: CustomYearSettings = CustomYearSettings()
)
