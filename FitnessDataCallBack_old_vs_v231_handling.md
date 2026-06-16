# FitnessDataCallBack Handling: Old vs v2.3.1

This document explains `FitnessDataCallBack` handling in a way you can use with a senior engineer.

It separates:

- the **old SDK contract** before `v2.3.1`
- the **new `v2.3.1` contract**
- the **current app implementation**
- the **expected production handling** for the new callbacks

This is not just an SDK summary. It is specifically about how the app handles each callback.

---

## 1. Executive summary

Before `v2.3.1`, the app already had a large `FitnessDataCallBack` implementation and most of the important health data was handled through the usual pattern:

1. sync request
2. SDK callback
3. conversion
4. app callback / local storage / server sync / UI

With `v2.3.1`, four new callbacks were added:

- `onSleepRRIData`
- `onSleepHRVData`
- `onContinuousHeartRateSportFiveMinAfter`
- `onContinuousRRIData`

Also, two already-existing continuous data beans changed:

- `ContinuousHeartRateBean` got `frequencyVersion`
- `ContinuousPressureBean` got `frequencyVersion`

So the migration introduced **two kinds of change**:

1. **new callbacks** that did not exist before
2. **new cadence semantics** for already-existing continuous HR / pressure callbacks

That is why the handling is partly similar and partly different.

---

## 2. Old SDK contract before v2.3.1

I checked the actual `v2.3.0` AAR.

### 2.1 Callbacks that existed before v2.3.1

Pre-`v2.3.1`, `FitnessDataCallBack` already had:

- `onProgress`
- `onDailyData`
- `onSleepData`
- `onContinuousHeartRateData`
- `onOfflineHeartRateData`
- `onContinuousBloodOxygenData`
- `onOfflineBloodOxygenData`
- `onContinuousPressureData`
- `onOfflinePressureData`
- `onContinuousTemperatureData`
- `onOfflineTemperatureData`
- `onEffectiveStandingData`
- `onActivityDurationData`
- `onOffEcgData`
- `onExaminationData`
- `onRingTodayActiveTypeData`
- `onRingOverallDayMovementData`
- `onRingTodayRespiratoryRateData`
- `onRingHealthScore`
- `onRingSleepResult`
- `onRingSleepNAP`
- `onRingAutoActiveSportData`
- `onRingBodyBatteryData`
- `onRingStressDetectionData`
- `onRingBatteryData`
- `onDrinkWaterData`

### 2.2 What did NOT exist before v2.3.1

Pre-`v2.3.1`, these did not exist:

- `onSleepRRIData`
- `onSleepHRVData`
- `onContinuousHeartRateSportFiveMinAfter`
- `onContinuousRRIData`

So these are true SDK contract additions.

### 2.3 Old sync API before v2.3.1

Before `v2.3.1`, the SDK exposed only:

- `getDailyHistoryData(listener)`

There was no mode-based overload yet.

So old app handling was based on one generic daily-history request path.

---

## 3. New SDK contract in v2.3.1

I checked the actual `v2.3.1` AAR too.

### 3.1 New callbacks added

`v2.3.1` adds:

- `onSleepRRIData(SleepRRIBean)`
- `onSleepHRVData(SleepHRVBean)`
- `onContinuousHeartRateSportFiveMinAfter(ContinuousHeartRateSportFiveMinAfterBean)`
- `onContinuousRRIData(ContinuousRRIDBean)`

### 3.2 New sync API added

`v2.3.1` adds:

- `getDailyHistoryData(int mode, listener)`

Modes:

- `1` = today
- `2` = history
- `3` = all

### 3.3 Existing bean changes

`ContinuousHeartRateBean`

- old: `continuousHeartRateFrequency`
- new: `continuousHeartRateFrequency + frequencyVersion`

`ContinuousPressureBean`

- old: `pressureFrequency`
- new: `pressureFrequency + frequencyVersion`

Meaning used by the app:

- `frequencyVersion = 0` -> frequency in minutes
- `frequencyVersion = 1` -> frequency in seconds

This is important because the old handling assumed minute-based cadence.

---

## 4. Old app handling before v2.3.1

This section explains the conceptual behavior before `v2.3.1`.

## 4.1 Main pattern

Before `v2.3.1`, the important health callbacks were already handled in the app through:

- `ZhUserActivityHandler`
- `DataConverter`
- `OreoDataConverter`

The app already had device-specific behavior:

- old/general path for legacy devices
- Oreo/Luna-specific path for Luna/ring family devices

## 4.2 Continuous HR and pressure assumptions before v2.3.1

The old converter behavior assumed:

- `continuousHeartRateFrequency` meant minute interval
- `pressureFrequency` meant minute interval

For example, old heart-rate handling literally advanced timestamps by:

