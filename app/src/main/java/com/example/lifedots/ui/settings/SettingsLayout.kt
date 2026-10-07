package com.example.lifedots.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun Gap(height: Dp) = Spacer(Modifier.height(height))

@Composable
internal fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
internal fun ToggleCard(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
                        subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    SettingsCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label)
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        if (checked) {
            Gap(12.dp)
            content()
        }
    }
}

@Composable
internal fun OptionGroup(label: String, content: @Composable RowScope.() -> Unit) {
    Column {
        Text(label)
        Gap(8.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
internal fun LabeledSlider(label: String, valueText: String, value: Float,
                          valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(valueText)
    }
    Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
}

@Composable
internal fun ExpandableCard(visible: Boolean, content: @Composable ColumnScope.() -> Unit) {
    if (visible) {
        Gap(8.dp)
        SettingsCard(content)
    }
}

@Composable
internal fun OptionRow(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
internal fun Expandable(visible: Boolean, content: @Composable ColumnScope.() -> Unit) {
    if (visible) Column(content = content)
}

@Composable
internal fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
