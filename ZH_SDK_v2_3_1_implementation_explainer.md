# ZH SDK v2.3.1 Integration Explainer

This document explains how the `v2.3.1` ZH SDK additions were integrated into the app and the green-button `BlankTestFragment`, with special focus on:

- how sync works for the new data
- how the app avoids crashing when raw payloads are large
- how data conversion is handled
- what frequency/cadence information the SDK now provides
- what is already wired into the production pipeline vs what is currently debug-only

The goal is to make this easy to explain to a senior engineer without hand-waving.

---

## 1. Short answer I can say verbally

The safe summary is:

1. We upgraded to SDK `v2.3.1` and added the four new callback overrides required by the new AAR.
2. For `LUNA_BAND`, the normal production daily sync path now uses the new SDK mode-based API with `mode = 3` (`ALL`) instead of relying on the old legacy request.
3. For the high-volume continuous data that now carries `frequencyVersion`, we do not push raw second-level arrays directly through the old app pipeline. We normalize them back into the legacy 5-minute breakup shape that the existing DB, mapper, and graphs already expect.
4. For the new raw payloads like sleep RRI, sleep HRV, continuous RRI, and post-workout 5-minute heart rate, we capture and inspect them safely in the existing `BlankTestFragment` tooling instead of forcing them into production storage before the contracts are ready.
5. We prevent raw dump UI crashes by not binding massive JSON strings directly into the dashboard anymore. We keep bounded previews in the fragment and open the full payload in a paged bottom sheet only when needed.
6. We also fixed a server-sync issue where sleep-only payloads could be treated as “uploaded” even when nothing was actually posted.

That is the high-level architecture decision: keep production stable, add compatibility, normalize what the old pipeline can support, and expose the rest safely in debug tools.

---

## 2. What changed in SDK v2.3.1

From the SDK delta and the shipped AAR, the meaningful app-impacting changes are:

### 2.1 New daily sync API

The SDK adds:

- `getDailyHistoryData(mode, listener)`

Documented modes:

- `1` = today
- `2` = history
- `3` = all

The old no-mode API still exists.

### 2.2 New callbacks

The app had to add these new `FitnessDataCallBack` methods so the integration would compile:

- `onSleepRRIData(SleepRRIBean)`
- `onSleepHRVData(SleepHRVBean)`
- `onContinuousHeartRateSportFiveMinAfter(ContinuousHeartRateSportFiveMinAfterBean)`
- `onContinuousRRIData(ContinuousRRIDBean)`

### 2.3 Existing bean changes

Two existing continuous beans now include `frequencyVersion`:

- `ContinuousHeartRateBean`
- `ContinuousPressureBean`

Meaning used by the app:

- `frequencyVersion = 0` -> frequency is in minutes
- `frequencyVersion = 1` -> frequency is in seconds

This is the key reason a normalization layer became necessary. The older app pipeline mostly assumes legacy minute-based or 5-minute breakup data.

### 2.4 Daily calories fields

`DailyBean` now exposes richer calorie fields such as:

- `calorieData`
- `todayCalorieData`
- `todayOuraCalorieData`
- `todayOuraCalorieHourlyData`
- `todaySportCalorieData`
- `todaySportCalorieHourlyData`

The app now prefers the total calorie fields first and only falls back to Oura-style calorie fields if needed.

---

## 3. Where the integration lives

The implementation is intentionally split by responsibility.

### 3.1 SDK bridge layer

`ZhUserActivityHandler`

Responsibilities:

- call the correct SDK sync API
- receive SDK callbacks
- log and capture raw payloads
- forward production-supported payloads into the app callback pipeline

### 3.2 Conversion layer

`OreoDataConverter`

Responsibilities:

- convert SDK beans into the Oreo/Luna app models
- normalize second-based continuous data into the old 5-minute breakup format
- normalize sleep duration units when the incoming payload is minute-based
- resolve updated calorie fields safely

### 3.3 Test/raw storage layer

`WatchDataStore` / `WatchDataStoreImpl`

Responsibilities:

- hold temporary raw payload capture sessions
- store raw callback payloads for validation
- avoid introducing new DB tables just for debug inspection

### 3.4 Background sync + server upload layer

`OreoOnlineDataMapper` and `OreoSyncDataWork`

Responsibilities:

- build the combined server payload from local app data
- avoid treating “nothing posted” as a successful sync
- preserve sleep-only unsynced data when no combined payload was actually uploaded

### 3.5 Validation UI

`BlankTestFragment` + `RawSdkPayloadBottomSheet`

Responsibilities:

