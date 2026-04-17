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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.BiometricSample

private const val TOUCH_SPREAD_GRID_STEPS = 4
private const val TOUCH_SPREAD_PAD_LEFT = 60f
private const val TOUCH_SPREAD_PAD_BOTTOM = 50f
private const val TOUCH_SPREAD_PAD_TOP = 70f
private const val TOUCH_SPREAD_PAD_RIGHT = 70f
private const val TOUCH_SPREAD_TEXT_OFFSET = 10f
private const val TOUCH_SPREAD_AXIS_WIDTH = 3f
private const val TOUCH_SPREAD_CIRCLE_RADIUS = 5f
private const val TOUCH_SPREAD_Y_LABEL_DIVISOR_X = 1.2f
private const val TOUCH_SPREAD_Y_LABEL_DIVISOR_Y = 10f
private const val TOUCH_SPREAD_X_LABEL_DIVISOR_Y = 0.85f
private const val TOUCH_SPREAD_BG_COLOR = 0xFFFAFAFA
private const val TOUCH_SPREAD_LINE_COLOR = 0xFFE0E0E0

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
                .background(Color(TOUCH_SPREAD_BG_COLOR))
        ) {
            val plotW = size.width - TOUCH_SPREAD_PAD_LEFT - TOUCH_SPREAD_PAD_RIGHT
            val plotH = size.height - TOUCH_SPREAD_PAD_BOTTOM - TOUCH_SPREAD_PAD_TOP

            drawTouchSpreadGrid(maxX, maxY, plotW, plotH, textMeasurer)
            drawTouchSpreadAxes(textMeasurer)
            drawTouchSpreadPoints(values, maxX, maxY, plotW, plotH, secondary)
        }
    }
}

private fun DrawScope.drawTouchSpreadGrid(
    maxX: Float,
    maxY: Float,
    plotW: Float,
    plotH: Float,
    textMeasurer: TextMeasurer
) {
    // сетка
    for (i in 0..TOUCH_SPREAD_GRID_STEPS) {
        val yPos =
            size.height - TOUCH_SPREAD_PAD_BOTTOM - i / TOUCH_SPREAD_GRID_STEPS.toFloat() * plotH
        val yVal = maxY * i / TOUCH_SPREAD_GRID_STEPS
        drawLine(
            Color(TOUCH_SPREAD_LINE_COLOR),
            Offset(TOUCH_SPREAD_PAD_LEFT, yPos),
            Offset(size.width - TOUCH_SPREAD_PAD_RIGHT, yPos)
        )
        val yText = textMeasurer.measure(
            "%.0f".format(yVal),
            TextStyle(fontSize = 10.sp, color = Color.DarkGray)
        )
        drawText(
            yText,
            topLeft = Offset(
                TOUCH_SPREAD_PAD_LEFT - yText.size.width - TOUCH_SPREAD_TEXT_OFFSET,
                yPos - yText.size.height / 2
            )
        )

        val xPos = TOUCH_SPREAD_PAD_LEFT + i / TOUCH_SPREAD_GRID_STEPS.toFloat() * plotW
        val xVal = maxX * i / TOUCH_SPREAD_GRID_STEPS
        drawLine(
            Color(TOUCH_SPREAD_LINE_COLOR),
            Offset(xPos, TOUCH_SPREAD_PAD_TOP),
            Offset(xPos, size.height - TOUCH_SPREAD_PAD_BOTTOM)
        )
        val xText = textMeasurer.measure(
            "%.0f".format(xVal),
            TextStyle(fontSize = 10.sp, color = Color.DarkGray)
        )
        drawText(
            xText,
            topLeft = Offset(
                xPos - xText.size.width / 2,
                size.height - TOUCH_SPREAD_PAD_BOTTOM + TOUCH_SPREAD_TEXT_OFFSET
            )
        )
    }
}

private fun DrawScope.drawTouchSpreadAxes(
    textMeasurer: TextMeasurer
) {
    // оси
    drawLine(
        Color.Black,
        Offset(TOUCH_SPREAD_PAD_LEFT, TOUCH_SPREAD_PAD_TOP),
        Offset(TOUCH_SPREAD_PAD_LEFT, size.height - TOUCH_SPREAD_PAD_BOTTOM),
        strokeWidth = TOUCH_SPREAD_AXIS_WIDTH
    )
    drawLine(
        Color.Black,
        Offset(TOUCH_SPREAD_PAD_LEFT, size.height - TOUCH_SPREAD_PAD_BOTTOM),
        Offset(size.width - TOUCH_SPREAD_PAD_RIGHT, size.height - TOUCH_SPREAD_PAD_BOTTOM),
        strokeWidth = TOUCH_SPREAD_AXIS_WIDTH
    )

    // подписи к осям
    drawText(
        textMeasurer.measure(
            "y",
            TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        ), topLeft = Offset(
            TOUCH_SPREAD_PAD_LEFT / TOUCH_SPREAD_Y_LABEL_DIVISOR_X,
            TOUCH_SPREAD_PAD_TOP / TOUCH_SPREAD_Y_LABEL_DIVISOR_Y
        )
    )
    drawText(
        textMeasurer.measure(
            "x",
            TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        ),
        topLeft = Offset(
            size.width - TOUCH_SPREAD_PAD_LEFT,
            size.height - TOUCH_SPREAD_PAD_TOP / TOUCH_SPREAD_X_LABEL_DIVISOR_Y
        )
    )
}

private fun DrawScope.drawTouchSpreadPoints(
    values: List<BiometricSample>,
    maxX: Float,
    maxY: Float,
    plotW: Float,
    plotH: Float,
    secondary: Color
) {
    values.forEach { sample ->
        val x = TOUCH_SPREAD_PAD_LEFT + sample.touchData.touchX / maxX * plotW
        val y = size.height - TOUCH_SPREAD_PAD_BOTTOM - sample.touchData.touchY / maxY * plotH
        drawCircle(secondary, radius = TOUCH_SPREAD_CIRCLE_RADIUS, center = Offset(x, y))
    }
}
