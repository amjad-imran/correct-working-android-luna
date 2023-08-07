package com.noisefit.util.moveToServer

import android.content.Context
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.session.SessionManager
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.util.notif.NotificationEventsClass
import com.noisefit.util.notif.NotificationUtil
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import javax.inject.Inject

private const val TAG = "BatteryNotificationUtils"
private const val TITLE = "Luna Ring Battery Alert ⚠"
private const val TITLE_1 = "Luna Ring Battery Alert"
const val CHARGE_REMINDER = "CHARGING_REMINDER"

class BatteryNotificationUtils
@Inject
constructor(
    val localDataStore: DataStoredInterface,
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

    fun handleNotification(currentBatteryLevel: Int, isCharging: Boolean) {
        var batteryNotification = localDataStore.getBatteryNotification()
        val context = NoiseFitApplicationMain.context!!
        // LOGS.d("$TAG ${Gson().toJson(batteryNotification)}")
        if (batteryNotification == null || batteryNotification.batteryNotificationData.isNullOrEmpty()) {
            batteryNotification = com.noisefit_commans.data.model.BatteryNotification(
                getBatteryNotList(),
                currentBatteryLevel
            )
            setBatteryNotification(batteryNotification)
        }
        handleLastState(currentBatteryLevel)
        var notificationTriggerFor: com.noisefit_commans.data.model.BatteryNotificationType? = null
        val batteryList = batteryNotification.batteryNotificationData!!
        LOGS.d("$TAG ${currentBatteryLevel} last: ${batteryNotification.lastBatteryPercentage}")

        if (currentBatteryLevel < 25 && DateFormats.isTimeBetween(21)) {
            val hasNotificationTriggered = localDataStore.getChargeOverNightNotification()

            if (!hasNotificationTriggered && !isCharging) {
                LOGS.d("$TAG inside 9pm ")
                if (currentBatteryLevel < batteryNotification.lastBatteryPercentage) {
                    val message =
                        "Your Luna Ring battery level is $currentBatteryLevel%, Please charge before going to bed."


                    LOGS.d("$TAG $message")
                    pushBatteryNotification(context, TITLE, message, "3")
                    localDataStore.setChargeOverNightNotification(true)
                    batteryNotification.lastBatteryPercentage = currentBatteryLevel
                    logLastState(CHARGE_REMINDER, currentBatteryLevel)
                    setNotificationTriggerStatus(batteryNotification, null)
                    return
                }

            }

        } else {
            localDataStore.setChargeOverNightNotification(false)
        }


        //reset
        when (currentBatteryLevel) {
            in 0..5 -> {
                resetExcept(
                    batteryNotification,
                    com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL
                )
            }

            in 6..20 -> {
                resetExcept(
                    batteryNotification,
                    com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL_REMINDER
                )
            }

            in 95..100 -> {
                resetExcept(
                    batteryNotification,
                    com.noisefit_commans.data.model.BatteryNotificationType.FULL
                )
            }

            else -> {
                resetAllState(batteryNotification)

            }
        }


        //broadcast notification
        when (currentBatteryLevel) {
            in 0..5 -> {
                val searchedObj =
                    hasNotificationTriggered(
                        batteryList,
                        com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL
                    )

                if (searchedObj != null) {
                    //trigger the notification
                    if (currentBatteryLevel < batteryNotification.lastBatteryPercentage) {
                        val message =
                            "⚠️Your luna ring battery is extremely low $currentBatteryLevel% ⚠️Please plug the charger."
                        LOGS.d("$TAG $message")
                        pushBatteryNotification(context, TITLE, message, "1")
                        notificationTriggerFor =
                            com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL
                        logLastState(
                            com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL.name,
                            currentBatteryLevel
                        )
                    }

                }
            }

            in 6..20 -> {
                val searchedObj =
                    hasNotificationTriggered(
                        batteryList,
                        com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL_REMINDER
                    )
                if (searchedObj != null) {
                    //trigger the notification
                    if (currentBatteryLevel < batteryNotification.lastBatteryPercentage) {
                        val message =
                            "Your luna ring battery level is low $currentBatteryLevel%. Please plug the charger."
                        LOGS.d("$TAG $message")
                        pushBatteryNotification(context, TITLE_1, message, "0")
                        notificationTriggerFor =
                            com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL_REMINDER
                        logLastState(
                            com.noisefit_commans.data.model.BatteryNotificationType.CRITICAL_REMINDER.name,
                            currentBatteryLevel
                        )
                    }

                }
            }

            in 95..100 -> {
                val searchedObj =
                    hasNotificationTriggered(
                        batteryList,
                        com.noisefit_commans.data.model.BatteryNotificationType.FULL
                    )
                if (searchedObj != null) {
                    //trigger the notification
                    if (currentBatteryLevel > batteryNotification.lastBatteryPercentage && isCharging) {
                        val message =
                            "Your luna ring is sufficiently charged. Please unplug the charger."
                        LOGS.d("$TAG $message")
                        pushBatteryNotification(context, TITLE_1, message, "2")
                        notificationTriggerFor =
                            com.noisefit_commans.data.model.BatteryNotificationType.FULL
                        logLastState(
                            com.noisefit_commans.data.model.BatteryNotificationType.FULL.name,
                            currentBatteryLevel
                        )
                    }

                }
            }

            else -> {

            }

        }

        batteryNotification.lastBatteryPercentage = currentBatteryLevel
        setNotificationTriggerStatus(batteryNotification, notificationTriggerFor)

    }

    private fun setNotificationTriggerStatus(
        batteryNotification: com.noisefit_commans.data.model.BatteryNotification,
        batteryNotificationType: com.noisefit_commans.data.model.BatteryNotificationType?
    ) {
        batteryNotificationType?.let {
            batteryNotification.batteryNotificationData!!.forEach {
                if (batteryNotificationType == it.type) {
                    it.hasTriggered = true
                    return@forEach
                }

            }
        }
        setBatteryNotification(batteryNotification)
    }

    private fun setBatteryNotification(batteryNotification: com.noisefit_commans.data.model.BatteryNotification) {
        localDataStore.setBatteryNotification(batteryNotification)
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

