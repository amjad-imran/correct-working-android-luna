package com.noisefit_commans.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.noisefit_commans.utils.prettyCountDecimal
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "watch_faces"
)
@Parcelize
data class WatchFace(
    @ColumnInfo(name = "uId")
    @PrimaryKey(autoGenerate = true) var uId: Int = 0,
    @ColumnInfo(name = "lastSync")
    var lastSync: Long? = System.currentTimeMillis(),
    @SerializedName("watchface_cat_id")
    var watchfaceCatId: Int? = null,
    @SerializedName("is_active")
    var active: Int? = null,
    @SerializedName("device_id")
    var deviceId: Int? = null,
    @SerializedName("image_url")
    var imageUrl: String? = null,
    @SerializedName("file_name")
    var fileName: String? = null,
    @SerializedName("face_id")
    var faceId: String? = null,
    var description: String? = null,
    @SerializedName("display_views")
    var displayViews: String? = null,
    @SerializedName("created_at")
    var createdAt: String? = null,
    @SerializedName("is_editable")
    var editable: Int? = null,
    @SerializedName("zip_name")
    var zipName: String? = null,
    @SerializedName("display_likes")
    var displayLikes: String? = null,
    @SerializedName("updated_at")
    var updatedAt: String? = null,
    @SerializedName("face_type")
    var faceType: Int? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("is_custom")
    var custom: Int? = null,
    @SerializedName("display_downloads")
    var displayDownloads: String? = null,
    @SerializedName("downloads")
    var downloads: String? = null,
    @SerializedName("in_use_count")
    var inUseCount: String? = null,
    @SerializedName("watchface_id")
    var id: Int? = null,
    @SerializedName("image_type")
    var imageType: String? = null,
    @SerializedName("file_url")
    var fileUrl: String? = null,
    @SerializedName("zip_file")
    var zip_file: String? = null,
    var is_favourite: String? = null
) : Parcelable, ColorfitData() {
    @Ignore
    var localFilePath: String = ""

    @Ignore
    var watchface_type: String = ""

    @Ignore
    var localImagePath: String = ""


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
                if (downCount >= 100000) {
                    return downCount.prettyCountDecimal()
                }
                return "$downCount"
            }
        } catch (exp: Exception) {
            return "1k+"
        }
    }

    fun getInUseToDisplay(): String {
        try {
            if (inUseCount == null) {
                return "1k+"
            } else {
                val downCount = inUseCount!!.toInt()
                if (downCount >= 100000) {
                    return downCount.prettyCountDecimal()
                }
                return "$downCount"
            }
        } catch (exp: Exception) {
            return "1k+"
        }
    }

    fun getZipFileName(): String? {
        return if (zip_file.isNullOrEmpty()) {
            "watchface_${id}"
        } else {
            try {
                val fileName = zip_file?.split("/")?.last()
                if (fileName.isNullOrEmpty()) {
                    "watchface_${id}"
                } else {
                    fileName.replace(".zip", "")
                }
            } catch (exp: Exception) {
                "watchface_${id}"
            }
        }
    }
}