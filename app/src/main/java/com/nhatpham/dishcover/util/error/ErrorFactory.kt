package com.nhatpham.dishcover.util.error

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory interface for creating AppError instances
 */
interface ErrorFactory {
    /**
     * Create a validation error
     */
    fun createValidationError(message: String, field: String? = null, value: Any? = null): AppError.DomainError.ValidationError

    /**
     * Create a not found error
     */
    fun createNotFoundError(entityType: String, identifier: Any?): AppError.DomainError.NotFoundError

    /**
     * Create a network connection error
     */
    fun createConnectionError(message: String? = null): AppError.DataError.NetworkError.ConnectionError

    /**
     * Create a server error
     */
    fun createServerError(statusCode: Int, message: String? = null): AppError.DataError.NetworkError.ServerError

    /**
     * Create an auth error for invalid credentials
     */
    fun createInvalidCredentialsError(message: String? = null): AppError.AuthError.InvalidCredentialsError

    /**
     * Create an error for email verification required
     */
    fun createEmailVerificationRequiredError(): AuthError.EmailVerificationRequiredError

    /**
     * Create a weak password error
     */
    fun createWeakPasswordError(requirements: List<String>? = null): AuthError.WeakPasswordError

    /**
     * Create a missing ingredients error
     */
    fun createMissingIngredientsError(missingIngredients: List<String>): RecipeError.MissingIngredientsError

    /**
     * Create a recipe access error
     */
    fun createRecipeAccessError(recipeId: String, action: String): RecipeError.RecipeAccessError
}

/**
 * Implementation of ErrorFactory for creating AppError instances
 */
@Singleton
class ErrorFactoryImpl @Inject constructor() : ErrorFactory {

    override fun createValidationError(
        message: String,
        field: String?,
        value: Any?
    ): AppError.DomainError.ValidationError {
        return AppError.DomainError.ValidationError(
            message = message,
            field = field,
            value = value
        )
    }

    override fun createNotFoundError(
        entityType: String,
        identifier: Any?
    ): AppError.DomainError.NotFoundError {
        return AppError.DomainError.NotFoundError(
            entityType = entityType,
            identifier = identifier
        )
    }

    override fun createConnectionError(message: String?): AppError.DataError.NetworkError.ConnectionError {
        return AppError.DataError.NetworkError.ConnectionError(
            message = message ?: "Unable to connect to server"
        )
    }

    override fun createServerError(
        statusCode: Int,
        message: String?
    ): AppError.DataError.NetworkError.ServerError {
        return AppError.DataError.NetworkError.ServerError(
            statusCode = statusCode,
            message = message ?: "Server error occurred"
        )
    }

    override fun createInvalidCredentialsError(message: String?): AppError.AuthError.InvalidCredentialsError {
        return AppError.AuthError.InvalidCredentialsError(
            message = message ?: "Invalid username or password"
        )
    }

    override fun createEmailVerificationRequiredError(): AuthError.EmailVerificationRequiredError {
        return AuthError.EmailVerificationRequiredError()
    }

    override fun createWeakPasswordError(requirements: List<String>?): AuthError.WeakPasswordError {
        return AuthError.WeakPasswordError(
            requirements = requirements
        )
    }

    override fun createMissingIngredientsError(missingIngredients: List<String>): RecipeError.MissingIngredientsError {
        return RecipeError.MissingIngredientsError(
            missingIngredients = missingIngredients
        )
    }

    override fun createRecipeAccessError(recipeId: String, action: String): RecipeError.RecipeAccessError {
        return RecipeError.RecipeAccessError(
            recipeId = recipeId,
            action = action
        )
    }
}