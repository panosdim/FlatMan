package com.panosdim.flatman.ui

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panosdim.flatman.LoginActivity
import com.panosdim.flatman.R
import com.panosdim.flatman.data.MainViewModel
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Response
import com.panosdim.flatman.paddingLarge
import com.panosdim.flatman.ui.theme.savingsDark
import com.panosdim.flatman.ui.theme.savingsLight
import com.panosdim.flatman.utils.moneyFormat
import java.math.BigDecimal

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val resources = context.resources
    val viewModel: MainViewModel = viewModel()
    val listState = rememberLazyListState()

    var isLoading by remember {
        mutableStateOf(false)
    }

    val darkTheme: Boolean = isSystemInDarkTheme()

    val totalSavings = viewModel.getSavings()
        .collectAsStateWithLifecycle(initialValue = BigDecimal.ZERO)

    var flats by remember { mutableStateOf(emptyList<Flat>()) }

    val flatsResponse =
        viewModel.flats.collectAsStateWithLifecycle(initialValue = Response.Loading)

    var flat: Flat? by remember { mutableStateOf(null) }

    when (flatsResponse.value) {
        is Response.Success -> {
            isLoading = false

            flats =
                (flatsResponse.value as Response.Success<List<Flat>>).data
        }

        is Response.Error -> {
            Toast.makeText(
                context,
                (flatsResponse.value as Response.Error).errorMessage,
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
        ProgressBar()
    } else {
        Column(
            modifier = Modifier.padding(paddingLarge),
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
                    Button(onClick = { /* Start activity to add Flat */ }) {
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
//                            context.unregisterReceiver(onComplete)
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
                        .padding(paddingLarge),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(paddingLarge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            text = resources.getString(R.string.total_savings),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            textAlign = TextAlign.Center,
                            text = moneyFormat(totalSavings.value),
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (darkTheme) savingsDark else savingsLight,
                            fontWeight = FontWeight.Bold
                        )
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
                    flats.iterator().forEachRemaining {
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
    }
}