package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.ui.theme.PrimaryColor
import com.nhatpham.dishcover.ui.theme.PrimaryDarkColor

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedRoute == "home_screen",
                onClick = onNavigateToHome
            )

            // Search
            BottomNavItem(
                icon = Icons.Default.Search,
                label = "Search",
                isSelected = selectedRoute == "search_screen",
                onClick = onNavigateToSearch
            )

            // Central Feed Button (Red circular)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedRoute == "feed_screen") PrimaryDarkColor else PrimaryColor
                    )
                    .clickable { onNavigateToFeed() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RssFeed,
                    contentDescription = "Feed",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Recipes/Bookmarks
            BottomNavItem(
                icon = Icons.Default.MenuBook,
                label = "Recipes",
                isSelected = selectedRoute == "recipes_screen",
                onClick = onNavigateToRecipes
            )

            // Profile
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = selectedRoute == "profile_screen",
                onClick = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )

        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(
        selectedRoute = "home_screen",
        onNavigateToHome = {},
        onNavigateToSearch = {},
        onNavigateToFeed = {},
        onNavigateToRecipes = {},
        onNavigateToProfile = {}
    )
}