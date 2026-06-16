# Luna App Diagnostic Documentation
**Date:** 2026-04-21  
**Version:** 1.0  
**Scope:** Logging System, Sleep Callbacks, Stress Graph Issue Diagnosis

---

## Table of Contents
1. [Logging System Documentation](#1-logging-system-documentation)
2. [Sleep Callbacks & Methods Documentation](#2-sleep-callbacks--methods-documentation)
3. [Logging Improvements & Enhancements](#3-logging-improvements--enhancements)
4. [New Callback Logging Requirements](#4-new-callback-logging-requirements)
5. [Stress Graph Issue Diagnosis & Fix Plan](#5-stress-graph-issue-diagnosis--fix-plan)

---

# 1. Logging System Documentation

## 1.1 Core Logging Classes Overview

| Class | File Location | Purpose |
|-------|---------------|---------|
| `AppLogs` | `commons/src/main/java/com/noisefit_commans/utils/AppLogs.kt` | Main app-level analytics & event logging |
| `LOGS` | `commons/src/main/java/com/noisefit_commans/utils/LOGS.kt` | Timber wrapper for debug console logging |
| `FileLogsUtils` | `commons/src/main/java/com/noisefit_commans/utils/FileLogsUtils.kt` | File compression, sharing via email |
| `ZhLogger` | `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/log/ZhLogger.kt` | SDK BLE/Behavior logging with rotation |
| `ZhBleLogUtils` | `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/log/ZhBleLogUtils.kt` | BLE and behavior log file utilities |

---

## 1.2 How Logs Are Written

### 1.2.1 AppLogs (Primary Analytics Logging)

**Location:** `commons/src/main/java/com/noisefit_commans/utils/AppLogs.kt`

```kotlin
object AppLogs {
    private var filePrinter: Printer? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val backgroundDispatcher = newFixedThreadPoolContext(1, "File Write-App")

    // Event-based logging (hex codes for categorization)
    fun sendAppLogs(event: LogEvents, subEvent: SubEvent) {
        scope.launch(backgroundDispatcher) {
            cleanLogFilesIfNecessary()
            val exception = "Exception = ${String.format("%02X %02X", event.code, subEvent.code)} ${comment}"
            XLog.printers(filePrinter).e(exception)
        }
    }

    // Text-based logging (most commonly used)
    fun sendAppLogs(logText: String) {
        scope.launch(backgroundDispatcher) {
            cleanLogFilesIfNecessary()
            XLog.printers(filePrinter).i(logText)
        }
    }
}
```

**Key Points:**
- Uses **XLog library** with `FilePrinter` for file output
- Writes occur on **single-threaded background dispatcher** to prevent race conditions
- Auto-cleans log files when they exceed size threshold
- Supports both event codes and free-text logging

### 1.2.2 LOGS Class (Debug Console)

**Location:** `commons/src/main/java/com/noisefit_commans/utils/LOGS.kt`

```kotlin
object LOGS : Timber.DebugTree() {
    fun e(error: String) { Timber.e(error) }
    fun d(log: String) { Timber.d(log) }
    fun i(log: String) { Timber.i(log) }
    fun w(log: String) { Timber.w(log) }
}
```

**Key Points:**
- **Timber wrapper** - logs appear in Logcat only
- **NOT persisted to files** - debug-only visibility
- No automatic server upload

### 1.2.3 ZhLogger (SDK BLE/Behavior Logging)

**Location:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/log/ZhLogger.kt`

```kotlin
fun writeFile(tag: String?, msg: String?, append: Boolean = true, isMainStyle: Boolean = true) {
    if (!isWriteLog) return
    createFile()
    val buffer = StringBuffer()
    buffer.append(logTime())  // Format: "yyyy-MM-dd HH:mm:ss:SSS"
    buffer.append(" ----> ")
    buffer.append(mTag)
    buffer.append(" ")
    buffer.append(msg)
    FileIOUtils.writeFileFromString(logFile, buffer.toString(), append)
}
```

**Key Points:**
- Date-based log files (e.g., `BLE_2026-04-21.log`)
- Configurable expiry period (default 3-10 days)
- Separate BLE and Behavior log directories

---

## 1.3 Log File Storage Locations

### File System Structure
```
externalCacheDir/
├── appLogs/
│   └── logs.txt              ← AppLogs primary output (XLog)
└── logs/
    └── logs.txt              ← FileLogsUtils output (email sharing)

externalFilesDir/ or filesDir/
└── log/
    ├── ble/
    │   └── BLE_yyyy-MM-dd.log     ← ZhBleLogUtils BLE logs
    └── behavior/
        └── BEHAVIOR_yyyy-MM-dd.log ← ZhBleLogUtils behavior logs
```

### Storage Location Logic

| Log Type | Release Build | Debug Build |
|----------|---------------|-------------|
| App Logs | `externalCacheDir/appLogs/` | Same |
| BLE Logs | `filesDir/log/ble/` | `externalFilesDir/log/ble/` |
| Behavior Logs | `filesDir/log/behavior/` | `externalFilesDir/log/behavior/` |

---

## 1.4 Log Data Sources

### Event Codes (Structured Logging)

**Location:** `commons/src/main/java/com/noisefit_commans/utils/AppLogs.kt`

```kotlin
sealed class LogEvents(val code: Int) {
    object Connect : LogEvents(0x01)     // BLE connection events
    object Binding : LogEvents(0x02)     // Device binding events
    object SyncData : LogEvents(0x03)    // Data sync events
    object Ota : LogEvents(0x04)         // OTA update events
    object WatchFace : LogEvents(0x05)   // Watch face transfer events
    object Agps : LogEvents(0x06)        // AGPS download events
}
```

### Data Callback Logging Sources

| Data Source | Log Function | What Gets Logged |
|-------------|--------------|------------------|
| BLE Connection | `AppLogs.sendAppLogs(LogEvents.Connect, ...)` | Connect/Disconnect/Timeout |
| Device Binding | `AppLogs.sendAppLogs(LogEvents.Binding, ...)` | Binding info issues |
| Data Sync | `AppLogs.sendAppLogs(LogEvents.SyncData, ...)` | Sync timeout/disconnect |
| OTA Updates | `AppLogs.sendAppLogs(LogEvents.Ota, ...)` | OTA failures |
| Watch Face | `AppLogs.sendAppLogs(LogEvents.WatchFace, ...)` | Transfer failures |
| AGPS | `AppLogs.sendAppLogs(LogEvents.Agps, ...)` | Download failures |
| SDK BLE Commands | `ZhBleLogUtils.bleLog(tag, msg)` | Raw BLE communication |
| User Behavior | `ZhBleLogUtils.behaviorLog(module, tag, msg)` | User actions |
| Fitness Callbacks | Direct `AppLogs.sendAppLogs("$TRACK_TAG ...")` | Data sync details |

### Fitness Data Callback Logging

The `TRACK_TAG = "LUNA->"` prefix is used for all fitness-related logs.

**Location:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

---

## 1.5 Server Upload Mechanism

### 1.5.1 API Endpoint

```kotlin
// Location: DeviceRepositoryImpl.kt line 480
val url = "${BuildConfig.BASE_URL_NEW}/logging/upload_logs"
```

### 1.5.2 Upload Methods

**1. Periodic Automatic Upload** (`periodicFeedbackFile`)
```kotlin
// NetworkService.kt
@Multipart
@POST
suspend fun periodicFeedbackFile(
    @Url url: String,
    @Part appLogs: MultipartBody.Part?,      // logs.txt
    @Part ringLogs: MultipartBody.Part?,      // BLE logs zip
    @Part firmwareLogs: MultipartBody.Part?, // Firmware logs zip
): BaseApiResponseData<Any>
```

**2. Manual Developer Report** (`reportToDeveloper`)
```kotlin
// URL: "${BuildConfig.BASE_URL_NEW}/luna/protean/v3/hamburger"
// Additional fields: title, description, mac, serial_no
```

### 1.5.3 Upload Trigger Conditions

**Location:** `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt` (lines 952-962)

```kotlin
fun shouldSyncAutoLogs(): Boolean {
    val lastTimeStamp = ringDataStore.getAutoLogsTimeStamp()
    val logSyncInterval = localDataStore.getLogSyncInterval()  // Default: 2 hours
    
    // Disabled if interval is 0
    if (logSyncInterval == 0) return false
    
    // First time - save timestamp and skip
    if (lastTimeStamp == 0L) {
        ringDataStore.saveAutoLogsTimeStamp()
        return false
    }
    
    // Check if enough time has passed
    return lastTimeStamp.checkDayDifferenceMoreNMinutes(logSyncInterval * 60)
}
```

### 1.5.4 When Logs Sync to Server

| Trigger | Condition | Frequency |
|---------|-----------|-----------|
| Automatic (OreoSyncDataWork) | `shouldSyncAutoLogs() == true` | Every 2 hours (configurable) |
| Manual (Profile menu) | User initiates | On demand |
| Data Sync Complete | During `OreoSyncDataWork.doWork()` | After data sync |

**Automatic Upload Flow:**
```
OreoSyncDataWork.doWork()
    ↓
shouldSyncAutoLogs() check
    ↓ (if true)
ApplicationUtils.startFeedbackSubmitWorker(context)
    ↓
FeedbackSubmitWorker.doWork()
    ↓
NetworkService.periodicFeedbackFile()
    ↓
Server receives logs
```

### 1.5.5 Server Sync Configuration

| Preference Key | Storage | Default | Purpose |
|----------------|---------|---------|---------|
| `LAST_SYNC_LOGS` | RingDataStoreImpl | `0L` | Timestamp of last upload |
| `LOGS_SYNC_INTERVAL` | DataStoredImpl | `2` hours | Sync interval (server-configurable) |

**Server Configuration Fetch:**
```kotlin
// SplashViewModel.kt
localDataStore.saveLogSyncInterval(it.logsSyncInterval ?: 2)
```

### 1.5.6 Why Logs May Appear Old on Server

**Root Causes:**
1. **Interval not elapsed:** If `logSyncInterval` hours haven't passed since last upload, new logs won't sync
2. **Worker not triggered:** `OreoSyncDataWork` must complete for auto-upload check to occur
3. **Network connectivity:** `FeedbackSubmitWorker` has `NetworkType.CONNECTED` constraint
4. **Log file size limit:** If file exceeds threshold, old lines are removed before new ones are written
5. **File not refreshed:** Server may cache old files; new upload replaces entire file

---

## 1.6 Log Rotation & Cleanup

### 1.6.1 AppLogs Size-Based Cleanup

**Location:** `AppLogs.kt` (lines 111-134)

```kotlin
private fun cleanLogFilesIfNecessary() {
    val files = logDir.listFiles() ?: return
    for (file in files) {
        if (file.sizeInMb > 2) {  // 2MB threshold
            clearNLines(file, 2000)  // Remove first 2000 lines
        }
    }
}

fun deleteFile() {
    val files = logDir.listFiles() ?: return
    for (file in files) { file.delete() }
}
```

### 1.6.2 ZhLogger Time-Based Cleanup

**Location:** `ZhLogger.kt` (lines 164-190)

```kotlin
fun clearExpiredFile() {
    // expiredDay: default 10 days (BLE logs: 3 days)
    val logFiles = FileUtils.listFilesInDirWithFilter(logDir, { ... })
    for (file in logFiles) {
        val fileTime = dateFormat.parse(fileNameDate)?.time ?: 0L
        val expired = abs(currentTime - fileTime) > expiredDay * 24 * 60 * 60 * 1000L
        if (expired) {
            file.delete()
        }
    }
}
```

### 1.6.3 Cleanup Thresholds

| Log Type | Cleanup Trigger | Action |
|----------|-----------------|--------|
| App Logs (logs.txt) | File > 2MB | Remove first 2000 lines |
| BLE Logs | File > 3 days old | Delete file |
| Behavior Logs | File > 3 days old | Delete file |
| Zip files | Before new zip creation | Delete all existing zips in directory |

---

# 2. Sleep Callbacks & Methods Documentation

## 2.1 Sleep Callback Interfaces

### FitnessDataCallBack (SDK Interface)

**Registration:** `CallBackUtils.fitnessDataCallBack = fitnessDataCallBack`

| Callback Method | SDK Bean | Device Type | Purpose |
|-----------------|----------|-------------|---------|
| `onSleepData(SleepBean)` | SleepBean | Luna/Luna Band | Legacy sleep data |
| `onRingSleepResult(RingSleepResultBean)` | RingSleepResultBean | Ring devices | Ring sleep data |
| `onRingSleepNAP(List<RingSleepNapBean>)` | RingSleepNapBean | Ring devices | Nap data |
| `onSleepRRIData(SleepRRIBean)` | SleepRRIBean | v2.3.1+ devices | Sleep R-R interval data |
| `onSleepHRVData(SleepHRVBean)` | SleepHRVBean | v2.3.1+ devices | Sleep HRV metrics |

---

## 2.2 SDK Sleep Data Models

### 2.2.1 SleepBean (Legacy Devices)

**Used by:** Luna, Luna Band

```kotlin
public class SleepBean extends BaseBean implements Serializable {
    public long startSleepTimestamp;      // Sleep start time (Unix seconds)
    public long endSleepTimestamp;        // Sleep end time (Unix seconds)
    public int sleepDuration;             // Total sleep duration
    public int sleepScore;                // Sleep score (0-100)
    public int awakeTime;                 // Total awake time
    public int awakeTimePercentage;       // Awake percentage
    public int lightSleepTime;            // Light sleep duration
    public int lightSleepTimePercentage;
    public int deepSleepTime;             // Deep sleep duration
    public int deepSleepTimePercentage;
    public int rapidEyeMovementTime;      // REM duration
    public int rapidEyeMovementTimePercentage;
    public boolean isNightSleep;          // Night sleep flag
    public List<SleepDistributionData> list;  // Sleep phase breakdowns
    public float readinessScore;          // Readiness score
}
```

### 2.2.2 RingSleepResultBean (Ring Devices)

```kotlin
public class RingSleepResultBean extends BaseBean implements Serializable {
    public boolean isExistSleep;      // Has sleep data
    public int entryTime;             // Sleep start (Unix seconds)
    public int exitTime;              // Wake up time (Unix seconds)
    public int sleepDuration;         // Duration (seconds)
    public int timeInBedTime;         // Time in bed (seconds)
    public int sleepLatency;          // Time to fall asleep (seconds)
    public int sleepEfficiency;       // Efficiency (0-100%)
    public int sleepScore;            // Score (0-99)
    public int awakeTime;             // Total awake
    public int lightSleepTime;        // Light sleep
    public int deepSleepTime;         // Deep sleep
    public int rapidEyeMovementTime;  // REM
    public List<SleepDistribution> sleepDistributionData;   // Sleep phases
    public List<SleepMovementsData> sleepMovementsData;     // Night movements
}
```

### 2.2.3 RingSleepNapBean (Nap Data)

```kotlin
public class RingSleepNapBean extends BaseBean implements Serializable {
    public boolean existSleepNap;     // Has nap
    public int asleepNapTime;         // Nap start (Unix seconds)
    public int wakeupNapTime;         // Nap end (Unix seconds)
    public int sleepNapDuration;      // Duration (seconds)
}
```

---

## 2.3 App Sleep Data Models

### 2.3.1 OreoSleepData (Room Entity)

**Location:** `commons/src/main/java/com/noisefit_commans/data/model/OreoDbTable.kt`

```kotlin
@Entity(tableName = "sleep_data")
data class OreoSleepData(
    var id: Int = 0,
    var isSynced: Boolean = false,
    var startTime: String? = null,
    var endTime: String? = null,
    var date: String? = null,
    var total: Int = 0,              // Total sleep (seconds)
    var timeInBedTime: Int = 0,      // Time in bed (seconds)
    var sleepLatency: Int = 0,       // Latency (seconds)
    var sleepEfficiency: Int = 0,    // Efficiency (0-100)
    var deep: Int = 0,               // Deep sleep (seconds)
    var light: Int = 0,              // Light sleep (seconds)
    var awake: Int = 0,              // Awake time (seconds)
    var remCount: Int = 0,           // REM time (seconds)
    var sleepScore: Int = 0,
    var startTimeStamp: Long? = null,
    var endTimeStamp: Long? = null,
    var readinessScore: Int? = 0,
    var sleepArray: ArrayList<OreoSleepDataBreakup>? = null,
    var nightTimeMovement: ArrayList<OreoSleepMovementDataBreakup>? = null
)
```

### 2.3.2 OreoNapData (Room Entity)

```kotlin
@Entity(tableName = "nap_data")
data class OreoNapData(
    var id: Int = 0,
    var isSynced: Boolean = false,
    var startTime: String? = null,
    var endTime: String? = null,
    var duration: Int = 0,           // Duration (minutes)
    var date: String? = null
)
```

---

## 2.4 Complete Sleep Data Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      DEVICE → APP SLEEP DATA FLOW                           │
└─────────────────────────────────────────────────────────────────────────────┘

1. CALLBACK REGISTRATION
   ├── Location: ZhUserActivityHandler.attachCallbacks()
   └── Code: CallBackUtils.fitnessDataCallBack = fitnessDataCallBack

2. SDK CALLBACKS TRIGGERED (During sync or daily data fetch)
   │
   ├── onSleepData(SleepBean)              [Luna/Luna Band]
   │   └── Line ~466 in ZhUserActivityHandler.kt
   │
   ├── onRingSleepResult(RingSleepResultBean)  [Ring devices]
   │   └── Line ~723 in ZhUserActivityHandler.kt
   │
   ├── onRingSleepNAP(List<RingSleepNapBean>)  [Nap data]
   │   └── Line ~759 in ZhUserActivityHandler.kt
   │
   ├── onSleepRRIData(SleepRRIBean)        [v2.3.1+ only]
   │   └── Line ~580 in ZhUserActivityHandler.kt
   │
   └── onSleepHRVData(SleepHRVBean)        [v2.3.1+ only]
       └── Line ~588 in ZhUserActivityHandler.kt

3. DATA PARSING (OreoDataConverter)
   │
   ├── parseSleepData(SleepBean): OreoSleepData?
   │   └── Lines 1104-1211 in OreoDataConverter.kt
   │   └── Uses LunaSdk231DataNormalizer for unit normalization
   │
   ├── parseSleepData(RingSleepResultBean): OreoSleepData?
   │   └── Lines 1215-1317 in OreoDataConverter.kt
   │
   └── parseNapData(List<RingSleepNapBean>): List<OreoNapData>
       └── Lines 1023-1102 in OreoDataConverter.kt

4. CALLBACKS TO APP LAYER
   │
   ├── UserActivityCallback.SleepDataObtainedOreo(OreoSleepData)
   │
   └── UserActivityCallback.NapObtainedOreo(List<OreoNapData>)

5. DATA STORAGE (OreoSyncDataWork)
   │
   ├── SleepDataObtainedOreo →
   │   └── syncRepository.saveSleepData(sleepData)
   │       └── sleepDataImpl.insertData(data)
   │           └── sleepDao.insert(data)  [Room DB: sleep_data]
   │
   └── NapObtainedOreo →
       └── syncRepository.saveNapData(napList)
           └── napDataSource.insertData(napList)  [Room DB: nap_data]
```

---

## 2.5 Sleep Type Enumerations

**Location:** `commons/src/main/java/com/noisefit_commans/models/ColorfitData.kt`

```kotlin
enum class SleepType(val type: String) {
    DEEP("deep"),       // SDK Type: 2
    LIGHT("light"),     // SDK Type: 1
    SOBER("sober"),     // Legacy
    AWAKE("awake"),     // SDK Type: 0
    REM("rem")          // SDK Type: 3
}

enum class SleepMovementType {
    NO_MOVEMENT,        // SDK Type: 0
    LOW,                // SDK Type: 1
    MEDIUM,             // SDK Type: 2
    INTENSE             // SDK Type: 3
}
```

---

## 2.6 When Sleep Callbacks Are Triggered

| Trigger | Callbacks Fired |
|---------|-----------------|
| Daily sync (`getDailyHistoryData`) | All sleep callbacks |
| Background auto-sync (periodic worker) | All sleep callbacks |
| `onProgress(total, total)` | Indicates sync complete |

---

## 2.7 Key Files Reference

| File | Purpose |
|------|---------|
| `noisefit_zh_sdk/.../ZhUserActivityHandler.kt` | Callback implementation |
| `noisefit_zh_sdk/.../OreoDataConverter.kt` | Sleep data parsing |
| `commons/.../UserActivityCallback.kt` | Callback sealed classes |
| `commons/.../OreoDbTable.kt` | Room entities |
| `app/.../OreoSleepDataImpl.kt` | Sleep storage implementation |
| `app/.../OreoSyncDataWork.kt` | Sync data handling |

---

# 3. Logging Improvements & Enhancements

## 3.1 Current Logging Architecture Gaps

### 3.1.1 Callbacks WITHOUT AppLogs

The following callbacks have **no** `AppLogs.sendAppLogs()` calls:

| Callback | Current Logging |
|----------|-----------------|
| `onOfflineHeartRateData` | Only `LOGS.d()` (console only) |
| `onEffectiveStandingData` | Only `LOGS.d()` (console only) |
| `onActivityDurationData` | **Completely empty** |
| `onOffEcgData` | **Completely empty** |
| `onExaminationData` | **Completely empty** |
| `onRingBatteryData` | **Completely empty** |
| `onDrinkWaterData` | **Completely empty** |

### 3.1.2 Callbacks WITH AppLogs (Reference)

These callbacks properly log via `AppLogs.sendAppLogs()`:

- `onProgress`
- `onDailyData`
- `onSleepData`
- `onContinuousHeartRateData`
- `onContinuousBloodOxygenData`
- `onOfflineBloodOxygenData`
- `onContinuousPressureData`
- `onOfflinePressureData`
- `onContinuousTemperatureData`
- `onOfflineTemperatureData`
- `onRingHealthScore`
- `onRingSleepResult`
- `onRingSleepNAP`
- `onRingAutoActiveSportData`
- `onRingBodyBatteryData`
- `onRingStressDetectionData`
- `onRingTodayRespiratoryRateData`
- `onRingOverallDayMovementData`
- `onRingTodayActiveTypeData`

---

## 3.2 Recommended Improvements

### 3.2.1 Add Logging to Missing Callbacks

**File:** `noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUserActivityHandler.kt`

For each empty callback, add:
```kotlin
override fun onActivityDurationData(data: ActivityDurationBean?) {
    AppLogs.sendAppLogs("$TRACK_TAG onActivityDurationData : ${Gson().toJson(data)}")
    // ... existing logic
}
```

### 3.2.2 Create Unified Logging Helper

Add a helper method to standardize logging:

```kotlin
private fun logCallback(callbackName: String, data: Any?) {
    val json = Gson().toJson(data)
    LOGS.d(TAG, "$callbackName : $json")
    AppLogs.sendAppLogs("$TRACK_TAG $callbackName : $json")
}
```

Usage:
```kotlin
override fun onRingBatteryData(data: RingBatteryBean?) {
    logCallback("onRingBatteryData", data)
}
```

### 3.2.3 Implement Automatic Callback Logging

Create a base class or interface decorator:

```kotlin
interface LoggingFitnessDataCallBack : FitnessDataCallBack {
    override fun onActivityDurationData(data: ActivityDurationBean?) {
        AppLogs.sendAppLogs("$TRACK_TAG onActivityDurationData : ${Gson().toJson(data)}")
        handleActivityDurationData(data)
    }
    
    fun handleActivityDurationData(data: ActivityDurationBean?)
}
```

---

## 3.3 Ensuring Data Shows in Logs

### 3.3.1 Checklist for Complete Logging

1. **Add AppLogs.sendAppLogs()** to every callback
2. **Include raw JSON** for debugging: `Gson().toJson(data)`
3. **Use TRACK_TAG prefix** for consistent filtering
4. **Call saveRawSdkPayload()** for important data (stores to SharedPreferences too)

### 3.3.2 Where to Make Changes

| Change | File Location |
|--------|---------------|
| Add callback logging | `ZhUserActivityHandler.kt` lines 350-900 |
| Add new log events | `AppLogs.kt` (LogEvents sealed class) |
| Modify log storage | `AppLogs.kt` (filePrinter setup) |
| Adjust sync interval | Server config or `DataStoredImpl` |

---

## 3.4 Improving Log Server Updates

### 3.4.1 Current Issues

1. **2-hour default interval** may be too long for debugging
2. **No manual refresh** without user action
3. **Old data persists** if sync fails silently

### 3.4.2 Improvements

**1. Reduce sync interval for debugging:**
```kotlin
// In DataStoredImpl or via server config
localDataStore.saveLogSyncInterval(1) // 1 hour or even 0.5 hours
```

**2. Force immediate sync after important events:**
```kotlin
// After critical errors or specific callbacks
ApplicationUtils.startFeedbackSubmitWorker(context)
ringDataStore.saveAutoLogsTimeStamp() // Reset timer
```

**3. Add sync status logging:**
```kotlin
// In FeedbackSubmitWorker
AppLogs.sendAppLogs("$TRACK_TAG Log sync started")
// After success:
AppLogs.sendAppLogs("$TRACK_TAG Log sync completed")
```

---

# 4. New Callback Logging Requirements

## 4.1 Will New Callbacks Automatically Log?

**NO.** New callbacks will **NOT** automatically have their data logged. You must manually add logging code.

---

## 4.2 Required Steps for New Callback Logging

### Step 1: Add AppLogs in Callback Implementation

**File:** `ZhUserActivityHandler.kt`

```kotlin
override fun onYourNewCallback(data: YourDataBean?) {
    // Step 1: Console logging
    LOGS.d(TAG, "onYourNewCallback : ${Gson().toJson(data)}")
    
    // Step 2: File logging (persisted & uploaded to server)
    AppLogs.sendAppLogs("$TRACK_TAG onYourNewCallback : ${Gson().toJson(data)}")
    
    // Step 3 (Optional): Raw payload storage for debugging
    watchDataStore.testSaveYourNewCallbackData(Gson().toJson(data))
    
    // Step 4: Pass to app layer
    userActivityDataCallbacks?.onUserActivityDataReceived(
        UserActivityCallback.YourNewCallbackDataObtained(
            oreoDataConverter.parseYourNewData(data)
        )
    )
}
```

### Step 2: Add UserActivityCallback Sealed Class

**File:** `commons/src/main/java/com/noisefit_commans/interfaces/data/UserActivityCallback.kt`

```kotlin
sealed class UserActivityCallback {
    // ... existing callbacks ...
    
    class YourNewCallbackDataObtained(val data: YourParsedData) : UserActivityCallback()
}
```

### Step 3: Handle in OreoSyncDataWork

**File:** `app/src/main/java/com/oreo/receiver/workManager/OreoSyncDataWork.kt`

```kotlin
when (userActivityCallback) {
    // ... existing handlers ...
    
    is UserActivityCallback.YourNewCallbackDataObtained -> {
        val data = userActivityCallback.data
        syncRepository.saveYourNewData(data)
    }
}
```

### Step 4: Add Data Persistence

**File:** Create `OreoYourNewDataImpl.kt`

```kotlin
class OreoYourNewDataImpl @Inject constructor(
    private val yourNewDao: YourNewDao
) : YourNewDataSource {
    override suspend fun insertData(data: YourParsedData): Boolean {
        yourNewDao.insert(data)
        return true
    }
}
```

---

## 4.3 Using saveRawSdkPayload Helper

For callbacks that need both logging AND raw storage:

```kotlin
private fun saveRawSdkPayload(label: String, raw: String?, saver: (String?) -> Unit) {
    LOGS.d(TAG, "$label : $raw")
    AppLogs.sendAppLogs("$TRACK_TAG $label : $raw")
    saver(raw)
}

// Usage:
override fun onYourNewCallback(data: YourDataBean?) {
    saveRawSdkPayload(
        label = "onYourNewCallback",
        raw = Gson().toJson(data),
        saver = watchDataStore::testSaveYourNewCallbackJson
    )
}
```

---

## 4.4 Complete Checklist for New Callback

- [ ] Add callback override in `ZhUserActivityHandler.kt`
- [ ] Add `LOGS.d()` for console output
- [ ] Add `AppLogs.sendAppLogs()` for file logging
- [ ] (Optional) Add `watchDataStore.testSave...()` for raw storage
- [ ] Add `UserActivityCallback` sealed class variant
- [ ] Add parser in `OreoDataConverter.kt`
- [ ] Add Room entity if needed in `OreoDbTable.kt`
- [ ] Add DAO interface
- [ ] Add DataSource implementation
- [ ] Add handler in `OreoSyncDataWork.kt`
- [ ] Add repository method in `OreoSyncRepositoryImpl.kt`

---

# 5. Stress Graph Issue Diagnosis & Fix Plan

## 5.1 Issue Description

**Reported:** User observed stress graph displaying data for time periods when the band/ring was NOT being worn.

---

## 5.2 Root Cause Analysis

### 5.2.1 How Stress Data Flows

```
SDK (Device Memory) → onContinuousPressureData(ContinuousPressureBean)
                      OR
                      onRingStressDetectionData(RingStressDetectionBean)
                      ↓
                 ZhUserActivityHandler
                      ↓
                 OreoDataConverter.parseStressData()
                      ↓
                 LunaSdk231DataNormalizer.normalizeLegacyMetricBreakup()
                 (30-sec raw → 5-min buckets, filters 0 and 255)
                      ↓
                 LunaSdk231DataNormalizer.trimFutureMetricBreakupForToday()
                 (zeros out future same-day slots)
                      ↓
                 OreoStressDataBreakup / OreoBodyStressData
                      ↓
                 OreoStressDataImpl.insertData() WITH MERGE LOGIC ← PROBLEM HERE
                      ↓
                 Home Stress Graph rendering
```

### 5.2.2 The Core Problem: Merge Logic Preserves Old Data

**Location:** `app/src/main/java/com/oreo/data/db/implementation/OreoStressDataImpl.kt` (lines 44-60)

```kotlin
private fun getMergedData(prevData: OreoStressDataBreakup, newData: OreoStressDataBreakup): List<Int> {
    val prevBreakup = Gson().fromJson<List<Int>>(prevData.breakUp ?: "")
    val newBreakup = Gson().fromJson<List<Int>>(newData.breakUp ?: "")
    val mergedData = ArrayList<Int>()
    
    newBreakup.forEachIndexed { index, value ->
        if (value == 0) {
            mergedData.add(prevBreakup[index])  // ← KEEPS OLD NON-ZERO VALUES
        } else {
            mergedData.add(value)
        }
    }
    return mergedData
}
```

**Same issue in `OreoBodyStressDataImpl.kt`:**
```kotlin
private fun getMergedData(prevData: OreoBodyStressData, newData: OreoBodyStressData): List<Int> {
    // ... same merge logic ...
    if (value == 0) {
        mergedData.add(prevBreakup[index])  // ← KEEPS OLD NON-ZERO VALUES
    }
}
```

### 5.2.3 Why This Causes the Issue

**Scenario:**
1. **Morning (9 AM):** User wears device, stress data recorded for slots 0-36 (5-min intervals = 3 hours)
2. **Afternoon (12 PM):** User removes device
3. **Evening (6 PM):** App syncs. SDK returns:
   - Slots 0-36: Valid stress values from morning
   - Slots 37-72: Zeros (device not worn)
4. **Next day sync:** SDK returns mostly zeros for yesterday
5. **Merge logic:** `if (value == 0) { mergedData.add(prevBreakup[index]) }`
   - Keeps the morning values for ALL slots
   - Old data persists indefinitely

### 5.2.4 No Wear Detection Gate

The stress data callback flow **does NOT check current wearing status** before storing:

```kotlin
// ZhUserActivityHandler.kt line 556-578
override fun onContinuousPressureData(data: ContinuousPressureBean) {
    // No wear check here!
    userActivityDataCallbacks?.onUserActivityDataReceived(
        UserActivityCallback.StressDataObtainedOreo(
            oreoDataConverter.parseStressData(data)
        )
    )
}
```

The wear detection API exists but is used separately:
```kotlin
// Separate API - not integrated with stress data flow
getRingWearingStatus(listener)  // Query wear status
onRingWearingStatus(int status) // Callback: 0=not worn, 1=worn
```

### 5.2.5 SDK Value Semantics

| Value | Meaning |
|-------|---------|
| `0` | Invalid/missing measurement (no sensor contact) |
| `255` | Invalid/out-of-range marker |
| `1-100` | Valid stress level |

**Current filtering:**
```kotlin
// LunaSdk231DataNormalizer
val validValues = bucket.filter { it > 0 && it != 255 }
```

This correctly identifies invalid readings, but the **merge logic** defeats it.

---

## 5.3 Contributing Factors

### 5.3.1 Historical Data Sync

The SDK syncs accumulated day-data from device memory. If the device was worn earlier, that data persists and gets displayed regardless of current wear status.

### 5.3.2 Server Fallback

**Location:** `app/src/main/java/com/oreo/data/dataConverter/OreoStressDataConvertor.kt`

If local data is empty, it falls back to server stress data:
```kotlin
dayData.stress?.breakUp?.forEachIndexed { index, i ->
    items.add(Item(i, index))
}
```

Server may have older cached data.

### 5.3.3 trimFutureMetricBreakupForToday Only Works for Same Day

**Location:** `OreoDataConverter.kt` (lines 1404-1434)

```kotlin
fun trimFutureMetricBreakupForToday(...): List<Int> {
    // Only applies to same-day data
    if (!dataDate.equals(currentDate, true)) {
        return values  // Past days returned as-is!
    }
    // ... zeros out future slots for today only
}
```

Past day data is **not modified** by this function.

---

## 5.4 Issue Introduction Timeline

This issue was **always present** in the architecture. It's not a regression but a design limitation:

1. **Original design intent:** Merge logic was meant to preserve valid readings when sync returned partial data
2. **Assumption:** New sync would provide complete data, not zeros for "not worn" periods
3. **Reality:** SDK returns zeros when device can't measure (not worn), but merge keeps old values

---

## 5.5 Detailed Fix Implementation Plan

### 5.5.1 Option A: Add Wear Detection Gate (Recommended)

**Complexity:** Medium  
**Risk:** Low  
**Impact:** Prevents storing stress data when device is not worn

**Implementation:**

1. **Track current wear status globally**

```kotlin
// In WatchDataStore or RingDataStore
interface WatchDataStore {
    fun setCurrentWearStatus(isWorn: Boolean)
    fun getCurrentWearStatus(): Boolean
}
```

2. **Update wear status on callback**

```kotlin
// ZhQueryDeviceUnitsHandler.kt
override fun onRingWearingStatus(status: Int) {
    val isWorn = (status == 1)
    watchDataStore.setCurrentWearStatus(isWorn)
    // ... existing logic
}
```

3. **Check wear status before storing stress data**

```kotlin
// ZhUserActivityHandler.kt
override fun onContinuousPressureData(data: ContinuousPressureBean) {
    // Only process if currently wearing OR if data is historical
    val isHistoricalData = !isDataFromToday(data.date)
    if (!watchDataStore.getCurrentWearStatus() && !isHistoricalData) {
        AppLogs.sendAppLogs("$TRACK_TAG onContinuousPressureData IGNORED - device not worn")
        return
    }
    // ... existing processing
}
```

**Limitation:** This only works for real-time sync, not historical data.

---

### 5.5.2 Option B: Modify Merge Logic (Recommended with A)

**Complexity:** Low  
**Risk:** Medium (may lose legitimate data during partial syncs)  
**Impact:** Zeros overwrite old values, showing accurate "no data" gaps

**Implementation:**

1. **Change merge to allow zero overwrites for past time slots**

```kotlin
// OreoStressDataImpl.kt
private fun getMergedData(
    prevData: OreoStressDataBreakup, 
    newData: OreoStressDataBreakup,
    currentTimeMinutes: Int
): List<Int> {
    val prevBreakup = Gson().fromJson<List<Int>>(prevData.breakUp ?: "")
    val newBreakup = Gson().fromJson<List<Int>>(newData.breakUp ?: "")
    val mergedData = ArrayList<Int>()
    
    newBreakup.forEachIndexed { index, value ->
        val slotMinutes = index * 5  // 5-minute slots
        
        if (value == 0) {
            // For past slots: trust the zero (device wasn't worn)
            // For current/future slots: keep previous value (partial sync)
            if (slotMinutes < currentTimeMinutes - 30) {  // 30-min buffer
                mergedData.add(0)  // Trust the zero
            } else {
                mergedData.add(prevBreakup.getOrElse(index) { 0 })
            }
        } else {
            mergedData.add(value)
        }
    }
    return mergedData
}
```

---

### 5.5.3 Option C: Add Validity Window to SDK Data

**Complexity:** High  
**Risk:** Low  
**Impact:** Only trust stress values within their measurement window

**Implementation:**

1. **Track when device was last worn**

```kotlin
// Store wear periods
data class WearPeriod(
    val startTimestamp: Long,
    val endTimestamp: Long?  // null = currently wearing
)
```

2. **Validate stress data against wear periods**

```kotlin
fun isStressSlotValid(slotIndex: Int, date: String, wearPeriods: List<WearPeriod>): Boolean {
    val slotTimestamp = calculateSlotTimestamp(date, slotIndex)
    return wearPeriods.any { period ->
        slotTimestamp >= period.startTimestamp &&
        (period.endTimestamp == null || slotTimestamp <= period.endTimestamp)
    }
}
```

---

### 5.5.4 Option D: UI-Level Filtering

**Complexity:** Low  
**Risk:** Low  
**Impact:** Don't display stress data for "not worn" periods

**Implementation:**

```kotlin
// OreoStressDataConvertor.kt
fun convertStressData(data: OreoStressDataBreakup?, wearPeriods: List<WearPeriod>): List<StressItem> {
    return data?.breakUp?.mapIndexed { index, value ->
        if (isSlotWithinWearPeriod(index, data.date, wearPeriods)) {
            StressItem(value, index)
        } else {
            StressItem(0, index)  // Show as no data
        }
    } ?: emptyList()
}
```

---

## 5.6 Recommended Fix Strategy

**Phase 1 (Immediate):**
1. Implement **Option B** - Modify merge logic to trust zeros for past slots
2. Add logging for merge decisions

**Phase 2 (Short-term):**
1. Implement **Option A** - Add wear detection gate for real-time data
2. Store wear periods in database

**Phase 3 (Long-term):**
1. Implement **Option C** - Full wear period validation
2. Add UI indicators for "device not worn" periods

---

## 5.7 Files to Modify

| File | Change |
|------|--------|
| `OreoStressDataImpl.kt` | Modify `getMergedData()` |
| `OreoBodyStressDataImpl.kt` | Modify `getMergedData()` |
| `ZhUserActivityHandler.kt` | Add wear check before processing |
| `WatchDataStore.kt` | Add wear status tracking |
| `ZhQueryDeviceUnitsHandler.kt` | Update wear status on callback |
| `OreoStressDataConvertor.kt` | (Optional) UI-level filtering |

---

## 5.8 Testing Recommendations

1. **Test Case 1:** Sync after wearing device for 2 hours, then removing for 4 hours
   - Expected: Graph shows data for first 2 hours only

2. **Test Case 2:** Multiple syncs on same day with device worn intermittently
   - Expected: Graph shows accurate wear/no-wear periods

3. **Test Case 3:** Historical data sync for previous day
   - Expected: Past day data reflects actual wear times

4. **Test Case 4:** Device reconnection after being off-wrist
   - Expected: New zeros don't get old data merged in

---

# Appendix A: Quick Reference

## Log Function Reference

| Function | Output | Persisted | Uploaded |
|----------|--------|-----------|----------|
| `LOGS.d()` | Logcat | No | No |
| `AppLogs.sendAppLogs()` | logs.txt | Yes | Yes (2hr) |
| `ZhBleLogUtils.bleLog()` | BLE_*.log | Yes | Yes |
| `ZhBleLogUtils.behaviorLog()` | BEHAVIOR_*.log | Yes | Yes |

## Key File Locations

| Purpose | File Path |
|---------|-----------|
| App logging | `commons/.../utils/AppLogs.kt` |
| Debug logging | `commons/.../utils/LOGS.kt` |
| SDK BLE logging | `noisefit_zh_sdk/.../log/ZhBleLogUtils.kt` |
| Fitness callbacks | `noisefit_zh_sdk/.../handler/ZhUserActivityHandler.kt` |
| Data parsing | `noisefit_zh_sdk/.../handler/OreoDataConverter.kt` |
| Stress storage | `app/.../db/implementation/OreoStressDataImpl.kt` |
| Body stress storage | `app/.../db/implementation/OreoBodyStressDataImpl.kt` |
| Log sync worker | `app/.../workManager/FeedbackSubmitWorker.kt` |
| Sync orchestrator | `app/.../workManager/OreoSyncDataWork.kt` |

---

**Document End**
