# Post-Workout HR (5-Min After Exercise) Implementation Explainer

This document is focused only on the new SDK callback:

- `onContinuousHeartRateSportFiveMinAfter(ContinuousHeartRateSportFiveMinAfterBean)`

It explains:

- what is confirmed from the code and shipped AAR
- whether this is fetched during sync or through a separate live path
- when you should expect this data to exist
- how it is currently handled in the app
- how production rollout would likely work

---

## 1. Final confirmed answer

The most accurate answer is:

**This is post-workout recovery data in meaning, but in the current app and SDK integration it is still received through the normal sync callback flow, not through a separate dedicated live “after workout” fetch API.**

So:

- yes, the data only makes sense **after a workout scenario**
- but no, the app does not currently fetch it through a special “workout ended, now get post-workout HR” path
- in the current implementation, you should expect to inspect it **during sync / after sync**, not from a separate standalone workout-only retrieval flow

---

## 2. What is confirmed from the shipped SDK AAR

I checked the actual `v2.3.1` AAR.

### 2.1 The callback exists

The SDK `FitnessDataCallBack` interface includes:

- `onContinuousHeartRateSportFiveMinAfter(ContinuousHeartRateSportFiveMinAfterBean)`

So this is a real SDK-delivered callback, not an app invention.

### 2.2 There is no dedicated fetch API for it in `ControlBleTools`

I also checked the SDK’s exposed `ControlBleTools` methods.

Relevant methods include:

- `getDailyHistoryData(listener)`
- `getDailyHistoryData(mode, listener)`
- `getAutoSportData(listener)`
- `getFitnessSportIdsData(listener)`

What is important:

- there is **no dedicated method** like:
  - `getContinuousHeartRateSportFiveMinAfter(...)`
  - `getPostWorkoutHeartRate(...)`
  - `getRecoveryHeartRate(...)`

That is a strong signal that the SDK expects this data to be delivered as part of the broader fitness/history sync pipeline, not through a standalone request.

### 2.3 Bean structure confirms it is stored recovery data, not a single live sample

The bean contains:

- `hrList`

Each entry contains:

- `startTimestamp`
- nested `heartRate: List<Int>`

That means the payload is not just “one immediate HR number.”

It is structured historical data:

- one or more segments
- each segment has a start timestamp
- each segment contains heart-rate sample arrays

So the SDK shape itself looks like retrieved stored data, not a one-time live event callback.

---

## 3. What is confirmed from the app code

## 3.1 The callback is captured, but not integrated into production models yet

In `ZhUserActivityHandler`, the callback currently does this:

1. receives `ContinuousHeartRateSportFiveMinAfterBean`
2. converts it to raw JSON
3. stores it in the test raw store

It does **not**:

- convert it into a production app entity
- send it into a repository
- upload it to server
- drive a production chart or workout detail screen

So the current app handling is:

- **capture only**
- **debug/test UI only**

This is very important.

## 3.2 It is not wired to a special live workout event path

I checked for any separate app handling that would imply:

- when workout ends -> fetch recovery HR
- after 5 minutes -> trigger dedicated API
- on workout details open -> fetch post-workout HR

There is no such wiring in the current app code.

So the current implementation does **not** treat it as a live workout-end event.

## 3.3 The current sync request path is still the normal sync path

`syncUserActivityByMode(...)` currently triggers:

- `requestDailyHistoryData(mode)`
- `getAutoSportData(null)`
- `getFitnessSportIdsData(null)`

Since the post-workout HR callback exists only as part of `FitnessDataCallBack`, and there is no dedicated fetch method, the most reasonable confirmed conclusion is:

- the app receives this callback during the broader sync/history retrieval flow

That matches the way the current implementation is written.

---

## 4. So when will this data actually exist?

This needs a careful answer because there are two separate questions:

1. when does the **device generate/store** this data?
2. when does the **app receive** this data?

## 4.1 When the device can have this data

The meaning of the callback is:

- heart rate data for the period **5 minutes after exercise/workout**

So the device can only have meaningful data here if:

- a qualifying workout happened
- the watch/ring actually recorded the post-workout recovery HR data
- the recovery period data was stored successfully on the device/firmware side

So yes:

- you should look for this **in workout-related situations**
- if there was no workout, or no recovery data was recorded, you should expect it to be empty or zero-only

## 4.2 When the app receives it

In the current implementation, the app receives it through **sync callback flow**.

That means:

- not “immediately only because workout just ended”
- but “when sync fetches data and the SDK emits that callback”

So the clean practical answer is:

- **generate condition** = after a workout / post-workout recovery period
- **transport condition into app** = during sync

This is the most precise explanation.

---

## 5. Should you look for it only after workout?

Yes, in terms of whether meaningful data should exist.

