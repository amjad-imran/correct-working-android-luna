package com.noisefit_ryeex_sdk.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00fe\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u001b\u0010\u0011\u001a\u00020\u0012\"\u0004\b\u0000\u0010\u00132\u0006\u0010\u0014\u001a\u0002H\u0013H\u0016\u00a2\u0006\u0002\u0010\u0015J\u001b\u0010\u0016\u001a\u00020\u0012\"\u0004\b\u0000\u0010\u00132\u0006\u0010\u0014\u001a\u0002H\u0013H\u0016\u00a2\u0006\u0002\u0010\u0015J\u0010\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u0018H\u0016J\u0012\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u001b\u001a\u00020\u001aH\u0002J\b\u0010\u001c\u001a\u00020\u0012H\u0016J\u0010\u0010\u001d\u001a\u00020\u00122\u0006\u0010\u001e\u001a\u00020\u001fH\u0016J\u0010\u0010 \u001a\u00020\u00122\u0006\u0010!\u001a\u00020\"H\u0016J\u0016\u0010#\u001a\u00020\u00122\f\u0010$\u001a\b\u0012\u0004\u0012\u00020&0%H\u0016J\u0010\u0010\'\u001a\u00020\u00122\u0006\u0010(\u001a\u00020\fH\u0016J\u0018\u0010)\u001a\u00020\u00122\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020-H\u0016J\u0010\u0010.\u001a\u00020\u00122\u0006\u0010,\u001a\u00020/H\u0016J\u0010\u00100\u001a\u00020\u00122\u0006\u00101\u001a\u000202H\u0016J\u0010\u00103\u001a\u00020\u00122\u0006\u00104\u001a\u000205H\u0016J\u0010\u00106\u001a\u00020\u00122\u0006\u00107\u001a\u000208H\u0016J\u0010\u00109\u001a\u00020\u00122\u0006\u0010:\u001a\u00020;H\u0016J\u0010\u0010<\u001a\u00020\u00122\u0006\u0010=\u001a\u00020>H\u0016J\u0010\u0010?\u001a\u00020\u00122\u0006\u0010@\u001a\u00020\u0018H\u0016J\u0010\u0010A\u001a\u00020\u00122\u0006\u00104\u001a\u000205H\u0016J\u0010\u0010B\u001a\u00020\u00122\u0006\u0010C\u001a\u00020DH\u0016J\"\u0010E\u001a\u00020\u00122\u0006\u0010F\u001a\u00020G2\u0006\u0010H\u001a\u00020I2\b\u0010J\u001a\u0004\u0018\u00010\u001aH\u0016J\u0010\u0010K\u001a\u00020\u00122\u0006\u00101\u001a\u00020LH\u0016J,\u0010M\u001a\u00020\u00122\f\u0010N\u001a\b\u0012\u0004\u0012\u00020O0%2\f\u0010P\u001a\b\u0012\u0004\u0012\u00020Q0%2\u0006\u0010R\u001a\u00020\u001aH\u0016J\u0010\u0010S\u001a\u00020\u00122\u0006\u0010T\u001a\u00020UH\u0016J\u0010\u0010V\u001a\u00020\u00122\u0006\u0010!\u001a\u00020\"H\u0016J\u0018\u0010W\u001a\u00020\u00122\u0006\u0010X\u001a\u00020Y2\u0006\u0010Z\u001a\u00020[H\u0016J\u0016\u0010\\\u001a\u00020\u00122\f\u0010]\u001a\b\u0012\u0004\u0012\u00020^0%H\u0016J\u0010\u0010_\u001a\u00020\u00122\u0006\u0010`\u001a\u00020aH\u0016J\u0010\u0010b\u001a\u00020\u00122\u0006\u0010c\u001a\u00020\u001aH\u0016J\u0010\u0010d\u001a\u00020\u00122\u0006\u0010e\u001a\u00020fH\u0016J\u0016\u0010g\u001a\u00020\u00122\f\u0010]\u001a\b\u0012\u0004\u0012\u00020^0%H\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006h"}, d2 = {"Lcom/noisefit_ryeex_sdk/handler/RyeexUpdateDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "dataConverter", "Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "ryeexApplicationHandler", "Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;Landroid/content/Context;Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "updateDeviceDataCallback", "Lcom/noisefit_commans/interfaces/device_data/IUpdateDeviceDataCallback;", "watchDevice", "Lcom/ryeex/watch/adapter/device/WatchDevice;", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "findDevice", "Lcom/noisefit_commans/models/SwitchSetting;", "getZipFileName", "", "zipFile", "init", "sendAppNotification", "appNotification", "Lcom/noisefit_commans/models/AppNotification;", "setBleCallingSwitch", "status", "", "setContactList", "contactList", "", "Lcom/noisefit_commans/models/Contact;", "setDevice", "device", "setDeviceDateTime", "calender", "Ljava/util/Calendar;", "units", "Lcom/noisefit_commans/models/TimeFormat;", "setDeviceUnits", "Lcom/noisefit_commans/models/DeviceUnits;", "setDiyWatchFaceCustom", "watchFace", "Lcom/noisefit_commans/models/DiyCustomWatchFace;", "setDrinkWaterReminder", "sedentaryData", "Lcom/noisefit_commans/models/SedentaryData;", "setHeartRateAlert", "heartRateAlert", "Lcom/noisefit_commans/models/HeartRateAlert;", "setHeartRateInterval", "heartRateInterval", "Lcom/noisefit_commans/models/HeartRateInterval;", "setIncomingCallInfo", "incomingCall", "Lcom/noisefit_commans/models/IncomingCall;", "setMusicSwitch", "musicSwitch", "setSedentaryData", "setSportModeInfo", "sportsModeList", "Lcom/noisefit_commans/models/SportsModeList;", "setUserInfo", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "userGoals", "Lcom/noisefit_commans/models/UserGoals;", "userName", "setWatchFace", "Lcom/noisefit_commans/models/WatchFace;", "setWeatherDataHourly", "weatherDataList", "Lcom/noisefit_commans/models/WeatherData;", "hourlyWeatherList", "Lcom/noisefit_commans/models/WeatherDataHourly;", "unit", "setWristLiftGesture", "wristLiftGesture", "Lcom/noisefit_commans/models/WristLiftGesture;", "startCameraMode", "updateAlarm", "alarm", "Lcom/noisefit_commans/models/AlarmsList;", "alarmAction", "Lcom/noisefit_commans/models/AlarmAction;", "updateApplicationList", "data", "Lcom/noisefit_commans/models/Widget;", "updateDND", "doNotDisturb", "Lcom/noisefit_commans/models/DoNotDisturb;", "updateFirmware", "fileUri", "updateLanguage", "language", "Lcom/noisefit_commans/models/Language;", "updateWidgetList", "noisefit_ryeex_sdk_debug"})
public final class RyeexUpdateDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions {
    private com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter;
    private android.content.Context context;
    private com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler;
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback updateDeviceDataCallback;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private com.ryeex.watch.adapter.device.WatchDevice watchDevice;
    
    @javax.inject.Inject
    public RyeexUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void updateFirmware(@org.jetbrains.annotations.NotNull
    java.lang.String fileUri) {
    }
    
    @java.lang.Override
    public void setContactList(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.Contact> contactList) {
    }
    
    @java.lang.Override
    public void startCameraMode(boolean status) {
    }
    
    private final java.lang.String getZipFileName(java.lang.String zipFile) {
        return null;
    }
    
    @java.lang.Override
    public void setWatchFace(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    @java.lang.Override
    public void setDiyWatchFaceCustom(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DiyCustomWatchFace watchFace) {
    }
    
    @java.lang.Override
    public void updateAlarm(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AlarmsList alarm, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AlarmAction alarmAction) {
    }
    
    @java.lang.Override
    public void updateLanguage(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.Language language) {
    }
    
    @java.lang.Override
    public void findDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting findDevice) {
    }
    
    @java.lang.Override
    public void updateDND(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DoNotDisturb doNotDisturb) {
    }
    
    @java.lang.Override
    public void setHeartRateInterval(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.HeartRateInterval heartRateInterval) {
    }
    
    @java.lang.Override
    public void setHeartRateAlert(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.HeartRateAlert heartRateAlert) {
    }
    
    @java.lang.Override
    public void setSedentaryData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void setUserInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UserInfo userInfo, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UserGoals userGoals, @org.jetbrains.annotations.Nullable
    java.lang.String userName) {
    }
    
    @java.lang.Override
    public void setDeviceUnits(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DeviceUnits units) {
    }
    
    @java.lang.Override
    public void setDeviceDateTime(@org.jetbrains.annotations.NotNull
    java.util.Calendar calender, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.TimeFormat units) {
    }
    
    @java.lang.Override
    public void setDrinkWaterReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void sendAppNotification(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AppNotification appNotification) {
    }
    
    @java.lang.Override
    public void setIncomingCallInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.IncomingCall incomingCall) {
    }
    
    @java.lang.Override
    public void setWristLiftGesture(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WristLiftGesture wristLiftGesture) {
    }
    
    @java.lang.Override
    public void setWeatherDataHourly(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.WeatherData> weatherDataList, @org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.WeatherDataHourly> hourlyWeatherList, @org.jetbrains.annotations.NotNull
    java.lang.String unit) {
    }
    
    @java.lang.Override
    public void setSportModeInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SportsModeList sportsModeList) {
    }
    
    @java.lang.Override
    public void setMusicSwitch(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting musicSwitch) {
    }
    
    @java.lang.Override
    public void updateApplicationList(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.Widget> data) {
    }
    
    @java.lang.Override
    public void updateWidgetList(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.Widget> data) {
    }
    
    @java.lang.Override
    public void setBleCallingSwitch(boolean status) {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice device) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
}