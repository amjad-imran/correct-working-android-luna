# Wear Detection Diagnosis and Fix Plan

Date: 2026-04-16
Scope: Luna ring / Luna band wear detection behavior, refresh behavior, callback handling, stale-data risk, and a no-code implementation plan for a future fix.

## Sources Used

1. ZH SDK v2.3.1 document:
   - `ZH SDK 文档 2.3.1.docx`
   - Key sections:
     - `4.1.11 获取戒指佩戴状态（戒指）`
     - Ring active measurement section
     - Ring sport section
2. App code:
   - `noisefit_zh_sdk/.../ZhQueryDeviceUnitsHandler.kt`
   - `noisefit_zh_sdk/.../ZhUpdateDeviceUnitsHandler.kt`
   - `app/.../RingConnectionService.kt`
   - `app/.../AlertSettingsViewModel.kt`
   - `app/.../BlankTestFragment.kt`
   - `app/.../AlertMirrorSupport.kt`
   - `commons/.../ColorfitData.kt`
3. Latest PDF logs from `latest logs`:
   - `jagathees latest logs app 15th night .pdf`
   - `amjad logs latest 15th night app logs .pdf`
4. Local alert debug logs already present in workspace:
   - `3.alert_debug.log`
   - `5.alert_debug.log`

## Short Answer

Yes, stale wear status is still possible in the current implementation, even when the user taps refresh.

The main reason is not just BLE latency. The current app intentionally prefers recent positive sensor evidence for up to 5 minutes over a direct `not worn` result, and some synced history callbacks incorrectly stamp old positive data as if it were fresh "now". Because of that, the app can still show `worn` after removal, including on refresh.

## Final Verdict

### 1. Is fresh, always-correct wear status guaranteed on refresh?

No.

The current refresh path is not a pure direct-query path. The raw SDK result is fused with recent sensor/manual-measurement evidence before the UI receives it.

### 2. Can the app still show stale `worn` after removal?

Yes.

There are two separate stale paths:

1. Direct-query override window:
   - If the direct wear query says `not worn`, but there was recent positive sensor evidence within the last 5 minutes, the app can still return `worn`.
2. Historical data stamped as fresh:
   - Some heart-rate / SpO2 / stress history callbacks use `System.currentTimeMillis()` instead of the original sample timestamp.
   - That can refresh the "recent positive evidence" window even when the positive value came from older data.

### 3. Is the SDK itself documented as instant?

No.

The SDK documents the API and callback contract, but it does not document a guaranteed latency for `getRingWearingStatus`.

## SDK-Documented Wear Detection APIs and Callbacks

## A. Dedicated direct wear-status query

SDK section: `4.1.11 获取戒指佩戴状态（戒指）`

Documented method:

```java
public void getRingWearingStatus(ParsingStateManager.SendCmdStateListener listener)
```

Documented callback:

```java
public void onRingWearingStatus(int status);
```

Documented values:

- `0` = not worn
- `1` = worn

Important note:

- The Kotlin example under this section appears to contain a documentation typo.
- The example calls `getRingNFCSleepErr(...)` instead of `getRingWearingStatus(...)`.
- The method signature and callback description are still clear enough to infer the intended direct-query API.

Device-scope note:

- The SDK document describes this as a ring API.
- The current app enables the same wear-detection flow for both `NOISEFIT_LUNA` and `LUNA_BAND` through `ScreenlessDeviceSupport`.
- So the current product behavior is broader than the literal SDK section title.

## B. Ring active measurement callbacks that also expose wear state

SDK ring measurement section documents:

```java
public void activeMeasurementStart(ActiveMeasureParamsBean bean, ParsingStateManager.SendCmdStateListener listener)
public void activeMeasurementStop(ActiveMeasureParamsBean bean, ParsingStateManager.SendCmdStateListener listener)
```

Relevant callback:

```java
public interface ActiveMeasureCallBack {
    void onMeasureStatus(ActiveMeasureStatusBean statusBean);
    void onMeasuring(ActiveMeasuringBean measuringBean);
    void onMeasureResult(ActiveMeasureResultBean resultBean);
}
```

Relevant fields:

- `ActiveMeasureStatusBean.isWrist`
- `ActiveMeasuringBean.isWrist`
- `ActiveMeasureResultBean.isWrist`
- `ActiveMeasureResultBean.errorReason`
- `ActiveMeasureStatusBean.measureTime`
- `ActiveMeasuringBean.measureTime`
- `ActiveMeasureResultBean.measureTime`

Documented measurement error reasons:

- `0x00` = success
- `0x01` = not wearing (`NOT_WRIST`)
- `0x02` = data error (`DATA_ERROR`)

