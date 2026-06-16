# Issue 5: Fix Plan Improvements - Smart Sync Mode Implementation

**Date:** April 20, 2026  
**Status:** REFERENCE DOCUMENT  
**Purpose:** Detailed implementation guide IF smart mode selection is required in the future

---

## Context

The simple "always mode 3" fix has been implemented. This document provides a detailed implementation guide for smart mode selection if:
1. Battery optimization becomes critical
2. Sync times become too long
3. Future requirements demand differentiated sync behavior

---

## Improved Fix Plan

### Overview of Improvements

The original fix plan had some gaps. This improved version addresses:
1. Per-device tracking for multi-device users
2. Clear callback flow documentation
3. Handling for all edge cases
4. Testable implementation

---

## Phase 1: Add State Tracking to RingDataStore

### Step 1.1: Interface Changes

**File:** `commons/src/main/java/com/noisefit_commans/data/local/abstraction/RingDataStore.kt`

```kotlin
// Add to interface
interface RingDataStore {
    // ... existing methods ...
    
    /**
     * Check if initial historical sync has completed for the current device.
     * Per-device tracking prevents issues when switching devices.
     */
    fun hasCompletedInitialHistoricalSync(): Boolean
    
    /**
     * Mark initial historical sync as completed for the current device.
     */
    fun setInitialHistoricalSyncCompleted(completed: Boolean)
    
    /**
     * Get timestamp of last mode 3 (full) sync.
     */
    fun getLastFullSyncTime(): Long
    
    /**
     * Save timestamp of mode 3 (full) sync completion.
     */
    fun setLastFullSyncTime(time: Long)
    
    /**
     * Clear all sync tracking state.
     * Called on device unbind or user logout.
     */
    fun clearSyncTrackingState()
}
```

### Step 1.2: Implementation

**File:** `commons/src/main/java/com/noisefit_commans/data/local/implementation/RingDataStoreImpl.kt`

```kotlin
// Add implementation

private val KEY_INITIAL_SYNC_DONE_PREFIX = "initial_historical_sync_done_"
private val KEY_LAST_FULL_SYNC_TIME = "last_full_sync_time"

override fun hasCompletedInitialHistoricalSync(): Boolean {
    val deviceAddress = getRingDevice()?.address ?: return false
    return sharedPreferences.getBoolean(KEY_INITIAL_SYNC_DONE_PREFIX + deviceAddress, false)
}

override fun setInitialHistoricalSyncCompleted(completed: Boolean) {
    val deviceAddress = getRingDevice()?.address ?: return
    sharedPreferences.edit()
        .putBoolean(KEY_INITIAL_SYNC_DONE_PREFIX + deviceAddress, completed)
        .apply()
}

override fun getLastFullSyncTime(): Long {
    val deviceAddress = getRingDevice()?.address ?: return 0L
    return sharedPreferences.getLong(KEY_LAST_FULL_SYNC_TIME + "_" + deviceAddress, 0L)
}

override fun setLastFullSyncTime(time: Long) {
    val deviceAddress = getRingDevice()?.address ?: return
    sharedPreferences.edit()
        .putLong(KEY_LAST_FULL_SYNC_TIME + "_" + deviceAddress, time)
        .apply()
}

override fun clearSyncTrackingState() {
    val deviceAddress = getRingDevice()?.address ?: return
    sharedPreferences.edit()
        .remove(KEY_INITIAL_SYNC_DONE_PREFIX + deviceAddress)
        .remove(KEY_LAST_FULL_SYNC_TIME + "_" + deviceAddress)
        .apply()
}
```

**Key Improvement:** Per-device tracking using device address prevents issues when users switch between multiple devices.

---

## Phase 2: Add Contextual Sync Interface

### Step 2.1: Interface Method

**File:** `commons/src/main/java/com/noisefit_commans/interfaces/data/UserActivityDataActions.kt`

```kotlin
/**
 * Sync user activity data with explicit context control.
 * 
 * @param date The date to sync (empty string for current day)
 * @param syncContext Context determining which mode to use
 */
open fun syncUserActivityWithContext(
    date: String,
    syncContext: SyncContext = SyncContext.Regular
)

/**
 * Sync context for determining mode selection.
 */
enum class SyncContext {
    /**
     * First sync after device binding. Uses mode 3 (all data).
     */
    Initial,
    
    /**
     * Regular periodic sync. Uses mode 1 (today only).
     */
    Regular,
    
    /**
     * Reconnection after >24h disconnect. Uses mode 3 (all data).
     */
    LongDisconnectRecovery,
    
    /**
     * User explicitly requested full sync. Uses mode 3 (all data).
     */
    ManualFullSync
}
```

### Step 2.2: Handler Implementation

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

