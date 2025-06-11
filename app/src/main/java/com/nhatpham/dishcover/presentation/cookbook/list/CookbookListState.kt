// CookbookListState.kt
package com.nhatpham.dishcover.presentation.cookbook.list

import com.nhatpham.dishcover.domain.model.cookbook.CookbookListItem

data class CookbookListState(
    val cookbooks: List<CookbookListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val hasMoreData: Boolean = true,
    val isLoadingMore: Boolean = false
)