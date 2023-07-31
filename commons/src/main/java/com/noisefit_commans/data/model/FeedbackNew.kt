package com.noisefit_commans.data.model

class FeedbackNew(
    var platform: String,
    var mobileDevice: String,
    var osVersion: String,
    var appVersion: String,
    var watchName: String,
    var watchFirmwareVersion: String,
    var rating: Int,
    var problemType: String,
    var suggestions: String,
    var date: String,
    var user_id: Int? = null,
)