But not necessarily in a separate place or separate live event path.

### Correct expectation

If you want to validate this payload, you should:

1. perform a workout that should produce post-workout recovery HR
2. allow the post-workout period to happen
3. then run sync
4. inspect the callback/raw payload during or after that sync

### Wrong expectation

You should **not** assume:

- it will appear instantly in a special “live after-workout panel”
- it has its own separate fetch button/API
- it is handled exactly like a workout-progress live stream

That is not how the current app is wired.

So:

- **look for the data after a workout scenario**
- **but inspect it through sync/callback capture**

---

## 6. What the current debug implementation does

## 6.1 Capture behavior

When `onContinuousHeartRateSportFiveMinAfter(...)` is called:

- the raw JSON is saved to `WatchDataStore`
- the fragment can show it in:
  - preview card
  - raw bottom sheet

## 6.2 Validity behavior

The payload is not treated as valid merely because `hrList` exists.

The debug UI checks:

- nested `hrList[*].heartRate` samples

This was explicitly fixed because otherwise:

- the app could wrongly treat a payload as valid when only segment wrappers existed but all nested HR samples were zero

So for post-workout HR, validity means:

- there are actual meaningful nested heart-rate values

## 6.3 Selected payload behavior

If the latest callback is zero-only but an earlier callback in the same session had valid nested HR samples, the debug UI prefers:

- **latest valid payload**

not just:

- literal latest callback

That makes the validation screen more useful.

---

## 7. Does auto sync fetch it like earlier things in the app?

## 7.1 In transport terms: mostly yes

If you productionize this, the fetch mechanism would most naturally stay similar to existing sync-based health data:

- sync request goes out
- SDK emits callback
- app handles callback

That is already how the current implementation receives it.

So in transport/fetching style, yes, it is closer to:

- other sync-delivered historical health data

than to:

- a special live workout-only feature

## 7.2 In production integration level: not yet

Right now it is **not** handled like the older production-integrated data in the later stages of the pipeline.

Unlike steps/sleep/HR/stress, it is not yet:

- normalized into a production data model
- stored in DB
- mapped to server upload
- rendered in production screens

So the correct statement is:

- **fetching style is similar to auto sync**
- **downstream integration is not yet similar to older fully productionized data**

That distinction is important.

---

## 8. What to tell a senior engineer

Use this exact explanation:

The new post-workout HR callback represents recovery heart-rate data collected for the period five minutes after exercise. In the current SDK and app integration, there is no separate dedicated fetch API for this payload. The SDK exposes it only as a `FitnessDataCallBack` callback, and in our app it is currently captured during the normal sync/history callback flow, not through a separate live workout-end retrieval path.

So if I want to validate it, I need a workout scenario first, because that is when the device would have meaningful recovery data to store. But I inspect and receive it through sync, not through a special standalone post-workout fetch.

In the current implementation, I only save the raw payload and expose it in `BlankTestFragment`; I have not yet rolled it into the production DB/server/chart pipeline.

---

## 9. What you should do when testing it

If you want to test this callback properly, the expected workflow is:

1. perform a workout that should generate recovery HR data
2. wait for the post-workout recovery window to pass
3. trigger sync
4. inspect the post-workout HR raw card / bottom sheet
5. confirm nested `hrList[*].heartRate` contains meaningful values

You should not rely only on:

- opening the screen without syncing
- looking immediately during workout
- expecting a separate post-workout API call

---

## 10. Why the current app does not prove a live path

There are three reasons I am confident saying this is sync-fetched in the current app:

1. the SDK exposes the data as a callback in `FitnessDataCallBack`
2. the SDK does not expose a dedicated fetch API for it in `ControlBleTools`
3. the app only captures it inside the existing sync callback handler and nowhere else

So this is not guesswork from naming only. It is the behavior supported by the current code structure.

---

## 11. Caveat to state honestly

What I can confirm from the current code is:

- how the app receives it
- how the SDK exposes it
- how the payload is currently handled

What I cannot guarantee from app code alone is:

- the exact firmware rule for when the device decides to generate/populate this payload
- whether every workout type produces it
- whether there are vendor-side restrictions such as minimum workout duration or supported modes

So the honest final statement is:

- from the app side, treat it as sync-fetched recovery data
- from the product/firmware side, validate on real device which workout scenarios actually populate it

---

## 12. Final takeaway

The best single-sentence answer is:

**Post-workout HR is “after workout” in meaning, but “during sync” in transport in the current app.**

That means:

- yes, you should test it after workout-related scenarios
- no, you should not look for a separate special live fetch path in the current implementation
- and if this is productionized later, its fetch entry point will most likely stay similar to other auto-sync-delivered data, unless a new dedicated SDK/live API is added in the future
