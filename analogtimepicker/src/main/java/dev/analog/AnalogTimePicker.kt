package dev.analog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class Hand { Minute, Hour }

/**
 * Рисует текст, отцентрованный по точке [center].
 * [fontSizePx] — размер в пикселях canvas (через toSp() не зависит от плотности экрана).
 */
private fun DrawScope.drawClockText(
  textMeasurer: TextMeasurer,
  text: String,
  center: Offset,
  color: Color,
  fontSizePx: Float,
  fontWeight: FontWeight = FontWeight.Normal
) {
  val layout = textMeasurer.measure(
    text = text,
    style = TextStyle(color = color, fontSize = fontSizePx.toSp(), fontWeight = fontWeight)
  )
  drawText(
    textLayoutResult = layout,
    topLeft = Offset(
      center.x - layout.size.width / 2f,
      center.y - layout.size.height / 2f
    )
  )
}

/** Рисует метку циферблата согласно выбранной стратегии: текст, точку или ничего. */
private fun DrawScope.drawNumeral(
  textMeasurer: TextMeasurer,
  cell: NumeralCell,
  center: Offset,
  color: Color,
  fontSizePx: Float,
  fontWeight: FontWeight = FontWeight.Normal
) {
  when (cell) {
    is NumeralCell.Text ->
      drawClockText(textMeasurer, cell.value, center, color, fontSizePx, fontWeight)

    NumeralCell.Dot ->
      drawCircle(color = color, radius = fontSizePx * 0.12f, center = center)

    NumeralCell.Empty -> Unit
  }
}

