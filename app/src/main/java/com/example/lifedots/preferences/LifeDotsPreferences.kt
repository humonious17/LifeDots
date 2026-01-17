package com.example.lifedots.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

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
)

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

data class WallpaperSettings(
    val theme: ThemeOption = ThemeOption.DARK,
    val dotSize: DotSize = DotSize.MEDIUM,
    val dotShape: DotShape = DotShape.CIRCLE,
    val gridDensity: GridDensity = GridDensity.COMPACT,
    val highlightToday: Boolean = true,
    val filledDotAlpha: Float = 1.0f,
    val emptyDotAlpha: Float = 1.0f,
    val customColors: CustomColors = CustomColors(),
    // New feature settings
    val dotEffectSettings: DotEffectSettings = DotEffectSettings(),
    val footerTextSettings: FooterTextSettings = FooterTextSettings(),
    val viewModeSettings: ViewModeSettings = ViewModeSettings(),
    val calendarViewSettings: CalendarViewSettings = CalendarViewSettings(),
    val backgroundSettings: BackgroundSettings = BackgroundSettings(),
    val goalSettings: GoalSettings = GoalSettings()
)

class LifeDotsPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<WallpaperSettings> = _settingsFlow.asStateFlow()

    val settings: WallpaperSettings
        get() = _settingsFlow.value

    private fun loadSettings(): WallpaperSettings {
        val customColors = CustomColors(
            backgroundColor = prefs.getInt(KEY_CUSTOM_BG_COLOR, 0xFF1A1A1A.toInt()),
            filledDotColor = prefs.getInt(KEY_CUSTOM_FILLED_COLOR, 0xFFE0E0E0.toInt()),
            emptyDotColor = prefs.getInt(KEY_CUSTOM_EMPTY_COLOR, 0xFF3A3A3A.toInt()),
            todayDotColor = prefs.getInt(KEY_CUSTOM_TODAY_COLOR, 0xFF5BA0E9.toInt())
        )

        // Feature 3: Dot Effects
        val dotEffectSettings = DotEffectSettings(
            style = DotStyle.valueOf(prefs.getString(KEY_DOT_STYLE, DotStyle.FLAT.name) ?: DotStyle.FLAT.name),
            glowRadius = prefs.getFloat(KEY_GLOW_RADIUS, 8f),
            outlineWidth = prefs.getFloat(KEY_OUTLINE_WIDTH, 2f)
        )

        // Feature 2: Footer Text
        val footerTextSettings = FooterTextSettings(
            enabled = prefs.getBoolean(KEY_FOOTER_ENABLED, false),
            text = prefs.getString(KEY_FOOTER_TEXT, "") ?: "",
            fontSize = prefs.getFloat(KEY_FOOTER_FONT_SIZE, 14f),
            color = prefs.getInt(KEY_FOOTER_COLOR, 0xFFFFFFFF.toInt()),
            alignment = TextAlignment.valueOf(prefs.getString(KEY_FOOTER_ALIGNMENT, TextAlignment.CENTER.name) ?: TextAlignment.CENTER.name)
        )

        // Features 4 & 5: View Modes
        val viewModeSettings = ViewModeSettings(
            mode = ViewMode.valueOf(prefs.getString(KEY_VIEW_MODE, ViewMode.CONTINUOUS.name) ?: ViewMode.CONTINUOUS.name),
            showMonthLabels = prefs.getBoolean(KEY_SHOW_MONTH_LABELS, true),
            monthLabelColor = prefs.getInt(KEY_MONTH_LABEL_COLOR, 0xFFFFFFFF.toInt())
        )

        val calendarViewSettings = CalendarViewSettings(
            columnsPerRow = prefs.getInt(KEY_CALENDAR_COLUMNS, 3)
        )

        // Feature 1: Background Photo
        val backgroundSettings = BackgroundSettings(
            enabled = prefs.getBoolean(KEY_BACKGROUND_ENABLED, false),
            imageUri = prefs.getString(KEY_BACKGROUND_URI, null),
            opacity = prefs.getFloat(KEY_BACKGROUND_OPACITY, 0.3f),
            blurRadius = prefs.getFloat(KEY_BACKGROUND_BLUR, 0f)
        )

        // Feature 6: Goal Tracking
        val goalsJson = prefs.getString(KEY_GOALS_JSON, "[]") ?: "[]"
        val goalsType = object : TypeToken<List<Goal>>() {}.type
        val goals: List<Goal> = try {
            gson.fromJson(goalsJson, goalsType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val goalSettings = GoalSettings(
            enabled = prefs.getBoolean(KEY_GOALS_ENABLED, false),
            goals = goals,
            position = GoalPosition.valueOf(prefs.getString(KEY_GOALS_POSITION, GoalPosition.TOP.name) ?: GoalPosition.TOP.name)
        )

        return WallpaperSettings(
            theme = ThemeOption.valueOf(prefs.getString(KEY_THEME, ThemeOption.DARK.name) ?: ThemeOption.DARK.name),
            dotSize = DotSize.valueOf(prefs.getString(KEY_DOT_SIZE, DotSize.MEDIUM.name) ?: DotSize.MEDIUM.name),
            dotShape = DotShape.valueOf(prefs.getString(KEY_DOT_SHAPE, DotShape.CIRCLE.name) ?: DotShape.CIRCLE.name),
            gridDensity = GridDensity.valueOf(prefs.getString(KEY_GRID_DENSITY, GridDensity.COMPACT.name) ?: GridDensity.COMPACT.name),
            highlightToday = prefs.getBoolean(KEY_HIGHLIGHT_TODAY, true),
            filledDotAlpha = prefs.getFloat(KEY_FILLED_DOT_ALPHA, 1.0f),
            emptyDotAlpha = prefs.getFloat(KEY_EMPTY_DOT_ALPHA, 1.0f),
            customColors = customColors,
            dotEffectSettings = dotEffectSettings,
            footerTextSettings = footerTextSettings,
            viewModeSettings = viewModeSettings,
            calendarViewSettings = calendarViewSettings,
            backgroundSettings = backgroundSettings,
            goalSettings = goalSettings
        )
    }

    fun setTheme(theme: ThemeOption) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(theme = theme)
        notifyWallpaperChanged()
    }

    fun setDotSize(size: DotSize) {
        prefs.edit().putString(KEY_DOT_SIZE, size.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(dotSize = size)
        notifyWallpaperChanged()
    }

    fun setDotShape(shape: DotShape) {
        prefs.edit().putString(KEY_DOT_SHAPE, shape.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(dotShape = shape)
        notifyWallpaperChanged()
    }

    fun setGridDensity(density: GridDensity) {
        prefs.edit().putString(KEY_GRID_DENSITY, density.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(gridDensity = density)
        notifyWallpaperChanged()
    }

    fun setHighlightToday(highlight: Boolean) {
        prefs.edit().putBoolean(KEY_HIGHLIGHT_TODAY, highlight).apply()
        _settingsFlow.value = _settingsFlow.value.copy(highlightToday = highlight)
        notifyWallpaperChanged()
    }

    fun setFilledDotAlpha(alpha: Float) {
        prefs.edit().putFloat(KEY_FILLED_DOT_ALPHA, alpha).apply()
        _settingsFlow.value = _settingsFlow.value.copy(filledDotAlpha = alpha)
        notifyWallpaperChanged()
    }

    fun setEmptyDotAlpha(alpha: Float) {
        prefs.edit().putFloat(KEY_EMPTY_DOT_ALPHA, alpha).apply()
        _settingsFlow.value = _settingsFlow.value.copy(emptyDotAlpha = alpha)
        notifyWallpaperChanged()
    }

    fun setCustomBackgroundColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_BG_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(backgroundColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    fun setCustomFilledDotColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_FILLED_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(filledDotColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    fun setCustomEmptyDotColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_EMPTY_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(emptyDotColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    fun setCustomTodayDotColor(color: Int) {
        prefs.edit().putInt(KEY_CUSTOM_TODAY_COLOR, color).apply()
        val newCustomColors = _settingsFlow.value.customColors.copy(todayDotColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(customColors = newCustomColors)
        notifyWallpaperChanged()
    }

    // Feature 3: Dot Effects setters
    fun setDotStyle(style: DotStyle) {
        prefs.edit().putString(KEY_DOT_STYLE, style.name).apply()
        val newDotEffects = _settingsFlow.value.dotEffectSettings.copy(style = style)
        _settingsFlow.value = _settingsFlow.value.copy(dotEffectSettings = newDotEffects)
        notifyWallpaperChanged()
    }

    fun setGlowRadius(radius: Float) {
        prefs.edit().putFloat(KEY_GLOW_RADIUS, radius).apply()
        val newDotEffects = _settingsFlow.value.dotEffectSettings.copy(glowRadius = radius)
        _settingsFlow.value = _settingsFlow.value.copy(dotEffectSettings = newDotEffects)
        notifyWallpaperChanged()
    }

    fun setOutlineWidth(width: Float) {
        prefs.edit().putFloat(KEY_OUTLINE_WIDTH, width).apply()
        val newDotEffects = _settingsFlow.value.dotEffectSettings.copy(outlineWidth = width)
        _settingsFlow.value = _settingsFlow.value.copy(dotEffectSettings = newDotEffects)
        notifyWallpaperChanged()
    }

    // Feature 2: Footer Text setters
    fun setFooterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOOTER_ENABLED, enabled).apply()
        val newFooter = _settingsFlow.value.footerTextSettings.copy(enabled = enabled)
        _settingsFlow.value = _settingsFlow.value.copy(footerTextSettings = newFooter)
        notifyWallpaperChanged()
    }

    fun setFooterText(text: String) {
        prefs.edit().putString(KEY_FOOTER_TEXT, text).apply()
        val newFooter = _settingsFlow.value.footerTextSettings.copy(text = text)
        _settingsFlow.value = _settingsFlow.value.copy(footerTextSettings = newFooter)
        notifyWallpaperChanged()
    }

    fun setFooterFontSize(size: Float) {
        prefs.edit().putFloat(KEY_FOOTER_FONT_SIZE, size).apply()
        val newFooter = _settingsFlow.value.footerTextSettings.copy(fontSize = size)
        _settingsFlow.value = _settingsFlow.value.copy(footerTextSettings = newFooter)
        notifyWallpaperChanged()
    }

    fun setFooterColor(color: Int) {
        prefs.edit().putInt(KEY_FOOTER_COLOR, color).apply()
        val newFooter = _settingsFlow.value.footerTextSettings.copy(color = color)
        _settingsFlow.value = _settingsFlow.value.copy(footerTextSettings = newFooter)
        notifyWallpaperChanged()
    }

    fun setFooterAlignment(alignment: TextAlignment) {
        prefs.edit().putString(KEY_FOOTER_ALIGNMENT, alignment.name).apply()
        val newFooter = _settingsFlow.value.footerTextSettings.copy(alignment = alignment)
        _settingsFlow.value = _settingsFlow.value.copy(footerTextSettings = newFooter)
        notifyWallpaperChanged()
    }

    // Features 4 & 5: View Mode setters
    fun setViewMode(mode: ViewMode) {
        prefs.edit().putString(KEY_VIEW_MODE, mode.name).apply()
        val newViewMode = _settingsFlow.value.viewModeSettings.copy(mode = mode)
        _settingsFlow.value = _settingsFlow.value.copy(viewModeSettings = newViewMode)
        notifyWallpaperChanged()
    }

    fun setShowMonthLabels(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_MONTH_LABELS, show).apply()
        val newViewMode = _settingsFlow.value.viewModeSettings.copy(showMonthLabels = show)
        _settingsFlow.value = _settingsFlow.value.copy(viewModeSettings = newViewMode)
        notifyWallpaperChanged()
    }

    fun setMonthLabelColor(color: Int) {
        prefs.edit().putInt(KEY_MONTH_LABEL_COLOR, color).apply()
        val newViewMode = _settingsFlow.value.viewModeSettings.copy(monthLabelColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(viewModeSettings = newViewMode)
        notifyWallpaperChanged()
    }

    fun setCalendarColumns(columns: Int) {
        prefs.edit().putInt(KEY_CALENDAR_COLUMNS, columns).apply()
        val newCalendar = _settingsFlow.value.calendarViewSettings.copy(columnsPerRow = columns)
        _settingsFlow.value = _settingsFlow.value.copy(calendarViewSettings = newCalendar)
        notifyWallpaperChanged()
    }

    // Feature 1: Background Photo setters
    fun setBackgroundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BACKGROUND_ENABLED, enabled).apply()
        val newBackground = _settingsFlow.value.backgroundSettings.copy(enabled = enabled)
        _settingsFlow.value = _settingsFlow.value.copy(backgroundSettings = newBackground)
        notifyWallpaperChanged()
    }

    fun setBackgroundUri(uri: String?) {
        prefs.edit().putString(KEY_BACKGROUND_URI, uri).apply()
        val newBackground = _settingsFlow.value.backgroundSettings.copy(imageUri = uri)
        _settingsFlow.value = _settingsFlow.value.copy(backgroundSettings = newBackground)
        notifyWallpaperChanged()
    }

    fun setBackgroundOpacity(opacity: Float) {
        prefs.edit().putFloat(KEY_BACKGROUND_OPACITY, opacity).apply()
        val newBackground = _settingsFlow.value.backgroundSettings.copy(opacity = opacity)
        _settingsFlow.value = _settingsFlow.value.copy(backgroundSettings = newBackground)
        notifyWallpaperChanged()
    }

    fun setBackgroundBlur(blur: Float) {
        prefs.edit().putFloat(KEY_BACKGROUND_BLUR, blur).apply()
        val newBackground = _settingsFlow.value.backgroundSettings.copy(blurRadius = blur)
        _settingsFlow.value = _settingsFlow.value.copy(backgroundSettings = newBackground)
        notifyWallpaperChanged()
    }

    // Feature 6: Goal Tracking setters
    fun setGoalsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GOALS_ENABLED, enabled).apply()
        val newGoals = _settingsFlow.value.goalSettings.copy(enabled = enabled)
        _settingsFlow.value = _settingsFlow.value.copy(goalSettings = newGoals)
        notifyWallpaperChanged()
    }

    fun setGoalsPosition(position: GoalPosition) {
        prefs.edit().putString(KEY_GOALS_POSITION, position.name).apply()
        val newGoals = _settingsFlow.value.goalSettings.copy(position = position)
        _settingsFlow.value = _settingsFlow.value.copy(goalSettings = newGoals)
        notifyWallpaperChanged()
    }

    fun addGoal(goal: Goal) {
        val currentGoals = _settingsFlow.value.goalSettings.goals.toMutableList()
        currentGoals.add(goal)
        saveGoals(currentGoals)
    }

    fun updateGoal(goal: Goal) {
        val currentGoals = _settingsFlow.value.goalSettings.goals.toMutableList()
        val index = currentGoals.indexOfFirst { it.id == goal.id }
        if (index >= 0) {
            currentGoals[index] = goal
            saveGoals(currentGoals)
        }
    }

    fun deleteGoal(goalId: String) {
        val currentGoals = _settingsFlow.value.goalSettings.goals.toMutableList()
        currentGoals.removeAll { it.id == goalId }
        saveGoals(currentGoals)
    }

    private fun saveGoals(goals: List<Goal>) {
        val goalsJson = gson.toJson(goals)
        prefs.edit().putString(KEY_GOALS_JSON, goalsJson).apply()
        val newGoalSettings = _settingsFlow.value.goalSettings.copy(goals = goals)
        _settingsFlow.value = _settingsFlow.value.copy(goalSettings = newGoalSettings)
        notifyWallpaperChanged()
    }

    private fun notifyWallpaperChanged() {
        wallpaperChangeListeners.forEach { it.invoke() }
    }

    companion object {
        private const val PREFS_NAME = "lifedots_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_DOT_SIZE = "dot_size"
        private const val KEY_DOT_SHAPE = "dot_shape"
        private const val KEY_GRID_DENSITY = "grid_density"
        private const val KEY_HIGHLIGHT_TODAY = "highlight_today"
        private const val KEY_FILLED_DOT_ALPHA = "filled_dot_alpha"
        private const val KEY_EMPTY_DOT_ALPHA = "empty_dot_alpha"
        private const val KEY_CUSTOM_BG_COLOR = "custom_bg_color"
        private const val KEY_CUSTOM_FILLED_COLOR = "custom_filled_color"
        private const val KEY_CUSTOM_EMPTY_COLOR = "custom_empty_color"
        private const val KEY_CUSTOM_TODAY_COLOR = "custom_today_color"

        // Feature 3: Dot Effects keys
        private const val KEY_DOT_STYLE = "dot_style"
        private const val KEY_GLOW_RADIUS = "glow_radius"
        private const val KEY_OUTLINE_WIDTH = "outline_width"

        // Feature 2: Footer Text keys
        private const val KEY_FOOTER_ENABLED = "footer_enabled"
        private const val KEY_FOOTER_TEXT = "footer_text"
        private const val KEY_FOOTER_FONT_SIZE = "footer_font_size"
        private const val KEY_FOOTER_COLOR = "footer_color"
        private const val KEY_FOOTER_ALIGNMENT = "footer_alignment"

        // Features 4 & 5: View Mode keys
        private const val KEY_VIEW_MODE = "view_mode"
        private const val KEY_SHOW_MONTH_LABELS = "show_month_labels"
        private const val KEY_MONTH_LABEL_COLOR = "month_label_color"
        private const val KEY_CALENDAR_COLUMNS = "calendar_columns"

        // Feature 1: Background Photo keys
        private const val KEY_BACKGROUND_ENABLED = "background_enabled"
        private const val KEY_BACKGROUND_URI = "background_uri"
        private const val KEY_BACKGROUND_OPACITY = "background_opacity"
        private const val KEY_BACKGROUND_BLUR = "background_blur"

        // Feature 6: Goal Tracking keys
        private const val KEY_GOALS_ENABLED = "goals_enabled"
        private const val KEY_GOALS_JSON = "goals_json"
        private const val KEY_GOALS_POSITION = "goals_position"

        private val wallpaperChangeListeners = mutableListOf<() -> Unit>()

        fun addWallpaperChangeListener(listener: () -> Unit) {
            wallpaperChangeListeners.add(listener)
        }

        fun removeWallpaperChangeListener(listener: () -> Unit) {
            wallpaperChangeListeners.remove(listener)
        }

        @Volatile
        private var instance: LifeDotsPreferences? = null

        fun getInstance(context: Context): LifeDotsPreferences {
            return instance ?: synchronized(this) {
                instance ?: LifeDotsPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
