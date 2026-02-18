package com.noisefit_commans.utils.imagepicker


import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import java.io.File
import java.util.*

object Util {
    var fileNamePrefix = "noisefit_"

    fun createFile(context: Context, fileType: String): File? {
        return try {
            val filename = fileNamePrefix + UUID.randomUUID().toString() + "." + fileType
            val fileDir: File = context.filesDir
            val file = File(fileDir, filename)
            file.createNewFile()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun getCaptureImageOutputUri(context: Context): File? {
        val getImage: File? = context.externalCacheDir
        if (getImage != null) {
            return File(getImage.path, "watchface.png")
        }
        return null
    }
    fun getMarkdownString(text: String): SpannableStringBuilder {
        val result = SpannableStringBuilder()
        var index = 0

        val pattern = Regex("(\\*\\*([^*]+)\\*\\*)|(_([^_]+)_)")

        pattern.findAll(text).forEach { match ->
            // add text before markdown
            if (match.range.first > index) {
                result.append(text.substring(index, match.range.first))
            }

            when {
                match.groups[2] != null -> {
                    // **bold**
                    val start = result.length
                    val content = match.groups[2]!!.value
                    result.append(content)
                    result.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        result.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                match.groups[4] != null -> {
                    // _italic_
                    val start = result.length
                    val content = match.groups[4]!!.value
                    result.append(content)
                    result.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        start,
                        result.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            index = match.range.last + 1
        }

        if (index < text.length) {
            result.append(text.substring(index))
        }

        return result
    }

}
