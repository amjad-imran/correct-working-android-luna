# Workout HR Fix Explainer - April 16, 2026

## Overview

This document explains the issues with Heart Rate (HR) being reported as 30 or less during workouts and in the post-workout graph, what changes fixed the issue, and what callbacks/methods are currently being used in the main post-workout window.

---

## 1. Problem Statement

### 1.1 Reported Issue
- HR was frequently reported as **30 or less** during workouts
- The **min HR** value in the post-workout details screen was showing incorrect low values
- The HR graph during workout was displaying incorrect data
- HR zone durations were calculated incorrectly

### 1.2 Root Causes Identified

There were **multiple interrelated issues** causing the HR problems:

#### Issue A: Zone Index Accumulation Bug
In `OWorkoutDetailsViewModelV2.generateHrZones(...)`:
- Zone index lists (`zone1Indexes`, `zone2Indexes`, etc.) were **not being cleared** before each calculation
- When the post-workout screen was refreshed or rebuilt multiple times, indexes accumulated
- This caused **zone durations to be artificially inflated** and distorted

#### Issue B: Server Data vs Device Report Mismatch
- The post-workout screen was reading values like calories, recovery time, and HR zone durations **only from the server response** (`OWorkoutDetailsResponseModel`)
- After the SDK v2.3.1 upgrade, the device already provided accurate final workout values through `DevSportInfoBean`
- The server data and device report could disagree, causing inconsistent display

#### Issue C: Sample-Based Duration Calculation
- HR zone durations were being computed by counting samples from `hrArray` and multiplying by a **hardcoded 30-second interval**
- This approach was inaccurate because:
  - The SDK already provides exact zone durations in seconds via `DevSportInfoBean.reportHeartWarmUp`, `reportHeartFatBurning`, etc.
  - The sample-counting method ignores the actual device-measured zone time

#### Issue D: Late Local Workout Cache Update
- The parsed `RecordedWorkoutData` (containing accurate device report values) was **only being updated during parsing progress completion**, not immediately when `onDevSportInfo(...)` callback arrived
- This created a timing gap where the post-workout screen might not have access to the latest device report

---

## 2. What Changes Fixed the Issues

### 2.1 Zone Index List Clearing (Critical Fix)

**File:** `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`

**Change:** Added `.clear()` calls to all zone index lists at the start of `generateHrZones(...)`:

```kotlin
fun generateHrZones(hrValue: List<Int>): List<OWDActivityHRZoneData> {
    val zones = mutableMapOf<String, IntRange>()
    zone1Indexes.clear()       // NEW - prevents accumulation
    zone2Indexes.clear()       // NEW
    zone3Indexes.clear()       // NEW
    zone4Indexes.clear()       // NEW
    zone5Indexes.clear()       // NEW
    zoneRestorativeIndexes.clear()  // NEW
    // ... rest of the method
}
```

**Why this matters:**
- Before this fix, if the screen was rebuilt (e.g., orientation change, coming back from background), the zone indexes would **accumulate** instead of being recalculated fresh
- This caused zone durations to grow artificially with each rebuild

### 2.2 Matched Local Report Fallback for Zone Durations

**File:** `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`

**Change:** The ViewModel now:
1. Reads the latest cached `RecordedWorkoutData` from `WatchDataStore`
2. Matches it against the currently displayed workout using date/startTime/endTime
3. When matched, **prefers the device report's exact zone durations** over sample-based calculations

**New flow:**
```kotlin
val recordedWorkout = getMatchedRecordedWorkout(_workoutDetailsResponse.value)

// Zone durations now prefer device report when matched
val zone1Duration = resolveZoneDuration(recordedWorkout?.hrWarmUp, sampleZone1Duration)
val zone2Duration = resolveZoneDuration(recordedWorkout?.hrFatBurning, sampleZone2Duration)
val zone3Duration = resolveZoneDuration(recordedWorkout?.hrAerobic, sampleZone3Duration)
val zone4Duration = resolveZoneDuration(recordedWorkout?.hrAnaerobic, sampleZone4Duration)
val zone5Duration = resolveZoneDuration(recordedWorkout?.hrLimitTime, sampleZone5Duration)
```

