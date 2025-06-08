// RatingBar.kt - Fixed component issues
package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    onRatingChanged: ((Float) -> Unit)? = null,
    size: RatingSize = RatingSize.MEDIUM,
    showCount: Boolean = false,
    ratingCount: Int = 0
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(5) { index ->
                val isSelected = index < rating
                val isHalfSelected = index < rating && index >= rating - 0.5f

                Icon(imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star ${index + 1}",
                    tint = if (isSelected || isHalfSelected) {
                        Color(0xFFFFB000)
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier
                        .size(size.iconSize)
                        .then(if (onRatingChanged != null) {
                            Modifier.clickable {
                                onRatingChanged(index + 1f)
                            }
                        } else Modifier))
            }
        }

        if (showCount && ratingCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "($ratingCount)",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = size.textSizeSp.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RatingDisplay(
    averageRating: Double,
    totalRatings: Int,
    modifier: Modifier = Modifier,
    showDistribution: Boolean = false,
    ratingDistribution: Map<Int, Int> = emptyMap()
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Average rating display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", averageRating),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                RatingBar(
                    rating = averageRating.toFloat(), size = RatingSize.SMALL
                )
                Text(
                    text = "$totalRatings reviews",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Rating distribution
            if (showDistribution && ratingDistribution.isNotEmpty()) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    ratingDistribution.toSortedMap(compareByDescending { it })
                        .forEach { (stars, count) ->
                            RatingDistributionBar(
                                stars = stars,
                                count = count,
                                totalCount = totalRatings,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun RatingDistributionBar(
    stars: Int, count: Int, totalCount: Int, modifier: Modifier = Modifier
) {
    val percentage = if (totalCount > 0) count.toFloat() / totalCount else 0f

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "$stars",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(12.dp)
        )

        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFB000),
            modifier = Modifier.size(12.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Progress
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFB000))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )
    }
}

enum class RatingSize(
    val iconSize: Dp, val textSizeSp: Int
) {
    SMALL(12.dp, 10), MEDIUM(16.dp, 12), LARGE(20.dp, 14)
}

@Composable
fun InteractiveRatingBar(
    modifier: Modifier = Modifier,
    initialRating: Int = 0,
    onRatingSelected: (Int) -> Unit,
) {
    var currentRating by remember { mutableIntStateOf(initialRating) }
    var hoverRating by remember { mutableIntStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier
    ) {
        Text(
            text = "Rate this recipe",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) { index ->
                val starIndex = index + 1
                val isSelected = starIndex <= (if (hoverRating > 0) hoverRating else currentRating)

                Icon(imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $starIndex",
                    tint = if (isSelected) Color(0xFFFFB000) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            currentRating = starIndex
                            onRatingSelected(starIndex)
                        })
            }
        }

        if (currentRating > 0) {
            Text(
                text = when (currentRating) {
                    1 -> "Poor"
                    2 -> "Fair"
                    3 -> "Good"
                    4 -> "Very Good"
                    5 -> "Excellent"
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}