package com.panosdim.flatman.utils

import kotlin.enums.enumEntries
import kotlin.reflect.KProperty1

enum class RentComment(val comment: String) {
    JANUARY("Ενοίκιο Ιανουαρίου"),
    FEBRUARY("Ενοίκιο Φεβρουαρίου"),
    MARCH("Ενοίκιο Μαρτίου"),
    APRIL("Ενοίκιο Απριλίου"),
    MAY("Ενοίκιο Μαΐου"),
    JUNE("Ενοίκιο Ιουνίου"),
    JULY("Ενοίκιο Ιουλίου"),
    AUGUST("Ενοίκιο Αυγούστου"),
    SEPTEMBER("Ενοίκιο Σεπτεμβρίου"),
    OCTOBER("Ενοίκιο Οκτωβρίου"),
    NOVEMBER("Ενοίκιο Νοεμβρίου"),
    DECEMBER("Ενοίκιο Δεκεμβρίου"),
}

inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Enum<T>, V> KProperty1<T, V>.findOrNull(value: V): T? =
    enumEntries<T>().firstOrNull { this(it) == value }