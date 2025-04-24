package com.panosdim.flatman.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.flatman.R
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Lessee
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.utils.FieldState
import com.panosdim.flatman.utils.currencyRegex
import com.panosdim.flatman.utils.deleteEvent
import com.panosdim.flatman.utils.insertEvent
import com.panosdim.flatman.utils.toEpochMilli
import com.panosdim.flatman.utils.toLocalDate
import com.panosdim.flatman.utils.updateEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFlatSheet(
    flat: Flat,
    bottomSheetState: SheetState
) {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val openDeleteDialog = remember { mutableStateOf(false) }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var isAddingLessee by remember {
        mutableStateOf(flat.lessee != null)
    }

    // Sheet content
    if (bottomSheetState.isVisible) {
        val address = remember {
            FieldState(flat.address) {
                if (it.isEmpty()) {
                    return@FieldState Pair(
                        true,
                        context.getString(R.string.address_error_empty)
                    )
                }
                return@FieldState Pair(false, "")
            }
        }

        val lesseeName = remember {
            FieldState(flat.lessee?.name ?: "") {
                if (it.isEmpty()) {
                    return@FieldState Pair(
                        true,
                        context.getString(R.string.lessee_name_error_empty)
                    )
                }
                return@FieldState Pair(false, "")
            }
        }

        val lesseeRent = remember {
            flat.lessee?.rent?.let { rent ->
                FieldState(rent.toString()) {
                    if (it.isEmpty()) {
                        return@FieldState Pair(
                            true,
                            context.getString(R.string.lessee_rent_error_empty)
                        )
                    }
                    return@FieldState Pair(false, "")
                }
            } ?: run {
                FieldState("") {
                    if (it.isEmpty()) {
                        return@FieldState Pair(
                            true,
                            context.getString(R.string.lessee_rent_error_empty)
                        )
                    }
                    return@FieldState Pair(false, "")
                }
            }

        }

        val dateStart by remember {
            mutableStateOf(
                flat.lessee?.start?.toLocalDate() ?: LocalDate.now()
            )
        }
        val datePickerStateStart =
            rememberDatePickerState(initialSelectedDateMillis = dateStart.toEpochMilli())

        val dateEnd by remember {
            mutableStateOf(
                flat.lessee?.end?.toLocalDate() ?: LocalDate.now().plusYears(1)
            )
        }
        val datePickerStateEnd =
            rememberDatePickerState(initialSelectedDateMillis = dateEnd.toEpochMilli())

        fun isFormValid(): Boolean {
            if (isAddingLessee) {
                if (address.hasError || lesseeName.hasError || lesseeRent.hasError) {
                    return false
                } else {
                    // Check if we change something in the object
                    flat.lessee?.let {
                        if (it.rent.toString() == lesseeRent.value &&
                            it.name == lesseeName.value &&
                            it.start.toLocalDate()
                                .isEqual(datePickerStateStart.selectedDateMillis?.toLocalDate()) &&
                            it.end.toLocalDate()
                                .isEqual(datePickerStateEnd.selectedDateMillis?.toLocalDate()) &&
                            flat.address == address.value
                        ) {
                            return false
                        }
                    }
                }
            } else {
                if (address.hasError) {
                    return false
                } else {
                    // Check if we change something in the object
                    if (flat.lessee != null) {
                        return true
                    }
                    return flat.address != address.value
                }
            }
            return true
        }

        if (openDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDeleteDialog.value = false
                },
                title = {
                    Text(text = stringResource(id = R.string.delete_flat_dialog_title))
                },
                text = {
                    Text(
                        stringResource(id = R.string.delete_flat_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                            isLoading = true

                            scope.launch {
                                viewModel.deleteFlat(flat)
                                    .collect {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false

                                            if (it) {
                                                flat.lessee?.eventID?.let {
                                                    deleteEvent(context, it)
                                                }
                                                Toast.makeText(
                                                    context,
                                                    R.string.delete_flat_result,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch { bottomSheetState.hide() }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    R.string.generic_error_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                }
            )
        }

        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column(
                Modifier.padding(paddingLarge),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge,
                    text = resources.getString(R.string.edit_flat)
                )

                OutlinedTextField(
                    value = address.value,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Words,
                    ),
                    singleLine = true,
                    isError = address.hasError,
                    supportingText = {
                        if (address.hasError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = address.errorMessage,
                                textAlign = TextAlign.End,
                            )
                        }
                    },
                    onValueChange = { address.value = it },
                    label = { Text(stringResource(id = R.string.address)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.is_rented))
                    Switch(
                        checked = isAddingLessee,
                        onCheckedChange = { isAddingLessee = it },
                    )
                }

                if (isAddingLessee) {
                    OutlinedTextField(
                        value = lesseeName.value,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Words,
                        ),
                        singleLine = true,
                        isError = lesseeName.hasError,
                        supportingText = {
                            if (lesseeName.hasError) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = lesseeName.errorMessage,
                                    textAlign = TextAlign.End,
                                )
                            }
                        },
                        onValueChange = { lesseeName.value = it },
                        label = { Text(stringResource(id = R.string.lessee_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lesseeRent.value,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.EuroSymbol,
                                contentDescription = "Euro Icon"
                            )
                        },
                        isError = lesseeRent.hasError,
                        supportingText = {
                            if (lesseeRent.hasError) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = lesseeRent.errorMessage,
                                    textAlign = TextAlign.End,
                                )
                            }
                        },
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex(currencyRegex))) {
                                lesseeRent.value = newValue
                            }
                        },
                        label = { Text(stringResource(id = R.string.rent)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedDatePicker(
                        modifier = Modifier.fillMaxWidth(),
                        state = datePickerStateStart,
                        label = stringResource(id = R.string.date_from)
                    )

                    OutlinedDatePicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = paddingLarge),
                        state = datePickerStateEnd, label = stringResource(id = R.string.date_until)
                    )
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = paddingLarge)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = paddingLarge)
                ) {
                    OutlinedButton(
                        onClick = { openDeleteDialog.value = true },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.delete))
                    }

                    Button(
                        enabled = isFormValid(),
                        onClick = {
                            isLoading = true

                            // Update flat object
                            flat.address = address.value
                            flat.lessee?.let {
                                if (!isAddingLessee) {
                                    flat.lessee = null
                                } else {
                                    it.name = lesseeName.value
                                    it.rent = lesseeRent.value.toFloat()
                                    it.start =
                                        datePickerStateStart.selectedDateMillis?.toLocalDate()
                                            .toString()
                                    it.end = datePickerStateEnd.selectedDateMillis?.toLocalDate()
                                        .toString()

                                    it.eventID?.let { eventId ->
                                        datePickerStateEnd.selectedDateMillis?.toLocalDate()
                                            ?.let { date ->
                                                val eventDate = date.minusMonths(1)
                                                updateEvent(
                                                    context,
                                                    eventId,
                                                    eventDate,
                                                    resources.getString(
                                                        R.string.rent_ends,
                                                        address.value
                                                    )
                                                )
                                            }
                                    } ?: kotlin.run {
                                        datePickerStateEnd.selectedDateMillis?.toLocalDate()
                                            ?.let { date ->
                                                val eventDate = date.minusMonths(1)
                                                it.eventID =
                                                    insertEvent(
                                                        context, eventDate, resources.getString(
                                                            R.string.rent_ends,
                                                            address.value
                                                        )
                                                    )
                                            }
                                    }
                                }
                            } ?: run {
                                if (isAddingLessee) {
                                    val eventDate =
                                        datePickerStateEnd.selectedDateMillis?.toLocalDate()
                                            ?.minusMonths(1)
                                    val lessee = Lessee(
                                        name = lesseeName.value,
                                        rent = lesseeRent.value.toFloat(),
                                        start = datePickerStateStart.selectedDateMillis?.toLocalDate()
                                            .toString(),
                                        end = datePickerStateEnd.selectedDateMillis?.toLocalDate()
                                            .toString(),
                                        eventID = eventDate?.let {
                                            insertEvent(
                                                context, it, resources.getString(
                                                    R.string.rent_ends,
                                                    address.value
                                                )
                                            )
                                        }
                                    )
                                    flat.lessee = lessee
                                }
                            }

                            scope.launch {
                                viewModel.updateFlat(flat)
                                    .collect {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false

                                            if (it) {
                                                Toast.makeText(
                                                    context,
                                                    R.string.update_flat_result,
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                scope.launch { bottomSheetState.hide() }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    R.string.generic_error_toast,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                            }
                        },
                    ) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.update))
                    }
                }
            }
        }
    }
}