# Advanced Workout Metrics Implementation Status

**Document Created:** April 16, 2026  
**Location:** BlankTestFragment → "Advanced Workout Metrics" section  
**Data Source:** `watchDataStore.getWorkout()` → `OreoWorkout` entity

---

## Overview

The BlankTestFragment's "Advanced Workout Metrics" section displays comprehensive workout data collected from the connected device. All metrics are populated from the local database via `WatchDataStore.getWorkout()`, which returns an `OreoWorkout` entity.

---

## Implementation Status: All Metrics Implemented ✅

All 21 advanced workout metrics are fully implemented and displaying data from the workout entity.

---

## Metrics Breakdown

### Performance Metrics

| Metric | View ID | Data Field | Status | Description |
|--------|---------|------------|--------|-------------|
| **VO2 MAX** | `tvWorkoutVo2Max` | `workout.vo2Max` | ✅ Implemented | Maximum oxygen uptake capacity |
| **TRAINING EFF** | `tvWorkoutTrainingEffect` | `workout.trainingEffect` | ✅ Implemented | Training effectiveness score |
| **TRAINING LOAD** | `tvWorkoutTrainingLoad` | `workout.trainingLoad` | ✅ Implemented | Cumulative training load |

### Heart Rate Metrics

| Metric | View ID | Data Field | Status | Description |
|--------|---------|------------|--------|-------------|
| **AVG HR** | `tvWorkoutAvgHeart` | `workout.avgHeart` | ✅ Implemented | Average heart rate during workout |
| **MAX HR** | `tvWorkoutMaxHeart` | `workout.maxHeart` | ✅ Implemented | Maximum heart rate reached |
| **MIN HR** | `tvWorkoutMinHeart` | `workout.minHeart` | ✅ Implemented | Minimum heart rate recorded |
| **HR LIMIT** | `tvWorkoutHrLimit` | `workout.hrLimitTime` | ✅ Implemented | Time spent at heart rate limit |
| **ANAEROBIC** | `tvWorkoutHrAnaerobic` | `workout.hrAnaerobic` | ✅ Implemented | Time in anaerobic zone |
| **AEROBIC** | `tvWorkoutHrAerobic` | `workout.hrAerobic` | ✅ Implemented | Time in aerobic zone |
| **FAT BURN** | `tvWorkoutHrFatBurn` | `workout.hrFatBurning` | ✅ Implemented | Time in fat burning zone |
| **WARM UP** | `tvWorkoutHrWarmUp` | `workout.hrWarmUp` | ✅ Implemented | Time in warm up zone |

### Pace & Speed Metrics

| Metric | View ID | Data Field | Status | Description |
|--------|---------|------------|--------|-------------|
| **AVG PACE** | `tvWorkoutAvgPace` | `workout.avgPace` | ✅ Implemented | Average pace (min/km or min/mile) |
| **FAST PACE** | `tvWorkoutFastPace` | `workout.fastPace` | ✅ Implemented | Fastest pace achieved |
| **AVG SPEED** | `tvWorkoutAvgSpeed` | `workout.avgSpeed` | ✅ Implemented | Average speed |
| **FAST SPEED** | `tvWorkoutFastSpeed` | `workout.fastSpeed` | ✅ Implemented | Maximum speed achieved |

### Stride Metrics

| Metric | View ID | Data Field | Status | Description |
|--------|---------|------------|--------|-------------|
| **AVG STRIDE** | `tvWorkoutAvgStride` | `workout.avgStride` | ✅ Implemented | Average stride length |
| **MAX STRIDE** | `tvWorkoutMaxStride` | `workout.maxStride` | ✅ Implemented | Maximum stride length |
| **MIN STRIDE** | `tvWorkoutMinStride` | `workout.minStride` | ✅ Implemented | Minimum stride length |

### Elevation Metrics

| Metric | View ID | Data Field | Status | Description |
|--------|---------|------------|--------|-------------|
| **CUM. RISE** | `tvWorkoutCumRise` | `workout.cumulativeRise` | ✅ Implemented | Total elevation gain |
| **CUM. DECLINE** | `tvWorkoutCumDecline` | `workout.cumulativeDecline` | ✅ Implemented | Total elevation loss |
| **AVG HEIGHT** | `tvWorkoutAvgHeight` | `workout.avgHeight` | ✅ Implemented | Average altitude during workout |

---

## Additional Workout Fields (Outside "Advanced" Section)

These fields are displayed in the "Latest Workout Summary" card, not the "Advanced Workout Metrics" section:

| Metric | View ID | Data Field | Status |
|--------|---------|------------|--------|
| Cadence | `tvWorkoutCadence` | `workout.cadence` | ✅ Implemented |
| Distance | `tvWorkoutDistance` | `workout.distance` | ✅ Implemented |
| Recovery | `tvWorkoutRecovery` | `workout.recoveryTime` | ✅ Implemented |
| Fitness Age | `tvWorkoutFitnessAge` | `workout.fitnessAge` | ✅ Implemented |
| Energy Consumption | `tvWorkoutEnergyConsumption` | `workout.energyConsumption` | ✅ Implemented |
| Date | `tvWorkoutDate` | `workout.date` | ✅ Implemented |
| HR Data | `tvWorkoutHrData` | `workout.hrData` | ✅ Implemented |
| Intensity List | `tvWorkoutIntensityList` | `workout.intensityList` | ✅ Implemented |

---

## Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         SDK LAYER                               │
├─────────────────────────────────────────────────────────────────┤
│  ZH SDK → WorkoutDataCallback → OreoSyncDataWork                │
│                     ↓                                           │
│  OreoDataConverter.parseWorkoutData() → OreoWorkout             │
│                     ↓                                           │
│  syncRepository.saveWorkoutData() → Local DB                    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         APP LAYER                               │
├─────────────────────────────────────────────────────────────────┤
│  BlankTestFragment.bindDashboardCards()                         │
│         ↓                                                       │
│  watchDataStore.getWorkout() → OreoWorkout                      │
│         ↓                                                       │
│  Display fields in UI TextViews                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Callbacks/Methods Used

### Primary Data Retrieval
- **Method:** `WatchDataStore.getWorkout(): OreoWorkout?`
- **Type:** Local database query (Room DAO)
- **Returns:** Latest stored workout entity or null

### Data Sync
- **SDK Callback:** Workout data obtained via sync callbacks
- **Parser:** `OreoDataConverter.parseWorkoutData()`
- **Storage:** `OreoSyncRepository.saveWorkoutData()`

### UI Update
- **Method:** `BlankTestFragment.bindDashboardCards()`
- **Frequency:** Every 2 seconds (via `dashboardRefreshJob` coroutine loop)
- **Thread:** Dispatches to `Dispatchers.Main` for UI updates

---

## Null Handling

All metrics use the `.nz()` extension function for null handling:
- If value is `null` or `0`: displays `"--"`
- If value is valid: displays the numeric value

---

## Summary

✅ **All 21 Advanced Workout Metrics are fully implemented**  
✅ **Data source is properly connected via `watchDataStore.getWorkout()`**  
✅ **Null handling is consistent with `"--"` placeholder**  
✅ **UI updates every 2 seconds automatically**
