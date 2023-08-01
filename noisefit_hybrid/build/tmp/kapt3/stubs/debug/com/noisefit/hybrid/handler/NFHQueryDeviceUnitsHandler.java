package com.noisefit.hybrid.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u001f\n\u0002\u0010\u000b\n\u0002\b\u0006\u0018\u0000 L2\u00020\u0001:\u0001LB?\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\u0002\u0010\u0010J\b\u0010\u001b\u001a\u00020\u001cH\u0016J\u001b\u0010\u001d\u001a\u00020\u001c\"\u0004\b\u0000\u0010\u001e2\u0006\u0010\u001f\u001a\u0002H\u001eH\u0016\u00a2\u0006\u0002\u0010 J\u001b\u0010!\u001a\u00020\u001c\"\u0004\b\u0000\u0010\u001e2\u0006\u0010\u001f\u001a\u0002H\u001eH\u0016\u00a2\u0006\u0002\u0010 J\b\u0010\"\u001a\u00020\u001cH\u0016J\b\u0010#\u001a\u00020\u001cH\u0016J\b\u0010$\u001a\u00020\u001cH\u0016J\b\u0010%\u001a\u00020\u001cH\u0016J\u0012\u0010&\u001a\u00020\'2\b\u0010(\u001a\u0004\u0018\u00010\u0018H\u0002J\b\u0010)\u001a\u00020\u001cH\u0016J\b\u0010*\u001a\u00020\u001cH\u0016J\b\u0010+\u001a\u00020\u001cH\u0016J\b\u0010,\u001a\u00020\u001cH\u0016J\b\u0010-\u001a\u00020\u001cH\u0016J\b\u0010.\u001a\u00020\u001cH\u0016J\b\u0010/\u001a\u00020\u001cH\u0016J\b\u00100\u001a\u00020\u001cH\u0016J\b\u00101\u001a\u00020\u001cH\u0016J\b\u00102\u001a\u00020\u001cH\u0016J\b\u00103\u001a\u00020\u001cH\u0016J\b\u00104\u001a\u00020\u001cH\u0016J\b\u00105\u001a\u00020\u001cH\u0016J\b\u00106\u001a\u00020\u001cH\u0016J\u0010\u00107\u001a\u00020\u00182\u0006\u00108\u001a\u00020\u0018H\u0002J\b\u00109\u001a\u00020\u001cH\u0016J\b\u0010:\u001a\u00020\u001cH\u0016J\u0010\u0010;\u001a\u00020\u001c2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016J\b\u0010<\u001a\u00020\u001cH\u0016J\b\u0010=\u001a\u00020\u001cH\u0002J\u0010\u0010>\u001a\u00020\u001c2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016J\b\u0010?\u001a\u00020\u001cH\u0002J)\u0010@\u001a\u00020\u001c2\u0006\u0010A\u001a\u00020\u00142\b\u0010B\u001a\u0004\u0018\u00010\u00182\b\u0010C\u001a\u0004\u0018\u00010\u0014H\u0016\u00a2\u0006\u0002\u0010DJ\u0010\u0010E\u001a\u00020\u001c2\u0006\u0010F\u001a\u00020GH\u0002J\u0010\u0010H\u001a\u00020\u001c2\u0006\u0010I\u001a\u00020GH\u0002J\u0010\u0010J\u001a\u00020\u001c2\u0006\u0010K\u001a\u00020\u0014H\u0002R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006M"}, d2 = {"Lcom/noisefit/hybrid/handler/NFHQueryDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "bitwiseUtils", "Lcom/noisefit/hybrid/utils/BitwiseUtils;", "dataConverter", "Lcom/noisefit/hybrid/dataconversions/DataConverter;", "bitwiseHelperUtils", "Lcom/noisefit/hybrid/utils/BitwiseHelperUtils;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "visionCommands", "Lcom/noisefit/hybrid/base/VisionCommands;", "context", "Landroid/content/Context;", "bluetoothsdkExp", "Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp;", "(Lcom/noisefit/hybrid/utils/BitwiseUtils;Lcom/noisefit/hybrid/dataconversions/DataConverter;Lcom/noisefit/hybrid/utils/BitwiseHelperUtils;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;Lcom/noisefit/hybrid/base/VisionCommands;Landroid/content/Context;Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp;)V", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "previousVolume", "", "resultCallBack", "Lcn/appscomm/bluetoothsdk/interfaces/ResultCallBack;", "songName", "", "testQueryDeviceDataCallback", "Lcom/noisefit_commans/interfaces/IQueryDataCallback;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "getAlarms", "getAutoSleep", "getCustomReplies", "getDeviceUnits", "getDisplayFirmware", "Lcom/noisefit_commans/models/DeviceFirmware;", "firmwareVersion", "getDoNotDisturbData", "getDrinkWaterSettings", "getHandwashData", "getHeartRateAlert", "getHeartRateInterval", "getLanguage", "getReminders", "getScreenAwakeInterval", "getSedentaryData", "getStressSettings", "getUserInfo", "getWatchPassword", "getWorldClock", "init", "parseFirmware", "firmware", "queryBatteryPower", "queryFirmwareUpgrade", "queryFirmwareUpgradeNew", "queryFirmwareVersion", "set8002CallbackNull", "setDevice", "setMusicPlayerState", "setMusicStatus", "status", "title", "sec", "(ILjava/lang/String;Ljava/lang/Integer;)V", "setPlayPauseMusic", "isPlay", "", "updateVolume", "isIncreaseVolume", "updateVolumeNew", "volume", "Companion", "noisefit_hybrid_debug"})
public final class NFHQueryDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions {
    private final com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils = null;
    private final com.noisefit.hybrid.dataconversions.DataConverter dataConverter = null;
    private final com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils = null;
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private final com.noisefit.hybrid.base.VisionCommands visionCommands = null;
    private final android.content.Context context = null;
    private final com.noisefit.hybrid.utils.BluetoothSDK_Exp bluetoothsdkExp = null;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit.hybrid.handler.NFHQueryDeviceUnitsHandler.Companion Companion = null;
    @org.jetbrains.annotations.Nullable
    private static com.noisefit_commans.models.AlarmsList alarmsList;
    @org.jetbrains.annotations.Nullable
    private static com.noisefit_commans.models.ReminderList remindersList;
    private int previousVolume = 0;
    private java.lang.String songName;
    private com.noisefit_commans.interfaces.IQueryDataCallback testQueryDeviceDataCallback;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private cn.appscomm.bluetoothsdk.interfaces.ResultCallBack resultCallBack;
    
    @javax.inject.Inject
    public NFHQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.dataconversions.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.VisionCommands visionCommands, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BluetoothSDK_Exp bluetoothsdkExp) {
        super();
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void queryFirmwareVersion() {
    }
    
    @java.lang.Override
    public void getDoNotDisturbData() {
    }
    
    @java.lang.Override
    public void queryBatteryPower() {
    }
    
    @java.lang.Override
    public void getUserInfo() {
    }
    
    @java.lang.Override
    public void getLanguage() {
    }
    
    @java.lang.Override
    public void queryFirmwareUpgrade() {
    }
    
    @java.lang.Override
    public void queryFirmwareUpgradeNew(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public void getCustomReplies() {
    }
    
    @java.lang.Override
    public void setMusicStatus(int status, @org.jetbrains.annotations.Nullable
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.Integer sec) {
    }
    
    @java.lang.Override
    public void getReminders() {
    }
    
    @java.lang.Override
    public void getAlarms() {
    }
    
    @java.lang.Override
    public void getAutoSleep() {
    }
    
    @java.lang.Override
    public void getHeartRateInterval() {
    }
    
    @java.lang.Override
    public void getHeartRateAlert() {
    }
    
    @java.lang.Override
    public void getSedentaryData() {
    }
    
    @java.lang.Override
    public void getDeviceUnits() {
    }
    
    @java.lang.Override
    public void getScreenAwakeInterval() {
    }
    
    private final void set8002CallbackNull() {
    }
    
    @java.lang.Override
    public void getHandwashData() {
    }
    
    @java.lang.Override
    public void getDrinkWaterSettings() {
    }
    
    @java.lang.Override
    public void getWorldClock() {
    }
    
    @java.lang.Override
    public void getWatchPassword() {
    }
    
    @java.lang.Override
    public void getStressSettings() {
    }
    
    private final void setPlayPauseMusic(boolean isPlay) {
    }
    
    private final void setMusicPlayerState() {
    }
    
    private final void updateVolumeNew(int volume) {
    }
    
    private final void updateVolume(boolean isIncreaseVolume) {
    }
    
    private final com.noisefit_commans.models.DeviceFirmware getDisplayFirmware(java.lang.String firmwareVersion) {
        return null;
    }
    
    private final java.lang.String parseFirmware(java.lang.String firmware) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001c\u0010\t\u001a\u0004\u0018\u00010\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/noisefit/hybrid/handler/NFHQueryDeviceUnitsHandler$Companion;", "", "()V", "alarmsList", "Lcom/noisefit_commans/models/AlarmsList;", "getAlarmsList", "()Lcom/noisefit_commans/models/AlarmsList;", "setAlarmsList", "(Lcom/noisefit_commans/models/AlarmsList;)V", "remindersList", "Lcom/noisefit_commans/models/ReminderList;", "getRemindersList", "()Lcom/noisefit_commans/models/ReminderList;", "setRemindersList", "(Lcom/noisefit_commans/models/ReminderList;)V", "noisefit_hybrid_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.AlarmsList getAlarmsList() {
            return null;
        }
        
        public final void setAlarmsList(@org.jetbrains.annotations.Nullable
        com.noisefit_commans.models.AlarmsList p0) {
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.noisefit_commans.models.ReminderList getRemindersList() {
            return null;
        }
        
        public final void setRemindersList(@org.jetbrains.annotations.Nullable
        com.noisefit_commans.models.ReminderList p0) {
        }
    }
}