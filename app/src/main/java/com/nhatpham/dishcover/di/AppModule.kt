package com.nhatpham.dishcover.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.data.repository.AuthRepositoryImpl
import com.nhatpham.dishcover.data.repository.RecipeRepositoryImpl
import com.nhatpham.dishcover.data.source.local.RecipeLocalDataSource
import com.nhatpham.dishcover.data.source.remote.FirebaseAuthDataSource
import com.nhatpham.dishcover.data.source.remote.FirestoreUserDataSource
import com.nhatpham.dishcover.data.source.remote.RecipeRemoteDataSource
import com.nhatpham.dishcover.domain.repository.AuthRepository
import com.nhatpham.dishcover.domain.repository.RecipeRepository
import com.nhatpham.dishcover.util.error.AuthErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(firebaseAuth: FirebaseAuth): FirebaseAuthDataSource {
        return FirebaseAuthDataSource(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideFirestoreUserDataSource(firestore: FirebaseFirestore): FirestoreUserDataSource {
        return FirestoreUserDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthDataSource: FirebaseAuthDataSource,
        firestoreUserDataSource: FirestoreUserDataSource,
        authErrorHandler: AuthErrorHandler
    ): AuthRepository {
        return AuthRepositoryImpl(
            firebaseAuthDataSource, firestoreUserDataSource, authErrorHandler
        )
    }

    @Provides
    @Singleton
    fun provideRecipeRemoteDataSource(firestore: FirebaseFirestore): RecipeRemoteDataSource {
        return RecipeRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideRecipeLocalDataSource(): RecipeLocalDataSource {
        return RecipeLocalDataSource()
    }

    @Provides
    @Singleton
    fun provideRecipeRepository(
        recipeRemoteDataSource: RecipeRemoteDataSource, recipeLocalDataSource: RecipeLocalDataSource
    ): RecipeRepository {
        return RecipeRepositoryImpl(recipeRemoteDataSource, recipeLocalDataSource)
    }
}