package com.kbk.presentation.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kbk.domain.models.sdk.VerificationResult

private const val COLOR_GREEN_BG = 0xFFE8F5E9
private const val COLOR_RED_BG = 0xFFFFEBEE
private const val COLOR_GREEN_TEXT = 0xFF2E7D32
private const val COLOR_RED_TEXT = 0xFFC62828

@Composable
fun PlaygroundScreen(viewModel: PlaygroundViewModel) {
    val attempts by viewModel.attempts.collectAsState()
    val message by viewModel.message.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Playground (Тест Защиты)", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.trainModel() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Обучить эталонный профиль")
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            val msgColor =
                if (message.startsWith("❌")) Color.Red else MaterialTheme.colorScheme.onBackground
            Text(message, color = msgColor, fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text("Лента верификаций:", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(attempts) { result ->
                ResultCard(result)
            }
        }
    }
}

@Composable
private fun ResultCard(result: VerificationResult) {
    val bgColor = if (result.isOwner) Color(COLOR_GREEN_BG) else Color(COLOR_RED_BG)
    val statusText = if (result.isOwner) "Владелец" else "ЗЛОУМЫШЛЕННИК!"
    val statusColor = if (result.isOwner) Color(COLOR_GREEN_TEXT) else Color(COLOR_RED_TEXT)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text("Аномальность: ${"%.2f".format(result.anomalyScore)}", fontSize = 14.sp)
            }
            Text(result.modelName, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
