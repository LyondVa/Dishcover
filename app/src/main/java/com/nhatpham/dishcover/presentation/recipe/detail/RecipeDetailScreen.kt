// RecipeDetailScreen.kt
package com.nhatpham.dishcover.presentation.recipe.detail

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
    var showShareDialog by remember { mutableStateOf(false)}
    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or error dialog
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

                        IconButton(onClick = { /* Add to favorites */ }) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                        }

                        // Show edit button if user owns the recipe
                        IconButton(onClick = { onNavigateToEdit(recipeId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
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
            }
        )
    }
}

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
    modifier: Modifier = Modifier
) {
    val recipe = state.scaledRecipe ?: return
    val originalRecipe = state.recipe ?: return

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Hero Image and Basic Info
        item {
            RecipeHeroSection(
                recipe = recipe,
                originalRecipe = originalRecipe
            )
        }

        // Serving Size Adjuster
        item {
            ServingSizeAdjuster(
                currentServings = state.currentServings,
                originalServings = state.originalServings,
                onServingsChanged = onServingsChanged,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Ingredients List
        item {
            RecipeIngredientsSection(
                ingredients = recipe.ingredients,
                isScaled = state.currentServings != state.originalServings
            )
        }

        // Instructions
        item {
            RecipeInstructionsSection(
                instructions = recipe.instructions,
                difficulty = RecipeDifficulty.fromString(recipe.difficultyLevel)
            )
        }

        // Nutritional Information
        item {
            NutritionalInfoPanel(
                nutritionalInfo = state.nutritionalInfo,
                isLoading = state.nutritionLoading,
                onRefreshNutrition = onCalculateNutrition,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Ratings and Reviews Section
        item {
            state.ratingAggregate?.let { ratingAggregate ->
                ReviewsSection(
                    ratingAggregate = ratingAggregate,
                    reviews = state.reviews,
                    currentUserId = "", // Get from user state
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

@Composable
private fun RecipeHeroSection(
    recipe: Recipe,
    originalRecipe: Recipe,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Cover Image
        if (recipe.coverImage?.isNotBlank() == true) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.coverImage)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Title and Description
        Text(
            text = recipe.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (recipe.description?.isNotBlank() == true) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recipe Metadata
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prep Time
            RecipeMetadataChip(
                icon = Icons.Default.Schedule,
                label = "Prep",
                value = "${recipe.prepTime} min"
            )

            // Cook Time
            RecipeMetadataChip(
                icon = Icons.Default.Restaurant,
                label = "Cook",
                value = "${recipe.cookTime} min"
            )

            // Difficulty
            DifficultyIndicator(
                difficulty = RecipeDifficulty.fromString(recipe.difficultyLevel),
                size = DifficultySize.COMPACT
            )
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
                        label = { Text(recipe.tags[index]) },
                        selected = false
                    )
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RecipeIngredientsSection(
    ingredients: List<RecipeIngredient>,
    isScaled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (isScaled) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Scaled",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ingredients.forEach { ingredient ->
            IngredientItem(
                ingredient = ingredient,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
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
            onCheckedChange = { isChecked = it }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${ingredient.quantity} ${ingredient.unit} ${ingredient.ingredient.name}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
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

@Composable
private fun RecipeInstructionsSection(
    instructions: String,
    difficulty: RecipeDifficulty,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        DifficultyIndicator(
            difficulty = difficulty,
            showDetails = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Split instructions into steps if they contain numbered steps
        val steps = instructions.split("\n").filter { it.isNotBlank() }

        steps.forEachIndexed { index, step ->
            InstructionStep(
                stepNumber = index + 1,
                instruction = step,
                modifier = Modifier.fillMaxWidth()
            )

            if (index < steps.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
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
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
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
        CircularProgressIndicator()
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
        Text(
            text = "Failed to load recipe",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}