package com.noisefit_commans.data.model

import android.net.Uri
import java.io.File

class Feedback(
    var platform: String,
    var mobileDevice: String,
    var osVersion: String,
    var appVersion: String,
    var watchName: String,
    var watchFirmwareVersion: String,
    var problemType: String,
    var problemDesc: String,
    var date: String,
    var screenShortList: List<Uri>,
    var file: File?,
    var watchLogs: File?,
    var user_id: Int? = null,
)