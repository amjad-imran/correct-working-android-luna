# Revert Guide: Post-Workout V2 DevSport Metrics and SDK Timezone Mapping

Date: 2026-04-24

This document describes how to fully revert the 2026-04-24 SDK timezone and post-workout V2 DevSport-only metric changes back to the previous implementation.

Do not revert unrelated workout or stress graph work.

## Revert summary

To restore the previous behavior:

- Remove SDK raw timezone storage from `DataStoredInterface` and `DataStoredImpl`.
- Remove `DataStoredInterface` injection from `ZhUserActivityHandler`.
- Stop saving timezone from `DevSportInfoBean.recordPointTimeZone`.
- Restore Android timezone fallback headers.
- Restore `OreoSyncDataWork` timezone detection from `TimeZone.getDefault().id` and `Calendar.ZONE_OFFSET`.
- Restore post-workout V2 server fallbacks for distance, duration, steps, calories, and recovery time.
- Restore post-upload local activity summary distance to `0` and duration to minutes-derived seconds.
- Restore distance upload priority from backend workout metadata instead of forcing `ring`.

## 1. Revert `DataStoredInterface`

File:

- `commons/src/main/java/com/noisefit_commans/data/local/abstraction/DataStoredInterface.kt`

Remove these methods:

```kotlin
fun getLastKnownSdkTimezone(): String?
fun setLastKnownSdkTimezone(timeZone: String)
fun getLastKnownSdkOffset(): String?
fun setLastKnownSdkOffset(offset: String)
```

## 2. Revert `DataStoredImpl`

File:

- `app/src/main/java/com/noisefit/data/local/dataStored/implementation/DataStoredImpl.kt`

Remove constants:

```kotlin
private const val LAST_KNOWN_SDK_TIMEZONE = "LAST_KNOWN_SDK_TIMEZONE"
private const val LAST_KNOWN_SDK_OFFSET = "LAST_KNOWN_SDK_OFFSET"
```

Remove these logout clears:

```kotlin
mPrefs.edit()?.remove(LAST_KNOWN_SDK_TIMEZONE)?.apply()
mPrefs.edit()?.remove(LAST_KNOWN_SDK_OFFSET)?.apply()
```

Remove these method implementations:

```kotlin
override fun getLastKnownSdkTimezone(): String? { ... }
override fun setLastKnownSdkTimezone(timeZone: String) { ... }
override fun getLastKnownSdkOffset(): String? { ... }
override fun setLastKnownSdkOffset(offset: String) { ... }
```

## 3. Revert `WatchModule`

File:

- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/di/WatchModule.kt`

Remove import:

```kotlin
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
```

Change `provideZhUserActivityHandler(...)` by removing the `localDataStore` parameter and constructor argument.

Previous constructor call shape:

```kotlin
return ZhUserActivityHandler(
    dataConverter,
    oreoDataConverter,
    context,
    watchDataStore,
    zhApplicationHandler
)
```

## 4. Revert `ZhUserActivityHandler`

File:

- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

Remove import:

```kotlin
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
```

Remove constructor parameter:

```kotlin
private val localDataStore: DataStoredInterface
```

Remove constant:

```kotlin
private const val SDK_TIMEZONE_UNIT_MINUTES = 15
```

Remove both calls:

```kotlin
updateSdkTimezone(data.recordPointTimeZone)
updateSdkTimezone(it.recordPointTimeZone)
```

Remove the helper:

```kotlin
private fun updateSdkTimezone(recordPointTimeZone: Int) { ... }
```

This restores the previous behavior where `onDevSportInfo(...)` updates workout, fitness age, energy consumption, and raw JSON only.

## 5. Revert normal request timezone headers

File:

- `app/src/main/java/com/noisefit/data/remote/NetworkConnectionInterceptor.kt`

Restore imports:

```kotlin
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit
```

Replace:

```kotlin
val timeZone = getSdkTimezoneForHeader()
val offset = getSdkOffsetForHeader()
```

with previous behavior:

```kotlin
val timeZone = localDataStore.getLastKnownTimezone() ?: TimeZone.getDefault().id
val offset = localDataStore.getLastKnownOffset() ?: TimeUnit.MILLISECONDS.toMinutes(
    Calendar.getInstance().get(Calendar.ZONE_OFFSET).toLong()
).toString()
```

Remove:

```kotlin
private fun getSdkTimezoneForHeader(): String { ... }
private fun getSdkOffsetForHeader(): String { ... }
```

## 6. Revert audio/chat/LifeOS timezone headers

Files:

- `app/src/main/java/com/noisefit/data/remote/HeaderInterceptorAudio.kt`
- `app/src/main/java/com/oreo/ui/chatGpt/ChatGptViewModel.kt`
- `app/src/main/java/com/oreo/ui/chatGpt/audio/LifeOSVoiceChatViewModel.kt`
- `app/src/main/java/com/oreo/ui/lifeos/LifeOsChatFragment.kt`

Restore each previous fallback:

- `HeaderInterceptorAudio`: `TimeZone.getDefault().id`.
- `ChatGptViewModel`: `localDataStore.getLastKnownTimezone() ?: TimeZone.getDefault().id` and offset from `Calendar.ZONE_OFFSET`.
- `LifeOSVoiceChatViewModel`: same as `ChatGptViewModel`.
- `LifeOsChatFragment`: `viewModel.localDataStore.getLastKnownTimezone() ?: TimeZone.getDefault().id` and offset from `TimeZone.getDefault().rawOffset`.

Remove the added `getSdkTimezoneForHeader()` and `getSdkOffsetForHeader()` helpers from those files.

## 7. Revert `OreoSyncDataWork` timezone saving

File:

- `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt`

Restore imports:

```kotlin
import java.util.TimeZone
import java.util.concurrent.TimeUnit
```

Replace SDK raw timezone logic with the previous implementation:

```kotlin
val currentTimezone = TimeZone.getDefault().id
val storedTimezone = localDataStore.getLastKnownTimezone()

val currentOffset = TimeUnit.MILLISECONDS.toMinutes(
    Calendar.getInstance().get(Calendar.ZONE_OFFSET).toLong()
).toString()

