// FeedLocalDataSource.kt
package com.nhatpham.dishcover.data.source.local

import com.nhatpham.dishcover.domain.model.feed.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FeedLocalDataSource @Inject constructor() {

    // Cache maps for domain models only
    private val postsCache = mutableMapOf<String, Post>()
    private val commentsCache = mutableMapOf<String, Comment>()
    private val userFeedCache = mutableMapOf<String, List<FeedItem>>()
    private val followingFeedCache = mutableMapOf<String, List<FeedItem>>()
    private val discoverFeedCache = mutableMapOf<String, List<FeedItem>>()
    private val userPostsCache = mutableMapOf<String, List<PostListItem>>()
    private val trendingPostsCache = mutableListOf<PostListItem>()
    private val popularPostsCache = mutableListOf<PostListItem>()
    private val postLikesCache = mutableMapOf<String, List<PostLike>>()
    private val postSharesCache = mutableMapOf<String, List<PostShare>>()
    private val postCommentsCache = mutableMapOf<String, List<Comment>>()
    private val commentRepliesCache = mutableMapOf<String, List<Comment>>()
    private val commentLikesCache = mutableMapOf<String, List<CommentLike>>()
    private val postRecipeReferencesCache = mutableMapOf<String, List<PostRecipeReference>>()
    private val postCookbookReferencesCache = mutableMapOf<String, List<PostCookbookReference>>()
    private val searchResultsCache = mutableMapOf<String, List<PostListItem>>()

    // Simple caches for non-domain features (using primitives)
    private val postLikeStatusCache = mutableMapOf<String, Boolean>() // "userId:postId" -> Boolean
    private val postShareStatusCache = mutableMapOf<String, Boolean>() // "userId:postId" -> Boolean
    private val commentLikeStatusCache = mutableMapOf<String, Boolean>() // "userId:commentId" -> Boolean
    private val postSaveStatusCache = mutableMapOf<String, Boolean>() // "userId:postId" -> Boolean
    private val userFollowingCache = mutableMapOf<String, List<String>>() // userId -> list of following IDs
    private val trendingHashtagsCache = mutableListOf<String>()
    private val postAnalyticsCache = mutableMapOf<String, Map<String, Any>>()
    private val userPostAnalyticsCache = mutableMapOf<String, Map<String, Any>>()

    // Post operations
    suspend fun savePost(post: Post) = withContext(Dispatchers.IO) {
        postsCache[post.postId] = post
    }

    suspend fun getPostById(postId: String): Post? = withContext(Dispatchers.IO) {
        return@withContext postsCache[postId]
    }

    suspend fun deletePost(postId: String) = withContext(Dispatchers.IO) {
        postsCache.remove(postId)

        // Clean up from other caches
        userPostsCache.forEach { (userId, posts) ->
            val updatedList = posts.filter { it.postId != postId }
            if (updatedList.size != posts.size) {
                userPostsCache[userId] = updatedList
            }
        }

        trendingPostsCache.removeAll { it.postId == postId }
        popularPostsCache.removeAll { it.postId == postId }

        // Clean up feed caches
        userFeedCache.forEach { (userId, feedItems) ->
            val updatedList = feedItems.filter { it.post?.postId != postId }
            if (updatedList.size != feedItems.size) {
                userFeedCache[userId] = updatedList
            }
        }

        followingFeedCache.forEach { (userId, feedItems) ->
            val updatedList = feedItems.filter { it.post?.postId != postId }
            if (updatedList.size != feedItems.size) {
                followingFeedCache[userId] = updatedList
            }
        }

        discoverFeedCache.forEach { (userId, feedItems) ->
            val updatedList = feedItems.filter { it.post?.postId != postId }
            if (updatedList.size != feedItems.size) {
                discoverFeedCache[userId] = updatedList
            }
        }

        // Clean up related data
        postLikesCache.remove(postId)
        postSharesCache.remove(postId)
        postCommentsCache.remove(postId)
        postRecipeReferencesCache.remove(postId)
        postCookbookReferencesCache.remove(postId)
        postAnalyticsCache.remove(postId)
    }

    // Comment operations
    suspend fun saveComment(comment: Comment) = withContext(Dispatchers.IO) {
        commentsCache[comment.commentId] = comment

        // Update comments list for the post
        val postComments = postCommentsCache[comment.postId]?.toMutableList() ?: mutableListOf()
        val existingIndex = postComments.indexOfFirst { it.commentId == comment.commentId }
        if (existingIndex >= 0) {
            postComments[existingIndex] = comment
        } else {
            postComments.add(0, comment) // Add at the beginning
        }
        postCommentsCache[comment.postId] = postComments

        // If it's a reply, update replies list
        comment.parentCommentId?.let { parentId ->
            val replies = commentRepliesCache[parentId]?.toMutableList() ?: mutableListOf()
            val replyIndex = replies.indexOfFirst { it.commentId == comment.commentId }
            if (replyIndex >= 0) {
                replies[replyIndex] = comment
            } else {
                replies.add(comment)
            }
            commentRepliesCache[parentId] = replies
        }
    }

    suspend fun getCommentById(commentId: String): Comment? = withContext(Dispatchers.IO) {
        return@withContext commentsCache[commentId]
    }

    suspend fun deleteComment(commentId: String) = withContext(Dispatchers.IO) {
        val comment = commentsCache[commentId]
        if (comment != null) {
            // Mark as deleted instead of removing
            val deletedComment = comment.copy(isDeleted = true)
            commentsCache[commentId] = deletedComment

            // Update in post comments
            val postComments = postCommentsCache[comment.postId]?.toMutableList()
            postComments?.let { comments ->
                val index = comments.indexOfFirst { it.commentId == commentId }
                if (index >= 0) {
                    comments[index] = deletedComment
                    postCommentsCache[comment.postId] = comments
                }
            }

            // Update in replies if it's a reply
            comment.parentCommentId?.let { parentId ->
                val replies = commentRepliesCache[parentId]?.toMutableList()
                replies?.let { replyList ->
                    val index = replyList.indexOfFirst { it.commentId == commentId }
                    if (index >= 0) {
                        replyList[index] = deletedComment
                        commentRepliesCache[parentId] = replyList
                    }
                }
            }
        }

        commentLikesCache.remove(commentId)
        commentRepliesCache.remove(commentId)
    }

    // Feed operations
    suspend fun getUserFeed(userId: String, limit: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        return@withContext userFeedCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveUserFeed(userId: String, feedItems: List<FeedItem>) = withContext(Dispatchers.IO) {
        userFeedCache[userId] = feedItems
    }

    suspend fun getFollowingFeed(userId: String, limit: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        return@withContext followingFeedCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveFollowingFeed(userId: String, feedItems: List<FeedItem>) = withContext(Dispatchers.IO) {
        followingFeedCache[userId] = feedItems
    }

    suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem> = withContext(Dispatchers.IO) {
        return@withContext discoverFeedCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveDiscoverFeed(userId: String, feedItems: List<FeedItem>) = withContext(Dispatchers.IO) {
        discoverFeedCache[userId] = feedItems
    }

    suspend fun getUserPosts(userId: String, limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext userPostsCache[userId]?.take(limit) ?: emptyList()
    }

    suspend fun saveUserPosts(userId: String, posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        userPostsCache[userId] = posts
    }

    suspend fun getTrendingPosts(limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext trendingPostsCache.take(limit)
    }

    suspend fun saveTrendingPosts(posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        trendingPostsCache.clear()
        trendingPostsCache.addAll(posts)
    }

    suspend fun getPopularPosts(limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext popularPostsCache.take(limit)
    }

    suspend fun savePopularPosts(posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        popularPostsCache.clear()
        popularPostsCache.addAll(posts)
    }

    suspend fun getSearchResults(query: String, limit: Int): List<PostListItem> = withContext(Dispatchers.IO) {
        return@withContext searchResultsCache[query.lowercase()]?.take(limit) ?: emptyList()
    }

    suspend fun saveSearchResults(query: String, posts: List<PostListItem>) = withContext(Dispatchers.IO) {
        searchResultsCache[query.lowercase()] = posts
    }

    // Post interaction operations
    suspend fun getPostLikes(postId: String, limit: Int): List<PostLike> = withContext(Dispatchers.IO) {
        return@withContext postLikesCache[postId]?.take(limit) ?: emptyList()
    }

    suspend fun savePostLikes(postId: String, likes: List<PostLike>) = withContext(Dispatchers.IO) {
        postLikesCache[postId] = likes
    }

    suspend fun getPostShares(postId: String, limit: Int): List<PostShare> = withContext(Dispatchers.IO) {
        return@withContext postSharesCache[postId]?.take(limit) ?: emptyList()
    }

    suspend fun savePostShares(postId: String, shares: List<PostShare>) = withContext(Dispatchers.IO) {
        postSharesCache[postId] = shares
    }

    suspend fun isPostLikedByUser(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postLikeStatusCache["$userId:$postId"] ?: false
    }

    suspend fun updatePostLikeStatus(userId: String, postId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        postLikeStatusCache["$userId:$postId"] = isLiked

        // Update post like count in cached post
        postsCache[postId]?.let { post ->
            val updatedCount = if (isLiked) post.likeCount + 1 else maxOf(0, post.likeCount - 1)
            postsCache[postId] = post.copy(likeCount = updatedCount)
        }

        // Update in user posts cache
        userPostsCache.forEach { (cacheUserId, posts) ->
            val updatedPosts = posts.map { postItem ->
                if (postItem.postId == postId) {
                    val updatedCount = if (isLiked) postItem.likeCount + 1 else maxOf(0, postItem.likeCount - 1)
                    postItem.copy(
                        likeCount = updatedCount,
                        isLikedByCurrentUser = if (cacheUserId == userId) isLiked else postItem.isLikedByCurrentUser
                    )
                } else postItem
            }
            userPostsCache[cacheUserId] = updatedPosts
        }

        // Update in feed caches
        updatePostInFeeds(postId) { feedItem ->
            feedItem.copy(isLikedByCurrentUser = isLiked)
        }
    }

    suspend fun isPostSharedByUser(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postShareStatusCache["$userId:$postId"] ?: false
    }

    suspend fun updatePostShareStatus(userId: String, postId: String, isShared: Boolean) = withContext(Dispatchers.IO) {
        postShareStatusCache["$userId:$postId"] = isShared

        // Update post share count in cached post
        postsCache[postId]?.let { post ->
            val updatedCount = if (isShared) post.shareCount + 1 else maxOf(0, post.shareCount - 1)
            postsCache[postId] = post.copy(shareCount = updatedCount)
        }

        // Update in user posts cache
        userPostsCache.forEach { (cacheUserId, posts) ->
            val updatedPosts = posts.map { postItem ->
                if (postItem.postId == postId) {
                    val updatedCount = if (isShared) postItem.shareCount + 1 else maxOf(0, postItem.shareCount - 1)
                    postItem.copy(
                        shareCount = updatedCount,
                        isSharedByCurrentUser = if (cacheUserId == userId) isShared else postItem.isSharedByCurrentUser
                    )
                } else postItem
            }
            userPostsCache[cacheUserId] = updatedPosts
        }

        // Update in feed caches
        updatePostInFeeds(postId) { feedItem ->
            feedItem.copy(isSharedByCurrentUser = isShared)
        }
    }

    suspend fun isPostSavedByUser(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postSaveStatusCache["$userId:$postId"] ?: false
    }

    suspend fun updatePostSaveStatus(userId: String, postId: String, isSaved: Boolean) = withContext(Dispatchers.IO) {
        postSaveStatusCache["$userId:$postId"] = isSaved
    }

    // Comment interaction operations
    suspend fun getPostComments(postId: String, limit: Int): List<Comment> = withContext(Dispatchers.IO) {
        return@withContext postCommentsCache[postId]?.take(limit) ?: emptyList()
    }

    suspend fun savePostComments(postId: String, comments: List<Comment>) = withContext(Dispatchers.IO) {
        postCommentsCache[postId] = comments
        comments.forEach { comment ->
            commentsCache[comment.commentId] = comment
        }
    }

    suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment> = withContext(Dispatchers.IO) {
        return@withContext commentRepliesCache[commentId]?.take(limit) ?: emptyList()
    }

    suspend fun saveCommentReplies(commentId: String, replies: List<Comment>) = withContext(Dispatchers.IO) {
        commentRepliesCache[commentId] = replies
        replies.forEach { reply ->
            commentsCache[reply.commentId] = reply
        }
    }

    suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike> = withContext(Dispatchers.IO) {
        return@withContext commentLikesCache[commentId]?.take(limit) ?: emptyList()
    }

    suspend fun saveCommentLikes(commentId: String, likes: List<CommentLike>) = withContext(Dispatchers.IO) {
        commentLikesCache[commentId] = likes
    }

    suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext commentLikeStatusCache["$userId:$commentId"] ?: false
    }

    suspend fun updateCommentLikeStatus(userId: String, commentId: String, isLiked: Boolean) = withContext(Dispatchers.IO) {
        commentLikeStatusCache["$userId:$commentId"] = isLiked

        // Update comment like count in cached comment
        commentsCache[commentId]?.let { comment ->
            val updatedCount = if (isLiked) comment.likeCount + 1 else maxOf(0, comment.likeCount - 1)
            val updatedComment = comment.copy(likeCount = updatedCount)
            commentsCache[commentId] = updatedComment

            // Update in post comments cache
            val postComments = postCommentsCache[comment.postId]?.toMutableList()
            postComments?.let { comments ->
                val index = comments.indexOfFirst { it.commentId == commentId }
                if (index >= 0) {
                    comments[index] = updatedComment
                    postCommentsCache[comment.postId] = comments
                }
            }

            // Update in replies cache if it's a reply
            comment.parentCommentId?.let { parentId ->
                val replies = commentRepliesCache[parentId]?.toMutableList()
                replies?.let { replyList ->
                    val index = replyList.indexOfFirst { it.commentId == commentId }
                    if (index >= 0) {
                        replyList[index] = updatedComment
                        commentRepliesCache[parentId] = replyList
                    }
                }
            }
        }
    }

    // References operations
    suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference> = withContext(Dispatchers.IO) {
        return@withContext postRecipeReferencesCache[postId] ?: emptyList()
    }

    suspend fun savePostRecipeReferences(postId: String, references: List<PostRecipeReference>) = withContext(Dispatchers.IO) {
        postRecipeReferencesCache[postId] = references
    }

    suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference> = withContext(Dispatchers.IO) {
        return@withContext postCookbookReferencesCache[postId] ?: emptyList()
    }

    suspend fun savePostCookbookReferences(postId: String, references: List<PostCookbookReference>) = withContext(Dispatchers.IO) {
        postCookbookReferencesCache[postId] = references
    }

    // Simple operations for non-domain features

    // Hashtag operations (simple strings)
    suspend fun getTrendingHashtags(limit: Int): List<String> = withContext(Dispatchers.IO) {
        return@withContext trendingHashtagsCache.take(limit)
    }

    suspend fun saveTrendingHashtags(hashtags: List<String>) = withContext(Dispatchers.IO) {
        trendingHashtagsCache.clear()
        trendingHashtagsCache.addAll(hashtags)
    }

    // Analytics operations (simple maps)
    suspend fun getPostAnalytics(postId: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        return@withContext postAnalyticsCache[postId]
    }

    suspend fun savePostAnalytics(postId: String, analytics: Map<String, Any>) = withContext(Dispatchers.IO) {
        postAnalyticsCache[postId] = analytics
    }

    suspend fun getUserPostAnalytics(userId: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        return@withContext userPostAnalyticsCache[userId]
    }

    suspend fun saveUserPostAnalytics(userId: String, analytics: Map<String, Any>) = withContext(Dispatchers.IO) {
        userPostAnalyticsCache[userId] = analytics
    }

    // User following operations (simple string lists)
    suspend fun getUserFollowing(userId: String): List<String> = withContext(Dispatchers.IO) {
        return@withContext userFollowingCache[userId] ?: emptyList()
    }

    suspend fun saveUserFollowing(userId: String, followingIds: List<String>) = withContext(Dispatchers.IO) {
        userFollowingCache[userId] = followingIds
    }

    // Cache management operations
    suspend fun clearUserCache(userId: String) = withContext(Dispatchers.IO) {
        userFeedCache.remove(userId)
        followingFeedCache.remove(userId)
        discoverFeedCache.remove(userId)
        userPostsCache.remove(userId)
        userPostAnalyticsCache.remove(userId)
        userFollowingCache.remove(userId)

        // Clear user-specific status caches
        val keysToRemove = mutableListOf<String>()
        postLikeStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postLikeStatusCache.remove(it) }

        keysToRemove.clear()
        postShareStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postShareStatusCache.remove(it) }

        keysToRemove.clear()
        commentLikeStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { commentLikeStatusCache.remove(it) }

        keysToRemove.clear()
        postSaveStatusCache.keys.forEach { key ->
            if (key.startsWith("$userId:")) {
                keysToRemove.add(key)
            }
        }
        keysToRemove.forEach { postSaveStatusCache.remove(it) }
    }

    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        postsCache.clear()
        commentsCache.clear()
        userFeedCache.clear()
        followingFeedCache.clear()
        discoverFeedCache.clear()
        userPostsCache.clear()
        trendingPostsCache.clear()
        popularPostsCache.clear()
        postLikesCache.clear()
        postSharesCache.clear()
        postCommentsCache.clear()
        commentRepliesCache.clear()
        commentLikesCache.clear()
        postRecipeReferencesCache.clear()
        postCookbookReferencesCache.clear()
        searchResultsCache.clear()
        trendingHashtagsCache.clear()
        postAnalyticsCache.clear()
        userPostAnalyticsCache.clear()
        postLikeStatusCache.clear()
        postShareStatusCache.clear()
        commentLikeStatusCache.clear()
        postSaveStatusCache.clear()
        userFollowingCache.clear()
    }

    suspend fun getCacheSize(): Map<String, Int> = withContext(Dispatchers.IO) {
        return@withContext mapOf(
            "posts" to postsCache.size,
            "comments" to commentsCache.size,
            "userFeeds" to userFeedCache.values.sumOf { it.size },
            "followingFeeds" to followingFeedCache.values.sumOf { it.size },
            "discoverFeeds" to discoverFeedCache.values.sumOf { it.size },
            "userPosts" to userPostsCache.values.sumOf { it.size },
            "trendingPosts" to trendingPostsCache.size,
            "popularPosts" to popularPostsCache.size,
            "postLikes" to postLikesCache.values.sumOf { it.size },
            "postShares" to postSharesCache.values.sumOf { it.size },
            "postComments" to postCommentsCache.values.sumOf { it.size },
            "commentReplies" to commentRepliesCache.values.sumOf { it.size },
            "commentLikes" to commentLikesCache.values.sumOf { it.size },
            "searchResults" to searchResultsCache.values.sumOf { it.size },
            "trendingHashtags" to trendingHashtagsCache.size,
            "statusCaches" to (postLikeStatusCache.size + postShareStatusCache.size +
                    commentLikeStatusCache.size + postSaveStatusCache.size)
        )
    }

    // Helper methods for data consistency
    suspend fun addPostToUserLists(userId: String, post: PostListItem) = withContext(Dispatchers.IO) {
        // Add to user posts
        val userPosts = userPostsCache[userId]?.toMutableList() ?: mutableListOf()
        userPosts.add(0, post) // Add at the beginning
        userPostsCache[userId] = userPosts
    }

    suspend fun updatePostInUserLists(userId: String, updatedPost: PostListItem) = withContext(Dispatchers.IO) {
        // Update in user posts cache
        val userPosts = userPostsCache[userId]?.toMutableList()
        userPosts?.let { posts ->
            val index = posts.indexOfFirst { it.postId == updatedPost.postId }
            if (index >= 0) {
                posts[index] = updatedPost
                userPostsCache[userId] = posts
            }
        }

        // Update in trending and popular posts
        val trendingIndex = trendingPostsCache.indexOfFirst { it.postId == updatedPost.postId }
        if (trendingIndex >= 0) {
            trendingPostsCache[trendingIndex] = updatedPost
        }

        val popularIndex = popularPostsCache.indexOfFirst { it.postId == updatedPost.postId }
        if (popularIndex >= 0) {
            popularPostsCache[popularIndex] = updatedPost
        }
    }

    suspend fun removePostFromUserLists(userId: String, postId: String) = withContext(Dispatchers.IO) {
        // Remove from user posts
        userPostsCache[userId] = userPostsCache[userId]?.filter { it.postId != postId } ?: emptyList()

        // Remove from trending and popular
        trendingPostsCache.removeAll { it.postId == postId }
        popularPostsCache.removeAll { it.postId == postId }
    }

    private suspend fun updatePostInFeeds(postId: String, updateFunction: (FeedItem) -> FeedItem) {
        userFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.map { feedItem ->
                if (feedItem.post?.postId == postId) {
                    updateFunction(feedItem)
                } else feedItem
            }
            userFeedCache[userId] = updatedItems
        }

        followingFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.map { feedItem ->
                if (feedItem.post?.postId == postId) {
                    updateFunction(feedItem)
                } else feedItem
            }
            followingFeedCache[userId] = updatedItems
        }

        discoverFeedCache.forEach { (userId, feedItems) ->
            val updatedItems = feedItems.map { feedItem ->
                if (feedItem.post?.postId == postId) {
                    updateFunction(feedItem)
                } else feedItem
            }
            discoverFeedCache[userId] = updatedItems
        }
    }

    // Data validation methods
    suspend fun isPostCached(postId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext postsCache.containsKey(postId)
    }

    suspend fun isCommentCached(commentId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext commentsCache.containsKey(commentId)
    }

    suspend fun isUserFeedCached(userId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userFeedCache.containsKey(userId) &&
                userFeedCache[userId]?.isNotEmpty() == true
    }

    suspend fun getLastCacheUpdate(): Long = withContext(Dispatchers.IO) {
        // In a real implementation, you might want to track cache timestamps
        return@withContext System.currentTimeMillis()
    }
}