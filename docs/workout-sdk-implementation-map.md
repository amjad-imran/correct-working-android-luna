# Workout SDK Implementation Map
**SDK Version**: ZH_SDK_20260420_V2.3.2  
**Date**: 2026-04-21  
**Scope**: Live HR during workouts · Post-workout data window · Home page calories & steps · Activities tab

---

## 1. Live Heart Rate During a Workout

### Transport path
Luna Band and Ring both use the **ring-sport transport** (switched April 21, 2026).  
The secondary-screen transport (`SecondaryScreenSportCallBack`) is still registered as a fallback but is not the active live-data path.

### SDK method that starts/updates/stops the workout session

| Action | SDK method | Bean passed |
|--------|-----------|-------------|
| Start | `ControlBleTools.getInstance().sendRingSportStatus(bean, listener)` | `SendRingSportStatusBean(sportType, START, startTime)` |
| Pause | `ControlBleTools.getInstance().sendRingSportStatus(bean, listener)` | `SendRingSportStatusBean(sportType, PAUSE, ts)` |
| Resume | `ControlBleTools.getInstance().sendRingSportStatus(bean, listener)` | `SendRingSportStatusBean(sportType, RESUME, ts)` |
| Stop | `ControlBleTools.getInstance().sendRingSportStatus(bean, listener)` | `SendRingSportStatusBean(sportType, STOP, ts)` |
| Poll status | `ControlBleTools.getInstance().getRingSportStatus(listener)` | — |

File: [ZhUpdateDeviceUnitsHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt) lines 799–868

### SDK callback that delivers live HR data

**Callback interface**: `RingSportCallBack`  
**Registered**: `CallBackUtils.ringSportCallBack = ringSportCallback`  
File: [ZhUpdateDeviceUnitsHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt) line 494

#### `onRingSportStatus(bean: RingSportStatusBean?)`
Called when workout start/pause/resume/stop is acknowledged by the device.  
Drives the workout state machine (confirming start, detecting device-side end, error reasons).

**`RingSportStatusBean` fields used**:
- `bean.startResult` — confirms start succeeded or failed (low battery, charging, not worn)
- `bean.sportStatus` — current ring sport state (`SPORT_STATUS_START`, `SPORT_STATUS_END`, etc.)
- `bean.endReason` — why the ring ended the workout (battery, timeout, memory, charging)
- `bean.isSporting` — whether a workout is in progress
- `bean.startTime` — workout start timestamp

#### `onRingSportData(bean: RingSportDataBean?)`
Called continuously during the workout with real-time metrics.

**`RingSportDataBean` fields and their app mapping**:

| SDK field | Type | Meaning (SDK doc) | Maps to `WorkoutRealTimeData` field |
|-----------|------|-------------------|-------------------------------------|
| `heartRate` | `Int` | Heart rate | `hrValue` |
| `calories` | `Int` | Calories burned | `calorieValue` |
| `steps` | `Int` | Steps taken | `steps` |
| `distance` | `Int` | Distance (metres) | `distance` (cast to `Long`) |

Implementation: [ZhUpdateDeviceUnitsHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt) lines 1031–1047

### App-internal callback emitted

`UpdateDeviceDataCallback.WorkoutRealTimeDataObtained(WorkoutRealTimeData(hrValue, calorieValue, steps, distance))`

### UI consumption

File: [RecordWorkoutFragmentV2.kt](app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt) lines 631–632, 412–432

- `updateWorkoutData(workoutRealTimeData)` → splits into separate UI bindings
- `updateWorkoutHeartRate(hrValue)` — renders HR; filters out `hrValue == 0` and `hrValue == 255` (both map to `"--"`)
- `calorieValue` is rendered as a string; `null` maps to `"-"`

> **Known gap (documented April 21, 2026)**: `steps` and `distance` from `WorkoutRealTimeData` are available in the model but are not yet rendered in the V2 live workout screen.

---

## 2. Post-Workout Window (Workout Details after Stop)

### Trigger sequence

1. User (or device) stops the workout.
2. `publishWorkoutStoppedConfirmed()` fires in `ZhUpdateDeviceUnitsHandler`.
3. Both workout fragments post `UserActivityAction.SyncSportsActivity(date)`.
4. `ZhUserActivityHandler.syncSportsActivity()` calls `startRecordedWorkoutSync(includeAutoSport=true, ...)`.
5. `startRecordedWorkoutSync()` calls:
   - `ControlBleTools.getInstance().getFitnessSportIdsData(null)` — fetches recorded workout history
   - `ControlBleTools.getInstance().getAutoSportData(null)` — fetches auto-detected sport history

