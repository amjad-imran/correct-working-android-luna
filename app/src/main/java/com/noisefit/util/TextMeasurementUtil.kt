package com.noisefit.util

import android.os.Build
import android.text.StaticLayout
import android.widget.TextView

object TextMeasurementUtil {
    fun getTextLines(textView: TextView): List<CharSequence> {

        val layout = getStaticLayout(textView)

        if (layout != null) {
            return (0 until layout.lineCount).map {
                layout.text.subSequence(layout.getLineStart(it), layout.getLineEnd(it))
            }
        }
        return emptyList()
    }

    private fun getStaticLayout(textView: TextView): StaticLayout =
        try {
            val builder = StaticLayout.Builder
                .obtain(
                    textView.text, 0, textView.text.length, textView.layout.paint,
                    textView.width
                )
                .setAlignment(textView.layout.alignment)
                .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
                .setIncludePad(textView.includeFontPadding)
                .setBreakStrategy(textView.breakStrategy)
                .setHyphenationFrequency(textView.hyphenationFrequency)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setJustificationMode(textView.justificationMode)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                builder.setUseLineSpacingFromFallbacks(textView.isFallbackLineSpacing)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setTextDirection(textView.textDirectionHeuristic)
            }
            builder.build()
        }
        catch (e:Exception){
            e.printStackTrace()
            StaticLayout(
                textView.text,
                textView.layout.paint,
                textView.width,
                textView.layout.alignment,
                textView.lineSpacingMultiplier,
                textView.lineSpacingExtra,
                textView.includeFontPadding
            )}
}