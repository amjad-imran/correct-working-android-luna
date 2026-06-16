# Stress Graph Root Cause and Fix - April 15, 2026

## Problem
- The main app stress graph could disappear or lose valid points after later syncs.

## Root cause
- `app/src/main/java/com/oreo/data/db/implementation/OreoStressDataImpl.kt`
- In `insertData(...)`:
  - the code built `mergedData` correctly
  - but `updateViaDate(...)` wrote `data.breakUp` instead of the merged result
- The update guard also compared only `mergedData.sum()` with the previous sum.
  - if the total sum stayed the same but the distribution changed, the update was skipped incorrectly

## Fix
- Write `Gson().toJson(mergedData)` back to the database.
- Compare `mergedData != prevBreakup` instead of comparing only the sums.

## Why this is the smallest safe fix
- No repository contract changed.
- No DB schema changed.
- No chart UI changed.
- No callback path changed.
- Only the broken merge persistence logic was corrected.

## Regression coverage added
- `app/src/test/java/com/oreo/data/db/implementation/OreoStressDataImplTest.kt`
- Covered cases:
  - preserve existing valid non-zero values when new payload contains zeros
  - update when breakup distribution changes even if total sum stays the same
