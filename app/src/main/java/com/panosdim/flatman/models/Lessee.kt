package com.panosdim.flatman.models

import kotlinx.serialization.Serializable

@Serializable
data class Lessee(
    var name: String = "",
    var rent: Float = 0.0F,
    var start: String = "",
    var end: String = "",
)
