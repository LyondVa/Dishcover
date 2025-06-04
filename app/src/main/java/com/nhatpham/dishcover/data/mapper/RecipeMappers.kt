package com.nhatpham.dishcover.data.mapper

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.model.dto.*
import com.nhatpham.dishcover.domain.model.*

// Recipe mapping
fun RecipeDto.toDomain(): Recipe {
    return Recipe(
        recipeId = this.recipeId ?: "",
        userId = this.userId ?: "",
        title = this.title ?: "",
        description = this.description,
        prepTime = this.prepTime ?: 0,
        cookTime = this.cookTime ?: 0,
        servings = this.servings ?: 0,
        instructions = this.instructions ?: "",
        difficultyLevel = this.difficultyLevel ?: "Easy",
        coverImage = this.coverImage,
        createdAt = this.createdAt ?: Timestamp.now(),
        updatedAt = this.updatedAt ?: Timestamp.now(),
        isPublic = this.isPublic != false,
        viewCount = this.viewCount ?: 0,
        likeCount = this.likeCount ?: 0,
        isFeatured = this.isFeatured == true,
        ingredients = emptyList(), // Will be populated separately
        tags = emptyList() // Will be populated separately
    )
}

fun Recipe.toDto(): RecipeDto {
    return RecipeDto(
        recipeId = this.recipeId,
        userId = this.userId,
        title = this.title,
        description = this.description,
        prepTime = this.prepTime,
        cookTime = this.cookTime,
        servings = this.servings,
        instructions = this.instructions,
        difficultyLevel = this.difficultyLevel,
        coverImage = this.coverImage,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isPublic = this.isPublic,
        viewCount = this.viewCount,
        likeCount = this.likeCount,
        isFeatured = this.isFeatured
    )
}

// Recipe to RecipeListItem mapping
fun Recipe.toListItem(): RecipeListItem {
    return RecipeListItem(
        recipeId = this.recipeId,
        title = this.title,
        description = this.description,
        coverImage = this.coverImage,
        prepTime = this.prepTime,
        cookTime = this.cookTime,
        servings = this.servings,
        difficultyLevel = this.difficultyLevel,
        likeCount = this.likeCount,
        viewCount = this.viewCount,
        isPublic = this.isPublic,
        isFeatured = this.isFeatured,
        userId = this.userId,
        createdAt = this.createdAt,
        tags = this.tags
    )
}

fun RecipeDto.toListItem(): RecipeListItem {
    return RecipeListItem(
        recipeId = this.recipeId ?: "",
        title = this.title ?: "",
        description = this.description,
        coverImage = this.coverImage,
        prepTime = this.prepTime ?: 0,
        cookTime = this.cookTime ?: 0,
        servings = this.servings ?: 0,
        difficultyLevel = this.difficultyLevel ?: "Easy",
        likeCount = this.likeCount ?: 0,
        viewCount = this.viewCount ?: 0,
        isPublic = this.isPublic != false,
        isFeatured = this.isFeatured == true,
        userId = this.userId ?: "",
        createdAt = this.createdAt ?: Timestamp.now(),
        tags = emptyList()
    )
}

// Ingredient mapping
fun IngredientDto.toDomain(): Ingredient {
    return Ingredient(
        ingredientId = this.ingredientId ?: "",
        name = this.name ?: "",
        description = this.description,
        category = this.category,
        isSystemIngredient = this.isSystemIngredient == true,
        createdBy = this.createdBy,
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun Ingredient.toDto(): IngredientDto {
    return IngredientDto(
        ingredientId = this.ingredientId,
        name = this.name,
        description = this.description,
        category = this.category,
        isSystemIngredient = this.isSystemIngredient,
        createdBy = this.createdBy,
        createdAt = this.createdAt
    )
}

// RecipeIngredient mapping
fun RecipeIngredientDto.toDomain(ingredient: Ingredient): RecipeIngredient {
    return RecipeIngredient(
        recipeIngredientId = this.recipeIngredientId ?: "",
        recipeId = this.recipeId ?: "",
        ingredientId = this.ingredientId ?: "",
        quantity = this.quantity ?: "",
        unit = this.unit ?: "",
        notes = this.notes,
        displayOrder = this.displayOrder ?: 0,
        ingredient = ingredient
    )
}

fun RecipeIngredient.toDto(): RecipeIngredientDto {
    return RecipeIngredientDto(
        recipeIngredientId = this.recipeIngredientId,
        recipeId = this.recipeId,
        ingredientId = this.ingredientId,
        quantity = this.quantity,
        unit = this.unit,
        notes = this.notes,
        displayOrder = this.displayOrder
    )
}

// RecipeCategory mapping
fun RecipeCategoryDto.toDomain(): RecipeCategory {
    return RecipeCategory(
        categoryId = this.categoryId ?: "",
        name = this.name ?: "",
        description = this.description,
        isSystemCategory = this.isSystemCategory == true,
        createdBy = this.createdBy,
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

fun RecipeCategory.toDto(): RecipeCategoryDto {
    return RecipeCategoryDto(
        categoryId = this.categoryId,
        name = this.name,
        description = this.description,
        isSystemCategory = this.isSystemCategory,
        createdBy = this.createdBy,
        createdAt = this.createdAt
    )
}

// RecipeStep mapping
fun RecipeStepDto.toDomain(): RecipeStep {
    return RecipeStep(
        stepId = this.stepId ?: "",
        recipeId = this.recipeId ?: "",
        stepNumber = this.stepNumber ?: 0,
        instruction = this.instruction ?: "",
        duration = this.duration,
        temperature = this.temperature,
        imageUrl = this.imageUrl,
        notes = this.notes
    )
}

fun RecipeStep.toDto(): RecipeStepDto {
    return RecipeStepDto(
        stepId = this.stepId,
        recipeId = this.recipeId,
        stepNumber = this.stepNumber,
        instruction = this.instruction,
        duration = this.duration,
        temperature = this.temperature,
        imageUrl = this.imageUrl,
        notes = this.notes
    )
}