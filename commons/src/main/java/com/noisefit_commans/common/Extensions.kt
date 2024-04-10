package com.noisefit_commans.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.TypedValue
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.Toast
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.utils.DateFormats
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt


inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

fun Int?.convertMinuteIntoSeconds(): Int {
    if (this == null) {
        return 0
    }

    return this * 60

}

fun Int.px(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        NoisefitApplication.context?.resources?.displayMetrics
    )
}


fun Context.dpToPx(px: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), this.resources.displayMetrics
    ).toInt()
}

fun Long.checkDayDifferenceMoreNMinutes(value: Int): Boolean {
    val timeStamp = DateFormats.getTimeStamp()
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    cal.add(Calendar.MINUTE, value)
    if (timeStamp > cal.timeInMillis) {
        return true
    }

    return false
}

fun String.decodeHex(): String {
    return try {
        chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
            .toString(Charsets.ISO_8859_1)
    } catch (exp: Exception) {
        exp.printStackTrace()
        ""
    }

}

fun List<Int>.averageWithoutZero(): Int {
    val newList = this.filter { it != 0 && it != 255 }
    return if (newList.isNotEmpty()) {
        newList.average().roundToInt()
    } else {
        0
    }
}

fun List<Int>.averageDaytimeValues(): Int {
    val newList = this.filter { it != 0 && it != 255 }
    return if (newList.isNotEmpty()) {
        newList.average().ceilRound()
    } else {
        0
    }
}

fun List<Float>.averageWithoutZeroFloat(): Float {
    val newList = this.filter { it != 0.0f && it != 255.0f }
    return if (newList.isNotEmpty()) {
        newList.average().toFloat()
    } else {
        0.0f
    }
}

fun List<Int>.averageIntWithoutZeroFloat(): Float {
    val newList = this.filter { it != 0 && it != 255 }
    return if (newList.isNotEmpty()) {
        newList.average().toFloat()
    } else {
        0.0f
    }
}

fun List<Int>.minWithoutZero(): Int {
    val newList = this.filter { it != 0 && it != 255 }
    return if (newList.isNotEmpty()) {
        newList.minOrNull() ?: 0
    } else {
        0
    }
}

fun List<Int>.maxWithoutZero(): Int {
    val newList = this.filter { it != 0 && it != 255 }
    return if (newList.isNotEmpty()) {
        newList.maxOrNull() ?: 0
    } else {
        0
    }
}

fun List<Int>.maxWithoutInvalidMovementValues(): Int {
    val newList = this.filter { it != 0 && it != 255 && it != 5 && it != 4 }
    return if (newList.isNotEmpty()) {
        newList.maxOrNull() ?: 0
    } else {
        0
    }
}

fun List<Int>.maxWithInvalidMovementValues(): Int {
    val newList = this.filter { it != 255 && it != 5 && it != 4 }
    return if (newList.isNotEmpty()) {
        newList.maxOrNull() ?: 255
    } else {
        255
    }
}

fun Bitmap.convertCorner(radius: Float): Bitmap {
    val rad = (Resources.getSystem().displayMetrics.density * radius + 0.5f).toInt()
    val output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint()
    val rect = Rect(0, 0, this.width, this.height)
    val rectF = RectF(rect)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = Color.BLACK
    canvas.drawRoundRect(rectF, rad.toFloat(), rad.toFloat(), paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}

fun Float.upToNDecimal(upTo: Int): String {
    return String.format("%.${upTo}f", this)
}

fun Double.upToNDecimal(upTo: Int): String {
    return String.format("%.${upTo}f", this)
}

fun Double.roundDownDecimal(): String {
    val df = DecimalFormat("0.00")
    df.roundingMode = RoundingMode.DOWN
    return df.format(this)
}

fun Double.ceilRound(): Int {
    return DecimalFormat("#").apply {
        roundingMode = RoundingMode.CEILING
    }.format(this).toInt()
}

fun Double.roundUpDecimal(): String {
    val df = DecimalFormat("0.00")
    df.roundingMode = RoundingMode.UP
    return df.format(this)
}

fun Double.roundToNearestDecimal(): String {
    val df = DecimalFormat("0.00")
    df.roundingMode = RoundingMode.HALF_EVEN
    return df.format(this)
}
fun Double.roundToNearestSingleDecimal(): String {
    val df = DecimalFormat("0.0")
    df.roundingMode = RoundingMode.HALF_EVEN
    return df.format(this)
}

fun Float.roundToNearestDecimalUp(): Float {
    return BigDecimal(this.toString()).setScale(2, BigDecimal.ROUND_HALF_UP).toFloat()
}

fun Float.roundToNearestDecimalDown(): Float {
    return BigDecimal(this.toString()).setScale(2, BigDecimal.ROUND_HALF_EVEN).toFloat()
}

fun Float.roundToNearestDecimalFloor(upTo: Int): Float {
    return BigDecimal(this.toString()).setScale(upTo, BigDecimal.ROUND_FLOOR).toFloat()
}

fun Double.roundToNearestDecimalFloor(upTo: Int): Double {
    return BigDecimal(this.toString()).setScale(upTo, BigDecimal.ROUND_FLOOR).toDouble()
}

fun String.copyToClipBoard() {
    val clipboardManager =
        NoisefitApplication.context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    // When setting the clipboard text.
    clipboardManager.setPrimaryClip(ClipData.newPlainText("", this))
    // Only show a toast for Android 12 and lower.
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        Toast.makeText(NoisefitApplication.context!!, "Copied", Toast.LENGTH_SHORT).show()
    }

}

