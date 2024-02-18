package com.panosdim.flatman.utils

val RentComment = arrayOf(
    "Ενοίκιο Ιανουαρίου",
    "Ενοίκιο Φεβρουαρίου",
    "Ενοίκιο Μαρτίου",
    "Ενοίκιο Απριλίου",
    "Ενοίκιο Μαΐου",
    "Ενοίκιο Ιουνίου",
    "Ενοίκιο Ιουλίου",
    "Ενοίκιο Αυγούστου",
    "Ενοίκιο Σεπτεμβρίου",
    "Ενοίκιο Οκτωβρίου",
    "Ενοίκιο Νοεμβρίου",
    "Ενοίκιο Δεκεμβρίου"
)

fun Array<String>.next(current: String): String {
    val index = this.indexOf(current)
    val nextIndex = (index + 1) % this.size
    return this[nextIndex]
}