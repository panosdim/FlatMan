package com.panosdim.flatman.models

import kotlinx.serialization.Serializable

@Serializable
data class Flat(
    var id: String? = "",
    var address: String = "",
    var lessee: Lessee? = null,
)
