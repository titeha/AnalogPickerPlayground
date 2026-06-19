package dev.analog

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockMathTest {

  // --- clockAngleDegrees: 0° наверху, по часовой ---

  @Test
  fun angle_up_is_0() {
    assertEquals(0f, ClockMath.clockAngleDegrees(0f, -10f), 0.001f)
  }

  @Test
  fun angle_right_is_90() {
    assertEquals(90f, ClockMath.clockAngleDegrees(10f, 0f), 0.001f)
  }

  @Test
  fun angle_down_is_180() {
    assertEquals(180f, ClockMath.clockAngleDegrees(0f, 10f), 0.001f)
  }

  @Test
  fun angle_left_is_270() {
    assertEquals(270f, ClockMath.clockAngleDegrees(-10f, 0f), 0.001f)
  }

  // --- minuteFromAngle ---

  @Test
  fun minute_at_cardinal_angles() {
    assertEquals(0, ClockMath.minuteFromAngle(0f, snap = false))
    assertEquals(15, ClockMath.minuteFromAngle(90f, snap = false))
    assertEquals(30, ClockMath.minuteFromAngle(180f, snap = false))
    assertEquals(45, ClockMath.minuteFromAngle(270f, snap = false))
  }

  @Test
  fun minute_snap_rounds_to_nearest_5() {
    // 90° + чуть-чуть ≈ 16-я минута -> при snap округляется к 15
    val angle = (16f / 60f) * 360f
    assertEquals(16, ClockMath.minuteFromAngle(angle, snap = false))
    assertEquals(15, ClockMath.minuteFromAngle(angle, snap = true))
  }

  @Test
  fun minute_snap_wraps_60_to_0() {
    // угол, дающий ~59.x -> snap округляет к 60, что должно стать 0
    val angle = (58f / 60f) * 360f
    assertEquals(0, ClockMath.minuteFromAngle(angle, snap = true))
  }

  // --- hour12FromAngle ---

  @Test
  fun hour12_at_cardinal_angles() {
    assertEquals(0, ClockMath.hour12FromAngle(0f))
    assertEquals(3, ClockMath.hour12FromAngle(90f))
    assertEquals(6, ClockMath.hour12FromAngle(180f))
    assertEquals(9, ClockMath.hour12FromAngle(270f))
  }

  // --- to24Hour ---

  @Test
  fun to24_am_is_unchanged() {
    assertEquals(0, ClockMath.to24Hour(0, pm = false))
    assertEquals(7, ClockMath.to24Hour(7, pm = false))
    assertEquals(11, ClockMath.to24Hour(11, pm = false))
  }

  @Test
  fun to24_pm_adds_12() {
    assertEquals(12, ClockMath.to24Hour(0, pm = true)) // полдень
    assertEquals(19, ClockMath.to24Hour(7, pm = true))
    assertEquals(23, ClockMath.to24Hour(11, pm = true))
  }

  // --- nextPmOnHourDrag: переключение половины суток через 12 ---

  @Test
  fun pm_toggles_when_crossing_11_to_0_forward() {
    // 11:00 (AM), тянем стрелку на 12 (hour12 == 0) -> становится PM
    assertEquals(true, ClockMath.nextPmOnHourDrag(currentMinutes = 11 * 60, newHour12 = 0))
  }

  @Test
  fun pm_toggles_when_crossing_0_to_11_backward() {
    // 12:00 (PM, 720 мин), тянем назад на 11 (hour12 == 11) -> становится AM
    assertEquals(false, ClockMath.nextPmOnHourDrag(currentMinutes = 720, newHour12 = 11))
  }

  @Test
  fun pm_unchanged_without_crossing() {
    // 03:00 AM -> 04:00, без перехода через 12
    assertEquals(false, ClockMath.nextPmOnHourDrag(currentMinutes = 3 * 60, newHour12 = 4))
    // 15:00 PM -> 16:00
    assertEquals(true, ClockMath.nextPmOnHourDrag(currentMinutes = 15 * 60, newHour12 = 4))
  }

  // --- floorTo5 ---

  @Test
  fun floorTo5_rounds_down() {
    assertEquals(0, ClockMath.floorTo5(4))
    assertEquals(5, ClockMath.floorTo5(5))
    assertEquals(10, ClockMath.floorTo5(13))
    assertEquals(55, ClockMath.floorTo5(59))
  }

  // --- distanceToSegment ---

  @Test
  fun distance_point_on_segment_is_0() {
    assertEquals(0f, ClockMath.distanceToSegment(5f, 0f, 0f, 0f, 10f, 0f), 0.001f)
  }

  @Test
  fun distance_perpendicular_offset() {
    assertEquals(3f, ClockMath.distanceToSegment(5f, 3f, 0f, 0f, 10f, 0f), 0.001f)
  }

  @Test
  fun distance_beyond_end_uses_endpoint() {
    // точка за концом отрезка — ближайшая точка это конец (10,0)
    assertEquals(5f, ClockMath.distanceToSegment(15f, 0f, 0f, 0f, 10f, 0f), 0.001f)
  }

  @Test
  fun distance_before_start_uses_startpoint() {
    assertEquals(4f, ClockMath.distanceToSegment(-4f, 0f, 0f, 0f, 10f, 0f), 0.001f)
  }

  @Test
  fun distance_degenerate_segment_is_point_distance() {
    assertEquals(5f, ClockMath.distanceToSegment(3f, 4f, 0f, 0f, 0f, 0f), 0.001f)
  }
}
