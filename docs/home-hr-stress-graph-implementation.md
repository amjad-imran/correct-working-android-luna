# Home HR and Stress Graph Implementation

## Scope
- This document explains how the **home-page HR** and **home-page stress** graphs are currently built in `noisefit-android-luna`.
- It also records how cadence differences, invalid values, and fallback behavior are handled.
- It includes a short validation appendix for **body battery**, because that callback is often compared against these graph paths.

## Best-fit implementation files
- `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`
- `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt`
- `app/src/main/java/com/oreo/data/repository/implementation/OreoSyncRepositoryImpl.kt`
- `app/src/main/java/com/oreo/data/repository/implementation/OreoUserActivityRepositoryImpl.kt`
- `app/src/main/java/com/oreo/data/dataConverter/OreoOfflineDataMapper.kt`
- `app/src/main/java/com/oreo/data/dataConverter/OreoHRDataConvertor.kt`
- `app/src/main/java/com/oreo/data/dataConverter/OreoStressDataConvertor.kt`
- `app/src/main/java/com/oreo/ui/home/summary/paginate/SummaryDataViewModelToday.kt`
- `app/src/main/java/com/oreo/ui/home/summary/OSummaryHealthOverviewAdapter.kt`

## 1. HR home graph

### 1.1 Callback path
1. `ZhUserActivityHandler.onContinuousHeartRateData(...)`
2. Converts SDK payload into `OreoHeartRate`
3. Sends `UserActivityCallback.HeartHistoryObtainedOreo(...)`
4. `OreoSyncDataWork` receives that callback
5. `OreoSyncRepositoryImpl.saveHeartRateData(...)` persists it
6. `OreoUserActivityRepositoryImpl.getSummaryHRHealthOverview()` reads the saved local day data
7. `OreoOfflineDataMapper.convertHeartRateOverviewData(...)` prepares the home-card model
8. `SummaryDataViewModelToday.getHeartRateCard()` and `updateHeartRateCard()` build the final card state
9. `OreoHRDataConvertor.getHrCombinedData(...)` builds the plotted chart model
10. `OSummaryHealthOverviewAdapter` renders the chart and trend

### 1.2 Data shape
- Stored local heart-rate breakup: expected as **288 slots** for the day
- Slot meaning: **5-minute cadence**
- Home card display list: **48 slots**
- Display cadence: **30-minute cadence**

### 1.3 How 5-minute data becomes a 30-minute graph
- `OreoOfflineDataMapper.convertHeartRateOverviewData(...)` reads the 288-slot breakup.
- If data is missing, it creates a 288-slot zero-filled list.
- The list is chunked with `chunked(6)`.
- Each chunk of 6 x 5-minute values becomes one **30-minute display bucket**.
- For each bucket:
  - invalid values are ignored through `minWithoutZero()` and `maxWithoutZero()`
  - the display point uses `(min + max) / 2`
  - min and max are also retained for candle rendering

### 1.4 What is plotted
- The home HR graph does **not** plot the raw 288 values directly.
- It plots the 48-slot `listData` through `OreoHRDataConvertor.getHrCombinedData(...)`.
- That convertor also overlays sleep/workout sections from `ServerUserHealthData`.

### 1.5 Trend and freshness handling
- HR card freshness/trend uses `rawData`, not the 48 display list.
- `SummaryDataViewModelToday.getLastMeasuredValue(this.rawData)` finds the most recent non-zero, non-255 sample.
- `getHrTrend(...)` compares recent valid values on the raw 5-minute stream.
- `OSummaryHealthOverviewAdapter` treats the last measured HR slot as **5 minutes**:
  - `lastUpdatedTimestamp = midnight + (index + 1) * 5 minutes`
- If the latest sample is stale, the trend chip is hidden.

### 1.6 Bad data handling
- `0` is treated as invalid / missing
- `255` is treated as invalid / missing
- Empty payloads become zero-filled arrays
- Trend is suppressed if the latest or nearby values are invalid
- Min/max/average calculations skip invalid values where possible

## 2. Stress home graph

### 2.1 Callback path
1. `ZhUserActivityHandler.onContinuousPressureData(...)`
2. Converts SDK payload into `OreoStressDataBreakup`
3. Sends `UserActivityCallback.StressDataObtainedOreo(...)`
4. `OreoSyncDataWork` receives that callback
5. `OreoSyncRepositoryImpl.saveStressData(...)` persists it into the continuous-stress store
6. `OreoUserActivityRepositoryImpl.getSummaryStressData()` now reads that continuous-stress store
7. `OreoOfflineDataMapper.convertStressOverviewData(OreoStressDataBreakup?)` prepares the home-card model
8. `SummaryDataViewModelToday` builds the home stress card from that local model
9. `OreoStressDataConvertor.getStressCombinedData(...)` builds the plotted graph model
10. `OSummaryHealthOverviewAdapter` renders the chart and trend

### 2.2 Data shape
- Stored local stress breakup: continuous stress local data from `OreoStressDataImpl`
- Expected source shape: commonly **288 slots** for the day after callback normalization
- Home stress display list: **96 slots**
- Display cadence: **15-minute cadence**

### 2.3 How local continuous stress is mapped for the home graph
- `OreoOfflineDataMapper.convertStressOverviewData(OreoStressDataBreakup?)` sanitizes invalid values.
- `mapStressBreakupToHomeSlots(...)` then reshapes the local breakup into the home card’s 96-slot format.

