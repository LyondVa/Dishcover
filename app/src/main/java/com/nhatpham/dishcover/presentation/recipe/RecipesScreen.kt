package com.nhatpham.dishcover.presentation.recipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onNavigateToRecipeDetail: (String) -> Unit = {},
    onNavigateToCreateRecipe: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Recipes", "Cookbooks")

    // Sample recipe data for UI display
    val sampleRecipes = listOf(
        RecipeItemUI(
            id = "1",
            title = "Chicken Ramen",
            timeText = "30 mins",
            difficulty = "Gordon Ramsay",
            imageRes = 0 // Will use placeholder
        ),
        RecipeItemUI(
            id = "2",
            title = "Chicken Ramen",
            timeText = "30 mins",
            difficulty = "Gordon Ramsay",
            imageRes = 0 // Will use placeholder
        ),
        RecipeItemUI(
            id = "3",
            title = "Beef Stir Fry",
            timeText = "25 mins",
            difficulty = "Easy",
            imageRes = 0
        ),
        RecipeItemUI(
            id = "4",
            title = "Pasta Carbonara",
            timeText = "20 mins",
            difficulty = "Medium",
            imageRes = 0
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Surface(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                    border = if (selectedTab == index) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = if (selectedTab == index) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (selectedTab == index) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTab) {
            0 -> { // Recipes tab
                RecipeGridContent(
                    recipes = sampleRecipes,
                    onRecipeClick = onNavigateToRecipeDetail
                )
            }
            1 -> { // Cookbooks tab
                CookbooksContent()
            }
        }
    }
}

@Composable
fun RecipeGridContent(
    recipes: List<RecipeItemUI>,
    onRecipeClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Recipe grid
        val chunkedRecipes = recipes.chunked(2)
        chunkedRecipes.forEach { recipePair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                recipePair.forEach { recipe ->
                    RecipeGridItem(
                        recipe = recipe,
                        onRecipeClick = onRecipeClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number of items
                if (recipePair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RecipeGridItem(
    recipe: RecipeItemUI,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onRecipeClick(recipe.id) }
            .aspectRatio(0.8f),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
//                    .background(
//                        brush = Brush.verticalGradient(
//                            colors = listOf(
//                                Color(0xFFFF8A65),
//                                Color(0xFFFF7043)
//                            )
//                        )
//                    )
            ) {
                // Placeholder for recipe image
                Text(
                    text = recipe.title.take(1),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Recipe info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recipe.timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = recipe.difficulty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CookbooksContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cookbooks feature coming soon!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class RecipeItemUI(
    val id: String,
    val title: String,
    val timeText: String,
    val difficulty: String,
    val imageRes: Int
)


//// Content with FAB
//Box(modifier = Modifier.fillMaxSize()) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .padding(16.dp)
//    ) {
//        // Recipe categories
//        ScrollableTabRow(selectedTabIndex = selectedCategory) {
//            categories.forEachIndexed { index, title ->
//                Tab(
//                    selected = selectedCategory == index,
//                    onClick = { selectedCategory = index },
//                    text = { Text(title) }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Empty state
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surfaceVariant
//            )
//        ) {
//            Column(
//                modifier = Modifier.padding(24.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Icon(
//                    imageVector = Icons.Default.MenuBook,
//                    contentDescription = null,
//                    modifier = Modifier.size(64.dp),
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(
//                    text = "No Recipes Yet",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Start creating your recipe collection! Tap the + button to add your first recipe.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(
//                    onClick = onNavigateToCreateRecipe,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Icon(Icons.Default.Add, contentDescription = null)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Create Your First Recipe")
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Quick actions
//        Text(
//            text = "Quick Actions",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            OutlinedCard(
//                onClick = { /* Import from URL */ },
//                modifier = Modifier.weight(1f)
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Link,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Import from URL",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//
//            OutlinedCard(
//                onClick = { /* Scan recipe */ },
//                modifier = Modifier.weight(1f)
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.CameraAlt,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Scan Recipe",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//        }
//    }
//
//    FloatingActionButton(
//        onClick = onNavigateToCreateRecipe,
//        modifier = Modifier
//            .align(Alignment.BottomEnd)
//            .padding(16.dp),
//        containerColor = MaterialTheme.colorScheme.primary
//    ) {
//        Icon(Icons.Default.Add, contentDescription = "Create Recipe")
//    }
//}
//}
//}