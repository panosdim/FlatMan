package com.panosdim.flatman.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.panosdim.flatman.models.Flat
import com.panosdim.flatman.utils.TransactionType
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(flat: Flat) {
    val scope = rememberCoroutineScope()
    val tabs = enumValues<TransactionType>().map { it.title }
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    icon = {
                        when (index) {
                            0 -> Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null
                            )

                            1 -> Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> FlatRents(flat)
                1 -> FlatExpenses(flat)
            }
        }
    }
}