package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import group12.budgetbuddi.ui.theme.BrandGreen

@Composable
fun CategorySelectorDialog(
    allCategories: List<String>,
    selectedCategories: Set<String>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelected by remember(selectedCategories) { mutableStateOf(selectedCategories.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Categories to Show",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = BrandGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Select All / Deselect All buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { tempSelected = allCategories.toSet() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandGreen
                        )
                    ) {
                        Text("Select All")
                    }

                    OutlinedButton(
                        onClick = { tempSelected = emptySet() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandGreen
                        )
                    ) {
                        Text("Deselect All")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider()

                // Category list with checkboxes
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(allCategories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = tempSelected.contains(category),
                                onCheckedChange = { isChecked ->
                                    tempSelected = if (isChecked) {
                                        tempSelected + category
                                    } else {
                                        tempSelected - category
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BrandGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = BrandGreen
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(tempSelected) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Apply (${tempSelected.size} selected)")
                    }
                }
            }
        }
    }
}