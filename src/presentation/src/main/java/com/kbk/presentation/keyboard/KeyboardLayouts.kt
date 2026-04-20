package com.kbk.presentation.keyboard

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.dp

object KeyboardLayouts {
    val RU_ROW_1 = listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х")
    val RU_ROW_2 = listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э")
    val RU_ROW_3 = listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю")

    val EN_ROW_1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val EN_ROW_2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val EN_ROW_3 = listOf("z", "x", "c", "v", "b", "n", "m")

    val NUM_ROW_1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val NUM_ROW_2 = listOf("@", "#", "₽", "&", "_", "-", "(", ")", "=", "%")
    val NUM_ROW_3 = listOf("\"", "*", "'", ":", "/", "!", "?", "+")

    val SYM_ROW_1 = listOf("$", "€", "¥", "¢", "©", "®", "™", "~", "¿")
    val SYM_ROW_2 = listOf("⇥", "[", "]", "{", "}", "<", ">", "^", "¡")
    val SYM_ROW_3 = listOf("`", ";", "÷", "\\", "|", "¦", "¬")
    val SYM_ROW_4 = listOf("×", "§", "¶", "°")
}

object KeyboardConstants {
    const val KEY_WEIGHT_NORMAL = 1f
    const val KEY_WEIGHT_LARGE = 2f
    const val KEY_WEIGHT_SPACE = 5f
    const val KEY_WEIGHT_SYMBOLS_SPACE = 3f
    const val ROW_PADDING = 0.5f

    val KEYBOARD_HEIGHT = 280.dp
    val KEYBOARD_PADDING_HORIZONTAL = 2.dp
    val KEYBOARD_PADDING_VERTICAL = 4.dp
    val DOUBLE_KEYBOARD_PADDING_HORIZONTAL = 4.dp
    val DOUBLE_KEYBOARD_PADDING_VERTICAL = 8.dp
    val ROUNDED_CORNER_SHAPE = 6.dp
}

@Composable
fun ColumnScope.LettersLayout(
    language: KeyboardLanguage,
    isShifted: Boolean,
    onKeyEvent: ((key: String, down: PointerInputChange, up: PointerInputChange) -> Unit)?,
    isBackdrop: Boolean = false,
    onAction: (KeyboardAction) -> Unit
) {
    val isRu = language == KeyboardLanguage.RU
    val row1 = if (isRu) KeyboardLayouts.RU_ROW_1 else KeyboardLayouts.EN_ROW_1
    val row2 = if (isRu) KeyboardLayouts.RU_ROW_2 else KeyboardLayouts.EN_ROW_2
    val row3 = if (isRu) KeyboardLayouts.RU_ROW_3 else KeyboardLayouts.EN_ROW_3
    val padding = if (isRu) ZERO_PADDING else KeyboardConstants.ROW_PADDING
    val sideWeight =
        if (isRu) KeyboardConstants.KEY_WEIGHT_NORMAL else KeyboardConstants.KEY_WEIGHT_LARGE

    KeyboardRow(
        KeyboardRowParams(
            KeyboardLayouts.NUM_ROW_1,
            isBackdrop = isBackdrop
        ),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(
            row1,
            isShifted = isShifted,
            isBackdrop = isBackdrop
        ),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(row2, padding, isShifted, isBackdrop),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    LettersThirdRow(
        LettersThirdRowParams(row3, isShifted, isBackdrop, sideWeight),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    BottomRow(language, onKeyEvent = onKeyEvent, isBackdrop = isBackdrop, onAction = onAction)
}

@Composable
internal fun ColumnScope.LettersThirdRow(
    params: LettersThirdRowParams,
    onKeyEvent: ((key: String, down: PointerInputChange, up: PointerInputChange) -> Unit)?,
    onAction: (KeyboardAction) -> Unit
) {
    Row(
        modifier = Modifier
            .weight(ROW_WEIGHT)
            .fillMaxWidth()
    ) {
        KeyboardKey(
            KeyboardKeyParams("⇧", KeyType.SHIFT, params.isBackdrop),
            Modifier.weight(params.sideWeight),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.Shift)
        }
        params.row3.forEach { key ->
            val text = if (params.isShifted) key.uppercase() else key
            KeyboardKey(
                KeyboardKeyParams(text, KeyType.NORMAL, params.isBackdrop),
                Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
                onKeyEvent = onKeyEvent
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            KeyboardKeyParams("⌫", KeyType.DELETE, params.isBackdrop),
            Modifier.weight(params.sideWeight),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
}

@Composable
internal fun ColumnScope.NumbersLayout(
    onKeyEvent: ((key: String, down: PointerInputChange, up: PointerInputChange) -> Unit)?,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.NUM_ROW_1),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.NUM_ROW_2),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    Row(
        modifier = Modifier
            .weight(ROW_WEIGHT)
            .fillMaxWidth()
    ) {
        KeyboardKey(
            KeyboardKeyParams("{&=", KeyType.LAYOUT_CHANGE),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.ChangeLayout(KeyboardLayoutType.SYMBOLS))
        }
        KeyboardLayouts.NUM_ROW_3.forEach { key ->
            KeyboardKey(
                KeyboardKeyParams(key, KeyType.NORMAL),
                Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
                onKeyEvent = onKeyEvent
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            KeyboardKeyParams("⌫", KeyType.DELETE),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
    BottomRow(
        language = null,
        layoutType = KeyboardLayoutType.NUMBERS,
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
}

@Composable
internal fun ColumnScope.SymbolsLayout(
    onKeyEvent: ((key: String, down: PointerInputChange, up: PointerInputChange) -> Unit)?,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.SYM_ROW_1),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.SYM_ROW_2),
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
    Row(
        modifier = Modifier
            .weight(ROW_WEIGHT)
            .fillMaxWidth()
    ) {
        KeyboardKey(
            KeyboardKeyParams("123", KeyType.LAYOUT_CHANGE),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.ChangeLayout(KeyboardLayoutType.NUMBERS))
        }
        KeyboardLayouts.SYM_ROW_3.forEach { key ->
            KeyboardKey(
                KeyboardKeyParams(key, KeyType.NORMAL),
                Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
                onKeyEvent = onKeyEvent
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            KeyboardKeyParams("⌫", KeyType.DELETE),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
    BottomRow(
        language = null,
        layoutType = KeyboardLayoutType.SYMBOLS,
        onKeyEvent = onKeyEvent,
        onAction = onAction
    )
}
