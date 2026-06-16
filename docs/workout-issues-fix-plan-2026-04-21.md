# Luna Band Workout Issues Fix Plan - April 21, 2026

## Goal

Fully fix the remaining Luna Band workout failures in the app layer, while clearly separating:

- what must be fixed in `noisefit-android-luna`
- what may still need firmware / SDK-runtime verification

This plan assumes:

- `ZH Android SDK v2.3.2.txt` is the source of truth
- `zh-sdk-master` is reference-only
- the AAR remains unchanged

The plan below is intentionally app-focused and does not require editing SDK source.

---

## Target Outcomes

After this fix, the app should behave like this:

1. Luna Band workouts start with the correct bracelet sport id.
2. Luna Band workouts use the correct workout transport for the device / firmware path that actually works in the SDK demo.
3. Live HR appears during workout from the correct callback family, with a bounded fallback if needed.
4. Tapping stop never leaves the user on an infinite spinner.
5. Workouts longer than 1 minute either:
   - open the workout details screen, or
   - exit with an explicit, bounded fallback outcome
6. The app never assumes that a workout command ACK automatically means the workout record is already ready.

---

## High-Level Strategy

The fix should be implemented in six coordinated parts:

1. **Correct Luna Band workout transport selection**
2. **Separate stop command ACK from workout-record readiness**
3. **Move post-stop workout-history fetch into a guarded, retryable sync flow**
4. **Make the workout-history bridge always emit a terminal result**
5. **Make the UI/service stop flow time-bounded and outcome-based**
6. **Make live-HR handling transport-aware, with bounded fallback**

---

## Important Design Rules

### 1. Keep the SDK contract primary

The SDK exposes two documented workout callback families:

- `RingSportCallBack`
- `SecondaryScreenSportCallBack`

The user-verified working demo path for this device/session is the ring-sport family, so the app should not hard-code Luna Band to the secondary-screen family.

### 2. Prefer the verified working transport

Because the SDK docs say bracelet projects are supported by `sendRingSportStatus(...)`, and because the working demo path is `Sub-screen Movement(Ring)`, the app should treat the ring-sport transport as the leading fix candidate for Luna Band app-launched workouts.

### 3. Do not use the stop ACK as proof the record is ready

Per the SDK docs, a command response means the device accepted the request on that transport. It does not explicitly guarantee that historical workout data is already persisted and ready for `getFitnessSportIdsData(...)`.

### 4. Never allow an unbounded wait

Every stop flow and every post-stop sync flow must have a terminal outcome:

- success with details
- success without details
- empty result
- timeout
- error

### 5. Keep Luna Band behavior isolated

The ring / old-device path is already working. The new logic should be guarded to Luna Band or secondary-screen workout paths so regressions are minimized.

---

## Workstream 1: Correct Luna Band Workout Transport

## Problem

The current app hard-codes Luna Band workouts to the secondary-screen transport:

- `secondaryScreenSportRequest(...)`
- `getSportStatus(...)`
- `SecondaryScreenSportCallBack`

But the strongest current evidence points elsewhere:

- the user's verified working demo path is `Sub-screen Movement(Ring)`
- that demo screen uses `sendRingSportStatus(...)`
- it receives live data through `RingSportDataBean`
- the SDK docs explicitly say bracelet projects are supported by `sendRingSportStatus(...)`
- the old app also used the ring-sport transport family for workouts

Relevant current code:

- current app Luna Band routing:
  - `ZhUpdateDeviceUnitsHandler.kt:738-742`
  - `ZhUpdateDeviceUnitsHandler.kt:805-899`
- current app already wired to receive ring-sport live data:
  - `ZhUpdateDeviceUnitsHandler.kt:1086-1102`
- old app workout transport:
  - `old noisefit-android-luna/noisefit_zh_sdk/.../ZhUpdateDeviceUnitsHandler.kt:329-467`
- SDK docs:
  - `ZH Android SDK v2.3.2.txt:15457-15649`

## Required change

Change Luna Band app-launched workout control to use the ring-sport transport family by default.

### Recommended transport for Luna Band app-launched workouts

