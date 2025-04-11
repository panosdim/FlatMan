package com.panosdim.flatman.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.flatman.R
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.paddingSmall
import com.panosdim.flatman.ui.theme.blueDark
import com.panosdim.flatman.ui.theme.blueLight
import com.panosdim.flatman.ui.theme.redDark
import com.panosdim.flatman.ui.theme.redLight
import com.panosdim.flatman.ui.theme.yellowDark
import com.panosdim.flatman.ui.theme.yellowLight
import com.panosdim.flatman.utils.TransactionType
import com.panosdim.flatman.utils.formatDate
import com.panosdim.flatman.utils.moneyFormat
import com.panosdim.flatman.utils.toLocalDate
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatCard(
    flat: Flat,
) {
    val context = LocalContext.current
    val resources = context.resources
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()

    val flatSavings = viewModel.getSavings(flat.id.toString())
        .collectAsStateWithLifecycle(initialValue = BigDecimal.ZERO)

    val darkTheme: Boolean = isSystemInDarkTheme()
    val today = LocalDate.now()
    val nextMonthLastDay = today.with(TemporalAdjusters.firstDayOfNextMonth())
        .with(TemporalAdjusters.lastDayOfMonth())
    var color: Color = Color.Unspecified

    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val addTransactionSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    var transactionType: TransactionType by remember { mutableStateOf(TransactionType.EXPENSES) }

    flat.lessee?.let {
        val rentEnds = it.end.toLocalDate()
        if (rentEnds.isBefore(today)) {
            color = if (darkTheme) redDark else redLight
        }

        if ((rentEnds.isBefore(nextMonthLastDay) || rentEnds.isEqual(nextMonthLastDay))
            && rentEnds.isAfter(today)
        ) {
            color = if (darkTheme) yellowDark else yellowLight
        }
    }

    Card(
        modifier = Modifier
            .padding(paddingSmall)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(paddingLarge)) {
            Column {
                Text(
                    text = flat.address,
                    style = MaterialTheme.typography.headlineLarge
                )
                flat.lessee?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = moneyFormat(it.rent),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = resources.getString(
                            R.string.rent_ends,
                            it.end.formatDate()
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = color
                    )
                }
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                text = resources.getString(
                    R.string.flat_savings,
                    moneyFormat(flatSavings.value)
                ),
                color = if (darkTheme) blueDark else blueLight,
                style = MaterialTheme.typography.headlineSmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ElevatedButton(onClick = {
                    transactionType = TransactionType.EXPENSES
                    scope.launch { addTransactionSheetState.show() }
                }) {
                    Text(
                        stringResource(id = R.string.add_expense)
                    )
                }

                Button(
                    onClick = {
                        transactionType = TransactionType.RENTS
                        scope.launch { addTransactionSheetState.show() }
                    },
                ) {
                    Text(
                        stringResource(id = R.string.add_rent)
                    )
                }
            }
        }
    }

    AddTransactionSheet(
        flat,
        transactionType,
        addTransactionSheetState
    )
}