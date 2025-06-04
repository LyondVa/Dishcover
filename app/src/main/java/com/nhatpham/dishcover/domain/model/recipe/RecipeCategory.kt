package com.nhatpham.dishcover.domain.model.recipe

import com.google.firebase.Timestamp

data class RecipeCategory(
    val categoryId: String = "",
    val name: String = "",
    val description: String? = null,
    val isSystemCategory: Boolean = false,
    val createdBy: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)