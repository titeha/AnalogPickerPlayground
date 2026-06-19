package dev.analog

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

/** Заливка круга циферблата. */
sealed interface DialBackground {
  /** Без заливки (прозрачный циферблат). */
  data object None : DialBackground

  /** Сплошной цвет. */
  data class Solid(val color: Color) : DialBackground

  /** Градиент (любая кисть Compose). */
  data class Gradient(val brush: Brush) : DialBackground

  /** Картинка, обрезанная по кругу циферблата. */
  data class Image(val painter: Painter, val alpha: Float = 1f) : DialBackground
}
