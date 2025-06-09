package com.nhatpham.dishcover.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.PostListItem
import com.nhatpham.dishcover.domain.model.recipe.RecipeListItem
import com.nhatpham.dishcover.domain.model.user.User
import com.nhatpham.dishcover.presentation.components.LoadingIndicator
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
    onNavigateToPostDetail: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.onEvent(UserProfileEvent.LoadProfile(userId))
        } else {
            viewModel.loadCurrentUserProfile()
        }
    }

    when {
        state.isLoading -> {
            LoadingIndicator()
        }
        state.error != null -> {
            EmptyState(
                message = state.error ?: "Something went wrong",
                icon = Icons.Default.ErrorOutline
            )
        }
        state.user != null -> {
            ModernProfileContent(
                user = state.user!!,
                isCurrentUser = state.isCurrentUser,
                isFollowing = state.isFollowing,
                isUpdatingFollowStatus = state.isUpdatingFollowStatus,
                followers = state.followers,
                following = state.following,
                userPosts = state.userPosts,
                userRecipes = state.userRecipes,
                selectedTab = 0, // You can add this to state
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onNavigateToFollowers = { onNavigateToFollowers(state.user!!.userId) },
                onNavigateToFollowing = { onNavigateToFollowing(state.user!!.userId) },
                onNavigateToRecipe = onNavigateToRecipe,
                onNavigateToPostDetail = onNavigateToPostDetail,
                onFollowToggle = { viewModel.onEvent(UserProfileEvent.ToggleFollowStatus) },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun ModernProfileContent(
    user: User,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    isUpdatingFollowStatus: Boolean,
    followers: List<User>,
    following: List<User>,
    userPosts: List<PostListItem>,
    userRecipes: List<RecipeListItem>,
    selectedTab: Int,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToFollowers: () -> Unit,
    onNavigateToFollowing: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onFollowToggle: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentTab by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Modern Profile Header
        item {
            ModernProfileHeader(
                user = user,
                isCurrentUser = isCurrentUser,
                isFollowing = isFollowing,
                isUpdatingFollowStatus = isUpdatingFollowStatus,
                followerCount = followers.size,
                followingCount = following.size,
                recipeCount = userRecipes.size,
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings,
                onEditProfile = onNavigateToEditProfile,
                onFollowToggle = onFollowToggle,
                onViewFollowers = onNavigateToFollowers,
                onViewFollowing = onNavigateToFollowing
            )
        }

        // Profile Stats and Bio
        item {
            ProfileBioSection(
                user = user,
                followerCount = followers.size,
                followingCount = following.size,
                recipeCount = userRecipes.size,
                onViewFollowers = onNavigateToFollowers,
                onViewFollowing = onNavigateToFollowing
            )
        }

        // Action Buttons
        item {
            ProfileActionButtons(
                isCurrentUser = isCurrentUser,
                isFollowing = isFollowing,
                isUpdatingFollowStatus = isUpdatingFollowStatus,
                onEditProfile = onNavigateToEditProfile,
                onFollowToggle = onFollowToggle,
                onMessage = { /* TODO: Message functionality */ },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Content Tabs
        item {
            ModernContentTabs(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it },
                postCount = userPosts.size,
                recipeCount = userRecipes.size,
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        // Content Grid
        item {
            when (currentTab) {
                0 -> {
                    if (userPosts.isEmpty()) {
                        EmptyContentSection(
                            title = "No Posts Yet",
                            description = if (isCurrentUser) "Share your cooking journey!" else "${user.username} hasn't posted anything yet",
                            icon = Icons.Default.DynamicFeed
                        )
                    } else {
                        PostsGrid(
                            posts = userPosts,
                            onPostClick = onNavigateToPostDetail
                        )
                    }
                }
                1 -> {
                    if (userRecipes.isEmpty()) {
                        EmptyContentSection(
                            title = "No Recipes Yet",
                            description = if (isCurrentUser) "Start creating delicious recipes!" else "${user.username} hasn't shared any recipes yet",
                            icon = Icons.Default.MenuBook
                        )
                    } else {
                        RecipesGrid(
                            recipes = userRecipes,
                            onRecipeClick = onNavigateToRecipe
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernProfileHeader(
    user: User,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )

        // Header actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
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
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            } else {
                IconButton(
                    onClick = { /* Share profile */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                }
            }
        }

        // Profile Picture - Large and centered
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(120.dp)
                .background(
                    Color.White,
                    CircleShape
                )
                .padding(4.dp)
        ) {
            if (user.profilePicture != null) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.username.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileBioSection(
    user: User,
    followerCount: Int,
    followingCount: Int,
    recipeCount: Int,
    onViewFollowers: () -> Unit,
    onViewFollowing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Username and verification
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Verification badge or chef badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Chef",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(4.dp),
                    tint = Color.White
                )
            }
        }

        // Full name (if different from username)
//        if (user.fullName.isNotEmpty() && user.fullName != user.username) {
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = user.fullName,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }

        // Bio
        if (user.bio?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = user.bio!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

//        // Website
//        if (user.website?.isNotEmpty() == true) {
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = user.website!!,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.clickable { /* Open website */ }
//            )
//        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                count = recipeCount,
                label = "Recipes",
                onClick = { /* Focus on recipes tab */ }
            )
            StatItem(
                count = followerCount,
                label = "Followers",
                onClick = onViewFollowers
            )
            StatItem(
                count = followingCount,
                label = "Following",
                onClick = onViewFollowing
            )
        }
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileActionButtons(
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    isUpdatingFollowStatus: Boolean,
    onEditProfile: () -> Unit,
    onFollowToggle: () -> Unit,
    onMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isCurrentUser) {
            Button(
                onClick = onEditProfile,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Button(
                onClick = onFollowToggle,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUpdatingFollowStatus,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (isFollowing) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        Color.White
                    }
                )
            ) {
                if (isUpdatingFollowStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            OutlinedButton(
                onClick = onMessage,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Message",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ModernContentTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    postCount: Int,
    recipeCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf(
            "Posts" to postCount,
            "Recipes" to recipeCount
        )

        tabs.forEachIndexed { index, (title, count) ->
            ModernContentTab(
                title = title,
                count = count,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModernContentTab(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = "($count)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Composable
private fun PostsGrid(
    posts: List<PostListItem>,
    onPostClick: (String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(posts) { post ->
            PostGridItem(
                post = post,
                onClick = { onPostClick(post.postId) }
            )
        }
    }
}

@Composable
private fun RecipesGrid(
    recipes: List<RecipeListItem>,
    onRecipeClick: (String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        items(recipes) { recipe ->
            RecipeGridItem(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.recipeId) }
            )
        }
    }
}

@Composable
private fun PostGridItem(
    post: PostListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Post image or placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // TODO: Load post image if available
                Icon(
                    imageVector = Icons.Default.DynamicFeed,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Post content preview
            Text(
                text = post.content,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecipeGridItem(
    recipe: RecipeListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Recipe image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (recipe.coverImage!!.isNotEmpty()) {
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
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFF9800),
                                        Color(0xFFFF5722)
                                    )
                                )
                            ),
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
            }

            // Recipe info
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${recipe.cookTime}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (recipe.likeCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFE91E63)
                            )
                            Text(
                                text = recipe.likeCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyContentSection(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
