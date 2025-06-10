// EditCookbookViewModel.kt
package com.nhatpham.dishcover.presentation.cookbook.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.nhatpham.dishcover.domain.model.cookbook.Cookbook
import com.nhatpham.dishcover.domain.usecase.cookbook.*
import com.nhatpham.dishcover.domain.usecase.user.GetCurrentUserUseCase
import com.nhatpham.dishcover.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditCookbookViewModel @Inject constructor(
    private val getCookbookUseCase: GetCookbookUseCase,
    private val updateCookbookUseCase: UpdateCookbookUseCase,
    private val deleteCookbookUseCase: DeleteCookbookUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditCookbookState())
    val state: StateFlow<EditCookbookState> = _state.asStateFlow()

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

    fun onEvent(event: EditCookbookEvent) {
        when (event) {
            is EditCookbookEvent.LoadCookbook -> {
                loadCookbook(event.cookbookId)
            }
            is EditCookbookEvent.UpdateTitle -> {
                updateTitle(event.title)
            }
            is EditCookbookEvent.UpdateDescription -> {
                updateDescription(event.description)
            }
            is EditCookbookEvent.UpdateCoverImage -> {
                updateCoverImage(event.imageUrl)
            }
            is EditCookbookEvent.UpdateIsPublic -> {
                updateIsPublic(event.isPublic)
            }
            is EditCookbookEvent.UpdateIsCollaborative -> {
                updateIsCollaborative(event.isCollaborative)
            }
            is EditCookbookEvent.UpdateTags -> {
                updateTags(event.tags)
            }
            is EditCookbookEvent.UpdateCookbook -> {
                updateCookbook()
            }
            is EditCookbookEvent.ShowDeleteConfirmation -> {
                _state.value = _state.value.copy(showDeleteDialog = true)
            }
            is EditCookbookEvent.HideDeleteConfirmation -> {
                _state.value = _state.value.copy(showDeleteDialog = false)
            }
            is EditCookbookEvent.DeleteCookbook -> {
                deleteCookbook()
            }
            is EditCookbookEvent.ClearDeleteNavigation -> {
                _state.value = _state.value.copy(navigateBackAfterDelete = false)
            }
        }
    }

    private fun loadCookbook(cookbookId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                loadError = null
            )

            getCookbookUseCase(cookbookId).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val cookbook = result.data
                        if (cookbook != null) {
                            // Check if user is owner
                            val isOwner = cookbook.userId == currentUserId
                            if (!isOwner) {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    loadError = "You don't have permission to edit this cookbook"
                                )
                                return@collectLatest
                            }

                            // Initialize form with cookbook data
                            _state.value = _state.value.copy(
                                isLoading = false,
                                originalCookbook = cookbook,
                                title = cookbook.title,
                                description = cookbook.description ?: "",
                                coverImageUrl = cookbook.coverImage,
                                isPublic = cookbook.isPublic,
                                isCollaborative = cookbook.isCollaborative,
                                tags = cookbook.tags,
                                isOwner = true,
                                loadError = null
                            )
                            updateValidation()
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                loadError = "Cookbook not found"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            loadError = result.message ?: "Failed to load cookbook"
                        )
                        Timber.e("Error loading cookbook: ${result.message}")
                    }
                }
            }
        }
    }

    private fun updateTitle(title: String) {
        _state.value = _state.value.copy(
            title = title,
            updateError = null
        )
        updateValidation()
        updateHasChanges()
    }

    private fun updateDescription(description: String) {
        _state.value = _state.value.copy(
            description = description,
            updateError = null
        )
        updateHasChanges()
    }

    private fun updateCoverImage(imageUrl: String?) {
        _state.value = _state.value.copy(
            coverImageUrl = imageUrl,
            updateError = null
        )
        updateHasChanges()
    }

    private fun updateIsPublic(isPublic: Boolean) {
        val currentState = _state.value
        val newState = currentState.copy(
            isPublic = isPublic,
            updateError = null
        )

        // If making private, disable collaboration
        if (!isPublic && newState.isCollaborative) {
            _state.value = newState.copy(isCollaborative = false)
        } else {
            _state.value = newState
        }
        updateHasChanges()
    }

    private fun updateIsCollaborative(isCollaborative: Boolean) {
        _state.value = _state.value.copy(
            isCollaborative = isCollaborative,
            updateError = null
        )
        updateHasChanges()
    }

    private fun updateTags(tags: List<String>) {
        _state.value = _state.value.copy(
            tags = tags,
            updateError = null
        )
        updateHasChanges()
    }

    private fun updateValidation() {
        val currentState = _state.value
        val canSave = currentState.title.isNotBlank() &&
                currentState.originalCookbook != null &&
                currentUserId.isNotEmpty() &&
                !currentState.isUpdating

        _state.value = currentState.copy(canSave = canSave)
    }

    private fun updateHasChanges() {
        val currentState = _state.value
        val originalCookbook = currentState.originalCookbook ?: return

        val hasChanges = currentState.title != originalCookbook.title ||
                currentState.description != (originalCookbook.description ?: "") ||
                currentState.coverImageUrl != originalCookbook.coverImage ||
                currentState.isPublic != originalCookbook.isPublic ||
                currentState.isCollaborative != originalCookbook.isCollaborative ||
                currentState.tags != originalCookbook.tags

        _state.value = currentState.copy(hasChanges = hasChanges)
        updateValidation()
    }

    private fun updateCookbook() {
        val currentState = _state.value
        val originalCookbook = currentState.originalCookbook ?: return

        if (!currentState.canSave) {
            _state.value = currentState.copy(
                updateError = "Please fill in all required fields"
            )
            return
        }

        if (!currentState.hasChanges) {
            _state.value = currentState.copy(
                updateError = "No changes to save"
            )
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(
                isUpdating = true,
                updateError = null
            )

            val updatedCookbook = originalCookbook.copy(
                title = currentState.title.trim(),
                description = currentState.description.trim().takeIf { it.isNotBlank() },
                coverImage = currentState.coverImageUrl,
                isPublic = currentState.isPublic,
                isCollaborative = currentState.isCollaborative,
                tags = currentState.tags.filter { it.isNotBlank() },
                updatedAt = Timestamp.now()
            )

            updateCookbookUseCase(updatedCookbook).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already set loading state above
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isUpdating = false,
                            isUpdated = true,
                            originalCookbook = result.data,
                            hasChanges = false,
                            updateError = null
                        )
                        Timber.d("Cookbook updated successfully")
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isUpdating = false,
                            updateError = result.message ?: "Failed to update cookbook"
                        )
                        Timber.e("Error updating cookbook: ${result.message}")
                    }
                }
            }
        }
    }

    private fun deleteCookbook() {
        val cookbookId = _state.value.originalCookbook?.cookbookId ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isUpdating = true,
                showDeleteDialog = false,
                updateError = null
            )

            deleteCookbookUseCase(cookbookId).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        // Already set loading state above
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isUpdating = false,
                            isDeleted = true,
                            navigateBackAfterDelete = true,
                            updateError = null
                        )
                        Timber.d("Cookbook deleted successfully")
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isUpdating = false,
                            updateError = result.message ?: "Failed to delete cookbook"
                        )
                        Timber.e("Error deleting cookbook: ${result.message}")
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(
            loadError = null,
            updateError = null
        )
    }
}

