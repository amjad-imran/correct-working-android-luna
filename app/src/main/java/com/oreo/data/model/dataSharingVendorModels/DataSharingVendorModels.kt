package com.oreo.data.model.dataSharingVendorModels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataSharingVendorListResponseItem(
    val consent: Boolean ?= null,
    val features: List<Feature> ?= null,
    val vendorId: String ?= null,
    val vendorIcon: String ?= null,
    val vendorName: String ?= null,

    // for app
    var type: DataSharingListEnum ?= null,
    val vendorIconDrawable: Int ?= null,
) : Parcelable

@Parcelize
data class Feature(
    val name: String ?= null,
    val value: String ?= null,
    val icon: String ?= null,
) : Parcelable

enum class DataSharingListEnum{
    GOOGLE_FIT, DYNAMIC_ITEM
}