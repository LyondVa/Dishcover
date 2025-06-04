package com.nhatpham.dishcover.domain.model

import com.google.firebase.Timestamp

data class Ingredient(
    val ingredientId: String = "",
    val name: String = "",
    val description: String? = null,
    val category: String? = null,
    val isSystemIngredient: Boolean = false,
    val createdBy: String? = null,
    val createdAt: Timestamp? = Timestamp.now()
)