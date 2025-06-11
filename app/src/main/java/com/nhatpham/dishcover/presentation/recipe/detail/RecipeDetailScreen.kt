// RecipeDetailScreen.kt
package com.nhatpham.dishcover.presentation.recipe.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nhatpham.dishcover.domain.model.recipe.RecipeDifficulty
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.domain.model.recipe.RecipeIngredient
import com.nhatpham.dishcover.presentation.components.RecipeShareDialog
import com.nhatpham.dishcover.presentation.recipe.create.components.DifficultyIndicator
import com.nhatpham.dishcover.presentation.recipe.create.components.DifficultySize
import com.nhatpham.dishcover.presentation.recipe.create.components.NutritionalInfoPanel
import com.nhatpham.dishcover.presentation.recipe.create.components.ReviewDialog
import com.nhatpham.dishcover.presentation.recipe.create.components.ReviewsSection
import com.nhatpham.dishcover.presentation.recipe.create.components.ServingSizeAdjuster

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showShareDialog by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.onErrorDismissed()
        }
    }

    if (showShareDialog) {
        RecipeShareDialog(
            recipe = state.recipe!!,
            onDismiss = { showShareDialog = false },
            onShare = {
                viewModel.onEvent(RecipeDetailEvent.ShareRecipe(context))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.recipe != null) {
                        if (state.recipe?.isPublic == true || state.isCurrentUserOwner) {
                            IconButton(
                                onClick = { showShareDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Recipe"
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.onEvent(RecipeDetailEvent.ToggleFavorite) },
                            enabled = !state.favoriteLoading
                        ) {
                            if (state.favoriteLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = if (state.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        IconButton(onClick = { onNavigateToEdit(recipeId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            state.isLoading && state.recipe == null -> {
                RecipeDetailLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            state.recipe != null -> {
                RecipeDetailContent(
                    state = state,
                    onServingsChanged = viewModel::onServingsChanged,
                    onRatingSubmitted = viewModel::onRatingSubmitted,
                    onReviewSubmitted = viewModel::onReviewSubmitted,
                    onReviewHelpful = viewModel::onReviewHelpful,
                    onLoadMoreReviews = viewModel::onLoadMoreReviews,
                    onCalculateNutrition = viewModel::onCalculateNutrition,
                    onShowReviewDialog = viewModel::onShowReviewDialog,
                    onNavigateToProfile = onNavigateToProfile,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                RecipeDetailErrorState(
                    onRetry = { viewModel.loadRecipe(recipeId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    // Review Dialog
    if (state.showReviewDialog) {
        ReviewDialog(
            onDismiss = viewModel::onHideReviewDialog,
            onSubmit = { rating, comment, images ->
                viewModel.onReviewSubmitted(rating, comment, images)
            },
            isSubmitting = state.reviewsLoading,
        )
    }
}

@Composable
private fun RecipeInfoSection(
    recipe: Recipe,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recipe Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Author and Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Created by",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Recipe Author", // You'd get this from user data
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickableWithRipple {
                        onNavigateToProfile(recipe.userId)
                    }
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Published",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(recipe.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Views
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${recipe.viewCount} views",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Likes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${recipe.likeCount} likes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Prep/Cook breakdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${recipe.prepTime}+${recipe.cookTime} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to format Timestamp
private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}

// Helper extension for clickable with ripple
private fun Modifier.clickableWithRipple(onClick: () -> Unit) = this.then(
    Modifier.clickable { onClick() }
)

@Composable
private fun RecipeDetailContent(
    state: RecipeDetailState,
    onServingsChanged: (Int) -> Unit,
    onRatingSubmitted: (Int) -> Unit,
    onReviewSubmitted: (Int, String, List<String>) -> Unit,
    onReviewHelpful: (String, Boolean) -> Unit,
    onLoadMoreReviews: () -> Unit,
    onCalculateNutrition: () -> Unit,
    onShowReviewDialog: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.scaledRecipe ?: return
    val originalRecipe = state.recipe ?: return

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp), // Remove spacing, handle within cards
    ) {
        // Hero Image and Basic Info
        item {
            RecipeHeroSection(
                recipe = recipe,
                originalRecipe = originalRecipe
            )
        }

        // Content Cards
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Recipe Info Card
                RecipeCard {
                    RecipeInfoSection(
                        recipe = recipe,
                        onNavigateToProfile = onNavigateToProfile
                    )
                }

                // Ingredients Card
                RecipeCard {
                    RecipeIngredientsSection(
                        ingredients = recipe.ingredients,
                        isScaled = state.currentServings != state.originalServings,
                        currentServings = state.currentServings,
                        originalServings = state.originalServings,
                        onServingsChanged = onServingsChanged
                    )
                }

                // Instructions Card
                RecipeCard {
                    RecipeInstructionsSection(
                        instructions = recipe.instructions,
                        difficulty = RecipeDifficulty.fromString(recipe.difficultyLevel)
                    )
                }

//                // Nutritional Information Card
//                RecipeCard {
//                    NutritionalInfoPanel(
//                        nutritionalInfo = state.nutritionalInfo,
//                        isLoading = state.nutritionLoading,
//                        onRefreshNutrition = onCalculateNutrition,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }

                // Reviews Card
                state.ratingAggregate?.let { ratingAggregate ->
                    RecipeCard {
                        ReviewsSection(
                            ratingAggregate = ratingAggregate,
                            reviews = state.reviews,
                            currentUserId = "",
                            onAddReview = onShowReviewDialog,
                            onReviewHelpful = onReviewHelpful,
                            onLoadMoreReviews = onLoadMoreReviews,
                            hasMoreReviews = state.hasMoreReviews,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
private fun RecipeHeroSection(
    recipe: Recipe,
    originalRecipe: Recipe,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Cover Image with gradient overlay
        if (recipe.coverImage?.isNotBlank() == true) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.coverImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )

                // Gradient overlay for better text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }
        }

        // Title and basic info overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (recipe.coverImage?.isNotBlank() == true) Color.White else MaterialTheme.colorScheme.onBackground
            )

            if (recipe.description?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (recipe.coverImage?.isNotBlank() == true) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recipe Metadata Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    RecipeMetadataChip(
                        icon = Icons.Default.Schedule,
                        label = "Total",
                        value = "${recipe.prepTime + recipe.cookTime} min",
                        isOverlay = recipe.coverImage?.isNotBlank() == true
                    )
                }

                item {
                    RecipeMetadataChip(
                        icon = Icons.Default.Restaurant,
                        label = "Serves",
                        value = "${recipe.servings}",
                        isOverlay = recipe.coverImage?.isNotBlank() == true
                    )
                }

                item {
                    DifficultyIndicator(
                        difficulty = RecipeDifficulty.fromString(recipe.difficultyLevel),
                        size = DifficultySize.COMPACT
                    )
                }

                if (recipe.isFeatured) {
                    item {
                        RecipeMetadataChip(
                            icon = Icons.Default.Star,
                            label = "Featured",
                            value = "",
                            isOverlay = recipe.coverImage?.isNotBlank() == true,
                            isFeatured = true
                        )
                    }
                }
            }

            // Tags
            if (recipe.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recipe.tags.size) { index ->
                        FilterChip(
                            onClick = { /* Navigate to tag search */ },
                            label = {
                                Text(
                                    recipe.tags[index],
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = false,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (recipe.coverImage?.isNotBlank() == true)
                                    Color.White.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (recipe.coverImage?.isNotBlank() == true)
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeMetadataChip(
    icon: ImageVector,
    label: String,
    value: String,
    isOverlay: Boolean = false,
    isFeatured: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = when {
            isFeatured -> Color(0xFFFFD700).copy(alpha = if (isOverlay) 0.9f else 1f)
            isOverlay -> Color.White.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = when {
                    isFeatured -> Color.Black
                    isOverlay -> Color.White
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (value.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isFeatured -> Color.Black.copy(alpha = 0.8f)
                            isOverlay -> Color.White.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isFeatured -> Color.Black
                            isOverlay -> Color.White
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            } else if (isFeatured) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun RecipeIngredientsSection(
    ingredients: List<RecipeIngredient>,
    isScaled: Boolean,
    currentServings: Int,
    originalServings: Int,
    onServingsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section Header with simple serving adjuster
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Simple serving size controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Serving size adjuster
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Serves",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { if (currentServings > 1) onServingsChanged(currentServings - 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease servings",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = currentServings.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { onServingsChanged(currentServings + 1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase servings",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isScaled) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Scaled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ingredients List
        ingredients.forEachIndexed { index, ingredient ->
            IngredientItem(
                ingredient = ingredient,
                modifier = Modifier.fillMaxWidth()
            )

            if (index < ingredients.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun IngredientItem(
    ingredient: RecipeIngredient,
    modifier: Modifier = Modifier
) {
    var isChecked by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${ingredient.quantity} ${ingredient.unit} ${ingredient.ingredient.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (ingredient.notes?.isNotBlank() == true) {
                Text(
                    text = ingredient.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecipeInstructionsSection(
    instructions: String,
    difficulty: RecipeDifficulty,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            DifficultyIndicator(
                difficulty = difficulty,
                size = DifficultySize.COMPACT
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions Steps
        val steps = instructions.split("\n").filter { it.isNotBlank() }

        steps.forEachIndexed { index, step ->
            InstructionStep(
                stepNumber = index + 1,
                instruction = step,
                modifier = Modifier.fillMaxWidth()
            )

            if (index < steps.size - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InstructionStep(
    stepNumber: Int,
    instruction: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RecipeDetailLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading recipe...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipeDetailErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Failed to load recipe",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please check your connection and try again",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}