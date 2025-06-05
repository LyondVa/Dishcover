// Updated FeedModule.kt
package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.repository.feed.*
import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSourceImpl
import com.nhatpham.dishcover.data.source.remote.feed.*
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

    // Specialized Remote Data Sources
    @Provides
    @Singleton
    fun providePostRemoteDataSource(
        firestore: FirebaseFirestore
    ): PostRemoteDataSource {
        return PostRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun providePostInteractionRemoteDataSource(
        firestore: FirebaseFirestore
    ): PostInteractionRemoteDataSource {
        return PostInteractionRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideCommentRemoteDataSource(
        firestore: FirebaseFirestore
    ): CommentRemoteDataSource {
        return CommentRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun providePostMediaRemoteDataSource(
        storage: FirebaseStorage
    ): PostMediaRemoteDataSource {
        return PostMediaRemoteDataSource(storage)
    }

    @Provides
    @Singleton
    fun providePostAnalyticsRemoteDataSource(
        firestore: FirebaseFirestore
    ): PostAnalyticsRemoteDataSource {
        return PostAnalyticsRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun providePostReferenceRemoteDataSource(
        firestore: FirebaseFirestore
    ): PostReferenceRemoteDataSource {
        return PostReferenceRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideFeedAggregationRemoteDataSource(
        firestore: FirebaseFirestore
    ): FeedAggregationRemoteDataSource {
        return FeedAggregationRemoteDataSource(firestore)
    }

    // Main FeedRemoteDataSource (Facade)
    @Provides
    @Singleton
    fun provideFeedRemoteDataSource(
        postRemoteDataSource: PostRemoteDataSource,
        postInteractionRemoteDataSource: PostInteractionRemoteDataSource,
        commentRemoteDataSource: CommentRemoteDataSource,
        postMediaRemoteDataSource: PostMediaRemoteDataSource,
        postAnalyticsRemoteDataSource: PostAnalyticsRemoteDataSource,
        postReferenceRemoteDataSource: PostReferenceRemoteDataSource,
        feedAggregationRemoteDataSource: FeedAggregationRemoteDataSource
    ): FeedRemoteDataSource {
        return FeedRemoteDataSourceImpl(
            postRemoteDataSource,
            postInteractionRemoteDataSource,
            commentRemoteDataSource,
            postMediaRemoteDataSource,
            postAnalyticsRemoteDataSource,
            postReferenceRemoteDataSource,
            feedAggregationRemoteDataSource
        )
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