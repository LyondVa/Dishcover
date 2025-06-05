// FeedModule.kt
package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.repository.feed.*
import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.repository.FeedRepository
import com.nhatpham.dishcover.domain.repository.feed.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    @Singleton
    fun provideFeedRemoteDataSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FeedRemoteDataSource {
        return FeedRemoteDataSource(firestore, storage)
    }

    @Provides
    @Singleton
    fun provideFeedLocalDataSource(): FeedLocalDataSource {
        return FeedLocalDataSource()
    }

    // Post Repository
    @Provides
    @Singleton
    fun providePostRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostRepository {
        return PostRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    // Post Interaction Repository
    @Provides
    @Singleton
    fun providePostInteractionRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostInteractionRepository {
        return PostInteractionRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    // Comment Repository
    @Provides
    @Singleton
    fun provideCommentRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): CommentRepository {
        return CommentRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    // Feed Aggregation Repository
    @Provides
    @Singleton
    fun provideFeedAggregationRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): FeedAggregationRepository {
        return FeedAggregationRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    // Post Media Repository
    @Provides
    @Singleton
    fun providePostMediaRepository(
        feedRemoteDataSource: FeedRemoteDataSource
    ): PostMediaRepository {
        return PostMediaRepositoryImpl(feedRemoteDataSource)
    }

    // Post Analytics Repository
    @Provides
    @Singleton
    fun providePostAnalyticsRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostAnalyticsRepository {
        return PostAnalyticsRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    // Post Reference Repository
    @Provides
    @Singleton
    fun providePostReferenceRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostReferenceRepository {
        return PostReferenceRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    // Legacy FeedRepository for backward compatibility
    // This will delegate to the new repositories
    @Provides
    @Singleton
    fun provideFeedRepository(
        postRepository: PostRepository,
        postInteractionRepository: PostInteractionRepository,
        commentRepository: CommentRepository,
        feedAggregationRepository: FeedAggregationRepository,
        postMediaRepository: PostMediaRepository,
        postAnalyticsRepository: PostAnalyticsRepository,
        postReferenceRepository: PostReferenceRepository
    ): FeedRepository {
        return FeedRepositoryFacade(
            postRepository,
            postInteractionRepository,
            commentRepository,
            feedAggregationRepository,
            postMediaRepository,
            postAnalyticsRepository,
            postReferenceRepository
        )
    }
}