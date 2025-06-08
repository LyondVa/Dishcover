package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.ui.theme.PrimaryColor

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val stepLabels = listOf("Basic Info", "Ingredients", "Difficulty", "Tags", "Review")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isActive = stepNumber <= currentStep
                val isCompleted = stepNumber < currentStep

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            animateColorAsState(
                                targetValue = if (isActive) PrimaryColor else Color.Gray.copy(alpha = 0.3f)
                            ).value
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            color = if (isActive) Color.White else Color.Gray,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (index < totalSteps - 1) {
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                animateColorAsState(
                                    targetValue = if (stepNumber < currentStep) PrimaryColor else Color.Gray.copy(alpha = 0.3f)
                                ).value
                            )
                    )
                }
            }
        }

        // Step labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stepLabels.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index + 1 == currentStep) PrimaryColor else Color.Gray,
                    fontWeight = if (index + 1 == currentStep) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}