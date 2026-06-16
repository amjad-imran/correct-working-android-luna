# SDK v2.3.1 Continuous Metrics and Stress Graph Diagnosis

## Scope

This document explains the current implementation and runtime behavior of the SDK v2.3.1 continuous callbacks that were added or changed for the Luna / ring integration:

- `onContinuousHeartRateData`
- `onContinuousPressureData`
- `onContinuousRRIData`
- `onRingStressDetectionData`
- `onRingBodyBatteryData`

It also explains exactly how the home-page HR and stress graphs handle:

- different callback frequencies
- bad data such as `0` and `255`
- repeated syncs that send partial or zero-filled payloads
- the difference between raw callback data and what is finally shown on the graph

Finally, it includes a log-verified diagnosis for why Amjad’s stress graph never populated on April 15, 2026, using:

- the SDK v2.3.1 document
- the app implementation
- the extracted PDF logs for Amjad and Jagathees

## Evidence Used

### SDK document

- `ZH SDK 文档 2.3.1.docx`

### Logs

- `latest logs/amjad logs latest 15th night app logs .pdf`
- `latest logs/jagathees latest logs app 15th night .pdf`

The PDFs were extracted to text and then correlated against the code.

### Main code paths inspected

- `noisefit_zh_sdk/.../ZhUserActivityHandler.kt`
- `noisefit_zh_sdk/.../OreoDataConverter.kt`
- `app/.../OreoStressDataImpl.kt`
- `app/.../OreoHeartRateDataImpl.kt`
- `app/.../OreoOfflineDataMapper.kt`
- `app/.../OreoStressDataConvertor.kt`
- `app/.../OreoHRDataConvertor.kt`
- `app/.../OreoUserActivityRepositoryImpl.kt`
- `app/.../SummaryDataViewModelToday.kt`
- `app/.../BlankTestFragment.kt`
- `app/.../RawSdkPayloadBottomSheet.kt`

## Executive Findings

1. The home stress graph is driven by `onContinuousPressureData`, not by `onRingStressDetectionData`.
2. In SDK v2.3.1, `onContinuousPressureData` is documented as the continuous pressure callback, but for the ring project it is explicitly described as HRV data.
3. The app normalizes both continuous HR and continuous pressure into 5-minute local storage before the home graphs use them.
4. The home HR card then converts 5-minute HR data into 30-minute graph bars.
5. The home stress card converts 5-minute stress/HRV data into 15-minute graph points.
6. `0` is treated as invalid by both the SDK documentation and the app logic. `255` is also treated as invalid in app graph code.
7. For Amjad, the stress graph did not populate because the synced stress source data for April 15, 2026 was zero-only, not because the app incorrectly discarded valid stress values.
8. The strongest likely upstream cause is missing valid sensor-derived stress measurements on the device side, supported by additional log signals such as `isWrist=false`, all-zero ring stress, all-zero respiratory data, health score `0`, and nearly empty body-battery data.
9. There is a log gap between `2026-04-15 06:53` and `2026-04-15 18:30` in the extracted Amjad log. So the exact mid-day device state cannot be proven minute-by-minute. However, the full-day stress payload retrieved at `18:37`, `18:38`, and `18:40` still contains no valid stress values for the day, which is enough to explain the empty graph since at least 11:30 AM.

## 1. SDK v2.3.1 Raw Callback Structures

## 1.1 Fixed bean shape vs variable list length

The bean structure is fixed by the SDK. The important part is:

- the field names are fixed
- the date field is fixed via `BaseBean.date`
- the actual data arrays are `List<Integer>`
- the array length is not hardcoded by the app

That means:

- the schema is fixed
- the number of samples is not assumed blindly
- the app derives timing mainly from `frequency`, `frequencyVersion`, and the actual list contents

So the correct mental model is:

- fixed object structure
- variable sample count
- frequency-driven interpretation

## 1.2 Continuous heart rate

SDK bean:

- `ContinuousHeartRateBean`

Fields from the SDK doc:

- `continuousHeartRateFrequency`
- `frequencyVersion`
- `heartRateData`
- `max`
- `min`
- `restingRate`
- `heartRateHourMaxValue`
- `heartRateHourMinValue`
- inherited `date`

SDK semantics:

- `frequencyVersion = 0` means frequency is in minutes
- `frequencyVersion = 1` means frequency is in seconds
- `0` in `heartRateData` means invalid value

Observed real logs:

