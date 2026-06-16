# Home Stress Graph Restore and BlankTest HRV Graph

## Date
- April 16, 2026

## Scope
- Restore the main app home stress graph to the same callback/data path used by `old noisefit-android-luna`
- Keep the `onContinuousPressureData(...)`-derived series available only inside `BlankTestFragment`, clearly labeled as HRV
- Do not change any SDK / AAR source

## Final callback ownership

### Main app home stress graph
- Callback source: `onRingStressDetectionData(RingStressDetectionBean)`
- SDK handler path:
  - `ZhUserActivityHandler.onRingStressDetectionData(...)`
  - `OreoDataConverter.parseBodyStressData(...)`
  - `UserActivityCallback.OreoBodyStressDataObtained(...)`
- Local storage used by home stress:
  - `OreoBodyStressDataImpl`
- Home graph assembly path:
  - `OreoUserActivityRepositoryImpl.getSummaryStressData()`
  - `OreoOfflineDataMapper.convertStressOverviewData(OreoBodyStressData?)`
  - `OreoStressDataConvertor.getStressCombinedData(ServerUserHealthData)`
  - `SummaryDataViewModelToday`

### BlankTest HRV graph
- Callback source: `onContinuousPressureData(ContinuousPressureBean)`
- SDK meaning used for this task:
  - local SDK notes already in this repo document that this callback is treated as HRV-related for the ring project
- Local storage used by BlankTest HRV graph:
  - `OreoStressDataImpl`
- BlankTest graph assembly path:
  - read today breakup from `OreoStressDataImpl`
  - map the stored 5-minute breakup into the same 96-slot home-card shape that the mistaken home stress graph had been using
  - plot it inside `BlankTestFragment`

## Is the fixed main stress graph exactly the same as the old Luna app?
- Yes for the production callback/data source path.
- The restored main home stress graph now again uses the body-stress path from `onRingStressDetectionData(...)`, matching the old Luna app flow.
- The `onContinuousPressureData(...)` path is no longer used by the production home stress graph.

## Why this separation is correct
- `onContinuousPressureData(...)` was the source of the accidental HRV-on-stress behavior in the new app.
- `old noisefit-android-luna` uses the ring body-stress callback path for the home stress graph.
- Moving the continuous-pressure series to `BlankTestFragment` preserves access to that new data without polluting the production stress card.

## Files involved
- Main app stress restore:
  - `app/src/main/java/com/oreo/data/repository/implementation/OreoUserActivityRepositoryImpl.kt`
  - `app/src/main/java/com/oreo/data/dataConverter/OreoOfflineDataMapper.kt`
  - `app/src/main/java/com/oreo/data/dataConverter/OreoStressDataConvertor.kt`
  - `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModelToday.kt`
- BlankTest HRV graph:
  - `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt`
  - `app/src/main/res/layout/fragment_blank_test.xml`

## SDK / dependency note
- `zh-sdk` remained reference-only.
- No SDK/AAR code was modified.
- No new dependency or build-structure change was introduced.