//no change in values
fun Float.roundToNearestDecimalFloor(): String {
    val df = DecimalFormat("0.0")
    df.roundingMode = RoundingMode.FLOOR
    return df.format(this)
}

fun Float.roundToNearestDecimalFlooor(uptoValue: Float): String {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.HALF_UP
    return df.format(uptoValue)
}

//fun <T> T.upToNDecimal(upTo: Int): String {
//    return String.format("%.${upTo}f", this)
//}

fun Float.upTo1Decimal(): Float {
    val decimalFormat = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US))
    return decimalFormat.format(this).toFloat()
}

fun Double.upTo2Decimal(): Double {
    val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))
    return decimalFormat.format(this).toDouble()
}

fun Double.truncateDecimal(numberOfDecimals: Int): String {
    return if (this > 0) {
        BigDecimal(this.toString()).setScale(numberOfDecimals, BigDecimal.ROUND_FLOOR).toString()
    } else {
        BigDecimal(this.toString()).setScale(numberOfDecimals, BigDecimal.ROUND_CEILING).toString()
    }
}


fun <T> T.upToNDecimal(upTo: Int): String {
    return String.format("%.${upTo}f", this)
}


fun ArrayList<Int>.handleCaloriesData(duration: Int): IntArray {
    val dataList = ArrayList<Int>()

    if (duration <= 0 || this.isEmpty()) {
        return dataList.toIntArray()
    }
    val dataWithSeconds = (duration).div(this.size)
    var interval = 60
    if (dataWithSeconds != 0) {
        interval = 60 / dataWithSeconds
    }
    if (interval <= 0) return dataList.toIntArray()

    val dataChunk = this.chunked(interval)
    dataChunk.forEachIndexed { _, heartRateList ->
        val caloriesValues = heartRateList.filter { it != 0 }
        val sum = caloriesValues.sum()
        dataList.add(sum)
    }
    return dataList.toIntArray()
}


fun ArrayList<Int>.handleHrData(duration: Int): IntArray {
    val dataList = ArrayList<Int>()
    if (duration <= 0 || this.isEmpty()) {
        return dataList.toIntArray()
    }
    val dataWithSeconds = (duration).div(this.size)
    var interval = 30
    if (dataWithSeconds != 0) {
        interval = 30 / dataWithSeconds
    }
    if (interval <= 0) return dataList.toIntArray()

    val dataChunk = this.chunked(interval)
    dataChunk.forEachIndexed { index, heartRateList ->
        val max = heartRateList.maxOf { it2 -> it2 }
        val min = heartRateList.minOf { it2 -> it2 }
        dataList.add(min)
        dataList.add(max)
    }
    return dataList.toIntArray()
}

fun IntArray.handleHrData(duration: Int): IntArray {
    val dataList = ArrayList<Int>()
    if (duration <= 0 || this.isEmpty()) {
        return dataList.toIntArray()
    }
    val dataWithSeconds = (duration).div(this.size)
    var interval = 30
    if (dataWithSeconds != 0) {
        interval = 30 / dataWithSeconds
    }
    if (interval <= 0) return dataList.toIntArray()

    val dataChunk = this.toList().chunked(interval)
    dataChunk.forEachIndexed { _, heartRateList ->
        val max = heartRateList.maxOf { it2 -> it2 }
        val min = heartRateList.minOf { it2 -> it2 }
        dataList.add(min)
        dataList.add(max)
    }

    return dataList.toIntArray()
}

fun TextView.clearDrawables() {
    this.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
}

fun TextView.setCompoundDrawable(
    drawable1: Int = 0,
    drawable2: Int = 0,
    drawable3: Int = 0,
    drawable4: Int = 0
) {
    this.setCompoundDrawablesWithIntrinsicBounds(drawable1, drawable2, drawable3, drawable4)
}


//fun <T> T.upToNDecimal(upTo: Int): String {
//    return String.format("%.${upTo}f", this)
//}

