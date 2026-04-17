package com.kbk.presentation.dashboard

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.BiometricSample
import com.kbk.presentation.keyboard.KeyboardConstants
import com.kbk.presentation.keyboard.KeyboardConstants.DOUBLE_KEYBOARD_PADDING_HORIZONTAL
import com.kbk.presentation.keyboard.KeyboardConstants.DOUBLE_KEYBOARD_PADDING_VERTICAL
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_HEIGHT
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_HORIZONTAL
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_VERTICAL
import com.kbk.presentation.keyboard.KeyboardLanguage
import com.kbk.presentation.keyboard.KeyboardLayouts
import com.kbk.presentation.keyboard.LettersLayout
import com.kbk.presentation.theme.HeatmapTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp

private const val HEATMAP_GRID_SIGMA = 0.2f
private const val HEATMAP_MIN_WEIGHT = 0.05f
private const val HEATMAP_HUE_MAX = 240f
private const val HEATMAP_ALPHA_MAX = 180
private const val HEATMAP_COLOR_ALPHA = 0.8f
private const val HEATMAP_RADIUS_MULTIPLIER = 0.06f
private const val HEATMAP_ROWS_DIVISOR = 5f
private const val HEATMAP_SPACE_MULTIPLIER = 5f
private const val HEATMAP_ENTER_MULTIPLIER = 2f

data class HeatmapConfig(
    val isRu: Boolean,
    val origW: Float,
    val origH: Float,
    val curW: Float,
    val curH: Float,
    val scaleFactor: Float,
    val padX: Float,
    val padY: Float
)

