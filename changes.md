# ZH SDK v2.3.1 Migration Summary

## Objective
- Move `noisefit-android-luna` from `ZH_SDK_20260204_V2.3.0.aar` to `ZH_SDK_20260402_V2.3.1.aar`.
- Keep the SDK consumed as an AAR only.
- Keep the diff small and place most new validation work in the existing green-button debug UI (`BlankTestFragment`).

## Best-fit implementation location
- `noisefit_libraries_zh`: existing AAR wrapper module, so the SDK asset swap stays isolated.
- `noisefit_zh_sdk`: existing ZH integration layer, so new SDK callbacks and sync-mode support stay inside the current BLE bridge.
- `commons` `WatchDataStore`: existing lightweight raw/test storage, so no new persistence abstraction was introduced.
- `app` `BlankTestFragment`: existing green-button Luna validation screen, so new screenless-band payloads and mode testing stay in the same UI already used for SDK verification.

## Verified SDK scope: v2.3.0 to v2.3.1
- New method overload: `getDailyHistoryData(int mode, listener)`.
- New `FitnessDataCallBack` methods:
  - `onSleepRRIData`
  - `onSleepHRVData`
  - `onContinuousHeartRateSportFiveMinAfter`
  - `onContinuousRRIData`
- New beans exposed by the AAR:
  - `SleepRRIBean`
  - `SleepHRVBean`
  - `ContinuousHeartRateSportFiveMinAfterBean`
  - `ContinuousRRIDBean`
- Existing beans changed:
  - `ContinuousHeartRateBean` now includes `frequencyVersion`
  - `ContinuousPressureBean` now includes `frequencyVersion`
- Verified non-change:
  - `DevSportInfoBean.RingPointData` did not change in the shipped `v2.3.1` AAR compared with `v2.3.0`.

## Post-upgrade runtime fix
- Pairing crash diagnosis showed the app reached BLE service discovery and notification setup, then crashed inside the SDK protobuf path with:
  - `NoClassDefFoundError: com.google.protobuf.RuntimeVersion$RuntimeDomain`
- Root cause:
  - the app was still resolving `protobuf-java:3.22.3`
  - `ZH_SDK_20260402_V2.3.1.aar` was generated against a newer full protobuf runtime
  - the cloned official `v2.3.1` SDK demo uses `protobuf-java:4.34.1`
- Implemented fix:
  - updated the shared version-catalog protobuf runtime to `protobuf-java:4.34.1`
  - kept the existing `implementation libs.protobuf` usage in `noisefit_zh_sdk`
  - did not switch to `protobuf-javalite`

## Completed phases
### Phase 1: SDK asset and compile compatibility
- Updated the wrapper module to use `ZH_SDK_20260402_V2.3.1.aar`.
- Copied the new AAR into `noisefit_libraries_zh/zh_app/`.
- Added app-side compatibility for the four new callback methods.
- Corrected the protobuf runtime version needed by the new AAR during pairing/MTU setup.

### Phase 2: Screenless payload capture
- Extended the existing raw test-data store with slots for:
  - continuous heart rate raw payload
  - continuous pressure raw payload
  - sleep RRI raw payload
  - sleep HRV raw payload
  - continuous RRI raw payload
  - post-workout 5-minute heart-rate raw payload
- Saved those payloads from the existing `ZhUserActivityHandler`.

### Phase 3: Green-button validation UI
- Extended `BlankTestFragment` and its existing layout.
- Added manual daily sync buttons for:
  - default
  - today
  - history
  - all
- Added metadata summaries for:
  - continuous heart rate frequency and unit version
  - continuous pressure frequency and unit version
  - continuous RRI frequency and unit version
- Added raw payload panels for the new screenless-band SDK data.

### Phase 4: Green-button ANR fix for raw payload rendering
- Diagnosed the remaining crash on the green-button UI as an ANR caused by repeated `TextView.setText()` calls with very large raw payload strings.
- Kept the screen and BLE flow unchanged, but converted the raw sections into bounded preview cards.
- Added one reusable raw-payload bottom sheet in the same package to inspect one payload at a time.
- The dashboard now shows:
  - payload availability
  - total char count
  - short preview
  - tap-to-open behavior
- The bottom sheet now shows:
  - snapshot/manual refresh only
  - paged raw text
  - copy full
  - share full
- This keeps the raw data testable without rebinding full payload dumps into the main dashboard `ScrollView`.

## Pending phases
### Phase 4: Production frequency-aware rollout
- Not implemented yet.
- Current production tables and network mappers still assume legacy fixed intervals.
- A safe full rollout needs entity/schema/API review before changing:
  - DB storage
  - backend payload contracts
  - chart and time-axis logic in production screens

### Phase 5: Device validation
- Validate the new callbacks and sync modes on real screenless wristband hardware.
- Confirm when `frequencyVersion = 1` appears and how often second-based payloads are returned.

## Files changed
- `gradle/libs.versions.toml`
- `noisefit_libraries_zh/build.gradle`
- `noisefit_libraries_zh/zh_app/ZH_SDK_20260402_V2.3.1.aar`
- `commons/src/main/java/com/noisefit_commans/interfaces/data/UserActivityDataActions.kt`
- `commons/src/main/java/com/noisefit_commans/data/local/abstraction/WatchDataStore.kt`
- `commons/src/main/java/com/noisefit_commans/data/local/implementation/WatchDataStoreImpl.kt`
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`
- `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt`
- `app/src/main/res/layout/fragment_blank_test.xml`
- `app/src/main/java/com/oreo/ui/home/summary/RawSdkPayloadBottomSheet.kt`
- `app/src/main/res/layout/bottom_sheet_sdk_raw_payload.xml`
- `changes.md`
- `changes_detailed.md`
- `sdk_v2_3_1_non_technical.md`

## Files not changed on purpose
- No SDK source files under `ZH-SDK`.
- No Room schema/entity changes.
- No backend API model changes.
- No broad refactor of production sleep/readiness/summary screens.

## Verification
- Dependency resolution confirms `stagingDebugRuntimeClasspath` now resolves `com.google.protobuf:protobuf-java:4.34.1`.
- `:app:compileStagingDebugKotlin` completed successfully after the protobuf runtime correction.
- `:app:assembleStagingDebug` completed successfully after the protobuf runtime correction.
- `:app:compileLiveDebugKotlin` completed successfully after the AAR upgrade and protobuf runtime correction.
