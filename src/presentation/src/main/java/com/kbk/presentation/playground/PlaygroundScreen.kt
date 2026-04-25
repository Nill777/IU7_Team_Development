package com.kbk.presentation.playground

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import com.kbk.domain.models.sdk.VerificationResult
import com.kbk.presentation.R
import com.kbk.presentation.components.DetailedResultCard
import com.kbk.presentation.components.SettingsActions
import com.kbk.presentation.components.SettingsCard
import com.kbk.presentation.components.SettingsState

private const val PLAYGROUND_PADDING_VAL = 8
private const val PLAYGROUND_SPACING_VAL = 8
private const val CARD_ELEVATION_VAL = 4
private const val SPACER_TOPBAR_VAL = 16

private const val LOGO_SIZE_DP = 56
private const val GLOW_ANIM_DURATION = 2000
private const val GLOW_MIN_ALPHA = 0.1f
private const val GLOW_MAX_ALPHA = 0.85f
private const val ORB_CENTER_Y_RATIO = 0.56f
private const val ORB_RADIUS_RATIO = 0.20f
private const val HALF_DIVISOR = 2f

private const val COLOR_CYAN_GLOW = 0xFF04FBFF
private const val COLOR_MODE_COLLECTION = 0xFFFFA500
private const val COLOR_MODE_VERIFICATION = 0xFF00FF00
private const val COLOR_GREY_BG = 0xFF696969
private const val COLOR_GREY_ONBG = 0xFFA9A9A9
private const val COLOR_GREY_INDICATOR = 0xFF808080

private const val FIELD_MIN_HEIGHT = 56
private const val FIELD_MAX_HEIGHT = 150
private const val FIELD_MAX_LINES = 5

@Composable
fun PlaygroundScreen(viewModel: PlaygroundViewModel) {
    val isVerificationMode by viewModel.isVerificationMode.collectAsState()
    val totalCount by viewModel.totalSamplesCount.collectAsState()
    val trainedCount by viewModel.trainedSamplesCount.collectAsState()
    val batchSize by viewModel.batchSize.collectAsState()
    val testText by viewModel.testText.collectAsState()
    val latestResults by viewModel.latestResults.collectAsState()
    val timingThreshold by viewModel.timingThreshold.collectAsState()
    val spatialThreshold by viewModel.spatialThreshold.collectAsState()
    val motionThreshold by viewModel.motionThreshold.collectAsState()

    PlaygroundLifecycleHandler(viewModel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PLAYGROUND_PADDING_VAL.dp)
    ) {
        PlaygroundTopBar(isVerificationMode)
        Spacer(Modifier.height(PLAYGROUND_PADDING_VAL.dp))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(PLAYGROUND_SPACING_VAL.dp)
        ) {
            StatusCard(
                isVerificationMode = isVerificationMode,
                totalCount = totalCount,
                trainedCount = trainedCount,
                onTrainClick = { viewModel.trainModel() }
            )
            SettingsCard(
                title = "Настройки тестирования",
                state = SettingsState(
                    batchSize = batchSize,
                    timingThreshold = timingThreshold,
                    spatialThreshold = spatialThreshold,
                    motionThreshold = motionThreshold
                ),
                actions = SettingsActions(
                    onBatchSizeChange = viewModel::updateBatchSize,
                    onTimingChange = viewModel::updateTimingThreshold,
                    onSpatialChange = viewModel::updateSpatialThreshold,
                    onMotionChange = viewModel::updateMotionThreshold
                )
            )
            TestCard(
                isVerificationMode = isVerificationMode,
                testText = testText,
                latestResults = latestResults,
                onTextChange = { viewModel.updateTestText(it) },
                onFocusChange = { isFocused ->
                    viewModel.setTestInputFocus(isFocused)
                }
            )
        }
    }
}

@Composable
private fun PlaygroundLifecycleHandler(viewModel: PlaygroundViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.setTestInputFocus(false)
                focusManager.clearFocus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setTestInputFocus(false)
        }
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.collectPlaygroundSamples()
        }
    }
}

@Composable
private fun PlaygroundTopBar(isVerificationMode: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        BatLogoWithGlow(
            isGlowing = isVerificationMode,
            modifier = Modifier.size(LOGO_SIZE_DP.dp)
        )
        Spacer(Modifier.width(SPACER_TOPBAR_VAL.dp))
        Text("Тест защиты", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun BatLogoWithGlow(isGlowing: Boolean, modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Индикатор режима",
            modifier = Modifier.fillMaxSize()
        )

        if (isGlowing) {
            val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = GLOW_MIN_ALPHA,
                targetValue = GLOW_MAX_ALPHA,
                animationSpec = infiniteRepeatable(
                    animation = tween(GLOW_ANIM_DURATION, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val orbCenter = Offset(size.width / HALF_DIVISOR, size.height * ORB_CENTER_Y_RATIO)
                val radius = size.width * ORB_RADIUS_RATIO

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(COLOR_CYAN_GLOW).copy(alpha = pulseAlpha),
                            Color.Transparent
                        ),
                        center = orbCenter,
                        radius = radius
                    ),
                    radius = radius,
                    center = orbCenter
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    isVerificationMode: Boolean,
    totalCount: Int,
    trainedCount: Int,
    onTrainClick: () -> Unit
) {
    val fullText = buildAnnotatedString {
        append("Режим: ")
        withStyle(
            SpanStyle(
                color = if (isVerificationMode) Color(COLOR_MODE_VERIFICATION) else Color(
                    COLOR_MODE_COLLECTION
                ),
                fontWeight = FontWeight.Bold
            )
        ) {
            append(if (isVerificationMode) "Активная защита" else "Сбор данных")
        }
        append(
            if (isVerificationMode) {
                "\nОбучено на: $trainedCount записях\nВсего в базе: $totalCount записей"
            } else {
                "\nСобрано: $totalCount записей"
            }
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(CARD_ELEVATION_VAL.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(PLAYGROUND_PADDING_VAL.dp)) {
            Text(text = fullText, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(PLAYGROUND_SPACING_VAL.dp))
            Button(
                onClick = onTrainClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Рассчитать эталонный профиль")
            }
        }
    }
}

@Composable
private fun TestCard(
    isVerificationMode: Boolean,
    testText: String,
    latestResults: List<VerificationResult>,
    onTextChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(CARD_ELEVATION_VAL.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(PLAYGROUND_PADDING_VAL.dp)) {
            Text("Тестирование", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(PLAYGROUND_SPACING_VAL.dp))

            DetailedResultCard(latestResults)
            Spacer(Modifier.height(PLAYGROUND_SPACING_VAL.dp))

            OutlinedTextField(
                value = testText,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = FIELD_MIN_HEIGHT.dp, max = FIELD_MAX_HEIGHT.dp)
                    .onFocusChanged { state -> onFocusChange(state.isFocused) },
                maxLines = FIELD_MAX_LINES,
                enabled = isVerificationMode,
                label = { Text("Поле тестового ввода") },
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = Color(COLOR_GREY_ONBG),
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = Color(COLOR_GREY_BG),
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    focusedIndicatorColor = MaterialTheme.colorScheme.onSecondary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                    disabledIndicatorColor = Color(COLOR_GREY_INDICATOR),
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    disabledLabelColor = Color(COLOR_GREY_ONBG)
                )
            )
        }
    }
}
