# NoiseFit Luna - Complete Workout Implementation Guide

> Last Updated: 28 April 2026

This document provides a comprehensive understanding of how workouts are implemented in the NoiseFit Luna Android application, covering the entire flow from user selection to data synchronization.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Workout Selection Flow](#2-workout-selection-flow)
3. [Starting a Workout](#3-starting-a-workout)
4. [During Workout - Live Data Collection](#4-during-workout---live-data-collection)
5. [Pause, Resume & Stop Operations](#5-pause-resume--stop-operations)
6. [GPS & Location Tracking](#6-gps--location-tracking)
7. [Timer & Duration Tracking](#7-timer--duration-tracking)
8. [Workout Data Sync](#8-workout-data-sync)
9. [Sport Types & ID Mapping](#9-sport-types--id-mapping)
10. [Error Handling & Edge Cases](#10-error-handling--edge-cases)
11. [Key Classes Reference](#11-key-classes-reference)
12. [Sequence Diagrams](#12-sequence-diagrams)

---

## 1. Architecture Overview

The workout system follows a layered architecture:

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer                                 │
│  SelectWorkoutFragment → RecordWorkoutFragmentV2 → PostWorkout  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                             │
│        RecordWorkoutV2ViewModel + SelectWorkoutViewModel         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Session Manager                              │
│         Orchestrates device communication via Actions            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SDK Handler Layer                              │
│                  ZhUpdateDeviceUnitsHandler                      │
│                    ZhUserActivityHandler                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ZH SDK (BLE)                                 │
│              ControlBleTools.getInstance()                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Luna Band Device                             │
└─────────────────────────────────────────────────────────────────┘
```

### Key Components

| Component | File Path | Purpose |
|-----------|-----------|---------|
| `SelectWorkoutFragment` | [app/src/main/java/com/oreo/ui/recordworkout/SelectWorkoutFragment.kt](../app/src/main/java/com/oreo/ui/recordworkout/SelectWorkoutFragment.kt) | Workout selection screen |
| `RecordWorkoutFragmentV2` | [app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt](../app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutFragmentV2.kt) | Main workout recording UI |
| `RecordWorkoutV2ViewModel` | [app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutV2ViewModel.kt](../app/src/main/java/com/oreo/ui/recordworkout/v2/RecordWorkoutV2ViewModel.kt) | State management & business logic |
| `ZhUpdateDeviceUnitsHandler` | [noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt](../noisefit_zh_sdk/src/main/java/com/noisefit_zhsdk/handler/ZhUpdateDeviceUnitsHandler.kt) | SDK communication handler |
| `LocationService` | [commons/src/main/java/com/noisefit_commans/location/LocationService.kt](../commons/src/main/java/com/noisefit_commans/location/LocationService.kt) | GPS tracking service |

---

## 2. Workout Selection Flow

### 2.1 Entry Point

User taps "Record Workout" from the home screen, navigating to `SelectWorkoutFragment`.

### 2.2 Workout List Loading

```kotlin
// SelectWorkoutFragment.kt
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewModel.getWorkoutList(isConnectedToInternet())
}
```

The workout list is fetched from the server and displayed in a RecyclerView using `OSelectWorkoutAdapter`.

### 2.3 Pre-Flight Checks

Before starting a workout, several validations occur:

```kotlin
fun navigateToStartWorkout(oWorkoutListModal: OWorkoutListModal) {
    // Check 1: Device connected?
    if (!viewModel.isDeviceConnected()) {
        navigate(R.id.bottomSheetRingConnecting)
        return
    }
    
    // Check 2: Device charging?
    if (viewModel.isDeviceCharging()) {
        navigate(R.id.bottomSheetRingCharging)
        return
    }
    
    // Check 3: Battery too low?
    if (viewModel.isBatteryLow()) {
        navigate(R.id.bottomSheetRingBatteryLow)
        return
    }
    
    // Check 4: Workout already in progress?
    if (viewModel.isWorkoutOngoing()) {
        navigate(R.id.bottomSheetWorkoutInProgress)
        return
    }
    
    // All checks passed - proceed to workout screen
    startWorkout(oWorkoutListModal)
}
```

### 2.4 Data Model

```kotlin
// OWorkoutListModal.kt
@Parcelize
data class OWorkoutListModal(
    val id: Int? = null,
    val ringId: Int? = null,           // Sport ID for the ring/band
    val iconUrl: String? = null,        // Workout icon URL
    val activityType: String? = null,   // Activity name (e.g., "outdoor_running")
    val workoutText: String? = null,    // Translated display name
    val lowIntensity: Float? = null,    // MET values for calories
    val mediumIntensity: Float? = null,
    val highIntensity: Float? = null,
    var isGpsRequired: Int = 0,         // 1 = GPS required
    val dataType: String? = null,
    val dataPriority: String? = null
) : Parcelable
```

---

## 3. Starting a Workout

### 3.1 Countdown Initiation

When user taps "Start" button:

```kotlin
// RecordWorkoutFragmentV2.kt
binding.btnStartWorkout.setOnClickListener {
    // Validate device connection
    if (!viewModel.isDeviceConnected()) return
    
    // Check GPS permission if required
    if (viewModel.requireGps()) {
        if (!hasGpsPermission()) {
            showPermDetailsDialog()
            return
        }
        if (!isGpsTurnedOn()) return
    }
    
    showStartCountDown()  // Start 3-2-1 countdown
}
```

### 3.2 Countdown Animation

```kotlin
// RecordWorkoutV2ViewModel.kt
fun startStartCountDown() {
    viewModelScope.launch {
        countDownTimer.postValue("3")
        delay(1000)
        countDownTimer.postValue("2")
        delay(1000)
        countDownTimer.postValue("1")
        delay(1000)
        countDownTimer.postValue("start")  // Triggers sendStartWorkoutCommand()
    }
}
```

### 3.3 Send Start Command to Device

```kotlin
// RecordWorkoutFragmentV2.kt
private fun sendStartWorkoutCommand() {
    binding.progressBar.root.visible()
    viewModel.sportStartTime = viewModel.getCurrentTimeStamp()  // Unix timestamp in seconds
    
    val sportId = getRequestedSportId()
    if (sportId == null) {
        showWorkoutCommandError(resetToReadyState = true)
        return
    }
    
    // Send action to SessionManager
    viewModel.sessionManager.sendUpdateQueryAction(
        UpdateDeviceAction.StartWorkout(
            sportId,                    // Sport type ID (244-266 for Luna Band)
            viewModel.sportStartTime,   // Start timestamp
            viewModel.requireGps()      // Whether to track GPS
        )
    )
}
```

### 3.4 SDK Handler Processing

```kotlin
// ZhUpdateDeviceUnitsHandler.kt
override fun startWorkout(sportType: Int, sportStartTime: Long, startGps: Boolean) {
    pendingWorkoutAction = PendingWorkoutAction.START
    pendingWorkoutStartGps = startGps
    workoutStartConfirmed = false
    
    val bean = SendRingSportStatusBean(
        sportType,
        RingSportCallBack.RingSportStatus.SPORT_STATUS_START.status,
        sportStartTime
    )
    
    ControlBleTools.getInstance().sendRingSportStatus(bean, object : SendCmdStateListener() {
        override fun onState(state: SendCmdState?) {
            when (state) {
                SendCmdState.SUCCEED -> {
                    LOGS.d("startWorkout request sent")
                    checkOngoingWorkout()  // Verify workout started
                }
                else -> {
                    publishWorkoutActionFailure(PendingWorkoutAction.START)
                }
            }
        }
    })
}
```

### 3.5 Workout Start Confirmation

After successful start, the UI updates:

```kotlin
// RecordWorkoutFragmentV2.kt
is UpdateDeviceDataCallback.WorkoutStartState -> {
    if (it.success) {
        startWorkoutUi()
        viewModel.sendWorkoutEvent(true)
    } else {
        showReadyToStartState()
        showToast(getString(R.string.text_something_went_wrong_please_try_again))
    }
    binding.progressBar.root.gone()
}

private fun startWorkoutUi() {
    ongoingWorkoutState()
    viewModel.currentWorkoutState = 1
    viewModel.starTimer()
    viewModel.saveOngoingRecordWorkout()  // Persist for app restart recovery
}
```

---

## 4. During Workout - Live Data Collection

### 4.1 Real-Time Data Model

```kotlin
// ColorfitData.kt
data class WorkoutRealTimeData(
    val hrValue: Int? = null,      // Heart rate (BPM)
    val calorieValue: Int? = null, // Calories burned
    val steps: Int? = null,        // Step count
    val distance: Long? = null     // Distance in meters
)
```

### 4.2 SDK Callbacks for Live Data

```kotlin
// ZhUpdateDeviceUnitsHandler.kt
private val ringSportCallback = object : RingSportCallBack {
    // Called when workout status changes
    override fun onRingSportStatus(bean: RingSportStatusBean?) {
        // Handle status updates (start confirmed, pause, resume, stop)
    }
    
    // Called periodically with live workout metrics
    override fun onRingSportData(p0: RingSportDataBean?) {
        if (p0 == null) return
        
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.WorkoutRealTimeDataObtained(
                WorkoutRealTimeData(
                    hrValue = p0.heartRate,
                    calorieValue = p0.calories,
                    steps = p0.steps,
                    distance = p0.distance.toLong()
                )
            )
        )
    }
}
```

### 4.3 UI Updates

```kotlin
// RecordWorkoutFragmentV2.kt
is UpdateDeviceDataCallback.WorkoutRealTimeDataObtained -> {
    updateWorkoutData(it.data)
}

private fun updateWorkoutData(workoutRealTimeData: WorkoutRealTimeData) {
    binding.lytOnGoingWorkout.tvCalories.text =
        workoutRealTimeData.calorieValue?.toString() ?: "-"
    
    updateWorkoutHeartRate(workoutRealTimeData.hrValue)
}
```

### 4.4 Heart Rate Zone Display

The app displays heart rate zones (1-5) based on user's age:

```kotlin
// RecordWorkoutV2ViewModel.kt
private fun setupZoneId() {
    val age = getUserAge()
    val hrMax = (208 - 0.7 * age)  // Tanaka formula for max HR
    
    // Zone 1: 50-60% of max HR (Warm-up)
    // Zone 2: 60-70% (Fat burn)
    // Zone 3: 70-80% (Cardio)
    // Zone 4: 80-90% (Hard)
    // Zone 5: 90-100% (Maximum)
    
    hrZones.add(HrZoneData(1, zone1Max, 1))
    hrZones.add(HrZoneData(zone1Max, zone2Max, 2))
    hrZones.add(HrZoneData(zone2Max, zone3Max, 3))
    hrZones.add(HrZoneData(zone3Max, zone4Max, 4))
    hrZones.add(HrZoneData(zone4Max, zone5Max, 5))
}

fun getHeartRateZone(hr: Int?): Int? {
    if (hr == null || hr == 0 || hr == 255) return null
    return hrZones.find { hr >= it.min && hr < it.max }?.zone
}
```

### 4.5 Secondary Screen Callback (Luna Band Fallback)

For Luna Band devices, there's a secondary callback for live data:

```kotlin
private val secondaryScreenSportCallback = object : SecondaryScreenSportCallBack {
    override fun onSecondaryScreenWearData(bean: SecondaryScreenWearDataBean?) {
        if (bean == null) return
        
        testUpdateDeviceDataCallback?.onUpdateDataReceived(
            UpdateDeviceDataCallback.WorkoutRealTimeDataObtained(
                WorkoutRealTimeData(
                    hrValue = bean.heartRate,
                    calorieValue = bean.calories,
                    steps = bean.steps,
                    distance = bean.distance.toLong()
                )
            )
        )
    }
}
```

---

## 5. Pause, Resume & Stop Operations

### 5.1 Action Codes

| Action | Code | SDK Status |
|--------|------|------------|
| Pause | `2` | `SPORT_STATUS_PAUSE` |
| Resume | `3` | `SPORT_STATUS_RESUME` |
| Stop | `4` | `SPORT_STATUS_END` |

### 5.2 Pause Workout

```kotlin
// RecordWorkoutFragmentV2.kt
binding.btnPause.setOnClickListener {
    binding.progressBar.root.visible()
    
    viewModel.sessionManager.sendUpdateQueryAction(
        UpdateDeviceAction.UpdateOngoingWorkout(
            sportId,
            viewModel.sportStartTime,
            2  // Pause action
        )
    )
}

// SDK Handler
override fun updateOngoingWorkout(sportType: Int, sportTimeStamp: Long, action: Int) {
    pendingWorkoutAction = when (action) {
        2 -> PendingWorkoutAction.PAUSE
        3 -> PendingWorkoutAction.RESUME
        else -> PendingWorkoutAction.STOP
    }
    
    val status = when (action) {
        2 -> RingSportCallBack.RingSportStatus.SPORT_STATUS_PAUSE.status
        3 -> RingSportCallBack.RingSportStatus.SPORT_STATUS_RESUME.status
        else -> RingSportCallBack.RingSportStatus.SPORT_STATUS_END.status
    }
    
    val bean = SendRingSportStatusBean(sportType, status, sportTimeStamp)
    ControlBleTools.getInstance().sendRingSportStatus(bean, listener)
}
```

### 5.3 Pause UI State

```kotlin
private fun pauseWorkout() {
    viewModel.currentWorkoutState = 2
    binding.btnPause.gone()
    binding.btnEnd.gone()
    viewModel.pauseTimer()
    binding.blurView.visible()  // Shows blur overlay with resume/end buttons
}
```

### 5.4 Resume Workout

```kotlin
binding.btnResume.setOnClickListener {
    binding.progressBar.root.visible()
    
    viewModel.sessionManager.sendUpdateQueryAction(
        UpdateDeviceAction.UpdateOngoingWorkout(
            sportId,
            viewModel.sportStartTime,
            3  // Resume action
        )
    )
}

private fun resumeWorkout() {
    viewModel.currentWorkoutState = 3
    binding.btnPause.visible()
    binding.btnEnd.visible()
    viewModel.resumeTimer()
    binding.blurView.gone()
}
```

### 5.5 Stop Workout

Stop has special handling for short workouts (<60 seconds):

```kotlin
private fun onCrossClicked() {
    if (viewModel.currentWorkoutState == 0) {
        navigateUpSafe()
        return
    }
    
    // Short workout - ask if user wants to discard
    if (viewModel.workoutDuration < 60) {
        setFragmentResultListener(END_WORKOUT_KEY_SHORT) { _, bundle ->
            val end = bundle.getBoolean("end")
            if (end) {
                binding.progressBar.root.visible()
                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateOngoingWorkout(sportId, viewModel.sportStartTime, 4)
                )
                viewModel.markedDeleted = true
                viewModel.markForDelete(viewModel.sportStartTime)
            }
        }
        navigate(R.id.bottomSheetEndWorkoutShort)
        return
    }
    
    // Normal workout - ask save or delete
    setFragmentResultListener(END_WORKOUT_KEY) { _, bundle ->
        val allow = bundle.getBoolean("allow")   // Save workout
        val delete = bundle.getBoolean("delete") // Discard workout
        
        if (allow || delete) {
            viewModel.sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.UpdateOngoingWorkout(sportId, viewModel.sportStartTime, 4)
            )
            
            if (delete) {
                viewModel.markedDeleted = true
                viewModel.markForDelete(viewModel.sportStartTime)
            }
        }
    }
    navigate(R.id.bottomSheetEndWorkout)
}
```

### 5.6 Stop Confirmation & Sync

```kotlin
private fun stopWorkout() {
    viewModel.sendWorkoutEvent(false)
    viewModel.deleteOngoingRecordWorkout()
    viewModel.currentWorkoutState = 4
    viewModel.stopTimer()
    
    if (viewModel.markedDeleted) {
        requestWorkoutSummarySync()
        navigateUpSafe()
    } else {
        binding.progressBar.root.visible()
        viewModel.sessionManager.lastOngoingWorkoutTimestamp = viewModel.sportStartTime
        requestWorkoutSummarySync()  // Sync and navigate to workout details
    }
}

private fun requestWorkoutSummarySync() {
    val syncDate = DateFormats.convertTimestampToDate(
        viewModel.sportStartTime * 1000L,
        DateFormats.dateFormat3()
    )
    viewModel.sessionManager.sendUserActivityAction(
        UserActivityAction.SyncSportsActivity(syncDate)
    )
}
```

---

## 6. GPS & Location Tracking

### 6.1 GPS Permission Check

```kotlin
// RecordWorkoutFragmentV2.kt
private fun hasGpsPermission(): Boolean {
    val fineLocation = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    
    val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    } else true
    
    return fineLocation && backgroundLocation
}
```

### 6.2 GPS Enable Check

```kotlin
private fun isGpsTurnedOn(): Boolean {
    if (!ApplicationUtils.isLocationProviderEnabled(requireContext())) {
        // Show GPS enable dialog
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
        
        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())
            .addOnCompleteListener(this)
        
        return false
    }
    return true
}
```

### 6.3 Location Service

```kotlin
// LocationService.kt
@AndroidEntryPoint
class LocationService : Service() {
    
    private val LOCATION_UPDATE_INTERVAL = 10 * 1000L  // 10 seconds
    
    private fun start() {
        locationClient
            .getLocationUpdates(LOCATION_UPDATE_INTERVAL)
            .onEach { location ->
                val model = LocationModel(
                    lat = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    isRunning = true,
                    timeStamp = DateFormats.getTimeStamp()
                )
                
                locationDataSource.insertData(model)  // Store in Room DB
                locationBroadCast.postValue(Event(Pair(location.latitude, location.longitude)))
            }
            .launchIn(serviceScope)
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification.build())
    }
    
    companion object {
        val locationBroadCast = MutableLiveData<Event<Pair<Double, Double>>>()
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
```

### 6.4 Location Service Control

```kotlin
// ZhUpdateDeviceUnitsHandler.kt
private fun startLocationTracking() {
    LOGS.d("LOCATION_lOG Start Location tracking")
    LocationUtils.startLocationService()
}

private fun stopLocationTracking() {
    LOGS.d("LOCATION_lOG Stop Location tracking")
    LocationUtils.stopLocationService()
}
```

### 6.5 GPS-Required Sports

Outdoor activities require GPS:

| Sport | GPS Required |
|-------|--------------|
| Outdoor Running | Yes |
| Outdoor Cycling | Yes |
| Hiking | Yes |
| Walking (Outdoor) | Yes |
| Trail Running | Yes |
| Swimming (Open Water) | Yes |
| Indoor Running | No |
| Yoga | No |
| Gym activities | No |

---

## 7. Timer & Duration Tracking

### 7.1 Monotonic Time Tracking

Uses `SystemClock.elapsedRealtime()` for accurate timing regardless of system clock changes:

```kotlin
// RecordWorkoutV2ViewModel.kt
private var baseDurationSec: Long = 0L
private var resumeRealtimeMs: Long = 0L

private fun computedDurationSec(): Long {
    return if (resumeRealtimeMs > 0L) {
        val delta = (SystemClock.elapsedRealtime() - resumeRealtimeMs) / 1000
        baseDurationSec + delta
    } else {
        baseDurationSec
    }
}
```

### 7.2 Timer Operations

```kotlin
fun starTimer() {
    timer?.cancel()
    resumeRealtimeMs = SystemClock.elapsedRealtime()
    timer = Timer().apply {
        scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateTimer()
            }
        }, 0, 1000)  // Update every second
    }
}

fun pauseTimer() {
    baseDurationSec = computedDurationSec()
    resumeRealtimeMs = 0L
    timer?.cancel()
}

fun resumeTimer() {
    starTimer()
}

fun stopTimer() {
    timer?.cancel()
}
```

### 7.3 Time Display Formatting

```kotlin
fun updateTimer() {
    workoutDuration = computedDurationSec()
    val hours = workoutDuration / 3600
    val minutes = (workoutDuration % 3600) / 60
    val seconds = workoutDuration % 60
    
    val timeString = if (hours == 0L) {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }
    displayTimer.postValue(timeString)
}
```

### 7.4 Resuming from Ongoing Workout

When app restarts with an ongoing workout:

```kotlin
fun initFromOngoing(durationSec: Long, sportStatus: Int) {
    baseDurationSec = durationSec
    workoutDuration = baseDurationSec
    resumeRealtimeMs = 0L
    updateTimer()
}
```

---

## 8. Workout Data Sync

### 8.1 Post-Workout Sync Trigger

```kotlin
// RecordWorkoutFragmentV2.kt
private fun requestWorkoutSummarySync() {
    val syncDate = DateFormats.convertTimestampToDate(
        viewModel.sportStartTime * 1000L,
        DateFormats.dateFormat3()
    )
    viewModel.sessionManager.sendUserActivityAction(
        UserActivityAction.SyncSportsActivity(syncDate)
    )
}
```

### 8.2 SDK Sync Calls

```kotlin
// ZhUserActivityHandler.kt
fun startRecordedWorkoutSync() {
    // Fetch recorded workout IDs from device
    ControlBleTools.getInstance().getFitnessSportIdsData(null)
    
    // Fetch auto-detected sports
    ControlBleTools.getInstance().getAutoSportData(null)
}
```

### 8.3 Data Conversion

The SDK returns `DevSportInfoBean` which is converted to app's `RecordedWorkoutData`:

```kotlin
// DataConverter.kt
fun convertDevSportInfoToWorkoutData(bean: DevSportInfoBean): RecordedWorkoutData {
    return RecordedWorkoutData(
        calories = bean.reportCal,
        distance = bean.reportDistance,
        durationSeconds = bean.reportDuration,
        steps = bean.reportTotalStep,
        startTime = bean.reportSportStartTime,
        endTime = bean.reportSportEndTime,
        avgHeartRate = bean.reportAvgHeart,
        maxHeartRate = bean.reportMaxHeart,
        minHeartRate = bean.reportMinHeart,
        vo2Max = bean.reportVO2max,
        trainingEffect = bean.reportTrainingEffect,
        trainingLoad = bean.reportTrainingLoad,
        hrData = parseHrArray(bean.ringPointData),
        intensityList = parseIntensityList(bean.ringPointData)
    )
}
```

### 8.4 Sync Progress Callback

```kotlin
CallBackUtils.setSportParsingProgressCallBack { progress, total ->
    // Update sync progress UI
    LOGS.d("Sport sync progress: $progress / $total")
}
```

---

## 9. Sport Types & ID Mapping

### 9.1 Luna Band Sport IDs (244-266)

| Sport ID | Sport Type | Activity Names |
|----------|------------|----------------|
| 244 | Outdoor Cycling | `outdoor_cycling`, `cycling_ring` |
| 245 | Indoor Cycling | `indoor_cycling`, `indoor_cycling_ring` |
| 246 | Outdoor Running | `outdoor_running`, `running`, `running_ring` |
| 247 | Indoor Running | `indoor_running`, `indoor_running_ring` |
| 248 | Swimming | `swimming`, `pool_swimming`, `open_water` |
| 249 | Hiking | `hiking`, `hiking_ring`, `outdoor_hiking` |
| 250 | Elliptical | `elliptical`, `elliptical_machine` |
| 251 | Walking | `walking`, `outdoor_walking`, `indoor_walking` |
| 252 | Trail Running | `trail_running` |
| 255 | Rowing | `rowing`, `rowing_machine` |
| 256 | Golf | `golf`, `golf_ring` |
| 257 | Football | `football`, `soccer_ring` |
| 258 | Badminton | `badminton`, `badminton_ring` |
| 259 | Tennis | `tennis`, `tennis_ring` |
| 260 | Squash | `squash` |
| 261 | Table Tennis | `table_tennis`, `pingpong` |
| 262 | Cricket | `cricket`, `cricket_ring` |
| 263 | Boxing | `boxing` |
| 264 | Dumbbell | `dumbbell`, `dumbbell_training` |
| 265 | Basketball | `basketball`, `basketball_ring` |
| 266 | Yoga | `yoga`, `yoga_ring` |

### 9.2 Sport ID Resolution

```kotlin
// OWorkoutListModal.kt
fun getRequestedSportId(deviceType: String?): Int? {
    // For Luna Band, map activity names to sport IDs
    if (DeviceType.findDeviceType(deviceType ?: "") != DeviceType.LUNA_BAND) {
        return ringId  // Use ringId directly for other devices
    }
    
    // If ringId is already in Luna Band range, use it
    if (ringId != null && ringId in 244..266) {
        return ringId
    }
    
    // Otherwise, map activity type to sport ID
    return when (activityType?.lowercase()) {
        "outdoor_running", "running" -> 246
        "indoor_running" -> 247
        "yoga" -> 266
        // ... other mappings
        else -> null
    }
}
```

---

## 10. Error Handling & Edge Cases

### 10.1 Workout Start Failures

```kotlin
// ZhUpdateDeviceUnitsHandler.kt
when (bean.startResult) {
    RingSportStartResult.SPORT_START_RESULT_LOW_POWER.result -> {
        // Device battery too low
        publishWorkoutActionFailure(PendingWorkoutAction.START, revertWorkoutStartState = true)
        stopWorkout("Low Battery")
    }
    
    RingSportStartResult.SPORT_START_RESULT_UN_WEAR.result -> {
        // Device not being worn
        publishWorkoutActionFailure(PendingWorkoutAction.START, revertWorkoutStartState = true)
    }
    
    RingSportStartResult.SPORT_START_RESULT_CHARGING.result -> {
        // Device is charging
        publishWorkoutActionFailure(PendingWorkoutAction.START, revertWorkoutStartState = true)
    }
}
```

### 10.2 Workout End Reasons

```kotlin
when (bean.endReason) {
    SPORT_END_REASON_LOW_POWER.reason -> {
        stopWorkout("Low Battery")
    }
    
    SPORT_END_REASON_TIMEOUT.reason -> {
        // 8-hour workout timeout
    }
    
    SPORT_END_REASON_NO_MEMORY.reason -> {
        // Device storage full
    }
    
    SPORT_END_REASON_CHARGE.reason -> {
        stopWorkout("charging")
    }
}
```

### 10.3 Device-Stopped Workout Dialog

```kotlin
viewModel.showWorkoutStoppedByRingDialog.observe(viewLifecycleOwner) { event ->
    event.getContent()?.let { error ->
        setFragmentResultListener(WORKOUT_STOP_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")   // Save
            val delete = bundle.getBoolean("delete") // Discard
            
            if (allow || delete) {
                if (delete) {
                    viewModel.markForDelete(viewModel.sportStartTime)
                }
                stopWorkout()
            }
        }
        
        val showSave = viewModel.workoutDuration >= 60L
        navigate(R.id.workoutStopRingBottomSheet, bundleOf(
            "showSave" to showSave,
            "message" to viewModel.getStoppedByRingMessage(error, showSave)
        ))
    }
}
```

### 10.4 Ongoing Workout Recovery

When app reopens with ongoing workout:

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val ongoingWorkoutArgs = navArgs.onGoingWorkout
    
    if (ongoingWorkoutArgs != null) {
        viewModel.initFromOngoing(
            ongoingWorkoutArgs.duration.toLong(),
            ongoingWorkoutArgs.sportStatus
        )
        
        when (ongoingWorkoutArgs.sportStatus) {
            1, 3 -> startWorkoutUi()  // Running or resumed
            2 -> {
                ongoingWorkoutState()
                pauseWorkout()
                viewModel.updateTimer()
            }
        }
    }
}
```

---

## 11. Key Classes Reference

### 11.1 Actions (Commands to Device)

```kotlin
// UpdateDeviceAction.kt
sealed class UpdateDeviceAction {
    class CheckOngoingWorkout : UpdateDeviceAction()
    
    class StartWorkout(
        val sportType: Int,
        val sportStartTime: Long,
        val startGps: Boolean
    ) : UpdateDeviceAction()
    
    class UpdateOngoingWorkout(
        val sportType: Int,
        val sportTimeStamp: Long,
        val action: Int  // 2=Pause, 3=Resume, 4=Stop
    ) : UpdateDeviceAction()
}
```

### 11.2 Callbacks (Responses from Device)

```kotlin
// UpdateDeviceDataCallback.kt
sealed class UpdateDeviceDataCallback {
    class WorkoutStartState(
        val success: Boolean,
        val failReason: WorkoutFailReason? = null
    ) : UpdateDeviceDataCallback()
    
    class WorkoutPaused(val success: Boolean) : UpdateDeviceDataCallback()
    class WorkoutResumed(val success: Boolean) : UpdateDeviceDataCallback()
    class WorkoutStopped(val success: Boolean) : UpdateDeviceDataCallback()
    class WorkoutStoppedByRing(val error: String) : UpdateDeviceDataCallback()
    
    class WorkoutRealTimeDataObtained(
        val data: WorkoutRealTimeData
    ) : UpdateDeviceDataCallback()
    
    class OngoingWorkoutData(
        val duration: Int,
        val sportStatus: Int,
        val sportType: Int,
        val startTimeStamp: Long
    ) : UpdateDeviceDataCallback()
}
```

### 11.3 Workout State Machine

```kotlin
/**
 * currentWorkoutState values:
 * 0 -> Default (ready to start)
 * 1 -> Started (workout running)
 * 2 -> Paused
 * 3 -> Resumed (after pause)
 * 4 -> Stopped
 */
var currentWorkoutState = 0
```

---

## 12. Sequence Diagrams

### 12.1 Start Workout Flow

```
┌──────┐      ┌──────────────────────┐      ┌─────────────┐      ┌─────────────────────────┐      ┌──────────┐
│ User │      │ RecordWorkoutFragment│      │ ViewModel   │      │ ZhUpdateDeviceUnitsHandler│      │ Luna Band│
└──┬───┘      └──────────┬───────────┘      └──────┬──────┘      └────────────┬────────────┘      └────┬─────┘
   │                     │                         │                          │                       │
   │  Tap "Start"        │                         │                          │                       │
   │────────────────────>│                         │                          │                       │
   │                     │                         │                          │                       │
   │                     │  showStartCountDown()   │                          │                       │
   │                     │────────────────────────>│                          │                       │
   │                     │                         │                          │                       │
   │                     │<────────────────────────│                          │                       │
   │                     │  3...2...1..."start"    │                          │                       │
   │                     │                         │                          │                       │
   │                     │  sendStartWorkoutCommand()                         │                       │
   │                     │────────────────────────>│                          │                       │
   │                     │                         │                          │                       │
   │                     │                         │  StartWorkout(sportId,   │                       │
   │                     │                         │    timestamp, gps)       │                       │
   │                     │                         │─────────────────────────>│                       │
   │                     │                         │                          │                       │
   │                     │                         │                          │  sendRingSportStatus()│
   │                     │                         │                          │──────────────────────>│
   │                     │                         │                          │                       │
   │                     │                         │                          │<──────────────────────│
   │                     │                         │                          │  SUCCEED              │
   │                     │                         │                          │                       │
   │                     │                         │                          │  checkOngoingWorkout()│
   │                     │                         │                          │──────────────────────>│
   │                     │                         │                          │                       │
   │                     │                         │                          │<──────────────────────│
   │                     │                         │                          │  Status: RUNNING      │
   │                     │                         │                          │                       │
   │                     │                         │<─────────────────────────│                       │
   │                     │                         │  WorkoutStartState(true) │                       │
   │                     │                         │                          │                       │
   │                     │<────────────────────────│                          │                       │
   │                     │  startWorkoutUi()       │                          │                       │
   │                     │                         │                          │                       │
   │<────────────────────│                         │                          │                       │
   │  Show workout UI    │                         │                          │                       │
   │                     │                         │                          │                       │
```

### 12.2 Live Data Flow

```
┌──────────┐                              ┌─────────────────────────┐      ┌──────────────────────┐
│ Luna Band│                              │ ZhUpdateDeviceUnitsHandler│      │ RecordWorkoutFragment│
└────┬─────┘                              └────────────┬────────────┘      └──────────┬───────────┘
     │                                                 │                              │
     │  RingSportDataBean(hr, cal, steps, dist)       │                              │
     │───────────────────────────────────────────────>│                              │
     │                                                 │                              │
     │                                                 │  WorkoutRealTimeDataObtained │
     │                                                 │─────────────────────────────>│
     │                                                 │                              │
     │                                                 │                              │  updateWorkoutData()
     │                                                 │                              │  Update UI
     │                                                 │                              │
     │  (10 sec later...)                             │                              │
     │  RingSportDataBean(hr, cal, steps, dist)       │                              │
     │───────────────────────────────────────────────>│                              │
     │                                                 │                              │
```

### 12.3 Stop & Sync Flow

```
┌──────┐      ┌──────────────────────┐      ┌─────────────────────────┐      ┌──────────┐      ┌────────┐
│ User │      │ RecordWorkoutFragment│      │ ZhUpdateDeviceUnitsHandler│      │ Luna Band│      │ Server │
└──┬───┘      └──────────┬───────────┘      └────────────┬────────────┘      └────┬─────┘      └───┬────┘
   │                     │                               │                        │                │
   │  Tap "Stop"         │                               │                        │                │
   │────────────────────>│                               │                        │                │
   │                     │                               │                        │                │
   │                     │  UpdateOngoingWorkout(        │                        │                │
   │                     │    sportId, ts, action=4)     │                        │                │
   │                     │──────────────────────────────>│                        │                │
   │                     │                               │                        │                │
   │                     │                               │  sendRingSportStatus(END)                │
   │                     │                               │───────────────────────>│                │
   │                     │                               │                        │                │
   │                     │                               │<───────────────────────│                │
   │                     │                               │  Status: ENDED         │                │
   │                     │                               │                        │                │
   │                     │<──────────────────────────────│                        │                │
   │                     │  WorkoutStopped(true)         │                        │                │
   │                     │                               │                        │                │
   │                     │  SyncSportsActivity(date)     │                        │                │
   │                     │──────────────────────────────>│                        │                │
   │                     │                               │                        │                │
   │                     │                               │  getFitnessSportIds()  │                │
   │                     │                               │───────────────────────>│                │
   │                     │                               │                        │                │
   │                     │                               │<───────────────────────│                │
   │                     │                               │  DevSportInfoBean      │                │
   │                     │                               │                        │                │
   │                     │                               │  Upload to server      │                │
   │                     │                               │───────────────────────────────────────>│
   │                     │                               │                        │                │
   │                     │<──────────────────────────────│                        │                │
   │                     │  Navigate to PostWorkout      │                        │                │
   │                     │                               │                        │                │
```

---

## Summary

The NoiseFit Luna workout system implements a complete workout tracking solution with:

1. **Pre-flight validation** - Device connection, battery, charging status
2. **Real-time data** - Heart rate, calories, steps, distance updates every few seconds
3. **GPS tracking** - Foreground service for outdoor activities
4. **State management** - Pause, resume, stop with proper state machine
5. **Error handling** - Device-initiated stops, low battery, charging detection
6. **Data sync** - Automatic upload after workout completion
7. **Recovery** - Resume ongoing workouts after app restart

The architecture uses a clean separation between UI (Fragments), business logic (ViewModels), communication orchestration (SessionManager), and SDK integration (Handlers), making it maintainable and testable.