@Composable
fun AnalogTimePicker(
  modifier: Modifier = Modifier,
  time: LocalTime = LocalTime.now(),
  onTimeChange: (LocalTime) -> Unit,
  config: TimePickerConfig = TimePickerConfig()
) {
  val colors = config.colors
  val handStyle = config.handStyle
  val textStyle = config.textStyle

  var minutes by remember(time) {
    mutableIntStateOf(time.hour * 60 + time.minute)
  }

  var snapEnabled by remember { mutableStateOf(config.snapTo5Minutes) }
  var selectedHand by remember { mutableStateOf<Hand?>(null) }

  val updateTime: (Int) -> Unit = { newMinutes ->
    minutes = newMinutes
    val h = newMinutes / 60
    val m = newMinutes % 60
    onTimeChange(LocalTime.of(h, m))
  }

  fun angleAt(position: Offset, size: Size): Float =
    ClockMath.clockAngleDegrees(position.x - size.width / 2f, position.y - size.height / 2f)

  fun minuteAt(position: Offset, size: Size): Int =
    ClockMath.minuteFromAngle(angleAt(position, size), snapEnabled)

  fun hour24At(position: Offset, size: Size): Int {
    val hour12 = ClockMath.hour12FromAngle(angleAt(position, size))
    val pm = ClockMath.nextPmOnHourDrag(minutes, hour12)
    return ClockMath.to24Hour(hour12, pm)
  }

  // Выбор стрелки по близости точки касания к её линии «центр→кончик».
  // dialR — тот же радиус, что используется при отрисовке стрелок.
  fun pickHand(position: Offset, center: Offset, dialR: Float): Hand {
    val mm = minutes % 60
    val hh = minutes / 60
    val minuteRad = Math.toRadians((mm * 6 - 90).toDouble())
    val hourRad = Math.toRadians(((hh % 12) * 30 - 90).toDouble())
    val minuteTip = Offset(
      center.x + cos(minuteRad).toFloat() * dialR * handStyle.minuteHandLength,
      center.y + sin(minuteRad).toFloat() * dialR * handStyle.minuteHandLength
    )
    val hourTip = Offset(
      center.x + cos(hourRad).toFloat() * dialR * handStyle.hourHandLength,
      center.y + sin(hourRad).toFloat() * dialR * handStyle.hourHandLength
    )
    val dM = ClockMath.distanceToSegment(
      position.x, position.y, center.x, center.y, minuteTip.x, minuteTip.y
    )
    val dH = ClockMath.distanceToSegment(
      position.x, position.y, center.x, center.y, hourTip.x, hourTip.y
    )
    val touchDist = (position - center).getDistance()
    return when {
      // линии почти совпали (стрелки наложились) — решаем по радиусу:
      // дальше кончика часовой это уже зона минутной
      abs(dM - dH) <= 2f ->
        if (touchDist > dialR * handStyle.hourHandLength) Hand.Minute else Hand.Hour

      dM < dH -> Hand.Minute
      else -> Hand.Hour
    }
  }

  val h = minutes / 60
  val m = minutes % 60

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (config.showTimeText) {
      Text(
        text = "%02d:%02d".format(h, m),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = colors.timeTextColor
      )
      Spacer(Modifier.height(12.dp))
    }

    val minuteHandColor = colors.minuteHandColor ?: MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    // Внешние минутные цифры рисуются на (r + numbersGap). Резервируем под них место
    // (зазор + половина высоты цифры + запас), иначе их режет край Canvas.
    // Один и тот же радиус используется при отрисовке и при выборе стрелки.
    val density = LocalDensity.current
    val numbersGap = 35f
    val minuteNumberHeightPx = remember(textStyle.minuteTextSize, density, textMeasurer) {
      textMeasurer.measure(
        "60",
        TextStyle(
          fontSize = with(density) { textStyle.minuteTextSize.toSp() },
          fontWeight = FontWeight.Bold
        )
      ).size.height
    }
    fun dialRadiusPx(w: Float, h: Float): Float =
      min(w, h) / 2f - (numbersGap + minuteNumberHeightPx / 2f + 4f)

    Box(modifier = Modifier.size(config.radius * 2)) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(updateTime, minutes, snapEnabled) {
            val canvasSize = Size(size.width.toFloat(), size.height.toFloat())

            awaitPointerEventScope {
              while (true) {
                val event = awaitPointerEvent()
                val pressedPointer = event.changes.find { it.pressed }
                if (pressedPointer != null) {
                  val center = Offset(size.width / 2f, size.height / 2f)
                  val r = dialRadiusPx(size.width.toFloat(), size.height.toFloat())

                  val hand = selectedHand
                    ?: pickHand(pressedPointer.position, center, r).also { selectedHand = it }

                  when (hand) {
                    Hand.Minute -> {
                      val newMinutes = minuteAt(pressedPointer.position, canvasSize)
                      val totalMinutes = (minutes / 60) * 60 + newMinutes
                      updateTime(totalMinutes)
                    }

                    Hand.Hour -> {
                      val newHour = hour24At(pressedPointer.position, canvasSize)
                      val totalMinutes = newHour * 60 + (minutes % 60)
                      updateTime(totalMinutes)
                    }
                  }
                  pressedPointer.consume()
                } else {
                  selectedHand = null
                }
              }
            }
          }
      ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = dialRadiusPx(size.width, size.height)
        val isPM = minutes >= 720

        // Фон циферблата
        when (val bg = config.background) {
          DialBackground.None -> Unit
          is DialBackground.Solid -> drawCircle(color = bg.color, radius = r)
          is DialBackground.Gradient -> drawCircle(brush = bg.brush, radius = r)
          is DialBackground.Image -> {
            val d = r * 2
            clipPath(Path().apply {
              addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r))
            }) {
              translate(center.x - r, center.y - r) {
                with(bg.painter) { draw(Size(d, d), alpha = bg.alpha) }
              }
            }
          }
        }
        // Обводка циферблата
        drawCircle(
          color = colors.dialStroke,
          radius = r,
          style = Stroke(width = 2f)
        )

        // Рисуем деления
        for (i in 0 until 60) {
          val a = Math.toRadians((i * 6 - 90).toDouble())
          val outer = Offset(center.x + cos(a).toFloat() * r, center.y + sin(a).toFloat() * r)
          val inner =
            Offset(
              center.x + cos(a).toFloat() * (r - 8f),
              center.y + sin(a).toFloat() * (r - 8f)
            )
          drawLine(
            color = if (i % 5 == 0) colors.majorDivisionColor else colors.divisionColor,
            start = inner,
            end = outer,
            strokeWidth = if (i % 5 == 0) 3f else 1.5f,
            cap = StrokeCap.Round
          )
        }

        for (i in 0 until 12) {
          val angle = Math.toRadians((i * 30 - 90).toDouble())

          // 1. Минутные цифры (снаружи). На позиции 0 это «60».
          val minuteLabelValue = if (i == 0) 60 else i * 5
          val textRadius = r + numbersGap
          val textPosition = Offset(
            center.x + cos(angle).toFloat() * textRadius,
            center.y + sin(angle).toFloat() * textRadius
          )

          // 2. Часовые цифры текущей половины (средний круг)
          val currentHourValue = if (isPM) i + 12 else i
          val currentHourRadius = r - 125f
          val currentHourPosition = Offset(
            center.x + cos(angle).toFloat() * currentHourRadius,
            center.y + sin(angle).toFloat() * currentHourRadius
          )

          // 3. Часовые цифры противоположной половины (внутри)
          val oppositeHourValue = if (!isPM) i + 12 else i
          val oppositeHourRadius = r - 190f
          val oppositeHourPosition = Offset(
            center.x + cos(angle).toFloat() * oppositeHourRadius,
            center.y + sin(angle).toFloat() * oppositeHourRadius
          )

          val numeralStyle = config.numeralStyle
          drawNumeral(
            textMeasurer, numeralStyle.cellFor(minuteLabelValue, i), textPosition,
            colors.minuteNumbersColor, textStyle.minuteTextSize, FontWeight.Bold
          )
          drawNumeral(
            textMeasurer, numeralStyle.cellFor(currentHourValue, i), currentHourPosition,
            colors.currentHourNumbersColor, textStyle.currentHourTextSize, FontWeight.Bold
          )
          drawNumeral(
            textMeasurer, numeralStyle.cellFor(oppositeHourValue, i), oppositeHourPosition,
            colors.oppositeHourNumbersColor, textStyle.oppositeHourTextSize
          )
        }

        // Рисуем минутную стрелку
        val minuteAngle = Math.toRadians((m * 6 - 90).toDouble())
        val minuteEnd = Offset(
          center.x + cos(minuteAngle).toFloat() * (r * handStyle.minuteHandLength),
          center.y + sin(minuteAngle).toFloat() * (r * handStyle.minuteHandLength)
        )
        drawLine(
          color = minuteHandColor,
          start = center,
          end = minuteEnd,
          strokeWidth = handStyle.minuteHandWidth,
          cap = StrokeCap.Round
        )

        // Рисуем часовую стрелку
        val currentHour = minutes / 60
        val hourAngle = Math.toRadians(((currentHour % 12) * 30 - 90).toDouble())
        val hourEnd = Offset(
          center.x + cos(hourAngle).toFloat() * (r * handStyle.hourHandLength),
          center.y + sin(hourAngle).toFloat() * (r * handStyle.hourHandLength)
        )

        drawLine(
          color = if (isPM) colors.hourHandColorPM else colors.hourHandColor,
          start = center,
          end = hourEnd,
          strokeWidth = handStyle.hourHandWidth,
          cap = StrokeCap.Round
        )

        // Рисуем центр
        drawCircle(color = colors.centerDotColor, radius = 5f, center = center)
      }
    }

    if (config.showSnapSwitch) {
      Spacer(Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("5 минут", color = colors.switchTextColor)
        Spacer(Modifier.width(8.dp))
        Switch(
          checked = snapEnabled,
          onCheckedChange = { isChecked ->
          snapEnabled = isChecked
          // При переключении сразу применяем новое правило к текущему времени
          val currentMinute = minutes % 60
          val adjustedMinute = if (isChecked) {
            ClockMath.floorTo5(currentMinute)  // Округляем до ближайших 5 минут
          } else {
            currentMinute  // Оставляем как есть
          }
          val totalMinutes = (minutes / 60) * 60 + adjustedMinute
          updateTime(totalMinutes)
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalogTimePickerDialog(
  initialTime: LocalTime = LocalTime.now(),
  onTimeSelected: (LocalTime) -> Unit,
  onDismiss: () -> Unit,
  title: String = "Выберите время",
  confirmButtonText: String = "Ok",
  dismissButtonText: String = "Отмена",
  config: TimePickerConfig = TimePickerConfig(radius = 200.dp)
) {
  var currentTime by remember { mutableStateOf(initialTime) }
  val typography = MaterialTheme.typography

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = title,
        style = typography.headlineSmall,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      AnalogTimePicker(
        time = currentTime,
        onTimeChange = { newTime -> currentTime = newTime },
        config = config
      )
    },
    confirmButton = {
      TextButton(onClick = { onTimeSelected(currentTime) }) { Text(confirmButtonText) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(dismissButtonText) }
    }
  )
}