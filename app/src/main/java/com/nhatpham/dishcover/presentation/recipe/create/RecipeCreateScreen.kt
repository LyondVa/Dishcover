package com.nhatpham.dishcover.presentation.recipe.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhatpham.dishcover.presentation.common.LoadingIndicator
import com.nhatpham.dishcover.presentation.recipe.create.components.*
import com.nhatpham.dishcover.presentation.recipe.create.utils.RecipeCreateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreateScreen(
    viewModel: RecipeCreateViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRecipeCreated: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
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
                title = { Text("Create New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
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
                totalSteps = 4,
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
                    3 -> Step3Categories(
                        state = state,
                        viewModel = viewModel
                    )
                    4 -> Step4Review(
                        state = state,
                        viewModel = viewModel
                    )
                }

                if (state.isSubmitting) {
                    LoadingIndicator()
                }
            }

            // Bottom navigation
            BottomNavigationButtons(
                currentStep = currentStep,
                totalSteps = 4,
                canProceed = RecipeCreateUtils.canProceedToNextStep(currentStep, state),
                onPrevious = { if (currentStep > 1) currentStep-- },
                onNext = {
                    print("step: $currentStep")
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        viewModel.onEvent(RecipeCreateEvent.Submit)
                    }
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}