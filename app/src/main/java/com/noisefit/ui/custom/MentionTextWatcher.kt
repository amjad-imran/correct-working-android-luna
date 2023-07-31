package com.noisefit.ui.custom

import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import com.noisefit.ui.feeds.create.AUTO_COMPLETE_PATTERN
import com.noisefit.ui.feeds.create.PostAutocompleteSpan
import com.noisefit.ui.feeds.create.PostHashtagSpan
import com.noisefit.ui.feeds.create.PostMentionSpan
import java.util.regex.Matcher


class MentionTextWatcher(val onTextChange: (hasContent: Boolean) -> Unit) : TextWatcher {
    private var lastChangeStart = 0
    private var lastChangeCount = 0

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.isNullOrEmpty()) {
            onTextChange.invoke(false)
            return
        } else {
            onTextChange.invoke(true)
        }
        lastChangeStart = start
        lastChangeCount = count

    }

    override fun afterTextChanged(s: Editable?) {
        if (s.isNullOrEmpty()) return

        var start = lastChangeStart
        val count = lastChangeCount
        // offset one char back to catch an already typed '@' or '#' or ':'
        val realStart = start
        start = Math.max(0, start - 1)
        val changedText = s.subSequence(start, realStart + count)
        val raw = changedText.toString()
        val editable = s
        // 1. find mentions, hashtags, and emoji shortcodes in any freshly inserted text, and put spans over them
        if (raw.contains("@") || raw.contains("#")) {
            val matcher: Matcher =
                AUTO_COMPLETE_PATTERN.matcher(
                    changedText
                )
            while (matcher.find()) {
                if (editable.getSpans(
                        start + matcher.start(), start + matcher.end(),
                        PostAutocompleteSpan::class.java
                    ).size > 0
                ) continue
                var span: PostAutocompleteSpan?
                if (TextUtils.isEmpty(matcher.group(4))) { // not an emoji
                    span = if (raw.startsWith("#")) {
                        PostHashtagSpan()
                    } else {
                        PostMentionSpan()
                    }
                } else {
                    span = PostAutocompleteSpan()
                }
                editable.setSpan(
                    span,
                    start + matcher.start(),
                    start + matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        }
        // 2. go over existing spans in the affected range, adjust end offsets and remove no longer valid spans
        val spans: Array<PostAutocompleteSpan> = editable.getSpans(
            realStart, realStart + count,
            PostAutocompleteSpan::class.java
        )
        for (span in spans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            if (spanStart == spanEnd) { // empty, remove
                editable.removeSpan(span)
                continue
            }
            val firstChar = editable[spanStart]
            val spanText = s.subSequence(spanStart, spanEnd).toString()
            if (firstChar == '@' || firstChar == '#') {
                val matcher: Matcher =
                    AUTO_COMPLETE_PATTERN.matcher(
                        spanText
                    )
                val prevChar = if (spanStart > 0) editable[spanStart - 1] else ' '
                if (!matcher.find() || !Character.isWhitespace(prevChar)) { // invalid mention, remove
                    editable.removeSpan(span)
                    continue
                } else if (matcher.end() + spanStart < spanEnd) { // mention with something at the end, move the end offset
                    editable.setSpan(
                        span,
                        spanStart,
                        spanStart + matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            } else {
                editable.removeSpan(span)
            }
        }
    }
}
