// CreateCookbookScreen.kt - Updated with recipe selection
package com.nhatpham.dishcover.presentation.cookbook.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.presentation.cookbook.create.components.RecipeSelectionSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCookbookScreen(
    onNavigateBack: () -> Unit,
    onCookbookCreated: (String) -> Unit,
    viewModel: CreateCookbookViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle navigation events
    LaunchedEffect(state.isSuccess, state.createdCookbookId) {
        if (state.isSuccess && state.createdCookbookId != null) {
            onCookbookCreated(state.createdCookbookId!!)
        }
    }

    // Handle errors
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Auto-clear error after some time or let user dismiss
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Cookbook") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.createCookbook() },
                        enabled = state.canCreate && !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cover Image Section
            CoverImageSection(
                coverImageUrl = state.coverImageUrl,
                onImageSelected = viewModel::updateCoverImage,
                onRemoveImage = { viewModel.updateCoverImage(null) }
            )

            // Basic Information
            BasicInformationSection(
                title = state.title,
                description = state.description,
                onTitleChanged = viewModel::updateTitle,
                onDescriptionChanged = viewModel::updateDescription,
                focusManager = focusManager
            )

            // Recipe Selection Section - NEW
            RecipeSelectionSection(
                selectedRecipes = state.selectedRecipes,
                availableRecipes = state.availableRecipes,
                isLoadingRecipes = state.isLoadingRecipes,
                onRecipeToggle = viewModel::toggleRecipeSelection
            )

            // Privacy Settings
            PrivacySection(
                isPublic = state.isPublic,
                isCollaborative = state.isCollaborative,
                onPublicChanged = viewModel::updateIsPublic,
                onCollaborativeChanged = viewModel::updateIsCollaborative
            )

            // Tags Section
            TagsSection(
                tags = state.tags,
                onTagsChanged = viewModel::updateTags
            )

            // Error message
            if (state.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = state.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Bottom spacer to avoid FAB overlap
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CoverImageSection(
    coverImageUrl: String?,
    onImageSelected: (String?) -> Unit,
    onRemoveImage: () -> Unit
) {
    Column {
        Text(
            text = "Cover Image",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    // TODO: Implement image picker
                },
            contentAlignment = Alignment.Center
        ) {
            if (coverImageUrl != null) {
                AsyncImage(
                    model = coverImageUrl,
                    contentDescription = "Cookbook cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Remove button
                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Add Cover Image",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Tap to select an image",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicInformationSection(
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            label = { Text("Cookbook Title") },
            placeholder = { Text("e.g., My Italian Favorites") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("Description (Optional)") },
            placeholder = { Text("Tell others about your cookbook...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun PrivacySection(
    isPublic: Boolean,
    isCollaborative: Boolean,
    onPublicChanged: (Boolean) -> Unit,
    onCollaborativeChanged: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Privacy & Sharing",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Public/Private toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Public Cookbook",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isPublic) "Anyone can view and follow" else "Only you can view",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isPublic,
                    onCheckedChange = onPublicChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Collaborative toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Allow Collaboration",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isCollaborative) "Others can contribute recipes" else "Only you can add recipes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isCollaborative,
                    onCheckedChange = onCollaborativeChanged,
                    enabled = isPublic // Only allow collaboration if public
                )
            }
        }
    }
}

@Composable
private fun TagsSection(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit
) {
    var newTag by remember { mutableStateOf("") }

    Column {
        Text(
            text = "Tags (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add tags to help others discover your cookbook",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tag input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = newTag,
                onValueChange = { newTag = it },
                label = { Text("Add tag") },
                placeholder = { Text("e.g., Italian, Quick meals") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newTag.isNotBlank() && !tags.contains(newTag.trim())) {
                            onTagsChanged(tags + newTag.trim())
                            newTag = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (newTag.isNotBlank() && !tags.contains(newTag.trim())) {
                        onTagsChanged(tags + newTag.trim())
                        newTag = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add tag"
                )
            }
        }

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            // Display tags
            tags.chunked(3).forEach { rowTags ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTags.forEach { tag ->
                        InputChip(
                            onClick = { },
                            label = { Text(tag) },
                            selected = false,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $tag",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            onTagsChanged(tags - tag)
                                        }
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}