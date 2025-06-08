package com.nhatpham.dishcover.presentation.recipe.create.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nhatpham.dishcover.ui.theme.PrimaryColor

@Composable
fun BottomNavigationButtons(
    currentStep: Int,
    totalSteps: Int,
    canProceed: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedContent(
            targetState = currentStep > 1,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f)
        ) { showPrevious ->
            if (showPrevious) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PrimaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Text(
                        text = "Previous",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.fillMaxWidth())
            }
        }

        AnimatedContent(
            targetState = canProceed && !isLoading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f)
        ) { enabled ->
            Button(
                onClick = onNext,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                if (isLoading && currentStep == totalSteps) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (currentStep < totalSteps) "Continue" else "Finish",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}