package com.kbk.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.BiometricSample
import kotlin.math.pow
import kotlin.math.sqrt

private const val HISTOGRAM_BIN_COUNT = 100
private const val HISTOGRAM_Y_STEPS = 4
private const val HISTOGRAM_X_STEPS = 5
private const val HISTOGRAM_PAD_LEFT = 60f
private const val HISTOGRAM_PAD_BOTTOM = 50f
private const val HISTOGRAM_PAD_TOP = 70f
private const val HISTOGRAM_PAD_RIGHT = 70f
private const val HISTOGRAM_SIGMA_MULTIPLIER = 2
private const val HISTOGRAM_MIN_STD_DEV = 0.001f
private const val HISTOGRAM_TEXT_OFFSET = 10f
private const val HISTOGRAM_AXIS_WIDTH = 3f
private const val HISTOGRAM_BG_COLOR = 0xFFFFFFFF
private const val HISTOGRAM_LINE_COLOR = 0xFFE0E0E0
private const val HISTOGRAM_SIGMA_COLOR = 0x224CAF50
private const val HISTOGRAM_FRACTION_MAX = 0.999f
private const val HISTOGRAM_Y_LABEL_DIVISOR_X = 1.7f
private const val HISTOGRAM_Y_LABEL_DIVISOR_Y = 10f
private const val HISTOGRAM_X_LABEL_DIVISOR_Y = 0.85f

@Composable
fun HistogramChart(
    title: String,
    unit: String,
    samples: List<BiometricSample>,
    targetKey: String,
    valueSelector: (BiometricSample) -> Float
) {
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val values = samples.filter { it.touchData.key == targetKey }.map(valueSelector)
    if (values.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    val initialMean = values.average().toFloat()
    val initialStdDev = sqrt(values.map { (it - initialMean).pow(2) }.average())
        .toFloat()
        .coerceAtLeast(HISTOGRAM_MIN_STD_DEV)

    // две сигмы (подрезаем статистические выбросы)
    val lowerBound = initialMean - HISTOGRAM_SIGMA_MULTIPLIER * initialStdDev
    val upperBound = initialMean + HISTOGRAM_SIGMA_MULTIPLIER * initialStdDev
    val filteredValues = values.filter { it in lowerBound..upperBound }
    if (filteredValues.isEmpty()) return

    val mean = filteredValues.average().toFloat()
    val stdDev = sqrt(filteredValues.map { (it - mean).pow(2) }.average())
        .toFloat()
        .coerceAtLeast(HISTOGRAM_MIN_STD_DEV)

    var minVal = filteredValues.minOrNull() ?: 0f
    var maxVal = filteredValues.maxOrNull() ?: 1f
    if (minVal == maxVal) {
        minVal -= 1f
        maxVal += 1f
    }

    val bins = IntArray(HISTOGRAM_BIN_COUNT)
    filteredValues.forEach { v ->
        val fraction = ((v - minVal) / (maxVal - minVal)).coerceIn(0f, HISTOGRAM_FRACTION_MAX)
        val idx = (fraction * HISTOGRAM_BIN_COUNT).toInt()
        bins[idx]++
    }
    val maxBinCount = bins.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            text = "μ = ${"%.2f".format(mean)}, σ = ${"%.2f".format(stdDev)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(HISTOGRAM_BG_COLOR))
        ) {
            val plotW = size.width - HISTOGRAM_PAD_LEFT - HISTOGRAM_PAD_RIGHT
            val plotH = size.height - HISTOGRAM_PAD_BOTTOM - HISTOGRAM_PAD_TOP

            drawHistogramGrid(maxBinCount, minVal, maxVal, plotW, plotH, textMeasurer)
            drawHistogramSigma(mean, stdDev, minVal, maxVal, plotW, plotH)
            drawHistogramBars(bins, maxBinCount, minVal, maxVal, mean, plotW, plotH, secondaryColor)
            drawHistogramAxes(unit, textMeasurer)
        }
    }
}

