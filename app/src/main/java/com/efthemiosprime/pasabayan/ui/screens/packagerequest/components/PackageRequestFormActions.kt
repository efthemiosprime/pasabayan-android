package com.efthemiosprime.pasabayan.ui.screens.packagerequest.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.efthemiosprime.pasabayan.ui.theme.PasabayanTheme
import com.efthemiosprime.pasabayan.ui.shared.cards.PCardStandard

/**
 * Package Request Form Actions - Focused component for form submission
 * Following functional programming patterns with pure event handlers
 */
@Composable
fun PackageRequestFormActions(
    isFormValid: Boolean,
    isLoading: Boolean,
    onSubmit: () -> Unit,
    onClearForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    PCardStandard(
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Review & Submit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            if (!isFormValid && !isLoading) {
                Text(
                    text = "Please fill in all required fields (*) to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearForm,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear Form")
                }
                
                Button(
                    onClick = onSubmit,
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier.weight(2f)
                ) {
                    if (isLoading) {
                        Row {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating...")
                        }
                    } else {
                        Text("Create Package Request")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Form Actions - Valid")
@Composable
private fun PackageRequestFormActionsValidPreview() {
    PasabayanTheme {
        PackageRequestFormActions(
            isFormValid = true,
            isLoading = false,
            onSubmit = { },
            onClearForm = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Form Actions - Invalid")
@Composable
private fun PackageRequestFormActionsInvalidPreview() {
    PasabayanTheme {
        PackageRequestFormActions(
            isFormValid = false,
            isLoading = false,
            onSubmit = { },
            onClearForm = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Form Actions - Loading")
@Composable
private fun PackageRequestFormActionsLoadingPreview() {
    PasabayanTheme {
        PackageRequestFormActions(
            isFormValid = true,
            isLoading = true,
            onSubmit = { },
            onClearForm = { },
            modifier = Modifier.padding(16.dp)
        )
    }
} 