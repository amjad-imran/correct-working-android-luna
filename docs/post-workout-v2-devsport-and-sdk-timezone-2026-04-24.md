# Post-Workout V2 DevSport Metrics and SDK Timezone Mapping

Date: 2026-04-24

## Scope

- Post-workout V2 metric source cleanup.
- Recorded-workout server upload and post-upload local update source cleanup.
- SDK raw timezone storage and request-header usage.
- No UI redesign, no dependency changes, no new feature architecture.

## SDK references used

From `ZH Android SDK v2.3.2.txt`:

- `SportCallBack.onDevSportInfo(DevSportInfoBean data)` is the recorded workout callback.
- `DevSportInfoBean.recordPointTimeZone` is the workout record timezone field.
- `DevSportInfoBean.reportDistance` is total distance in metres.
- `DevSportInfoBean.reportDuration` is total duration in seconds.
- `DevSportInfoBean.reportTotalStep` is total steps.
- `DevSportInfoBean.reportCal` is calories in kcal.
- `DevSportInfoBean.reportRecoveryTime` is estimated recovery time.
- SDK timezone offsets use a 15-minute scale in the SDK world-clock section; the app stores the raw value and derives minute offset as `raw * 15`.

## Best-fit implementation locations

The implementation stays in the existing owners:

- `ZhUserActivityHandler` already receives `onDevSportInfo(...)`, so SDK raw timezone capture was added there.
- `DataStoredInterface` / `DataStoredImpl` already store timezone state, so SDK raw timezone and offset were stored there.
- `NetworkConnectionInterceptor`, `HeaderInterceptorAudio`, `ChatGptViewModel`, `LifeOSVoiceChatViewModel`, and `LifeOsChatFragment` already add timezone headers, so they were updated in place.
- `OreoSyncDataWork` already updates saved timezone state after sync, so it now saves only the SDK raw timezone state.
- `OWorkoutDetailsViewModelV2` and `OWorkoutDetailsFragmentV2` already prepare and display post-workout V2 metrics, so DevSport-only source selection was implemented there.
- `app/src/main/java/com/noisefit/data/dataConverter/DataConverter.kt` already builds recorded-workout upload payloads and local post-upload activity summaries, so upload/update behavior was aligned there.

## Timezone behavior before this change

The app stored and sent timezone values from Android runtime fallbacks:

- `TimeZone.getDefault().id` for timezone header fallback.
- `Calendar.getInstance().get(Calendar.ZONE_OFFSET)` or `TimeZone.getDefault().rawOffset` for offset fallback.
- `OreoSyncDataWork` saved Android timezone id and offset into `LAST_KNOWN_TIMEZONE` / `LAST_KNOWN_OFFSET`.

That meant cloud headers and saved timezone state could come from phone/system timezone instead of the SDK workout record.

## Timezone behavior after this change

The app now captures timezone from `DevSportInfoBean.recordPointTimeZone` in `ZhUserActivityHandler`.

Saved values:

- `LAST_KNOWN_SDK_TIMEZONE` = raw SDK `recordPointTimeZone` string.
- `LAST_KNOWN_SDK_OFFSET` = `recordPointTimeZone * 15` minutes.
- `LAST_KNOWN_TIMEZONE` and `LAST_KNOWN_OFFSET` are updated only from those SDK values inside `OreoSyncDataWork`.

Header behavior:

- Network and LifeOS/chat headers prefer SDK raw values.
- Legacy stored timezone values are used only if they are already numeric SDK-style values.
- Android timezone id / offset fallback is removed from the touched request-header paths.
- If no SDK raw timezone is available yet, the header value is an empty string instead of a phone timezone fallback.

## Post-workout V2 metric behavior before this change

V2 used mixed sources:

- Distance used server `distance` or server `gpsDistance` based on `dataPriority`.
- Duration used server `durationSeconds`, falling back to server `duration`.
- Steps used server `steps`.
- Calories preferred matched local DevSport parsed data, but fell back to server `calories`.
- Recovery time preferred matched local DevSport parsed data, but fell back to server `recoveryTime`.

This could make the visible post-workout screen disagree with the raw SDK `DevSportInfoBean`.

## Post-workout V2 metric behavior after this change

The requested workout summary metrics now come only from the matched parsed `RecordedWorkoutData`, which is produced by `DataConverter.parseRecordedData(DevSportInfoBean)`.

Mapping:

| Screen/server metric | SDK field | Parsed field |
|---|---|---|
| Distance | `reportDistance` | `RecordedWorkoutData.distance` |
| Duration | `reportDuration` | `RecordedWorkoutData.durationSeconds` |
| Steps | `reportTotalStep` | `RecordedWorkoutData.steps` |
| Calories | `reportCal` | `RecordedWorkoutData.calories` |
| Recovery time | `reportRecoveryTime` | `RecordedWorkoutData.recoveryTime` |

