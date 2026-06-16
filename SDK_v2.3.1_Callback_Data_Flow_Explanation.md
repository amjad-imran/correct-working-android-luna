# SDK v2.3.1 Callback & Data Flow - Complete Explanation

**Created:** April 10, 2026  
**Purpose:** Clarify which callbacks are used, why raw data shows zeros, and how data reaches graphs

---

## Quick Answer to Your Confusion

### ✅ YES - Using NEW v2.3.1 Callbacks with Frequency Support

The app **IS using the new v2.3.1 callbacks** with `frequencyVersion` field for both heart rate and stress graphs:

| Data Type | Callback Used | Bean Class | Frequency Field |
|-----------|---------------|------------|-----------------|
| **Heart Rate** | `onContinuousHeartRateData()` | `ContinuousHeartRateBean` | `continuousHeartRateFrequency` + `frequencyVersion` |
| **Stress** | `onContinuousPressureData()` | `ContinuousPressureBean` | `pressureFrequency` + `frequencyVersion` |
| **Sleep** | `onSleepData()` | `SleepBean` (legacy but re-routed) | N/A - minute-based durations |

### ❌ NOT Using Legacy Offline Callbacks

The legacy `onOfflineHeartRateData()` and `onOfflinePressureData()` callbacks are **NOT** used for Luna/Luna Band graphs.

---

## The Two Parallel Data Paths (Why Raw Dump Shows Zeros)

Your confusion comes from seeing **TWO DIFFERENT DATA PATHS** that both happen simultaneously:

```
┌─────────────────────────────────────────────────────────────────┐
│  SDK CALLBACK: onContinuousHeartRateData(bean)                  │
└────────────┬────────────────────────────────────────────────────┘
             │
             ├──────────────────────────────────────────────────────┐
             │                                                      │
      ┌──────▼──────┐                                    ┌─────────▼─────────┐
      │ PATH 1:     │                                    │ PATH 2:           │
      │ RAW DUMP    │                                    │ PRODUCTION GRAPHS │
      └──────┬──────┘                                    └─────────┬─────────┘
             │                                                      │
             ▼                                                      ▼
    Save to SharedPrefs                              Parse → Normalize → Database
    as raw JSON                                      → Accumulate over syncs
             │                                                      │
             ▼                                                      ▼
    testGetRawContinuousHeartRateJson()             Read from DB for graphs
    returns LATEST callback JSON                     Shows accumulated valid data
             │                                                      │
             ▼                                                      ▼
    May contain zeros if                             Retains valid historical
    latest SDK callback was empty                    data from earlier syncs
```

### Path 1: Raw Data Dump (What You See in BlankTestFragment)

**File:** `ZhUserActivityHandler.kt` line 472-476

```kotlin
override fun onContinuousHeartRateData(data: ContinuousHeartRateBean) {
    // PATH 1: Save raw JSON immediately to SharedPreferences
    saveRawSdkPayload(
        label = "onContinuousHeartRateData",
        raw = Gson().toJson(data),
        saver = watchDataStore::testSaveRawContinuousHeartRateJson  // ← Saves to SharedPrefs
    )
    // ... PATH 2 happens below ...
}
```

**What happens:**
1. SDK sends callback with data
2. Raw JSON saved to `SharedPreferences` immediately
3. Each manual sync button press **clears previous session** via `testStartSdkRawCaptureSession()`
4. BlankTestFragment polls `testGetRawContinuousHeartRateJson()` every 2 seconds
5. Shows **latest captured callback** which may have zeros

**Why you see zeros:**
- ✗ Session resets on each manual sync button press
- ✗ Latest callback in current session may be empty
- ✗ SDK may send empty callback if no new data since last sync
- ✗ No accumulation - shows single callback snapshot

### Path 2: Production Graphs (What Works Correctly)

**File:** `ZhUserActivityHandler.kt` line 478-486

```kotlin
override fun onContinuousHeartRateData(data: ContinuousHeartRateBean) {
    // ... PATH 1 above ...
    
    // PATH 2: Parse, normalize, and save to database for production
    if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)
        || colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.HeartHistoryObtainedOreo(
                oreoDataConverter.parseHeartRateData(data)  // ← Normalizes frequency data
            )
        )
    }
}
```

**What happens:**
1. Same SDK callback triggers this path
2. Data goes through `oreoDataConverter.parseHeartRateData(data)`
3. **Normalization happens** via `LunaSdk231DataNormalizer.normalizeLegacyMetricBreakup()`
4. Saves to database via `UserActivityCallback.HeartHistoryObtainedOreo`
5. Database **accumulates** data from multiple syncs
6. Graphs read from database showing **all valid accumulated data**

**Why graphs work:**
- ✓ Data normalized from seconds/minutes to 5-minute intervals
- ✓ Database retains valid data from all syncs
- ✓ Filters out zeros and invalid values (255)
- ✓ Accumulates over time

