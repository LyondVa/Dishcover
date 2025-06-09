package com.nhatpham.dishcover.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.presentation.components.LoadingIndicator

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

    // Image picker launchers
    val profileImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(ProfileEditEvent.ProfilePictureSelected(it.toString()))
        }
    }

    val bannerImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(ProfileEditEvent.BannerImageSelected(it.toString()))
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

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(
                "Edit Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                )
            }
        }, actions = {
            Button(
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
                enabled = !state.isSaving && username.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "Save", color = Color.White, fontWeight = FontWeight.Medium
                    )
                }
            }
        })
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Enhanced Banner and Profile Picture Section
                BannerAndProfileSection(
                    currentBannerImage = state.bannerImageUri,
                    currentProfilePicture = state.profilePictureUri,
                    onSelectBannerImage = { bannerImagePickerLauncher.launch("image/*") },
                    onSelectProfileImage = { profileImagePickerLauncher.launch("image/*") },
                    isUploadingBanner = state.isUploadingBanner,
                    isUploadingProfile = state.isUploadingProfile
                )

                // Form Section with Enhanced Design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Form Header
                        Text(
                            text = "Profile Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Enhanced Form Fields
                        EnhancedFormField(
                            label = "Username",
                            value = username,
                            onValueChange = { username = it },
                            leadingIcon = Icons.Default.AlternateEmail,
                            isRequired = true,
                            placeholder = "Enter your username"
                        )

                        EnhancedFormField(
                            label = "Full Name",
                            value = fullName,
                            onValueChange = { fullName = it },
                            leadingIcon = Icons.Default.Person,
                            placeholder = "Enter your full name"
                        )

                        EnhancedFormField(
                            label = "Bio",
                            value = bio,
                            onValueChange = { bio = it },
                            leadingIcon = Icons.Default.Info,
                            placeholder = "Tell us about yourself",
                            singleLine = false,
                            maxLines = 4
                        )

                        EnhancedFormField(
                            label = "Website",
                            value = website,
                            onValueChange = { website = it },
                            leadingIcon = Icons.Default.Language,
                            placeholder = "https://yourwebsite.com"
                        )

                        EnhancedFormField(
                            label = "Location",
                            value = location,
                            onValueChange = { location = it },
                            leadingIcon = Icons.Default.LocationOn,
                            placeholder = "Where are you located?"
                        )
                    }
                }

                // Error Display
                if (state.error != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Bottom spacing for better UX
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun BannerAndProfileSection(
    currentBannerImage: String?,
    currentProfilePicture: String?,
    onSelectBannerImage: () -> Unit,
    onSelectProfileImage: () -> Unit,
    isUploadingBanner: Boolean,
    isUploadingProfile: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Banner Image Section
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onSelectBannerImage() }) {
            if (currentBannerImage != null) {
                AsyncImage(
                    model = currentBannerImage,
                    contentDescription = "Banner image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )
            }

            // Banner overlay with upload button
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            // Banner upload button
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isUploadingBanner) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp), strokeWidth = 3.dp, color = Color.White
                    )
                } else {
                    FloatingActionButton(
                        onClick = onSelectBannerImage,
                        modifier = Modifier.size(48.dp),
                        containerColor = Color.White.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change banner",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = if (currentBannerImage != null) "Change Banner" else "Add Banner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Profile Picture Section (overlapping banner)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(120.dp)
        ) {
            // Profile Picture
            if (currentProfilePicture != null) {
                AsyncImage(
                    model = currentProfilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape
                        )
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }

            // Profile Picture Upload Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
            ) {
                if (isUploadingProfile) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primary, CircleShape
                            ), contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White
                        )
                    }
                } else {
                    FloatingActionButton(
                        onClick = onSelectProfileImage,
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change profile picture",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isRequired: Boolean = false,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Field Label
        Text(
            text = if (isRequired) "$label *" else label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Text Field
        OutlinedTextField(value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ))

        // Required field indicator
        if (isRequired && value.isEmpty()) {
            Text(
                text = "This field is required",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