- start: `sendRingSportStatus(...)`
- status query: `getRingSportStatus(...)`
- pause / resume / stop: `sendRingSportStatus(...)`
- live data: `RingSportCallBack.onRingSportData(...)`
- stop / ongoing state: `RingSportCallBack.onRingSportStatus(...)`

### Why this is the best-fit app change

- it matches the working SDK demo path the user actually validated
- it matches the old app's pre-regression workout transport family
- the current app already has `onRingSportData(...) -> WorkoutRealTimeDataObtained`
- it avoids depending on `SecondaryScreenWearDataBean` for a device/session that already proved it emits `RingSportDataBean`

### Keep both callback families attached

Do not remove `secondaryScreenSportCallBack`.

Reason:

- some firmware variants may still use secondary-screen flows
- keeping both families attached gives the app compatibility and better logging

### Optional rollout-safe variant

If the team wants lower rollout risk, make the transport selectable:

- default Luna Band to ring-sport transport
- keep a feature flag or runtime override to fall back to secondary-screen transport if a specific firmware variant requires it

### Acceptance criteria

- Luna Band no longer hard-codes workouts to `secondaryScreenSportRequest(...)`
- real-device live metrics can arrive through `RingSportDataBean`
- existing ring devices are unaffected

---

## Workstream 2: Make Stop Handling Transport-Aware

## Problem

The current stop flow is built around secondary-screen semantics for Luna Band. After correcting transport selection, stop handling must follow the chosen transport family rather than one hard-coded path.

## Required change

Introduce a transport-aware Luna Band stop state machine in `ZhUpdateDeviceUnitsHandler`.

### If Luna Band is on ring-sport transport

Use `RingSportStatusBean` as the primary stop-state signal.

Important fields already available in the SDK doc:

- `isSportNoSync`
- `isSporting`
- `sportStatus`
- `endReason`

This gives the app a stronger stop/status contract than the current secondary-screen ACK-only approach.

### If Luna Band is on secondary-screen fallback transport

Keep the earlier rule:

- do not treat response `code == OK` as proof the record is ready
- wait for inactive / stop confirmation or bounded polling before starting post-stop history sync

### Recommended transport-aware states

- `IDLE`
- `STOP_REQUEST_SENT`
- `STOP_ACK_RECEIVED`
- `STOP_DEVICE_CONFIRMED`
- `POST_STOP_SYNC_REQUESTED`
- `POST_STOP_SYNC_FINISHED`
- `FAILED`

### Acceptance criteria

- stop handling uses the correct status callback family for the chosen transport
- stop does not depend on send success alone
- duplicate stop confirmations remain safely ignored

---

## Workstream 3: Move Post-Stop Workout Fetch into a Guarded Sync Path

## Problem

The current post-stop path calls raw `getFitnessSportIdsData(null)` directly from `ZhUpdateDeviceUnitsHandler`.

That bypasses the normal workout-sync entrypoint in `ZhUserActivityHandler.syncSportsActivity(...)`, which already does important setup:

- sets `isSyncProtoSportSyncing = true`
- clears `sportModleInfoList`

Relevant current code:

- direct raw fetch:
  - `ZhUpdateDeviceUnitsHandler.kt:344`
- guarded sync path:
  - `ZhUserActivityHandler.kt:861-870`

## Why this matters

Direct raw fetch after stop can:

- skip the list clear that protects against stale records
- skip the sync-active guard
- make post-stop behavior inconsistent with the rest of the sports-sync pipeline

## Required change

Do not let `ZhUpdateDeviceUnitsHandler` directly own raw historical workout fetch anymore for Luna Band post-stop.

### Recommended architecture

Use a dedicated user-activity-side entrypoint for post-stop workout sync.

Best-fit options:

1. add a dedicated method in `ZhUserActivityHandler`, such as:
   - `syncPostWorkoutHistory()`
2. or reuse `syncSportsActivity(...)` with a clear post-stop reason / metadata

The key requirement is that all post-stop history fetches must go through one controlled place that:

- clears stale sport records
- marks sync active
- starts timeout handling
- knows how to emit a terminal callback

### Additional post-stop metadata to carry

The sync flow should know:

- sport type requested
- workout start timestamp
- stop request time
- whether this is a post-stop fetch vs a general sync

