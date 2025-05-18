package com.nhatpham.dishcover.domain.repository

import com.nhatpham.dishcover.domain.model.User
import kotlinx.coroutines.flow.Flow
import com.nhatpham.dishcover.util.error.Result

interface AuthRepository {
    fun signIn(email: String, password: String): Flow<Result<User>>
    fun signUp(email: String, password: String, username: String): Flow<Result<User>>
    fun signInWithGoogle(idToken: String): Flow<Result<User>>
    fun resetPassword(email: String): Flow<Result<Unit>>
    fun verifyPasswordResetCode(code: String): Flow<Result<String>>
    fun confirmPasswordReset(code: String, newPassword: String): Flow<Result<Unit>>
    fun signOut(): Flow<Result<Unit>>
    fun getCurrentUser(): Flow<Result<User>>
}