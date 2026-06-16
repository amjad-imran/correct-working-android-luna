# HR Cloud Sync Diagnosis and Conservative Fix Plan

Date: April 15, 2026

## Scope
- This document is documentation-only.
- It does **not** introduce any production code change for the HR cloud-sync issue.
- It explains the current behavior, the most accurate diagnosis the codebase supports today, and the safest implementation plan to fix the issue without creating backend or cloud regressions.

## Reported symptom
- HR graph is visible locally in the app.
- After logout and login, the HR graph is missing again.
- That means the local session had HR data, but the post-login source of truth did not restore it.

## Best-fit implementation locations for the future fix

### 1. `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt`
Why this is the right place:
- This is where device sync completes and server sync success is interpreted.
- This is the existing place that decides when local rows are marked as synced.
- Any conservative cloud-verification gate belongs here instead of in UI code.

### 2. `app/src/main/java/com/oreo/data/repository/implementation/OreoSyncRepositoryImpl.kt`
Why this is the right place:
- This is the existing repository boundary for reading unsynced HR rows and marking them synced.
- If HR cloud verification later needs scoped mark-as-synced behavior, this repository is the narrowest existing place.

### 3. `app/src/main/java/com/oreo/data/dataConverter/OreoOnlineDataMapper.kt`
Why this is the right place:
- This is the existing mapper that converts local HR rows into the `heart_rate` payload sent to the server.
- If additional payload-level diagnostics or fingerprints are needed, this is the correct place to add them without changing UI or DB layers.

### 4. `app/src/main/java/com/oreo/data/repository/implementation/OreoUserActivityRepositoryImpl.kt`
Why this is the right place:
- This is the existing fetch/cache path for `ServerUserHealthData`.
- Any post-upload verification that checks whether the cloud now returns `heart.break_up` should reuse this repository layer.

## Current proven code path

### A. Local HR capture and persistence
The current app-side HR graph path is:
1. `ZhUserActivityHandler.onContinuousHeartRateData(...)`
2. `OreoDataConverter.parseHeartRateData(...)`
3. `UserActivityCallback.HeartHistoryObtainedOreo(...)`
4. `OreoSyncDataWork` receives the callback
5. `OreoSyncRepositoryImpl.saveHeartRateData(...)`
6. `OreoHeartRateDataImpl.insertData(...)`
7. `OreoUserActivityRepositoryImpl.getSummaryHRHealthOverview()`

What this proves:
- The app is already capturing v2.3.1 continuous HR locally.
- The app is already saving that HR into the local `heart_rate` Room table.
- If the graph is visible before logout, local capture/persistence is working for that session.

### B. Logout behavior
The logout path calls `database.clearAllTables()`.

What this proves:
- Local `heart_rate` rows are intentionally deleted on logout.
- Cached `UserHealthData` rows are also deleted on logout.
- After login, the graph can only come back from a new server fetch.

### C. Post-login cloud fetch behavior
`OreoUserActivityRepositoryImpl.getUserHealthData(...)` fetches `ServerUserHealthData` from the backend and caches it in `userHealthDataSource`.

`getSummaryHRHealthOverview()` then:
- prefers the local `heart_rate` Room row
- falls back to cached `ServerUserHealthData.heart.break_up` when local HR data is empty

What this proves:
- After logout/login, the only durable source for restoring the HR graph is `ServerUserHealthData.heart.break_up`.
- If the graph is still missing after login, then the post-login fetch path is not receiving usable heart breakup data from the cloud.

### D. HR upload behavior
The current combined sync path is:
1. `OreoSyncRepositoryImpl.getUnSyncUserActivities()`
2. HR rows are included as `hrHistoryData`
3. `OreoOnlineDataMapper.parseHeartHistoryData(...)`
4. payload field sent to server: `heart_rate`
5. endpoint: `${BuildConfig.OREO_BASE_URL}/protean/v1/sync`

What this proves:
- The client does attempt to upload HR breakup data to the existing combined sync endpoint.
- The local-to-network mapping for HR is present.

## Most accurate diagnosis

## Proven facts
1. The missing graph after logout/login is **not primarily a local chart-rendering bug**.
2. The app already has a valid local HR path before logout.
3. Logout deletes the local HR Room table and the cached dashboard table.
4. After login, the graph depends on the backend returning `ServerUserHealthData.heart.break_up`.
5. The app uploads HR through the combined sync API as `heart_rate`.

## Highest-confidence client-side problem
The current client treats **generic combined-sync success** as if **all included HR data definitely reached cloud storage**, but the success contract does not actually prove that.

Why:
- `OreoSyncDataWork` calls `syncRepository.markDataSynced(userActivities.second)` on combined sync success.
- `VersionCheckResponse` only contains generic fields such as `dates`; it does **not** acknowledge `heart_rate` separately.
- `markDataSynced(...)` then marks HR rows as synced even though there is no HR-specific acceptance verification.

