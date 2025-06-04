package com.nhatpham.dishcover.data.model.dto

import com.google.firebase.Timestamp

data class RecipeCategoryDto(
    val categoryId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val isSystemCategory: Boolean? = null,
    val createdBy: String? = null,
    val createdAt: Timestamp? = null
)