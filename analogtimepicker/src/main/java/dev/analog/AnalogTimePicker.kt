package dev.analog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

  var snapEnabled by remember { mutableStateOf(snapTo5Minutes) }

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
          .pointerInput(updateTime, minutes, snapEnabled) {
            val canvasSize = Size(size.width.toFloat(), size.height.toFloat())

            awaitPointerEventScope {
              while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { change ->
                  if (change.pressed) {
                    val newMinutes = calculateMinutes(change.position, canvasSize)
                    val totalMinutes = (minutes / 60) * 60 + newMinutes
                    updateTime(totalMinutes)
                    change.consume()
                  }
                }
              }
            }
          }
      ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = min(size.width, size.height) / 2f

        // Рисуем циферблат
        drawCircle(color = Color.Gray.copy(alpha = 0.12f), radius = r)
        drawCircle(color = Color.Gray.copy(alpha = 0.32f), radius = r, style = Stroke(width = 2f))

        // Рисуем деления
        for (i in 0 until 60) {
          val a = Math.toRadians((i * 6 - 90).toDouble())
          val outer = Offset(center.x + cos(a).toFloat() * r, center.y + sin(a).toFloat() * r)
          val inner =
            Offset(center.x + cos(a).toFloat() * (r - 8f), center.y + sin(a).toFloat() * (r - 8f))
          drawLine(
            color = if (i % 5 == 0) Color.DarkGray else Color.Gray,
            start = inner,
            end = outer,
            strokeWidth = if (i % 5 == 0) 3f else 1.5f,
            cap = StrokeCap.Round
          )
        }

        for (i in 0 until 12) {
          val minuteValue = i * 5
          val angle = Math.toRadians((i * 30 - 90).toDouble())

          // Позиция цифры (немного дальше от центра чем деления)
          val textRadius = r + 25f
          val textPosition = Offset(
            center.x + cos(angle).toFloat() * textRadius,
            center.y + sin(angle).toFloat() * textRadius
          )

          val minuteText = if (minuteValue == 0) "60" else minuteValue.toString()

          drawContext.canvas.nativeCanvas.apply {
            drawText(
              minuteText,
              textPosition.x - 10f,
              textPosition.y + 5f,
              android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 60f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
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