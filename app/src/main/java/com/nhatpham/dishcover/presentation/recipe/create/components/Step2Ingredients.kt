package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.domain.model.recipe.RecipeIngredient
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateEvent
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel

@Composable
fun Step2Ingredients(
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
        RecipeIngredientsSection(
            ingredients = state.ingredients,
            onAddIngredient = { name, quantity, unit, notes ->
                viewModel.onEvent(RecipeCreateEvent.AddIngredient(name, quantity, unit, notes))
            },
            onRemoveIngredient = { index ->
                viewModel.onEvent(RecipeCreateEvent.RemoveIngredient(index))
            }
        )
        RecipeInstructionsSection(
            instructions = state.instructionSteps,
            onInstructionChanged = { index, instruction ->
                viewModel.onEvent(RecipeCreateEvent.InstructionStepChanged(index, instruction))
            },
            onAddInstruction = {
                viewModel.onEvent(RecipeCreateEvent.AddInstructionStep)
            },
            onRemoveInstruction = { index ->
                viewModel.onEvent(RecipeCreateEvent.RemoveInstructionStep(index))
            }
        )
        state.instructionsError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RecipeIngredientsSection(
    ingredients: List<RecipeIngredient>,
    onAddIngredient: (String, String, String, String?) -> Unit,
    onRemoveIngredient: (Int) -> Unit
) {
    var ingredientName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = { /* Form is always visible */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        // Add ingredient form
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    placeholder = { Text("Enter an ingredient") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    placeholder = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                placeholder = { Text("Unit (e.g., cups, tsp)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = {
                    if (ingredientName.isNotBlank() && quantity.isNotBlank()) {
                        onAddIngredient(ingredientName, quantity, unit, notes.ifBlank { null })
                        ingredientName = ""
                        quantity = ""
                        unit = ""
                        notes = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = ingredientName.isNotBlank() && quantity.isNotBlank()
            ) {
                Text("Add Ingredient")
            }
        }

        // Ingredients list
        if (ingredients.isEmpty()) {
            Text(
                text = "No ingredients added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ingredients.forEachIndexed { index, recipeIngredient ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${recipeIngredient.quantity} ${recipeIngredient.unit} ${recipeIngredient.ingredient.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (recipeIngredient.notes != null) {
                                Text(
                                    text = recipeIngredient.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { onRemoveIngredient(index) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeInstructionsSection(
    instructions: List<String>,
    onInstructionChanged: (Int, String) -> Unit,
    onAddInstruction: () -> Unit,
    onRemoveInstruction: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onAddInstruction) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Step")
            }
        }

        if (instructions.isEmpty()) {
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Click 'Add Step' to start adding instructions") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        } else {
            instructions.forEachIndexed { index, instruction ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp, top = 16.dp)
                    )
                    OutlinedTextField(
                        value = instruction,
                        onValueChange = { onInstructionChanged(index, it) },
                        placeholder = { Text("Enter instruction step ${index + 1}") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        minLines = 2,
                        maxLines = 4,
                        trailingIcon = {
                            if (instructions.size > 1) {
                                IconButton(
                                    onClick = { onRemoveInstruction(index) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove step")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}