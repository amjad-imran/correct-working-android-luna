package com.noisefit.watch

import com.noisefit_commans.interfaces.connection.ConnectionDataActions
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject


class ConnectionHandler
@Inject
constructor(
    private val nfhConnection: ConnectionDataActions,
    private val cf2Connection: ConnectionDataActions,
    private val navPlusConnection: ConnectionDataActions,
    private val zhConnection: ConnectionDataActions,
    private val proConnectHandler: ConnectionDataActions,
    private val evolveConnectHandler: ConnectionDataActions,
    private val ryeexxConnectHandler: ConnectionDataActions,
    private val watchesSdk: WatchesSDK
) {


    fun getConnectionActions(connectedDevice: ColorFitDevice): ConnectionDataActions? {
        when (watchesSdk.getWatchType(connectedDevice)) {
            SDKWatchType.SDK_CF_PRO -> {
                cf2Connection.init()
                cf2Connection.attachCallbacks()
                CommonGlobals.connectionDataActions = cf2Connection
                AppLogs.sendAppLogs("ConnectionData Actions for Colorfit Pro SDK")

            }
            SDKWatchType.SDK_QUBE -> {
                proConnectHandler.init()
                proConnectHandler.attachCallbacks()
                CommonGlobals.connectionDataActions = proConnectHandler
                AppLogs.sendAppLogs("ConnectionData Actions for Qube SDK")

            }
            SDKWatchType.SDK_EVOLVE -> {
                evolveConnectHandler.init()
                evolveConnectHandler.attachCallbacks()
                CommonGlobals.connectionDataActions = evolveConnectHandler
                AppLogs.sendAppLogs("ConnectionData Actions for Evolve SDK")

            }
            SDKWatchType.SDK_HYBRID -> {
                nfhConnection.init()
                nfhConnection.attachCallbacks()
                CommonGlobals.connectionDataActions = nfhConnection
                AppLogs.sendAppLogs("ConnectionData Actions for Hybrid SDK")

            }
            SDKWatchType.SDK_NAV_PLUS -> {
                navPlusConnection.init()
                navPlusConnection.attachCallbacks()
                CommonGlobals.connectionDataActions = navPlusConnection
                AppLogs.sendAppLogs("ConnectionData Actions for NAV Plus SDK")

            }
            SDKWatchType.SDK_ZH->{
                zhConnection.init()
                zhConnection.attachCallbacks()
                CommonGlobals.connectionDataActions = zhConnection
                AppLogs.sendAppLogs("ConnectionData Actions for ZH SDK")

            }
            SDKWatchType.SDK_RYEEX->{
                ryeexxConnectHandler.init()
                ryeexxConnectHandler.attachCallbacks()
                CommonGlobals.connectionDataActions = ryeexxConnectHandler
                LOGS.i("SDK_INIT", "SDKWatchType.SDK_RYEEX")
                AppLogs.sendAppLogs("ConnectionData Actions for ZH SDK")

            }

        }
        return CommonGlobals.connectionDataActions
    }

    fun getConnectionActions(): ConnectionDataActions? {
        return CommonGlobals.connectionDataActions
    }

}

