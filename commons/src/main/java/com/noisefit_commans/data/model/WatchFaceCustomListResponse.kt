package com.noisefit.data.remote.response

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.models.WatchFace
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Deprecated("no use in wf2.0")
data class WatchFaceCustomListResponse(
    val recentlyUsed: List<WatchFace>?,
    val topDownloaded: List<WatchFace>?,
    val designWise: List<WatchFace>?,
    val newlyAdded: List<WatchFace>?,
    var lastSync: Long = 0
)

@Deprecated("no use in wf2.0")
data class CatWiseWatchFacesItem(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("faces")
    val faces: List<WatchFace>
)

@Parcelize
data class DiyMyCreation(
    val id: Int,
    @SerializedName("background_url")
    val backgroundUrl: String = "",
    @SerializedName("filter_intensity")
    val filterIntensity: Int = 50,
    @SerializedName("text_layer_name")
    val textLayerName: String = "",
    @SerializedName("text_layer_link")
    val textLayerLink: String = "",
    @SerializedName("text_bin")
    val textBin: String = "",
    @SerializedName("text_type")
    val textType: String = "",
    val colour: String = "",
    val filter: String = ""
) : Parcelable {
    @IgnoredOnParcel
    var bgBitmap: Bitmap? = null

    @IgnoredOnParcel
    var textBitmap: Bitmap? = null


    @IgnoredOnParcel
    var colorMatrix: ColorMatrix? = null

}

data class WatchFaceCategory2(
    val id: Int,
    val limit: Int,
    val name: String,
    var faces: List<Watchface2>? = null
)


@Parcelize
data class Watchface2(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("image_url")
    val imageUrl: String = "",
    @SerializedName("rating")
    val rating: String? = null,
    @SerializedName("category_id")
    val cId: Int = 0,
    @SerializedName("watchface_id")
    val wId: Int = 0,
    @SerializedName("is_rated")
    val isRated: Boolean = false,
    @SerializedName("is_favourite")
    var isFavourite: String? = null,
    @SerializedName("zip_file")
    val zipFile: String? = null,
    @SerializedName("watchface_type")
    val watchface_type: String? = null,
) : Parcelable {

    fun setFav(fav: String) {
        isFavourite = if (fav == "true") {
            "1"
        } else {
            "0"
        }
    }

    fun isFav(): Boolean {
        return if (isFavourite.isNullOrEmpty()) {
            false
        } else {
            try {
                val isFav = isFavourite?.toInt() ?: 0
                isFav != 0
            } catch (exp: Exception) {
                false
            }
        }
    }
}
