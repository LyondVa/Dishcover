package com.nhatpham.dishcover.presentation.feed.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.domain.model.feed.Post

@Composable
fun PostDetailActions(
    post: Post,
    isLikedByCurrentUser: Boolean,
    isSharedByCurrentUser: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Like button
        PostActionButton(
            icon = if (isLikedByCurrentUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            text = "Like",
            isActive = isLikedByCurrentUser,
            onClick = onLike,
            modifier = Modifier.weight(1f)
        )

        // Comment button
        PostActionButton(
            icon = Icons.Outlined.ChatBubbleOutline,
            text = "Comment",
            isActive = false,
            onClick = onComment,
            modifier = Modifier.weight(1f)
        )

        // Share button
        PostActionButton(
            icon = if (isSharedByCurrentUser) Icons.Filled.Share else Icons.Outlined.Share,
            text = "Share",
            isActive = isSharedByCurrentUser,
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PostActionButton(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp),
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}