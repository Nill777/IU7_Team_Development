package com.kbk.presentation.playground

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kbk.domain.iservice.IBiometricService
import com.kbk.domain.models.sdk.VerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaygroundViewModel(
    private val biometricService: IBiometricService
) : ViewModel() {

    private val _attempts = MutableStateFlow<List<VerificationResult>>(emptyList())
    val attempts = _attempts.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    init {
        viewModelScope.launch {
            biometricService.verificationResultFlow.collect { result ->
                if (result != null) {
                    _attempts.value = listOf(result) + _attempts.value
                }
            }
        }
    }

    fun trainModel() {
        viewModelScope.launch {
            try {
                _message.value = "⏳ Обучение матриц..."
                biometricService.trainProfileFromDb()
                _message.value =
                    "✅ Профиль успешно обучен! Теперь перейдите в любой чат и печатайте текст."
            } catch (e: IllegalArgumentException) {
                // конкретную ошибку валидации данных
                _message.value = "❌ Ошибка: ${e.message}"
            } catch (e: IllegalStateException) {
                // ошибку вырожденной матрицы
                _message.value = "❌ Ошибка математического ядра: ${e.message}"
            }
        }
    }
}
