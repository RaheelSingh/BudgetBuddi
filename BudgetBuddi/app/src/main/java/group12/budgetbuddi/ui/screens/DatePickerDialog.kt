/* Code Attribution
*
* JetBrains (2024). DatePickerDialog - Compose Multiplatform [Online].
* Available at: https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3/-date-picker-dialog.html
* (Accessed: 29 April 2026)
*/

package group12.budgetbuddi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Date

@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val selectedDateMillis = datePickerState.selectedDateMillis

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.Companion.padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )

                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.Companion.width(8.dp))

                    TextButton(
                        onClick = {
                            selectedDateMillis?.let {
                                onDateSelected(Date(it))
                            }
                            onDismiss()
                        },
                        enabled = selectedDateMillis != null
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}