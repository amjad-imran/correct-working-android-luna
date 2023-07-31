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
            SDKWatchType.SDK_CF_PRO -> {


            }
            SDKWatchType.SDK_QUBE -> {


            }
            SDKWatchType.SDK_EVOLVE -> {


            }
            SDKWatchType.SDK_HYBRID -> {


            }
            SDKWatchType.SDK_NAV_PLUS -> {


            }
            SDKWatchType.SDK_ZH->{
                zhConnection.init()
                zhConnection.attachCallbacks()
                CommonGlobals.connectionDataActions = zhConnection
                AppLogs.sendAppLogs("ConnectionData Actions for ZH SDK")

            }
            SDKWatchType.SDK_RYEEX->{


            }

        }
        return CommonGlobals.connectionDataActions
    }

    fun getConnectionActions(): ConnectionDataActions? {
        return CommonGlobals.connectionDataActions
    }

}

