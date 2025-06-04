package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.presentation.recipe.components.ImageUploaderWithProgress
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateEvent
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel

@Composable
fun Step1BasicInfo(
    state: RecipeCreateState,
    viewModel: RecipeCreateViewModel,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image upload section with progress
        ImageUploaderWithProgress(
            imageUri = state.coverImageUri,
            isUploading = state.isUploadingImage,
            uploadError = state.imageUploadError,
            onImageSelected = { uri ->
                viewModel.onEvent(RecipeCreateEvent.CoverImageChanged(uri))
            },
            onRequestImagePicker = { imagePickerLauncher.launch("image/*") }
        )

        // Recipe Name
        Text(
            text = "Recipe Name",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.onEvent(RecipeCreateEvent.TitleChanged(it)) },
            placeholder = { Text("Chicken Ramen") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.titleError != null
        )

        state.titleError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Estimated Time
        Text(
            text = "Estimated Time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = state.prepTime,
            onValueChange = { viewModel.onEvent(RecipeCreateEvent.PrepTimeChanged(it)) },
            placeholder = { Text("45 mins") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Description
        Text(
            text = "Description (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.onEvent(RecipeCreateEvent.DescriptionChanged(it)) },
            placeholder = { Text("Enter the detail of your recipe") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )
    }
}