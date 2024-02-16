package com.panosdim.flatman.models

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    var id: String? = "",
    var amount: Float = 0.0F,
    var comment: String = "",
    var date: String = ""
)