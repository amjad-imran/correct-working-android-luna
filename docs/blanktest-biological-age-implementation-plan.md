# BlankTest Biological Age Implementation Plan

## Scope
- This plan is for the **BlankTestFragment** biological-age / fitness-age display.
- It prioritizes **existing working app data** and avoids inventing a new backend or a new parallel age algorithm.

## 1. Current state in the app

### Existing sources already present
- `WatchDataStore.getFitnessAge()`
- `RecordedWorkoutData.fitnessAge`

### Where those values come from
- `ZhUserActivityHandler` updates `WatchDataStore` when workout/sport parsing completes.
- `DataConverter.parseRecordedData(...)` stores `fitnessAge` into `RecordedWorkoutData`.
- `BlankTestFragment` reads both the stored age and the latest workout.

### Important limitation
- I did not find a separate server-backed or repository-backed “biological age” feature in the app.
- The current usable source is the SDK workout field `fitnessAge`.
- So the safest no-backend implementation is to treat BlankTest’s biological-age display as a **sanitized latest valid fitness-age display**.

## 2. Problem identified
- Invalid sentinel-style values such as non-positive ages can reach the app from the workout parsing path.
- Those values should not overwrite the last valid age or be shown in the UI as if they are meaningful.

## 3. Fix implemented now

### Storage sanitization
- `WatchDataStoreImpl.updateFitnessAge(age)` now ignores non-positive values.
- `WatchDataStoreImpl.getFitnessAge()` now sanitizes stored non-positive values back to `0`.

### UI resolution in BlankTest
- `BlankTestFragment` now resolves biological age in this order:
  1. latest valid stored `fitnessAge`
  2. latest valid `workout.fitnessAge`
  3. otherwise `--`

This gives the best possible current result without new backend work.

## 4. Why this is the best current no-backend approach
- It uses data already flowing through the existing app.
- It does not invent a new “biological age” formula that the rest of the app does not use.
- It prevents obviously bad default values from leaking into the UI.
- It preserves the last valid positive age instead of replacing it with a bad sentinel like `-5`.

## 5. Recommended next-step behavior

### Keep now
- Use the latest valid positive workout-derived age as the canonical BlankTest value.
- Show `--` before the first valid workout-derived age exists.

### Good optional app-only improvements
- Store a timestamp alongside the saved fitness age so BlankTest can show:
  - `Age 31`
  - `Updated from last synced workout`
- Add a small helper label in BlankTest:
  - `Source: latest valid workout`
- Clear the stored age on explicit device reset / app debug reset if that matches the rest of BlankTest behavior.

## 6. What should not be done right now
- Do not calculate a new biological age from DOB, VO2 max, training load, or energy consumption inside BlankTest.
- Do not create a new backend field just for BlankTest unless the product already wants a real biological-age feature elsewhere.
- Do not rename the source contract silently. The source currently behaves like `fitnessAge`, not a separately validated biological-age engine.

## 7. If a future full feature is required
- The clean future path is:
  1. define a single canonical source of truth for biological age
  2. expose it through an existing repository/server contract or validated SDK field
  3. keep BlankTest as a consumer of that single source, not a special calculator

## 8. Summary
- The current app already has one usable age source: workout-derived `fitnessAge`.
- The correct minimal implementation is to:
  - keep only valid positive values
  - reuse the existing stored/workout path
  - show `--` when the source is invalid or unavailable
- That is the best low-risk implementation available without adding backend work or inventing a new metric.
