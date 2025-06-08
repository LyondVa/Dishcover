package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.domain.model.recipe.RecipeDifficulty
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateEvent
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel

@Composable
fun Step3Difficulty(
    state: RecipeCreateState,
    viewModel: RecipeCreateViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Difficulty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose the skill level required for your recipe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        RecipeDifficultySection(
            selectedDifficulty = RecipeDifficulty.fromString(state.difficultyLevel),
            onDifficultyChanged = { difficulty ->
                viewModel.onEvent(RecipeCreateEvent.DifficultyLevelChanged(difficulty.displayName))
            }
        )
    }
}

@Composable
private fun RecipeDifficultySection(
    selectedDifficulty: RecipeDifficulty,
    onDifficultyChanged: (RecipeDifficulty) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Recipe Difficulty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            DifficultySelector(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = onDifficultyChanged,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}