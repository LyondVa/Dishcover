package com.nhatpham.dishcover.data.model.dto.recipe

import com.google.firebase.Timestamp

data class IngredientDto(
    val ingredientId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val isSystemIngredient: Boolean? = null,
    val createdBy: String? = null,
    val createdAt: Timestamp? = null
)