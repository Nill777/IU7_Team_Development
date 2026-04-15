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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading || state.samples.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                if (state.isLoading) "Загрузка..." else "Нет данных. Поэксплуатируйте клавиатуру.",
                fontSize = 18.sp
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Биометрический анализ",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(Modifier.padding(8.dp)) {
                Text("Тепловая карта набора", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

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

                Spacer(Modifier.height(8.dp))
                Text(
                    "Русская раскладка:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                KeyboardHeatmap(
                    isRu = true,
                    samples = state.samples,
                    metricType = state.heatmapMetric
                )

                Spacer(Modifier.height(32.dp))
                Text(
                    "Английская раскладка:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                KeyboardHeatmap(
                    isRu = false,
                    samples = state.samples,
                    metricType = state.heatmapMetric
                )
            }
        }

        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(Modifier.padding(8.dp)) {
                Text("Анализ распределения", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

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

                Spacer(Modifier.height(16.dp))

                HistogramChart(
                    title = "Время удержания", unit = "мс",
                    samples = state.samples, targetKey = state.selectedKey,
                    valueSelector = { it.touchData.dwellTime.toFloat() }
                )

                HistogramChart(
                    title = "Время полёта", unit = "мс",
                    samples = state.samples, targetKey = state.selectedKey,
                    valueSelector = { it.touchData.flightTime.toFloat() }
                )

                HistogramChart(
                    title = "Сила нажатия", unit = "у.е.",
                    samples = state.samples, targetKey = state.selectedKey,
                    valueSelector = { it.touchData.pressure }
                )

                HistogramChart(
                    title = "Смещение по X", unit = "px",
                    samples = state.samples, targetKey = state.selectedKey,
                    valueSelector = { it.touchData.touchX }
                )

                HistogramChart(
                    title = "Смещение по Y", unit = "px",
                    samples = state.samples, targetKey = state.selectedKey,
                    valueSelector = { it.touchData.touchY }
                )
                TouchSpreadChart(
                    title = "Кучность касаний",
                    samples = state.samples,
                    targetKey = state.selectedKey
                )
            }
        }

        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(Modifier.padding(8.dp)) {
                Text("Матрица переходов", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                TransitionMatrixChart(matrix = state.transitionMatrix)
            }
        }

        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(Modifier.padding(8.dp)) {
                Text("Микромоторика", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                RadarChart(
                    "Акселерометр",
                    state.samples,
                    SensorType.ACCELEROMETER,
                    Color(0xFFF44336)
                )
                Spacer(Modifier.height(8.dp))
                RadarChart(
                    "Гироскоп",
                    state.samples,
                    SensorType.GYROSCOPE,
                    Color(0xFF4CAF50)
                )
                Spacer(Modifier.height(8.dp))
                RadarChart(
                    "Вектор поворота",
                    state.samples,
                    SensorType.ROTATION_VECTOR,
                    Color(0xFF2196F3)
                )
            }
        }
    }
}
