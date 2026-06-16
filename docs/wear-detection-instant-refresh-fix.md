# Wear Detection Instant Refresh Fix

**Document Created:** April 16, 2026  
**Location:** BlankTestFragment → "Wear Detection" card  
**Version:** staging v1.6.4.1

---

## Problem Statement

The wear detection refresh button in BlankTestFragment was not providing instant feedback to the user. When the user tapped the refresh button:
1. No immediate visual feedback was shown
2. User had to wait for the full callback chain to complete before seeing any change
3. This made the button feel unresponsive

---

## Root Cause Analysis

The original flow was:

```
User taps "Refresh" button
        ↓
alertSettingsViewModel.refreshWearDetectionStatus()
        ↓
sessionManager.sendQueryAction(QueryAction.GetRingWearingStatus)
        ↓
[SDK queries device - network/BLE latency]
        ↓
RingConnectionService receives QueryCallback.RingWearingStatusObtained
        ↓
sessionManager.setQueryCallback(callbackForUi)
        ↓
BlankTestFragment observes deviceQueryCallback
        ↓
alertSettingsViewModel.handleQueryCallback()
        ↓
alertSettings LiveData updated
        ↓
bindAlertState() → bindWearDetectionStatus()
        ↓
UI finally updates
```

**Issue:** No visual feedback during the SDK query latency period (can be 1-3 seconds).

---

## Solution Implemented

### 1. Immediate Visual Feedback on Button Click

Added immediate UI update when the refresh button is clicked:

```kotlin
root.findViewById<TextView>(R.id.tvRefreshWearDetection).setOnClickListener {
    // Show immediate "Querying..." feedback for instant responsiveness
    root.findViewById<TextView>(R.id.tvWearDetectionStatus).text = "Querying..."
    root.findViewById<TextView>(R.id.tvWearDetectionMeta).text = ""
    alertSettingsViewModel.refreshWearDetectionStatus()
}
```

### 2. Direct Callback Observer for Instant UI Update

Added direct handling of `RingWearingStatusObtained` callback to update UI immediately when response is received, in parallel with the ViewModel flow:

```kotlin
alertSettingsViewModel.sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) { queryCallback ->
    alertSettingsViewModel.handleQueryCallback(queryCallback)
    // Instant UI update for wear detection refresh
    if (queryCallback is QueryCallback.RingWearingStatusObtained) {
        val wearStatusText = when {
            queryCallback.wearDetectionStatus.isWorn == true -> "Currently worn"
            queryCallback.wearDetectionStatus.isWorn == false -> "Currently not worn"
            else -> "Wear status unknown"
        }
        root.findViewById<TextView>(R.id.tvWearDetectionStatus).text = wearStatusText
        val timestampFormatted = if (queryCallback.wearDetectionStatus.lastUpdatedAt > 0) {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(Date(queryCallback.wearDetectionStatus.lastUpdatedAt))
        } else ""
        root.findViewById<TextView>(R.id.tvWearDetectionMeta).text =
            if (timestampFormatted.isNotEmpty()) "Last updated: $timestampFormatted" else ""
    }
}
```

---

## New Flow

```
User taps "Refresh" button
        ↓
IMMEDIATE: tvWearDetectionStatus shows "Querying..."
IMMEDIATE: tvWearDetectionMeta cleared
        ↓
alertSettingsViewModel.refreshWearDetectionStatus()
        ↓
sessionManager.sendQueryAction(QueryAction.GetRingWearingStatus)
        ↓
[SDK queries device - network/BLE latency]
        ↓
RingConnectionService receives QueryCallback.RingWearingStatusObtained
        ↓
sessionManager.setQueryCallback(callbackForUi)
        ↓
BlankTestFragment observes deviceQueryCallback
        ↓
IMMEDIATE: Direct UI update with wear status
PARALLEL: alertSettingsViewModel.handleQueryCallback() for state persistence
        ↓
UI updated instantly
```

---

## Files Changed

| File | Change |
|------|--------|
| `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt` | Added immediate "Querying..." feedback on button click |
| `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt` | Added direct callback observer for instant UI update |
| `app/src/main/java/com/oreo/ui/home/summary/BlankTestFragment.kt` | Added `QueryCallback` import |

---

## User Experience Improvement

| Before | After |
|--------|-------|
| No visual feedback on tap | Immediate "Querying..." text |
| Wait 1-3 seconds for any change | Instant acknowledgment of action |
| Felt unresponsive | Feels responsive and interactive |

---

## Imports Added

```kotlin
import com.noisefit_commans.interfaces.QueryCallback
```

---

## Testing Verification

1. ✅ Tap refresh button → "Querying..." appears immediately
2. ✅ When device responds → status updates immediately (no additional delay)
3. ✅ If device is disconnected → appropriate message shown via ViewModel flow
4. ✅ Multiple rapid taps handled correctly
5. ✅ No regression in other alert settings functionality

---

## Notes

- The ViewModel flow is preserved for state persistence and validation
- Direct UI update runs in parallel for responsiveness
- Both paths update the same UI elements, so the final state is always consistent
- SimpleDateFormat is used for timestamp display (locale-aware)
