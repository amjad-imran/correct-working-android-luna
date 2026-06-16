# Issue 5: Historical Data Not Synchronizing - Fix Plan

**Date:** April 20, 2026  
**Status:** FIX PLAN READY  
**Severity:** Critical  
**Related:** [issue-5-historical-data-sync-diagnosis-2026-04-20.md](issue-5-historical-data-sync-diagnosis-2026-04-20.md)

---

## Objective

Implement proper historical data synchronization for Luna Band devices by:
1. Triggering historical sync (mode 2 or mode 3) at appropriate times
2. Preserving efficient daily syncs (mode 1) for regular operations
3. Preventing duplicate syncs and excessive battery usage

---

## SDK API Reference

```java
/**
 * @param mode 模式 1：传输当天  2:传输历史  3:传输所有 
 *             Mode 1: Transmit on the same day (today only)
 *             Mode 2: Transmit history (past days only)
 *             Mode 3: Transmit all (today + history)
 */
public void getDailyHistoryData(int mode, ParsingStateManager.SendCmdStateListener listener)
```

---

## Fix Strategy

### Strategy A: Use Mode 3 (All Data) for Initial Sync

**Trigger historical sync with mode 3 at these key moments:**

1. **First device binding/pairing** - Sync all accumulated data
2. **First connection after app reinstall** - Recover historical data
3. **First connection after user login** - Ensure data continuity
4. **Optional: Weekly full sync** - Catch any missed data

### Strategy B: Smart Mode Selection

**Dynamically select mode based on context:**

| Context | Mode | Reason |
|---------|------|--------|
| First bind | 3 (All) | Get all device data |
| Regular periodic sync | 1 (Today) | Efficient daily sync |
| Reconnect after >24h | 3 (All) | May have missed days |
| Manual refresh | 1 (Today) | Quick refresh |
| User requests historical | 2 (History) | Explicit request |

---

## Implementation Plan

### Phase 1: Add Historical Sync Trigger (Priority: Critical)

#### Step 1.1: Track First Sync State

**File:** `app/src/main/java/com/oreo/data/db/preferences/RingDataStore.kt`

**Add methods:**

```kotlin
// Add to RingDataStore interface and implementation
fun hasCompletedInitialHistoricalSync(): Boolean
fun setInitialHistoricalSyncCompleted(completed: Boolean)

// Track last full sync time for smart mode selection
fun getLastFullSyncTime(): Long
fun setLastFullSyncTime(time: Long)
```

**Implementation:**

```kotlin
private val KEY_INITIAL_HISTORICAL_SYNC_DONE = "initial_historical_sync_done"
private val KEY_LAST_FULL_SYNC_TIME = "last_full_sync_time"

override fun hasCompletedInitialHistoricalSync(): Boolean {
    return sharedPreferences.getBoolean(KEY_INITIAL_HISTORICAL_SYNC_DONE, false)
}

override fun setInitialHistoricalSyncCompleted(completed: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_INITIAL_HISTORICAL_SYNC_DONE, completed).apply()
}

override fun getLastFullSyncTime(): Long {
    return sharedPreferences.getLong(KEY_LAST_FULL_SYNC_TIME, 0L)
}

override fun setLastFullSyncTime(time: Long) {
    sharedPreferences.edit().putLong(KEY_LAST_FULL_SYNC_TIME, time).apply()
}
```

#### Step 1.2: Update Mode Resolution Logic

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

**Current Code (Lines 343-348):**

```kotlin
private fun resolveDefaultDailyHistoryMode(): Int? {
    return if (colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        DAILY_HISTORY_MODE_TODAY
    } else {
        null
    }
}
```

**Updated Code:**

```kotlin
private fun resolveDefaultDailyHistoryMode(): Int? {
    return if (colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        DAILY_HISTORY_MODE_TODAY  // Default for regular syncs
    } else {
        null
    }
}

/**
 * Determines the appropriate sync mode based on context.
 * 
 * @param isInitialSync True if this is the first sync after binding/login
 * @param forceFullSync True if caller explicitly wants all data
 * @return Mode 1 (today), 2 (history), or 3 (all)
 */
fun resolveDailyHistoryModeForContext(
    isInitialSync: Boolean = false,
    forceFullSync: Boolean = false
): Int? {
    if (!colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        return null  // Legacy behavior for non-band devices
    }
    
    return when {
        forceFullSync -> DAILY_HISTORY_MODE_ALL
        isInitialSync -> DAILY_HISTORY_MODE_ALL
        else -> DAILY_HISTORY_MODE_TODAY
    }
}
```

#### Step 1.3: Add Interface Method for Contextual Sync

**File:** `commons/src/main/java/com/noisefit_commans/interfaces/data/UserActivityDataActions.kt`

**Add method:**

```kotlin
/**
 * Sync user activity data with explicit mode control.
 * 
 * @param date The date to sync
 * @param isInitialSync True for first sync after binding/login (uses mode 3)
 * @param forceFullSync True to force sync all data (uses mode 3)
 */
open fun syncUserActivityWithContext(
    date: String, 
    isInitialSync: Boolean = false,
    forceFullSync: Boolean = false
) {
    // Default implementation falls back to regular sync
    syncUserActivity(date, true)
}
```

