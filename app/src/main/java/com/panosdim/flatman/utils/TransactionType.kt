package com.panosdim.flatman.utils

interface IGetFirebasePath {
    fun getFirebasePath(): String
}

enum class TransactionType : IGetFirebasePath {
    RENTS {
        override fun getFirebasePath() = "rents"
    },
    EXPENSES {
        override fun getFirebasePath() = "expenses"
    },
}