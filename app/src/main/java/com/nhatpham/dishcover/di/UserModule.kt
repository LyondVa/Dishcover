package com.nhatpham.dishcover.di

import com.google.firebase.storage.FirebaseStorage
import com.nhatpham.dishcover.data.repository.UserImageRepositoryImpl
import com.nhatpham.dishcover.data.source.remote.UserImageRemoteDataSource
import com.nhatpham.dishcover.domain.repository.UserImageRepository
import com.nhatpham.dishcover.domain.usecase.user.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserImageRemoteDataSource(
        storage: FirebaseStorage
    ): UserImageRemoteDataSource {
        return UserImageRemoteDataSource(storage)
    }

    @Provides
    @Singleton
    fun provideUserImageRepository(
        userImageRemoteDataSource: UserImageRemoteDataSource
    ): UserImageRepository {
        return UserImageRepositoryImpl(userImageRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUploadProfileImageUseCase(
        userImageRepository: UserImageRepository
    ): UploadProfileImageUseCase {
        return UploadProfileImageUseCase(userImageRepository)
    }

    @Provides
    @Singleton
    fun provideUploadBannerImageUseCase(
        userImageRepository: UserImageRepository
    ): UploadBannerImageUseCase {
        return UploadBannerImageUseCase(userImageRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteUserImageUseCase(
        userImageRepository: UserImageRepository
    ): DeleteUserImageUseCase {
        return DeleteUserImageUseCase(userImageRepository)
    }
}