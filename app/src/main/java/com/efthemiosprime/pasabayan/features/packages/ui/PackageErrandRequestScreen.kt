package com.efthemiosprime.pasabayan.features.packages.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.efthemiosprime.pasabayan.R
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanSpacing
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTextStyles
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PButton
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonSize
import com.efthemiosprime.pasabayan.core.designsystem.component.PButtonStyle
import com.efthemiosprime.pasabayan.core.designsystem.component.PExpandableSection
import com.efthemiosprime.pasabayan.core.designsystem.component.POutlinedTextField
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequestBaseScaffold
import com.efthemiosprime.pasabayan.features.packages.components.PackageRequirementChipUi
import com.efthemiosprime.pasabayan.features.packages.model.ServiceRequestShoppingItem
import com.efthemiosprime.pasabayan.features.packages.model.ServiceRequestSubmitPayload
import com.efthemiosprime.pasabayan.shared.model.CityCatalog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class ErrandServiceType(val code: String, val labelRes: Int) {
    GroceryShopping("grocery_shopping", R.string.packages_service_type_grocery_shopping),
    FoodDelivery("food_delivery", R.string.packages_service_type_food_delivery),
    PharmacyPickup("pharmacy_pickup", R.string.packages_service_type_pharmacy_pickup),
    GeneralErrand("general_errand", R.string.packages_service_type_general_errand),
}

private enum class ErrandDirection(val code: String, val labelRes: Int) {
    Receive("receive", R.string.packages_service_direction_receive),
    Send("send", R.string.packages_service_direction_send),
}

private data class ShoppingItemInput(
    val name: String = "",
    val quantity: String = "",
    val notes: String = "",
)

