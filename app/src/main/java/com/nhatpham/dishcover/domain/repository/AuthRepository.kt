package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.User
import com.nhatpham.dishcover.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signIn(email: String, password: String): Flow<Resource<User>>
    fun signUp(email: String, password: String, username: String): Flow<Resource<User>>
    fun signInWithGoogle(idToken: String): Flow<Resource<User>>
    fun resetPassword(email: String): Flow<Resource<Unit>>
    fun verifyPasswordResetCode(code: String): Flow<Resource<String>>
    fun confirmPasswordReset(code: String, newPassword: String): Flow<Resource<Unit>>
    fun signOut(): Flow<Resource<Unit>>
    fun getCurrentUser(): Flow<Resource<User>>
}