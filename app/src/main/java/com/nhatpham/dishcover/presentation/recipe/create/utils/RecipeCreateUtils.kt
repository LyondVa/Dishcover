package com.nhatpham.dishcover.presentation.recipe.create.utils

import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState

object RecipeCreateUtils {

    fun canProceedToNextStep(currentStep: Int, state: RecipeCreateState): Boolean {
        return when (currentStep) {
            1 -> state.title.isNotBlank()
            2 -> state.ingredients.isNotEmpty() && state.instructionSteps.any { it.isNotBlank() }
            3 -> true
            4 -> true
            else -> false
        }
    }

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
                if (state.instructionSteps.none { it.isNotBlank() }) errors.add("At least one instruction step is required")
                if (state.ingredientsError != null) errors.add(state.ingredientsError!!)
                if (state.instructionsError != null) errors.add(state.instructionsError!!)
                errors
            }
            else -> emptyList()
        }
    }

    fun getCompletionPercentage(state: RecipeCreateState): Float {
        var completedItems = 0
        val totalItems = 6

        if (state.title.isNotBlank()) completedItems++
        if (state.coverImageUri != null) completedItems++
        if (state.prepTime.isNotBlank()) completedItems++
        if (state.description.isNotBlank()) completedItems++
        if (state.ingredients.isNotEmpty()) completedItems++
        if (state.instructionSteps.any { it.isNotBlank() }) completedItems++

        return completedItems.toFloat() / totalItems.toFloat()
    }

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