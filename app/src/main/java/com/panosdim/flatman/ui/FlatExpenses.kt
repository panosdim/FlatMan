package com.panosdim.flatman.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.flatman.FLAT
import com.panosdim.flatman.R
import com.panosdim.flatman.TRANSACTION
import com.panosdim.flatman.TRANSACTION_TYPE
import com.panosdim.flatman.TransactionActivity
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.utils.TransactionType
import com.panosdim.flatman.utils.formatDate
import com.panosdim.flatman.utils.moneyFormat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun FlatExpenses(flat: Flat) {
    val viewModel: MainViewModel = viewModel()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val expenses by viewModel.getExpenses(flat.id.toString())
        .collectAsStateWithLifecycle(initialValue = null)

    expenses?.let {
        Column(
            modifier = Modifier.padding(paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = paddingLarge, vertical = paddingLarge),
                state = listState
            ) {
                if (it.isNotEmpty()) {
                    it.iterator().forEachRemaining { expense ->
                        item {
                            ListItem(headlineContent = {
                                Text(
                                    text = expense.date.formatDate(),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }, supportingContent = {
                                if (expense.comment.isNotBlank()) {
                                    Text(expense.comment)
                                }
                            }, trailingContent = {
                                Text(
                                    text = moneyFormat(expense.amount),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }, modifier = Modifier.clickable {
                                val intent = Intent(context, TransactionActivity::class.java)
                                val flatJson = Json.encodeToString(flat)
                                val expenseJson = Json.encodeToString(expense)

                                intent.putExtra(FLAT, flatJson)
                                intent.putExtra(TRANSACTION_TYPE, TransactionType.EXPENSES)
                                intent.putExtra(TRANSACTION, expenseJson)
                                context.startActivity(intent)
                            })
                            HorizontalDivider()
                        }
                    }
                } else {
                    item {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = stringResource(id = R.string.no_expenses),
                                modifier = Modifier

                            )
                            Text(
                                text = stringResource(id = R.string.no_expenses)
                            )
                        }
                    }
                }
            }
        }
    } ?: run {
        ProgressBar()
    }
}