package com.nhatpham.dishcover.presentation.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.components.RecipeList
import com.nhatpham.dishcover.presentation.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToFollowers: (String) -> Unit = {},
    onNavigateToFollowing: (String) -> Unit = {},
    onNavigateToRecipe: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    // Load profile data if userId is provided
    LaunchedEffect(userId) {
        userId?.let {
            viewModel.onEvent(UserProfileEvent.LoadProfile(it))
        }
    }

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = state.user?.username ?: "Profile",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                },
//                actions = {
//                    if (state.isCurrentUser) {
//                        IconButton(onClick = onNavigateToSettings) {
//                            Icon(
//                                imageVector = Icons.Default.Settings,
//                                contentDescription = "Settings"
//                            )
//                        }
//                    } else {
//                        IconButton(onClick = { /* Share profile */ }) {
//                            Icon(
//                                imageVector = Icons.Default.Share,
//                                contentDescription = "Share Profile"
//                            )
//                        }
//                    }
//                }
//            )
//        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingIndicator()
                }
                state.error != null -> {
                    EmptyState(
                        message = state.error ?: "Failed to load profile",
                        icon = Icons.Default.Error
                    )
                }
                state.user != null -> {
                    UserProfileContent(
                        state = state,
                        onEditProfile = onNavigateToEditProfile,
                        onFollowToggle = {
                            viewModel.onEvent(UserProfileEvent.ToggleFollowStatus)
                        },
                        onViewFollowers = {
                            state.user?.userId?.let { onNavigateToFollowers(it) }
                        },
                        onViewFollowing = {
                            state.user?.userId?.let { onNavigateToFollowing(it) }
                        },
                        onRecipeClick = onNavigateToRecipe
                    )
                }
                else -> {
                    EmptyState(
                        message = "No profile data found",
                        icon = Icons.Default.Person
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(
    state: UserProfileState,
    onEditProfile: () -> Unit,
    onFollowToggle: () -> Unit,
    onViewFollowers: () -> Unit,
    onViewFollowing: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val user = state.user ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Profile header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (user.profilePicture != null) {
                    AsyncImage(
                        model = user.profilePicture,
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Bio
            if (!user.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = state.followers.size.toString(),
                    label = "Followers",
                    onClick = onViewFollowers
                )

                StatItem(
                    value = state.following.size.toString(),
                    label = "Following",
                    onClick = onViewFollowing
                )

                StatItem(
                    value = "0", // Replace with actual recipe count when available
                    label = "Recipes"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            if (state.isCurrentUser) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onFollowToggle,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !state.isUpdatingFollowStatus,
                        colors = if (!state.isFollowing) {
                            ButtonDefaults.buttonColors()
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        }
                    ) {
                        if (state.isUpdatingFollowStatus) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (state.isFollowing) "Following" else "Follow")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { /* Message user */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Message")
                    }
                }
            }
        }

        Divider()

        // Tabs for Recipes, Favorites, etc.
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("Recipes", "Favorites", "Cookbooks")

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> {
                // Recipes tab content
                RecipeList(
                    recipes = listOf(), // Get recipes from a separate VM or API call
                    onRecipeClick = onRecipeClick,
                    emptyMessage = "No recipes yet"
                )
            }
            1 -> {
                // Favorites tab content
                RecipeList(
                    recipes = listOf(), // Get favorite recipes
                    onRecipeClick = onRecipeClick,
                    emptyMessage = "No favorite recipes"
                )
            }
            2 -> {
                // Cookbooks tab content
                // Implement cookbook list here
                EmptyState(
                    message = "No cookbooks yet",
                    icon = Icons.Default.Book
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .let {
                if (onClick != null) {
                    it.clickable(onClick = onClick)
                } else {
                    it
                }
            }
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}