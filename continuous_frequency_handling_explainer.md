# Continuous Frequency Handling Explainer

This document explains how different continuous-data frequencies are handled, whether the SDK gives a fixed array, and how the app converts newer `v2.3.1` cadences into the older app pipeline.

## Short Answer

You should **not** think of continuous raw data as a fixed hardcoded array size in app code.

The app receives:

- a data list such as `heartRateData` or `pressureData`
- a frequency field such as `continuousHeartRateFrequency` or `pressureFrequency`
- a `frequencyVersion`

Then it does one of two things:

1. **raw debug path**
   - keeps the payload as-is
   - shows actual array size from the payload
2. **production conversion path**
   - if the source cadence is faster than the old 5-minute pipeline and divides cleanly into 5 minutes, it averages samples into 5-minute buckets
   - otherwise it keeps the list unchanged

## The Main Frequency Problem

Before `v2.3.1`, the older app pipeline mainly expected continuous HR and pressure data in a legacy shape that behaved like 5-minute breakup data.

After `v2.3.1`, the SDK can return faster cadence data, and it tells us how to interpret that cadence using:

- `continuousHeartRateFrequency` or `pressureFrequency`
- `frequencyVersion`

Relevant code:

- [OreoDataConverter.kt:496](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt#L496)
- [OreoDataConverter.kt:649](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt#L649)

## What `frequencyVersion` Means

The app interprets `frequencyVersion` like this:

- `1` -> frequency is in **seconds**
- anything else, including `0` -> frequency is in **minutes**

Relevant code:

- [OreoDataConverter.kt:1232](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt#L1232)

So examples are:

- `frequency=5`, `frequencyVersion=0`
  - every 5 minutes
- `frequency=30`, `frequencyVersion=1`
  - every 30 seconds
- `frequency=60`, `frequencyVersion=1`
  - every 60 seconds

## Am I Receiving A Fixed Array?

From the app-side implementation, the safe answer is:

**No, the code does not assume a fixed array length.**

The app always uses the actual list returned by the SDK:

- `heartRateData.size`
- `pressureData.size`
- `rriData.size`
- `rri.size`

In the raw viewer, counts are calculated directly from the received array.

Relevant code:

- [RawSdkPayloadBottomSheet.kt:157](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/app/src/main/java/com/oreo/ui/home/summary/RawSdkPayloadBottomSheet.kt#L157)
- [RawSdkPayloadBottomSheet.kt:403](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/app/src/main/java/com/oreo/ui/home/summary/RawSdkPayloadBottomSheet.kt#L403)

So the app is **array-length driven**, not **hardcoded-length driven**.

## Why You Sometimes See Sizes Like `288`

If the payload is effectively one sample every 5 minutes for a full day, then:

- `24 hours * 60 minutes / 5 = 288`

So `288` is a natural result for one full day of 5-minute cadence data.

But that does **not** mean every continuous payload must always be length `288`.

Examples:

- every 5 minutes for 24h -> `288`
- every 1 minute for 24h -> `1440`
- every 30 seconds for 24h -> `2880`

Those numbers are just math from cadence, not a hardcoded app constraint.

## How The App Handles Different Frequencies

The key function is:

- [OreoDataConverter.kt:1222](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt#L1222)

That function:

1. reads the source list
2. converts source cadence into seconds
3. compares it against the target legacy cadence of 5 minutes
4. decides whether to normalize or keep original values

## Exact Normalization Rules

### Case 1: empty list

If the list is null or empty:

- return `emptyList()`

### Case 2: invalid frequency

If frequency is invalid or non-positive:

- return original values unchanged

### Case 3: source cadence is already 5 minutes or slower

If source cadence is `>= 5 minutes`:

- return original values unchanged

This means the app does not force re-bucketing if the source is already compatible with legacy expectations.

### Case 4: source cadence is faster than 5 minutes and divides evenly into 5 minutes

If source cadence is faster than 5 minutes and `5 minutes % source cadence == 0`, then:

- group samples into buckets
- average valid values inside each bucket
- output one value per 5-minute bucket

### Case 5: source cadence does not divide evenly into 5 minutes

If the cadence does not divide cleanly into 5 minutes:

- return original values unchanged

This avoids inventing a lossy or ambiguous bucket rule.

## What Counts As A Valid Sample During Averaging

During normalization, a sample is considered valid if:

- it is `> 0`
- and it is not `255`

So:

- `0` is ignored
- `255` is ignored
- positive normal samples are used

If a whole bucket has no valid values, the normalized output for that bucket becomes `0`.

Relevant code:

- [OreoDataConverter.kt:1249](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt#L1249)

## Example: 30-Second Heart Rate Data

Suppose the SDK gives:

- `continuousHeartRateFrequency = 30`
- `frequencyVersion = 1`
- `heartRateData = [...]`

That means the data is every 30 seconds.

Five minutes is `300` seconds.

So:

- `300 / 30 = 10`

The app will group every 10 samples into one 5-minute bucket.

If one day is fully covered:

- raw source size would be about `2880`
- normalized legacy size becomes `288`

## Example: 1-Minute Pressure Data

Suppose:

- `pressureFrequency = 1`
- `frequencyVersion = 0`

That means every 1 minute.

Five minutes is divisible by 1 minute, so:

- every 5 source values become one legacy bucket

If a full day is covered:

- raw source size would be about `1440`
- normalized size becomes `288`

## Example: Already 5-Minute Data

Suppose:

- `continuousHeartRateFrequency = 5`
- `frequencyVersion = 0`

That is already every 5 minutes.

So the app does **not** normalize further.

It keeps the array as-is.

## Important Separation: Raw Viewer Vs Production Converter

This is where confusion usually happens.

### Raw viewer

In the `BlankTestFragment` raw cards:

- the payload is shown as received
- the preview count uses actual raw array length
- the summary shows `every X seconds` or `every X minutes`

Relevant code:

- [BlankTestFragment.kt:650](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt#L650)
- [RawSdkPayloadBottomSheet.kt:157](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/app/src/main/java/com/oreo/ui/home/summary/RawSdkPayloadBottomSheet.kt#L157)

So the raw UI is for **inspection of original SDK shape**.

### Production converter

In the real app pipeline for continuous HR and pressure:

- the app may normalize faster cadence values back into legacy 5-minute breakup data
- then store or pass that normalized breakup onward

Relevant code:

- [OreoDataConverter.kt:496](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt#L496)
- [OreoDataConverter.kt:649](/Users/amjadimran/Downloads/demo/noisefit-android-luna%20/noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt#L649)

So the same callback has two views:

- raw SDK shape for debugging
- normalized legacy shape for production compatibility

## What I Can Safely Claim To A Senior Engineer

You can say:

The app does not assume a fixed array length for continuous data. It uses the list length actually returned by the SDK and interprets cadence using `frequency` plus `frequencyVersion`. For raw inspection, we keep the SDK payload unchanged. For production HR and pressure handling, if the SDK sends a faster cadence that cleanly divides into 5 minutes, we average those samples into legacy 5-minute buckets so the older downstream pipeline continues to work.

## Short Review Version

If you need the shortest version:

- not fixed array by app logic
- frequency metadata tells us seconds vs minutes
- raw viewer shows original array as received
- production HR/stress converter normalizes faster cadences into 5-minute buckets only when mathematically safe
- otherwise original list is kept unchanged

