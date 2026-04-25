package com.kbk.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kbk.data.local.BiometricDatabase
import com.kbk.data.repository.BiometricRepository
import com.kbk.data.repository.SettingsRepository
import com.kbk.data.sensors.AndroidMotionRepository
import com.kbk.domain.isdk.VerificationStrategy
import com.kbk.domain.service.BiometricService
import com.kbk.keystrokeSdk.ensemble.KeystrokeVerificationManager
import com.kbk.presentation.keyboard.KeyboardScreen
import com.kbk.presentation.keyboard.KeyboardViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardProtectionE2ETest {
    // создаем без Activity
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: BiometricDatabase
    private lateinit var biometricService: BiometricService

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // поднимаем In-Memory БД
        database = Room.inMemoryDatabaseBuilder(context, BiometricDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val biometricRepo = BiometricRepository(database.biometricSampleDao())
        val motionRepo = AndroidMotionRepository(context)
        val verificationManager = KeystrokeVerificationManager()
        val settingsRepo = SettingsRepository(context)

        biometricService =
            BiometricService(biometricRepo, motionRepo, verificationManager, settingsRepo)

        // переключаем стратегию на проверку только таймингов.
        biometricService.setVerificationStrategy(VerificationStrategy.TIMING_ONLY)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testKeyboardTypingAndProtectionTrigger() {
        val viewModel = KeyboardViewModel(biometricService)
        // отрисовываем UI клавиатуры на экране
        composeTestRule.setContent {
            KeyboardScreen(viewModel = viewModel, onAction = { })
        }

        // проверяем, что клавиша "й" отрендерилась
        val keyNode = composeTestRule.onNodeWithText("й")
        keyNode.assertIsDisplayed()

        // обучение
        // имитируем стабильный ввод: удержание 100мс
        repeat(15) {
            keyNode.performTouchInput {
                down(center)
                advanceEventTime(100)
                up()
            }
            composeTestRule.mainClock.advanceTimeBy(50)
        }

        // ждем, пока корутины запишут все 15 семплов в базу
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            biometricService.totalSamplesCount.value >= 15
        }

        // рассчитываем эталонный профиль на сохраненных данных
        runBlocking { biometricService.trainProfileFromDb() }
        assertTrue(
            "Сервис должен перейти в режим защиты",
            biometricService.isVerificationMode.value
        )

        // злоумышленник печатает в 5 раз медленнее (удержание 500мс)
        repeat(4) { // 4 - размер батча по умолчанию
            keyNode.performTouchInput {
                down(center)
                advanceEventTime(500)
                up()
            }
            composeTestRule.mainClock.advanceTimeBy(300)
        }

        // ждем вердикт
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            biometricService.verificationResultFlow.value != null
        }

        val result = biometricService.verificationResultFlow.value
        assertNotNull("Результат не должен быть null", result)
        assertFalse("Защита должна выявить злоумышленника (isOwner = false)", result!!.isOwner)
    }
}
