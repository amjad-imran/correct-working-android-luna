package com.noisefit.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MentionUser(
    val id: Long,
    val image_url: String? = null,
    val first_name: String? = null,
    val is_active: Boolean? = null,
    var start_pos: Int? = -1,
) : Parcelable {
    fun getFirstName(): String {
        val userName = first_name
        if (!userName.isNullOrEmpty()) {
            val names = userName.split(" ")
            return names.first()
        }
        return ""
    }
}
