// CookbookDto.kt
package com.nhatpham.dishcover.data.model.dto.cookbook

import com.google.firebase.Timestamp

/**
 * Cookbook DTO for Firebase serialization
 * Note: Firebase automatically converts isPublic -> public
 */
data class CookbookDto(
    val cookbookId: String? = null,
    val userId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val coverImage: String? = null,
    val public: Boolean? = null, // Firebase field name for isPublic
    val collaborative: Boolean? = null, // Firebase field name for isCollaborative
    val tags: List<String>? = null,
    val recipeCount: Int? = null,
    val followerCount: Int? = null,
    val likeCount: Int? = null,
    val viewCount: Int? = null,
    val featured: Boolean? = null, // Firebase field name for isFeatured
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

/**
 * Cookbook recipe association DTO
 */
data class CookbookRecipeDto(
    val cookbookRecipeId: String? = null,
    val cookbookId: String? = null,
    val recipeId: String? = null,
    val addedBy: String? = null,
    val notes: String? = null,
    val displayOrder: Int? = null,
    val addedAt: Timestamp? = null
)

/**
 * Cookbook collaborator DTO
 */
data class CookbookCollaboratorDto(
    val collaboratorId: String? = null,
    val cookbookId: String? = null,
    val userId: String? = null,
    val role: String? = null, // CookbookRole enum as string
    val invitedBy: String? = null,
    val invitedAt: Timestamp? = null,
    val acceptedAt: Timestamp? = null,
    val status: String? = null // CollaboratorStatus enum as string
)

/**
 * Cookbook follower DTO
 */
data class CookbookFollowerDto(
    val followId: String? = null,
    val cookbookId: String? = null,
    val userId: String? = null,
    val followedAt: Timestamp? = null
)

/**
 * Cookbook like DTO
 */
data class CookbookLikeDto(
    val likeId: String? = null,
    val cookbookId: String? = null,
    val userId: String? = null,
    val likedAt: Timestamp? = null
)

/**
 * Cookbook view tracking DTO
 */
data class CookbookViewDto(
    val viewId: String? = null,
    val cookbookId: String? = null,
    val userId: String? = null,
    val viewedAt: Timestamp? = null,
    val sessionId: String? = null
)

/**
 * Cookbook analytics DTO
 */
data class CookbookAnalyticsDto(
    val cookbookId: String? = null,
    val totalViews: Int? = null,
    val totalLikes: Int? = null,
    val totalFollowers: Int? = null,
    val totalRecipes: Int? = null,
    val totalCollaborators: Int? = null,
    val viewsThisWeek: Int? = null,
    val viewsThisMonth: Int? = null,
    val lastUpdated: Timestamp? = null
)