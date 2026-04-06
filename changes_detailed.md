# ZH SDK v2.3.1 Migration Detailed Notes

## 1. Verified SDK delta

### 1.1 Public API additions in the new AAR
The `v2.3.1` AAR adds one new daily-data sync entry point:

- `ControlBleTools.getDailyHistoryData(int mode, SendCmdStateListener listener)`

Supported documented modes:
- `1`: today only
- `2`: history
- `3`: all

The existing no-argument mode path still exists:
- `ControlBleTools.getDailyHistoryData(SendCmdStateListener listener)`

### 1.2 `FitnessDataCallBack` additions
The `v2.3.1` AAR adds four required callback methods:

- `onSleepRRIData(SleepRRIBean bean)`
- `onSleepHRVData(SleepHRVBean bean)`
- `onContinuousHeartRateSportFiveMinAfter(ContinuousHeartRateSportFiveMinAfterBean bean)`
- `onContinuousRRIData(ContinuousRRIDBean bean)`

These are compile-impacting additions because the app implements `FitnessDataCallBack`.

### 1.3 Existing bean changes
The following existing beans add `frequencyVersion`:

- `ContinuousHeartRateBean`
  - existing `continuousHeartRateFrequency`
  - new `frequencyVersion`
- `ContinuousPressureBean`
  - existing `pressureFrequency`
  - new `frequencyVersion`

Meaning:
- `frequencyVersion = 0`: frequency is in minutes
- `frequencyVersion = 1`: frequency is in seconds

### 1.4 New beans present in the `v2.3.1` AAR
- `SleepRRIBean`
- `SleepHRVBean`
- `ContinuousHeartRateSportFiveMinAfterBean`
- `ContinuousRRIDBean`

### 1.5 Verified documentation mismatch
The SDK release note line about screenless wristband motion point recording was checked against the shipped AAR.

Result:
- `DevSportInfoBean.RingPointData` is unchanged between `v2.3.0` and `v2.3.1`.
- This release-note item is not an actual binary contract change for the app migration.

## 2. App impact assessment

### 2.1 Compile-critical impact
The main compile blocker after replacing the AAR is the existing `FitnessDataCallBack` implementation in:

- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

Without adding the four new overrides, the app will not compile against `v2.3.1`.

### 2.2 Safe first-pass impact
The new callback payloads are not yet part of the older Luna production data pipeline, DB schema, or backend contracts.

That means the safe first pass is:
- keep production behavior unchanged where possible
- add compatibility in the wrapper
- expose new payloads in the existing green-button debug UI
- avoid schema/API refactors until real-device validation confirms payload shape and cadence

### 2.3 Production data-model limitation
The current production entities only store `breakUp` strings for heart rate, stress, body stress, respiratory, and similar data.

They do not store:
- `frequencyVersion`
- sample unit metadata
- alternate second-based cadence

Because of that, a full production rollout of frequency-aware data would require a broader follow-up touching:
- Room entities and migrations
- repository/use-case wiring
- online request mapping
- chart and summary logic

This migration intentionally does not take that broader step yet.

### 2.4 Pairing crash root cause found after initial migration
After the first `v2.3.1` integration build, physical-device pairing crashed after BLE service discovery and notification setup.

Observed fatal error:
- `NoClassDefFoundError: com.google.protobuf.RuntimeVersion$RuntimeDomain`

Crash location from logs:
- `com.zh.ble.wear.protobuf.CommonProtos$SEBLEConnectParameterConfig.<clinit>`
- `ControlBleTools.nativeMethodGetMtu(...)`

What this means:
- the crash is not caused by the new callback implementations
- the crash is not caused by the green-button debug UI
- the crash happens when the SDK loads protobuf-generated classes during the connect/MTU path

Verified cause:
- the app was still resolving `com.google.protobuf:protobuf-java:3.22.3`
- that runtime does not contain `RuntimeVersion$RuntimeDomain`
- the shipped `v2.3.1` AAR bytecode directly references `RuntimeVersion.validateProtobufGencodeVersion(...)`
- the cloned official `ZH_SDK_20260402_V2.3.1` demo uses `com.google.protobuf:protobuf-java:4.34.1`

Fix chosen:
- update the existing version-catalog protobuf runtime from `3.22.3` to `4.34.1`
- keep using full `protobuf-java`
- do not switch to `protobuf-javalite`

## 3. Implementation plan and status

