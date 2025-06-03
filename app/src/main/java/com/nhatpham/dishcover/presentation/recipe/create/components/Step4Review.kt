package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
fun Step4Review(
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

        // Recipe details
        Text(
            text = "Recipe Name",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = state.title.ifBlank { "Chicken Ramen" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Estimated Time",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = state.prepTime.ifBlank { "45 mins" },
            style = MaterialTheme.typography.bodyLarge
        )

        // Ingredients
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        if (state.ingredients.isNotEmpty()) {
            state.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ingredient.ingredient.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = ingredient.quantity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            Text(
                text = "Chicken broth • 6 cups\nSoy sauce • 1 tbsp\nSha sauce (Optional) • 1 tbsp\nSmall onion, chopped • 1 jar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        // How-to
        Text(
            text = "How-to",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        val howToSteps = listOf(
            "1. Preparation (10 Minutes)",
            "In a large pot, heat the sesame oil over medium heat.",
            "Add the minced garlic, grated ginger, and chopped green onions. Sauté for 2-3 minutes until fragrant.",
            "Pour in the chicken broth and bring to a simmer."
        )

        howToSteps.forEach { step ->
            Text(
                text = step,
                style = MaterialTheme.typography.bodyMedium,
                color = if (step.startsWith("1.")) Color.Black else Color.Gray
            )
        }

        // See More
        Text(
            text = "See More →",
            color = PrimaryColor,
            modifier = Modifier.clickable { /* Show more steps */ }
        )

        // Categories
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        // Category chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Asian", "Japanese", "Noodle", "Oriental").forEach { category ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, PrimaryColor)
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = PrimaryColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Dinner", "Comfort", "Beginner").forEach { category ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, PrimaryColor)
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = PrimaryColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}