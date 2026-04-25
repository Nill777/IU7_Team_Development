package com.kbk.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kbk.data.local.BiometricDatabase
import com.kbk.data.repository.BiometricRepository
import com.kbk.domain.irepository.IMotionRepository
import com.kbk.domain.irepository.ISettingsRepository
import com.kbk.domain.models.MotionData
import com.kbk.domain.models.TouchData
import com.kbk.domain.service.BiometricService
import com.kbk.keystrokeSdk.ensemble.KeystrokeVerificationManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BiometricIntegrationTest {

    private lateinit var database: BiometricDatabase
    private lateinit var biometricRepository: BiometricRepository
    private lateinit var verificationManager: KeystrokeVerificationManager
    private lateinit var biometricService: BiometricService

    private lateinit var mockMotionRepo: IMotionRepository
    private lateinit var mockSettingsRepo: ISettingsRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()

        // создаем In-Memory базу данных
        database = Room.inMemoryDatabaseBuilder(context, BiometricDatabase::class.java)
            .allowMainThreadQueries() // в тестах разрешаем синхронные запросы
            .build()
        biometricRepository = BiometricRepository(database.biometricSampleDao())

        // создаем менеджер математических моделей
        verificationManager = KeystrokeVerificationManager()

        // мокаем внешние зависимости
        mockMotionRepo = mockk(relaxed = true)
        every { mockMotionRepo.motionState } returns MutableStateFlow(MotionData(timestamp = 1000L))

        mockSettingsRepo = mockk(relaxed = true)
        every { mockSettingsRepo.batchSize } returns MutableStateFlow(4)
        every { mockSettingsRepo.timingThreshold } returns MutableStateFlow(5.0f)
        every { mockSettingsRepo.spatialThreshold } returns MutableStateFlow(5.0f)
        every { mockSettingsRepo.motionThreshold } returns MutableStateFlow(5.0f)

        // собираем сервис
        biometricService = BiometricService(
            biometricRepository,
            mockMotionRepo,
            verificationManager,
            mockSettingsRepo
        )
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `test complete flow from data collection to verification`() = runTest {
        // ввод 15 одинаковых букв "а"
        repeat(15) {
            biometricService.saveSample(
                TouchData(key = "а", dwellTime = 100L, flightTime = 50L)
            )
        }
        // даем корутинам время на выполнение
        testScheduler.advanceUntilIdle()

        // построение биометрического профиля
        biometricService.trainProfileFromDb()
        testScheduler.advanceUntilIdle()

        assertTrue(
            "Сервис должен перейти в режим защиты",
            biometricService.isVerificationMode.value
        )

        // верификация легитимного ввода
        // вводим еще 4 буквы "а" с такими же таймингами
        repeat(4) {
            biometricService.saveSample(
                TouchData(key = "а", dwellTime = 101L, flightTime = 51L)
            )
        }
        testScheduler.advanceUntilIdle()

        val result = biometricService.verificationResultFlow.value
        assertNotNull("Результат верификации должен быть сформирован", result)
        assertTrue("Владелец должен быть успешно авторизован", result!!.isOwner)

        // проверяем, что попытка попала в историю
        val history = biometricService.verificationHistoryFlow.value
        assertEquals("История фоновых верификаций должна содержать 1 запись", 1, history.size)
    }
}
