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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_HORIZONTAL
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_VERTICAL
import com.kbk.presentation.keyboard.KeyboardConstants.ROUNDED_CORNER_SHAPE

private const val FONT_SIZE_NORMAL = 20
private const val FONT_SIZE_LARGE = 35
private const val FONT_SIZE_SMALL = 18
private const val FONT_SIZE_MEDIUM = 22
private const val BORDER_WIDTH_BACKDROP = 1

enum class KeyType {
    NORMAL, SHIFT, ENTER, SPACE, DELETE, LAYOUT_CHANGE, LANGUAGE_CHANGE, PUNCTUATION_MARKS
}

private val KeyType.fontSize: TextUnit
    get() = when (this) {
        KeyType.NORMAL -> FONT_SIZE_NORMAL.sp
        KeyType.SHIFT -> FONT_SIZE_LARGE.sp
        KeyType.ENTER -> FONT_SIZE_LARGE.sp
        KeyType.SPACE -> FONT_SIZE_LARGE.sp
        KeyType.LAYOUT_CHANGE -> FONT_SIZE_SMALL.sp
        KeyType.LANGUAGE_CHANGE -> FONT_SIZE_NORMAL.sp
        KeyType.DELETE -> FONT_SIZE_MEDIUM.sp
        KeyType.PUNCTUATION_MARKS -> FONT_SIZE_NORMAL.sp
    }

data class KeyboardKeyParams(
    val text: String,
    val type: KeyType = KeyType.NORMAL,
    val isBackdrop: Boolean = false
)

data class KeyboardKeyStyles(
    val bgColor: Color,
    val borderColor: Color,
    val borderWidth: Dp
)

@Composable
fun KeyboardKey(
    params: KeyboardKeyParams,
    modifier: Modifier = Modifier,
    onKeyEvent: ((String, PointerInputChange, PointerInputChange) -> Unit)?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor = if (params.type != KeyType.NORMAL) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (params.isBackdrop) Color.Black else Color.Transparent
    val borderWidth = if (params.isBackdrop) BORDER_WIDTH_BACKDROP.dp else 0.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .applyKeyboardInteraction(params, interactionSource, onKeyEvent, onClick),
        contentAlignment = Alignment.Center
    ) {
        KeyboardKeyContent(
            text = params.text,
            type = params.type,
            styles = KeyboardKeyStyles(bgColor, borderColor, borderWidth),
            interactionSource = interactionSource
        )
    }
}

private fun Modifier.applyKeyboardInteraction(
    params: KeyboardKeyParams,
    interactionSource: MutableInteractionSource,
    onKeyEvent: ((String, PointerInputChange, PointerInputChange) -> Unit)?,
    onClick: () -> Unit
): Modifier {
    return if (!params.isBackdrop && onKeyEvent != null) {
        this
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // pass = Initial перехват данных до onClick
                        val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)

                        if (upEvent != null) {
                            onKeyEvent(params.text, downEvent, upEvent)
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
        this
    }
}

@Composable
private fun KeyboardKeyContent(
    text: String,
    type: KeyType,
    styles: KeyboardKeyStyles,
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
            .background(styles.bgColor)
            .border(
                styles.borderWidth,
                styles.borderColor,
                RoundedCornerShape(ROUNDED_CORNER_SHAPE)
            )
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
