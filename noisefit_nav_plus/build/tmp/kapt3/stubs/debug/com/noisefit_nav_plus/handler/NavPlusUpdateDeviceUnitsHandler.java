package com.noisefit_nav_plus.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00e8\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B/\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0010\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u000200H\u0016J\b\u00101\u001a\u00020.H\u0016J\u0018\u00102\u001a\u0002032\u000e\u00104\u001a\n\u0012\u0004\u0012\u000206\u0018\u000105H\u0002J\u001b\u00107\u001a\u00020.\"\u0004\b\u0000\u001082\u0006\u00109\u001a\u0002H8H\u0016\u00a2\u0006\u0002\u0010:J\u001b\u0010;\u001a\u00020.\"\u0004\b\u0000\u001082\u0006\u00109\u001a\u0002H8H\u0016\u00a2\u0006\u0002\u0010:J\u0010\u0010<\u001a\u00020.2\u0006\u0010=\u001a\u000206H\u0016J\u0010\u0010>\u001a\u00020.2\u0006\u0010?\u001a\u00020@H\u0016J\u0010\u0010A\u001a\u00020.2\u0006\u0010B\u001a\u00020CH\u0016J\u0010\u0010D\u001a\u00020.2\u0006\u0010E\u001a\u00020 H\u0016J\u0010\u0010F\u001a\u00020.2\u0006\u0010F\u001a\u00020GH\u0016J\b\u0010H\u001a\u00020.H\u0016J\b\u0010I\u001a\u00020.H\u0002J\b\u0010J\u001a\u00020.H\u0002J\b\u0010K\u001a\u00020.H\u0002J\u0010\u0010L\u001a\u00020.2\u0006\u0010M\u001a\u00020NH\u0016J\'\u0010O\u001a\u00020.2\u0006\u0010=\u001a\u0002062\u0010\u0010P\u001a\f\u0012\u0006\b\u0001\u0012\u00020R\u0018\u00010QH\u0002\u00a2\u0006\u0002\u0010SJ\u0010\u0010T\u001a\u00020.2\u0006\u0010U\u001a\u00020VH\u0016J\u0010\u0010W\u001a\u00020.2\b\u0010X\u001a\u0004\u0018\u00010YJ\u0010\u0010Z\u001a\u00020.2\u0006\u0010[\u001a\u00020\\H\u0016J\u0010\u0010]\u001a\u00020.2\u0006\u0010^\u001a\u00020_H\u0016J\u0016\u0010`\u001a\u00020.2\f\u0010a\u001a\b\u0012\u0004\u0012\u00020b05H\u0016J\u0010\u0010c\u001a\u00020.2\u0006\u0010d\u001a\u00020\u000eH\u0016J\u0018\u0010e\u001a\u00020.2\u0006\u0010f\u001a\u00020g2\u0006\u0010h\u001a\u00020iH\u0016J\u0010\u0010j\u001a\u00020.2\u0006\u0010h\u001a\u00020iH\u0002J\u0010\u0010k\u001a\u00020.2\u0006\u0010h\u001a\u00020lH\u0016J\u0010\u0010m\u001a\u00020.2\u0006\u0010M\u001a\u00020nH\u0016J\u0010\u0010o\u001a\u00020.2\u0006\u0010p\u001a\u00020qH\u0016J\b\u0010r\u001a\u00020.H\u0016J\u0010\u0010s\u001a\u00020.2\u0006\u0010t\u001a\u00020uH\u0016J\u0010\u0010v\u001a\u00020.2\u0006\u0010w\u001a\u00020xH\u0016J\u0010\u0010y\u001a\u00020.2\u0006\u0010z\u001a\u00020{H\u0016J\u0010\u0010|\u001a\u00020.2\u0006\u0010}\u001a\u00020~H\u0016J\u0010\u0010\u007f\u001a\u00020.2\u0006\u0010p\u001a\u00020qH\u0016J\u0011\u0010\u0080\u0001\u001a\u00020.2\u0006\u0010p\u001a\u00020qH\u0016J\u0011\u0010\u0081\u0001\u001a\u00020.2\u0006\u0010=\u001a\u000206H\u0016J\t\u0010\u0082\u0001\u001a\u00020.H\u0016J\u0012\u0010\u0083\u0001\u001a\u00020.2\u0007\u0010\u0084\u0001\u001a\u000203H\u0016J\u0011\u0010\u0085\u0001\u001a\u00020.2\u0006\u0010p\u001a\u00020qH\u0016J\u0012\u0010\u0086\u0001\u001a\u00020.2\u0007\u0010\u0087\u0001\u001a\u00020GH\u0016J\u0011\u0010\u0088\u0001\u001a\u00020.2\u0006\u0010^\u001a\u00020 H\u0016J(\u0010\u0089\u0001\u001a\u00020.2\b\u0010\u008a\u0001\u001a\u00030\u008b\u00012\b\u0010\u008c\u0001\u001a\u00030\u008d\u00012\t\u0010\u008e\u0001\u001a\u0004\u0018\u00010 H\u0016J\u0011\u0010\u008f\u0001\u001a\u00020.2\u0006\u0010M\u001a\u00020NH\u0016J!\u0010\u0090\u0001\u001a\u00020.2\u000e\u0010\u0091\u0001\u001a\t\u0012\u0005\u0012\u00030\u0092\u0001052\u0006\u0010^\u001a\u00020 H\u0016J\u0012\u0010\u0093\u0001\u001a\u00020.2\u0007\u0010X\u001a\u00030\u0094\u0001H\u0016J\u0013\u0010\u0095\u0001\u001a\u00020.2\b\u0010\u0096\u0001\u001a\u00030\u0097\u0001H\u0016J\u0011\u0010\u0098\u0001\u001a\u00020.2\u0006\u0010=\u001a\u000206H\u0016J\u0013\u0010\u0099\u0001\u001a\u00020.2\b\u0010\u009a\u0001\u001a\u00030\u009b\u0001H\u0016J\u001d\u0010\u009c\u0001\u001a\u00020.2\b\u0010\u009d\u0001\u001a\u00030\u009e\u00012\b\u0010\u009f\u0001\u001a\u00030\u009e\u0001H\u0016J\u001b\u0010\u00a0\u0001\u001a\u00020.2\u0006\u0010?\u001a\u00020@2\b\u0010\u00a1\u0001\u001a\u00030\u00a2\u0001H\u0016J\u0013\u0010\u00a3\u0001\u001a\u00020.2\b\u0010\u00a4\u0001\u001a\u00030\u00a5\u0001H\u0016J\u0013\u0010\u00a6\u0001\u001a\u00020.2\b\u0010\u00a7\u0001\u001a\u00030\u00a8\u0001H\u0016J\u0012\u0010\u00a9\u0001\u001a\u00020.2\u0007\u0010\u00aa\u0001\u001a\u00020 H\u0016J\u0013\u0010\u00ab\u0001\u001a\u00020.2\b\u0010\u00ac\u0001\u001a\u00030\u00ad\u0001H\u0016J\u0013\u0010\u00ae\u0001\u001a\u00020.2\b\u0010\u00af\u0001\u001a\u00030\u00b0\u0001H\u0016J\u000f\u0010\u00b1\u0001\u001a\u00020.2\u0006\u0010X\u001a\u00020YR\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0014\"\u0004\b\u0015\u0010\u0016R\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001aR\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u001eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001f\u001a\u0004\u0018\u00010 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u00020\tX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\"\"\u0004\b#\u0010$R\u0010\u0010%\u001a\u0004\u0018\u00010&X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\'\u001a\u0004\u0018\u00010(X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b)\u0010*\"\u0004\b+\u0010,\u00a8\u0006\u00b2\u0001"}, d2 = {"Lcom/noisefit_nav_plus/handler/NavPlusUpdateDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "dataConverter", "Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "gson", "Lcom/google/gson/Gson;", "navPlusApplicationHandler", "Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;Landroid/content/Context;Lcom/google/gson/Gson;Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "getDataConverter", "()Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;", "setDataConverter", "(Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;)V", "getGson", "()Lcom/google/gson/Gson;", "setGson", "(Lcom/google/gson/Gson;)V", "mBleService", "Lcom/zjw/zhbraceletsdk/service/ZhBraceletService;", "mPerformerListener", "Lcom/zjw/zhbraceletsdk/linstener/SimplePerformerListener;", "mobileNumber", "", "getNavPlusApplicationHandler", "()Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "setNavPlusApplicationHandler", "(Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;)V", "testUpdateDeviceDataCallback", "Lcom/noisefit_commans/interfaces/device_data/IUpdateDeviceDataCallback;", "updateDeviceDataCallbacks", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataCallbacks;", "getWatchDataStore", "()Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "setWatchDataStore", "(Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "addReminder", "", "reminder", "Lcom/noisefit_commans/models/ReminderList$Reminder;", "attachCallbacks", "booleanArrayToBinaryString", "", "booleanList", "", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "closeFindPhoneFromWatch", "status", "deleteAlarm", "alarm", "Lcom/noisefit_commans/models/AlarmsList;", "deleteReminders", "reminderList", "Lcom/noisefit_commans/models/ReminderList;", "deleteStock", "symbol", "findDevice", "Lcom/noisefit_commans/models/SwitchSetting;", "init", "initDeviceSendMuteListener", "initQuickEyeListener", "initSingleQuickReplyListener", "onDownloadedWatchFaceContents", "watchFace", "Lcom/noisefit_commans/models/WatchFace;", "onSwitchSettingUpdated", "p1", "", "", "(Z[Ljava/lang/Object;)V", "sendAppNotification", "appNotification", "Lcom/noisefit_commans/models/AppNotification;", "sendWatchData", "data", "", "setAutoSleep", "autoSleep", "Lcom/noisefit_commans/models/AutoSleep;", "setBodyTemperatureUnit", "unit", "Lcom/noisefit_commans/models/Units;", "setContactList", "contactList", "Lcom/noisefit_commans/models/Contact;", "setDevice", "device", "setDeviceDateTime", "calender", "Ljava/util/Calendar;", "units", "Lcom/noisefit_commans/models/TimeFormat;", "setDeviceTime", "setDeviceUnits", "Lcom/noisefit_commans/models/DeviceUnits;", "setDiyWatchFaceCustom", "Lcom/noisefit_commans/models/DiyCustomWatchFace;", "setDrinkWaterReminder", "sedentaryData", "Lcom/noisefit_commans/models/SedentaryData;", "setFactoryReset", "setHandWashing", "handWashing", "Lcom/noisefit_commans/models/HandWashing;", "setHeartRateAlert", "heartRateAlert", "Lcom/noisefit_commans/models/HeartRateAlert;", "setHeartRateInterval", "heartRateInterval", "Lcom/noisefit_commans/models/HeartRateInterval;", "setIncomingCallInfo", "incomingCall", "Lcom/noisefit_commans/models/IncomingCall;", "setMealReminder", "setMedicineReminder", "setQuickEyeMovementSwitch", "setRestartDevice", "setScreenAwakeInterval", "interval", "setSedentaryData", "setSwitchSetting", "switchSetting", "setTemperatureUnit", "setUserInfo", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "userGoals", "Lcom/noisefit_commans/models/UserGoals;", "userName", "setWatchFace", "setWeatherData", "weatherDataList", "Lcom/noisefit_commans/models/WeatherData;", "setWorldClock", "Lcom/noisefit_commans/models/WorldClocksPushData;", "setWristLiftGesture", "wristLiftGesture", "Lcom/noisefit_commans/models/WristLiftGesture;", "startCameraMode", "syncStockInfoList", "stockInfoList", "Lcom/noisefit_commans/models/StockInfoList;", "updateAPGSData", "data1", "Landroid/net/Uri;", "data2", "updateAlarm", "alarmAction", "Lcom/noisefit_commans/models/AlarmAction;", "updateCustomReply", "customReplyData", "Lcom/noisefit_commans/models/CustomReplyData;", "updateDND", "doNotDisturb", "Lcom/noisefit_commans/models/DoNotDisturb;", "updateFirmware", "fileUri", "updateLanguage", "language", "Lcom/noisefit_commans/models/Language;", "updateMenstrualData", "menstrualData", "Lcom/noisefit_commans/models/MenstrualData;", "uploadWatch", "noisefit_nav_plus_debug"})
public final class NavPlusUpdateDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions {
    @org.jetbrains.annotations.NotNull
    private com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter;
    @org.jetbrains.annotations.NotNull
    private android.content.Context context;
    @org.jetbrains.annotations.NotNull
    private com.google.gson.Gson gson;
    @org.jetbrains.annotations.NotNull
    private com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler;
    @org.jetbrains.annotations.NotNull
    private com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore;
    private com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallbacks updateDeviceDataCallbacks;
    private java.lang.String mobileNumber;
    private com.zjw.zhbraceletsdk.service.ZhBraceletService mBleService;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback testUpdateDeviceDataCallback;
    private final com.zjw.zhbraceletsdk.linstener.SimplePerformerListener mPerformerListener = null;
    
    @javax.inject.Inject
    public NavPlusUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson, @org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
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
    public final com.noisefit_nav_plus.base.NavPlusApplicationHandler getNavPlusApplicationHandler() {
        return null;
    }
    
    public final void setNavPlusApplicationHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler p0) {
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
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice device) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void setBodyTemperatureUnit(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.Units unit) {
    }
    
    @java.lang.Override
    public void syncStockInfoList(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.StockInfoList stockInfoList) {
    }
    
    @java.lang.Override
    public void deleteStock(@org.jetbrains.annotations.NotNull
    java.lang.String symbol) {
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
    public void updateAlarm(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AlarmsList alarm, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AlarmAction alarmAction) {
    }
    
    private final int booleanArrayToBinaryString(java.util.List<java.lang.Boolean> booleanList) {
        return 0;
    }
    
    @java.lang.Override
    public void setContactList(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.Contact> contactList) {
    }
    
    @java.lang.Override
    public void deleteAlarm(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AlarmsList alarm) {
    }
    
    @java.lang.Override
    public void deleteReminders(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ReminderList reminderList) {
    }
    
    @java.lang.Override
    public void updateMenstrualData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.MenstrualData menstrualData) {
    }
    
    @java.lang.Override
    public void addReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ReminderList.Reminder reminder) {
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
    
    private final void setDeviceTime(com.noisefit_commans.models.TimeFormat units) {
    }
    
    /**
     * 0=Ready, 1=Busy, 2=Already up to date, 3=Insufficient memory, 4=Low battery, 5=Failed
     */
    @java.lang.Override
    public void onDownloadedWatchFaceContents(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    @java.lang.Override
    public void setWatchFace(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    @java.lang.Override
    public void setDiyWatchFaceCustom(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DiyCustomWatchFace watchFace) {
    }
    
    public final void uploadWatch(@org.jetbrains.annotations.NotNull
    byte[] data) {
    }
    
    public final void sendWatchData(@org.jetbrains.annotations.Nullable
    byte[] data) {
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
    public void setWeatherData(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.WeatherData> weatherDataList, @org.jetbrains.annotations.NotNull
    java.lang.String unit) {
    }
    
    @java.lang.Override
    public void closeFindPhoneFromWatch(boolean status) {
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
    public void setSedentaryData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void setAutoSleep(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AutoSleep autoSleep) {
    }
    
    @java.lang.Override
    public void setSwitchSetting(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting switchSetting) {
    }
    
    @java.lang.Override
    public void setScreenAwakeInterval(int interval) {
    }
    
    private final void onSwitchSettingUpdated(boolean status, java.lang.Object[] p1) {
    }
    
    @java.lang.Override
    public void setHandWashing(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.HandWashing handWashing) {
    }
    
    @java.lang.Override
    public void updateAPGSData(@org.jetbrains.annotations.NotNull
    android.net.Uri data1, @org.jetbrains.annotations.NotNull
    android.net.Uri data2) {
    }
    
    @java.lang.Override
    public void setHeartRateAlert(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.HeartRateAlert heartRateAlert) {
    }
    
    @java.lang.Override
    public void setFactoryReset() {
    }
    
    @java.lang.Override
    public void setRestartDevice() {
    }
    
    @java.lang.Override
    public void setDrinkWaterReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void setMedicineReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void setMealReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void updateCustomReply(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.CustomReplyData customReplyData) {
    }
    
    private final void initSingleQuickReplyListener() {
    }
    
    private final void initDeviceSendMuteListener() {
    }
    
    @java.lang.Override
    public void setWorldClock(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WorldClocksPushData data) {
    }
    
    @java.lang.Override
    public void setTemperatureUnit(@org.jetbrains.annotations.NotNull
    java.lang.String unit) {
    }
    
    @java.lang.Override
    public void setQuickEyeMovementSwitch(boolean status) {
    }
    
    private final void initQuickEyeListener() {
    }
}