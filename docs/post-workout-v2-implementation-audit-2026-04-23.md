# Post-Workout V2 Implementation Audit

Date: 2026-04-23

Scope:
- Full post-workout V2 screen implementation
- Every major visible section
- Exact source used for each section
- Local-vs-server precedence behavior

Constraints followed:
- No app code changed

## 1. Entry points and data fetch

Main user entry from home recent workouts:
- `SummaryDataFragmentToday` routes `USERWORKOUT` items to V2 in `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataFragmentToday.kt:646-656`
- `OActivityListModal.getDisplayVersionType()` returns V2 only for `USERWORKOUT` in `app/src/main/java/com/oreo/data/model/OWorkoutListModal.kt:35-45`

Main fragment:
- `OWorkoutDetailsFragmentV2` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:75-419`

API fetch:
- `OWorkoutDetailsViewModelV2.getWorkoutDetails(workoutId)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:67-108`
- repository hits `/activity/v2/workout_detail/{id}` in `app/src/main/java/com/oreo/data/repository/implementation/OreoUserActivityRepositoryImpl.kt:1787-1791`

Server response model:
- `OWorkoutDetailsResponseModel` in `app/src/main/java/com/oreo/data/model/OWorkoutDetailsResponseModel.kt:10-59`

## 2. Local exact-workout cache used by V2

SDK workout callback:
- `ZhUserActivityHandler.onDevSportInfo(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt:147-156`

Local workout cache update:
- `watchDataStore.updateWorkout(dataConverter.parseRecordedData(data))` in `.../ZhUserActivityHandler.kt:151-154`
- persistence in `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt:339-350`

Local exact-workout fields come from:
- `DataConverter.parseRecordedData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt:1108-1178`

Important note:
- Luna stores only one latest `RecordedWorkoutData` object in this preference key.
- V2 does not have a local table of all workout summaries here; it has one latest cache entry.

## 3. Matching algorithm used by V2

Local-vs-server match logic:
- `getMatchedRecordedWorkout(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:251-270`

How the match works:
- load the latest local `RecordedWorkoutData`
- compare date
- normalize server `startTime` and `endTime`
- normalize local `startTime` and `endTime`
- consider it matched if either normalized start times match or normalized end times match

Important limitations:
- only the latest cached workout can match
- older workouts usually cannot match
- if times are formatted differently or slightly shifted, the exact local workout can fail to match
- when match fails, V2 falls back to server values

## 4. Screen sections and exact data source

## 4.1 Header and top metadata

Updated in:
- `OWorkoutDetailsFragmentV2.updateUi(...)` at `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:190-419`

Fields used:
- date/title: `it.date` at `216-239`
- activity name: `it.getTranslatedActivityName()` at `240`
- workout time: `it.startTime`, `it.endTime` at `206-208`
- city: `it.location?.startLocation` at `209-214`

Source:
- server `OWorkoutDetailsResponseModel`

## 4.2 Duration

Displayed in:
- `OWorkoutDetailsFragmentV2.updateUi(...)` at `242-253`

Fields used:
- `durationSeconds` first
- fallback to `duration`

Source:
- server `OWorkoutDetailsResponseModel`

## 4.3 Top metric card

Resolver:
- `OWorkoutDetailsViewModelV2.getDistance(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:339-366`

Behavior:
- if `dataType == distance` and the chosen distance source is positive:
  - show distance
- otherwise:
  - show calories

Distance precedence:
- `showDistance(...)` in `.../OWorkoutDetailsViewModelV2.kt:239-249`
- if `dataPriority == app`, use `gpsDistance`
- else use server `distance`

Calories precedence:
- `resolveCalories(...)` in `.../OWorkoutDetailsViewModelV2.kt:302-312`
- local matched `RecordedWorkoutData.calories` first
- fallback to server `data.calories`

Verdict:
- Top-card calories in V2 are not purely server-driven.
- Top-card distance in V2 is purely server response driven, using `dataPriority` and `dataType`.

## 4.4 Edit distance icon

Conditions:
- after top metric is calculated, the edit icon is shown only when distance is being shown and the workout is today
- code at `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:257-265`
- `isTodayWorkout()` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:326-337`

Source:
- server date plus view-model distance decision

## 4.5 Detail recycler rows

Built by:
- `OWorkoutDetailsViewModelV2.prepareDataForActivity(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:161-236`

Rows and sources:

1. Calories burned
- added only when distance is the primary top metric and calories are positive
- value uses `resolveCalories(...)`
- source: local matched workout calories first, else server calories

2. Cadence
- source: server `it.cadence`

3. Max HR
- source: server `it.hrMax`

4. Min HR
- source: server `it.hrLow`

5. Steps
- source: server `it.steps`
- no local fallback

6. Recovery time
- source: local matched `RecordedWorkoutData.recoveryTime` first, else server `it.recoveryTime`

Important conclusion:
- Post-workout V2 already treats calories and recovery time as "local exact summary preferred".
- It does not do the same for steps.

## 4.6 Nudges

Set in:
- `setNudgesViewPager(it.nudges)` at `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:269`

Source:
- server `OWorkoutDetailsResponseModel.nudges`

## 4.7 Workout image

Set in:
- `ivWorkoutImage.loadImage(..., it.iconUrl)` at `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:271-273`

Source:
- server `iconUrl`

## 4.8 Heart-rate section

Guard:
- only executed for `USERWORKOUT`
- returns early if `hrArray` is empty
- `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:298-340`

Average and max labels:
- `hrAvg` and `hrMax` at `301-313`

Chart input:
- `hrArray`
- invalid `255` values are converted to `0` at `347-358`

Chart rendering:
- `HeartRateChartView.updateDataWithMax(...)` at `363-390`

Source:
- server `hrArray`, `hrAvg`, `hrMax`

Important conclusion:
- The HR chart does not use local `RecordedWorkoutData.hrData`.
- It uses the server workout-detail `hrArray`.

## 4.9 Heart-rate zones

Zone generation:
- `hrZoneAdapter.setDataSet(viewModel.generateHrZones(it.hrArray))` at `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:315-317`

Zone list UI:
- adapter in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutHRZoneAdapter.kt:14-103`

Zone calculation logic:
- `generateHrZones(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:482-669`

How V2 combines sources:
- selected zone indexes come from server `hrArray`
- zone durations prefer matched local `RecordedWorkoutData.hrWarmUp/hrFatBurning/hrAerobic/hrAnaerobic/hrLimitTime`
- if those are missing or zero, V2 falls back to durations inferred from `hrArray`

Important conclusion:
- The zone section is a mixed-source feature.
- Highlight positions come from the server heart-rate array.
- Displayed durations may come from the local exact workout summary.

## 4.10 Map and weather

Visibility logic:
- if there are fewer than 3 locations, overlays are hidden at `394-401`
- if weather exists, temperature and weather icon are shown at `403-417`

Source:
- server `location` and `weather`

## 4.11 Delete button

Shown only when:
- workout date is today
- type is not `auto`
- type is not `apple`
- type is not `google`
- `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt:193-202`

Delete action:
- `deleteWorkout(id)` through server API at `OWorkoutDetailsViewModelV2.kt:110-158`

Source:
- server workout type and date

## 5. Exact local fields V2 can use

The matched local `RecordedWorkoutData` contains:
- calories
- steps
- distance
- duration/durationSeconds
- recoveryTime
- cadence
- heart-rate zone durations
- many advanced training metrics

Defined in:
- `commons/src/main/java/com/noisefit_commans/data/model/OreoDbTable.kt:71-120`

But current V2 only uses the matched local cache for:
- calories
- recovery time
- HR zone durations

Current V2 does not use matched local cache for:
- steps
- distance
- cadence
- avg/max/min heart rate
- heart-rate series

## 6. Legacy V1 comparison

Legacy V1 detail rows:
- `OWorkoutDetailsFragment.prepareDataForActivity(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragment.kt:420-446`

Legacy V1 behavior:
- calories are server-only
- steps are server-only

Why this matters:
- If a workout opens in V1, none of the newer local-override behavior is applied.

## 7. Post-workout V2 verdict

Correctly implemented in V2:
- fetches dedicated V2 detail endpoint
- shows server workout metadata correctly
- uses local exact workout calories when the match succeeds
- uses local exact recovery time when the match succeeds
- uses local exact HR-zone durations when the match succeeds

Implementation limitations in V2:
- only the latest local workout can be matched
- step display is server-only
- heart-rate chart is server-only
- home workout-list calories and V2 detail calories can legitimately disagree because they use different source precedence

Most important audit conclusions:
- V2 is not a pure server screen and not a pure SDK screen; it is a mixed-source screen.
- The mixed-source logic is deliberate for calories, recovery time, and HR zones.
- Steps are the main inconsistency: the exact local SDK value exists but is not used by V2.
