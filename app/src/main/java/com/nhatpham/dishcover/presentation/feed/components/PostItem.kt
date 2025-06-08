// presentation/feed/components/PostItem.kt - Complete with Real-Time Features
package com.nhatpham.dishcover.presentation.feed.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.FeedItem
import com.nhatpham.dishcover.domain.model.feed.PostRecipeReference
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
    // NEW: Real-time engagement data
    realTimeEngagement: LiveEngagementData? = null,
    // NEW: Callback for when post comes into view
    onPostViewed: (String) -> Unit = {}
) {
    val post = feedItem.post ?: return
    val author = feedItem.author

    // Use real-time engagement data when available, fallback to post data
    val likeCount = realTimeEngagement?.likeCount ?: post.likeCount
    val commentCount = realTimeEngagement?.commentCount ?: post.commentCount
    val shareCount = realTimeEngagement?.shareCount ?: post.shareCount
    val viewCount = realTimeEngagement?.viewCount ?: 0
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

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clickable { onPostClick(post.postId) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with user info and real-time activity indicators
            PostHeader(
                userId = post.userId,
                username = post.username,
                userProfilePicture = author?.profilePicture,
                location = post.location,
                timestamp = post.createdAt.toDate(),
                activeViewers = activeViewers,
                onUserClick = onUserClick,
                modifier = Modifier.padding(16.dp)
            )

            // Post content (preview)
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Post images (preview)
            if (post.imageUrls.isNotEmpty()) {
                PostImagePreview(
                    imageUrls = post.imageUrls,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recipe references (if any)
            if (post.recipeReferences.isNotEmpty()) {
                Text(
                    text = "Linked Recipes",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(post.recipeReferences) { reference ->
                        PostRecipeReferenceCard(
                            reference = reference,
                            onRecipeClick = onRecipeClick,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Real-time recent reactions overlay
            if (recentReactions.isNotEmpty()) {
                RecentReactionsOverlay(
                    reactions = recentReactions,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Engagement stats with real-time updates
            if (likeCount > 0 || commentCount > 0 || viewCount > 0) {
                PostEngagementStats(
                    likeCount = likeCount,
                    commentCount = commentCount,
                    viewCount = viewCount,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Action buttons with real-time state
            PostActionButtons(
                postId = post.postId,
                isLiked = feedItem.isLikedByCurrentUser,
                likeCount = likeCount,
                commentCount = commentCount,
                shareCount = shareCount,
                onLike = onLike,
                onComment = onComment,
                onShare = onShare,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun PostHeader(
    userId: String,
    username: String,
    userProfilePicture: String?,
    location: String?,
    timestamp: Date,
    activeViewers: Int,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        AsyncImage(
            model = userProfilePicture,
            contentDescription = "$username's profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onUserClick(userId) },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // User info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onUserClick(userId) }
                )

                // Real-time active viewers indicator
                if (activeViewers > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ActiveViewersIndicator(count = activeViewers)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (location != null) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // More options
        IconButton(onClick = { /* Handle more options */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostImagePreview(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    when (imageUrls.size) {
        1 -> {
            // Single image
            AsyncImage(
                model = imageUrls[0],
                contentDescription = "Post image",
                modifier = modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            // Two images side by side
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                imageUrls.forEach { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Post image",
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .background(
                                Color.Gray.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        else -> {
            // Multiple images in grid
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // First row - main image
                AsyncImage(
                    model = imageUrls[0],
                    contentDescription = "Post image 1",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentScale = ContentScale.Crop
                )

                // Second row - remaining images
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1 until minOf(imageUrls.size, 4)) {
                        Box(modifier = Modifier.weight(1f)) {
                            AsyncImage(
                                model = imageUrls[i],
                                contentDescription = "Post image ${i + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(
                                        Color.Gray.copy(alpha = 0.1f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )

                            // Show "+X more" overlay on last image if there are more
                            if (i == 3 && imageUrls.size > 4) {
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
                                        text = "+${imageUrls.size - 4}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge,
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
private fun RecentReactionsOverlay(
    reactions: List<RecentReaction>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Recent activity:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        reactions.take(3).forEach { reaction ->
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(24.dp)
            ) {
                AsyncImage(
                    model = reaction.profilePicture,
                    contentDescription = "${reaction.username} reacted",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (reactions.size > 3) {
            Text(
                text = "+${reactions.size - 3} more",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActiveViewersIndicator(
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PostEngagementStats(
    likeCount: Int,
    commentCount: Int,
    viewCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (likeCount > 0) {
            Text(
                text = "$likeCount ${if (likeCount == 1) "like" else "likes"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (commentCount > 0) {
            Text(
                text = "$commentCount ${if (commentCount == 1) "comment" else "comments"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (viewCount > 0) {
            Text(
                text = "$viewCount ${if (viewCount == 1) "view" else "views"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostActionButtons(
    postId: String,
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
        // Like button with animation potential
        PostActionButton(
            icon = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
            text = if (likeCount > 0) likeCount.toString() else "Like",
            isActive = isLiked,
            activeColor = Color(0xFFE91E63),
            onClick = { onLike(postId, !isLiked) }
        )

        // Comment button
        PostActionButton(
            icon = Icons.Outlined.ChatBubbleOutline,
            text = if (commentCount > 0) commentCount.toString() else "Comment",
            isActive = false,
            onClick = { onComment(postId) }
        )

        // Share button
        PostActionButton(
            icon = Icons.Outlined.Share,
            text = if (shareCount > 0) shareCount.toString() else "Share",
            isActive = false,
            onClick = { onShare(postId) }
        )
    }
}

@Composable
private fun PostActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isActive: Boolean,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
    }
}

private fun formatTimestamp(date: Date): String {
    val now = System.currentTimeMillis()
    val time = date.time
    val diff = now - time

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}

@Composable
private fun PostRecipeReferenceCard(
    reference: PostRecipeReference,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onRecipeClick(reference.recipeId) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recipe icon
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Recipe reference text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reference.displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tap to view recipe",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}