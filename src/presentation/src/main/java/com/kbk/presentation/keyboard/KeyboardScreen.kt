package com.kbk.presentation.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val KeyboardHeight = 280.dp

@Composable
fun KeyboardScreen(onAction: (KeyboardAction) -> Unit) {
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
            .height(KeyboardHeight)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 2.dp, vertical = 4.dp)
    ) {
        when (layoutType) {
            KeyboardLayoutType.LETTERS -> LettersLayout(language, isShifted, internalOnAction)
            KeyboardLayoutType.NUMBERS -> NumbersLayout(internalOnAction)
            KeyboardLayoutType.SYMBOLS -> SymbolsLayout(internalOnAction)
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
private fun ColumnScope.LettersLayout(
    language: KeyboardLanguage,
    isShifted: Boolean,
    onAction: (KeyboardAction) -> Unit
) {
    val isRu = language == KeyboardLanguage.RU
    val row1 = if (isRu) KeyboardLayouts.RU_ROW_1 else KeyboardLayouts.EN_ROW_1
    val row2 = if (isRu) KeyboardLayouts.RU_ROW_2 else KeyboardLayouts.EN_ROW_2
    val row3 = if (isRu) KeyboardLayouts.RU_ROW_3 else KeyboardLayouts.EN_ROW_3

    val padding = if (isRu) 0f else KeyboardConstants.ROW_PADDING
    val sideWeight =
        if (isRu) KeyboardConstants.KEY_WEIGHT_NORMAL else KeyboardConstants.KEY_WEIGHT_LARGE

    KeyboardRow(KeyboardLayouts.NUM_ROW_1, onAction = onAction)
    KeyboardRow(row1, isShifted = isShifted, onAction = onAction)
    KeyboardRow(row2, horizontalPadding = padding, isShifted = isShifted, onAction = onAction)

    Row(modifier = Modifier
        .weight(1f)
        .fillMaxWidth()) {
        KeyboardKey("⇧", Modifier.weight(sideWeight), type = KeyType.SHIFT) {
            onAction(KeyboardAction.Shift)
        }
        row3.forEach { key ->
            val text = if (isShifted) key.uppercase() else key
            KeyboardKey(text, Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey("⌫", Modifier.weight(sideWeight), type = KeyType.DELETE) {
            onAction(KeyboardAction.Delete)
        }
    }
    BottomRow(language, onAction = onAction)
}

@Composable
private fun ColumnScope.NumbersLayout(onAction: (KeyboardAction) -> Unit) {
    KeyboardRow(KeyboardLayouts.NUM_ROW_1, onAction = onAction)
    KeyboardRow(KeyboardLayouts.NUM_ROW_2, onAction = onAction)

    Row(modifier = Modifier
        .weight(1f)
        .fillMaxWidth()) {
        KeyboardKey(
            "{&=",
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            type = KeyType.LAYOUT_CHANGE
        ) {
            onAction(KeyboardAction.ChangeLayout(KeyboardLayoutType.SYMBOLS))
        }
        KeyboardLayouts.NUM_ROW_3.forEach { key ->
            KeyboardKey(key, Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            "⌫",
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            type = KeyType.DELETE
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
    BottomRow(language = null, layoutType = KeyboardLayoutType.NUMBERS, onAction = onAction)
}

@Composable
private fun ColumnScope.SymbolsLayout(onAction: (KeyboardAction) -> Unit) {
    KeyboardRow(KeyboardLayouts.SYM_ROW_1, onAction = onAction)
    KeyboardRow(KeyboardLayouts.SYM_ROW_2, onAction = onAction)

    Row(modifier = Modifier
        .weight(1f)
        .fillMaxWidth()) {
        KeyboardKey(
            "123",
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            type = KeyType.LAYOUT_CHANGE
        ) {
            onAction(KeyboardAction.ChangeLayout(KeyboardLayoutType.NUMBERS))
        }
        KeyboardLayouts.SYM_ROW_3.forEach { key ->
            KeyboardKey(key, Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        KeyboardKey(
            "⌫",
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            type = KeyType.DELETE
        ) {
            onAction(KeyboardAction.Delete)
        }
    }
    BottomRow(language = null, layoutType = KeyboardLayoutType.SYMBOLS, onAction = onAction)
}

@Composable
private fun ColumnScope.KeyboardRow(
    keys: List<String>,
    horizontalPadding: Float = 0f,
    isShifted: Boolean = false,
    onAction: (KeyboardAction) -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .weight(1f)) {
        if (horizontalPadding > 0f) Spacer(modifier = Modifier.weight(horizontalPadding))
        keys.forEach { key ->
            val text = if (isShifted) key.uppercase() else key
            KeyboardKey(
                text = text,
                modifier = Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)
            ) {
                onAction(KeyboardAction.CommitText(key))
            }
        }
        if (horizontalPadding > 0f)
            Spacer(modifier = Modifier.weight(horizontalPadding))
    }
}

@Composable
private fun ColumnScope.BottomRow(
    language: KeyboardLanguage?,
    layoutType: KeyboardLayoutType = KeyboardLayoutType.LETTERS,
    onAction: (KeyboardAction) -> Unit
) {
    Row(modifier = Modifier
        .weight(1f)
        .fillMaxWidth()) {
        val switchText = if (layoutType == KeyboardLayoutType.LETTERS) "123" else "abc"
        val nextLayout =
            if (layoutType == KeyboardLayoutType.LETTERS)
                KeyboardLayoutType.NUMBERS
            else
                KeyboardLayoutType.LETTERS

        KeyboardKey(
            switchText,
            Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
            type = KeyType.LAYOUT_CHANGE
        ) {
            onAction(KeyboardAction.ChangeLayout(nextLayout))
        }

        when (layoutType) {
            KeyboardLayoutType.LETTERS -> BottomRowLetters(language, onAction)
            KeyboardLayoutType.NUMBERS -> BottomRowNumbers(onAction)
            KeyboardLayoutType.SYMBOLS -> BottomRowSymbols(onAction)
        }
    }
}

@Composable
private fun RowScope.BottomRowLetters(
    language: KeyboardLanguage?,
    onAction: (KeyboardAction) -> Unit
) {
    KeyboardKey(
        ",",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        type = KeyType.PUNCTUATION_MARKS
    ) { onAction(KeyboardAction.CommitText(",")) }

    val langText = if (language == KeyboardLanguage.RU) "RU" else "EN"
    KeyboardKey(
        langText,
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        type = KeyType.LANGUAGE_CHANGE
    ) { onAction(KeyboardAction.SwitchLanguage) }

    KeyboardKey(
        "",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SPACE),
        type = KeyType.SPACE
    ) { onAction(KeyboardAction.Space) }
    KeyboardKey(
        ".",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        type = KeyType.PUNCTUATION_MARKS
    ) { onAction(KeyboardAction.CommitText(".")) }
    KeyboardKey(
        "↵",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_LARGE),
        type = KeyType.ENTER
    ) { onAction(KeyboardAction.Enter) }
}

@Composable
private fun RowScope.BottomRowNumbers(onAction: (KeyboardAction) -> Unit) {
    KeyboardKey(
        ",",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        type = KeyType.PUNCTUATION_MARKS
    ) { onAction(KeyboardAction.CommitText(",")) }
    KeyboardKey(
        "",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SPACE),
        type = KeyType.SPACE
    ) { onAction(KeyboardAction.Space) }
    KeyboardKey(
        ".",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        type = KeyType.PUNCTUATION_MARKS
    ) { onAction(KeyboardAction.CommitText(".")) }
    KeyboardKey(
        "↵",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_LARGE),
        type = KeyType.ENTER
    ) { onAction(KeyboardAction.Enter) }
}

@Composable
private fun RowScope.BottomRowSymbols(onAction: (KeyboardAction) -> Unit) {
    KeyboardKey(
        "",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_SYMBOLS_SPACE),
        type = KeyType.SPACE
    ) { onAction(KeyboardAction.Space) }
    KeyboardLayouts.SYM_ROW_4.forEach { key ->
        KeyboardKey(key, Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL)) {
            onAction(
                KeyboardAction.CommitText(key)
            )
        }
    }
    KeyboardKey(
        "↵",
        Modifier.weight(KeyboardConstants.KEY_WEIGHT_NORMAL),
        type = KeyType.ENTER
    ) { onAction(KeyboardAction.Enter) }
}
