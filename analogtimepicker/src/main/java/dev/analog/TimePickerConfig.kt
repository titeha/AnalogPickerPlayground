package dev.analog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class TimePickerColors(
  val dialBackground: Color = Color.Gray.copy(alpha = 0.12f),
  val dialStroke: Color = Color.Gray.copy(alpha = 0.32f),
  val hourHandColor: Color = Color(0xFFFFFF00),
  val hourHandColorPM: Color = Color(0xFFFF8000),
  val minuteHandColor: Color = Color(0xFF2196F3),
  val centerDotColor: Color = Color.Black.copy(alpha = 0.6f),
  val timeTextColor: Color = Color.Yellow,
  val switchTextColor: Color = Color.Black,
  val divisionColor: Color = Color.Gray,
  val majorDivisionColor: Color = Color.DarkGray,
  val minuteNumbersColor: Int = android.graphics.Color.LTGRAY,
  val currentHourNumbersColor: Int = android.graphics.Color.LTGRAY,
  val oppositeHourNumbersColor: Int = android.graphics.Color.GRAY
)

data class HandStyle(
  val hourHandWidth: Dp = 14.dp,
  val minuteHandWidth: Dp = 8.dp,
  val hourHandLength: Float = 0.5f,  // 50% радиуса
  val minuteHandLength: Float = 0.86f // 86% радиуса
)

data class TextStyle(
  val minuteTextSize: Float = 60f,
  val currentHourTextSize: Float = 55f,
  val oppositeHourTextSize: Float = 40f
)

data class TimePickerConfig(
  val colors: TimePickerColors = TimePickerColors(),
  val handStyle: HandStyle = HandStyle(),
  val textStyle: TextStyle = TextStyle(),
  val showTimeText: Boolean = true,
  val showSnapSwitch: Boolean = true,
  val snapTo5Minutes: Boolean = true,
  val radius: Dp = 160.dp
)