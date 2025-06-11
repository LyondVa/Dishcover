// CookbookListViewModel.kt
package com.nhatpham.dishcover.presentation.cookbook.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhatpham.dishcover.domain.usecase.cookbook.GetUserCookbooksUseCase
import com.nhatpham.dishcover.domain.usecase.cookbook.SearchCookbooksUseCase
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CookbookListViewModel @Inject constructor(
    private val getUserCookbooksUseCase: GetUserCookbooksUseCase,
    private val searchCookbooksUseCase: SearchCookbooksUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CookbookListState())
    val state: StateFlow<CookbookListState> = _state.asStateFlow()

    private var currentUserId: String? = null
    private var searchJob: Job? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            currentUserId = user.userId
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = "Failed to load user: ${result.message}"
                        )
                    }
                    is Resource.Loading -> {
                        // Handle if needed
                    }
                }
            }
        }
    }

    fun loadUserCookbooks() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            getUserCookbooksUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            cookbooks = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to load cookbooks"
                        )
                        Timber.e("Failed to load user cookbooks: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun searchCookbooks(query: String) {
        _state.value = _state.value.copy(searchQuery = query)

        searchJob?.cancel()

        if (query.isBlank()) {
            loadUserCookbooks()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce search

            _state.value = _state.value.copy(isSearching = true, error = null)

            searchCookbooksUseCase(query).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            cookbooks = result.data ?: emptyList(),
                            isSearching = false,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isSearching = false,
                            error = result.message ?: "Search failed"
                        )
                        Timber.e("Failed to search cookbooks: ${result.message}")
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isSearching = true)
                    }
                }
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _state.value = _state.value.copy(searchQuery = "")
        loadUserCookbooks()
    }

    fun retry() {
        if (_state.value.searchQuery.isBlank()) {
            loadUserCookbooks()
        } else {
            searchCookbooks(_state.value.searchQuery)
        }
    }
}