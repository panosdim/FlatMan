package com.panosdim.flatman.ui

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.flatman.R
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Response
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.ui.theme.blueDark
import com.panosdim.flatman.ui.theme.blueLight
import com.panosdim.flatman.ui.theme.redDark
import com.panosdim.flatman.ui.theme.redLight
import com.panosdim.flatman.utils.moneyFormat
import java.math.BigDecimal


@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val listState = rememberLazyListState()

    val darkTheme: Boolean = isSystemInDarkTheme()

    val totalSavings = viewModel.getSavings()
        .collectAsStateWithLifecycle(initialValue = BigDecimal.ZERO)

    val lastYearSavings =
        viewModel.getLastYearSavings().collectAsStateWithLifecycle(initialValue = BigDecimal.ZERO)

    val flatsResponse by viewModel.getFlats()
        .collectAsStateWithLifecycle(initialValue = Response.Loading)

    var flats by remember { mutableStateOf(emptyList<Flat>()) }
    var isLoading by remember { mutableStateOf(false) }

    when (flatsResponse) {
        is Response.Success -> {
            isLoading = false

            flats = (flatsResponse as Response.Success<List<Flat>>).data
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
            ElevatedCard(
                modifier = Modifier
                    .padding(paddingLarge)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
            ) {
                Column(modifier = Modifier.padding(paddingLarge)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = resources.getString(R.string.savings),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = resources.getString(R.string.total),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = moneyFormat(totalSavings.value),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (totalSavings.value > BigDecimal(0)) {
                                    if (darkTheme) blueDark else blueLight
                                } else {
                                    if (darkTheme) redDark else redLight
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = resources.getString(R.string.last_year),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = moneyFormat(lastYearSavings.value),
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (lastYearSavings.value > BigDecimal(0)) {
                                    if (darkTheme) blueDark else blueLight
                                } else {
                                    if (darkTheme) redDark else redLight
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Show Flats
            LazyColumn(
                Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = paddingLarge, vertical = paddingLarge),
                state = listState
            ) {
                if (flats.isNotEmpty()) {
                    flats.forEach {
                        item {
                            FlatCard(flat = it)
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
                                    contentDescription = stringResource(id = R.string.no_items),
                                )
                                Text(
                                    text = stringResource(id = R.string.no_items)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}