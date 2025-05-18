package com.nhatpham.dishcover.util.error

/**
 * Represents actions that can be taken to recover from errors
 */
sealed class ErrorRecoveryAction {
    /**
     * Retry the failed operation
     */
    data class Retry(val label: String) : ErrorRecoveryAction()

    /**
     * Navigate to another screen
     */
    data class Navigate(val label: String, val destination: String) : ErrorRecoveryAction()

    /**
     * Dismiss the error
     */
    data class Dismiss(val label: String) : ErrorRecoveryAction()

    /**
     * Custom action with callback
     */
    data class Custom(val label: String, val action: () -> Unit) : ErrorRecoveryAction()

    /**
     * Multiple actions that can be presented as options
     */
    data class MultiAction(
        val primary: ErrorRecoveryAction,
        val secondary: List<ErrorRecoveryAction>
    ) : ErrorRecoveryAction()
}