---

## Frequency Handling - How v2.3.1 Works

### The New Frequency Fields

**File:** SDK beans now include these fields:

```kotlin
// ContinuousHeartRateBean (v2.3.1)
val continuousHeartRateFrequency: Int     // e.g., 5 or 30
val frequencyVersion: Int                 // 0 = minutes, 1 = seconds
val heartRateData: List<Int>

// ContinuousPressureBean (v2.3.1)
val pressureFrequency: Int                // e.g., 5 or 30
val frequencyVersion: Int                 // 0 = minutes, 1 = seconds
val pressureData: List<Int>
```

### How Frequency is Interpreted

| frequencyVersion | frequency value | Meaning | Example |
|------------------|----------------|---------|---------|
| `0` (minutes) | `5` | Data every 5 **minutes** | Legacy Luna Ring behavior |
| `1` (seconds) | `30` | Data every 30 **seconds** | New Luna Band behavior |

### Normalization to 5-Minute Intervals

**File:** `OreoDataConverter.kt` line 649-662

```kotlin
fun parseHeartRateData(bean: ContinuousHeartRateBean): OreoHeartRate {
    val heartRate = OreoHeartRate()
    heartRate.date = DateFormats.dateFormat3().format(startDayTimeStamp)
    
    // ✓ NORMALIZATION HAPPENS HERE - handles both frequency versions
    val normalizedHeartRateData = LunaSdk231DataNormalizer.normalizeLegacyMetricBreakup(
        values = bean.heartRateData,
        frequency = bean.continuousHeartRateFrequency,
        frequencyVersion = bean.frequencyVersion,  // ← Distinguishes seconds vs minutes
        targetFrequencyMinutes = 5                 // ← Always normalize to 5-min buckets
    )
    
    heartRate.breakUp = gson.toJson(normalizedHeartRateData)
    return heartRate
}
```

**File:** `OreoDataConverter.kt` line 498-514 (same for stress)

```kotlin
fun parseStressData(bean: ContinuousPressureBean): OreoStressDataBreakup {
    val stressData = OreoStressDataBreakup()
    
    // ✓ NORMALIZATION HAPPENS HERE - handles both frequency versions
    val normalizedPressureData = LunaSdk231DataNormalizer.normalizeLegacyMetricBreakup(
        values = bean.pressureData,
        frequency = bean.pressureFrequency,
        frequencyVersion = bean.frequencyVersion,  // ← Distinguishes seconds vs minutes
        targetFrequencyMinutes = 5                 // ← Always normalize to 5-min buckets
    )
    
    stressData.breakUp = gson.toJson(normalizedPressureData)
    return stressData
}
```

### What Normalization Does

**File:** `LunaSdk231DataNormalizer.kt`

Example: If Luna Band sends 30-second data (`frequencyVersion=1, frequency=30`):

```
INPUT (30-second intervals):
[75, 78, 80, 82, 0, 85, 72, 74, 78, 80, ...]
 └────────────┘ └────────────┘
  Bucket 1       Bucket 2
  (0-5 min)      (5-10 min)

PROCESSING:
- Bucket size = 300 sec / 30 sec = 10 readings per bucket
- Filter out zeros and 255 values
- Average valid values in each bucket

OUTPUT (5-minute intervals):
[80, 76, ...]  ← Averaged values ready for graphs
```

---

## Sleep Data Flow

### Sleep Uses Legacy Bean but New Routing

**File:** `ZhUserActivityHandler.kt` line 445-470

```kotlin
override fun onSleepData(data: SleepBean) {  // ← LEGACY bean name
    if (colorFitDevice?.deviceType.equals(DeviceType.NOISEFIT_LUNA.deviceType, true)
        || colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        
        // ✓ ROUTED THROUGH NEW OREO PATH (fixed in April 10, 2026)
        val sleepDataParsed = oreoDataConverter.parseSleepData(data)
        sleepDataParsed?.let {
            userActivityDataCallbacks?.onUserActivityDataReceived(
                UserActivityCallback.SleepDataObtainedOreo(sleepDataParsed)  // ← Oreo path
            )
        }
    } else {
        // Legacy devices use old path
        val sleepDataParsed = dataConverter.parseSleepData(data)
        userActivityDataCallbacks?.onUserActivityDataReceived(
            UserActivityCallback.SleepDataObtained(sleepDataParsed)
        )
    }
}
```

**Why this matters:**
- Before fix: Luna Band sleep went to legacy path → silently dropped
- After fix: Luna Band sleep goes to Oreo path → saved correctly
- Sleep RRI/HRV data: Captured separately via new callbacks (raw dump only, not production yet)

---

## Sync Modes - How Luna Band Uses "ALL" Mode

### Production Sync Uses Mode 3 (ALL) for Luna Band

