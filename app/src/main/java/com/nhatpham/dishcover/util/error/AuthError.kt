package com.nhatpham.dishcover.util.error

/**
 * Authentication-specific errors that extend the base AppError hierarchy
 */
sealed class AuthError(
    override val message: String,
    override val cause: Throwable? = null
) : AppError.AuthError(message, cause) {

    /**
     * Error when email verification is required
     */
    class EmailVerificationRequiredError(
        override val message: String = "Email verification is required",
        override val cause: Throwable? = null
    ) : AuthError(message, cause)

    /**
     * Error when too many login attempts have been made
     */
    class TooManyAttemptsError(
        val attemptsCount: Int,
        val timeToReset: Long? = null,
        override val message: String = "Too many login attempts, please try again later",
        override val cause: Throwable? = null
    ) : AuthError(message, cause)

    /**
     * Error when password reset token has expired
     */
    class TokenExpiredError(
        override val message: String = "Password reset link has expired",
        override val cause: Throwable? = null
    ) : AuthError(message, cause)

    /**
     * Error when social login fails
     */
    class SocialLoginError(
        val provider: String,
        override val message: String = "Login with $provider failed",
        override val cause: Throwable? = null
    ) : AuthError(message, cause)

    /**
     * Error for invalid email format
     */
    class InvalidEmailError(
        val email: String,
        override val message: String = "Invalid email format",
        override val cause: Throwable? = null
    ) : AuthError(message, cause)

    /**
     * Error for weak password
     */
    class WeakPasswordError(
        override val message: String = "Password doesn't meet security requirements",
        val requirements: List<String>? = null,
        override val cause: Throwable? = null
    ) : AuthError(message, cause)
}