package com.spanishapp

import com.spanishapp.ui.games.guessArticle
import com.spanishapp.ui.games.stripArticle
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticlesGameTest {

    @Test
    fun testStripArticle() {
        assertEquals("casa", stripArticle("la casa"))
        assertEquals("libro", stripArticle("el libro"))
        assertEquals("manzana", stripArticle("una manzana"))
        assertEquals("perro", stripArticle("un perro"))
        assertEquals("flores", stripArticle("las flores"))
        assertEquals("gatos", stripArticle("los gatos"))
        assertEquals("agua", stripArticle("agua"))
    }

    @Test
    fun testGuessArticle() {
        assertEquals("la", guessArticle("casa"))     // ends in 'a'
        assertEquals("el", guessArticle("libro"))    // ends in 'o'
        assertEquals("la", guessArticle("nación"))   // ends in 'ión'
        assertEquals("la", guessArticle("ciudad"))   // ends in 'dad'
        assertEquals("el", guessArticle("día"))      // exception
        assertEquals("el", guessArticle("problema")) // exception 'ma'
        assertEquals("el", guessArticle("color"))    // ends in 'or'
        assertEquals("el", guessArticle("viaje"))    // ends in 'aje'
    }
}