**File:** `ZhUserActivityHandler.kt` line 806-817

```kotlin
override fun syncUserActivity(date: String, isRefresh: Boolean) {
    // ✓ Automatically resolves to mode 3 (ALL) for Luna Band
    val resolvedMode = resolveDefaultDailyHistoryMode()
    
    syncUserActivityByMode(date, resolvedMode)
}

private fun resolveDefaultDailyHistoryMode(): Int? {
    return if (colorFitDevice?.deviceType.equals(DeviceType.LUNA_BAND.deviceType, true)) {
        DAILY_HISTORY_MODE_ALL  // ← Mode 3 for Luna Band
    } else {
        null  // ← Legacy no-mode for other devices
    }
}
```

### Sync Mode Mapping

| Mode | SDK Constant | Label | Used For |
|------|-------------|--------|----------|
| `null` | No mode | `legacy-default` | Luna Ring, other devices |
| `1` | `DAILY_HISTORY_MODE_TODAY` | `today` | Manual test: today's data only |
| `2` | `DAILY_HISTORY_MODE_HISTORY` | `history` | Manual test: historical data |
| `3` | `DAILY_HISTORY_MODE_ALL` | `all` | **Luna Band production** + manual test |

**File:** `ZhUserActivityHandler.kt` line 820-831

```kotlin
override fun syncUserActivityByMode(date: String, mode: Int?) {
    requestDailyHistoryData(mode)  // ← Passes mode to SDK
    ControlBleTools.getInstance().getAutoSportData(null)
    ControlBleTools.getInstance().getFitnessSportIdsData(null)
}

private fun requestDailyHistoryData(mode: Int?) {
    if (mode == null) {
        ControlBleTools.getInstance().getDailyHistoryData(null)  // ← Legacy API
    } else {
        ControlBleTools.getInstance().getDailyHistoryData(mode, null)  // ← v2.3.1 API
    }
}
```

---

## Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  USER ACTION: App opens / Manual sync button pressed                        │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                    ┌────────────▼──────────────┐
                    │ ZhUserActivityHandler     │
                    │ syncUserActivity()        │
                    └────────────┬──────────────┘
                                 │
                    ┌────────────▼──────────────┐
                    │ Resolve sync mode         │
                    │ Luna Band → mode 3 (ALL)  │
                    │ Others → null (legacy)    │
                    └────────────┬──────────────┘
                                 │
                    ┌────────────▼─────────────────────────────────────┐
                    │ SDK ControlBleTools.getDailyHistoryData(mode)    │
                    │ Triggers BLE data sync from device               │
                    └────────────┬─────────────────────────────────────┘
                                 │
                    ┌────────────▼──────────────┐
                    │ SDK sends callbacks:      │
                    │ - onContinuousHeartRate   │
                    │ - onContinuousPressure    │
                    │ - onSleepData             │
                    │ - onSleepRRI (v2.3.1)     │
                    │ - onSleepHRV (v2.3.1)     │
                    │ - etc.                    │
                    └────────────┬──────────────┘
                                 │
                ┌────────────────┴───────────────────┐
                │                                    │
      ┌─────────▼──────────┐            ┌───────────▼──────────┐
      │ PATH 1: RAW DUMP   │            │ PATH 2: PRODUCTION   │
      └─────────┬──────────┘            └───────────┬──────────┘
                │                                    │
      ┌─────────▼───────────────┐        ┌──────────▼────────────────────┐
      │ Save raw JSON to        │        │ oreoDataConverter.parse*()    │
      │ SharedPreferences       │        │ → normalizeLegacyMetricBreakup│
      └─────────┬───────────────┘        │ → Filter zeros/255            │
                │                        └──────────┬────────────────────┘
      ┌─────────▼───────────────┐                  │
      │ BlankTestFragment       │        ┌─────────▼─────────────────────┐
      │ polls every 2 sec       │        │ UserActivityCallback          │
      │ → Shows latest callback │        │ → Repository → Database       │
      │ → May show zeros        │        └─────────┬─────────────────────┘
      └─────────────────────────┘                  │
                                          ┌────────▼──────────────────────┐
                                          │ Graphs read from database     │
                                          │ → Shows accumulated valid data│
                                          └───────────────────────────────┘
```

---

## Why Raw Data Dump Shows Zeros - Detailed Scenarios

### Scenario 1: Session Reset

```
10:00 AM - Manual sync → Triggers mode 3 (ALL)
          → SDK sends callback with valid HR data [80, 82, 85, ...]
          → PATH 1: Saves to SharedPrefs
          → PATH 2: Saves to database

