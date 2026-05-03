package com.spanishapp.util

object AuthValidator {
    
    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    private val PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.matches(EMAIL_REGEX)
    }

    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.matches(PASSWORD_REGEX)
    }

    fun getEmailError(email: String): String? {
        return when {
            email.isBlank() -> "Email не может быть пустым"
            !email.matches(EMAIL_REGEX) -> "Неверный формат email"
            else -> null
        }
    }

    fun getPasswordError(password: String): String? {
        return when {
            password.isBlank() -> "Пароль не может быть пустым"
            password.length < 8 -> "Минимум 8 символов"
            !password.any { it.isDigit() } -> "Нужна хотя бы одна цифра"
            !password.any { it.isLetter() } -> "Нужна хотя бы одна буква"
            else -> null
        }
    }

    fun getNameError(name: String): String? {
        val namePattern = "^[a-zA-Zа-яА-Я0-9\\s]{2,20}$".toRegex()
        return when {
            name.isBlank() -> "Имя не может быть пустым"
            name.length < 2 -> "Слишком короткое имя"
            name.length > 20 -> "Максимум 20 символов"
            !name.matches(namePattern) -> "Используйте только буквы и цифры"
            else -> null
        }
    }
}
