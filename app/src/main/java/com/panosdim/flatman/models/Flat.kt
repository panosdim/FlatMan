package com.panosdim.flatman.models

import kotlinx.serialization.Serializable

@Serializable
data class Flat(
    var id: String = "",
    var address: String = "",
    var floor: Int = 0,
    var name: String = "",
    var lessee: Lessee? = null,
)
