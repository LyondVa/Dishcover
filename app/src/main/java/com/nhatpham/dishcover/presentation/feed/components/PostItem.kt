// PostItem.kt
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

@Composable
fun PostItem(
    feedItem: FeedItem,
    currentUserId: String,
    realTimeEngagement: Any? = null,
    onLike: (String, Boolean) -> Unit,
    onComment: (String) -> Unit,
    onShare: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onCookbookClick: (String) -> Unit = {},
    onPostClick: (String) -> Unit,
    onPostViewed: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val post = feedItem.post ?: return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick(post.postId) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post header with user info
            PostHeader(
                author = feedItem.author,
                post = post,
                onUserClick = onUserClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Post images
            if (post.imageUrls.isNotEmpty()) {
                PostImageGallery(
                    imageUrls = post.imageUrls,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recipe references
            if (post.recipeReferences.isNotEmpty()) {
                PostRecipeReferences(
                    references = post.recipeReferences,
                    onRecipeClick = onRecipeClick
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Cookbook references
            if (post.cookbookReferences.isNotEmpty()) {
                PostCookbookReferences(
                    references = post.cookbookReferences,
                    onCookbookClick = onCookbookClick
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                PostHashtags(hashtags = post.hashtags)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Location
            post.location?.let { location ->
                PostLocation(location = location)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Post actions (like, comment, share)
            PostActions(
                post = post,
                isLiked = false, // TODO: Get from realTimeEngagement or post state
                onLike = { onLike(post.postId, !false) },
                onComment = { onComment(post.postId) },
                onShare = { onShare(post.postId) }
            )
        }
    }
}

@Composable
private fun PostHeader(
    author: com.nhatpham.dishcover.domain.model.user.User?,
    post: com.nhatpham.dishcover.domain.model.feed.Post,
    onUserClick: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // User avatar
        AsyncImage(
            model = author?.profilePicture,
            contentDescription = "User avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onUserClick(post.userId) },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = author?.username ?: post.username,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatTimeAgo(post.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // More options
        IconButton(onClick = { /* TODO: Show options menu */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // First row with main image
                AsyncImage(
                    model = imageUrls[0],
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Second row with remaining images
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imageUrls.drop(1).take(3)) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Post image",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (imageUrls.size > 4) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${imageUrls.size - 4}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostRecipeReferences(
    references: List<com.nhatpham.dishcover.domain.model.feed.PostRecipeReference>,
    onRecipeClick: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Recipe${if (references.size > 1) "s" else ""} referenced",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(references) { reference ->
                CompactReferenceCard(
                    title = reference.displayText,
                    icon = Icons.Default.MenuBook,
                    onClick = { onRecipeClick(reference.recipeId) }
                )
            }
        }
    }
}

@Composable
private fun PostCookbookReferences(
    references: List<com.nhatpham.dishcover.domain.model.feed.PostCookbookReference>,
    onCookbookClick: (String) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Cookbook${if (references.size > 1) "s" else ""} referenced",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(references) { reference ->
                CompactReferenceCard(
                    title = reference.displayText,
                    icon = Icons.Default.Book,
                    onClick = { onCookbookClick(reference.cookbookId) }
                )
            }
        }
    }
}

@Composable
private fun CompactReferenceCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostHashtags(hashtags: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(hashtags) { hashtag ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Text(
                    text = "#$hashtag",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PostLocation(location: String) {
    Row(
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
private fun PostActions(
    post: com.nhatpham.dishcover.domain.model.feed.Post,
    isLiked: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PostActionButton(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                count = post.likeCount,
                isActive = isLiked,
                onClick = onLike
            )
            PostActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                count = post.commentCount,
                onClick = onComment
            )
            PostActionButton(
                icon = Icons.Default.Share,
                count = post.shareCount,
                onClick = onShare
            )
        }

        // View count
        if (post.viewCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCount(post.viewCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PostActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (count > 0) {
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions
private fun formatTimeAgo(timestamp: com.google.firebase.Timestamp): String {
    val now = System.currentTimeMillis()
    val postTime = timestamp.toDate().time
    val diff = now - postTime

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        diff < 604_800_000 -> "${diff / 86_400_000}d"
        else -> "${diff / 604_800_000}w"
    }
}

private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1_000_000 -> "${count / 1000}k"
        else -> "${count / 1_000_000}m"
    }
}