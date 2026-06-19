package dev.analog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class TimePickerColors(
  val dialStroke: Color = Color.Gray.copy(alpha = 0.32f),
  val hourHandColor: Color = Color(0xFFFFFF00),
  val hourHandColorPM: Color = Color(0xFFFF8000),
  /** null — взять primary из текущей MaterialTheme (поведение по умолчанию). */
  val minuteHandColor: Color? = null,
  val centerDotColor: Color = Color.Black.copy(alpha = 0.6f),
  val timeTextColor: Color = Color.Yellow,
  val switchTextColor: Color = Color.Black,
  val divisionColor: Color = Color.Gray,
  val majorDivisionColor: Color = Color.DarkGray,
  val minuteNumbersColor: Color = Color.LightGray,
  val currentHourNumbersColor: Color = Color.LightGray,
  val oppositeHourNumbersColor: Color = Color.Gray
)

data class HandStyle(
  val hourHandWidth: Float = 14f,    // толщина в px канвы
  val minuteHandWidth: Float = 8f,   // толщина в px канвы
  val hourHandLength: Float = 0.5f,  // 50% радиуса
  val minuteHandLength: Float = 0.86f // 86% радиуса
)

data class NumeralTextStyle(
  val minuteTextSize: Float = 60f,
  val currentHourTextSize: Float = 55f,
  val oppositeHourTextSize: Float = 40f
)

data class TimePickerConfig(
  val colors: TimePickerColors = TimePickerColors(),
  val background: DialBackground = DialBackground.Solid(Color.Gray.copy(alpha = 0.12f)),
  val handStyle: HandStyle = HandStyle(),
  val textStyle: NumeralTextStyle = NumeralTextStyle(),
  val numeralStyle: NumeralStyle = NumeralStyle.Arabic,
  val showTimeText: Boolean = true,
  val showSnapSwitch: Boolean = true,
  val snapTo5Minutes: Boolean = true,
  val radius: Dp = 160.dp
)