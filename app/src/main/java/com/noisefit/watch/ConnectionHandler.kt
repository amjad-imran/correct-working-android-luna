package com.noisefit.watch

import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


class ConnectionHandler
@Inject
constructor(
    private val zhConnection: ConnectionDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getConnectionActions(connectedDevice: ColorFitDevice): ConnectionDataActions? {
        when (watchesSdk.getWatchType(connectedDevice)) {

            SDKWatchType.SDK_ZH->{
                zhConnection.init()
                zhConnection.attachCallbacks()
                CommonGlobals.connectionDataActions = zhConnection
                AppLogs.sendAppLogs("ConnectionData Actions for ZH SDK")

            }

        }
        return CommonGlobals.connectionDataActions
    }

    fun getConnectionActions(): ConnectionDataActions? {
        return CommonGlobals.connectionDataActions
    }

}

