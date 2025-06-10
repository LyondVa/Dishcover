// Cookbook.kt
package com.nhatpham.dishcover.domain.model.cookbook

import com.google.firebase.Timestamp

/**
 * Core cookbook domain model
 */
data class Cookbook(
    val cookbookId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val coverImage: String? = null,
    val isPublic: Boolean = true,
    val isCollaborative: Boolean = false,
    val tags: List<String> = emptyList(),
    val recipeCount: Int = 0,
    val followerCount: Int = 0,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val isFeatured: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * Cookbook list item for UI lists
 */
data class CookbookListItem(
    val cookbookId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val coverImage: String? = null,
    val isPublic: Boolean = true,
    val tags: List<String> = emptyList(),
    val recipeCount: Int = 0,
    val followerCount: Int = 0,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val isFeatured: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Cookbook recipe association
 */
data class CookbookRecipe(
    val cookbookRecipeId: String = "",
    val cookbookId: String = "",
    val recipeId: String = "",
    val addedBy: String = "",
    val notes: String? = null,
    val displayOrder: Int = 0,
    val addedAt: Timestamp = Timestamp.now()
)

/**
 * Cookbook collaboration permissions
 */
data class CookbookCollaborator(
    val collaboratorId: String = "",
    val cookbookId: String = "",
    val userId: String = "",
    val role: CookbookRole = CookbookRole.VIEWER,
    val invitedBy: String = "",
    val invitedAt: Timestamp = Timestamp.now(),
    val acceptedAt: Timestamp? = null,
    val status: CollaboratorStatus = CollaboratorStatus.PENDING
)

/**
 * Cookbook follow relationship
 */
data class CookbookFollower(
    val followId: String = "",
    val cookbookId: String = "",
    val userId: String = "",
    val followedAt: Timestamp = Timestamp.now()
)

/**
 * Cookbook like relationship
 */
data class CookbookLike(
    val likeId: String = "",
    val cookbookId: String = "",
    val userId: String = "",
    val likedAt: Timestamp = Timestamp.now()
)

/**
 * Cookbook collaboration roles
 */
enum class CookbookRole {
    OWNER,       // Full control
    EDITOR,      // Can add/remove recipes, edit cookbook
    CONTRIBUTOR, // Can add recipes only
    VIEWER       // Read-only access
}

/**
 * Collaborator invitation status
 */
enum class CollaboratorStatus {
    PENDING,    // Invitation sent
    ACCEPTED,   // Active collaborator
    DECLINED,   // Invitation declined
    REMOVED     // Removed from cookbook
}