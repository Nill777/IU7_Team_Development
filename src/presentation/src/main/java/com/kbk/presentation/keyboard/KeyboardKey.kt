package com.kbk.presentation.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_HORIZONTAL
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_VERTICAL
import com.kbk.presentation.keyboard.KeyboardConstants.ROUNDED_CORNER_SHAPE

enum class KeyType {
    NORMAL, SHIFT, ENTER, SPACE, DELETE, LAYOUT_CHANGE, LANGUAGE_CHANGE, PUNCTUATION_MARKS
}

private val KeyType.fontSize: TextUnit
    get() = when (this) {
        KeyType.NORMAL -> 20.sp
        KeyType.SHIFT -> 35.sp
        KeyType.ENTER -> 35.sp
        KeyType.SPACE -> 35.sp
        KeyType.LAYOUT_CHANGE -> 18.sp
        KeyType.LANGUAGE_CHANGE -> 20.sp
        KeyType.DELETE -> 22.sp
        KeyType.PUNCTUATION_MARKS -> 20.sp
    }

data class KeyboardKeyParams(
    val text: String,
    val type: KeyType = KeyType.NORMAL,
    val viewModel: KeyboardViewModel? = null,
    val isBackdrop: Boolean = false
)

@Composable
fun KeyboardKey(
    params: KeyboardKeyParams,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor = if (params.type != KeyType.NORMAL) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (params.isBackdrop) Color.Black else Color.Transparent
    val borderWidth = if (params.isBackdrop) 1.dp else 0.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (!params.isBackdrop && params.viewModel != null) {
                    Modifier
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    // pass = Initial перехват данных до onClick
                                    val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)
                                    val upEvent =
                                        waitForUpOrCancellation(pass = PointerEventPass.Initial)

                                    if (upEvent != null) {
                                        params.viewModel.onKeyEvent(
                                            params.text,
                                            downEvent,
                                            upEvent
                                        )
                                    }
                                }
                            }
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        KeyboardKeyContent(
            text = params.text,
            type = params.type,
            bgColor = bgColor,
            borderColor = borderColor,
            borderWidth = borderWidth,
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun KeyboardKeyContent(
    text: String,
    type: KeyType,
    bgColor: Color,
    borderColor: Color,
    borderWidth: androidx.compose.ui.unit.Dp,
    interactionSource: MutableInteractionSource
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = KEYBOARD_PADDING_HORIZONTAL,
                vertical = KEYBOARD_PADDING_VERTICAL
            )
            .clip(RoundedCornerShape(ROUNDED_CORNER_SHAPE))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(ROUNDED_CORNER_SHAPE))
            .indication(interactionSource, ripple()),
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
