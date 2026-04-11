package com.kbk.presentation.keyboard

import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.TouchData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KeyboardViewModel(
    private val biometricService: IBiometricService
) : ViewModel() {
    private var lastUpTime: Long = 0L

    fun onKeyboardShown() {
        lastUpTime = 0L
        biometricService.startBiometricCollection()
    }

    fun onKeyboardHidden() = biometricService.stopBiometricCollection()

    fun onKeyEvent(
        key: String,
        downEvent: PointerInputChange,
        upEvent: PointerInputChange
    ) {
        val dwellTime = upEvent.uptimeMillis - downEvent.uptimeMillis
        val flightTime = if (lastUpTime == 0L) 0L else downEvent.uptimeMillis - lastUpTime
        lastUpTime = upEvent.uptimeMillis

        viewModelScope.launch(Dispatchers.Default) {
            biometricService.saveSample(
                TouchData(
                    key,
                    dwellTime,
                    flightTime,
                    downEvent.pressure,
                    downEvent.position.x,
                    downEvent.position.y,
                    upEvent.position.x - downEvent.position.x,
                    upEvent.position.y - downEvent.position.y
                )
            )
        }
    }
}
