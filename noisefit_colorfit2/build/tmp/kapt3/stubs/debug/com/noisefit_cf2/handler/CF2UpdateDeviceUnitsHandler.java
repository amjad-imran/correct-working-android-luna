package com.noisefit_cf2.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u009c\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0002\u008f\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u001c\u001a\u00020\u001dH\u0016J\u001b\u0010\u001e\u001a\u00020\u001d\"\u0004\b\u0000\u0010\u001f2\u0006\u0010 \u001a\u0002H\u001fH\u0016\u00a2\u0006\u0002\u0010!J\u001b\u0010\"\u001a\u00020\u001d\"\u0004\b\u0000\u0010\u001f2\u0006\u0010 \u001a\u0002H\u001fH\u0016\u00a2\u0006\u0002\u0010!J\u0010\u0010#\u001a\u00020\u001d2\u0006\u0010$\u001a\u00020\u000eH\u0002J\b\u0010%\u001a\u00020\u001dH\u0002J\u0010\u0010&\u001a\u00020\u001d2\u0006\u0010\'\u001a\u00020\u000eH\u0002J\u0010\u0010(\u001a\u00020\u001d2\u0006\u0010(\u001a\u00020)H\u0016J\u000e\u0010*\u001a\b\u0012\u0004\u0012\u00020,0+H\u0002J\b\u0010-\u001a\u00020\u001dH\u0016J\u0010\u0010.\u001a\u00020\u001d2\u0006\u0010/\u001a\u00020\nH\u0016J\b\u00100\u001a\u00020\u001dH\u0016J\u0010\u00101\u001a\u00020\u001d2\u0006\u00102\u001a\u000203H\u0016J\u0010\u00104\u001a\u00020\u001d2\u0006\u00105\u001a\u00020)H\u0016J\u0010\u00106\u001a\u00020\u001d2\u0006\u00107\u001a\u000208H\u0016J\b\u00109\u001a\u00020\u001dH\u0016J\u0018\u0010:\u001a\u00020\u001d2\u0006\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020\u000eH\u0016J\u0010\u0010>\u001a\u00020\u001d2\u0006\u0010?\u001a\u00020\fH\u0016J\u0018\u0010@\u001a\u00020\u001d2\u0006\u0010A\u001a\u00020B2\u0006\u0010C\u001a\u00020DH\u0016J\u0010\u0010E\u001a\u00020\u001d2\u0006\u0010C\u001a\u00020FH\u0016J\u0010\u0010G\u001a\u00020\u001d2\u0006\u0010H\u001a\u00020IH\u0016J\b\u0010J\u001a\u00020\u001dH\u0016J\u0010\u0010K\u001a\u00020\u001d2\u0006\u0010L\u001a\u00020)H\u0016J\u0010\u0010M\u001a\u00020\u001d2\u0006\u0010N\u001a\u00020OH\u0016J\u0010\u0010P\u001a\u00020\u001d2\u0006\u0010Q\u001a\u00020RH\u0016J\u0010\u0010S\u001a\u00020\u001d2\u0006\u0010T\u001a\u00020UH\u0016J\u0010\u0010V\u001a\u00020\u001d2\u0006\u0010W\u001a\u00020XH\u0016J\u0010\u0010Y\u001a\u00020\u001d2\u0006\u0010Z\u001a\u00020)H\u0016J\u0006\u0010[\u001a\u00020\u001dJ\b\u0010\\\u001a\u00020\u001dH\u0016J\u0010\u0010]\u001a\u00020\u001d2\u0006\u0010H\u001a\u00020IH\u0016J\u0010\u0010^\u001a\u00020\u001d2\u0006\u0010_\u001a\u00020`H\u0016J\b\u0010a\u001a\u00020\u001dH\u0016J\u0010\u0010b\u001a\u00020\u001d2\u0006\u0010H\u001a\u00020IH\u0016J\"\u0010c\u001a\u00020\u001d2\u0006\u0010d\u001a\u00020e2\u0006\u0010f\u001a\u00020g2\b\u0010h\u001a\u0004\u0018\u00010\u000eH\u0016J\u0010\u0010i\u001a\u00020\u001d2\u0006\u0010j\u001a\u00020kH\u0016J\u0010\u0010l\u001a\u00020\u001d2\u0006\u0010/\u001a\u00020\nH\u0016J\u001e\u0010m\u001a\u00020\u001d2\f\u0010n\u001a\b\u0012\u0004\u0012\u00020o0+2\u0006\u0010p\u001a\u00020\u000eH\u0016J\u0010\u0010q\u001a\u00020\u001d2\u0006\u0010L\u001a\u00020)H\u0016J\u0010\u0010r\u001a\u00020\u001d2\u0006\u0010s\u001a\u00020tH\u0016J\u0010\u0010u\u001a\u00020\u001d2\u0006\u0010v\u001a\u00020wH\u0016J\u0006\u0010x\u001a\u00020\u001dJ\u0018\u0010y\u001a\u00020\u001d2\u0006\u0010z\u001a\u00020{2\u0006\u0010|\u001a\u00020}H\u0016J\u0011\u0010~\u001a\u00020\u001d2\u0007\u0010\u007f\u001a\u00030\u0080\u0001H\u0002J\u0011\u0010\u0081\u0001\u001a\u00020\u001d2\u0006\u0010/\u001a\u00020\nH\u0002J\u0012\u0010\u0082\u0001\u001a\u00020\u001d2\u0007\u0010\u0083\u0001\u001a\u00020<H\u0002J\u0013\u0010\u0084\u0001\u001a\u00020\u001d2\b\u0010\u0085\u0001\u001a\u00030\u0086\u0001H\u0016J\u0012\u0010\u0087\u0001\u001a\u00020\u001d2\u0007\u0010\u0088\u0001\u001a\u00020\u000eH\u0016J\u0013\u0010\u0089\u0001\u001a\u00020\u001d2\b\u0010\u008a\u0001\u001a\u00030\u008b\u0001H\u0016J\u0013\u0010\u008c\u0001\u001a\u00020\u001d2\b\u0010\u008d\u0001\u001a\u00030\u008e\u0001H\u0016R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0090\u0001"}, d2 = {"Lcom/noisefit_cf2/handler/CF2UpdateDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "appSendDataCallBack", "Lcom/ido/ble/callback/AppSendDataCallBack$ICallBack;", "bindCallBack", "Lcom/ido/ble/callback/BindCallBack$ICallBack;", "cloudWatchFace", "Lcom/noisefit_commans/models/WatchFace;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "currentDialPlate", "", "deviceResponseCallback", "Lcom/ido/ble/callback/DeviceResponseCommonCallBack$ICallBack;", "fileNameToBeSet", "fileNameToBeSetNoisefitActive", "fileNameToBeSetNoisefitAgile", "fileNameToBeSetPro2Oxy", "fileNameToBeSetPro3", "settingsCallBack", "Lcom/ido/ble/callback/SettingCallBack$ICallBack;", "testUpdateDeviceDataCallback", "Lcom/noisefit_commans/interfaces/device_data/IUpdateDeviceDataCallback;", "watchOperateCallback", "Lcom/ido/ble/watch/custom/callback/WatchPlateCallBack$IOperateCallBack;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "convertToPng", "localImagePath", "createCustomFacesDir", "createDirectory", "dirName", "findDevice", "Lcom/noisefit_commans/models/SwitchSetting;", "getAllAvailableSportInfo", "", "Lcom/ido/ble/protocol/model/SportModeSortV3$SportModeSortItemV3;", "init", "onDownloadedWatchFaceContents", "watchFace", "removeCallbacks", "sendAppNotification", "appNotification", "Lcom/noisefit_commans/models/AppNotification;", "setActivityRecogniseSwitch", "activitySwitch", "setBrightnessLevel", "level", "", "setCallBacks", "setCustomBackground", "imagePath", "Landroid/net/Uri;", "firmware", "setDevice", "device", "setDeviceDateTime", "calender", "Ljava/util/Calendar;", "units", "Lcom/noisefit_commans/models/TimeFormat;", "setDeviceUnits", "Lcom/noisefit_commans/models/DeviceUnits;", "setDrinkWaterReminder", "sedentaryData", "Lcom/noisefit_commans/models/SedentaryData;", "setFactoryReset", "setFindMyPhone", "switchSetting", "setHandWashing", "handWashing", "Lcom/noisefit_commans/models/HandWashing;", "setHeartRateAlert", "heartRateAlert", "Lcom/noisefit_commans/models/HeartRateAlert;", "setHeartRateInterval", "heartRateInterval", "Lcom/noisefit_commans/models/HeartRateInterval;", "setIncomingCallInfo", "incomingCall", "Lcom/noisefit_commans/models/IncomingCall;", "setMusicSwitch", "musicSwitch", "setOutdoorActivitiesPro3", "setRestartDevice", "setSedentaryData", "setSportModeInfo", "data", "Lcom/noisefit_commans/models/SportsModeList;", "setSportSyncParamPro3", "setStressData", "setUserInfo", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "userGoals", "Lcom/noisefit_commans/models/UserGoals;", "userName", "setWalkReminderPro3", "walkReminderData", "Lcom/noisefit_commans/models/WalkReminderData;", "setWatchFace", "setWeatherData", "weatherData", "Lcom/noisefit_commans/models/WeatherData;", "unit", "setWeatherSwitch", "setWristLiftGesture", "wristLiftGesture", "Lcom/noisefit_commans/models/WristLiftGesture;", "startCameraMode", "status", "", "transferCloudWatchFace", "updateAlarm", "alarm", "Lcom/noisefit_commans/models/AlarmsList;", "alarmAction", "Lcom/noisefit_commans/models/AlarmAction;", "updateBackground", "config", "Lcom/ido/ble/protocol/model/WallpaperFileCreateConfig;", "updateCloudWatchFace", "updateCustomBackground", "uri", "updateDND", "doNotDisturb", "Lcom/noisefit_commans/models/DoNotDisturb;", "updateFirmware", "fileUri", "updateLanguage", "language", "Lcom/noisefit_commans/models/Language;", "updateMenstrualData", "menstrualData", "Lcom/noisefit_commans/models/MenstrualData;", "FirmwareUpgradeStatus", "noisefit_colorfit2_debug"})
public final class CF2UpdateDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions {
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private com.noisefit_commans.models.WatchFace cloudWatchFace;
    private final java.lang.String fileNameToBeSet = null;
    private final java.lang.String fileNameToBeSetPro3 = null;
    private final java.lang.String fileNameToBeSetPro2Oxy = null;
    private final java.lang.String fileNameToBeSetNoisefitActive = null;
    private final java.lang.String fileNameToBeSetNoisefitAgile = null;
    private java.lang.String currentDialPlate = "";
    private com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback testUpdateDeviceDataCallback;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private final com.ido.ble.watch.custom.callback.WatchPlateCallBack.IOperateCallBack watchOperateCallback = null;
    private final com.ido.ble.callback.AppSendDataCallBack.ICallBack appSendDataCallBack = null;
    private final com.ido.ble.callback.BindCallBack.ICallBack bindCallBack = null;
    private final com.ido.ble.callback.SettingCallBack.ICallBack settingsCallBack = null;
    private final com.ido.ble.callback.DeviceResponseCommonCallBack.ICallBack deviceResponseCallback = null;
    
    @javax.inject.Inject
    public CF2UpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @java.lang.Override
    public void init() {
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
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    @java.lang.Override
    public void setCallBacks() {
    }
    
    @java.lang.Override
    public void setDeviceUnits(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DeviceUnits units) {
    }
    
    @java.lang.Override
    public void setBrightnessLevel(int level) {
    }
    
    @java.lang.Override
    public void setUserInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UserInfo userInfo, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UserGoals userGoals, @org.jetbrains.annotations.Nullable
    java.lang.String userName) {
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
    public void setDeviceDateTime(@org.jetbrains.annotations.NotNull
    java.util.Calendar calender, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.TimeFormat units) {
    }
    
    @java.lang.Override
    public void setWatchFace(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    public final void transferCloudWatchFace() {
    }
    
    @java.lang.Override
    public void onDownloadedWatchFaceContents(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    private final void updateCloudWatchFace(com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    @java.lang.Override
    public void updateFirmware(@org.jetbrains.annotations.NotNull
    java.lang.String fileUri) {
    }
    
    @java.lang.Override
    public void startCameraMode(boolean status) {
    }
    
    @java.lang.Override
    public void sendAppNotification(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AppNotification appNotification) {
    }
    
    @java.lang.Override
    public void setWristLiftGesture(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WristLiftGesture wristLiftGesture) {
    }
    
    @java.lang.Override
    public void setFactoryReset() {
    }
    
    @java.lang.Override
    public void updateDND(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DoNotDisturb doNotDisturb) {
    }
    
    @java.lang.Override
    public void updateMenstrualData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.MenstrualData menstrualData) {
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
    public void setWalkReminderPro3(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WalkReminderData walkReminderData) {
    }
    
    @java.lang.Override
    public void setDrinkWaterReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void setStressData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void setMusicSwitch(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting musicSwitch) {
    }
    
    @java.lang.Override
    public void setActivityRecogniseSwitch(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting activitySwitch) {
    }
    
    @java.lang.Override
    public void setIncomingCallInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.IncomingCall incomingCall) {
    }
    
    @java.lang.Override
    public void setWeatherSwitch(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting switchSetting) {
    }
    
    @java.lang.Override
    public void setWeatherData(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.WeatherData> weatherData, @org.jetbrains.annotations.NotNull
    java.lang.String unit) {
    }
    
    @java.lang.Override
    public void setFindMyPhone(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting switchSetting) {
    }
    
    @java.lang.Override
    public void setCustomBackground(@org.jetbrains.annotations.NotNull
    android.net.Uri imagePath, @org.jetbrains.annotations.NotNull
    java.lang.String firmware) {
    }
    
    @java.lang.Override
    public void setHandWashing(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.HandWashing handWashing) {
    }
    
    private final void updateCustomBackground(android.net.Uri uri) {
    }
    
    private final void createCustomFacesDir() {
    }
    
    private final void createDirectory(java.lang.String dirName) {
    }
    
    private final void convertToPng(java.lang.String localImagePath) {
    }
    
    private final void updateBackground(com.ido.ble.protocol.model.WallpaperFileCreateConfig config) {
    }
    
    @java.lang.Override
    public void setSportSyncParamPro3() {
    }
    
    @java.lang.Override
    public void setSportModeInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SportsModeList data) {
    }
    
    @java.lang.Override
    public void setRestartDevice() {
    }
    
    public final void setOutdoorActivitiesPro3() {
    }
    
    private final java.util.List<com.ido.ble.protocol.model.SportModeSortV3.SportModeSortItemV3> getAllAvailableSportInfo() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&\u00a8\u0006\u0006"}, d2 = {"Lcom/noisefit_cf2/handler/CF2UpdateDeviceUnitsHandler$FirmwareUpgradeStatus;", "", "onUpdate", "", "firmware", "Lcom/noisefit_commans/models/DeviceFirmware;", "noisefit_colorfit2_debug"})
    public static abstract interface FirmwareUpgradeStatus {
        
        public abstract void onUpdate(@org.jetbrains.annotations.NotNull
        com.noisefit_commans.models.DeviceFirmware firmware);
    }
}