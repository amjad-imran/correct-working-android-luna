# Alerts And Notifications Implementation

## 1. Purpose

This document explains how the alert settings, mirrored alert notifications, recent-alert UI, logging, and wear-status flow are implemented in the current app build for screenless ZH SDK devices, specifically the Luna family (`noisefit_luna` / `luna_band`).

The goal of this document is to let an engineer explain to seniors:

- what is stored locally
- which ZH SDK methods are used
- which callbacks are used
- how the UI writes and verifies settings
- how repeated alert notifications are generated for a screenless device
- how wear status is inferred
- what parts are device-native vs app-mirrored

This implementation is local-only. It does not use or update backend APIs.

## 2. Scope

The current implementation covers the alert section shown from the green-button debug/dashboard window and keeps the older dashboard content intact.

Implemented alert-related areas:

- Resting high heart-rate alert
- Low heart-rate alert
- Low SpO2 alert
- High stress index alert
- Relaxation prompt
- Bedtime reminder
- Sedentary reminder
- Wear status display and manual refresh
- Mirrored alert notification feed in the app
- Alert log export

Visible but intentionally limited:

- High HR Alert (Workout) stays visible, but on screenless devices it is disabled because the screenless SDK alert bean (`SWHRMonitorBean`) does not expose a distinct workout threshold in the current implementation path.

## 3. Main Design Decision

The implementation separates alerts into two layers:

### 3.1 Device-native alert configuration

These are the actual settings pushed to the band through the ZH AAR. Examples:

- heart-rate threshold settings
- SpO2 threshold setting
- HRV/stress warning setting
- sleep reminder setting
- sedentary reminder setting
- breathing/relaxation prompt setting

These settings are configured through `UpdateDeviceAction` -> wrapper -> `ControlBleTools` SDK calls.

### 3.2 App-mirrored alert notifications

Because the device is screenless and native haptics are hard to identify visually, the app also runs a connected-only mirror engine inside `RingConnectionService`.

That engine:

- listens to live or derived samples
- decides when a threshold crossing happened
- applies hysteresis and repeat intervals
- sends an app notification to the band
- records an alert event locally
- shows a banner and recent-alert feed in the app UI

This means:

- native band alerts can still happen from device firmware
- app-mirrored notifications are an extra, explicit alert path so the app can say exactly what it sent

## 4. Key Files

