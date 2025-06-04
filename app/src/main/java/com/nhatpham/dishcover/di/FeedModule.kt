// FeedModule.kt
package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.repository.FeedRepositoryImpl
import com.nhatpham.dishcover.data.source.local.FeedLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FeedRemoteDataSource
import com.nhatpham.dishcover.domain.repository.FeedRepository
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

    @Provides
    @Singleton
    fun provideFeedRepository(
        feedRemoteDataSource: FeedRemoteDataSource,
        feedLocalDataSource: FeedLocalDataSource
    ): FeedRepository {
        return FeedRepositoryImpl(feedRemoteDataSource, feedLocalDataSource)
    }
}