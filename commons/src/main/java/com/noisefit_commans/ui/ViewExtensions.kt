package com.noisefit_commans.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.R
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.model.Emoji
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.prettyCountDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLDecoder
import java.text.NumberFormat
import java.util.*

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}

fun String?.getParseList(): List<Int> {
    val list = this?.replace("255", "0")

    var breakupArray = Gson().fromJson<List<Int>>(list ?: "")
    if (breakupArray.isNullOrEmpty()) {
        val dummyArray = ArrayList<Int>()
        for (i in 0..287) {
            dummyArray.add(0)
        }
        breakupArray = dummyArray
    }
    return breakupArray
}

@UiThread
fun Context?.showShortToast(message: String?) {
    if (this != null && message != null) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

fun Int.getColor(): Int {
    return ContextCompat.getColor(
        NoisefitApplication.context!!,
        this
    )
}

fun String?.clearAmPm(): String? {
    return this?.lowercase()?.replace("pm","")?.replace("am","")?.trim()
}

fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

 fun CollapsingToolbarLayout.setScrollBehavior(enabled: Boolean) {
    this.updateLayoutParams<AppBarLayout.LayoutParams> {
        scrollFlags =
            if (enabled) AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
            else 0
    }
}
fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}

fun String.html(): Spanned {
    return HtmlCompat.fromHtml(this, 0)
}

fun LottieAnimationView.playAnimation(repeatCount: Int, animationRes: Int) {
    this.repeatCount = repeatCount
    this.setAnimation(animationRes)
    this.playAnimation()
}

fun <T> tryCatch(block: () -> T) = try {
    block()
} catch (e: Exception) {
    e.printStackTrace()
    FirebaseCrashlytics.getInstance().recordException(e)
}

fun Int?.getFormattedTimeInHourMinSec(): String {
    if (this == null) {
        return "_"
    }
    val stringBuilder = StringBuilder()
    val hrs = (this / 3600)
    val mins = (this % 3600 / 60)
    val secs = this % 60

    if (hrs > 0 && mins >= 0) {
        return stringBuilder.append(hrs).append("h").append(" ").append(mins).append("m").toString()
    }

    if (mins > 0 && secs >= 0) {
        return stringBuilder.append(mins).append("m").append(" ").append(secs).append("s")
            .toString()
    }

    // Output like "00:00:00"
    return stringBuilder.append(secs).append("s").toString()

}

fun Int?.getFormattedTimeInMinutes(): String {
    if (this == null) {
        return "_"
    }
    val stringBuilder = StringBuilder()
    //val hrs = (this / 3600)
    val mins = (this % 3600 / 60)
    // val secs = this % 60

    // Output like "00:00:00"
    return stringBuilder.append(mins).toString()

}
//fun String.capitalize(): String {
//    return this.trim().split("\\s+".toRegex())
//        .map { it.capitalize() }.joinToString(" ")
//}

fun String.replaceUnderScore(): String {
    if (this.startsWith("0")) {
        return "_"
    }
    return this
}

fun String.isUnderScore(): Boolean {
    if (this.equals("_", false)) {
        return true
    }
    return false
}

fun Long.checkDayDifferenceMoreOne(): Boolean {
    val lastSyncDate = DateFormats.convertTimestampToDate(
        this, DateFormats.dateFormat
    )
    LOGS.d("checkDayDifferenceMoreOne $lastSyncDate")
    val difference = DateFormats.getDateDiff(
        DateFormats.dateFormat, lastSyncDate, DateFormats.getTodaysDateString(7)
    ).toInt()
    LOGS.d("checkDayDifferenceMoreOne $difference")
    if (difference > 0) {
        return true
    }

    return false
}

fun Long.checkDayDifferenceMore30Minutes(): Boolean {
    val timeStamp = DateFormats.getTimeStamp()
    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    cal.add(Calendar.MINUTE, 30)
    if (timeStamp > cal.timeInMillis) {
        return true
    }

    return false
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

fun String.getRequestBody(): RequestBody {
    return this.toRequestBody("text/plain;charset=utf-8".toMediaType())
}

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            true
        }
        false
    }
}

fun String.decodeUtf8Url(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    return URLDecoder.decode(this, "UTF-8")
}

fun String.getTimeIn12HourFormat(): String? {
    return DateFormats.getConvertToDateFormat(
        this, DateFormats.timeFormat, DateFormats.time12Meridian
    )
}