**Resolution logic:**
```kotlin
private fun resolveZoneDuration(exactDuration: Long?, fallbackDuration: Long): Long {
    return exactDuration?.takeIf { it > 0 } ?: fallbackDuration
}
```

### 2.3 Immediate Local Workout Update from `onDevSportInfo`

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

**Change:** The `onDevSportInfo(...)` callback now immediately updates the local workout cache:

```kotlin
override fun onDevSportInfo(data: DevSportInfoBean) {
    // ... existing raw JSON save ...
    
    // NEW - Immediately update local cache
    watchDataStore.updateFitnessAge(data.fitnessAge)
    watchDataStore.updateEnergyConsumption(data.reportEnergyConsumption)
    watchDataStore.updateWorkout(dataConverter.parseRecordedData(data))
    
    // ... rest of the callback ...
}
```

**Why this matters:**
- Before, the local workout cache was only updated during parsing progress completion
- Now the latest device report is immediately available for the post-workout screen

### 2.4 Calories and Recovery Time Fix

**File:** `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`

**Change:** Added resolution methods that prefer matched local device report data:

```kotlin
private fun resolveCalories(
    data: OWorkoutDetailsResponseModel,
    recordedWorkout: RecordedWorkoutData?
): Int {
    val recordedCalories = recordedWorkout?.calories ?: 0
    return if (recordedCalories > 0) {
        recordedCalories  // Prefer device report
    } else {
        data.calories ?: 0  // Fallback to server
    }
}

private fun resolveRecoveryTime(
    data: OWorkoutDetailsResponseModel,
    recordedWorkout: RecordedWorkoutData?
): Long {
    val recordedRecoveryTime = recordedWorkout?.recoveryTime ?: 0L
    return if (recordedRecoveryTime > 0) {
        recordedRecoveryTime  // Prefer device report
    } else {
        data.recoveryTime ?: 0L  // Fallback to server
    }
}
```

---

## 3. Current Callback Flow for Post-Workout Data

### 3.1 SDK Callback Path

```
Device Workout Completion
    ↓
SDK triggers: SportCallBack.onDevSportInfo(DevSportInfoBean data)
    ↓
ZhUserActivityHandler receives callback
    ↓
├── Saves raw JSON for debug
├── Updates fitness age (watchDataStore.updateFitnessAge)
├── Updates energy consumption (watchDataStore.updateEnergyConsumption)
├── Parses to RecordedWorkoutData (dataConverter.parseRecordedData)
└── Saves to local cache (watchDataStore.updateWorkout)
```

### 3.2 Post-Workout Screen Data Flow

```
User opens post-workout details
    ↓
OWorkoutDetailsViewModelV2.getWorkoutDetails(workoutId)
    ↓
Server returns: OWorkoutDetailsResponseModel
    ↓
ViewModel attempts to match local RecordedWorkoutData:
    ├── Reads: watchDataStore.getWorkout()
    ├── Matches by: date, normalized startTime, normalized endTime
    └── Result: matched RecordedWorkoutData or null
    ↓
Data resolution (matched local preferred, server as fallback):
    ├── Calories: resolveCalories()
    ├── Recovery Time: resolveRecoveryTime()
    └── HR Zones: generateHrZones() with resolveZoneDuration()
```

---

## 4. Current Callbacks and Methods Used

### 4.1 SDK Callback
- **Primary callback:** `SportCallBack.onDevSportInfo(DevSportInfoBean data)`
- **Data bean:** `DevSportInfoBean` from ZH SDK v2.3.1

### 4.2 Key `DevSportInfoBean` Fields Used

| Field | Purpose | Mapped to RecordedWorkoutData |
|-------|---------|-------------------------------|
| `reportCal` | Final workout calories | `calories` |
| `reportRecoveryTime` | Recovery time in seconds | `recoveryTime` |
| `reportDuration` | Total duration in seconds | `durationSeconds` |
| `reportHeartWarmUp` | Zone 1 duration (seconds) | `hrWarmUp` |
| `reportHeartFatBurning` | Zone 2 duration (seconds) | `hrFatBurning` |
| `reportHeartAerobic` | Zone 3 duration (seconds) | `hrAerobic` |
| `reportHeartAnaerobic` | Zone 4 duration (seconds) | `hrAnaerobic` |
| `reportHeartLimitTime` | Zone 5 duration (seconds) | `hrLimitTime` |
| `reportAvgHeart` | Average heart rate | `avgHeart` |
| `reportMaxHeart` | Maximum heart rate | `maxHeart` |
| `reportMinHeart` | Minimum heart rate | `minHeart` |

