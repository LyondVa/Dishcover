package com.nhatpham.dishcover.util

object ValidationUtils {

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
    }

    fun validatePassword(password: String): Boolean {
        // At least 8 characters, 1 uppercase, 1 lowercase, 1 number
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+\$).{8,}\$"
        return password.matches(passwordRegex.toRegex())
    }

    fun validateUsername(username: String): Boolean {
        // 3-20 characters, letters, numbers, underscores, no spaces
        val usernameRegex = "^[A-Za-z0-9_]{3,20}\$"
        return username.matches(usernameRegex.toRegex())
    }
}