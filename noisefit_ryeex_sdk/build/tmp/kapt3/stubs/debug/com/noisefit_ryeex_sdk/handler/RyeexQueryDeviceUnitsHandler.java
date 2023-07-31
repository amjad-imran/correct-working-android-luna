package com.noisefit_ryeex_sdk.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b \n\u0002\u0018\u0002\n\u0002\b\u0013\u0018\u0000 T2\u00020\u0001:\u0001TB\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\b\u0010 \u001a\u00020!H\u0016J\u001b\u0010\"\u001a\u00020!\"\u0004\b\u0000\u0010#2\u0006\u0010$\u001a\u0002H#H\u0016\u00a2\u0006\u0002\u0010%J\u001b\u0010&\u001a\u00020!\"\u0004\b\u0000\u0010#2\u0006\u0010$\u001a\u0002H#H\u0016\u00a2\u0006\u0002\u0010%J\b\u0010\'\u001a\u00020!H\u0002J\b\u0010(\u001a\u00020!H\u0002J\b\u0010)\u001a\u00020!H\u0016J\b\u0010*\u001a\u00020!H\u0016J\b\u0010+\u001a\u00020!H\u0016J\b\u0010,\u001a\u00020!H\u0016J\b\u0010-\u001a\u00020\u0016H\u0002J\b\u0010.\u001a\u00020!H\u0016J\b\u0010/\u001a\u00020!H\u0016J\b\u00100\u001a\u00020!H\u0016J\b\u00101\u001a\u00020!H\u0016J\b\u00102\u001a\u00020!H\u0016J\b\u00103\u001a\u00020!H\u0016J\b\u00104\u001a\u00020!H\u0016J\b\u00105\u001a\u00020!H\u0016J\b\u00106\u001a\u00020!H\u0016J\b\u00107\u001a\u00020!H\u0016J\b\u00108\u001a\u00020!H\u0016J\b\u00109\u001a\u00020\fH\u0002J\b\u0010:\u001a\u00020!H\u0016J\b\u0010;\u001a\u00020!H\u0016J\b\u0010<\u001a\u00020!H\u0016J\u0010\u0010=\u001a\u00020!2\u0006\u0010>\u001a\u00020\u0014H\u0016J\b\u0010?\u001a\u00020!H\u0016J\b\u0010@\u001a\u00020!H\u0016J\u0014\u0010A\u001a\u000e\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\u00160BH\u0002J\u0010\u0010C\u001a\u00020!2\u0006\u0010>\u001a\u00020\u0014H\u0016J\b\u0010D\u001a\u00020!H\u0002J)\u0010E\u001a\u00020!2\u0006\u0010F\u001a\u00020\u00162\b\u0010G\u001a\u0004\u0018\u00010\u001c2\b\u0010H\u001a\u0004\u0018\u00010\u0016H\u0016\u00a2\u0006\u0002\u0010IJ\u0010\u0010J\u001a\u00020!2\u0006\u0010K\u001a\u00020\fH\u0002J\u0018\u0010L\u001a\u00020!2\u0006\u0010M\u001a\u00020\u00162\u0006\u0010N\u001a\u00020\u0016H\u0016J(\u0010O\u001a\u00020!2\u0006\u0010P\u001a\u00020\u001c2\u0006\u0010K\u001a\u00020\f2\u0006\u0010N\u001a\u00020\u00162\u0006\u0010M\u001a\u00020\u0016H\u0002J\u0010\u0010Q\u001a\u00020!2\u0006\u0010R\u001a\u00020\fH\u0002J\u0010\u0010S\u001a\u00020!2\u0006\u0010S\u001a\u00020\u0016H\u0002R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006U"}, d2 = {"Lcom/noisefit_ryeex_sdk/handler/RyeexQueryDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "ryeexApplicationHandler", "Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;", "dataConverter", "Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;Landroid/content/Context;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "isPause", "", "lastDistance", "", "locationClientClass", "Lcom/noisefit_commans/utils/LocationClientClass;", "locationReceiver", "Landroid/content/BroadcastReceiver;", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "previousVolume", "", "queryDeviceDataCallback", "Lcom/noisefit_commans/interfaces/IQueryDataCallback;", "sessionId", "", "songName", "", "totalDistance", "watchDevice", "Lcom/ryeex/watch/adapter/device/WatchDevice;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "disableLocation", "enableLocation", "getAlarms", "getAppList", "getBleCallingSwitch", "getContactList", "getDeviceId", "getDrinkWaterSettings", "getFirmwareLogs", "getHeartRateAlert", "getHeartRateInterval", "getMusicControlSettings", "getSedentaryData", "getSportModeInfo", "getUserGoals", "getUserInfo", "getWeatherSwitchStatus", "getWidgetList", "hasLocationPermission", "init", "queryBatteryPower", "queryFirmwareUpgrade", "queryFirmwareUpgradeNew", "colorFitDevice", "queryFirmwareVersion", "removeCallbacks", "returnVolume", "Lkotlin/Pair;", "setDevice", "setMusicPlayerState", "setMusicStatus", "status", "title", "sec", "(ILjava/lang/String;Ljava/lang/Integer;)V", "setPlayPauseMusic", "isPlay", "setVolume", "currentVolume", "maxVolume", "syncMusic", "musicName", "updateVolume", "isIncreaseVolume", "volume", "Companion", "noisefit_ryeex_sdk_debug"})
public final class RyeexQueryDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions {
    private com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler;
    private com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter;
    private android.content.Context context;
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private com.noisefit_commans.utils.LocationClientClass locationClientClass;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_ryeex_sdk.handler.RyeexQueryDeviceUnitsHandler.Companion Companion = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String LAT_LONG = "LAT_LONG";
    private int previousVolume = 0;
    private java.lang.String songName;
    private com.noisefit_commans.interfaces.IQueryDataCallback queryDeviceDataCallback;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private com.ryeex.watch.adapter.device.WatchDevice watchDevice;
    private double totalDistance = 0.0;
    private double lastDistance = 0.0;
    private boolean isPause = false;
    private long sessionId = 0L;
    private android.content.BroadcastReceiver locationReceiver;
    
    @javax.inject.Inject
    public RyeexQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    private final boolean hasLocationPermission() {
        return false;
    }
    
    private final void enableLocation() {
    }
    
    private final void disableLocation() {
    }
    
    private final void updateVolume(boolean isIncreaseVolume) {
    }
    
    private final void setMusicPlayerState() {
    }
    
    @java.lang.Override
    public void setVolume(int currentVolume, int maxVolume) {
    }
    
    @java.lang.Override
    public void setMusicStatus(int status, @org.jetbrains.annotations.Nullable
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.Integer sec) {
    }
    
    private final void syncMusic(java.lang.String musicName, boolean isPlay, int maxVolume, int currentVolume) {
    }
    
    private final void setPlayPauseMusic(boolean isPlay) {
    }
    
    private final void volume(int volume) {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    @java.lang.Override
    public void queryFirmwareVersion() {
    }
    
    private final int getDeviceId() {
        return 0;
    }
    
    @java.lang.Override
    public void queryFirmwareUpgrade() {
    }
    
    @java.lang.Override
    public void queryFirmwareUpgradeNew(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public void queryBatteryPower() {
    }
    
    @java.lang.Override
    public void getUserInfo() {
    }
    
    @java.lang.Override
    public void getAppList() {
    }
    
    @java.lang.Override
    public void getAlarms() {
    }
    
    @java.lang.Override
    public void getHeartRateInterval() {
    }
    
    @java.lang.Override
    public void getSedentaryData() {
    }
    
    @java.lang.Override
    public void getUserGoals() {
    }
    
    @java.lang.Override
    public void getDrinkWaterSettings() {
    }
    
    @java.lang.Override
    public void getHeartRateAlert() {
    }
    
    @java.lang.Override
    public void getSportModeInfo() {
    }
    
    @java.lang.Override
    public void getMusicControlSettings() {
    }
    
    private final kotlin.Pair<java.lang.Integer, java.lang.Integer> returnVolume() {
        return null;
    }
    
    @java.lang.Override
    public void getWidgetList() {
    }
    
    @java.lang.Override
    public void getContactList() {
    }
    
    @java.lang.Override
    public void getBleCallingSwitch() {
    }
    
    @java.lang.Override
    public void getFirmwareLogs() {
    }
    
    @java.lang.Override
    public void getWeatherSwitchStatus() {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/noisefit_ryeex_sdk/handler/RyeexQueryDeviceUnitsHandler$Companion;", "", "()V", "LAT_LONG", "", "LOCATION_BROADCAST_RECEIVER", "noisefit_ryeex_sdk_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}