package com.kbk.presentation.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_HEIGHT
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_HORIZONTAL
import com.kbk.presentation.keyboard.KeyboardConstants.KEYBOARD_PADDING_VERTICAL

data class KeyboardRowParams(
    val keys: List<String>,
    val horizontalPadding: Float = 0f,
    val isShifted: Boolean = false,
    val viewModel: KeyboardViewModel? = null,
    val isBackdrop: Boolean = false
)

@Composable
fun KeyboardScreen(
    viewModel: KeyboardViewModel,
    onAction: (KeyboardAction) -> Unit
) {
    var language by remember { mutableStateOf(KeyboardLanguage.RU) }
    var layoutType by remember { mutableStateOf(KeyboardLayoutType.LETTERS) }
    var isShifted by remember { mutableStateOf(false) }

    val actions = KeyboardActions(
        onLanguageChanged = { language = it },
        onLayoutChanged = { layoutType = it },
        onShiftChanged = { isShifted = it },
        onExternalAction = onAction
    )

    val internalOnAction: (KeyboardAction) -> Unit = { action ->
        processKeyboardAction(action, language, isShifted, actions)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .height(KEYBOARD_HEIGHT)
            .padding(horizontal = KEYBOARD_PADDING_HORIZONTAL, vertical = KEYBOARD_PADDING_VERTICAL)
    ) {
        when (layoutType) {
            KeyboardLayoutType.LETTERS -> LettersLayout(
                language,
                isShifted,
                viewModel,
                onAction = internalOnAction
            )

            KeyboardLayoutType.NUMBERS -> NumbersLayout(viewModel, internalOnAction)
            KeyboardLayoutType.SYMBOLS -> SymbolsLayout(viewModel, internalOnAction)
        }
    }
}

private fun processKeyboardAction(
    action: KeyboardAction,
    language: KeyboardLanguage,
    isShifted: Boolean,
    actions: KeyboardActions
) {
    when (action) {
        is KeyboardAction.SwitchLanguage -> {
            actions.onLanguageChanged(
                if (language == KeyboardLanguage.RU)
                    KeyboardLanguage.EN
                else
                    KeyboardLanguage.RU
            )
        }

        is KeyboardAction.ChangeLayout -> actions.onLayoutChanged(action.layout)
        is KeyboardAction.Shift -> actions.onShiftChanged(!isShifted)
        is KeyboardAction.CommitText -> {
            val textToCommit = if (isShifted) action.text.uppercase() else action.text
            actions.onExternalAction(KeyboardAction.CommitText(textToCommit))
            if (isShifted) actions.onShiftChanged(false)
        }

        else -> actions.onExternalAction(action)
    }
}

@Composable
fun ColumnScope.LettersLayout(
    language: KeyboardLanguage,
    isShifted: Boolean,
    viewModel: KeyboardViewModel? = null,
    isBackdrop: Boolean = false,
    onAction: (KeyboardAction) -> Unit
) {
    val isRu = language == KeyboardLanguage.RU
    val row1 = if (isRu) KeyboardLayouts.RU_ROW_1 else KeyboardLayouts.EN_ROW_1
    val row2 = if (isRu) KeyboardLayouts.RU_ROW_2 else KeyboardLayouts.EN_ROW_2
    val row3 = if (isRu) KeyboardLayouts.RU_ROW_3 else KeyboardLayouts.EN_ROW_3

    val padding = if (isRu) 0f else KeyboardConstants.ROW_PADDING
    val sideWeight =
        if (isRu) KeyboardConstants.KEY_WEIGHT_NORMAL else KeyboardConstants.KEY_WEIGHT_LARGE

    KeyboardRow(
        KeyboardRowParams(
            KeyboardLayouts.NUM_ROW_1,
            viewModel = viewModel,
            isBackdrop = isBackdrop
        ),
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(
            row1,
            isShifted = isShifted,
            viewModel = viewModel,
            isBackdrop = isBackdrop
        ),
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(row2, padding, isShifted, viewModel, isBackdrop),
        onAction = onAction
    )
    LettersThirdRow(row3, isShifted, viewModel, isBackdrop, sideWeight, onAction)
    BottomRow(language, viewModel = viewModel, isBackdrop = isBackdrop, onAction = onAction)
}