- both users show `continuousHeartRateFrequency=30`
- both users show `frequencyVersion=1`
- effective raw cadence is therefore `30 seconds`

Current practical meaning:

- the callback shape is fixed
- the heart-rate list is raw time-series data sampled every 30 seconds in current logs
- app code converts this to 5-minute storage immediately

## 1.3 Continuous pressure

SDK bean:

- `ContinuousPressureBean`

Fields from the SDK doc:

- `pressureFrequency`
- `frequencyVersion`
- `pressureData`
- `pressureDataMaxValue`
- `pressureDataMinValue`
- `rriData`
- inherited `date`

SDK semantics:

- `frequencyVersion = 0` means minutes
- `frequencyVersion = 1` means seconds
- `0` in `pressureData` means invalid value
- for the ring project, this callback is explicitly documented as HRV data

Observed real logs:

- both users show `pressureFrequency=30`
- both users show `frequencyVersion=1`
- effective raw cadence is therefore `30 seconds`

Important runtime observation:

- `rriData` exists in the SDK bean for v2.3.1
- but in both inspected `onContinuousPressureData` bean logs, `rriData=null`
- separate `onContinuousRRIData` logs exist and do carry real RRI values

This means the correct implementation assumption today is:

- do not assume `ContinuousPressureBean.rriData` will be populated
- if you need RRI, inspect `onContinuousRRIData` separately

Also important:

- Jagathees has a clearly populated `pressureData` series
- but `pressureDataMaxValue` and `pressureDataMinValue` are still `0`

So:

- `pressureDataMaxValue` and `pressureDataMinValue` are not reliable enough to decide whether data is valid
- validity must be determined from `pressureData` itself

## 1.4 Continuous RRI

SDK bean:

- `ContinuousRRIDBean`

Fields from SDK doc:

- `frequency`
- `rri`
- `frequencyVersion`
- inherited `date`

Observed logs:

- both users use `frequency=30`
- both users use `frequencyVersion=1`
- effective raw cadence is `30 seconds`

Current app behavior:

- raw data is saved for debugging
- it is not used by the production home stress graph

This is important because people may expect:

- continuous pressure callback and RRI callback to be interchangeable

They are not interchangeable in the current app.

## 1.5 Ring body battery

SDK bean:

- `RingBodyBatteryBean`

Fields from SDK doc:

- `bodyBatteryFrequency`
- `data`
- inherited `date`

Observed logs:

- `bodyBatteryFrequency=15`
- arrays are day-level ring body-battery arrays

The SDK bean does not expose `frequencyVersion`.

Current app interpretation:

- the current code treats this as a 15-minute cadence source
- the debug-only body-battery chart also assumes 15-minute slots

## 1.6 Ring stress detection

SDK bean:

- `RingStressDetectionBean`

Fields from SDK doc:

- `stressFrequency`
- `data`
- inherited `date`

Observed logs:

- `stressFrequency=15`

Current app interpretation:

- this is a 15-minute ring body-stress style series
- it is stored separately from the main stress/HRV graph path

## 2. What Each Callback Actually Feeds in the App

## 2.1 Continuous heart rate production flow

Current flow:

1. `ZhUserActivityHandler.onContinuousHeartRateData(...)`
2. raw JSON is saved to `watchDataStore.testSaveRawContinuousHeartRateJson(...)`
3. `oreoDataConverter.parseHeartRateData(...)` normalizes to 5-minute data
4. result is emitted as `UserActivityCallback.HeartHistoryObtainedOreo(...)`
5. local DB path stores `OreoHeartRate.breakUp`
6. repository builds the HR overview model
7. home card converts that 5-minute data into 30-minute graph bars

Key point:

- the home HR graph is not reading the raw 30-second list directly

## 2.2 Continuous pressure production flow

Current flow:

1. `ZhUserActivityHandler.onContinuousPressureData(...)`
2. raw JSON is saved to `watchDataStore.testSaveRawContinuousPressureJson(...)`
3. `oreoDataConverter.parseStressData(...)` normalizes to 5-minute data
4. result is emitted as `UserActivityCallback.StressDataObtainedOreo(...)`
5. local DB path stores `OreoStressDataBreakup.breakUp`
6. repository builds the stress overview model
7. home card converts that 5-minute stress data into 15-minute graph points

Key point:

- this is the main stress graph source on the home page

## 2.3 Ring stress detection flow

Current flow:

