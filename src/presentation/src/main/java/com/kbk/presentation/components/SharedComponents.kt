package com.kbk.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kbk.domain.models.sdk.VerificationResult

const val SHARED_CARD_ELEVATION_VAL = 4
const val SHARED_CARD_PADDING_VAL = 8
const val SHARED_CARD_INNER_PADDING_VAL = 16
const val SHARED_SLIDER_MIN_VAL = 0f
const val SHARED_SLIDER_MAX_VAL = 10f
const val SHARED_BATCH_MIN_VAL = 1f
const val SHARED_BATCH_MAX_VAL = 20f
const val SHARED_BATCH_STEPS = 18

const val SHARED_COLOR_GREEN_BG = 0xFFE8F5E9
const val SHARED_COLOR_RED_BG = 0xFFFFEBEE
const val SHARED_COLOR_GREEN_TEXT = 0xFF2E7D32
const val SHARED_COLOR_RED_TEXT = 0xFFC62828
const val SHARED_COLOR_GREY_BG = 0xFF696969
const val SHARED_COLOR_GREY_ONBG = 0xFFA9A9A9
const val SHARED_COLOR_GREY_INDICATOR = 0xFF808080

data class SettingsState(
    val batchSize: Int,
    val timingThreshold: Float,
    val spatialThreshold: Float,
    val motionThreshold: Float
)

data class SettingsActions(
    val onBatchSizeChange: (Float) -> Unit,
    val onTimingChange: (Float) -> Unit,
    val onSpatialChange: (Float) -> Unit,
    val onMotionChange: (Float) -> Unit
)

@Composable
fun SettingsCard(
    title: String = "Настройки",
    state: SettingsState,
    actions: SettingsActions
) {
    Card(
        elevation = CardDefaults.cardElevation(SHARED_CARD_ELEVATION_VAL.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(SHARED_CARD_PADDING_VAL.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(SHARED_CARD_PADDING_VAL.dp))
            Text("Батч верификации: ${state.batchSize}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = state.batchSize.toFloat(),
                onValueChange = actions.onBatchSizeChange,
                valueRange = SHARED_BATCH_MIN_VAL..SHARED_BATCH_MAX_VAL,
                steps = SHARED_BATCH_STEPS,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onSecondary,
                    activeTrackColor = MaterialTheme.colorScheme.onSecondary,
                    activeTickColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTickColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            Spacer(Modifier.height(SHARED_CARD_PADDING_VAL.dp))
            Text("Пороги аномальности:", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(SHARED_CARD_PADDING_VAL.dp))

            ThresholdSlider("TimingModel", state.timingThreshold, actions.onTimingChange)
            ThresholdSlider("SpatialModel", state.spatialThreshold, actions.onSpatialChange)
            ThresholdSlider("MotionModel", state.motionThreshold, actions.onMotionChange)
        }
    }
}

@Composable
fun ThresholdSlider(name: String, value: Float, onValueChange: (Float) -> Unit) {
    Text("$name: ${"%.1f".format(value)} \u03C3", style = MaterialTheme.typography.titleMedium)
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = SHARED_SLIDER_MIN_VAL..SHARED_SLIDER_MAX_VAL,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.onSecondary,
            activeTrackColor = MaterialTheme.colorScheme.onSecondary,
            activeTickColor = MaterialTheme.colorScheme.secondary,
            inactiveTrackColor = MaterialTheme.colorScheme.secondary,
            inactiveTickColor = MaterialTheme.colorScheme.onSecondary
        )
    )
}

data class ResultCardUiState(
    val bgColor: Color,
    val titleColor: Color,
    val titleText: String
)

fun getResultCardUiState(results: List<VerificationResult>): ResultCardUiState {
    if (results.isEmpty()) {
        return ResultCardUiState(Color(SHARED_COLOR_GREY_BG), Color(SHARED_COLOR_GREY_ONBG), "")
    }
    val isOwner = results.find { it.modelName.startsWith("Ensemble") }?.isOwner == true
    return if (isOwner) {
        ResultCardUiState(Color(SHARED_COLOR_GREEN_BG), Color(SHARED_COLOR_GREEN_TEXT), "владелец")
    } else {
        ResultCardUiState(Color(SHARED_COLOR_RED_BG), Color(SHARED_COLOR_RED_TEXT), "взлом")
    }
}

@Composable
fun DetailedResultCard(results: List<VerificationResult>) {
    val uiState = getResultCardUiState(results)
    val models = results.filter { !it.modelName.startsWith("Ensemble") }

    Card(
        elevation = CardDefaults.cardElevation(SHARED_CARD_ELEVATION_VAL.dp),
        colors = CardDefaults.cardColors(containerColor = uiState.bgColor)
    ) {
        Column(modifier = Modifier.padding(SHARED_CARD_INNER_PADDING_VAL.dp)) {
            Row {
                Text(
                    text = "Итог ансамбля:",
                    color = uiState.titleColor,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = uiState.titleText,
                    color = uiState.titleColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider(color = Color(SHARED_COLOR_GREY_INDICATOR))
            Spacer(Modifier.height(SHARED_CARD_PADDING_VAL.dp))

            val modelNames = listOf("TimingModel", "SpatialModel", "MotionModel")

            modelNames.forEach { modelName ->
                val res = models.find { it.modelName == modelName }
                if (res != null) {
                    val statusText = if (res.isOwner) "✅" else "❌"
                    Row {
                        Text(
                            text = "${res.modelName}: ${"%.1f".format(res.anomalyScore)} / ${
                                "%.1f".format(
                                    res.thresholdUsed
                                )
                            }",
                            color = Color.Black
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = statusText,
                            color = Color.Black
                        )
                    }
                } else {
                    Text(
                        text = "$modelName: -",
                        color = Color(SHARED_COLOR_GREY_ONBG)
                    )
                }
            }
        }
    }
}
