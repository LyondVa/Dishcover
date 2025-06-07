// CreatePostWithRecipesUseCase.kt
package com.nhatpham.dishcover.domain.usecase.feed

import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.repository.FeedRepository
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CreatePostWithRecipesUseCase @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(
        post: Post,
        linkedRecipes: List<RecipeListItem>
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

                        // Create recipe references if there are any linked recipes
                        if (linkedRecipes.isNotEmpty()) {
                            val recipeReferences = linkedRecipes.map { recipe ->
                                PostRecipeReference(
                                    referenceId = UUID.randomUUID().toString(),
                                    postId = createdPost.postId,
                                    recipeId = recipe.recipeId,
                                    displayText = recipe.title,
                                    createdAt = createdPost.createdAt,
                                    userId = createdPost.userId,
                                    coverImage = recipe.coverImage,
                                )
                            }

                            Timber.d("🔧 Saving ${recipeReferences.size} recipe references...")

                            // Save recipe references one by one and wait for completion
                            val savedReferences = mutableListOf<PostRecipeReference>()

                            for (reference in recipeReferences) {
                                try {
                                    feedRepository.addRecipeReference(reference).collect { refResult ->
                                        when (refResult) {
                                            is Resource.Success -> {
                                                refResult.data?.let { savedRef ->
                                                    savedReferences.add(savedRef)
                                                    Timber.d("✅ Saved recipe reference: ${savedRef.displayText}")
                                                }
                                            }
                                            is Resource.Error -> {
                                                Timber.e("❌ Failed to save recipe reference: ${reference.displayText} - ${refResult.message}")
                                            }
                                            is Resource.Loading -> {
                                                // Continue waiting
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "💥 Exception saving recipe reference: ${reference.displayText}")
                                }
                            }

                            // Update the post with the saved recipe references
                            val updatedPost = createdPost.copy(
                                recipeReferences = savedReferences
                            )

                            Timber.d("🎉 Post created with ${savedReferences.size} recipe references")
                            emit(Resource.Success(updatedPost))
                        } else {
                            emit(Resource.Success(createdPost))
                        }
                    }
                    is Resource.Error -> {
                        emit(Resource.Error(createResult.message ?: "Failed to create post"))
                    }
                }
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Failed to create post with recipes"))
        }
    }
}