This makes it possible to:

- log the correct session
- match the right record later
- retry intelligently

### Recommended retry policy

For Luna Band post-stop history retrieval:

- attempt fetch
- wait for progress / records / terminal outcome
- if nothing usable arrives, retry a limited number of times with short backoff

The exact numbers can be tuned, but the structure should be:

- bounded
- logged
- deterministic

### Use transport status to decide when sync is needed

When the ring-sport path is active, prefer to start post-stop history retrieval only after a ring status that indicates the workout finished and needs sync, for example via `isSportNoSync` / final `sportStatus`.

### File ownership

- primary file: `noisefit_zh_sdk/.../ZhUserActivityHandler.kt`
- coordinating caller: `noisefit_zh_sdk/.../ZhUpdateDeviceUnitsHandler.kt`

### Acceptance criteria

- post-stop workout fetch always starts from a path that clears `sportModleInfoList`
- Luna Band post-stop fetch is retried in a controlled, bounded way
- no raw `getFitnessSportIdsData(...)` remains in the Luna Band stop-confirmation fast path

---

## Workstream 4: Make Workout-History Sync Always End with a Result

## Problem

`ZhUserActivityHandler` currently has no safe terminal behavior for these cases:

1. `SportParsingProgressCallBack` reports `total == 0`
2. `getFitnessSportIdsData(...)` produces no progress callback at all
3. records arrive partially and the flow stalls

Relevant current code:

- `ZhUserActivityHandler.kt:124-188`

Today:

- `total == 0` is ignored
- if no progress callback comes, nothing is emitted
- downstream service/UI may wait forever

## Required change

Give workout-history sync an explicit completion contract.

### Recommended new terminal outcomes

The bridge layer should emit one terminal outcome for every post-stop sync request:

- `RecordedWorkoutSyncSuccess(records)`
- `RecordedWorkoutSyncEmpty`
- `RecordedWorkoutSyncTimeout`
- `RecordedWorkoutSyncFailed`

If a minimal-diff approach is preferred, `RingUserWorkoutData(emptyList())` can still be used for the empty case, but an explicit typed outcome is cleaner and easier to debug.

### Required behavior changes

#### A. Handle `total == 0`

Instead of returning early forever, immediately emit an empty terminal result.

#### B. Add a sync timeout

When a post-stop workout fetch begins:

- start a timeout timer
- cancel it on terminal completion
- if no progress / record arrives in time, emit timeout and clean state

#### C. Reset internal state on every terminal path

Always reset:

- `isSyncProtoSportSyncing`
- `sportModleInfoList`
- any timeout / retry state

#### D. Keep `onDevSportInfo(...)` accumulation scoped to one request

The record list used for a post-stop fetch must belong only to that fetch, not to prior unfinished requests.

### File ownership

- primary file: `noisefit_zh_sdk/.../ZhUserActivityHandler.kt`

### Acceptance criteria

- post-stop sync can never stall forever waiting for `progress == total`
- zero-record responses are surfaced cleanly
- stale workout records are not reused across requests

---

## Workstream 5: Fix the Service and UI Completion Contract

## Problem

The fragments currently wait for:

- `sessionManager.showWorkoutDetails`

That event is only posted by `RingConnectionService.postWorkout(...)`, which today depends on the historical workout pipeline completing.

Relevant current code:

- `RecordWorkoutFragment.kt:586-604`
- `RecordWorkoutFragmentV2.kt:576-600`
- `RingConnectionService.kt:2155-2161`
- `RingConnectionService.kt:2244-2362`

This means:

- stop transport can succeed
- but the UI still spins forever if the later sync pipeline never emits a terminal outcome

## Required change

Make the stop-completion contract explicit from service to UI.

### Recommended model

Replace the current string-based `showWorkoutDetails: LiveData<Event<String?>>` outcome with a typed result, for example:

- `WorkoutPostStopResult.ShowDetails(workoutId)`
- `WorkoutPostStopResult.NoRecord`
- `WorkoutPostStopResult.NoInternet`
- `WorkoutPostStopResult.Timeout`
- `WorkoutPostStopResult.SyncFailed`

This avoids overloading string values like:

- `"none"`
- `"no_internet"`

