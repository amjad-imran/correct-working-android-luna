package com.noisefit.colorfit_pro.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00ae\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0015\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0013\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\b\u00102\u001a\u000203H\u0016J\u001b\u00104\u001a\u000203\"\u0004\b\u0000\u001052\u0006\u00106\u001a\u0002H5H\u0016\u00a2\u0006\u0002\u00107J\u001b\u00108\u001a\u000203\"\u0004\b\u0000\u001052\u0006\u00106\u001a\u0002H5H\u0016\u00a2\u0006\u0002\u00107J0\u00109\u001a\u0002032\u0006\u0010:\u001a\u00020;2\u0006\u0010<\u001a\u00020,2\u0016\u0010=\u001a\u0012\u0012\u0004\u0012\u00020>0\u001cj\b\u0012\u0004\u0012\u00020>`?H\u0002J\b\u0010@\u001a\u000203H\u0016J\b\u0010A\u001a\u000203H\u0016J\b\u0010B\u001a\u000203H\u0016J\b\u0010C\u001a\u000203H\u0016J\b\u0010D\u001a\u000203H\u0016J\u0010\u0010E\u001a\u0002032\u0006\u0010<\u001a\u00020,H\u0002J\b\u0010F\u001a\u000203H\u0016J\b\u0010G\u001a\u000203H\u0016J\b\u0010H\u001a\u000203H\u0016J\b\u0010I\u001a\u000203H\u0016J\b\u0010J\u001a\u000203H\u0016J\b\u0010K\u001a\u000203H\u0016J\b\u0010L\u001a\u000203H\u0016J\b\u0010M\u001a\u000203H\u0016J\b\u0010N\u001a\u000203H\u0016J\b\u0010O\u001a\u000203H\u0016J\b\u0010P\u001a\u000203H\u0016J\b\u0010Q\u001a\u000203H\u0016J\b\u0010R\u001a\u000203H\u0016Jv\u0010R\u001a\u0002032\u0006\u0010:\u001a\u00020;2\u0006\u0010<\u001a\u00020,2\u0006\u0010S\u001a\u00020)2F\u0010T\u001aB\u0012#\u0012!\u0012\u0004\u0012\u00020>0\u001cj\b\u0012\u0004\u0012\u00020>`?\u00a2\u0006\f\bV\u0012\b\bW\u0012\u0004\b\b(=\u0012\u0013\u0012\u00110)\u00a2\u0006\f\bV\u0012\b\bW\u0012\u0004\b\b(X\u0012\u0004\u0012\u0002030U2\f\u0010Y\u001a\b\u0012\u0004\u0012\u0002030ZH\u0002J\b\u0010[\u001a\u000203H\u0016J\b\u0010\\\u001a\u000203H\u0016J\b\u0010]\u001a\u000203H\u0016J\b\u0010^\u001a\u000203H\u0002J\u0010\u0010_\u001a\u0002032\u0006\u0010`\u001a\u00020aH\u0002J\b\u0010b\u001a\u000203H\u0016J\b\u0010c\u001a\u000203H\u0016J\u0010\u0010d\u001a\u0002032\u0006\u0010\r\u001a\u00020\u000eH\u0016J\b\u0010e\u001a\u000203H\u0016J\u0014\u0010f\u001a\u000e\u0012\u0004\u0012\u00020)\u0012\u0004\u0012\u00020)0gH\u0002J\u0018\u0010h\u001a\u0002032\u0006\u0010i\u001a\u00020,2\u0006\u0010j\u001a\u00020,H\u0002J\u0010\u0010k\u001a\u0002032\u0006\u0010l\u001a\u00020\u000eH\u0016J\b\u0010m\u001a\u000203H\u0002J)\u0010n\u001a\u0002032\u0006\u0010o\u001a\u00020)2\b\u0010p\u001a\u0004\u0018\u00010,2\b\u0010q\u001a\u0004\u0018\u00010)H\u0016\u00a2\u0006\u0002\u0010rJ\u0018\u0010s\u001a\u0002032\u0006\u0010t\u001a\u00020)2\u0006\u0010u\u001a\u00020)H\u0016J \u0010v\u001a\u0002032\u0006\u0010w\u001a\u00020,2\u0006\u0010u\u001a\u00020)2\u0006\u0010t\u001a\u00020)H\u0002J\u0010\u0010x\u001a\u0002032\u0006\u0010y\u001a\u00020aH\u0002R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u001f\"\u0004\b \u0010!R\u0010\u0010\"\u001a\u0004\u0018\u00010#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b$\u0010%\"\u0004\b&\u0010\'R\u000e\u0010(\u001a\u00020)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010*\u001a\u00020)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010+\u001a\u0004\u0018\u00010,X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b.\u0010/\"\u0004\b0\u00101\u00a8\u0006z"}, d2 = {"Lcom/noisefit/colorfit_pro/handler/ProQueryDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "navPlusApplicationHandler", "Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "dataConverter", "Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;Landroid/content/Context;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "batteryListener", "Lcom/crrepa/ble/conn/listener/CRPDeviceBatteryListener;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "crpCameraOperationListener", "Lcom/crrepa/ble/conn/listener/CRPCameraOperationListener;", "crpFindPhoneListener", "Lcom/crrepa/ble/conn/listener/CRPFindPhoneListener;", "crpPhoneOperationListener", "Lcom/crrepa/ble/conn/listener/CRPPhoneOperationListener;", "crpQuickResponsesChangeListener", "Lcom/crrepa/ble/conn/listener/CRPQuickResponsesChangeListener;", "customReplyDataList", "Ljava/util/ArrayList;", "Lcom/noisefit_commans/models/CustomReplyData$CustomReply;", "getDataConverter", "()Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;", "setDataConverter", "(Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;)V", "iQueryDataCallback", "Lcom/noisefit_commans/interfaces/IQueryDataCallback;", "getNavPlusApplicationHandler", "()Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "setNavPlusApplicationHandler", "(Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;)V", "previousVolume", "", "quickReplyCount", "songName", "", "wFaceIndex", "getWatchDataStore", "()Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "setWatchDataStore", "(Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "fetchSupplierWatchFace", "info", "Lcom/crrepa/ble/conn/bean/CRPSupportWatchFaceInfo;", "version", "watchFaceList", "Lcom/noisefit_commans/models/WatchFace;", "Lkotlin/collections/ArrayList;", "getAlarms", "getContactList", "getCustomReplies", "getDoNotDisturbData", "getHeartRateInterval", "getIconBuzzVersion", "getLanguage", "getMenstrualSettings", "getScreenAwakeInterval", "getSedentaryData", "getSpo2Settings", "getStressSettings", "getUPIQRCode", "getUserGoals", "getUserInfo", "getVibrationIntensity", "getWalkReminderData", "getWatchFaceLayout", "getWatchFaces", "pageIndex", "success", "Lkotlin/Function2;", "Lkotlin/ParameterName;", "name", "totalWCount", "failed", "Lkotlin/Function0;", "getWeatherSwitchStatus", "getWristLiftGesture", "init", "musicPlayPause", "nextPreviousSong", "nextSong", "", "queryBatteryPower", "queryFirmwareUpgrade", "queryFirmwareUpgradeNew", "queryFirmwareVersion", "returnVolume", "Lkotlin/Pair;", "sendSMS", "phoneNumber", "text", "setDevice", "device", "setMusicPlayerState", "setMusicStatus", "status", "title", "sec", "(ILjava/lang/String;Ljava/lang/Integer;)V", "setVolume", "currentVolume", "maxVolume", "syncMusic", "musicName", "updateVolume", "isIncreaseVolume", "noisefit_colorfit_pro_debug"})
public final class ProQueryDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions {
    @org.jetbrains.annotations.NotNull
    private com.noisefit.colorfit_pro.base.ProApplicationHandler navPlusApplicationHandler;
    @org.jetbrains.annotations.NotNull
    private com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter;
    @org.jetbrains.annotations.NotNull
    private android.content.Context context;
    @org.jetbrains.annotations.NotNull
    private com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore;
    private final java.util.ArrayList<com.noisefit_commans.models.CustomReplyData.CustomReply> customReplyDataList = null;
    private java.lang.String songName;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private com.noisefit_commans.interfaces.IQueryDataCallback iQueryDataCallback;
    private int previousVolume = 0;
    private int wFaceIndex = 1;
    private final com.crrepa.ble.conn.listener.CRPCameraOperationListener crpCameraOperationListener = null;
    private int quickReplyCount = 0;
    private final com.crrepa.ble.conn.listener.CRPQuickResponsesChangeListener crpQuickResponsesChangeListener = null;
    private final com.crrepa.ble.conn.listener.CRPPhoneOperationListener crpPhoneOperationListener = null;
    private final com.crrepa.ble.conn.listener.CRPFindPhoneListener crpFindPhoneListener = null;
    private final com.crrepa.ble.conn.listener.CRPDeviceBatteryListener batteryListener = null;
    
    @javax.inject.Inject
    public ProQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler navPlusApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit.colorfit_pro.base.ProApplicationHandler getNavPlusApplicationHandler() {
        return null;
    }
    
    public final void setNavPlusApplicationHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit.colorfit_pro.dataConversion.DataConverter getDataConverter() {
        return null;
    }
    
    public final void setDataConverter(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.dataConversion.DataConverter p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final android.content.Context getContext() {
        return null;
    }
    
    public final void setContext(@org.jetbrains.annotations.NotNull
    android.content.Context p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.data.local.abstraction.WatchDataStore getWatchDataStore() {
        return null;
    }
    
    public final void setWatchDataStore(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore p0) {
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    private final void sendSMS(java.lang.String phoneNumber, java.lang.String text) {
    }
    
    private final void updateVolume(boolean isIncreaseVolume) {
    }
    
    @java.lang.Override
    public void getCustomReplies() {
    }
    
    @java.lang.Override
    public void setVolume(int currentVolume, int maxVolume) {
    }
    
    private final void nextPreviousSong(boolean nextSong) {
    }
    
    @java.lang.Override
    public void getScreenAwakeInterval() {
    }
    
    @java.lang.Override
    public void getContactList() {
    }
    
    private final kotlin.Pair<java.lang.Integer, java.lang.Integer> returnVolume() {
        return null;
    }
    
    private final void setMusicPlayerState() {
    }
    
    private final void musicPlayPause() {
    }
    
    private final void syncMusic(java.lang.String musicName, int maxVolume, int currentVolume) {
    }
    
    @java.lang.Override
    public void setMusicStatus(int status, @org.jetbrains.annotations.Nullable
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.Integer sec) {
    }
    
    @java.lang.Override
    public void getVibrationIntensity() {
    }
    
    private final void getIconBuzzVersion(java.lang.String version) {
    }
    
    @java.lang.Override
    public void queryFirmwareVersion() {
    }
    
    @java.lang.Override
    public void getWeatherSwitchStatus() {
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
    public void queryBatteryPower() {
    }
    
    @java.lang.Override
    public void getUserInfo() {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice device) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public void getAlarms() {
    }
    
    @java.lang.Override
    public void getDoNotDisturbData() {
    }
    
    @java.lang.Override
    public void getUPIQRCode() {
    }
    
    @java.lang.Override
    public void getWalkReminderData() {
    }
    
    @java.lang.Override
    public void getMenstrualSettings() {
    }
    
    @java.lang.Override
    public void getHeartRateInterval() {
    }
    
    @java.lang.Override
    public void getSpo2Settings() {
    }
    
    private final void getWatchFaces(com.crrepa.ble.conn.bean.CRPSupportWatchFaceInfo info, java.lang.String version, int pageIndex, kotlin.jvm.functions.Function2<? super java.util.ArrayList<com.noisefit_commans.models.WatchFace>, ? super java.lang.Integer, kotlin.Unit> success, kotlin.jvm.functions.Function0<kotlin.Unit> failed) {
    }
    
    @java.lang.Override
    public void getWatchFaces() {
    }
    
    private final void fetchSupplierWatchFace(com.crrepa.ble.conn.bean.CRPSupportWatchFaceInfo info, java.lang.String version, java.util.ArrayList<com.noisefit_commans.models.WatchFace> watchFaceList) {
    }
    
    @java.lang.Override
    public void getStressSettings() {
    }
    
    @java.lang.Override
    public void getWatchFaceLayout() {
    }
    
    @java.lang.Override
    public void getSedentaryData() {
    }
    
    @java.lang.Override
    public void getUserGoals() {
    }
    
    @java.lang.Override
    public void getWristLiftGesture() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
}