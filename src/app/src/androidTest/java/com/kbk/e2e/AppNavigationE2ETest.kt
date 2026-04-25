package com.kbk.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kbk.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationE2ETest {
    // поднимает MainActivity перед каждым тестом
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppNavigation() {
        // дашборд загрузится (при чистом запуске БД пуста)
        composeTestRule.onNodeWithText("Загрузка...")
            .assertIsDisplayed()

        // кликаем по вкладке "Настройки" и проверяем переход
        composeTestRule.onNodeWithText("Настройки").performClick()
        composeTestRule.onNodeWithText("Настройки защиты").assertIsDisplayed()

        // кликаем по вкладке "Тест" и проверяем переход
        composeTestRule.onNodeWithText("Тест").performClick()
        composeTestRule.onNodeWithText("Тест защиты").assertIsDisplayed()
    }
}
