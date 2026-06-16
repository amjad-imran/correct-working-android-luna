# Luna Band Workout Issues Diagnosis - April 21, 2026

## Purpose

This document explains, with code and SDK-contract evidence, why Luna Band workouts currently show no live heart-rate data and why workouts longer than 1 minute can get stuck forever on the "please wait" screen after stop.

This diagnosis is based on:

- `ZH Android SDK v2.3.2.txt` as the source of truth
- current production app code in `noisefit-android-luna`
- the old working app in `old noisefit-android-luna`
- the SDK demo in `ZH-SDK-master`
- the provided runtime logs
- `changes.md` and `changes_detailed.md` as change-history context only

`ZH-SDK-master` was used only as reference. No changes are required there.

---

## Executive Verdict

There are two user-visible failures, but the new demo evidence shows they are driven by a larger integration mismatch:

1. **No live HR during workout**
   - The current app forces Luna Band workouts onto the `secondaryScreenSportRequest(...)` transport and expects live metrics mainly from `onSecondaryScreenWearData(...)`.
   - But the user verified that the **working SDK demo path** was `Sub-screen Movement(Ring)`, which uses `sendRingSportStatus(...)` / `getRingSportStatus(...)` and receives live data through `RingSportDataBean`.
   - The app already knows how to display `RingSportDataBean`, but the current Luna Band transport selection prevents that working path from being used.

2. **Workout stop hangs forever for workouts longer than 1 minute**
   - The current app also ties Luna Band stop to the secondary-screen transport and treats secondary-screen stop response `code == OK` as enough to confirm stop and immediately starts historical workout sync.
   - That response only proves the device accepted the stop command on that path. The SDK documentation does not say it guarantees the workout record is already finalized and ready for `getFitnessSportIdsData(...)`.
   - The UI for workouts `>= 60s` waits for a later app event (`sessionManager.showWorkoutDetails`) that only fires after the historical workout-sync pipeline completes.
   - If the sync pipeline returns no progress, no records, or returns too early, the current app has no robust completion fallback and the spinner can remain forever.

So the current failures are a combination of:

- **a likely wrong transport choice for Luna Band workouts in the app**
- **confirmed app-side robustness gaps** in the post-stop record retrieval pipeline

---

## What v2.3.2 Actually Changed

The top-level SDK changelog in `ZH Android SDK v2.3.2.txt` shows two relevant additions for `ZH_SDK_20260420_V2.3.2`:

- screenless-band event reminders
- screenless-band workout types and data

The workout callback surface itself did **not** change in this release, but the SDK still exposes **two different workout-control families**:

The source-of-truth workout contracts still remain:

- historical workout sync:
  - `ControlBleTools.getInstance().getFitnessSportIdsData(...)`
  - `SportCallBack.onDevSportInfo(DevSportInfoBean data)`
  - `SportParsingProgressCallBack.onProgress(progress, total)`
- secondary-screen workout control and live data:
  - `onSportStatus(...)`
  - `onSecondaryScreenSportResponseBean(...)`
  - `onSecondaryScreenSportRequestBean(...)`
  - `onSecondaryScreenWearData(...)`
- ring-sport workout control and live data:
  - `getRingSportStatus(...)`
  - `sendRingSportStatus(...)`
  - `RingSportCallBack.onRingSportStatus(...)`
  - `RingSportCallBack.onRingSportData(...)`

Relevant source-of-truth references:

- `ZH Android SDK v2.3.2.txt:3071-3114`
- `ZH Android SDK v2.3.2.txt:6858-7063`
- `ZH Android SDK v2.3.2.txt:15457-15649`

Important implication:

- There is **no new documented "better" stop callback** added in v2.3.2 that the app forgot to wire.
- There is **no new documented live-workout callback** replacing either `onSecondaryScreenWearData(...)` or `RingSportDataBean`.
- v2.3.2 mainly adds **new bracelet sport ids / data coverage**, not a new workout lifecycle contract.

The most important new finding from the SDK text is this:

- in the `sendRingSportStatus(...)` section, the documentation explicitly says:
  - ring project supported initiated workouts: `206..243`
  - **bracelet project supported initiated workouts: bracelet workouts**