@Composable
private fun ColumnScope.LettersThirdRow(
    row3: List<String>,
    isShifted: Boolean,
    viewModel: KeyboardViewModel?,
    isBackdrop: Boolean,
    sideWeight: Float,
    onAction: (KeyboardAction) -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        KeyboardKey(
            KeyboardKeyParams("⇧", KeyType.SHIFT, viewModel, isBackdrop),
            Modifier.weight(sideWeight)
        ) {
            onAction(KeyboardAction.Shift)
        }
        row3.forEach { key ->
            val text = if (isShifted) key.uppercase() else key
            KeyboardKey(
                KeyboardKeyParams(text, KeyType.NORMAL, viewModel, isBackdrop),
                Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            KeyboardKeyParams("⌫", KeyType.DELETE, viewModel, isBackdrop),
            Modifier.weight(sideWeight)
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
}

@Composable
private fun ColumnScope.NumbersLayout(
    viewModel: KeyboardViewModel,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.NUM_ROW_1, viewModel = viewModel),
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.NUM_ROW_2, viewModel = viewModel),
        onAction = onAction
    )

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        KeyboardKey(
            KeyboardKeyParams("{&=", KeyType.LAYOUT_CHANGE, viewModel),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
        ) {
            onAction(KeyboardAction.ChangeLayout(KeyboardLayoutType.SYMBOLS))
        }
        KeyboardLayouts.NUM_ROW_3.forEach { key ->
            KeyboardKey(
                KeyboardKeyParams(key, KeyType.NORMAL, viewModel),
                Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            KeyboardKeyParams("⌫", KeyType.DELETE, viewModel),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
    BottomRow(
        language = null,
        viewModel = viewModel,
        layoutType = KeyboardLayoutType.NUMBERS,
        onAction = onAction
    )
}

@Composable
private fun ColumnScope.SymbolsLayout(
    viewModel: KeyboardViewModel,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.SYM_ROW_1, viewModel = viewModel),
        onAction = onAction
    )
    KeyboardRow(
        KeyboardRowParams(KeyboardLayouts.SYM_ROW_2, viewModel = viewModel),
        onAction = onAction
    )

    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        KeyboardKey(
            KeyboardKeyParams("123", KeyType.LAYOUT_CHANGE, viewModel),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
        ) {
            onAction(KeyboardAction.ChangeLayout(KeyboardLayoutType.NUMBERS))
        }
        KeyboardLayouts.SYM_ROW_3.forEach { key ->
            KeyboardKey(
                KeyboardKeyParams(key, KeyType.NORMAL, viewModel),
                Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            KeyboardKeyParams("⌫", KeyType.DELETE, viewModel),
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
    BottomRow(
        language = null,
        layoutType = KeyboardLayoutType.SYMBOLS,
        viewModel = viewModel,
        onAction = onAction
    )
}

@Composable
private fun ColumnScope.KeyboardRow(
    params: KeyboardRowParams,
    onAction: (KeyboardAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        if (params.horizontalPadding > 0f) Spacer(modifier = Modifier.weight(params.horizontalPadding))
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
        if (params.horizontalPadding > 0f)
            Spacer(modifier = Modifier.weight(params.horizontalPadding))
    }
}

@Composable
private fun ColumnScope.BottomRow(
    language: KeyboardLanguage?,
    layoutType: KeyboardLayoutType = KeyboardLayoutType.LETTERS,
    viewModel: KeyboardViewModel? = null,
    isBackdrop: Boolean = false,
    onAction: (KeyboardAction) -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    ) {
        val switchText = if (layoutType == KeyboardLayoutType.LETTERS) "123" else "abc"
        val nextLayout =
            if (layoutType == KeyboardLayoutType.LETTERS)
                KeyboardLayoutType.NUMBERS
            else
                KeyboardLayoutType.LETTERS

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

            KeyboardLayoutType.NUMBERS -> BottomRowNumbers(viewModel!!, onAction)
            KeyboardLayoutType.SYMBOLS -> BottomRowSymbols(viewModel!!, onAction)
        }
    }
}

@Composable
private fun RowScope.BottomRowLetters(
    language: KeyboardLanguage?,
    viewModel: KeyboardViewModel? = null,
    isBackdrop: Boolean = false,
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
    viewModel: KeyboardViewModel,
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
    viewModel: KeyboardViewModel,
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
            onAction(
                KeyboardAction.CommitText(key)
            )
        }
    }
    KeyboardKey(
        KeyboardKeyParams("↵", KeyType.ENTER, viewModel),
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
    ) { onAction(KeyboardAction.Enter) }
}
