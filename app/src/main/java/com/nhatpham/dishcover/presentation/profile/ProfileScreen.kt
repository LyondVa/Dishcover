package com.nhatpham.dishcover.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
import com.nhatpham.dishcover.presentation.components.EmptyState
import com.nhatpham.dishcover.presentation.components.FollowButton
import com.nhatpham.dishcover.presentation.components.FollowButtonSize
import com.nhatpham.dishcover.presentation.feed.components.PostItem
import com.nhatpham.dishcover.ui.theme.getCategoryColor

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
    onNavigateToPostDetail: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    // Load profile data if userId is provided, otherwise load current user
    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.onEvent(UserProfileEvent.LoadProfile(userId))
        } else {
            // For current user profile (when userId is null)
            viewModel.loadCurrentUserProfile()
        }
    }

    // Handle errors
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
        }
    }

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
            XStyleProfileContent(
                state = state,
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings,
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
                onRecipeClick = onNavigateToRecipe,
                onPostClick = onNavigateToPostDetail,
                onUserClick = onNavigateToUserProfile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XStyleProfileContent(
    state: UserProfileState,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onFollowToggle: () -> Unit,
    onViewFollowers: () -> Unit,
    onViewFollowing: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    val user = state.user ?: return
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Recipes", "Cookbooks")

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with banner and profile info
        item {
            ProfileHeader(
                user = user,
                isCurrentUser = state.isCurrentUser,
                isFollowing = state.isFollowing,
                isUpdatingFollowStatus = state.isUpdatingFollowStatus,
                followerCount = state.followers.size,
                followingCount = state.following.size,
                recipeCount = 0, // TODO: Get from recipes
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings,
                onEditProfile = onEditProfile,
                onFollowToggle = onFollowToggle,
                onViewFollowers = onViewFollowers,
                onViewFollowing = onViewFollowing
            )
        }

        // Sticky tabs
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Tab content
        when (selectedTabIndex) {
            0 -> {
                // Posts tab
                if (state.isLoadingPosts) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.userPosts.isEmpty()) {
                    item {
                        EmptyState(
                            message = if (state.isCurrentUser) "You haven't posted anything yet" else "${user.username} hasn't posted anything yet",
                            icon = Icons.Default.RssFeed
                        )
                    }
                } else {
                    items(
                        items = state.userPosts,
                        key = { it.postId }
                    ) { post ->
                        // Convert PostListItem to simplified post display
                        ProfilePostItem(
                            post = post,
                            onClick = { onPostClick(post.postId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            1 -> {
                // Recipes tab
                if (state.isLoadingRecipes) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.userRecipes.isEmpty()) {
                    item {
                        EmptyState(
                            message = if (state.isCurrentUser) "You haven't created any recipes yet" else "${user.username} hasn't created any recipes yet",
                            icon = Icons.Default.Restaurant
                        )
                    }
                } else {
                    // Display recipes in a grid
                    val chunkedRecipes = state.userRecipes.chunked(2)
                    items(chunkedRecipes) { recipePair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recipePair.forEach { recipe ->
                                ProfileRecipeItem(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.recipeId) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if odd number of items
                            if (recipePair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            2 -> {
                // Cookbooks tab - TODO: Implement when cookbooks are ready
                item {
                    EmptyState(
                        message = if (state.isCurrentUser) "You haven't created any cookbooks yet" else "${user.username} hasn't created any cookbooks yet",
                        icon = Icons.Default.MenuBook
                    )
                }
            }
        }
    }
}

// Updated ProfileHeader component in ProfileScreen.kt

@Composable
fun ProfileHeader(
    user: com.nhatpham.dishcover.domain.model.user.User,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    isUpdatingFollowStatus: Boolean,
    followerCount: Int,
    followingCount: Int,
    recipeCount: Int,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onFollowToggle: () -> Unit,
    onViewFollowers: () -> Unit,
    onViewFollowing: () -> Unit
) {
    Column {
        // Banner and profile picture section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Banner background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                getCategoryColor(user.username),
                                getCategoryColor(user.username).copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Top navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                if (isCurrentUser) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.3f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                } else {
                    Row {
                        IconButton(
                            onClick = { /* TODO: Share profile */ },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.3f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { /* TODO: More options */ },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.3f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }
                    }
                }
            }

            // Profile picture positioned to overlap banner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 8.dp)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profilePicture != null) {
                        AsyncImage(
                            model = user.profilePicture,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user.username.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Profile info section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Action buttons aligned to the right
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCurrentUser) {
                    OutlinedButton(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Edit profile")
                    }
                } else {
                    // Message button
                    OutlinedButton(
                        onClick = { /* TODO: Message */ },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Follow button using the new FollowButton component
                    FollowButton(
                        isFollowing = isFollowing,
                        isLoading = isUpdatingFollowStatus,
                        onToggleFollow = onFollowToggle,
                        size = FollowButtonSize.MEDIUM
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Username and verification
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Email (for current user only)
            if (isCurrentUser && user.email.isNotBlank()) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bio
            if (!user.bio.isNullOrBlank()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Join date
            Text(
                text = "Joined ${formatJoinDate(user.createdAt.toDate())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Following and followers with enhanced styling
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FollowStat(
                    count = followingCount,
                    label = "Following",
                    onClick = onViewFollowing
                )
                FollowStat(
                    count = followerCount,
                    label = if (followerCount == 1) "Follower" else "Followers",
                    onClick = onViewFollowers
                )
                FollowStat(
                    count = recipeCount,
                    label = if (recipeCount == 1) "Recipe" else "Recipes",
                    onClick = null
                )
            }
        }
    }
}

@Composable
fun FollowStat(
    count: Int,
    label: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .let {
                if (onClick != null) {
                    it.clickable { onClick() }
                } else it
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1000000 -> "${count / 1000}K"
        else -> "${count / 1000000}M"
    }
}

private fun formatJoinDate(date: java.util.Date): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date
    val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${monthNames[calendar.get(java.util.Calendar.MONTH)]} ${calendar.get(java.util.Calendar.YEAR)}"
}

@Composable
fun ProfilePostItem(
    post: PostListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post content preview
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Post image preview
            post.firstImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Post stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engagement stats
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (post.likeCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatCount(post.likeCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (post.commentCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatCount(post.commentCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Post date
                Text(
                    text = formatPostDate(post.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileRecipeItem(
    recipe: RecipeListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                if (recipe.coverImage != null) {
                    AsyncImage(
                        model = recipe.coverImage,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getCategoryColor(recipe.title)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                }

                // Recipe stats overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (recipe.likeCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = formatCount(recipe.likeCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Recipe info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.prepTime + recipe.cookTime} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.servings}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatPostDate(date: java.util.Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    return when {
        diff < 24 * 60 * 60 * 1000 -> {
            when {
                diff < 60 * 1000 -> "now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
                else -> "${diff / (60 * 60 * 1000)}h"
            }
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d"
        else -> {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = date
            "${calendar.get(java.util.Calendar.MONTH) + 1}/${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
        }
    }
}