1. `ZhUserActivityHandler.onRingStressDetectionData(...)`
2. raw JSON is saved to `watchDataStore.testSaveStressData(...)`
3. `oreoDataConverter.parseBodyStressData(...)` trims future values and stores a 15-minute breakup
4. result is emitted as `UserActivityCallback.OreoBodyStressDataObtained(...)`

Key point:

- this is a separate body-stress path
- it is not the same as the home stress graph path

## 2.4 Ring body battery flow

Current flow:

1. `ZhUserActivityHandler.onRingBodyBatteryData(...)`
2. raw JSON is saved to `watchDataStore.testSaveBodyBatteryData(...)`
3. debug/test UI can read and chart it

Key point:

- body battery is not currently wired into the main production home graph pipeline
- it is currently more of a raw/debug pipeline than a full production feature path

## 2.5 Continuous RRI flow

Current flow:

1. `ZhUserActivityHandler.onContinuousRRIData(...)`
2. raw JSON is saved to `watchDataStore.testSaveRawContinuousRriJson(...)`

Key point:

- this is not used by the production home stress card today

## 3. Frequency Handling

## 3.1 How frequency is interpreted

The key rule is:

- if `frequencyVersion == 1`, the frequency value is in seconds
- otherwise it is in minutes

So in the real logs:

- HR: `30` with version `1` = every `30 seconds`
- continuous pressure: `30` with version `1` = every `30 seconds`
- continuous RRI: `30` with version `1` = every `30 seconds`
- ring stress: `15` = treated by app as `15 minutes`
- ring body battery: `15` = treated by app as `15 minutes`

## 3.2 Continuous HR normalization

`OreoDataConverter.parseHeartRateData(...)` calls:

- `normalizeLegacyMetricBreakup(values, frequency, frequencyVersion, targetFrequencyMinutes = 5)`

That means current continuous HR behavior is:

- raw 30-second values are averaged into 5-minute buckets
- each 5-minute bucket contains 10 raw samples when cadence is 30 seconds
- invalid entries are removed before averaging
- if a whole bucket has no valid values, the stored bucket becomes `0`

Result:

- local HR storage is effectively 288 five-minute slots per full day

## 3.3 Continuous pressure normalization

`OreoDataConverter.parseStressData(...)` uses the same normalization helper with target `5 minutes`.

That means:

- raw 30-second pressure/HRV values are averaged into 5-minute buckets
- invalid entries are ignored
- if a whole bucket has no valid values, that bucket becomes `0`

Result:

- local stress storage is also effectively 288 five-minute slots per full day

## 3.4 Ring stress and body battery cadence

`parseBodyStressData(...)` does not run the 30-second to 5-minute normalization step.

Instead it:

- assumes the bean already represents a 15-minute series
- trims future values for today
- stores that 15-minute breakup directly

Body battery debug handling follows the same 15-minute idea.

## 4. Invalid Data Handling

## 4.1 What the SDK says is invalid

The SDK document explicitly says:

- `0` means invalid value for continuous HR
- `0` means invalid value for continuous pressure
- `0` means invalid value for ring body battery
- `0` means invalid value for ring stress detection

## 4.2 What app normalization treats as invalid

For averaging during normalization:

- values `<= 0` are ignored
- value `255` is also ignored

If a normalized bucket contains only invalid values:

- the app stores `0`

## 4.3 Future values are forcibly zeroed for today

For same-day payloads, the app trims any buckets beyond the current time:

- continuous pressure after normalization
- ring stress
- debug body-battery chart path

So future slots are always zero for today.

This is correct and intentional.

## 4.4 Repeated sync merge behavior

`OreoHeartRateDataImpl` and `OreoStressDataImpl` merge multiple same-day syncs like this:

- if the new slot is `0`, keep the previous slot
- if the new slot is non-zero, overwrite the previous slot

This is important because it means:

- later syncs will not erase already-stored valid values with zeros
- but if no sync ever provides a non-zero value for that slot, the slot remains zero

So for Amjad:

- if any earlier valid stress slots had existed locally, a later zero-only sync would not have wiped them out
- the fact that the graph still never populated strongly supports that valid stress slots were never produced locally for that day

## 5. Home Graph Handling

## 5.1 Home HR graph

The home HR graph is built from `convertHeartRateOverviewData(...)` and then `OreoHRDataConvertor.getHrCombinedData(...)`.

Important behavior:

