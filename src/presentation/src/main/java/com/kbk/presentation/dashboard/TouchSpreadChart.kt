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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.BiometricSample

@Composable
fun TouchSpreadChart(
    title: String,
    samples: List<BiometricSample>,
    targetKey: String
) {
    val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    val values = samples.filter { it.touchData.key == targetKey }
    if (values.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    val maxX = values.maxOfOrNull { it.touchData.touchX }?.coerceAtLeast(1f) ?: 1f
    val maxY = values.maxOfOrNull { it.touchData.touchY }?.coerceAtLeast(1f) ?: 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

            // сетка
            val steps = 4
            for (i in 0..steps) {
                val yPos = size.height - padBottom - (i / steps.toFloat()) * plotH
                val yVal = (maxY * i) / steps
                drawLine(
                    Color(0xFFE0E0E0),
                    Offset(padLeft, yPos),
                    Offset(size.width - padRight, yPos)
                )
                val yText = textMeasurer.measure(
                    "%.0f".format(yVal),
                    TextStyle(fontSize = 10.sp, color = Color.DarkGray)
                )
                drawText(
                    yText,
                    topLeft = Offset(padLeft - yText.size.width - 10f, yPos - yText.size.height / 2)
                )

                val xPos = padLeft + (i / steps.toFloat()) * plotW
                val xVal = (maxX * i) / steps
                drawLine(
                    Color(0xFFE0E0E0),
                    Offset(xPos, padTop),
                    Offset(xPos, size.height - padBottom)
                )
                val xText = textMeasurer.measure(
                    "%.0f".format(xVal),
                    TextStyle(fontSize = 10.sp, color = Color.DarkGray)
                )
                drawText(
                    xText,
                    topLeft = Offset(xPos - xText.size.width / 2, size.height - padBottom + 10f)
                )
            }

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
                    "y",
                    TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                ), topLeft = Offset(
                    padLeft / 1.2f,
                    padTop / 10
                )
            )
            drawText(
                textMeasurer.measure(
                    "x",
                    TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                ),
                topLeft = Offset(
                    size.width - padLeft,
                    size.height - padTop / 0.85f
                )
            )

            values.forEach { sample ->
                val x = padLeft + (sample.touchData.touchX / maxX) * plotW
                val y = size.height - padBottom - (sample.touchData.touchY / maxY) * plotH
                drawCircle(secondary, radius = 5f, center = Offset(x, y))
            }
        }
    }
}
