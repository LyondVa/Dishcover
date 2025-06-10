// CookbookModule.kt
package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.data.repository.CookbookRepositoryImpl
import com.nhatpham.dishcover.data.source.local.CookbookLocalDataSource
import com.nhatpham.dishcover.data.source.remote.CookbookRemoteDataSource
import com.nhatpham.dishcover.domain.repository.CookbookRepository
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CookbookModule {

    @Provides
    @Singleton
    fun provideCookbookRemoteDataSource(
        firestore: FirebaseFirestore
    ): CookbookRemoteDataSource {
        return CookbookRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideCookbookLocalDataSource(): CookbookLocalDataSource {
        return CookbookLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideCookbookRepository(
        cookbookRemoteDataSource: CookbookRemoteDataSource,
        cookbookLocalDataSource: CookbookLocalDataSource,
        recipeRepository: RecipeRepository
    ): CookbookRepository {
        return CookbookRepositoryImpl(
            cookbookRemoteDataSource,
            cookbookLocalDataSource,
            recipeRepository
        )
    }
}