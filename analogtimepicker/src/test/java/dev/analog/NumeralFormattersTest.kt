package dev.analog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumeralFormattersTest {

  @Test
  fun roman_basic_values() {
    assertEquals("I", NumeralFormatters.toRoman(1))
    assertEquals("IV", NumeralFormatters.toRoman(4))
    assertEquals("IX", NumeralFormatters.toRoman(9))
    assertEquals("XII", NumeralFormatters.toRoman(12))
    assertEquals("XXIII", NumeralFormatters.toRoman(23))
    assertEquals("LX", NumeralFormatters.toRoman(60))
  }

  @Test
  fun roman_zero_and_negative_are_null() {
    assertNull(NumeralFormatters.toRoman(0))
    assertNull(NumeralFormatters.toRoman(-5))
  }

  @Test
  fun arabic_indic_digits() {
    assertEquals("٠", NumeralFormatters.toArabicIndic(0))
    assertEquals("٥", NumeralFormatters.toArabicIndic(5))
    assertEquals("١٢", NumeralFormatters.toArabicIndic(12))
    assertEquals("٦٠", NumeralFormatters.toArabicIndic(60))
  }
}
