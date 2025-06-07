// FeedRecipeModule.kt - Dependency injection for recipe linking functionality
package com.nhatpham.dishcover.di

import com.nhatpham.dishcover.domain.repository.FeedRepository
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.domain.usecase.feed.CreatePostWithRecipesUseCase
import com.nhatpham.dishcover.domain.usecase.recipe.GetUserRecipesForSelectionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedRecipeModule {

    @Provides
    @Singleton
    fun provideGetUserRecipesForSelectionUseCase(
        recipeRepository: RecipeRepository
    ): GetUserRecipesForSelectionUseCase {
        return GetUserRecipesForSelectionUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideCreatePostWithRecipesUseCase(
        feedRepository: FeedRepository
    ): CreatePostWithRecipesUseCase {
        return CreatePostWithRecipesUseCase(feedRepository)
    }
}