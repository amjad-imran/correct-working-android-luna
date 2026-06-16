# Workout SDK Verification Report
**SDK Version**: ZH_SDK_20260420_V2.3.2  
**Date**: 2026-04-21  
**Verified against**: ZH Android SDK v2.3.2.txt (official SDK documentation)

Legend: ✅ Correct · ⚠️ Issue / Gap · ❌ Wrong

---

## A. Live HR During a Workout

### A1 — Transport path: ring-sport vs secondary-screen

**Claim**: Luna Band workouts now use `sendRingSportStatus()` / `getRingSportStatus()` / `RingSportCallBack`.

**SDK doc** (section 4.1.1):
> "发送运动状态给戒指 Send movement status to the ring … 手环项目支持发起的运动 (Bracelet projects support initiating exercise)"  
> `ControlBleTools.getInstance().sendRingSportStatus(SendRingSportStatusBean, listener)`  
> `CallBackUtils.ringSportCallBack = object : RingSportCallBack { ... }`

**Implementation**: `ZhUpdateDeviceUnitsHandler.startWorkout()` uses `sendRingSportStatus(bean, ...)` and `checkOngoingWorkout()` uses `getRingSportStatus(...)`. Both are the correct SDK methods for this transport.

**Verdict**: ✅ **CORRECT** — Ring-sport transport is the right path for both Ring and Luna Band per SDK doc v2.3.2.

---

### A2 — Live HR field: `RingSportDataBean.heartRate`

**Claim**: `onRingSportData(bean: RingSportDataBean)` delivers live HR via `bean.heartRate`.

**SDK doc** (section 4.1.1, `RingSportDataBean`):
```java
public class RingSportDataBean implements Serializable {
    public int heartRate;  // 心率 Heart rate
    public int calories;   // 卡路里 Calorie
    public int steps;      // 步数 Steps
    public int distance;   // 距离 Distance
}
```

**Implementation** ([ZhUpdateDeviceUnitsHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt) line 1037):
```kotlin
hrValue = p0.heartRate
```

**Verdict**: ✅ **CORRECT** — Field name matches SDK doc exactly.

---

### A3 — Live calories during workout: `RingSportDataBean.calories`

**Claim**: Live calorie display during workout uses `bean.calories` from `RingSportDataBean`.

**SDK doc**: `public int calories; // 卡路里 Calorie`

**Implementation** (line 1038): `calorieValue = p0.calories`

**Verdict**: ✅ **CORRECT** — Field name matches SDK doc.

---

### A4 — Live steps and distance during workout

**Claim**: `RingSportDataBean.steps` and `RingSportDataBean.distance` are mapped to `WorkoutRealTimeData`.

**SDK doc**: `public int steps` and `public int distance` both exist in `RingSportDataBean`.

**Implementation** (lines 1039–1040):
```kotlin
steps = p0.steps
distance = p0.distance.toLong()
```
The `toLong()` cast is safe: SDK declares `distance` as `Int`, `WorkoutRealTimeData.distance` is `Long`.

**UI rendering**: ⚠️ **GAP** — `WorkoutRealTimeData.steps` and `.distance` are correctly received and stored in the model, but `RecordWorkoutFragmentV2.updateWorkoutData()` only renders `hrValue` and `calorieValue`. The live workout screen does not yet display steps or distance to the user.

**Verdict**: ✅ SDK mapping correct · ⚠️ UI not yet rendering steps/distance live

---

### A5 — HR validity filter in UI

**Claim**: HR values of `0` and `255` are filtered out and shown as `"--"`.

**SDK doc**: No explicit documentation of sentinel values for invalid HR, but `0` = not measured and `255` is the standard BLE "measurement invalid" sentinel widely used in ZH SDK beans.

**Implementation** ([RecordWorkoutFragmentV2.kt](app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt) line 431):
```kotlin
if (hrValue != null && hrValue != 0 && hrValue != 255) hrValue.toString() else "--"
```

