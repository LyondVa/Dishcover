package com.nhatpham.dishcover.data.model.dto

import com.google.firebase.Timestamp

data class RecipeLikeDto(
    val likeId: String? = null,
    val userId: String? = null,
    val recipeId: String? = null,
    val likedAt: Timestamp? = null
)