10:05 AM - Click "TODAY" button in BlankTestFragment
          → testStartSdkRawCaptureSession(mode=1) CLEARS SharedPrefs
          → Triggers new sync with mode 1 (TODAY)
          → SDK may send empty callback if no new today-only data
          → PATH 1: Saves [0, 0, 0, ...] to SharedPrefs ← You see zeros
          → PATH 2: Database still has 10:00 AM data ← Graphs still work
```

### Scenario 2: SDK Sends Empty Callback

```
SDK Behavior:
- If no NEW data since last sync
- SDK may send callback with empty arrays or zeros
- This is expected SDK behavior

App Behavior:
- PATH 1: Saves whatever SDK sent (even if zeros) ← Raw dump shows zeros
- PATH 2: Database keeps previous valid data ← Graphs show last valid data
```

### Scenario 3: Capture Validation

```kotlin
// Raw dump validation logic (RawSdkPayloadBottomSheet.kt)
val isValid = if (validityKeys.isEmpty()) {
    true  // No validation
} else {
    jsonObject?.hasMeaningfulPayloadData() == true  // Checks for non-zero values
}

// If latest callback has zeros but earlier callback had valid data:
selectedCapture = resolvedCaptures.lastOrNull { it.isValid } ?: latestCapture

// ✓ Shows "latest valid payload" if found
// ✗ Shows "latest captured payload" even if zeros if no valid found
```

---

## New v2.3.1 Callbacks - Current Usage Status

| Callback | Status | Current Usage |
|----------|--------|---------------|
| `onContinuousHeartRateData` | ✅ **PRODUCTION** | Graphs + raw dump |
| `onContinuousPressureData` | ✅ **PRODUCTION** | Graphs + raw dump |
| `onSleepData` | ✅ **PRODUCTION** | Sleep screens (Oreo path) |
| `onSleepRRIData` | ⚠️ **RAW DUMP ONLY** | Not in production yet |
| `onSleepHRVData` | ⚠️ **RAW DUMP ONLY** | Not in production yet |
| `onContinuousRRIData` | ⚠️ **RAW DUMP ONLY** | Not in production yet |
| `onContinuousHeartRateSportFiveMinAfter` | ⚠️ **RAW DUMP ONLY** | Not in production yet, but has chart in BlankTestFragment |

---

## Summary - Your Questions Answered

### Q1: Which callbacks for HR/stress graphs?

**A:** NEW v2.3.1 callbacks:
- Heart Rate: `onContinuousHeartRateData(ContinuousHeartRateBean)` with `frequencyVersion`
- Stress: `onContinuousPressureData(ContinuousPressureBean)` with `frequencyVersion`

### Q2: Why raw dump shows zeros but graphs work?

**A:** Two parallel data paths:
- **Raw dump:** Latest callback snapshot from SharedPreferences (may be empty)
- **Graphs:** Accumulated normalized data from database (retains valid data)

### Q3: How is sleep handled?

**A:** 
- Uses legacy `onSleepData(SleepBean)` callback
- Routed through NEW Oreo converter path (not legacy path)
- Sleep RRI/HRV callbacks captured but not in production yet

### Q4: Are new sync modes used?

**A:** YES:
- Luna Band: Automatically uses mode 3 (ALL)
- Luna Ring/others: Uses legacy no-mode
- Manual test buttons: Can trigger mode 1 (TODAY), 2 (HISTORY), 3 (ALL)

### Q5: Where is frequency (0=minutes, 1=seconds) handled?

**A:** In `LunaSdk231DataNormalizer.normalizeLegacyMetricBreakup()`:
- Receives `frequencyVersion` parameter
- Converts seconds to minutes if needed
- Normalizes to 5-minute target buckets
- Filters invalid values (0, 255)
- Averages valid values

---

## Debugging Tips

### To See Non-Zero Raw Data

1. **Ensure device has recent data:**
   - Wear device for at least 30 minutes
   - Wait for device to collect HR/stress readings

2. **Trigger fresh sync:**
   - Open BlankTestFragment
   - Click "ALL" button (mode 3)
   - Wait 5-10 seconds for SDK callbacks

3. **Check raw dump metadata:**
   - Look for "captures X/Y valid"
   - If Y > X, some callbacks had zeros
   - Click raw dump to see all captured sessions

4. **Verify database has data:**
   - Check if graphs show data
   - If graphs work but raw dump shows zeros → this is normal behavior

### Log Pattern to Watch

```
LUNA-> onContinuousHeartRateData : ContinuousHeartRateBean(
    date=2026-04-10 14:30:00,
    heartRateData=[75, 78, 80, ...],
    continuousHeartRateFrequency=30,
    frequencyVersion=1  ← 1 means seconds
)
```

---

**Last Updated:** April 10, 2026  
**Related Files:**
- `ZhUserActivityHandler.kt`
- `OreoDataConverter.kt`
- `LunaSdk231DataNormalizer.kt`
- `BlankTestFragment.kt`
- `RawSdkPayloadBottomSheet.kt`
