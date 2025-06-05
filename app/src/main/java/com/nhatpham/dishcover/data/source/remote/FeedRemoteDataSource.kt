// FeedRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.mapper.*
import com.nhatpham.dishcover.data.model.dto.feed.CommentDto
import com.nhatpham.dishcover.data.model.dto.feed.CommentLikeDto
import com.nhatpham.dishcover.data.model.dto.feed.PostCookbookReferenceDto
import com.nhatpham.dishcover.data.model.dto.feed.PostDto
import com.nhatpham.dishcover.data.model.dto.feed.PostLikeDto
import com.nhatpham.dishcover.data.model.dto.feed.PostRecipeReferenceDto
import com.nhatpham.dishcover.data.model.dto.feed.PostShareDto
import com.nhatpham.dishcover.domain.model.feed.*
import com.nhatpham.dishcover.domain.model.user.User
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FeedRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val postsCollection = firestore.collection("POSTS")
    private val commentsCollection = firestore.collection("COMMENTS")
    private val postLikesCollection = firestore.collection("POST_LIKES")
    private val commentLikesCollection = firestore.collection("COMMENT_LIKES")
    private val postSharesCollection = firestore.collection("POST_SHARES")
    private val postActivityCollection = firestore.collection("POST_ACTIVITY")
    private val postRecipeReferencesCollection = firestore.collection("POST_RECIPE_REFERENCES")
    private val postCookbookReferencesCollection = firestore.collection("POST_COOKBOOK_REFERENCES")
    private val usersCollection = firestore.collection("USERS")

    // Simple collections for non-domain features
    private val userFollowsCollection = firestore.collection("USER_FOLLOWS")
    private val savedPostsCollection = firestore.collection("SAVED_POSTS")
    private val postViewsCollection = firestore.collection("POST_VIEWS")
    private val hashtagCountsCollection = firestore.collection("HASHTAG_COUNTS")

    // Post CRUD Operations
    suspend fun createPost(post: Post): Post? {
        return try {
            val postId = post.postId.takeIf { it.isNotBlank() }
                ?: postsCollection.document().id

            // Get user's username if not provided
            val username = if (post.username.isBlank()) {
                getUserById(post.userId)?.username ?: "Unknown User"
            } else {
                post.username
            }

            val postDto = post.copy(
                postId = postId,
                username = username,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ).toDto()

            // Save post document
            postsCollection.document(postId)
                .set(postDto)
                .await()

            // Save recipe references
            savePostRecipeReferences(postId, post.recipeReferences)

            // Save cookbook references
            savePostCookbookReferences(postId, post.cookbookReferences)

            // Update hashtag counts
            updateHashtagCounts(post.hashtags, increment = true)

            // Return the created post with updated ID and timestamps
            post.copy(
                postId = postId,
                username = username,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating post")
            null
        }
    }

    suspend fun updatePost(post: Post): Post? {
        return try {
            val postId = post.postId
            if (postId.isBlank()) {
                Timber.e("Cannot update post with blank ID")
                return null
            }

            // Get old post to update hashtag counts
            val oldPost = getPostById(postId)

            // Ensure username is preserved/updated
            val username = if (post.username.isBlank()) {
                getUserById(post.userId)?.username ?: "Unknown User"
            } else {
                post.username
            }

            val updatedPost = post.copy(
                username = username,
                updatedAt = Timestamp.now(),
                isEdited = true
            )
            val postDto = updatedPost.toDto()

            // Update post document
            postsCollection.document(postId)
                .set(postDto)
                .await()

            // Update references
            deletePostRecipeReferences(postId)
            savePostRecipeReferences(postId, post.recipeReferences)

            deletePostCookbookReferences(postId)
            savePostCookbookReferences(postId, post.cookbookReferences)

            // Update hashtag counts
            oldPost?.let { updateHashtagCounts(it.hashtags, increment = false) }
            updateHashtagCounts(post.hashtags, increment = true)

            updatedPost
        } catch (e: Exception) {
            Timber.e(e, "Error updating post")
            null
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            // Get post to update hashtag counts
            val post = getPostById(postId)

            // Delete post document
            postsCollection.document(postId).delete().await()

            // Delete associated data
            deletePostComments(postId)
            deletePostLikes(postId)
            deletePostShares(postId)
            deletePostActivity(postId)
            deletePostRecipeReferences(postId)
            deletePostCookbookReferences(postId)
            deleteSavedPosts(postId)
            deletePostViews(postId)

            // Update hashtag counts
            post?.let { updateHashtagCounts(it.hashtags, increment = false) }

            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post")
            false
        }
    }

    suspend fun getPostById(postId: String): Post? {
        return try {
            val postDoc = postsCollection.document(postId).get().await()
            if (!postDoc.exists()) return null

            val postDto = postDoc.toObject(PostDto::class.java) ?: return null
            val recipeReferences = getPostRecipeReferences(postId)
            val cookbookReferences = getPostCookbookReferences(postId)

            postDto.toDomain().copy(
                recipeReferences = recipeReferences,
                cookbookReferences = cookbookReferences
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting post by ID: $postId")
            null
        }
    }

    suspend fun getUserPosts(userId: String, limit: Int): List<PostListItem> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)?.let { postDto ->
                    val hasRecipeRefs = hasPostRecipeReferences(postDto.postId ?: "")
                    val hasCookbookRefs = hasPostCookbookReferences(postDto.postId ?: "")

                    postDto.toListItem(
                        hasRecipeReferences = hasRecipeRefs,
                        hasCookbookReferences = hasCookbookRefs
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user posts")
            emptyList()
        }
    }

    // Feed Operations
    suspend fun getUserFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem> {
        return try {
            // Get user's following list
            val followingIds = getUserFollowingIds(userId)
            if (followingIds.isEmpty()) return emptyList()

            var query = postsCollection
                .whereIn("userId", followingIds.take(10)) // Firestore limit
                .whereEqualTo("public", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            // Handle pagination
            lastPostId?.let { lastId ->
                val lastDoc = postsCollection.document(lastId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc) as Query
                }
            }

            val snapshot = query.get().await()
            buildFeedItems(snapshot.documents.mapNotNull { it.toObject(PostDto::class.java) }, userId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting user feed")
            emptyList()
        }
    }

    suspend fun getFollowingFeed(userId: String, limit: Int, lastPostId: String?): List<FeedItem> {
        return getUserFeed(userId, limit, lastPostId) // Same as user feed for now
    }

    suspend fun getTrendingPosts(limit: Int, timeRange: String): List<PostListItem> {
        return try {
            val hoursAgo = when (timeRange) {
                "1h" -> 1
                "24h" -> 24
                "7d" -> 168
                else -> 24
            }

            val sinceTime = Timestamp(Date(System.currentTimeMillis() - hoursAgo * 60 * 60 * 1000))

            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .whereGreaterThan("createdAt", sinceTime)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)
            }

            // Sort by engagement score (likes + comments + shares)
            posts.sortedByDescending {
                (it.likeCount ?: 0) + (it.commentCount ?: 0) + (it.shareCount ?: 0)
            }.map { postDto ->
                val hasRecipeRefs = hasPostRecipeReferences(postDto.postId ?: "")
                val hasCookbookRefs = hasPostCookbookReferences(postDto.postId ?: "")

                postDto.toListItem(
                    hasRecipeReferences = hasRecipeRefs,
                    hasCookbookReferences = hasCookbookRefs
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending posts")
            emptyList()
        }
    }

    suspend fun getPopularPosts(limit: Int): List<PostListItem> {
        return try {
            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)?.let { postDto ->
                    val hasRecipeRefs = hasPostRecipeReferences(postDto.postId ?: "")
                    val hasCookbookRefs = hasPostCookbookReferences(postDto.postId ?: "")

                    postDto.toListItem(
                        hasRecipeReferences = hasRecipeRefs,
                        hasCookbookReferences = hasCookbookRefs
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting popular posts")
            emptyList()
        }
    }

    suspend fun getDiscoverFeed(userId: String, limit: Int): List<FeedItem> {
        return try {
            // Get posts from users not followed, popular posts, etc.
            val followingIds = getUserFollowingIds(userId)

            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .orderBy("likeCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                val postDto = doc.toObject(PostDto::class.java)
                // Filter out posts from users already followed
                if (postDto != null && !followingIds.contains(postDto.userId)) {
                    postDto
                } else null
            }

            buildFeedItems(posts, userId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting discover feed")
            emptyList()
        }
    }

    suspend fun searchPosts(query: String, userId: String?, limit: Int): List<PostListItem> {
        return try {
            // Search in content and hashtags
            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)
            }.filter { postDto ->
                val content = postDto.content?.lowercase() ?: ""
                val hashtags = postDto.hashtags?.joinToString(" ")?.lowercase() ?: ""
                content.contains(query.lowercase()) || hashtags.contains(query.lowercase())
            }

            posts.map { postDto ->
                val hasRecipeRefs = hasPostRecipeReferences(postDto.postId ?: "")
                val hasCookbookRefs = hasPostCookbookReferences(postDto.postId ?: "")

                postDto.toListItem(
                    hasRecipeReferences = hasRecipeRefs,
                    hasCookbookReferences = hasCookbookRefs
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching posts")
            emptyList()
        }
    }

    // Post Interaction Operations
    suspend fun likePost(userId: String, postId: String, likeType: LikeType): Boolean {
        return try {
            val existingLike = postLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            if (existingLike.isEmpty) {
                val likeDto = PostLikeDto(
                    likeId = postLikesCollection.document().id,
                    postId = postId,
                    userId = userId,
                    likeType = likeType.name,
                    createdAt = Timestamp.now()
                )

                postLikesCollection.document(likeDto.likeId!!)
                    .set(likeDto)
                    .await()

                // Increment like count
                postsCollection.document(postId)
                    .update("likeCount", FieldValue.increment(1))
                    .await()

                // Track activity
                trackPostActivity(PostActivity(
                    activityId = "",
                    postId = postId,
                    userId = userId,
                    activityType = PostActivityType.LIKE
                ))
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error liking post")
            false
        }
    }

    suspend fun unlikePost(userId: String, postId: String): Boolean {
        return try {
            val existingLike = postLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            if (!existingLike.isEmpty) {
                existingLike.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Decrement like count
                postsCollection.document(postId)
                    .update("likeCount", FieldValue.increment(-1))
                    .await()

                // Track activity
                trackPostActivity(PostActivity(
                    activityId = "",
                    postId = postId,
                    userId = userId,
                    activityType = PostActivityType.UNLIKE
                ))
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unliking post")
            false
        }
    }

    suspend fun sharePost(userId: String, postId: String, shareMessage: String?, shareType: ShareType): PostShare? {
        return try {
            val shareId = postSharesCollection.document().id
            val postShare = PostShare(
                shareId = shareId,
                originalPostId = postId,
                sharedByUserId = userId,
                shareMessage = shareMessage,
                shareType = shareType,
                createdAt = Timestamp.now()
            )

            postSharesCollection.document(shareId)
                .set(postShare.toDto())
                .await()

            // Increment share count
            postsCollection.document(postId)
                .update("shareCount", FieldValue.increment(1))
                .await()

            // Track activity
            trackPostActivity(PostActivity(
                activityId = "",
                postId = postId,
                userId = userId,
                activityType = PostActivityType.SHARE
            ))

            postShare
        } catch (e: Exception) {
            Timber.e(e, "Error sharing post")
            null
        }
    }

    suspend fun unsharePost(userId: String, postId: String): Boolean {
        return try {
            val existingShare = postSharesCollection
                .whereEqualTo("sharedByUserId", userId)
                .whereEqualTo("originalPostId", postId)
                .get()
                .await()

            if (!existingShare.isEmpty) {
                existingShare.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Decrement share count
                postsCollection.document(postId)
                    .update("shareCount", FieldValue.increment(-1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unsharing post")
            false
        }
    }

    suspend fun getPostLikes(postId: String, limit: Int): List<PostLike> {
        return try {
            val snapshot = postLikesCollection
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostLikeDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post likes")
            emptyList()
        }
    }

    suspend fun getPostShares(postId: String, limit: Int): List<PostShare> {
        return try {
            val snapshot = postSharesCollection
                .whereEqualTo("originalPostId", postId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostShareDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post shares")
            emptyList()
        }
    }

    suspend fun isPostLikedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = postLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is liked")
            false
        }
    }

    suspend fun isPostSharedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = postSharesCollection
                .whereEqualTo("sharedByUserId", userId)
                .whereEqualTo("originalPostId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is shared")
            false
        }
    }

    // Comment Operations
    suspend fun addComment(comment: Comment): Comment? {
        return try {
            val commentId = comment.commentId.takeIf { it.isNotBlank() }
                ?: commentsCollection.document().id

            // Get user's username if not provided
            val username = if (comment.username.isBlank()) {
                getUserById(comment.userId)?.username ?: "Unknown User"
            } else {
                comment.username
            }

            val commentDto = comment.copy(
                commentId = commentId,
                username = username,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            ).toDto()

            commentsCollection.document(commentId)
                .set(commentDto)
                .await()

            // Increment comment count on post
            postsCollection.document(comment.postId)
                .update("commentCount", FieldValue.increment(1))
                .await()

            // If it's a reply, increment reply count on parent comment
            comment.parentCommentId?.let { parentId ->
                commentsCollection.document(parentId)
                    .update("replyCount", FieldValue.increment(1))
                    .await()
            }

            // Track activity
            trackPostActivity(PostActivity(
                activityId = "",
                postId = comment.postId,
                userId = comment.userId,
                activityType = PostActivityType.COMMENT
            ))

            comment.copy(
                commentId = commentId,
                username = username,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error adding comment")
            null
        }
    }

    suspend fun updateComment(comment: Comment): Comment? {
        return try {
            val updatedComment = comment.copy(
                updatedAt = Timestamp.now(),
                isEdited = true
            )

            commentsCollection.document(comment.commentId)
                .set(updatedComment.toDto())
                .await()

            updatedComment
        } catch (e: Exception) {
            Timber.e(e, "Error updating comment")
            null
        }
    }

    suspend fun deleteComment(commentId: String): Boolean {
        return try {
            val comment = getComment(commentId)
            if (comment != null) {
                // Soft delete - mark as deleted
                commentsCollection.document(commentId)
                    .update("deleted", true)
                    .await()

                // Decrement comment count on post
                postsCollection.document(comment.postId)
                    .update("commentCount", FieldValue.increment(-1))
                    .await()

                // If it's a reply, decrement reply count on parent comment
                comment.parentCommentId?.let { parentId ->
                    commentsCollection.document(parentId)
                        .update("replyCount", FieldValue.increment(-1))
                        .await()
                }
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting comment")
            false
        }
    }

    suspend fun getPostComments(postId: String, limit: Int, lastCommentId: String?): List<Comment> {
        return try {
            var query = commentsCollection
                .whereEqualTo("postId", postId)
                .whereEqualTo("parentCommentId", null) // Top-level comments only
                .whereEqualTo("deleted", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            lastCommentId?.let { lastId ->
                val lastDoc = commentsCollection.document(lastId).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc) as Query
                }
            }

            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CommentDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post comments")
            emptyList()
        }
    }

    suspend fun getCommentReplies(commentId: String, limit: Int): List<Comment> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("parentCommentId", commentId)
                .whereEqualTo("deleted", false)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CommentDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment replies")
            emptyList()
        }
    }

    suspend fun getComment(commentId: String): Comment? {
        return try {
            val doc = commentsCollection.document(commentId).get().await()
            if (doc.exists()) {
                doc.toObject(CommentDto::class.java)?.toDomain()
            } else null
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment")
            null
        }
    }

    // Comment Interaction Operations
    suspend fun likeComment(userId: String, commentId: String, likeType: LikeType): Boolean {
        return try {
            val existingLike = commentLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("commentId", commentId)
                .get()
                .await()

            if (existingLike.isEmpty) {
                val likeDto = CommentLikeDto(
                    likeId = commentLikesCollection.document().id,
                    commentId = commentId,
                    userId = userId,
                    likeType = likeType.name,
                    createdAt = Timestamp.now()
                )

                commentLikesCollection.document(likeDto.likeId!!)
                    .set(likeDto)
                    .await()

                // Increment like count
                commentsCollection.document(commentId)
                    .update("likeCount", FieldValue.increment(1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error liking comment")
            false
        }
    }

    suspend fun unlikeComment(userId: String, commentId: String): Boolean {
        return try {
            val existingLike = commentLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("commentId", commentId)
                .get()
                .await()

            if (!existingLike.isEmpty) {
                existingLike.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // Decrement like count
                commentsCollection.document(commentId)
                    .update("likeCount", FieldValue.increment(-1))
                    .await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unliking comment")
            false
        }
    }

    suspend fun getCommentLikes(commentId: String, limit: Int): List<CommentLike> {
        return try {
            val snapshot = commentLikesCollection
                .whereEqualTo("commentId", commentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(CommentLikeDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting comment likes")
            emptyList()
        }
    }

    suspend fun isCommentLikedByUser(userId: String, commentId: String): Boolean {
        return try {
            val snapshot = commentLikesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("commentId", commentId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if comment is liked")
            false
        }
    }

    // Recipe and Cookbook Reference Operations
    suspend fun addRecipeReference(reference: PostRecipeReference): PostRecipeReference? {
        return try {
            val referenceId = reference.referenceId.takeIf { it.isNotBlank() }
                ?: postRecipeReferencesCollection.document().id

            val updatedReference = reference.copy(
                referenceId = referenceId,
                createdAt = Timestamp.now()
            )

            postRecipeReferencesCollection.document(referenceId)
                .set(updatedReference.toDto())
                .await()

            updatedReference
        } catch (e: Exception) {
            Timber.e(e, "Error adding recipe reference")
            null
        }
    }

    suspend fun addCookbookReference(reference: PostCookbookReference): PostCookbookReference? {
        return try {
            val referenceId = reference.referenceId.takeIf { it.isNotBlank() }
                ?: postCookbookReferencesCollection.document().id

            val updatedReference = reference.copy(
                referenceId = referenceId,
                createdAt = Timestamp.now()
            )

            postCookbookReferencesCollection.document(referenceId)
                .set(updatedReference.toDto())
                .await()

            updatedReference
        } catch (e: Exception) {
            Timber.e(e, "Error adding cookbook reference")
            null
        }
    }

    suspend fun removeRecipeReference(referenceId: String): Boolean {
        return try {
            postRecipeReferencesCollection.document(referenceId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing recipe reference")
            false
        }
    }

    suspend fun removeCookbookReference(referenceId: String): Boolean {
        return try {
            postCookbookReferencesCollection.document(referenceId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing cookbook reference")
            false
        }
    }

    suspend fun getPostRecipeReferences(postId: String): List<PostRecipeReference> {
        return try {
            val snapshot = postRecipeReferencesCollection
                .whereEqualTo("postId", postId)
                .orderBy("position")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostRecipeReferenceDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post recipe references")
            emptyList()
        }
    }

    suspend fun getPostCookbookReferences(postId: String): List<PostCookbookReference> {
        return try {
            val snapshot = postCookbookReferencesCollection
                .whereEqualTo("postId", postId)
                .orderBy("position")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostCookbookReferenceDto::class.java)?.toDomain()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting post cookbook references")
            emptyList()
        }
    }

    // Media Operations
    suspend fun uploadPostImage(postId: String, imageData: ByteArray): String? {
        return try {
            val storageRef = storage.reference
                .child("post_images")
                .child("$postId/${UUID.randomUUID()}.jpg")

            val uploadTask = storageRef.putBytes(imageData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post image")
            null
        }
    }

    suspend fun uploadPostVideo(postId: String, videoData: ByteArray): String? {
        return try {
            val storageRef = storage.reference
                .child("post_videos")
                .child("$postId/${UUID.randomUUID()}.mp4")

            val uploadTask = storageRef.putBytes(videoData).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error uploading post video")
            null
        }
    }

    suspend fun deletePostMedia(mediaUrl: String): Boolean {
        return try {
            val storageRef = storage.getReferenceFromUrl(mediaUrl)
            storageRef.delete().await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post media")
            false
        }
    }

    // Activity Tracking and Analytics
    suspend fun trackPostActivity(activity: PostActivity): Boolean {
        return try {
            val activityId = activity.activityId.takeIf { it.isNotBlank() }
                ?: postActivityCollection.document().id

            val activityDto = activity.copy(
                activityId = activityId,
                createdAt = Timestamp.now()
            ).toDto()

            postActivityCollection.document(activityId)
                .set(activityDto)
                .await()

            true
        } catch (e: Exception) {
            Timber.e(e, "Error tracking post activity")
            false
        }
    }

    suspend fun getPostAnalytics(postId: String): Map<String, Any> {
        return try {
            val post = getPostById(postId)
            val viewCount = getPostViewCount(postId)
            val uniqueViewers = getUniqueViewerCount(postId)
            val engagementRate = calculateEngagementRate(postId)

            mapOf(
                "likeCount" to (post?.likeCount ?: 0),
                "commentCount" to (post?.commentCount ?: 0),
                "shareCount" to (post?.shareCount ?: 0),
                "viewCount" to viewCount,
                "uniqueViewers" to uniqueViewers,
                "engagementRate" to engagementRate
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting post analytics")
            emptyMap()
        }
    }

    suspend fun getUserPostAnalytics(userId: String, dateRange: String): Map<String, Any> {
        return try {
            val posts = getUserPosts(userId, 100) // Get recent posts

            val totalLikes = posts.sumOf { it.likeCount }
            val totalComments = posts.sumOf { it.commentCount }
            val totalShares = posts.sumOf { it.shareCount }
            val avgEngagement = if (posts.isNotEmpty()) {
                (totalLikes + totalComments + totalShares).toDouble() / posts.size
            } else 0.0

            mapOf(
                "totalPosts" to posts.size,
                "totalLikes" to totalLikes,
                "totalComments" to totalComments,
                "totalShares" to totalShares,
                "averageEngagement" to avgEngagement
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting user post analytics")
            emptyMap()
        }
    }

    // Simple operations for non-domain features

    // Hashtag operations (simple counts)
    suspend fun getTrendingHashtags(limit: Int): List<String> {
        return try {
            val snapshot = hashtagCountsCollection
                .orderBy("count", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getString("hashtag")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting trending hashtags")
            emptyList()
        }
    }

    // User follow operations (simple relationships)
    suspend fun followUser(followerId: String, followingId: String): Boolean {
        return try {
            val followData = mapOf(
                "followerId" to followerId,
                "followingId" to followingId,
                "createdAt" to Timestamp.now()
            )

            userFollowsCollection.add(followData).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error following user")
            false
        }
    }

    suspend fun unfollowUser(followerId: String, followingId: String): Boolean {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unfollowing user")
            false
        }
    }

    // Saved posts operations (simple bookmarking)
    suspend fun savePost(userId: String, postId: String): Boolean {
        return try {
            val saveData = mapOf(
                "userId" to userId,
                "postId" to postId,
                "savedAt" to Timestamp.now()
            )

            savedPostsCollection.add(saveData).await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving post")
            false
        }
    }

    suspend fun unsavePost(userId: String, postId: String): Boolean {
        return try {
            val snapshot = savedPostsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unsaving post")
            false
        }
    }

    suspend fun isPostSavedByUser(userId: String, postId: String): Boolean {
        return try {
            val snapshot = savedPostsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            Timber.e(e, "Error checking if post is saved")
            false
        }
    }

    // Helper Methods
    private suspend fun getUserById(userId: String): User? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                doc.toObject(User::class.java)
            } else null
        } catch (e: Exception) {
            Timber.e(e, "Error getting user by ID")
            null
        }
    }

    private suspend fun getUserFollowingIds(userId: String): List<String> {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getString("followingId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user following IDs")
            emptyList()
        }
    }

    private suspend fun buildFeedItems(posts: List<PostDto>, currentUserId: String): List<FeedItem> {
        return posts.mapNotNull { postDto ->
            try {
                val post = postDto.toDomain()
                val isLiked = isPostLikedByUser(currentUserId, post.postId)
                val isShared = isPostSharedByUser(currentUserId, post.postId)
                val isFollowing = isUserFollowing(currentUserId, post.userId)

                buildFeedItem(
                    post = post,
                    author = null, // No longer needed since username is in post
                    isLikedByCurrentUser = isLiked,
                    isSharedByCurrentUser = isShared,
                    isFollowingAuthor = isFollowing
                )
            } catch (e: Exception) {
                Timber.e(e, "Error building feed item")
                null
            }
        }
    }

    private suspend fun isUserFollowing(followerId: String, followingId: String): Boolean {
        return try {
            val snapshot = userFollowsCollection
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun hasPostRecipeReferences(postId: String): Boolean {
        return try {
            val snapshot = postRecipeReferencesCollection
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun hasPostCookbookReferences(postId: String): Boolean {
        return try {
            val snapshot = postCookbookReferencesCollection
                .whereEqualTo("postId", postId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun savePostRecipeReferences(postId: String, references: List<PostRecipeReference>) {
        references.forEach { reference ->
            try {
                addRecipeReference(reference.copy(postId = postId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving recipe reference")
            }
        }
    }

    private suspend fun savePostCookbookReferences(postId: String, references: List<PostCookbookReference>) {
        references.forEach { reference ->
            try {
                addCookbookReference(reference.copy(postId = postId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving cookbook reference")
            }
        }
    }

    private suspend fun deletePostRecipeReferences(postId: String) {
        try {
            val snapshot = postRecipeReferencesCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting recipe references")
        }
    }

    private suspend fun deletePostCookbookReferences(postId: String) {
        try {
            val snapshot = postCookbookReferencesCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting cookbook references")
        }
    }

    private suspend fun deletePostComments(postId: String) {
        try {
            val snapshot = commentsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post comments")
        }
    }

    private suspend fun deletePostLikes(postId: String) {
        try {
            val snapshot = postLikesCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post likes")
        }
    }

    private suspend fun deletePostShares(postId: String) {
        try {
            val snapshot = postSharesCollection
                .whereEqualTo("originalPostId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post shares")
        }
    }

    private suspend fun deletePostActivity(postId: String) {
        try {
            val snapshot = postActivityCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post activity")
        }
    }

    private suspend fun deleteSavedPosts(postId: String) {
        try {
            val snapshot = savedPostsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting saved posts")
        }
    }

    private suspend fun deletePostViews(postId: String) {
        try {
            val snapshot = postViewsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting post views")
        }
    }

    private suspend fun updateHashtagCounts(hashtags: List<String>, increment: Boolean) {
        hashtags.forEach { hashtag ->
            try {
                val hashtagDoc = hashtagCountsCollection.document(hashtag)
                val incrementValue = if (increment) 1 else -1

                hashtagDoc.update("count", FieldValue.increment(incrementValue.toLong())).await()
            } catch (e: Exception) {
                // Create if doesn't exist
                if (increment) {
                    try {
                        hashtagCountsCollection.document(hashtag)
                            .set(mapOf("hashtag" to hashtag, "count" to 1))
                            .await()
                    } catch (createException: Exception) {
                        Timber.e(createException, "Error creating hashtag count")
                    }
                }
            }
        }
    }

    private suspend fun getPostViewCount(postId: String): Int {
        return try {
            val snapshot = postViewsCollection
                .whereEqualTo("postId", postId)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()

            snapshot.count.toInt()
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getUniqueViewerCount(postId: String): Int {
        return try {
            val snapshot = postViewsCollection
                .whereEqualTo("postId", postId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("userId") }.distinct().size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun calculateEngagementRate(postId: String): Double {
        return try {
            val post = getPostById(postId)
            val viewCount = getPostViewCount(postId)

            if (post != null && viewCount > 0) {
                val totalEngagements = post.likeCount + post.commentCount + post.shareCount
                (totalEngagements.toDouble() / viewCount) * 100
            } else 0.0
        } catch (e: Exception) {
            0.0
        }
    }
}