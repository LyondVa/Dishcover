package com.nhatpham.dishcover.data.model.dto.feed

import com.google.firebase.Timestamp

data class PostRecipeReferenceDto(
    val referenceId: String? = null,
    val postId: String? = null,
    val recipeId: String? = null,
    val displayText: String? = null,
    val position: Int? = null,
    val createdAt: Timestamp? = null,
    val coverImage: String? = null,
)
