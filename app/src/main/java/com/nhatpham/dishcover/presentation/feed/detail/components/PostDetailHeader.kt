package com.nhatpham.dishcover.presentation.feed.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.Post
import com.nhatpham.dishcover.domain.model.user.User
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostDetailHeader(
    post: Post,
    author: User?,
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
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable { onUserClick(post.userId) },
            contentAlignment = Alignment.Center
        ) {
            if (author?.profilePicture != null) {
                AsyncImage(
                    model = author.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = post.username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // User info and post metadata
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location if available
                post.location?.let { location ->
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
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

                // Timestamp
                Text(
                    text = formatDetailedTimestamp(post.createdAt.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Public/Private indicator
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (post.isPublic) Icons.Default.Public else Icons.Default.Lock,
                    contentDescription = if (post.isPublic) "Public post" else "Private post",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Edited indicator
                if (post.isEdited) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• edited",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Follow button (if viewing someone else's post)
        // This would need to be implemented based on follow status
        /*
        if (post.userId != currentUserId) {
            OutlinedButton(
                onClick = { /* Toggle follow */ },
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        */
    }
}

private fun formatDetailedTimestamp(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        else -> SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(date)
    }
}