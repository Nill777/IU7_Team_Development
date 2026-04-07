package com.kbk.presentation.keyboard

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
}
