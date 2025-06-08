package com.nhatpham.dishcover.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.presentation.auth.AuthViewModel
import com.nhatpham.dishcover.presentation.component.LoadingIndicator
import com.nhatpham.dishcover.presentation.component.RecipeGridItem
import com.nhatpham.dishcover.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToAllRecipes: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCreateRecipe: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val state by homeViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            authViewModel.signOut().collect {
                                onSignOut()
                            }
                        }
                    }
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DropdownMenu(
            expanded = showAddMenu,
            onDismissRequest = { showAddMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "New recipe",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        tint = PrimaryColor
                    )
                },
                onClick = {
                    showAddMenu = false
                    onNavigateToCreateRecipe()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Import from website",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = AccentBlue
                    )
                },
                onClick = { showAddMenu = false }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Scan from photo",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = AccentPurple
                    )
                },
                onClick = { showAddMenu = false }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Scan from PDF",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = PrimaryColor
                    )
                },
                onClick = { showAddMenu = false }
            )
        }

        // Content
        HomeContent(
            state = state,
            onNavigateToRecipeDetail = onNavigateToRecipeDetail,
            onNavigateToCategory = onNavigateToCategory,
            onNavigateToAllRecipes = onNavigateToAllRecipes,
            onRefresh = { homeViewModel.refreshData() },
            onClearError = { errorType -> homeViewModel.clearError(errorType) }
        )
    }
}

@Composable
fun HomeContent(
    state: HomeViewState,
    onNavigateToRecipeDetail: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAllRecipes: () -> Unit,
    onRefresh: () -> Unit,
    onClearError: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Show general loading state if user data is loading
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            // Favorites Section
            RecipeSection(
                title = "Favorites",
                icon = Icons.Default.Favorite,
                iconTint = PrimaryColor,
                recipes = state.favorites,
                isLoading = state.isFavoritesLoading,
                error = state.favoriteError,
                onRecipeClick = onNavigateToRecipeDetail,
                onSeeAllClick = { onNavigateToCategory("favorites") },
                onRetry = onRefresh,
                onClearError = { onClearError("favorites") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Recipes Section
            RecipeSection(
                title = "Recent",
                icon = Icons.Default.History,
                iconTint = AccentTeal,
                recipes = state.recentRecipes,
                isLoading = state.isRecentLoading,
                error = state.recentError,
                onRecipeClick = onNavigateToRecipeDetail,
                onSeeAllClick = { onNavigateToCategory("recent") },
                onRetry = onRefresh,
                onClearError = { onClearError("recent") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Categories Section
            SectionHeader(
                title = "Categories",
                icon = Icons.Default.Category,
                iconTint = AccentPurple,
                onSeeAllClick = { onNavigateToCategory("all_categories") }
            )

            if (state.isCategoriesLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.categoriesError != null) {
                ErrorSection(
                    error = state.categoriesError!!,
                    onRetry = onRefresh,
                    onClearError = { onClearError("categories") }
                )
            } else {
                CategoryList(
                    categories = state.availableCategories,
                    onCategoryClick = onNavigateToCategory
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // All Recipes Section
            RecipeSection(
                title = "All Recipes",
                icon = Icons.Default.RestaurantMenu,
                iconTint = AccentOrange,
                recipes = state.allRecipes,
                isLoading = state.isAllRecipesLoading,
                error = state.allRecipesError,
                onRecipeClick = onNavigateToRecipeDetail,
                onSeeAllClick = onNavigateToAllRecipes,
                onRetry = onRefresh,
                onClearError = { onClearError("all") }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Show general error if present
        state.error?.let { error ->
            ErrorSection(
                error = error,
                onRetry = onRefresh,
                onClearError = { onClearError("general") }
            )
        }
    }
}

@Composable
fun RecipeSection(
    title: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    recipes: List<RecipeListItem>,
    isLoading: Boolean,
    error: String?,
    onRecipeClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    SectionHeader(
        title = title,
        icon = icon,
        iconTint = iconTint,
        onSeeAllClick = onSeeAllClick
    )

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            ErrorSection(
                error = error,
                onRetry = onRetry,
                onClearError = onClearError
            )
        }
        recipes.isEmpty() -> {
            EmptyRecipeSection(title = title)
        }
        else -> {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recipes) { recipe ->
                    RecipeGridItem(
                        recipe = recipe,
                        onRecipeClick = onRecipeClick,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryColor
            )
        }

        TextButton(
            onClick = onSeeAllClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "See all $title",
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun CategoryList(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    if (categories.isEmpty()) {
        EmptyRecipeSection(title = "Categories")
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = getCategoryColor(category).copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            getCategoryColor(category)
        )
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = getCategoryColor(category),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyRecipeSection(title: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No $title yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start creating recipes to see them here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorSection(
    error: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Error loading data",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                IconButton(onClick = onClearError) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Retry")
            }
        }
    }
}