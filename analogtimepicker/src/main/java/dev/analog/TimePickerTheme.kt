package dev.analog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** Готовые пресеты оформления циферблата. Любой можно донастроить через .copy(...). */
object TimePickerThemes {

  /** Классический — текущий вид по умолчанию (серый циферблат, жёлтая/оранжевая часовая). */
  val Classic: TimePickerConfig = TimePickerConfig()

  /** Тёмный — тёмная заливка, светлые цифры и стрелки. */
  val Dark: TimePickerConfig = TimePickerConfig(
    background = DialBackground.Solid(Color(0xFF202124)),
    colors = TimePickerColors(
      dialStroke = Color.White.copy(alpha = 0.30f),
      centerDotColor = Color.White,
      timeTextColor = Color.White,
      switchTextColor = Color.White,
      divisionColor = Color.Gray,
      majorDivisionColor = Color.LightGray,
      minuteNumbersColor = Color.White,
      currentHourNumbersColor = Color.White,
      oppositeHourNumbersColor = Color.Gray
    ),
    hourHand = HandShape.Line(
      color = Color(0xFF80DEEA), colorPm = Color(0xFF26C6DA),
      widthPx = 14f, lengthFraction = 0.5f
    ),
    minuteHand = HandShape.Line(color = Color.White, widthPx = 8f, lengthFraction = 0.86f)
  )

  /** Минимализм — цифры только на четвертях, остальное точками. */
  val Minimal: TimePickerConfig = TimePickerConfig(
    numeralStyle = NumeralStyle.QuartersOnly()
  )

  /** Римский — римские цифры, шрифт с засечками. */
  val Roman: TimePickerConfig = TimePickerConfig(
    numeralStyle = NumeralStyle.Roman,
    textStyle = NumeralTextStyle(fontFamily = FontFamily.Serif)
  )
}
