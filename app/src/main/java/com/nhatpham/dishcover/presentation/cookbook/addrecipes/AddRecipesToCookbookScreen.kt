// AddRecipesToCookbookScreen.kt
package com.nhatpham.dishcover.presentation.cookbook.addrecipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.cookbook.addrecipes.components.RecipeSelectionCard
import com.nhatpham.dishcover.presentation.cookbook.addrecipes.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipesToCookbookScreen(
    cookbookId: String,
    onNavigateBack: () -> Unit,
    viewModel: AddRecipesToCookbookViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(cookbookId) {
        viewModel.loadCookbook(cookbookId)
        viewModel.loadUserRecipes()
    }

    LaunchedEffect(state.navigationEvent) {
        state.navigationEvent?.let { event ->
            when (event) {
                is AddRecipesToCookbookNavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
            viewModel.clearNavigationEvent()
        }
    }

    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show error message
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Recipes",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (state.selectedRecipes.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.addSelectedRecipes() }
                        ) {
                            if (state.isAdding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add (${state.selectedRecipes.size})")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                placeholder = "Search your recipes...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }

                state.availableRecipes.isEmpty() && state.searchQuery.isEmpty() -> {
                    EmptyRecipesState(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                state.filteredRecipes.isEmpty() && state.searchQuery.isNotEmpty() -> {
                    NoSearchResultsState(
                        query = state.searchQuery,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.filteredRecipes,
                            key = { it.recipeId }
                        ) { recipe ->
                            RecipeSelectionCard(
                                recipe = recipe,
                                isSelected = state.selectedRecipes.contains(recipe.recipeId),
                                isAlreadyInCookbook = state.existingRecipeIds.contains(recipe.recipeId),
                                onSelectionToggle = { recipeId ->
                                    viewModel.toggleRecipeSelection(recipeId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRecipesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Recipes Found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create some recipes first to add them to your cookbook",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No recipes found for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try a different search term",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}