```kotlin
// Add state tracking
private var currentSyncContext: SyncContext = SyncContext.Regular

override fun syncUserActivityWithContext(
    date: String,
    syncContext: SyncContext
) {
    currentSyncContext = syncContext
    
    val resolvedMode = when (syncContext) {
        SyncContext.Initial -> DAILY_HISTORY_MODE_ALL
        SyncContext.Regular -> DAILY_HISTORY_MODE_TODAY
        SyncContext.LongDisconnectRecovery -> DAILY_HISTORY_MODE_ALL
        SyncContext.ManualFullSync -> DAILY_HISTORY_MODE_ALL
    }
    
    val deviceType = colorFitDevice?.deviceType ?: "unknown"
    val modeLabel = dailyHistoryModeLabel(resolvedMode)
    val contextLabel = syncContext.name
    
    LOGS.d(
        TAG,
        "syncUserActivityWithContext date=$date context=$contextLabel " +
        "deviceType=$deviceType resolvedMode=$modeLabel"
    )
    AppLogs.sendAppLogs(
        "$TRACK_TAG syncUserActivityWithContext date=$date context=$contextLabel " +
        "deviceType=$deviceType resolvedMode=$modeLabel"
    )
    
    // Only use mode selection for Luna Band
    if (colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        syncUserActivityByMode(date, resolvedMode)
    } else {
        syncUserActivityByMode(date, null)  // Legacy behavior
    }
}
```

---

## Phase 3: Add Sync Complete Callback

### Step 3.1: Callback Type

**File:** `commons/src/main/java/com/noisefit_commans/interfaces/data/UserActivityCallback.kt`

```kotlin
sealed class UserActivityCallback {
    // ... existing callbacks ...
    
    /**
     * Indicates that a full data sync (mode 3) has completed successfully.
     * App should update tracking flags accordingly.
     * 
     * @param context The sync context that was used
     */
    data class FullSyncCompleted(val context: SyncContext) : UserActivityCallback()
}
```

### Step 3.2: Emit Callback on Completion

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

In `fitnessDataCallBack.onProgress()`:

```kotlin
override fun onProgress(progress: Int, total: Int) {
    LOGS.d(TAG, "onProgress : 进度 $progress  总数 $total")
    AppLogs.sendAppLogs("$TRACK_TAG on progress $progress  总数 $total")
    
    when (progress) {
        0 -> {
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncEvents.Started(progress, total)
                )
            )
            AppLogs.sendAppLogs("$TRACK_TAG Sync data start")
        }

        total -> {
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncEvents.Success(progress, total)
                )
            )
            
            // Notify if this was a full sync
            if (currentSyncContext != SyncContext.Regular) {
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.FullSyncCompleted(currentSyncContext)
                )
            }
            
            // Reset context for next sync
            currentSyncContext = SyncContext.Regular

            ControlBleTools.getInstance().ringExecutesDeleteDailyData(1,
                object : ParsingStateManager.SendCmdStateListener() {
                    override fun onState(p0: SendCmdState?) {
                        LOGS.d(TAG, "ringExecutesDeleteDailyData onState $p0")
                    }
                })

            AppLogs.sendAppLogs("$TRACK_TAG Sync data complete")
        }

        else -> {
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.UserDataSyncUpdated(
                    SyncEvents.InProgress(progress, total)
                )
            )
        }
    }
}
```

---

## Phase 4: Integrate with RingConnectionService

### Step 4.1: Initial Sync Trigger

**File:** `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`

Add after successful connection validation:

```kotlin
/**
 * Checks if initial historical sync is needed and triggers it.
 * Called after device connection is validated.
 */
private fun checkAndTriggerInitialHistoricalSync() {
    val device = ringDataStore.getRingDevice()
    val deviceType = device?.deviceType
    
    // Only for Luna Band
    if (!deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        LOGS.d(TAG, "checkAndTriggerInitialHistoricalSync: Not Luna Band, skipping")
        return
    }
    
    val hasCompletedInitialSync = ringDataStore.hasCompletedInitialHistoricalSync()
    LOGS.d(TAG, "checkAndTriggerInitialHistoricalSync: " +
        "hasCompleted=$hasCompletedInitialSync deviceType=$deviceType")
    
    if (!hasCompletedInitialSync) {
        LOGS.d(TAG, "Triggering initial historical sync for Luna Band")
        AppLogs.sendAppLogs("$TAG Triggering initial historical sync")
        
        CommonGlobals.userActivityDataActions?.syncUserActivityWithContext(
            date = "",
            syncContext = SyncContext.Initial
        )
    } else {
        // Check if reconnection sync needed
        checkAndTriggerReconnectionSync()
    }
}

/**
 * Checks if full sync is needed due to long disconnection (>24h).
 */
private fun checkAndTriggerReconnectionSync() {
    val lastFullSyncTime = ringDataStore.getLastFullSyncTime()
    val hoursSinceLastFullSync = 
        (System.currentTimeMillis() - lastFullSyncTime) / (1000 * 60 * 60)
    
    if (hoursSinceLastFullSync > 24) {
        LOGS.d(TAG, "Triggering full sync on reconnect (${hoursSinceLastFullSync}h since last)")
        AppLogs.sendAppLogs("$TAG Triggering full sync on reconnect")
        
        CommonGlobals.userActivityDataActions?.syncUserActivityWithContext(
            date = "",
            syncContext = SyncContext.LongDisconnectRecovery
        )
    } else {
        // Regular sync
        CommonGlobals.userActivityDataActions?.syncUserActivityWithContext(
            date = "",
            syncContext = SyncContext.Regular
        )
    }
}
```

