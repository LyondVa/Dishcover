package com.nhatpham.dishcover.presentation.feed.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.nhatpham.dishcover.presentation.components.LoadingDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Handle post creation success
    LaunchedEffect(state.isPostCreated) {
        if (state.isPostCreated) {
            onPostCreated()
        }
    }

    // Show loading dialog
    if (state.isCreating) {
        LoadingDialog()
    }

    // Show error snackbar
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar and clear error
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Caption input
            OutlinedTextField(
                value = state.caption,
                onValueChange = viewModel::onCaptionChanged,
                label = { Text("Caption") },
                placeholder = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Images section
            Text(
                text = "Images",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Image grid
            ImageSelectionGrid(
                selectedImages = state.selectedImages,
                onImageAdd = { imagePickerLauncher.launch("image/*") },
                onImageRemove = viewModel::onImageRemoved,
                maxImages = CreatePostViewModel.MAX_IMAGES
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Additional options
            PostOptionsSection(
                hashtags = state.hashtags.joinToString(" ") { "#$it" },
                onHashtagsChanged = viewModel::onHashtagsChanged,
                location = state.location ?: "",
                onLocationChanged = viewModel::onLocationChanged,
                isPublic = state.isPublic,
                onPrivacyToggled = viewModel::onPrivacyToggled,
                allowComments = state.allowComments,
                onCommentsToggled = viewModel::onCommentsToggled
            )

            Spacer(modifier = Modifier.weight(1f))

            // Post button
            Button(
                onClick = viewModel::createPost,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isCreating && (state.caption.isNotBlank() || state.selectedImages.isNotEmpty()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (state.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
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
        }
    }
}

@Composable
private fun ImageSelectionGrid(
    selectedImages: List<Uri>,
    onImageAdd: () -> Unit,
    onImageRemove: (Uri) -> Unit,
    maxImages: Int
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Selected images
        items(selectedImages) { uri ->
            SelectedImageItem(
                uri = uri,
                onRemove = { onImageRemove(uri) }
            )
        }

        // Add image button
        if (selectedImages.size < maxImages) {
            item {
                AddImageButton(onClick = onImageAdd)
            }
        }
    }
}

@Composable
private fun SelectedImageItem(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Selected image",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove image",
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun AddImageButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                2.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Add image",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add Image",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    Column {
        // Hashtags
        OutlinedTextField(
            value = hashtags,
            onValueChange = onHashtagsChanged,
            label = { Text("Hashtags") },
            placeholder = { Text("#food #cooking #recipe") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Tag, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChanged,
            label = { Text("Location") },
            placeholder = { Text("Add location...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy and comment settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPublic) "Public" else "Private",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = isPublic,
                onCheckedChange = { onPrivacyToggled() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Allow Comments",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Switch(
                checked = allowComments,
                onCheckedChange = { onCommentsToggled() }
            )
        }
    }
}