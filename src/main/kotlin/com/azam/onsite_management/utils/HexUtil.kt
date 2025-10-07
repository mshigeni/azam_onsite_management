package com.azam.onsite_management.utils

object HexUtil {

    fun convertAndReorderHexadecimal(decimal: Long): String {
        // Convert decimal → hex (lowercase by default)
        var hex = decimal.toString(16)

        // Pad to 8 characters with leading zeros
        hex = hex.padStart(8, '0')

        // Split into 2-char chunks
        val chunks = hex.chunked(2)

        // Reverse chunk order and rejoin
        return chunks.reversed().joinToString("")
    }
}