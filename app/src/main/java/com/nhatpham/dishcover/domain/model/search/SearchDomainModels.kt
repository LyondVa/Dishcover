// SearchDomainModels.kt - Search domain models
package com.nhatpham.dishcover.domain.model.search

import com.google.firebase.Timestamp

/**
 * Unified search result that can contain users, posts, or recipes
 */
data class SearchResult(
    val query: String = "",
    val searchType: SearchType = SearchType.ALL,
    val users: List<UserSearchResult> = emptyList(),
    val posts: List<PostSearchResult> = emptyList(),
    val recipes: List<RecipeSearchResult> = emptyList(),
    val totalResults: Int = 0,
    val searchedAt: Timestamp = Timestamp.now()
)

enum class SearchType {
    ALL, USERS, POSTS, RECIPES
}

/**
 * User search result - only using existing User domain fields
 */
data class UserSearchResult(
    val userId: String = "",
    val username: String = "",
    val email: String = "", // For admin/self viewing only
    val profilePicture: String? = null,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val followerCount: Int = 0, // From UserProfile
    val recipeCount: Int = 0, // From UserProfile
    val createdAt: Timestamp = Timestamp.now(),
    val matchedFields: List<SearchMatchField> = emptyList()
)

/**
 * Post search result - only using existing Post domain fields
 */
data class PostSearchResult(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val firstImageUrl: String? = null,
    val postType: String = "TEXT", // From PostType enum
    val hashtags: List<String> = emptyList(),
    val taggedUsers: List<String> = emptyList(),
    val location: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val isPublic: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val matchedFields: List<SearchMatchField> = emptyList()
)

/**
 * Recipe search result - only using existing Recipe domain fields
 */
data class RecipeSearchResult(
    val recipeId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String? = null,
    val coverImage: String? = null,
    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val servings: Int = 0,
    val difficultyLevel: String = "Easy",
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val isPublic: Boolean = true,
    val isFeatured: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val matchedFields: List<SearchMatchField> = emptyList()
)

/**
 * Represents which field(s) matched the search query
 */
data class SearchMatchField(
    val fieldName: String = "",
    val matchedText: String = "",
    val confidence: Float = 1.0f
)

/**
 * Search filters using only existing domain fields
 */
data class SearchFilters(
    val searchType: SearchType = SearchType.ALL,
    val dateRange: DateRange? = null,
    val postTypes: List<String> = emptyList(), // PostType values
    val difficultyLevels: List<String> = emptyList(), // Recipe difficulty
    val isPublicOnly: Boolean = true,
    val isFeaturedOnly: Boolean = false,
    val hasImages: Boolean? = null,
    val minLikes: Int? = null,
    val maxResults: Int = 20
)

data class DateRange(
    val startDate: Timestamp,
    val endDate: Timestamp
)

/**
 * Search pagination support
 */
data class SearchPagination(
    val offset: Int = 0,
    val limit: Int = 20,
    val hasMore: Boolean = false,
    val total: Int = 0
)

/**
 * Search analytics for query optimization
 */
data class SearchAnalytics(
    val query: String = "",
    val searchType: SearchType = SearchType.ALL,
    val resultCount: Int = 0,
    val searchDuration: Long = 0, // milliseconds
    val userId: String? = null,
    val timestamp: Timestamp = Timestamp.now()
)