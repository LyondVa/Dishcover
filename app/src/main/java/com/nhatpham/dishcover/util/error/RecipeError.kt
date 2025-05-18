package com.nhatpham.dishcover.util.error

/**
 * Recipe-specific errors that extend the base AppError hierarchy
 */
sealed class RecipeError(
    override val message: String,
    override val cause: Throwable? = null
) : AppError.DomainError(message, cause) {

    /**
     * Error when required recipe ingredients are missing
     */
    class MissingIngredientsError(
        val missingIngredients: List<String>,
        override val message: String = "Recipe is missing required ingredients",
        override val cause: Throwable? = null
    ) : RecipeError(message, cause)

    /**
     * Error when recipe instructions are invalid or incomplete
     */
    class InvalidInstructionsError(
        val details: String? = null,
        override val message: String = "Recipe has invalid or incomplete instructions",
        override val cause: Throwable? = null
    ) : RecipeError(message, cause)

    /**
     * Error when trying to rate your own recipe
     */
    class SelfRatingError(
        val recipeId: String,
        override val message: String = "You cannot rate your own recipe",
        override val cause: Throwable? = null
    ) : RecipeError(message, cause)

    /**
     * Error when recipe content violates community guidelines
     */
    class ContentViolationError(
        val violationType: String,
        override val message: String = "Recipe content violates community guidelines",
        override val cause: Throwable? = null
    ) : RecipeError(message, cause)

    /**
     * Error when recipe image upload fails
     */
    class ImageUploadError(
        val reason: String? = null,
        override val message: String = "Failed to upload recipe image",
        override val cause: Throwable? = null
    ) : RecipeError(message, cause)

    /**
     * Error when trying to modify a recipe without permission
     */
    class RecipeAccessError(
        val recipeId: String,
        val action: String,
        override val message: String = "You don't have permission to $action this recipe",
        override val cause: Throwable? = null
    ) : RecipeError(message, cause)
}