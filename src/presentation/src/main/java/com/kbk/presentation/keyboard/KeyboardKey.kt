package com.kbk.presentation.keyboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@Composable
fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier,
    type: KeyType = KeyType.NORMAL,
    viewModel: KeyboardViewModel,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor = if (type != KeyType.NORMAL) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // pass = Initial перехват данных до onClick
                        val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)

                        if (upEvent != null) {
                            viewModel.onKeyEvent(
                                text,
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
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
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
}
