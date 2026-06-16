# HRV/Stress Background Spot-Measurement Loop Diagnosis

Date: April 16, 2026

Scope:
- Diagnose whether the app's autonomous background HRV/stress measurement has the same issues as the SpO2 loop.
- Determine whether the behavior came from the old baseline or from the v2.3.1 integration work.
- Produce a no-code diagnosis only.

Constraints respected:
- No production code changed in this diagnosis.
- No `ZH-SDK` reference project code changed.
- No vendor AAR code changed.

## Executive conclusion

The autonomous background HRV/stress measurement probe in `RingConnectionService.maybeRequestManualFallbackMeasurements()` has the same structural issues as the SpO2 probe:

1. It was NOT in the old baseline.
2. It was introduced during the v2.3.1 SDK integration work.
3. It is NOT required for stress alert threshold configuration (dedicated SDK API handles that).
4. It is NOT required for stress samples (continuous callbacks already provide them).
5. It has the same lifecycle/reset vulnerability that can cause rapid-repeat measurements.

## Confirmed facts

### 1. Old baseline does not have this feature

Verification:
```bash
grep -n "alertMonitor\|lastStressSampleAt\|HIGH_STRESS_INDEX" "/Users/amjadimran/Downloads/demo/old noisefit-android-luna /app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt"
# Output: (empty)
```

File size comparison:
- Old file (`old noisefit-android-luna`): 2,028 lines
- Current file (`noisefit-android-luna`): 3,388 lines

The ~1,360 additional lines include the entire alert monitor system.

### 2. Autonomous sender chain

The HRV/stress autonomous probe follows the same pattern as SpO2:

```
RingConnectionService.runAlertMonitorTick()
→ maybeRequestManualFallbackMeasurements(settings, now)
→ requestAlertMeasurement(ManualMeasureType.HRV, DeviceAlertFeature.HIGH_STRESS_INDEX, now)
→ sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetManualMeasurement(HRV, true))
→ ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)
→ ControlBleTools.getInstance().activeMeasurementStart(...)
```

Current code (after SpO2 fix):
```kotlin
// In maybeRequestManualFallbackMeasurements():
if (settings.isSupported(DeviceAlertFeature.HIGH_STRESS_INDEX) &&
    settings.highStress.enabled &&
    now - lastStressSampleAt >= ALERT_MANUAL_MEASUREMENT_INTERVAL_MS
) {
    requestAlertMeasurement(
        manualMeasureType = ManualMeasureType.HRV,
        feature = DeviceAlertFeature.HIGH_STRESS_INDEX,
        now = now
    )
}
```

### 3. Dedicated SDK API exists for stress alerts

Just like SpO2 has `getSWSPO2Monitor()` / `setSWSPO2Monitor()`, stress/HRV has:
- Query: `ControlBleTools.getInstance().getSWHRVMonitor(...)`
- Update: `ControlBleTools.getInstance().setSWHRVMonitor(...)`

These are used by the app's wrapper methods:
- `ZhQueryDeviceUnitsHandler.getHighStressAlertSettings()`
- `ZhUpdateDeviceUnitsHandler.setHighStressAlertSettings(...)`

This means:
- Stress alert threshold configuration does NOT require manual/active measurement.
- The dedicated screenless API handles all threshold read/write operations.

### 4. Continuous stress data already provides samples

The app already receives continuous stress data through:

```kotlin
is UserActivityCallback.StressDataObtainedOreo -> {
    handleOreoStressSample(userActivityCallback.stressData)
}
```

Which calls:
```kotlin
private fun handleOreoStressSample(stressData: OreoStressDataBreakup) {
    AlertMirrorEvaluator.extractLatestPositiveValue(stressData.breakUp)
        ?.takeIf { it > 0 }
        ?.let { observedValue ->
            processHighStressSample(observedValue, System.currentTimeMillis())
        }
}
```

And `processHighStressSample()` updates:
```kotlin
lastStressSampleAt = sampleTime
```

This means:
- Stress samples are already flowing passively from continuous callbacks.
- The autonomous manual measurement probe is redundant for sample freshness.

### 5. Same lifecycle/reset vulnerability

`stopAlertMonitor()` clears the throttle state:
```kotlin
private fun stopAlertMonitor() {
    alertMonitorHandler.removeCallbacks(alertMonitorRunnable)
    sessionManager.updateAlertRealtimeMonitoringState(false)
    alertMeasurementRequests.clear()
    alertMirrorStates.clear()
    recentWearEvidenceAt = null
    recentWearEvidenceValue = null
    lastHeartRateSampleAt = 0L
    lastSpo2SampleAt = 0L
    lastStressSampleAt = 0L  // <-- cleared here
    lastWearProbeAt = 0L
}
```

On reconnect or service lifecycle events, this clearing allows the autonomous probe to fire again immediately, potentially causing rapid-repeat measurement commands.

### 6. User-triggered stress measurements are separate

Home one-tap vitals (stress):
- File: `SummaryDataFragmentToday.kt` line 939
- Code: `viewModel.performOneTapVitalsOp(ManualMeasureType.STRESS, true)`

Measurement testing screen:
- File: `MeasurementsViewModel.kt`
- Handles both `ManualMeasureType.STRESS` and `ManualMeasureType.HRV`

These UI-driven paths are completely independent from the service's autonomous sender.

## Comparison with SpO2 diagnosis

| Aspect | SpO2 | HRV/Stress |
|--------|------|------------|
| In old baseline | No | No |
| Introduced in v2.3.1 | Yes | Yes |
| Dedicated SDK alert API | `getSWSPO2Monitor` / `setSWSPO2Monitor` | `getSWHRVMonitor` / `setSWHRVMonitor` |
| Continuous data callback | `UserActivityCallback.OreoBloodOxygenObtained` | `UserActivityCallback.StressDataObtainedOreo` |
| Sample timestamp update | `lastSpo2SampleAt` | `lastStressSampleAt` |
| Lifecycle reset issue | Yes | Yes |
| User-triggered paths affected | No | No |

## Why this is NOT from BlankTest / HR graph / post-workout work

The recent April 14-16 work documented in `changes.md` and `changes_detailed.md` includes:
- BlankTest graphs (body battery, HRV)
- HR/stress graph data shaping
- Post-workout data display
- DevSport visibility

None of these features:
- Call `maybeRequestManualFallbackMeasurements()`
- Dispatch autonomous `SetManualMeasurement(HRV, true)` commands
- Modify the alert service logic

Therefore this autonomous HRV probe is:
- Separate from the recent graph/post-workout work
- Part of the v2.3.1 alert system integration
- Safe to disable without affecting BlankTest or post-workout features

## Safe diagnosis verdict

Confirmed:
- The autonomous HRV/stress background measurement probe has the same structural issues as the SpO2 probe.
- It was introduced in the v2.3.1 SDK integration, not in the old baseline.
- It is not required for stress alert threshold configuration (dedicated API handles that).
- It is not required for stress sample freshness (continuous callbacks already provide samples).
- Disabling it will NOT affect user-triggered stress measurements.
- Disabling it will NOT affect BlankTest, HR graph, or post-workout work.

Recommended fix:
- Disable the HRV/stress branch in `maybeRequestManualFallbackMeasurements()` using the same pattern as the SpO2 fix.
- Keep the dedicated `getSWHRVMonitor()` / `setSWHRVMonitor()` alert configuration paths intact.
- Keep user-triggered stress measurement paths intact.
