// CreatePostCookbookSection.kt
package com.nhatpham.dishcover.presentation.feed.create.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.domain.model.cookbook.CookbookListItem

@Composable
fun CreatePostCookbookSection(
    selectedCookbooks: List<CookbookListItem>,
    onAddCookbook: () -> Unit,
    onRemoveCookbook: (CookbookListItem) -> Unit,
    onCookbookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header with add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Linked Cookbooks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            if (selectedCookbooks.size < 3) { // Limit to 3 cookbooks
                TextButton(
                    onClick = onAddCookbook,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Cookbook")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected cookbooks or empty state
        if (selectedCookbooks.isEmpty()) {
            EmptyCookbookState(onAddCookbook = onAddCookbook)
        } else {
            SelectedCookbooksList(
                cookbooks = selectedCookbooks,
                onRemoveCookbook = onRemoveCookbook,
                onCookbookClick = onCookbookClick
            )
        }
    }
}

@Composable
private fun EmptyCookbookState(
    onAddCookbook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddCookbook() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Link a cookbook to your post",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap to browse your cookbooks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SelectedCookbooksList(
    cookbooks: List<CookbookListItem>,
    onRemoveCookbook: (CookbookListItem) -> Unit,
    onCookbookClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(cookbooks) { cookbook ->
            CookbookWidget(
                cookbook = cookbook,
                onCookbookClick = onCookbookClick,
                onRemove = { onRemoveCookbook(cookbook) },
                isCompact = true
            )
        }
    }
}

@Composable
private fun CookbookWidget(
    cookbook: CookbookListItem,
    onCookbookClick: (String) -> Unit,
    onRemove: () -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(if (isCompact) 140.dp else 200.dp)
            .clickable { onCookbookClick(cookbook.cookbookId) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Cookbook cover and remove button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isCompact) 80.dp else 100.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                // TODO: Add cookbook cover image when available
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier
                        .size(if (isCompact) 24.dp else 32.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Remove button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove cookbook",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cookbook title
            Text(
                text = cookbook.title,
                style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            if (!isCompact) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${cookbook.recipeCount} recipes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Cookbook selection button component
@Composable
fun CookbookSelectionButton(
    selectedCount: Int,
    onAddCookbook: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onAddCookbook,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (selectedCount > 0) {
                "Cookbooks ($selectedCount)"
            } else {
                "Link Cookbook"
            }
        )
    }
}

// Compact cookbook display for post creation toolbar
@Composable
fun CompactCookbookIndicator(
    cookbookCount: Int,
    onManageCookbooks: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cookbookCount > 0) {
        Surface(
            modifier = modifier
                .clickable { onManageCookbooks() }
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "$cookbookCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}