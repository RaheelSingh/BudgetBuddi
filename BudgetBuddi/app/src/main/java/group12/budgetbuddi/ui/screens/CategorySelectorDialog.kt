//  Code Attribtions

/* Title: How to Use Null as Default Value in Compose MutableState
* Author: Stack Overflow Community
* Date Accessed: 29 April 2026
* Available: https://stackoverflow.com/questions/75425433/how-to-use-null-as-default-value-compose-mutablestate
*
* Title: Jetpack Compose Button Alignment
* Author: Stack Overflow Community
* Date Accessed: 29 April 2026
* Available: https://stackoverflow.com/questions/79085592/jetpack-compose-button-alignment
*
* Title: Compose Lists
* Author: Android Developers
* Date Accessed: 29 April 2026
* Available: https://developer.android.com/develop/ui/compose/lists
*/



package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import group12.budgetbuddi.ui.theme.BrandGreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


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
            modifier = Modifier.Companion
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp)
            ) {
                Text(
                    text = "Select Categories to Show",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Companion.Bold,
                    color = BrandGreen
                )

                Spacer(modifier = Modifier.Companion.height(8.dp))

                // Select All / Deselect All buttons
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { tempSelected = allCategories.toSet() },
                        modifier = Modifier.Companion.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandGreen
                        )
                    ) {
                        Text("Select All")
                    }

                    OutlinedButton(
                        onClick = { tempSelected = emptySet() },
                        modifier = Modifier.Companion.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandGreen
                        )
                    ) {
                        Text("Deselect All")
                    }
                }

                Spacer(modifier = Modifier.Companion.height(8.dp))

                HorizontalDivider()


                LazyColumn(
                    modifier = Modifier.Companion.weight(1f)
                ) {
                    items(allCategories) { category ->
                        Row(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Companion.CenterVertically
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
                            Spacer(modifier = Modifier.Companion.width(8.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.Companion.height(16.dp))


                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.Companion.width(8.dp))

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