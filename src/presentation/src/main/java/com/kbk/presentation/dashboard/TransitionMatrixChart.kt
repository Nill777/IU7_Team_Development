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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransitionMatrixChart(title: String, matrix: Map<Pair<String, String>, Float>) {
    if (matrix.isEmpty()) return

    // уникальные ключи отсортированные по алфавиту
    val keys = matrix.keys.flatMap { listOf(it.first, it.second) }.distinct()
    val sortedKeys = keys.sorted()
    val maxVal = matrix.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.White)
        ) {
            val pad = 50f
            val plotSize = size.width - pad
            val cellSize = plotSize / sortedKeys.size

            // 1. Подписи строк и столбцов
            sortedKeys.forEachIndexed { i, key ->
                val textLayout = textMeasurer.measure(
                    text = key.uppercase(), // Выводим в верхнем регистре для красоты
                    style = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )

                // подписи колонки
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = pad + cellSize * i + (cellSize - textLayout.size.width) / 2,
                        y = pad - textLayout.size.height - 8f
                    )
                )

                // подписи строки
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        x = pad - textLayout.size.width - 8f,
                        y = pad + cellSize * i + (cellSize - textLayout.size.height) / 2
                    )
                )
            }

            // отрисовка
            sortedKeys.forEachIndexed { i, from ->
                sortedKeys.forEachIndexed { j, to ->
                    val value = matrix[from to to] ?: 0f

                    val cellColor = if (value > 0f) {
                        val fraction = (value / maxVal).coerceIn(0f, 1f)
                        val h = (1.0f - fraction) * 240f
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(h, 1f, 1f)))
                    } else {
                        Color.White
                    }

                    val rectTopLeft = Offset(pad + cellSize * j, pad + cellSize * i)
                    val rectSize = Size(cellSize, cellSize)

                    // ячейка
                    drawRect(
                        color = cellColor,
                        topLeft = rectTopLeft,
                        size = rectSize
                    )

                    // граница
                    drawRect(
                        color = Color.Black,
                        topLeft = rectTopLeft,
                        size = rectSize,
                        style = Stroke(width = 1f)
                    )

                    if (value > 0f) {
                        val valText = textMeasurer.measure(
                            text = value.toInt().toString(),
                            style = TextStyle(fontSize = 6.sp, color = Color.Black)
                        )
                        if (valText.size.width < cellSize - 2f) {
                            drawText(
                                textLayoutResult = valText,
                                topLeft = Offset(
                                    x = rectTopLeft.x + (cellSize - valText.size.width) / 2,
                                    y = rectTopLeft.y + (cellSize - valText.size.height) / 2
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)) {
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
            Text("0", fontSize = 14.sp)
            Text("${(maxVal / 2).toInt()}", fontSize = 14.sp)
            Text("${maxVal.toInt()} мс", fontSize = 14.sp)
        }
    }
}