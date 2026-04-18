package com.kbk.presentation.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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

internal const val ROW_WEIGHT = 1f
internal const val ZERO_PADDING = 0f

data class KeyboardRowParams(
    val keys: List<String>,
    val horizontalPadding: Float = ZERO_PADDING,
    val isShifted: Boolean = false,
    val viewModel: KeyboardViewModel? = null,
    val isBackdrop: Boolean = false
)

data class LettersThirdRowParams(
    val row3: List<String>,
    val isShifted: Boolean,
    val viewModel: KeyboardViewModel?,
    val isBackdrop: Boolean,
    val sideWeight: Float
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
                isBackdrop = false,
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
                if (language == KeyboardLanguage.RU) KeyboardLanguage.EN else KeyboardLanguage.RU
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
