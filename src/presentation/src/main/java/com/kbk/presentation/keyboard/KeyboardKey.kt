package com.kbk.presentation.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.TouchData

private val KeyHorizontalPadding = 2.dp
private val KeyVerticalPadding = 4.dp
private val KeyCornerRadius = 6.dp
private val NormalTextSize = 20.sp
private val SpecialTextSize = 22.sp
private val ShiftTextSize = 35.sp
private val EnterTextSize = 35.sp
private val SpaceTextSize = 35.sp

private val ChangeLayoutTextSize = 18.sp
private val ChangeLanguageTextSize = 20.sp
private val PunctuationMarks = 20.sp

enum class KeyType {
    NORMAL, SHIFT, ENTER, SPACE, DELETE, LAYOUT_CHANGE, LANGUAGE_CHANGE, PUNCTUATION_MARKS
}

private val KeyType.fontSize: TextUnit
    get() = when (this) {
        KeyType.NORMAL -> NormalTextSize
        KeyType.SHIFT -> ShiftTextSize
        KeyType.ENTER -> EnterTextSize
        KeyType.SPACE -> SpaceTextSize
        KeyType.LAYOUT_CHANGE -> ChangeLayoutTextSize
        KeyType.LANGUAGE_CHANGE -> ChangeLanguageTextSize
        KeyType.DELETE -> SpecialTextSize
        KeyType.PUNCTUATION_MARKS -> PunctuationMarks
    }

@Composable
fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier,
    type: KeyType = KeyType.NORMAL,
    viewModel: KeyboardViewModel? = null,
    onClick: () -> Unit
) {
    val bgColor = if (type != KeyType.NORMAL) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .padding(horizontal = KeyHorizontalPadding, vertical = KeyVerticalPadding)
            .fillMaxSize()
            .clip(RoundedCornerShape(KeyCornerRadius))
            .background(bgColor)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // pass = Initial перехват данных до onClick
                        val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)

                        val downTime = downEvent.uptimeMillis
                        val p = downEvent.pressure
                        val initialX = downEvent.position.x
                        val initialY = downEvent.position.y

                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)

                        if (upEvent != null && viewModel != null) {
                            val upTime = upEvent.uptimeMillis

                            // микроскольжение
                            val swipeVectorX = upEvent.position.x - initialX
                            val swipeVectorY = upEvent.position.y - initialY

                            val dwellTime = upTime - downTime
                            val flightTime = viewModel.calculateFlightTime(downTime)
                            viewModel.updateLastUpTime(upTime)

                            val touchData = TouchData(
                                key = text,
                                dwellTime = dwellTime,
                                flightTime = flightTime,
                                pressure = p,
                                touchX = initialX,
                                touchY = initialY,
                                swipeVectorX = swipeVectorX,
                                swipeVectorY = swipeVectorY
                            )

                            viewModel.onKeyTouchRecorded(touchData)
                        }
                    }
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = type.fontSize,
            maxLines = 1
        )
    }
}
