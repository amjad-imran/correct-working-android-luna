# SpO2 Background Spot-Measurement Loop Diagnosis

Date: April 16, 2026

Scope:
- Diagnose why the app keeps sending repeated spot/active SpO2 measurement commands to the band.
- Determine whether the behavior came from the recent BlankTest / HR graph / post-workout work, or from another app-side change.
- Produce a no-code diagnosis only.

Constraints respected:
- No production code changed.
- No `ZH-SDK` reference project code changed.
- No vendor AAR code changed.

## Inputs reviewed

- Current app code in `noisefit-android-luna`
- Old app snapshot in `old noisefit-android-luna`
- Vendor SDK documentation in `ZH SDK 文档 2.3.1.docx`
- Reference SDK demo project in `ZH-SDK`
- Existing local alert logs:
  - `alert_debug.log`
  - `1.alert_debug.log`
  - `2.alert_debug.log`
  - `3.alert_debug.log`
  - `4.alert_debug.log`
  - `5.alert_debug.log`
- Existing change notes:
  - `changes.md`
  - `changes_detailed.md`
- Local git history and blame in the current app repo

## Executive conclusion

The repeated command shown in the screenshot is not a raw SDK defect and it is not coming from the BlankTest graph work, home HR graph work, or post-workout window work.

It comes from a newer main-app background alert-monitor path in `RingConnectionService` that can autonomously request a manual blood-oxygen measurement:

`RingConnectionService.maybeRequestManualFallbackMeasurements()`
-> `requestAlertMeasurement(ManualMeasureType.BLOOD_OXYGEN, ...)`
-> `UpdateDeviceAction.SetManualMeasurement(BLOOD_OXYGEN, true)`
-> `ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)`
-> `ControlBleTools.getInstance().activeMeasurementStart(...)`

That sender does not exist in the old app snapshot. The lines that implement it were introduced in local commit `207def46fe95970bd633a009effa0086dd9c1523` (`V2.3.1 sdk integration`, author `thatamjad`, Apr 6 2026 08:53:19 +0530).

## Confirmed facts

1. The screenshot command is `activeMeasurementStart(...)` with `switchMeasure=true`.
2. In the current app, the shared manual-measurement wrapper sends that exact SDK call from `ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)`.
3. The current app has one autonomous main-app sender for background SpO2 measurement: `RingConnectionService.requestAlertMeasurement(...)`.
4. The old app snapshot has explicit user-triggered manual-measurement senders, but it does not have the service-level autonomous background sender.
5. The SpO2 alert write/query path itself already uses the dedicated screenless alert API:
   - query: `getSWSPO2Monitor(...)`
   - update: `setSWSPO2Monitor(...)`
6. The SDK documentation clearly separates:
   - active/manual measurement: `activeMeasurementStart(ActiveMeasureParamsBean, ...)`
   - screenless SpO2 alert settings: `getSWSPO2Monitor(...)` / `setSWSPO2Monitor(...)`
7. The current app also has a separate screenless continuous SpO2 settings wrapper:
   - `getSpo2Settings()`
   - `setSpo2Settings(Spo2Data)`
   - `setContinuousBloodOxygenSettings(...)`
   but that is not the command visible in the screenshot.

## Exact sender chain

### 1. SpO2 alert is enabled locally

The app stores `state.spo2.enabled = true` in `LocalDeviceAlertSettings`.

That write/query flow is handled by the alert settings layer and the ZH screenless alert API, not by manual measurement.

### 2. The service turns on the background alert monitor

`RingConnectionService.shouldRunAlertMonitor(...)` returns `true` when:
- the device is connected, and
- SpO2 alert support is confirmed, and
- `settings.spo2.enabled == true`

When that happens, `updateAlertMonitorState(...)` starts the service monitor.

### 3. The background tick evaluates whether a sample is missing

`runAlertMonitorTick()` calls:
- `maybeSendScheduledAlerts(...)`
- `maybeRequestManualFallbackMeasurements(...)`
- `maybePublishPassiveWearStatus(...)`
- `maybeRequestWearProbe(...)`

Inside `maybeRequestManualFallbackMeasurements(...)`, the SpO2 branch is:
- feature supported
- `settings.spo2.enabled == true`
- `now - lastSpo2SampleAt >= ALERT_MANUAL_MEASUREMENT_INTERVAL_MS`

### 4. The service requests a manual blood-oxygen measurement

The service then calls:

`requestAlertMeasurement(manualMeasureType = ManualMeasureType.BLOOD_OXYGEN, feature = DeviceAlertFeature.SPO2, now = now)`

That method immediately dispatches:

`UpdateDeviceAction.SetManualMeasurement(ManualMeasureType.BLOOD_OXYGEN, true)`

### 5. The SDK wrapper translates it into `activeMeasurementStart(...)`

`ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)` calls:

`ControlBleTools.getInstance().activeMeasurementStart(...)`

The `ActiveMeasureParamsBean` is built by `OreoDataConverter.getManualMeasurement(...)`, which sets:
- `switchMeasure = true`
- `measureType = BLOOD_OXYGEN`

