// di/RealTimeModule.kt
package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.data.repository.realtime.*
import com.nhatpham.dishcover.data.source.local.realtime.*
import com.nhatpham.dishcover.data.source.remote.realtime.*
import com.nhatpham.dishcover.domain.repository.realtime.*
import com.nhatpham.dishcover.domain.usecase.realtime.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealTimeModule {

    // Remote Data Sources
    @Provides
    @Singleton
    fun provideRealTimeEngagementDataSource(
        firestore: FirebaseFirestore
    ): RealTimeEngagementDataSource {
        return RealTimeEngagementDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideRealTimeFeedDataSource(
        firestore: FirebaseFirestore
    ): RealTimeFeedDataSource {
        return RealTimeFeedDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideRealTimeCommentDataSource(
        firestore: FirebaseFirestore
    ): RealTimeCommentDataSource {
        return RealTimeCommentDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideRealTimeUserActivityDataSource(
        firestore: FirebaseFirestore
    ): RealTimeUserActivityDataSource {
        return RealTimeUserActivityDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideRealTimeNotificationDataSource(
        firestore: FirebaseFirestore
    ): RealTimeNotificationDataSource {
        return RealTimeNotificationDataSource(firestore)
    }

    // Local Data Sources
    @Provides
    @Singleton
    fun provideRealTimeEngagementLocalDataSource(): RealTimeEngagementLocalDataSource {
        return RealTimeEngagementLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideRealTimeFeedLocalDataSource(): RealTimeFeedLocalDataSource {
        return RealTimeFeedLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideRealTimeCommentLocalDataSource(): RealTimeCommentLocalDataSource {
        return RealTimeCommentLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideRealTimeUserActivityLocalDataSource(): RealTimeUserActivityLocalDataSource {
        return RealTimeUserActivityLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideRealTimeNotificationLocalDataSource(): RealTimeNotificationLocalDataSource {
        return RealTimeNotificationLocalDataSource()
    }

    // Repository Implementations
    @Provides
    @Singleton
    fun provideRealTimeEngagementRepository(
        remoteDataSource: RealTimeEngagementDataSource,
        localDataSource: RealTimeEngagementLocalDataSource
    ): RealTimeEngagementRepository {
        return RealTimeEngagementRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Provides
    @Singleton
    fun provideRealTimeFeedRepository(
        remoteDataSource: RealTimeFeedDataSource,
        localDataSource: RealTimeFeedLocalDataSource
    ): RealTimeFeedRepository {
        return RealTimeFeedRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Provides
    @Singleton
    fun provideRealTimeCommentRepository(
        remoteDataSource: RealTimeCommentDataSource,
        localDataSource: RealTimeCommentLocalDataSource
    ): RealTimeCommentRepository {
        return RealTimeCommentRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Provides
    @Singleton
    fun provideRealTimeUserActivityRepository(
        remoteDataSource: RealTimeUserActivityDataSource,
        localDataSource: RealTimeUserActivityLocalDataSource
    ): RealTimeUserActivityRepository {
        return RealTimeUserActivityRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Provides
    @Singleton
    fun provideRealTimeNotificationRepository(
        remoteDataSource: RealTimeNotificationDataSource,
        localDataSource: RealTimeNotificationLocalDataSource
    ): RealTimeNotificationRepository {
        return RealTimeNotificationRepositoryImpl(remoteDataSource, localDataSource)
    }

    // Use Cases
    @Provides
    @Singleton
    fun provideObservePostEngagementUseCase(
        repository: RealTimeEngagementRepository
    ): ObservePostEngagementUseCase {
        return ObservePostEngagementUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideObserveFeedEngagementsUseCase(
        repository: RealTimeEngagementRepository
    ): ObserveFeedEngagementsUseCase {
        return ObserveFeedEngagementsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideReactToPostUseCase(
        repository: RealTimeEngagementRepository
    ): ReactToPostUseCase {
        return ReactToPostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveReactionUseCase(
        repository: RealTimeEngagementRepository
    ): RemoveReactionUseCase {
        return RemoveReactionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideTrackPostViewUseCase(
        engagementRepository: RealTimeEngagementRepository,
        userActivityRepository: RealTimeUserActivityRepository
    ): TrackPostViewUseCase {
        return TrackPostViewUseCase(engagementRepository, userActivityRepository)
    }

    @Provides
    @Singleton
    fun provideObserveFeedUpdatesUseCase(
        repository: RealTimeFeedRepository
    ): ObserveFeedUpdatesUseCase {
        return ObserveFeedUpdatesUseCase(repository)
    }

    @Provides
    @Singleton
    fun providePublishFeedUpdateUseCase(
        repository: RealTimeFeedRepository
    ): PublishFeedUpdateUseCase {
        return PublishFeedUpdateUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideObservePostCommentsUseCase(
        repository: RealTimeCommentRepository
    ): ObservePostCommentsUseCase {
        return ObservePostCommentsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddCommentUseCase(
        commentRepository: RealTimeCommentRepository,
        engagementRepository: RealTimeEngagementRepository,
        userActivityRepository: RealTimeUserActivityRepository
    ): AddCommentUseCase {
        return AddCommentUseCase(commentRepository, engagementRepository, userActivityRepository)
    }

    @Provides
    @Singleton
    fun provideStartTypingCommentUseCase(
        repository: RealTimeUserActivityRepository
    ): StartTypingCommentUseCase {
        return StartTypingCommentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideStopTypingCommentUseCase(
        repository: RealTimeUserActivityRepository
    ): StopTypingCommentUseCase {
        return StopTypingCommentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideObservePostViewersUseCase(
        repository: RealTimeUserActivityRepository
    ): ObservePostViewersUseCase {
        return ObservePostViewersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserOnlineStatusUseCase(
        repository: RealTimeUserActivityRepository
    ): UpdateUserOnlineStatusUseCase {
        return UpdateUserOnlineStatusUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideObserveUserNotificationsUseCase(
        repository: RealTimeNotificationRepository
    ): ObserveUserNotificationsUseCase {
        return ObserveUserNotificationsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSendNotificationUseCase(
        repository: RealTimeNotificationRepository
    ): SendNotificationUseCase {
        return SendNotificationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkNotificationAsReadUseCase(
        repository: RealTimeNotificationRepository
    ): MarkNotificationAsReadUseCase {
        return MarkNotificationAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetUnreadNotificationCountUseCase(
        repository: RealTimeNotificationRepository
    ): GetUnreadNotificationCountUseCase {
        return GetUnreadNotificationCountUseCase(repository)
    }

    // Composite Use Cases
    @Provides
    @Singleton
    fun provideHandlePostInteractionUseCase(
        reactToPostUseCase: ReactToPostUseCase,
        removeReactionUseCase: RemoveReactionUseCase,
        sendNotificationUseCase: SendNotificationUseCase
    ): HandlePostInteractionUseCase {
        return HandlePostInteractionUseCase(
            reactToPostUseCase,
            removeReactionUseCase,
            sendNotificationUseCase
        )
    }

    @Provides
    @Singleton
    fun provideHandleCommentAddedUseCase(
        addCommentUseCase: AddCommentUseCase,
        sendNotificationUseCase: SendNotificationUseCase
    ): HandleCommentAddedUseCase {
        return HandleCommentAddedUseCase(addCommentUseCase, sendNotificationUseCase)
    }
}

// Additional helper module for real-time configuration
@Module
@InstallIn(SingletonComponent::class)
object RealTimeConfigModule {

    @Provides
    @Singleton
    fun provideRealTimeConfig(): RealTimeConfig {
        return RealTimeConfig(
            maxCachedEngagements = 200,
            maxCachedComments = 100,
            maxFeedUpdates = 50,
            viewerActivityTimeoutMs = 300_000, // 5 minutes
            typingIndicatorTimeoutMs = 30_000,  // 30 seconds
            cleanupIntervalMs = 600_000         // 10 minutes
        )
    }
}

data class RealTimeConfig(
    val maxCachedEngagements: Int,
    val maxCachedComments: Int,
    val maxFeedUpdates: Int,
    val viewerActivityTimeoutMs: Long,
    val typingIndicatorTimeoutMs: Long,
    val cleanupIntervalMs: Long
)