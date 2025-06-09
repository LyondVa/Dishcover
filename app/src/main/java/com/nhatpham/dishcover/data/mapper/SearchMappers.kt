// SearchMappers.kt - Search result mappers
package com.nhatpham.dishcover.data.mapper.search

import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.model.search.*
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.domain.model.user.UserProfile

// User to UserSearchResult mapping
fun User.toSearchResult(
    followerCount: Int = 0,
    recipeCount: Int = 0,
    matchedFields: List<SearchMatchField> = emptyList()
): UserSearchResult {
    return UserSearchResult(
        userId = this.userId,
        username = this.username,
        email = this.email,
        profilePicture = this.profilePicture,
        bio = this.bio,
        isVerified = this.isVerified,
        followerCount = followerCount,
        recipeCount = recipeCount,
        createdAt = this.createdAt,
        matchedFields = matchedFields
    )
}

fun UserProfile.toSearchResult(
    matchedFields: List<SearchMatchField> = emptyList()
): UserSearchResult {
    return UserSearchResult(
        userId = this.userId,
        username = this.username,
        email = this.email,
        profilePicture = this.profilePicture,
        bio = this.bio,
        isVerified = false, // UserProfile doesn't have this field
        followerCount = this.followerCount,
        recipeCount = this.recipeCount,
        createdAt = this.createdAt,
        matchedFields = matchedFields
    )
}

// Post to PostSearchResult mapping
fun Post.toSearchResult(
    matchedFields: List<SearchMatchField> = emptyList()
): PostSearchResult {
    return PostSearchResult(
        postId = this.postId,
        userId = this.userId,
        username = this.username,
        content = this.content,
        firstImageUrl = this.imageUrls.firstOrNull(),
        postType = this.postType.name,
        hashtags = this.hashtags,
        taggedUsers = this.taggedUsers,
        location = this.location,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        shareCount = this.shareCount,
        isPublic = this.isPublic,
        createdAt = this.createdAt,
        matchedFields = matchedFields
    )
}

fun PostListItem.toSearchResult(
    hashtags: List<String> = emptyList(),
    taggedUsers: List<String> = emptyList(),
    location: String? = null,
    matchedFields: List<SearchMatchField> = emptyList()
): PostSearchResult {
    return PostSearchResult(
        postId = this.postId,
        userId = this.userId,
        username = this.username,
        content = this.content,
        firstImageUrl = this.firstImageUrl,
        postType = this.postType.name,
        hashtags = hashtags,
        taggedUsers = taggedUsers,
        location = location,
        likeCount = this.likeCount,
        commentCount = this.commentCount,
        shareCount = this.shareCount,
        isPublic = true, // PostListItem doesn't store this field directly
        createdAt = this.createdAt,
        matchedFields = matchedFields
    )
}

// Recipe to RecipeSearchResult mapping
fun Recipe.toSearchResult(
    matchedFields: List<SearchMatchField> = emptyList()
): RecipeSearchResult {
    return RecipeSearchResult(
        recipeId = this.recipeId,
        userId = this.userId,
        title = this.title,
        description = this.description,
        coverImage = this.coverImage,
        prepTime = this.prepTime,
        cookTime = this.cookTime,
        servings = this.servings,
        difficultyLevel = this.difficultyLevel,
        tags = this.tags,
        likeCount = this.likeCount,
        viewCount = this.viewCount,
        isPublic = this.isPublic,
        isFeatured = this.isFeatured,
        createdAt = this.createdAt,
        matchedFields = matchedFields
    )
}

fun RecipeListItem.toSearchResult(
    matchedFields: List<SearchMatchField> = emptyList()
): RecipeSearchResult {
    return RecipeSearchResult(
        recipeId = this.recipeId,
        userId = this.userId,
        title = this.title,
        description = this.description,
        coverImage = this.coverImage,
        prepTime = this.prepTime,
        cookTime = this.cookTime,
        servings = this.servings,
        difficultyLevel = this.difficultyLevel,
        tags = this.tags,
        likeCount = this.likeCount,
        viewCount = this.viewCount,
        isPublic = this.isPublic,
        isFeatured = this.isFeatured,
        createdAt = this.createdAt,
        matchedFields = matchedFields
    )
}

// Search match field helpers
fun createMatchField(
    fieldName: String,
    matchedText: String,
    confidence: Float = 1.0f
): SearchMatchField {
    return SearchMatchField(
        fieldName = fieldName,
        matchedText = matchedText,
        confidence = confidence
    )
}

// Helper functions to determine matching fields
fun findUserMatchFields(user: User, query: String): List<SearchMatchField> {
    val matchFields = mutableListOf<SearchMatchField>()
    val lowerQuery = query.lowercase()

    if (user.username.lowercase().contains(lowerQuery)) {
        matchFields.add(createMatchField("username", user.username, 1.0f))
    }

    user.bio?.let { bio ->
        if (bio.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("bio", bio, 0.8f))
        }
    }

    return matchFields
}

fun findPostMatchFields(post: Post, query: String): List<SearchMatchField> {
    val matchFields = mutableListOf<SearchMatchField>()
    val lowerQuery = query.lowercase()

    if (post.content.lowercase().contains(lowerQuery)) {
        matchFields.add(createMatchField("content", post.content, 1.0f))
    }

    post.hashtags.forEach { hashtag ->
        if (hashtag.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("hashtags", hashtag, 0.9f))
        }
    }

    post.location?.let { location ->
        if (location.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("location", location, 0.7f))
        }
    }

    return matchFields
}

fun findRecipeMatchFields(recipe: Recipe, query: String): List<SearchMatchField> {
    val matchFields = mutableListOf<SearchMatchField>()
    val lowerQuery = query.lowercase()

    if (recipe.title.lowercase().contains(lowerQuery)) {
        matchFields.add(createMatchField("title", recipe.title, 1.0f))
    }

    recipe.description?.let { description ->
        if (description.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("description", description, 0.9f))
        }
    }

    recipe.tags.forEach { tag ->
        if (tag.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("tags", tag, 0.8f))
        }
    }

    if (recipe.difficultyLevel.lowercase().contains(lowerQuery)) {
        matchFields.add(createMatchField("difficultyLevel", recipe.difficultyLevel, 0.6f))
    }

    return matchFields
}