### Phase 1: AAR upgrade
Status: completed

Changes:
- Updated the wrapper module reference from `ZH_SDK_20260204_V2.3.0.aar` to `ZH_SDK_20260402_V2.3.1.aar`.
- Copied the new AAR into `noisefit_libraries_zh/zh_app/`.
- Updated the version-catalog protobuf runtime from `3.22.3` to `4.34.1` after pairing-crash diagnosis.

Why this location:
- The app already consumes ZH through `noisefit_libraries_zh`.
- Keeping the asset swap here avoids any SDK source usage.
- The app already resolves protobuf through `libs.versions.toml` and `implementation libs.protobuf`, so changing the shared version catalog is the narrowest safe runtime fix.

### Phase 2: Wrapper compatibility and new callback capture
Status: completed

Changes:
- Added app-side support for the four new callback methods in `ZhUserActivityHandler`.
- Added an in-place extension method on `UserActivityDataActions`:
  - `syncUserActivityByMode(date, mode)`
- Overrode that method in the ZH handler to call the new SDK overload when a mode is provided.
- Preserved the old `syncUserActivity()` behavior by routing it to the default legacy request.

Why this location:
- `ZhUserActivityHandler` is already the app’s BLE-to-app bridge.
- `UserActivityDataActions` is already the existing abstraction exposed to the app layer.

### Phase 3: Lightweight raw-payload storage
Status: completed

Changes:
- Reused the existing `WatchDataStore` test/raw storage pattern.
- Added raw payload slots for:
  - continuous heart rate
  - continuous pressure
  - sleep RRI
  - sleep HRV
  - continuous RRI
  - 5-minute post-workout heart rate

Why this location:
- The app already stores raw Luna-band SDK payloads in `WatchDataStore` for validation.
- This avoided a new repository, DB table, or feature-specific storage abstraction.

### Phase 4: Green-button debug UI exposure
Status: completed

Changes:
- Reused `BlankTestFragment`, which is the green-button validation screen already used for Luna-band work.
- Added daily sync mode controls:
  - default
  - today
  - history
  - all
- Added metadata summaries for the frequency-bearing payloads.
- Added raw dump panels for the new screenless-band payloads.

Why this location:
- The user requested that new SDK features be implemented in the existing green-button UI whenever possible.
- This keeps the migration localized and avoids unnecessary changes to user-facing production screens.

### Phase 5: Production frequency-aware rollout
Status: intentionally pending

Not implemented in this pass:
- Room entity changes for `frequencyVersion`
- network mapper changes to send second-based cadence
- chart/time-axis changes in production summary/readiness/sleep screens

Reason:
- current app schema and payload contracts do not carry the new frequency metadata
- changing them now would increase regression risk significantly
- the debug UI now provides the needed visibility to validate real device behavior before broader rollout

## 4. Exact file changes

### 4.1 `noisefit_libraries_zh/build.gradle`
Purpose:
- switch the local SDK artifact reference to `ZH_SDK_20260402_V2.3.1.aar`

### 4.0 `gradle/libs.versions.toml`
Purpose:
- align the app's existing full protobuf runtime with the `v2.3.1` AAR requirement

Change:
- `protobuf-java` version updated from `3.22.3` to `4.34.1`

### 4.2 `noisefit_libraries_zh/zh_app/ZH_SDK_20260402_V2.3.1.aar`
Purpose:
- place the new SDK asset in the exact wrapper location already used by the project

### 4.3 `commons/src/main/java/com/noisefit_commans/interfaces/data/UserActivityDataActions.kt`
Purpose:
- add `syncUserActivityByMode(date, mode)` as the narrowest existing app-layer hook for the new SDK overload

Notes:
- no new abstraction tree was introduced
- default implementation falls back to the old sync method

### 4.4 `commons/src/main/java/com/noisefit_commans/data/local/abstraction/WatchDataStore.kt`
Purpose:
- extend the existing raw/test storage contract with six new payload families

Added getters/setters for:
- continuous heart rate raw JSON
- continuous pressure raw JSON
- sleep RRI raw JSON
- sleep HRV raw JSON
- continuous RRI raw JSON
- post-workout heart-rate raw JSON

### 4.5 `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`
Purpose:
- implement the new `WatchDataStore` methods using the existing SharedPreferences pattern

Notes:
- no DB schema change
- no new storage mechanism