and gives the UI enough information to stop spinning correctly.

### Required service changes

`RingConnectionService` should:

1. listen for the new post-stop sync terminal outcomes
2. perform save / server sync when records exist
3. always post a final result back to the fragment

Important rule:

- even if no workout record is found, the service must still post a terminal event so the workout screen can exit the progress state

### Required fragment changes

Both workout fragments should:

1. stop showing the progress indicator on every terminal outcome
2. navigate to details only on explicit success
3. show a user-facing fallback on empty / timeout / failure
4. never remain indefinitely in state `currentWorkoutState == 4`

Possible bounded fallbacks:

- navigate back to the previous screen with a toast
- open workout list without details
- show "workout saved on device, sync again later" if appropriate

### Add a fragment-side safety timer

Even after service fixes, add a final UI safety timeout.

Reason:

- UI should never depend on any background component being perfect

### Files to update

- `app/src/main/java/com/noisefit/session/SessionManager.kt`
- `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`
- `app/src/main/java/com/oreo/ui/recordworkout/RecordWorkoutFragment.kt`
- `app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt`

### Acceptance criteria

- stopping a workout longer than 1 minute can no longer leave the user on an infinite spinner
- the fragment always receives a final outcome
- typed outcomes replace ambiguous string sentinels for the post-stop flow

---

## Workstream 6: Make Live HR Handling Transport-Aware

## Problem

The current app previously assumed Luna Band live workout data should mainly come from `onSecondaryScreenWearData(...)`.

The stronger current evidence is:

- the working demo path delivers live data through `RingSportDataBean`
- the current app already forwards `RingSportDataBean` to `WorkoutRealTimeDataObtained`
- generic realtime HR still exists as a separate lower-confidence fallback source

The app also receives `QueryCallback.RealTimeHeartRateSampleObtained`, but that path is currently used for alert processing, not workout UI.

Relevant current code:

- `ZhQueryDeviceUnitsHandler.kt:471-520`
- `ZhQueryDeviceUnitsHandler.kt:1297-1308`
- `RingConnectionService.kt:2850-2862`

## Required change

Make Luna Band live-HR handling depend on the chosen workout transport.

### Recommended priority order

If Luna Band is on ring-sport transport:

1. primary: `onRingSportData(...)`
2. fallback: `onSecondaryScreenWearData(...)` only if that callback also appears
3. fallback: realtime HR sample path
4. last resort fallback: parse HR from generic realtime bean if needed

If Luna Band is on secondary-screen transport:

1. primary: `onSecondaryScreenWearData(...)`
2. fallback: `onRingSportData(...)` if a specific firmware unexpectedly emits it
3. fallback: realtime HR sample path
4. last resort fallback: generic realtime bean

### Important scope limits

This fallback should be:

- Luna Band only
- active workout only
- disabled when the workout ends
- automatically superseded when proper secondary-screen wear data resumes

### Important data limits

Use fallback only for **live HR display and zones**.

Do **not** use generic realtime fallback to fabricate:

- final historical workout records
- final calories / distance / steps if those values are not guaranteed to be workout-scoped

### Best-fit implementation options

#### Preferred

Translate fallback HR into the same `UpdateDeviceDataCallback.WorkoutRealTimeDataObtained` channel used by the workout UI, so the fragment keeps a single rendering path.

#### Acceptable

Let the fragment observe a second callback source only during active Luna Band workouts.

The preferred option is cleaner and reduces UI branching.

### Transport-facing note

Even after adding fallback handling, the primary fix should still be to align Luna Band with the working transport family rather than relying on fallback forever.

### Files to update

- likely `noisefit_zh_sdk/.../ZhQueryDeviceUnitsHandler.kt`
- likely `app/src/main/java/com/oreo/receiver/service/RingConnectionService.kt`
- possibly `ZhUpdateDeviceUnitsHandler.kt` or the workout fragments, depending on chosen merge point

### Acceptance criteria

- active Luna Band workouts show HR from the correct transport family
- once the primary transport stream arrives, it takes precedence automatically
- no final recorded-workout logic depends on the fallback path

---

## Workstream 7: Logging and Verification Hooks

## Problem

The current logs are enough to show symptoms, but not enough to unambiguously separate:

