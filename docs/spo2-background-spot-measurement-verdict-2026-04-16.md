# SpO2 Background Spot-Measurement Verdict

Date: April 16, 2026

This document answers the narrow questions:
- Was this issue already present in the old baseline?
- Was it introduced by the recent BlankTest / HR graph / post-workout work?
- If the background SpO2 probe is removed correctly, what recent work is affected and what is not?

## Verdict summary

### 1. Did this exist in the old baseline?

Verdict: No evidence of the autonomous background SpO2 sender in the old app snapshot.

What the old snapshot still had:
- explicit user-triggered manual measurement call sites

What the old snapshot did not have:
- the service-level background alert-monitor sender that can autonomously dispatch `SetManualMeasurement(BLOOD_OXYGEN, true)`

### 2. Was this introduced by the recent BlankTest / HR graph / post-workout work?

Verdict: No.

Reason:
- those recent changes are centered on:
  - BlankTest graphs and raw callback inspection
  - HR/stress graph data shaping
  - body-battery rendering
  - post-workout mapping and display
- those areas do not introduce the autonomous service-side SpO2 measurement sender

### 3. Was it introduced by newer app-side work in the current codebase?

Verdict: Yes.

Confirmed source:
- the current alert-monitor implementation introduced in commit:
  - `207def46fe95970bd633a009effa0086dd9c1523`
  - `V2.3.1 sdk integration`
  - author: `thatamjad`
  - date: Apr 6 2026

### 4. Is this an SDK/AAR issue by itself?

Verdict: No.

The SDK exposes:
- manual active measurement API
- dedicated screenless SpO2 alert-setting API

The repeated command is happening because the app is calling the manual active-measurement API from a background service path.

## Impact analysis if the fix is done correctly

Correct fix boundary:
- change only the autonomous service caller in `RingConnectionService`

Incorrect fix boundary:
- change or remove the shared manual measurement wrapper itself

## What will NOT be affected by the correct fix

### BlankTest graph work

Safe:
- HRV graph sourced from `onContinuousPressureData(...)`
- body-battery chart work
- post-workout HR recovery chart in BlankTest
- biological-age display logic
- raw callback/data viewer panels

Reason:
- those features do not depend on the background service autonomously calling manual SpO2 measurement

### Production HR graph work

Safe:
- home HR graph behavior
- stress/HRV graph shaping work

Reason:
- those flows are repository / mapper / callback-data paths, not background manual SpO2 start senders

### Post-workout window work

Safe:
- post-workout details mapping
- DevSport-backed calorie / recovery / HR-zone display
- latest parsed workout matching

Reason:
- those flows do not depend on service-side manual blood-oxygen start commands

## What CAN be affected by the correct fix

### Alert-specific behavior

Needs retest:
- passive app-side alert mirroring behavior for SpO2
- passive freshness of wear inference that currently benefits from manual-measurement results
- recent-alert list behavior that depends on app-side mirror decisions

Important distinction:
- this is alert-system impact only
- it is not graph or post-workout impact

### BlankTest alert panel

Needs retest:
- the alert settings section inside `BlankTestFragment`
- especially any alert-related passive status updates

Still expected to keep working:
- direct threshold query/write
- direct wear refresh button
- log export

## What WILL break if the fix is done at the wrong layer

If someone disables or rewrites the shared manual-measurement wrapper instead of the service caller, then these valid user-triggered flows are at risk:
- home one-tap vitals manual SpO2 measure
- measurement testing screen SpO2 measure
- any future explicit manual-measurement UX reusing the same wrapper

Therefore the future implementation must not remove:
- `ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)`
- `OreoDataConverter.getManualMeasurement(...)`

## Final verdict

Answer to the main question:
- the loop is not from the old baseline
- it is not from the later BlankTest / HR graph / post-workout work
- it comes from the newer app-side background alert-monitor implementation added during the v2.3.1 integration work

Answer to the dependency question:
- if the fix is done correctly at the service caller boundary, your recent BlankTest graph work, HR graph work, and post-workout window work should remain unaffected
- only alert-system behavior and alert-related passive wear/status refresh logic need dedicated retesting

