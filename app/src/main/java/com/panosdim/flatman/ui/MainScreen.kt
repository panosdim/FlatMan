package com.panosdim.flatman.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panosdim.flatman.FlatActivity
import com.panosdim.flatman.LoginActivity
import com.panosdim.flatman.R
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.ui.theme.blueDark
import com.panosdim.flatman.ui.theme.blueLight
import com.panosdim.flatman.ui.theme.redDark
import com.panosdim.flatman.ui.theme.redLight
import com.panosdim.flatman.utils.moneyFormat
import java.math.BigDecimal

@Composable
fun MainScreen(onComplete: BroadcastReceiver) {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val listState = rememberLazyListState()

    val darkTheme: Boolean = isSystemInDarkTheme()

    val totalSavings = viewModel.getSavings()
        .collectAsStateWithLifecycle(initialValue = BigDecimal.ZERO)

    val lastYearSavings =
        viewModel.getLastYearSavings().collectAsStateWithLifecycle(initialValue = BigDecimal.ZERO)

    val flats by viewModel.getFlats().collectAsStateWithLifecycle(initialValue = null)

    flats?.let {
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = paddingLarge, end = paddingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        val intent = Intent(context, FlatActivity::class.java)

                        context.startActivity(intent)
                    }) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            stringResource(id = R.string.add_flat)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.signOut()
                            context.unregisterReceiver(onComplete)
                            Firebase.auth.signOut()
                            (context as? Activity)?.finish()

                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            stringResource(id = R.string.logout)
                        )
                    }
                }

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
            }

            // Show Flats
            LazyColumn(
                Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = paddingLarge, vertical = paddingLarge),
                state = listState
            ) {
                if (it.isNotEmpty()) {
                    it.iterator().forEachRemaining {
                        item {
                            FlatCard(flat = it)
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
                                contentDescription = stringResource(id = R.string.no_items),
                                modifier = Modifier

                            )
                            Text(
                                text = stringResource(id = R.string.no_items)
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