@Composable
fun PackageErrandRequestScreen(
    onSave: (ServiceRequestSubmitPayload) -> Unit,
    onCancel: () -> Unit,
    isSubmitting: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val serviceTypes = remember { ErrandServiceType.entries }
    val shoppingItems = remember { mutableStateListOf(ShoppingItemInput()) }

    var serviceType by remember { mutableStateOf(ErrandServiceType.GroceryShopping) }
    var direction by remember { mutableStateOf(ErrandDirection.Receive) }
    var deliveryAddress by remember { mutableStateOf("") }
    var deliveryCity by remember { mutableStateOf("") }
    var deliveryCitySuggestions by remember { mutableStateOf(emptyList<String>()) }
    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var estimatedCost by remember { mutableStateOf("") }
    var maxBudget by remember { mutableStateOf("") }
    var specialRequirements by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    val serviceTypeLabels = serviceTypes.associateWith { stringResource(it.labelRes) }

    val itemsComplete = shoppingItems.any { it.name.isNotBlank() && it.quantity.isNotBlank() }
    val addressComplete = deliveryAddress.isNotBlank() && deliveryCity.isNotBlank()
    val completedRequirements = listOf(itemsComplete, addressComplete).count { it }
    val requireRecipient = serviceType == ErrandServiceType.GeneralErrand && direction == ErrandDirection.Send
    val canSubmit = itemsComplete && addressComplete && (!requireRecipient || recipientName.isNotBlank())

    PackageRequestBaseScaffold(
        title = stringResource(R.string.packages_service_title),
        completedRequirements = completedRequirements,
        totalRequirements = 2,
        requirementChips = listOf(
            PackageRequirementChipUi(
                icon = Icons.Default.List,
                label = stringResource(R.string.packages_service_chip_items),
                isComplete = itemsComplete,
            ),
            PackageRequirementChipUi(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.packages_service_chip_handoff),
                isComplete = addressComplete,
            ),
        ),
        onClose = onCancel,
        closeContentDescription = stringResource(R.string.packages_service_close),
        requiredStepsLabel = stringResource(R.string.packages_service_required_steps),
        modifier = modifier,
        footer = {
            PButton(
                text = stringResource(R.string.packages_service_action_send_request),
                onClick = {
                    val requestItems = shoppingItems
                        .filter { it.name.isNotBlank() && it.quantity.isNotBlank() }
                        .map { item ->
                            ServiceRequestShoppingItem(
                                item = item.name,
                                quantity = item.quantity,
                                notes = item.notes,
                            )
                        }
                    onSave(
                        ServiceRequestSubmitPayload(
                            serviceTypeCode = serviceType.code,
                            shoppingItems = requestItems,
                            deliveryCity = deliveryCity,
                            deliveryAddress = deliveryAddress,
                            storeName = storeName,
                            storeAddress = storeAddress,
                            estimatedCost = estimatedCost.toDoubleOrNull(),
                            maxPriceBudget = maxBudget.toDoubleOrNull(),
                            deliveryDateNeeded = deliveryDate,
                            urgencyLevelCode = "normal",
                            directionCode = if (serviceType == ErrandServiceType.GeneralErrand) direction.code else null,
                            recipientName = if (requireRecipient) recipientName else null,
                            recipientPhone = if (requireRecipient) recipientPhone else null,
                        ),
                    )
                },
                style = PButtonStyle.Submit,
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier.padding(PasabayanSpacing.lg),
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.lg),
        ) {
            PExpandableSection(
                title = stringResource(R.string.packages_service_section_service_needed),
                initiallyExpanded = true,
            ) {
                PackageStringDropdownField(
                    label = stringResource(R.string.packages_service_field_service_type),
                    value = serviceTypeLabels.getValue(serviceType),
                    options = serviceTypeLabels.values.toList(),
                    onSelected = { selected ->
                        serviceType = serviceTypeLabels.entries.firstOrNull { it.value == selected }?.key
                            ?: ErrandServiceType.GroceryShopping
                    },
                )
                if (serviceType == ErrandServiceType.GeneralErrand) {
                    ErrandDirectionToggle(
                        direction = direction,
                        onDirectionChanged = { direction = it },
                    )
                }
            }

            PExpandableSection(
                title = stringResource(R.string.packages_service_section_items_list),
                initiallyExpanded = true,
            ) {
                shoppingItems.forEachIndexed { index, item ->
                    ShoppingItemEditor(
                        item = item,
                        onItemChanged = { updated -> shoppingItems[index] = updated },
                        onRemove = { if (shoppingItems.size > 1) shoppingItems.removeAt(index) },
                    )
                }
                PButton(
                    text = stringResource(R.string.packages_service_action_add_item),
                    onClick = { shoppingItems.add(ShoppingItemInput()) },
                    style = PButtonStyle.Secondary,
                    size = PButtonSize.Small,
                )
            }

            PExpandableSection(
                title = stringResource(R.string.packages_service_section_handoff_details),
                initiallyExpanded = true,
            ) {
                POutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text(stringResource(R.string.packages_service_field_delivery_address)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                POutlinedTextField(
                    value = deliveryCity,
                    onValueChange = {
                        deliveryCity = it
                        deliveryCitySuggestions = if (CityCatalog.containsInPopularCanada(it)) {
                            emptyList()
                        } else {
                            CityCatalog.popularCanadaSuggestions(it)
                        }
                    },
                    label = { Text(stringResource(R.string.packages_service_field_delivery_city)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (deliveryCitySuggestions.isNotEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = PasabayanSpacing.xs,
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            deliveryCitySuggestions.forEachIndexed { index, suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            deliveryCity = suggestion
                                            deliveryCitySuggestions = emptyList()
                                        }
                                        .padding(
                                            horizontal = PasabayanSpacing.md,
                                            vertical = PasabayanSpacing.sm,
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.width(PasabayanSpacing.sm))
                                    Text(
                                        text = suggestion,
                                        style = PasabayanTextStyles.Body.regular,
                                    )
                                }
                                if (index < deliveryCitySuggestions.lastIndex) {
                                    androidx.compose.material3.HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                if (requireRecipient) {
                    POutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text(stringResource(R.string.packages_service_field_recipient_name)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    POutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text(stringResource(R.string.packages_service_field_recipient_phone)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            PExpandableSection(
                title = stringResource(R.string.packages_service_section_optional_details),
            ) {
                if (serviceType != ErrandServiceType.GeneralErrand) {
                    POutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text(stringResource(R.string.packages_service_field_store_name)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    POutlinedTextField(
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        label = { Text(stringResource(R.string.packages_service_field_store_address)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    POutlinedTextField(
                        value = estimatedCost,
                        onValueChange = { estimatedCost = it },
                        label = { Text(stringResource(R.string.packages_service_field_estimated_total)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    POutlinedTextField(
                        value = maxBudget,
                        onValueChange = { maxBudget = it },
                        label = { Text(stringResource(R.string.packages_service_field_max_budget)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                DateSelectorField(
                    label = stringResource(R.string.packages_service_field_needed_by_date),
                    value = deliveryDate.format(errandDateFormatter),
                    onClick = {
                        showDatePicker(
                            context = context,
                            current = deliveryDate,
                            onSelected = { selected -> deliveryDate = selected },
                        )
                    },
                )
                POutlinedTextField(
                    value = specialRequirements,
                    onValueChange = { specialRequirements = it },
                    label = { Text(stringResource(R.string.packages_service_field_special_instructions)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                )
            }
        }
    }
}

@Composable
private fun ShoppingItemEditor(
    item: ShoppingItemInput,
    onItemChanged: (ShoppingItemInput) -> Unit,
    onRemove: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.packages_service_hint_optional),
                style = PasabayanTextStyles.Caption.regular,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable(onClick = onRemove),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = PasabayanSpacing.sm, vertical = PasabayanSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(PasabayanSpacing.xs))
                    Text(
                        text = stringResource(R.string.packages_service_action_remove_item),
                        style = PasabayanTextStyles.Caption.regular,
                    )
                }
            }
        }
        POutlinedTextField(
            value = item.name,
            onValueChange = { onItemChanged(item.copy(name = it)) },
            label = { Text(stringResource(R.string.packages_service_item_name)) },
            modifier = Modifier.fillMaxWidth(),
        )
        POutlinedTextField(
            value = item.quantity,
            onValueChange = { onItemChanged(item.copy(quantity = it)) },
            label = { Text(stringResource(R.string.packages_service_item_quantity)) },
            modifier = Modifier.fillMaxWidth(),
        )
        POutlinedTextField(
            value = item.notes,
            onValueChange = { onItemChanged(item.copy(notes = it)) },
            label = { Text(stringResource(R.string.packages_service_item_notes)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
        )
    }
}

@Composable
private fun ErrandDirectionToggle(
    direction: ErrandDirection,
    onDirectionChanged: (ErrandDirection) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        Text(
            text = stringResource(R.string.packages_service_direction_label),
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm),
        ) {
            DirectionOption(
                label = stringResource(ErrandDirection.Receive.labelRes),
                selected = direction == ErrandDirection.Receive,
                onClick = { onDirectionChanged(ErrandDirection.Receive) },
                modifier = Modifier.weight(1f),
            )
            DirectionOption(
                label = stringResource(ErrandDirection.Send.labelRes),
                selected = direction == ErrandDirection.Send,
                onClick = { onDirectionChanged(ErrandDirection.Send) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DirectionOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.sm),
        )
    }
}

@Composable
private fun DateSelectorField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PasabayanSpacing.sm)) {
        Text(
            text = label,
            style = PasabayanTextStyles.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PasabayanSpacing.md, vertical = PasabayanSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    style = PasabayanTextStyles.Body.medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun PackageStringDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        POutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}

private fun showDatePicker(
    context: Context,
    current: LocalDate,
    onSelected: (LocalDate) -> Unit,
) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        current.year,
        current.monthValue - 1,
        current.dayOfMonth,
    ).show()
}

private val errandDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@Preview(showBackground = true, name = "PackageErrandRequest - light", heightDp = 900)
@Preview(
    showBackground = true,
    name = "PackageErrandRequest - dark",
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PackageErrandRequestPreview() {
    PasabayanTheme {
        PackageErrandRequestScreen(
            onSave = { _ -> },
            onCancel = {},
        )
    }
}
