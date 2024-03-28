package com.noisefit.watch

import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.interfaces.data.UserActivityDataActions
import com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions
import com.noisefit_commans.models.ColorFitDevice

object CommonGlobals {



    var connectionDataActions: ConnectionDataActions? = null

    var userActivityDataActions : UserActivityDataActions? = null
    var updateDeviceDataActions : UpdateDeviceDataActions? = null
    var queryDeviceDataActions : QueryDeviceDataActions? = null

}