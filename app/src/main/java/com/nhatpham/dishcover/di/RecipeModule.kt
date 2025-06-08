package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.repository.RecipeRepositoryImpl
import com.nhatpham.dishcover.data.source.local.RecipeLocalDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.domain.usecase.recipe.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecipeModule {

    @Provides
    @Singleton
    fun provideRecipeLocalDataSource(): RecipeLocalDataSource {
        return RecipeLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideRecipeRemoteDataSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): RecipeRemoteDataSource {
        return RecipeRemoteDataSource(
            firestore = firestore,
            storage = storage
        )
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipeRemoteDataSource: RecipeRemoteDataSource,
        recipeLocalDataSource: RecipeLocalDataSource
    ): RecipeRepository {
        return RecipeRepositoryImpl(recipeRemoteDataSource, recipeLocalDataSource)
    }

    // Existing Recipe Use Cases
    @Provides
    @Singleton
    fun provideCreateRecipeUseCase(
        recipeRepository: RecipeRepository
    ): CreateRecipeUseCase {
        return CreateRecipeUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideGetRecipeUseCase(
        recipeRepository: RecipeRepository
    ): GetRecipeUseCase {
        return GetRecipeUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateRecipeUseCase(
        recipeRepository: RecipeRepository
    ): UpdateRecipeUseCase {
        return UpdateRecipeUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteRecipeUseCase(
        recipeRepository: RecipeRepository
    ): DeleteRecipeUseCase {
        return DeleteRecipeUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideSearchRecipesUseCase(
        recipeRepository: RecipeRepository
    ): SearchRecipesUseCase {
        return SearchRecipesUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserRecipesUseCase(
        recipeRepository: RecipeRepository
    ): GetUserRecipesUseCase {
        return GetUserRecipesUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideGetFavoriteRecipesUseCase(
        recipeRepository: RecipeRepository
    ): GetFavoriteRecipesUseCase {
        return GetFavoriteRecipesUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideGetRecentRecipesUseCase(
        recipeRepository: RecipeRepository
    ): GetRecentRecipesUseCase {
        return GetRecentRecipesUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideCreateIngredientUseCase(
        recipeRepository: RecipeRepository
    ): CreateIngredientUseCase {
        return CreateIngredientUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideGetSystemIngredientsUseCase(
        recipeRepository: RecipeRepository
    ): GetSystemIngredientsUseCase {
        return GetSystemIngredientsUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideSearchIngredientsUseCase(
        recipeRepository: RecipeRepository
    ): SearchIngredientsUseCase {
        return SearchIngredientsUseCase(recipeRepository)
    }

    // NEW: Rating Use Cases
    @Provides
    @Singleton
    fun provideGetRecipeRatingsUseCase(
        recipeRepository: RecipeRepository
    ): GetRecipeRatingsUseCase {
        return GetRecipeRatingsUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideAddRecipeRatingUseCase(
        recipeRepository: RecipeRepository
    ): AddRecipeRatingUseCase {
        return AddRecipeRatingUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserRecipeRatingUseCase(
        recipeRepository: RecipeRepository
    ): GetUserRecipeRatingUseCase {
        return GetUserRecipeRatingUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateRecipeRatingUseCase(
        recipeRepository: RecipeRepository
    ): UpdateRecipeRatingUseCase {
        return UpdateRecipeRatingUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteRecipeRatingUseCase(
        recipeRepository: RecipeRepository
    ): DeleteRecipeRatingUseCase {
        return DeleteRecipeRatingUseCase(recipeRepository)
    }

    // NEW: Review Use Cases
    @Provides
    @Singleton
    fun provideGetRecipeReviewsUseCase(
        recipeRepository: RecipeRepository
    ): GetRecipeReviewsUseCase {
        return GetRecipeReviewsUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideAddRecipeReviewUseCase(
        recipeRepository: RecipeRepository
    ): AddRecipeReviewUseCase {
        return AddRecipeReviewUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateRecipeReviewUseCase(
        recipeRepository: RecipeRepository
    ): UpdateRecipeReviewUseCase {
        return UpdateRecipeReviewUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteRecipeReviewUseCase(
        recipeRepository: RecipeRepository
    ): DeleteRecipeReviewUseCase {
        return DeleteRecipeReviewUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideMarkReviewHelpfulUseCase(
        recipeRepository: RecipeRepository
    ): MarkReviewHelpfulUseCase {
        return MarkReviewHelpfulUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserReviewForRecipeUseCase(
        recipeRepository: RecipeRepository
    ): GetUserReviewForRecipeUseCase {
        return GetUserReviewForRecipeUseCase(recipeRepository)
    }

    // NEW: Recipe Scaling Use Cases
    @Provides
    @Singleton
    fun provideScaleRecipeIngredientsUseCase(): ScaleRecipeIngredientsUseCase {
        return ScaleRecipeIngredientsUseCase()
    }

    // NEW: Nutritional Information Use Cases
    @Provides
    @Singleton
    fun provideGetNutritionalInfoUseCase(
        recipeRepository: RecipeRepository
    ): GetNutritionalInfoUseCase {
        return GetNutritionalInfoUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideCalculateNutritionalInfoUseCase(
        recipeRepository: RecipeRepository
    ): CalculateNutritionalInfoUseCase {
        return CalculateNutritionalInfoUseCase(recipeRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateNutritionalInfoUseCase(
        recipeRepository: RecipeRepository
    ): UpdateNutritionalInfoUseCase {
        return UpdateNutritionalInfoUseCase(recipeRepository)
    }
}
