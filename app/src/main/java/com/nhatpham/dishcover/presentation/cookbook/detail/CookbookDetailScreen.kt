// CookbookDetailScreen.kt
package com.nhatpham.dishcover.presentation.cookbook.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.cookbook.detail.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookDetailScreen(
    cookbookId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToEdit: () -> Unit = {},
    onNavigateToAddRecipes: () -> Unit = {},
    viewModel: CookbookDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Load cookbook data
    LaunchedEffect(cookbookId) {
        viewModel.onEvent(CookbookDetailEvent.LoadCookbook(cookbookId))
    }

    // Handle navigation events
    LaunchedEffect(state.navigateToRecipe) {
        state.navigateToRecipe?.let { recipeId ->
            onNavigateToRecipe(recipeId)
            viewModel.onEvent(CookbookDetailEvent.ClearNavigation)
        }
    }

    // Handle actions
    LaunchedEffect(state.shareSuccess) {
        state.shareSuccess?.let { message ->
            kotlinx.coroutines.delay(2000)
            viewModel.onEvent(CookbookDetailEvent.ClearShareSuccess)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.cookbook?.title ?: "Cookbook",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.cookbook != null) {
                        // Share button
                        IconButton(
                            onClick = {
                                viewModel.onEvent(CookbookDetailEvent.ShareCookbook(context))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        // More options menu
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (state.isOwner) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Cookbook") },
                                        onClick = {
                                            showMenu = false
                                            onNavigateToEdit()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = null)
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = { Text("Report") },
                                    onClick = {
                                        showMenu = false
                                        // Handle report
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Flag, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
//        floatingActionButton = {
//            if (state.canAddRecipes) {
//                FloatingActionButton(
//                    onClick = onNavigateToAddRecipes,
//                    containerColor = MaterialTheme.colorScheme.primary
//                ) {
//                    Icon(
//                        Icons.Default.Add,
//                        contentDescription = "Add recipes",
//                        tint = Color.White
//                    )
//                }
//            }
//        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingIndicator()
                }
                state.error != null -> {
                    CookbookErrorState(
                        error = state.error!!,
                        onRetry = {
                            viewModel.onEvent(CookbookDetailEvent.LoadCookbook(cookbookId))
                        }
                    )
                }
                state.cookbook != null -> {
                    CookbookDetailContent(
                        cookbook = state.cookbook!!,
                        recipes = state.recipes,
                        isFollowing = state.isFollowing,
                        isLiked = state.isLiked,
                        isOwner = state.isOwner,
                        canAddRecipes = state.canAddRecipes,
                        onFollowClick = {
                            viewModel.onEvent(CookbookDetailEvent.ToggleFollow)
                        },
                        onLikeClick = {
                            viewModel.onEvent(CookbookDetailEvent.ToggleLike)
                        },
                        onRecipeClick = { recipeId ->
                            viewModel.onEvent(CookbookDetailEvent.NavigateToRecipe(recipeId))
                        },
                        onAddRecipes = onNavigateToAddRecipes
                    )
                }
            }

            // Success message overlay
            state.shareSuccess?.let { message ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.inverseSurface
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CookbookDetailContent(
    cookbook: com.nhatpham.dishcover.domain.model.cookbook.Cookbook,
    recipes: List<RecipeListItem>,
    isFollowing: Boolean,
    isLiked: Boolean,
    isOwner: Boolean,
    canAddRecipes: Boolean,
    onFollowClick: () -> Unit,
    onLikeClick: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onAddRecipes: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = if (canAddRecipes) 80.dp else 16.dp)
    ) {
        item {
            // Cookbook Header
            CookbookHeader(
                cookbook = cookbook,
                isFollowing = isFollowing,
                isLiked = isLiked,
                isOwner = isOwner,
                onFollowClick = onFollowClick,
                onLikeClick = onLikeClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Recipes Section Header
            RecipesSectionHeader(
                recipeCount = recipes.size,
                canAddRecipes = canAddRecipes,
                onAddRecipes = onAddRecipes
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (recipes.isEmpty()) {
            item {
                EmptyRecipesState(
                    isOwner = isOwner,
                    canAddRecipes = canAddRecipes,
                    onAddRecipes = onAddRecipes
                )
            }
        } else {
            item {
                // Use a fixed height container for the staggered grid
                Box(
                    modifier = Modifier.height(((recipes.size / 2 + recipes.size % 2) * 200).dp)
                ) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        userScrollEnabled = false // Disable scroll since we're inside LazyColumn
                    ) {
                        items(recipes) { recipe ->
                            CookbookRecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.recipeId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CookbookErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptyRecipesState(
    isOwner: Boolean,
    canAddRecipes: Boolean,
    onAddRecipes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isOwner) "No Recipes Yet" else "No Recipes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isOwner) {
                "Start building your cookbook by adding recipes"
            } else {
                "This cookbook doesn't have any recipes yet"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (canAddRecipes) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddRecipes,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Recipes")
            }
        }
    }
}

@Composable
private fun CookbookRecipeCard(
    recipe: RecipeListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (recipe.coverImage != null) {
                    AsyncImage(
                        model = recipe.coverImage,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                }

                // Difficulty badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = recipe.difficultyLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time and servings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${recipe.prepTime + recipe.cookTime}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (recipe.likeCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${recipe.likeCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}