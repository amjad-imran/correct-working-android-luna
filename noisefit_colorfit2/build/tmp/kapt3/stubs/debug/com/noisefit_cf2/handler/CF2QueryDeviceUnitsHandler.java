package com.noisefit_cf2.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u001e\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u0000 U2\u00020\u0001:\u0001UB\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u001a\u001a\u00020\u001bH\u0016J\u001b\u0010\u001c\u001a\u00020\u001b\"\u0004\b\u0000\u0010\u001d2\u0006\u0010\u001e\u001a\u0002H\u001dH\u0016\u00a2\u0006\u0002\u0010\u001fJ\u001b\u0010 \u001a\u00020\u001b\"\u0004\b\u0000\u0010\u001d2\u0006\u0010\u001e\u001a\u0002H\u001dH\u0016\u00a2\u0006\u0002\u0010\u001fJ\b\u0010!\u001a\u00020\u001bH\u0016J\b\u0010\"\u001a\u00020\u001bH\u0016J\u000e\u0010#\u001a\b\u0012\u0004\u0012\u00020%0$H\u0002J\b\u0010&\u001a\u00020\u001bH\u0016J\u0010\u0010\'\u001a\u00020\u001b2\u0006\u0010(\u001a\u00020\u0012H\u0002J\n\u0010)\u001a\u0004\u0018\u00010*H\u0002J\b\u0010+\u001a\u00020\u001bH\u0016J\b\u0010,\u001a\u00020\u001bH\u0016J\b\u0010-\u001a\u00020\u001bH\u0016J\b\u0010.\u001a\u00020\u001bH\u0016J\b\u0010/\u001a\u00020\u001bH\u0016J\b\u00100\u001a\u00020\u001bH\u0016J\b\u00101\u001a\u00020\u001bH\u0016J\b\u00102\u001a\u00020\u001bH\u0016J\b\u00103\u001a\u00020\u001bH\u0016J\b\u00104\u001a\u00020\u001bH\u0016J\b\u00105\u001a\u00020\u001bH\u0016J\b\u00106\u001a\u00020\u001bH\u0016J\b\u00107\u001a\u00020\u001bH\u0016J\b\u00108\u001a\u00020\u001bH\u0016J\b\u00109\u001a\u00020\u001bH\u0016J\b\u0010:\u001a\u00020\u001bH\u0016J\b\u0010;\u001a\u00020\u001bH\u0016J\b\u0010<\u001a\u00020\u001bH\u0016J\b\u0010=\u001a\u00020\u001bH\u0016J\b\u0010>\u001a\u00020\u001bH\u0016J\b\u0010?\u001a\u00020\u001bH\u0016J\b\u0010@\u001a\u00020\u001bH\u0016J\b\u0010A\u001a\u00020\u001bH\u0016J\u0010\u0010B\u001a\u00020\u001b2\u0006\u0010\t\u001a\u00020\nH\u0016J\b\u0010C\u001a\u00020\u001bH\u0016J\b\u0010D\u001a\u00020\u001bH\u0016J\u0010\u0010E\u001a\u00020\u001b2\u0006\u0010\t\u001a\u00020\nH\u0016J)\u0010F\u001a\u00020\u001b2\u0006\u0010G\u001a\u00020\u00122\b\u0010H\u001a\u0004\u0018\u00010I2\b\u0010J\u001a\u0004\u0018\u00010\u0012H\u0016\u00a2\u0006\u0002\u0010KJ\b\u0010L\u001a\u00020\u001bH\u0002J\u0006\u0010M\u001a\u00020\u001bJ\u0018\u0010N\u001a\u00020\u001b2\u0006\u0010O\u001a\u00020\u00122\u0006\u0010P\u001a\u00020\u0012H\u0016J\b\u0010Q\u001a\u00020\u001bH\u0016J\u0010\u0010R\u001a\u00020\u001b2\u0006\u0010S\u001a\u00020TH\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u0004\u00a8\u0006V"}, d2 = {"Lcom/noisefit_cf2/handler/CF2QueryDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "appDeviceParaCallBack", "Lcom/ido/ble/callback/GetDeviceParaCallBack$ICallBack;", "callBack", "Lcom/ido/ble/callback/GetDeviceInfoCallBack$ICallBack;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "configCallBack", "Lcom/ido/ble/callback/SyncCallBack$IConfigCallBack;", "info", "Lcom/ido/ble/protocol/model/BasicInfo;", "phoneMsgNoticeCallBack", "Lcom/ido/ble/callback/PhoneMsgNoticeCallBack$ICallBack;", "previousVolume", "", "settingsCallBack", "Lcom/ido/ble/callback/SettingCallBack$ICallBack;", "testQueryDeviceDataCallback", "Lcom/noisefit_commans/interfaces/IQueryDataCallback;", "getWatchDataStore", "()Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "setWatchDataStore", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "getActivityRecogniseSettings", "getAlarms", "getAllAvailableSportInfo", "", "Lcom/ido/ble/protocol/model/SportModeSortV3$SportModeSortItemV3;", "getBrightnessLevel", "getCloudWatchFaces", "deviceId", "getDeviceControlAppCallback", "Lcom/ido/ble/callback/DeviceControlAppCallBack$ICallBack;", "getDeviceUnits", "getDoNotDisturbData", "getDrinkWaterSettings", "getFindPhoneSwitch", "getFirmwareLogs", "getHandwashData", "getHeartRateAlert", "getHeartRateInterval", "getLanguage", "getMenstrualSettings", "getMusicControlSettings", "getSedentaryData", "getSportModeInfo", "getStressSettings", "getUserGoals", "getUserInfo", "getWalkReminderData", "getWatchFaces", "getWeatherSwitchStatus", "getWristLiftGesture", "init", "queryBatteryPower", "queryFirmwareUpgrade", "queryFirmwareUpgradeNew", "queryFirmwareVersion", "removeCallbacks", "setDevice", "setMusicStatus", "status", "title", "", "sec", "(ILjava/lang/String;Ljava/lang/Integer;)V", "setOutdoorActivities", "setOutdoorActivitiesPro3", "setVolume", "currentVolume", "maxVolume", "syncDeviceUnits", "updateVolume", "isIncreaseVolume", "", "Companion", "noisefit_colorfit2_debug"})
public final class CF2QueryDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions {
    @org.jetbrains.annotations.NotNull
    private com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore;
    private com.noisefit_commans.interfaces.IQueryDataCallback testQueryDeviceDataCallback;
    private int previousVolume = 0;
    private com.ido.ble.protocol.model.BasicInfo info;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private final com.ido.ble.callback.SettingCallBack.ICallBack settingsCallBack = null;
    private final com.ido.ble.callback.SyncCallBack.IConfigCallBack configCallBack = null;
    private final com.ido.ble.callback.GetDeviceInfoCallBack.ICallBack callBack = null;
    private final com.ido.ble.callback.PhoneMsgNoticeCallBack.ICallBack phoneMsgNoticeCallBack = null;
    private final com.ido.ble.callback.GetDeviceParaCallBack.ICallBack appDeviceParaCallBack = null;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_cf2.handler.CF2QueryDeviceUnitsHandler.Companion Companion = null;
    private static com.ido.ble.callback.DeviceControlAppCallBack.ICallBack deviceControlsCallback;
    
    @javax.inject.Inject
    public CF2QueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
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
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public void setMusicStatus(int status, @org.jetbrains.annotations.Nullable
    java.lang.String title, @org.jetbrains.annotations.Nullable
    java.lang.Integer sec) {
    }
    
    @java.lang.Override
    public void init() {
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
    public void getBrightnessLevel() {
    }
    
    @java.lang.Override
    public void getUserInfo() {
    }
    
    @java.lang.Override
    public void getFindPhoneSwitch() {
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
    
    private final void getCloudWatchFaces(int deviceId) {
    }
    
    @java.lang.Override
    public void getAlarms() {
    }
    
    @java.lang.Override
    public void getDoNotDisturbData() {
    }
    
    @java.lang.Override
    public void getMenstrualSettings() {
    }
    
    @java.lang.Override
    public void getHeartRateInterval() {
    }
    
    @java.lang.Override
    public void getSedentaryData() {
    }
    
    @java.lang.Override
    public void getWalkReminderData() {
    }
    
    @java.lang.Override
    public void getDrinkWaterSettings() {
    }
    
    @java.lang.Override
    public void getStressSettings() {
    }
    
    @java.lang.Override
    public void getMusicControlSettings() {
    }
    
    @java.lang.Override
    public void getActivityRecogniseSettings() {
    }
    
    @java.lang.Override
    public void getDeviceUnits() {
    }
    
    @java.lang.Override
    public void getUserGoals() {
    }
    
    @java.lang.Override
    public void getWeatherSwitchStatus() {
    }
    
    @java.lang.Override
    public void getWristLiftGesture() {
    }
    
    @java.lang.Override
    public void getHandwashData() {
    }
    
    @java.lang.Override
    public void getHeartRateAlert() {
    }
    
    @java.lang.Override
    public void syncDeviceUnits() {
    }
    
    private final void setOutdoorActivities() {
    }
    
    public final void setOutdoorActivitiesPro3() {
    }
    
    private final java.util.List<com.ido.ble.protocol.model.SportModeSortV3.SportModeSortItemV3> getAllAvailableSportInfo() {
        return null;
    }
    
    @java.lang.Override
    public void getSportModeInfo() {
    }
    
    @java.lang.Override
    public void getFirmwareLogs() {
    }
    
    private final com.ido.ble.callback.DeviceControlAppCallBack.ICallBack getDeviceControlAppCallback() {
        return null;
    }
    
    @java.lang.Override
    public void setVolume(int currentVolume, int maxVolume) {
    }
    
    private final void updateVolume(boolean isIncreaseVolume) {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/noisefit_cf2/handler/CF2QueryDeviceUnitsHandler$Companion;", "", "()V", "deviceControlsCallback", "Lcom/ido/ble/callback/DeviceControlAppCallBack$ICallBack;", "noisefit_colorfit2_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}