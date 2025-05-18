package com.nhatpham.dishcover.util.error.logging

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhatpham.dishcover.BuildConfig
import com.nhatpham.dishcover.util.error.AppError
import com.nhatpham.dishcover.util.error.AuthError
import com.nhatpham.dishcover.util.error.RecipeError
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for error logging
 */
interface ErrorLogger {
    /**
     * Log an error
     */
    fun logError(error: AppError, additionalInfo: Map<String, Any>? = null)

    /**
     * Log an exception
     */
    fun logException(throwable: Throwable, additionalInfo: Map<String, Any>? = null)

    /**
     * Set user identifier for error reporting
     */
    fun setUserId(userId: String?)

    /**
     * Set custom key value for error reporting
     */
    fun setCustomKey(key: String, value: String)
}

/**
 * Implementation of ErrorLogger that combines multiple loggers
 */
@Singleton
class CompositeErrorLogger @Inject constructor(
    private val crashlyticsLogger: CrashlyticsErrorLogger,
    private val fileLogger: FileErrorLogger,
    private val consoleLogger: ConsoleErrorLogger
) : ErrorLogger {

    override fun logError(error: AppError, additionalInfo: Map<String, Any>?) {
        // Log to Crashlytics in production
        if (!BuildConfig.DEBUG) {
            crashlyticsLogger.logError(error, additionalInfo)
        }

        // Log to file in both debug and production
        fileLogger.logError(error, additionalInfo)

        // Log to console in debug mode
        if (BuildConfig.DEBUG) {
            consoleLogger.logError(error, additionalInfo)
        }
    }

    override fun logException(throwable: Throwable, additionalInfo: Map<String, Any>?) {
        // Log to Crashlytics in production
        if (!BuildConfig.DEBUG) {
            crashlyticsLogger.logException(throwable, additionalInfo)
        }

        // Log to file in both debug and production
        fileLogger.logException(throwable, additionalInfo)

        // Log to console in debug mode
        if (BuildConfig.DEBUG) {
            consoleLogger.logException(throwable, additionalInfo)
        }
    }

    override fun setUserId(userId: String?) {
        crashlyticsLogger.setUserId(userId)
        fileLogger.setUserId(userId)
        consoleLogger.setUserId(userId)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlyticsLogger.setCustomKey(key, value)
        fileLogger.setCustomKey(key, value)
        consoleLogger.setCustomKey(key, value)
    }
}

/**
 * Crashlytics implementation of ErrorLogger
 */
@Singleton
class CrashlyticsErrorLogger @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) : ErrorLogger {

    override fun logError(error: AppError, additionalInfo: Map<String, Any>?) {
        // Add error information as custom keys
        crashlytics.setCustomKey("error_type", error::class.simpleName ?: "Unknown")
        crashlytics.setCustomKey("error_message", error.message)

        // Add specific error details based on error type
        when (error) {
            is AppError.DataError.NetworkError.ServerError -> {
                crashlytics.setCustomKey("status_code", error.statusCode)
            }
            is AppError.DomainError.ValidationError -> {
                error.field?.let { crashlytics.setCustomKey("field", it) }
                error.value?.let { crashlytics.setCustomKey("value", it.toString()) }
            }
            is AppError.DomainError.NotFoundError -> {
                crashlytics.setCustomKey("entity_type", error.entityType)
                error.identifier?.let { crashlytics.setCustomKey("identifier", it.toString()) }
            }
            is AuthError.SocialLoginError -> {
                crashlytics.setCustomKey("provider", error.provider)
            }
            is RecipeError.MissingIngredientsError -> {
                crashlytics.setCustomKey("missing_ingredients", error.missingIngredients.toString())
            }
            else -> {
                // No specific error details to add
            }
        }

        // Add additional info
        additionalInfo?.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value.toString())
        }

        // Log the error
        crashlytics.recordException(error.cause ?: Exception(error.message))
    }

    override fun logException(throwable: Throwable, additionalInfo: Map<String, Any>?) {
        // Add additional info
        additionalInfo?.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value.toString())
        }

        // Log the exception
        crashlytics.recordException(throwable)
    }

    override fun setUserId(userId: String?) {
        userId?.let {
            crashlytics.setUserId(it)
        }
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}

/**
 * File implementation of ErrorLogger
 */
