# Post-workout calorie fix in the main app

## Objective
- Document exactly how the post-workout calorie issue was fixed in the main app details window.
- Keep this focused on calories, with only the nearby callback and data-flow details needed to understand the fix.

## Problem
- The post-workout details window was showing calories from the server workout-detail payload.
- After the v2.3.1 SDK upgrade, the device report already exposed the final workout calories through `DevSportInfoBean.reportCal`.
- That meant the main app could disagree with the final device report for the just-finished workout.

## Correct callback and field
- SDK callback:
  - `SportCallBack.onDevSportInfo(DevSportInfoBean data)`
- Correct calorie source:
  - `DevSportInfoBean.reportCal`

## Existing v2.3.1 parsing path that was already correct
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt`
  - `parseRecordedData(it: DevSportInfoBean)` was already mapping:
    - `reportCal -> RecordedWorkoutData.calories`

## Root cause in the main app
- The parsing layer already had the correct value.
- The post-workout screen was still reading the server workout-detail calories directly instead of preferring the freshly parsed local `RecordedWorkoutData`.

## Fix implemented
- `ZhUserActivityHandler.onDevSportInfo(...)` now updates the latest parsed workout cache immediately when the SDK callback arrives.
- `OWorkoutDetailsViewModelV2` now:
  - reads the latest cached `RecordedWorkoutData` from `WatchDataStore`
  - matches it against the currently opened workout using:
    - workout `date`
    - normalized `startTime`
    - normalized `endTime`
  - when the workout clearly matches, it prefers:
    - `RecordedWorkoutData.calories`
  - otherwise it safely falls back to:
    - server `calories`

## Why this is the safe fix
- No SDK/AAR code was changed.
- No server contract was changed.
- The override only happens when the local parsed workout clearly matches the workout currently being shown.
- If the match is not reliable, the old server fallback remains in place.

## Effective calorie mapping after the fix
- Device callback:
  - `onDevSportInfo(...)`
- Device report field:
  - `reportCal`
- Parsed local model:
  - `RecordedWorkoutData.calories`
- Main app display rule:
  - matched local `RecordedWorkoutData.calories`
  - else server `calories`

## Related files
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt`
- `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`
- `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`

## Manual verification
1. Finish a workout on the device.
2. Open the main app post-workout details window for that workout.
3. Confirm the calories shown in the main app match the final device report / BlankTest raw dev-sport output.
4. Re-open the same workout and confirm the value remains stable.
