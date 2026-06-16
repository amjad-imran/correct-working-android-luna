# Luna Steps and Calories Mapping Audit

Date: 2026-04-23

Scope:
- Daily steps and calories
- Live workout steps and calories
- Home-page activity/workout surfaces
- Post-workout V2 and legacy post-workout screens
- SDK v2.3.2 mapping expectations for these fields

Constraints followed:
- No app code changed
- Findings below are based on repository code plus `ZH Android SDK v2.3.2.txt`

## 1. SDK source of truth

### 1.1 Daily activity data from SDK

Relevant SDK callback:
- `FitnessDataCallBack.onDailyData(DailyBean bean)` in `ZH Android SDK v2.3.2.txt:1924`

Relevant SDK fields:
- `DailyBean.stepsData` in `ZH Android SDK v2.3.2.txt:2212-2214`
- `DailyBean.distanceData` in `ZH Android SDK v2.3.2.txt:2220-2222`
- `DailyBean.calorieData` in `ZH Android SDK v2.3.2.txt:2228-2231`
- `DailyBean.todayCalorieData` in `ZH Android SDK v2.3.2.txt:2237-2240`
- `DailyBean.todayOuraCalorieData` in `ZH Android SDK v2.3.2.txt:2242-2245`
- `DailyBean.todayOuraCalorieHourlyData` in `ZH Android SDK v2.3.2.txt:2247-2250`
- `DailyBean.todaySportCalorieData` in `ZH Android SDK v2.3.2.txt:2252-2254`
- `DailyBean.todaySportCalorieHourlyData` in `ZH Android SDK v2.3.2.txt:2255-2259`

Meaning from the SDK doc:
- `stepsData`: daily hourly step buckets
- `calorieData` / `todayCalorieData`: Apple-style total calorie path
- `todaySportCalorieData` / hourly: exercise or active calories
- `todayOuraCalorieData` / hourly: walking calorie path

Important conclusion:
- The SDK does not expose one single "calories" number for the day.
- It exposes multiple calorie concepts.
- Any audit must distinguish `total calories` from `active/exercise calories`.

### 1.2 Workout data from SDK

Relevant SDK callback:
- `SportCallBack.onDevSportInfo(DevSportInfoBean data)` in `ZH Android SDK v2.3.2.txt:3111-3114`

Relevant SDK fields:
- `reportDuration` in `ZH Android SDK v2.3.2.txt:3476-3480`
- `reportDistance` in `ZH Android SDK v2.3.2.txt:3481-3485`
- `reportCal` in `ZH Android SDK v2.3.2.txt:3486-3490`
- `reportTotalStep` in `ZH Android SDK v2.3.2.txt:3516-3520`
- per-point `RecordPointSportData.cal` and `RecordPointSportData.step` in `ZH Android SDK v2.3.2.txt:3847-3867`

Meaning from the SDK doc:
- `reportCal` is the workout total calories
- `reportTotalStep` is the workout total steps
- `RecordPointSportData.cal` and `RecordPointSportData.step` are point-level cumulative workout values, not the final summary surface value used by Luna post-workout V2

### 1.3 Live workout realtime data from SDK adapter

Ring realtime callbacks are translated in app code:
- `ZhUpdateDeviceUnitsHandler.onRingSportData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt:1031-1046`
- `ZhUpdateDeviceUnitsHandler.onSecondaryScreenWearData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt:1083-1097`

Realtime app model:
- `WorkoutRealTimeData(hrValue, calorieValue, steps, distance)` in `commons/src/main/java/com/noisefit_commans/models/ColorfitData.kt:587-592`

Important conclusion:
- Live workout callbacks contain both calories and steps.
- Whether the user sees them depends on UI binding, not on callback availability.

## 2. Daily steps and calories pipeline

### 2.1 SDK callback to Luna daily model

Luna daily path:
- `ZhUserActivityHandler.onDailyData(...)` routes Luna and Luna Band to `OreoDataConverter.parseStepsData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt:532-544`

Daily normalization:
- `OreoDataConverter.parseStepsData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt:768-799`
- hourly calories are resolved by `LunaSdk231DataNormalizer.resolveHourlyCalories(...)` in `.../OreoDataConverter.kt:1433-1439`
- total calories are resolved by `LunaSdk231DataNormalizer.resolveTotalCalories(...)` in `.../OreoDataConverter.kt:1441-1452`

