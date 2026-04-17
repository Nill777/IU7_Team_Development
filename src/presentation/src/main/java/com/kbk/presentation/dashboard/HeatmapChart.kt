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
private const val SCALE_FACTOR = 0.10f
private const val HEATMAP_IMAGE_ALPHA = 0.7f
private const val GRADIENT_HEIGHT_VAL = 16

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

    HeatmapCanvasArea(
        isRu = isRu,
        samples = samples,
        metricType = metricType,
        origWidthPx = origWidthPx,
        origHeightPx = origHeightPx,
        padXDp = padXDp,
        padYDp = padYDp,
        imageBitmap = imageBitmap,
        onUpdateBitmap = { bmp, min, max ->
            imageBitmap = bmp
            minVal = min
            maxVal = max
        }
    )

    HeatmapLegend(minVal, maxVal, metricType)
}

@Composable
private fun HeatmapCanvasArea(
    isRu: Boolean,
    samples: List<BiometricSample>,
    metricType: HeatmapMetricType,
    origWidthPx: Float,
    origHeightPx: Float,
    padXDp: Float,
    padYDp: Float,
    imageBitmap: ImageBitmap?,
    onUpdateBitmap: (ImageBitmap, Float, Float) -> Unit
) {
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
                    isRu, origWidthPx, origHeightPx, widthPx, heightPx, SCALE_FACTOR, padXDp, padYDp
                )
                val calculator = HeatmapCalculator(samples, metricType, config)
                val result = calculator.calculate()
                onUpdateBitmap(result.bitmap, result.min, result.max)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            imageBitmap?.let { bmp ->
                drawImage(
                    image = bmp,
                    dstSize = androidx.compose.ui.unit.IntSize(widthPx.toInt(), heightPx.toInt()),
                    alpha = HEATMAP_IMAGE_ALPHA,
                    filterQuality = FilterQuality.High
                )
            }
        }
    }
}

@Composable
private fun HeatmapLegend(minVal: Float, maxVal: Float, metricType: HeatmapMetricType) {
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

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("%.1f".format(minVal), fontSize = 14.sp)
        Text("%.1f".format((minVal + maxVal) / 2), fontSize = 14.sp)
        Text("${"%.1f".format(maxVal)} ${metricType.unit}", fontSize = 14.sp)
    }
}