Fallbacks removed:

- No server fallback for distance in V2 top card.
- No server fallback for duration in V2 header.
- No server fallback for steps in detail rows.
- No server fallback for calories in top card/detail rows.
- No server fallback for recovery time in detail rows.

If the latest parsed DevSport record cannot be matched to the opened workout, these requested values stay empty/zero instead of silently using server data.

## Recorded-workout upload/update behavior after this change

The `/activity/v1/add_workout` payload was already built from `RecordedWorkoutData`; this change keeps that path and forces distance-priority uploads to declare ring/device priority when `data_type == distance`.

Upload payload sources:

- `distance` from `RecordedWorkoutData.distance`.
- `duration` from `RecordedWorkoutData.duration`.
- `duration_seconds` from `RecordedWorkoutData.durationSeconds`.
- `steps` from `RecordedWorkoutData.steps`.
- `calories` from `RecordedWorkoutData.calories`.
- `recovery_time` from `RecordedWorkoutData.recoveryTime`.

Post-upload local activity summary now also uses:

- `distance = RecordedWorkoutData.distance`.
- `duration = RecordedWorkoutData.durationSeconds` when available.
- `steps = RecordedWorkoutData.steps`.
- `calories = RecordedWorkoutData.calories`.

## Files changed

| File | Purpose |
|---|---|
| `commons/src/main/java/com/noisefit_commans/data/local/abstraction/DataStoredInterface.kt` | Added SDK raw timezone and SDK offset getters/setters. |
| `app/src/main/java/com/noisefit/data/local/dataStored/implementation/DataStoredImpl.kt` | Persisted SDK raw timezone/offset and cleared them on logout. |
| `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/di/WatchModule.kt` | Injected `DataStoredInterface` into `ZhUserActivityHandler`. |
| `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt` | Captured `recordPointTimeZone` from `onDevSportInfo(...)` and saved raw SDK timezone state. |
| `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt` | Replaced system timezone offset usage in active GPS sport parsing with SDK `recordPointTimeZone` offset. |
| `app/src/main/java/com/noisefit/data/remote/NetworkConnectionInterceptor.kt` | Removed Android timezone fallbacks from normal request headers. |
| `app/src/main/java/com/noisefit/data/remote/HeaderInterceptorAudio.kt` | Removed Android timezone fallback from audio headers. |
| `app/src/main/java/com/oreo/ui/chatGpt/ChatGptViewModel.kt` | Removed Android timezone fallback from chat headers. |
| `app/src/main/java/com/oreo/ui/chatGpt/audio/LifeOSVoiceChatViewModel.kt` | Removed Android timezone fallback from voice chat headers. |
| `app/src/main/java/com/oreo/ui/lifeos/LifeOsChatFragment.kt` | Removed Android timezone fallback from LifeOS manual request headers. |
| `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt` | Saved timezone change state only from SDK raw timezone values. |
| `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModelToday.kt` | Avoided showing timezone changed alert for old non-SDK timezone ids. |
| `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt` | Made V2 distance, steps, calories, and recovery time resolve only from matched DevSport parsed data. |
| `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt` | Made V2 duration display use only matched DevSport parsed duration seconds. |
| `app/src/main/java/com/noisefit/data/dataConverter/DataConverter.kt` | Kept upload metrics sourced from `RecordedWorkoutData`, forced distance priority to ring/device, and aligned post-upload local summary distance/duration. |

## Manual verification checklist

1. Complete a Luna workout and confirm `onDevSportInfo(...)` logs include `recordPointTimeZone`.
2. Confirm network headers use numeric SDK timezone and numeric SDK offset, not `Asia/Kolkata` or any other Android timezone id.
3. Open the immediate post-workout V2 screen and compare distance, duration, steps, calories, and recovery time with raw `DevSportInfoBean`.
4. Confirm those same values are sent in `/activity/v1/add_workout`.
5. Confirm the post-upload activity list summary uses DevSport distance, duration seconds, steps, and calories.
6. Open an older workout that does not match the latest DevSport cache and confirm requested fields do not fall back to server values.

## Risks and assumptions

- `recordPointTimeZone` is treated as the SDK raw source of truth.
- SDK minute offset is derived using the 15-minute scale documented for SDK timezone offsets.
- Only the latest cached `RecordedWorkoutData` can be matched by V2, so older workouts may show zero/blank for the requested strict DevSport-only fields.
- Server may still receive `gps_distance` when location data exists, but `data_priority` is forced to `ring` for distance workouts so workout distance selection uses the DevSport/device distance.

