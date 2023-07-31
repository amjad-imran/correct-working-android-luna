package com.noisefit_commans.utils

import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowInsets
import android.view.WindowMetrics
import com.noisefit_commans.R
import javax.inject.Inject
import kotlin.math.roundToInt


const val ITEM_HEIGHT = 56

class ScreenUtils
@Inject
constructor() {

    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }

    }

    fun dpToPx(px: Int, context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        )
    }

    fun dpToPx2(dp: Int, context: Context): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    fun pxToDp(px: Int, context: Context): Int {
        return (px / context.resources.displayMetrics.density).roundToInt()
    }


    fun getPadding(context: Context): Int {
        val rvHeight = context.resources.getDimension(R.dimen.recycler_height_spinner)
        val dpHeight = pxToDp(rvHeight.roundToInt(), context)
        return dpToPx(
            (dpHeight / 2) - (ITEM_HEIGHT / 2),
            context
        ).roundToInt()
    }


}