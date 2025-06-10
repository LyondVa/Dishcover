// CookbookMappers.kt
package com.nhatpham.dishcover.data.mapper

import com.google.firebase.Timestamp
import com.nhatpham.dishcover.data.model.dto.cookbook.*
import com.nhatpham.dishcover.domain.model.cookbook.*
import com.nhatpham.dishcover.domain.repository.CookbookStats

// Cookbook mapping
fun CookbookDto.toDomain(): Cookbook {
    return Cookbook(
        cookbookId = this.cookbookId ?: "",
        userId = this.userId ?: "",
        title = this.title ?: "",
        description = this.description,
        coverImage = this.coverImage,
        isPublic = this.public != false, // Map from 'public' to 'isPublic'
        isCollaborative = this.collaborative == true, // Map from 'collaborative' to 'isCollaborative'
        tags = this.tags ?: emptyList(),
        recipeCount = this.recipeCount ?: 0,
        followerCount = this.followerCount ?: 0,
        likeCount = this.likeCount ?: 0,
        viewCount = this.viewCount ?: 0,
        isFeatured = this.featured == true, // Map from 'featured' to 'isFeatured'
        createdAt = this.createdAt ?: Timestamp.now(),
        updatedAt = this.updatedAt ?: Timestamp.now()
    )
}

fun Cookbook.toDto(): CookbookDto {
    return CookbookDto(
        cookbookId = this.cookbookId,
        userId = this.userId,
        title = this.title,
        description = this.description,
        coverImage = this.coverImage,
        public = this.isPublic, // Map to 'public' for Firebase
        collaborative = this.isCollaborative, // Map to 'collaborative' for Firebase
        tags = this.tags,
        recipeCount = this.recipeCount,
        followerCount = this.followerCount,
        likeCount = this.likeCount,
        viewCount = this.viewCount,
        featured = this.isFeatured, // Map to 'featured' for Firebase
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

// Cookbook to CookbookListItem mapping
fun Cookbook.toListItem(): CookbookListItem {
    return CookbookListItem(
        cookbookId = this.cookbookId,
        userId = this.userId,
        title = this.title,
        description = this.description,
        coverImage = this.coverImage,
        isPublic = this.isPublic,
        tags = this.tags,
        recipeCount = this.recipeCount,
        followerCount = this.followerCount,
        likeCount = this.likeCount,
        viewCount = this.viewCount,
        isFeatured = this.isFeatured,
        createdAt = this.createdAt
    )
}

fun CookbookDto.toListItem(): CookbookListItem {
    return CookbookListItem(
        cookbookId = this.cookbookId ?: "",
        userId = this.userId ?: "",
        title = this.title ?: "",
        description = this.description,
        coverImage = this.coverImage,
        isPublic = this.public != false,
        tags = this.tags ?: emptyList(),
        recipeCount = this.recipeCount ?: 0,
        followerCount = this.followerCount ?: 0,
        likeCount = this.likeCount ?: 0,
        viewCount = this.viewCount ?: 0,
        isFeatured = this.featured == true,
        createdAt = this.createdAt ?: Timestamp.now()
    )
}

// CookbookRecipe mapping
fun CookbookRecipeDto.toDomain(): CookbookRecipe {
    return CookbookRecipe(
        cookbookRecipeId = this.cookbookRecipeId ?: "",
        cookbookId = this.cookbookId ?: "",
        recipeId = this.recipeId ?: "",
        addedBy = this.addedBy ?: "",
        notes = this.notes,
        displayOrder = this.displayOrder ?: 0,
        addedAt = this.addedAt ?: Timestamp.now()
    )
}

fun CookbookRecipe.toDto(): CookbookRecipeDto {
    return CookbookRecipeDto(
        cookbookRecipeId = this.cookbookRecipeId,
        cookbookId = this.cookbookId,
        recipeId = this.recipeId,
        addedBy = this.addedBy,
        notes = this.notes,
        displayOrder = this.displayOrder,
        addedAt = this.addedAt
    )
}

// CookbookCollaborator mapping
fun CookbookCollaboratorDto.toDomain(): CookbookCollaborator {
    return CookbookCollaborator(
        collaboratorId = this.collaboratorId ?: "",
        cookbookId = this.cookbookId ?: "",
        userId = this.userId ?: "",
        role = when (this.role) {
            "OWNER" -> CookbookRole.OWNER
            "EDITOR" -> CookbookRole.EDITOR
            "CONTRIBUTOR" -> CookbookRole.CONTRIBUTOR
            "VIEWER" -> CookbookRole.VIEWER
            else -> CookbookRole.VIEWER
        },
        invitedBy = this.invitedBy ?: "",
        invitedAt = this.invitedAt ?: Timestamp.now(),
        acceptedAt = this.acceptedAt,
        status = when (this.status) {
            "PENDING" -> CollaboratorStatus.PENDING
            "ACCEPTED" -> CollaboratorStatus.ACCEPTED
            "DECLINED" -> CollaboratorStatus.DECLINED
            "REMOVED" -> CollaboratorStatus.REMOVED
            else -> CollaboratorStatus.PENDING
        }
    )
}

fun CookbookCollaborator.toDto(): CookbookCollaboratorDto {
    return CookbookCollaboratorDto(
        collaboratorId = this.collaboratorId,
        cookbookId = this.cookbookId,
        userId = this.userId,
        role = this.role.name,
        invitedBy = this.invitedBy,
        invitedAt = this.invitedAt,
        acceptedAt = this.acceptedAt,
        status = this.status.name
    )
}

// CookbookFollower mapping
fun CookbookFollowerDto.toDomain(): CookbookFollower {
    return CookbookFollower(
        followId = this.followId ?: "",
        cookbookId = this.cookbookId ?: "",
        userId = this.userId ?: "",
        followedAt = this.followedAt ?: Timestamp.now()
    )
}

fun CookbookFollower.toDto(): CookbookFollowerDto {
    return CookbookFollowerDto(
        followId = this.followId,
        cookbookId = this.cookbookId,
        userId = this.userId,
        followedAt = this.followedAt
    )
}

// CookbookLike mapping
fun CookbookLikeDto.toDomain(): CookbookLike {
    return CookbookLike(
        likeId = this.likeId ?: "",
        cookbookId = this.cookbookId ?: "",
        userId = this.userId ?: "",
        likedAt = this.likedAt ?: Timestamp.now()
    )
}

fun CookbookLike.toDto(): CookbookLikeDto {
    return CookbookLikeDto(
        likeId = this.likeId,
        cookbookId = this.cookbookId,
        userId = this.userId,
        likedAt = this.likedAt
    )
}

// CookbookStats mapping
fun CookbookAnalyticsDto.toDomain(): CookbookStats {
    return CookbookStats(
        cookbookId = this.cookbookId ?: "",
        totalViews = this.totalViews ?: 0,
        totalLikes = this.totalLikes ?: 0,
        totalFollowers = this.totalFollowers ?: 0,
        totalRecipes = this.totalRecipes ?: 0,
        totalCollaborators = this.totalCollaborators ?: 0,
        viewsThisWeek = this.viewsThisWeek ?: 0,
        viewsThisMonth = this.viewsThisMonth ?: 0
    )
}