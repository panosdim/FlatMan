package com.panosdim.flatman.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.panosdim.flatman.paddingSmall
import com.panosdim.flatman.utils.FieldState
import com.panosdim.flatman.utils.currencyRegex
import com.panosdim.flatman.utils.insertEvent
import com.panosdim.flatman.utils.toEpochMilli
import com.panosdim.flatman.utils.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFlatForm() {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val scope = CoroutineScope(Dispatchers.IO)
    val focusRequester = remember { FocusRequester() }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var isAddingLessee by remember {
        mutableStateOf(false)
    }

    val address = remember {
        FieldState("") {
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
        FieldState("") {
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

    val dateFrom by remember { mutableStateOf(LocalDate.now()) }
    val datePickerStateFrom =
        rememberDatePickerState(initialSelectedDateMillis = dateFrom.toEpochMilli())

    val dateUntil by remember { mutableStateOf(LocalDate.now().plusYears(1)) }
    val datePickerStateUntil =
        rememberDatePickerState(initialSelectedDateMillis = dateUntil.toEpochMilli())

    LaunchedEffect(Unit) {
        if (address.value.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    fun isFormValid(): Boolean {
        return if (isAddingLessee) {
            !(address.hasError || lesseeName.hasError || lesseeRent.hasError)
        } else {
            !address.hasError
        }
    }

    if (isLoading) {
        ProgressBar()
    }

    Card(
        modifier = Modifier
            .padding(paddingSmall)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
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
                text = resources.getString(R.string.add_flat)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            if (isAddingLessee) {
                ElevatedButton(onClick = { isAddingLessee = false }) {
                    Text(stringResource(id = R.string.remove_lessee))
                }
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
                    state = datePickerStateFrom, label = stringResource(id = R.string.date_from)
                )

                OutlinedDatePicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = paddingLarge),
                    state = datePickerStateUntil, label = stringResource(id = R.string.date_until)
                )
            } else {
                ElevatedButton(onClick = { isAddingLessee = true }) {
                    Text(stringResource(id = R.string.add_lessee))
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingLarge)
            ) {
                Button(
                    enabled = isFormValid(),
                    onClick = {
                        isLoading = true

                        val newFlat = Flat(
                            id = null,
                            address = address.value,
                            lessee = null
                        )

                        if (isAddingLessee) {
                            val eventDate = datePickerStateUntil.selectedDateMillis?.toLocalDate()
                                ?.minusMonths(1)
                            newFlat.lessee = Lessee(
                                name = lesseeName.value,
                                rent = lesseeRent.value.toFloat(),
                                start = datePickerStateFrom.selectedDateMillis?.toLocalDate()
                                    .toString(),
                                end = datePickerStateUntil.selectedDateMillis?.toLocalDate()
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
                        }

                        scope.launch {
                            viewModel.addFlat(newFlat)
                                .collect {
                                    withContext(Dispatchers.Main) {
                                        if (it) {
                                            Toast.makeText(
                                                context,
                                                R.string.add_flat_result,
                                                Toast.LENGTH_LONG
                                            ).show()

                                            (context as? Activity)?.finish()
                                        } else {
                                            isLoading = false

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
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.add))
                }
            }
        }
    }
}