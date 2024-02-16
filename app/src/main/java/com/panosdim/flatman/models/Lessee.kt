package com.panosdim.flatman.models

import kotlinx.serialization.Serializable

@Serializable
data class Lessee(
    var name: String = "",
    var rent: Int = 0,
    var start: String = "",
    var end: String = "",
)
