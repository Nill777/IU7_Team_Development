package com.kbk.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DASHBOARD_PADDING = 8.dp
private val DASHBOARD_SPACING = 24.dp
private val CARD_ELEVATION = 4.dp
private val EMPTY_TEXT_SIZE = 18.sp
private val NO_MATRIX_TEXT_SIZE = 14.sp
private val COLOR_ACCELEROMETER = Color(0xFFF44336)
private val COLOR_GYROSCOPE = Color(0xFF4CAF50)
private val COLOR_ROTATION = Color(0xFF2196F3)

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading || state.samples.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                if (state.isLoading) "Загрузка..." else "Нет данных. Поэксплуатируйте клавиатуру.",
                fontSize = EMPTY_TEXT_SIZE
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DASHBOARD_PADDING),
        verticalArrangement = Arrangement.spacedBy(DASHBOARD_SPACING)
    ) {
        Text(
            "Биометрический анализ",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        HeatmapCard(state, viewModel)
        DistributionCard(state, viewModel)
        TransitionMatrixCard(state)
        MicromotorCard(state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeatmapCard(state: DashboardUiState, viewModel: DashboardViewModel) {
    Card(
        elevation = CardDefaults.cardElevation(CARD_ELEVATION),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(Modifier.padding(DASHBOARD_PADDING)) {
            Text("Тепловая карта набора", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(DASHBOARD_PADDING))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = state.heatmapMetric.label,
                    onValueChange = { },
                    label = { Text("Отображаемая метрика") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedLabelColor = MaterialTheme.colorScheme.onBackground,

                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,

                        focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary
                    )

                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    HeatmapMetricType.entries.forEach { metric ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    metric.label,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            onClick = {
                                viewModel.setHeatmapMetric(metric)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(DASHBOARD_PADDING))
            Text(
                "Русская раскладка:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            KeyboardHeatmap(
                isRu = true,
                samples = state.samples,
                metricType = state.heatmapMetric
            )

            Spacer(Modifier.height(DASHBOARD_PADDING))
            Text(
                "Английская раскладка:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            KeyboardHeatmap(
                isRu = false,
                samples = state.samples,
                metricType = state.heatmapMetric
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistributionCard(state: DashboardUiState, viewModel: DashboardViewModel) {
    Card(
        elevation = CardDefaults.cardElevation(CARD_ELEVATION),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(Modifier.padding(DASHBOARD_PADDING)) {
            Text("Анализ распределения", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(DASHBOARD_PADDING))

            var expandedKey by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedKey,
                onExpandedChange = { expandedKey = !expandedKey }) {
                TextField(
                    readOnly = true,
                    value = if (state.selectedKey.isEmpty()) "Клавиша: Пробел" else "Клавиша: ${state.selectedKey}",
                    onValueChange = { },
                    label = { Text("Анализируемая клавиша") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKey) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedLabelColor = MaterialTheme.colorScheme.onBackground,

                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,

                        focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedKey,
                    onDismissRequest = { expandedKey = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    state.sortedAvailableKeys.forEach { key ->
                        val displayKey = key.ifEmpty { "Пробел" }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    displayKey,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            onClick = { viewModel.setSelectedKey(key); expandedKey = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(DASHBOARD_PADDING))
            HistogramChart(
                title = "Время удержания", unit = "мс",
                samples = state.samples, targetKey = state.selectedKey,
                valueSelector = { it.touchData.dwellTime.toFloat() }
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            HistogramChart(
                title = "Время полёта", unit = "мс",
                samples = state.samples, targetKey = state.selectedKey,
                valueSelector = { it.touchData.flightTime.toFloat() }
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            HistogramChart(
                title = "Сила нажатия", unit = "у.е.",
                samples = state.samples, targetKey = state.selectedKey,
                valueSelector = { it.touchData.pressure }
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            HistogramChart(
                title = "Смещение по X", unit = "px",
                samples = state.samples, targetKey = state.selectedKey,
                valueSelector = { it.touchData.touchX }
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            HistogramChart(
                title = "Смещение по Y", unit = "px",
                samples = state.samples, targetKey = state.selectedKey,
                valueSelector = { it.touchData.touchY }
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            TouchSpreadChart(
                title = "Кучность касаний",
                samples = state.samples,
                targetKey = state.selectedKey
            )
        }
    }
}

@Composable
private fun TransitionMatrixCard(state: DashboardUiState) {
    Card(
        elevation = CardDefaults.cardElevation(CARD_ELEVATION),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(Modifier.padding(DASHBOARD_PADDING)) {
            Text("Матрицы переходов", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(DASHBOARD_PADDING))

            TransitionMatrixChart(
                title = "Русские буквы",
                matrix = state.ruTransitionMatrix
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            TransitionMatrixChart(
                title = "Английские буквы",
                matrix = state.enTransitionMatrix
            )

            if (state.ruTransitionMatrix.isEmpty() && state.enTransitionMatrix.isEmpty()) {
                Text(
                    "Недостаточно данных для построения матриц",
                    color = Color.Gray,
                    fontSize = NO_MATRIX_TEXT_SIZE
                )
            }
        }
    }
}

@Composable
private fun MicromotorCard(state: DashboardUiState) {
    Card(
        elevation = CardDefaults.cardElevation(CARD_ELEVATION),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(Modifier.padding(DASHBOARD_PADDING)) {
            Text("Микромоторика", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(DASHBOARD_PADDING))
            RadarChart(
                "Акселерометр",
                state.samples,
                SensorType.ACCELEROMETER,
                COLOR_ACCELEROMETER
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            RadarChart(
                "Гироскоп",
                state.samples,
                SensorType.GYROSCOPE,
                COLOR_GYROSCOPE
            )
            Spacer(Modifier.height(DASHBOARD_PADDING))
            RadarChart(
                "Вектор поворота",
                state.samples,
                SensorType.ROTATION_VECTOR,
                COLOR_ROTATION
            )
        }
    }
}