### SDK callbacks that deliver post-workout data

#### `SportCallBack.onDevSportInfo(data: DevSportInfoBean)`
**Registered**: `CallBackUtils.setSportCallBack(object : SportCallBack { ... })`  
File: [ZhUserActivityHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt) lines 146–156

Called once per recorded workout item as the device streams them.

#### `SportParsingProgressCallBack`
**Registered**: `CallBackUtils.setSportParsingProgressCallBack { progress, total -> ... }`  
File: [ZhUserActivityHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt) lines 168–179

`progress == total` triggers `completeRecordedWorkoutSync("progress_complete")`.  
`total == 0` triggers `completeRecordedWorkoutSync("progress_total_zero")` (no records available).  
Timeout (15 s) also triggers completion as a safety net.

### `DevSportInfoBean` — fields used and mapping to `RecordedWorkoutData`

Conversion function: `DataConverter.parseRecordedData(it: DevSportInfoBean)`  
File: [DataConverter.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt) lines 1108–1184

| `DevSportInfoBean` field | SDK doc meaning | `RecordedWorkoutData` field |
|--------------------------|-----------------|----------------------------|
| `reportCal` (`Long`) | Calorie kcal | `calories` (cast to `Int`) |
| `reportDistance` (`Long`) | Total distance m | `distance` |
| `reportDuration` (`Long`) | Sport total duration s | `durationSeconds`; also `duration = reportDuration / 60` (minutes) |
| `reportTotalStep` (`Long`) | Total steps | `steps` (cast to `Int`) |
| `reportSportStartTime` (`Long`) | Sport start time ms | `startTime` |
| `reportSportEndTime` (`Long`) | Sport end time ms | `endTime` |
| `recordPointSportType` (`Int`) | Sport type ID (1–266+) | `type` |
| `ringPointData` (`ArrayList<RingPointData>`) | Ring dot data (per-point HR) | `hrData` (JSON array of `heartRate` ints) |
| `reportAvgHeart` (`Int`) | Average heart rate | `avgHeart` |
| `reportMaxHeart` (`Int`) | Maximum heart rate | `maxHeart` |
| `reportMinHeart` (`Int`) | Minimum heart rate | `minHeart` |
| `reportRecoveryTime` (`Long`) | Estimated recovery time | `recoveryTime` |
| `fitnessAge` (`Int`) | Fitness age | `fitnessAge` |
| `reportEnergyConsumption` (`Int`) | Body energy consumption | `energyConsumption` |
| `reportVO2max` (`Float`) | VO2 max | `vo2Max` |
| `reportTrainingEffect` (`Float`) | Training effect | `trainingEffect` |
| `reportTrainingLoad` (`Int`) | Training load | `trainingLoad` |
| `reportAvgPace` / `reportFastPace` | Avg/fast pace | `avgPace` / `fastPace` |
| `reportAvgSpeed` / `reportFastSpeed` | Avg/fast speed | `avgSpeed` / `fastSpeed` |
| `reportHeartLimitTime` / `reportHeartAnaerobic` / `reportHeartAerobic` / `reportHeartFatBurning` / `reportHeartWarmUp` | HR zone durations | `hrLimitTime` / `hrAnaerobic` / `hrAerobic` / `hrFatBurning` / `hrWarmUp` |
| `reportAvgStride` / `reportMaxStride` / `reportMinStride` | Stride stats | `avgStride` / `maxStride` / `minStride` |
| `reportCumulativeRise` / `reportCumulativeDecline` | Elevation gain/loss | `cumulativeRise` / `cumulativeDecline` |
| `reportAvgHeight` / `reportMaxHeight` / `reportMinHeight` | Altitude stats | `avgHeight` / `maxHeight` / `minHeight` |

#### `RingPointData` sub-bean (inside `ringPointData` array)

| Field | Meaning |
|-------|---------|
| `pointTime` (`Long`) | Timestamp of the dot |
| `heartRate` (`Int`) | Heart rate at that point |
| `heartRateConfidence` (`Int`) | Confidence of HR reading |
| `exerciseIntensity` (`Int`) | Exercise intensity at that point |

Only `heartRate` and `exerciseIntensity` are extracted; they are stored in `RecordedWorkoutData.hrData` (JSON) and `intensityList` (JSON).

### App-internal callback emitted (sync complete)

`UserActivityCallback.RingUserWorkoutData(data: List<RecordedWorkoutData>)`  
Emitted by `completeRecordedWorkoutSync()` → observed in `RingConnectionService`.

---

## 3. Home Page — Calories & Steps

### SDK method that requests the data

