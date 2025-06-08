// ReviewDialog.kt
package com.nhatpham.dishcover.presentation.recipe.create.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String, images: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var verified by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris.take(3) // Limit to 3 images
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Write a Review",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rating Section
                InteractiveRatingBar(
                    initialRating = rating,
                    onRatingSelected = { rating = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Comment Section
                ReviewCommentSection(
                    comment = comment,
                    onCommentChanged = { comment = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Photo Section
                ReviewPhotoSection(
                    selectedImages = selectedImages,
                    onAddPhoto = { imagePickerLauncher.launch("image/*") },
                    onRemovePhoto = { uri ->
                        selectedImages = selectedImages.filter { it != uri }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Verification Section
                ReviewVerificationSection(
                    verified = verified,
                    onVerifiedChanged = { verified = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSubmitting
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (rating > 0) {
                                isSubmitting = true
                                // Convert URIs to strings (in real app, upload images first)
                                val imageUrls = selectedImages.map { it.toString() }
                                onSubmit(rating, comment, imageUrls)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = rating > 0 && !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Submit Review")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCommentSection(
    comment: String,
    onCommentChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Share your experience (optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChanged,
            placeholder = {
                Text("Tell others about your cooking experience, any modifications you made, or tips for success...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            maxLines = 5
        )
    }
}

@Composable
private fun ReviewPhotoSection(
    selectedImages: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Add photos (optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "${selectedImages.size}/3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Photo Button
            if (selectedImages.size < 3) {
                item {
                    PhotoAddButton(onClick = onAddPhoto)
                }
            }

            // Selected Images
            items(selectedImages) { uri ->
                PhotoItem(
                    uri = uri,
                    onRemove = { onRemovePhoto(uri) }
                )
            }
        }
    }
}

@Composable
private fun PhotoAddButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add photo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Photo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoItem(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.size(80.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Review photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        // Remove button
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(4.dp, (-4).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            shadowElevation = 2.dp
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewVerificationSection(
    verified: Boolean,
    onVerifiedChanged: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVerifiedChanged(!verified) }
            .padding(vertical = 8.dp)
    ) {
        Checkbox(
            checked = verified,
            onCheckedChange = onVerifiedChanged
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "I actually cooked this recipe",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Verified reviews help other cooks make better decisions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (verified) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}