What Luna stores for each day:
- hourly `steps` from `DailyBean.stepsData`
- hourly `distance` from `DailyBean.distanceData`
- hourly `calories` from:
  - `calorieData[index]` first
  - fallback to `todayOuraCalorieHourlyData[index]`
- hourly `activeCalories` from `todaySportCalorieHourlyData[index]`
- daily `totalSteps` as sum of `stepsData`
- daily `totalCalories` from:
  - `todayCalorieData` first
  - fallback to sum of `calorieData`
  - fallback to `todayOuraCalorieData`
- daily `activeCalories` from `todaySportCalorieData`

Conclusion:
- The daily Luna mapping is internally consistent with the SDK field descriptions.
- The code does not appear to swap total calories and active calories.

### 2.2 Local daily data to server payload

Daily save:
- `OreoSyncDataWork` saves daily steps data in `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt:364-376`

Daily upload mapping:
- `OreoOnlineDataMapper.parseStepsDataOreo(...)` in `app/src/main/java/com/oreo/data/dataConverter/OreoOnlineDataMapper.kt:92-149`
- day-breakup mapping in `.../OreoOnlineDataMapper.kt:131-140`

Uploaded daily fields:
- `total_steps`
- `active_calories`
- `total_calories`
- `total_distance`

Uploaded hourly fields:
- `steps`
- `active_calories`
- `calories`
- `distance`

Conclusion:
- The sync layer preserves the distinction between total calories and active calories.
- This is not a "same field reused for both" implementation.

## 3. User-visible step displays

## 3.1 During workout: Record Workout V2

Source wiring:
- realtime callback observed in `RecordWorkoutFragmentV2` at `app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt:622-633`
- realtime model includes `steps` and `calorieValue` in `commons/src/main/java/com/noisefit_commans/models/ColorfitData.kt:587-592`
- realtime calories text update in `.../RecordWorkoutFragmentV2.kt:412-418`

What the screen actually shows:
- the layout contains calorie views, but they are `android:visibility="invisible"` in `app/src/main/res/layout/layout_ongoing_workout.xml:68-104`
- there is no bound steps TextView in `layout_ongoing_workout.xml`

Verdict for live workout steps:
- Realtime steps are received from the device callback.
- Current V2 live workout UI does not display steps at all.
- So there is no visible live-step mapping on this screen to be wrong; the data simply is not rendered.

## 3.2 Post-workout V2

Detail row creation:
- `OWorkoutDetailsViewModelV2.prepareDataForActivity(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:161-236`

Step row source:
- `it.steps` from server `OWorkoutDetailsResponseModel.steps` in `.../OWorkoutDetailsViewModelV2.kt:212-220`
- `OWorkoutDetailsResponseModel.steps` is defined in `app/src/main/java/com/oreo/data/model/OWorkoutDetailsResponseModel.kt:16-18`

Important limitation:
- Post-workout V2 does not use local `RecordedWorkoutData.steps` as a fallback or override.
- `RecordedWorkoutData.steps` exists and is filled from SDK `reportTotalStep` in:
  - `DataConverter.parseRecordedData(...)` at `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt:1141-1154`
  - model field in `commons/src/main/java/com/noisefit_commans/data/model/OreoDbTable.kt:71-85`
- but that local step value is not consumed by `OWorkoutDetailsViewModelV2`

Verdict for post-workout V2 steps:
- Field mapping itself is not wrong: the screen uses the server workout detail `steps` field.
- However, this screen is inconsistent with calories because it does not fall back to the exact local SDK workout summary.
- If server `steps` is wrong while device `reportTotalStep` is correct, V2 can still display the wrong step total.
- That is an app-side source-precedence gap, not proof of a wrong SDK field mapping.

## 3.3 Legacy post-workout V1

Legacy detail row creation:
- `OWorkoutDetailsFragment.prepareDataForActivity(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragment.kt:420-446`

Step row source:
- server `it.steps`

Verdict:
- V1 is fully server-driven for steps.

## 3.4 Home summary activity card

