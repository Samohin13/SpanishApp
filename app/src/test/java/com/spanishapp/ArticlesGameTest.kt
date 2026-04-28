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
        assertEquals("habitaciones", stripArticle("unas habitaciones"))
    }

    @Test
    fun testGuessArticle() {
        // Стандартные окончания
        assertEquals("la", guessArticle("casa"))     // ends in 'a'
        assertEquals("el", guessArticle("libro"))    // ends in 'o'
        assertEquals("la", guessArticle("nación"))   // ends in 'ión'
        assertEquals("la", guessArticle("ciudad"))   // ends in 'dad'
        assertEquals("el", guessArticle("color"))    // ends in 'or'
        assertEquals("el", guessArticle("viaje"))    // ends in 'aje'
        assertEquals("el", guessArticle("hospital")) // ends in 'al'
        
        // Исключения (мужской род на -а)
        assertEquals("el", guessArticle("día"))
        assertEquals("el", guessArticle("mapa"))
        assertEquals("el", guessArticle("idioma"))
        assertEquals("el", guessArticle("problema"))
        assertEquals("el", guessArticle("tema"))
        assertEquals("el", guessArticle("sistema"))
        assertEquals("el", guessArticle("clima"))
        assertEquals("el", guessArticle("planeta"))
        
        // Женский род (другие окончания)
        assertEquals("la", guessArticle("libertad"))
        assertEquals("la", guessArticle("actitud"))
        assertEquals("la", guessArticle("costumbre"))
    }
    
    @Test
    fun testGuessArticleWithStripping() {
        // Проверка что функция работает даже если передать слово с артиклем (хотя игра стрипает сама)
        assertEquals("la", guessArticle(stripArticle("una manzana")))
        assertEquals("el", guessArticle(stripArticle("el problema")))
    }
}
