// ServingSizeAdjuster.kt - Fixed OutlinedTextField issues
package com.nhatpham.dishcover.presentation.component.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ServingSizeAdjuster(
    currentServings: Int,
    originalServings: Int,
    onServingsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customValue by remember { mutableStateOf(currentServings.toString()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Servings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                if (currentServings != originalServings) {
                    TextButton(
                        onClick = { onServingsChanged(originalServings) }
                    ) {
                        Text("Reset")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Serving Size Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease Button
                IconButton(
                    onClick = {
                        if (currentServings > 1) {
                            onServingsChanged(currentServings - 1)
                        }
                    },
                    enabled = currentServings > 1,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease servings",
                        tint = if (currentServings > 1) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Current Servings Display/Input
                if (showCustomInput) {
                    OutlinedTextField(
                        value = customValue,
                        onValueChange = { customValue = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val newServings = customValue.toIntOrNull()
                                if (newServings != null && newServings > 0) {
                                    onServingsChanged(newServings)
                                }
                                showCustomInput = false
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.width(80.dp),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = currentServings.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .clickable {
                                showCustomInput = true
                                customValue = currentServings.toString()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Increase Button
                IconButton(
                    onClick = { onServingsChanged(currentServings + 1) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase servings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick Serving Options
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 4, 6, 8, 12).forEach { servings ->
                    if (servings != currentServings) {
                        FilterChip(
                            onClick = { onServingsChanged(servings) },
                            label = { Text(servings.toString()) },
                            selected = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Scaling Factor Display
            if (currentServings != originalServings) {
                Spacer(modifier = Modifier.height(8.dp))

                val scaleFactor = currentServings.toDouble() / originalServings.toDouble()
                Text(
                    text = "Ingredients scaled by ${String.format("%.1fx", scaleFactor)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}