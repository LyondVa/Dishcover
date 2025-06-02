package com.nhatpham.dishcover.presentation.recipe.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeMetadataEditor(
    prepTime: String,
    onPrepTimeChanged: (String) -> Unit,
    cookTime: String,
    onCookTimeChanged: (String) -> Unit,
    servings: String,
    onServingsChanged: (String) -> Unit,
    difficultyLevel: String,
    onDifficultyLevelChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Recipe Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Prep time and cook time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = prepTime,
                onValueChange = onPrepTimeChanged,
                label = { Text("Prep Time (mins)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
//                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
//                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
//                )
            )

            OutlinedTextField(
                value = cookTime,
                onValueChange = onCookTimeChanged,
                label = { Text("Cook Time (mins)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
//                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
//                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
//                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Servings and difficulty
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = servings,
                onValueChange = onServingsChanged,
                label = { Text("Servings") },
                modifier = Modifier.weight(1f),
                singleLine = true,
//                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
//                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
//                )
            )

            // Difficulty level dropdown
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                val difficulties = listOf("Easy", "Medium", "Hard")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = difficultyLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Difficulty") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        difficulties.forEach { difficulty ->
                            DropdownMenuItem(
                                text = { Text(text = difficulty) },
                                onClick = {
                                    onDifficultyLevelChanged(difficulty)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}