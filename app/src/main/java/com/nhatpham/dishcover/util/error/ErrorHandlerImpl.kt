package com.nhatpham.dishcover.util.error

import android.content.Context
import android.util.Log
import com.nhatpham.dishcover.BuildConfig
import com.nhatpham.dishcover.util.error.logging.ErrorLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import com.nhatpham.dishcover.R
import com.nhatpham.dishcover.util.analytics.AnalyticsTracker

/**
 * Implementation of the ErrorHandler interface
 */
@Singleton
class ErrorHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsTracker: AnalyticsTracker
) : ErrorHandler {

    private val errorHandlers = mutableMapOf<KClass<out AppError>, (AppError) -> String>()
    private val recoveryActionProviders = mutableMapOf<KClass<out AppError>, (AppError) -> ErrorRecoveryAction?>()
    private var errorLogger: ErrorLogger? = null

    init {
        setupDefaultHandlers()
        setupDefaultRecoveryActions()
    }

    private fun setupDefaultHandlers() {
        // Network errors
        registerErrorHandler(AppError.DataError.NetworkError.ConnectionError::class) { error ->
            context.getString(R.string.error_connection)
        }

        registerErrorHandler(AppError.DataError.NetworkError.TimeoutError::class) { error ->
            context.getString(R.string.error_timeout)
        }

        registerErrorHandler(AppError.DataError.NetworkError.ServerError::class) { error ->
            error
            when (error.statusCode) {
                in 400..499 -> context.getString(R.string.error_client, error.statusCode)
                in 500..599 -> context.getString(R.string.error_server, error.statusCode)
                else -> context.getString(R.string.error_unknown_server, error.statusCode)
            }
        }

        // Authentication errors
        registerErrorHandler(AppError.AuthError.InvalidCredentialsError::class) { error ->
            context.getString(R.string.error_invalid_credentials)
        }

        registerErrorHandler(AppError.AuthError.UserNotFoundError::class) { error ->
            context.getString(R.string.error_user_not_found)
        }

        registerErrorHandler(AppError.AuthError.SessionError::class) { error ->
            context.getString(R.string.error_session_expired)
        }

        registerErrorHandler(AppError.AuthError.UserExistsError::class) { error ->
            context.getString(R.string.error_user_exists)
        }

        // Local data errors
        registerErrorHandler(AppError.DataError.LocalDataError.DatabaseError::class) { error ->
            context.getString(R.string.error_database)
        }

        registerErrorHandler(AppError.DataError.LocalDataError.FileError::class) { error ->
            context.getString(R.string.error_file)
        }

        // Sync errors
        registerErrorHandler(AppError.DataError.SyncError::class) { error ->
            context.getString(R.string.error_sync)
        }

        // Domain errors
        registerErrorHandler(AppError.DomainError.ValidationError::class) { error ->
            error
            if (error.field != null) {
                context.getString(R.string.error_validation_field, error.field, error.message)
            } else {
                error.message
            }
        }

        registerErrorHandler(AppError.DomainError.NotFoundError::class) { error ->
            error
            context.getString(R.string.error_entity_not_found, error.entityType)
        }

        registerErrorHandler(AppError.DomainError.PermissionError::class) { error ->
            context.getString(R.string.error_permission)
        }

        // System errors
        registerErrorHandler(AppError.SystemError.OutOfMemoryError::class) { error ->
            context.getString(R.string.error_memory)
        }

        registerErrorHandler(AppError.SystemError.UnexpectedError::class) { error ->
            context.getString(R.string.error_unexpected)
        }

        // Default handler for unhandled error types
        registerErrorHandler(AppError::class) { error ->
            error.message
        }
    }

    private fun setupDefaultRecoveryActions() {
        // Network errors
        registerRecoveryAction(AppError.DataError.NetworkError.ConnectionError::class) { error ->
            ErrorRecoveryAction.Retry(context.getString(R.string.action_retry))
        }

        registerRecoveryAction(AppError.DataError.NetworkError.TimeoutError::class) { error ->
            ErrorRecoveryAction.Retry(context.getString(R.string.action_retry))
        }

        // Auth errors
        registerRecoveryAction(AppError.AuthError.SessionError::class) { error ->
            ErrorRecoveryAction.Navigate(
                label = context.getString(R.string.action_login),
                destination = "login_screen"
            )
        }

        registerRecoveryAction(AppError.DomainError.PermissionError::class) { error ->
            ErrorRecoveryAction.Navigate(
                label = context.getString(R.string.action_login),
                destination = "login_screen"
            )
        }

        // Sync errors
        registerRecoveryAction(AppError.DataError.SyncError::class) { error ->
            ErrorRecoveryAction.MultiAction(
                primary = ErrorRecoveryAction.Retry(context.getString(R.string.action_retry)),
                secondary = listOf(
                    ErrorRecoveryAction.Dismiss(context.getString(R.string.action_dismiss))
                )
            )
        }
    }

    override fun <T : AppError> registerErrorHandler(
        errorClass: KClass<T>,
        handler: (T) -> String
    ) {
        @Suppress("UNCHECKED_CAST")
        errorHandlers[errorClass] = handler as (AppError) -> String
    }

    override fun <T : AppError> registerRecoveryAction(
        errorClass: KClass<T>,
        provider: (T) -> ErrorRecoveryAction?
    ) {
        @Suppress("UNCHECKED_CAST")
        recoveryActionProviders[errorClass] = provider as (AppError) -> ErrorRecoveryAction?
    }

    override fun handleError(error: AppError): String {
        val handler = findMostSpecificHandler(error::class)
        return handler(error)
    }

    override fun getRecoveryAction(error: AppError): ErrorRecoveryAction? {
        val provider = findMostSpecificRecoveryProvider(error::class)
        return provider?.invoke(error)
    }

    override fun logError(error: AppError) {
        // Log to analytics
        analyticsTracker.logError(error)

        // Log to error logger if available
        errorLogger?.logError(error)

        // Log to console in debug mode if no error logger
        if (errorLogger == null && BuildConfig.DEBUG) {
            Log.e("ErrorHandler", "Error: ${error.message}", error.cause)
        }
    }

    override fun setErrorLogger(errorLogger: ErrorLogger) {
        this.errorLogger = errorLogger
    }

    /**
     * Find the most specific handler for an error type
     */
    private fun findMostSpecificHandler(errorClass: KClass<out AppError>): (AppError) -> String {
        // Try to find an exact match
        errorHandlers[errorClass]?.let { return it }

        // Try to find handlers for parent classes
        for ((registeredClass, handler) in errorHandlers) {
            if (errorClass.isSubclassOf(registeredClass) && registeredClass != AppError::class) {
                return handler
            }
        }

        // Return the default handler
        return errorHandlers[AppError::class] ?: { error -> error.message }
    }

    /**
     * Find the most specific recovery action provider for an error type
     */
    private fun findMostSpecificRecoveryProvider(errorClass: KClass<out AppError>): ((AppError) -> ErrorRecoveryAction?)? {
        // Try to find an exact match
        recoveryActionProviders[errorClass]?.let { return it }

        // Try to find providers for parent classes
        for ((registeredClass, provider) in recoveryActionProviders) {
            if (errorClass.isSubclassOf(registeredClass)) {
                return provider
            }
        }

        return null
    }
}