#### Step 1.4: Implement Contextual Sync in Handler

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

**Add implementation:**

```kotlin
override fun syncUserActivityWithContext(
    date: String,
    isInitialSync: Boolean,
    forceFullSync: Boolean
) {
    val resolvedMode = resolveDailyHistoryModeForContext(
        isInitialSync = isInitialSync,
        forceFullSync = forceFullSync
    )
    val deviceType = colorFitDevice?.deviceType ?: "unknown"
    val modeLabel = dailyHistoryModeLabel(resolvedMode)
    LOGS.d(
        TAG,
        "syncUserActivityWithContext date=$date isInitialSync=$isInitialSync " +
        "forceFullSync=$forceFullSync deviceType=$deviceType resolvedMode=$modeLabel"
    )
    AppLogs.sendAppLogs(
        "$TRACK_TAG syncUserActivityWithContext date=$date isInitialSync=$isInitialSync " +
        "forceFullSync=$forceFullSync deviceType=$deviceType resolvedMode=$modeLabel"
    )
    syncUserActivityByMode(date, resolvedMode)
}
```

---

### Phase 2: Trigger Initial Sync on First Connection

#### Step 2.1: Detect First Connection After Bind

**File:** `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`

**Find the connection success handler and add initial sync logic:**

**Location:** After device is connected and ready (in `checkValidateConnection()` or similar)

**Add logic:**

```kotlin
private fun checkAndTriggerInitialHistoricalSync() {
    val hasCompletedInitialSync = ringDataStore.hasCompletedInitialHistoricalSync()
    val deviceType = ringDataStore.getRingDevice()?.deviceType
    
    LOGS.d(TAG, "checkAndTriggerInitialHistoricalSync: " +
        "hasCompletedInitialSync=$hasCompletedInitialSync deviceType=$deviceType")
    
    if (!hasCompletedInitialSync && 
        deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        
        LOGS.d(TAG, "Triggering initial historical sync for Luna Band")
        AppLogs.sendAppLogs("$TAG Triggering initial historical sync")
        
        // Trigger sync with mode 3 (all data)
        CommonGlobals.userActivityDataActions?.syncUserActivityWithContext(
            date = "",
            isInitialSync = true,
            forceFullSync = false
        )
    }
}
```

#### Step 2.2: Mark Initial Sync Complete

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

**In the sync progress callback, detect when initial sync completes:**

**Location:** In `fitnessDataCallBack.onProgress()` around line 385

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
            
            // Mark initial historical sync as complete if this was a full sync
            if (lastSyncWasInitialOrFull) {
                // Notify via callback that initial sync completed
                userActivityDataCallbacks?.onUserActivityDataReceived(
                    UserActivityCallback.InitialHistoricalSyncCompleted
                )
            }

            // Existing code...
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

#### Step 2.3: Add Callback Type

**File:** `commons/src/main/java/com/noisefit_commans/interfaces/callback/UserActivityCallback.kt`

**Add new callback type:**

```kotlin
sealed class UserActivityCallback {
    // Existing callbacks...
    
    /**
     * Indicates that the initial historical sync has completed.
     * The app should mark this in preferences to avoid re-triggering.
     */
    object InitialHistoricalSyncCompleted : UserActivityCallback()
}
```

#### Step 2.4: Handle Callback in Service

**File:** `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`

**In the callback handler:**

```kotlin
is UserActivityCallback.InitialHistoricalSyncCompleted -> {
    LOGS.d(TAG, "Initial historical sync completed, marking in preferences")
    ringDataStore.setInitialHistoricalSyncCompleted(true)
    ringDataStore.setLastFullSyncTime(System.currentTimeMillis())
    AppLogs.sendAppLogs("$TAG Initial historical sync marked complete")
}
```

---

### Phase 3: Clear Initial Sync Flag on Reset/Logout

#### Step 3.1: Reset on Device Unbind

**File:** `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`

**In `onDisconnectSuccess()` or device unbind handling:**

```kotlin
fun onDisconnectSuccess() {
    // Existing code...
    
    // Reset initial sync flag so next device gets full sync
    ringDataStore.setInitialHistoricalSyncCompleted(false)
}
```

#### Step 3.2: Reset on Logout

**Wherever logout clears device data, also clear:**

```kotlin
ringDataStore.setInitialHistoricalSyncCompleted(false)
```

---

### Phase 4: Add Smart Reconnection Sync (Optional Enhancement)

#### Step 4.1: Check Time Since Last Full Sync

**File:** `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`

```kotlin
private fun shouldTriggerFullSyncOnReconnect(): Boolean {
    val lastFullSyncTime = ringDataStore.getLastFullSyncTime()
    val hoursSinceLastFullSync = (System.currentTimeMillis() - lastFullSyncTime) / (1000 * 60 * 60)
    
    // If more than 24 hours since last full sync, do a full sync
    return hoursSinceLastFullSync > 24
}

private fun checkAndTriggerReconnectSync() {
    if (shouldTriggerFullSyncOnReconnect()) {
        LOGS.d(TAG, "Triggering full sync on reconnect (>24h since last full sync)")
        CommonGlobals.userActivityDataActions?.syncUserActivityWithContext(
            date = "",
            isInitialSync = false,
            forceFullSync = true
        )
    } else {
        // Regular today-only sync
        CommonGlobals.userActivityDataActions?.syncUserActivity("", true)
    }
}
```

