package com.oreo.util.alarm

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import com.noisefit.NoiseFitApplicationMain
import com.noisefit_commans.utils.LOGS
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

object WakeLockManager {
    private val wakelockCounter = AtomicInteger(0)
    private val wakeLockIds = CopyOnWriteArrayList<Int>()
    val pm =
        NoiseFitApplicationMain.context!!.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val serviceWakelock =
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SimpleAlarmClock:AlertServiceWrapper")
    private val transitionWakelock =
        pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SimpleAlarmClock:AlertServicePusher")

    fun acquireServiceLock() {
        LOGS.d { "Acquired service wakelock" }
        serviceWakelock.acquire(60 * 60_000)
    }

    fun releaseServiceLock() {
        if (serviceWakelock.isHeld) {
            LOGS.d { "Released service wakelock" }
            serviceWakelock.release()
        }
    }

    /**
     * Acquires a partial [WakeLock], stores it internally and puts the tag into the [Intent]. To be
     * used with [WakeLockManager.releaseTransitionWakeLock]
     */
    fun acquireTransitionWakeLock(intent: Intent) {
        transitionWakelock.acquire(60 * 1000)
        wakelockCounter.incrementAndGet().also { count ->
            wakeLockIds.add(count)
            intent.putExtra(COUNT, count)
            LOGS.d { "Acquired $transitionWakelock #$count" }
        }
    }

    /**
     * Releases a partial [WakeLock] with a tag contained in the given [Intent]
     *
     * @param intent
     */
    fun releaseTransitionWakeLock(intent: Intent) {
        val count = intent.getIntExtra(COUNT, -1)
        val wasRemoved = wakeLockIds.remove(count)
        if (wasRemoved && transitionWakelock.isHeld) {
            transitionWakelock.release()
            LOGS.d { "Released $transitionWakelock #$count" }
        }
    }
    const val COUNT = "WakeLockManager.COUNT"

}
