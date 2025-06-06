package com.nhatpham.dishcover.presentation.feed.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.Comment
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PostDetailCommentItem(
    comment: Comment,
    onLikeComment: (String) -> Unit,
    onReplyToComment: (String) -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isReply: Boolean = false
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .size(if (isReply) 32.dp else 40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clickable { onUserClick(comment.userId) },
                contentAlignment = Alignment.Center
            ) {
                // In a real app, you'd get the user's profile picture
                Text(
                    text = comment.username.take(1).uppercase(),
                    style = if (isReply) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Comment content
            Column(modifier = Modifier.weight(1f)) {
                // Comment bubble
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Username
                        Text(
                            text = comment.username,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Comment content
                        Text(
                            text = comment.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Comment actions and metadata
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Timestamp
                    Text(
                        text = formatCommentTime(comment.createdAt.toDate()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeComment(comment.commentId) }
                    ) {
                        Text(
                            text = "Like",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        if (comment.likeCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${comment.likeCount})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Reply button (only for top-level comments)
                    if (!isReply) {
                        Text(
                            text = "Reply",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onReplyToComment(comment.commentId) }
                        )
                    }

                    // Show reply count if has replies
                    if (comment.replyCount > 0 && !isReply) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /* TODO: Load replies */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${comment.replyCount} ${if (comment.replyCount == 1) "reply" else "replies"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Edited indicator
                    if (comment.isEdited) {
                        Text(
                            text = "• edited",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // More options button
            IconButton(
                onClick = { /* TODO: Show comment options */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Comment options",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // TODO: Add replies section here if this comment has replies
        // You would load and display replies with isReply = true
    }
}

private fun formatCommentTime(date: Date): String {
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