package com.nhatpham.dishcover.presentation.feed.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nhatpham.dishcover.domain.model.feed.Post

@Composable
fun PostDetailContent(
    post: Post,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Post text content with "See More" functionality
        if (post.content.isNotBlank()) {
            ExpandableText(
                text = post.content,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Post images
        if (post.imageUrls.isNotEmpty()) {
            PostDetailImages(
                imageUrls = post.imageUrls,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Video (if available)
        post.videoUrl?.let { videoUrl ->
            PostDetailVideo(
                videoUrl = videoUrl,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Recipe references
        if (post.recipeReferences.isNotEmpty()) {
            Text(
                text = "Referenced Recipes:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(post.recipeReferences) { reference ->
                    RecipeReferenceCard(
                        reference = reference,
                        onClick = { onRecipeClick(reference.recipeId) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Cookbook references
        if (post.cookbookReferences.isNotEmpty()) {
            Text(
                text = "Referenced Cookbooks:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(post.cookbookReferences) { reference ->
                    CookbookReferenceCard(
                        reference = reference,
                        onClick = { /* TODO: Navigate to cookbook */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Hashtags
        if (post.hashtags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(post.hashtags) { hashtag ->
                    HashtagChip(hashtag = hashtag)
                }
            }
        }
    }
}

@Composable
private fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 3
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showReadMoreButton by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                append(text)

                // Add hashtag styling
                val hashtagRegex = "#\\w+".toRegex()
                val matches = hashtagRegex.findAll(text)

                matches.forEach { match ->
                    addStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        ),
                        start = match.range.first,
                        end = match.range.last + 1
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                showReadMoreButton = textLayoutResult.hasVisualOverflow
            }
        )

        if (showReadMoreButton) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.padding(top = 4.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (isExpanded) "See less" else "See more",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PostDetailImages(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    when (imageUrls.size) {
        1 -> {
            // Single image - full width
            AsyncImage(
                model = imageUrls[0],
                contentDescription = "Post image",
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
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
                imageUrls.forEachIndexed { index, url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Post image ${index + 1}",
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
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
                        .aspectRatio(2f)
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
                                    .aspectRatio(1f)
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
private fun PostDetailVideo(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    // Placeholder for video component
    // In a real app, you'd use ExoPlayer or similar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(
                Color.Black,
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = "Play video",
            modifier = Modifier.size(64.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun RecipeReferenceCard(
    reference: com.nhatpham.dishcover.domain.model.feed.PostRecipeReference,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = reference.displayText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CookbookReferenceCard(
    reference: com.nhatpham.dishcover.domain.model.feed.PostCookbookReference,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = reference.displayText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HashtagChip(hashtag: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        modifier = Modifier.clickable { /* TODO: Navigate to hashtag */ }
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