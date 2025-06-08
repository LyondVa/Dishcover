// DifficultyIndicator.kt - Fixed @Composable annotation issues
package com.nhatpham.dishcover.presentation.component.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nhatpham.dishcover.domain.model.recipe.RecipeDifficulty

@Composable
fun DifficultyIndicator(
    difficulty: RecipeDifficulty,
    size: DifficultySize = DifficultySize.MEDIUM,
    showDetails: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = getDifficultyColors(difficulty)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    when {
        showDetails -> {
            DetailedDifficultyIndicator(
                difficulty = difficulty,
                colors = colors,
                modifier = modifier.then(clickableModifier)
            )
        }
        size == DifficultySize.COMPACT -> {
            CompactDifficultyIndicator(
                difficulty = difficulty,
                colors = colors,
                modifier = modifier.then(clickableModifier)
            )
        }
        else -> {
            StandardDifficultyIndicator(
                difficulty = difficulty,
                colors = colors,
                size = size,
                modifier = modifier.then(clickableModifier)
            )
        }
    }
}

@Composable
private fun StandardDifficultyIndicator(
    difficulty: RecipeDifficulty,
    colors: DifficultyColors,
    size: DifficultySize,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Difficulty dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(size.dotSize)
                        .clip(CircleShape)
                        .background(
                            if (index < difficulty.level) {
                                colors.activeColor
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }

        if (size != DifficultySize.SMALL) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = difficulty.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = size.textSizeSp.sp),
                color = colors.activeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CompactDifficultyIndicator(
    difficulty: RecipeDifficulty,
    colors: DifficultyColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.activeColor.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.activeColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = getDifficultyIcon(difficulty),
                contentDescription = null,
                tint = colors.activeColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = difficulty.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = colors.activeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailedDifficultyIndicator(
    difficulty: RecipeDifficulty,
    colors: DifficultyColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.activeColor.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            colors.activeColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = getDifficultyIcon(difficulty),
                    contentDescription = null,
                    tint = colors.activeColor,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = difficulty.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.activeColor
                    )

                    // Difficulty dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < difficulty.level) {
                                            colors.activeColor
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = difficulty.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Details grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DifficultyDetail(
                    icon = Icons.Default.Schedule,
                    label = "Time",
                    value = difficulty.estimatedTime,
                    modifier = Modifier.weight(1f)
                )

                DifficultyDetail(
                    icon = Icons.Default.Psychology,
                    label = "Skill",
                    value = difficulty.skillRequired,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DifficultyDetail(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DifficultySelector(
    selectedDifficulty: RecipeDifficulty,
    onDifficultySelected: (RecipeDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recipe Difficulty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RecipeDifficulty.entries.forEach { difficulty ->
            val colors = getDifficultyColors(difficulty)
            val isSelected = difficulty == selectedDifficulty

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .selectable(
                        selected = isSelected,
                        onClick = { onDifficultySelected(difficulty) }
                    ),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) {
                    colors.activeColor.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                },
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) {
                        colors.activeColor
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = getDifficultyIcon(difficulty),
                        contentDescription = null,
                        tint = colors.activeColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = difficulty.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.activeColor
                        )

                        Text(
                            text = difficulty.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Difficulty dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index < difficulty.level) {
                                            colors.activeColor
                                        } else {
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class DifficultySize(
    val dotSize: androidx.compose.ui.unit.Dp,
    val textSizeSp: Int
) {
    SMALL(4.dp, 10),
    MEDIUM(6.dp, 12),
    LARGE(8.dp, 14),
    COMPACT(0.dp, 10)
}

private data class DifficultyColors(
    val activeColor: Color,
    val backgroundColor: Color
)

private fun getDifficultyColors(difficulty: RecipeDifficulty): DifficultyColors {
    return when (difficulty) {
        RecipeDifficulty.BEGINNER -> DifficultyColors(
            activeColor = Color(0xFF4CAF50), // Green
            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
        RecipeDifficulty.EASY -> DifficultyColors(
            activeColor = Color(0xFF8BC34A), // Light Green
            backgroundColor = Color(0xFF8BC34A).copy(alpha = 0.1f)
        )
        RecipeDifficulty.INTERMEDIATE -> DifficultyColors(
            activeColor = Color(0xFFFF9800), // Orange
            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        )
        RecipeDifficulty.ADVANCED -> DifficultyColors(
            activeColor = Color(0xFFFF5722), // Deep Orange
            backgroundColor = Color(0xFFFF5722).copy(alpha = 0.1f)
        )
        RecipeDifficulty.EXPERT -> DifficultyColors(
            activeColor = Color(0xFFF44336), // Red
            backgroundColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
    }
}

private fun getDifficultyIcon(difficulty: RecipeDifficulty): ImageVector {
    return when (difficulty) {
        RecipeDifficulty.BEGINNER -> Icons.Default.School
        RecipeDifficulty.EASY -> Icons.Default.ThumbUp
        RecipeDifficulty.INTERMEDIATE -> Icons.Default.Build
        RecipeDifficulty.ADVANCED -> Icons.Default.Star
        RecipeDifficulty.EXPERT -> Icons.Default.EmojiEvents
    }
}