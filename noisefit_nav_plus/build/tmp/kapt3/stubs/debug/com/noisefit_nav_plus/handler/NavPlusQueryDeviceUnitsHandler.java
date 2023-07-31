package com.noisefit_nav_plus.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b;\u0018\u0000 q2\u00020\u0001:\u0001qB/\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\b\u00106\u001a\u000207H\u0016J\u001b\u00108\u001a\u000207\"\u0004\b\u0000\u001092\u0006\u0010:\u001a\u0002H9H\u0016\u00a2\u0006\u0002\u0010;J\u001b\u0010<\u001a\u000207\"\u0004\b\u0000\u001092\u0006\u0010:\u001a\u0002H9H\u0016\u00a2\u0006\u0002\u0010;J\b\u0010=\u001a\u000207H\u0002J\b\u0010>\u001a\u000207H\u0002J\b\u0010?\u001a\u000207H\u0016J\b\u0010@\u001a\u000207H\u0016J\b\u0010A\u001a\u000207H\u0016J\b\u0010B\u001a\u000207H\u0016J\b\u0010C\u001a\u000207H\u0016J\b\u0010D\u001a\u000207H\u0016J\b\u0010E\u001a\u000207H\u0016J\b\u0010F\u001a\u000207H\u0016J\b\u0010G\u001a\u000207H\u0016J\b\u0010H\u001a\u000207H\u0016J\b\u0010I\u001a\u000207H\u0016J\b\u0010J\u001a\u000207H\u0016J\b\u0010K\u001a\u000207H\u0016J\b\u0010L\u001a\u000207H\u0016J\b\u0010M\u001a\u000207H\u0016J\b\u0010N\u001a\u00020&H\u0002J\b\u0010O\u001a\u000207H\u0016J\b\u0010P\u001a\u000207H\u0016J\b\u0010Q\u001a\u000207H\u0016J\b\u0010R\u001a\u000207H\u0016J\b\u0010S\u001a\u000207H\u0016J\b\u0010T\u001a\u000207H\u0016J\b\u0010U\u001a\u000207H\u0016J\b\u0010V\u001a\u000207H\u0016J\b\u0010W\u001a\u00020\u0018H\u0002J\b\u0010X\u001a\u000207H\u0016J\b\u0010Y\u001a\u000207H\u0002J\b\u0010Z\u001a\u000207H\u0002J\b\u0010[\u001a\u000207H\u0016J\b\u0010\\\u001a\u000207H\u0016J\u0010\u0010]\u001a\u0002072\u0006\u0010^\u001a\u00020,H\u0016J\b\u0010_\u001a\u000207H\u0016J\b\u0010`\u001a\u000207H\u0016J\b\u0010a\u001a\u00020\u0012H\u0002J\u0018\u0010b\u001a\u0002072\u0006\u0010c\u001a\u00020/2\u0006\u0010d\u001a\u00020/H\u0002J\u0010\u0010e\u001a\u0002072\u0006\u0010f\u001a\u00020,H\u0016J)\u0010g\u001a\u0002072\u0006\u0010h\u001a\u00020\u00122\b\u0010d\u001a\u0004\u0018\u00010/2\b\u0010i\u001a\u0004\u0018\u00010\u0012H\u0016\u00a2\u0006\u0002\u0010jJ\b\u0010k\u001a\u000207H\u0002J\u0018\u0010l\u001a\u0002072\u0006\u0010m\u001a\u00020\u00122\u0006\u0010n\u001a\u00020\u0012H\u0016J\u0010\u0010o\u001a\u0002072\u0006\u0010p\u001a\u00020\u0018H\u0002R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010!\u001a\u0004\u0018\u00010\"X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020&X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\'\u0010(\"\u0004\b)\u0010*R\u0010\u0010+\u001a\u0004\u0018\u00010,X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010.\u001a\u0004\u0018\u00010/X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u00100\u001a\u0004\u0018\u000101X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b2\u00103\"\u0004\b4\u00105\u00a8\u0006r"}, d2 = {"Lcom/noisefit_nav_plus/handler/NavPlusQueryDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "navPlusApplicationHandler", "Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "dataConverter", "Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "gson", "Lcom/google/gson/Gson;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;Landroid/content/Context;Lcom/google/gson/Gson;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "currentGpsSportState", "", "getDataConverter", "()Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;", "setDataConverter", "(Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;)V", "firstLocation", "", "getGson", "()Lcom/google/gson/Gson;", "setGson", "(Lcom/google/gson/Gson;)V", "locationClientClass", "Lcom/noisefit_commans/utils/LocationClientClass;", "locationReceiver", "Landroid/content/BroadcastReceiver;", "mBleService", "Lcom/zjw/zhbraceletsdk/service/ZhBraceletService;", "mPerformerListener", "Lcom/zjw/zhbraceletsdk/linstener/SimplePerformerListener;", "musicInfo", "Lcom/zjw/zhbraceletsdk/bean/MusicInfo;", "getNavPlusApplicationHandler", "()Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "setNavPlusApplicationHandler", "(Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;)V", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "previousVolume", "songName", "", "testQueryDeviceDataCallback", "Lcom/noisefit_commans/interfaces/IQueryDataCallback;", "getWatchDataStore", "()Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "setWatchDataStore", "(Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "disableLocation", "enableLocation", "getAlarms", "getAutoSleep", "getBluetoothCallStatus", "getBodyTempUnit", "getContactList", "getCustomReplies", "getDeviceUnits", "getDrinkWaterSettings", "getHandwashData", "getHeartRateAlert", "getHeartRateInterval", "getLanguage", "getMealReminderSettings", "getMedicineReminderSettings", "getMedicineReminders", "getMusicInfo", "getQuickEyeMovementSwitch", "getReminders", "getScreenAwakeInterval", "getSedentaryData", "getStockList", "getUserInfo", "getWatchFaces", "getWorldClock", "hasPermission", "init", "initLogListener", "initQuickEyeListener", "queryBatteryPower", "queryFirmwareUpgrade", "queryFirmwareUpgradeNew", "colorFitDevice", "queryFirmwareVersion", "removeCallbacks", "returnVolume", "sendErrorMessageToApp", "message", "title", "setDevice", "device", "setMusicStatus", "status", "sec", "(ILjava/lang/String;Ljava/lang/Integer;)V", "setStockListener", "setVolume", "currentVolume", "maxVolume", "updateVolume", "isIncreaseVolume", "Companion", "noisefit_nav_plus_debug"})
public final class NavPlusQueryDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions {
    @org.jetbrains.annotations.NotNull
    private com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler;
    @org.jetbrains.annotations.NotNull
    private com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter;
    @org.jetbrains.annotations.NotNull
    private android.content.Context context;
    @org.jetbrains.annotations.NotNull
    private com.google.gson.Gson gson;
    @org.jetbrains.annotations.NotNull
    private com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_nav_plus.handler.NavPlusQueryDeviceUnitsHandler.Companion Companion = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String LAT_LONG = "LAT_LONG";
    private java.lang.String songName;
    private int previousVolume = 0;
    private com.noisefit_commans.utils.LocationClientClass locationClientClass;
    private boolean firstLocation = true;
    private com.zjw.zhbraceletsdk.service.ZhBraceletService mBleService;
    private com.zjw.zhbraceletsdk.bean.MusicInfo musicInfo;
    private com.noisefit_commans.interfaces.IQueryDataCallback testQueryDeviceDataCallback;
    private int currentGpsSportState = -1;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private android.content.BroadcastReceiver locationReceiver;
    private final com.zjw.zhbraceletsdk.linstener.SimplePerformerListener mPerformerListener = null;
    
    @javax.inject.Inject
    public NavPlusQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_nav_plus.base.NavPlusApplicationHandler getNavPlusApplicationHandler() {
        return null;
    }
    
    public final void setNavPlusApplicationHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_nav_plus.handler.dataConversion.DataConverter getDataConverter() {
        return null;
    }
    
    public final void setDataConverter(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.dataConversion.DataConverter p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final android.content.Context getContext() {
        return null;
    }
    
    public final void setContext(@org.jetbrains.annotations.NotNull
    android.content.Context p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.google.gson.Gson getGson() {
        return null;
    }
    
    public final void setGson(@org.jetbrains.annotations.NotNull
    com.google.gson.Gson p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.data.local.abstraction.WatchDataStore getWatchDataStore() {
        return null;
    }
    
    public final void setWatchDataStore(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore p0) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice device) {
    }
    
    @java.lang.Override
    public void setVolume(int currentVolume, int maxVolume) {
    }
    
    @java.lang.Override
    public void setMusicStatus(int status, @org.jetbrains.annotations.Nullable
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.Integer sec) {
    }
    
    private final com.zjw.zhbraceletsdk.bean.MusicInfo getMusicInfo() {
        return null;
    }
    
    @java.lang.Override
    public void init() {
    }
    
    private final boolean hasPermission() {
        return false;
    }
    
    private final void sendErrorMessageToApp(java.lang.String message, java.lang.String title) {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void getContactList() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    private final void enableLocation() {
    }
    
    private final void disableLocation() {
    }
    
    @java.lang.Override
    public void queryFirmwareVersion() {
    }
    
    @java.lang.Override
    public void getBluetoothCallStatus() {
    }
    
    private final void initLogListener() {
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
    public void getWatchFaces() {
    }
    
    @java.lang.Override
    public void getMedicineReminders() {
    }
    
    @java.lang.Override
    public void getReminders() {
    }
    
    @java.lang.Override
    public void getBodyTempUnit() {
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
    
    @java.lang.Override
    public void getHandwashData() {
    }
    
    private final int returnVolume() {
        return 0;
    }
    
    private final void updateVolume(boolean isIncreaseVolume) {
    }
    
    private final void initQuickEyeListener() {
    }
    
    @java.lang.Override
    public void getMedicineReminderSettings() {
    }
    
    @java.lang.Override
    public void getMealReminderSettings() {
    }
    
    @java.lang.Override
    public void getDrinkWaterSettings() {
    }
    
    @java.lang.Override
    public void getCustomReplies() {
    }
    
    @java.lang.Override
    public void getWorldClock() {
    }
    
    @java.lang.Override
    public void getStockList() {
    }
    
    private final void setStockListener() {
    }
    
    @java.lang.Override
    public void getQuickEyeMovementSwitch() {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/noisefit_nav_plus/handler/NavPlusQueryDeviceUnitsHandler$Companion;", "", "()V", "LAT_LONG", "", "LOCATION_BROADCAST_RECEIVER", "noisefit_nav_plus_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}