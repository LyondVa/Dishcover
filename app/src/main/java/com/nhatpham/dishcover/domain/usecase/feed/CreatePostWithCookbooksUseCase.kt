// CreatePostWithCookbooksUseCase.kt
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.cookbook.CookbookListItem
import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostCookbookReference
import com.nhatpham.dishcover.domain.repository.FeedRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CreatePostWithCookbooksUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(
        post: Post,
        linkedCookbooks: List<CookbookListItem>
    ): Flow<Resource<Post>> = flow {
        try {
            emit(Resource.Loading())

            // Create the post first
            feedRepository.createPost(post).collect { createResult ->
                when (createResult) {
                    is Resource.Loading -> {
                        // Already emitted above
                    }
                    is Resource.Success -> {
                        val createdPost = createResult.data
                        if (createdPost == null) {
                            emit(Resource.Error("Failed to create post"))
                            return@collect
                        }

                        // Create cookbook references if there are any linked cookbooks
                        if (linkedCookbooks.isNotEmpty()) {
                            val cookbookReferences = linkedCookbooks.map { cookbook ->
                                PostCookbookReference(
                                    referenceId = UUID.randomUUID().toString(),
                                    postId = createdPost.postId,
                                    cookbookId = cookbook.cookbookId,
                                    displayText = cookbook.title,
                                    position = 0,
                                    userId = cookbook.userId,
                                    coverImage = cookbook.coverImage
                                )
                            }

                            // Add cookbook references to the post
                            var allReferencesAdded = true
                            val addedReferences = mutableListOf<PostCookbookReference>()

                            for (reference in cookbookReferences) {
                                feedRepository.addCookbookReference(reference).collect { refResult ->
                                    when (refResult) {
                                        is Resource.Success -> {
                                            refResult.data?.let { addedReferences.add(it) }
                                        }
                                        is Resource.Error -> {
                                            Timber.e("Failed to add cookbook reference: ${refResult.message}")
                                            allReferencesAdded = false
                                        }
                                        is Resource.Loading -> {
                                            // Continue processing
                                        }
                                    }
                                }
                            }

                            if (allReferencesAdded) {
                                val postWithReferences = createdPost.copy(
                                    cookbookReferences = addedReferences
                                )
                                emit(Resource.Success(postWithReferences))
                            } else {
                                emit(Resource.Error("Post created but some cookbook references failed to add"))
                            }
                        } else {
                            // No cookbook references to add
                            emit(Resource.Success(createdPost))
                        }
                    }
                    is Resource.Error -> {
                        emit(Resource.Error(createResult.message ?: "Failed to create post"))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating post with cookbooks")
            emit(Resource.Error(e.message ?: "Unknown error occurred"))
        }
    }
}