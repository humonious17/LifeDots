package com.example.lifedots

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.lifedots.preferences.Goal
import com.example.lifedots.preferences.LifeDotsPreferences
import com.example.lifedots.preferences.ThemeOption
import com.example.lifedots.ui.components.ColorPickerDialog
import com.example.lifedots.ui.components.DatePickerDialog
import com.example.lifedots.ui.components.GoalEditorDialog
import com.example.lifedots.ui.settings.AppearanceSettingsSection
import com.example.lifedots.ui.settings.ContentSettingsSection
import com.example.lifedots.ui.settings.EffectsSettingsSection
import com.example.lifedots.ui.settings.TimeScaleSettingsSection
import com.example.lifedots.ui.theme.LifeDotsTheme
import com.example.lifedots.ui.theme.lifeDotsBackground

// region Activity

class SettingsActivity : ComponentActivity() {

    private lateinit var preferences: LifeDotsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = LifeDotsPreferences.getInstance(this)

        setContent {
            val settings by preferences.settingsFlow.collectAsState()
            LifeDotsTheme(liquidGlass = settings.theme == ThemeOption.LIQUID_GLASS) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsScreen(
                        preferences = preferences,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// endregion

// region Dialog state

/** Every color the user can pick. Titles live in the dialog spec below. */
private enum class ColorTarget {
    BACKGROUND, FILLED_DOT, EMPTY_DOT, TODAY_DOT,
    FOOTER, PROGRESS,
    GLASS_TINT,
    TREE_TRUNK, TREE_LEAF, TREE_BLOOM
}

private enum class DateTarget { BIRTH, CUSTOM_YEAR_START, CUSTOM_YEAR_END }

/** Only one dialog is ever open, so one nullable value replaces a boolean per dialog. */
private sealed interface SettingsDialog {
    data class ColorPicker(val target: ColorTarget) : SettingsDialog
    data class DatePicker(val target: DateTarget) : SettingsDialog
    data class GoalEditor(val goal: Goal?) : SettingsDialog
}

private class ColorSpec<T>(val initial: T, val title: String, val onSelected: (T) -> Unit)

private class DateSpec<T>(val initial: T, val onSelected: (T) -> Unit)

// endregion

// region Screen

private val SectionSpacing = 24.dp

@Composable
fun SettingsScreen(
    preferences: LifeDotsPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settingsFlow.collectAsState()
    var activeDialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val imagePicker = rememberImagePicker(preferences)

    fun show(dialog: SettingsDialog) {
        activeDialog = dialog
    }

    fun showColor(target: ColorTarget) = show(SettingsDialog.ColorPicker(target))
    fun showDate(target: DateTarget) = show(SettingsDialog.DatePicker(target))

    Column(
        modifier = modifier
            .fillMaxSize()
            .lifeDotsBackground()
            .verticalScroll(rememberScrollState())
            .padding(SectionSpacing),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Time scale and calendars
        TimeScaleSettingsSection(
            preferences = preferences,
            settings = settings,
            onShowCustomYearStartDatePicker = { showDate(DateTarget.CUSTOM_YEAR_START) },
            onShowCustomYearEndDatePicker = { showDate(DateTarget.CUSTOM_YEAR_END) },
            onShowBirthDatePicker = { showDate(DateTarget.BIRTH) }
        )

        // Appearance and themes
        AppearanceSettingsSection(
            preferences = preferences,
            settings = settings,
            onShowBgColorPicker = { showColor(ColorTarget.BACKGROUND) },
            onShowFilledColorPicker = { showColor(ColorTarget.FILLED_DOT) },
            onShowEmptyColorPicker = { showColor(ColorTarget.EMPTY_DOT) },
            onShowTodayColorPicker = { showColor(ColorTarget.TODAY_DOT) }
        )

        // Content and overlays (footer, progress, background, goals)
        ContentSettingsSection(
            preferences = preferences,
            settings = settings,
            hasImagePermission = imagePicker.hasPermission,
            onSelectImage = imagePicker.pick,
            onShowFooterColorPicker = { showColor(ColorTarget.FOOTER) },
            onShowProgressColorPicker = { showColor(ColorTarget.PROGRESS) },
            onAddGoal = { show(SettingsDialog.GoalEditor(goal = null)) },
            onEditGoal = { goal -> show(SettingsDialog.GoalEditor(goal)) }
        )

        // Effects and animations (position, animation, glass, tree, fluid)
        EffectsSettingsSection(
            preferences = preferences,
            settings = settings,
            onShowGlassTintPicker = { showColor(ColorTarget.GLASS_TINT) },
            onShowTreeTrunkColorPicker = { showColor(ColorTarget.TREE_TRUNK) },
            onShowTreeLeafColorPicker = { showColor(ColorTarget.TREE_LEAF) },
            onShowTreeBloomColorPicker = { showColor(ColorTarget.TREE_BLOOM) }
        )

        // Extra room at the bottom (24dp spacing + 8dp)
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Dialogs: what each color or date target reads and writes lives in one place each.

    fun colorSpec(target: ColorTarget) = when (target) {
        ColorTarget.BACKGROUND -> ColorSpec(
            settings.customColors.backgroundColor, "Background Color"
        ) { preferences.setCustomBackgroundColor(it) }

        ColorTarget.FILLED_DOT -> ColorSpec(
            settings.customColors.filledDotColor, "Filled Dots Color"
        ) { preferences.setCustomFilledDotColor(it) }

        ColorTarget.EMPTY_DOT -> ColorSpec(
            settings.customColors.emptyDotColor, "Empty Dots Color"
        ) { preferences.setCustomEmptyDotColor(it) }

        ColorTarget.TODAY_DOT -> ColorSpec(
            settings.customColors.todayDotColor, "Today's Dot Color"
        ) { preferences.setCustomTodayDotColor(it) }

        ColorTarget.FOOTER -> ColorSpec(
            settings.footerTextSettings.color, "Footer Text Color"
        ) { preferences.setFooterColor(it) }

        ColorTarget.PROGRESS -> ColorSpec(
            settings.progressSettings.color, "Progress Text Color"
        ) { preferences.setProgressColor(it) }

        ColorTarget.GLASS_TINT -> ColorSpec(
            settings.glassEffectSettings.tint, "Glass Tint Color"
        ) { preferences.setGlassTint(it) }

        ColorTarget.TREE_TRUNK -> ColorSpec(
            settings.treeEffectSettings.trunkColor, "Trunk Color"
        ) { preferences.setTreeTrunkColor(it) }

        ColorTarget.TREE_LEAF -> ColorSpec(
            settings.treeEffectSettings.leafColor, "Leaf Color"
        ) { preferences.setTreeLeafColor(it) }

        ColorTarget.TREE_BLOOM -> ColorSpec(
            settings.treeEffectSettings.bloomColor, "Bloom Color"
        ) { preferences.setTreeBloomColor(it) }
    }

    fun dateSpec(target: DateTarget) = when (target) {
        DateTarget.BIRTH -> DateSpec(
            settings.lifeSettings.birthDate
        ) { preferences.setLifeBirthDate(it) }

        DateTarget.CUSTOM_YEAR_START -> DateSpec(
            settings.customYearSettings.startDate
        ) { preferences.setCustomYearStartDate(it) }

        DateTarget.CUSTOM_YEAR_END -> DateSpec(
            settings.customYearSettings.endDate
        ) { preferences.setCustomYearEndDate(it) }
    }

    val dismiss = { activeDialog = null }

    when (val dialog = activeDialog) {
        null -> Unit

        is SettingsDialog.ColorPicker -> {
            val spec = colorSpec(dialog.target)
            ColorPickerDialog(
                initialColor = spec.initial,
                title = spec.title,
                onColorSelected = {
                    spec.onSelected(it)
                    dismiss()
                },
                onDismiss = dismiss
            )
        }

        is SettingsDialog.DatePicker -> {
            val spec = dateSpec(dialog.target)
            DatePickerDialog(
                initialDate = spec.initial,
                onDateSelected = {
                    spec.onSelected(it)
                    dismiss()
                },
                onDismiss = dismiss
            )
        }

        is SettingsDialog.GoalEditor -> {
            val editing = dialog.goal
            GoalEditorDialog(
                goal = editing,
                onSave = { goal ->
                    if (editing != null) preferences.updateGoal(goal) else preferences.addGoal(goal)
                },
                onDelete = editing?.let { { preferences.deleteGoal(it.id) } },
                onDismiss = dismiss
            )
        }
    }
}

// endregion

// region Image picker

private val ImagePermission: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

private class ImagePicker(
    val hasPermission: Boolean,
    val pick: () -> Unit
)

/**
 * Handles the permission check, the permission request and the image picker.
 * Calling [ImagePicker.pick] launches the picker, asking for permission first if needed.
 */
@Composable
private fun rememberImagePicker(preferences: LifeDotsPreferences): ImagePicker {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, ImagePermission) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Not persistable, that's okay
            }
            preferences.setBackgroundUri(it.toString())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) pickerLauncher.launch("image/*")
    }

    return ImagePicker(
        hasPermission = hasPermission,
        pick = {
            if (hasPermission) {
                pickerLauncher.launch("image/*")
            } else {
                permissionLauncher.launch(ImagePermission)
            }
        }
    )
}

// endregion