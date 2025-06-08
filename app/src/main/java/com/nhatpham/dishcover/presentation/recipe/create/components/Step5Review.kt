package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel
import com.nhatpham.dishcover.ui.theme.PrimaryColor

@Composable
fun Step5Review(
    state: RecipeCreateState,
    viewModel: RecipeCreateViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recipe image
        if (state.coverImageUri != null) {
            AsyncImage(
                model = state.coverImageUri,
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // Recipe title
        Text(
            text = "Recipe Name",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = state.title.ifBlank { "Untitled Recipe" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Recipe details
        Text(
            text = "Details",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = buildString {
                append("Prep Time: ${state.prepTime.ifBlank { "Not set" }}")
                if (state.cookTime.isNotBlank()) append(" • Cook Time: ${state.cookTime}")
                if (state.servings.isNotBlank()) append(" • Servings: ${state.servings}")
                if (state.difficultyLevel.isNotBlank()) append(" • Difficulty: ${state.difficultyLevel}")
            },
            style = MaterialTheme.typography.bodyLarge
        )

        // Description
        if (state.description.isNotBlank()) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Ingredients
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        if (state.ingredients.isEmpty()) {
            Text(
                text = "No ingredients added",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            state.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${ingredient.quantity} ${ingredient.unit} ${ingredient.ingredient.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (ingredient.notes != null) {
                        Text(
                            text = ingredient.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Instructions
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        if (state.instructionSteps.isEmpty()) {
            Text(
                text = "No instructions added",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            state.instructionSteps.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Tags
        if (state.selectedTags.isNotEmpty()) {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.selectedTags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, PrimaryColor)
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = PrimaryColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Privacy
        Text(
            text = "Privacy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = if (state.isPublic) "Public Recipe" else "Private Recipe",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}