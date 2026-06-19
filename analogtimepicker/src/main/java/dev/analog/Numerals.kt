package dev.analog

/** Что нарисовать в конкретной позиции циферблата. */
sealed interface NumeralCell {
  data class Text(val value: String) : NumeralCell
  data object Dot : NumeralCell
  data object Empty : NumeralCell
}

/**
 * Стратегия отрисовки меток циферблата.
 * Готовые варианты + лазейка [Custom] для произвольной логики.
 *
 * @see cellFor
 */
sealed interface NumeralStyle {
  /**
   * @param value число в этой позиции (час 0..23 или минута 5..60)
   * @param positionIndex индекс позиции 0..11 (0 — сверху, далее по часовой)
   */
  fun cellFor(value: Int, positionIndex: Int): NumeralCell

  /** Арабские (западные) цифры: 1, 2, 3 … */
  data object Arabic : NumeralStyle {
    override fun cellFor(value: Int, positionIndex: Int) = NumeralCell.Text(value.toString())
  }

  /** Римские: I, II … XII. Для значений вне 1..3999 — пусто. */
  data object Roman : NumeralStyle {
    override fun cellFor(value: Int, positionIndex: Int) =
      NumeralFormatters.toRoman(value)?.let { NumeralCell.Text(it) } ?: NumeralCell.Empty
  }

  /** Арабо-индийские цифры: ٠ ١ ٢ … */
  data object ArabicIndic : NumeralStyle {
    override fun cellFor(value: Int, positionIndex: Int) =
      NumeralCell.Text(NumeralFormatters.toArabicIndic(value))
  }

  /** Точки вместо цифр во всех позициях. */
  data object Dots : NumeralStyle {
    override fun cellFor(value: Int, positionIndex: Int) = NumeralCell.Dot
  }

  /** Без меток. */
  data object None : NumeralStyle {
    override fun cellFor(value: Int, positionIndex: Int) = NumeralCell.Empty
  }

  /**
   * Метки только на четвертях (позиции 0/3/6/9 — это 12/3/6/9 часов).
   * На четвертях рисуется [base], в остальных позициях — [off].
   */
  data class QuartersOnly(
    val base: NumeralStyle = Arabic,
    val off: NumeralCell = NumeralCell.Dot
  ) : NumeralStyle {
    override fun cellFor(value: Int, positionIndex: Int) =
      if (positionIndex % 3 == 0) base.cellFor(value, positionIndex) else off
  }

  /** Произвольная стратегия. */
  class Custom(
    private val block: (value: Int, positionIndex: Int) -> NumeralCell
  ) : NumeralStyle {
    override fun cellFor(value: Int, positionIndex: Int) = block(value, positionIndex)
  }
}

/** Чистые (без UI) преобразования чисел в разные системы записи. */
internal object NumeralFormatters {

  private val romanValues = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
  private val romanSymbols =
    arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")

  /** Римская запись для 1..3999, иначе null (у римлян не было нуля). */
  fun toRoman(n: Int): String? {
    if (n <= 0 || n >= 4000) return null
    var rest = n
    val sb = StringBuilder()
    for (i in romanValues.indices) {
      while (rest >= romanValues[i]) {
        sb.append(romanSymbols[i])
        rest -= romanValues[i]
      }
    }
    return sb.toString()
  }

  /** Арабо-индийские цифры (U+0660..U+0669). */
  fun toArabicIndic(n: Int): String =
    n.toString().map { c -> if (c in '0'..'9') '٠' + (c - '0') else c }.joinToString("")
}
