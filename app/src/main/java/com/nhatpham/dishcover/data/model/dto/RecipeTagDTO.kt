package com.nhatpham.dishcover.data.model.dto

import com.google.firebase.Timestamp

data class RecipeTagDto(
    val recipeTagId: String? = null,
    val recipeId: String? = null,
    val tagName: String? = null,
    val createdAt: Timestamp? = null
)
