package com.noisefit.colorfit_pro.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00ce\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u0093\u00012\u00020\u0001:\u0002\u0093\u0001B/\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\b\u0010\u001f\u001a\u00020 H\u0016J\u001b\u0010!\u001a\u00020 \"\u0004\b\u0000\u0010\"2\u0006\u0010#\u001a\u0002H\"H\u0016\u00a2\u0006\u0002\u0010$J\u001b\u0010%\u001a\u00020 \"\u0004\b\u0000\u0010\"2\u0006\u0010#\u001a\u0002H\"H\u0016\u00a2\u0006\u0002\u0010$J\u0010\u0010&\u001a\u00020 2\u0006\u0010&\u001a\u00020\'H\u0016J\u001a\u0010(\u001a\u0004\u0018\u00010)2\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020-H\u0002J\u0018\u0010.\u001a\u00020)2\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020-H\u0002J\b\u0010/\u001a\u000200H\u0002J4\u00101\u001a\u00020 2\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020-2\f\u00102\u001a\b\u0012\u0004\u0012\u00020 032\f\u00104\u001a\b\u0012\u0004\u0012\u00020 03H\u0002J\u0010\u00105\u001a\u00020 2\u0006\u00106\u001a\u000207H\u0016J\"\u00108\u001a\u00020 2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020<2\b\u0010=\u001a\u0004\u0018\u00010>H\u0002J\b\u0010?\u001a\u00020 H\u0016J\u0010\u0010@\u001a\u00020 2\u0006\u0010A\u001a\u000200H\u0016J\u0016\u0010B\u001a\u00020 2\f\u0010C\u001a\b\u0012\u0004\u0012\u00020-0DH\u0016J\u0018\u0010E\u001a\u00020 2\u0006\u0010;\u001a\u00020<2\u0006\u0010F\u001a\u00020GH\u0016J\u0018\u0010H\u001a\u00020 2\u0006\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020>H\u0016J\u0010\u0010I\u001a\u00020 2\u0006\u0010J\u001a\u00020\u000eH\u0016J\u0018\u0010K\u001a\u00020 2\u0006\u0010L\u001a\u00020M2\u0006\u0010N\u001a\u00020OH\u0016J\u0010\u0010P\u001a\u00020 2\u0006\u0010N\u001a\u00020QH\u0016J\b\u0010R\u001a\u00020 H\u0016J\u0010\u0010S\u001a\u00020 2\u0006\u0010T\u001a\u00020UH\u0016J\u0010\u0010V\u001a\u00020 2\u0006\u0010W\u001a\u00020XH\u0016J\u0010\u0010Y\u001a\u00020 2\u0006\u0010Z\u001a\u000200H\u0016J\u0010\u0010[\u001a\u00020 2\u0006\u0010\\\u001a\u00020]H\u0016J\u0010\u0010^\u001a\u00020 2\u0006\u0010_\u001a\u00020`H\u0016J\u0010\u0010a\u001a\u00020 2\u0006\u0010\\\u001a\u00020]H\u0016J\u0010\u0010b\u001a\u00020 2\u0006\u0010c\u001a\u00020GH\u0016J\u0016\u0010d\u001a\u00020 2\f\u0010e\u001a\b\u0012\u0004\u0012\u00020f0DH\u0016J\"\u0010g\u001a\u00020 2\u0006\u0010h\u001a\u00020i2\u0006\u0010j\u001a\u00020k2\b\u0010l\u001a\u0004\u0018\u00010GH\u0016J\u0010\u0010m\u001a\u00020 2\u0006\u0010n\u001a\u00020oH\u0016J\u0010\u0010p\u001a\u00020 2\u0006\u0010q\u001a\u00020rH\u0016J\u0010\u0010s\u001a\u00020 2\u0006\u0010t\u001a\u00020uH\u0016J\u0018\u0010v\u001a\u00020 2\u0006\u0010=\u001a\u00020>2\u0006\u0010;\u001a\u00020<H\u0016J\u001e\u0010w\u001a\u00020 2\f\u0010x\u001a\b\u0012\u0004\u0012\u00020y0D2\u0006\u0010c\u001a\u00020GH\u0016J\u0010\u0010z\u001a\u00020 2\u0006\u0010{\u001a\u00020|H\u0016J\u0010\u0010}\u001a\u00020 2\u0006\u0010~\u001a\u00020\u007fH\u0016J\u001d\u0010\u0080\u0001\u001a\u00020 2\b\u0010\u0081\u0001\u001a\u00030\u0082\u00012\b\u0010\u0083\u0001\u001a\u00030\u0084\u0001H\u0016J\u0013\u0010\u0085\u0001\u001a\u00020 2\b\u0010\u0086\u0001\u001a\u00030\u0087\u0001H\u0016J\u0013\u0010\u0088\u0001\u001a\u00020 2\b\u0010\u0089\u0001\u001a\u00030\u008a\u0001H\u0016J\u0012\u0010\u008b\u0001\u001a\u00020 2\u0007\u0010\u008c\u0001\u001a\u00020GH\u0016J\u0013\u0010\u008d\u0001\u001a\u00020 2\b\u0010\u008e\u0001\u001a\u00030\u008f\u0001H\u0016J\u0013\u0010\u0090\u0001\u001a\u00020 2\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0016R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001e\u00a8\u0006\u0094\u0001"}, d2 = {"Lcom/noisefit/colorfit_pro/handler/ProUpdateDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "dataConverter", "Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "proConnectHandler", "Lcom/noisefit/colorfit_pro/handler/connect/ProConnectHandler;", "proApplicationHandler", "Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;Landroid/content/Context;Lcom/noisefit/colorfit_pro/handler/connect/ProConnectHandler;Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "getContext", "()Landroid/content/Context;", "crpWatchFaceTransListener", "Lcom/crrepa/ble/conn/listener/CRPWatchFaceTransListener;", "getDataConverter", "()Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;", "iUpdateDeviceDataCallback", "Lcom/noisefit_commans/interfaces/device_data/IUpdateDeviceDataCallback;", "mFirmwareUpgradeListener", "Lcom/crrepa/ble/conn/listener/CRPBleFirmwareUpgradeListener;", "getProApplicationHandler", "()Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "getProConnectHandler", "()Lcom/noisefit/colorfit_pro/handler/connect/ProConnectHandler;", "getWatchDataStore", "()Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "findDevice", "Lcom/noisefit_commans/models/SwitchSetting;", "getNameAvatar", "Landroid/graphics/Bitmap;", "crpContactConfig", "Lcom/crrepa/ble/conn/bean/CRPContactConfigInfo;", "contact", "Lcom/noisefit_commans/models/Contact;", "getProfileImage", "getVersionInt", "", "saveAvatar", "success", "Lkotlin/Function0;", "failed", "sendAppNotification", "appNotification", "Lcom/noisefit_commans/models/AppNotification;", "sendWatchFaceBg", "info", "Lcom/crrepa/ble/conn/bean/CRPWatchFaceLayoutInfo;", "imagePath", "Landroid/net/Uri;", "watchFaceLayout", "Lcom/noisefit_commans/models/WatchFaceLayout;", "setCallBacks", "setClearUPIQRCode", "id", "setContactList", "contactList", "", "setCustomBackground", "firmware", "", "setCustomBackgroundWithLayout", "setDevice", "device", "setDeviceDateTime", "calender", "Ljava/util/Calendar;", "units", "Lcom/noisefit_commans/models/TimeFormat;", "setDeviceUnits", "Lcom/noisefit_commans/models/DeviceUnits;", "setFactoryReset", "setHeartRateInterval", "heartRateInterval", "Lcom/noisefit_commans/models/HeartRateInterval;", "setIncomingCallInfo", "incomingCall", "Lcom/noisefit_commans/models/IncomingCall;", "setScreenAwakeInterval", "interval", "setSedentaryData", "sedentaryData", "Lcom/noisefit_commans/models/SedentaryData;", "setSpo2Settings", "data", "Lcom/noisefit_commans/models/Spo2Data;", "setStressData", "setTemperatureUnit", "unit", "setUPIQRCode", "uPIQRCode", "Lcom/noisefit_commans/models/UPIQRCode;", "setUserInfo", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "userGoals", "Lcom/noisefit_commans/models/UserGoals;", "userName", "setVibrationIntensity", "vibrationIntensity", "Lcom/noisefit_commans/models/VibrationIntensity;", "setWalkReminderPro3", "walkReminderData", "Lcom/noisefit_commans/models/WalkReminderData;", "setWatchFace", "watchFace", "Lcom/noisefit_commans/models/WatchFace;", "setWatchFaceLayout", "setWeatherData", "weatherDataList", "Lcom/noisefit_commans/models/WeatherData;", "setWristLiftGesture", "wristLiftGesture", "Lcom/noisefit_commans/models/WristLiftGesture;", "startCameraMode", "status", "", "updateAlarm", "alarm", "Lcom/noisefit_commans/models/AlarmsList;", "alarmAction", "Lcom/noisefit_commans/models/AlarmAction;", "updateCustomReply", "customReplyData", "Lcom/noisefit_commans/models/CustomReplyData;", "updateDND", "doNotDisturb", "Lcom/noisefit_commans/models/DoNotDisturb;", "updateFirmware", "fileUri", "updateLanguage", "language", "Lcom/noisefit_commans/models/Language;", "updateMenstrualData", "menstrualData", "Lcom/noisefit_commans/models/MenstrualData;", "Companion", "noisefit_colorfit_pro_debug"})
public final class ProUpdateDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions {
    @org.jetbrains.annotations.NotNull
    private final com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter = null;
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.noisefit.colorfit_pro.handler.connect.ProConnectHandler proConnectHandler = null;
    @org.jetbrains.annotations.NotNull
    private final com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler = null;
    @org.jetbrains.annotations.NotNull
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback iUpdateDeviceDataCallback;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit.colorfit_pro.handler.ProUpdateDeviceUnitsHandler.Companion Companion = null;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String mobileNumber;
    private final com.crrepa.ble.conn.listener.CRPWatchFaceTransListener crpWatchFaceTransListener = null;
    private final com.crrepa.ble.conn.listener.CRPBleFirmwareUpgradeListener mFirmwareUpgradeListener = null;
    
    @javax.inject.Inject
    public ProUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.handler.connect.ProConnectHandler proConnectHandler, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit.colorfit_pro.dataConversion.DataConverter getDataConverter() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final android.content.Context getContext() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit.colorfit_pro.handler.connect.ProConnectHandler getProConnectHandler() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit.colorfit_pro.base.ProApplicationHandler getProApplicationHandler() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.data.local.abstraction.WatchDataStore getWatchDataStore() {
        return null;
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
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    private final android.graphics.Bitmap getNameAvatar(com.crrepa.ble.conn.bean.CRPContactConfigInfo crpContactConfig, com.noisefit_commans.models.Contact contact) {
        return null;
    }
    
    private final android.graphics.Bitmap getProfileImage(com.crrepa.ble.conn.bean.CRPContactConfigInfo crpContactConfig, com.noisefit_commans.models.Contact contact) {
        return null;
    }
    
    @java.lang.Override
    public void setScreenAwakeInterval(int interval) {
    }
    
    private final void saveAvatar(com.crrepa.ble.conn.bean.CRPContactConfigInfo crpContactConfig, com.noisefit_commans.models.Contact contact, kotlin.jvm.functions.Function0<kotlin.Unit> success, kotlin.jvm.functions.Function0<kotlin.Unit> failed) {
    }
    
    @java.lang.Override
    public void setClearUPIQRCode(int id) {
    }
    
    @java.lang.Override
    public void setUPIQRCode(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.UPIQRCode> uPIQRCode) {
    }
    
    @java.lang.Override
    public void setContactList(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.Contact> contactList) {
    }
    
    @java.lang.Override
    public void setWalkReminderPro3(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WalkReminderData walkReminderData) {
    }
    
    @java.lang.Override
    public void setCallBacks() {
    }
    
    @java.lang.Override
    public void setDeviceUnits(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DeviceUnits units) {
    }
    
    @java.lang.Override
    public void updateMenstrualData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.MenstrualData menstrualData) {
    }
    
    @java.lang.Override
    public void setWeatherData(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.WeatherData> weatherDataList, @org.jetbrains.annotations.NotNull
    java.lang.String unit) {
    }
    
    @java.lang.Override
    public void setUserInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UserInfo userInfo, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.UserGoals userGoals, @org.jetbrains.annotations.Nullable
    java.lang.String userName) {
    }
    
    @java.lang.Override
    public void setStressData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void updateCustomReply(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.CustomReplyData customReplyData) {
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
    public void updateFirmware(@org.jetbrains.annotations.NotNull
    java.lang.String fileUri) {
    }
    
    @java.lang.Override
    public void startCameraMode(boolean status) {
    }
    
    @java.lang.Override
    public void setTemperatureUnit(@org.jetbrains.annotations.NotNull
    java.lang.String unit) {
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
    public void setIncomingCallInfo(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.IncomingCall incomingCall) {
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
    public void setSpo2Settings(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.Spo2Data data) {
    }
    
    @java.lang.Override
    public void setCustomBackgroundWithLayout(@org.jetbrains.annotations.NotNull
    android.net.Uri imagePath, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFaceLayout watchFaceLayout) {
    }
    
    @java.lang.Override
    public void setWatchFaceLayout(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFaceLayout watchFaceLayout, @org.jetbrains.annotations.NotNull
    android.net.Uri imagePath) {
    }
    
    @java.lang.Override
    public void setSedentaryData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void setWatchFace(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    private final void sendWatchFaceBg(com.crrepa.ble.conn.bean.CRPWatchFaceLayoutInfo info, android.net.Uri imagePath, com.noisefit_commans.models.WatchFaceLayout watchFaceLayout) {
    }
    
    @java.lang.Override
    public void setCustomBackground(@org.jetbrains.annotations.NotNull
    android.net.Uri imagePath, @org.jetbrains.annotations.NotNull
    java.lang.String firmware) {
    }
    
    private final int getVersionInt() {
        return 0;
    }
    
    @java.lang.Override
    public void setVibrationIntensity(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.VibrationIntensity vibrationIntensity) {
    }
    
    @java.lang.Override
    public void setFactoryReset() {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/noisefit/colorfit_pro/handler/ProUpdateDeviceUnitsHandler$Companion;", "", "()V", "mobileNumber", "", "getMobileNumber", "()Ljava/lang/String;", "setMobileNumber", "(Ljava/lang/String;)V", "noisefit_colorfit_pro_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getMobileNumber() {
            return null;
        }
        
        public final void setMobileNumber(@org.jetbrains.annotations.Nullable
        java.lang.String p0) {
        }
    }
}