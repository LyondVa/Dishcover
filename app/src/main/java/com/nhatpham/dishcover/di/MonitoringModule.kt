package com.nhatpham.dishcover.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhatpham.dishcover.util.analytics.AnalyticsTracker
import com.nhatpham.dishcover.util.analytics.FirebaseAnalyticsTracker
import com.nhatpham.dishcover.util.error.*
import com.nhatpham.dishcover.util.error.logging.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonitoringModule {

    // Firebase Analytics
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsTracker(
        firebaseAnalytics: FirebaseAnalytics
    ): AnalyticsTracker {
        return FirebaseAnalyticsTracker(firebaseAnalytics)
    }

    // Firebase Crashlytics
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }

    // Error Loggers
    @Provides
    @Singleton
    fun provideCrashlyticsErrorLogger(
        crashlytics: FirebaseCrashlytics
    ): CrashlyticsErrorLogger {
        return CrashlyticsErrorLogger(crashlytics)
    }

    @Provides
    @Singleton
    fun provideFileErrorLogger(
        @ApplicationContext context: Context
    ): FileErrorLogger {
        return FileErrorLogger(context)
    }

    @Provides
    @Singleton
    fun provideConsoleErrorLogger(): ConsoleErrorLogger {
        return ConsoleErrorLogger()
    }

    @Provides
    @Singleton
    fun provideErrorLogger(
        crashlyticsLogger: CrashlyticsErrorLogger,
        fileLogger: FileErrorLogger,
        consoleLogger: ConsoleErrorLogger
    ): ErrorLogger {
        return CompositeErrorLogger(crashlyticsLogger, fileLogger, consoleLogger)
    }

    // Error Handlers
    @Provides
    @Singleton
    fun provideErrorFactory(): ErrorFactory {
        return ErrorFactoryImpl()
    }

    @Provides
    @Singleton
    fun provideErrorHandler(
        @ApplicationContext context: Context,
        analyticsTracker: AnalyticsTracker,
        errorLogger: ErrorLogger
    ): ErrorHandler {
        val errorHandler = ErrorHandlerImpl(context, analyticsTracker)
        errorHandler.setErrorLogger(errorLogger)
        return errorHandler
    }

    @Provides
    @Singleton
    fun provideAuthErrorHandler(
        @ApplicationContext context: Context,
        errorHandler: ErrorHandler
    ): AuthErrorHandler {
        return AuthErrorHandler(context, errorHandler)
    }

    @Provides
    @Singleton
    fun provideRecipeErrorHandler(
        @ApplicationContext context: Context,
        errorHandler: ErrorHandler
    ): RecipeErrorHandler {
        return RecipeErrorHandler(context, errorHandler)
    }
}