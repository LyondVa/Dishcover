// PostRemoteDataSource.kt
package com.nhatpham.dishcover.data.source.remote.feed

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nhatpham.dishcover.data.mapper.toDomain
import com.nhatpham.dishcover.data.mapper.toDto
import com.nhatpham.dishcover.data.mapper.toListItem
import com.nhatpham.dishcover.data.model.dto.feed.PostDto
import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.domain.model.user.User
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class PostRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val postsCollection = firestore.collection("POSTS")
    private val usersCollection = firestore.collection("USERS")

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

            updatedPost
        } catch (e: Exception) {
            Timber.e(e, "Error updating post")
            null
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            // Delete post document
            postsCollection.document(postId).delete().await()
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
            postDto.toDomain()
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
                doc.toObject(PostDto::class.java)?.toListItem()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user posts")
            emptyList()
        }
    }

    suspend fun searchPosts(query: String, userId: String?, limit: Int): List<PostListItem> {
        return try {
            val lowerQuery = query.lowercase().trim()
            if (lowerQuery.isBlank()) return emptyList()

            // Search in posts with comprehensive matching
            val snapshot = postsCollection
                .whereEqualTo("public", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit((limit * 2).toLong()) // Get more to filter locally
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PostDto::class.java)
            }.filter { postDto ->
                val content = postDto.content?.lowercase() ?: ""
                val hashtags = postDto.hashtags?.joinToString(" ")?.lowercase() ?: ""
                val username = postDto.username?.lowercase() ?: ""
                val location = postDto.location?.lowercase() ?: ""
                val taggedUsers = postDto.taggedUsers?.joinToString(" ")?.lowercase() ?: ""

                // Comprehensive search matching
                content.contains(lowerQuery) ||
                        hashtags.contains(lowerQuery) ||
                        username.contains(lowerQuery) ||
                        location.contains(lowerQuery) ||
                        taggedUsers.contains(lowerQuery)
            }

            // Sort by relevance (username matches first, then content, etc.)
            posts.sortedWith { a, b ->
                val aUsernameMatch = a.username?.lowercase()?.contains(lowerQuery) ?: false
                val bUsernameMatch = b.username?.lowercase()?.contains(lowerQuery) ?: false
                val aContentMatch = a.content?.lowercase()?.contains(lowerQuery) ?: false
                val bContentMatch = b.content?.lowercase()?.contains(lowerQuery) ?: false

                when {
                    aUsernameMatch && !bUsernameMatch -> -1
                    !aUsernameMatch && bUsernameMatch -> 1
                    aContentMatch && !bContentMatch -> -1
                    !aContentMatch && bContentMatch -> 1
                    else -> (b.createdAt?.compareTo(a.createdAt ?: Timestamp.now()) ?: 0)
                }
            }.take(limit).map { it.toListItem() }

        } catch (e: Exception) {
            Timber.e(e, "Error searching posts")
            emptyList()
        }
    }

    suspend fun updatePostVisibility(postId: String, isPublic: Boolean): Boolean {
        return try {
            postsCollection.document(postId)
                .update("public", isPublic)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating post visibility")
            false
        }
    }

    suspend fun updateCommentSettings(postId: String, allowComments: Boolean): Boolean {
        return try {
            postsCollection.document(postId)
                .update("allowComments", allowComments)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating comment settings")
            false
        }
    }

    suspend fun updateShareSettings(postId: String, allowShares: Boolean): Boolean {
        return try {
            postsCollection.document(postId)
                .update("allowShares", allowShares)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error updating share settings")
            false
        }
    }

    suspend fun archivePost(postId: String): Boolean {
        return try {
            postsCollection.document(postId)
                .update("archived", true)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error archiving post")
            false
        }
    }

    suspend fun unarchivePost(postId: String): Boolean {
        return try {
            postsCollection.document(postId)
                .update("archived", false)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unarchiving post")
            false
        }
    }

    suspend fun pinPost(postId: String): Boolean {
        return try {
            postsCollection.document(postId)
                .update("pinned", true)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error pinning post")
            false
        }
    }

    suspend fun unpinPost(postId: String): Boolean {
        return try {
            postsCollection.document(postId)
                .update("pinned", false)
                .await()
            true
        } catch (e: Exception) {
            Timber.e(e, "Error unpinning post")
            false
        }
    }

    // Helper method
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
}