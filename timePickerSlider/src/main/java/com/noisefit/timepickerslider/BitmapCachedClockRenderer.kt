package com.noisefit.timepickerslider

interface BitmapCachedClockRenderer: ClockRenderer {
    var isBitmapCacheEnabled: Boolean

    fun invalidateBitmapCache()
    fun recycleBitmapCache()
}