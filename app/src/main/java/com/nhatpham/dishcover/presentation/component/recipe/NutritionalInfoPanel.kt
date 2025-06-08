// NutritionalInfoPanel.kt
package com.nhatpham.dishcover.presentation.component.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.domain.model.recipe.NutritionalInfo

@Composable
fun NutritionalInfoPanel(
    nutritionalInfo: NutritionalInfo?,
    isLoading: Boolean,
    onRefreshNutrition: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Nutritional Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onRefreshNutrition,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh nutrition data"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    NutritionLoadingState()
                }
                nutritionalInfo != null -> {
                    NutritionContent(nutritionalInfo = nutritionalInfo)
                }
                else -> {
                    NutritionEmptyState(onCalculate = onRefreshNutrition)
                }
            }
        }
    }
}

@Composable
private fun NutritionContent(
    nutritionalInfo: NutritionalInfo
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Serving info
        if (nutritionalInfo.servingSize.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Per ${nutritionalInfo.servingSize}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (nutritionalInfo.isEstimated) {
                        Text(
                            text = "Estimated",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Main nutrition grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Calories (prominent)
            NutritionCard(
                title = "Calories",
                value = nutritionalInfo.calories.toString(),
                unit = "kcal",
                icon = Icons.Default.LocalFireDepartment,
                modifier = Modifier.weight(1f),
                isHighlight = true
            )

            // Protein
            NutritionCard(
                title = "Protein",
                value = String.format("%.1f", nutritionalInfo.protein),
                unit = "g",
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Carbs
            NutritionCard(
                title = "Carbs",
                value = String.format("%.1f", nutritionalInfo.carbohydrates),
                unit = "g",
                icon = Icons.Default.Grain,
                modifier = Modifier.weight(1f)
            )

            // Fat
            NutritionCard(
                title = "Fat",
                value = String.format("%.1f", nutritionalInfo.fat),
                unit = "g",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }

        // Additional nutrients (if available)
        if (nutritionalInfo.fiber > 0 || nutritionalInfo.sugar > 0 || nutritionalInfo.sodium > 0) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (nutritionalInfo.fiber > 0) {
                    NutritionCard(
                        title = "Fiber",
                        value = String.format("%.1f", nutritionalInfo.fiber),
                        unit = "g",
                        icon = Icons.Default.Eco,
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }

                if (nutritionalInfo.sugar > 0) {
                    NutritionCard(
                        title = "Sugar",
                        value = String.format("%.1f", nutritionalInfo.sugar),
                        unit = "g",
                        icon = Icons.Default.Cake,
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }

                if (nutritionalInfo.sodium > 0) {
                    NutritionCard(
                        title = "Sodium",
                        value = String.format("%.0f", nutritionalInfo.sodium),
                        unit = "mg",
                        icon = Icons.Default.Opacity,
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false,
    isCompact: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 8.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (isCompact) 16.dp else 20.dp),
                tint = if (isHighlight) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Text(
                text = title,
                style = if (isCompact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    style = if (isCompact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlight) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NutritionLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text(
            text = "Calculating nutrition information...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NutritionEmptyState(
    onCalculate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "No nutritional data available",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tap refresh to calculate nutritional information based on ingredients",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onCalculate,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calculate Nutrition")
        }
    }
}