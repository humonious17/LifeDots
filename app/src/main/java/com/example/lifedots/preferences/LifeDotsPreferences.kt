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

// ===== NEW ADVANCED FEATURES =====

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
    // Advanced feature settings
    val positionSettings: PositionSettings = PositionSettings(),
    val animationSettings: AnimationSettings = AnimationSettings(),
    val glassEffectSettings: GlassEffectSettings = GlassEffectSettings(),
    val treeEffectSettings: TreeEffectSettings = TreeEffectSettings(),
    val fluidEffectSettings: FluidEffectSettings = FluidEffectSettings(),
    val visualTheme: VisualTheme = VisualTheme.CLASSIC
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

        // Position Settings
        val positionSettings = PositionSettings(
            horizontalOffset = prefs.getFloat(KEY_HORIZONTAL_OFFSET, 0f),
            verticalOffset = prefs.getFloat(KEY_VERTICAL_OFFSET, 0f),
            scale = prefs.getFloat(KEY_SCALE, 1.0f)
        )

        // Animation Settings
        val animationSettings = AnimationSettings(
            enabled = prefs.getBoolean(KEY_ANIMATION_ENABLED, false),
            type = AnimationType.valueOf(prefs.getString(KEY_ANIMATION_TYPE, AnimationType.NONE.name) ?: AnimationType.NONE.name),
            speed = prefs.getFloat(KEY_ANIMATION_SPEED, 1.0f),
            intensity = prefs.getFloat(KEY_ANIMATION_INTENSITY, 0.5f)
        )

        // Glass Effect Settings
        val glassEffectSettings = GlassEffectSettings(
            enabled = prefs.getBoolean(KEY_GLASS_ENABLED, false),
            style = GlassStyle.valueOf(prefs.getString(KEY_GLASS_STYLE, GlassStyle.NONE.name) ?: GlassStyle.NONE.name),
            blur = prefs.getFloat(KEY_GLASS_BLUR, 10f),
            opacity = prefs.getFloat(KEY_GLASS_OPACITY, 0.3f),
            tint = prefs.getInt(KEY_GLASS_TINT, 0x80FFFFFF.toInt())
        )

        // Tree Effect Settings
        val treeEffectSettings = TreeEffectSettings(
            enabled = prefs.getBoolean(KEY_TREE_ENABLED, false),
            style = TreeStyle.valueOf(prefs.getString(KEY_TREE_STYLE, TreeStyle.SIMPLE.name) ?: TreeStyle.SIMPLE.name),
            trunkColor = prefs.getInt(KEY_TREE_TRUNK_COLOR, 0xFF8B4513.toInt()),
            leafColor = prefs.getInt(KEY_TREE_LEAF_COLOR, 0xFF228B22.toInt()),
            bloomColor = prefs.getInt(KEY_TREE_BLOOM_COLOR, 0xFFFF69B4.toInt()),
            showGround = prefs.getBoolean(KEY_TREE_SHOW_GROUND, true)
        )

        // Fluid Effect Settings
        val fluidEffectSettings = FluidEffectSettings(
            enabled = prefs.getBoolean(KEY_FLUID_ENABLED, false),
            style = FluidStyle.valueOf(prefs.getString(KEY_FLUID_STYLE, FluidStyle.NONE.name) ?: FluidStyle.NONE.name),
            flowSpeed = prefs.getFloat(KEY_FLUID_FLOW_SPEED, 1.0f),
            turbulence = prefs.getFloat(KEY_FLUID_TURBULENCE, 0.5f),
            colorIntensity = prefs.getFloat(KEY_FLUID_COLOR_INTENSITY, 0.7f)
        )

        val visualTheme = VisualTheme.valueOf(prefs.getString(KEY_VISUAL_THEME, VisualTheme.CLASSIC.name) ?: VisualTheme.CLASSIC.name)

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
            goalSettings = goalSettings,
            positionSettings = positionSettings,
            animationSettings = animationSettings,
            glassEffectSettings = glassEffectSettings,
            treeEffectSettings = treeEffectSettings,
            fluidEffectSettings = fluidEffectSettings,
            visualTheme = visualTheme
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

    // ===== Position Settings setters =====
    fun setHorizontalOffset(offset: Float) {
        prefs.edit().putFloat(KEY_HORIZONTAL_OFFSET, offset).apply()
        val newPosition = _settingsFlow.value.positionSettings.copy(horizontalOffset = offset)
        _settingsFlow.value = _settingsFlow.value.copy(positionSettings = newPosition)
        notifyWallpaperChanged()
    }

    fun setVerticalOffset(offset: Float) {
        prefs.edit().putFloat(KEY_VERTICAL_OFFSET, offset).apply()
        val newPosition = _settingsFlow.value.positionSettings.copy(verticalOffset = offset)
        _settingsFlow.value = _settingsFlow.value.copy(positionSettings = newPosition)
        notifyWallpaperChanged()
    }

    fun setScale(scale: Float) {
        prefs.edit().putFloat(KEY_SCALE, scale).apply()
        val newPosition = _settingsFlow.value.positionSettings.copy(scale = scale)
        _settingsFlow.value = _settingsFlow.value.copy(positionSettings = newPosition)
        notifyWallpaperChanged()
    }

    // ===== Animation Settings setters =====
    fun setAnimationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATION_ENABLED, enabled).apply()
        val newAnimation = _settingsFlow.value.animationSettings.copy(enabled = enabled)
        _settingsFlow.value = _settingsFlow.value.copy(animationSettings = newAnimation)
        notifyWallpaperChanged()
    }

    fun setAnimationType(type: AnimationType) {
        prefs.edit().putString(KEY_ANIMATION_TYPE, type.name).apply()
        val newAnimation = _settingsFlow.value.animationSettings.copy(type = type)
        _settingsFlow.value = _settingsFlow.value.copy(animationSettings = newAnimation)
        notifyWallpaperChanged()
    }

    fun setAnimationSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_ANIMATION_SPEED, speed).apply()
        val newAnimation = _settingsFlow.value.animationSettings.copy(speed = speed)
        _settingsFlow.value = _settingsFlow.value.copy(animationSettings = newAnimation)
        notifyWallpaperChanged()
    }

    fun setAnimationIntensity(intensity: Float) {
        prefs.edit().putFloat(KEY_ANIMATION_INTENSITY, intensity).apply()
        val newAnimation = _settingsFlow.value.animationSettings.copy(intensity = intensity)
        _settingsFlow.value = _settingsFlow.value.copy(animationSettings = newAnimation)
        notifyWallpaperChanged()
    }

    // ===== Glass Effect Settings setters =====
    fun setGlassEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLASS_ENABLED, enabled).apply()
        val newGlass = _settingsFlow.value.glassEffectSettings.copy(enabled = enabled)
        _settingsFlow.value = _settingsFlow.value.copy(glassEffectSettings = newGlass)
        notifyWallpaperChanged()
    }

    fun setGlassStyle(style: GlassStyle) {
        prefs.edit().putString(KEY_GLASS_STYLE, style.name).apply()
        val newGlass = _settingsFlow.value.glassEffectSettings.copy(style = style)
        _settingsFlow.value = _settingsFlow.value.copy(glassEffectSettings = newGlass)
        notifyWallpaperChanged()
    }

    fun setGlassBlur(blur: Float) {
        prefs.edit().putFloat(KEY_GLASS_BLUR, blur).apply()
        val newGlass = _settingsFlow.value.glassEffectSettings.copy(blur = blur)
        _settingsFlow.value = _settingsFlow.value.copy(glassEffectSettings = newGlass)
        notifyWallpaperChanged()
    }

    fun setGlassOpacity(opacity: Float) {
        prefs.edit().putFloat(KEY_GLASS_OPACITY, opacity).apply()
        val newGlass = _settingsFlow.value.glassEffectSettings.copy(opacity = opacity)
        _settingsFlow.value = _settingsFlow.value.copy(glassEffectSettings = newGlass)
        notifyWallpaperChanged()
    }

    fun setGlassTint(tint: Int) {
        prefs.edit().putInt(KEY_GLASS_TINT, tint).apply()
        val newGlass = _settingsFlow.value.glassEffectSettings.copy(tint = tint)
        _settingsFlow.value = _settingsFlow.value.copy(glassEffectSettings = newGlass)
        notifyWallpaperChanged()
    }

    // ===== Tree Effect Settings setters =====
    fun setTreeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TREE_ENABLED, enabled).apply()
        val newTree = _settingsFlow.value.treeEffectSettings.copy(enabled = enabled)
        _settingsFlow.value = _settingsFlow.value.copy(treeEffectSettings = newTree)
        notifyWallpaperChanged()
    }

    fun setTreeStyle(style: TreeStyle) {
        prefs.edit().putString(KEY_TREE_STYLE, style.name).apply()
        val newTree = _settingsFlow.value.treeEffectSettings.copy(style = style)
        _settingsFlow.value = _settingsFlow.value.copy(treeEffectSettings = newTree)
        notifyWallpaperChanged()
    }

    fun setTreeTrunkColor(color: Int) {
        prefs.edit().putInt(KEY_TREE_TRUNK_COLOR, color).apply()
        val newTree = _settingsFlow.value.treeEffectSettings.copy(trunkColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(treeEffectSettings = newTree)
        notifyWallpaperChanged()
    }

    fun setTreeLeafColor(color: Int) {
        prefs.edit().putInt(KEY_TREE_LEAF_COLOR, color).apply()
        val newTree = _settingsFlow.value.treeEffectSettings.copy(leafColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(treeEffectSettings = newTree)
        notifyWallpaperChanged()
    }

    fun setTreeBloomColor(color: Int) {
        prefs.edit().putInt(KEY_TREE_BLOOM_COLOR, color).apply()
        val newTree = _settingsFlow.value.treeEffectSettings.copy(bloomColor = color)
        _settingsFlow.value = _settingsFlow.value.copy(treeEffectSettings = newTree)
        notifyWallpaperChanged()
    }

    fun setTreeShowGround(show: Boolean) {
        prefs.edit().putBoolean(KEY_TREE_SHOW_GROUND, show).apply()
        val newTree = _settingsFlow.value.treeEffectSettings.copy(showGround = show)
        _settingsFlow.value = _settingsFlow.value.copy(treeEffectSettings = newTree)
        notifyWallpaperChanged()
    }

    // ===== Fluid Effect Settings setters =====
    fun setFluidEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLUID_ENABLED, enabled).apply()
        val newFluid = _settingsFlow.value.fluidEffectSettings.copy(enabled = enabled)
        _settingsFlow.value = _settingsFlow.value.copy(fluidEffectSettings = newFluid)
        notifyWallpaperChanged()
    }

    fun setFluidStyle(style: FluidStyle) {
        prefs.edit().putString(KEY_FLUID_STYLE, style.name).apply()
        val newFluid = _settingsFlow.value.fluidEffectSettings.copy(style = style)
        _settingsFlow.value = _settingsFlow.value.copy(fluidEffectSettings = newFluid)
        notifyWallpaperChanged()
    }

    fun setFluidFlowSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_FLUID_FLOW_SPEED, speed).apply()
        val newFluid = _settingsFlow.value.fluidEffectSettings.copy(flowSpeed = speed)
        _settingsFlow.value = _settingsFlow.value.copy(fluidEffectSettings = newFluid)
        notifyWallpaperChanged()
    }

    fun setFluidTurbulence(turbulence: Float) {
        prefs.edit().putFloat(KEY_FLUID_TURBULENCE, turbulence).apply()
        val newFluid = _settingsFlow.value.fluidEffectSettings.copy(turbulence = turbulence)
        _settingsFlow.value = _settingsFlow.value.copy(fluidEffectSettings = newFluid)
        notifyWallpaperChanged()
    }

    fun setFluidColorIntensity(intensity: Float) {
        prefs.edit().putFloat(KEY_FLUID_COLOR_INTENSITY, intensity).apply()
        val newFluid = _settingsFlow.value.fluidEffectSettings.copy(colorIntensity = intensity)
        _settingsFlow.value = _settingsFlow.value.copy(fluidEffectSettings = newFluid)
        notifyWallpaperChanged()
    }

    // ===== Visual Theme setter =====
    fun setVisualTheme(theme: VisualTheme) {
        prefs.edit().putString(KEY_VISUAL_THEME, theme.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(visualTheme = theme)
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

        // Position Settings keys
        private const val KEY_HORIZONTAL_OFFSET = "horizontal_offset"
        private const val KEY_VERTICAL_OFFSET = "vertical_offset"
        private const val KEY_SCALE = "scale"

        // Animation Settings keys
        private const val KEY_ANIMATION_ENABLED = "animation_enabled"
        private const val KEY_ANIMATION_TYPE = "animation_type"
        private const val KEY_ANIMATION_SPEED = "animation_speed"
        private const val KEY_ANIMATION_INTENSITY = "animation_intensity"

        // Glass Effect Settings keys
        private const val KEY_GLASS_ENABLED = "glass_enabled"
        private const val KEY_GLASS_STYLE = "glass_style"
        private const val KEY_GLASS_BLUR = "glass_blur"
        private const val KEY_GLASS_OPACITY = "glass_opacity"
        private const val KEY_GLASS_TINT = "glass_tint"

        // Tree Effect Settings keys
        private const val KEY_TREE_ENABLED = "tree_enabled"
        private const val KEY_TREE_STYLE = "tree_style"
        private const val KEY_TREE_TRUNK_COLOR = "tree_trunk_color"
        private const val KEY_TREE_LEAF_COLOR = "tree_leaf_color"
        private const val KEY_TREE_BLOOM_COLOR = "tree_bloom_color"
        private const val KEY_TREE_SHOW_GROUND = "tree_show_ground"

        // Fluid Effect Settings keys
        private const val KEY_FLUID_ENABLED = "fluid_enabled"
        private const val KEY_FLUID_STYLE = "fluid_style"
        private const val KEY_FLUID_FLOW_SPEED = "fluid_flow_speed"
        private const val KEY_FLUID_TURBULENCE = "fluid_turbulence"
        private const val KEY_FLUID_COLOR_INTENSITY = "fluid_color_intensity"

        // Visual Theme key
        private const val KEY_VISUAL_THEME = "visual_theme"

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
