package com.nhatpham.dishcover.presentation.recipe.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.recipe.create.components.*
import com.nhatpham.dishcover.presentation.recipe.create.utils.RecipeCreateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreateScreen(
    viewModel: RecipeCreateViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRecipeCreated: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }

    // Image picker launcher with upload handling
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(RecipeCreateEvent.UploadImage(context, it))
        }
    }

    // Handle successful recipe creation
    LaunchedEffect(state.isCreated, state.createdRecipeId) {
        if (state.isCreated && state.createdRecipeId != null) {
            onRecipeCreated(state.createdRecipeId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (currentStep == 5) {
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Step indicator
            StepIndicator(
                currentStep = currentStep,
                totalSteps = 5,
                modifier = Modifier.padding(16.dp)
            )

            // Content based on current step
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (currentStep) {
                    1 -> Step1BasicInfo(
                        state = state,
                        viewModel = viewModel,
                        imagePickerLauncher = imagePickerLauncher
                    )
                    2 -> Step2Ingredients(
                        state = state,
                        viewModel = viewModel
                    )
                    3 -> Step3Difficulty(
                        state = state,
                        viewModel = viewModel
                    )
                    4 -> Step4Tags(
                        state = state,
                        viewModel = viewModel
                    )
                    5 -> Step5Review(
                        state = state,
                        viewModel = viewModel
                    )
                }

                if (state.isSubmitting || state.isUploadingImage) {
                    LoadingIndicator()
                }
            }

            // Bottom navigation
            BottomNavigationButtons(
                currentStep = currentStep,
                totalSteps = 5,
                canProceed = RecipeCreateUtils.canProceedToNextStep(currentStep, state) && !state.isUploadingImage,
                onPrevious = { if (currentStep > 1) currentStep-- },
                onNext = {
                    if (currentStep < 5) {
                        currentStep++
                    } else {
                        viewModel.onEvent(RecipeCreateEvent.Submit)
                    }
                },
                modifier = Modifier.padding(16.dp),
                isLoading = state.isUploadingImage || state.isSubmitting
            )
        }
    }

    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Implement snackbar or error dialog
        }
    }
}