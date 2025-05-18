package com.nhatpham.dishcover.ui.components.error

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.util.error.AppError
import com.nhatpham.dishcover.util.error.ErrorRecoveryAction
import com.nhatpham.dishcover.util.error.ErrorState

/**
 * Composable function to display an error message with optional recovery action
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    recoveryAction: ErrorRecoveryAction? = null,
    onRecoveryAction: () -> Unit = {},
    severity: ErrorSeverity = ErrorSeverity.ERROR
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (severity) {
                ErrorSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer
                ErrorSeverity.WARNING -> MaterialTheme.colorScheme.secondaryContainer
                ErrorSeverity.INFO -> MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (severity) {
                    ErrorSeverity.ERROR -> Icons.Default.Error
                    ErrorSeverity.WARNING -> Icons.Default.Warning
                    ErrorSeverity.INFO -> Icons.Default.Info
                },
                contentDescription = "Error",
                tint = when (severity) {
                    ErrorSeverity.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                    ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onSecondaryContainer
                    ErrorSeverity.INFO -> MaterialTheme.colorScheme.onTertiaryContainer
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message,
                    color = when (severity) {
                        ErrorSeverity.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onSecondaryContainer
                        ErrorSeverity.INFO -> MaterialTheme.colorScheme.onTertiaryContainer
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                recoveryAction?.let {
                    Spacer(modifier = Modifier.height(8.dp))

                    when (it) {
                        is ErrorRecoveryAction.MultiAction -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                // Secondary actions
                                it.secondary.forEach { action ->
                                    TextButton(
                                        onClick = onRecoveryAction,
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = when (severity) {
                                                ErrorSeverity.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                                ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onSecondaryContainer
                                                ErrorSeverity.INFO -> MaterialTheme.colorScheme.onTertiaryContainer
                                            }
                                        )
                                    ) {
                                        Text(
                                            when (action) {
                                                is ErrorRecoveryAction.Retry -> action.label
                                                is ErrorRecoveryAction.Navigate -> action.label
                                                is ErrorRecoveryAction.Dismiss -> action.label
                                                is ErrorRecoveryAction.Custom -> action.label
                                                else -> ""
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                // Primary action
                                Button(
                                    onClick = onRecoveryAction,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (severity) {
                                            ErrorSeverity.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                            ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onSecondaryContainer
                                            ErrorSeverity.INFO -> MaterialTheme.colorScheme.onTertiaryContainer
                                        },
                                        contentColor = when (severity) {
                                            ErrorSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer
                                            ErrorSeverity.WARNING -> MaterialTheme.colorScheme.secondaryContainer
                                            ErrorSeverity.INFO -> MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    )
                                ) {
                                    Text(
                                        when (val primary = it.primary) {
                                            is ErrorRecoveryAction.Retry -> primary.label
                                            is ErrorRecoveryAction.Navigate -> primary.label
                                            is ErrorRecoveryAction.Dismiss -> primary.label
                                            is ErrorRecoveryAction.Custom -> primary.label
                                            else -> ""
                                        }
                                    )
                                }
                            }
                        }
                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = onRecoveryAction,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (severity) {
                                            ErrorSeverity.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                            ErrorSeverity.WARNING -> MaterialTheme.colorScheme.onSecondaryContainer
                                            ErrorSeverity.INFO -> MaterialTheme.colorScheme.onTertiaryContainer
                                        },
                                        contentColor = when (severity) {
                                            ErrorSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer
                                            ErrorSeverity.WARNING -> MaterialTheme.colorScheme.secondaryContainer
                                            ErrorSeverity.INFO -> MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    )
                                ) {
                                    Text(
                                        when (recoveryAction) {
                                            is ErrorRecoveryAction.Retry -> recoveryAction.label
                                            is ErrorRecoveryAction.Navigate -> recoveryAction.label
                                            is ErrorRecoveryAction.Dismiss -> recoveryAction.label
                                            is ErrorRecoveryAction.Custom -> recoveryAction.label
                                            else -> ""
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable function to handle error states
 */
@Composable
fun ErrorHandler(
    errorState: ErrorState,
    onRecoveryAction: (ErrorRecoveryAction) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
    getSeverity: (AppError?) -> ErrorSeverity = { ErrorSeverity.ERROR }
) {
    if (errorState.showError && errorState.errorMessage != null) {
        ErrorMessage(
            message = errorState.errorMessage ?: "",
            modifier = modifier,
            recoveryAction = errorState.recoveryAction,
            onRecoveryAction = {
                errorState.recoveryAction?.let { action ->
                    onRecoveryAction(action)
                }
                onDismissError()
            },
            severity = getSeverity(errorState.error)
        )
    }
}

/**
 * Error severity levels for UI display
 */
enum class ErrorSeverity {
    ERROR,      // Critical errors that prevent functionality
    WARNING,    // Warnings that don't prevent functionality
    INFO        // Informational messages about errors
}

/**
 * Extension function to determine the severity of an error
 */
fun AppError.getSeverity(): ErrorSeverity {
    return when (this) {
        // Critical errors
        is AppError.AuthError.SessionError,
        is AppError.DataError.NetworkError.ServerError,
        is AppError.SystemError.OutOfMemoryError,
        is AppError.SystemError.SecurityError -> ErrorSeverity.ERROR

        // Warnings
        is AppError.DataError.NetworkError.ConnectionError,
        is AppError.DataError.NetworkError.TimeoutError,
        is AppError.DataError.SyncError -> ErrorSeverity.WARNING

        // Informational
        is AppError.DomainError.ValidationError,
        is AppError.DomainError.NotFoundError -> ErrorSeverity.INFO

        // Default
        else -> ErrorSeverity.ERROR
    }
}