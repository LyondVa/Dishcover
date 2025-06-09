package com.nhatpham.dishcover.di

import com.google.firebase.firestore.FirebaseFirestore
import com.nhatpham.dishcover.data.repository.AdminRepositoryImpl
import com.nhatpham.dishcover.data.source.remote.AdminRemoteDataSource
import com.nhatpham.dishcover.domain.repository.AdminRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminModule {

    @Provides
    @Singleton
    fun provideAdminRemoteDataSource(
        firestore: FirebaseFirestore
    ): AdminRemoteDataSource {
        return AdminRemoteDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideAdminRepository(
        adminRemoteDataSource: AdminRemoteDataSource
    ): AdminRepository {
        return AdminRepositoryImpl(adminRemoteDataSource)
    }
}