`ControlBleTools.getInstance().getDailyHistoryData(mode, null)`  
- Mode 3 used for Luna Band (today + history).

File: [ZhUserActivityHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt) lines 449–483

### SDK callback

**Callback interface**: `FitnessDataCallBack.onDailyData(data: DailyBean)`  
**Registered**: `CallBackUtils.fitnessDataCallBack = fitnessDataCallBack`  
File: [ZhUserActivityHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt) line 144

### `DailyBean` — fields used for home page steps and calories

| `DailyBean` field | SDK doc meaning | How it's used |
|-------------------|-----------------|---------------|
| `stepsData` (`List<Int>`) | Hourly step data | Summed → `totalSteps`; per-hour → `stepArray[i].steps` |
| `distanceData` (`List<Int>`) | Hourly distance data | Summed → `totalDistance`; per-hour → `stepArray[i].distance` |
| `calorieData` (`List<Int>`) | Hourly calorie data (Apple-style) | Per-hour via `resolveHourlyCalories()`; sum fallback for total |
| `todayCalorieData` (`Int`) | Today's total calories (Apple, Ring) | **Primary** source for `totalCalories` |
| `todayOuraCalorieData` (`Int`) | Today's walking calories (Oura, Ring) | **Fallback** total calories |
| `todayOuraCalorieHourlyData` (`List<Int>`) | Hourly walking calories (Oura, Ring) | Fallback for per-hour calories when `calorieData[i] == 0` |
| `todaySportCalorieData` (`Int`) | Today's active/exercise calories | `activeCalories` on daily summary |
| `todaySportCalorieHourlyData` (`List<Int>`) | Hourly active calories | `stepArray[i].activeCalories` |

#### Calorie resolution logic (`LunaSdk231DataNormalizer`)

**Total calories** (`resolveTotalCalories`):
1. `todayCalorieData` if > 0 → use directly
2. Else sum of `calorieData` list if > 0
3. Else `todayOuraCalorieData`

**Hourly calories** (`resolveHourlyCalories(index)`):
1. `calorieData[index]` if > 0 → use directly
2. Else `todayOuraCalorieHourlyData[index]`

File: [OreoDataConverter.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt) lines 1433–1456

### App-internal callback emitted

`UserActivityCallback.StepsDataObtainedOreo(oreoDataConverter.parseStepsData(data))`  
→ `OreoStepsData` containing `totalCalories`, `totalSteps`, `totalDistance`, `activeCalories`, `stepArray`

---

## 4. Activities Tab — Calories, Steps, and Workout History

### Daily activity data (calories & steps)
Identical pipeline to Home Page above:  
`FitnessDataCallBack.onDailyData(DailyBean)` → `StepsDataObtainedOreo(OreoStepsData)`

### Recorded workouts list
`SportCallBack.onDevSportInfo(DevSportInfoBean)` → `DataConverter.parseRecordedData()` → `RecordedWorkoutData`  
Emitted as `UserActivityCallback.RingUserWorkoutData(List<RecordedWorkoutData>)` after sync completes.

### Auto-detected sports
**Callback**: `AutoSportDataCallBack`  
**Registered**: `CallBackUtils.autoSportDataCallBack = autoSportsCallback`  
**SDK method**: `ControlBleTools.getInstance().getAutoSportData(null)`  
**App callback**: `UserActivityCallback.AutoSportDataObtained(OreoAutoSportData)`

---

## Callback Registration Summary

| Callback | Registration point | File |
|----------|--------------------|------|
| `RingSportCallBack` | `CallBackUtils.ringSportCallBack = ringSportCallback` | ZhUpdateDeviceUnitsHandler.kt:494 |
| `SecondaryScreenSportCallBack` | `CallBackUtils.secondaryScreenSportCallBack = secondaryScreenSportCallback` | ZhUpdateDeviceUnitsHandler.kt:495 |
| `FitnessDataCallBack` | `CallBackUtils.fitnessDataCallBack = fitnessDataCallBack` | ZhUserActivityHandler.kt:144 |
| `SportCallBack` | `CallBackUtils.setSportCallBack(...)` | ZhUserActivityHandler.kt:146 |
| `SportParsingProgressCallBack` | `CallBackUtils.setSportParsingProgressCallBack { ... }` | ZhUserActivityHandler.kt:168 |
| `AutoSportDataCallBack` | `CallBackUtils.autoSportDataCallBack = autoSportsCallback` | ZhUserActivityHandler.kt:145 |
| `DeviceReminderEventCallBack` | `CallBackUtils.deviceReminderEventCallBack = ...` | ZhUserActivityHandler.kt:136 |
