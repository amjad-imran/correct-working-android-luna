package com.noisefit_commans.data.model

import android.graphics.drawable.Drawable
import android.os.Parcelable
import com.noisefit_commans.enums.ApplicationType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationApp(
    val image: Int,
    val appCode: ApplicationType,
    val appDisplayName: String,
    val appPackageName: String?,
    var isEnabled: Boolean = false,
    var isWatchSupported: Boolean = false,

) : Parcelable{
    @IgnoredOnParcel
    var imageDrawable: Drawable?=null
}