- manually trigger each daily sync mode
- inspect payload metadata
- preview raw JSON safely
- open full raw payloads in a controlled, paged bottom sheet

---

## 4. Exact sync behavior for the new implementation

## 4.1 Production sync path

The important production change is not just “there is a new button in the fragment.” The real fix is that the normal app sync path was updated.

Current behavior:

- `syncUserActivity(...)` first resolves a default daily-history mode based on device type.
- If the device is `LUNA_BAND`, the resolved mode is `ALL` (`3`).
- For non-`LUNA_BAND` devices, it still falls back to the legacy no-mode request.

Why this matters:

- Before this, the new mode-based daily sync was only being exercised from the debug fragment.
- After the fix, the real production sync for `LUNA_BAND` goes through the `v2.3.1` daily mode API and requests full daily data with `mode = 3`.

So if someone asks, “How are the new screenless daily payloads actually getting into the app during normal sync?”, the answer is:

- they come through the standard `syncUserActivity(...)` path
- that path now resolves to `getDailyHistoryData(3, ...)` for `LUNA_BAND`

## 4.2 Manual test fragment sync path

The fragment intentionally keeps four manual options:

- `DEFAULT` -> legacy no-mode call
- `TODAY` -> `mode = 1`
- `HISTORY` -> `mode = 2`
- `ALL` -> `mode = 3`

This lets you compare:

- legacy behavior
- today-only behavior
- history-only behavior
- full `v2.3.1` mode behavior

It is useful both for SDK validation and for showing a senior engineer exactly which request path was used when a raw payload was captured.

---

## 5. What happens when each new callback arrives

## 5.1 Continuous heart rate

`onContinuousHeartRateData(...)`

Current handling:

1. Raw JSON is saved into `WatchDataStore`.
2. For Luna devices, the payload is converted with `OreoDataConverter.parseHeartRateData(...)`.
3. If the payload is second-based, it is normalized into legacy 5-minute buckets.
4. The normalized breakup is then passed into the existing heart-rate pipeline.

This is already production-supported.

## 5.2 Continuous pressure / stress

`onContinuousPressureData(...)`

Current handling:

1. Raw JSON is saved into `WatchDataStore`.
2. For Luna devices, the payload is converted with `OreoDataConverter.parseStressData(...)`.
3. If the payload is second-based, it is normalized into legacy 5-minute buckets.
4. The normalized breakup is passed into the existing stress pipeline.

This is already production-supported.

## 5.3 Sleep RRI

`onSleepRRIData(...)`

Current handling:

1. Raw JSON is saved.
2. No production DB/server flow is attached yet.
3. It is exposed in the debug/test UI for validation and analysis.

Reason:

- the older production schema and server contracts do not yet support this raw structure end-to-end

## 5.4 Sleep HRV

`onSleepHRVData(...)`

Current handling:

1. Raw JSON is saved.
2. No production persistence or server upload path is attached yet.
3. It is available in the test fragment and raw bottom sheet.

## 5.5 Continuous RRI

`onContinuousRRIData(...)`

Current handling:

1. Raw JSON is saved.
2. Metadata such as `frequency` / `frequencyVersion` can be inspected in the fragment.
3. It is not yet wired into the old production DB/API contract.

## 5.6 Post-workout 5-minute heart rate

`onContinuousHeartRateSportFiveMinAfter(...)`

Current handling:

1. Raw JSON is saved.
2. It is exposed in the test fragment.
3. Validation logic was updated so the app looks at nested `hrList[*].heartRate` samples, not just the outer segment list.

This matters because otherwise the UI could incorrectly report “valid” payloads just because the outer wrapper existed, even when the nested heart-rate arrays were zero-only.

---

## 6. How the app handles high data volume without crashing

This is one of the most important points to explain.

## 6.1 What would have caused the crash

If we dump full raw payloads directly into multiple `TextView`s inside the main fragment dashboard, and those payloads are large or repeatedly refreshed, the UI thread does a lot of expensive text layout and rebind work.

That can lead to:

- heavy UI jank
- ANR
- fragment instability

The problem is not the BLE payload itself. The problem is trying to continuously render huge raw strings in the main scrolling screen.

## 6.2 The safety strategy used

Instead of binding full raw JSON into the dashboard:

1. The dashboard shows only a bounded preview.
2. The preview includes metadata:
   - how many captures arrived
   - how many were valid
   - which mode/session they belong to
   - short preview text
3. The full payload opens only when the user taps the card.
4. The full payload is shown in `RawSdkPayloadBottomSheet`.
5. The bottom sheet paginates the payload text instead of rendering one huge block into the main dashboard.

So the performance fix is basically:

