package com.spanishapp.util

import org.junit.Assert.*
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `isValidEmail returns true for valid emails`() {
        assertTrue(AuthValidator.isValidEmail("test@example.com"))
        assertTrue(AuthValidator.isValidEmail("user.name@domain.co"))
    }

    @Test
    fun `isValidEmail returns false for invalid emails`() {
        assertFalse(AuthValidator.isValidEmail("plainaddress"))
        assertFalse(AuthValidator.isValidEmail("@missingusername.com"))
        assertFalse(AuthValidator.isValidEmail("username@.com"))
        assertFalse(AuthValidator.isValidEmail(""))
    }

    @Test
    fun `isValidPassword returns true for strong passwords`() {
        // At least 8 chars, 1 letter, 1 digit
        assertTrue(AuthValidator.isValidPassword("password123"))
        assertTrue(AuthValidator.isValidPassword("A1b2c3d4"))
    }

    @Test
    fun `isValidPassword returns false for weak passwords`() {
        assertFalse(AuthValidator.isValidPassword("short1")) // Too short
        assertFalse(AuthValidator.isValidPassword("onlyletters")) // No digits
        assertFalse(AuthValidator.isValidPassword("12345678")) // No letters
        assertFalse(AuthValidator.isValidPassword(""))
    }

    @Test
    fun `getEmailError returns correct messages`() {
        assertEquals("Email не может быть пустым", AuthValidator.getEmailError(""))
        assertEquals("Неверный формат email", AuthValidator.getEmailError("invalid-email"))
        assertNull(AuthValidator.getEmailError("valid@mail.com"))
    }

    @Test
    fun `getPasswordError returns correct messages`() {
        assertEquals("Пароль не может быть пустым", AuthValidator.getPasswordError(""))
        assertEquals("Минимум 8 символов", AuthValidator.getPasswordError("123"))
        assertEquals("Нужна хотя бы одна цифра", AuthValidator.getPasswordError("onlyletters"))
        assertEquals("Нужна хотя бы одна буква", AuthValidator.getPasswordError("12345678"))
        assertNull(AuthValidator.getPasswordError("validPass1"))
    }
}
