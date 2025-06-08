package com.nhatpham.dishcover.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nhatpham.dishcover.domain.model.recipe.Recipe
import com.nhatpham.dishcover.util.ShareUtils

@Composable
fun RecipeShareDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val shareLink = ShareUtils.generateWebShareLink(recipe.recipeId)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Share Recipe",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Recipe preview
                RecipeLinkPreview(recipe = recipe)

                Spacer(modifier = Modifier.height(16.dp))

                // Share options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Copy link
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(shareLink))
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Copy Link",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Share via apps
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                onShare()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}