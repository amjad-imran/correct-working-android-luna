# iOS vs Android: Timestamp Handling for Continuous Data

## Overview

This document explains the key difference in how iOS and Android SDKs handle timestamps for continuous data items (like pressure/stress data).

---

## iOS Approach: Per-Item Timestamps

In iOS, **each data item has its own separate timestamp** stored directly in the object.

```objc
@interface ZHDContinuousPressureItemData : NSObject

/** 压力值(0值代表无效值) / pressure (A value of 0 represents an invalid value.) */
@property (nonatomic, assign) NSInteger pressure;

/** rri */
@property (nonatomic, assign) NSInteger rri;

/** 时间戳(秒) / Timestamp (seconds) */
@property (nonatomic, assign) NSInteger dateStamp;  // ← Each item has its own timestamp

@end
```

**iOS Container:**
```objc
@interface ZHDContinuousPressureData : NSObject

/** 开始日期时间戳 (秒) / start date timestamp (seconds) */
@property(nonatomic, assign) NSInteger dateStamp;

/** 连续压力频率 / 单位:分钟  Continuous Pressure Frequency / Unit: Minutes */
@property(nonatomic, readwrite) int pressureFrequency;

/** 压力详细 / pressure array */
@property(nonatomic, strong) NSArray<ZHDContinuousPressureItemData *> *items;  // ← Items have individual timestamps

@end
```

---

## Android Approach: Computed Timestamps

In Android, the SDK provides:
1. **A start timestamp** (from `bean.date`)
2. **A frequency value** (`pressureFrequency`)
3. **A frequency version** (`frequencyVersion`)
4. **A flat list of values** (`pressureData`)

The Android app **computes individual timestamps** from these fields.

```kotlin
// ContinuousPressureBean (Android SDK v2.3.1)
val date: String                 // Start date/time e.g., "2026-04-28 00:00:00"
val pressureFrequency: Int       // e.g., 5 or 30
val frequencyVersion: Int        // 0 = minutes, 1 = seconds
val pressureData: List<Int>      // Flat list of pressure values
```

### How Android Computes Timestamps

**File:** `DataConverter.kt` (lines 677-701)

```kotlin
fun parseStressData(bean: ContinuousPressureBean): ArrayList<StressDataBreakup> {
    val stressArray = ArrayList<StressDataBreakup>()
    
    // 1. Get start timestamp from bean.date
    var startDayTimeStamp = DateFormats.convertDateTimeToTimeStamp(
        bean.date, 
        DateFormats.dateTimeFormat5()
    )

    // 2. Iterate through each value
    bean.pressureData.forEach { measureData ->
        // 3. Increment timestamp by frequency
        startDayTimeStamp = DateFormats.addMinuteToTimeStamp(
            startDayTimeStamp!!, 
            bean.pressureFrequency  // Add N minutes per item
        )
        
        if (measureData > 0) {
            val date = DateFormats.dateFormatOld().format(startDayTimeStamp)
            val time = DateFormats.timeFormat().format(startDayTimeStamp)
            
            // 4. Create item with computed timestamp
            val stressItem = StressDataBreakup(
                value = measureData,
                date = date,
                time = time,
                timeStamp = syncDate  // Computed, not from SDK
            )
            stressArray.add(stressItem)
        }
    }
    return stressArray
}
```

---

## Side-by-Side Comparison

| Aspect | iOS | Android |
|--------|-----|---------|
| **Timestamp source** | Each item has `dateStamp` from SDK | Computed from start time + index × frequency |
| **Data structure** | Array of objects with embedded timestamps | Flat list of values + metadata |
| **Frequency handling** | Implicit in item timestamps | Explicit `frequencyVersion` + `pressureFrequency` |
| **Flexibility** | Items can have irregular intervals | Assumes regular intervals |

---

## Visual Example

### iOS Data Flow
```
SDK Response:
├── dateStamp: 1714262400 (start)
├── pressureFrequency: 5
└── items:
    ├── { pressure: 45, dateStamp: 1714262400 }  ← 00:00
    ├── { pressure: 52, dateStamp: 1714262700 }  ← 00:05
    ├── { pressure: 48, dateStamp: 1714263000 }  ← 00:10
    └── ...
```

### Android Data Flow
```
SDK Response:
├── date: "2026-04-28 00:00:00" (start)
├── pressureFrequency: 5
├── frequencyVersion: 0 (minutes)
└── pressureData: [45, 52, 48, ...]

App Computation:
├── Item 0: timestamp = start + (0 × 5 min) = 00:00 → value 45
├── Item 1: timestamp = start + (1 × 5 min) = 00:05 → value 52
├── Item 2: timestamp = start + (2 × 5 min) = 00:10 → value 48
└── ...
```

---

## Why This Design?

### iOS Advantage
- More flexible - can handle irregular measurement intervals
- Self-documenting data - each item knows its exact time
- Simpler parsing - no computation needed

### Android Advantage  
- More compact data transfer - no redundant timestamps
- Smaller payload size over Bluetooth
- Works well for regular interval measurements

---

## Key Points for Seniors

1. **Same underlying data** - Both platforms receive the same measurements from the device

2. **Different representation** - iOS embeds timestamps per-item; Android computes them

3. **Android relies on regularity** - Android assumes measurements happen at regular intervals (every N minutes/seconds). If the device skips a measurement, Android shows a `0` value at that slot rather than a gap.

4. **Normalization** - Android has an additional normalization layer (`LunaSdk231DataNormalizer`) that can convert different cadences (e.g., 30-second data → 5-minute buckets) for backward compatibility with legacy UI.

5. **`frequencyVersion` is Android-specific** - This field tells Android whether `pressureFrequency` is in minutes (`0`) or seconds (`1`). iOS doesn't need this since each item has its own timestamp.

---

## Related Files

- [OreoDataConverter.kt](../noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/OreoDataConverter.kt) - Production path with normalization
- [DataConverter.kt](../noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/DataConverter.kt) - Per-item timestamp computation
- [LunaSdk231DataNormalizer.kt](../noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/LunaSdk231DataNormalizer.kt) - Frequency normalization
- [continuous_frequency_handling_explainer.md](../continuous_frequency_handling_explainer.md) - Detailed frequency handling

---

*Document created: 2026-04-28*
