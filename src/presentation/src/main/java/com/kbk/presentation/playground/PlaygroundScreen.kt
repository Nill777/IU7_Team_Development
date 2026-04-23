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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.sdk.VerificationResult
import com.kbk.presentation.R

private const val COLOR_GREEN_BG = 0xFFE8F5E9
private const val COLOR_RED_BG = 0xFFFFEBEE
private const val COLOR_GREEN_TEXT = 0xFF2E7D32
private const val COLOR_RED_TEXT = 0xFFC62828

// Константы для анимации и лого (чтобы не было ошибок MagicNumber)
private const val LOGO_SIZE_DP = 56
private const val GLOW_ANIM_DURATION = 2000
private const val GLOW_MIN_ALPHA = 0.1f
private const val GLOW_MAX_ALPHA = 0.85f
private const val ORB_CENTER_Y_RATIO = 0.56f
private const val ORB_RADIUS_RATIO = 0.20f
private const val COLOR_CYAN_GLOW = 0xFF04FBFF
private const val COLOR_MODE_COLLECTION = 0xFFE65100

@Composable
fun PlaygroundScreen(viewModel: PlaygroundViewModel) {
    val attempts by viewModel.attempts.collectAsState()
    val message by viewModel.message.collectAsState()
    val isVerificationMode by viewModel.isVerificationMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PlaygroundTopBar(isVerificationMode)
        Spacer(Modifier.height(8.dp))

        ModeInfoText(isVerificationMode)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.trainModel() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Обучить эталонный профиль")
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            val msgColor =
                if (message.startsWith("❌")) Color.Red else MaterialTheme.colorScheme.onBackground
            Text(message, color = msgColor, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text("Лента верификаций:", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(attempts) { result ->
                ResultCard(result)
            }
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
        Spacer(Modifier.width(16.dp))
        Text("Playground (Тест Защиты)", style = MaterialTheme.typography.headlineMedium)
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
                val orbCenter = Offset(size.width / 2f, size.height * ORB_CENTER_Y_RATIO)
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
private fun ModeInfoText(isVerificationMode: Boolean) {
    val modeText = if (isVerificationMode) {
        "Режим: Активная защита\n(Непрерывная верификация и эволюция профиля)"
    } else {
        "Режим: Сбор данных\n(Ожидание накопления базы для первой тренировки)"
    }
    val modeColor =
        if (isVerificationMode) Color(COLOR_GREEN_TEXT) else Color(COLOR_MODE_COLLECTION)

    Text(
        text = modeText,
        color = modeColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ResultCard(result: VerificationResult) {
    val bgColor = if (result.isOwner) Color(COLOR_GREEN_BG) else Color(COLOR_RED_BG)
    val statusText = if (result.isOwner) "Владелец" else "ЗЛОУМЫШЛЕННИК!"
    val statusColor = if (result.isOwner) Color(COLOR_GREEN_TEXT) else Color(COLOR_RED_TEXT)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text("Аномальность: ${"%.2f".format(result.anomalyScore)}", fontSize = 14.sp)
            }
            Text(result.modelName, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
