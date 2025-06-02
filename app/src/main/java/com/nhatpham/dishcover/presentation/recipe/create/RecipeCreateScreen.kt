package com.nhatpham.dishcover.presentation.recipe.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.common.LoadingIndicator
import com.nhatpham.dishcover.presentation.recipe.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreateScreen(
    viewModel: RecipeCreateViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRecipeCreated: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // In a real app, we would upload the image and get a URL
            // For now, we'll just use the URI as a string
            viewModel.onEvent(RecipeCreateEvent.CoverImageChanged(it.toString()))
        }
    }

    // Handle successful recipe creation
    LaunchedEffect(state.isCreated) {
        if (state.isCreated && state.createdRecipeId != null) {
            onRecipeCreated(state.createdRecipeId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Save button
                    Button(
                        onClick = { viewModel.onEvent(RecipeCreateEvent.Submit) },
                        enabled = !state.isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Recipe"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title and description
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { viewModel.onEvent(RecipeCreateEvent.TitleChanged(it)) },
                        label = { Text("Recipe Title") },
                        isError = state.titleError != null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    state.titleError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onEvent(RecipeCreateEvent.DescriptionChanged(it)) },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Cover image
                ImageUploader(
                    imageUri = state.coverImageUri,
                    onImageSelected = { viewModel.onEvent(RecipeCreateEvent.CoverImageChanged(it)) },
                    onRequestImagePicker = { imagePickerLauncher.launch("image/*") }
                )

                // Recipe metadata
                RecipeMetadataEditor(
                    prepTime = state.prepTime,
                    onPrepTimeChanged = { viewModel.onEvent(RecipeCreateEvent.PrepTimeChanged(it)) },
                    cookTime = state.cookTime,
                    onCookTimeChanged = { viewModel.onEvent(RecipeCreateEvent.CookTimeChanged(it)) },
                    servings = state.servings,
                    onServingsChanged = { viewModel.onEvent(RecipeCreateEvent.ServingsChanged(it)) },
                    difficultyLevel = state.difficultyLevel,
                    onDifficultyLevelChanged = { viewModel.onEvent(RecipeCreateEvent.DifficultyLevelChanged(it)) }
                )

                // Ingredients
                IngredientEditor(
                    ingredients = state.ingredients,
                    onAddIngredient = { name, quantity, unit, notes ->
                        viewModel.onEvent(
                            RecipeCreateEvent.AddIngredient(
                                name = name,
                                quantity = quantity,
                                unit = unit,
                                notes = notes
                            )
                        )
                    },
                    onRemoveIngredient = { index ->
                        viewModel.onEvent(RecipeCreateEvent.RemoveIngredient(index))
                    },
                    error = state.ingredientsError
                )

                // Instructions
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = state.instructions,
                        onValueChange = { viewModel.onEvent(RecipeCreateEvent.InstructionsChanged(it)) },
                        label = { Text("Step-by-step instructions") },
                        isError = state.instructionsError != null,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 10
                    )

                    state.instructionsError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Provide clear, step-by-step instructions. Each step should be on a new line or numbered.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tags
                TagSelector(
                    availableTags = state.availableCategories,
                    selectedTags = state.selectedTags,
                    onTagToggle = { viewModel.onEvent(RecipeCreateEvent.ToggleTag(it)) },
                    onAddCustomTag = { viewModel.onEvent(RecipeCreateEvent.AddCustomTag(it)) }
                )

                // Privacy settings
                RecipePrivacyControl(
                    isPublic = state.isPublic,
                    onPrivacyChanged = { viewModel.onEvent(RecipeCreateEvent.PrivacyChanged(it)) }
                )

                // Error message
                state.error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Bottom space for better scrolling experience
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Loading indicator
            if (state.isSubmitting) {
                LoadingIndicator()
            }
        }
    }
}