### 4.6 `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`
Purpose:
- make the ZH wrapper compile and behave correctly with `v2.3.1`

Changes:
- imported the new bean classes
- added the four required `FitnessDataCallBack` overrides
- saved raw payload JSON for the new screenless-band data
- saved raw JSON for the modified continuous heart-rate and pressure payloads so `frequencyVersion` can be inspected in the app
- added a helper for mode-based daily history requests
- routed the legacy `syncUserActivity()` path through the existing default behavior

What was not changed:
- no SDK source
- no existing production callback dispatch behavior was removed

### 4.7 `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt`
Purpose:
- extend the existing green-button Luna debug screen instead of creating a new screen

Changes:
- added a sync-mode status string and sync-button handlers
- added mode-specific sync requests through `CommonGlobals.userActivityDataActions`
- added raw JSON pretty-print helpers
- added metadata summary helpers for frequency-bearing payloads
- extended the existing periodic dashboard refresh to populate the new text blocks

### 4.8 `app/src/main/res/layout/fragment_blank_test.xml`
Purpose:
- extend the existing layout in place

Added sections:
- SDK `v2.3.1` daily sync mode controls
- SDK `v2.3.1` screenless data summary block
- raw payload panels for:
  - continuous heart rate
  - continuous pressure
  - sleep RRI
  - sleep HRV
  - continuous RRI
  - post-workout heart rate after exercise

### 4.9 `changes.md`
Purpose:
- concise migration summary

### 4.10 `changes_detailed.md`
Purpose:
- detailed migration scope, plan, and implementation notes

### 4.11 `sdk_v2_3_1_non_technical.md`
Purpose:
- simple-language explanation for non-technical stakeholders

## 5. Assets and dependency notes

### 5.1 AAR assets found
The `v2.3.1` AAR was located at:
- project root asset copy
- cloned SDK demo `app/libs/`
- app repo root asset copy

The wrapper module now uses the local asset copy in:
- `noisefit_libraries_zh/zh_app/ZH_SDK_20260402_V2.3.1.aar`

### 5.2 SDK source handling
No SDK source files under `ZH-SDK` were changed.

### 5.3 Protobuf note
The cloned SDK demo references `protobuf-java:4.34.1`.

This became a required runtime correction after real-device pairing logs showed:
- `NoClassDefFoundError: com.google.protobuf.RuntimeVersion$RuntimeDomain`

The project already depended on full `protobuf-java`; the fix was:
- keep the same dependency family
- update the resolved runtime version to `4.34.1`

Why not `protobuf-javalite`:
- the shipped AAR uses full protobuf classes
- the official `v2.3.1` demo also uses full `protobuf-java:4.34.1`

## 6. Risks and follow-ups

### 6.1 Remaining known limitation
The app still does not productize `frequencyVersion` in the production data pipeline.

Current implication:
- the new metadata is visible in the debug UI
- production DB/network/chart logic still behaves like the older fixed-frequency implementation

### 6.2 Why this was deferred
Changing production cadence handling safely would require a wider update surface than this minimal-diff migration allows:
- data entities
- migrations
- upload contracts
- chart rendering assumptions
- readiness/summary graph logic

### 6.3 Recommended next follow-up after device validation
If real hardware confirms second-based payloads are in active use, the next scoped change should be:
- add frequency metadata to the affected production entities
- migrate stored data safely
- update online mappers to stop hard-coding legacy intervals when metadata exists
- update affected charts to derive sample spacing from metadata instead of fixed slot counts

## 7. Verification

### 7.1 Build verification run
Executed:
- `:app:dependencies --configuration stagingDebugRuntimeClasspath`
- `:app:compileStagingDebugKotlin`
- `:app:assembleStagingDebug`
- `:app:compileLiveDebugKotlin`

Result:
- dependency resolution now shows `com.google.protobuf:protobuf-java:4.34.1`
- all listed builds succeeded

### 7.2 Existing warnings
The compile output still includes existing project warnings unrelated to this migration:
- resource formatting warnings
- existing deprecations
- existing nullability and language-version warnings in unrelated files

These warnings were not introduced or expanded by this SDK migration work.

## 8. Folder / abstraction / dependency discipline

Confirmed:
- no unnecessary new feature folders
- no parallel architecture
- no new Gradle dependency
- no SDK source modification
- no broad cleanup or refactor unrelated to the migration
- no deleted production files
