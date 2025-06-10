// CookbooksViewModel.kt
package com.nhatpham.dishcover.presentation.cookbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.model.cookbook.CookbookListItem
import com.nhatpham.dishcover.domain.usecase.cookbook.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CookbooksViewModel @Inject constructor(
    private val getUserCookbooksUseCase: GetUserCookbooksUseCase,
    private val getPublicCookbooksUseCase: GetPublicCookbooksUseCase,
    private val getFeaturedCookbooksUseCase: GetFeaturedCookbooksUseCase,
    private val getFollowedCookbooksUseCase: GetFollowedCookbooksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CookbooksState())
    val state: StateFlow<CookbooksState> = _state.asStateFlow()

    private var currentUserId: String = ""

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        currentUserId = result.data?.userId ?: ""
                        if (currentUserId.isNotEmpty()) {
                            loadCookbooks()
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Error getting current user: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            }
        }
    }

    fun loadCookbooks() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            when (_state.value.selectedFilter) {
                CookbookFilter.MY_COOKBOOKS -> loadUserCookbooks()
                CookbookFilter.FOLLOWING -> loadFollowedCookbooks()
                CookbookFilter.FEATURED -> loadFeaturedCookbooks()
                CookbookFilter.PUBLIC -> loadPublicCookbooks()
            }
        }
    }

    fun selectFilter(filter: CookbookFilter) {
        if (_state.value.selectedFilter == filter) return

        _state.value = _state.value.copy(
            selectedFilter = filter,
            cookbooks = emptyList(),
            isLoading = true,
            error = null
        )
        loadCookbooks()
    }

    private suspend fun loadUserCookbooks() {
        getUserCookbooksUseCase(currentUserId, limit = 50).collectLatest { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        cookbooks = result.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load cookbooks"
                    )
                    Timber.e("Error loading user cookbooks: ${result.message}")
                }
            }
        }
    }

    private suspend fun loadFollowedCookbooks() {
        getFollowedCookbooksUseCase(currentUserId, limit = 50).collectLatest { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        cookbooks = result.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load followed cookbooks"
                    )
                    Timber.e("Error loading followed cookbooks: ${result.message}")
                }
            }
        }
    }

    private suspend fun loadFeaturedCookbooks() {
        getFeaturedCookbooksUseCase(limit = 50).collectLatest { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        cookbooks = result.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load featured cookbooks"
                    )
                    Timber.e("Error loading featured cookbooks: ${result.message}")
                }
            }
        }
    }

    private suspend fun loadPublicCookbooks() {
        getPublicCookbooksUseCase(limit = 50).collectLatest { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                }
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        cookbooks = result.data ?: emptyList(),
                        error = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "Failed to load public cookbooks"
                    )
                    Timber.e("Error loading public cookbooks: ${result.message}")
                }
            }
        }
    }

    fun refreshCookbooks() {
        loadCookbooks()
    }
}

// CookbooksState.kt
data class CookbooksState(
    val cookbooks: List<CookbookListItem> = emptyList(),
    val selectedFilter: CookbookFilter = CookbookFilter.MY_COOKBOOKS,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class CookbookFilter {
    MY_COOKBOOKS,
    FOLLOWING,
    FEATURED,
    PUBLIC
}