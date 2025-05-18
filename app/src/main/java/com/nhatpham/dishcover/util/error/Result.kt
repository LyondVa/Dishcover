package com.nhatpham.dishcover.util.error

import com.nhatpham.dishcover.util.Resource

/**
 * A generic result class that wraps success, error, and loading states.
 * This is an enhanced version of the existing Resource class with additional utilities.
 */
sealed class Result<T> {
    /**
     * Success state containing the actual data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Error state containing the error and optionally some partial data
     */
    data class Error<T>(val error: AppError, val data: T? = null) : Result<T>()

    /**
     * Loading state, optionally with partial data
     */
    data class Loading<T>(val data: T? = null) : Result<T>()

    /**
     * Check if the result is successful
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Check if the result is an error
     */
    fun isError(): Boolean = this is Error

    /**
     * Check if the result is loading
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Get the data or null if not available
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> data
        is Loading -> data
    }

    /**
     * Get the data or throw the error if failed
     */
    fun getOrThrow(): T {
        when (this) {
            is Success -> return data
            is Error -> throw error.cause ?: RuntimeException(error.message)
            is Loading -> throw IllegalStateException("Cannot get data while loading")
        }
    }

    /**
     * Map the data to a new type if successful
     */
    fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(error, data?.let { transform(it) })
            is Loading -> Loading(data?.let { transform(it) })
        }
    }

    /**
     * Handle specific error types
     */
    inline fun <reified E : AppError> onError(handler: (E) -> Unit): Result<T> {
        if (this is Error && error is E) {
            handler(error as E)
        }
        return this
    }

    /**
     * Process the result with handlers for each state
     */
    fun fold(
        onSuccess: (T) -> Unit = {},
        onError: (AppError, T?) -> Unit = { _, _ -> },
        onLoading: (T?) -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(error, data)
            is Loading -> onLoading(data)
        }
    }

    /**
     * Convert to the legacy Resource type for backward compatibility
     */
    fun toResource(): Resource<T> {
        return when (this) {
            is Success -> Resource.Success(data)
            is Error -> Resource.Error(error.message, data)
            is Loading -> Resource.Loading(data)
        }
    }

    companion object {
        /**
         * Create a Result from a Resource for backward compatibility
         */
        fun <T> fromResource(resource: Resource<T>): Result<T> {
            return when (resource) {
                is Resource.Success -> Success(resource.data!!)
                is Resource.Error -> Error(
                    AppError.SystemError.UnexpectedError(
                        message = resource.message ?: "Unknown error"
                    ),
                    resource.data
                )
                is Resource.Loading -> Loading(resource.data)
            }
        }

        /**
         * Safely execute a block of code and wrap the result
         */
        fun <T> runCatching(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(ErrorConverter.fromThrowable(e))
            }
        }
    }
}