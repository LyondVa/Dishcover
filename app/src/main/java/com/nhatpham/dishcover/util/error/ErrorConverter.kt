package com.nhatpham.dishcover.util.error

import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketTimeoutException
import android.database.sqlite.SQLiteException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.FirebaseTooManyRequestsException
import retrofit2.HttpException

/**
 * Utility class to convert standard exceptions to AppError types
 */
object ErrorConverter {
    /**
     * Convert a general throwable to an appropriate AppError type
     */
    fun fromThrowable(throwable: Throwable): AppError {
        return when (throwable) {
            // Network errors
            is IOException -> AppError.DataError.NetworkError.ConnectionError(cause = throwable)
            is SocketTimeoutException -> AppError.DataError.NetworkError.TimeoutError(cause = throwable)
            is HttpException -> fromHttpStatusCode(throwable.code(), throwable.message())

            // Database errors
            is SQLiteException -> AppError.DataError.LocalDataError.DatabaseError(cause = throwable)

            // File errors
            is FileNotFoundException -> AppError.DataError.LocalDataError.FileError(cause = throwable)

            // Firebase errors
            is FirebaseException -> fromFirebaseException(throwable)

            // Validation errors
            is IllegalArgumentException -> AppError.DomainError.ValidationError(
                message = throwable.message ?: "Invalid argument",
                cause = throwable
            )

            // System errors
            is SecurityException -> AppError.SystemError.SecurityError(
                message = throwable.message ?: "Security error",
                cause = throwable
            )
            is OutOfMemoryError -> AppError.SystemError.OutOfMemoryError(cause = throwable)

            // Default case
            else -> AppError.SystemError.UnexpectedError(
                message = throwable.message ?: "Unknown error",
                cause = throwable
            )
        }
    }

    /**
     * Convert Firebase exceptions to specific AppError types
     */
    fun fromFirebaseException(exception: FirebaseException): AppError {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException ->
                AppError.AuthError.InvalidCredentialsError(cause = exception)

            is FirebaseAuthInvalidUserException ->
                AppError.AuthError.UserNotFoundError(cause = exception)

            is FirebaseAuthUserCollisionException ->
                AppError.AuthError.UserExistsError(cause = exception)

            is FirebaseNetworkException ->
                AppError.DataError.NetworkError.ConnectionError(cause = exception)

            is FirebaseTooManyRequestsException ->
                AppError.DataError.NetworkError.ServerError(
                    statusCode = 429,
                    message = "Too many requests",
                    cause = exception
                )

            else -> AppError.SystemError.UnexpectedError(
                message = exception.message ?: "Firebase error",
                cause = exception
            )
        }
    }

    /**
     * Convert HTTP status codes to appropriate AppError types
     */
    fun fromHttpStatusCode(statusCode: Int, message: String? = null): AppError {
        return when (statusCode) {
            in 400..499 -> {
                when (statusCode) {
                    401 -> AppError.AuthError.SessionError(
                        message = message ?: "Authentication required"
                    )
                    403 -> AppError.DomainError.PermissionError(
                        message = message ?: "Permission denied"
                    )
                    404 -> AppError.DomainError.NotFoundError(
                        entityType = "Resource",
                        identifier = null,
                        message = message ?: "Resource not found"
                    )
                    422 -> AppError.DomainError.ValidationError(
                        message = message ?: "Validation failed"
                    )
                    else -> AppError.DataError.NetworkError.ServerError(
                        statusCode = statusCode,
                        message = message ?: "Client error"
                    )
                }
            }
            in 500..599 -> AppError.DataError.NetworkError.ServerError(
                statusCode = statusCode,
                message = message ?: "Server error"
            )
            else -> AppError.DataError.NetworkError.ServerError(
                statusCode = statusCode,
                message = message ?: "Unknown HTTP error"
            )
        }
    }
}