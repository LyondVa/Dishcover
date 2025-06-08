// RecipeCreateScreen.kt - Updated with DifficultySelector integration
package com.nhatpham.dishcover.presentation.recipe.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhatpham.dishcover.domain.model.recipe.RecipeDifficulty
import com.nhatpham.dishcover.domain.model.recipe.RecipeIngredient
import com.nhatpham.dishcover.presentation.component.recipe.DifficultySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreateScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    viewModel: RecipeCreateViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle navigation when recipe is created
    LaunchedEffect(state.isCreated, state.createdRecipeId) {
        if (state.isCreated && state.createdRecipeId != null) {
            onNavigateToRecipe(state.createdRecipeId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(RecipeCreateEvent.Submit) },
                        enabled = !state.isSubmitting && state.title.isNotBlank()
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Section
            item {
                RecipeTitleSection(
                    title = state.title,
                    onTitleChanged = { viewModel.onEvent(RecipeCreateEvent.TitleChanged(it)) }
                )
            }

            // Description Section
            item {
                RecipeDescriptionSection(
                    description = state.description,
                    onDescriptionChanged = { viewModel.onEvent(RecipeCreateEvent.DescriptionChanged(it)) }
                )
            }

            // Recipe Details Section
            item {
                RecipeDetailsSection(
                    prepTime = state.prepTime,
                    cookTime = state.cookTime,
                    servings = state.servings,
                    onPrepTimeChanged = { viewModel.onEvent(RecipeCreateEvent.PrepTimeChanged(it)) },
                    onCookTimeChanged = { viewModel.onEvent(RecipeCreateEvent.CookTimeChanged(it)) },
                    onServingsChanged = { viewModel.onEvent(RecipeCreateEvent.ServingsChanged(it)) }
                )
            }

            // Difficulty Section - NEW: Using DifficultySelector
            item {
                RecipeDifficultySection(
                    selectedDifficulty = RecipeDifficulty.fromString(state.difficultyLevel),
                    onDifficultyChanged = { difficulty ->
                        viewModel.onEvent(RecipeCreateEvent.DifficultyLevelChanged(difficulty.displayName))
                    }
                )
            }

            // Ingredients Section
            item {
                RecipeIngredientsSection(
                    ingredients = state.ingredients,
                    onAddIngredient = { name, quantity, unit, notes ->
                        viewModel.onEvent(RecipeCreateEvent.AddIngredient(name, quantity, unit, notes))
                    },
                    onRemoveIngredient = { index ->
                        viewModel.onEvent(RecipeCreateEvent.RemoveIngredient(index))
                    }
                )
            }

            // Instructions Section
            item {
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
            }

            // Tags Section
            item {
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

            // Privacy Section
            item {
                RecipePrivacySection(
                    isPublic = state.isPublic,
                    onPrivacyChanged = { isPublic ->
                        viewModel.onEvent(RecipeCreateEvent.PrivacyChanged(isPublic))
                    }
                )
            }
        }
    }

    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or error dialog
        }
    }
}

@Composable
private fun RecipeTitleSection(
    title: String,
    onTitleChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Recipe Title *",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            placeholder = { Text("Enter recipe title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun RecipeDescriptionSection(
    description: String,
    onDescriptionChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            placeholder = { Text("Describe your recipe") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )
    }
}

@Composable
private fun RecipeDetailsSection(
    prepTime: String,
    cookTime: String,
    servings: String,
    onPrepTimeChanged: (String) -> Unit,
    onCookTimeChanged: (String) -> Unit,
    onServingsChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Recipe Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Prep Time
            OutlinedTextField(
                value = prepTime,
                onValueChange = onPrepTimeChanged,
                label = { Text("Prep (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Cook Time
            OutlinedTextField(
                value = cookTime,
                onValueChange = onCookTimeChanged,
                label = { Text("Cook (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Servings
            OutlinedTextField(
                value = servings,
                onValueChange = onServingsChanged,
                label = { Text("Servings") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@Composable
private fun RecipeDifficultySection(
    selectedDifficulty: RecipeDifficulty,
    onDifficultyChanged: (RecipeDifficulty) -> Unit
) {
    Column {
        Text(
            text = "Recipe Difficulty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Use the DifficultySelector component
        DifficultySelector(
            selectedDifficulty = selectedDifficulty,
            onDifficultySelected = onDifficultyChanged,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecipeIngredientsSection(
    ingredients: List<RecipeIngredient>,
    onAddIngredient: (String, String, String, String?) -> Unit,
    onRemoveIngredient: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            TextButton(
                onClick = { /* TODO: Show add ingredient dialog */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                        Text(
                            text = "${recipeIngredient.quantity} ${recipeIngredient.unit} ${recipeIngredient.ingredient.name}",
                            modifier = Modifier.weight(1f)
                        )
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
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            TextButton(onClick = onAddInstruction) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Step")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        instructions.forEachIndexed { index, instruction ->
            OutlinedTextField(
                value = instruction,
                onValueChange = { onInstructionChanged(index, it) },
                label = { Text("Step ${index + 1}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                trailingIcon = {
                    IconButton(
                        onClick = { onRemoveInstruction(index) }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove step")
                    }
                }
            )
        }

        if (instructions.isEmpty()) {
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Click 'Add Step' to start adding instructions") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }
    }
}

@Composable
private fun RecipeTagsSection(
    selectedTags: List<String>,
    onToggleTag: (String) -> Unit,
    onAddCustomTag: (String) -> Unit
) {
    var customTag by remember { mutableStateOf("") }

    Column {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Popular tags
        val popularTags = listOf("Quick", "Healthy", "Vegetarian", "Dessert", "Spicy", "Comfort Food")

        Text(
            text = "Popular Tags",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Tag chips
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

        Spacer(modifier = Modifier.height(8.dp))

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

@Composable
private fun RecipePrivacySection(
    isPublic: Boolean,
    onPrivacyChanged: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Privacy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPublic) "Public Recipe" else "Private Recipe",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isPublic) "Anyone can view and cook this recipe" else "Only you can view this recipe",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isPublic,
                        onCheckedChange = onPrivacyChanged
                    )
                }
            }
        }
    }
}