fun Bundle.getDeeplinkPathArg(): String? {
    val deepLinkKey = this.keySet()?.firstOrNull()
    if (!deepLinkKey.isNullOrEmpty() && this.get(deepLinkKey) is Intent) {
        return try {
            val data = ((this.get(deepLinkKey) as Intent).data as Uri)
            data.path?.replace("/", "")

        } catch (exp: Exception) {
            null
        }
    }
    return null
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.gone() {
    visibility = View.GONE
}

fun CheckBox.checked() {
    this.isChecked = true
}

fun CheckBox.unChecked() {
    this.isChecked = false
}


fun String.isValidMobileNumber(): Boolean {
    if (this.isEmpty()) {
        return false
    }
    if (this.replace(" ", "").trim().length < 10) {
        return false
    }

    if (this[0].toString().equals("0", false)) {
        return false
    }
    return true
}

fun CoroutineScope.launchPeriodicAsync(repeatMillis: Long, action: () -> Unit) = this.async {
    while (isActive) {
        action()
        delay(repeatMillis)
    }
}

fun String.isValidOTP(): Boolean {
    if (this.isEmpty()) {
        return false
    }
    if (this.replace(" ", "").trim().length == 4) {
        return true
    }
    return false
}

fun Int.toMakeTwoDecimal(): String {
    if (this < 9) {
        return "0$this"
    }
    return this.toString()
}

fun EditText.disableContentInteraction() {
    keyListener = null
    isFocusable = false
    isFocusableInTouchMode = false
    isCursorVisible = false
    setBackgroundResource(android.R.color.transparent)
    clearFocus()
}

fun EditText.enableContentInteraction() {
    keyListener = EditText(context).keyListener
    isFocusable = true
    isFocusableInTouchMode = true
    isCursorVisible = true
    setBackgroundResource(android.R.color.white)
    requestFocus()
    if (text != null) {
        setSelection(text.length)
    }
}

fun ImageView.loadImage(context: Context, url: String?) {
    Glide.with(context).load(url).into(this)
}
fun ImageView.loadImageWithCache(context: Context, url: String?) {
    Glide.with(context).load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.loadProfileEditImage(context: Context, url: String?, default: Int) {
    Glide.with(context).load(url).error(default)
        .placeholder(default).into(this)

}


fun <T> ImageView.loadImage(context: Context, url: T) {
    Glide.with(context).load(url).into(this)

}

fun ImageView.loadImage(context: Context, url: Uri) {
    Glide.with(context).load(url).into(this)
}

fun ImageView.loadImage(context: Context, url: String?, error: Int) {
    Glide.with(context).load(url).error(error).placeholder(error).into(this)

}


fun ImageView.loadWatchImage(context: Context, url: String, default: Int) {
    Glide.with(context).load(url).placeholder(default)
        .error(default).into(this)
}

fun ImageView.loadCircleImage(context: Context, url: String, defaultImage: Int) {
    LOGS.d(url)
    Glide.with(context).load(url).placeholder(defaultImage).error(defaultImage).circleCrop()
        .into(this)

}

fun <T> ImageView.loadImage(context: Context, url: T, defaultImage: Int? = null) {
    if (defaultImage == null) {
        Glide.with(context).load(url).into(this)
    } else {
        Glide.with(context).load(url).placeholder(defaultImage).error(defaultImage).into(this)
    }


}

fun ImageView.loadCircleEmoji(context: Context, type: String) {

    var emoji: Drawable? = null
    when (type) {
        Emoji.EmojiHeart.emoji -> {
            emoji = ContextCompat.getDrawable(context, R.drawable.ic_heart_emoji)
        }
        Emoji.Emoji100.emoji -> {
            emoji = ContextCompat.getDrawable(context, R.drawable.ic_100_emoji)
        }
        Emoji.EmojiFire.emoji -> {
            emoji = ContextCompat.getDrawable(context, R.drawable.ic_fire_emoji)
        }
        Emoji.EmojiHand.emoji -> {
            emoji = ContextCompat.getDrawable(context, R.drawable.ic_strong_emoji)
        }
    }

    Glide.with(context).load(emoji).circleCrop().into(this)

}

fun <T> ImageView.loadCircleImage(context: Context, url: T, defaultImage: Int? = null) {

    if (defaultImage == null) {
        Glide.with(context).load(url).circleCrop().into(this)
    } else {
        Glide.with(context).load(url).placeholder(defaultImage).error(defaultImage).circleCrop()
            .into(this)
    }


}

//TODO: combine all image loading methods into one
fun <T> ImageView.loadImageWithoutCache(context: Context, url: T, defaultImage: Int? = null) {
    if (defaultImage == null) {
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).into(this)
    } else {
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).error(defaultImage).into(this)
    }


}

fun <T> ImageView.loadCircleImageWithoutCache(context: Context, url: T, defaultImage: Int? = null) {

    if (defaultImage == null) {
        Glide.with(context).load(url).circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).into(this)
    } else {
        Glide.with(context).load(url).error(defaultImage).diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).circleCrop().into(this)
    }


}

fun <T> ImageView.loadImageCacheWithProgress(context: Context, url: T) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    circularProgressDrawable.start()
    Glide.with(context)
        .load(url)
//        .diskCacheStrategy(DiskCacheStrategy.NONE)
//        .skipMemoryCache(true)
        .placeholder(circularProgressDrawable)
        .into(this)
}

fun <T> ImageView.loadCircleCacheWithProgress(context: Context, url: T) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    circularProgressDrawable.start()
    Glide.with(context)
        .load(url)
        .circleCrop()
