package com.nhatpham.dishcover.presentation.recipe.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import com.nhatpham.dishcover.ui.theme.PrimaryColor

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
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Image picker launcher
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

    // Error handling with snackbar
    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Recipe", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryColor
                        )
                    }
                }
                // Removed "Create" button from actions
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content with transition animation
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { step ->
                Box {
                    when (step) {
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
                        showConfirmDialog = true
                    }
                },
                modifier = Modifier.padding(16.dp),
                isLoading = state.isUploadingImage || state.isSubmitting
            )
        }
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Create Recipe") },
            text = { Text("Are you sure you want to create this recipe?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(RecipeCreateEvent.Submit)
                        showConfirmDialog = false
                    }
                ) {
                    Text("Create", color = PrimaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}