package com.nhatpham.dishcover.data.model.dto

import com.google.firebase.Timestamp

data class RecipeViewDto(
    val viewId: String? = null,
    val userId: String? = null,
    val recipeId: String? = null,
    val viewedAt: Timestamp? = null,
    val viewDuration: Int? = null // in seconds
)