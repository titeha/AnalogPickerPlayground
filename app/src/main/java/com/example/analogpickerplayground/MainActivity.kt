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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.analog.AnalogTimePicker
import dev.analog.AnalogTimePickerDialog
import dev.analog.DialBackground
import dev.analog.NumeralStyle
import dev.analog.TimePickerConfig
import java.time.LocalTime

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        var time by remember { mutableStateOf(LocalTime.of(7, 30)) }
        var showDialog by remember { mutableStateOf(false) }
        var numeralStyle by remember { mutableStateOf<NumeralStyle>(NumeralStyle.Arabic) }
        var background by remember {
          mutableStateOf<DialBackground>(DialBackground.Solid(Color.Gray.copy(alpha = 0.12f)))
        }

        val bgOptions = listOf(
          "Серый" to DialBackground.Solid(Color.Gray.copy(alpha = 0.12f)),
          "Градиент" to DialBackground.Gradient(
            Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF7E57C2)))
          ),
          "Картинка" to DialBackground.Image(
            painterResource(R.drawable.ic_launcher_foreground), alpha = 0.5f
          ),
          "Нет" to DialBackground.None
        )

        val styleOptions = listOf(
          "Арабские" to NumeralStyle.Arabic,
          "Римские" to NumeralStyle.Roman,
          "Арабо-инд." to NumeralStyle.ArabicIndic,
          "Точки" to NumeralStyle.Dots,
          "Четверти" to NumeralStyle.QuartersOnly(),
          "Нет" to NumeralStyle.None
        )

        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text("Выбрано: $time", style = MaterialTheme.typography.titleLarge)
          Spacer(Modifier.height(16.dp))

          // Существующий встроенный виджет
          AnalogTimePicker(
            time = time,
            onTimeChange = { time = it },
            config = TimePickerConfig(
              radius = 120.dp,
              numeralStyle = numeralStyle,
              background = background
            )
          )

          Spacer(Modifier.height(16.dp))

          // Переключатель стиля цифр (для демонстрации)
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
          ) {
            styleOptions.forEach { (label, style) ->
              Button(onClick = { numeralStyle = style }) { Text(label) }
            }
          }

          // Переключатель фона (для демонстрации)
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
          ) {
            bgOptions.forEach { (label, bg) ->
              Button(onClick = { background = bg }) { Text(label) }
            }
          }

          Spacer(Modifier.height(16.dp))

          // Кнопка для открытия диалога
          Button(onClick = { showDialog = true }) {
            Text("Открыть диалог выбора времени")
          }
        }

        // Диалог
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
            dismissButtonText = "Отмена"
          )
        }
      }
    }
  }
}