// EditCookbookState.kt
data class EditCookbookState(
    // Original cookbook data
    val originalCookbook: Cookbook? = null,

    // Form fields
    val title: String = "",
    val description: String = "",
    val coverImageUrl: String? = null,
    val isPublic: Boolean = true,
    val isCollaborative: Boolean = false,
    val tags: List<String> = emptyList(),

    // UI state
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isUpdated: Boolean = false,
    val isDeleted: Boolean = false,
    val navigateBackAfterDelete: Boolean = false,
    val canSave: Boolean = false,
    val hasChanges: Boolean = false,
    val isOwner: Boolean = false,
    val showDeleteDialog: Boolean = false,

    // Error states
    val loadError: String? = null,
    val updateError: String? = null
)

// EditCookbookEvent.kt
sealed class EditCookbookEvent {
    data class LoadCookbook(val cookbookId: String) : EditCookbookEvent()
    data class UpdateTitle(val title: String) : EditCookbookEvent()
    data class UpdateDescription(val description: String) : EditCookbookEvent()
    data class UpdateCoverImage(val imageUrl: String?) : EditCookbookEvent()
    data class UpdateIsPublic(val isPublic: Boolean) : EditCookbookEvent()
    data class UpdateIsCollaborative(val isCollaborative: Boolean) : EditCookbookEvent()
    data class UpdateTags(val tags: List<String>) : EditCookbookEvent()
    object UpdateCookbook : EditCookbookEvent()
    object ShowDeleteConfirmation : EditCookbookEvent()
    object HideDeleteConfirmation : EditCookbookEvent()
    object DeleteCookbook : EditCookbookEvent()
    object ClearDeleteNavigation : EditCookbookEvent()
}