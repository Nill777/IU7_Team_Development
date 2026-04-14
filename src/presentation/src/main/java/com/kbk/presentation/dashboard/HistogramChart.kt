package com.kbk.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.BiometricSample
import kotlin.math.pow
import kotlin.math.sqrt

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
        .coerceAtLeast(0.001f)

    // две сигмы (подрезаем статистические выбросы)
    val lowerBound = initialMean - 2 * initialStdDev
    val upperBound = initialMean + 2 * initialStdDev
    val filteredValues = values.filter { it in lowerBound..upperBound }
    if (filteredValues.isEmpty()) return

    val mean = filteredValues.average().toFloat()
    val stdDev = sqrt(filteredValues.map { (it - mean).pow(2) }.average())
        .toFloat()
        .coerceAtLeast(0.001f)

    var minVal = filteredValues.minOrNull() ?: 0f
    var maxVal = filteredValues.maxOrNull() ?: 1f
    if (minVal == maxVal) {
        minVal -= 1f
        maxVal += 1f
    }

    val binCount = 100
    val bins = IntArray(binCount)
    filteredValues.forEach { v ->
        val fraction = ((v - minVal) / (maxVal - minVal)).coerceIn(0f, 0.999f)
        val idx = (fraction * binCount).toInt()
        bins[idx]++
    }
    val maxBinCount = bins.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
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
                .background(Color(0xFFFAFAFA))
        ) {
            val padLeft = 60f
            val padBottom = 50f
            val padTop = 70f
            val padRight = 70f

            val plotW = size.width - padLeft - padRight
            val plotH = size.height - padBottom - padTop

            // cетка и подписи осей
            val ySteps = 4
            for (i in 0..ySteps) {
                val yCount = (maxBinCount * i) / ySteps
                val yPos = size.height - padBottom - (i / ySteps.toFloat()) * plotH
                drawLine(
                    Color(0xFFE0E0E0),
                    Offset(padLeft, yPos),
                    Offset(size.width - padRight, yPos)
                )

                val textLayout = textMeasurer.measure(
                    yCount.toString(),
                    TextStyle(fontSize = 10.sp, color = Color.DarkGray)
                )
                drawText(
                    textLayout,
                    topLeft = Offset(
                        padLeft - textLayout.size.width - 10f,
                        yPos - textLayout.size.height / 2
                    )
                )
            }

            val xSteps = 5
            for (i in 0..xSteps) {
                val xVal = minVal + (maxVal - minVal) * i / xSteps
                val xPos = padLeft + (i / xSteps.toFloat()) * plotW
                drawLine(
                    Color(0xFFE0E0E0),
                    Offset(xPos, padTop),
                    Offset(xPos, size.height - padBottom)
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
                        size.height - padBottom + 10f
                    )
                )
            }

            // коридор сигма
            val sigmaLeft = (mean - stdDev).coerceAtLeast(minVal)
            val sigmaRight = (mean + stdDev).coerceAtMost(maxVal)

            val startX = padLeft + ((sigmaLeft - minVal) / (maxVal - minVal)) * plotW
            val endX = padLeft + ((sigmaRight - minVal) / (maxVal - minVal)) * plotW

            drawRect(
                color = Color(0x224CAF50),
                topLeft = Offset(startX, padTop),
                size = Size(endX - startX, plotH)
            )

            // столбцы гистограммы
            val binW = plotW / binCount
            for (i in 0 until binCount) {
                val count = bins[i]
                if (count > 0) {
                    val barH = (count.toFloat() / maxBinCount) * plotH
                    val barX = padLeft + i * binW
                    val barY = size.height - padBottom - barH
                    drawRect(
                        color = secondaryColor,
                        topLeft = Offset(barX, barY),
                        size = Size(binW, barH)
                    )
                }
            }

            val meanX = padLeft + ((mean - minVal) / (maxVal - minVal)) * plotW
            drawLine(
                Color.Red,
                Offset(meanX, padTop),
                Offset(meanX, size.height - padBottom),
                strokeWidth = 3f
            )

            // оси
            drawLine(
                Color.Black,
                Offset(padLeft, padTop),
                Offset(padLeft, size.height - padBottom),
                strokeWidth = 3f
            )
            drawLine(
                Color.Black,
                Offset(padLeft, size.height - padBottom),
                Offset(size.width - padRight, size.height - padBottom),
                strokeWidth = 3f
            )

            // подписи к осям
            drawText(
                textMeasurer.measure(
                    "шт",
                    TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                ), topLeft = Offset(
                    padLeft/1.5f,
                    padTop/3
                )
            )
            val xLabel = textMeasurer.measure(
                unit,
                TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
            drawText(
                xLabel,
                topLeft = Offset(
                    size.width - padLeft,
                    size.height - padTop
                )
            )
        }
    }
}
