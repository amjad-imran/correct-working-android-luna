# Workout Zone Mapping Verdict and Fix Plan

Date: 2026-04-23

Scope:
- Workout zone 1/2/3/4 mapping
- Full zone-duration behavior in post-workout V2
- Behavior when exact zone values are zero
- Whether some workouts can look correct and others incorrect from app side
- Final verdict: SDK issue vs firmware issue vs app issue
- No code changed

## 1. SDK source of truth

Relevant callback:
- `SportCallBack.onDevSportInfo(DevSportInfoBean data)` in `ZH Android SDK v2.3.2.txt:3111-3114`

Relevant SDK fields:
- `reportHeartWarmUp` in `ZH Android SDK v2.3.2.txt:3617-3621`
- `reportHeartFatBurning` in `ZH Android SDK v2.3.2.txt:3612-3616`
- `reportHeartAerobic` in `ZH Android SDK v2.3.2.txt:3607-3611`
- `reportHeartAnaerobic` in `ZH Android SDK v2.3.2.txt:3602-3606`
- `reportHeartLimitTime` in `ZH Android SDK v2.3.2.txt:3597-3601`
- `reportDuration` in `ZH Android SDK v2.3.2.txt:3476-3480`

Canonical mapping from the SDK doc:
- Zone 1 = warm up = `reportHeartWarmUp`
- Zone 2 = fat burning = `reportHeartFatBurning`
- Zone 3 = aerobic = `reportHeartAerobic`
- Zone 4 = anaerobic = `reportHeartAnaerobic`
- Zone 5 = limit = `reportHeartLimitTime`

Important conclusion:
- The SDK already provides exact duration fields for workout zones.
- Those fields are the correct mapping source when present and valid.

## 2. App mapping from SDK to local workout model

Local parse:
- `DataConverter.parseRecordedData(...)` in `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt:1141-1174`

Exact field mapping in app code:
- `reportHeartWarmUp -> RecordedWorkoutData.hrWarmUp`
- `reportHeartFatBurning -> RecordedWorkoutData.hrFatBurning`
- `reportHeartAerobic -> RecordedWorkoutData.hrAerobic`
- `reportHeartAnaerobic -> RecordedWorkoutData.hrAnaerobic`
- `reportHeartLimitTime -> RecordedWorkoutData.hrLimitTime`

Model fields:
- `RecordedWorkoutData` defines these zone-duration fields in `commons/src/main/java/com/noisefit_commans/data/model/OreoDbTable.kt:104-109`

Verdict on SDK-to-local mapping:
- This mapping is correct.
- I did not find a swapped zone assignment in the local parse layer.

## 3. How post-workout V2 actually computes zone UI

Zone generation:
- `OWorkoutDetailsViewModelV2.generateHrZones(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:482-669`

### 3.1 Threshold calculation

App computes age-based heart-rate thresholds:
- `HRmax = 208 - 0.7 * age` at `496`
- Zone 1 = `50-60%` at `497-500`
- Zone 2 = `60-70%` at `502-504`
- Zone 3 = `70-80%` at `506-508`
- Zone 4 = `80-90%` at `510-512`
- Zone 5 = `90-100%` at `514-516`

Important conclusion:
- The app uses age-based thresholds to derive zone indexes from `hrArray`.
- This threshold logic is app logic, not the SDK's explicit exact-duration source.

### 3.2 Index selection

Zone highlight indexes are chosen from `hrArray`:
- value-to-zone assignment at `519-548`
- `0` and `255` are treated as invalid for zone placement
- non-zero values below Zone 1 become the app-only "restorative" zone

Important conclusion:
- The colored highlighted segments on the HR chart are based on server `hrArray`, not on SDK zone-duration fields.

### 3.3 Duration selection

Sample interval assumption:
- `hrIntervalInSecond = 30L` at `492`

Fallback sample durations:
- calculated from index counts at `551-559`

