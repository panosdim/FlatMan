package com.panosdim.flatman.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.flatman.R
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Response
import com.panosdim.flatman.models.Transaction
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.utils.TransactionType
import com.panosdim.flatman.utils.formatDate
import com.panosdim.flatman.utils.moneyFormat
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()
    val listState = rememberLazyListState()

    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val editTransactionSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    var selectedRent: Transaction by remember { mutableStateOf(Transaction()) }

    val flatsResponse by viewModel.getFlats()
        .collectAsStateWithLifecycle(initialValue = Response.Loading)

    var flats by remember { mutableStateOf(emptyList<Flat>()) }
    var isLoading by remember { mutableStateOf(false) }

    var selectedFlat by remember { mutableStateOf<Flat?>(null) }

    val rentsResponse by
    viewModel.getRents(selectedFlat?.id.toString())
        .collectAsStateWithLifecycle(initialValue = Response.Loading)
    var rents by remember { mutableStateOf(emptyList<Transaction>()) }


    when (flatsResponse) {
        is Response.Success -> {
            isLoading = false

            flats = (flatsResponse as Response.Success<List<Flat>>).data
            selectedFlat = flats.firstOrNull()
        }

        is Response.Error -> {
            Toast.makeText(
                context,
                (flatsResponse as Response.Error).errorMessage,
                Toast.LENGTH_SHORT
            )
                .show()

            isLoading = false
        }

        is Response.Loading -> {
            isLoading = true
        }
    }

    when (rentsResponse) {
        is Response.Success -> {
            isLoading = false

            rents = (rentsResponse as Response.Success<List<Transaction>>).data
        }

        is Response.Error -> {
            Toast.makeText(
                context,
                (flatsResponse as Response.Error).errorMessage,
                Toast.LENGTH_SHORT
            )
                .show()

            isLoading = false
        }

        is Response.Loading -> {
            isLoading = true
        }
    }

    if (isLoading) {
        Column(
            modifier = Modifier
                .padding(paddingLarge)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(paddingLarge)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier
                        .padding(paddingLarge)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(start = paddingLarge)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.fetching_data)
                            )
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .padding(paddingLarge)
                .imePadding()
                .navigationBarsPadding()
                .systemBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Show Rents
            LazyColumn(
                Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = paddingLarge, vertical = paddingLarge),
                state = listState
            ) {
                if (rents.isNotEmpty()) {
                    rents.forEach { rent ->
                        item {
                            ListItem(headlineContent = {
                                Text(
                                    text = rent.date.formatDate(),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }, supportingContent = {
                                if (rent.comment.isNotBlank()) {
                                    Text(rent.comment)
                                }
                            }, trailingContent = {
                                Text(
                                    text = moneyFormat(rent.amount),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }, modifier = Modifier.clickable {
                                selectedRent = rent
                                scope.launch { editTransactionSheetState.show() }
                            })
                            HorizontalDivider()
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier
                                .padding(paddingLarge)
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingLarge),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    modifier = Modifier.fillMaxHeight(),
                                    contentDescription = stringResource(id = R.string.no_rents),
                                )
                                Text(
                                    text = stringResource(id = R.string.no_rents)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedFlat?.let {
        EditTransactionSheet(it, TransactionType.RENTS, selectedRent, editTransactionSheetState)
    }
}