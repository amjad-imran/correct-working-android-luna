# SpO2 Background Spot-Measurement Loop Fix Plan

Date: April 16, 2026

Goal:
- Stop autonomous background SpO2 spot-measurement starts completely.
- Preserve valid user-triggered manual measurements.
- Preserve screenless SpO2 alert read/write support.
- Avoid any change to vendor AAR code or the `ZH-SDK` reference project.

Recommended implementation direction:
- Fix this only in the app-side caller path.
- Do not remove or weaken the shared `setManualMeasurement(...)` wrapper.
- Do not change the vendor AAR.

## Phase 0 - Fix boundary agreement

Objective:
- Freeze the exact scope before touching code.

Decision to lock in:
- The problem is not the existence of manual SpO2 measurement itself.
- The problem is the service autonomously invoking it in background.

Boundary:
- Allowed fix area:
  - `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`
  - optionally alert-specific tests and documentation
- Must not be used for the immediate containment fix:
  - `ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)`
  - `OreoDataConverter.getManualMeasurement(...)`
  - vendor AAR code

Reason:
- Those shared layers are still required by valid user-triggered manual measurement screens.

## Phase 1 - Immediate containment

Objective:
- Ensure the app never starts background SpO2 spot measurements by itself.

Primary change:
- Remove or fully disable the SpO2 branch inside `RingConnectionService.maybeRequestManualFallbackMeasurements(...)`.

Recommended implementation choice:
- Keep the method if needed for future review, but stop it from dispatching:
  - `ManualMeasureType.BLOOD_OXYGEN`

Optional parallel decision:
- Review whether the same autonomous pattern for `ManualMeasureType.HRV` should also be removed in the same patch or deferred to a follow-up task.

Acceptance criteria:
- Enabling low SpO2 alert must not produce autonomous `UpdateDeviceAction.SetManualMeasurement(BLOOD_OXYGEN, true)`.
- The screenshot pattern `activeMeasurementStart(... measureType=2, switchMeasure=true)` must disappear unless a user explicitly triggers a manual SpO2 measurement.

## Phase 2 - Preserve native SpO2 alert configuration

Objective:
- Keep the real SpO2 alert feature working.

What must remain unchanged:
- threshold query through `getSWSPO2Monitor(...)`
- threshold update through `setSWSPO2Monitor(...)`
- alert UI persistence through `AlertSettingsViewModel`

Why this is safe:
- The dedicated screenless threshold alert API is separate from manual active measurement.
- The screenshoted loop is not required for storing or syncing the low-SpO2 threshold.

Validation after this phase:
- toggling the SpO2 alert still queries and writes `SWSPO2MonitorBean`
- settings still survive reconnects through the existing local-state + pending-sync logic

## Phase 3 - Define the desired product behavior for app-side mirroring

Objective:
- Decide what should happen after autonomous manual probing is removed.

### Recommended default

For screenless SpO2 alerts, rely on:
- native/device threshold configuration via `setSWSPO2Monitor(...)`

Do not rely on:
- service-triggered spot measurements

Why this is the safest default:
- it fixes the loop directly
- it keeps the feature aligned with the dedicated device-side alert API
- it avoids hidden background sensor activation

### Optional later enhancement

If the product still needs app-side mirrored SpO2 notifications, then the mirror engine must use passive data only.

Allowed future sample sources:
- continuous SpO2 callbacks already available in the pipeline
- existing stored/passive samples
- explicitly enabled continuous SpO2 monitoring settings, if product approves the battery tradeoff

Not allowed future source:
- background `activeMeasurementStart(...)`

Existing app-side hook available for future passive design:
- `setSpo2Settings(Spo2Data)` already maps to `setContinuousBloodOxygenSettings(...)`

Important note:
- enabling continuous SpO2 monitoring is a product/battery decision, not just a technical one
- it should not be bundled silently into the containment fix

## Phase 4 - Lifecycle hardening

Objective:
- Prevent similar loops even if some background probe logic remains elsewhere.

Hardening actions:
- Persist request-throttle state across short reconnect/monitor restarts if any automatic probe is ever retained.
- Avoid clearing request/sample throttles too aggressively in `stopAlertMonitor()`.
- Add a strict single in-flight guard per automatic measurement type.
- Tag measurement origin explicitly:
  - `USER_UI`
  - `ALERT_SERVICE`
  - `TEST_SCREEN`
- If any background measurement is intentionally retained in future, make stop/timeout handling explicit instead of relying only on start calls.

Why this phase matters:
- the current fast-loop symptom strongly suggests lifecycle/reset churn can amplify the issue
- even if the SpO2 branch is removed now, the app should not be able to reintroduce the same class of bug later

## Phase 5 - Protect legitimate manual measurement UX

Objective:
- Ensure the fix does not break explicit user actions.

User-triggered paths that must continue working:
- home one-tap vitals SpO2 measurement
- measurement testing screen SpO2 measurement
- any existing intentional manual-measurement UI

How to guarantee that:
- do not modify the shared wrapper:
  - `ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)`
- do not change the `OreoDataConverter` manual-measurement mapping
- change only the background service caller

## Phase 6 - Regression verification

### Mandatory code-level verification targets

- `RingConnectionService`
- alert settings write/query flow
- home one-tap vitals SpO2 measure flow
- measurement testing screen SpO2 measure flow
- BlankTest alert panel
- BlankTest graphs
- production HR graph
- post-workout window

### Mandatory behavior checks

1. Enable low SpO2 alert while connected.
2. Confirm the app writes only the threshold/config path:
   - `setSWSPO2Monitor(...)`
3. Leave the app idle in the same scenario that previously produced the loop.
4. Confirm no autonomous `activeMeasurementStart(... measureType=2, switchMeasure=true)` appears.
5. Tap a legitimate manual SpO2 action from home one-tap vitals.
6. Confirm the manual measurement still starts normally.
7. Tap the measurement testing screen SpO2 action.
8. Confirm that screen still works normally.
9. Verify BlankTest body-battery, HRV graph, and post-workout HR chart remain unaffected.
10. Verify post-workout details still load and match the current behavior.
11. Verify direct wear refresh still works through `GetRingWearingStatus`.

### Recommended automated coverage

Add focused tests for:
- service does not request `ManualMeasureType.BLOOD_OXYGEN` just because SpO2 alert is enabled
- user-triggered SpO2 measurement senders still dispatch correctly
- low-SpO2 alert query/write state still persists and verifies correctly

Good candidate test areas:
- `app/src/test/java/com/oreo/ui/home/summary/alerts/`
- new service-focused tests under `app/src/test/java/com/oreo/receiver/service/`

## Phase 7 - Rollout strategy

Objective:
- Minimize risk while landing the fix.

Recommended order:
1. Ship the smallest caller-side containment fix first.
2. Verify on real hardware that the loop is gone.
3. Re-validate all explicit manual-measurement screens.
4. Only after that, decide whether app-side passive SpO2 mirroring still needs a second task.

Rollback strategy:
- if the containment patch unexpectedly affects manual measurement UX, revert only the service caller change
- do not revert alert-setting API integration unless there is a separate alert-setting regression

## Final implementation recommendation

The safest complete fix is:
- stop `RingConnectionService` from autonomously starting manual SpO2 measurements
- keep screenless SpO2 threshold alerts on the dedicated `SWSPO2Monitor` API
- keep explicit user-triggered manual measurements untouched
- treat any future passive/mirrored SpO2 alerting as a separate follow-up based on passive data, not active spot measurement