Important practical point:

- These callbacks are not the same thing as the dedicated direct wear query.
- They are measurement callbacks, but they still expose wear-related state.

## C. Other indirect wear-related SDK signal

Ring sport section documents:

- `RingSportStartResult.UN_WEAR(0x02)`

That is wear-related, but it is not the same as the dedicated wear-status query used by the refresh button.

## Current App Flow

## 1. User refresh flow

Current UI flow:

1. User taps `Refresh`.
2. UI immediately shows `Querying...`.
3. `AlertSettingsViewModel.refreshWearDetectionStatus()` sends `QueryAction.GetRingWearingStatus`.
4. `ZhQueryDeviceUnitsHandler.getRingWearingStatus()` dispatches the SDK query.
5. SDK callback `onRingWearingStatus(int)` fires.
6. SDK handler converts the raw int to a boolean:
   - `p0 == 1` -> `true`
   - everything else -> `false`
7. `RingConnectionService` receives `QueryCallback.RingWearingStatusObtained`.
8. `RingConnectionService` does not directly trust that result.
9. It passes the result through `WearStatusResolver.fromDirectQuery(...)`.
10. The fused result is what gets saved and shown to the UI.

## 2. Passive/inferred wear flow

The app also updates wear status indirectly from:

- real-time heart-rate samples
- synced heart-rate history
- synced SpO2 data
- synced stress data
- manual measurement results

These paths call:

- `processHeartRateSample(...)`
- `processSpo2Sample(...)`
- `processHighStressSample(...)`
- `WearStatusResolver.fromManualMeasurement(...)`

That means the app has two different concepts mixed together:

1. direct wear query result
2. inferred wear state from health data

This mixing is the main design reason refresh is not guaranteed to be "fresh raw truth".

## Exact Current Handling

## A. Raw direct callback handling

In `ZhQueryDeviceUnitsHandler`, raw SDK wear status is handled as:

```kotlin
isWorn = p0 == 1
```

Meaning:

- raw `1` -> `true`
- raw `0` -> `false`
- raw `2`, `3`, `255`, or any other unexpected value -> also `false`

The raw integer is not preserved anywhere in the model.

## B. Direct refresh is then fused with recent evidence

In `RingConnectionService`, direct query results go through:

```kotlin
WearStatusResolver.fromDirectQuery(...)
```

Behavior:

1. If direct status is `1`:
   - return `worn`, source `direct_query`
2. If direct status is not `1`, but there is recent positive evidence within `STALE_NOT_WORN_WINDOW_MS`:
   - return `worn`, source `sensor_sample`
3. If current state is already fresh positive sensor/manual-measurement state within the same window:
   - keep current `worn`
4. Otherwise:
   - return `not worn`, source `direct_query`

Configured stale window:

- `STALE_NOT_WORN_WINDOW_MS = 5 * 60_000L`
- this is 5 minutes

This is the first confirmed stale path.

## C. Passive timeout inference

There is also a passive inference path:

- `RECENT_SENSOR_EVIDENCE_WINDOW_MS = 2 * 60_000L`
- `STALE_NOT_WORN_WINDOW_MS = 5 * 60_000L`
- alert monitor tick = `30_000L` (30 seconds)
- automatic wear probe interval = `60_000L` (60 seconds)

Passive behavior:

1. If there was recent positive evidence within 2 minutes:
   - infer `worn`
2. If there has been no recent evidence for 5 minutes:
   - infer `not worn` with source `stale_timeout`
3. Otherwise:
   - keep current state

So even without user refresh, background behavior is heuristic, not purely device-truth.

## D. Historical health data can be marked as fresh "now"

This is the second confirmed stale path, and it is more subtle.

For some user-activity callbacks, the app does this:

- extract latest positive value from a breakup/history payload
- call `processHeartRateSample(...)`, `processSpo2Sample(...)`, or `processHighStressSample(...)`
- pass `System.currentTimeMillis()` as the sample time

That means older synced data can become fresh wear evidence right now.

Examples:

- heart history:
  - `processHeartRateSample(it, System.currentTimeMillis())`
- continuous SpO2 history:
  - `processSpo2Sample(observedValue, System.currentTimeMillis())`
- stress history:
  - `processHighStressSample(observedValue, System.currentTimeMillis())`

This is enough by itself to keep wear status stale, because it refreshes the recent-evidence window using sync time rather than original measurement time.

## Latency and Timing

## 1. Direct refresh button

What is immediate:

- the UI text `Querying...` appears immediately on tap

What is asynchronous:

- SDK query dispatch
- BLE/device callback
- service processing
- LiveData/UI propagation

Observed direct-query timing from local alert debug logs:

- clean sample A:
  - dispatch at `19:33:47.512`
  - raw callback at `19:33:47.848`
  - about `336 ms`
- clean sample B:
  - dispatch at `22:06:43.992`
  - raw callback at `22:06:44.067`
  - about `75 ms`

Observed service/UI propagation after callback in those samples:

- raw callback to `AlertService` handling: about `1-7 ms`
- raw callback to `AlertVM` handling: about `22-30 ms`

Important caveat:

- repeated rapid taps create multiple in-flight queries
- there is no in-flight refresh guard in the current UI/ViewModel path
- under repeated taps, apparent completion time can stretch into seconds because many queries are queued or overlap

So the refresh path is responsive visually, but not guaranteed to be a single clean query unless taps are controlled.

## 2. Background passive updates

Background timing in current code:

- alert monitor tick: every 30 seconds
- auto wear probe: at most once every 60 seconds
- stale-to-not-worn timeout: 5 minutes without fresh positive evidence

So if the app relies on passive inference instead of an explicit direct query, "not worn" can take:

- up to 5 minutes
- plus up to the next 30-second monitor tick

That means up to roughly 5 minutes 30 seconds in the passive path.

## 3. Active measurement timing

SDK documentation says:

- `measureTime` is the total time required for overall measurement
- it is not guaranteed to be exactly 30s or 60s
- app should use it for timeout judgment

Latest PDF logs show real active-measurement result payloads with:

- `measureTime = 45`
- `measureTime = 35`

So for measurement-based wear signals, 35-45 seconds is directly evidenced in the provided logs.

## What the Latest PDF Logs Prove

## A. Proven from the PDFs

The provided PDFs do show wear-related measurement results with `isWrist=false`.

Examples found:

### Jagathees PDF

- Page 182:
  - `errorReason=1`
  - `isWrist=false`
  - `measureType=2`
  - `measureTime=45`
- Page 316:
  - `errorReason=2`
  - `isWrist=false`
  - `measureType=5`
  - `measureTime=45`
- Page 346:
  - `errorReason=2`
  - `isWrist=false`
  - `measureType=5`
  - `measureTime=35`

### Amjad PDF

- Page 27:
  - `errorReason=2`
  - `isWrist=false`
  - `measureType=2`
  - `measureTime=45`

What this proves:

1. latest logs definitely contain wear-related callback payloads
2. `isWrist=false` is occurring in real sessions
3. `errorReason` is not limited to `1`; `2` is also present in real logs
4. active-measurement callback timing is at least sometimes 35-45 seconds

## B. What the PDFs do not prove

I did not find raw direct-query log lines like:

- `getRingWearingStatus ...`
- `onRingWearingStatus status=...`

inside the two provided PDF logs.

So from the PDFs alone, I cannot claim that the direct wear-status callback returned any value outside `0` or `1`.

## What the Local Alert Debug Logs Prove

The local alert debug logs are useful for the direct refresh path.

What they show:

1. direct refresh dispatch is happening
2. raw callback `onRingWearingStatus status=0` is occurring
3. `AlertVM` then receives `wearDetection query value={"is_worn":false,...}`

What I did not find in available local logs:

- `onRingWearingStatus status=1`
- `onRingWearingStatus status=2`
- any raw direct wear callback value outside `0`

So the strongest precise statement is:

- in the logs available here, raw direct wear callback values outside `0/1` are not evidenced
- in fact, only raw `0` is evidenced in the available direct-query logs

## What Happens If the Direct Wear Callback Returns a Value Other Than 0 or 1

## Current code behavior

Today, the app does this:

```kotlin
isWorn = p0 == 1
```

So:

- `1` -> `true`
- `0` -> `false`
- any other value -> also `false`

That means:

1. unexpected values are not preserved
2. unexpected values are not surfaced as `unknown`
3. unexpected values are silently collapsed into `not worn`

## Is that behavior correct?

Not fully.

It is simple, but it is not precise.

If the SDK ever emits:

- reserved values
- transitional values
- undocumented values
- corrupted values

the app would misclassify them as `not worn` instead of `unknown/unexpected`.

## Is there log evidence that such unexpected direct values actually happened?

No, not in the logs available here.

So this is a correctness risk in code, but not a proven observed runtime event from the provided logs.

## Root-Cause Summary

The stale-worn behavior is caused by application logic, not just device/SDK latency.

Confirmed causes:

1. Direct refresh is not authoritative.
   - direct `not worn` can be overridden by recent positive evidence for 5 minutes.