- stop ACK received
- device inactive confirmed
- post-stop sync started
- post-stop sync completed empty
- post-stop sync timed out

## Required change

Add structured logging around the full Luna Band workout stop pipeline.

### Log points to add

- selected workout transport family for the session
- stop request sent
- stop ACK received
- stop inactive confirmed from the active status callback family
- stop confirmed from the active request/status callback family
- fallback polling attempt count
- post-stop workout fetch attempt number
- first `onDevSportInfo(...)` received for the request
- first live workout data callback received and which bean family produced it
- progress updates
- terminal sync result: success / empty / timeout / failed
- UI terminal outcome posted

### Why this matters

If firmware still withholds `onSecondaryScreenWearData(...)` or delays historical record persistence, these logs will let the team prove which boundary is failing.

---

## Recommended Execution Order

Implement in this order:

1. `ZhUpdateDeviceUnitsHandler`
   - switch Luna Band to the correct transport family
   - make stop handling transport-aware
2. `ZhUserActivityHandler`
   - add guarded post-stop sync entrypoint
   - add terminal outcomes, timeout, zero-result handling
3. `RingConnectionService` and `SessionManager`
   - convert post-stop flow to explicit typed result
4. workout fragments
   - remove infinite spinner behavior
   - handle all final outcomes
5. live HR fallback / dual-family compatibility
   - add only after the transport and stop/summary path are stable

This order isolates the most severe bug first:

- wrong Luna Band workout transport plus stop never resolving

---

## Verification Matrix

The fix should not be considered complete until all of the following pass on a real Luna Band:

### Stop / summary flow

1. Start workout, run for less than 60 seconds, stop
   - no infinite spinner
   - expected short-workout behavior preserved

2. Start workout, run for more than 60 seconds, stop
   - no infinite spinner
   - details screen opens if record exists
   - bounded fallback if record does not exist

2a. Confirm which transport family was used during the session
   - ring-sport expected for the validated Luna Band path
   - logs show the live-data bean family actually received

3. Stop after pause / resume cycle
   - same as above

4. Device-initiated stop
   - app exits cleanly
   - no duplicate post-stop fetches

### Live HR flow

5. Start workout and confirm live HR updates on screen
   - first from the selected primary transport callback family
   - otherwise from fallback path

6. Confirm fallback HR stops after workout ends

### Post-stop sync robustness

7. Simulate no workout records returned
   - spinner still exits
   - user sees bounded fallback outcome

8. Simulate no internet during server sync
   - user gets explicit no-internet outcome
   - no spinner hang

9. Repeated back-to-back workouts
   - no stale record reuse
   - correct workout details mapping each time

---

## Risks and Mitigations

### Risk 1: Over-fixing and breaking the ring path

Mitigation:

- keep all new stop logic behind Luna Band / secondary-screen checks

### Risk 2: Fallback HR conflicts with proper workout data

Mitigation:

- use fallback only for HR display
- prefer the selected primary transport stream whenever it exists

### Risk 3: Timeout chosen too aggressively

Mitigation:

- keep retry/timeout values configurable
- log actual timings during staging validation

### Risk 4: Record arrives after timeout

Mitigation:

- treat late record arrival as a recoverable sync event, not as a reason to freeze the original UI
- optionally surface it later in activity history even if the immediate details screen already closed

---

## Final Implementation Summary

The complete fix is not "add one missing callback." It requires correcting the contract between five layers:

1. Luna Band workout transport selection
2. transport-specific stop/status handling
3. workout-history retrieval
4. service/session result routing
5. workout UI waiting behavior

The most important concrete changes are:

- stop forcing Luna Band onto the secondary-screen transport when the working demo path is ring-sport
- stop treating command acceptance as proof the workout record is ready
- stop calling raw `getFitnessSportIdsData(...)` directly from the stop-confirmation helper
- make `ZhUserActivityHandler` always emit a terminal result
- make the fragments stop waiting forever
- make live data transport-aware and add a controlled fallback only as resilience

If those changes are implemented together, both major user-reported failures should be fully addressed on the app side, and any remaining firmware-side gap will be isolated cleanly by logs instead of being masked by an infinite UI wait.
