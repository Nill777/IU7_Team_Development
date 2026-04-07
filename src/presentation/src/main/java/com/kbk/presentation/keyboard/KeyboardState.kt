package com.kbk.presentation.keyboard

enum class KeyboardLanguage { RU, EN }
enum class KeyboardLayoutType { LETTERS, NUMBERS, SYMBOLS }

data class KeyboardActions(
    val onLanguageChanged: (KeyboardLanguage) -> Unit,
    val onLayoutChanged: (KeyboardLayoutType) -> Unit,
    val onShiftChanged: (Boolean) -> Unit,
    val onExternalAction: (KeyboardAction) -> Unit
)

sealed interface KeyboardAction {
    data class CommitText(val text: String) : KeyboardAction
    data object Delete : KeyboardAction
    data object Enter : KeyboardAction
    data object Space : KeyboardAction
    data object Shift : KeyboardAction
    data object SwitchLanguage : KeyboardAction
    data class ChangeLayout(val layout: KeyboardLayoutType) : KeyboardAction
}
