package com.spanishapp

import com.spanishapp.ui.games.NumberToSpanish
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberToSpanishTest {

    @Test fun zero() = assertEquals("cero", NumberToSpanish.convert(0))
    @Test fun one() = assertEquals("uno", NumberToSpanish.convert(1))
    @Test fun nine() = assertEquals("nueve", NumberToSpanish.convert(9))
    @Test fun ten() = assertEquals("diez", NumberToSpanish.convert(10))
    @Test fun fifteen() = assertEquals("quince", NumberToSpanish.convert(15))
    @Test fun nineteen() = assertEquals("diecinueve", NumberToSpanish.convert(19))
    @Test fun twenty() = assertEquals("veinte", NumberToSpanish.convert(20))
    @Test fun twentyOne() = assertEquals("veintiuno", NumberToSpanish.convert(21))
    @Test fun twentyNine() = assertEquals("veintinueve", NumberToSpanish.convert(29))
    @Test fun thirty() = assertEquals("treinta", NumberToSpanish.convert(30))

    /** Регрессионный тест: раньше возвращалось «treinta y 1». */
    @Test fun thirtyOne() = assertEquals("treinta y uno", NumberToSpanish.convert(31))
    @Test fun fortyFive() = assertEquals("cuarenta y cinco", NumberToSpanish.convert(45))
    @Test fun fiftyOne() = assertEquals("cincuenta y uno", NumberToSpanish.convert(51))
    @Test fun seventy() = assertEquals("setenta", NumberToSpanish.convert(70))
    @Test fun ninetyNine() = assertEquals("noventa y nueve", NumberToSpanish.convert(99))

    @Test fun hundred() = assertEquals("cien", NumberToSpanish.convert(100))
    @Test fun hundredOne() = assertEquals("ciento uno", NumberToSpanish.convert(101))
    @Test fun hundredFifteen() = assertEquals("ciento quince", NumberToSpanish.convert(115))
    @Test fun twoHundred() = assertEquals("doscientos", NumberToSpanish.convert(200))
    @Test fun fiveHundred() = assertEquals("quinientos", NumberToSpanish.convert(500))
    @Test fun sevenHundredFortySix() = assertEquals("setecientos cuarenta y seis", NumberToSpanish.convert(746))
    @Test fun nineHundredNinetyNine() = assertEquals("novecientos noventa y nueve", NumberToSpanish.convert(999))

    @Test fun negative() = assertEquals("menos cinco", NumberToSpanish.convert(-5))
}