Current mapping rules:
- If the source is already 96 slots: use it directly
- If the source is larger and evenly divisible into 96: average each bucket into one 15-minute slot
- If the source is smaller and evenly expands into 96: repeat values to fill the 96 home slots
- Otherwise: use the available values and pad/truncate to 96

For the normal 288-slot case:
- 288 / 96 = 3
- every 3 x 5-minute samples become one 15-minute display bucket
- only positive values contribute to the average
- all-invalid buckets become `0`

### 2.4 What is plotted
- The home stress graph is rendered from `StressCombineModel`.
- `SummaryDataViewModelToday` passes the **local mapped 96-slot stress list** into `OreoStressDataConvertor.getStressCombinedData(...)`.
- `OreoStressDataConvertor` uses that local list for graph items when it contains at least one valid positive sample.
- If the local list is empty or all invalid/zero, it falls back to `ServerUserHealthData.stress.breakUp`.
- Sleep/workout overlays are still taken from `ServerUserHealthData`, so the graph keeps the existing section overlays while using local ring data for the plotted line.

### 2.5 Trend and freshness handling
- Stress currently uses the home-card `listData` itself for latest-value and trend evaluation.
- `SummaryDataViewModelToday.getLastMeasuredValue(stressData?.listData)` finds the latest non-zero, non-255 slot.
- `getStressTrend(...)` compares the last two valid display slots.
- `OSummaryHealthOverviewAdapter` treats the stress display slots as **15 minutes**:
  - `lastUpdatedTimestamp = midnight + (index + 1) * 15 minutes`
- If the latest slot is stale or invalid, the trend chip is hidden.

### 2.6 Important difference from HR
- HR keeps **two shapes**:
  - raw 288-slot data for freshness/trend
  - 48-slot display data for plotting
- Stress keeps **one existing home-card shape**:
  - 96-slot display data for plotting and trend

Why this difference still exists:
- The current stress home-card model does not carry a separate raw-data field like HR does.
- Keeping the 96-slot contract avoids a broader home-card/UI refactor.
- The fix keeps the graph on the **new continuous stress callback path** while preserving the existing UI cadence and lower regression risk.

### 2.7 Bad data handling
- `0` is treated as invalid / missing
- `255` is converted to `0`
- Empty or null stress data becomes a zero-filled 96-slot list
- Buckets average only positive values
- Graph fallback uses server stress only when the local mapped list has no valid positive samples
- `updateStressCard()` now posts the freshly fetched stress card back into state instead of reusing stale state data

This last point matters because otherwise the card could keep showing an old/empty graph even after new local stress data was saved.

## 3. Why the graph cadences differ from callback cadences

### HR
- callback/local storage cadence: 5 minutes
- home graph cadence: 30 minutes
- reason: existing HR home card uses aggregated candle-style display buckets

### Stress
- callback/local storage cadence: usually 5 minutes after normalization
- home graph cadence: 15 minutes
- reason: existing stress home card and stress chart sectioning are built around a 96-slot day

This is intentional display aggregation, not data loss in the callback path.

## 4. What happens when values are all zero

### HR
- 288 raw zeros become a zero-filled raw array
- the 48 display buckets become zeros
- `lastMeasuredValue = 0`
- trend chip is hidden
- graph still renders the empty baseline shape

### Stress
- local mapped stress list becomes 96 zeros
- if local data has no valid positive values, the graph builder can fall back to server stress breakup
- if both local and server stress are effectively empty, the chart renders as empty
- `lastMeasuredValue = 0`
- trend chip is hidden

## 5. Body-battery callback validation

### Correct callback
- The app is using the dedicated body-battery callback:
  - `ZhUserActivityHandler.onRingBodyBatteryData(RingBodyBatteryBean?)`

### Current implementation
- The callback currently:
  - logs the raw payload
  - stores the raw JSON with `watchDataStore.testSaveBodyBatteryData(...)`
- `BlankTestFragment.loadBodyBatteryChart(...)` reads that stored raw payload and plots it for the debug screen.

### What the debug chart does
- Reads:
  - `date`
  - `bodyBatteryFrequency`
  - `data`
- Trims future same-day slots before plotting
- Plots only positive values
- Splits the line into separate segments when there are gaps
- Highlights the latest valid point

### Validation result
- I did **not** find the body-battery callback wired to the wrong SDK callback.
- I did **not** find a stress callback being used for body battery.
- The current app-side architecture for body battery is:
  - **correct callback**
  - **raw save**
  - **debug chart render**

### Limitation
- Body battery is not currently normalized into a production DB/repository/home-card flow the way HR and stress are.
- Because of that, if the firmware payload itself is noisy, sparse, inconsistent, or future-filled, the strange behavior can still be visible in the debug chart even though the callback wiring is correct.

## 6. Summary
- HR home graph is already correctly driven from the continuous HR callback path.
- Stress home graph now follows the same overall pattern:
  - continuous callback
  - local persistence
  - local home-card read
  - graph model built from local ring data
- The key difference is only the retained home-card display cadence:
  - HR display = 30-minute buckets
  - stress display = 15-minute buckets
- Body battery is using the correct callback, but it remains a raw/debug rendering path rather than a full production graph pipeline.
