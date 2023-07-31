package com.noisefit.util

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.noisefit.ui.feeds.create.PostHashtagSpan
import com.noisefit.ui.feeds.create.PostMentionSpan
import com.noisefit_commans.ui.tryCatch
import java.util.regex.Matcher
import java.util.regex.Pattern


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

fun String.generatePostSpan(): SpannableStringBuilder {
    val builder = SpannableStringBuilder(this)
    var pattern: Pattern = Pattern.compile("@\\w+")
    var matcher: Matcher = pattern.matcher(this)
    while (matcher.find()) {
        builder.setSpan(
            PostMentionSpan(),
            matcher.start(),
            matcher.end(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    pattern = Pattern.compile("#\\w+");
    matcher = pattern.matcher(this)
    while (matcher.find()) {
        builder.setSpan(
            PostHashtagSpan(),
            matcher.start(),
            matcher.end(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return builder
}
