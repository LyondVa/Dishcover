// Enhanced PostItem.kt - Strictly grounded in existing domain models
package com.nhatpham.dishcover.presentation.feed.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.FeedItem
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
import com.nhatpham.dishcover.domain.model.feed.PostType
import com.nhatpham.dishcover.domain.model.realtime.LiveEngagementData
import com.nhatpham.dishcover.domain.model.realtime.RecentReaction
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostItem(
    feedItem: FeedItem,
    currentUserId: String,
    onLike: (String, Boolean) -> Unit,
    onComment: (String) -> Unit,
    onShare: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    realTimeEngagement: LiveEngagementData? = null,
    onPostViewed: (String) -> Unit = {}
) {
    val post = feedItem.post ?: return
    val author = feedItem.author

    // Use real-time engagement data when available, fallback to post data
    val likeCount = realTimeEngagement?.likeCount ?: post.likeCount
    val commentCount = realTimeEngagement?.commentCount ?: post.commentCount
    val shareCount = realTimeEngagement?.shareCount ?: post.shareCount
    val viewCount = realTimeEngagement?.viewCount ?: post.viewCount
    val activeViewers = realTimeEngagement?.activeViewers ?: 0
    val recentReactions = realTimeEngagement?.recentReactions ?: emptyList()

    // Track when post comes into view for analytics
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(post.postId, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onPostViewed(post.postId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Animation states for interactions
    var isLiked by remember { mutableStateOf(feedItem.isLikedByCurrentUser) }
    var localLikeCount by remember { mutableStateOf(likeCount) }

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clickable { onPostClick(post.postId) },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enhanced Header
            PostHeader(
                post = post,
                author = author,
                activeViewers = activeViewers,
                onUserClick = onUserClick,
                currentUserId = currentUserId,
                modifier = Modifier.padding(16.dp)
            )

            // Post content with improved typography
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Post type indicator if not text
            if (post.postType != PostType.TEXT) {
                PostTypeIndicator(
                    postType = post.postType,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Media content based on post type
            PostMediaContent(
                imageUrls = post.imageUrls,
                videoUrl = post.videoUrl,
                postType = post.postType
            )

            // Recipe references (using actual domain model fields)
            if (post.recipeReferences.isNotEmpty()) {
                RecipeReferencesSection(
                    references = post.recipeReferences,
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hashtags display
            if (post.hashtags.isNotEmpty()) {
                HashtagsSection(
                    hashtags = post.hashtags,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Location display
            post.location?.let { location ->
                LocationSection(
                    location = location,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Real-time reactions overlay (only if available)
            AnimatedVisibility(
                visible = recentReactions.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                RecentReactionsFlow(
                    reactions = recentReactions,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Engagement stats (using actual domain fields)
//            if (localLikeCount > 0 || commentCount > 0 || viewCount > 0) {
//                EngagementStats(
//                    likeCount = localLikeCount,
//                    commentCount = commentCount,
//                    shareCount = shareCount,
//                    viewCount = viewCount,
//                    modifier = Modifier.padding(horizontal = 16.dp)
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )

            // Action buttons (respecting post settings)
            ActionButtons(
                post = post,
                isLiked = isLiked,
                likeCount = localLikeCount,
                commentCount = commentCount,
                shareCount = shareCount,
                onLike = { postId, liked ->
                    isLiked = liked
                    localLikeCount = if (liked) localLikeCount + 1 else localLikeCount - 1
                    onLike(postId, liked)
                },
                onComment = onComment,
                onShare = onShare,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun PostHeader(
    post: com.nhatpham.dishcover.domain.model.feed.Post,
    author: com.nhatpham.dishcover.domain.model.user.User?,
    activeViewers: Int,
    onUserClick: (String) -> Unit,
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable { onUserClick(post.userId) }
        ) {
            if (author?.profilePicture != null) {
                AsyncImage(
                    model = author.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback avatar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.username.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // User info and timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatTimeAgo(post.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Show if post is edited
                if (post.isEdited) {
                    Text(
                        text = "• edited",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Show if post is pinned
                if (post.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Privacy indicator
                if (!post.isPublic) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Active viewers (real-time feature)
                if (activeViewers > 0) {
                    Text(
                        text = "• $activeViewers watching",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // More options button
        if (currentUserId == post.userId) {
            IconButton(
                onClick = {  },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PostTypeIndicator(
    postType: PostType,
    modifier: Modifier = Modifier
) {
    val (icon, text, color) = when (postType) {
        PostType.RECIPE_SHARE -> Triple(Icons.Default.MenuBook, "Shared a recipe", Color(0xFF4CAF50))
        PostType.COOKBOOK_SHARE -> Triple(Icons.Default.Book, "Shared a cookbook", Color(0xFF2196F3))
        PostType.COOKING_PROGRESS -> Triple(Icons.Default.DinnerDining, "Cooking update", Color(0xFFFF9800))
        PostType.REVIEW -> Triple(Icons.Default.Star, "Recipe review", Color(0xFFE91E63))
        else -> return
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PostMediaContent(
    imageUrls: List<String>,
    videoUrl: String?,
    postType: PostType
) {
    when {
        videoUrl != null -> {
            // Video content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Video Player Placeholder",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play video",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        imageUrls.isNotEmpty() -> {
            // Image gallery
            PostImageGallery(imageUrls = imageUrls)
        }
    }
}

@Composable
private fun PostImageGallery(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    when (imageUrls.size) {
        1 -> {
            AsyncImage(
                model = imageUrls[0],
                contentDescription = "Post image",
                modifier = modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                imageUrls.forEach { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Post image",
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        else -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // First image
                AsyncImage(
                    model = imageUrls[0],
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Remaining images
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    imageUrls.drop(1).take(2).forEachIndexed { index, imageUrl ->
                        Box(modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Post image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            // Show +X more on last image
                            if (index == 1 && imageUrls.size > 3) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${imageUrls.size - 3}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeReferencesSection(
    references: List<PostRecipeReference>,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Featured Recipe${if (references.size > 1) "s" else ""}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(references) { reference ->
                RecipeReferenceCard(
                    reference = reference,
                    onRecipeClick = onRecipeClick,
                    modifier = Modifier.width(220.dp)
                )
            }
        }
    }
}

@Composable
fun RecipeReferenceCard(
    reference: PostRecipeReference,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onRecipeClick(reference.recipeId) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Recipe icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Recipe info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reference.displayText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Tap to view recipe",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chevron arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View recipe",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HashtagsSection(
    hashtags: List<String>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(hashtags) { hashtag ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "#$hashtag",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LocationSection(
    location: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentReactionsFlow(
    reactions: List<RecentReaction>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reactions.take(5)) { reaction ->
            ReactionBubble(reaction = reaction)
        }
    }
}

@Composable
private fun ReactionBubble(reaction: RecentReaction) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = getReactionEmoji(reaction.reactionType),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = reaction.username,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EngagementStats(
    likeCount: Int,
    commentCount: Int,
    shareCount: Int,
    viewCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (likeCount > 0) {
            EngagementStat(
                icon = Icons.Default.Favorite,
                count = likeCount,
                label = "like${if (likeCount != 1) "s" else ""}",
                color = Color(0xFFE91E63)
            )
        }

        if (commentCount > 0) {
            EngagementStat(
                icon = Icons.Default.ChatBubble,
                count = commentCount,
                label = "comment${if (commentCount != 1) "s" else ""}",
                color = Color(0xFF2196F3)
            )
        }

        if (shareCount > 0) {
            EngagementStat(
                icon = Icons.Default.Share,
                count = shareCount,
                label = "share${if (shareCount != 1) "s" else ""}",
                color = Color(0xFF4CAF50)
            )
        }

        if (viewCount > 0) {
            EngagementStat(
                icon = Icons.Default.Visibility,
                count = viewCount,
                label = "view${if (viewCount != 1) "s" else ""}",
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun EngagementStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtons(
    post: com.nhatpham.dishcover.domain.model.feed.Post,
    isLiked: Boolean,
    likeCount: Int,
    commentCount: Int,
    shareCount: Int,
    onLike: (String, Boolean) -> Unit,
    onComment: (String) -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Like button
        AnimatedActionButton(
            icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label = formatCount(likeCount),
            isActive = isLiked,
            activeColor = Color(0xFFE91E63),
            onClick = { onLike(post.postId, !isLiked) }
        )

        // Comment button (respect allowComments setting)
        if (post.allowComments) {
            AnimatedActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                label = formatCount(commentCount),
                isActive = false,
                activeColor = Color(0xFF2196F3),
                onClick = { onComment(post.postId) }
            )
        }

        // Share button (respect allowShares setting)
        if (post.allowShares) {
            AnimatedActionButton(
                icon = Icons.Default.Share,
                label = formatCount(shareCount),
                isActive = false,
                activeColor = Color(0xFF4CAF50),
                onClick = { onShare(post.postId) }
            )
        }

        // Bookmark button
        AnimatedActionButton(
            icon = Icons.Default.BookmarkBorder,
            label = "",
            isActive = false,
            activeColor = Color(0xFFFF9800),
            onClick = { /* TODO: Implement bookmark */ }
        )
    }
}

@Composable
private fun AnimatedActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    val tint by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "button_tint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                isPressed = true
                onClick()
            }
            .scale(scale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint
        )

        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = tint,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

// Helper functions
private fun formatTimeAgo(date: Date): String {
    val now = System.currentTimeMillis()
    val time = date.time
    val diff = now - time

    return when {
        diff < 60000 -> "now"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> {
            val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
            formatter.format(date)
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> "${count / 1000}.${(count % 1000) / 100}k"
        count < 1000000 -> "${count / 1000}k"
        else -> "${count / 1000000}.${(count % 1000000) / 100000}M"
    }
}

private fun getReactionEmoji(reactionType: com.nhatpham.dishcover.domain.model.realtime.ReactionType): String {
    return when (reactionType) {
        com.nhatpham.dishcover.domain.model.realtime.ReactionType.LIKE -> "❤️"
        com.nhatpham.dishcover.domain.model.realtime.ReactionType.LOVE -> "😍"
        com.nhatpham.dishcover.domain.model.realtime.ReactionType.LAUGH -> "😂"
        com.nhatpham.dishcover.domain.model.realtime.ReactionType.WOW -> "😮"
        com.nhatpham.dishcover.domain.model.realtime.ReactionType.SAD -> "😢"
        com.nhatpham.dishcover.domain.model.realtime.ReactionType.ANGRY -> "😡"
    }
}