- `addMinuteToTimeStamp(..., continuousHeartRateFrequency)`

and old pressure handling advanced timestamps by:

- `addMinuteToTimeStamp(..., pressureFrequency)`

So before `v2.3.1`, the app effectively assumed:

- continuous HR cadence = minute-based
- continuous pressure cadence = minute-based

There was no `frequencyVersion` to interpret.

## 4.3 New payloads simply did not exist before

Before `v2.3.1`, the app had no need to handle:

- sleep RRI
- sleep HRV
- post-workout 5-minute HR
- continuous RRI

because the SDK contract did not expose them.

---

## 5. Current app handling after v2.3.1

After `v2.3.1`, the app updated `ZhUserActivityHandler` to compile against the new interface and then chose a safe rollout strategy:

- productionize only what fits the existing pipeline
- raw-capture the new unsupported payloads for validation

This is the key design decision.

---

## 6. Callback-by-callback handling

This section is the most important one.

For each callback, I explain:

- old handling
- current `v2.3.1` handling
- expected handling going forward

---

## 6.1 Progress and sync envelope

### `onProgress(int progress, int total)`

### Old handling

- update sync status callbacks
- send app logs
- on completion, call delete-daily-data on device

### Current handling

- same core behavior
- still used to reflect sync start / progress / completion

### Expected handling

- unchanged
- this is the transport status callback, not a data-model callback

---

## 6.2 Daily data

### `onDailyData(DailyBean)`

### Old handling

- daily steps/distance/calorie payload parsed into app step models
- routed differently for Luna/Oreo devices vs legacy devices

### Current handling

- still routed into the step-data pipeline
- for Luna/Oreo devices, uses `OreoDataConverter.parseStepsData(...)`
- calorie mapping is more careful, preferring:
  - `todayCalorieData`
  - `calorieData`
  - only then Oura calorie fallback fields

### Expected handling

- productionized already
- remains part of normal sync flow

### Important difference vs old

- daily callback itself is not new
- but the field interpretation in the converter was improved for `v2.3.1`

---

## 6.3 Sleep data

### `onSleepData(SleepBean)`

### Old handling

- parsed into app sleep model
- Luna/Oreo devices used Oreo converter path
- older logic implicitly trusted payload units more directly

### Current handling

- still productionized
- now sleep conversion is more defensive
- app detects whether sleep durations are minute-based legacy values or second-based values
- if needed, it normalizes durations into seconds

### Expected handling

- productionized already
- remains normal sync-driven behavior

### Important difference vs old

- callback is old
- handling is stronger because unit interpretation is now more defensive

---

## 6.4 Continuous heart rate

### `onContinuousHeartRateData(ContinuousHeartRateBean)`

### Old handling

- existing callback
- old converter treated cadence as minute-based
- values flowed into existing heart-rate history pipeline

### Current handling

- raw JSON is also captured to debug store
- production Luna/Oreo path still forwards to app heart-rate pipeline
- but now the app checks:
  - `continuousHeartRateFrequency`
  - `frequencyVersion`
- if the source data is faster than legacy 5-minute cadence, it normalizes it into 5-minute buckets before storing/using it in the old pipeline

### Expected handling

- productionized
- fetch style remains similar to old auto-sync
- the new thing is cadence normalization

### Key old vs new difference

Old:

- “frequency means minutes”

New:

- “frequency meaning depends on `frequencyVersion`, then normalize if needed”

---

## 6.5 Offline heart rate

### `onOfflineHeartRateData(OfflineHeartRateBean)`

### Old handling

- callback existed
- not meaningfully forwarded in the current app’s main Luna handler path

### Current handling

- still effectively only logged in `ZhUserActivityHandler`
- no production forwarding from this specific method in the current code shown

### Expected handling

- remains secondary / not the main Luna continuous HR path

---

## 6.6 Continuous blood oxygen

### `onContinuousBloodOxygenData(ContinuousBloodOxygenBean)`

### Old handling

- existing callback
- for Luna/Oreo devices, converted and forwarded into Oreo blood oxygen pipeline

### Current handling

- same general pattern
- productionized for relevant devices

### Expected handling

- unchanged in architecture

---

## 6.7 Offline blood oxygen

### `onOfflineBloodOxygenData(OfflineBloodOxygenBean)`

### Old handling

- existing callback
- parsed into legacy blood oxygen data flow

### Current handling

- still forwarded into the older/legacy blood oxygen path

### Expected handling

- unchanged in concept

---

## 6.8 Continuous pressure / HRV-like ring stress data

### `onContinuousPressureData(ContinuousPressureBean)`

### Old handling

- existing callback
- old converter assumed `pressureFrequency` was minute-based
- data entered stress/HRV-like pipeline directly