- local stored HR data is treated as 5-minute breakup data
- it is chunked into groups of 6 samples
- each group therefore becomes a 30-minute graph bar
- each bar stores:
  - min non-zero HR in that 30-minute block
  - max non-zero HR in that 30-minute block
  - mid value calculated as `(min + max) / 2`

Handling of bad data:

- `0` is ignored for min/max calculations
- if only one non-zero value exists, min/max are collapsed to that value
- if an entire 30-minute block has no valid HR, the bar becomes zero

Practical result:

- home HR graph = 48 half-hour bars across the day

## 5.2 Home stress graph

The home stress graph is built from:

- `convertStressOverviewData(...)`
- `mapStressBreakupToHomeSlots(...)`
- `OreoStressDataConvertor.getStressCombinedData(...)`

Important behavior:

- the source local breakup is expected to be the 5-minute stress data from `onContinuousPressureData`
- the mapper converts it to 96 home slots
- 288 five-minute points become 96 fifteen-minute graph points by chunking into groups of 3
- each 15-minute graph point is the average of valid values in that 3-sample window

Handling of bad data:

- `255` is converted to `0`
- only values `> 0` are considered valid
- if all values in a 15-minute window are invalid, the graph point becomes `0`
- if the final 96-point list has no valid values at all, the stress graph is considered empty

Practical result:

- home stress graph = 96 fifteen-minute points across the day

## 5.3 Frequency mismatch handling in the stress mapper

`mapStressBreakupToHomeSlots(...)` is intentionally tolerant:

- if size is already `96`, use as-is
- if size is larger and divisible by `96`, downsample by averaging chunks
- if size is smaller and `96` is divisible by that size, repeat values
- otherwise use the available data, then pad or truncate to `96`

This means the stress graph code is more frequency-flexible than the HR card code.

## 5.4 Server fallback behavior

If the local today stress data has no valid values, the repository can fall back to cached server health data:

- stress fallback maps `healthData.stress.breakUp` to 96 slots
- HR fallback uses `healthData.heart.break_up`

This means a graph can still show if:

- local sync is empty
- but cached server data for that day contains valid data

For the Amjad diagnosis, the important point is:

- the local callback payload itself is already zero-only
- no evidence in the provided logs suggests a valid fallback source for that same day

## 6. Runtime Correlation from Logs

## 6.1 Jagathees as the healthy reference case

Key log points on `2026-04-14`:

- `22:47:28.902` `onContinuousPressureData`
- `22:47:32.625` `onRingBodyBatteryData`
- `22:47:33.287` `onRingStressDetectionData`
- `22:47:35+` `onContinuousRRIData`

Observed facts:

1. `ContinuousPressureBean.pressureData` contains a long valid non-zero region.
2. `RingStressDetectionBean.data` contains many valid non-zero values, mostly in the `70-95` range.
3. `RingBodyBatteryBean.data` contains many valid non-zero values.
4. `ContinuousRRIDBean.rri` also contains many non-zero values.
5. `ContinuousPressureBean.rriData` is still `null`.
6. `ContinuousPressureBean.pressureDataMaxValue` and `pressureDataMinValue` are still `0` even though the array is valid.

What this proves:

- the app and SDK are capable of delivering valid stress-related data
- the absence of stress graph data in Amjad is not explained by a generic app regression alone
- the pressure-bean max/min fields cannot be trusted as the main validity signal

## 6.2 Amjad callback evidence

Key log points on `2026-04-15`:

- `18:35:33` manual measurement result shows `isWrist=false`, `errorReason=2`, `measureValue=0`
- `18:37:26` `onContinuousHeartRateData`
- `18:37:28` `onContinuousPressureData`
- `18:37:32` `onRingBodyBatteryData`
- `18:37:32` `onRingStressDetectionData`
- `18:37:34` `onContinuousRRIData`
- the same full sync pattern repeats at `18:38:52-18:39:02`
- and again at `18:40:54-18:41:00`

Observed facts:

1. `ContinuousHeartRateBean` is not empty. It contains valid HR values and reports `max=108`, `min=74`.
2. `ContinuousPressureBean.pressureData` is all zero for the day.
3. `ContinuousPressureBean.rriData=null`.
4. `RingStressDetectionBean.data` is all zero for the day.
5. `RingBodyBatteryBean.data` is almost entirely zero and contains only one isolated non-zero sample (`70`).
6. `TodayRespiratoryRateData.data` is all zero.
7. `RingHealthScoreBean.healthScore=0`.
8. `OverallDayMovementData.data` is all `255`.
9. `onContinuousRRIData.rri` is also all zero.