- small dashboard preview
- on-demand full view
- paged rendering

## 6.3 Extra safety in raw capture storage

The raw payload capture is session-based.

When a manual sync starts:

- a new SDK raw capture session is created
- previous session keys are cleared
- the session has an expiry window of 10 minutes

This means the raw store does not keep accumulating unbounded callback history forever during testing. It captures the relevant sync window and then naturally stops accepting stale data.

## 6.4 Why this is better than storing everything permanently

For validation, we only need:

- what the callback delivered
- which sync mode/session produced it
- whether it contained meaningful values
- a safe way to inspect it

Using temporary raw capture sessions gives us all of that without:

- schema migration
- extra DB complexity
- permanent large local payload storage

---

## 7. How frequency and cadence are handled

This is another key senior-level topic.

## 7.1 What `frequencyVersion` means

For the updated continuous beans:

- `frequencyVersion = 0` means the `frequency` value is in minutes
- `frequencyVersion = 1` means the `frequency` value is in seconds

This means the exact cadence is dynamic and comes from the payload.

Examples:

- `frequency = 5`, `frequencyVersion = 0` -> every 5 minutes
- `frequency = 30`, `frequencyVersion = 1` -> every 30 seconds

## 7.2 Why this is a problem for the old pipeline

The older app pipeline expects legacy breakup shapes such as:

- one value per 5-minute bucket
- old graph logic built around those bucket sizes
- old DB/API contracts that do not store cadence metadata

If we directly passed second-based arrays into the old code, then:

- chart assumptions would be wrong
- server payload assumptions would be wrong
- database semantics would become inconsistent

## 7.3 The normalization strategy

For production-supported continuous HR and continuous pressure:

1. Read `frequency` and `frequencyVersion`.
2. Convert the incoming cadence into seconds.
3. Compare it with the legacy target cadence of 5 minutes.
4. If the source is faster than 5 minutes and divides evenly into 5 minutes, group samples into buckets.
5. Inside each bucket:
   - ignore `0`
   - ignore `255`
   - average the valid values
6. If a bucket has no meaningful values, output `0`.

So the app is not “dropping data randomly.” It is intentionally compressing higher-frequency data into the old 5-minute shape because that is what the existing production pipeline understands.

## 7.4 When normalization does not happen

If any of these are true, the original list is kept:

- values are empty
- frequency is invalid
- source cadence is already 5 minutes or slower
- the source cadence does not divide cleanly into the 5-minute target

That is a defensive choice to avoid corrupting data by forcing a bad aggregation.

## 7.5 Which payloads currently use this

Normalization is actively applied to:

- continuous heart rate
- continuous pressure / stress

It is not yet applied as a production persistence strategy for:

- sleep RRI
- sleep HRV
- continuous RRI
- post-workout 5-minute heart rate

Those remain primarily validation/debug data at this stage.

---

## 8. How sleep conversion is handled

The sleep side has an additional complication: older payloads may use minute-based durations while newer flows may use second-based durations.

## 8.1 Sleep unit detection

For `SleepBean`, the converter first checks whether the payload is using legacy minute units.

It does this by comparing:

- total sleep duration
- stage totals
- breakup totals

against the actual timestamp span of the sleep session.

If those values match the session duration in minutes within a tolerance, the app assumes the payload is using minute units.

## 8.2 Sleep normalization

If minute units are detected:

- the sleep totals are multiplied by `60`
- stage breakup durations are also converted to seconds

If the payload is already second-based:

- values are kept as-is

## 8.3 Why this matters

Without this unit normalization, sleep totals, stage durations, efficiency, and overlay windows could all be off by a factor of 60.

This is especially important because the sleep UI and server payloads work in second-based durations.

---

## 9. How calorie conversion is handled

`DailyBean` in `v2.3.1` adds more calorie-related fields, and the app had to choose which ones are trustworthy for the existing UI.

Current strategy:

- hourly calories prefer `calorieData[index]`
- if that is zero or missing, fall back to `todayOuraCalorieHourlyData[index]`
- total calories prefer `todayCalorieData`
- if that is not available, sum `calorieData`
- only then fall back to `todayOuraCalorieData`

Why:

- some observed payloads had zero Oura walking calories even though total calorie data existed
- using Oura-specific fields first would incorrectly show `0`

So the fix was to use the more general total calorie fields as the primary source.

---

## 10. How raw payload storage works

Raw payload storage is intentionally lightweight.

## 10.1 What gets stored

The debug raw store now has slots for:

- continuous heart rate
- continuous pressure
- sleep RRI
- sleep HRV
- continuous RRI
- post-workout heart rate after 5 minutes
- plus existing respiratory and dev-sport debug payloads

