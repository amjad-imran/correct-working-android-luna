package com.noisefit.util

import com.noisefit_commans.ui.tryCatch


/**
 * Removes new lines and spaces from start and end
 */
fun String.formatPostLeadingString(): String {
    return this.replace("^[\n\r]", "").trimStart()
}

fun String.formatPostTrailingString(): String {
    return this.replace("[\n\r]$", "").trimEnd()
}

fun String.findWordStart(offset: Int): Int {
    var start = offset
    tryCatch {
        while (start > 0 && Character.isLetterOrDigit(this[start - 1])) {
            start--
        }
    }
    return start
}
