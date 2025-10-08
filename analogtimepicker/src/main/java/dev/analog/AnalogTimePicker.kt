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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class Hand { Minute, Hour }

@Composable
fun AnalogTimePicker(
  modifier: Modifier = Modifier,
  time: LocalTime = LocalTime.now(),
  onTimeChange: (LocalTime) -> Unit,
  radius: Dp = 160.dp,
  snapTo5Minutes: Boolean = true
) {
  var minutes by remember(time) {
    mutableStateOf(time.hour * 60 + time.minute)
  }

  var isPM by remember(time) {
    mutableStateOf(time.hour >= 12)
  }

  var snapEnabled by remember { mutableStateOf(snapTo5Minutes) }

  var selectedHand by remember { mutableStateOf<Hand?>(null) }

  val updateTime: (Int) -> Unit = { newMinutes ->
    minutes = newMinutes
    val h = newMinutes / 60
    val m = newMinutes % 60
    onTimeChange(LocalTime.of(h, m))
  }

  fun calculateMinutes(position: Offset, size: Size): Int {
    val center = Offset(size.width / 2f, size.height / 2f)
    val dx = position.x - center.x
    val dy = position.y - center.y

    val angleRad = atan2(dy, dx)
    val angleDeg = angleRad * 180f / PI.toFloat()
    var clockAngle = (angleDeg + 90f) % 360f
    if (clockAngle < 0) clockAngle += 360f

    val rawMinutes = (clockAngle / 360f * 60f)
    return if (snapEnabled) {
      (rawMinutes / 5f).roundToInt() * 5 % 60
    } else {
      rawMinutes.roundToInt() % 60
    }
  }

  fun calculateHours(position: Offset, size: Size): Pair<Int, Boolean> {
    val center = Offset(size.width / 2f, size.height / 2f)
    val dx = position.x - center.x
    val dy = position.y - center.y

    val angleRad = atan2(dy, dx)
    val angleDeg = angleRad * 180f / PI.toFloat()
    var clockAngle = (angleDeg + 90f) % 360f
    if (clockAngle < 0) clockAngle += 360f

    // 360° = 12 часов
    val rawHour = (clockAngle / 360f * 12f)
    val hour12 = rawHour.roundToInt() % 12

    // Определяем переключение половины суток
    var newIsPM = isPM

    val currentHour12 = (minutes / 60) % 12
    if (currentHour12 == 11 && hour12 == 0) {
      newIsPM = !isPM
    } else if (currentHour12 == 0 && hour12 == 11) {
      newIsPM = !isPM
    }

    // Преобразуем в 24-часовой формат
    var hour24 = if (newIsPM) hour12 + 12 else hour12
    if (hour24 == 24) hour24 = 0

    return hour24 to newIsPM
  }

  fun pickHand(position: Offset, center: Offset, dialRadius: Float): Hand {
    val v = position - center
    val dist = v.getDistance()

    val angle = atan2(v.y, v.x) * 180f / PI.toFloat()
    var clockAngle = (angle + 90f) % 360f
    if (clockAngle < 0) clockAngle += 360f

    return if (dist > dialRadius * 0.85f) Hand.Minute else Hand.Hour
  }

  val density = LocalDensity.current
  val rPx = with(density) { radius.toPx() }

  val h = minutes / 60
  val m = minutes % 60

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "%02d:%02d".format(h, m),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = Color.Yellow
    )
    Spacer(Modifier.height(12.dp))

    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.size(radius * 2)) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(updateTime, minutes, snapEnabled, isPM) {
            val canvasSize = Size(size.width.toFloat(), size.height.toFloat())

            awaitPointerEventScope {
              while (true) {
                val event = awaitPointerEvent()
                val pressedPointer = event.changes.find { it.pressed }
                if (pressedPointer != null) {
                  val center = Offset(size.width / 2f, size.height / 2f)
                  val r = min(size.width, size.height) / 2f

                  if (selectedHand == null) {
                    selectedHand = pickHand(pressedPointer.position, center, r)
                  }

                  when (selectedHand) {
                    Hand.Minute -> {
                      val newMinutes = calculateMinutes(pressedPointer.position, canvasSize)
                      val totalMinutes = (minutes / 60) * 60 + newMinutes
                      updateTime(totalMinutes)
                    }

                    Hand.Hour -> {
                      val (newHour, newIsPM) = calculateHours(pressedPointer.position, canvasSize)
                      val totalMinutes = newHour * 60 + (minutes % 60)
                      isPM = newIsPM
                      updateTime(totalMinutes)
                    }

                    null -> Unit
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
        val r = min(size.width, size.height) / 2f

        // Рисуем циферблат
        drawCircle(color = Color.Gray.copy(alpha = 0.12f), radius = r)
        drawCircle(
          color = Color.Gray.copy(alpha = 0.32f),
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
            color = if (i % 5 == 0) Color.DarkGray else Color.Gray,
            start = inner,
            end = outer,
            strokeWidth = if (i % 5 == 0) 3f else 1.5f,
            cap = StrokeCap.Round
          )
        }

        for (i in 0 until 12) {
          val angle = Math.toRadians((i * 30 - 90).toDouble())

          // 1. Минутные цифры (снаружи)
          val minuteValue = i * 5
          val minuteText = if (minuteValue == 0) "60" else minuteValue.toString()
          // Позиция цифры (немного дальше от центра чем деления)
          val textRadius = r + 35f
          val textPosition = Offset(
            center.x + cos(angle).toFloat() * textRadius,
            center.y + sin(angle).toFloat() * textRadius
          )

          // 2. Часовые цифры текущей половины (средний круг)
          val currentHourValue = if (isPM) i + 12 else i
          val currentHourText = if (currentHourValue == 24) "0" else currentHourValue.toString()
          val currentHourRadius = r - 125f
          val currentHourPosition = Offset(
            center.x + cos(angle).toFloat() * currentHourRadius,
            center.y + sin(angle).toFloat() * currentHourRadius
          )

          // 3. Часовые цифры противоположной половины (внутри)
          val oppositeHourValue = if (!isPM) i + 12 else i
          val oppositeHourText =
            if (oppositeHourValue == 24) "0" else oppositeHourValue.toString()
          val oppositeHourRadius = r - 190f
          val oppositeHourPosition = Offset(
            center.x + cos(angle).toFloat() * oppositeHourRadius,
            center.y + sin(angle).toFloat() * oppositeHourRadius
          )

          drawContext.canvas.nativeCanvas.apply {
            // Рисуем минутные цифры
            drawText(
              minuteText,
              textPosition.x - 10f,
              textPosition.y + 5f,
              android.graphics.Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = 60f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
              }
            )

            // Рисуем текущие часовые цифры (крупно)
            drawText(
              currentHourText,
              currentHourPosition.x,
              currentHourPosition.y,
              android.graphics.Paint().apply {
                color = android.graphics.Color.LTGRAY
                textSize = 55f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
              })

            // Рисуем противоположные часовые цифры (мелко)
            drawText(
              oppositeHourText,
              oppositeHourPosition.x,
              oppositeHourPosition.y,
              android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 40f
                textAlign = android.graphics.Paint.Align.CENTER
              }
            )
          }
        }

        // Рисуем минутную стрелку
        val minuteAngle = Math.toRadians((m * 6 - 90).toDouble())
        val minuteEnd = Offset(
          center.x + cos(minuteAngle).toFloat() * (r * 0.86f),
          center.y + sin(minuteAngle).toFloat() * (r * 0.86f)
        )
        drawLine(
          color = primary,
          start = center,
          end = minuteEnd,
          strokeWidth = 8f,
          cap = StrokeCap.Round
        )

        // Рисуем часовую стрелку
        val currentHour = minutes / 60
        val hourAngle = Math.toRadians(((currentHour % 12) * 30 - 90).toDouble())
        val hourEnd = Offset(
          center.x + cos(hourAngle).toFloat() * (r * 0.5f),
          center.y + sin(hourAngle).toFloat() * (r * 0.5f)
        )

        val hourHandColor = if (isPM) Color(0xFFFF8000) else Color(0xFFFFFF00)

        drawLine(
          color = hourHandColor,
          start = center,
          end = hourEnd,
          strokeWidth = 14f,
          cap = StrokeCap.Round
        )

        // Рисуем центр
        drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = 5f, center = center)
      }
    }

    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("5 минут")
      Spacer(Modifier.width(8.dp))
      Switch(
        checked = snapEnabled,
        onCheckedChange = { isChecked ->
          snapEnabled = isChecked
          // При переключении сразу применяем новое правило к текущему времени
          val currentMinute = minutes % 60
          val adjustedMinute = if (isChecked) {
            (currentMinute / 5) * 5  // Округляем до ближайших 5 минут
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