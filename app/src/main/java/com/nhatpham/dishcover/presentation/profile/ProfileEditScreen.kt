package com.nhatpham.dishcover.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.presentation.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: ProfileEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onProfileUpdated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf(state.initialUsername) }
    var fullName by remember { mutableStateOf(state.initialFullName) }
    var bio by remember { mutableStateOf(state.initialBio ?: "") }
    var website by remember { mutableStateOf(state.initialWebsite ?: "") }
    var location by remember { mutableStateOf(state.initialLocation ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Handle image selection
            // In a real app, you'd upload this to storage and get a URL
            // For this example, we'll just use a placeholder
            viewModel.onEvent(ProfileEditEvent.ProfilePictureSelected(it.toString()))
        }
    }

    // Update form values when initial data changes
    LaunchedEffect(
        state.initialUsername,
        state.initialFullName,
        state.initialBio,
        state.initialWebsite,
        state.initialLocation
    ) {
        username = state.initialUsername
        fullName = state.initialFullName
        bio = state.initialBio ?: ""
        website = state.initialWebsite ?: ""
        location = state.initialLocation ?: ""
    }

    // Handle successful update
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onProfileUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.onEvent(
                                ProfileEditEvent.SaveProfile(
                                    username = username,
                                    fullName = fullName,
                                    bio = bio,
                                    website = website,
                                    location = location
                                )
                            )
                        },
                        enabled = !state.isSaving && username.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePickerLauncher.launch("image/*") }
                    ) {
                        if (state.profilePictureUri != null) {
                            AsyncImage(
                                model = state.profilePictureUri,
                                contentDescription = "Profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change profile picture",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Form fields
                FormField(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it },
                    leadingIcon = Icons.Default.AlternateEmail,
                    isRequired = true
                )

                FormField(
                    label = "Full Name",
                    value = fullName,
                    onValueChange = { fullName = it },
                    leadingIcon = Icons.Default.Person
                )

                FormField(
                    label = "Bio",
                    value = bio,
                    onValueChange = { bio = it },
                    leadingIcon = Icons.Default.Info,
                    singleLine = false,
                    maxLines = 4
                )

                FormField(
                    label = "Website",
                    value = website,
                    onValueChange = { website = it },
                    leadingIcon = Icons.Default.Language
                )

                FormField(
                    label = "Location",
                    value = location,
                    onValueChange = { location = it },
                    leadingIcon = Icons.Default.LocationOn
                )

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.isSaving) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isRequired: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = if (isRequired) "$label*" else label
                )
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            maxLines = maxLines
        )
    }
}