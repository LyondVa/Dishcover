package com.nhatpham.dishcover.presentation.auth

import androidx.lifecycle.ViewModel
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel  // Make sure this annotation is present
class AuthViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    fun getCurrentUser() = getCurrentUserUseCase()

    fun signOut() = signOutUseCase()
}