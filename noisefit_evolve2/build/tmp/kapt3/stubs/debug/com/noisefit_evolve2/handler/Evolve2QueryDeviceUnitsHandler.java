package com.noisefit_evolve2.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u001e\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0014\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\b\u0010 \u001a\u00020!H\u0016J\u001b\u0010\"\u001a\u00020!\"\u0004\b\u0000\u0010#2\u0006\u0010$\u001a\u0002H#H\u0016\u00a2\u0006\u0002\u0010%J\u001b\u0010&\u001a\u00020!\"\u0004\b\u0000\u0010#2\u0006\u0010$\u001a\u0002H#H\u0016\u00a2\u0006\u0002\u0010%J\b\u0010\'\u001a\u00020!H\u0002J\b\u0010(\u001a\u00020!H\u0002J\b\u0010)\u001a\u00020!H\u0016J\b\u0010*\u001a\u00020!H\u0016J\b\u0010+\u001a\u00020!H\u0016J\b\u0010,\u001a\u00020!H\u0016J\b\u0010-\u001a\u00020!H\u0016J\b\u0010.\u001a\u00020!H\u0016J\b\u0010/\u001a\u00020!H\u0016J\b\u00100\u001a\u00020!H\u0016J\b\u00101\u001a\u00020!H\u0016J\b\u00102\u001a\u00020!H\u0016J\b\u00103\u001a\u00020!H\u0016J\b\u00104\u001a\u00020!H\u0016J\b\u00105\u001a\u00020!H\u0016J\b\u00106\u001a\u00020!H\u0016J\b\u00107\u001a\u00020!H\u0016J\u0010\u00108\u001a\u00020\u000f2\u0006\u00109\u001a\u00020\fH\u0002J\b\u0010:\u001a\u00020!H\u0016J\b\u0010;\u001a\u00020\u0011H\u0002J\b\u0010<\u001a\u00020!H\u0016J\b\u0010=\u001a\u00020!H\u0002J\u0012\u0010>\u001a\u00020!2\b\u0010?\u001a\u0004\u0018\u00010@H\u0002J\b\u0010A\u001a\u00020!H\u0016J\b\u0010B\u001a\u00020!H\u0016J\u0010\u0010C\u001a\u00020!2\u0006\u0010D\u001a\u00020\u0019H\u0016J\b\u0010E\u001a\u00020!H\u0016J\b\u0010F\u001a\u00020!H\u0016J\u0014\u0010G\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0HH\u0002J\u0018\u0010I\u001a\u00020!2\u0006\u0010J\u001a\u00020\f2\u0006\u0010K\u001a\u00020\fH\u0002J\u0010\u0010L\u001a\u00020!2\u0006\u0010D\u001a\u00020\u0019H\u0016J\b\u0010M\u001a\u00020!H\u0002J)\u0010N\u001a\u00020!2\u0006\u0010O\u001a\u00020\u000f2\b\u0010K\u001a\u0004\u0018\u00010\f2\b\u0010P\u001a\u0004\u0018\u00010\u000fH\u0016\u00a2\u0006\u0002\u0010QJ\u0010\u0010R\u001a\u00020!2\u0006\u0010S\u001a\u00020\u0011H\u0002J\u0010\u0010T\u001a\u00020!2\u0006\u0010O\u001a\u00020\u000fH\u0002J\u0018\u0010U\u001a\u00020!2\u0006\u0010V\u001a\u00020\u000f2\u0006\u0010W\u001a\u00020\u000fH\u0016J(\u0010X\u001a\u00020!2\u0006\u0010Y\u001a\u00020\f2\u0006\u0010S\u001a\u00020\u00112\u0006\u0010W\u001a\u00020\u000f2\u0006\u0010V\u001a\u00020\u000fH\u0002J\u0010\u0010Z\u001a\u00020!2\u0006\u0010[\u001a\u00020\u0011H\u0002R\u000e\u0010\u000b\u001a\u00020\fX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\\"}, d2 = {"Lcom/noisefit_evolve2/handler/Evolve2QueryDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "evolve2ApplicationHandler", "Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;", "dataConverter", "Lcom/noisefit_evolve2/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;Lcom/noisefit_evolve2/dataConversion/DataConverter;Landroid/content/Context;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "LAT_LONG", "", "LOCATION_BROADCAST_RECEIVER", "currentGpsSportState", "", "firstLocation", "", "locationClientClass", "Lcom/noisefit_commans/utils/LocationClientClass;", "locationReceiver", "Landroid/content/BroadcastReceiver;", "mClient", "Lcom/touchgui/sdk/TGClient;", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "previousVolume", "songName", "testQueryDeviceDataCallback", "Lcom/noisefit_commans/interfaces/IQueryDataCallback;", "tgEventListener", "Lcom/touchgui/sdk/TGEventListener;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "disableLocation", "enableLocation", "getAlarms", "getAutoSleep", "getCameraSwitchSettings", "getContactList", "getCustomReplies", "getDeviceUnits", "getDoNotDisturbData", "getDrinkWaterSettings", "getHeartRateAlert", "getHeartRateInterval", "getLanguage", "getMenstrualSettings", "getMusicControlSettings", "getSedentaryData", "getUserInfo", "getVersionFromString", "version", "getWristLiftGesture", "hasPermission", "init", "listenForGpsSports", "listenForGpsSports2", "event", "Lcom/touchgui/sdk/bean/TGSportStatusEvent;", "queryBatteryPower", "queryFirmwareUpgrade", "queryFirmwareUpgradeNew", "colorFitDevice", "queryFirmwareVersion", "removeCallbacks", "returnVolume", "Lkotlin/Pair;", "sendErrorMessageToApp", "message", "title", "setDevice", "setMusicPlayerState", "setMusicStatus", "status", "sec", "(ILjava/lang/String;Ljava/lang/Integer;)V", "setPlayPauseMusic", "isPlay", "setPositioningMessage", "setVolume", "currentVolume", "maxVolume", "syncMusic", "musicName", "updateVolume", "isIncreaseVolume", "noisefit_evolve2_debug"})
public final class Evolve2QueryDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions {
    private com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler;
    private com.noisefit_evolve2.dataConversion.DataConverter dataConverter;
    private android.content.Context context;
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private int previousVolume = 0;
    private java.lang.String songName;
    private int currentGpsSportState = -1;
    private boolean firstLocation = true;
    private final java.lang.String LAT_LONG = "LAT_LONG";
    private com.noisefit_commans.utils.LocationClientClass locationClientClass;
    private final java.lang.String LOCATION_BROADCAST_RECEIVER = "LOCATION_BROADCAST_RECEIVER";
    private com.noisefit_commans.interfaces.IQueryDataCallback testQueryDeviceDataCallback;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private com.touchgui.sdk.TGClient mClient;
    private android.content.BroadcastReceiver locationReceiver;
    private final com.touchgui.sdk.TGEventListener tgEventListener = null;
    
    @javax.inject.Inject
    public Evolve2QueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
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
    
    private final void enableLocation() {
    }
    
    private final void disableLocation() {
    }
    
    private final void setPositioningMessage(int status) {
    }
    
    private final boolean hasPermission() {
        return false;
    }
    
    private final void sendErrorMessageToApp(java.lang.String message, java.lang.String title) {
    }
    
    private final void listenForGpsSports2(com.touchgui.sdk.bean.TGSportStatusEvent event) {
    }
    
    private final void listenForGpsSports() {
    }
    
    @java.lang.Override
    public void getLanguage() {
    }
    
    private final void setMusicPlayerState() {
    }
    
    @java.lang.Override
    public void setVolume(int currentVolume, int maxVolume) {
    }
    
    private final void updateVolume(boolean isIncreaseVolume) {
    }
    
    private final void setPlayPauseMusic(boolean isPlay) {
    }
    
    private final void syncMusic(java.lang.String musicName, boolean isPlay, int maxVolume, int currentVolume) {
    }
    
    @java.lang.Override
    public void setMusicStatus(int status, @org.jetbrains.annotations.Nullable
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.Integer sec) {
    }
    
    private final kotlin.Pair<java.lang.Integer, java.lang.Integer> returnVolume() {
        return null;
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    @java.lang.Override
    public void queryFirmwareVersion() {
    }
    
    @java.lang.Override
    public void queryBatteryPower() {
    }
    
    @java.lang.Override
    public void getUserInfo() {
    }
    
    @java.lang.Override
    public void getWristLiftGesture() {
    }
    
    @java.lang.Override
    public void getMenstrualSettings() {
    }
    
    @java.lang.Override
    public void getMusicControlSettings() {
    }
    
    @java.lang.Override
    public void getDoNotDisturbData() {
    }
    
    @java.lang.Override
    public void queryFirmwareUpgrade() {
    }
    
    @java.lang.Override
    public void queryFirmwareUpgradeNew(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    private final int getVersionFromString(java.lang.String version) {
        return 0;
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
    public void getCustomReplies() {
    }
    
    @java.lang.Override
    public void getContactList() {
    }
    
    @java.lang.Override
    public void getCameraSwitchSettings() {
    }
    
    @java.lang.Override
    public void getDrinkWaterSettings() {
    }
}