package com.noisefit.util.moveToServer

import android.content.Context
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.session.SessionManager
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.data.local.abstraction.ChargingNotificationLevel
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

private const val TAG = "BatteryNotificationUtils"
private const val TITLE = "Luna Ring Battery Alert"
private const val TITLE_1 = "Luna Ring Battery Alert"
const val CHARGE_REMINDER = "CHARGING_REMINDER"

class BatteryNotificationUtils
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val watchDataStore: WatchDataStore,
    val sessionManager: SessionManager
) {


    private fun getBatteryNotList(): List<com.noisefit_commans.data.model.BatteryNotificationData> {
        val batteryNotList = ArrayList<com.noisefit_commans.data.model.BatteryNotificationData>()
        batteryNotList.add(
            com.noisefit_commans.data.model.BatteryNotificationData(
                com.noisefit_commans.data.model.BatteryNotificationType.FULL,
                false
            )
        )
        batteryNotList.add(
            com.noisefit_commans.data.model.BatteryNotificationData(
                com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL_REMINDER,
                false
            )
        )
        batteryNotList.add(
            com.noisefit_commans.data.model.BatteryNotificationData(
                com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL,
                false
            )
        )


        return batteryNotList
    }

    private fun resetExcept(
        batteryNotification: com.noisefit_commans.data.model.BatteryNotification,
        batteryNotificationType: com.noisefit_commans.data.model.BatteryNotificationType
    ) {
        batteryNotification.batteryNotificationData!!.forEach {
            if (batteryNotificationType != it.type) {
                it.hasTriggered = false
            }

        }
    }

    private fun resetAllState(
        batteryNotification: com.noisefit_commans.data.model.BatteryNotification
    ) {
        batteryNotification.batteryNotificationData!!.forEach {
            it.hasTriggered = false
        }
    }

    /* fun handleBatteryNotification(
         currentBatteryLevel: Int
     ) {

         if (currentBatteryLevel <= 20) {
             val message ="Your ring battery is below 20%. Please charge your ring to get uninterrupted insights."

             val notificationShown = watchDataStore.getChargingNotificationsShown()

             val level = when (currentBatteryLevel) {
                 in 0..5 -> ChargingNotificationLevel.LEVEL_5
                 in 6..10 -> ChargingNotificationLevel.LEVEL_10
                 in 11..15 -> ChargingNotificationLevel.LEVEL_15
                 in 16..20 -> ChargingNotificationLevel.LEVEL_20
                 else -> null
             } ?: return

             if (notificationShown[level.name] == true) {
                 return
             }
             //watchDataStore.setChargingNotificationShown(level)
             pushBatteryNotification(NoiseFitApplicationMain.context!!, TITLE, message, "3")
         }
     }*/

    fun handleBatteryNotification(
        currentBatteryLevel: Int
    ) {

        if (currentBatteryLevel < 20) {
            val message =
                "Your ring battery is below 20%. Please charge your ring to get uninterrupted insights."

            val notificationShown = watchDataStore.getChargingNotificationsShown()

            val level = when (currentBatteryLevel) {
                in 0..10 -> ChargingNotificationLevel.LEVEL_10
                in 11..20 -> ChargingNotificationLevel.LEVEL_20
                else -> null
            } ?: return

            if (notificationShown[level.name] == true) {
                return
            }
            watchDataStore.setChargingNotificationShown(level)
            pushBatteryNotification(NoiseFitApplicationMain.context!!, TITLE, message, "3")
        } else {
            watchDataStore.resetChargingNotificationData()
        }
    }


    fun handleBatteryNotification(
        currentBatteryLevel: Int,
        lastBatteryLevel: Int,
        isCharging: Boolean
    ) {
        if (isCharging) {
            watchDataStore.resetChargingNotificationData()
            return
        }

        if (currentBatteryLevel > lastBatteryLevel) {
            if (currentBatteryLevel > 20) {
                watchDataStore.resetChargingNotificationData()
            }
            return
        }

        if (currentBatteryLevel <= 20) {
            val message =
                "Your Luna Ring battery level is $currentBatteryLevel%"

            val notificationShown = watchDataStore.getChargingNotificationsShown()

            val level = when (currentBatteryLevel) {
                in 0..5 -> ChargingNotificationLevel.LEVEL_5
                in 6..10 -> ChargingNotificationLevel.LEVEL_10
                in 11..15 -> ChargingNotificationLevel.LEVEL_15
                in 16..20 -> ChargingNotificationLevel.LEVEL_20
                else -> null
            } ?: return

            if (notificationShown[level.name] == true) {
                return
            }
            watchDataStore.setChargingNotificationShown(level)
            pushBatteryNotification(NoiseFitApplicationMain.context!!, TITLE, message, "3")
        }
    }


    private fun logLastState(type: String, batteryPercentage: Int) {
//        sessionManager.logFirebaseEvent(
//            AppEvents.LOW_BATTERY_NOTIFICATION,
//            HashMap<String, Any>().apply {
//                this["type"] = type.lowercase()
//            }
//        )
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.LOW_BATTERY_NOTIFICATION,
            HashMap<String, Any>().apply {
                this["type"] = type.lowercase()
            })
        localDataStore.setLastBatteryNotificationState(
            com.noisefit_commans.data.model.BatteryNotificationLastStateData(
                batteryPercentage,
                DateFormats.getTimeStamp(),
                type
            )
        )
    }


    private fun handleLastState(batteryPercentage: Int) {
        val lastState = localDataStore.getLastBatteryNotificationState() ?: return
        val difference = kotlin.math.abs(DateFormats.getTimeStamp() - lastState.lastTimeStamp)
        if (difference > 7200 * 1000L) {
            LOGS.d("$TAG time out. battery has not been charged yet")
            localDataStore.setLastBatteryNotificationState(null)
            return
        }
        if (batteryPercentage <= lastState.batteryPercentage) {
            LOGS.d("$TAG abort batteryPercentage: $batteryPercentage lastBatteryPercentage ${lastState.batteryPercentage}")
            return
        }

        LOGS.d("$TAG Low battery charged event fired")
//        sessionManager.logFirebaseEvent(
//            AppEvents.LOW_BATTERY_CHARGED,
//            HashMap<String, Any>().apply {
//                this["last_battery"] = lastState.batteryPercentage
//                this["current_battery"] = batteryPercentage
//            }
//        )
        sessionManager.logInsiderAppEvent(
            InsiderAppEvents.LOW_BATTERY_CHARGED,
            HashMap<String, Any>().apply {
                this["last_battery"] = lastState.batteryPercentage
                this["current_battery"] = batteryPercentage
            })

        localDataStore.setLastBatteryNotificationState(null)

    }


    private fun hasNotificationTriggered(
        batteryList: List<com.noisefit_commans.data.model.BatteryNotificationData>,
        batteryNotificationType: com.noisefit_commans.data.model.BatteryNotificationType
    ): com.noisefit_commans.data.model.BatteryNotificationData? {
        return batteryList.find { batteryObj ->
            batteryObj.type == batteryNotificationType && !batteryObj.hasTriggered
        }
    }

    private fun pushBatteryNotification(
        context: Context,
        title: String,
        content: String,
        index: String
    ) {
        NotificationUtil.pushNotification(
            context,
            title,
            content,
            NotificationEventsClass.NOTIFICATION_TYPE_WATCH_LOW_BATTERY,
            index
        )
    }
}