- `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt`
- `app/src/main/java/com/oreo/ui/home/summary/AlertSettingsViewModel.kt`
- `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`
- `app/src/main/res/layout/layout_alert_settings_panel.xml`
- `app/src/main/java/com/noisefit/session/SessionManager.kt`
- `commons/src/main/java/com/noisefit_commans/models/ColorfitData.kt`
- `commons/src/main/java/com/noisefit_commans/models/AlertSettingsDeviceDefaults.kt`
- `commons/src/main/java/com/noisefit_commans/models/AlertSettingsStateUtils.kt`
- `commons/src/main/java/com/noisefit_commans/data/local/abstraction/WatchDataStore.kt`
- `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`
- `commons/src/main/java/com/noisefit_commans/interfaces/QueryAction.kt`
- `commons/src/main/java/com/noisefit_commans/interfaces/QueryCallback.kt`
- `commons/src/main/java/com/noisefit_commans/interfaces/device_data/UpdateDeviceAction.kt`
- `commons/src/main/java/com/noisefit_commans/interfaces/device_data/UpdateDeviceDataCallback.kt`
- `commons/src/main/java/com/noisefit_commans/utils/AlertDebugLogger.kt`
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/AlertSettingsMapper.kt`
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhQueryDeviceUnitsHandler.kt`
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt`
- `app/src/main/java/com/oreo/alerts/AlertMirrorSupport.kt`

## 5. SDK Methods Used

The current screenless implementation treats the following ZH SDK methods as authoritative for device-side settings.

### 5.1 Heart-rate alert

- Query: `ControlBleTools.getInstance().getSWHRMonitor(...)`
- Update: `ControlBleTools.getInstance().setSWHRMonitor(bean, ...)`
- Query callback used in wrapper: `SettingMenuCallBack.onSWHRMonitor(...)`
- Mirror sample callback: `RealTimeHeartRateCallback.onDataResult(timeMillis, hrValue)`

Mapped app model:

- `HeartRateAlertSettings.restingEnabled`
- `HeartRateAlertSettings.restingThreshold`
- `HeartRateAlertSettings.lowEnabled`
- `HeartRateAlertSettings.lowThreshold`

Current screenless limitation:

- `workoutEnabled/workoutThreshold` are not written through `SWHRMonitorBean`
- UI keeps workout alert visible but disabled on screenless devices

### 5.2 SpO2 alert

- Query: `ControlBleTools.getInstance().getSWSPO2Monitor(...)`
- Update: `ControlBleTools.getInstance().setSWSPO2Monitor(bean, ...)`
- Query callback used in wrapper: `SettingMenuCallBack.onSWSPO2Monitor(...)`

Mirror sample sources:

- derived user-activity callback path for continuous/offline SpO2 data
- fallback active measurement using `UpdateDeviceAction.SetManualMeasurement(ManualMeasureType.BLOOD_OXYGEN, true)`

### 5.3 High stress index alert

This is implemented on screenless devices by reusing the SDK HRV warning API.

- Query: `ControlBleTools.getInstance().getSWHRVMonitor(...)`
- Update: `ControlBleTools.getInstance().setSWHRVMonitor(bean, ...)`
- Query callback used in wrapper: `SettingMenuCallBack.onSWHRVMonitor(...)`

Mapped app model:

- `HighStressAlertSettings.enabled`
- `HighStressAlertSettings.threshold`

Mirror sample sources:

- derived user-activity callback path for continuous pressure / HRV data
- fallback active measurement using `UpdateDeviceAction.SetManualMeasurement(ManualMeasureType.HRV, true)`

### 5.4 Relaxation prompt

- Query: `ControlBleTools.getInstance().getSWBRMonitor(...)`
- Update: `ControlBleTools.getInstance().setSWBRMonitor(bean, ...)`
- Query callback used in wrapper: `SettingMenuCallBack.onSWBRMonitor(...)`

Mapped app model:

- `PressureModeSettings.relaxationPromptEnabled`
- `PressureModeSettings.stressMonitoringEnabled`

For screenless mode, both flags are normalized to the same enable state.

### 5.5 Bedtime reminder

- Query: `ControlBleTools.getInstance().getSleepReminder(...)`
- Update: `ControlBleTools.getInstance().setSleepReminder(bean, ...)`
- Query callback used in wrapper: `QueryCallback.SleepReminderObtained(...)`

Important support rule:

- a query-time `NOT_SUPPORT` does not immediately disable the row
- bedtime support remains unresolved until a set attempt also fails

### 5.6 Sedentary reminder

- Query: `ControlBleTools.getInstance().getSedentaryReminder(...)`
- Update: `ControlBleTools.getInstance().setSedentaryReminder(bean, ...)`
- Query callback used in wrapper: `QueryCallback.SedentaryReminderSettingsObtained(...)`

Mapped app model:

- `SedentaryData.status`
- `SedentaryData.interval`
- `SedentaryData.startHour/startMinute`
- `SedentaryData.endHour/endMinute`

There is special readback handling for `luna_band` because the device can return a zeroed reminder after a successful write.

### 5.7 Wear status

- Manual probe: `ControlBleTools.getInstance().getRingWearingStatus(...)`
- Query callback used in wrapper: `MicroCallBack.onRingWearingStatus(status)`

Wear detection is not treated as a threshold alert. It is implemented as a status/inference feature.

### 5.8 App-side notification sent to the band

- `UpdateDeviceAction.SendAppNotification(AppNotification(...))`
- routed by `ServiceUtil`
- handled in wrapper by `ZhUpdateDeviceUnitsHandler.sendAppNotification(appNotification)`
- SDK call used:
  - `ControlBleTools.getInstance().sendAppNotification(...)`
  - or `sendSystemNotification(...)` for missed call / SMS types

The mirrored alert engine uses `ApplicationType.NOISEFIT.type` and sends a named app notification to the band.

### 5.9 Realtime streaming switch

- `UpdateDeviceAction.SetRealTimeDataState(state)`
- wrapper method: `ZhUpdateDeviceUnitsHandler.setRealTimeDataState(status)`
- SDK call: `ControlBleTools.getInstance().realTimeDataSwitch(status, null)`

This is important because the mirror engine depends on realtime/live-derived samples while the app is connected.

## 6. SDK Documentation References

The implementation is aligned with these method families documented in `ZH-SDK.txt`:

- `realTimeDataSwitch(...)`
- `getSleepReminder(...)` / `setSleepReminder(...)`
- `getSedentaryReminder(...)` / `setSedentaryReminder(...)`
- `getSWHRMonitor(...)` / `setSWHRMonitor(...)`
- `getSWSPO2Monitor(...)` / `setSWSPO2Monitor(...)`
- `getSWHRVMonitor(...)` / `setSWHRVMonitor(...)`
- `getSWBRMonitor(...)` / `setSWBRMonitor(...)`
- `getRingWearingStatus(...)`
- `MicroCallBack.onRingWearingStatus(...)`
- `RealTimeHeartRateCallback.onDataResult(...)`
- `sendAppNotification(...)`

The docs also describe:

- continuous blood oxygen callbacks
- continuous pressure callbacks
- HRV/stress measurement type `STRESS_HRV`

Those are relevant to the app-side mirror engine and manual fallback measurements.

## 7. Local Models And Persistence

All alert state is local-only and persisted through `WatchDataStore`.

Main persisted model:

- `LocalDeviceAlertSettings`

Important fields inside it:

- `heartRate`
- `spo2`
- `highStress`
- `pressureMode`
- `sleepReminder`
- `sedentaryReminder`
- `wearDetectionStatus`
- `recentAlerts`
- `pendingSync`
- `support`
- `snapshots`

Persistence path:

- interface: `WatchDataStore.updateLocalDeviceAlertSettings(...)`
- implementation: `WatchDataStoreImpl.updateLocalDeviceAlertSettings(...)`
- storage format: JSON in shared preferences

Important persistence rule:

- `WatchDataStoreImpl.clearWatchData()` keeps the alert JSON instead of deleting it, so alert settings survive broader watch-data cleanup.

## 8. UI Layer

The active alert UI is inside `BlankTestFragment`, not a separate production settings page.

Main UI widgets:

- toggle + numeric input for HR, SpO2, stress
- toggle + time chooser for bedtime reminder
- toggle + time window + interval for sedentary reminder
- toggle for relaxation prompt
- wear-status row with Refresh
- banner for latest mirrored alert
- recent-alert list
- export-log button

Important UI methods in `BlankTestFragment`:

- `initAlertSettings(root)`
- `bindAlertState(root, state)`
- `commitHeartRateSettings(root)`
- `commitSpo2Settings(root)`
- `commitHighStressSettings(root)`
- `commitSleepReminder(root)`
- `commitRelaxationPrompt(root)`
- `commitSedentaryReminder(root)`
- `bindWearDetectionStatus(root, state)`
- `bindRecentAlerts(root, state)`
- `showAlertBanner(root, event)`
- `exportAlertLogs()`

UI behavior:

- every switch or field commit immediately calls into `AlertSettingsViewModel`
- values are rebound from `LocalDeviceAlertSettings`
- stale responses are intentionally ignored by the ViewModel so older callback data does not overwrite the user’s latest change

## 9. ViewModel Layer

`AlertSettingsViewModel` is the screen-level coordinator between UI and the service/wrapper callback system.

### 9.1 Responsibilities

- load local alert state
- validate user input
- write the edited feature locally first
- mark the feature as pending
- start sync immediately if connected
- verify device writeback by querying again
- keep unsupported/pending state local
- prevent stale callback data from resetting the visible value

### 9.2 Main public methods

- `loadState()`
- `onConnectedStateChanged(connectState)`
- `refreshFromBand()`
- `saveHeartRateSettings(...)`
- `saveSpo2Settings(...)`
- `saveHighStressSettings(...)`
- `savePressureModeSettings(...)`
- `saveSleepReminder(...)`
- `saveSedentaryReminder(...)`
- `refreshWearDetectionStatus()`
- `handleQueryCallback(queryCallback)`
- `handleUpdateCallback(dataCallback)`

### 9.3 Validation

Validation is in `AlertSettingsValidation`.

- heart rate thresholds: `1..250`
- SpO2 threshold: `1..100`
- stress threshold: `1..100`
- sedentary interval: positive multiple of `60`

Important detail:

- disabled fields are not forced to pass validation
- only enabled alerts must provide a valid threshold

### 9.4 Pending sync flow

When the user edits a feature:

1. `BlankTestFragment.commit...()` builds a model.
2. `AlertSettingsViewModel.save...()` validates it.
3. `persistFeature(feature)` updates `LocalDeviceAlertSettings`.
4. `pendingSync.<feature>` is set to `true`.
5. state is saved locally and published to the UI immediately.
6. if connected, `syncQueuedFeatures()` starts the device sync.
7. if disconnected, the user gets a local-only message and the service will sync later on reconnect.

### 9.5 Write/verify state machine

The ViewModel uses:

- `ScreenSyncPhase.SNAPSHOT_QUERY`
- `ScreenSyncPhase.UPDATE_SENT`
- `ScreenSyncPhase.VERIFY_QUERY`

It also tracks an operation id per feature to reject stale callbacks.

Why this exists:

- alert writes and reads can overlap
- a slow callback from an older operation must not overwrite a newer local change

## 10. Service Layer

`RingConnectionService` contains the background alert engine and the reconnect sync logic.

### 10.1 Responsibilities

- persist current alert settings
- auto-sync pending settings when connected
- run the connected-only mirror engine
- keep realtime streaming enabled while the mirror engine is active
- convert incoming samples into alert events
- push app notifications to the band
- publish alert events into the UI through `SessionManager`
- infer wear state from sensor evidence and direct queries

### 10.2 Main alert-related service methods

- `saveAlertSettings(settings)`
- `updateAlertMonitorState(settings)`
- `shouldRunAlertMonitor(settings)`
- `stopAlertMonitor()`
- `runAlertMonitorTick()`
- `maybeSendScheduledAlerts(settings, now)`
- `maybeRequestManualFallbackMeasurements(settings, now)`
- `requestAlertMeasurement(manualMeasureType, feature, now)`
- `maybeRequestWearProbe(now)`
- `maybePublishPassiveWearStatus(now)`
- `processHeartRateSample(value, sampleTime)`
- `processSpo2Sample(value, sampleTime)`
- `processHighStressSample(value, sampleTime)`
- `handleMirrorDecision(...)`
- `dispatchMirroredAlert(...)`
- `consumeAlertMeasurementRequest(manualMeasurement)`
- `sendAlertQuery(feature)`
- `sendAlertUpdate(feature, settings)`
- `syncPendingAlertSettings()`

### 10.3 When the monitor is active

The service monitor runs only if:

- the band is connected, and
- at least one supported mirror-capable feature is enabled

Current mirror-capable features:

- heart rate
- SpO2
- high stress
- bedtime reminder
- sedentary reminder

When active, the service calls:

- `sessionManager.updateAlertRealtimeMonitoringState(true)`

That causes:

- `UpdateDeviceAction.SetRealTimeDataState(true)`
- then wrapper:
  - `ZhUpdateDeviceUnitsHandler.setRealTimeDataState(status)`
  - `ControlBleTools.getInstance().realTimeDataSwitch(status, null)`

This keeps the live sample path available even when the app goes to background.

## 11. SessionManager Role

`SessionManager` is used as the in-process bus between UI, service, and SDK handlers.

Important alert-related members:

- `sendQueryAction(action)`
- `setQueryCallback(callback)`
- `sendUpdateQueryAction(action)`
- `setUpdateDeviceCallback(callback)`
- `postAlertMirrorEvent(event)`
- `alertMirrorEvent`
- `alertRealtimeMonitoringActive`
- `updateAlertRealtimeMonitoringState(active)`

Important background behavior:

- when the app goes to background, realtime data stays enabled if `alertRealtimeMonitoringActive` is true
- this is how mirrored alerts can continue while connected even when the user is not staring at the alert screen

## 12. Wrapper Mapping Layer

`AlertSettingsMapper` translates app models to ZH SDK beans and back.

### 12.1 Heart rate

Screenless mapping:

- app -> `SWHRMonitorBean`
- device -> `HeartRateAlertSettings`

Fields used:

- `isHeightWarning`
- `heightWarningValue`
- `isLowWarning`
- `lowWarningValue`

### 12.2 SpO2

- app -> `SWSPO2MonitorBean`
- device -> `Spo2AlertSettings`

Fields used:

- `isWarning`
- `warningValue`

### 12.3 High stress

- app -> `SWHRVMonitorBean`
- device -> `HighStressAlertSettings`

Fields used:

- `isWarning`
- `warningValue`

### 12.4 Relaxation prompt

Screenless mapping:

- app -> `SWBRMonitorBean`
- device -> `PressureModeSettings`

Fields used:

- `isWarning`
- `warningValue`

### 12.5 Sedentary reminder

- app -> `CommonReminderBean`
- device -> `SedentaryData`

Snapshot preservation:

- lunch/no-disturb fields are preserved in `SedentaryReminderSnapshot`

### 12.6 Sleep reminder

Normalization:

- `second = 0`
- `millisecond = 0`

## 13. Query Callbacks Used

The wrapper converts ZH callback data into app-level `QueryCallback` events.

Main callback objects used by the alert flow:

- `QueryCallback.HeartRateAlertSettingsObtained(...)`
- `QueryCallback.Spo2AlertSettingsObtained(...)`
- `QueryCallback.HighStressAlertSettingsObtained(...)`
- `QueryCallback.PressureModeSettingsObtained(...)`
- `QueryCallback.SleepReminderObtained(...)`
- `QueryCallback.SedentaryReminderSettingsObtained(...)`
- `QueryCallback.RingWearingStatusObtained(...)`
- `QueryCallback.RealTimeHeartRateSampleObtained(...)`
- `QueryCallback.AlertFeatureSupportObtained(...)`

How they are produced:

- `ZhQueryDeviceUnitsHandler.get...()` dispatches the SDK request
- SDK callback arrives
- wrapper maps the bean
- wrapper sends `QueryCallback` into `SessionManager`
- `AlertSettingsViewModel` and `RingConnectionService` observe/use that callback

## 14. Update Callbacks Used

The wrapper converts SDK `SendCmdState` results into app-level `UpdateDeviceDataCallback` events.

Main callback objects used by alerts:

- `HeartRateAlertSettingsUpdated(success)`
- `Spo2AlertSettingsUpdated(success)`
- `HighStressAlertSettingsUpdated(success)`
- `PressureModeSettingsUpdated(success)`
- `SleepReminderUpdated(success)`
- `SedentaryDataUpdated(success)`
- `AlertFeatureSupportResolved(feature, supported)`
- `ManualMeasurementObtained(manualMeasurement)`

How they are produced:

- ViewModel or service sends `UpdateDeviceAction`
- wrapper executes the corresponding SDK setter
- `SendCmdStateListener.onState(...)` returns
- wrapper emits success/failure callback
- on `SUCCEED`, wrapper also resolves support as `true`
- on `NOT_SUPPORT`, wrapper resolves support as `false`

## 15. Authoritative Query/Write Flow Per Feature

### 15.1 Heart-rate alert

Read flow:

- `QueryAction.GetHeartRateAlertSettings`
- `ZhQueryDeviceUnitsHandler.getHeartRateAlertSettings()`
- screenless path uses `getSWHRMonitor(...)`
- callback `onSWHRMonitor(...)`
- mapped with `AlertSettingsMapper.fromScreenlessHeartRateMonitor(...)`
- emitted as `QueryCallback.HeartRateAlertSettingsObtained(...)`

Write flow:

- `UpdateDeviceAction.SetHeartRateAlertSettings(...)`
- `ZhUpdateDeviceUnitsHandler.setHeartRateAlertSettings(...)`
- screenless path uses `setSWHRMonitor(...)`
- result returned as `HeartRateAlertSettingsUpdated(success)`
- support resolved with `AlertFeatureSupportResolved(HEART_RATE, true/false)`

Mirror sample flow:

- realtime callback `RealTimeHeartRateCallback.onDataResult(...)`
- wrapper emits `QueryCallback.RealTimeHeartRateSampleObtained(...)`
- service calls `processHeartRateSample(...)`
- threshold engine evaluates:
  - high threshold for resting alert
  - low threshold for low HR alert
- if triggered, service sends app notification to the band and records `AlertEvent`

### 15.2 SpO2 alert

Read flow:

- `QueryAction.GetSpo2AlertSettings`
- wrapper calls `getSWSPO2Monitor(...)`
- callback `onSWSPO2Monitor(...)`
- emitted as `QueryCallback.Spo2AlertSettingsObtained(...)`

Write flow:

- `UpdateDeviceAction.SetSpo2AlertSettings(...)`
- wrapper calls `setSWSPO2Monitor(...)`
- result returned as `Spo2AlertSettingsUpdated(success)`

Mirror sample flow:

- continuous/offline data is converted into `UserActivityCallback.OreoBloodOxygenObtained`
- service extracts the latest positive sample from `breakUp`
- if no recent stream sample is seen, service requests manual blood oxygen measurement every 5 minutes while enabled and connected
- `processSpo2Sample(...)` evaluates threshold crossing and repeat logic

### 15.3 High stress index alert

Read flow:

- `QueryAction.GetHighStressAlertSettings`
- wrapper calls `getSWHRVMonitor(...)`
- callback `onSWHRVMonitor(...)`
- emitted as `QueryCallback.HighStressAlertSettingsObtained(...)`

Write flow:

- `UpdateDeviceAction.SetHighStressAlertSettings(...)`
- wrapper calls `setSWHRVMonitor(...)`
- result returned as `HighStressAlertSettingsUpdated(success)`

Mirror sample flow:

- user-activity stress data arrives as `UserActivityCallback.StressDataObtainedOreo`
- service extracts the latest positive sample from `breakUp`
- if no recent stress sample is available, service requests manual HRV measurement every 5 minutes while enabled and connected
- `processHighStressSample(...)` evaluates threshold crossing and repeat logic

### 15.4 Relaxation prompt

Read flow:

- `QueryAction.GetPressureModeSettings`
- screenless path uses `getSWBRMonitor(...)`
- callback `onSWBRMonitor(...)`
- emitted as `QueryCallback.PressureModeSettingsObtained(...)`

Write flow:

- `UpdateDeviceAction.SetPressureModeSettings(...)`
- screenless path uses `setSWBRMonitor(...)`
- result returned as `PressureModeSettingsUpdated(success)`

Current note:

- relaxation prompt is configured natively on the device
- there is no separate mirrored threshold engine for this feature

### 15.5 Bedtime reminder

Read flow:

- `QueryAction.GetSleepReminder`
- wrapper calls `getSleepReminder(...)`
- emitted as `QueryCallback.SleepReminderObtained(...)`

Write flow:

- `UpdateDeviceAction.UpdateSleepReminder(...)`
- wrapper calls `setSleepReminder(...)`
- result returned as `SleepReminderUpdated(success)`

Mirror schedule flow:

- service checks current local time against stored bedtime
- `AlertMirrorEvaluator.isBedtimeDue(...)` prevents duplicates in the same minute
- when due, service sends a mirrored notification and records an `AlertEvent`

### 15.6 Sedentary reminder

Read flow:

- `QueryAction.GetSedentaryData`
- wrapper calls `getSedentaryReminder(...)`
- emitted as `QueryCallback.SedentaryReminderSettingsObtained(...)`

Write flow:

- `UpdateDeviceAction.SetSedentaryData(...)`
- wrapper calls `setSedentaryReminder(...)`
- result returned as `SedentaryDataUpdated(success)`

Special handling:

- `AlertSettingsStateUtils.isScreenlessSedentaryReadbackAnomaly(...)`
- for `luna_band`, if the device readback returns disabled + all zero values after a valid local config, the app treats that as a known anomaly and preserves the local setting instead of wiping it

Mirror schedule flow:

- service checks time-window and interval
- `AlertMirrorEvaluator.isSedentaryDue(...)` prevents duplicate sends in the same minute
- when due, service sends a mirrored notification and records an `AlertEvent`

### 15.7 Wear status

Direct query flow:

- user taps Refresh
- ViewModel sends `QueryAction.GetRingWearingStatus`
- wrapper calls `getRingWearingStatus(...)`
- callback `MicroCallBack.onRingWearingStatus(status)`
- wrapper emits `QueryCallback.RingWearingStatusObtained(...)`

Fused inference flow:

- positive HR / SpO2 / HRV samples count as recent wear evidence
- manual measurement with `errorReason == 0x01` or `isWrist == false` marks not worn
- direct `status == 1` is trusted immediately
- direct `status == 0` does not override fresh positive sensor evidence
- if there is no recent evidence for 5 minutes, passive status falls back to not worn

## 16. Mirrored Alert Engine

The repeated-alert behavior is implemented in `AlertMirrorSupport.kt` and executed from `RingConnectionService`.

### 16.1 Core types

- `MirrorCondition`
- `MirrorConditionState`
- `MirrorDecision`
- `AlertMirrorEvaluator`
- `WearStatusResolver`

### 16.2 Heart-rate rules

- high-HR trigger when `value >= threshold`
- low-HR trigger when `value <= threshold`
- re-arm after recovery by `5 BPM`
- repeat interval while still unsafe: `3 minutes`

### 16.3 SpO2 rules

- trigger when `value <= threshold`
- re-arm after recovery by `2 points`
- repeat interval while still unsafe: `10 minutes`

### 16.4 High stress rules

- trigger when `value >= threshold`
- re-arm after recovery by `5 points`
- repeat interval while still unsafe: `10 minutes`

### 16.5 Scheduled reminders

- bedtime reminder fires once when local time matches the configured minute
- sedentary reminder fires once when current minute falls on the configured interval inside the configured window
- duplicate sends inside the same minute are blocked by checking recent alert events

### 16.6 Dispatch path

When a decision triggers:

1. service creates an `AlertEvent`
2. event is persisted into `LocalDeviceAlertSettings.recentAlerts`
3. service builds `AppNotification(appType = ApplicationType.NOISEFIT.type, ...)`
4. service sends `UpdateDeviceAction.SendAppNotification(notification)`
5. wrapper calls `ControlBleTools.getInstance().sendAppNotification(...)`
6. service also posts the same event into `SessionManager.alertMirrorEvent`
7. UI shows banner and recent-alert feed

## 17. Recent Alerts UI

`LocalDeviceAlertSettings.recordAlertEvent(event, limit = 50)` persists the latest 50 alert events locally.

The UI:

- shows the latest 5 events in the “Recent Alerts” panel
- displays:
  - title
  - timestamp
  - message
  - observed value
  - threshold
  - source
  - band send state

Current `AlertEventSource` values:

- `MIRROR_PUSH`
- `SCHEDULE`
- `VERIFY_ONLY`

In current usage:

- threshold-driven mirrored alerts use `MIRROR_PUSH`
- bedtime/sedentary schedule sends use `SCHEDULE`

## 18. Banner Notification In The App

The app UI banner is not the same thing as the system notification sent to the band.

Banner flow:

- service posts `sessionManager.postAlertMirrorEvent(event)`
- `BlankTestFragment` observes `sessionManager.alertMirrorEvent`
- `showAlertBanner(root, event)` displays `title + message`
- banner auto-hides after 5 seconds

This gives the user a visible app-side confirmation of what was sent to the band.

## 19. Logging And Export

Logging is implemented in `AlertDebugLogger`.

Log file:

- `alert_debug.log`

Storage location:

- Android external cache on modern Android versions
- exported through a `FileProvider` URI

Main log helpers:

- `log(source, message)`
- `logValue(source, label, value)`
- `logAlertFlow(source, feature, operationId, stage, message)`
- `logAlertFlowValue(source, feature, operationId, stage, label, value)`

Alert-specific stages used by the implementation:

- `support_check`
- `local_save`
- `threshold_api`
- `verify_query`
- `background_probe`
- `mirror_sample`
- `threshold_cross`
- `cooldown_skip`
- `rearm`
- `mirror_push`
- `wear_probe`
- `wear_inference`
- `stale_response`

Export flow:

- UI button `tvExportAlertLogs`
- `BlankTestFragment.exportAlertLogs()`
- `AlertDebugLogger.getFileUri(context)`
- `ShareUtil.shareFile(context, uri)`

## 20. Support Resolution Rules

A critical part of the implementation is avoiding false “not supported” states.

Current rules:

- screenless HR support comes from `get/setSWHRMonitor`
- screenless SpO2 support comes from `get/setSWSPO2Monitor`
- screenless high stress support comes from `get/setSWHRVMonitor`
- screenless relaxation support comes from `get/setSWBRMonitor`
- bedtime reminder is not disabled from query-only `NOT_SUPPORT`
- wear detection on screenless devices stays optimistic if direct query returns `NOT_SUPPORT`

Non-authoritative or probe-style APIs must not disable the real alert path.

Examples:

- `getHeartRateInterval` is logged as background information only
- `getSpo2Settings` / continuous SpO2 settings query is logged as background information only

These are intentionally not used to decide whether threshold alerts are supported.

## 21. Known Constraints

Important constraints of the current design:

- all configuration is local-only and device-only; no backend sync
- mirrored repeated alerts require the phone to stay connected to the band
- firmware-native alerts may still fire independently of the mirror engine
- the app cannot detect a native firmware haptic unless the app itself mirrored and sent that alert
- workout-specific HR threshold is not exposed in the current screenless path, so it is shown but disabled
- “High stress” is implemented through the screenless HRV warning API because that is the available ZH screenless alert primitive

## 22. Short Senior-Level Summary

If this needs to be explained quickly to seniors:

- The app writes alert settings to the screenless band through ZH AAR methods like `setSWHRMonitor`, `setSWSPO2Monitor`, `setSWHRVMonitor`, `setSWBRMonitor`, `setSleepReminder`, and `setSedentaryReminder`.
- All alert settings are stored locally in `LocalDeviceAlertSettings` first, then synced to the device with pending/verify logic.
- The app verifies writes by querying the same authoritative alert APIs back again and comparing with the local value.
- Because the device is screenless, the app also runs a connected-only mirror engine in `RingConnectionService` that watches live HR/SpO2/HRV-related data, detects threshold crossings, and sends a named app notification to the band through `sendAppNotification(...)`.
- Every mirrored alert is also recorded locally and shown in the app via a recent-alert list and temporary banner.
- Wear status is not treated as a threshold alert; it is inferred from direct wear query plus fresh sensor evidence and manual-measurement results.
- No backend is involved anywhere in this flow.

## 23. Suggested Talking Track For Review

Recommended 60-second explanation:

1. The UI saves alert edits locally first and marks them pending.
2. The ViewModel and service sync those changes to the band using authoritative screenless SDK setters.
3. The same authoritative getters are used to verify the device state after a write.
4. Realtime monitoring is kept on while alerts are active so the service can mirror threshold crossings.
5. Mirrored alerts are sent as app notifications to the band, persisted locally, shown in recent alerts, and logged for debugging.
6. Wear state is inferred from both direct device queries and fresh physiological sample evidence.

