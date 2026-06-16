# Heart Rate Background Measurement Analysis

Date: April 16, 2026

Scope:
- Analyze whether heart rate has the same autonomous background measurement issue as SpO2 and HRV/Stress.
- Document the findings.

## Executive Summary

**Finding: Heart rate does NOT have the same autonomous background measurement issue.**

The `maybeRequestManualFallbackMeasurements()` method, which was causing the SpO2 and HRV/Stress measurement loops, **never had a heart rate branch**. Heart rate follows a fundamentally different design pattern in this app.

## Analysis Details

### 1. Method inspection

The `maybeRequestManualFallbackMeasurements()` method only contained branches for:
- **SpO2 (BLOOD_OXYGEN)** - NOW DISABLED
- **HRV/Stress (HRV)** - NOW DISABLED
- **Heart rate (HEART_RATE)** - NEVER EXISTED

There was never an autonomous background heart rate measurement request in this method.

### 2. Heart rate sample sources

Heart rate samples flow through continuous callbacks, not manual measurements:

```kotlin
// Source 1: Real-time heart rate from SDK
is QueryCallback.RealTimeHeartRateSampleObtained -> {
    processHeartRateSample(
        value = queryCallback.value,
        sampleTime = queryCallback.timeStamp
    )
}

// Source 2: Continuous heart rate history
is UserActivityCallback.HeartHistoryObtainedOreo -> {
    AlertMirrorEvaluator.extractLatestPositiveValue(userActivityCallback.heartRateData.breakUp)
        ?.takeIf { it > 0 }
        ?.let { processHeartRateSample(it, System.currentTimeMillis()) }
}
```

These are passive callbacks - the app receives heart rate data without needing to request manual measurements.

### 3. Heart rate alert configuration

Heart rate alerts use dedicated SDK APIs for threshold configuration:

| Operation | API | Wrapper |
|-----------|-----|---------|
| Query | `getSWHRMonitor()` | `ZhQueryDeviceUnitsHandler.getHeartRateAlertSettings()` |
| Update | `setSWHRMonitor()` | `ZhUpdateDeviceUnitsHandler.setHeartRateAlertSettings()` |

These are device-side threshold alert settings, NOT manual/active measurement triggers.

### 4. User-triggered heart rate measurements

User-triggered heart rate measurements do exist:
- Home one-tap vitals: `SummaryDataFragmentToday.kt` line 934
  - `viewModel.performOneTapVitalsOp(ManualMeasureType.HEART_RATE, true)`

This is a UI-driven path, completely independent from any background service logic.

### 5. Manual measurement callback handling

The service handles heart rate manual measurement results:
```kotlin
} else if (dataCallback.manualMeasurement.manualMeasureType == ManualMeasureType.HEART_RATE) {
    ringDataStore.setManualMeasurementValue(dataCallback.manualMeasurement)
    sessionManager.setManualMeasurementValue(
        true,
        dataCallback.manualMeasurement.manualMeasureType
    )
}
```

This is for **receiving** results, not **requesting** measurements.

## Comparison with SpO2 and HRV/Stress

| Aspect | SpO2 | HRV/Stress | Heart Rate |
|--------|------|------------|------------|
| Autonomous background probe | Had one (disabled) | Had one (disabled) | **Never had one** |
| Branch in `maybeRequestManualFallbackMeasurements()` | Yes (disabled) | Yes (disabled) | **No** |
| Continuous data callbacks | Yes | Yes | **Yes** |
| Dedicated SDK alert API | Yes | Yes | **Yes** |
| User-triggered manual measurement | Yes | Yes | **Yes** |
| Issue present | Yes (fixed) | Yes (fixed) | **No issue** |

## Why Heart Rate Was Designed Differently

Possible reasons why heart rate never had an autonomous background probe:

1. **Continuous heart rate monitoring is more common**
   - Real-time heart rate callbacks (`RealTimeHeartRateSampleObtained`) are typically enabled.
   - The device provides heart rate data continuously without app-side requests.

2. **Heart rate alert is device-side**
   - The device itself monitors and alerts for heart rate thresholds.
   - No need for app-side "freshness probing."

3. **Different alert architecture**
   - SpO2 and stress alerts were designed with an app-side mirror/fallback pattern.
   - Heart rate alerts were designed to rely on the device's native alert mechanism.

## Conclusion

**No fix is needed for heart rate.**

The heart rate implementation follows a cleaner pattern where:
1. Heart rate samples come from continuous SDK callbacks.
2. Heart rate alert thresholds are configured via dedicated SDK APIs.
3. User-triggered manual measurements are UI-driven.
4. No autonomous background measurement requests exist in the service.

## Verification Checklist

To verify heart rate behavior is correct:

1. ✅ Check `maybeRequestManualFallbackMeasurements()` - no heart rate branch exists.
2. ✅ Check heart rate sample sources - all from continuous callbacks.
3. ✅ Check heart rate alert APIs - use dedicated `getSWHRMonitor()` / `setSWHRMonitor()`.
4. ✅ Check user-triggered paths - exist and are UI-driven.

**Result: Heart rate implementation is correct and does not require any changes.**