**Verdict**: ✅ **CORRECT** — Standard sentinel filtering.

---

### A6 — Callback registration for live workout

**Claim**: `CallBackUtils.ringSportCallBack = ringSportCallback` is called inside `attachCallbacks()`.

**SDK doc** example: `CallBackUtils.ringSportCallBack = object : RingSportCallBack { ... }`

**Implementation** ([ZhUpdateDeviceUnitsHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt) line 494): matches the SDK pattern exactly.

**Verdict**: ✅ **CORRECT**

---

## B. Post-Workout Window — Calories, Distance, Duration, Steps, HR

### B1 — SDK method to fetch recorded workout data

**Claim**: `ControlBleTools.getInstance().getFitnessSportIdsData(null)` fetches the completed workout record.

**SDK doc** (section 2.3.7):
> `public void getFitnessSportIdsData(ParsingStateManager.SendCmdStateListener listener)`  
> "获取运动数据 Get sport data"

**Implementation** ([ZhUserActivityHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt) line 275): `ControlBleTools.getInstance().getFitnessSportIdsData(null)` — `null` listener is acceptable per SDK signature.

**Verdict**: ✅ **CORRECT**

---

### B2 — SDK callback for post-workout data: `SportCallBack.onDevSportInfo`

**Claim**: `CallBackUtils.setSportCallBack(object : SportCallBack { ... })` is the correct registration for `onDevSportInfo`.

**SDK doc** (section 2.3.7):
```kotlin
CallBackUtils.sportCallBack = object : SportCallBack {
    override fun onDevSportInfo(bean: DevSportInfoBean) { }
}
```
> Note: SDK example uses `CallBackUtils.sportCallBack = ...` (property setter), but the codebase uses `CallBackUtils.setSportCallBack(...)` (method call). These are functionally equivalent and both appear in the SDK documentation for different sections. The `setSportCallBack()` form is used in section 2.3.10 (screenless band data mode) which is the applicable section for Luna Band.

**Implementation** ([ZhUserActivityHandler.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt) line 146): `CallBackUtils.setSportCallBack(object : SportCallBack { override fun onDevSportInfo(data: DevSportInfoBean) { ... } })`

**Verdict**: ✅ **CORRECT** — `setSportCallBack()` is the correct form for the screenless-band transport mode.

---

### B3 — Post-workout calories: `DevSportInfoBean.reportCal`

**SDK doc**: `private long reportCal; // 卡路里 kcal Calorie kcal`

**Implementation** ([DataConverter.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt) line 1150):
```kotlin
calories = it.reportCal.toInt()
```
Cast from `Long` to `Int` is safe for typical calorie values (< 2 billion kcal).

**Verdict**: ✅ **CORRECT** — Correct field, correct unit (kcal).

---

### B4 — Post-workout distance: `DevSportInfoBean.reportDistance`

**SDK doc**: `private long reportDistance; // 总里程 m Total distance m`

**Implementation** (line 1143): `distance = it.reportDistance`

**Verdict**: ✅ **CORRECT** — Field name and unit (metres) match SDK doc.

---

### B5 — Post-workout duration: `DevSportInfoBean.reportDuration`

**SDK doc**: `private long reportDuration; // 运动总时长 s Sport total duration s`

**Implementation** (lines 1122, 1147–1148):
```kotlin
val duration = it.reportDuration.toInt() / 60  // minutes for display
durationSeconds = it.reportDuration             // raw seconds stored
duration = duration                              // minutes stored
```

**Verdict**: ✅ **CORRECT** — Raw seconds stored in `durationSeconds`, minutes derived correctly.

---

### B6 — Post-workout steps: `DevSportInfoBean.reportTotalStep`

**SDK doc**: `private long reportTotalStep; // 总步数 Total steps`

**Implementation** (line 1153): `steps = it.reportTotalStep.toInt()`

**Verdict**: ✅ **CORRECT**

---

