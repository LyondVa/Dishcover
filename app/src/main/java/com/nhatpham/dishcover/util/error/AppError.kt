package com.nhatpham.dishcover.util.error

/**
 * Base error hierarchy for the application.
 * All domain-specific errors should extend this class.
 */
sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    /**
     * Domain-specific errors related to business rules and validation
     */
    sealed class DomainError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        /**
         * Validation error for invalid data
         */
        class ValidationError(
            override val message: String,
            val field: String? = null,
            val value: Any? = null,
            override val cause: Throwable? = null
        ) : DomainError(message, cause)

        /**
         * Entity not found errors
         */
        class NotFoundError(
            val entityType: String,
            val identifier: Any?,
            override val message: String = "$entityType with identifier $identifier not found",
            override val cause: Throwable? = null
        ) : DomainError(message, cause)

        /**
         * Permission or authorization errors
         */
        class PermissionError(
            override val message: String = "You don't have permission to perform this action",
            override val cause: Throwable? = null
        ) : DomainError(message, cause)

        /**
         * Business rule violation errors
         */
        class BusinessRuleError(
            override val message: String,
            val rule: String? = null,
            override val cause: Throwable? = null
        ) : DomainError(message, cause)
    }

    /**
     * Data layer errors related to data access and persistence
     */
    sealed class DataError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        /**
         * Network-related errors
         */
        sealed class NetworkError(
            override val message: String,
            override val cause: Throwable? = null
        ) : DataError(message, cause) {
            /**
             * Error when unable to connect to the network
             */
            class ConnectionError(
                override val message: String = "Unable to connect to server",
                override val cause: Throwable? = null
            ) : NetworkError(message, cause)

            /**
             * Error for server responses with error status codes
             */
            class ServerError(
                val statusCode: Int,
                override val message: String = "Server error occurred",
                override val cause: Throwable? = null
            ) : NetworkError(message, cause)

            /**
             * Error when a request times out
             */
            class TimeoutError(
                override val message: String = "Request timed out",
                override val cause: Throwable? = null
            ) : NetworkError(message, cause)
        }

        /**
         * Local data storage errors
         */
        sealed class LocalDataError(
            override val message: String,
            override val cause: Throwable? = null
        ) : DataError(message, cause) {
            /**
             * Database operation error
             */
            class DatabaseError(
                override val message: String = "Database operation failed",
                override val cause: Throwable? = null
            ) : LocalDataError(message, cause)

            /**
             * File operation error
             */
            class FileError(
                override val message: String = "File operation failed",
                override val cause: Throwable? = null
            ) : LocalDataError(message, cause)

            /**
             * Error accessing preferences or settings
             */
            class PreferencesError(
                override val message: String = "Failed to access preferences",
                override val cause: Throwable? = null
            ) : LocalDataError(message, cause)
        }

        /**
         * Error during data synchronization
         */
        class SyncError(
            override val message: String = "Failed to synchronize data",
            val entity: String? = null,
            val operation: String? = null,
            override val cause: Throwable? = null
        ) : DataError(message, cause)

        /**
         * Error during data conversion or mapping
         */
        class DataConversionError(
            override val message: String = "Failed to convert data",
            val source: Any? = null,
            val targetType: String? = null,
            override val cause: Throwable? = null
        ) : DataError(message, cause)
    }

    /**
     * Authentication and user-related errors
     */
    sealed class AuthError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        /**
         * Invalid credentials error
         */
        class InvalidCredentialsError(
            override val message: String = "Invalid username or password",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)

        /**
         * User account not found
         */
        class UserNotFoundError(
            val identifier: String? = null,
            override val message: String = "User not found",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)

        /**
         * Session expired or invalid
         */
        class SessionError(
            override val message: String = "Your session has expired",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)

        /**
         * Account already exists
         */
        class UserExistsError(
            val email: String? = null,
            override val message: String = "User already exists",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)
    }

    /**
     * Presentation layer errors
     */
    sealed class PresentationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        /**
         * UI resource not found
         */
        class ResourceNotFoundError(
            val resourceId: Any,
            override val message: String = "Resource not found: $resourceId",
            override val cause: Throwable? = null
        ) : PresentationError(message, cause)

        /**
         * Error in UI state management
         */
        class UiStateError(
            override val message: String,
            override val cause: Throwable? = null
        ) : PresentationError(message, cause)
    }

    /**
     * System-level errors
     */
    sealed class SystemError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        /**
         * Out of memory error
         */
        class OutOfMemoryError(
            override val message: String = "Out of memory",
            override val cause: Throwable? = null
        ) : SystemError(message, cause)

        /**
         * Security-related error
         */
        class SecurityError(
            override val message: String,
            override val cause: Throwable? = null
        ) : SystemError(message, cause)

        /**
         * Unexpected or uncategorized error
         */
        class UnexpectedError(
            override val message: String = "An unexpected error occurred",
            override val cause: Throwable? = null
        ) : SystemError(message, cause)
    }

    /**
     * Feature-specific errors can be added by extending AppError or one of its subclasses
     */
}