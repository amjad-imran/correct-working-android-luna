# Revert All Workout and DevSport Info Related Changes 15th April

## Purpose
- This document explains how to fully revert the April 15, 2026 workout/dev-sport/stress changes and return to the previous behavior.

## Files changed in this change set
- Modified:
  - `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`
  - `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`
  - `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`
  - `app/src/main/java/com/oreo/data/db/implementation/OreoStressDataImpl.kt`
  - `changes.md`
  - `changes_detailed.md`
- Created:
  - `app/src/test/java/com/oreo/data/db/implementation/OreoStressDataImplTest.kt`
  - `docs/workout-devsport-post-workout-mapping-2026-04-15.md`
  - `docs/stress-graph-root-cause-2026-04-15.md`
  - `revert all workout and devsport info related changes 15th April.md`

## Revert order
1. Revert `OWorkoutDetailsViewModelV2.kt`
2. Revert `ZhUserActivityHandler.kt`
3. Revert `WatchDataStoreImpl.kt`
4. Revert `OreoStressDataImpl.kt`
5. Remove the new stress unit test and the new docs if a full rollback is required
6. Remove the April 15 entries from `changes.md` and `changes_detailed.md`

## 1. Revert the main app post-workout screen changes

### File
- `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`

### Remove these changes
- Remove `WatchDataStore` and `RecordedWorkoutData` imports.
- Remove `watchDataStore` from the injected constructor.
- Delete these helper methods:
  - `getMatchedRecordedWorkout(...)`
  - `normalizeWorkoutTime(...)`
  - `resolveCalories(...)`
  - `resolveRecoveryTime(...)`
  - `resolveZoneDuration(...)`
  - `resolveRestorativeDuration(...)`
  - `calculateZonePercentage(...)`

### Restore previous logic
- In `prepareDataForActivity(...)`:
  - use `it.calories` directly again
  - use `it.recoveryTime` directly again
- In `getDistance(...)`:
  - use `data.calories` directly again for calorie-priority workouts
- In `generateHrZones(...)`:
  - remove the matched local workout lookup
  - remove exact zone-duration fallback from `RecordedWorkoutData`
  - restore durations to:
    - `zoneIndexes.size * 30`
  - restore percentages to be based on sample counts only

### Optional partial revert
- If you want to revert only the dev-sport exact mapping but keep the safe bug fix:
  - keep the `.clear()` calls on zone index lists
  - remove only the local report-duration fallback

## 2. Revert the immediate `onDevSportInfo(...)` local workout update

### File
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

### Remove these statements from `onDevSportInfo(data: DevSportInfoBean)`
- `watchDataStore.updateFitnessAge(data.fitnessAge)`
- `watchDataStore.updateEnergyConsumption(data.reportEnergyConsumption)`
- `watchDataStore.updateWorkout(dataConverter.parseRecordedData(data))`

### Result after revert
- The app will again wait for the later parsing-progress completion path before updating the latest local workout cache.

## 3. Revert the BlankTest raw dev-sport persistence fallback

### File
- `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`

### Remove these changes
- Change `testSaveRawDevSportJson(...)` back from:
  - `saveSdkRawCaptureOrLatest(TEST_RAW_DEV_SPORT, data)`
- To the old behavior:
  - `saveSdkRawCapture(TEST_RAW_DEV_SPORT, data)`

### Remove helper fallback logic
- Delete `saveSdkRawCaptureOrLatest(...)`
- If `saveSdkRawCapture(...)` was changed to return `Boolean`, revert it to the earlier signature if desired

### Result after revert
- BlankTest raw dev-sport will again depend completely on an active SDK raw capture session being present.

## 4. Revert the stress graph persistence fix

### File
- `app/src/main/java/com/oreo/data/db/implementation/OreoStressDataImpl.kt`

### Restore previous logic
- Replace:
  - `if (mergedData != prevBreakup)`
- With:
  - `if (mergedData.sum() != prevBreakup.sum())`
- Replace:
  - `stressDao.updateViaDate(Gson().toJson(mergedData), data.date!!, false)`
- With:
  - `stressDao.updateViaDate(data.breakUp ?: "", data.date!!, false)`

### Result after revert
- The old merge bug returns:
  - sparse later payloads can overwrite earlier valid samples
  - same-sum distribution changes can be skipped

## 5. Remove created files if doing a full rollback
- Delete:
  - `app/src/test/java/com/oreo/data/db/implementation/OreoStressDataImplTest.kt`
  - `docs/workout-devsport-post-workout-mapping-2026-04-15.md`
  - `docs/stress-graph-root-cause-2026-04-15.md`
  - `revert all workout and devsport info related changes 15th April.md`

## 6. Revert documentation changelog updates
- Remove the April 15, 2026 sections added for this work from:
  - `changes.md`
  - `changes_detailed.md`

## Expected product behavior after full rollback
- Post-workout calories in the main details screen will again come only from server workout detail fields.
- Recovery time in that screen will again come only from server workout detail fields.
- HR zone durations will again be derived only from `hrArray` sample counting.
- BlankTest raw dev-sport preview will again depend on capture-session state.
- The stress graph merge overwrite bug will return.

## Recommended rollback verification
- Open a recent workout details screen and verify the old server-only values are back.
- Complete a new workout and confirm BlankTest raw dev-sport only appears when an active capture session exists.
- Sync stress data and confirm the old behavior matches the pre-fix build.
