# HRV/Stress Background Spot-Measurement Verdict

Date: April 16, 2026

This document answers the narrow questions:
- Was this issue already present in the old baseline?
- Was it introduced by the recent BlankTest / HR graph / post-workout work?
- If the background HRV/stress probe is removed correctly, what recent work is affected and what is not?

## Verdict summary

### 1. Did this exist in the old baseline?

**Verdict: No.**

Evidence:
- The old `RingConnectionService.kt` is 2,028 lines.
- The current file is 3,388 lines.
- The old file contains zero references to:
  - `alertMonitor`
  - `lastStressSampleAt`
  - `HIGH_STRESS_INDEX`
  - `maybeRequestManualFallbackMeasurements`

The entire alert monitor system was added during the v2.3.1 SDK integration.

### 2. Was this introduced by the recent BlankTest / HR graph / post-workout work?

**Verdict: No.**

Reason:
- The recent April 14-16 work is concentrated in:
  - BlankTest graphs (body battery, HRV visualization)
  - HR/stress graph data shaping and rendering
  - Post-workout data mapping and display
  - DevSport callback visibility
- These areas do NOT introduce autonomous service-side measurement commands.
- The autonomous HRV probe is part of the alert monitor system added in the v2.3.1 integration work (commit `207def46fe95970bd633a009effa0086dd9c1523`, Apr 6 2026).

### 3. Was it introduced by newer app-side work in the current codebase?

**Verdict: Yes.**

The autonomous HRV/stress measurement probe was introduced along with the SpO2 probe in the v2.3.1 SDK integration work.

### 4. Is this an SDK/AAR issue by itself?

**Verdict: No.**

The SDK exposes:
- Manual active measurement API for HRV (`activeMeasurementStart` with HRV measure type)
- Dedicated screenless HRV/stress alert-setting API (`getSWHRVMonitor` / `setSWHRVMonitor`)

The app is choosing to call the manual active-measurement API from a background service path. This is an app-side design decision, not an SDK defect.

## Impact analysis if the fix is done correctly

**Correct fix boundary:**
- Change only the autonomous service caller in `RingConnectionService.maybeRequestManualFallbackMeasurements()`
- Comment out or remove the HRV/stress branch

**Incorrect fix boundary:**
- Change or remove the shared manual measurement wrapper itself (`setManualMeasurement`)

## What will NOT be affected by the correct fix

### BlankTest graph work

**Safe:**
- HRV graph sourced from `onContinuousPressureData(...)`
- Body battery chart work
- Post-workout HR recovery chart in BlankTest
- Biological age display logic
- Raw callback/data viewer panels

**Reason:**
- These features receive data from continuous callbacks, not from service-triggered manual measurements.

### Production HR graph work

**Safe:**
- Home HR graph behavior
- Stress/HRV graph shaping work

**Reason:**
- These flows are repository / mapper / callback-data paths, not background manual measurement start senders.

### Production stress graph/card

**Safe:**
- Home stress card
- Stress details screen
- Stress graph data

**Reason:**
- Stress data is already flowing through `UserActivityCallback.StressDataObtainedOreo` continuous callback.
- This callback calls `handleOreoStressSample()` which updates `lastStressSampleAt`.
- The autonomous manual measurement is redundant.

### Post-workout window work

**Safe:**
- Post-workout details mapping
- DevSport-backed calorie / recovery / HR-zone display
- Latest parsed workout matching

**Reason:**
- These flows do not depend on service-side manual HRV/stress start commands.

### User-triggered stress measurements

**Safe:**
- Home one-tap vitals stress measure (`SummaryDataFragmentToday.performOneTapVitalsOp(STRESS, true)`)
- Measurement testing screen stress/HRV measure (`MeasurementsViewModel`)

**Reason:**
- These are UI-driven paths that directly call `UpdateDeviceAction.SetManualMeasurement`.
- They do NOT go through `maybeRequestManualFallbackMeasurements()`.

## What CAN be affected by the correct fix

### Alert-specific behavior

**Needs retest:**
- Passive app-side alert mirroring behavior for high stress
- Passive freshness of wear inference that currently benefits from manual-measurement results
- Recent-alert list behavior that depends on app-side mirror decisions

**Important distinction:**
- This is alert-system impact only.
- It is NOT graph or post-workout impact.

### BlankTest alert panel

**Needs retest:**
- The alert settings section inside `BlankTestFragment`
- Especially any alert-related passive status updates

**Still expected to keep working:**
- Direct threshold query/write
- Direct wear refresh button
- Log export

## What WILL break if the fix is done at the wrong layer

If someone disables or rewrites the shared manual-measurement wrapper (`setManualMeasurement`) instead of the service caller, then these valid user-triggered flows are at risk:
- Home one-tap vitals manual stress measure
- Measurement testing screen stress/HRV measure
- Any future explicit manual-measurement UX reusing the same wrapper

**Therefore the fix must NOT remove:**
- `ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)`
- `OreoDataConverter.getManualMeasurement(...)`

## Final verdict

**Answer to the main question:**
- The HRV/stress autonomous probe is NOT from the old baseline.
- It is NOT from the later BlankTest / HR graph / post-workout work.
- It comes from the same v2.3.1 integration work that introduced the SpO2 probe.

**Answer to the dependency question:**
- If the fix is done correctly at the service caller boundary, your recent BlankTest graph work, HR graph work, stress graph work, and post-workout window work should remain unaffected.
- User-triggered stress measurements will continue working.
- Only alert-system behavior and alert-related passive wear/status refresh logic need dedicated retesting.

**Answer to the redundancy question:**
- The autonomous HRV probe is redundant because:
  1. Stress samples already flow through `UserActivityCallback.StressDataObtainedOreo` callback.
  2. Stress alert threshold configuration uses the dedicated `getSWHRVMonitor()` / `setSWHRVMonitor()` API.
  3. User-triggered stress measurements use separate UI-driven paths.

## Recommendation

**Proceed with the fix:**
- Disable the HRV/stress branch in `maybeRequestManualFallbackMeasurements()` using the same pattern as the SpO2 fix.
- Add clear inline documentation explaining the reason.
- Preserve all dedicated alert API paths.
- Preserve all user-triggered measurement paths.