fun getHeatmapColor(value: Float, min: Float, max: Float): Color {
    val fraction = if (max <= min) 0.5f else ((value - min) / (max - min)).coerceIn(0f, 1f)
    val h = (1.0f - fraction) * HEATMAP_HUE_MAX
    val hsv = floatArrayOf(h, 1f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv)).copy(alpha = HEATMAP_COLOR_ALPHA)
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun KeyboardHeatmap(
    isRu: Boolean,
    samples: List<BiometricSample>,
    metricType: HeatmapMetricType
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var minVal by remember { mutableFloatStateOf(0f) }
    var maxVal by remember { mutableFloatStateOf(1f) }

    val density = LocalDensity.current.density
    val padXDp = 2f * density
    val padYDp = 4f * density

    val configuration = LocalConfiguration.current
    val origWidthPx =
        with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() - DOUBLE_KEYBOARD_PADDING_HORIZONTAL.toPx() }
    val origHeightPx =
        with(LocalDensity.current) { KEYBOARD_HEIGHT.toPx() - DOUBLE_KEYBOARD_PADDING_VERTICAL.toPx() }

    // масштабируем пиксели
    // (считаем карту в 10 раза меньшим разрешением, а Canvas ее растянет)
    val scaleFactor = 0.10f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(KEYBOARD_HEIGHT)
            .background(Color.White)
            .padding(horizontal = KEYBOARD_PADDING_HORIZONTAL, vertical = KEYBOARD_PADDING_VERTICAL)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        Column(Modifier.fillMaxSize()) {
            HeatmapTheme {
                LettersLayout(
                    language = if (isRu) KeyboardLanguage.RU else KeyboardLanguage.EN,
                    isShifted = false,
                    isBackdrop = true,
                    onAction = {}
                )
            }
        }

        LaunchedEffect(samples, metricType, isRu, widthPx, heightPx) {
            if (widthPx > 0 && heightPx > 0 && samples.isNotEmpty()) {
                val config = HeatmapConfig(
                    isRu, origWidthPx, origHeightPx, widthPx, heightPx, scaleFactor, padXDp, padYDp
                )
                val result = calculatePixelHeatmap(samples, metricType, config)
                imageBitmap = result.bitmap
                minVal = result.min
                maxVal = result.max
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            imageBitmap?.let { bmp ->
                drawImage(
                    image = bmp,
                    dstSize = androidx.compose.ui.unit.IntSize(widthPx.toInt(), heightPx.toInt()),
                    alpha = 0.7f,
                    filterQuality = FilterQuality.High
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        val brush = Brush.horizontalGradient(
            colors = listOf(
                getHeatmapColor(0f, 0f, 1f),
                getHeatmapColor(0.5f, 0f, 1f),
                getHeatmapColor(1f, 0f, 1f)
            )
        )
        drawRect(brush = brush)
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("%.1f".format(minVal), fontSize = 14.sp)
        Text("%.1f".format((minVal + maxVal) / 2), fontSize = 14.sp)
        Text("${"%.1f".format(maxVal)} ${metricType.unit}", fontSize = 14.sp)
    }
}

/**
 * Поскольку у нас две координаты (X и Y), мы используем двумерную Гауссову функцию.
 * Упрощенно, ее "вес" (влияние) в точке `(x, y)` от центра касания `(cx, cy)` можно описать так:
 *
 * `w(x, y) = e^(-((x-cx)^2 + (y-cy)^2) / h^2)`
 *
 * * `(x, y)` — координаты пикселя, для которого мы считаем "тепло".
 * * `(cx, cy)` — координаты центра нашего касания (нашего "события").
 * * `h` — ширина ядра. Это важный параметр, который мы подбираем.
 * Он определяет, насколько "широко" растекается тепло от одного касания. В коде это наш `radiusPx`.
 *
 * #### Считаем метрики для каждого пикселя
 *
 * Мы знаем, как одно касание создает "тепловое пятно", нужно рассчитать итоговое значение
 * для каждого пикселя на всей карте.
 *
 * Для метрики **"Частота нажатий":** "Тепло" в каждом пикселе — это просто сумма "влияний" (весов)
 * от всех касаний.
 *
 * `Heat(x, y) = Σ w_i(x, y)`
 *
 * * `n` — общее количество касаний.
 * * `w_i(x,y)` — Гауссов вес от *i*-го касания в пикселе `(x, y)`. В коде это массив `weightGrid`.
 *
 * Для метрик **"Время удержания"** и **"Сила нажатия":** сложнее, мы не можем просто суммировать миллисекунды.
 * Если в одну точку попали два касания: одно на 100 мс, другое на 50 мс, — итоговое значение
 * должно быть где-то посередине, а не 150 мс.
 *
 * Нам нужно рассчитать **взвешенное среднее**. "Тепло" в пикселе — это среднее значение метрики всех касаний,
 * взвешенное по их Гауссовому влиянию на этот пиксель.
 *
 * `Value(x, y) = Σ(v_i * w_i(x, y)) / Σ w_i(x, y)`
 *
 * * `v_i` — значение метрики (например, 100 мс) для *i*-го касания.
 * * `w_i(x,y)` — Гауссов вес от *i*-го касания.
 * * Числитель `Σ(v_i * w_i)` — это наш `sumGrid` в коде.
 * * Знаменатель `Σw_i(x,y)` — это наш `weightGrid` in коде.
 */
private data class HeatmapResult(val bitmap: ImageBitmap, val min: Float, val max: Float)

private suspend fun calculatePixelHeatmap(
    samples: List<BiometricSample>,
    metricType: HeatmapMetricType,
    config: HeatmapConfig
): HeatmapResult = withContext(Dispatchers.Default) {
    val w = (config.curW * config.scaleFactor).toInt()
    val h = (config.curH * config.scaleFactor).toInt()

    if (w <= 0 || h <= 0)
        return@withContext HeatmapResult(ImageBitmap(1, 1), 0f, 0f)

    val sumGrid = FloatArray(w * h)
    val weightGrid = FloatArray(w * h)

    accumulateHeat(sumGrid, weightGrid, samples, metricType, config, w, h)
    normalizeAndColorize(sumGrid, weightGrid, metricType, w, h)
}

private fun accumulateHeat(
    sumGrid: FloatArray,
    weightGrid: FloatArray,
    samples: List<BiometricSample>,
    metricType: HeatmapMetricType,
    config: HeatmapConfig,
    w: Int,
    h: Int
) {
    // радиус Гауссова пятна
    val radiusPx = w * HEATMAP_RADIUS_MULTIPLIER
    val radiusSq = radiusPx * radiusPx

    // сетки для исходного экрана и для подложки
    val origRects = buildKeyRects(config.isRu, config.origW, config.origH)
    val curRects = buildKeyRects(config.isRu, config.curW, config.curH)

    samples.forEach { sample ->
        val key = sample.touchData.key.lowercase()
        val origRect = origRects[key] ?: return@forEach
        val curRect = curRects[key] ?: return@forEach

        // внутренняя (кликабельная) ширина/высота кнопок без паддингов
        val origInnerW = origRect.width - 2 * config.padX
        val origInnerH = origRect.height - 2 * config.padY
        val curInnerW = curRect.width - 2 * config.padX
        val curInnerH = curRect.height - 2 * config.padY

        // масштаб для координат внутри самой кнопки
        val scaleX = if (origInnerW > 0) curInnerW / origInnerW else 1f
        val scaleY = if (origInnerH > 0) curInnerH / origInnerH else 1f

        val scaledTouchX = sample.touchData.touchX * scaleX
        val scaledTouchY = sample.touchData.touchY * scaleY

        // глобальные координаты на текущей подложке
        val globalX = curRect.left + config.padX + scaledTouchX
        val globalY = curRect.top + config.padY + scaledTouchY

        val gridX = globalX * config.scaleFactor
        val gridY = globalY * config.scaleFactor

        val value = when (metricType) {
            HeatmapMetricType.FREQUENCY -> 1f
            HeatmapMetricType.DWELL_TIME -> sample.touchData.dwellTime.toFloat()
            HeatmapMetricType.PRESSURE -> sample.touchData.pressure
        }

        val startX = (gridX - radiusPx).toInt().coerceAtLeast(0)
        val endX = (gridX + radiusPx).toInt().coerceAtMost(w - 1)
        val startY = (gridY - radiusPx).toInt().coerceAtLeast(0)
        val endY = (gridY + radiusPx).toInt().coerceAtMost(h - 1)

        // по пикселям внутри прямоугольника
        for (y in startY..endY) {
            for (x in startX..endX) {
                val dx = x - gridX
                val dy = y - gridY
                val distSq = dx * dx + dy * dy
                if (distSq <= radiusSq) {
                    // 0.2f - сигма, насколько концентрировано тепло, больше будет все равномерно
                    val weight = exp(-distSq / (HEATMAP_GRID_SIGMA * radiusSq)) // Гауссиана
                    val idx = y * w + x
                    sumGrid[idx] += value * weight
                    weightGrid[idx] += weight
                }
            }
        }
    }
}

private fun normalizeAndColorize(
    sumGrid: FloatArray,
    weightGrid: FloatArray,
    metricType: HeatmapMetricType,
    w: Int,
    h: Int
): HeatmapResult {
    var minVal = Float.MAX_VALUE
    var maxVal = Float.MIN_VALUE

    // нормализация и итоговые значения
    for (i in sumGrid.indices) {
        if (weightGrid[i] > HEATMAP_MIN_WEIGHT) {
            val v =
                if (metricType == HeatmapMetricType.FREQUENCY)
                    sumGrid[i]
                else sumGrid[i] / weightGrid[i] // взвешенное среднее
            sumGrid[i] = v
            if (v < minVal) minVal = v
            if (v > maxVal) maxVal = v
        } else {
            sumGrid[i] = 0f
        }
    }

    if (minVal == Float.MAX_VALUE) minVal = 0f
    if (maxVal == Float.MIN_VALUE) maxVal = 1f
    if (minVal == maxVal) maxVal = minVal + 1f

    // раскрашиваем
    val pixels = IntArray(w * h)
    for (i in sumGrid.indices) {
        if (weightGrid[i] > HEATMAP_MIN_WEIGHT) {
            val v = sumGrid[i]
            // нормализация в градиенте
            val fraction = ((v - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
            // цвет от синего (240) к красному (0)
            val hue = (1f - fraction) * HEATMAP_HUE_MAX
            // прозрачность зависима от веса для плавности на краях пятна
            val alpha = (weightGrid[i] * 255).toInt().coerceAtMost(HEATMAP_ALPHA_MAX)
            pixels[i] = android.graphics.Color.HSVToColor(alpha, floatArrayOf(hue, 1f, 1f))
        } else {
            pixels[i] = android.graphics.Color.TRANSPARENT
        }
    }

    val bitmap = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    return HeatmapResult(bitmap.asImageBitmap(), minVal, maxVal)
}

// сетка клавиш (повторяет LettersLayout)
private fun buildKeyRects(isRu: Boolean, w: Float, h: Float): Map<String, Rect> {
    val rects = mutableMapOf<String, Rect>()
    val rowH = h / HEATMAP_ROWS_DIVISOR

    buildRow0(w, rowH, rects)
    buildRow1(isRu, w, rowH, rects)
    buildRow2(isRu, w, rowH, rects)
    buildRow3(isRu, w, rowH, rects)
    buildRow4(isRu, w, rowH, rects)

    return rects
}

private fun buildRow0(w: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    // Row 0
    val numKeys = KeyboardLayouts.NUM_ROW_1
    val numW = w / numKeys.size
    var curX = 0f
    numKeys.forEach { k -> rects[k] = Rect(curX, 0f, curX + numW, rowH); curX += numW }
}

private fun buildRow1(isRu: Boolean, w: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    // Row 1
    val r1 = if (isRu) KeyboardLayouts.RU_ROW_1 else KeyboardLayouts.EN_ROW_1
    val r1W = w / r1.size
    var curX = 0f
    r1.forEach { k -> rects[k] = Rect(curX, rowH, curX + r1W, rowH * 2); curX += r1W }
}

private fun buildRow2(isRu: Boolean, w: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    // Row 2
    val r2 = if (isRu) KeyboardLayouts.RU_ROW_2 else KeyboardLayouts.EN_ROW_2
    val pad = if (isRu) 0f else KeyboardConstants.ROW_PADDING
    val totalW2 = pad * 2 + r2.size * 1f
    val r2W = w / totalW2
    var curX = pad * r2W
    r2.forEach { k -> rects[k] = Rect(curX, rowH * 2, curX + r2W, rowH * 3); curX += r2W }
}

private fun buildRow3(isRu: Boolean, w: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    // Row 3
    val r3 = if (isRu) KeyboardLayouts.RU_ROW_3 else KeyboardLayouts.EN_ROW_3
    val sw = if (isRu) 1f else 2f
    val totalW3 = sw * 2 + r3.size * 1f
    val r3W = w / totalW3
    var curX = 0f
    rects["⇧"] = Rect(curX, rowH * 3, curX + sw * r3W, rowH * 4); curX += sw * r3W
    r3.forEach { k -> rects[k] = Rect(curX, rowH * 3, curX + r3W, rowH * 4); curX += r3W }
    rects["⌫"] = Rect(curX, rowH * 3, curX + sw * r3W, rowH * 4)
}

private fun buildRow4(isRu: Boolean, w: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    // Row 4
    // 11f
    val totalW4 = 1f + 1f + 1f + HEATMAP_SPACE_MULTIPLIER + 1f + HEATMAP_ENTER_MULTIPLIER
    val r4W = w / totalW4
    var curX = 0f
    rects["123"] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
    rects[","] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
    rects[if (isRu) "ru" else "en"] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
    // Пробел
    rects[""] = Rect(
        curX,
        rowH * 4,
        curX + HEATMAP_SPACE_MULTIPLIER * r4W,
        rowH * 5
    ); curX += HEATMAP_SPACE_MULTIPLIER * r4W
    rects["."] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
    rects["↵"] = Rect(curX, rowH * 4, curX + HEATMAP_ENTER_MULTIPLIER * r4W, rowH * 5)
}