## 10.2 How it is stored

The store writes a `SdkRawCaptureEnvelope` containing:

- session id
- session start time
- update time
- mode
- label
- list of captured callback payloads

This is useful because one sync session can trigger multiple callbacks for the same payload type, and not all of them are necessarily meaningful. Some may be zero-only or partial.

## 10.3 Why multiple captures are useful

This lets the debug tooling answer questions like:

- how many callbacks were received?
- was the latest callback zero-only?
- was there an earlier valid callback in the same sync?
- which mode generated the payload?

That is a much better debugging story than storing only a single last-value string.

---

## 11. How the fragment decides what is “valid”

The fragment does not assume that “payload exists” means “data exists.”

It uses payload-aware validity checks:

- continuous HR: look for meaningful values in `heartRateData`
- continuous pressure: look for meaningful values in `pressureData` or `rriData`
- sleep RRI: look for meaningful values in `rri`
- sleep HRV: look for meaningful values in `hrv`
- continuous RRI: look for meaningful values in `rri`
- post-workout HR: look for meaningful values in nested `hrList[*].heartRate`

Meaningful values exclude zero-only noise.

This matters because the SDK can call back with structurally valid JSON that contains no useful physiological samples.

The fragment therefore prefers:

- latest valid capture, if available
- otherwise latest captured payload

That makes the validation screen much more honest and useful.

---

## 12. How server sync is protected from false success

There was a real regression risk here.

## 12.1 The old problem

If the mapper returned `null` for the combined payload, the worker could still see a `Resource.Success(null)` shape and continue as if a server sync succeeded.

That could cause:

- local rows marked as synced
- sleep cleanup executed
- server-sync success emitted

even though no combined payload had actually been posted.

## 12.2 The fix

Two protections were added:

### Mapper-side protection

The combined payload is now considered non-empty if `sleeps` is the only populated section.

That prevents valid sleep-only sync batches from being dropped before upload.

### Worker-side protection

The worker now checks whether the success response actually corresponds to a posted payload.

If nothing was posted:

- it does not mark local data as synced
- it does not run sleep cleanup
- it does not emit server sync success

This is important because otherwise the app could silently “lose” unsynced sleep from the production flow.

---

## 13. What data is production-ready right now vs debug-only

This is one of the clearest ways to explain the implementation boundary.

| Data type | Callback captured | Raw debug visible | Production conversion | Production DB/API contract |
|---|---|---|---|---|
| Daily data | Yes | Indirectly | Yes | Yes |
| Sleep data (`SleepBean`) | Yes | Via logs, model flow | Yes | Yes |
| Continuous heart rate | Yes | Yes | Yes, normalized | Yes, legacy breakup shape |
| Continuous pressure / stress | Yes | Yes | Yes, normalized | Yes, legacy breakup shape |
| Sleep RRI | Yes | Yes | No | No |
| Sleep HRV | Yes | Yes | No | No |
| Continuous RRI | Yes | Yes | Not fully rolled out | No |
| Post-workout 5-min HR | Yes | Yes | Debug validation only | No |

So if your senior asks “Did you fully integrate all new payloads into backend and DB?”, the honest answer is:

- no, not all of them end-to-end
- we fully added SDK compatibility and capture
- we productionized only the parts that could safely fit the existing contract
- the rest are intentionally exposed in debug until schema/API changes are reviewed

That is a deliberate risk-management decision, not an incomplete hack.

---

## 14. What frequency information we actually receive

It is important not to overclaim here.

## 14.1 Continuous heart rate

The payload contains:

- `heartRateData`
- `continuousHeartRateFrequency`
- `frequencyVersion`
- `date`

This lets us derive the actual sampling cadence from the payload itself.

## 14.2 Continuous pressure

The payload contains:

- `pressureData`
- `pressureFrequency`
- `frequencyVersion`
- sometimes `rriData`
- `date`

Again, cadence is derived from the payload.

## 14.3 Continuous RRI

The validation UI expects:

- `rri`
- `frequency`
- `frequencyVersion`
- `date`

So the cadence is also payload-driven here.

## 14.4 Sleep RRI / Sleep HRV

The current integration treats these as payloads with meaningful arrays:

- sleep RRI -> `rri`
- sleep HRV -> `hrv`

At this stage, the app does not rely on a production cadence model for these. They are captured and inspected as raw debug payloads.

## 14.5 Post-workout HR after 5 minutes

The payload uses nested segments:

- top-level `hrList`
- inside each segment, nested `heartRate`

The debug tooling now validates the nested heart-rate sample arrays, because those are the actual physiological data points.

