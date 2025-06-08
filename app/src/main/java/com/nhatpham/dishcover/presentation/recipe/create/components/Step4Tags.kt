package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateEvent
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Popular tags
        val popularTags = listOf("Quick", "Healthy", "Vegetarian", "Dessert", "Spicy", "Comfort Food")

        Text(
            text = "Popular Tags",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularTags.size) { index ->
                val tag = popularTags[index]
                FilterChip(
                    onClick = { onToggleTag(tag) },
                    label = { Text(tag) },
                    selected = selectedTags.contains(tag)
                )
            }
        }

        // Custom tag input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customTag,
                onValueChange = { customTag = it },
                placeholder = { Text("Add custom tag") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    if (customTag.isNotBlank()) {
                        onAddCustomTag(customTag)
                        customTag = ""
                    }
                },
                enabled = customTag.isNotBlank()
            ) {
                Text("Add")
            }
        }
    }
}