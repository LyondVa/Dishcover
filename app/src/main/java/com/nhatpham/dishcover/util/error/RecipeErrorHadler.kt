package com.nhatpham.dishcover.util.error

import android.content.Context
import com.nhatpham.dishcover.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Specialized error handler for recipe-related errors
 */
class RecipeErrorHandler @Inject constructor(
    @ApplicationContext private val context: Context, private val errorHandler: ErrorHandler
) {
    init {
        // Register custom handlers for recipe-specific errors
        errorHandler.registerErrorHandler(RecipeError.MissingIngredientsError::class) { error ->
            error
            val baseMessage = context.getString(R.string.error_missing_ingredients)
            if (error.missingIngredients.isNotEmpty()) {
                "$baseMessage\n• ${error.missingIngredients.joinToString("\n• ")}"
            } else {
                baseMessage
            }
        }

        errorHandler.registerErrorHandler(RecipeError.InvalidInstructionsError::class) { error ->
            error
            val baseMessage = context.getString(R.string.error_invalid_instructions)
            error.details?.let { "$baseMessage: $it" } ?: baseMessage
        }

        errorHandler.registerErrorHandler(RecipeError.SelfRatingError::class) { error ->
            context.getString(R.string.error_self_rating)
        }

        errorHandler.registerErrorHandler(RecipeError.ContentViolationError::class) { error ->
            error
            context.getString(R.string.error_content_violation, error.violationType)
        }

        errorHandler.registerErrorHandler(RecipeError.ImageUploadError::class) { error ->
            error
            val baseMessage = context.getString(R.string.error_image_upload)
            error.reason?.let { "$baseMessage: $it" } ?: baseMessage
        }

        errorHandler.registerErrorHandler(RecipeError.RecipeAccessError::class) { error ->
            error
            context.getString(R.string.error_recipe_access, error.action)
        }

        // Register recovery actions
        errorHandler.registerRecoveryAction(RecipeError.MissingIngredientsError::class) { error ->
            ErrorRecoveryAction.MultiAction(primary = ErrorRecoveryAction.Custom(label = context.getString(
                R.string.action_add_ingredients
            ), action = {}  // To be implemented by handler
            ), secondary = listOf(
                ErrorRecoveryAction.Dismiss(context.getString(R.string.action_save_draft))
            ))
        }

        errorHandler.registerRecoveryAction(RecipeError.ImageUploadError::class) { error ->
            ErrorRecoveryAction.Retry(context.getString(R.string.action_retry_upload))
        }
    }

    /**
     * Handle a recipe error with appropriate message and recovery action
     */
    fun handleRecipeError(error: AppError): String {
        return errorHandler.handleError(error)
    }

    /**
     * Get a recovery action for a recipe error
     */
    fun getRecipeRecoveryAction(error: AppError): ErrorRecoveryAction? {
        return errorHandler.getRecoveryAction(error)
    }

    /**
     * Validate a recipe and return appropriate errors if any
     */
    fun validateRecipe(
        title: String, ingredients: List<String>, instructions: String
    ): Result<Unit> {
        val errors = mutableListOf<AppError>()

        if (title.isBlank()) {
            errors.add(
                AppError.DomainError.ValidationError(
                    message = "Recipe title is required", field = "title"
                )
            )
        }

        if (ingredients.isEmpty()) {
            errors.add(
                RecipeError.MissingIngredientsError(
                    missingIngredients = emptyList(),
                    message = "Recipe must have at least one ingredient"
                )
            )
        }

        if (instructions.isBlank()) {
            errors.add(
                RecipeError.InvalidInstructionsError(
                    details = "Instructions cannot be empty",
                    message = "Recipe instructions are required"
                )
            )
        }

        return if (errors.isEmpty()) {
            Result.Success(Unit)
        } else {
            Result.Error(errors.first()) // Return first error, could also create a composite error
        }
    }
}