---

## 15. What I would say if asked “How are you handling this much data?”

Use this answer:

We are handling the added `v2.3.1` data in two different ways depending on whether the existing app contract already supports it.

For continuous heart rate and continuous pressure, the SDK now sometimes returns higher-frequency payloads, including second-based cadence. The old app pipeline expects 5-minute breakup data, so I normalize the high-frequency samples back into 5-minute buckets before they enter the existing DB, mapper, and graph flow. That gives us compatibility without breaking older assumptions.

For the newly introduced raw payloads like sleep RRI, sleep HRV, continuous RRI, and post-workout 5-minute heart rate, I did not force them into production persistence yet because the current schema and backend contracts do not carry those structures cleanly. Instead, I added full raw capture and validation support in the existing `BlankTestFragment`.

To avoid crashes from large raw dumps, I do not bind full payload strings directly into the main dashboard. The fragment only shows metadata and short previews. Full payloads are opened on demand in a separate bottom sheet, and that screen paginates the text. So I can still inspect the raw SDK data without overloading the main UI thread.

---

## 16. What I would say if asked “Did you lose data by averaging?”

Use this answer:

I only average in the specific path where the new SDK returns higher-frequency continuous HR or pressure data but the production app still expects the legacy 5-minute breakup format. In that case, I aggregate the faster samples into 5-minute buckets so the existing pipeline remains consistent.

Inside each bucket, I ignore placeholder values like `0` and `255`, then average the valid samples. If a bucket has no meaningful samples, I store `0`. If the cadence does not divide cleanly into the 5-minute target, I do not force aggregation and I keep the original list.

So the averaging is controlled, compatibility-driven, and defensive. It is not a blind reduction of all payloads.

---

## 17. Remaining limitations and follow-up work

This is the honest “not done yet” section.

### 17.1 Not yet persisted end-to-end

The app does not yet persist these concepts through the full production stack:

- raw `frequencyVersion`
- second-based cadence metadata
- raw sleep RRI structure
- raw sleep HRV structure
- raw continuous RRI structure
- post-workout 5-minute HR structure

### 17.2 What a full production rollout would require

A true end-to-end rollout would need review and possibly migration work across:

- Room entities
- DB migrations
- repository/use-case wiring
- network contracts
- backend acceptance
- graph time-axis logic
- summary calculations

### 17.3 Why it was not done in this pass

Because that would be a much broader behavioral change with higher regression risk. This pass focused on:

- SDK compatibility
- production-safe sync fixes
- backward-compatible normalization
- debug visibility for new payloads

That was the safer migration strategy.

---

## 18. Code references

These are the main files to cite in a walkthrough:

- `noisefit_zh_sdk/.../ZhUserActivityHandler.kt`
  - default `LUNA_BAND -> ALL mode`
  - new callback overrides
  - raw payload capture
  - sync request path

- `noisefit_zh_sdk/.../OreoDataConverter.kt`
  - continuous HR normalization
  - continuous pressure normalization
  - sleep unit normalization
  - calorie field resolution

- `commons/.../WatchDataStore.kt`
  - raw capture contracts and envelope models

- `commons/.../WatchDataStoreImpl.kt`
  - 10-minute raw capture session
  - envelope-based raw capture accumulation

- `app/.../BlankTestFragment.kt`
  - manual sync mode buttons
  - bounded previews
  - payload metadata summaries

- `app/.../RawSdkPayloadBottomSheet.kt`
  - latest-valid payload selection
  - nested sport-HR validity handling
  - paged rendering for large raw payloads

- `app/.../OreoOnlineDataMapper.kt`
  - combined payload emptiness handling
  - sleep-only payload support

- `app/.../OreoSyncDataWork.kt`
  - skip sync completion when nothing was actually posted

---

## 19. Final takeaway

The best way to describe the implementation is:

- I upgraded the BLE bridge to understand the new `v2.3.1` callbacks and sync modes.
- I moved normal `LUNA_BAND` sync onto the new mode-based daily-history request with `ALL`.
- I normalized new higher-frequency continuous HR and stress data back into the legacy 5-minute format so the existing production app could continue working safely.
- I captured the rest of the new raw payloads in a controlled debug flow instead of prematurely forcing them into production storage.
- I made raw payload inspection safe by moving large dumps out of the main fragment and into a paged bottom sheet.
- I also hardened the server sync flow so sleep data is not falsely marked synced when nothing was actually uploaded.

That is the real implementation story: compatibility first, controlled normalization where needed, debug visibility for the new payloads, and no unsafe raw-data rendering in the main UI.
