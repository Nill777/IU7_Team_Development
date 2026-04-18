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
private const val HSV_ALPHA_MAX = 255f
private const val HEATMAP_RADIUS_MULTIPLIER = 0.06f
private const val HEATMAP_ROWS_DIVISOR = 5f
private const val HEATMAP_SPACE_MULTIPLIER = 5f
private const val HEATMAP_ENTER_MULTIPLIER = 2f
private const val SCALE_FACTOR = 0.10f
private const val HEATMAP_IMAGE_ALPHA = 0.7f
private const val GRADIENT_HEIGHT_VAL = 16

private const val ROW_MUL_0 = 0f
private const val ROW_MUL_1 = 1f
private const val ROW_MUL_2 = 2f
private const val ROW_MUL_3 = 3f
private const val ROW_MUL_4 = 4f
private const val ROW_MUL_5 = 5f
private const val KEY_WEIGHT_NORMAL_HEAT = 1f
private const val KEY_WEIGHT_RU_SHIFT = 1f
private const val KEY_WEIGHT_EN_SHIFT = 2f
private const val TWO_PADS = 2f
private const val HEATMAP_PAD_X = 2f
private const val HEATMAP_PAD_Y = 4f

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

data class HeatmapCanvasParams(
    val isRu: Boolean,
    val samples: List<BiometricSample>,
    val metricType: HeatmapMetricType,
    val origWidthPx: Float,
    val origHeightPx: Float,
    val padXDp: Float,
    val padYDp: Float
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
    val padXDp = HEATMAP_PAD_X * density
    val padYDp = HEATMAP_PAD_Y * density

    val configuration = LocalConfiguration.current
    val origWidthPx =
        with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() - DOUBLE_KEYBOARD_PADDING_HORIZONTAL.toPx() }
    val origHeightPx =
        with(LocalDensity.current) { KEYBOARD_HEIGHT.toPx() - DOUBLE_KEYBOARD_PADDING_VERTICAL.toPx() }

    val canvasParams = HeatmapCanvasParams(
        isRu = isRu,
        samples = samples,
        metricType = metricType,
        origWidthPx = origWidthPx,
        origHeightPx = origHeightPx,
        padXDp = padXDp,
        padYDp = padYDp
    )

    HeatmapCanvasArea(
        params = canvasParams,
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
    params: HeatmapCanvasParams,
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
                    language = if (params.isRu) KeyboardLanguage.RU else KeyboardLanguage.EN,
                    isShifted = false,
                    isBackdrop = true,
                    onAction = {}
                )
            }
        }

        LaunchedEffect(params.samples, params.metricType, params.isRu, widthPx, heightPx) {
            if (widthPx > 0 && heightPx > 0 && params.samples.isNotEmpty()) {
                val config = HeatmapConfig(
                    params.isRu,
                    params.origWidthPx,
                    params.origHeightPx,
                    widthPx,
                    heightPx,
                    SCALE_FACTOR,
                    params.padXDp,
                    params.padYDp
                )
                val calculator = HeatmapCalculator(params.samples, params.metricType, config)
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
            processSampleHeat(sample, origRects, curRects, radiusPx, radiusSq)
        }
    }

    private fun processSampleHeat(
        sample: BiometricSample,
        origRects: Map<String, Rect>,
        curRects: Map<String, Rect>,
        radiusPx: Float,
        radiusSq: Float
    ) {
        val key = sample.touchData.key.lowercase()
        val origRect = origRects[key] ?: return
        val curRect = curRects[key] ?: return

        val origInnerW = origRect.width - TWO_PADS * config.padX
        val origInnerH = origRect.height - TWO_PADS * config.padY
        val curInnerW = curRect.width - TWO_PADS * config.padX
        val curInnerH = curRect.height - TWO_PADS * config.padY

        val scaleX = if (origInnerW > 0) curInnerW / origInnerW else 1f
        val scaleY = if (origInnerH > 0) curInnerH / origInnerH else 1f

        val scaledTouchX = sample.touchData.touchX * scaleX
        val scaledTouchY = sample.touchData.touchY * scaleY

        val globalX = curRect.left + config.padX + scaledTouchX
        val globalY = curRect.top + config.padY + scaledTouchY

        val gridX = globalX * config.scaleFactor
        val gridY = globalY * config.scaleFactor

        val value = getMetricValue(sample)

        processHeatRegion(gridX, gridY, radiusPx, radiusSq, value)
    }

    private fun getMetricValue(sample: BiometricSample): Float {
        return when (metricType) {
            HeatmapMetricType.FREQUENCY -> 1f
            HeatmapMetricType.DWELL_TIME -> sample.touchData.dwellTime.toFloat()
            HeatmapMetricType.PRESSURE -> sample.touchData.pressure
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
        val (minVal, maxVal) = normalizeGridAndFindExtremes()
        return createBitmap(minVal, maxVal)
    }

    private fun normalizeGridAndFindExtremes(): Pair<Float, Float> {
        var minVal = Float.MAX_VALUE
        var maxVal = Float.MIN_VALUE
        val isFreq = metricType == HeatmapMetricType.FREQUENCY
        for (i in sumGrid.indices) {
            val v = normalizeCell(i, isFreq)
            if (weightGrid[i] > HEATMAP_MIN_WEIGHT) {
                minVal = minOf(minVal, v)
                maxVal = maxOf(maxVal, v)
            }
        }
        minVal = if (minVal == Float.MAX_VALUE) 0f else minVal
        maxVal = if (maxVal == Float.MIN_VALUE) 1f else maxVal
        maxVal = if (minVal == maxVal) minVal + 1f else maxVal

        return Pair(minVal, maxVal)
    }

    private fun normalizeCell(index: Int, isFreq: Boolean): Float {
        val weight = weightGrid[index]
        if (weight <= HEATMAP_MIN_WEIGHT) {
            sumGrid[index] = 0f
            return 0f
        }
        val v = if (isFreq) sumGrid[index] else sumGrid[index] / weight
        sumGrid[index] = v
        return v
    }

    private fun createBitmap(minVal: Float, maxVal: Float): HeatmapResult {
        val pixels = IntArray(w * h)
        for (i in sumGrid.indices) {
            if (weightGrid[i] > HEATMAP_MIN_WEIGHT) {
                val v = sumGrid[i]
                val fraction = ((v - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                val hue = (1f - fraction) * HEATMAP_HUE_MAX
                val alpha = (weightGrid[i] * HSV_ALPHA_MAX).toInt().coerceAtMost(HEATMAP_ALPHA_MAX)
                pixels[i] = android.graphics.Color.HSVToColor(alpha, floatArrayOf(hue, 1f, 1f))
            } else {
                pixels[i] = android.graphics.Color.TRANSPARENT
            }
        }
        val bitmap = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        return HeatmapResult(bitmap.asImageBitmap(), minVal, maxVal)
    }
}

// сетка клавиш (повторяет LettersLayout)
private fun buildKeyRects(isRu: Boolean, cW: Float, cH: Float): Map<String, Rect> {
    val rects = mutableMapOf<String, Rect>()
    val rowH = cH / HEATMAP_ROWS_DIVISOR

    buildRow0(cW, rowH, rects)
    buildRow1(isRu, cW, rowH, rects)
    buildRow2(isRu, cW, rowH, rects)
    buildRow3(isRu, cW, rowH, rects)
    buildRow4(isRu, cW, rowH, rects)

    return rects
}

private fun buildRow0(cW: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    val numKeys = KeyboardLayouts.NUM_ROW_1
    val numW = cW / numKeys.size
    var curX = 0f
    numKeys.forEach { k ->
        rects[k] = Rect(curX, rowH * ROW_MUL_0, curX + numW, rowH * ROW_MUL_1)
        curX += numW
    }
}

private fun buildRow1(isRu: Boolean, cW: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    val r1 = if (isRu) KeyboardLayouts.RU_ROW_1 else KeyboardLayouts.EN_ROW_1
    val r1W = cW / r1.size
    var curX = 0f
    r1.forEach { k ->
        rects[k] = Rect(curX, rowH * ROW_MUL_1, curX + r1W, rowH * ROW_MUL_2)
        curX += r1W
    }
}

private fun buildRow2(isRu: Boolean, cW: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    val r2 = if (isRu) KeyboardLayouts.RU_ROW_2 else KeyboardLayouts.EN_ROW_2
    val pad = if (isRu) 0f else KeyboardConstants.ROW_PADDING
    val totalW2 = pad * TWO_PADS + r2.size * KEY_WEIGHT_NORMAL_HEAT
    val r2W = cW / totalW2
    var curX = pad * r2W
    r2.forEach { k ->
        rects[k] = Rect(curX, rowH * ROW_MUL_2, curX + r2W, rowH * ROW_MUL_3)
        curX += r2W
    }
}

private fun buildRow3(isRu: Boolean, cW: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    val r3 = if (isRu) KeyboardLayouts.RU_ROW_3 else KeyboardLayouts.EN_ROW_3
    val sw = if (isRu) KEY_WEIGHT_RU_SHIFT else KEY_WEIGHT_EN_SHIFT
    val totalW3 = sw * TWO_PADS + r3.size * KEY_WEIGHT_NORMAL_HEAT
    val r3W = cW / totalW3
    var curX = 0f

    rects["⇧"] = Rect(curX, rowH * ROW_MUL_3, curX + sw * r3W, rowH * ROW_MUL_4)
    curX += sw * r3W

    r3.forEach { k ->
        rects[k] = Rect(curX, rowH * ROW_MUL_3, curX + r3W, rowH * ROW_MUL_4)
        curX += r3W
    }

    rects["⌫"] = Rect(curX, rowH * ROW_MUL_3, curX + sw * r3W, rowH * ROW_MUL_4)
}

private fun buildRow4(isRu: Boolean, cW: Float, rowH: Float, rects: MutableMap<String, Rect>) {
    val totalW4 = KEY_WEIGHT_NORMAL_HEAT + KEY_WEIGHT_NORMAL_HEAT + KEY_WEIGHT_NORMAL_HEAT +
            HEATMAP_SPACE_MULTIPLIER + KEY_WEIGHT_NORMAL_HEAT + HEATMAP_ENTER_MULTIPLIER
    val r4W = cW / totalW4
    var curX = 0f

    rects["123"] = Rect(curX, rowH * ROW_MUL_4, curX + r4W, rowH * ROW_MUL_5)
    curX += r4W

    rects[","] = Rect(curX, rowH * ROW_MUL_4, curX + r4W, rowH * ROW_MUL_5)
    curX += r4W

    rects[if (isRu) "ru" else "en"] = Rect(curX, rowH * ROW_MUL_4, curX + r4W, rowH * ROW_MUL_5)
    curX += r4W

    rects[""] = Rect(
        curX,
        rowH * ROW_MUL_4,
        curX + HEATMAP_SPACE_MULTIPLIER * r4W,
        rowH * ROW_MUL_5
    )
    curX += HEATMAP_SPACE_MULTIPLIER * r4W

    rects["."] = Rect(curX, rowH * ROW_MUL_4, curX + r4W, rowH * ROW_MUL_5)
    curX += r4W

    rects["↵"] = Rect(
        curX,
        rowH * ROW_MUL_4,
        curX + HEATMAP_ENTER_MULTIPLIER * r4W,
        rowH * ROW_MUL_5
    )
}
