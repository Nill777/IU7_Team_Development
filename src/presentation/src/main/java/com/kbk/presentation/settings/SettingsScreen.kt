package com.kbk.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kbk.domain.models.sdk.VerificationAttempt
import com.kbk.presentation.components.DetailedResultCard
import com.kbk.presentation.components.SettingsActions
import com.kbk.presentation.components.SettingsCard
import com.kbk.presentation.components.SettingsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SETTING_PADDING_VAL = 8
private const val SETTING_SPACING_VAL = 8
private const val CARD_ELEVATION_VAL = 4

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val batchSize by viewModel.batchSize.collectAsState()
    val timingThreshold by viewModel.timingThreshold.collectAsState()
    val spatialThreshold by viewModel.spatialThreshold.collectAsState()
    val motionThreshold by viewModel.motionThreshold.collectAsState()
    val history by viewModel.verificationHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SETTING_PADDING_VAL.dp),
        verticalArrangement = Arrangement.spacedBy(SETTING_SPACING_VAL.dp)
    ) {
        Text("Настройки защиты", style = MaterialTheme.typography.headlineMedium)
        SettingsCard(
            title = "Параметры верификации",
            state = SettingsState(
                batchSize = batchSize,
                timingThreshold = timingThreshold,
                spatialThreshold = spatialThreshold,
                motionThreshold = motionThreshold
            ),
            actions = SettingsActions(
                onBatchSizeChange = viewModel::updateBatchSize,
                onTimingChange = viewModel::updateTimingThreshold,
                onSpatialChange = viewModel::updateSpatialThreshold,
                onMotionChange = viewModel::updateMotionThreshold
            )
        )
        HistoryCard(history = history)
    }
}

@Composable
private fun HistoryCard(history: List<VerificationAttempt>) {
    Card(
        elevation = CardDefaults.cardElevation(CARD_ELEVATION_VAL.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(SETTING_PADDING_VAL.dp)) {
            Text(
                text = "История верификаций",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(SETTING_SPACING_VAL.dp))

            if (history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "История пуста. Удостоверьтесь, что приложение работает " +
                                "в режиме верификации. При необходимости возможен " +
                                "принудительный расчет эталонного профиля на тестовом экране",
                        color = Color.Gray,
                    )
                }
            } else {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(SETTING_SPACING_VAL.dp),
                ) {
                    items(history) { attempt ->
                        val dateString = dateFormat.format(Date(attempt.timestamp))
                        Column {
                            Text(
                                text = dateString,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            DetailedResultCard(attempt.results)
                        }
                    }
                }
            }
        }
    }
}