That means the "ring sport" API is not actually limited to ring hardware at workout-start time.

The new band workout ids are documented as `244..266` in `DevSportInfoBean`:

- `ZH Android SDK v2.3.2.txt:3371-3393`

---

## Critical Demo Finding

The SDK demo has two different sport screens:

1. `SportScreenActivity`
   - UI label: `Sub-screen Movement`
   - callback family: `SecondaryScreenSportCallBack`
   - live-data bean: `SecondaryScreenWearDataBean`

2. `RingSportScreenActivity`
   - UI label: `Sub-screen Movement(Ring)`
   - callback family: `RingSportCallBack`
   - live-data bean: `RingSportDataBean`

Relevant references:

- `ZH-SDK-master/app/src/main/res/values/strings.xml`
- `MainActivity.kt:170-171`
- `RingActivity.kt:55-59`
- `SportScreenActivity.kt:105-161`
- `RingSportScreenActivity.kt:92-128`

The user explicitly verified that the **working** live-workout path was:

- `Sub-screen Movement(Ring)`
- with live data arriving as `RingSportDataBean`

That matters because it changes the diagnosis materially:

- the working demo path for this real device/session is not the secondary-screen wear-data path
- it is the ring-sport path

So the current app's Luna Band-only switch to secondary-screen transport is no longer the strongest assumption.

---

## Current Luna Band Workout Architecture

### 1. Workout callback registration

The current app registers **both** sport callback families globally:

- `CallBackUtils.ringSportCallBack = ringSportCallback`
- `CallBackUtils.secondaryScreenSportCallBack = secondaryScreenSportCallback`

Relevant code:

- `ZhUpdateDeviceUnitsHandler.kt:493-497`

### 2. Workout transport selection

Even though both callback families are attached, the current app forces Luna Band onto the secondary-screen transport:

- `ZhUpdateDeviceUnitsHandler.startWorkout(...)`
  - for Luna Band sends `SportRequestBean.getStartPhoneSportRequest(...)`
- `ZhUpdateDeviceUnitsHandler.updateOngoingWorkout(...)`
  - for Luna Band sends pause / resume / stop via `secondaryScreenSportRequest(...)`
- `ZhUpdateDeviceUnitsHandler.checkOngoingWorkout(...)`
  - for Luna Band calls `requestSecondaryScreenWorkoutStatus()`

Relevant code:

- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt:738-742`
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt:805-899`

The old app did not do this. It used the ring-sport transport:

- `sendRingSportStatus(...)`
- `getRingSportStatus(...)`

for workouts generally, which matches the working demo path the user reported.

Relevant code:

- `old noisefit-android-luna/noisefit_zh_sdk/.../ZhUpdateDeviceUnitsHandler.kt:329-467`

### 3. Live workout UI updates

The current workout screen updates live metrics only through:

- `UpdateDeviceDataCallback.WorkoutRealTimeDataObtained`

That UI observer is present in:

- `app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt:625-637`

The important detail is that the app already has **two** producers for that callback:

- `ringSportCallback.onRingSportData(...)`
- `secondaryScreenSportCallback.onSecondaryScreenWearData(...)`

Relevant code:

- `ZhUpdateDeviceUnitsHandler.kt:1086-1102`
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt:1138-1153`

So the live-workout UI is not incapable of rendering `RingSportDataBean`. The current Luna Band routing simply does not use the working demo transport.

### 4. Post-stop summary / details navigation

After stop is confirmed, the workout screen does **not** navigate directly to details.

Instead the app waits for the historical-sync pipeline:

1. `WorkoutStopped(true)` reaches the fragment
2. fragment `stopWorkout()` sets `currentWorkoutState = 4`
3. for non-deleted workouts, it shows the progress bar and stores `lastOngoingWorkoutTimestamp`
4. later, `RingConnectionService` must receive `UserActivityCallback.RingUserWorkoutData`
5. then the service saves/syncs workouts and finally posts `sessionManager.showWorkoutDetails`

Relevant code:

- `RecordWorkoutFragment.kt:131-141`
- `RecordWorkoutFragment.kt:170-235`
- `RecordWorkoutFragmentV2.kt:200-260`
- `RecordWorkoutFragmentV2.kt:657-664`
- `RecordWorkoutFragmentV2.kt:758-769`
- `SessionManager.kt:119-120`
- `RingConnectionService.kt:2155-2161`
- `RingConnectionService.kt:2244-2362`

This is the key reason the stop issue appears as a UI freeze even though the real failure is deeper in the sync chain.

---

## What Is Already Correct in the Current App

These were important to verify because they were likely suspects earlier, but they are **not** the primary cause anymore.

### 1. Luna Band workout id mapping is already updated

The current app already resolves Luna Band startable workouts to bracelet ids `244..266` instead of blindly using old ring ids.

Relevant code:

- `commons/src/main/java/com/noisefit_commans/data/model/OWorkoutListModal.kt:36-137`

That same model also supports reverse matching so historical records with bracelet sport ids can map back to the existing app workout catalog.

### 2. Historical workout mapping already uses device-aware matching

Current parsing logic already uses `matchesSportType(...)`.

Relevant code:

- `app/src/main/java/com/noisefit/data/dataConverter/DataConverter.kt:381-408`

### 3. Both workout callback families are already wired

The app already attaches:

- `onRingSportStatus(...)`
- `onRingSportData(...)`
- `onSportStatus(...)`
- `onSecondaryScreenSportResponseBean(...)`
- `onSecondaryScreenSportRequestBean(...)`
- `onSecondaryScreenWearData(...)`

Relevant code:

- `ZhUpdateDeviceUnitsHandler.kt:944-1153`

### 4. The old app is not the Luna Band protocol reference

The old app is still useful for the shared workout-summary flow, and it is also now useful for one more reason:

- it confirms the previous app workout transport was the ring-sport path, not the Luna Band secondary-screen-only path

---

## Issue 1: No Live HR During Workout

## Confirmed Evidence

The user logs show that while the workout is active, the device continues to emit generic daily realtime packets:

- `REPORT_BASIC_DATA`
- `RealTimeBean.heartRate = 82, 81, 79, 85, 94, 84 ...`

So the band is still sending heart-rate information at runtime.

The new demo evidence adds a more important comparison:

- the user's working demo session delivered live data via `RingSportDataBean`
- the current app routes Luna Band workouts toward `SecondaryScreenWearDataBean`

So the strongest current mismatch is no longer just "missing wear-data callback." It is:

- **working demo transport/callback family vs current app transport/callback family**

### Why the UI stays empty

The live workout screen depends on:

- `UpdateDeviceDataCallback.WorkoutRealTimeDataObtained`

That callback is already emitted from:

- `onRingSportData(...)`
- `onSecondaryScreenWearData(...)`

So if the Luna Band workout were running on the same path as the working demo, the app could already consume `RingSportDataBean` without changing the UI callback contract.

The generic realtime handler still exists too:

- `ZhQueryDeviceUnitsHandler.realDataCallback.onResult(RealTimeBean?)`

Relevant code:

- `ZhQueryDeviceUnitsHandler.kt:471-520`

But that handler no longer publishes workout live metrics to the workout screen.

The old app still contains a commented-out fallback block that previously converted `RealTimeBean` into `QueryCallback.WorkoutRealTimeDataObtained(...)`:

- `old noisefit-android-luna/noisefit_zh_sdk/.../ZhQueryDeviceUnitsHandler.kt:369-391`

Also, the current app still has a generic realtime HR sample callback:

- `QueryCallback.RealTimeHeartRateSampleObtained`
- `ZhQueryDeviceUnitsHandler.kt:1297-1308`

But the workout fragments do not use that callback to drive live workout HR.

## Root Cause

**Confirmed app-side condition**

- The app now routes Luna Band workout control through the secondary-screen transport.
- The app therefore expects Luna Band live workout data to arrive from the secondary-screen callback family.
- The app no longer uses generic realtime HR as a fallback for the workout screen.

**Stronger diagnosis after demo re-check**

- The verified working demo path for this device/session uses the ring-sport transport and delivers live data as `RingSportDataBean`.
- The current app already knows how to convert `RingSportDataBean` into `WorkoutRealTimeDataObtained`.
- Therefore the no-live-HR issue is more accurately explained as a **transport/callback-family mismatch** than as a pure firmware failure.

## Why this is not a documentation-miss in app integration

The SDK documentation contains **both** documented live-workout callback families:

- `onSecondaryScreenWearData(SecondaryScreenWearDataBean bean)`
- `onRingSportData(RingSportDataBean bean)`

And the verified working demo path the user used is the second one:

- `ZH-SDK-master/app/src/main/java/com/zjw/sdkdemo/function/apricot/ring/RingSportScreenActivity.kt:92-128`

So the app is not missing a hidden callback. The stronger issue is that Luna Band was moved to the wrong documented callback family for the observed working path.

## Practical conclusion for Issue 1

The no-live-HR symptom exists because:

1. the current app drives Luna Band workouts through the secondary-screen path
2. the user's verified working demo path uses the ring-sport path
3. the app is therefore waiting for the wrong live-workout callback family for this working device/session
4. the generic fallback was removed, so there is no resilience layer when the chosen transport does not produce live data

This means the final fix may require:

- restoring the Luna Band workout transport to the ring-sport path, or
- supporting both documented callback families for Luna Band
- plus an app-side fallback for resilience

---

## Issue 2: Stop Hangs Forever on "Please Wait"

## What the current code does

When Luna Band stop is requested:

1. `updateOngoingWorkout(..., action = 4)` sends `SportRequestBean.getStopPhoneSportRequest(...)`
2. the app waits for secondary-screen callbacks
3. if `onSecondaryScreenSportResponseBean(...)` returns `code == OK`, the app immediately calls `publishWorkoutStoppedConfirmed()`
4. `publishWorkoutStoppedConfirmed()` emits `WorkoutStopped(true)` and immediately calls `getFitnessSportIdsData(null)`

Relevant code:

- `ZhUpdateDeviceUnitsHandler.kt:332-348`
- `ZhUpdateDeviceUnitsHandler.kt:402-440`
- `ZhUpdateDeviceUnitsHandler.kt:1124-1135`

## What the SDK documentation actually guarantees

Per `ZH Android SDK v2.3.2.txt`:

- `onSecondaryScreenSportResponseBean(SportResponseBean bean)` means:
  - the device replies to the app's secondary-screen motion status report
- `onSecondaryScreenSportRequestBean(SportRequestBean bean)` means:
  - the device reports the motion status of the secondary screen

The doc describes `SportResponseBean.code` as a status response, not as proof that workout history has already been committed and is ready for historical retrieval.

Important source-of-truth reference:

- `ZH Android SDK v2.3.2.txt:6942-7004`

## Why the current stop handling is risky

The current Luna Band implementation effectively collapses two different events into one:

- command accepted by device
- workout record finalized and ready for historical sync

Those are not documented as the same thing.

This is the strongest protocol-level reason the app can get stuck after stop:

- the app begins `getFitnessSportIdsData(...)` too early
- the record is not ready yet
- the later sync pipeline never completes cleanly

The new demo evidence makes this risk worse, not smaller:

- if the correct Luna Band workout transport for this device/session is actually the ring-sport path, then the app is also using the wrong stop/status protocol family before it even reaches the historical sync problem

## Evidence from the provided logs

The stop logs show:

- `SECONDARY_SCREEN_SPORT_REQUEST ... code: OK`
- then shortly after: `GET_FITNESS_SPORT_ID_LIST`
- but there is no evidence of:
  - `onDevSportInfo(...)`
  - progress completion
  - workout summary navigation

That pattern is exactly consistent with:

- stop transport ACK received
- immediate historical sync triggered
- no completed workout payload returned to the app

## The UI reason the user gets "stuck forever"

For workouts shorter than 60 seconds:

- the app marks them deleted and exits quickly

For workouts `>= 60 seconds`:

- the app shows the progress bar and waits for summary routing via `sessionManager.showWorkoutDetails`

Relevant code:

- `RecordWorkoutFragment.kt:170-235`
- `RecordWorkoutFragmentV2.kt:200-260`
- `RecordWorkoutFragment.kt:131-141`
- `RecordWorkoutFragmentV2.kt:758-769`

That event is only posted later by:

- `RingConnectionService.postWorkout(...)`

Relevant code:

- `RingConnectionService.kt:2355-2362`

So the screen is not actually waiting for the stop command. It is waiting for the **post-stop workout-history pipeline** to succeed.

## The confirmed app-side blind spot that lets the spinner hang forever

`ZhUserActivityHandler` collects `onDevSportInfo(...)` records into `sportModleInfoList` and waits for `SportParsingProgressCallBack`.

But the current implementation has two important weaknesses:

### 1. `total == 0` is ignored

Current code:

- `if (total == 0) return@setSportParsingProgressCallBack`

Relevant code:

- `ZhUserActivityHandler.kt:159-188`

So if the SDK indicates zero records, the app does not emit:

- `UserActivityCallback.RingUserWorkoutData(emptyList())`

That means downstream code may wait forever.

### 2. No timeout / completion fallback exists if no progress callback arrives

If `getFitnessSportIdsData(...)` is triggered but the SDK never delivers:

- progress callbacks
- or `onDevSportInfo(...)`

then the current bridge does not emit any terminal event. Nothing tells the service or UI that sync finished with no usable record.

### 3. The current post-stop path bypasses the guarded sports-sync entrypoint

The normal user-activity sports-sync method already performs important setup:

- `isSyncProtoSportSyncing = true`
- `sportModleInfoList.clear()`

Relevant code:

- `ZhUserActivityHandler.kt:861-870`

But the current Luna Band stop-confirmation helper calls raw:

- `ControlBleTools.getInstance().getFitnessSportIdsData(null)`

directly from `ZhUpdateDeviceUnitsHandler.publishWorkoutStoppedConfirmed()`.

Relevant code:

- `ZhUpdateDeviceUnitsHandler.kt:332-348`

That means the post-stop fetch bypasses the existing guard/clear flow and can behave differently from the normal sports-sync path.

## Why the "> 1 minute" symptom is especially visible

This is not a separate BLE protocol for long workouts.

It happens because the app has two UI branches:

- `< 60s` stop path: delete-and-exit quickly
- `>= 60s` stop path: wait for saved summary routing

So the long-workout issue is the same underlying failure, just exposed by the branch that waits for historical workout retrieval.

---

## Relationship to the Old App

The old app is useful for understanding the shared summary flow:

- stop confirmation eventually leads to workout history retrieval
- history retrieval eventually leads to details routing

It is also now useful for one more reason:

- it shows that the older workout transport in the app was `sendRingSportStatus(...)` / `getRingSportStatus(...)`

That matches the working demo path the user reported more closely than the current Luna Band secondary-screen-only routing.

The old app therefore supports this conclusion:

- the post-workout summary screen has always depended on successful record retrieval
- Luna Band now adds a new stop/control transport layer, and the current app bridges that layer too optimistically

---

## Final Root-Cause Summary

## Confirmed root causes

1. **The current app switched Luna Band workouts onto the secondary-screen transport family (`secondaryScreenSportRequest` / `getSportStatus` / `SecondaryScreenSportCallBack`).**
2. **The user's verified working demo path uses the ring-sport transport family (`sendRingSportStatus` / `getRingSportStatus` / `RingSportCallBack`) and receives live data through `RingSportDataBean`.**
3. **The SDK documentation explicitly says bracelet projects are supported by `sendRingSportStatus(...)`, so the ring-sport path remains a documented candidate for Luna Band rather than a demo-only hack.**
4. **The app already has a working `RingSportDataBean -> WorkoutRealTimeDataObtained` bridge, but the current Luna Band transport selection prevents that path from driving the workout UI.**
5. **The app waits for historical workout sync to finish before leaving the post-stop spinner.**
6. **The current Luna Band stop flow triggers historical workout sync immediately on secondary-screen response `code == OK`, even though the SDK docs do not say this means the record is ready.**
7. **`ZhUserActivityHandler` does not emit a terminal callback when the workout-history request yields zero results or no progress callback.**
8. **The current post-stop path bypasses the guarded sports-sync entrypoint and directly calls raw `getFitnessSportIdsData(...)`.**
9. **The generic fallback was removed, so there is no resilience if the chosen workout transport does not produce live data.**

## Not the primary root cause anymore

10. **Workout id mapping mismatch is not the main blocker in the current codebase, because device-aware Luna Band mapping `244..266` is already implemented.**

---

## Fix Direction Implied by This Diagnosis

The app must be updated so that it:

1. re-evaluates Luna Band workout transport and aligns it with the documented, user-verified working ring-sport path
2. separates "stop command accepted" from "workout record ready"
3. makes the historical workout retrieval path retryable and time-bounded
4. emits a terminal result even when zero workout records arrive
5. prevents the spinner from waiting forever
6. keeps a resilience fallback if a given firmware variant emits one workout callback family but not the other

The detailed implementation plan is documented in:

- `docs/workout-issues-fix-plan-2026-04-21.md`

---

## Validation Against April 21, 2026 16:27-16:30 Logs

The newer logs materially change what can now be stated with confidence about the current implementation.

### What the new logs prove

1. **The app is now using the ring-sport workout transport family for this Luna Band session.**

Evidence from the logs:

- `01 63 -> SET_RING_SPORT_STATUS`
- `01 64 -> GET_RING_SPORT_STATUS`
- `01 6B -> RING_WEAR_SPORT_DATA`

That means the session is no longer being driven by the previously broken secondary-screen-only path for this flow.

2. **The live workout stream is active and is not being sourced from generic realtime fallback for the on-screen workout HR/calorie UI.**

The app code now routes live workout UI updates in `RecordWorkoutFragmentV2` only through:

- `UpdateDeviceDataCallback.WorkoutRealTimeDataObtained`

For the ring-sport transport family, that callback is produced by:

- `ZhUpdateDeviceUnitsHandler.ringSportCallback.onRingSportData(...)`

and mapped directly from `RingSportDataBean` into:

- `WorkoutRealTimeData(hrValue, calorieValue, steps, distance)`

The generic daily realtime callback path (`REPORT_BASIC_DATA`) is still arriving in the logs, but the V2 workout screen does **not** read that payload for live HR display.

3. **The live HR value shown on the V2 workout screen is therefore the workout callback value, not the generic basic-data HR value.**

In the current code:

- `RecordWorkoutFragmentV2.updateWorkoutData(...)` updates calories and heart-rate zone UI from `WorkoutRealTimeData`
- `WorkoutRealTimeData` comes from `RingSportDataBean` for the current Luna Band path

So the visible live HR / live calorie value on the V2 workout screen is now tied to `RING_WEAR_SPORT_DATA`.

### What the logs suggest about live-data quality

The new session shows this sequence:

- `2026-04-21 16:28:51` start accepted through `SET_RING_SPORT_STATUS`
- `2026-04-21 16:28:52` active state confirmed through `GET_RING_SPORT_STATUS`
- repeated `RING_WEAR_SPORT_DATA` packets continue during the session
- early packets show HR immediately, while calories / steps / distance remain at zero
- later packets begin increasing calories / steps / distance

This pattern is consistent with a real workout stream that has reset for a newly started session and then begins accumulating movement data.

### Important nuance: `REPORT_BASIC_DATA` is still present, but it is not the live workout UI source

The logs still contain:

- `00 A5 -> REPORT_BASIC_DATA`

Those packets are generic device realtime summaries and may continue in parallel during workouts.

However, based on the current app code:

- `REPORT_BASIC_DATA` is **not** the callback feeding `RecordWorkoutFragmentV2` workout HR / calorie display
- the displayed live workout HR / calorie values are coming from `RING_WEAR_SPORT_DATA`

So for the current V2 workout screen, the live displayed HR is no longer a fallback or substituted daily HR source.

### Important nuance: `sport_level: INACTIVE` and `sport_timestamp: 0`

The BLE logs printed by the SDK show extra text fields such as:

- `sport_timestamp: 0`
- `sport_level: INACTIVE`

But the SDK text documentation's `RingSportDataBean` definition documents only:

- heartRate
- calories
- steps
- distance

The app also does not read `sport_level` for workout UI logic.

So:

- these fields are currently **not** part of the app-side decision path
- their values do **not** prove the live workout callback is wrong
- they look more like firmware / SDK logging extras than a field the app is expected to consume for this flow

### What is correct now

1. **Transport family selection is now correct for the validated Luna Band path.**
2. **The V2 live workout HR / calorie UI is using the workout callback family, not the generic fallback path.**
3. **The callback / method family for active workout control is now aligned with the working SDK demo path:**
   - `sendRingSportStatus(...)`
   - `getRingSportStatus(...)`
   - `RingSportCallBack.onRingSportData(...)`
   - `RingSportCallBack.onRingSportStatus(...)`

### What is still not fully proven from these logs alone

1. **The final workout details page cannot be fully certified from the pasted logs alone.**

Why:

- the pasted stop sequence shows:
  - `SET_RING_SPORT_STATUS` with stop / end
  - `GET_RING_SPORT_STATUS`
  - `GET_FITNESS_SPORT_ID_LIST`
  - `GET_AUTO_SPORT_DATA_LIST`
  - `CONFIRM_FITNESS_SPORT_ID_LIST`
- but it does **not** show the later record-sync completion evidence such as:
  - `onDevSportInfo(...)`
  - `setSportParsingProgressCallBack progress == total`
  - `UserActivityCallback.RingUserWorkoutData(...)`
  - `postWorkout(workoutId)`
  - screen navigation to the workout details fragment

So the logs prove the stop flow has entered the correct fetch stage, but they do not yet prove that this particular pasted session completed all the way to saved workout details routing.

2. **The live workout screen still does not render every live field that the callback contains.**

In `RecordWorkoutFragmentV2`, the realtime callback currently updates:

- calories
- live HR / zone UI

It does **not** currently render the callback's:

- steps
- distance

So those values may be live in the callback stream without being visible on the workout screen.

3. **The legacy `RecordWorkoutFragment` still does not consume `WorkoutRealTimeDataObtained`.**

That is a residual inconsistency in the codebase.

However, the currently wired entry path in `OreoMainActivity` navigates users into:

- `recordWorkoutFragmentV2`

So the active user flow is presently on the fragment that does consume the live workout callback.

### Final-page mapping status

The final details page uses `OWorkoutDetailsFragmentV2`, which is primarily server-backed through:

- `OWorkoutDetailsViewModelV2.getWorkoutDetails(...)`

Current mapping behavior:

1. **HR graph points come from server `hrArray`.**
2. **That server `hrArray` is originally uploaded from local recorded workout `hrData` created from `DevSportInfoBean.ringPointData`.**
3. **Calories and recovery time are supplemented from locally cached `RecordedWorkoutData` when the just-recorded workout matches by date/start/end time.**
4. **HR zone durations are also supplemented from local recorded workout fields when available:**
   - `hrWarmUp`
   - `hrFatBurning`
   - `hrAerobic`
   - `hrAnaerobic`
   - `hrLimitTime`

So the final-page mapping is **architecturally correct for the intended flow**, but these pasted logs do not yet provide enough runtime evidence to prove that the full record-sync and detail-page routing completed successfully for this exact session.

### Validation conclusion from the new logs

1. **Yes, the live workout implementation is now on the correct callback / method family for the validated Luna Band path.**
2. **Yes, the V2 on-screen live HR value is now sourced from live `RING_WEAR_SPORT_DATA`, not from generic `REPORT_BASIC_DATA` fallback.**
3. **Yes, calories shown on the V2 live screen are also sourced from the workout callback stream.**
4. **No, the logs provided are still not sufficient to claim end-to-end proof that the final workout details page completed correctly for this exact stop attempt.**
5. **No, the V2 live screen is not yet showing every available live callback field, because steps and distance are not bound into that screen even though the callback contains them.**
