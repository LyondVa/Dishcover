// CreatePostEvent.kt - Updated with recipe linking events
package com.nhatpham.dishcover.presentation.feed.create

import android.net.Uri
import com.nhatpham.dishcover.domain.model.cookbook.CookbookListItem
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem

sealed class CreatePostEvent {
    data class CaptionChanged(val caption: String) : CreatePostEvent()
    data class HashtagsChanged(val hashtags: String) : CreatePostEvent()
    data class LocationChanged(val location: String) : CreatePostEvent()
    object PrivacyToggled : CreatePostEvent()
    object CommentsToggled : CreatePostEvent()

    data class ImageSelected(val uri: Uri) : CreatePostEvent()
    data class ImageRemoved(val uri: Uri) : CreatePostEvent()

    data class RecipeAdded(val recipe: RecipeListItem) : CreatePostEvent()
    data class RecipeRemoved(val recipe: RecipeListItem) : CreatePostEvent()
    object ClearSelectedRecipes : CreatePostEvent()

    data class CookbookAdded(val cookbook: CookbookListItem) : CreatePostEvent()
    data class CookbookRemoved(val cookbook: CookbookListItem) : CreatePostEvent()
    object ClearSelectedCookbooks : CreatePostEvent()

    object CreatePost : CreatePostEvent()
    object ResetState : CreatePostEvent()
}