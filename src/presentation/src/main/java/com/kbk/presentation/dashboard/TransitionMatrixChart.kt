package com.kbk.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val MATRIX_PAD = 50f
private const val MATRIX_MIN_MAX_VAL = 1f
private const val MATRIX_LABEL_OFFSET = 8f
private const val MATRIX_HUE_MULTIPLIER = 240f
private const val MATRIX_CELL_STROKE = 1f
private const val MATRIX_VAL_TEXT_OFFSET = 2f
private const val TEXT_SIZE_LABEL = 10
private const val TEXT_SIZE_VALUE = 6
private const val TEXT_SIZE_LEGEND = 14
private const val GRADIENT_HEIGHT_VAL = 16
private const val CHART_SPACING_VAL = 8

data class MatrixDrawConfig(
    val pad: Float,
    val cellSize: Float,
    val maxVal: Float
)

@Composable
fun TransitionMatrixChart(title: String, matrix: Map<Pair<String, String>, Float>) {
    if (matrix.isEmpty()) return

    // уникальные ключи отсортированные по алфавиту
    val keys = matrix.keys.flatMap { listOf(it.first, it.second) }.distinct()
    val sortedKeys = keys.sorted()
    val maxVal = matrix.values.maxOrNull()?.coerceAtLeast(MATRIX_MIN_MAX_VAL) ?: MATRIX_MIN_MAX_VAL
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(CHART_SPACING_VAL.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White)
        ) {
            val pad = MATRIX_PAD
            val plotSize = size.width - pad
            val cellSize = plotSize / sortedKeys.size
            val config = MatrixDrawConfig(pad, cellSize, maxVal)

            drawMatrixLabels(sortedKeys, config, textMeasurer)
            drawMatrixCells(sortedKeys, matrix, config, textMeasurer)
        }

        TransitionMatrixLegend(maxVal)
    }
}

@Composable
private fun TransitionMatrixLegend(maxVal: Float) {
    Spacer(Modifier.height(GRADIENT_HEIGHT_VAL.dp))

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(GRADIENT_HEIGHT_VAL.dp)
    ) {
        val brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Blue,
                Color.Green,
                Color.Red
            )
        )
        drawRect(brush = brush)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("0", fontSize = TEXT_SIZE_LEGEND.sp)
        Text("${(maxVal / 2).toInt()}", fontSize = TEXT_SIZE_LEGEND.sp)
        Text("${maxVal.toInt()} мс", fontSize = TEXT_SIZE_LEGEND.sp)
    }
}

private fun DrawScope.drawMatrixLabels(
    sortedKeys: List<String>,
    config: MatrixDrawConfig,
    textMeasurer: TextMeasurer
) {
    // 1. Подписи строк и столбцов
    sortedKeys.forEachIndexed { i, key ->
        val textLayout = textMeasurer.measure(
            text = key.uppercase(), // Выводим в верхнем регистре для красоты
            style = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = TEXT_SIZE_LABEL.sp
            )
        )

        // подписи колонки
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = config.pad + config.cellSize * i + (config.cellSize - textLayout.size.width) / 2,
                y = config.pad - textLayout.size.height - MATRIX_LABEL_OFFSET
            )
        )

        // подписи строки
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                x = config.pad - textLayout.size.width - MATRIX_LABEL_OFFSET,
                y = config.pad + config.cellSize * i + (config.cellSize - textLayout.size.height) / 2
            )
        )
    }
}

private fun DrawScope.drawMatrixCells(
    sortedKeys: List<String>,
    matrix: Map<Pair<String, String>, Float>,
    config: MatrixDrawConfig,
    textMeasurer: TextMeasurer
) {
    // отрисовка
    sortedKeys.forEachIndexed { i, from ->
        sortedKeys.forEachIndexed { j, to ->
            val value = matrix[from to to] ?: 0f

            val cellColor = if (value > 0f) {
                val fraction = (value / config.maxVal).coerceIn(0f, 1f)
                val h = (1.0f - fraction) * MATRIX_HUE_MULTIPLIER
                Color(android.graphics.Color.HSVToColor(floatArrayOf(h, 1f, 1f)))
            } else {
                Color.White
            }

            val rectTopLeft =
                Offset(config.pad + config.cellSize * j, config.pad + config.cellSize * i)
            val rectSize = Size(config.cellSize, config.cellSize)

            // ячейка
            drawRect(color = cellColor, topLeft = rectTopLeft, size = rectSize)

            // граница
            drawRect(
                color = Color.Black,
                topLeft = rectTopLeft,
                size = rectSize,
                style = Stroke(width = MATRIX_CELL_STROKE)
            )

            if (value > 0f) {
                val valText = textMeasurer.measure(
                    text = value.toInt().toString(),
                    style = TextStyle(fontSize = TEXT_SIZE_VALUE.sp, color = Color.Black)
                )
                if (valText.size.width < config.cellSize - MATRIX_VAL_TEXT_OFFSET) {
                    drawText(
                        textLayoutResult = valText,
                        topLeft = Offset(
                            x = rectTopLeft.x + (config.cellSize - valText.size.width) / 2,
                            y = rectTopLeft.y + (config.cellSize - valText.size.height) / 2
                        )
                    )
                }
            }
        }
    }
}
