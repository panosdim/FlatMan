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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
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
import com.panosdim.flatman.models.Transaction
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.utils.FieldState
import com.panosdim.flatman.utils.TransactionType
import com.panosdim.flatman.utils.currencyRegex
import com.panosdim.flatman.utils.toEpochMilli
import com.panosdim.flatman.utils.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    flat: Flat,
    transactionType: TransactionType,
    transaction: Transaction,
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

    // Sheet content
    if (bottomSheetState.isVisible) {
        val date by remember { mutableStateOf(transaction.date.toLocalDate()) }
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = date.toEpochMilli())

        val comment = remember {
            FieldState(transaction.comment) {
                if (transactionType == TransactionType.RENTS && it.isEmpty()) {
                    return@FieldState Pair(
                        true,
                        context.getString(R.string.comment_error_empty)
                    )
                }
                return@FieldState Pair(false, "")
            }
        }

        val amount = remember {
            FieldState(transaction.amount.toString()) {
                if (it.isEmpty()) {
                    return@FieldState Pair(
                        true,
                        context.getString(R.string.amount_error_empty)
                    )
                }
                return@FieldState Pair(false, "")
            }
        }

        fun isFormValid(): Boolean {
            if (amount.hasError || comment.hasError) {
                return false
            } else {
                // Check if we change something in the object
                if (transaction.amount.toString() == amount.value &&
                    transaction.comment == comment.value &&
                    transaction.date.toLocalDate()
                        .isEqual(datePickerState.selectedDateMillis?.toLocalDate())
                ) {
                    return false
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
                    if (transactionType == TransactionType.EXPENSES) {
                        Text(text = stringResource(id = R.string.delete_expense_dialog_title))
                    } else {
                        Text(text = stringResource(id = R.string.delete_rent_dialog_title))
                    }
                },
                text = {
                    Text(
                        stringResource(id = R.string.delete_transaction_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                            isLoading = true

                            scope.launch {
                                flat.id?.let {
                                    viewModel.deleteTransaction(it, transaction, transactionType)
                                        .collect {
                                            withContext(Dispatchers.Main) {
                                                if (it) {
                                                    if (transactionType == TransactionType.EXPENSES) {
                                                        Toast.makeText(
                                                            context,
                                                            R.string.delete_expense_result,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            R.string.delete_rent_result,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }

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
            if (isLoading) {
                ProgressBar()
            }

            Column(
                Modifier.padding(paddingLarge),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (transactionType) {
                    TransactionType.RENTS -> {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineLarge,
                            text = resources.getString(R.string.edit_rent)
                        )
                    }

                    TransactionType.EXPENSES -> {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineLarge,
                            text = resources.getString(R.string.edit_expense)
                        )
                    }
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    text = flat.address
                )
                
                OutlinedTextField(
                    value = amount.value,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.EuroSymbol,
                            contentDescription = "Euro Icon"
                        )
                    },
                    isError = amount.hasError,
                    supportingText = {
                        if (amount.hasError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = amount.errorMessage,
                                textAlign = TextAlign.End,
                            )
                        }
                    },
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex(currencyRegex))) {
                            amount.value = newValue
                        }
                    },
                    label = { Text(stringResource(id = R.string.amount)) },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                OutlinedTextField(
                    value = comment.value,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    isError = comment.hasError,
                    supportingText = {
                        if (comment.hasError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = comment.errorMessage,
                                textAlign = TextAlign.End,
                            )
                        }
                    },
                    onValueChange = { comment.value = it },
                    label = { Text(stringResource(id = R.string.comment)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedDatePicker(
                    modifier = Modifier.fillMaxWidth(),
                    state = datePickerState, label = stringResource(id = R.string.date)
                )

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
                            datePickerState.selectedDateMillis?.toLocalDate()?.let {
                                isLoading = true

                                // Update transaction object
                                transaction.date = it.toString()
                                transaction.amount = amount.value.toFloat()
                                transaction.comment = comment.value

                                scope.launch {
                                    flat.id?.let { id ->
                                        viewModel.updateTransaction(
                                            id,
                                            transaction,
                                            transactionType
                                        )
                                            .collect {
                                                withContext(Dispatchers.Main) {
                                                    if (it) {
                                                        if (transactionType == TransactionType.EXPENSES) {
                                                            Toast.makeText(
                                                                context,
                                                                R.string.update_expense_result,
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                R.string.update_rent_result,
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }

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