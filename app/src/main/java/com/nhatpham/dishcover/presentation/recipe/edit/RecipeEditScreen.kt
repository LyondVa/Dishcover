package com.nhatpham.dishcover.presentation.recipe.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.component.LoadingIndicator
import com.nhatpham.dishcover.presentation.recipe.components.*
import com.nhatpham.dishcover.presentation.recipe.edit.components.CollapsibleSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    recipeId: String,
    viewModel: RecipeEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRecipeUpdated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Image picker launcher with upload handling
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Handle image upload when image is selected
            viewModel.onEvent(RecipeEditEvent.UploadImage(context, it))
        }
    }

    // Load recipe when screen is first displayed
    LaunchedEffect(recipeId) {
        viewModel.onEvent(RecipeEditEvent.LoadRecipe(recipeId))
    }

    // Handle successful update
    LaunchedEffect(state.isUpdated) {
        if (state.isUpdated) {
            onRecipeUpdated()
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Edit Recipe") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                )
            }
        }, actions = {
            IconButton(
                onClick = { viewModel.onEvent(RecipeEditEvent.Submit) },
                enabled = !state.isSubmitting && state.originalRecipe != null
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Changes"
                    )
                }
            }
        })
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingIndicator()
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error ?: "Failed to load recipe",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.onEvent(RecipeEditEvent.LoadRecipe(recipeId)) }) {
                            Text("Retry")
                        }
                    }
                }

                state.originalRecipe != null -> {
                    RecipeEditContent(
                        state = state,
                        viewModel = viewModel,
                        imagePickerLauncher = imagePickerLauncher
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeEditContent(
    state: RecipeEditState,
    viewModel: RecipeEditViewModel,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic Information Section (Always Visible)
        RecipeBasicInfoSection(
            title = state.title,
            onTitleChanged = { viewModel.onEvent(RecipeEditEvent.TitleChanged(it)) },
            titleError = state.titleError,
            description = state.description,
            onDescriptionChanged = { viewModel.onEvent(RecipeEditEvent.DescriptionChanged(it)) },
            coverImageUri = state.coverImageUri,
            onImageSelected = { viewModel.onEvent(RecipeEditEvent.CoverImageChanged(it)) },
            onRequestImagePicker = { imagePickerLauncher.launch("image/*") },

            isUploadingImage = state.isUploadingImage,
            imageUploadError = state.imageUploadError,
        )

        // Recipe Details Section (Always Visible)
        RecipeMetadataEditor(prepTime = state.prepTime,
            onPrepTimeChanged = { viewModel.onEvent(RecipeEditEvent.PrepTimeChanged(it)) },
            cookTime = state.cookTime,
            onCookTimeChanged = { viewModel.onEvent(RecipeEditEvent.CookTimeChanged(it)) },
            servings = state.servings,
            onServingsChanged = { viewModel.onEvent(RecipeEditEvent.ServingsChanged(it)) },
            difficultyLevel = state.difficultyLevel,
            onDifficultyLevelChanged = { viewModel.onEvent(RecipeEditEvent.DifficultyLevelChanged(it)) })

        // Collapsible Sections
        CollapsibleSection(
            title = "Ingredients", isExpanded = true, // Could be made stateful
            badge = "${state.ingredients.size} items"
        ) {
            IngredientEditor(
                ingredients = state.ingredients,
                onAddIngredient = { name, quantity, unit, notes ->
                    viewModel.onEvent(RecipeEditEvent.AddIngredient(name, quantity, unit, notes))
                },
                onRemoveIngredient = { index ->
                    viewModel.onEvent(RecipeEditEvent.RemoveIngredient(index))
                },
                error = state.ingredientsError
            )
        }

        CollapsibleSection(
            title = "Instructions",
            isExpanded = true,
            badge = if (state.instructions.isNotBlank()) "Added" else "Required"
        ) {
            InstructionsEditor(
                instructions = state.instructions,
                onInstructionsChanged = { viewModel.onEvent(RecipeEditEvent.InstructionsChanged(it)) },
                error = state.instructionsError
            )
        }

        CollapsibleSection(
            title = "Tags & Categories",
            isExpanded = false,
            badge = "${state.selectedTags.size} tags"
        ) {
            TagSelector(availableTags = state.availableCategories,
                selectedTags = state.selectedTags,
                onTagToggle = { viewModel.onEvent(RecipeEditEvent.ToggleTag(it)) },
                onAddCustomTag = { viewModel.onEvent(RecipeEditEvent.AddCustomTag(it)) })
        }

        CollapsibleSection(
            title = "Privacy Settings",
            isExpanded = false,
            badge = if (state.isPublic) "Public" else "Private"
        ) {
            RecipePrivacyControl(isPublic = state.isPublic,
                onPrivacyChanged = { viewModel.onEvent(RecipeEditEvent.PrivacyChanged(it)) })
        }

        // Save Button (Bottom Action)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.onEvent(RecipeEditEvent.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !state.isSubmitting
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving Changes...")
            } else {
                Text("Save Changes")
            }
        }

        // Show any general errors
        state.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Bottom spacing for better scrolling experience
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun RecipeBasicInfoSection(
    title: String,
    isUploadingImage: Boolean = false,
    imageUploadError: String? = null,
    onTitleChanged: (String) -> Unit,
    titleError: String?,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    coverImageUri: String?,
    onImageSelected: (String?) -> Unit,
    onRequestImagePicker: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cover Image
        ImageUploaderWithProgress(
            imageUri = coverImageUri,
            isUploading = isUploadingImage,
            uploadError = imageUploadError,
            onImageSelected = onImageSelected,
            onRequestImagePicker = onRequestImagePicker
        )

        // Recipe Title
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            label = { Text("Recipe Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = titleError != null
        )

        titleError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Description
        OutlinedTextField(value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("Description (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4,
            placeholder = { Text("Tell us about this recipe...") })
    }
}

@Composable
fun InstructionsEditor(
    instructions: String, onInstructionsChanged: (String) -> Unit, error: String?
) {
    Column {
        OutlinedTextField(
            value = instructions,
            onValueChange = onInstructionsChanged,
            label = { Text("Cooking Instructions *") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("1. First step...\n2. Second step...\n3. Final step...") },
            isError = error != null
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(
            text = "Write step-by-step instructions. Each step should be clear and easy to follow.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}