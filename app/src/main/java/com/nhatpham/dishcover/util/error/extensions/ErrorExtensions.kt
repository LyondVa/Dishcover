package com.nhatpham.dishcover.util.error.extensions

import com.nhatpham.dishcover.util.error.AppError
import com.nhatpham.dishcover.util.error.ErrorHandler
import com.nhatpham.dishcover.util.error.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Extension functions for working with errors
 */

/**
 * Execute a suspending operation and wrap the result in a Result
 */
suspend inline fun <T> runCatchingResult(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
        // Don't catch cancellation exceptions
        throw e
    } catch (e: Exception) {
        Result.Error(AppError.SystemError.UnexpectedError(cause = e))
    }
}

/**
 * Execute a suspending operation and wrap the result in a Result,
 * Converting exceptions to AppError using the provided error handler
 */
suspend inline fun <T> runCatchingResult(
    crossinline block: suspend () -> T,
    errorHandler: ErrorHandler
): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
        // Don't catch cancellation exceptions
        throw e
    } catch (e: Exception) {
        val appError = when (e) {
            is AppError -> e
            else -> AppError.SystemError.UnexpectedError(cause = e)
        }

        // Log the error
        errorHandler.logError(appError)

        Result.Error(appError)
    }
}

/**
 * Run a suspending operation with automatic retry on failure
 */
suspend fun <T> withRetry(
    maxRetries: Int,
    initialDelayMillis: Long = 100,
    maxDelayMillis: Long = 1000,
    shouldRetry: (Exception) -> Boolean = { true },
    block: suspend () -> T
): Result<T> {
    var currentDelay = initialDelayMillis
    repeat(maxRetries) { attemptNumber ->
        val result = runCatchingResult { block() }

        when (result) {
            is Result.Success -> return result
            is Result.Error -> {
                val cause = result.error.cause

                // If we should not retry for this exception, return the error
                if (cause !is Exception || !shouldRetry(cause)) {
                    return result
                }

                // If this is the last attempt, return the error
                if (attemptNumber == maxRetries - 1) {
                    return result
                }

                // Exponential backoff
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = (currentDelay * 1.5).toLong().coerceAtMost(maxDelayMillis)
            }
            is Result.Loading -> {
                // This should not happen in this context
                return@repeat
            }
        }
    }

    // This should never be reached
    return Result.Error(AppError.SystemError.UnexpectedError("Maximum retries reached"))
}

/**
 * Configure a flow with common error handling
 */
fun <T> Flow<T>.withErrorHandling(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    errorHandler: ErrorHandler? = null,
    onStart: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null
): Flow<T> {
    var flow = this

    if (onStart != null) {
        flow = flow.onStart { onStart() }
    }

    if (onComplete != null) {
        flow = flow.onCompletion { onComplete() }
    }

    if (onError != null || errorHandler != null) {
        flow = flow.catch { throwable ->
            if (throwable is CancellationException) {
                // Don't catch cancellation exceptions
                throw throwable
            }

            // Log the error if we have an error handler
            errorHandler?.let {
                when (throwable) {
                    is AppError -> it.logError(throwable)
                    else -> it.logError(AppError.SystemError.UnexpectedError(cause = throwable))
                }
            }

            // Call the error handler if provided
            onError?.invoke(throwable)

            throw throwable
        }
    }

    if (coroutineContext != EmptyCoroutineContext) {
        flow = flow.flowOn(coroutineContext)
    }

    return flow
}

/**
 * Handle errors in a flow with a fallback value
 */
inline fun <T> Flow<T>.withErrorFallback(
    crossinline fallback: (Throwable) -> T
): Flow<T> {
    return catch { throwable ->
        if (throwable is CancellationException) {
            // Don't catch cancellation exceptions
            throw throwable
        }

        emit(fallback(throwable))
    }
}