2. Historical health data can be treated as fresh evidence.
   - sync/history callbacks stamp values with `System.currentTimeMillis()`.
3. Refresh can be spammed.
   - no in-flight guard means overlapping queries can distort perceived latency.
4. Unexpected direct callback values would be misclassified.
   - current code collapses everything except `1` into `false`.

## Precise Answer to the User's Main Question

If the app is left exactly as currently implemented, then no, wear detection is not guaranteed to be always updated, always fresh, and always direct-truth on refresh.

There is still a real stale-data possibility.

And yes, the current code can explain a scenario where the user removes the ring/band and still sees `worn` later, especially when:

- there was recent positive HR / SpO2 / stress evidence
- synced history refreshed that evidence using current time
- the user tapped refresh but the app fused the result instead of honoring the raw direct query

## Implementation Plan to Fix It Clearly

This section is only a plan. No code changes were made in this task.

## Phase 1. Make direct refresh authoritative

Goal:

- when the user taps refresh, show the raw direct device answer, not a fused heuristic answer

Recommended change:

1. preserve raw direct callback integer value
2. store it separately from inferred wear state
3. on manual refresh:
   - bypass `WearStatusResolver.fromDirectQuery(...)`
   - publish raw direct result directly
   - mark source as `direct_query`

Expected result:

- if the device directly says `0`, refresh should show `not worn`
- recent positive sensor evidence should not override a user-initiated direct refresh

## Phase 2. Stop stamping historical sync data as fresh wear evidence

Goal:

- only real fresh samples should refresh wear freshness

Recommended change:

1. do not use `System.currentTimeMillis()` for historical HR / SpO2 / stress sync callbacks
2. use the original sample timestamp if available
3. if the callback contains only historical aggregate data with no trustworthy timestamp:
   - do not feed it into wear freshness at all

Expected result:

- old synced health data will no longer revive `worn` status after removal

## Phase 3. Separate direct status from inferred status

Goal:

- avoid mixing "device said worn" with "app inferred worn"

Recommended model split:

1. `directWearStatus`
2. `inferredWearStatus`
3. `effectiveWearStatus`

Recommended effective policy:

- manual refresh screen should prefer `directWearStatus`
- background alerts may use `effectiveWearStatus`

Expected result:

- refresh becomes trustworthy
- background heuristics can still exist if the product team wants them

## Phase 4. Handle unexpected raw direct callback values safely

Goal:

- be correct even if SDK behavior expands later

Recommended behavior:

- raw `0` -> `not worn`
- raw `1` -> `worn`
- anything else -> `unknown`

Also:

1. log raw value
2. surface it in debug UI/logging
3. do not silently collapse it to `not worn`

Expected result:

- future SDK changes or edge cases will not be misclassified

## Phase 5. Add in-flight refresh protection

Goal:

- prevent overlapping refreshes from making latency or state look worse

Recommended behavior:

1. disable refresh button while one direct wear query is in flight
2. re-enable on callback or timeout
3. ignore duplicate taps during the in-flight period

Expected result:

- cleaner UX
- cleaner logs
- more reliable latency measurements

## Phase 6. Define a timeout contract

Goal:

- make the refresh outcome predictable

Recommended UX contract:

1. immediate `Querying...`
2. wait for direct callback
3. if no response by timeout:
   - show `Refresh timed out`
   - keep previous value but clearly mark it stale

Suggested starting timeout:

- 3 seconds for direct wear query
- tune after observing real device logs

## Validation Plan

To confirm the fix really solves stale refresh:

1. Wear ring, wait for positive HR/SpO2/stress samples, remove ring, tap refresh immediately.
   - expected after fix: direct refresh shows `not worn` if device returns `0`
2. Wear ring, sync dashboard/history, remove ring, wait 1-5 minutes, tap refresh.
   - expected after fix: history sync must not keep status falsely fresh
3. Trigger manual measurement after removal.
   - expected: `isWrist=false` and `errorReason=1/2` should move wear state to `not worn`
4. Simulate unexpected raw direct callback value `2`.
   - expected: UI/debug state becomes `unknown`, not `not worn`
5. Spam refresh button.
   - expected: only one in-flight query at a time

## Bottom Line

The current implementation does not provide guaranteed fresh direct wear truth on refresh.

The stale behavior is real and explainable by code:

- 5-minute positive-evidence override
- old sync data treated as fresh now
- direct refresh not treated as authoritative

The clean fix is to make manual refresh honor the raw direct callback, stop using historical sync data as fresh wear evidence, and treat unexpected raw callback values as `unknown` instead of silently forcing `not worn`.
