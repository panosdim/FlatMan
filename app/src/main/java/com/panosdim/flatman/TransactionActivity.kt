package com.panosdim.flatman

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.models.Transaction
import com.panosdim.flatman.ui.AddTransactionForm
import com.panosdim.flatman.ui.EditTransactionForm
import com.panosdim.flatman.ui.theme.FlatManTheme
import com.panosdim.flatman.utils.TransactionType
import kotlinx.serialization.json.Json

class TransactionActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val flatJson: String = intent.getStringExtra(FLAT).toString()
        val transactionType: TransactionType? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(TRANSACTION_TYPE, TransactionType::class.java)
            } else {
                intent.getSerializableExtra(TRANSACTION_TYPE) as TransactionType
            }
        val transactionJson: String = intent.getStringExtra(TRANSACTION).toString()
        val flat: Flat = Json.decodeFromString<Flat>(flatJson)
        val transaction: Transaction? = try {
            Json.decodeFromString<Transaction>(transactionJson)
        } catch (_: Exception) {
            null
        }

        setContent {
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

            FlatManTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                        topBar = {
                            CenterAlignedTopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary,
                                ),
                                title = {
                                    Text(
                                        flat.address,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { this.finish() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Localized description"
                                        )
                                    }
                                },
                                scrollBehavior = scrollBehavior,
                            )
                        },
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (transactionType != null) {
                                if (transaction == null) {
                                    AddTransactionForm(
                                        flat = flat,
                                        transactionType = transactionType
                                    )
                                } else {
                                    EditTransactionForm(
                                        flat = flat,
                                        transactionType = transactionType,
                                        transaction = transaction
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}