//        .diskCacheStrategy(DiskCacheStrategy.NONE)
//        .skipMemoryCache(true)
        .placeholder(circularProgressDrawable)
        .into(this)
}

fun <T> ImageView.loadImageWCacheWithProgress(context: Context, url: T) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    circularProgressDrawable.start()
    Glide.with(context)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .placeholder(circularProgressDrawable)
        .into(this)
}

fun <T> ImageView.loadCircleWCacheWithProgress(context: Context, url: T) {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    circularProgressDrawable.start()
    Glide.with(context)
        .load(url)
        .circleCrop()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .placeholder(circularProgressDrawable)
        .into(this)
}

fun PopupWindow.dimBehind() {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = 0.4f
    wm.updateViewLayout(container, p)
}

fun PopupWindow.dimBehind80() {
    val container = contentView.rootView
    val context = contentView.context
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val p = container.layoutParams as WindowManager.LayoutParams
    p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    p.dimAmount = 0.8f
    wm.updateViewLayout(container, p)
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}


fun SearchView.onQueryTextChange(onQueryTextChange: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            onQueryTextChange.invoke(newText.toString())
            return true
        }

    })
}


fun Activity.displayToast(
    @StringRes message: Int
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}

fun Activity.displayToast(
    message: String,
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}

fun FragmentActivity?.safeFragClickListener(onClick: (FragmentActivity) -> Unit) {
    this?.takeIf { !it.isFinishing && !it.isDestroyed }?.let { activity ->
        onClick.invoke(activity)
    }
}

fun LinearProgressIndicator.setIndicatorColor1(color: Int) {
    this.setIndicatorColor(
        ContextCompat.getColor(
            this.context, color
        )
    )
}

fun LinearProgressIndicator.setTrackColor1(color: Int) {
    this.trackColor = (ContextCompat.getColor(
        this.context, color
    ))
}

fun <T : View> T.width(function: (Int) -> Unit) {
    if (width == 0) viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            function(width)
        }
    })
    else function(width)
}
//fun TextView.setHyperLinkText(string: String, startPos: Int, endPos: Int) {
//    val spannableString =
//        SpannableString(string)
//    spannableString.setSpan(
//        UnderlineSpan(),
//        startPos,
//        endPos,
//        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
//    )
//    spannableString.setSpan(
//        ForegroundColorSpan(resources.getColor(R.color.dark_sky_blue)),
//        startPos,
//        endPos,
//        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
//    )
//    this.text = spannableString
//}

fun TextView.makeLinks(
    isUnderline: Boolean, vararg links: Pair<String, View.OnClickListener>
) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                textPaint.color = textPaint.linkColor
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                if (isUnderline) {
                    textPaint.isUnderlineText = true
                }

            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
//      if(startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(com.noisefit_commans.R.color.link_color)),
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun IntRange.random() = Random().nextInt((endInclusive + 1) - start) + start


fun String.onlyNumber(): String {
    val re = "[^0-9]".toRegex()
    return re.replace(this, "")
}

fun String.numberWithSTDCode(): String {
    val re = "[^+0-9]".toRegex()
    return re.replace(this, "")
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.checkItemsAre() = if (all { it is T }) this as List<T>
else null


/*fun Context?.showPermissionDenialDialog(
    message: String, onAllowClicked: () -> Unit, onCancelClicked: () -> Unit
) {
    if (this == null) return
    val builder = MaterialAlertDialogBuilder(this*//*, R.style.AlertDialogTheme*//*)
    builder.setTitle(getString(R.string.text_permission_required))
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.text_allow)) { dialog, which ->
        dialog.cancel()
        onAllowClicked.invoke()
    }
    builder.setNegativeButton(getString(R.string.text_cancel)) { dialog, which ->
        dialog.cancel()
        onCancelClicked.invoke()
    }
    val alertDialog = builder.create()
    alertDialog.show()

}*/

fun String.spanText(startPos: Int, endPos: Int): Spannable {
    val wordToSpan: Spannable = SpannableString(this)
    wordToSpan.setSpan(
        Typeface.BOLD, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return wordToSpan
}

fun Long.numberFormatter(locale: Locale = Locale("en", "IN")): String {
    return NumberFormat.getNumberInstance(locale).format(this)
}

fun Long.countToDisplay(countValue: Long): String {
    try {
        if (countValue == null) {
            return "1k+"
        } else {
            val downCount = countValue.toInt()
            if (downCount >= 100000) {
                return downCount.prettyCountDecimal()
            }
            return "$downCount"
        }
    } catch (exp: Exception) {
        return "1k+"
    }

}


fun <E> List<E>.returnValueIfExist(index: Int): E? {
    val hasIndex = index >= 0 && index < this.size
    return if (hasIndex) {
        this[index]
    } else {
        null
    }
}

fun getCircleProgressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        start()
    }
}

fun delay(duration: Long, `do`: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(`do`, duration)
}

