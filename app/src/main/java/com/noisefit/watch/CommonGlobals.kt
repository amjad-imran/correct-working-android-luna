package com.noisefit.watch

import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.models.ColorFitDevice

object CommonGlobals {
    fun clearCallbackHandlers() {
        connectionDataActions?.removeCallbacks()
        connectionDataActions = null
        userActivityDataActions?.removeCallbacks()
        userActivityDataActions = null
        updateDeviceDataActions?.removeCallbacks()
        updateDeviceDataActions = null
        queryDeviceDataActions?.removeCallbacks()
        queryDeviceDataActions = null
    }

    const val STEP_LENGTH = 0.8
    var incomingNumber: String? = null

    var devices = ArrayList<ColorFitDevice>()

    var connectionDataActions: ConnectionDataActions? = null

    var userActivityDataActions : UserActivityDataActions? = null
    var updateDeviceDataActions : UpdateDeviceDataActions? = null
    var queryDeviceDataActions : QueryDeviceDataActions? = null
    var isWatchDataUpdating: Boolean = false

}