private data class HeatmapResult(val bitmap: ImageBitmap, val min: Float, val max: Float)

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
private class HeatmapCalculator(
    private val samples: List<BiometricSample>,
    private val metricType: HeatmapMetricType,
    private val config: HeatmapConfig
) {
    private val w = (config.curW * config.scaleFactor).toInt()
    private val h = (config.curH * config.scaleFactor).toInt()
    private val sumGrid = FloatArray(w * h)
    private val weightGrid = FloatArray(w * h)

    suspend fun calculate(): HeatmapResult = withContext(Dispatchers.Default) {
        if (w <= 0 || h <= 0) return@withContext HeatmapResult(ImageBitmap(1, 1), 0f, 0f)
        accumulateHeat()
        return@withContext normalizeAndColorize()
    }

    private fun accumulateHeat() {
        val radiusPx = w * HEATMAP_RADIUS_MULTIPLIER
        val radiusSq = radiusPx * radiusPx
        val origRects = buildKeyRects(config.isRu, config.origW, config.origH)
        val curRects = buildKeyRects(config.isRu, config.curW, config.curH)

        for (sample in samples) {
            val key = sample.touchData.key.lowercase()
            val origRect = origRects[key] ?: continue
            val curRect = curRects[key] ?: continue

            val origInnerW = origRect.width - 2 * config.padX
            val origInnerH = origRect.height - 2 * config.padY
            val curInnerW = curRect.width - 2 * config.padX
            val curInnerH = curRect.height - 2 * config.padY

            val scaleX = if (origInnerW > 0) curInnerW / origInnerW else 1f
            val scaleY = if (origInnerH > 0) curInnerH / origInnerH else 1f

            val scaledTouchX = sample.touchData.touchX * scaleX
            val scaledTouchY = sample.touchData.touchY * scaleY

            val globalX = curRect.left + config.padX + scaledTouchX
            val globalY = curRect.top + config.padY + scaledTouchY

            val gridX = globalX * config.scaleFactor
            val gridY = globalY * config.scaleFactor

            val value = when (metricType) {
                HeatmapMetricType.FREQUENCY -> 1f
                HeatmapMetricType.DWELL_TIME -> sample.touchData.dwellTime.toFloat()
                HeatmapMetricType.PRESSURE -> sample.touchData.pressure
            }

            processHeatRegion(gridX, gridY, radiusPx, radiusSq, value)
        }
    }

    private fun processHeatRegion(
        gridX: Float,
        gridY: Float,
        radiusPx: Float,
        radiusSq: Float,
        value: Float
    ) {
        val startX = (gridX - radiusPx).toInt().coerceAtLeast(0)
        val endX = (gridX + radiusPx).toInt().coerceAtMost(w - 1)
        val startY = (gridY - radiusPx).toInt().coerceAtLeast(0)
        val endY = (gridY + radiusPx).toInt().coerceAtMost(h - 1)

        for (y in startY..endY) {
            for (x in startX..endX) {
                val dx = x - gridX
                val dy = y - gridY
                val distSq = dx * dx + dy * dy
                if (distSq <= radiusSq) {
                    val weight = exp(-distSq / (HEATMAP_GRID_SIGMA * radiusSq))
                    val idx = y * w + x
                    sumGrid[idx] += value * weight
                    weightGrid[idx] += weight
                }
            }
        }
    }

    private fun normalizeAndColorize(): HeatmapResult {
        var minVal = Float.MAX_VALUE
        var maxVal = Float.MIN_VALUE

        for (i in sumGrid.indices) {
            if (weightGrid[i] > HEATMAP_MIN_WEIGHT) {
                val v = if (metricType == HeatmapMetricType.FREQUENCY) {
                    sumGrid[i]
                } else {
                    sumGrid[i] / weightGrid[i]
                }
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

        return createBitmap(minVal, maxVal)
    }

    private fun createBitmap(minVal: Float, maxVal: Float): HeatmapResult {
        val pixels = IntArray(w * h)
        for (i in sumGrid.indices) {
            if (weightGrid[i] > HEATMAP_MIN_WEIGHT) {
                val v = sumGrid[i]
                val fraction = ((v - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                val hue = (1f - fraction) * HEATMAP_HUE_MAX
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
    private fun buildKeyRects(isRu: Boolean, cW: Float, cH: Float): Map<String, Rect> {
        val rects = mutableMapOf<String, Rect>()
        val rowH = cH / HEATMAP_ROWS_DIVISOR

        // Row 0
        val numKeys = KeyboardLayouts.NUM_ROW_1
        val numW = cW / numKeys.size
        var curX = 0f
        numKeys.forEach { k -> rects[k] = Rect(curX, 0f, curX + numW, rowH); curX += numW }

        // Row 1
        val r1 = if (isRu) KeyboardLayouts.RU_ROW_1 else KeyboardLayouts.EN_ROW_1
        val r1W = cW / r1.size
        curX = 0f
        r1.forEach { k -> rects[k] = Rect(curX, rowH, curX + r1W, rowH * 2); curX += r1W }

        // Row 2
        val r2 = if (isRu) KeyboardLayouts.RU_ROW_2 else KeyboardLayouts.EN_ROW_2
        val pad = if (isRu) 0f else KeyboardConstants.ROW_PADDING
        val totalW2 = pad * 2 + r2.size * 1f
        val r2W = cW / totalW2
        curX = pad * r2W
        r2.forEach { k -> rects[k] = Rect(curX, rowH * 2, curX + r2W, rowH * 3); curX += r2W }

        // Row 3
        val r3 = if (isRu) KeyboardLayouts.RU_ROW_3 else KeyboardLayouts.EN_ROW_3
        val sw = if (isRu) 1f else 2f
        val totalW3 = sw * 2 + r3.size * 1f
        val r3W = cW / totalW3
        curX = 0f
        rects["⇧"] = Rect(curX, rowH * 3, curX + sw * r3W, rowH * 4); curX += sw * r3W
        r3.forEach { k -> rects[k] = Rect(curX, rowH * 3, curX + r3W, rowH * 4); curX += r3W }
        rects["⌫"] = Rect(curX, rowH * 3, curX + sw * r3W, rowH * 4)

        // Row 4
        val totalW4 = 1f + 1f + 1f + HEATMAP_SPACE_MULTIPLIER + 1f + HEATMAP_ENTER_MULTIPLIER
        val r4W = cW / totalW4
        curX = 0f
        rects["123"] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
        rects[","] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
        rects[if (isRu) "ru" else "en"] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
        rects[""] = Rect(
            curX,
            rowH * 4,
            curX + HEATMAP_SPACE_MULTIPLIER * r4W,
            rowH * 5
        ); curX += HEATMAP_SPACE_MULTIPLIER * r4W
        rects["."] = Rect(curX, rowH * 4, curX + r4W, rowH * 5); curX += r4W
        rects["↵"] = Rect(curX, rowH * 4, curX + HEATMAP_ENTER_MULTIPLIER * r4W, rowH * 5)

        return rects
    }
}
