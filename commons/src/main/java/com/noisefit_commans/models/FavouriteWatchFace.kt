package com.noisefit_commans.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "favourite_watch_faces"
)
@Parcelize
data class FavouriteWatchFace(
    @ColumnInfo(name = "uId")
    @PrimaryKey(autoGenerate = true) var uId: Int = 0,
    @ColumnInfo(name = "lastSync")
    var lastSync: Long? = System.currentTimeMillis(),

    @SerializedName("image_url")
    var imageUrl: String? = null,


    var description: String? = null,


    @SerializedName("name")
    var name: String? = null,


    @SerializedName("downloads")
    var downloads: String? = null,

    @SerializedName("watchface_id")
    var id: Int? = null,
    @SerializedName("image_type")
    var imageType: String? = null,

    var is_favourite: String? = null
) : Parcelable, ColorfitData() {

    fun isFav(): Boolean {
        return if (is_favourite.isNullOrEmpty()) {
            false
        } else {
            try {
                val isFav = is_favourite?.toInt() ?: 0
                isFav != 0
            } catch (exp: Exception) {
                false
            }
        }
    }

    fun getDownloadsToDisplay(): String {
        try {
            if (downloads == null) {
                return "1k+"
            } else {
                val downCount = downloads!!.toInt()
                if (downCount >= 10000) {
                    return "$downCount"
                }
            }
        } catch (exp: Exception) {
            return "1k+"
        }

        return "1k+"
    }

}