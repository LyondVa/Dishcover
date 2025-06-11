package com.nhatpham.dishcover.presentation.feed.create

import android.net.Uri
import com.nhatpham.dishcover.domain.model.cookbook.CookbookListItem
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem

data class CreatePostState(
    // Post content
    val caption: String = "",
    val hashtags: String = "",
    val location: String = "",
    val isPublic: Boolean = true,
    val allowComments: Boolean = true,

    // Media
    val selectedImages: List<Uri> = emptyList(),
    val isUploadingImages: Boolean = false,
    val imageUploadError: String? = null,

    // Recipe linking
    val selectedRecipes: List<RecipeListItem> = emptyList(),
    val maxRecipes: Int = 5,

    val selectedCookbooks: List<CookbookListItem> = emptyList(),
    val maxCookbooks: Int = 3,

    // Post creation state
    val isCreating: Boolean = false,
    val isCreated: Boolean = false,
    val createdPostId: String? = null,
    val error: String? = null
) {
    val canAddMoreRecipes: Boolean
        get() = selectedRecipes.size < maxRecipes

    val canAddMoreCookbooks: Boolean
        get() = selectedCookbooks.size < maxCookbooks

    val hasRecipes: Boolean
        get() = selectedRecipes.isNotEmpty()

    val hasCookbooks: Boolean
        get() = selectedCookbooks.isNotEmpty()

    val hasReferences: Boolean
        get() = hasRecipes || hasCookbooks

    val canCreatePost: Boolean
        get() = !isCreating && (caption.isNotBlank() || selectedImages.isNotEmpty() || hasRecipes)
}