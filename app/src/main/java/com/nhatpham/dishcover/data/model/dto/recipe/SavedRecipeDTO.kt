package com.nhatpham.dishcover.data.model.dto.recipe

import com.google.firebase.Timestamp

data class SavedRecipeDto(
    val savedRecipeId: String? = null,
    val userId: String? = null,
    val recipeId: String? = null,
    val savedCategory: String? = null, // "favorite", "to_try", "custom"
    val savedAt: Timestamp? = null,
    val notes: String? = null
)
