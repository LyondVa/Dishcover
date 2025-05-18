package com.nhatpham.dishcover.util.error

import android.content.Context
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.nhatpham.dishcover.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Specialized error handler for authentication-related errors
 */
class AuthErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val errorHandler: ErrorHandler
) {
    init {
        // Register custom handlers for auth-specific errors
        errorHandler.registerErrorHandler(AuthError.EmailVerificationRequiredError::class) { error ->
            context.getString(R.string.error_email_verification_required)
        }

        errorHandler.registerErrorHandler(AuthError.TooManyAttemptsError::class) { error ->
            error
            if (error.timeToReset != null) {
                context.getString(R.string.error_too_many_attempts_with_time, error.timeToReset)
            } else {
                context.getString(R.string.error_too_many_attempts)
            }
        }

        errorHandler.registerErrorHandler(AuthError.TokenExpiredError::class) { error ->
            context.getString(R.string.error_token_expired)
        }

        errorHandler.registerErrorHandler(AuthError.SocialLoginError::class) { error ->
            error
            context.getString(R.string.error_social_login, error.provider)
        }

        errorHandler.registerErrorHandler(AuthError.InvalidEmailError::class) { error ->
            context.getString(R.string.error_invalid_email_format)
        }

        errorHandler.registerErrorHandler(AuthError.WeakPasswordError::class) { error ->
            error
            val baseMessage = context.getString(R.string.error_weak_password)
            error.requirements?.let { requirements ->
                "$baseMessage\n• ${requirements.joinToString("\n• ")}"
            } ?: baseMessage
        }

        // Register recovery actions
        errorHandler.registerRecoveryAction(AuthError.EmailVerificationRequiredError::class) { error ->
            ErrorRecoveryAction.MultiAction(
                primary = ErrorRecoveryAction.Custom(
                    label = context.getString(R.string.action_resend_verification),
                    action = {}  // To be implemented by handler
                ),
                secondary = listOf(
                    ErrorRecoveryAction.Dismiss(context.getString(R.string.action_dismiss))
                )
            )
        }

        errorHandler.registerRecoveryAction(AuthError.TokenExpiredError::class) { error ->
            ErrorRecoveryAction.Navigate(
                label = context.getString(R.string.action_request_new_link),
                destination = "forgot_password_screen"
            )
        }
    }

    /**
     * Convert Firebase Auth exceptions to specific AppError types
     */
    fun convertFirebaseAuthException(exception: Exception): AppError {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                // Check for specific credential issues
                when {
                    exception.message?.contains("email", ignoreCase = true) == true ->
                        AuthError.InvalidEmailError(
                            email = "",  // We don't have the email here
                            cause = exception
                        )

                    exception.message?.contains("password", ignoreCase = true) == true ->
                        AppError.AuthError.InvalidCredentialsError(cause = exception)

                    exception.message?.contains("verification", ignoreCase = true) == true ->
                        AuthError.TokenExpiredError(cause = exception)

                    else -> AppError.AuthError.InvalidCredentialsError(cause = exception)
                }
            }

            is FirebaseAuthInvalidUserException -> {
                // Check for specific user issues
                when {
                    exception.message?.contains("disabled", ignoreCase = true) == true ->
                        AppError.AuthError.UserNotFoundError(
                            message = "Account has been disabled",
                            cause = exception
                        )

                    exception.message?.contains("user-not-found", ignoreCase = true) == true ->
                        AppError.AuthError.UserNotFoundError(cause = exception)

                    else -> AppError.AuthError.UserNotFoundError(cause = exception)
                }
            }

            is FirebaseAuthUserCollisionException -> {
                AppError.AuthError.UserExistsError(cause = exception)
            }

            else -> ErrorConverter.fromThrowable(exception)
        }
    }

    /**
     * Handle an auth error with appropriate message and recovery action
     */
    fun handleAuthError(error: AppError): String {
        return errorHandler.handleError(error)
    }

    /**
     * Get a recovery action for an auth error
     */
    fun getAuthRecoveryAction(error: AppError): ErrorRecoveryAction? {
        return errorHandler.getRecoveryAction(error)
    }
}