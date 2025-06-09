// SearchModule.kt - Dependency injection for search functionality
package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.data.repository.SearchRepositoryImpl
import com.nhatpham.dishcover.data.source.remote.search.SearchRemoteDataSource
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.data.source.remote.feed.PostRemoteDataSource
import com.nhatpham.dishcover.data.source.local.search.SearchLocalDataSource
import com.nhatpham.dishcover.domain.repository.SearchRepository
import com.nhatpham.dishcover.domain.usecase.search.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

    @Provides
    @Singleton
    fun provideSearchLocalDataSource(): SearchLocalDataSource {
        return SearchLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideSearchRemoteDataSource(
        firestore: FirebaseFirestore,
        userDataSource: FirestoreUserDataSource,
        postDataSource: PostRemoteDataSource,
        recipeDataSource: RecipeRemoteDataSource
    ): SearchRemoteDataSource {
        return SearchRemoteDataSource(
            firestore = firestore,
            userDataSource = userDataSource,
            postDataSource = postDataSource,
            recipeDataSource = recipeDataSource
        )
    }

    @Provides
    @Singleton
    fun provideSearchRepository(
        searchLocalDataSource: SearchLocalDataSource,
        searchRemoteDataSource: SearchRemoteDataSource
    ): SearchRepository {
        return SearchRepositoryImpl(
            searchLocalDataSource = searchLocalDataSource,
            searchRemoteDataSource = searchRemoteDataSource
        )
    }

    // Use Cases
    @Provides
    @Singleton
    fun provideUnifiedSearchUseCase(
        searchRepository: SearchRepository
    ): UnifiedSearchUseCase {
        return UnifiedSearchUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideSearchUsersUseCase(
        searchRepository: SearchRepository
    ): SearchUsersUseCase {
        return SearchUsersUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideSearchPostsUseCase(
        searchRepository: SearchRepository
    ): SearchPostsUseCase {
        return SearchPostsUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideSearchRecipesUseCase(
        searchRepository: SearchRepository
    ): SearchRecipesUseCase {
        return SearchRecipesUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideGetRecentSearchesUseCase(
        searchRepository: SearchRepository
    ): GetRecentSearchesUseCase {
        return GetRecentSearchesUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideSaveSearchQueryUseCase(
        searchRepository: SearchRepository
    ): SaveSearchQueryUseCase {
        return SaveSearchQueryUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideClearSearchHistoryUseCase(
        searchRepository: SearchRepository
    ): ClearSearchHistoryUseCase {
        return ClearSearchHistoryUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideGetTrendingSearchesUseCase(
        searchRepository: SearchRepository
    ): GetTrendingSearchesUseCase {
        return GetTrendingSearchesUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideGetSearchSuggestionsUseCase(
        searchRepository: SearchRepository
    ): GetSearchSuggestionsUseCase {
        return GetSearchSuggestionsUseCase(searchRepository)
    }

    @Provides
    @Singleton
    fun provideLogSearchAnalyticsUseCase(
        searchRepository: SearchRepository
    ): LogSearchAnalyticsUseCase {
        return LogSearchAnalyticsUseCase(searchRepository)
    }
}