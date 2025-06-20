package com.nhatpham.dishcover.data.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return authResult.user
    }



    suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return authResult.user
    }

    suspend fun updateUserProfile(displayName: String, photoUrl: String? = null): FirebaseUser? {
        val user = firebaseAuth.currentUser ?: return null

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .apply {
                photoUrl?.let { photoUri = android.net.Uri.parse(it) }
            }
            .build()

        user.updateProfile(profileUpdates).await()
        return user
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    suspend fun verifyPasswordResetCode(code: String): String {
        return firebaseAuth.verifyPasswordResetCode(code).await()
    }

    suspend fun confirmPasswordReset(code: String, newPassword: String) {
        firebaseAuth.confirmPasswordReset(code, newPassword).await()
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    suspend fun sendEmailVerification() {
        val user = firebaseAuth.currentUser ?: throw Exception("No authenticated user")
        user.sendEmailVerification().await()
    }

    suspend fun reloadUser(): FirebaseUser? {
        val user = firebaseAuth.currentUser
        user?.reload()?.await()
        return user
    }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        return authResult.user
    }
}