Why this matters:
- If the backend silently ignores HR for some requests, stores it later than expected, or returns success without durable `heart.break_up`, the client still stops retrying those HR rows.
- Once the user logs out, the local fallback is gone, so the missing cloud HR becomes visible as a lost graph.

## What is likely happening at runtime
The most likely runtime sequence is:
1. HR is captured locally and the graph appears.
2. Combined sync returns a generic success.
3. The client marks HR rows as synced.
4. The subsequent server state still does not provide usable `heart.break_up` for that date.
5. Logout clears the local-only copy.
6. Login fetches cloud data without the expected HR breakup, so the graph is gone.

## Important accuracy note
From code inspection alone, I can prove the **client-side verification gap** above.

I cannot prove from static code alone whether the initial failure is:
- backend persistence dropping `heart_rate`
- delayed backend materialization
- a date/frequency mismatch on the server side
- or a specific request-condition under which the client sends no HR section

So the correct diagnosis is:
- **Immediate visible failure after relogin:** cloud fetch lacks usable HR breakup data
- **Definite client-side bug that makes this permanent:** the app marks HR as synced without verifying that the cloud actually materialized HR for those dates

## Conservative full fix plan

### Phase 0: keep payload and backend contract unchanged
Do not change:
- endpoint
- payload schema
- backend date formats
- frequency values
- server model contracts

Reason:
- This keeps the blast radius low and avoids introducing backend/cloud regressions while diagnosing.

### Phase 1: add verification-first diagnostics
Implementation target:
- `OreoOnlineDataMapper.kt`
- `OreoSyncDataWork.kt`

Add narrow logs for:
- whether `hrHistoryData` is present in the outgoing combined payload
- which dates are included for HR
- breakup size and valid-value count per HR date
- server sync success timestamp
- post-sync fetch result for `ServerUserHealthData.heart.break_up`

Goal:
- separate “HR was never posted” from “HR was posted but never materialized in cloud fetch”.

### Phase 2: stop blindly marking HR as cloud-synced
Implementation target:
- `OreoSyncDataWork.kt`
- `OreoSyncRepositoryImpl.kt`

Change behavior:
- keep current immediate mark-as-synced for unrelated metrics
- do **not** immediately mark HR rows synced just because combined sync returned generic success
- instead, keep the affected HR dates pending until cloud verification completes

Goal:
- preserve retry capability for HR without changing other data families.

### Phase 3: add post-sync cloud verification for HR dates
Implementation target:
- `OreoSyncDataWork.kt`
- `OreoUserActivityRepositoryImpl.kt`

Flow:
1. combined sync succeeds
2. identify the dates whose HR was included in the upload
3. invalidate cached `UserHealthData` for those dates
4. refetch dashboard/health data for those dates
5. inspect `ServerUserHealthData.heart.break_up`
6. only then mark those HR rows synced

Verification rule:
- treat the date as verified only when the fetched `heart.break_up` is present and contains valid metric values

Goal:
- confirm actual cloud materialization before removing local retry.

### Phase 4: retry behavior when verification fails
Implementation target:
- existing sync worker/repository flow only

Behavior:
- if post-sync fetch still lacks HR breakup:
  - leave HR rows unsynced
  - log a dedicated warning
  - allow next normal sync cycle to retry

Do not:
- delete local HR rows
- mutate other metric sync state
- invent a second upload API

Goal:
- conservative recovery with no backend contract change.

### Phase 5: optional hardening after verification data is available
Only after Phase 1-4 evidence is collected:
- if backend requires a different HR frequency/date contract, adjust only that mapper
- if backend materialization is delayed, add bounded retry/backoff before declaring verification failure
- if server returns partial acceptance in a future response model, use that instead of fetch-back verification

## Test plan for the future HR fix

### Unit tests
- `OreoOnlineDataMapper`:
  - HR payload present when unsynced HR row contains valid breakup data
- `OreoSyncRepositoryImpl`:
  - HR rows selected correctly for upload
- `OreoSyncDataWork`:
  - generic success does not immediately mark HR rows synced when verification is still pending
  - HR rows are marked synced only after verified cloud fetch
  - HR rows remain unsynced when fetch returns missing/empty `heart.break_up`

### Manual verification checklist
1. Sync a day with visible local HR graph.
2. Confirm outgoing combined payload contains `heart_rate` for that date.
3. Confirm post-sync fetch returns `ServerUserHealthData.heart.break_up` for the same date.
4. Log out.
5. Log back in.
6. Fetch dashboard again.
7. Confirm HR graph is restored from cloud data without requiring a fresh device sync.

## Final conclusion
- The visible relogin failure is a **cloud-restoration failure**, not just a local graph bug.
- The app already captures and stores HR locally.
- The safest and most correct client-side fix is **verification before marking HR rows synced**.
- That approach is conservative, keeps existing backend contracts unchanged, and directly addresses the current loss mechanism without broad refactors.