### B7 — Post-workout HR data: `DevSportInfoBean.ringPointData` (not `recordPointSportData`)

**Claim**: Per-point HR data for a Ring/Luna Band workout is sourced from `ringPointData`, not from `recordPointSportData`.

**SDK doc** (`DevSportInfoBean`):
- `private ArrayList<RecordPointSportData> recordPointSportData;` — "打点数据 Dot data" (watch/standard)
- `private ArrayList<RingPointData> ringPointData;` — "戒指打点数据 Ring dot data"

`RingPointData.heartRate` — "心率 Heart rate"  
`RingPointData.exerciseIntensity` — "运动强度 Exercise intensity"

**Implementation** ([DataConverter.kt](noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt) lines 1115–1120):
```kotlin
if (it.ringPointData != null && it.ringPointData.isNotEmpty()) {
    it.ringPointData.forEach { ringPointData ->
        hrData.add(ringPointData.heartRate)
        intensity.add(ringPointData.exerciseIntensity)
    }
}
```

**Verdict**: ✅ **CORRECT** — Uses `ringPointData` (ring-specific) not `recordPointSportData` (watch-specific). This is the correct field for ring/Luna Band devices.

---

### B8 — Post-workout avg/max/min HR from report-level fields

**SDK doc**:
- `reportAvgHeart` — Average heart rate
- `reportMaxHeart` — Maximum heart rate
- `reportMinHeart` — Minimum heart rate

**Implementation** (lines 1163–1165): `avgHeart`, `maxHeart`, `minHeart` mapped correctly.

**Verdict**: ✅ **CORRECT**

---

### B9 — Progress / completion tracking via `SportParsingProgressCallBack`

**SDK doc** (section 2.3.7):
```java
void onProgress(int progress, int total); // 进度 Progress / 总进度 Total progress
```

**Implementation** (lines 168–179):
- `total == 0` → no records, call `completeRecordedWorkoutSync("progress_total_zero")` immediately
- `progress == total` → all records received, call `completeRecordedWorkoutSync("progress_complete")`
- 15-second timeout guard as additional safety net

**Verdict**: ✅ **CORRECT** — Both normal and zero-record cases handled.

---

## C. Home Page — Calories & Steps

### C1 — SDK method: `getDailyHistoryData`

**SDK doc** (section 2.3.6): `getDailyHistoryData(mode, listener)` — mode 3 = today + history.

**Implementation**: mode 3 for Luna Band/Ring. ✅ **CORRECT**

---

### C2 — Total calories: priority order

**Claim**: `resolveTotalCalories()` uses `todayCalorieData` → `calorieData.sum()` → `todayOuraCalorieData`.

**SDK doc** (`DailyBean`):
- `todayCalorieData` — "当天当前的卡路里（戒指项目对标Apple）Calories for the day (Rings project compares to Apple)"
- `calorieData` — "卡路里数据（在戒指项目对标Apple）" — list, 0 = invalid
- `todayOuraCalorieData` — "当天当前的走路卡路里（戒指项目对标Oura的卡路里）"

The priority makes sense: `todayCalorieData` is the definitive Apple-style total when available. `calorieData` sum as a fallback constructs the total from hourly segments. `todayOuraCalorieData` (Oura walking calories) is a last resort.

**Verdict**: ✅ **CORRECT** — Priority correctly handles the SDK's multiple calorie sources.

---

### C3 — Hourly calories: priority order

**Claim**: `resolveHourlyCalories(index)` uses `calorieData[index]` → `todayOuraCalorieHourlyData[index]`.

**SDK doc**: Both fields exist in `DailyBean`; `0 = invalid` documented on both.

**Verdict**: ✅ **CORRECT**

---

### C4 — Active/exercise calories: `todaySportCalorieData`

**SDK doc**: `todaySportCalorieData` — "当天当前的运动卡路里 Current exercise calories for the day"

**Implementation**: `dailyStepData.activeCalories = dailyBean.todaySportCalorieData` ✅ **CORRECT**

