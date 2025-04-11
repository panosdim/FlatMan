package com.panosdim.flatman.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(var title: String, var icon: ImageVector, var screenRoute: String) {
    data object Dashboard : BottomNavItem("Dashboard", Icons.Default.Dashboard, "dashboard")
    data object Rents : BottomNavItem("Rents", Icons.Default.Savings, "rents")
    data object Expenses : BottomNavItem("Expenses", Icons.Default.Payments, "expenses")
    data object Flats : BottomNavItem("Flats", Icons.Default.Apartment, "flats")
}