// CreatePostRecipeSection.kt
package com.nhatpham.dishcover.presentation.feed.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem

@Composable
fun CreatePostRecipeSection(
    selectedRecipes: List<RecipeListItem>,
    onAddRecipe: () -> Unit,
    onRemoveRecipe: (RecipeListItem) -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header with add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Linked Recipes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            if (selectedRecipes.size < 5) { // Limit to 5 recipes
                TextButton(
                    onClick = onAddRecipe,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Recipe")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected recipes or empty state
        if (selectedRecipes.isEmpty()) {
            EmptyRecipeState(onAddRecipe = onAddRecipe)
        } else {
            SelectedRecipesList(
                recipes = selectedRecipes,
                onRemoveRecipe = onRemoveRecipe,
                onRecipeClick = onRecipeClick
            )
        }
    }
}

@Composable
private fun EmptyRecipeState(
    onAddRecipe: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddRecipe() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Link a recipe to your post",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap to browse your recipes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SelectedRecipesList(
    recipes: List<RecipeListItem>,
    onRemoveRecipe: (RecipeListItem) -> Unit,
    onRecipeClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(recipes) { recipe ->
            RecipeWidget(
                recipe = recipe,
                onRecipeClick = onRecipeClick,
                onRemove = { onRemoveRecipe(recipe) },
                isCompact = true
            )
        }
    }
}

// Recipe selection button component
@Composable
fun RecipeSelectionButton(
    selectedCount: Int,
    onAddRecipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onAddRecipe,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (selectedCount > 0) {
                "Recipes ($selectedCount)"
            } else {
                "Link Recipe"
            }
        )
    }
}

// Compact recipe display for post creation toolbar
@Composable
fun CompactRecipeIndicator(
    recipeCount: Int,
    onManageRecipes: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (recipeCount > 0) {
        Surface(
            modifier = modifier
                .clickable { onManageRecipes() }
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$recipeCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}