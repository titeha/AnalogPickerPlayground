package dev.analog

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * Чистая (без UI и I/O) логика преобразований циферблата.
 * Вынесена отдельным «швом», чтобы покрыть юнит-тестами без запуска Compose.
 */
internal object ClockMath {

  /**
   * Угол точки (dx, dy) относительно центра в «часовых» градусах:
   * 0° — направление на 12 часов, рост по часовой стрелке, диапазон [0, 360).
   */
  fun clockAngleDegrees(dx: Float, dy: Float): Float {
    val deg = atan2(dy, dx) * 180f / PI.toFloat()
    var angle = (deg + 90f) % 360f
    if (angle < 0f) angle += 360f
    return angle
  }

  /** Минута [0..59] по углу. [snap] — округление до ближайших 5 минут. */
  fun minuteFromAngle(angleDeg: Float, snap: Boolean): Int {
    val raw = angleDeg / 360f * 60f
    return if (snap) (raw / 5f).roundToInt() * 5 % 60 else raw.roundToInt() % 60
  }

  /** Час 12-часового циферблата [0..11] по углу. */
  fun hour12FromAngle(angleDeg: Float): Int {
    val raw = angleDeg / 360f * 12f
    return raw.roundToInt() % 12
  }

  /** Перевод 12-часового значения в 24-часовое [0..23]. */
  fun to24Hour(hour12: Int, pm: Boolean): Int =
    if (pm) (hour12 % 12) + 12 else (hour12 % 12)

  /**
   * Новая половина суток (PM?) при перетаскивании часовой стрелки.
   * Источник истины — текущее время в минутах суток [0..1439]; отдельного
   * состояния isPM нет. Половина переключается при переходе через 12 (11↔0).
   */
  fun nextPmOnHourDrag(currentMinutes: Int, newHour12: Int): Boolean {
    val pm = currentMinutes >= 720
    val currentHour12 = (currentMinutes / 60) % 12
    return when {
      currentHour12 == 11 && newHour12 == 0 -> !pm
      currentHour12 == 0 && newHour12 == 11 -> !pm
      else -> pm
    }
  }

  /** Округление минуты до ближайших 5 вниз — для переключателя snap. */
  fun floorTo5(minute: Int): Int = (minute / 5) * 5
}
