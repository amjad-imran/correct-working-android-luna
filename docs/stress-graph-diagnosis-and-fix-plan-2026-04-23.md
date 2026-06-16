# Stress Graph Diagnosis and Fix Plan

Date: 2026-04-23

Scope:
- Home stress graph and stress detail graph behavior
- Why the graph can appear compressed into a small time window
- Whether the issue is app-side or firmware-side
- What the app should fix
- No code changed

## 1. What the SDK exposes

Relevant SDK classes:
- `ContinuousPressureBean` in `ZH Android SDK v2.3.2.txt:2450-2480`
- `RingStressDetectionBean` in `ZH Android SDK v2.3.2.txt:2940-2951`

SDK meaning:
- `ContinuousPressureBean` is documented for the ring project as HRV data, not the final body-stress graph metric
- `RingStressDetectionBean` contains:
  - `stressFrequency`
  - `data`

Important conclusion:
- The Luna codebase has two different stress-like paths:
  - HRV/continuous-pressure path
  - ring stress-detection/body-stress path

## 2. Current Luna body-stress pipeline

Ring stress callback:
- `ZhUserActivityHandler.onRingStressDetectionData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt:905-913`

Converter:
- `OreoDataConverter.parseBodyStressData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt:704-717`

Save path:
- `OreoSyncDataWork` calls `saveBodyStressData(...)` in `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt:629-635`
- repository save in `app/src/main/java/com/oreo/data/repository/implementation/OreoSyncRepositoryImpl.kt:285-290`
- database implementation in `app/src/main/java/com/oreo/data/db/implementation/OreoBodyStressDataImpl.kt:30-70`

Upload mapping:
- `OreoOnlineDataMapper.parseBodyStressData(...)` in `app/src/main/java/com/oreo/data/dataConverter/OreoOnlineDataMapper.kt:402-423`
- upload frequency is hardcoded to `15` at `412-415`

Server response model used by UI:
- `ServerUserHealthData.stress` in `app/src/main/java/com/oreo/data/model/ServerUserHealthData.kt:79-104`

Naming note:
- the app uploads local ring stress data through the `bodyStress` payload path in `OreoOnlineDataMapper.kt:402-423`
- the UI later consumes server summary `stress`
- that `bodyStress -> stress` translation happens outside the visible app code, so it should be treated as a server contract, not a local-app rename step

Graph data preparation:
- `OreoStressDataConvertor.getStressCombinedData(dayData)` in `app/src/main/java/com/oreo/data/dataConverter/OreoStressDataConvertor.kt:27-100`

Graph rendering:
- `StressCombinedChart` in `app/src/main/java/com/oreo/ui/custom/StressCombinedChart.kt`
- stress detail screen uses it in `app/src/main/java/com/oreo/ui/stress/OStressDataMovementFragment.kt:831-837`

## 3. What cadence the graph expects

The app is internally built around 96 buckets per day for this graph.

Evidence:
- empty fallback graph creates `96` items in `OreoStressDataConvertor.kt:84-89`
- workout overlays are converted in 15-minute buckets in `OreoStressDataConvertor.kt:167-187`
- sleep and nap overlays are also converted in 15-minute buckets in `OreoStressDataConvertor.kt:199-241`
- body-stress upload uses `frequency = 15` in `OreoOnlineDataMapper.kt:412-415`
- stress detail minute totals multiply bucket counts by `15` in `app/src/main/java/com/oreo/ui/stress/OStressDetailViewModel.kt:156-181`

Important conclusion:
- The app consistently expects the rendered stress graph to be a 96-slot, 15-minute-day series.

## 4. Why the graph is not actually "compressed" by drawing math

Renderer behavior:
- `StressCombinedChart.updateData(...)` reverses the list and keeps the full item count in `app/src/main/java/com/oreo/ui/custom/StressCombinedChart.kt:288-294`
- drawing width per item is `unitHLenth = (mWith - leftWith - rightWith) / (list.size - 1)` in `.../StressCombinedChart.kt:548`
- every point is drawn using that full-width spacing in `.../StressCombinedChart.kt:621-625`

What this means:
- if the input list has 96 items, the renderer spreads them across the whole chart width
- the renderer itself does not squash all 96 positions into a tiny left-side region

Important conclusion:
- The reported "whole day plotted in a short space" symptom is much more consistent with bad input data distribution than with bad x-axis spacing math

## 5. Concrete app-side bug found

Problem file:
- `app/src/main/java/com/oreo/data/db/implementation/OreoBodyStressDataImpl.kt`

Bug:
- `insertData(...)` computes `mergedData = getMergedData(prevData, data)` at `41`
- but if an update is needed, it writes `data.breakUp` instead of `mergedData` at `45-46`

Exact problematic lines:
- `mergedData` is created in `OreoBodyStressDataImpl.kt:41`
- update writes `bodyStressDao.updateViaDate(data.breakUp ?: "", data.date!!, false)` in `.../OreoBodyStressDataImpl.kt:45-46`

