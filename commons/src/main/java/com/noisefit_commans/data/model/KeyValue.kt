package com.noisefit_commans.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "key_value"
)
@Parcelize
data class KeyValue(
    @ColumnInfo(name = "uId")
    @PrimaryKey(autoGenerate = true) var uId: Int = 0,
    @ColumnInfo(name = "lastSync")
    var lastSync: Long? = System.currentTimeMillis(),

    @SerializedName("key")
    var key: String? = null,

    @SerializedName("type")
    var type: String? = null,

    @SerializedName("value")
    var value: String? = null

) : Parcelable {


    fun getSafeLastSyncValue(): Long {
        return lastSync ?: 0
    }
}