Home activity model creation:
- `SummaryDataViewModelToday` builds `ODashboardActivityModel(... steps = healthData.activity?.steps ?: 0)` in `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModelToday.kt:733-742`
- the same pattern also exists in `SummaryDataViewModel` in `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModel.kt:170-183`

Home activity card rendering:
- `ActivityViewHolder2` shows steps from `data.data.steps` in `app/src/main/java/com/oreo/ui/home/summary/OSummaryHealthOverviewAdapter.kt:3614-3690`
- specific steps binding is in `.../OSummaryHealthOverviewAdapter.kt:3689`
- the paired layout labels this field as `text_steps` in `app/src/main/res/layout/list_activity_burn_card_item_2.xml:155-185`

Verdict:
- Home summary steps come from the server summary response `healthData.activity.steps`.
- They are not read directly from the latest SDK callback on the home screen.
- Any mismatch here can be caused by server freshness, sync timing, or upstream aggregation, not just by local field mapping.

## 3.5 Daily activity detail screen

Daily activity detail labels:
- `total calories` and `steps` labels are set in `app/src/main/java/com/oreo/ui/activity/OreoActivityFragment.kt:293-296`

Daily activity detail values:
- steps are displayed from `it.steps` in `.../OreoActivityFragment.kt:362-369`

Verdict:
- This is also server-summary driven, not direct device-callback driven.

## 3.6 Home recent workout list

Home workout-history card:
- built by `getWorkoutHistoryCard(activity)` in `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModelToday.kt:2469-2477`
- rendered through `OSummaryHealthOverviewAdapter` in `app/src/main/java/com/oreo/ui/home/summary/OSummaryHealthOverviewAdapter.kt:2038-2064`
- item model is `OActivityListModal` in `app/src/main/java/com/oreo/data/model/OWorkoutListModal.kt:9-46`

Important finding:
- `OreoRWorkoutAdapter` shows calories only; it does not show steps in `app/src/main/java/com/oreo/ui/home/summary/OreoRWorkoutAdapter.kt:27-88`

Verdict:
- Home recent workout list does not display steps.

## 4. User-visible calorie displays

## 4.1 During workout: Record Workout V2

Realtime calorie source:
- `WorkoutRealTimeData.calorieValue` is filled from device callbacks in `ZhUpdateDeviceUnitsHandler.kt:1037-1044` and `1088-1095`
- `RecordWorkoutFragmentV2.updateWorkoutData(...)` writes that into `tvCalories` in `app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt:412-418`

What the screen actually shows:
- calorie views are invisible in `app/src/main/res/layout/layout_ongoing_workout.xml:68-104`

Verdict:
- Realtime calories are wired correctly from the device callback to the fragment.
- They are not currently visible because the layout hides them.

## 4.2 Post-workout V2

Calories fallback logic:
- `resolveCalories(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:302-312`
- matching logic in `getMatchedRecordedWorkout(...)` at `.../OWorkoutDetailsViewModelV2.kt:251-270`
- local workout cache stored by `watchDataStore.updateWorkout(...)` in `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt:339-350`
- local workout populated from SDK `reportCal` in `DataConverter.parseRecordedData(...)` at `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt:1141-1154`

Where V2 uses calories:
- top metric card uses `getDistance(...)` and falls back to calories when `dataType != distance`, at `.../OWorkoutDetailsViewModelV2.kt:339-366`
- details recycler adds `Calories burned` using resolved calories at `.../OWorkoutDetailsViewModelV2.kt:167-177`

Verdict for post-workout V2 calories:
- The field mapping is correct.
- For the matched latest local workout, V2 prefers the exact SDK-derived `reportCal`.
- If the local workout cannot be matched, V2 falls back to the server detail `data.calories`.
- So a remaining mismatch on some workouts can still happen when the screen fails to match the local exact workout and falls back to server data.

## 4.3 Home recent workout list

Home workout list calorie source:
- `OreoRWorkoutAdapter` renders `activity.calories` from `OActivityListModal.calories` in `app/src/main/java/com/oreo/ui/home/summary/OreoRWorkoutAdapter.kt:46-58`

