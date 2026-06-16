# Issue 5: Historical Data Not Synchronizing - Diagnosis Report

**Date:** April 20, 2026  
**Status:** ROOT CAUSE IDENTIFIED  
**Severity:** Critical  
**Reported By:** Firmware Team (Weekend Testing)

---

## Problem Statement

The bracelet does not synchronize historical data. After checking the SDK logs, it was determined that the APP did not issue the instruction to synchronize historical data.

### Evidence from SDK Logs

```
行 972: 2026-04-20 08:10:31:885 ----> controlbletools ---------> getDailyHistoryData() : 1
行 3318: 2026-04-20 08:14:52:502 ----> controlbletools ---------> getDailyHistoryData() : 1
行 5764: 2026-04-20 08:41:01:368 ----> controlbletools ---------> getDailyHistoryData() : 1
行 7102: 2026-04-20 08:41:17:727 ----> controlbletools ---------> getDailyHistoryData() : 1
行 8526: 2026-04-20 08:52:33:240 ----> controlbletools ---------> getDailyHistoryData() : 1
行 9986: 2026-04-20 08:57:02:984 ----> controlbletools ---------> getDailyHistoryData() : 1
```

**Key Observation:** Every call shows `getDailyHistoryData() : 1` - the app ONLY calls mode 1 (today's data).

---

## SDK API Reference

### Section 2.3.10: 无屏手环传输模式获取日常数据 (Screenless Bracelet Transmission Mode to Obtain Daily Data)

From `ZH Android SDK.txt`:

```java
/**
 * 根据传输模式获取日常数据 Obtain daily data based on transmission mode
 * @param mode 模式 1：传输当天  2:传输历史  3:传输所有 
 *             Mode 1: Transmit on the same day
 *             Mode 2: Transmit history  
 *             Mode 3: Transmit all
 * @param listener
 */
public void getDailyHistoryData(int mode, ParsingStateManager.SendCmdStateListener listener)
```

### Mode Definitions

| Mode | Chinese | English | Description |
|------|---------|---------|-------------|
| **1** | 传输当天 | Transmit on the same day | Only syncs today's data |
| **2** | 传输历史 | Transmit history | Syncs historical data (past days) |
| **3** | 传输所有 | Transmit all | Syncs all data (today + history) |

---

## Root Cause Analysis

### 1. Mode Resolution Logic

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

**Lines 343-348:**

```kotlin
private fun resolveDefaultDailyHistoryMode(): Int? {
    return if (colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        DAILY_HISTORY_MODE_TODAY  // Returns mode 1 for Luna Band
    } else {
        null
    }
}
```

**Issue:** For Luna Band devices, the default mode is **hardcoded to `DAILY_HISTORY_MODE_TODAY` (mode 1)**, which ONLY syncs today's data.

### 2. Constants Definition

**Lines 101-103:**

```kotlin
companion object {
    private const val DAILY_HISTORY_MODE_TODAY = 1
    private const val DAILY_HISTORY_MODE_HISTORY = 2
    private const val DAILY_HISTORY_MODE_ALL = 3
}
```

### 3. Sync Entry Point

**Lines 809-820:**

```kotlin
override fun syncUserActivity(date: String, isRefresh: Boolean) {
    val resolvedMode = resolveDefaultDailyHistoryMode()  // Returns 1 for Luna Band
    val deviceType = colorFitDevice?.deviceType ?: "unknown"
    val modeLabel = dailyHistoryModeLabel(resolvedMode)
    LOGS.d(
        TAG,
        "syncUserActivity date=$date isRefresh=$isRefresh deviceType=$deviceType resolvedMode=$modeLabel"
    )
    AppLogs.sendAppLogs(
        "$TRACK_TAG syncUserActivity date=$date isRefresh=$isRefresh deviceType=$deviceType resolvedMode=$modeLabel"
    )
    syncUserActivityByMode(date, resolvedMode)  // Always passes mode 1
}
```

### 4. SDK Call

**Lines 823-831:**

```kotlin
override fun syncUserActivityByMode(date: String, mode: Int?) {
    try {
        val deviceType = colorFitDevice?.deviceType ?: "unknown"
        val modeLabel = dailyHistoryModeLabel(mode)
        LOGS.d(TAG, "syncUserActivityByMode date=$date deviceType=$deviceType mode=$modeLabel")
        AppLogs.sendAppLogs("$TRACK_TAG syncUserActivityByMode date=$date deviceType=$deviceType mode=$modeLabel")
        requestDailyHistoryData(mode)  // Calls SDK with mode 1
        ControlBleTools.getInstance().getAutoSportData(null)
        ControlBleTools.getInstance().getFitnessSportIdsData(null)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
```

### 5. Final SDK Call

**Lines 361-370:**

```kotlin
private fun requestDailyHistoryData(mode: Int?) {
    val deviceType = colorFitDevice?.deviceType ?: "unknown"
    val modeLabel = dailyHistoryModeLabel(mode)
    LOGS.d(TAG, "requestDailyHistoryData deviceType=$deviceType mode=$modeLabel")
    AppLogs.sendAppLogs("$TRACK_TAG requestDailyHistoryData deviceType=$deviceType mode=$modeLabel")
    if (mode == null) {
        ControlBleTools.getInstance().getDailyHistoryData(null)
    } else {
        ControlBleTools.getInstance().getDailyHistoryData(mode, null)  // Called with mode=1
    }
}
```

---

## Call Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        HISTORICAL DATA SYNC FLOW                            │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────┐
│   OreoSyncDataWork   │
│ (WorkManager Timer)  │
│         or           │
│ SummaryDataFragment  │
│    (UI Refresh)      │
└──────────┬───────────┘
           │
           │ UserActivityAction.SyncUserActivity("", false)
           ▼
┌──────────────────────┐
│    ServiceUtil.kt    │
│    handleAction()    │
└──────────┬───────────┘
           │
           │ activityAction.syncUserActivity(date, isRefresh)
           ▼
┌──────────────────────────────────────┐
│     ZhUserActivityHandler.kt         │
│       syncUserActivity()             │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ resolveDefaultDailyHistoryMode()│  │
│  │   ↓                             │  │
│  │ deviceType == LUNA_BAND?        │  │
│  │   YES → return 1 (TODAY)        │◄─┼── PROBLEM: Always returns 1
│  │   NO  → return null             │  │
│  └────────────────────────────────┘  │
│                                      │
│  syncUserActivityByMode(date, 1)     │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│  syncUserActivityByMode(date, mode)  │
│                                      │
│  requestDailyHistoryData(1)          │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│    requestDailyHistoryData(mode)     │
│                                      │
│  if (mode == null)                   │
│    SDK.getDailyHistoryData(null)     │
│  else                                │
│    SDK.getDailyHistoryData(1, null)  │◄── RESULT: Only mode 1 called
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│          ZH SDK (AAR)                │
│                                      │
│  getDailyHistoryData(1)              │
│                                      │
│  ┌────────────────────────────────┐  │
│  │  Mode 1: Transmit TODAY only   │  │
│  │  Mode 2: Transmit HISTORY      │◄─┼── NEVER CALLED
│  │  Mode 3: Transmit ALL          │◄─┼── NEVER CALLED
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
```

---

## Where Sync is Triggered

### 1. Periodic WorkManager Sync

**File:** `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt`

**Lines 793-798:**

```kotlin
sessionManager.sendUserActivityAction(
    UserActivityAction.SyncUserActivity(
        "",
        false
    )
)
```

This triggers `syncUserActivity()` which uses mode 1.

### 2. UI Manual Refresh

**File:** `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataFragmentToday.kt`

Multiple places call sync with the same pattern, all resulting in mode 1.

### 3. BlankTest Manual Override (TEST ONLY)

**File:** `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt`

**Lines 720-743:**

```kotlin
root.findViewById<TextView>(R.id.tvSdkSyncToday).setOnClickListener {
    requestDailySync(root, DAILY_SYNC_MODE_TODAY, "...")  // Mode 1
}
root.findViewById<TextView>(R.id.tvSdkSyncHistory).setOnClickListener {
    requestDailySync(root, DAILY_SYNC_MODE_HISTORY, "...")  // Mode 2
}
root.findViewById<TextView>(R.id.tvSdkSyncAll).setOnClickListener {
    requestDailySync(root, DAILY_SYNC_MODE_ALL, "...")  // Mode 3
}
```

**Note:** These are manual TEST buttons only - not part of normal app flow.

---

## Impact Analysis

### What Works

| Data Type | Today's Data | Historical Data |
|-----------|--------------|-----------------|
| Heart Rate | ✅ Syncs | ❌ Never syncs |
| Steps | ✅ Syncs | ❌ Never syncs |
| Sleep | ✅ Syncs | ❌ Never syncs |
| Stress | ✅ Syncs | ❌ Never syncs |
| SpO2 | ✅ Syncs | ❌ Never syncs |
| Sports | ✅ Syncs | ❌ Never syncs |

### User Scenarios Affected

1. **New Device Pairing:**
   - User pairs a bracelet that has days/weeks of accumulated data
   - Only today's data is synced
   - Historical data on device is never retrieved

2. **App Reinstall:**
   - User reinstalls app and reconnects device
   - Historical data on device is never retrieved

3. **Long Disconnection:**
   - Device disconnected for several days
   - When reconnected, only today's data syncs
   - Data from disconnected days is lost

4. **Week/Month Views:**
   - Historical graphs show gaps or missing data
   - Trends and averages are inaccurate

---

## Why This Wasn't Detected Earlier

1. **Ring vs Band Difference:**
   - `resolveDefaultDailyHistoryMode()` returns `null` for Ring devices (legacy behavior)
   - Returns mode 1 specifically for LUNA_BAND
   - This logic was added to differentiate band sync behavior

2. **Daily Sync Appears Working:**
   - Today's data syncs correctly
   - Users see current day data
   - Issue only visible when checking historical data

3. **Test UI Hidden:**
   - BlankTestFragment has mode 2/3 buttons for testing
   - These are developer test features, not user-accessible
   - Normal production flow never uses these

---

## Evidence Summary

| Evidence | Finding |
|----------|---------|
| SDK Logs | All calls show `getDailyHistoryData() : 1` |
| Code Review | `resolveDefaultDailyHistoryMode()` hardcoded to mode 1 for Luna Band |
| Flow Analysis | No code path calls mode 2 (history) or mode 3 (all) in production |
| Impact | Historical data from device is never synchronized |

---

## Conclusion

**ROOT CAUSE CONFIRMED:**

The `ZhUserActivityHandler.resolveDefaultDailyHistoryMode()` method hardcodes the sync mode to `DAILY_HISTORY_MODE_TODAY` (mode 1) for Luna Band devices. This means:

1. **Mode 1 (Today)** - Called on every sync → Works
2. **Mode 2 (History)** - NEVER called → Historical data lost
3. **Mode 3 (All)** - NEVER called → Full sync never happens

The SDK supports historical sync, but the app never requests it.

---

## Files Involved

| File | Role |
|------|------|
| `ZhUserActivityHandler.kt` | Mode resolution and SDK call |
| `OreoSyncDataWork.kt` | Periodic sync trigger |
| `SummaryDataFragmentToday.kt` | UI sync trigger |
| `BlankTestFragment.kt` | Test buttons (mode 2/3 available) |
| `UserActivityDataActions.kt` | Interface definitions |

---

## Next Steps

See: [issue-5-historical-data-sync-fix-plan-2026-04-20.md](issue-5-historical-data-sync-fix-plan-2026-04-20.md)
