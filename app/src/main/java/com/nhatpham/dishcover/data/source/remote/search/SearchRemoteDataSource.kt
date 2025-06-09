// SearchRemoteDataSource.kt - Remote data source for search operations
package com.nhatpham.dishcover.data.source.remote.search

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.mapper.search.*
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.data.source.remote.feed.PostRemoteDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.domain.model.search.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class SearchRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDataSource: FirestoreUserDataSource,
    private val postDataSource: PostRemoteDataSource,
    private val recipeDataSource: RecipeRemoteDataSource
) {
    private val searchAnalyticsCollection = firestore.collection("SEARCH_ANALYTICS")
    private val searchHistoryCollection = firestore.collection("USER_SEARCH_HISTORY")
    private val trendingSearchesCollection = firestore.collection("TRENDING_SEARCHES")

    /**
     * Unified search across all content types
     */
    suspend fun searchAll(
        query: String,
        filters: SearchFilters,
        pagination: SearchPagination
    ): SearchResult {
        return try {
            coroutineScope {
                val userSearchDeferred = async {
                    if (filters.searchType == SearchType.ALL || filters.searchType == SearchType.USERS) {
                        searchUsers(query, pagination.limit)
                    } else emptyList()
                }

                val postSearchDeferred = async {
                    if (filters.searchType == SearchType.ALL || filters.searchType == SearchType.POSTS) {
                        searchPosts(query, filters, pagination.limit)
                    } else emptyList()
                }

                val recipeSearchDeferred = async {
                    if (filters.searchType == SearchType.ALL || filters.searchType == SearchType.RECIPES) {
                        searchRecipes(query, filters, pagination.limit)
                    } else emptyList()
                }

                val (users, posts, recipes) = listOf(
                    userSearchDeferred,
                    postSearchDeferred,
                    recipeSearchDeferred
                ).awaitAll()

                SearchResult(
                    query = query,
                    searchType = filters.searchType,
                    users = users as List<UserSearchResult>,
                    posts = posts as List<PostSearchResult>,
                    recipes = recipes as List<RecipeSearchResult>,
                    totalResults = (users as List<*>).size + (posts as List<*>).size + (recipes as List<*>).size,
                    searchedAt = Timestamp.now()
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in unified search")
            SearchResult(query = query, searchType = filters.searchType)
        }
    }

    /**
     * Search users using existing Firestore queries
     */
    suspend fun searchUsers(query: String, limit: Int): List<UserSearchResult> {
        return try {
            val usersCollection = firestore.collection("USERS")

            // Search by username (case-insensitive)
            val usernameResults = usersCollection
                .orderBy("username")
                .startAt(query.lowercase())
                .endAt(query.lowercase() + "\uf8ff")
                .limit(limit.toLong())
                .get()
                .await()

            val userResults = mutableListOf<UserSearchResult>()

            usernameResults.documents.forEach { doc ->
                try {
                    val user = userDataSource.getUserById(doc.id)
                    user?.let {
                        val matchFields = findUserMatchFields(it, query)
                        userResults.add(it.toSearchResult(matchedFields = matchFields))
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Error processing user search result: ${doc.id}")
                }
            }

            userResults.take(limit)
        } catch (e: Exception) {
            Timber.e(e, "Error searching users")
            emptyList()
        }
    }

    /**
     * Search posts using existing functionality
     */
    suspend fun searchPosts(query: String, filters: SearchFilters, limit: Int): List<PostSearchResult> {
        return try {
            val postsCollection = firestore.collection("POSTS")
            var queryBuilder = postsCollection.whereEqualTo("public", filters.isPublicOnly)

            // Apply post type filters if specified
            if (filters.postTypes.isNotEmpty()) {
                queryBuilder = queryBuilder.whereIn("postType", filters.postTypes)
            }

            // Search in content field
            val results = queryBuilder
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val postResults = mutableListOf<PostSearchResult>()

            results.documents.forEach { doc ->
                try {
                    val content = doc.getString("content") ?: ""
                    val hashtags = doc.get("hashtags") as? List<String> ?: emptyList()
                    val location = doc.getString("location")

                    // Check if query matches content, hashtags, or location
                    val matchesContent = content.contains(query, ignoreCase = true)
                    val matchesHashtags = hashtags.any { it.contains(query, ignoreCase = true) }
                    val matchesLocation = location?.contains(query, ignoreCase = true) ?: false

                    if (matchesContent || matchesHashtags || matchesLocation) {
                        val postSearchResult = PostSearchResult(
                            postId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "",
                            content = content,
                            firstImageUrl = (doc.get("imageUrls") as? List<String>)?.firstOrNull(),
                            postType = doc.getString("postType") ?: "TEXT",
                            hashtags = hashtags,
                            taggedUsers = doc.get("taggedUsers") as? List<String> ?: emptyList(),
                            location = location,
                            likeCount = (doc.getLong("likeCount") ?: 0).toInt(),
                            commentCount = (doc.getLong("commentCount") ?: 0).toInt(),
                            shareCount = (doc.getLong("shareCount") ?: 0).toInt(),
                            isPublic = doc.getBoolean("public") ?: true,
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            matchedFields = buildPostMatchFields(content, hashtags, location, query)
                        )
                        postResults.add(postSearchResult)
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Error processing post search result: ${doc.id}")
                }
            }

            postResults.take(limit)
        } catch (e: Exception) {
            Timber.e(e, "Error searching posts")
            emptyList()
        }
    }

    /**
     * Search recipes using existing functionality
     */
    suspend fun searchRecipes(query: String, filters: SearchFilters, limit: Int): List<RecipeSearchResult> {
        return try {
            val recipesCollection = firestore.collection("RECIPES")
            var queryBuilder = recipesCollection.whereEqualTo("public", filters.isPublicOnly)

            // Apply difficulty filters if specified
            if (filters.difficultyLevels.isNotEmpty()) {
                queryBuilder = queryBuilder.whereIn("difficultyLevel", filters.difficultyLevels)
            }

            // Apply featured filter if specified
            if (filters.isFeaturedOnly) {
                queryBuilder = queryBuilder.whereEqualTo("featured", true)
            }

            val results = queryBuilder
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val recipeResults = mutableListOf<RecipeSearchResult>()

            results.documents.forEach { doc ->
                try {
                    val title = doc.getString("title") ?: ""
                    val description = doc.getString("description")
                    val tags = doc.get("tags") as? List<String> ?: emptyList()
                    val difficultyLevel = doc.getString("difficultyLevel") ?: ""

                    // Check if query matches title, description, tags, or difficulty
                    val matchesTitle = title.contains(query, ignoreCase = true)
                    val matchesDescription = description?.contains(query, ignoreCase = true) ?: false
                    val matchesTags = tags.any { it.contains(query, ignoreCase = true) }
                    val matchesDifficulty = difficultyLevel.contains(query, ignoreCase = true)

                    if (matchesTitle || matchesDescription || matchesTags || matchesDifficulty) {
                        val recipeSearchResult = RecipeSearchResult(
                            recipeId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            title = title,
                            description = description,
                            coverImage = doc.getString("coverImage"),
                            prepTime = (doc.getLong("prepTime") ?: 0).toInt(),
                            cookTime = (doc.getLong("cookTime") ?: 0).toInt(),
                            servings = (doc.getLong("servings") ?: 0).toInt(),
                            difficultyLevel = difficultyLevel,
                            tags = tags,
                            likeCount = (doc.getLong("likeCount") ?: 0).toInt(),
                            viewCount = (doc.getLong("viewCount") ?: 0).toInt(),
                            isPublic = doc.getBoolean("isPublic") ?: true,
                            isFeatured = doc.getBoolean("isFeatured") ?: false,
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                            matchedFields = buildRecipeMatchFields(title, description, tags, difficultyLevel, query)
                        )
                        recipeResults.add(recipeSearchResult)
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Error processing recipe search result: ${doc.id}")
                }
            }

            recipeResults.take(limit)
        } catch (e: Exception) {
            Timber.e(e, "Error searching recipes")
            emptyList()
        }
    }

    /**
     * Save search query for analytics and history
     */
    suspend fun saveSearchQuery(userId: String, query: String, searchType: SearchType) {
        try {
            val historyData = mapOf(
                "userId" to userId,
                "query" to query,
                "searchType" to searchType.name,
                "timestamp" to Timestamp.now()
            )

            searchHistoryCollection.add(historyData).await()
        } catch (e: Exception) {
            Timber.e(e, "Error saving search query")
        }
    }

    /**
     * Get recent searches for user
     */
    suspend fun getRecentSearches(userId: String, limit: Int): List<String> {
        return try {
            val results = searchHistoryCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            results.documents.mapNotNull { it.getString("query") }
        } catch (e: Exception) {
            Timber.e(e, "Error getting recent searches")
            emptyList()
        }
    }

    /**
     * Log search analytics
     */
    suspend fun logSearchAnalytics(analytics: SearchAnalytics) {
        try {
            val analyticsData = mapOf(
                "query" to analytics.query,
                "searchType" to analytics.searchType.name,
                "resultCount" to analytics.resultCount,
                "searchDuration" to analytics.searchDuration,
                "userId" to analytics.userId,
                "timestamp" to analytics.timestamp
            )

            searchAnalyticsCollection.add(analyticsData).await()
        } catch (e: Exception) {
            Timber.e(e, "Error logging search analytics")
        }
    }

    // Helper functions for building match fields
    private fun buildPostMatchFields(
        content: String,
        hashtags: List<String>,
        location: String?,
        query: String
    ): List<SearchMatchField> {
        val matchFields = mutableListOf<SearchMatchField>()
        val lowerQuery = query.lowercase()

        if (content.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("content", content, 1.0f))
        }

        hashtags.forEach { hashtag ->
            if (hashtag.lowercase().contains(lowerQuery)) {
                matchFields.add(createMatchField("hashtags", hashtag, 0.9f))
            }
        }

        location?.let { loc ->
            if (loc.lowercase().contains(lowerQuery)) {
                matchFields.add(createMatchField("location", loc, 0.7f))
            }
        }

        return matchFields
    }

    private fun buildRecipeMatchFields(
        title: String,
        description: String?,
        tags: List<String>,
        difficultyLevel: String,
        query: String
    ): List<SearchMatchField> {
        val matchFields = mutableListOf<SearchMatchField>()
        val lowerQuery = query.lowercase()

        if (title.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("title", title, 1.0f))
        }

        description?.let { desc ->
            if (desc.lowercase().contains(lowerQuery)) {
                matchFields.add(createMatchField("description", desc, 0.9f))
            }
        }

        tags.forEach { tag ->
            if (tag.lowercase().contains(lowerQuery)) {
                matchFields.add(createMatchField("tags", tag, 0.8f))
            }
        }

        if (difficultyLevel.lowercase().contains(lowerQuery)) {
            matchFields.add(createMatchField("difficultyLevel", difficultyLevel, 0.6f))
        }

        return matchFields
    }
}