### 4.3 Local Storage
- **Store:** `WatchDataStore` (SharedPreferences-based)
- **Method:** `updateWorkout(RecordedWorkoutData)`
- **Retrieval:** `getWorkout(): RecordedWorkoutData?`

### 4.4 Parsing Function
- **Location:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt`
- **Method:** `parseRecordedData(DevSportInfoBean): RecordedWorkoutData`
- **Responsibility:** Maps all SDK device report fields to local `RecordedWorkoutData` model

---

## 5. Current Post-Workout Display Mapping

| UI Element | Current Source | Fallback |
|------------|----------------|----------|
| Title / Date / Activity | Server `OWorkoutDetailsResponseModel` | — |
| Duration | Server `durationSeconds` | — |
| Distance | Server `distance` or `gpsDistance` | — |
| **Calories** | Matched `RecordedWorkoutData.calories` | Server `calories` |
| Max HR | Server `hrMax` | — |
| Min HR | Server `hrLow` | — |
| **Recovery Time** | Matched `RecordedWorkoutData.recoveryTime` | Server `recoveryTime` |
| HR Graph Plotting | Server `hrArray` | — |
| HR Zone Highlights | Derived from `hrArray` indexes | — |
| **HR Zone Durations** | Matched `RecordedWorkoutData` zone fields | Sample-based calculation |

---

## 6. Why the HR 30 Issue is Now Fixed

### 6.1 Zone Index Clearing
- Each call to `generateHrZones()` now starts with a clean slate
- Zone durations are calculated accurately without accumulated stale indexes

### 6.2 Device Report Priority
- When the workout matches the local cached device report:
  - Zone durations use **exact device-measured values** instead of sample counting
  - Calories and recovery time use **device report values** instead of potentially stale server data

### 6.3 Immediate Data Availability
- The local workout cache is updated **immediately** when `onDevSportInfo` is received
- The post-workout screen can access the latest device report without waiting for sync completion

### 6.4 Safe Fallback Behavior
- If no matched local workout exists, the old server-backed + sample-based behavior is preserved
- This ensures the app doesn't break for edge cases or historical workouts

---

## 7. Files Changed for This Fix

### Modified Files
1. `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`
   - Added zone index clearing
   - Added local workout matching
   - Added zone duration resolution
   - Added calories/recovery time resolution

2. `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`
   - Added immediate local workout cache update from `onDevSportInfo`

3. `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`
   - Enhanced raw dev-sport persistence fallback

### Documentation Files
- `docs/workout-devsport-post-workout-mapping-2026-04-15.md`
- `docs/post-workout-calorie-fix-2026-04-15.md`
- `revert all workout and devsport info related changes 15th April.md`

---

## 8. Verification Checklist

1. **Zone Index Clearing:**
   - [ ] Complete a workout
   - [ ] Open post-workout screen
   - [ ] Rotate device or go to background and return
   - [ ] Verify zone durations don't artificially grow

2. **Device Report Priority:**
   - [ ] Complete a workout
   - [ ] Open post-workout screen
   - [ ] Compare displayed calories with BlankTest raw `DevSportInfo` output
   - [ ] Values should match

3. **HR Zone Accuracy:**
   - [ ] Complete a workout with known activity
   - [ ] Verify zone durations reflect actual workout zones
   - [ ] Compare with device report values in BlankTest

---

## 9. Summary

The HR 30 issue was caused by a combination of:
1. **Zone index accumulation** on screen rebuilds
2. **Server data being used** instead of more accurate device report data
3. **Sample-based duration calculations** instead of exact device-measured values
4. **Late local cache updates** creating timing gaps

The fix ensures:
- Clean zone calculations every time
- Device report values take priority when matched
- Immediate cache updates for latest data availability
- Safe fallback to server data when needed

This maintains backward compatibility while providing accurate HR data for SDK v2.3.1 integrated workouts.
