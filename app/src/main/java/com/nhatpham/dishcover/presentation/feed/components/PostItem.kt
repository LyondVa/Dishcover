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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.FeedItem
import com.nhatpham.dishcover.domain.model.feed.PostType
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
    modifier: Modifier = Modifier
) {
    val post = feedItem.post ?: return

    Card(
        modifier = modifier.padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with user info - Fixed to use post.username directly
            PostHeader(
                userId = post.userId,
                username = post.username.takeIf { it.isNotBlank() } ?: "Unknown User",
                userProfilePicture = feedItem.author?.profilePicture,
                location = post.location,
                timestamp = post.createdAt.toDate(),
                onUserClick = onUserClick,
                modifier = Modifier.padding(16.dp)
            )

            // Post content
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Post images
            if (post.imageUrls.isNotEmpty()) {
                PostImages(
                    imageUrls = post.imageUrls,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recipe references (if any)
            if (post.recipeReferences.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(post.recipeReferences) { reference ->
                        RecipeReferenceChip(
                            reference = reference,
                            onClick = { onRecipeClick(reference.recipeId) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(post.hashtags) { hashtag ->
                        HashtagChip(hashtag = hashtag)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Interaction buttons and stats
            PostInteractionSection(
                postId = post.postId,
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                shareCount = post.shareCount,
                isLikedByCurrentUser = feedItem.isLikedByCurrentUser,
                isSharedByCurrentUser = feedItem.isSharedByCurrentUser,
                onLike = onLike,
                onComment = onComment,
                onShare = onShare,
                modifier = Modifier.padding(16.dp)
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
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable { onUserClick(userId) },
            contentAlignment = Alignment.Center
        ) {
            if (userProfilePicture != null) {
                AsyncImage(
                    model = userProfilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // User info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (location != null) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // More options button
        IconButton(onClick = { /* Show options menu */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostImages(
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
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            // Two images side by side
            Row(modifier = modifier) {
                imageUrls.forEachIndexed { index, url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Post image ${index + 1}",
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        3 -> {
            // Three images: one large, two small
            Row(modifier = modifier) {
                AsyncImage(
                    model = imageUrls[0],
                    contentDescription = "Post image 1",
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    imageUrls.drop(1).forEachIndexed { index, url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Post image ${index + 2}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        else -> {
            // Four or more images in a 2x2 grid
            Column(modifier = modifier) {
                for (rowIndex in 0 until 2) {
                    Row(modifier = Modifier.weight(1f)) {
                        for (colIndex in 0 until 2) {
                            val imageIndex = rowIndex * 2 + colIndex
                            if (imageIndex < imageUrls.size) {
                                Box(modifier = Modifier.weight(1f)) {
                                    AsyncImage(
                                        model = imageUrls[imageIndex],
                                        contentDescription = "Post image ${imageIndex + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .aspectRatio(1f),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Show "+X" overlay for additional images
                                    if (imageIndex == 3 && imageUrls.size > 4) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.6f)),
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
    }
}

@Composable
private fun RecipeReferenceChip(
    reference: com.nhatpham.dishcover.domain.model.feed.PostRecipeReference,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = reference.displayText,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
private fun HashtagChip(hashtag: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = "#$hashtag",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PostInteractionSection(
    postId: String,
    likeCount: Int,
    commentCount: Int,
    shareCount: Int,
    isLikedByCurrentUser: Boolean,
    isSharedByCurrentUser: Boolean,
    onLike: (String, Boolean) -> Unit,
    onComment: (String) -> Unit,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Interaction buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Like button
                InteractionButton(
                    icon = if (isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    count = likeCount,
                    isActive = isLikedByCurrentUser,
                    onClick = { onLike(postId, isLikedByCurrentUser) }
                )

                // Comment button
                InteractionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    count = commentCount,
                    isActive = false,
                    onClick = { onComment(postId) }
                )

                // Share button
                InteractionButton(
                    icon = if (isSharedByCurrentUser) Icons.Filled.Share else Icons.Outlined.Share,
                    count = shareCount,
                    isActive = isSharedByCurrentUser,
                    onClick = { onShare(postId) }
                )
            }
        }
    }
}

@Composable
private fun InteractionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    return when {
        diff < 60 * 1000 -> "now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}

private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1000000 -> "${count / 1000}k"
        else -> "${count / 1000000}m"
    }
}