when {
    storedTimezone == null -> {
        localDataStore.setLastKnownTimezone(currentTimezone)
        localDataStore.setLastKnownOffset(currentOffset)
        localDataStore.isTimezoneChangedAlertCardDismissed(true)
    }

    storedTimezone != currentTimezone -> {
        localDataStore.setLastKnownTimezone(currentTimezone)
        localDataStore.setLastKnownOffset(currentOffset)
        localDataStore.isTimezoneChangedAlertCardDismissed(false)
    }

    else -> {}
}
```

## 8. Revert `SummaryDataViewModelToday`

File:

- `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModelToday.kt`

Change:

```kotlin
return if (storedTimezone == null || storedTimezone.toIntOrNull() == null) {
```

back to:

```kotlin
return if (storedTimezone == null) {
```

## 9. Revert `OreoDataConverter` GPS sport timezone handling

File:

- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt`

Remove:

```kotlin
private fun getSdkTimezoneOffsetMillis(recordPointTimeZone: Int): Long { ... }
```

Restore the previous active GPS sport time handling:

```kotlin
val startCalendar = Calendar.getInstance()
val calendar = Calendar.getInstance(DateFormats.defaultLocale)
val offset =
    -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000)
startCalendar.timeInMillis = (p1.reportSportStartTime + offset)
val endCalendar = Calendar.getInstance()
endCalendar.timeInMillis = (p1.reportSportEndTime)
```

## 10. Revert post-workout V2 metric source changes

File:

- `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt`

Restore `prepareDataForActivity(...)` to:

- Use `resolveCalories(it, recordedWorkout)`.
- Use `resolveRecoveryTime(it, recordedWorkout)`.
- Use server `it.steps` for the steps row.
- Call `showDistance(it)` without passing `recordedWorkout`.

Restore `showDistance(...)` to server distance/data-priority logic:

```kotlin
private fun showDistance(data: OWorkoutDetailsResponseModel): Boolean {
    return if (data.dataType.equals("distance", true)) {
        if (data.dataPriority.equals("app", true)) {
            (data.gpsDistance ?: 0) > 0
        } else {
            data.distance != null && data.distance > 0L
        }
    } else {
        false
    }
}
```

Restore `resolveCalories(...)` to local-first/server-fallback:

```kotlin
private fun resolveCalories(
    data: OWorkoutDetailsResponseModel,
    recordedWorkout: RecordedWorkoutData? = getMatchedRecordedWorkout(data)
): Int {
    val recordedCalories = recordedWorkout?.calories ?: 0
    return if (recordedCalories > 0) recordedCalories else data.calories ?: 0
}
```

Restore `resolveRecoveryTime(...)` to local-first/server-fallback:

```kotlin
private fun resolveRecoveryTime(
    data: OWorkoutDetailsResponseModel,
    recordedWorkout: RecordedWorkoutData? = getMatchedRecordedWorkout(data)
): Long {
    val recordedRecoveryTime = recordedWorkout?.recoveryTime ?: 0L
    return if (recordedRecoveryTime > 0) recordedRecoveryTime else data.recoveryTime ?: 0L
}
```

Remove:

```kotlin
private fun resolveSteps(...)
fun getDurationSeconds(...)
```

Restore `getDistance(...)` to use server distance/data-priority and fallback calories:

```kotlin
val distanceToUse =
    if (data.dataPriority.equals("app")) data.gpsDistance ?: 0 else data.distance
...
val calories = resolveCalories(data, getMatchedRecordedWorkout(data))
```

## 11. Revert V2 duration display

File:

- `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsFragmentV2.kt`

Replace the strict `viewModel.getDurationSeconds(it)` display with previous server fallback behavior:

```kotlin
if (it.durationSeconds == null || it.durationSeconds == 0L) {
    binding.lytTop.lytActivityItem.tvDurationValue.text =
        ApplicationUtils.getActivityDurationFormat2(it.duration)

    binding.lytTop.lytActivityItem.tvDurationUnit.text = "00"
} else {
    val (hour, minute, seconds) = ApplicationUtils.getFormattedDuration(it.durationSeconds)
    binding.lytTop.lytActivityItem.tvDurationValue.text =
        String.format(locale = Locale.US, "%02d:%02d", hour, minute)
    binding.lytTop.lytActivityItem.tvDurationUnit.text =
        String.format(locale = Locale.US, ":%02d", seconds)
}
```

## 12. Revert upload/update source changes

File:

- `app/src/main/java/com/noisefit/data/dataConverter/DataConverter.kt`

Remove:

```kotlin
val workoutDataPriority = if (dataType.equals("distance", true)) "ring" else dataPriority
```

Change:

```kotlin
this.addProperty("data_priority", workoutDataPriority)
```

back to:

```kotlin
this.addProperty("data_priority", dataPriority)
```

In `getSportModeResponseArray(...)`, change:

```kotlin
distance = it.distance ?: 0L,
duration = it.durationSeconds ?: ((it.duration ?: 0).toLong() * 60),
```

back to:

```kotlin
distance = 0,
duration = (it.duration ?: 0).toLong() * 60,
```

## 13. Documentation cleanup for full revert

Delete these docs if a full rollback is required:

- `docs/post-workout-v2-devsport-and-sdk-timezone-2026-04-24.md`
- `docs/revert-post-workout-v2-devsport-and-sdk-timezone-2026-04-24.md`

Optionally add a rollback note to:

- `changes.md`
- `changes_detailed.md`

## Verification after revert

1. Build the staging APK.
2. Confirm request headers again send Android timezone id fallbacks when no stored timezone exists.
3. Confirm timezone changed alert works for Android timezone id changes.
4. Confirm post-workout V2 again falls back to server values when the latest local DevSport record is not matched.
5. Confirm distance display again respects server `dataPriority == app` and `gpsDistance`.
6. Confirm post-upload local activity summary distance returns to `0`.

