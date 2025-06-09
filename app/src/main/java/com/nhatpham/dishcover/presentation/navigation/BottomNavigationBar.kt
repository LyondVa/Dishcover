// Enhanced BottomNavigationBar.kt - Balanced navigation design
package com.nhatpham.dishcover.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            BottomNavItem(
                icon = if (selectedRoute == "home_screen") Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = selectedRoute == "home_screen",
                onClick = onNavigateToHome
            )

            // Search
            BottomNavItem(
                icon = if (selectedRoute == "search_screen") Icons.Filled.Search else Icons.Outlined.Search,
                label = "Search",
                isSelected = selectedRoute == "search_screen",
                onClick = onNavigateToSearch
            )

            // Feed - Now balanced with other icons
            BottomNavItem(
                icon = if (selectedRoute == "feed_screen") Icons.Filled.DynamicFeed else Icons.Outlined.DynamicFeed,
                label = "Feed",
                isSelected = selectedRoute == "feed_screen",
                onClick = onNavigateToFeed
            )

            // Recipes
            BottomNavItem(
                icon = if (selectedRoute == "recipes_screen") Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                label = "Recipes",
                isSelected = selectedRoute == "recipes_screen",
                onClick = onNavigateToRecipes
            )

            // Profile
            BottomNavItem(
                icon = if (selectedRoute == "profile_screen") Icons.Filled.Person else Icons.Outlined.Person,
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
    val interactionSource = remember { MutableInteractionSource() }

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .then(
                    if (isSelected) {
                        Modifier.background(
                            animatedColor.copy(alpha = 0.1f),
                            CircleShape
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale),
                tint = animatedColor
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = animatedColor,
                fontSize = 11.sp
            )
        }
    }
}