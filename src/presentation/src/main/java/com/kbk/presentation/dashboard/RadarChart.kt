package com.kbk.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current

            // отступы со всех сторон
            val textPaddingDp = 60.dp
            val textPaddingPx = with(density) { textPaddingDp.toPx() }
            val usableWidthPx = canvasWidthPx - 2 * textPaddingPx
            val radiusPx = (usableWidthPx / sqrt(3.0)).toFloat().coerceAtLeast(10f)
            val usableHeightPx = 1.5f * radiusPx

            // итоговая высота холста
            val canvasHeightPx = usableHeightPx + 2 * textPaddingPx
            val canvasHeightDp = with(density) { canvasHeightPx.toDp() }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeightDp)
                    .background(Color.White)
            ) {
                val center = Offset(
                    x = size.width / 2f,
                    y = textPaddingPx + radiusPx
                )

                val maxVal = values.maxOrNull()?.coerceAtLeast(0.01f) ?: 1f
                val angles =
                    listOf(270.0, 30.0, 150.0)
                val labels = listOf("Y", "X", "Z")

                // фоновая сетка и шкалы значений
                for (step in 1..3) {
                    val stepRadius = radiusPx * (step / 3f)
                    val stepValue = maxVal * (step / 3f)

                    val gridPoints = angles.map { angleDeg ->
                        val rad = Math.toRadians(angleDeg)
                        Offset(
                            x = center.x + stepRadius * cos(rad).toFloat(),
                            y = center.y + stepRadius * sin(rad).toFloat()
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
                        Color.LightGray.copy(alpha = 0.5f),
                        style = Stroke(width = 2f)
                    )

                    val scaleText = textMeasurer.measure(
                        text = "%.2f".format(stepValue),
                        style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                    )
                    drawText(
                        textLayoutResult = scaleText,
                        topLeft = Offset(
                            center.x + 5f,
                            center.y - stepRadius - scaleText.size.height / 2
                        )
                    )
                }

                // оси и значений метрики
                val points = mutableListOf<Offset>()
                angles.forEachIndexed { i, angleDeg ->
                    val rad = Math.toRadians(angleDeg)

                    val endX = center.x + radiusPx * cos(rad).toFloat()
                    val endY = center.y + radiusPx * sin(rad).toFloat()
                    drawLine(Color.Gray, center, Offset(endX, endY), strokeWidth = 2f)

                    val textOffset = 30.dp.toPx() // сдвиг текста от конца оси
                    val labelX = center.x + (radiusPx + textOffset) * cos(rad).toFloat()
                    val labelY = center.y + (radiusPx + textOffset) * sin(rad).toFloat()

                    val labelText = "${labels[i]}: ${"%.2f".format(values[i])}"
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

                    // точки
                    val vRad = radiusPx * (values[i] / maxVal)
                    points.add(
                        Offset(
                            center.x + vRad * cos(rad).toFloat(),
                            center.y + vRad * sin(rad).toFloat()
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
                drawPath(path, color.copy(alpha = 0.5f))
                drawPath(path, color, style = Stroke(width = 3f))
            }
        }
    }
}
