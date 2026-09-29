package com.example.lifedots

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import com.example.lifedots.ui.components.ColorPickerDialog
import com.example.lifedots.ui.components.DatePickerDialog
import com.example.lifedots.ui.components.GoalEditorDialog
import com.example.lifedots.ui.settings.AppearanceSettingsSection
import com.example.lifedots.ui.settings.ContentSettingsSection
import com.example.lifedots.ui.settings.EffectsSettingsSection
import com.example.lifedots.ui.settings.TimeScaleSettingsSection
import com.example.lifedots.ui.theme.LifeDotsTheme

class SettingsActivity : ComponentActivity() {

    private lateinit var preferences: LifeDotsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = LifeDotsPreferences.getInstance(this)

        setContent {
            LifeDotsTheme {
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

@Composable
fun SettingsScreen(
    preferences: LifeDotsPreferences,
    modifier: Modifier = Modifier
) {
    val settings by preferences.settingsFlow.collectAsState()
    val context = LocalContext.current

    var showBgColorPicker by remember { mutableStateOf(false) }
    var showFilledColorPicker by remember { mutableStateOf(false) }
    var showEmptyColorPicker by remember { mutableStateOf(false) }
    var showTodayColorPicker by remember { mutableStateOf(false) }
    var showFooterColorPicker by remember { mutableStateOf(false) }
    var showProgressColorPicker by remember { mutableStateOf(false) }
    var showGoalEditor by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    var showGlassTintPicker by remember { mutableStateOf(false) }
    var showTreeTrunkColorPicker by remember { mutableStateOf(false) }
    var showTreeLeafColorPicker by remember { mutableStateOf(false) }
    var showTreeBloomColorPicker by remember { mutableStateOf(false) }
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showCustomYearStartDatePicker by remember { mutableStateOf(false) }
    var showCustomYearEndDatePicker by remember { mutableStateOf(false) }

    // Permission state for image picker
    var hasImagePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Permission not persistable, that's okay
            }
            preferences.setBackgroundUri(it.toString())
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasImagePermission = isGranted
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Time Scale & Calendars
        TimeScaleSettingsSection(
            preferences = preferences,
            settings = settings,
            onShowCustomYearStartDatePicker = { showCustomYearStartDatePicker = true },
            onShowCustomYearEndDatePicker = { showCustomYearEndDatePicker = true },
            onShowBirthDatePicker = { showBirthDatePicker = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance & Themes
        AppearanceSettingsSection(
            preferences = preferences,
            settings = settings,
            onShowBgColorPicker = { showBgColorPicker = true },
            onShowFilledColorPicker = { showFilledColorPicker = true },
            onShowEmptyColorPicker = { showEmptyColorPicker = true },
            onShowTodayColorPicker = { showTodayColorPicker = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Content & Overlays (Footer, Progress, Background, Goals)
        ContentSettingsSection(
            preferences = preferences,
            settings = settings,
            hasImagePermission = hasImagePermission,
            onSelectImage = {
                if (hasImagePermission) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    permissionLauncher.launch(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    )
                }
            },
            onShowFooterColorPicker = { showFooterColorPicker = true },
            onShowProgressColorPicker = { showProgressColorPicker = true },
            onAddGoal = {
                editingGoal = null
                showGoalEditor = true
            },
            onEditGoal = { goal ->
                editingGoal = goal
                showGoalEditor = true
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Effects & Animations (Position, Animation, Glass, Tree, Fluid)
        EffectsSettingsSection(
            preferences = preferences,
            settings = settings,
            onShowGlassTintPicker = { showGlassTintPicker = true },
            onShowTreeTrunkColorPicker = { showTreeTrunkColorPicker = true },
            onShowTreeLeafColorPicker = { showTreeLeafColorPicker = true },
            onShowTreeBloomColorPicker = { showTreeBloomColorPicker = true }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Color Picker Dialogs
    if (showBgColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.backgroundColor,
            title = "Background Color",
            onColorSelected = {
                preferences.setCustomBackgroundColor(it)
                showBgColorPicker = false
            },
            onDismiss = { showBgColorPicker = false }
        )
    }

    if (showFilledColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.filledDotColor,
            title = "Filled Dots Color",
            onColorSelected = {
                preferences.setCustomFilledDotColor(it)
                showFilledColorPicker = false
            },
            onDismiss = { showFilledColorPicker = false }
        )
    }

    if (showEmptyColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.emptyDotColor,
            title = "Empty Dots Color",
            onColorSelected = {
                preferences.setCustomEmptyDotColor(it)
                showEmptyColorPicker = false
            },
            onDismiss = { showEmptyColorPicker = false }
        )
    }

    if (showTodayColorPicker) {
        ColorPickerDialog(
            initialColor = settings.customColors.todayDotColor,
            title = "Today's Dot Color",
            onColorSelected = {
                preferences.setCustomTodayDotColor(it)
                showTodayColorPicker = false
            },
            onDismiss = { showTodayColorPicker = false }
        )
    }

    if (showFooterColorPicker) {
        ColorPickerDialog(
            initialColor = settings.footerTextSettings.color,
            title = "Footer Text Color",
            onColorSelected = {
                preferences.setFooterColor(it)
                showFooterColorPicker = false
            },
            onDismiss = { showFooterColorPicker = false }
        )
    }

    if (showProgressColorPicker) {
        ColorPickerDialog(
            initialColor = settings.progressSettings.color,
            title = "Progress Text Color",
            onColorSelected = {
                preferences.setProgressColor(it)
                showProgressColorPicker = false
            },
            onDismiss = { showProgressColorPicker = false }
        )
    }

    // Goal editor dialog
    if (showGoalEditor) {
        GoalEditorDialog(
            goal = editingGoal,
            onSave = { goal ->
                if (editingGoal != null) {
                    preferences.updateGoal(goal)
                } else {
                    preferences.addGoal(goal)
                }
            },
            onDelete = editingGoal?.let { { preferences.deleteGoal(it.id) } },
            onDismiss = {
                showGoalEditor = false
                editingGoal = null
            }
        )
    }

    // Glass tint color picker
    if (showGlassTintPicker) {
        ColorPickerDialog(
            initialColor = settings.glassEffectSettings.tint,
            title = "Glass Tint Color",
            onColorSelected = {
                preferences.setGlassTint(it)
                showGlassTintPicker = false
            },
            onDismiss = { showGlassTintPicker = false }
        )
    }

    // Tree trunk color picker
    if (showTreeTrunkColorPicker) {
        ColorPickerDialog(
            initialColor = settings.treeEffectSettings.trunkColor,
            title = "Trunk Color",
            onColorSelected = {
                preferences.setTreeTrunkColor(it)
                showTreeTrunkColorPicker = false
            },
            onDismiss = { showTreeTrunkColorPicker = false }
        )
    }

    // Tree leaf color picker
    if (showTreeLeafColorPicker) {
        ColorPickerDialog(
            initialColor = settings.treeEffectSettings.leafColor,
            title = "Leaf Color",
            onColorSelected = {
                preferences.setTreeLeafColor(it)
                showTreeLeafColorPicker = false
            },
            onDismiss = { showTreeLeafColorPicker = false }
        )
    }

    // Tree bloom color picker
    if (showTreeBloomColorPicker) {
        ColorPickerDialog(
            initialColor = settings.treeEffectSettings.bloomColor,
            title = "Bloom Color",
            onColorSelected = {
                preferences.setTreeBloomColor(it)
                showTreeBloomColorPicker = false
            },
            onDismiss = { showTreeBloomColorPicker = false }
        )
    }

    // Birth date picker dialog
    if (showBirthDatePicker) {
        DatePickerDialog(
            initialDate = settings.lifeSettings.birthDate,
            onDateSelected = {
                preferences.setLifeBirthDate(it)
                showBirthDatePicker = false
            },
            onDismiss = { showBirthDatePicker = false }
        )
    }

    // Custom year start date picker dialog
    if (showCustomYearStartDatePicker) {
        DatePickerDialog(
            initialDate = settings.customYearSettings.startDate,
            onDateSelected = {
                preferences.setCustomYearStartDate(it)
                showCustomYearStartDatePicker = false
            },
            onDismiss = { showCustomYearStartDatePicker = false }
        )
    }

    // Custom year end date picker dialog
    if (showCustomYearEndDatePicker) {
        DatePickerDialog(
            initialDate = settings.customYearSettings.endDate,
            onDateSelected = {
                preferences.setCustomYearEndDate(it)
                showCustomYearEndDatePicker = false
            },
            onDismiss = { showCustomYearEndDatePicker = false }
        )
    }
}
