package com.example.lifedots.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.preferences.*
import kotlin.math.roundToInt

@Composable
fun EffectsSettingsSection(
    preferences: LifeDotsPreferences,
    settings: WallpaperSettings,
    onShowGlassTintPicker: () -> Unit,
    onShowTreeTrunkColorPicker: () -> Unit,
    onShowTreeLeafColorPicker: () -> Unit,
    onShowTreeBloomColorPicker: () -> Unit
) {
    // ===== Position & Scale Section =====
    SettingsSection(title = "Position & Scale") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Horizontal Offset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horizontal Offset",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${settings.positionSettings.horizontalOffset.roundToInt()}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Slider(
                    value = settings.positionSettings.horizontalOffset,
                    onValueChange = { preferences.setHorizontalOffset(it) },
                    valueRange = -50f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Vertical Offset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vertical Offset",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${settings.positionSettings.verticalOffset.roundToInt()}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Slider(
                    value = settings.positionSettings.verticalOffset,
                    onValueChange = { preferences.setVerticalOffset(it) },
                    valueRange = -50f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scale
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scale",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(settings.positionSettings.scale * 100).roundToInt()}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Slider(
                    value = settings.positionSettings.scale,
                    onValueChange = { preferences.setScale(it) },
                    valueRange = 0.5f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ===== Animation Section =====
    SettingsSection(title = "Animation") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Animation", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = settings.animationSettings.enabled,
                        onCheckedChange = { preferences.setAnimationEnabled(it) }
                    )
                }

                AnimatedVisibility(
                    visible = settings.animationSettings.enabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Animation Type", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnimationTypeOption(
                                label = "Fade",
                                type = AnimationType.FADE_IN,
                                isSelected = settings.animationSettings.type == AnimationType.FADE_IN,
                                onClick = { preferences.setAnimationType(AnimationType.FADE_IN) },
                                modifier = Modifier.weight(1f)
                            )
                            AnimationTypeOption(
                                label = "Pulse",
                                type = AnimationType.PULSE,
                                isSelected = settings.animationSettings.type == AnimationType.PULSE,
                                onClick = { preferences.setAnimationType(AnimationType.PULSE) },
                                modifier = Modifier.weight(1f)
                            )
                            AnimationTypeOption(
                                label = "Wave",
                                type = AnimationType.WAVE,
                                isSelected = settings.animationSettings.type == AnimationType.WAVE,
                                onClick = { preferences.setAnimationType(AnimationType.WAVE) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnimationTypeOption(
                                label = "Breathe",
                                type = AnimationType.BREATHE,
                                isSelected = settings.animationSettings.type == AnimationType.BREATHE,
                                onClick = { preferences.setAnimationType(AnimationType.BREATHE) },
                                modifier = Modifier.weight(1f)
                            )
                            AnimationTypeOption(
                                label = "Ripple",
                                type = AnimationType.RIPPLE,
                                isSelected = settings.animationSettings.type == AnimationType.RIPPLE,
                                onClick = { preferences.setAnimationType(AnimationType.RIPPLE) },
                                modifier = Modifier.weight(1f)
                            )
                            AnimationTypeOption(
                                label = "Cascade",
                                type = AnimationType.CASCADE,
                                isSelected = settings.animationSettings.type == AnimationType.CASCADE,
                                onClick = { preferences.setAnimationType(AnimationType.CASCADE) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${(settings.animationSettings.speed * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.animationSettings.speed,
                            onValueChange = { preferences.setAnimationSpeed(it) },
                            valueRange = 0.5f..2f
                        )

                        // Intensity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Intensity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${(settings.animationSettings.intensity * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.animationSettings.intensity,
                            onValueChange = { preferences.setAnimationIntensity(it) },
                            valueRange = 0.1f..1f
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ===== Glass Effect Section =====
    SettingsSection(title = "Glass Effect") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Glass Effect", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Switch(
                        checked = settings.glassEffectSettings.enabled,
                        onCheckedChange = { preferences.setGlassEnabled(it) }
                    )
                }

                AnimatedVisibility(
                    visible = settings.glassEffectSettings.enabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Glass Style", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassStyleOption(
                                label = "Light",
                                style = GlassStyle.LIGHT_FROST,
                                isSelected = settings.glassEffectSettings.style == GlassStyle.LIGHT_FROST,
                                onClick = { preferences.setGlassStyle(GlassStyle.LIGHT_FROST) },
                                modifier = Modifier.weight(1f)
                            )
                            GlassStyleOption(
                                label = "Heavy",
                                style = GlassStyle.HEAVY_FROST,
                                isSelected = settings.glassEffectSettings.style == GlassStyle.HEAVY_FROST,
                                onClick = { preferences.setGlassStyle(GlassStyle.HEAVY_FROST) },
                                modifier = Modifier.weight(1f)
                            )
                            GlassStyleOption(
                                label = "Acrylic",
                                style = GlassStyle.ACRYLIC,
                                isSelected = settings.glassEffectSettings.style == GlassStyle.ACRYLIC,
                                onClick = { preferences.setGlassStyle(GlassStyle.ACRYLIC) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassStyleOption(
                                label = "Crystal",
                                style = GlassStyle.CRYSTAL,
                                isSelected = settings.glassEffectSettings.style == GlassStyle.CRYSTAL,
                                onClick = { preferences.setGlassStyle(GlassStyle.CRYSTAL) },
                                modifier = Modifier.weight(1f)
                            )
                            GlassStyleOption(
                                label = "Ice",
                                style = GlassStyle.ICE,
                                isSelected = settings.glassEffectSettings.style == GlassStyle.ICE,
                                onClick = { preferences.setGlassStyle(GlassStyle.ICE) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Blur
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Blur", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${settings.glassEffectSettings.blur.roundToInt()}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.glassEffectSettings.blur,
                            onValueChange = { preferences.setGlassBlur(it) },
                            valueRange = 0f..25f
                        )

                        // Opacity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Opacity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${(settings.glassEffectSettings.opacity * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.glassEffectSettings.opacity,
                            onValueChange = { preferences.setGlassOpacity(it) },
                            valueRange = 0.1f..0.9f
                        )

                        // Tint Color
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tint Color", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(settings.glassEffectSettings.tint))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable { onShowGlassTintPicker() }
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ===== Tree Effect Section =====
    SettingsSection(title = "Tree Growth Effect") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Tree Effect", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Replaces dot grid with tree", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = settings.treeEffectSettings.enabled,
                        onCheckedChange = { preferences.setTreeEnabled(it) }
                    )
                }

                AnimatedVisibility(
                    visible = settings.treeEffectSettings.enabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tree Style", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TreeStyleOption(
                                label = "Simple",
                                style = TreeStyle.SIMPLE,
                                isSelected = settings.treeEffectSettings.style == TreeStyle.SIMPLE,
                                onClick = { preferences.setTreeStyle(TreeStyle.SIMPLE) },
                                modifier = Modifier.weight(1f)
                            )
                            TreeStyleOption(
                                label = "Detailed",
                                style = TreeStyle.DETAILED,
                                isSelected = settings.treeEffectSettings.style == TreeStyle.DETAILED,
                                onClick = { preferences.setTreeStyle(TreeStyle.DETAILED) },
                                modifier = Modifier.weight(1f)
                            )
                            TreeStyleOption(
                                label = "Bonsai",
                                style = TreeStyle.BONSAI,
                                isSelected = settings.treeEffectSettings.style == TreeStyle.BONSAI,
                                onClick = { preferences.setTreeStyle(TreeStyle.BONSAI) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TreeStyleOption(
                                label = "Sakura",
                                style = TreeStyle.SAKURA,
                                isSelected = settings.treeEffectSettings.style == TreeStyle.SAKURA,
                                onClick = { preferences.setTreeStyle(TreeStyle.SAKURA) },
                                modifier = Modifier.weight(1f)
                            )
                            TreeStyleOption(
                                label = "Willow",
                                style = TreeStyle.WILLOW,
                                isSelected = settings.treeEffectSettings.style == TreeStyle.WILLOW,
                                onClick = { preferences.setTreeStyle(TreeStyle.WILLOW) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tree Colors
                        Text("Colors", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(settings.treeEffectSettings.trunkColor))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable { onShowTreeTrunkColorPicker() }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Trunk", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(settings.treeEffectSettings.leafColor))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable { onShowTreeLeafColorPicker() }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Leaf", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(settings.treeEffectSettings.bloomColor))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .clickable { onShowTreeBloomColorPicker() }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Bloom", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Ground", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = settings.treeEffectSettings.showGround,
                                onCheckedChange = { preferences.setTreeShowGround(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ===== Fluid Effect Section =====
    SettingsSection(title = "Fluid Effect") {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Fluid Effect", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Animated background effect", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = settings.fluidEffectSettings.enabled,
                        onCheckedChange = { preferences.setFluidEnabled(it) }
                    )
                }

                AnimatedVisibility(
                    visible = settings.fluidEffectSettings.enabled,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Fluid Style", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FluidStyleOption(
                                label = "Water",
                                style = FluidStyle.WATER,
                                isSelected = settings.fluidEffectSettings.style == FluidStyle.WATER,
                                onClick = { preferences.setFluidStyle(FluidStyle.WATER) },
                                modifier = Modifier.weight(1f)
                            )
                            FluidStyleOption(
                                label = "Lava",
                                style = FluidStyle.LAVA,
                                isSelected = settings.fluidEffectSettings.style == FluidStyle.LAVA,
                                onClick = { preferences.setFluidStyle(FluidStyle.LAVA) },
                                modifier = Modifier.weight(1f)
                            )
                            FluidStyleOption(
                                label = "Mercury",
                                style = FluidStyle.MERCURY,
                                isSelected = settings.fluidEffectSettings.style == FluidStyle.MERCURY,
                                onClick = { preferences.setFluidStyle(FluidStyle.MERCURY) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FluidStyleOption(
                                label = "Plasma",
                                style = FluidStyle.PLASMA,
                                isSelected = settings.fluidEffectSettings.style == FluidStyle.PLASMA,
                                onClick = { preferences.setFluidStyle(FluidStyle.PLASMA) },
                                modifier = Modifier.weight(1f)
                            )
                            FluidStyleOption(
                                label = "Aurora",
                                style = FluidStyle.AURORA,
                                isSelected = settings.fluidEffectSettings.style == FluidStyle.AURORA,
                                onClick = { preferences.setFluidStyle(FluidStyle.AURORA) },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Flow Speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Flow Speed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${(settings.fluidEffectSettings.flowSpeed * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.fluidEffectSettings.flowSpeed,
                            onValueChange = { preferences.setFluidFlowSpeed(it) },
                            valueRange = 0.1f..2f
                        )

                        // Turbulence
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Turbulence", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${(settings.fluidEffectSettings.turbulence * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.fluidEffectSettings.turbulence,
                            onValueChange = { preferences.setFluidTurbulence(it) },
                            valueRange = 0.1f..1f
                        )

                        // Color Intensity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Color Intensity", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${(settings.fluidEffectSettings.colorIntensity * 100).roundToInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Slider(
                            value = settings.fluidEffectSettings.colorIntensity,
                            onValueChange = { preferences.setFluidColorIntensity(it) },
                            valueRange = 0.1f..1f
                        )
                    }
                }
            }
        }
    }
}
