// CreatePostScreen.kt - Updated with recipe linking functionality
package com.nhatpham.dishcover.presentation.feed.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.presentation.component.LoadingDialog
import com.nhatpham.dishcover.presentation.feed.create.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    onRecipeClick: (String) -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showRecipeSelection by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Handle post creation success
    LaunchedEffect(state.isCreated) {
        if (state.isCreated) {
            onPostCreated()
        }
    }

    // Show loading dialog while creating post
    if (state.isCreating) {
        LoadingDialog(
            message = if (state.isUploadingImages) "Uploading images..." else "Creating post..."
        )
    }

    // Recipe selection dialog
    if (showRecipeSelection) {
        RecipeSelectionDialog(
            onDismiss = { showRecipeSelection = false },
            onRecipeSelected = { recipe ->
                viewModel.onRecipeAdded(recipe)
                showRecipeSelection = false
            },
            selectedRecipes = state.selectedRecipes.map { it.recipeId }.toSet()
        )
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Create Post") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }, actions = {
            // Recipe count indicator
            if (state.hasRecipes) {
                CompactRecipeIndicator(
                    recipeCount = state.selectedRecipes.size,
                    onManageRecipes = { showRecipeSelection = true },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Caption input
            PostCaptionField(
                caption = state.caption, onCaptionChanged = viewModel::onCaptionChanged
            )

            // Image selection section
            ImageSelectionSection(
                selectedImages = state.selectedImages,
                onImageAdd = { imagePickerLauncher.launch("image/*") },
                onImageRemove = viewModel::onImageRemoved,
                maxImages = 10
            )

            // Recipe linking section
            CreatePostRecipeSection(
                selectedRecipes = state.selectedRecipes,
                onAddRecipe = { showRecipeSelection = true },
                onRemoveRecipe = viewModel::onRecipeRemoved,
                onRecipeClick = onRecipeClick
            )

            // Post options
            PostOptionsSection(
                hashtags = state.hashtags,
                onHashtagsChanged = viewModel::onHashtagsChanged,
                location = state.location,
                onLocationChanged = viewModel::onLocationChanged,
                isPublic = state.isPublic,
                onPrivacyToggled = viewModel::onPrivacyToggled,
                allowComments = state.allowComments,
                onCommentsToggled = viewModel::onCommentsToggled
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Post button
            Button(
                onClick = {
                    // Use the new method that handles context properly
                    viewModel.uploadImagesAndCreatePost(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state.canCreatePost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), color = Color.White
                    )
                } else {
                    Text(
                        text = "Post",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Error display
            state.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun PostCaptionField(
    caption: String, onCaptionChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = caption,
        onValueChange = onCaptionChanged,
        label = { Text("What's on your mind?") },
        placeholder = { Text("Share your thoughts, recipe story, or cooking tips...") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        maxLines = 8,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun ImageSelectionSection(
    selectedImages: List<Uri>, onImageAdd: () -> Unit, onImageRemove: (Uri) -> Unit, maxImages: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Photos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            if (selectedImages.size < maxImages) {
                TextButton(onClick = onImageAdd) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Photo")
                }
            }
        }

        if (selectedImages.isEmpty()) {
            ImageSelectionPlaceholder(onImageAdd = onImageAdd)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(selectedImages) { uri ->
                    SelectedImageItem(uri = uri, onRemove = { onImageRemove(uri) })
                }

                if (selectedImages.size < maxImages) {
                    item {
                        AddImageButton(onClick = onImageAdd)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageSelectionPlaceholder(onImageAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onImageAdd() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add photos to your post",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectedImageItem(
    uri: Uri, onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f), CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(12.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun AddImageButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add image",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostOptionsSection(
    hashtags: String,
    onHashtagsChanged: (String) -> Unit,
    location: String,
    onLocationChanged: (String) -> Unit,
    isPublic: Boolean,
    onPrivacyToggled: () -> Unit,
    allowComments: Boolean,
    onCommentsToggled: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hashtags
        OutlinedTextField(value = hashtags,
            onValueChange = onHashtagsChanged,
            label = { Text("Hashtags") },
            placeholder = { Text("#food #cooking #recipe") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Tag, contentDescription = null)
            })

        // Location
        OutlinedTextField(value = location,
            onValueChange = onLocationChanged,
            label = { Text("Location") },
            placeholder = { Text("Add location...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            })

        // Privacy and comment settings
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onPrivacyToggled() }) {
                Switch(checked = isPublic, onCheckedChange = { onPrivacyToggled() })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPublic) "Public" else "Private",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCommentsToggled() }) {
                Switch(checked = allowComments, onCheckedChange = { onCommentsToggled() })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Allow Comments", style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}