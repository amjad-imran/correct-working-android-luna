package com.noisefit_commans.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import kotlinx.parcelize.Parcelize


@Parcelize
class LocationDataModel(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var mTitle: String = ""
) : Parcelable, ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    override fun getTitle(): String {
        return "Distance $mTitle"
    }

    override fun getSnippet(): String {
        return ""
    }

    override fun getZIndex(): Float? {
        return 0f
    }
}