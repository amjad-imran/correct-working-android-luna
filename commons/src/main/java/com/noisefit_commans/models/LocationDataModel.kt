package com.noisefit_commans.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class LocationDataModel(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var speed: Float = 0f,
    var accuracy: Float = 0f,
    var isHasSpeed: Boolean = false,
    var isRunning: Boolean = false,
    var altitude: Double = 0.0,
    var bearing: Float = 0f,
    var time: Long = 0
) : Parcelable


@Parcelize
class WeatherDataModel(
    var temp: Double = 0.0,
    var uvIndex: Double = 0.0,
    var humidity: Double = 0.0,
    var timeStamp: Long = 0L,
) : Parcelable