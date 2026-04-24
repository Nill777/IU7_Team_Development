package com.kbk.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.BiometricSample
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val RADAR_ANGLE_Y = 270.0
private const val RADAR_ANGLE_X = 30.0
private const val RADAR_ANGLE_Z = 150.0
private const val RADAR_MIN_RADIUS = 10f
private const val RADAR_RADIUS_MULTIPLIER = 1.5f
private const val RADAR_GRID_STEPS = 3
private const val RADAR_STROKE_WIDTH = 2f
private const val RADAR_POLYGON_STROKE_WIDTH = 3f
private const val RADAR_TEXT_OFFSET_DP = 30f
private const val RADAR_GRID_ALPHA = 0.5f
private const val RADAR_SCALE_TEXT_OFFSET_PX = 5f
private const val RADAR_MIN_MAX_COERCE = 0.01f
private const val RADAR_DEFAULT_MAX_VAL = 1f
private const val SQRT_3 = 3.0
private const val PADDING_TEXT_DP = 60

data class RadarConfig(
    val center: Offset,
    val radiusPx: Float,
    val maxVal: Float,
    val values: FloatArray,
    val angles: List<Double>
)

@Composable
fun RadarChart(
    title: String,
    samples: List<BiometricSample>,
    sensorType: SensorType,
    color: Color
) {
    if (samples.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()

    val values = when (sensorType) {
        SensorType.ACCELEROMETER -> floatArrayOf(
            samples.map { abs(it.motionData.accX) }.average().toFloat(),
            samples.map { abs(it.motionData.accY) }.average().toFloat(),
            samples.map { abs(it.motionData.accZ) }.average().toFloat()
        )

        SensorType.GYROSCOPE -> floatArrayOf(
            samples.map { abs(it.motionData.gyroX) }.average().toFloat(),
            samples.map { abs(it.motionData.gyroY) }.average().toFloat(),
            samples.map { abs(it.motionData.gyroZ) }.average().toFloat()
        )

        SensorType.ROTATION_VECTOR -> floatArrayOf(
            samples.map { abs(it.motionData.rotVecX) }.average().toFloat(),
            samples.map { abs(it.motionData.rotVecY) }.average().toFloat(),
            samples.map { abs(it.motionData.rotVecZ) }.average().toFloat()
        )

        SensorType.GRAVITY -> floatArrayOf(
            samples.map { abs(it.motionData.gravX) }.average().toFloat(),
            samples.map { abs(it.motionData.gravY) }.average().toFloat(),
            samples.map { abs(it.motionData.gravZ) }.average().toFloat()
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current

            // отступы со всех сторон
            val textPaddingDp = PADDING_TEXT_DP.dp
            val textPaddingPx = with(density) { textPaddingDp.toPx() }
            val usableWidthPx = canvasWidthPx - 2 * textPaddingPx
            val radiusPx = (usableWidthPx / sqrt(SQRT_3)).toFloat().coerceAtLeast(RADAR_MIN_RADIUS)
            val usableHeightPx = RADAR_RADIUS_MULTIPLIER * radiusPx

            // итоговая высота холста
            val canvasHeightPx = usableHeightPx + 2 * textPaddingPx
            val canvasHeightDp = with(density) { canvasHeightPx.toDp() }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeightDp)
                    .background(Color.White)
            ) {
                val center = Offset(x = size.width / 2f, y = textPaddingPx + radiusPx)
                val maxVal =
                    values.maxOrNull()?.coerceAtLeast(RADAR_MIN_MAX_COERCE) ?: RADAR_DEFAULT_MAX_VAL
                val angles = listOf(RADAR_ANGLE_Y, RADAR_ANGLE_X, RADAR_ANGLE_Z)
                val labels = listOf("Y", "X", "Z")

                val config = RadarConfig(center, radiusPx, maxVal, values, angles)

                drawRadarGrid(config, textMeasurer)
                drawRadarAxes(config, labels, textMeasurer)
                drawRadarPolygon(config, color)
            }
        }
    }
}

private fun DrawScope.drawRadarGrid(config: RadarConfig, textMeasurer: TextMeasurer) {
    // фоновая сетка и шкалы значений
    for (step in 1..RADAR_GRID_STEPS) {
        val stepRadius = config.radiusPx * (step / RADAR_GRID_STEPS.toFloat())
        val stepValue = config.maxVal * (step / RADAR_GRID_STEPS.toFloat())

        val gridPoints = config.angles.map { angleDeg ->
            val rad = Math.toRadians(angleDeg)
            Offset(
                x = config.center.x + stepRadius * cos(rad).toFloat(),
                y = config.center.y + stepRadius * sin(rad).toFloat()
            )
        }
        val gridPath = Path().apply {
            moveTo(gridPoints[0].x, gridPoints[0].y)
            lineTo(gridPoints[1].x, gridPoints[1].y)
            lineTo(gridPoints[2].x, gridPoints[2].y)
            close()
        }
        drawPath(
            gridPath,
            Color.LightGray.copy(alpha = RADAR_GRID_ALPHA),
            style = Stroke(width = RADAR_STROKE_WIDTH)
        )

        val scaleText = textMeasurer.measure(
            text = "%.2f".format(stepValue),
            style = TextStyle(fontSize = 10.sp, color = Color.Gray)
        )
        drawText(
            textLayoutResult = scaleText,
            topLeft = Offset(
                config.center.x + RADAR_SCALE_TEXT_OFFSET_PX,
                config.center.y - stepRadius - scaleText.size.height / 2
            )
        )
    }
}

private fun DrawScope.drawRadarAxes(
    config: RadarConfig,
    labels: List<String>,
    textMeasurer: TextMeasurer
) {
    // оси и значений метрики
    config.angles.forEachIndexed { i, angleDeg ->
        val rad = Math.toRadians(angleDeg)

        val endX = config.center.x + config.radiusPx * cos(rad).toFloat()
        val endY = config.center.y + config.radiusPx * sin(rad).toFloat()
        drawLine(Color.Gray, config.center, Offset(endX, endY), strokeWidth = RADAR_STROKE_WIDTH)

        val textOffset = RADAR_TEXT_OFFSET_DP.dp.toPx()
        val labelX = config.center.x + (config.radiusPx + textOffset) * cos(rad).toFloat()
        val labelY = config.center.y + (config.radiusPx + textOffset) * sin(rad).toFloat()

        val labelText = "${labels[i]}: ${"%.2f".format(config.values[i])}"
        val textLayout = textMeasurer.measure(
            text = labelText,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                labelX - textLayout.size.width / 2,
                labelY - textLayout.size.height / 2
            )
        )
    }
}

private fun DrawScope.drawRadarPolygon(config: RadarConfig, color: Color) {
    val points = mutableListOf<Offset>()
    config.angles.forEachIndexed { i, angleDeg ->
        val rad = Math.toRadians(angleDeg)
        val vRad = config.radiusPx * (config.values[i] / config.maxVal)
        points.add(
            Offset(
                config.center.x + vRad * cos(rad).toFloat(),
                config.center.y + vRad * sin(rad).toFloat()
            )
        )
    }

    // полигон
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        lineTo(points[1].x, points[1].y)
        lineTo(points[2].x, points[2].y)
        close()
    }
    drawPath(path, color.copy(alpha = RADAR_GRID_ALPHA))
    drawPath(path, color, style = Stroke(width = RADAR_POLYGON_STROKE_WIDTH))
}