@Singleton
class FileErrorLogger @Inject constructor(
    @ApplicationContext private val context: Context
) : ErrorLogger {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private var userId: String? = null
    private val customKeys = mutableMapOf<String, String>()

    private val logDir: File by lazy {
        File(context.filesDir, "logs").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private val logFile: File by lazy {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        File(logDir, "error_log_$date.txt")
    }

    override fun logError(error: AppError, additionalInfo: Map<String, Any>?) {
        val logEntry = buildLogEntry(
            level = "ERROR",
            message = "AppError: ${error::class.simpleName} - ${error.message}",
            throwable = error.cause,
            additionalInfo = additionalInfo
        )

        writeToLogFile(logEntry)
    }

    override fun logException(throwable: Throwable, additionalInfo: Map<String, Any>?) {
        val logEntry = buildLogEntry(
            level = "EXCEPTION",
            message = "Exception: ${throwable::class.simpleName} - ${throwable.message}",
            throwable = throwable,
            additionalInfo = additionalInfo
        )

        writeToLogFile(logEntry)
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }

    override fun setCustomKey(key: String, value: String) {
        customKeys[key] = value
    }

    private fun buildLogEntry(
        level: String,
        message: String,
        throwable: Throwable?,
        additionalInfo: Map<String, Any>?
    ): String {
        val timestamp = dateFormat.format(Date())
        val sb = StringBuilder()

        sb.append("[$timestamp] [$level] - $message\n")

        // Add user ID if available
        userId?.let { sb.append("User ID: $it\n") }

        // Add custom keys
        if (customKeys.isNotEmpty()) {
            sb.append("Custom Keys: ")
            customKeys.forEach { (key, value) ->
                sb.append("$key=$value, ")
            }
            sb.setLength(sb.length - 2) // Remove trailing comma and space
            sb.append("\n")
        }

        // Add additional info
        additionalInfo?.let {
            sb.append("Additional Info: ")
            it.forEach { (key, value) ->
                sb.append("$key=$value, ")
            }
            sb.setLength(sb.length - 2) // Remove trailing comma and space
            sb.append("\n")
        }

        // Add stack trace if available
        throwable?.let {
            sb.append("Stack Trace:\n")
            it.stackTrace.take(10).forEach { element ->
                sb.append("    at $element\n")
            }
            if (it.stackTrace.size > 10) {
                sb.append("    ... ${it.stackTrace.size - 10} more\n")
            }
        }

        sb.append("\n")

        return sb.toString()
    }

    private fun writeToLogFile(logEntry: String) {
        try {
            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
        } catch (e: Exception) {
            Log.e("FileErrorLogger", "Failed to write to log file", e)
        }
    }
}

/**
 * Console implementation of ErrorLogger
 */
@Singleton
class ConsoleErrorLogger @Inject constructor() : ErrorLogger {
    private val TAG = "DishcoverError"
    private var userId: String? = null
    private val customKeys = mutableMapOf<String, String>()

    override fun logError(error: AppError, additionalInfo: Map<String, Any>?) {
        // Prepare the log message
        val message = StringBuilder().apply {
            append("AppError: ${error::class.simpleName} - ${error.message}")

            // Add user ID if available
            userId?.let { append(" | User: $it") }

            // Add custom keys
            if (customKeys.isNotEmpty()) {
                append(" | Keys: ")
                customKeys.forEach { (key, value) ->
                    append("$key=$value, ")
                }
                setLength(length - 2) // Remove trailing comma and space
            }

            // Add additional info
            additionalInfo?.let {
                append(" | Info: ")
                it.forEach { (key, value) ->
                    append("$key=$value, ")
                }
                setLength(length - 2) // Remove trailing comma and space
            }
        }.toString()

        // Log the error
        Log.e(TAG, message, error.cause)
    }

    override fun logException(throwable: Throwable, additionalInfo: Map<String, Any>?) {
        // Prepare the log message
        val message = StringBuilder().apply {
            append("Exception: ${throwable::class.simpleName} - ${throwable.message}")

            // Add user ID if available
            userId?.let { append(" | User: $it") }

            // Add custom keys
            if (customKeys.isNotEmpty()) {
                append(" | Keys: ")
                customKeys.forEach { (key, value) ->
                    append("$key=$value, ")
                }
                setLength(length - 2) // Remove trailing comma and space
            }

            // Add additional info
            additionalInfo?.let {
                append(" | Info: ")
                it.forEach { (key, value) ->
                    append("$key=$value, ")
                }
                setLength(length - 2) // Remove trailing comma and space
            }
        }.toString()

        // Log the exception
        Log.e(TAG, message, throwable)
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }

    override fun setCustomKey(key: String, value: String) {
        customKeys[key] = value
    }
}