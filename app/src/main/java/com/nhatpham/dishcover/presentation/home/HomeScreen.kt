package com.nhatpham.dishcover.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToAllRecipes: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCreateRecipe: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DISHCOVER",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        showAddMenu = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

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
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomBottomNavItem(
                        selected = true,
                        icon = Icons.Default.Home,
                        label = "Home",
                        onClick = {}
                    )
                    CustomBottomNavItem(
                        selected = false,
                        icon = Icons.Default.ShoppingCart,
                        label = "Shopping",
                        onClick = {}
                    )
                    CustomBottomNavItem(
                        selected = false,
                        icon = Icons.Default.DateRange,
                        label = "Planner",
                        onClick = {}
                    )
                    CustomBottomNavItem(
                        selected = false,
                        icon = Icons.Default.Book,
                        label = "Cookbooks",
                        onClick = {}
                    )
                }
            }
        }
    ) { paddingValues ->
        HomeContent(
            state = state,
            paddingValues = paddingValues,
            onNavigateToRecipeDetail = onNavigateToRecipeDetail,
            onNavigateToCategory = onNavigateToCategory,
            onNavigateToAllRecipes = onNavigateToAllRecipes
        )
    }
}

@Composable
fun CustomBottomNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )

        if (selected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun HomeContent(
    state: HomeViewState,
    paddingValues: PaddingValues,
    onNavigateToRecipeDetail: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToAllRecipes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
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
        if (state.recentRecipes.isNotEmpty()) {
            FeaturedRecipe(
                recipe = state.recentRecipes.first(),
                onRecipeClick = onNavigateToRecipeDetail
            )
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
        HorizontalRecipeList(
            recipes = state.allRecipes,
            onRecipeClick = onNavigateToRecipeDetail
        )

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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryColor
            )
        }

        TextButton(
            onClick = onSeeAllClick,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "See all")
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "See all $title",
                modifier = Modifier.size(16.dp)
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
            .height(110.dp),
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
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(4.dp, CircleShape)
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = recipe.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = TextPrimaryColor
        )
    }
}

@Composable
fun FeaturedRecipe(
    recipe: RecipeListItemUI,
    onRecipeClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp)
            .clickable { onRecipeClick(recipe.id) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Recipe image
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
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    recipe.category?.let { getCategoryColor(it) } ?: PrimaryColor,
                                    Color(0xFF121212)
                                )
                            )
                        )
                )
            }

            // Gradient overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xCC000000)
                            )
                        )
                    )
            )

            // Recipe info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                recipe.category?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(getCategoryColor(it))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Additional recipe info chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecipeInfoChip(
                        icon = Icons.Default.Timer,
                        text = "30 min",
                        tint = AccentYellow
                    )
                    RecipeInfoChip(
                        icon = Icons.Default.Restaurant,
                        text = "Easy",
                        tint = EasyDifficultyColor
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeInfoChip(
    icon: ImageVector,
    text: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, tint)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}