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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Select All / Deselect All buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { tempSelected = allCategories.toSet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Select All")
                    }

                    TextButton(
                        onClick = { tempSelected = emptySet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deselect All")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Divider()

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
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Divider()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(tempSelected) }
                    ) {
                        Text("Apply (${tempSelected.size} selected)")
                    }
                }
            }
        }
    }
}