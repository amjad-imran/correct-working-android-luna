package com.noisefit_commans.data.model.matches

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "sport_event"
)
@Parcelize
data class SportEvent(
    @PrimaryKey(autoGenerate = true) var uId: Int = 0,
    var eventId: String,
    var timeInMilliseconds: Long,
    var date: String,
    var time: String,
    var title: String,
    val venue: String
) : Parcelable{
    @Ignore
    var lastCheckInDb: Long = 0
}