package dev.analog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Цвета элементов циферблата (фон задаётся отдельно через [DialBackground]). */
data class TimePickerColors(
  val dialStroke: Color = Color.Gray.copy(alpha = 0.32f),
  val centerDotColor: Color = Color.Black.copy(alpha = 0.6f),
  val timeTextColor: Color = Color.Yellow,
  val switchTextColor: Color = Color.Black,
  val divisionColor: Color = Color.Gray,
  val majorDivisionColor: Color = Color.DarkGray,
  val minuteNumbersColor: Color = Color.LightGray,
  val currentHourNumbersColor: Color = Color.LightGray,
  val oppositeHourNumbersColor: Color = Color.Gray
)

/** Размеры (в px канвы) и шрифт цифр на циферблате. */
data class NumeralTextStyle(
  val minuteTextSize: Float = 60f,
  val currentHourTextSize: Float = 55f,
  val oppositeHourTextSize: Float = 40f,
  /** Шрифт цифр; null — шрифт по умолчанию. */
  val fontFamily: FontFamily? = null
)

/**
 * Полная конфигурация оформления циферблата: цвета, фон, стрелки, цифры и размер.
 * Готовые пресеты — в [TimePickerThemes]; любой можно донастроить через .copy(...).
 */
data class TimePickerConfig(
  val colors: TimePickerColors = TimePickerColors(),
  val background: DialBackground = DialBackground.Solid(Color.Gray.copy(alpha = 0.12f)),
  val hourHand: HandShape = HandShape.Line(
    color = Color(0xFFFFFF00),    // AM — жёлтая
    colorPm = Color(0xFFFF8000),  // PM — оранжевая
    widthPx = 14f,
    lengthFraction = 0.5f
  ),
  val minuteHand: HandShape = HandShape.Line(
    color = null,                 // null — primary из темы
    widthPx = 8f,
    lengthFraction = 0.86f
  ),
  val textStyle: NumeralTextStyle = NumeralTextStyle(),
  val numeralStyle: NumeralStyle = NumeralStyle.Arabic,
  val radius: Dp = 160.dp
)