---

## Summary of Changes

### Files to Modify

| File | Changes |
|------|---------|
| `RingDataStore.kt` | Add initial sync tracking methods |
| `UserActivityDataActions.kt` | Add `syncUserActivityWithContext()` interface |
| `ZhUserActivityHandler.kt` | Add contextual mode resolution and sync method |
| `UserActivityCallback.kt` | Add `InitialHistoricalSyncCompleted` callback |
| `RingConnectionService.kt` | Add initial sync trigger logic |

### New Methods

| Method | Purpose |
|--------|---------|
| `hasCompletedInitialHistoricalSync()` | Check if initial full sync done |
| `setInitialHistoricalSyncCompleted()` | Mark initial full sync done |
| `getLastFullSyncTime()` | Track last mode 3 sync time |
| `syncUserActivityWithContext()` | Sync with explicit context/mode |
| `resolveDailyHistoryModeForContext()` | Smart mode selection |

### Mode Usage After Fix

| Scenario | Mode | When |
|----------|------|------|
| First binding | 3 (All) | Once per device |
| Regular sync | 1 (Today) | Every periodic sync |
| >24h reconnect | 3 (All) | After long disconnect |
| Manual refresh | 1 (Today) | UI pull-to-refresh |
| Test buttons | 1/2/3 | BlankTestFragment |

---

## Testing Plan

### Test Case 1: First Device Binding

1. Factory reset device with accumulated data (multiple days)
2. Pair device in app
3. **Expected:** Mode 3 sync triggered, all historical data received
4. **Verify:** Historical data appears in app graphs

### Test Case 2: Regular Daily Sync

1. With device already paired and initial sync done
2. Trigger periodic sync or manual refresh
3. **Expected:** Mode 1 sync triggered (today only)
4. **Verify:** SDK logs show `getDailyHistoryData() : 1`

### Test Case 3: Reconnect After Long Disconnect

1. Disconnect device for >24 hours
2. Reconnect device
3. **Expected:** Mode 3 sync triggered
4. **Verify:** Data from disconnected days appears

### Test Case 4: App Reinstall

1. Uninstall and reinstall app
2. Login and reconnect device
3. **Expected:** Mode 3 sync triggered (initial sync flag cleared)
4. **Verify:** Historical data synced

### Test Case 5: Device Unbind/Rebind

1. Unbind device from app
2. Rebind same or different device
3. **Expected:** Mode 3 sync triggered for new binding
4. **Verify:** Fresh initial sync happens

---

## SDK Log Verification

### Before Fix

```
getDailyHistoryData() : 1
getDailyHistoryData() : 1
getDailyHistoryData() : 1
```

### After Fix (Initial Sync)

```
getDailyHistoryData() : 3    ← Initial full sync
getDailyHistoryData() : 1    ← Subsequent daily sync
getDailyHistoryData() : 1    ← Subsequent daily sync
```

### After Fix (Reconnect >24h)

```
getDailyHistoryData() : 3    ← Reconnect full sync
getDailyHistoryData() : 1    ← Subsequent daily sync
```

---

## Risk Assessment

| Risk | Mitigation |
|------|------------|
| Mode 3 sync takes longer | Only trigger on first bind or >24h reconnect |
| Battery drain | Limit mode 3 frequency with time checks |
| Duplicate data | SDK handles, app should also check timestamps |
| Sync fails midway | Keep initial sync flag false until confirmed complete |

---

## Alternative Approaches

### Alternative A: Always Use Mode 3

**Pros:** Simplest implementation
**Cons:** Slower syncs, more battery usage, unnecessary for daily use

**Decision:** NOT RECOMMENDED

### Alternative B: Use Mode 2 (History) Separately

**Approach:** Call mode 1 (today) first, then mode 2 (history) if needed
**Pros:** More granular control
**Cons:** Two API calls, more complex logic

**Decision:** OPTIONAL - Could be added later for specific use cases

### Alternative C: Smart Mode Selection (RECOMMENDED)

**Approach:** Use mode 3 for initial/recovery, mode 1 for regular
**Pros:** Efficient regular syncs, complete data on bind/recovery
**Cons:** Slightly more complex logic

**Decision:** RECOMMENDED - Best balance of completeness and efficiency

---

## Implementation Priority

1. **Critical (Phase 1-2):** Initial sync on first binding with mode 3
2. **High (Phase 3):** Reset flag on unbind/logout
3. **Medium (Phase 4):** Smart reconnect sync after >24h

---

## Estimated Effort

| Phase | Effort |
|-------|--------|
| Phase 1: Add methods | 2 hours |
| Phase 2: Initial sync trigger | 3 hours |
| Phase 3: Reset handling | 1 hour |
| Phase 4: Smart reconnect | 2 hours |
| Testing | 4 hours |
| **Total** | **~12 hours** |