This matches the screenshot pattern exactly:
- active measurement API
- blood-oxygen measure type
- start flag set to `true`

## Why the screenshot is not an SpO2 alert-setting write

This distinction is important.

The screenshot shows repeated `activeMeasurementStart(...)`.

That means the app is repeatedly asking the device to start an active/manual SpO2 measurement.

It does not mean the app is repeatedly writing the threshold alert itself. Threshold alert writes use:
- `setSWSPO2Monitor(...)`

Those are different SDK commands and different code paths.

## Old-vs-current comparison

### What already existed in the old app

The old app already had explicit user-triggered manual measurement senders in UI flows such as:
- `MeasurementsViewModel`
- `SummaryDataViewModelToday`

Those paths are user-driven. They require a tap and are not autonomous background senders.

### What is new in the current app

The current app adds a service-level autonomous sender in:
- `RingConnectionService.maybeRequestManualFallbackMeasurements(...)`

The old app snapshot does not contain:
- `AlertSettingsFragment`
- `AlertSettingsViewModel`
- `com/oreo/alerts/AlertMirrorSupport.kt`
- the autonomous service-side SpO2/HRV fallback request logic

### Historical attribution

`git blame` on the service lines shows the background SpO2 fallback sender was introduced in:
- commit: `207def46fe95970bd633a009effa0086dd9c1523`
- subject: `V2.3.1 sdk integration`
- author: `thatamjad`
- date: `Mon Apr 6 08:53:19 2026 +0530`

## Why this is not caused by the recent BlankTest / HR graph / post-workout work

The recent April 14-16 work recorded in `changes.md` / `changes_detailed.md` is concentrated around:
- `BlankTestFragment`
- HR/stress graph data shaping
- body-battery chart rendering
- post-workout data display and mapping
- DevSport / raw callback visibility

Those areas do not introduce a new autonomous `SetManualMeasurement(BLOOD_OXYGEN, true)` sender.

Specifically:
- `BlankTestFragment` binds alert settings UI and charts, but it does not directly call `SetManualMeasurement(...)` for the screenshoted loop.
- The production HR graph and stress graph code paths are data-converter / repository / viewmodel paths, not active-measurement senders.
- The post-workout window uses workout parsing/mapping paths, not manual blood-oxygen start commands.

Therefore:
- this issue is newer than the old baseline
- but it is separate from the later BlankTest graph / HR graph / post-workout fixes

## Why the fast repeat cadence in the screenshot can still happen

### Confirmed behavior

The current design intentionally allows background service-driven manual SpO2 requests when alert monitoring is active and no fresh sample is considered available.

### Designed guard

The service contains a nominal time guard:
- `ALERT_MANUAL_MEASUREMENT_INTERVAL_MS = 5 * 60_000L`

### Why the screenshot can still look faster

The screenshot cadence appears much faster than 5 minutes.

The exact Apr 15 2026 14:13:48 loop trigger is not directly provable from the local logs currently available, so the next point is an inference, not a confirmed fact:

Most likely amplifiers are:
- alert monitor restarts
- reconnect/state churn
- service lifecycle resets

Why that inference is reasonable:
- `stopAlertMonitor()` clears:
  - `alertMeasurementRequests`
  - `lastSpo2SampleAt`
  - `lastStressSampleAt`
- repeated `ConnectSuccess` transitions are visible in the local alert logs
- repeated alert-setting saves are also visible during alert synchronization

If the monitor is repeatedly stopped and restarted, the request/sample throttle state can be cleared, allowing a new active measurement start sooner than the nominal 5-minute cadence.

Confirmed status:
- autonomous sender path: confirmed
- old-vs-new origin: confirmed
- exact sub-cause of second-level repetition in the specific screenshot: inferred, not fully proven from the available timestamp-matching logs

## Why removing the wrong layer would be dangerous

The shared wrapper method:
- `ZhUpdateDeviceUnitsHandler.setManualMeasurement(...)`

is still needed for legitimate user-triggered flows such as:
- home one-tap vitals measurement
- measurement testing screen
- any intentional manual measurement UX

So the safest fix boundary is:
- stop the service from autonomously calling manual SpO2 measurement

The unsafe fix boundary is:
- removing or breaking the shared manual measurement wrapper itself

## Impact if left as-is

- repeated unwanted blood-oxygen measurement commands to the band
- possible battery drain on both phone and wearable
- noisy logs and harder debugging
- risk of interfering with user-triggered measurements
- misleading wear-status freshness because manual measurement results are also used as wear evidence
- user-visible behavior that feels like a loop or stuck background command stream

## Safe diagnosis verdict

Confirmed:
- The repeated SpO2 spot-measurement start command is coming from the current app’s background alert-monitor implementation.
- It is not coming from the vendor AAR alone.
- It is not caused by the later BlankTest graph / HR graph / post-workout work.
- It was introduced in the newer app-side v2.3.1 integration work, not in the old baseline.

Recommended future fix boundary:
- caller-side in `RingConnectionService`
- not in the shared SDK-wrapper manual measurement implementation

