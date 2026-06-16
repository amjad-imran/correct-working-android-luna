# Update Changes

## April 10, 2026

### Task
- Support Luna band devices whose BLE name now starts with `Noise_REP_XXXX` while keeping them on the existing `luna_band` pairing, connection, and sync pipeline.
- Keep UI naming separate:
  - `Noise_REP_XXXX` devices display as `Noise Band`
  - legacy `Luna Band_*` devices display as `Luna Band`

### Best-fit implementation location
- `app/src/main/java/com/noisefit/ui/onboarding/pairing/find/SearchNearbyDeviceViewModel.kt`
  - this is the current scan-classification layer, so it is the narrowest existing place to accept the renamed devices without changing downstream Luna band behavior

### File audit
| File | Mode | Notes |
| --- | --- | --- |
| `app/src/main/java/com/noisefit/ui/onboarding/pairing/find/SearchNearbyDeviceViewModel.kt` | Modified | Added `Noise_REP_` fallback after the existing catalog matcher fails |
| `app/src/test/java/com/noisefit/ui/onboarding/pairing/find/SearchNearbyDeviceViewModelTest.kt` | Created | Added focused unit tests for old/new Luna band name matching and UI-name resolution |
| `changes.md` | Modified | Added task summary |
| `changes_detailed.md` | Modified | Added detailed notes and verification |
| `update_changes.md` | Created | Added this audit file |
| `revert_bluetooth_namechnage.mds` | Created | Added line-by-line revert documentation for this Bluetooth-name change set |

### Deleted files
- None

### Dependencies and structure
- No new dependencies added
- No new modules or package trees added
- No unrelated refactor or cleanup done

### Verification
- Passed:
  - `:app:testStagingDebugUnitTest --tests "com.noisefit.ui.onboarding.pairing.find.SearchNearbyDeviceViewModelTest"`
- Built:
  - `app/build/outputs/apk/staging/debug/Luna_1.6.3.staging.luna_154.apk`

### Narrow assumptions
- These `Noise_REP_XXXX` devices are the same Luna band hardware and should continue using the existing `DeviceType.LUNA_BAND` path.
- The rename change is limited to BLE scan-name recognition; no additional SDK command-path difference was introduced for this device batch.