### Step 4.2: Handle Sync Complete Callback

In `sessionManager.userActivityCallback.observe`:

```kotlin
is UserActivityCallback.FullSyncCompleted -> {
    when (it.context) {
        SyncContext.Initial -> {
            LOGS.d(TAG, "Initial historical sync completed")
            ringDataStore.setInitialHistoricalSyncCompleted(true)
            ringDataStore.setLastFullSyncTime(System.currentTimeMillis())
            AppLogs.sendAppLogs("$TAG Initial historical sync marked complete")
        }
        SyncContext.LongDisconnectRecovery -> {
            LOGS.d(TAG, "Long disconnect recovery sync completed")
            ringDataStore.setLastFullSyncTime(System.currentTimeMillis())
            AppLogs.sendAppLogs("$TAG Recovery sync marked complete")
        }
        SyncContext.ManualFullSync -> {
            LOGS.d(TAG, "Manual full sync completed")
            ringDataStore.setLastFullSyncTime(System.currentTimeMillis())
        }
        SyncContext.Regular -> {
            // Should not happen, but handle gracefully
        }
    }
}
```

### Step 4.3: Clear State on Unbind

In device unbind handling:

```kotlin
private fun handleDeviceUnbind() {
    // ... existing unbind code ...
    
    // Clear sync tracking state for this device
    ringDataStore.clearSyncTrackingState()
    LOGS.d(TAG, "Cleared sync tracking state on unbind")
}
```

---

## Phase 5: Testing Checklist

### Test Case 1: First Device Binding

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Fresh app install | No sync flags set |
| 2 | Pair Luna Band with accumulated data | `hasCompletedInitialHistoricalSync() = false` |
| 3 | Wait for sync trigger | Mode 3 sync triggered |
| 4 | Sync completes | `FullSyncCompleted(Initial)` callback received |
| 5 | Check flags | `hasCompletedInitialHistoricalSync() = true` |
| 6 | Verify data | Historical data present in app |

### Test Case 2: Regular Daily Sync

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Device already paired, initial sync done | - |
| 2 | Trigger periodic sync | Mode 1 sync triggered |
| 3 | Verify SDK log | `getDailyHistoryData() : 1` |

### Test Case 3: Long Disconnect Recovery

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Disconnect device for >24 hours | - |
| 2 | Reconnect device | `hoursSinceLastFullSync > 24` |
| 3 | Wait for sync trigger | Mode 3 sync triggered |
| 4 | Verify context | `LongDisconnectRecovery` context used |

### Test Case 4: Device Unbind/Rebind

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Unbind device | `clearSyncTrackingState()` called |
| 2 | Check flags | All tracking flags cleared |
| 3 | Pair same/different device | `hasCompletedInitialHistoricalSync() = false` |
| 4 | Wait for sync | Mode 3 sync triggered |

### Test Case 5: Multiple Devices

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Pair Device A, complete initial sync | Flag set for Device A |
| 2 | Switch to Device B (different address) | Flag NOT set for Device B |
| 3 | Wait for sync | Mode 3 sync triggered for Device B |
| 4 | Switch back to Device A | Regular sync (flag already set) |

---

## Summary of Files to Change

| File | Changes | Lines ~|
|------|---------|--------|
| `RingDataStore.kt` | Add 5 interface methods | 20 |
| `RingDataStoreImpl.kt` | Implement 5 methods | 35 |
| `UserActivityDataActions.kt` | Add contextual sync method, SyncContext enum | 25 |
| `UserActivityCallback.kt` | Add FullSyncCompleted callback | 8 |
| `ZhUserActivityHandler.kt` | Add contextual sync impl, emit callback | 45 |
| `RingConnectionService.kt` | Add sync trigger, handle callback, clear on unbind | 60 |
| **Total** | | **~193** |

---

## When to Use This Implementation

Use the smart mode selection if:
- Battery consumption reports indicate sync is a significant drain
- Sync times exceed user expectations (>30 seconds regularly)
- Future features require distinguishing sync types

Otherwise, keep the simpler "always mode 3" implementation.