---

### C5 — Steps: summing `stepsData` list

**SDK doc**: `stepsData: List<Integer>` — "步数数据 Step data (0 = invalid)"

**Implementation**: iterates the list, sums non-zero values into `totalSteps`. ✅ **CORRECT**

---

### C6 — Distance: summing `distanceData` list

**SDK doc**: `distanceData: List<Integer>` — "距离数据 Distance data (0 = invalid)"

**Implementation**: sums into `totalDistance`. ✅ **CORRECT**

---

## D. Activities Tab

### D1 — Recorded workouts list

Same pipeline as Post-Workout Window (section B above).  
`getFitnessSportIdsData` → `SportCallBack.onDevSportInfo` → `parseRecordedData` → `RecordedWorkoutData`.  
**Verdict**: ✅ **CORRECT** — identical path, no separate issues.

### D2 — Auto-detected sports

**SDK doc** (section 4.1.5): `getAutoSportData(listener)` → `AutoSportDataCallBack`  
**Implementation**: `ControlBleTools.getInstance().getAutoSportData(null)` + `CallBackUtils.autoSportDataCallBack = autoSportsCallback`  
**Verdict**: ✅ **CORRECT**

---

## Summary Table

| Feature | Area | Status | Notes |
|---------|------|--------|-------|
| Live HR field (`heartRate`) | Live workout | ✅ Correct | `RingSportDataBean.heartRate` |
| Live calories field (`calories`) | Live workout | ✅ Correct | `RingSportDataBean.calories` |
| Live steps/distance mapped | Live workout | ✅ Correct (model) | ⚠️ Not rendered in V2 UI |
| HR validity filter (0, 255) | Live workout UI | ✅ Correct | Standard sentinel values |
| Transport: ring-sport for Luna Band | Live workout | ✅ Correct | SDK doc explicitly states bracelet support |
| `ringSportCallBack` registration | Live workout | ✅ Correct | Matches SDK pattern |
| `getFitnessSportIdsData` trigger | Post-workout | ✅ Correct | Correct SDK method for recorded workouts |
| `setSportCallBack` registration | Post-workout | ✅ Correct | Right form for screenless-band mode |
| Post-workout calories (`reportCal`) | Post-workout | ✅ Correct | kcal, `Long` → `Int` cast safe |
| Post-workout distance (`reportDistance`) | Post-workout | ✅ Correct | Metres, matching SDK doc |
| Post-workout duration (`reportDuration`) | Post-workout | ✅ Correct | Seconds raw + minutes derived |
| Post-workout steps (`reportTotalStep`) | Post-workout | ✅ Correct | |
| HR dot data (`ringPointData`, not `recordPointSportData`) | Post-workout | ✅ Correct | Ring-specific field correctly chosen |
| Avg/max/min HR (`reportAvgHeart` etc.) | Post-workout | ✅ Correct | |
| Progress callback completion | Post-workout | ✅ Correct | Zero-record and normal paths handled |
| Daily calories (`todayCalorieData` priority) | Home page | ✅ Correct | Three-tier fallback appropriate |
| Daily steps (`stepsData` sum) | Home page | ✅ Correct | |
| Daily distance (`distanceData` sum) | Home page | ✅ Correct | |
| Active calories (`todaySportCalorieData`) | Home page | ✅ Correct | |
| Recorded workouts in Activities | Activities tab | ✅ Correct | Same as post-workout path |
| Auto-sport data in Activities | Activities tab | ✅ Correct | `getAutoSportData` + `AutoSportDataCallBack` |

---

## Outstanding Gap

| Gap | Location | Impact | Recommendation |
|-----|----------|--------|----------------|
| Live steps and distance not displayed in V2 workout screen | `RecordWorkoutFragmentV2.updateWorkoutData()` | Minor UX — data is received and available in `WorkoutRealTimeData`, just not rendered | Add UI binding for `steps` and `distance` fields in `updateWorkoutData()` |