Verdict:
- Home recent workout list calories are server-list driven only.
- They do not use the local `reportCal` override logic used by post-workout V2.
- This means the same workout can show one calorie number on the home list and another on the V2 detail page.

## 4.4 Legacy post-workout V1

Legacy calorie source:
- `OWorkoutDetailsFragment.prepareDataForActivity(...)` uses server `it.calories` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragment.kt:428-435`

Verdict:
- Legacy V1 is fully server-driven for calories.

## 4.5 Home summary activity card

Home card source:
- `SummaryDataViewModelToday` sets `activeCalories = healthData.activity?.activeCalories ?: 0` in `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModelToday.kt:733-742` and again in `2859-2872`

Home card rendering:
- `ActivityViewHolder` renders `data.data.activeCalories` in `app/src/main/java/com/oreo/ui/home/summary/OSummaryHealthOverviewAdapter.kt:3744-3796`
- `ActivityViewHolder2` renders `data.data.activeCalories` in `.../OSummaryHealthOverviewAdapter.kt:3614-3690`
- labels explicitly use `text_active_calories` or goal-progress wording in:
  - `app/src/main/res/layout/list_activity_burn_card_item_2.xml:130-166`
  - `app/src/main/res/layout/list_activity_minimal_item.xml:150-176`
  - `app/src/main/res/layout/list_activity_burn_card_item.xml:145-176`

Verdict:
- The home activity card is intentionally showing active calories, not total daily calories.
- If firmware team compares home card calories against SDK `todayCalorieData`, that comparison is not apples-to-apples.
- This is the single biggest place where a definition mismatch can look like a mapping bug even when the mapping is correct.

## 4.6 Daily activity detail screen

Daily activity detail calories:
- Goal progress uses `activeCalories` in `app/src/main/java/com/oreo/ui/activity/OreoActivityFragment.kt:325-338`
- `Total calories` uses `totalCalories` in `.../OreoActivityFragment.kt:345-356`

Verdict:
- The daily activity detail screen correctly separates active calories from total calories.

## 5. Final verdict

### 5.1 Steps verdict

Correct mappings:
- SDK `reportTotalStep` is correctly mapped into local `RecordedWorkoutData.steps`
- SDK daily `stepsData` is correctly mapped into hourly and daily totals for Luna
- Home daily steps surfaces use server `activity.steps`, which is the correct server-side daily-steps field

App-side gaps:
- Post-workout V2 does not use local exact `RecordedWorkoutData.steps` even though that value exists
- Live workout V2 receives steps but does not display them

Most likely interpretation:
- There is no evidence of a direct step field swap or wrong SDK-to-local step mapping.
- There is an app inconsistency in which steps and calories use different source-precedence rules on post-workout V2.

### 5.2 Calories verdict

Correct mappings:
- Daily total calories and active calories are mapped separately and correctly in Luna daily sync
- Post-workout V2 calories use the correct exact SDK summary (`reportCal`) when the local workout is matched
- Daily activity detail screen correctly separates active and total calories

App-side gaps:
- Home recent workout list remains server-only for workout calories
- Legacy post-workout V1 remains server-only for workout calories
- Live workout calories are wired but hidden by UI

Most likely interpretation:
- If the complaint is about the home summary activity card versus firmware total calories, the main problem is a definition mismatch: the card shows active calories, not total calories.
- If the complaint is about post-workout calories, V2 is mapped correctly when the workout match succeeds; remaining mismatches are more likely to be unmatched-fallback or upstream/server issues than a direct field-mapping bug.

## 6. Practical diagnostic checklist

When a tester reports a step or calorie mismatch, first identify the surface:
- Home activity card: compare against `activeCalories`, not `todayCalorieData`
- Daily activity detail screen: compare `goal progress` against active calories and `total calories` against total calories
- Home recent workout list: value is server-list only
- Post-workout V2: calories can be local-override, steps are server-only
- Live workout V2: calories callback exists but hidden, steps not rendered

Then identify the expected SDK field:
- Daily steps: `DailyBean.stepsData`
- Daily total calories: `todayCalorieData` or `calorieData`
- Daily active calories: `todaySportCalorieData`
- Workout steps: `DevSportInfoBean.reportTotalStep`
- Workout calories: `DevSportInfoBean.reportCal`
