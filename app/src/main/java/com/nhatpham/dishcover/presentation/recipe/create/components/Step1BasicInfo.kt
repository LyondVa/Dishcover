package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateEvent
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateState
import com.nhatpham.dishcover.presentation.recipe.create.RecipeCreateViewModel
import com.nhatpham.dishcover.presentation.recipe.components.ImageUploaderWithProgress

@Composable
fun Step1BasicInfo(
    state: RecipeCreateState,
    viewModel: RecipeCreateViewModel,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RecipeTitleSection(
            title = state.title,
            onTitleChanged = { viewModel.onEvent(RecipeCreateEvent.TitleChanged(it)) }
        )
        RecipeDescriptionSection(
            description = state.description,
            onDescriptionChanged = { viewModel.onEvent(RecipeCreateEvent.DescriptionChanged(it)) }
        )
        RecipeDetailsSection(
            prepTime = state.prepTime,
            cookTime = state.cookTime,
            servings = state.servings,
            onPrepTimeChanged = { viewModel.onEvent(RecipeCreateEvent.PrepTimeChanged(it)) },
            onCookTimeChanged = { viewModel.onEvent(RecipeCreateEvent.CookTimeChanged(it)) },
            onServingsChanged = { viewModel.onEvent(RecipeCreateEvent.ServingsChanged(it)) }
        )
        ImageUploaderWithProgress(
            imageUri = state.coverImageUri,
            isUploading = state.isUploadingImage,
            uploadError = state.imageUploadError,
            onImageSelected = { uri ->
                viewModel.onEvent(RecipeCreateEvent.CoverImageChanged(uri))
            },
            onRequestImagePicker = { imagePickerLauncher.launch("image/*") }
        )
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
            OutlinedTextField(
                value = prepTime,
                onValueChange = onPrepTimeChanged,
                label = { Text("Prep (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = cookTime,
                onValueChange = onCookTimeChanged,
                label = { Text("Cook (min)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
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