package dev.analog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter

/**
 * Внешний вид стрелки. [lengthFraction] — длина как доля радиуса циферблата (0..1).
 */
sealed interface HandShape {
  val lengthFraction: Float

  /**
   * Стрелка-линия.
   * @param color цвет; null — взять primary из MaterialTheme.
   * @param colorPm если задан — цвет во второй половине суток (PM); иначе используется [color].
   * @param widthPx толщина в px канвы.
   */
  data class Line(
    val color: Color? = null,
    val colorPm: Color? = null,
    val widthPx: Float = 8f,
    override val lengthFraction: Float = 0.86f,
    val cap: StrokeCap = StrokeCap.Round
  ) : HandShape

  /**
   * Стрелка-картинка. Изображение должно «смотреть вверх» (на 12 часов);
   * рисуется от центра вверх и поворачивается под нужный угол.
   * @param widthPx ширина картинки в px канвы (длина = [lengthFraction] * радиус).
   */
  data class Image(
    val painter: Painter,
    val widthPx: Float,
    override val lengthFraction: Float
  ) : HandShape
}
