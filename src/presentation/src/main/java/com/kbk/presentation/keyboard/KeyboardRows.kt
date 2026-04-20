package com.kbk.presentation.keyboard

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange

@Composable
internal fun ColumnScope.KeyboardRow(
    params: KeyboardRowParams,
    onKeyEvent: ((String, PointerInputChange, PointerInputChange) -> Unit)?,
    onAction: (KeyboardAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(ROW_WEIGHT)
    ) {
        if (params.horizontalPadding > ZERO_PADDING) Spacer(modifier = Modifier.weight(params.horizontalPadding))
        params.keys.forEach { key ->
            val text = if (params.isShifted) key.uppercase() else key
            KeyboardKey(
                params = KeyboardKeyParams(
                    text,
                    KeyType.NORMAL,
                    params.isBackdrop
                ),
                modifier = Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
                onKeyEvent = onKeyEvent
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        if (params.horizontalPadding > ZERO_PADDING) Spacer(modifier = Modifier.weight(params.horizontalPadding))
    }
}

@Composable
internal fun ColumnScope.BottomRow(
    language: KeyboardLanguage?,
    layoutType: KeyboardLayoutType = KeyboardLayoutType.LETTERS,
    onKeyEvent: ((String, PointerInputChange, PointerInputChange) -> Unit)?,
    isBackdrop: Boolean = false,
    onAction: (KeyboardAction) -> Unit
) {
    Row(
        modifier = Modifier
            .weight(ROW_WEIGHT)
            .fillMaxWidth()
    ) {
        val switchText = if (layoutType == KeyboardLayoutType.LETTERS) "123" else "abc"
        val nextLayout =
            if (layoutType == KeyboardLayoutType.LETTERS) KeyboardLayoutType.NUMBERS else KeyboardLayoutType.LETTERS

        KeyboardKey(
            KeyboardKeyParams(switchText, KeyType.LAYOUT_CHANGE, isBackdrop),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.ChangeLayout(nextLayout))
        }

        when (layoutType) {
            KeyboardLayoutType.LETTERS -> BottomRowLetters(
                language,
                onKeyEvent,
                isBackdrop,
                onAction
            )

            KeyboardLayoutType.NUMBERS -> BottomRowNumbers(onKeyEvent, onAction)
            KeyboardLayoutType.SYMBOLS -> BottomRowSymbols(onKeyEvent, onAction)
        }
    }
}

@Composable
private fun RowScope.BottomRowLetters(
    language: KeyboardLanguage?,
    onKeyEvent: ((String, PointerInputChange, PointerInputChange) -> Unit)?,
    isBackdrop: Boolean,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardKey(
        KeyboardKeyParams(",", KeyType.PUNCTUATION_MARKS, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.CommitText(",")) }
    val langText = if (language == KeyboardLanguage.RU) "RU" else "EN"
    KeyboardKey(
        KeyboardKeyParams(langText, KeyType.LANGUAGE_CHANGE, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.SwitchLanguage) }
    KeyboardKey(
        KeyboardKeyParams(" ", KeyType.SPACE, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SPACE),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.Space) }
    KeyboardKey(
        KeyboardKeyParams(".", KeyType.PUNCTUATION_MARKS, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.CommitText(".")) }
    KeyboardKey(
        KeyboardKeyParams("↵", KeyType.ENTER, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_LARGE),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.Enter) }
}

@Composable
private fun RowScope.BottomRowNumbers(
    onKeyEvent: ((String, PointerInputChange, PointerInputChange) -> Unit)?,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardKey(
        KeyboardKeyParams(",", KeyType.PUNCTUATION_MARKS),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.CommitText(",")) }
    KeyboardKey(
        KeyboardKeyParams(" ", KeyType.SPACE),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SPACE),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.Space) }
    KeyboardKey(
        KeyboardKeyParams(".", KeyType.PUNCTUATION_MARKS),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.CommitText(".")) }
    KeyboardKey(
        KeyboardKeyParams("↵", KeyType.ENTER),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_LARGE),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.Enter) }
}

@Composable
private fun RowScope.BottomRowSymbols(
    onKeyEvent: ((String, PointerInputChange, PointerInputChange) -> Unit)?,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardKey(
        KeyboardKeyParams(" ", KeyType.SPACE),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SYMBOLS_SPACE),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.Space) }
    KeyboardLayouts.SYM_ROW_4.forEach { key ->
        KeyboardKey(
            KeyboardKeyParams(key, KeyType.NORMAL),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            onKeyEvent = onKeyEvent
        ) {
            onAction(KeyboardAction.CommitText(key))
        }
    }
    KeyboardKey(
        KeyboardKeyParams("↵", KeyType.ENTER),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        onKeyEvent = onKeyEvent
    ) { onAction(KeyboardAction.Enter) }
}
