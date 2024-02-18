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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.flatman.R
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Transaction
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.paddingSmall
import com.panosdim.flatman.utils.FieldState
import com.panosdim.flatman.utils.RentComment
import com.panosdim.flatman.utils.TransactionType
import com.panosdim.flatman.utils.currencyRegex
import com.panosdim.flatman.utils.next
import com.panosdim.flatman.utils.toEpochMilli
import com.panosdim.flatman.utils.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionForm(
    flat: Flat,
    transactionType: TransactionType
) {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val scope = CoroutineScope(Dispatchers.IO)
    val focusRequester = remember { FocusRequester() }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val date by remember { mutableStateOf(LocalDate.now()) }
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = date.toEpochMilli())

    val comment = remember {
        FieldState("") {
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
        FieldState("") {
            if (it.isEmpty()) {
                return@FieldState Pair(
                    true,
                    context.getString(R.string.amount_error_empty)
                )
            }
            return@FieldState Pair(false, "")
        }
    }

    LaunchedEffect(Unit) {
        if (amount.value.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    if (transactionType == TransactionType.RENTS) {
        val today = LocalDate.now()
        flat.lessee?.let {
            if (it.end.toLocalDate().isAfter(today)) {
                amount.value = it.rent.toString()
            }
        }

        val rent by viewModel.getLastRent(flat.id.toString())
            .collectAsStateWithLifecycle(initialValue = null)
        rent?.let {
            comment.value = RentComment.next(it.comment)
        }
    }

    fun isFormValid(): Boolean {
        return !(amount.hasError || comment.hasError)
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
            when (transactionType) {
                TransactionType.RENTS -> {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge,
                        text = resources.getString(R.string.add_rent)
                    )
                }

                TransactionType.EXPENSES -> {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge,
                        text = resources.getString(R.string.add_expense)
                    )
                }
            }
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
                    .focusRequester(focusRequester)
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
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingLarge)
            ) {
                Button(
                    enabled = isFormValid(),
                    onClick = {
                        datePickerState.selectedDateMillis?.toLocalDate()?.let {
                            isLoading = true

                            val newTransaction = Transaction(
                                id = null,
                                amount = amount.value.toFloat(),
                                date = it.toString(),
                                comment = comment.value
                            )

                            scope.launch {
                                flat.id?.let { id ->
                                    viewModel.addTransaction(id, newTransaction, transactionType)
                                        .collect {
                                            withContext(Dispatchers.Main) {
                                                if (it) {
                                                    if (transactionType == TransactionType.EXPENSES) {
                                                        Toast.makeText(
                                                            context,
                                                            R.string.add_expense_result,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            R.string.add_rent_result,
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