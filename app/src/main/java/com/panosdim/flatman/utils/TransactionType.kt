package com.panosdim.flatman.utils

interface IGetFirebasePath {
    fun getFirebasePath(): String
}

enum class TransactionType(val title: String) : IGetFirebasePath {
    RENTS("Rents") {
        override fun getFirebasePath() = "rents"
    },
    EXPENSES("Expenses") {
        override fun getFirebasePath() = "expenses"
    },
}