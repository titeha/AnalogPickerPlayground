package dev.analog

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*
import java.time.LocalTime

/**
 * Minimal, reusable analog time picker for Compose.
 *
 * Features:
 * - Minute snapping to 5 min (toggleable)
 * - 12h/24h modes; PM via 2nd rotation of the hour hand (0..720°)
 * - Drag either hand; whichever is closer to touch takes focus
 * - Live preview string on top
 * - A11y labels & semantics
 */
@Composable
fun AnalogTimePicker(
  modifier: Modifier = Modifier,
  time: LocalTime = LocalTime.now(),
  is24h: Boolean = true,
  snapMinutes5: Boolean = true,
  onTimeChange: (LocalTime) -> Unit,
  radius: Dp = 140.dp
) {
  // Internal state is kept in minutes since midnight, but we allow hour-hand double turn (AM/PM in 12h mode)
  var minutes by remember(time) { mutableStateOf(time.hour * 60 + time.minute) }

  // Track hour-hand additional revolution in 12h mode
  var hourTurns by remember { mutableStateOf( if (is24h) 0 else if (time.hour >= 12) 1 else 0) }

  val density = LocalDensity.current
  val rPx = with(density) { radius.toPx() }

  fun commit(newMinutes: Int) {
    val total = ((newMinutes % (24*60)) + (24*60)) % (24*60)
    minutes = total
    val h = total / 60
    val m = total % 60
    onTimeChange(LocalTime.of(h, m))
  }

  // Geometry helpers
  fun angleToMinute(angle: Float): Int {
    // 0° at 12 o'clock, increasing clockwise; Compose gives radians from +X; convert
    val a = ((90f - angle).mod(360f) + 360f).mod(360f)
    val rawMin = (a / 360f) * 60f
    val m = if (snapMinutes5) (round(rawMin / 5f) * 5f).toInt() % 60 else round(rawMin).toInt() % 60
    return m
  }

  fun angleToHour(angle: Float, currentMinutes: Int): Pair<Int, Int /*turns*/> {
    val a = ((90f - angle).mod(360f) + 360f).mod(360f) // 0..360
    // Determine rough hour from angle, allow double turn in 12h mode
    val rawHour12 = (a / 360f) * 12f // 0..12
    val hourIndex = floor(rawHour12).toInt().mod(12)
    val minutePart = ((rawHour12 - floor(rawHour12)) * 60f).roundToInt() % 60
    var turns = hourTurns
    // Heuristic: if hour hand crosses from near 11 to 0 while dragging clockwise, increment turns, and vice versa
    // We infer cross via delta between previous hour and new hour
    val prevHour12 = ((currentMinutes/60) % 12)
    val delta = ((hourIndex - prevHour12 + 12) % 12)
    if (!is24h) {
      if (delta in 7..11 && hourIndex < 2 && prevHour12 > 9) turns =  (hourTurns + 1).coerceAtMost(1)
      if (delta in 1..5 && hourIndex > 9 && prevHour12 < 2) turns =  (hourTurns - 1).coerceAtLeast(0)
    }
    return (hourIndex to turns).also { hourTurns = turns }
  }

  fun minuteToAngle(min: Int): Float = (90f - min * 6f)
  fun hourToAngle(hour: Int, min: Int): Float {
    val h12 = (hour % 12) + (min / 60f)
    return (90f - h12 * 30f)
  }

  val h = minutes / 60
  val m = minutes % 60

  Column(
    modifier = modifier
      .semantics(mergeDescendants = true) {
        contentDescription = "Analog time picker"
        stateDescription = "%02d:%02d".format(h, m)
      },
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = if (is24h) "%02d:%02d".format(h, m) else {
        val h12 = ((h + 11) % 12) + 1
        val ampm = if (h >= 12) "PM" else "AM"
        "%02d:%02d %s".format(h12, m, ampm)
      },
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(12.dp))

    Box(modifier = Modifier.size(radius * 2)) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(is24h, snapMinutes5, minutes) {
            detectDragGestures { change, _ ->
              val center = Offset(size.width/2f, size.height/2f)
              val v = change.position - center
              val angle = atan2(v.y, v.x) * 180f / Math.PI.toFloat() // -180..180
              // Decide which hand is closer
              val angMin = minuteToAngle(m)
              val angHour = hourToAngle(h, m)
              fun normDiff(a: Float, b: Float): Float {
                val d = abs(((a - b + 540f) % 360f) - 180f)
                return d
              }
              val dMin = normDiff(angle, angMin)
              val dHour = normDiff(angle, angHour)
              if (dMin <= dHour) {
                val newM = angleToMinute(angle)
                commit(h * 60 + newM)
              } else {
                val (h12, turns) = angleToHour(angle, minutes)
                val newHour = if (is24h) {
                  // Map 0..11 to 0..23 choosing closest of current half-day
                  val base = (h / 12) * 12
                  val cand1 = base + h12
                  val cand2 = ((base + 12) % 24) + h12
                  if (abs(cand1 - h) <= abs(cand2 - h)) cand1 else cand2
                } else {
                  h12 + turns * 12
                }
                commit(newHour * 60 + m)
              }
            }
          }
      ) {
        val center = Offset(size.width/2f, size.height/2f)
        val r = min(size.width, size.height) / 2f
        // dial
        drawCircle(color = Color.Gray.copy(alpha = 0.12f), radius = r)
        drawCircle(color = Color.Gray.copy(alpha = 0.32f), radius = r, style = Stroke(width = 2f))
        // ticks (5-min)
        for (i in 0 until 60) {
          val a = Math.toRadians((i * 6 - 90).toDouble())
          val outer = Offset(center.x + cos(a).toFloat()*r, center.y + sin(a).toFloat()*r)
          val inner = Offset(center.x + cos(a).toFloat()*(r - if (i%5==0) 18f else 8f), center.y + sin(a).toFloat()*(r - if (i%5==0) 18f else 8f))
          drawLine(color = if (i%5==0) Color.DarkGray else Color.Gray, start = inner, end = outer, strokeWidth = if (i%5==0) 3f else 1.5f, cap = StrokeCap.Round)
        }
        // minute hand
        val am = Math.toRadians((m*6 - 90).toDouble())
        val mEnd = Offset(center.x + cos(am).toFloat()*(r*0.86f), center.y + sin(am).toFloat()*(r*0.86f))
        drawLine(color = MaterialTheme.colorScheme.primary, start = center, end = mEnd, strokeWidth = 6f, cap = StrokeCap.Round)
        // hour hand
        val ah = Math.toRadians(((h%12 + m/60f)*30 - 90).toDouble())
        val hEnd = Offset(center.x + cos(ah).toFloat()*(r*0.6f), center.y + sin(ah).toFloat()*(r*0.6f))
        drawLine(color = MaterialTheme.colorScheme.secondary, start = center, end = hEnd, strokeWidth = 8f, cap = StrokeCap.Round)
        // hub
        drawCircle(color = Color.Black.copy(alpha = 0.6f), radius = 5f, center = center)
      }
    }

    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Snap 5m")
      var snap by remember { mutableStateOf(snapMinutes5) }
      Switch(checked = snap, onCheckedChange = {
        snap = it
        // When toggled off, keep current minute as-is; when on — snap to nearest 5
        if (it) commit(h*60 + ((m+2)/5)*5 % 60) // light snap
      })
      Spacer(Modifier.width(16.dp))
      Text(if (is24h) "24h" else "12h")
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalogTimePickerDialog(
  initial: LocalTime = LocalTime.now(),
  is24h: Boolean = true,
  onDismiss: () -> Unit,
  onConfirm: (LocalTime) -> Unit
) {
  var value by remember { mutableStateOf(initial) }
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = { onConfirm(value) }) { Text("OK") }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    text = {
      AnalogTimePicker(
        time = value,
        is24h = is24h,
        onTimeChange = { value = it }
      )
    }
  )
}

// --- End of file ---
