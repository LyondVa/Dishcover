// FeedModule.kt - Updated with new local data source structure
package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.repository.feed.*
import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.local.FeedLocalDataSourceImpl
import com.nhatpham.dishcover.data.source.local.feed.*
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

    // Remote Data Sources (Domain-specific)
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
    fun provideFeedAggregationRemoteDataSource(
        firestore: FirebaseFirestore
    ): FeedAggregationRemoteDataSource {
        return FeedAggregationRemoteDataSource(firestore)
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

    // Legacy FeedRemoteDataSource facade
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

    // Local Data Sources (Domain-specific)
    @Provides
    @Singleton
    fun providePostLocalDataSource(): PostLocalDataSource {
        return PostLocalDataSource()
    }

    @Provides
    @Singleton
    fun providePostInteractionLocalDataSource(): PostInteractionLocalDataSource {
        return PostInteractionLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideCommentLocalDataSource(): CommentLocalDataSource {
        return CommentLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideFeedAggregationLocalDataSource(): FeedAggregationLocalDataSource {
        return FeedAggregationLocalDataSource()
    }

    @Provides
    @Singleton
    fun providePostAnalyticsLocalDataSource(): PostAnalyticsLocalDataSource {
        return PostAnalyticsLocalDataSource()
    }

    @Provides
    @Singleton
    fun providePostReferenceLocalDataSource(): PostReferenceLocalDataSource {
        return PostReferenceLocalDataSource()
    }

    // Legacy FeedLocalDataSource facade
    @Provides
    @Singleton
    fun provideFeedLocalDataSource(
        postLocalDataSource: PostLocalDataSource,
        postInteractionLocalDataSource: PostInteractionLocalDataSource,
        commentLocalDataSource: CommentLocalDataSource,
        feedAggregationLocalDataSource: FeedAggregationLocalDataSource,
        postAnalyticsLocalDataSource: PostAnalyticsLocalDataSource,
        postReferenceLocalDataSource: PostReferenceLocalDataSource
    ): FeedLocalDataSource {
        return FeedLocalDataSourceImpl(
            postLocalDataSource,
            postInteractionLocalDataSource,
            commentLocalDataSource,
            feedAggregationLocalDataSource,
            postAnalyticsLocalDataSource,
            postReferenceLocalDataSource
        )
    }

    // Domain-specific Repositories
    @Provides
    @Singleton
    fun providePostRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostRepository {
        return PostRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    @Provides
    @Singleton
    fun providePostInteractionRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostInteractionRepository {
        return PostInteractionRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): CommentRepository {
        return CommentRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    @Provides
    @Singleton
    fun provideFeedAggregationRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): FeedAggregationRepository {
        return FeedAggregationRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    @Provides
    @Singleton
    fun providePostMediaRepository(
        feedRemoteDataSource: FeedRemoteDataSource
    ): PostMediaRepository {
        return PostMediaRepositoryImpl(feedRemoteDataSource)
    }

    @Provides
    @Singleton
    fun providePostAnalyticsRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostAnalyticsRepository {
        return PostAnalyticsRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    @Provides
    @Singleton
    fun providePostReferenceRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): PostReferenceRepository {
        return PostReferenceRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }

    // Legacy FeedRepository for backward compatibility
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