What this proves:

- HR collection worked at least partially
- stress-related collection did not produce usable values
- the problem is not “the graph rejected good stress data”
- the input stress data itself is zero-only

## 6.3 About the time window from 11:30 AM onward

The extracted Amjad log has recorded timestamps on April 15 at:

- `00`
- `01`
- `02`
- `03`
- `04`
- `05`
- `06`
- then nothing until `18`

So the exact app/device state between `06:53` and `18:30` is not directly visible in the extracted log.

However, the sync at `18:37`, `18:38`, and `18:40` pulls the day payload for `date='2026-04-15 00:00:00'`.

That full-day payload still shows:

- all-zero continuous pressure
- all-zero ring stress
- all-zero continuous RRI

Therefore the empty graph since `11:30 AM` is fully consistent with the actual synced source data for the same day.

## 7. Verified Diagnosis for Amjad’s Empty Stress Graph

## 7.1 Proven root cause

The home stress graph did not populate because the app never received valid same-day stress source values from the production stress path:

- `onContinuousPressureData.pressureData` was zero-only
- after app normalization, that remains zero-only
- after DB merge, it remains zero-only
- after home-slot mapping, it remains zero-only
- the home stress card therefore has no valid graph points to render

This is the primary root cause and it is proven by both code and logs.

## 7.2 Why this is not an app-side graphing bug

If the graph code were the main problem, we would expect at least one of these:

- non-zero `pressureData` in raw logs but empty graph
- non-zero stored breakup but empty mapped home slots
- merge logic overwriting good data with zeros

That does not match the evidence.

Instead:

- the raw pressure series is already zero-only
- the DB merge logic would preserve earlier non-zero values if they existed
- Jagathees proves the same callback path can carry real values

So the empty graph is not caused by the home graph algorithm rejecting valid Amjad stress data.

## 7.3 Most likely upstream reason

The most likely upstream reason is lack of valid sensor-derived stress measurement for that user/day/session.

This is not proven as a single specific hardware fault, but it is strongly supported by:

- manual measurement result with `isWrist=false`
- zero-only continuous pressure
- zero-only continuous RRI
- zero-only ring stress
- almost empty body battery
- zero respiratory data
- health score `0`
- movement series full of `255`

Taken together, the logs look like:

- the device was connected and able to sync some data
- heart rate had at least limited success
- the stress/HRV-related ring metrics never achieved a valid measured state for that day

## 7.4 What cannot be claimed with certainty

The provided evidence does not let us prove exactly which one of the following happened:

- ring worn loosely
- ring not worn long enough
- firmware/device-side stress feature disabled or not ready
- sensor quality blocked by motion / contact / skin conditions
- upstream SDK/firmware bug producing empty stress data for that firmware + device state combination

So the correct statement is:

- the app-side root cause is missing valid stress source data
- the most likely deeper cause is upstream measurement readiness / sensor validity, not graph rendering

## 8. Important Implementation Notes for Future Work

## 8.1 The home stress card is really a pressure/HRV card

In the current implementation, the main home stress card is fed by:

- `ContinuousPressureBean`

not by:

- `RingStressDetectionBean`

That matters for debugging because a person may see ring-stress logs and assume they drive the main graph. They do not.

## 8.2 `rriData` in `ContinuousPressureBean` should not be trusted as the only RRI source

SDK v2.3.1 added `rriData` to `ContinuousPressureBean`, but the real logs show:

- `rriData=null` inside `onContinuousPressureData`
- actual RRI is delivered in the separate `onContinuousRRIData` callback

So if product or data science wants RRI-based debugging or fallback:

- the separate callback must be inspected and persisted properly

## 8.3 `pressureDataMaxValue` and `pressureDataMinValue` should not be used for validity checks

Jagathees shows:

- non-zero `pressureData`
- but `pressureDataMaxValue=0`
- and `pressureDataMinValue=0`

So validity should be computed from the actual data array, not from those fields.

## 9. Complete Fix Plan

This section is an implementation plan only. No code is changed in this document.

## 9.1 Goal of the fix

The correct goal is not:

- “show a stress graph even when the device delivered no valid stress data”

The correct goal is:

