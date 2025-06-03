package com.nhatpham.dishcover.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.auth.AuthViewModel
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
                onClick = onNavigateToCreateRecipe
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
            onNavigateToAllRecipes = onNavigateToAllRecipes
        )
    }
}

@Composable
fun HomeContent(
    state: HomeViewState,
    onNavigateToRecipeDetail: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAllRecipes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(
            title = "Favorites",
            icon = Icons.Default.Favorite,
            iconTint = PrimaryColor,
            onSeeAllClick = { onNavigateToCategory("favorites") }
        )
        HorizontalRecipeList(
            recipes = state.favorites,
            onRecipeClick = onNavigateToRecipeDetail
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "Recent",
            icon = Icons.Default.History,
            iconTint = AccentTeal,
            onSeeAllClick = { onNavigateToCategory("recent") }
        )

        // Show recent recipes as cards instead of featured
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.recentRecipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onRecipeClick = onNavigateToRecipeDetail
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "Categories",
            icon = Icons.Default.Category,
            iconTint = AccentPurple,
            onSeeAllClick = { onNavigateToCategory("all_categories") }
        )
        HorizontalRecipeList(
            recipes = state.categories,
            onRecipeClick = { recipeId ->
                val recipe = state.categories.find { it.id == recipeId }
                recipe?.category?.let { onNavigateToCategory(it) }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(
            title = "All recipes",
            icon = Icons.Default.RestaurantMenu,
            iconTint = AccentOrange,
            onSeeAllClick = onNavigateToAllRecipes
        )

        // Show all recipes as cards in a grid
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.allRecipes) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onRecipeClick = onNavigateToRecipeDetail
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color,
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
fun HorizontalRecipeList(
    recipes: List<RecipeListItemUI>,
    onRecipeClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        recipes.forEach { recipe ->
            RecipeCircle(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.id) }
            )
        }
    }
}

@Composable
fun RecipeCircle(
    recipe: RecipeListItemUI,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(getRecipeColor(recipe.name))
        ) {
            if (recipe.imageRes != 0) {
                Image(
                    painter = painterResource(id = recipe.imageRes),
                    contentDescription = recipe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Show first letter of recipe as fallback
                Text(
                    text = recipe.name.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = recipe.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = TextPrimaryColor
        )
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeListItemUI,
    onRecipeClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable { onRecipeClick(recipe.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                if (recipe.imageRes != 0) {
                    Image(
                        painter = painterResource(id = recipe.imageRes),
                        contentDescription = recipe.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Colorful background if no image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getRecipeColor(recipe.name))
                    ) {
                        Text(
                            text = recipe.name.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // Recipe info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimaryColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Add time and difficulty info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "30 mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Easy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}