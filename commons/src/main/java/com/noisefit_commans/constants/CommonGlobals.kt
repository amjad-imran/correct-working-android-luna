package com.noisefit_commans.constants

import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice

object CommonGlobals {

    var version: String? = null
    var hasIconBuzzWatchFaces: Boolean = false
    var devices = ArrayList<ColorFitDevice>()

    var songName: String? = null
    var connectionDataActions: ConnectionDataActions? = null

    var screenDuration = 5
    var smartDndSwitch = false
    var isWatchDataUpdating: Boolean = false
}