### Current handling

- raw JSON is also stored for debug
- Luna/Oreo path still productionizes it
- now the app reads:
  - `pressureFrequency`
  - `frequencyVersion`
- and normalizes higher-frequency data back into legacy 5-minute breakup shape if needed

### Expected handling

- productionized
- same sync-driven transport as before
- new handling is cadence-aware

### Key old vs new difference

Old:

- timestamp spacing assumed minute-based

New:

- frequency unit is interpreted from `frequencyVersion`
- fast cadence can be bucketed back into legacy 5-minute data

---

## 6.9 Offline pressure

### `onOfflinePressureData(OfflinePressureDataBean)`

### Old handling

- existing callback
- parsed into legacy stress data flow

### Current handling

- still forwarded to legacy `DataConverter.parseStressData(...)`

### Expected handling

- unchanged as secondary/legacy path

---

## 6.10 Continuous temperature

### `onContinuousTemperatureData(ContinuousTemperatureBean)`

### Old handling

- existing callback
- for Luna/Oreo devices, used as the main temperature path

### Current handling

- still productionized for Luna/Oreo devices

### Expected handling

- unchanged in architecture

---

## 6.11 Offline temperature

### `onOfflineTemperatureData(OfflineTemperatureDataBean)`

### Old handling

- existing callback
- used for non-Luna/non-Oreo devices

### Current handling

- still handled that way
- for Luna/Oreo devices, continuous temperature is the relevant path instead

### Expected handling

- unchanged

---

## 6.12 Effective standing / activity duration / ECG / examination

Callbacks:

- `onEffectiveStandingData`
- `onActivityDurationData`
- `onOffEcgData`
- `onExaminationData`

### Old handling

- callbacks existed
- current app implementation did little or nothing meaningful with them

### Current handling

- still effectively no-op in `ZhUserActivityHandler`

### Expected handling

- only relevant if product requirements later choose to expose them

---

## 6.13 Ring activity type / all-day movement / respiratory / health score

Callbacks:

- `onRingTodayActiveTypeData`
- `onRingOverallDayMovementData`
- `onRingTodayRespiratoryRateData`
- `onRingHealthScore`

### Old handling

- ring-specific callbacks already existed
- some were debug/log only
- some were productionized into Oreo/ring-specific models

### Current handling

- `onRingTodayActiveTypeData` -> mostly logs only
- `onRingOverallDayMovementData` -> forwarded into Oreo day-time movement model
- `onRingTodayRespiratoryRateData` -> raw save + forwarded to respiratory data model
- `onRingHealthScore` -> forwarded into health score app callback

### Expected handling

- same conceptual behavior

---

## 6.14 Ring sleep result / naps / auto active sport / body battery / stress detection

Callbacks:

- `onRingSleepResult`
- `onRingSleepNAP`
- `onRingAutoActiveSportData`
- `onRingBodyBatteryData`
- `onRingStressDetectionData`

### Old handling

- already part of ring/Oreo-specific flow before `v2.3.1`

### Current handling

- `onRingSleepResult` -> parsed and forwarded to Oreo sleep model
- `onRingSleepNAP` -> parsed and forwarded to nap model
- `onRingAutoActiveSportData` -> raw save + converted to auto sport model
- `onRingBodyBatteryData` -> raw save only, mainly for debug/UI
- `onRingStressDetectionData` -> raw save + forwarded to body stress model

### Expected handling

- unchanged in architecture

---

## 6.15 Ring battery and drink water

Callbacks:

- `onRingBatteryData`
- `onDrinkWaterData`

### Old handling

- callbacks existed
- current app handler did not implement meaningful forwarding here

### Current handling

- still no-op in `ZhUserActivityHandler`

### Expected handling

- only if product decides to expose them directly through this path

---

## 6.16 New in v2.3.1: Sleep RRI

### `onSleepRRIData(SleepRRIBean)`

### Old handling

- did not exist

### Current handling

- raw JSON is saved into `WatchDataStore`
- visible in `BlankTestFragment` and raw bottom sheet
- not yet mapped into production DB/server/UI flow

### Expected handling

If productionized later, it would need:

- data model design
- local storage design
- server contract decision
- feature/UI decision

So right now:

- **captured**
- **debug-visible**
- **not productionized**

---

## 6.17 New in v2.3.1: Sleep HRV

### `onSleepHRVData(SleepHRVBean)`

### Old handling

- did not exist

### Current handling

- raw JSON saved
- debug/test UI visible
- no production mapping yet

### Expected handling

- same as sleep RRI: would need explicit product/data-contract rollout

---

## 6.18 New in v2.3.1: Post-workout HR after 5 minutes

