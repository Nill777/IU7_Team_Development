package com.kbk.domain.models

data class TouchData(
    val key: String = "",
    val dwellTime: Long = 0L,
    val flightTime: Long = 0L,
    val pressure: Float = 0f,
    val touchX: Float = 0f,
    val touchY: Float = 0f,
    val swipeVectorX: Float = 0f,
    val swipeVectorY: Float = 0f
)
