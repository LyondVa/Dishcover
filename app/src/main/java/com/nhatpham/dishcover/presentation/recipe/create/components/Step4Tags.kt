package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateEvent
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel
import com.nhatpham.dishcover.ui.theme.PrimaryColor

@Composable
fun Step4Tags(
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
        Text(
            text = "Add Tags",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select or create tags to categorize your recipe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        RecipeTagsSection(
            selectedTags = state.selectedTags,
            onToggleTag = { tag ->
                viewModel.onEvent(RecipeCreateEvent.ToggleTag(tag))
            },
            onAddCustomTag = { tag ->
                viewModel.onEvent(RecipeCreateEvent.AddCustomTag(tag))
            }
        )
    }
}

@Composable
private fun RecipeTagsSection(
    selectedTags: List<String>,
    onToggleTag: (String) -> Unit,
    onAddCustomTag: (String) -> Unit
) {
    var customTag by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Popular Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        val popularTags = listOf("Quick", "Healthy", "Vegetarian", "Dessert", "Spicy", "Comfort Food")

        AnimatedContent(
            targetState = popularTags,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { tags ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags.size) { index ->
                    val tag = tags[index]
                    FilterChip(
                        onClick = { onToggleTag(tag) },
                        label = { Text(tag, style = MaterialTheme.typography.labelLarge, fontSize = 14.sp) },
                        selected = selectedTags.contains(tag),
                        enabled = true, // Added required parameter
                        modifier = Modifier.padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryColor.copy(alpha = 0.1f),
                            selectedLabelColor = PrimaryColor,
                            containerColor = Color.Transparent,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = if (selectedTags.contains(tag)) {
                            BorderStroke(1.dp, PrimaryColor) // Dynamic border when selected
                        } else {
                            null // No border when not selected
                        }
                    )
                }
            }
        }

        Text(
            text = "Custom Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customTag,
                onValueChange = { customTag = it },
                placeholder = { Text("Add custom tag") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Button(
                onClick = {
                    if (customTag.isNotBlank()) {
                        onAddCustomTag(customTag)
                        customTag = ""
                    }
                },
                enabled = customTag.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Add", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}