package com.kbk.keystrokeSdk.models

import com.kbk.domain.models.BiometricSample
import com.kbk.domain.models.MotionData
import com.kbk.domain.models.TouchData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimingModelTest {
    private val model = TimingModel()

    private fun createSample(key: String, dwell: Long, flight: Long): BiometricSample {
        return BiometricSample(
            touchData = TouchData(key = key, dwellTime = dwell, flightTime = flight),
            motionData = MotionData()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `train should throw exception if samples size is less than 10`() {
        val samples = List(5) { createSample("a", 100L, 50L) }
        model.train(samples) // Должно выбросить IllegalArgumentException
    }

    @Test
    fun `verify should return isOwner true for legitimate user`() {
        // Обучаем на стабильных данных (ровно 100мс и 50мс)
        val trainSamples = List(15) { createSample("a", 100L, 50L) }
        val profile = model.train(trainSamples)

        // Попытка ввода идентичными данными
        val attempt = List(3) { createSample("a", 100L, 50L) }

        // Махаланобис при полной идентичности даст 0.0, что меньше порога
        val result = model.verify(attempt, profile, threshold = 0.1f)

        assertTrue("Владелец должен быть успешно верифицирован", result.isOwner)
    }

    @Test
    fun `verify should return isOwner false for imposter`() {
        // Обучаем модель (владелец печатает быстро)
        val trainSamples = List(15) { createSample("a", 100L, 50L) }
        val profile = model.train(trainSamples)

        // Попытка ввода мошенником (печатает медленно)
        val attempt = List(3) { createSample("a", 500L, 300L) }

        val result = model.verify(attempt, profile, threshold = 3.0f)

        assertFalse("Мошенник должен быть отклонен", result.isOwner)
    }
}