Exact local durations preferred when available:
- Zone 1 uses `recordedWorkout?.hrWarmUp` at `561`
- Zone 2 uses `recordedWorkout?.hrFatBurning` at `562-563`
- Zone 3 uses `recordedWorkout?.hrAerobic` at `564`
- Zone 4 uses `recordedWorkout?.hrAnaerobic` at `565-566`
- Zone 5 uses `recordedWorkout?.hrLimitTime` at `567`

Fallback rule:
- `resolveZoneDuration(exactDuration, fallbackDuration)` at `671-673`
- if exact duration is `> 0`, use exact
- otherwise use the `hrArray`-derived fallback duration

Important conclusion:
- Post-workout V2 durations are exact when matched local zone fields are present and positive.
- Otherwise V2 silently falls back to inferred durations from `hrArray`.

### 3.4 Live workout zone display

Live workout V2 uses a different path:
- thresholds are prepared in `RecordWorkoutV2ViewModel.setupZoneId()` in `app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutV2ViewModel.kt:74-84`
- the current zone is selected by `getHeartRateZone(currentBpm)` in `.../RecordWorkoutV2ViewModel.kt:571-585`
- the fragment displays that zone in `RecordWorkoutFragmentV2.updateWorkoutHeartRate(...)` at `app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt:420-445`

Important conclusion:
- live workout zone display is purely app-estimated from current BPM and age-based thresholds
- it does not use the exact SDK workout summary zone-duration fields

This means:
- live zone display and post-workout zone-duration display are not the same data source
- some difference between live zone visuals and post-workout exact durations is expected by design

## 4. What happens if all exact zone values are 0

## 4.1 If `hrArray` has valid heart-rate values

Behavior:
- each zone duration falls back to `selectedIndexes.size * 30 seconds`
- this happens because `resolveZoneDuration(...)` uses the fallback whenever exact duration is zero

Result:
- zones still show non-zero durations if `hrArray` supports them
- those durations are inferred, not exact SDK durations

## 4.2 If `hrArray` has no valid values

Behavior:
- sample zone durations are zero
- tracked zone total becomes zero
- restorative duration uses `resolveRestorativeDuration(...)` at `675-686`

If workout duration is positive:
- `trackedZoneDuration <= workoutDuration` is true
- restorative becomes `workoutDuration - 0`
- zone 1 to 5 remain `0`
- restorative becomes the full workout duration

This is the exact code behavior when all exact zone fields are zero and the app cannot infer zones from valid HR samples.

## 5. App-only "restorative zone"

The SDK does not provide a dedicated restorative-zone field.

In V2, restorative is synthetic:
- index collection happens for values below zone 1 at `541-544`
- duration is resolved by `resolveRestorativeDuration(...)` at `675-686`
- percentage is computed as another displayed zone at `590-603`

Important conclusion:
- Restorative is app-generated, not a direct SDK field.

## 6. Can some workouts be correct and others incorrect purely because of app behavior?

Yes.

There are two app-side reasons.

### 6.1 Reason 1: exact zone durations are only available when the latest local workout matches

Matching logic:
- `getMatchedRecordedWorkout(...)` in `app/src/main/java/com/oreo/ui/workout/details/OWorkoutDetailsViewModelV2.kt:251-270`

What this means:
- if the viewed workout matches the latest cached `RecordedWorkoutData`, V2 can use exact SDK zone durations
- if it does not match, V2 falls back to age-based `hrArray` inference

So:
- one workout can show exact SDK zone durations
- another workout can show inferred zone durations
- that difference can happen without any firmware defect

This is a real app-side source of inconsistency across workouts.

### 6.2 Reason 2: the app trusts impossible exact durations

No validation or clamping exists before display:
- zone durations are used directly in `calculateZonePercentage(...)` at `688-693`
- total duration prefers `recordedWorkout.durationSeconds` at `576-581`

What this means:
- if exact zone durations are larger than the workout duration, V2 still uses them
- percentages can become nonsensical
- the app does not reject or correct impossible zone totals

