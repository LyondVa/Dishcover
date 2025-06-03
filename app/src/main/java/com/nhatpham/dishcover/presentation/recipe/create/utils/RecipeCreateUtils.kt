package com.nhatpham.dishcover.presentation.recipe.create.utils

import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState

/**
 * Utility functions for recipe creation validation and state management
 */
object RecipeCreateUtils {

    /**
     * Determines if the user can proceed to the next step based on current step validation
     */
    fun canProceedToNextStep(currentStep: Int, state: RecipeCreateState): Boolean {
        return when (currentStep) {
            1 -> state.title.isNotBlank() // Basic info step - title is required
            2 -> state.ingredients.isNotEmpty() // Ingredients step - at least one ingredient required
            3 -> true // Categories step (optional)
            4 -> true // Review step - always can proceed (finish)
            else -> false
        }
    }

    /**
     * Gets the validation errors for the current step
     */
    fun getStepErrors(currentStep: Int, state: RecipeCreateState): List<String> {
        return when (currentStep) {
            1 -> {
                val errors = mutableListOf<String>()
                if (state.title.isBlank()) errors.add("Recipe name is required")
                if (state.titleError != null) errors.add(state.titleError!!)
                errors
            }
            2 -> {
                val errors = mutableListOf<String>()
                if (state.ingredients.isEmpty()) errors.add("At least one ingredient is required")
                if (state.ingredientsError != null) errors.add(state.ingredientsError!!)
                errors
            }
            else -> emptyList()
        }
    }

    /**
     * Gets the completion percentage for the recipe creation process
     */
    fun getCompletionPercentage(state: RecipeCreateState): Float {
        var completedItems = 0
        val totalItems = 6 // Title, Image, Time, Description, Ingredients, Instructions

        if (state.title.isNotBlank()) completedItems++
        if (state.coverImageUri != null) completedItems++
        if (state.prepTime.isNotBlank()) completedItems++
        if (state.description.isNotBlank()) completedItems++
        if (state.ingredients.isNotEmpty()) completedItems++
        if (state.instructions.isNotBlank()) completedItems++

        return completedItems.toFloat() / totalItems.toFloat()
    }

    /**
     * Gets a user-friendly step name
     */
    fun getStepName(step: Int): String {
        return when (step) {
            1 -> "Basic Information"
            2 -> "Ingredients & Instructions"
            3 -> "Categories"
            4 -> "Review"
            else -> "Unknown Step"
        }
    }
}