### `onContinuousHeartRateSportFiveMinAfter(ContinuousHeartRateSportFiveMinAfterBean)`

### Old handling

- did not exist

### Current handling

- raw JSON saved only
- debug/test UI only
- validity is based on nested `hrList[*].heartRate` samples, not just outer segment wrappers
- not converted into production entity / DB / upload flow

### Important behavioral meaning

This callback means:

- heart-rate recovery data for the period 5 minutes after exercise

But in the current app:

- it is still received through sync callback flow
- not through a special separate live workout-end fetch API

### Expected handling

If productionized later, the fetch style would still most likely remain sync-driven unless the SDK adds a dedicated API.

Downstream work would still be needed for:

- entity design
- repository path
- upload contract
- workout details or recovery UI

---

## 6.19 New in v2.3.1: Continuous RRI

### `onContinuousRRIData(ContinuousRRIDBean)`

### Old handling

- did not exist

### Current handling

- raw JSON saved only
- debug/test UI visible
- no production converter/storage contract yet

### Expected handling

- would require separate production rollout

---

## 7. Old handling vs new handling in one table

| Callback category | Old SDK availability | Old app handling | Current v2.3.1 handling | Productionized now? |
|---|---|---|---|---|
| Progress | Yes | Sync state updates | Same | Yes |
| Daily | Yes | Steps/day aggregates | Same, better calorie mapping | Yes |
| Sleep | Yes | Productionized | Same, better unit normalization | Yes |
| Continuous HR | Yes | Minute-based assumption | Frequency-aware normalization | Yes |
| Offline HR | Yes | Minimal | Minimal | No/mainly not used |
| Continuous blood oxygen | Yes | Productionized | Same | Yes |
| Offline blood oxygen | Yes | Productionized legacy path | Same | Yes |
| Continuous pressure | Yes | Minute-based assumption | Frequency-aware normalization | Yes |
| Offline pressure | Yes | Legacy path | Same | Yes/legacy |
| Continuous temperature | Yes | Productionized for Luna/Oreo | Same | Yes |
| Offline temperature | Yes | Legacy path | Same | Yes/legacy |
| Effective standing / activity duration / ECG / examination | Yes | Mostly unused | Mostly unused | No |
| Ring movement / respiratory / health score / sleep / nap / auto sport / body battery / ring stress | Yes | Ring-specific | Same | Mixed |
| Sleep RRI | No | N/A | Raw capture only | No |
| Sleep HRV | No | N/A | Raw capture only | No |
| Post-workout HR after 5 min | No | N/A | Raw capture only | No |
| Continuous RRI | No | N/A | Raw capture only | No |

---

## 8. The biggest old vs new differences

If you need the shortest technical comparison, it is this:

### Old world

- one daily-history request path
- continuous HR/pressure assumed minute-based cadence
- no sleep RRI / sleep HRV / post-workout HR / continuous RRI callbacks

### New `v2.3.1` world

- mode-based daily-history request exists
- continuous HR/pressure can now be second-based via `frequencyVersion`
- four new callbacks exist
- the app productionized only the parts compatible with existing contracts
- the newly added unsupported payloads are currently raw-captured and debug-visible only

---

## 9. What “expected handling” means for the new callbacks

When you say “new expected handling,” the clean engineering answer is:

For the new callbacks, the expected mature production handling would be the same broad pattern as the older productionized callbacks:

1. callback received
2. validate payload
3. convert into stable app model
4. persist locally if needed
5. include in server contract if needed
6. expose in production UI if product wants it

But today, only step 1 and debug visibility are implemented for:

- sleep RRI
- sleep HRV
- post-workout HR after 5 min
- continuous RRI

So they are **SDK-compatible and testable**, but **not fully productized yet**.

---

## 10. Best verbal explanation

If you want to explain this to a senior engineer in a clear way, say:

Before `v2.3.1`, `FitnessDataCallBack` already powered most of the app’s sync-delivered health data, and the main assumptions were that continuous heart-rate and pressure cadence were minute-based and that all important payloads fit the existing app models. In `v2.3.1`, the SDK added four brand-new callbacks and also added `frequencyVersion` to existing continuous HR and pressure beans. So the migration had two parts: first, keep old productionized callbacks working by making HR and pressure cadence-aware and normalizing higher-frequency data back into the legacy 5-minute shape; second, add compatibility for the new callbacks and expose them safely in debug/raw capture until DB/API/UI contracts are ready.

---

## 11. Final takeaway

The correct high-level statement is:

**Old handling was built around the earlier callback set and minute-based continuous data assumptions. New `v2.3.1` handling keeps the old productionized callbacks working, upgrades HR/pressure to frequency-aware normalization, and adds raw/debug handling for the four brand-new callbacks that are not yet fully productionized.**