So:
- impossible zone times can be made worse by app display behavior even if the original bad numbers came from upstream

## 7. Final root-cause verdict

## 7.1 Is this an SDK field-mapping issue?

Verdict:
- No.

Reason:
- The SDK field names and meanings are explicit.
- The app maps them correctly into `RecordedWorkoutData`.
- I found no evidence that Zone 1/2/3/4 are swapped during the SDK-to-local parse.

## 7.2 Is this a firmware or upstream-payload issue?

Verdict:
- Yes, if the exact `reportHeart*` durations themselves are impossible.

Examples of impossible upstream data:
- one zone duration greater than full workout duration
- sum of exact zones greater than workout duration
- exact zone values inconsistent with the recorded workout length

Reason:
- the app does not invent those exact values
- it receives them through `DevSportInfoBean` and copies them directly

## 7.3 Is there also an app issue?

Verdict:
- Yes.

App issue A:
- only the latest workout can provide exact local zone durations to V2
- unmatched workouts fall back to inferred `hrArray` zones

App issue B:
- impossible exact zone durations are not validated or clamped

App issue C:
- the app mixes two different semantics in one UI:
  - exact SDK duration fields for displayed duration
  - age-based `hrArray` inference for zone highlight positions

This mixed-source design can produce workouts that look internally inconsistent.

## 8. Proof summary

Proof that mapping itself is correct:
- SDK meanings in `ZH Android SDK v2.3.2.txt:3597-3621`
- app parse in `DataConverter.kt:1170-1174`

Proof that some workouts can fall back:
- `getMatchedRecordedWorkout(...)` in `OWorkoutDetailsViewModelV2.kt:251-270`
- `resolveZoneDuration(...)` in `OWorkoutDetailsViewModelV2.kt:671-673`

Proof that impossible values are not guarded:
- direct usage in `generateHrZones(...)` and `calculateZonePercentage(...)` at `561-693`

## 9. Recommended implementation plan

No code was changed in this audit. This is the implementation plan only.

### 9.1 Make exact local workout matching reliable

Recommended change:
- stop relying on a single latest cached workout
- persist exact SDK workout summaries in a keyed store or table
- match by a stable workout identity first
- if no stable id exists, match by date plus precise start/end timestamps with a small tolerance

Why:
- this removes the current "some workouts use exact zones, some use fallback zones" inconsistency

### 9.2 Validate exact zone durations before display

Recommended rule:
- reject exact zone data when any exact duration is negative
- reject exact zone data when any exact duration exceeds total workout duration
- reject exact zone data when sum of exact zone durations exceeds total workout duration

Recommended fallback when exact data is invalid:
- if `hrArray` is valid, derive durations from `hrArray`
- if `hrArray` is not valid, show zero for zones 1-5 and do not synthesize misleading percentages

Why:
- impossible values must not be shown as trusted output

### 9.3 Use a single source-of-truth flag per workout

Recommended behavior:
- for each workout, decide once whether zone durations come from:
  - exact SDK fields, or
  - inferred `hrArray`
- surface that decision in debug logs

Why:
- mixed semantics are harder to audit and harder to explain to firmware teams

### 9.4 Add explicit tests

Recommended tests:
- exact valid zone durations are displayed as-is
- exact all-zero durations with valid `hrArray` fall back to inferred durations
- exact impossible durations are rejected and fall back
- unmatched local workout falls back to inferred durations
- matched older workouts still use exact durations after persistence redesign

## 10. Bottom-line verdict

Field mapping verdict:
- correct

SDK issue verdict:
- not supported by code evidence

Firmware or upstream data issue verdict:
- likely when exact zone durations themselves are impossible

App issue verdict:
- definitely yes, because V2:
  - uses fragile matching to decide exact-vs-fallback behavior
  - does not validate impossible exact zone values
  - mixes exact durations with inferred zone highlight positions
