package com.nhatpham.dishcover.util.error

import com.nhatpham.dishcover.util.error.logging.ErrorLogger
import kotlin.reflect.KClass

/**
 * Interface for handling application errors
 */
interface ErrorHandler {
    /**
     * Get a user-friendly message for an error
     */
    fun handleError(error: AppError): String

    /**
     * Get a recovery action for an error if available
     */
    fun getRecoveryAction(error: AppError): ErrorRecoveryAction?

    /**
     * Log an error for analytics and debugging
     */
    fun logError(error: AppError)

    /**
     * Register a custom handler for a specific error type
     */
    fun <T : AppError> registerErrorHandler(
        errorClass: KClass<T>,
        handler: (T) -> String
    )

    /**
     * Register a custom recovery action provider for a specific error type
     */
    fun <T : AppError> registerRecoveryAction(
        errorClass: KClass<T>,
        provider: (T) -> ErrorRecoveryAction?
    )

    /**
     * Set the error logger
     */
    fun setErrorLogger(errorLogger: ErrorLogger)
}