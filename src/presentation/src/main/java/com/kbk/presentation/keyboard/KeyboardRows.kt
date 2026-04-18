package com.kbk.presentation.keyboard

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ColumnScope.KeyboardRow(
    params: KeyboardRowParams,
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
                    params.viewModel,
                    params.isBackdrop
                ),
                modifier = Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
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
    viewModel: KeyboardViewModel? = null,
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
            KeyboardKeyParams(switchText, KeyType.LAYOUT_CHANGE, viewModel, isBackdrop),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
        ) {
            onAction(KeyboardAction.ChangeLayout(nextLayout))
        }

        when (layoutType) {
            KeyboardLayoutType.LETTERS -> BottomRowLetters(
                language,
                viewModel,
                isBackdrop,
                onAction
            )

            KeyboardLayoutType.NUMBERS -> BottomRowNumbers(viewModel, onAction)
            KeyboardLayoutType.SYMBOLS -> BottomRowSymbols(viewModel, onAction)
        }
    }
}

@Composable
private fun RowScope.BottomRowLetters(
    language: KeyboardLanguage?,
    viewModel: KeyboardViewModel?,
    isBackdrop: Boolean,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardKey(
        KeyboardKeyParams(",", KeyType.PUNCTUATION_MARKS, viewModel, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
    ) { onAction(KeyboardAction.CommitText(",")) }
    val langText = if (language == KeyboardLanguage.RU) "RU" else "EN"
    KeyboardKey(
        KeyboardKeyParams(langText, KeyType.LANGUAGE_CHANGE, viewModel, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
    ) { onAction(KeyboardAction.SwitchLanguage) }
    KeyboardKey(
        KeyboardKeyParams("", KeyType.SPACE, viewModel, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SPACE)
    ) { onAction(KeyboardAction.Space) }
    KeyboardKey(
        KeyboardKeyParams(".", KeyType.PUNCTUATION_MARKS, viewModel, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
    ) { onAction(KeyboardAction.CommitText(".")) }
    KeyboardKey(
        KeyboardKeyParams("↵", KeyType.ENTER, viewModel, isBackdrop),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_LARGE)
    ) { onAction(KeyboardAction.Enter) }
}

@Composable
private fun RowScope.BottomRowNumbers(
    viewModel: KeyboardViewModel?,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardKey(
        KeyboardKeyParams(",", KeyType.PUNCTUATION_MARKS, viewModel),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
    ) { onAction(KeyboardAction.CommitText(",")) }
    KeyboardKey(
        KeyboardKeyParams("", KeyType.SPACE, viewModel),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SPACE)
    ) { onAction(KeyboardAction.Space) }
    KeyboardKey(
        KeyboardKeyParams(".", KeyType.PUNCTUATION_MARKS, viewModel),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
    ) { onAction(KeyboardAction.CommitText(".")) }
    KeyboardKey(
        KeyboardKeyParams("↵", KeyType.ENTER, viewModel),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_LARGE)
    ) { onAction(KeyboardAction.Enter) }
}

@Composable
private fun RowScope.BottomRowSymbols(
    viewModel: KeyboardViewModel?,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardKey(
        KeyboardKeyParams("", KeyType.SPACE, viewModel),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SYMBOLS_SPACE)
    ) { onAction(KeyboardAction.Space) }
    KeyboardLayouts.SYM_ROW_4.forEach { key ->
        KeyboardKey(
            KeyboardKeyParams(key, KeyType.NORMAL, viewModel),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
        ) {
            onAction(KeyboardAction.CommitText(key))
        }
    }
    KeyboardKey(
        KeyboardKeyParams("↵", KeyType.ENTER, viewModel),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
    ) { onAction(KeyboardAction.Enter) }
}