Why this is wrong:
- `getMergedData(...)` explicitly preserves previous non-zero buckets when new payload buckets are zero, in `.../OreoBodyStressDataImpl.kt:52-69`
- but the update throws that merged result away and stores the raw new breakup instead

Result:
- later partial payloads can overwrite previously valid later-day buckets with zeros
- after that, the graph input contains valid values only for an early portion of the day and zeros for the rest
- the chart then appears to have data only in one small time region, with the remaining day empty

This directly matches the user-reported symptom.

## 6. Why HR graph does not show the same problem

Comparable implementation:
- `OreoStressDataImpl.insertData(...)` in `app/src/main/java/com/oreo/data/db/implementation/OreoStressDataImpl.kt:21-62`

Important difference:
- the analogous stress implementation correctly writes `Gson().toJson(mergedData)` at `OreoStressDataImpl.kt:35-38`

Additional cadence difference:
- `OreoStressDataImpl` is the 288-slot, 5-minute path
- `OreoBodyStressDataImpl` is the 96-slot, 15-minute body-stress path

Verdict:
- the body-stress merge implementation is broken
- the other stress path is not broken in the same way
- that is a strong app-side explanation for "stress graph bad, HR graph fine"

## 7. Is there any second app issue around time alignment?

I found one smaller time-label issue, but it is not the main root cause.

Interactive selected time:
- `OStressDetailViewModel.getTimeFromPosition(position)` uses `(96 - position) * 15` in `app/src/main/java/com/oreo/ui/stress/OStressDetailViewModel.kt:232-240`
- click listener uses that selected time in `app/src/main/java/com/oreo/ui/stress/OStressDataMovementFragment.kt:263-269`

Interpretation:
- because the chart list is reversed, this produces bucket-end style timestamps and is effectively one 15-minute slot ahead at the edges
- this affects selected-value time text more than the actual compressed-graph symptom

Verdict:
- this is a secondary app issue
- it is not the primary reason the plotted line occupies only a small early portion of the chart

## 8. Is firmware also a possible cause?

Possible, but not required to explain the observed behavior.

Why firmware is not required for this symptom:
- the local app persistence bug alone can take a previously healthy day-series and turn most of it into zeros
- that bad local breakup is then what gets uploaded and later displayed

When firmware could still be involved:
- if `RingStressDetectionBean.data` itself already arrives mostly zero or with the wrong bucket cadence
- if the device actually sends a cadence different from 15 minutes and the app hardcoded assumption is wrong

But from the code audit:
- there is already a complete app-side root cause for the main symptom

## 9. Final verdict

Main verdict:
- This stress-graph issue is app-side.

Primary root cause:
- `OreoBodyStressDataImpl` calculates merged body-stress data but stores the unmerged raw payload instead.

Why this is strong proof:
- the bug is explicit in code
- the bug exactly explains "valid values clustered in a small part of the day, rest empty"
- the chart renderer itself uses full-width spacing across all buckets
- the comparable non-body-stress implementation does not have this bug

SDK issue verdict:
- not supported by this code audit

Firmware issue verdict:
- possible in some cases, but not needed to explain the reported graph compression

## 10. Recommended implementation plan

No code was changed in this audit. This is the implementation plan only.

### 10.1 Fix the merge write

Recommended change:
- when updating body stress, store `mergedData`, not raw `data.breakUp`

Why:
- this preserves earlier non-zero buckets when later partial payloads contain zeros

### 10.2 Compare full arrays, not only sums

Current update gate:
- `if (mergedData.sum() != prevBreakup.sum())` in `OreoBodyStressDataImpl.kt:45`

Recommended change:
- compare full arrays or full JSON content

Why:
- two different arrays can have the same sum
- sum-only comparison can miss real changes

### 10.3 Add targeted tests

Recommended tests:
- previous day breakup has later non-zero values
- new payload has zeros for those same later slots
- merged result must preserve the old later non-zero values
- database update must store merged JSON

Also add:
- test for no-op when merged array equals previous array
- test for partial-day early update
- test for partial-day late update

### 10.4 Make cadence explicit

Recommended change:
- validate that body-stress input length matches a 96-slot day
- if device cadence varies by hardware/firmware, prefer the actual SDK `stressFrequency` instead of a hardcoded 15-minute assumption

Why:
- current app logic is internally consistent around 15 minutes, but it is safer to validate rather than silently assume

### 10.5 Optional cleanup: interaction time label

Recommended change:
- revisit `getTimeFromPosition(position)` so the selected time maps exactly to the plotted bucket start or bucket center

Why:
- this improves tooltips and makes x-axis interpretation cleaner

## 11. Bottom line

The stress graph problem described here is explainable from app code alone.

Most likely fault:
- app persistence/merge bug in `OreoBodyStressDataImpl`

Not the main fault:
- chart width math

Possible secondary cleanup:
- selected-time mapping
