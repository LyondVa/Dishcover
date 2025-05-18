package com.nhatpham.dishcover.util.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Interface for ViewModels that handle errors
 */
interface ErrorState {
    val error: AppError?
    val errorMessage: String?
    val showError: Boolean
    val recoveryAction: ErrorRecoveryAction?
}

/**
 * Helper extension function to handle errors in ViewModels
 */
fun <T> ViewModel.handleErrorInViewModel(
    result: Result<T>,
    errorHandler: ErrorHandler,
    updateState: (AppError?, String?, ErrorRecoveryAction?) -> Unit,
    onSuccess: (T) -> Unit = {}
) {
    when (result) {
        is Result.Success -> {
            updateState(null, null, null)
            onSuccess(result.data)
        }
        is Result.Error -> {
            val errorMessage = errorHandler.handleError(result.error)
            val recoveryAction = errorHandler.getRecoveryAction(result.error)
            updateState(result.error, errorMessage, recoveryAction)
            errorHandler.logError(result.error)
        }
        is Result.Loading -> {
            // Usually don't need to handle loading here as it's part of state
        }
    }
}

/**
 * Extension function to collect a Flow with error handling
 */
fun <T> Flow<Result<T>>.handleErrorsIn(
    viewModelScope: CoroutineScope,
    errorHandler: ErrorHandler,
    updateState: (error: AppError?, errorMessage: String?, recoveryAction: ErrorRecoveryAction?) -> Unit,
    onSuccess: (value: T) -> Unit = {}
): Job {
    return onEach { result ->
        when (result) {
            is Result.Success -> {
                updateState(null, null, null)
                onSuccess(result.data)
            }
            is Result.Error -> {
                val errorMessage = errorHandler.handleError(result.error)
                val recoveryAction = errorHandler.getRecoveryAction(result.error)
                updateState(result.error, errorMessage, recoveryAction)
                errorHandler.logError(result.error)
            }
            is Result.Loading -> {
                // Usually don't update error state during loading
            }
        }
    }.catch { throwable ->
        // Handle uncaught exceptions
        val error = ErrorConverter.fromThrowable(throwable)
        val errorMessage = errorHandler.handleError(error)
        val recoveryAction = errorHandler.getRecoveryAction(error)
        updateState(error, errorMessage, recoveryAction)
        errorHandler.logError(error)
    }.launchIn(viewModelScope)
}

/**
 * Extension function to check if a Result is a specific error type
 */
inline fun <reified E : AppError> Result<*>.isErrorType(): Boolean {
    return this is Result.Error && error is E
}

/**
 * Extension function to cast the error to a specific type if it matches
 */
inline fun <reified E : AppError> Result<*>.getErrorAs(): E? {
    return if (this is Result.Error && error is E) error as E else null
}

/**
 * Extension function to handle specific error types
 */
inline fun <T, reified E : AppError> Result<T>.handleSpecificError(
    crossinline handler: (E) -> Unit
): Result<T> {
    if (this is Result.Error && error is E) {
        handler(error as E)
    }
    return this
}