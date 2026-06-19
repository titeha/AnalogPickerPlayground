package com.example.analogpickerplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.analog.AnalogTimePicker
import dev.analog.AnalogTimePickerDialog
import dev.analog.TimePickerThemes
import java.time.LocalTime

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        var time by remember { mutableStateOf(LocalTime.of(7, 30)) }
        var showDialog by remember { mutableStateOf(false) }
        var theme by remember { mutableStateOf(TimePickerThemes.Classic) }

        val themeOptions = listOf(
          "Classic" to TimePickerThemes.Classic,
          "Dark" to TimePickerThemes.Dark,
          "Minimal" to TimePickerThemes.Minimal,
          "Roman" to TimePickerThemes.Roman
        )

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text("Выбрано: $time", style = MaterialTheme.typography.titleLarge)
          Spacer(Modifier.height(16.dp))

          AnalogTimePicker(
            time = time,
            onTimeChange = { time = it },
            config = theme.copy(radius = 120.dp),
            snapLabel = "5 минут"
          )

          Spacer(Modifier.height(16.dp))

          // Переключатель темы
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
          ) {
            themeOptions.forEach { (label, t) ->
              Button(onClick = { theme = t }) { Text(label) }
            }
          }

          Spacer(Modifier.height(16.dp))

          Button(onClick = { showDialog = true }) {
            Text("Открыть диалог выбора времени")
          }
        }

        if (showDialog) {
          AnalogTimePickerDialog(
            initialTime = time,
            onTimeSelected = { newTime ->
              time = newTime
              showDialog = false
            },
            onDismiss = { showDialog = false },
            title = "Установите время",
            confirmButtonText = "Выбрать",
            dismissButtonText = "Отмена",
            snapLabel = "5 минут"
          )
        }
      }
    }
  }
}
