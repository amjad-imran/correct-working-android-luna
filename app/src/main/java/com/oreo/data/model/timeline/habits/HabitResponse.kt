package com.oreo.data.model.timeline.habits

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HabitsResponse(
    @SerializedName("options") var options: ArrayList<Options> = arrayListOf()
) : Parcelable

@Parcelize
data class Items(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("options") var options: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("sub_id") var subId: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null
) : Parcelable

@Parcelize
data class Options(
    @SerializedName("type") var type: String? = null,
    @SerializedName("typeLabel") var typeLabel: String? = null,
    @SerializedName("items") var items: ArrayList<Items> = arrayListOf()
) : Parcelable


