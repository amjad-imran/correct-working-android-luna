# Workout and DevSport Post-Workout Mapping - April 15, 2026

## Scope
- Repository in scope: `noisefit-android-luna`
- Reference only: `zh-sdk` / AAR / `ZH SDK 文档 2.3.1.docx`
- Goal of this change set:
  - fix incorrect post-workout calories in the main app details window
  - fix incorrect recovery time in the same window
  - fix incorrect heart-rate zone durations in that window
  - restore BlankTest raw `DevSportInfo` visibility when the app really receives the callback
  - document the current callback and mapping flow precisely

## Best-fit implementation locations
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`
  - existing bridge where SDK sport callbacks are already attached
- `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`
  - existing lightweight persistence used by BlankTest raw previews and latest parsed workout cache
- `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`
  - existing ViewModel already preparing the post-workout details screen

## SDK v2.3.1 callbacks and methods used
- `ControlBleTools.getInstance().getFitnessSportIdsData(listener)`
  - request the device sports data sync
- `CallBackUtils.sportParsingProgressCallBack`
  - reports parsing progress `(progress, total)`
- `CallBackUtils.sportCallBack = object : SportCallBack { ... }`
  - production callback registration point
- `SportCallBack.onDevSportInfo(DevSportInfoBean data)`
  - callback that carries the parsed device workout report

## Relevant `DevSportInfoBean` fields for this task
- `reportCal`
  - final workout calories in kcal
- `reportRecoveryTime`
  - recovery time in seconds
- `reportHeartWarmUp`
  - zone 1 duration in seconds
- `reportHeartFatBurning`
  - zone 2 duration in seconds
- `reportHeartAerobic`
  - zone 3 duration in seconds
- `reportHeartAnaerobic`
  - zone 4 duration in seconds
- `reportHeartLimitTime`
  - zone 5 duration in seconds
- `reportDuration`
  - total workout duration in seconds

## Old implementation before this change

### 1. Main app post-workout details screen
- `OWorkoutDetailsViewModelV2.prepareDataForActivity(...)`
  - calories came only from server `OWorkoutDetailsResponseModel.calories`
  - recovery time came only from server `OWorkoutDetailsResponseModel.recoveryTime`
- `OWorkoutDetailsViewModelV2.getDistance(...)`
  - if the workout was calorie-priority instead of distance-priority, the top card also used server `calories`
- `OWorkoutDetailsViewModelV2.generateHrZones(...)`
  - zone durations were recomputed from `hrArray`
  - sample interval was hardcoded to `30` seconds
  - exact report zone durations from `DevSportInfoBean` were not used
  - zone index lists were not cleared before recomputation, so repeated UI refreshes could accumulate stale indexes and distort durations/highlights

### 2. `onDevSportInfo` handling
- `ZhUserActivityHandler.onDevSportInfo(...)`
  - raw JSON was saved for debug preview
  - payloads were collected into `sportModleInfoList`
  - the latest parsed workout cache was **not** updated immediately from this callback
- Local workout persistence mainly happened later inside `sportParsingProgressCallBack` only when `progress == total`

### 3. BlankTest raw `DevSportInfo` preview
- `WatchDataStoreImpl.testSaveRawDevSportJson(...)`
  - wrote through the session-envelope capture path only
  - if no active SDK raw capture session existed, the latest raw dev-sport string was not persisted as a directly readable value
- Result:
  - BlankTest could show nothing even when the app had actually received `onDevSportInfo(...)`

## Implemented behavior now

### 1. Immediate local workout update from `onDevSportInfo`
- `ZhUserActivityHandler.onDevSportInfo(...)` now immediately:
  - updates fitness age
  - updates energy consumption
  - stores the parsed `RecordedWorkoutData` produced by `DataConverter.parseRecordedData(data)`
- This keeps the latest local workout cache aligned with the exact device report instead of waiting only for the progress callback completion path.

### 2. Main app post-workout details window now prefers matched local report data
- `OWorkoutDetailsViewModelV2` now:
  - reads the latest `RecordedWorkoutData` from `WatchDataStore`
  - matches it against the current details response by:
    - workout `date`
    - normalized `start_time`
    - or normalized `end_time`
- Only when the workout clearly matches, the ViewModel prefers locally parsed `DevSportInfo` values.
- If no match is found, the old server-backed behavior is preserved.

### 3. Calories mapping after the fix
- Main activity list calories:
  - use `RecordedWorkoutData.calories` first when the workout matches
  - otherwise fall back to server `OWorkoutDetailsResponseModel.calories`
- Top summary card for calorie-priority workouts:
  - same fallback order
- Source of local `RecordedWorkoutData.calories`:
  - `DataConverter.parseRecordedData(...)`
  - mapped from `DevSportInfoBean.reportCal`

### 4. Recovery time mapping after the fix
- Recovery time item in the details list now uses:
  - `RecordedWorkoutData.recoveryTime` first when the workout matches
  - otherwise server `OWorkoutDetailsResponseModel.recoveryTime`
- Source of local `RecordedWorkoutData.recoveryTime`:
  - `DataConverter.parseRecordedData(...)`
  - mapped from `DevSportInfoBean.reportRecoveryTime`

### 5. Heart-rate zone mapping after the fix
- Zone duration display now uses exact locally parsed report values when the workout matches:
  - Zone 1 = `hrWarmUp`
  - Zone 2 = `hrFatBurning`
  - Zone 3 = `hrAerobic`
  - Zone 4 = `hrAnaerobic`
  - Zone 5 = `hrLimitTime`
  - Restorative zone = `durationSeconds - (zone1 + zone2 + zone3 + zone4 + zone5)` when `durationSeconds` is available
- Fallback:
  - if no matched local workout exists, the old `hrArray` sample-based calculation remains in use
- Additional correctness fix:
  - zone index arrays are now cleared before each recomputation so repeated renders do not accumulate stale data

## Current mapping of the main post-workout details window

| UI area | Current source | Notes |
| --- | --- | --- |
| Top title / date / activity name | `OWorkoutDetailsResponseModel` | unchanged |
| Duration | `durationSeconds` or `duration` from server details | unchanged |
| Distance top card | server `distance` or `gpsDistance` | unchanged |
| Calories top card when no distance card | matched local `RecordedWorkoutData.calories`, else server `calories` | fixed |
| Calories list row when distance workout | matched local `RecordedWorkoutData.calories`, else server `calories` | fixed |
| Cadence | server `cadence` | unchanged |
| Max HR | server `hrMax` | unchanged |
| Min HR | server `hrLow` | unchanged |
| Steps | server `steps` | unchanged |
| Recovery time | matched local `RecordedWorkoutData.recoveryTime`, else server `recoveryTime` | fixed |
| HR chart plotting | server `hrArray` | unchanged |
| HR zone highlights | derived from `hrArray` sample indexes | unchanged |
| HR zone duration labels | matched local report zone seconds, else `hrArray` sample durations | fixed |

## BlankTest raw `DevSportInfo` diagnosis

### What was wrong
- The fragment itself was still reading `watchDataStore.testGetRawDevSportJson()`.
- The regression was in storage:
  - the latest raw dev-sport payload was only being written to the capture envelope flow
  - when no active capture session existed, the direct latest-string value remained empty

### Fix applied
- `WatchDataStoreImpl.testSaveRawDevSportJson(...)` now:
  - still writes to the session capture when a session exists
  - also falls back to directly saving the latest raw JSON when no capture session exists

### What this means in practice
- If BlankTest still shows no raw dev-sport text **after this fix**, the likely remaining possibilities are:
  - `onDevSportInfo(...)` was never emitted by the device / firmware / SDK runtime for that workout
  - the watch did not return the report payload for that session
  - only parsing progress `0 / 0` happened and the device-side report never arrived

### Why no further BlankTest code change was made
- The fragment rendering path itself was not the proven cause.
- Changing BlankTest UI logic without proof would increase regression risk and blur app-vs-firmware diagnosis.

## Stress graph issue found during this pass
- Separate from workout mapping, the home stress graph had a concrete merge bug:
  - merged stress breakup data was computed correctly
  - but the DB update wrote the incoming raw breakup string instead of the merged breakup
  - update detection also compared only `sum()` values, which could miss distribution changes with the same total
- Fix:
  - write the merged breakup back to storage
  - compare full merged list equality instead of only sums

## Manual verification checklist
- Complete a new workout from the device.
- Open the main post-workout details window for that workout.
- Verify calories match the device report shown in BlankTest / raw payload.
- Verify recovery time matches the device report.
- Verify HR zone durations match:
  - warm up
  - fat burning
  - aerobic
  - anaerobic
  - limit
- Open BlankTest from the Home Screen bottom-left button after workout completion.
- Verify raw `DevSportInfo` text is visible when the callback is received.
- Sync stress data and verify the home stress graph does not drop earlier valid samples after later sparse updates.

## Explicit non-changes
- No SDK/AAR source was modified.
- No new dependencies were added.
- No new modules or package trees were introduced.
- No existing app architecture was expanded.