- detect exactly why the graph is empty
- preserve all usable upstream evidence
- distinguish app-side transformation failures from upstream measurement failures
- make the empty-state reason diagnosable without manual log digging

## 9.2 Fix track A: make raw metric validity explicit

Add a small diagnostic summary object for every sync of:

- callback name
- date
- frequency
- frequency version
- raw sample count
- valid sample count
- first non-zero index
- last non-zero index
- min valid value
- max valid value
- whether `rriData` is null

Store this for:

- continuous heart rate
- continuous pressure
- continuous RRI
- ring stress detection
- ring body battery

Why this is required:

- today, logs must be read manually to discover that the source array is zero-only
- this should be machine-detectable in-app

## 9.3 Fix track B: preserve source provenance in storage

Extend stress storage or adjacent metadata so each stored day can record:

- source callback used
- source frequency
- frequency version
- raw length before normalization
- valid-count before normalization
- valid-count after normalization
- sync time

Why this is required:

- without provenance, it is hard to prove whether an empty graph came from raw zeros or from bad transformation

## 9.4 Fix track C: improve empty-state diagnosis on the home stress card

When the graph has no valid values, show a classified reason instead of a generic empty graph.

Suggested classifications:

- no stress data synced yet
- stress data synced but all samples invalid
- device not worn / low-contact suspected
- source callback missing but other health callbacks present
- source cadence unsupported or malformed

The classification should be based on:

- raw-valid-count summary
- manual measurement status
- presence of HR data vs stress data
- presence of ring stress/body-battery/respiratory support signals

## 9.5 Fix track D: decide product behavior for `ContinuousRRIData`

A product and data decision is needed here.

Current reality:

- `ContinuousRRIData` may contain valid data even when `ContinuousPressureBean.rriData` is null
- but the home stress graph does not use it

Decision required:

- should RRI remain debug-only?
- should it become a diagnostic-only source?
- should data science define a validated transformation from RRI to graphable HRV/stress when `pressureData` is missing?

Important caution:

- do not silently backfill the production stress graph from raw RRI unless the algorithm is explicitly approved
- otherwise the graph may become inconsistent with server/firmware meaning

## 9.6 Fix track E: validate upstream measurement readiness

Add or verify device-side checks for:

- stress/HRV monitoring enabled state
- sensor contact readiness
- wear detection state
- required low-motion conditions
- firmware version correlation

Because Amjad’s logs strongly suggest:

- sync works
- HR works partly
- stress-related sensing readiness did not

## 9.7 Fix track F: add regression tests around cadence and invalid values

Add tests for:

- `30-second` source -> `5-minute` storage -> `15-minute` stress graph
- `30-second` HR -> `5-minute` storage -> `30-minute` HR bars
- all-zero source arrays
- arrays containing `255`
- arrays where only one value in a bucket is valid
- repeated sync where second sync is zero-only
- future-slot trimming for same-day data
- `rriData=null` but `onContinuousRRIData` non-zero

Expected result:

- we can prove empty graphs are correct when source is empty
- and catch real mapping regressions if they appear later

## 9.8 Fix track G: QA matrix

Create a repeatable QA matrix with at least these cases:

1. Valid HR + valid pressure + valid ring stress
2. Valid HR + zero-only pressure + zero-only ring stress
3. Valid HR + zero-only pressure + non-zero continuous RRI
4. Valid pressure but `pressureDataMaxValue=0`
5. Multiple same-day syncs where later sync is partial
6. Today sync before noon and after noon
7. Device not worn / loose wear simulation

This matrix is necessary because the current behavior depends on:

- callback source
- invalid-value semantics
- same-day trimming
- merge behavior

## 10. Final Conclusion

The current implementation is internally consistent:

- raw continuous HR and continuous pressure are normalized to 5-minute local series
- home HR uses 30-minute bars
- home stress uses 15-minute points
- zeros and `255` are treated as invalid
- repeated zero-only syncs do not wipe earlier valid values

For Amjad on April 15, 2026:

- the home stress graph stayed empty because the app repeatedly synced zero-only stress source data for that day
- this is proven by the raw callback logs and the current code path
- the strongest likely deeper cause is upstream measurement invalidity or sensor readiness, not the home graph implementation itself

For Jagathees:

- the same callback path shows clearly populated stress/body-battery data
- this confirms the implementation can handle valid upstream data

The highest-value fix is therefore:

- improve observability and diagnosis around empty stress source data
- not force the graph to render values that were never actually produced by the SDK/device
