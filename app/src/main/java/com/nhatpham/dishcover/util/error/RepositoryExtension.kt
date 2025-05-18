package com.nhatpham.dishcover.util.error

import com.google.firebase.FirebaseException
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import retrofit2.HttpException
import java.io.IOException


/**
 * Extension function to safely execute a suspending operation and wrap the result
 * in a Result flow with proper error handling
 */
fun <T> runCatchingFlow(block: suspend () -> T): Flow<Result<T>> = flow {
    emit(Result.Loading())
    try {
        val result = block()
        emit(Result.Success(result))
    } catch (e: Exception) {
        val appError = when (e) {
            is FirebaseException -> ErrorConverter.fromFirebaseException(e)
            is HttpException -> ErrorConverter.fromHttpStatusCode(e.code(), e.message())
            is IOException -> AppError.DataError.NetworkError.ConnectionError(cause = e)
            else -> ErrorConverter.fromThrowable(e)
        }
        emit(Result.Error(appError))
    }
}

/**
 * Extension function to convert a Flow<Resource<T>> to Flow<Result<T>>
 */
fun <T> Flow<Resource<T>>.toResultFlow(): Flow<Result<T>> {
    return map { resource ->
        when (resource) {
            is Resource.Success -> Result.Success(resource.data!!)
            is Resource.Error -> Result.Error(
                AppError.SystemError.UnexpectedError(
                    message = resource.message ?: "Unknown error"
                ),
                resource.data
            )
            is Resource.Loading -> Result.Loading(resource.data)
        }
    }
}

/**
 * Extension function to convert a Flow<Result<T>> to Flow<Resource<T>>
 */
fun <T> Flow<Result<T>>.toResourceFlow(): Flow<Resource<T>> {
    return map { result ->
        when (result) {
            is Result.Success -> Resource.Success(result.data)
            is Result.Error -> Resource.Error(result.error.message, result.data)
            is Result.Loading -> Resource.Loading(result.data)
        }
    }
}

/**
 * Extension function to log errors in a flow
 */
fun <T> Flow<Result<T>>.logErrors(errorHandler: ErrorHandler): Flow<Result<T>> {
    return onEach { result ->
        if (result is Result.Error) {
            errorHandler.logError(result.error)
        }
    }
}

/**
 * Extension function to add global error handling to a flow
 */
fun <T> Flow<Result<T>>.withErrorHandling(): Flow<Result<T>> {
    return catch { throwable ->
        val error = ErrorConverter.fromThrowable(throwable)
        emit(Result.Error(error))
    }
}

/**
 * Extension function to execute an operation with offline-first strategy
 */
fun <T> offlineFirstFlow(
    fetchLocal: suspend () -> T?,
    fetchRemote: suspend () -> T,
    saveLocal: suspend (T) -> Unit
): Flow<Result<T>> = flow {
    emit(Result.Loading())

    // Try to get from local cache first
    try {
        val localData = fetchLocal()
        if (localData != null) {
            emit(Result.Success(localData))
        }
    } catch (e: Exception) {
        // Log but don't emit error if local fails
        val error = ErrorConverter.fromThrowable(e)
        // Could log error here
    }

    // Then try to get from remote
    try {
        val remoteData = fetchRemote()

        // Save to local database
        try {
            saveLocal(remoteData)
        } catch (e: Exception) {
            // Log but continue if saving fails
            val error = ErrorConverter.fromThrowable(e)
            // Could log error here
        }

        // Emit the remote data
        emit(Result.Success(remoteData))
    } catch (e: Exception) {
        val error = when (e) {
            is FirebaseException -> ErrorConverter.fromFirebaseException(e)
            is HttpException -> ErrorConverter.fromHttpStatusCode(e.code(), e.message())
            is IOException -> AppError.DataError.NetworkError.ConnectionError(cause = e)
            else -> ErrorConverter.fromThrowable(e)
        }

        // Only emit error if we never emitted a success
        val localData = fetchLocal()
        if (localData == null) {
            emit(Result.Error(error))
        }
    }
}