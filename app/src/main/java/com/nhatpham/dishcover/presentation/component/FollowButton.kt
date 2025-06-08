package com.nhatpham.dishcover.presentation.component

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
    size: FollowButtonSize = FollowButtonSize.MEDIUM
) {
    var isHovered by remember { mutableStateOf(false) }

    val buttonColors = if (isFollowing) {
        if (isHovered) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface
        )
    }

    val buttonText = when {
        isLoading -> ""
        isFollowing -> if (isHovered) "Unfollow" else "Following"
        else -> "Follow"
    }

    val buttonWidth = when (size) {
        FollowButtonSize.SMALL -> 80.dp
        FollowButtonSize.MEDIUM -> 100.dp
        FollowButtonSize.LARGE -> 120.dp
    }

    val buttonHeight = when (size) {
        FollowButtonSize.SMALL -> 32.dp
        FollowButtonSize.MEDIUM -> 36.dp
        FollowButtonSize.LARGE -> 40.dp
    }

    val iconSize = when (size) {
        FollowButtonSize.SMALL -> 14.dp
        FollowButtonSize.MEDIUM -> 16.dp
        FollowButtonSize.LARGE -> 18.dp
    }

    Button(
        onClick = onToggleFollow,
        enabled = !isLoading,
        modifier = modifier
            .width(buttonWidth)
            .height(buttonHeight)
            .let {
                if (isFollowing && !isHovered) {
                    it.border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(20.dp)
                    )
                } else it
            },
        colors = buttonColors,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        AnimatedContent(
            targetState = Triple(isLoading, isFollowing, isHovered),
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            label = "follow_button_content"
        ) { (loading, following, hovered) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(iconSize),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    following && hovered -> {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Unfollow",
                            style = when (size) {
                                FollowButtonSize.SMALL -> MaterialTheme.typography.labelSmall
                                FollowButtonSize.MEDIUM -> MaterialTheme.typography.labelMedium
                                FollowButtonSize.LARGE -> MaterialTheme.typography.labelLarge
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    following -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Following",
                            style = when (size) {
                                FollowButtonSize.SMALL -> MaterialTheme.typography.labelSmall
                                FollowButtonSize.MEDIUM -> MaterialTheme.typography.labelMedium
                                FollowButtonSize.LARGE -> MaterialTheme.typography.labelLarge
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Follow",
                            style = when (size) {
                                FollowButtonSize.SMALL -> MaterialTheme.typography.labelSmall
                                FollowButtonSize.MEDIUM -> MaterialTheme.typography.labelMedium
                                FollowButtonSize.LARGE -> MaterialTheme.typography.labelLarge
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

enum class FollowButtonSize {
    SMALL, MEDIUM, LARGE
}