private fun DrawScope.drawHistogramGrid(
    maxBinCount: Int,
    minVal: Float,
    maxVal: Float,
    plotW: Float,
    plotH: Float,
    textMeasurer: TextMeasurer
) {
    // cетка и подписи осей
    for (i in 0..HISTOGRAM_Y_STEPS) {
        val yCount = maxBinCount * i / HISTOGRAM_Y_STEPS
        val yPos = size.height - HISTOGRAM_PAD_BOTTOM - i / HISTOGRAM_Y_STEPS.toFloat() * plotH
        drawLine(
            Color(HISTOGRAM_LINE_COLOR),
            Offset(HISTOGRAM_PAD_LEFT, yPos),
            Offset(size.width - HISTOGRAM_PAD_RIGHT, yPos)
        )

        val textLayout = textMeasurer.measure(
            yCount.toString(),
            TextStyle(fontSize = 10.sp, color = Color.DarkGray)
        )
        drawText(
            textLayout,
            topLeft = Offset(
                HISTOGRAM_PAD_LEFT - textLayout.size.width - HISTOGRAM_TEXT_OFFSET,
                yPos - textLayout.size.height / 2
            )
        )
    }

    for (i in 0..HISTOGRAM_X_STEPS) {
        val xVal = minVal + (maxVal - minVal) * i / HISTOGRAM_X_STEPS
        val xPos = HISTOGRAM_PAD_LEFT + i / HISTOGRAM_X_STEPS.toFloat() * plotW
        drawLine(
            Color(HISTOGRAM_LINE_COLOR),
            Offset(xPos, HISTOGRAM_PAD_TOP),
            Offset(xPos, size.height - HISTOGRAM_PAD_BOTTOM)
        )

        val label = "%.0f".format(xVal)
        val textLayout =
            textMeasurer.measure(
                label,
                TextStyle(fontSize = 10.sp, color = Color.DarkGray)
            )
        drawText(
            textLayout,
            topLeft = Offset(
                xPos - textLayout.size.width / 2,
                size.height - HISTOGRAM_PAD_BOTTOM + HISTOGRAM_TEXT_OFFSET
            )
        )
    }
}

private fun DrawScope.drawHistogramSigma(
    mean: Float,
    stdDev: Float,
    minVal: Float,
    maxVal: Float,
    plotW: Float,
    plotH: Float
) {
    // коридор сигма
    val sigmaLeft = (mean - stdDev).coerceAtLeast(minVal)
    val sigmaRight = (mean + stdDev).coerceAtMost(maxVal)

    val startX = HISTOGRAM_PAD_LEFT + (sigmaLeft - minVal) / (maxVal - minVal) * plotW
    val endX = HISTOGRAM_PAD_LEFT + (sigmaRight - minVal) / (maxVal - minVal) * plotW

    drawRect(
        color = Color(HISTOGRAM_SIGMA_COLOR),
        topLeft = Offset(startX, HISTOGRAM_PAD_TOP),
        size = Size(endX - startX, plotH)
    )
}

private fun DrawScope.drawHistogramBars(
    bins: IntArray,
    maxBinCount: Int,
    minVal: Float,
    maxVal: Float,
    mean: Float,
    plotW: Float,
    plotH: Float,
    secondaryColor: Color
) {
    // столбцы гистограммы
    val binW = plotW / HISTOGRAM_BIN_COUNT
    for (i in 0 until HISTOGRAM_BIN_COUNT) {
        val count = bins[i]
        if (count > 0) {
            val barH = count.toFloat() / maxBinCount * plotH
            val barX = HISTOGRAM_PAD_LEFT + i * binW
            val barY = size.height - HISTOGRAM_PAD_BOTTOM - barH
            drawRect(
                color = secondaryColor,
                topLeft = Offset(barX, barY),
                size = Size(binW, barH)
            )
        }
    }

    val meanX = HISTOGRAM_PAD_LEFT + (mean - minVal) / (maxVal - minVal) * plotW
    drawLine(
        Color.Red,
        Offset(meanX, HISTOGRAM_PAD_TOP),
        Offset(meanX, size.height - HISTOGRAM_PAD_BOTTOM),
        strokeWidth = HISTOGRAM_AXIS_WIDTH
    )
}

private fun DrawScope.drawHistogramAxes(
    unit: String,
    textMeasurer: TextMeasurer
) {
    // оси
    drawLine(
        Color.Black,
        Offset(HISTOGRAM_PAD_LEFT, HISTOGRAM_PAD_TOP),
        Offset(HISTOGRAM_PAD_LEFT, size.height - HISTOGRAM_PAD_BOTTOM),
        strokeWidth = HISTOGRAM_AXIS_WIDTH
    )
    drawLine(
        Color.Black,
        Offset(HISTOGRAM_PAD_LEFT, size.height - HISTOGRAM_PAD_BOTTOM),
        Offset(size.width - HISTOGRAM_PAD_RIGHT, size.height - HISTOGRAM_PAD_BOTTOM),
        strokeWidth = HISTOGRAM_AXIS_WIDTH
    )

    // подписи к осям
    drawText(
        textMeasurer.measure(
            "шт",
            TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        ), topLeft = Offset(
            HISTOGRAM_PAD_LEFT / HISTOGRAM_Y_LABEL_DIVISOR_X,
            HISTOGRAM_PAD_TOP / HISTOGRAM_Y_LABEL_DIVISOR_Y
        )
    )
    val xLabel = textMeasurer.measure(
        unit,
        TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        xLabel,
        topLeft = Offset(
            size.width - HISTOGRAM_PAD_LEFT,
            size.height - HISTOGRAM_PAD_TOP / HISTOGRAM_X_LABEL_DIVISOR_Y
        )
    )
}
