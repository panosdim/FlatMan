package com.panosdim.flatman.ui

import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.flatman.FLAT
import com.panosdim.flatman.FlatActivity
import com.panosdim.flatman.FlatTransactionsActivity
import com.panosdim.flatman.R
import com.panosdim.flatman.TRANSACTION_TYPE
import com.panosdim.flatman.TransactionActivity
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.paddingSmall
import com.panosdim.flatman.ui.theme.expiredDark
import com.panosdim.flatman.ui.theme.expiredLight
import com.panosdim.flatman.ui.theme.expiresSoonDark
import com.panosdim.flatman.ui.theme.expiresSoonLight
import com.panosdim.flatman.ui.theme.savingsDark
import com.panosdim.flatman.ui.theme.savingsLight
import com.panosdim.flatman.utils.TransactionType
import com.panosdim.flatman.utils.formatDate
import com.panosdim.flatman.utils.moneyFormat
import com.panosdim.flatman.utils.toLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun FlatCard(
    flat: Flat,
) {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()

    val flatSavings = viewModel.getSavings(flat.id.toString())
        .collectAsStateWithLifecycle(initialValue = BigDecimal.ZERO)

    val darkTheme: Boolean = isSystemInDarkTheme()
    val today = LocalDate.now()
    val nextMonthLastDay = today.with(TemporalAdjusters.firstDayOfNextMonth())
        .with(TemporalAdjusters.lastDayOfMonth())
    var color: Color? = null

    flat.lessee?.let {
        val rentEnds = it.end.toLocalDate()
        if (rentEnds.isBefore(today)) {
            color = if (darkTheme) expiredDark else expiredLight
        }

        if ((rentEnds.isBefore(nextMonthLastDay) || rentEnds.isEqual(nextMonthLastDay))
            && rentEnds.isAfter(today)
        ) {
            color = if (darkTheme) expiresSoonDark else expiresSoonLight
        }
    }



    Card(
        modifier = Modifier
            .padding(paddingSmall)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        onClick = {
            val intent = Intent(context, FlatTransactionsActivity::class.java)
            val flatJson = Json.encodeToString(flat)

            intent.putExtra(FLAT, flatJson)
            context.startActivity(intent)
        }
    ) {
        Column(Modifier.padding(paddingLarge)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        color?.let { color ->
                            Text(
                                text = resources.getString(
                                    R.string.rent_ends,
                                    it.end.formatDate()
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                color = color
                            )
                        } ?: run {
                            Text(
                                text = resources.getString(
                                    R.string.rent_ends,
                                    it.end.formatDate()
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                }

                FilledIconButton(onClick = {
                    val intent = Intent(context, FlatActivity::class.java)
                    val flatJson = Json.encodeToString(flat)

                    intent.putExtra(FLAT, flatJson)
                    context.startActivity(intent)
                }) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
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
                color = if (darkTheme) savingsDark else savingsLight,
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
                    val intent = Intent(context, TransactionActivity::class.java)
                    val flatJson = Json.encodeToString(flat)

                    intent.putExtra(FLAT, flatJson)
                    intent.putExtra(TRANSACTION_TYPE, TransactionType.EXPENSES)
                    context.startActivity(intent)
                }) {
                    Text(
                        stringResource(id = R.string.add_expense)
                    )
                }

                Button(
                    onClick = {
                        val intent = Intent(context, TransactionActivity::class.java)
                        val flatJson = Json.encodeToString(flat)

                        intent.putExtra(FLAT, flatJson)
                        intent.putExtra(TRANSACTION_TYPE, TransactionType.RENTS)
                        context.startActivity(intent)
                    },
                ) {
                    Text(
                        stringResource(id = R.string.add_rent)
                    )
                }
            }
        }
    }
}