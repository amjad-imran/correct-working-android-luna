package com.noisefit.hybrid.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00d6\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0010\u0011\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001BG\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!H\u0016J\u0010\u0010\"\u001a\u00020\u001f2\u0006\u0010#\u001a\u00020$H\u0002J\b\u0010%\u001a\u00020\u001fH\u0016J\u001b\u0010&\u001a\u00020\u001f\"\u0004\b\u0000\u0010\'2\u0006\u0010(\u001a\u0002H\'H\u0016\u00a2\u0006\u0002\u0010)J\u001b\u0010*\u001a\u00020\u001f\"\u0004\b\u0000\u0010\'2\u0006\u0010(\u001a\u0002H\'H\u0016\u00a2\u0006\u0002\u0010)J\u0010\u0010+\u001a\u00020\u001f2\u0006\u0010,\u001a\u00020-H\u0016J\u0010\u0010.\u001a\u00020\u001f2\u0006\u0010/\u001a\u000200H\u0016J\u0010\u00101\u001a\u00020\u001f2\u0006\u00101\u001a\u000202H\u0016J\u0010\u00103\u001a\u0002042\u0006\u00105\u001a\u00020\u0018H\u0002J \u00106\u001a\u0002042\u0006\u00107\u001a\u0002042\u0006\u00108\u001a\u0002042\u0006\u00109\u001a\u000204H\u0002J\u001f\u0010:\u001a\u00020\u001f2\u0010\u0010;\u001a\f\u0012\u0006\b\u0001\u0012\u00020=\u0018\u00010<H\u0002\u00a2\u0006\u0002\u0010>J\u001f\u0010?\u001a\u00020\u001f2\u0010\u0010@\u001a\f\u0012\u0006\b\u0001\u0012\u00020=\u0018\u00010<H\u0002\u00a2\u0006\u0002\u0010>J\'\u0010A\u001a\u00020\u001f2\u0006\u0010B\u001a\u00020C2\u0010\u0010;\u001a\f\u0012\u0006\b\u0001\u0012\u00020=\u0018\u00010<H\u0002\u00a2\u0006\u0002\u0010DJ\u0010\u0010E\u001a\u00020\u001f2\u0006\u0010F\u001a\u00020GH\u0016J\b\u0010H\u001a\u00020\u001fH\u0002J\u0010\u0010I\u001a\u00020\u001f2\u0006\u0010J\u001a\u00020KH\u0016J\u0010\u0010L\u001a\u00020\u001f2\u0006\u0010M\u001a\u00020\u0014H\u0016J\u0018\u0010N\u001a\u00020\u001f2\u0006\u0010O\u001a\u00020P2\u0006\u0010Q\u001a\u00020RH\u0016J\u0010\u0010S\u001a\u00020\u001f2\u0006\u0010Q\u001a\u00020TH\u0016J\u0010\u0010U\u001a\u00020\u001f2\u0006\u0010V\u001a\u00020WH\u0016J\b\u0010X\u001a\u00020\u001fH\u0016J\u0010\u0010Y\u001a\u00020\u001f2\u0006\u0010Z\u001a\u00020[H\u0016J\u0010\u0010\\\u001a\u00020\u001f2\u0006\u0010]\u001a\u00020^H\u0016J\u0010\u0010_\u001a\u00020\u001f2\u0006\u0010`\u001a\u00020aH\u0016J\u0010\u0010b\u001a\u00020\u001f2\u0006\u0010c\u001a\u00020dH\u0016J\b\u0010e\u001a\u00020\u001fH\u0016J\u0010\u0010f\u001a\u00020\u001f2\u0006\u0010g\u001a\u000204H\u0016J\u0010\u0010h\u001a\u00020\u001f2\u0006\u0010V\u001a\u00020WH\u0016J\u0010\u0010i\u001a\u00020\u001f2\u0006\u0010V\u001a\u00020WH\u0016J\u0010\u0010j\u001a\u00020\u001f2\u0006\u0010k\u001a\u000202H\u0016J\u0010\u0010l\u001a\u00020\u001f2\u0006\u0010m\u001a\u00020\u0018H\u0016J\"\u0010n\u001a\u00020\u001f2\u0006\u0010o\u001a\u00020p2\u0006\u0010q\u001a\u00020r2\b\u0010s\u001a\u0004\u0018\u00010\u0018H\u0016J\u0010\u0010t\u001a\u00020\u001f2\u0006\u0010u\u001a\u00020vH\u0016J\u0010\u0010w\u001a\u00020\u001f2\u0006\u0010u\u001a\u00020xH\u0017J\u0010\u0010y\u001a\u00020\u001f2\u0006\u0010u\u001a\u00020zH\u0016J\u0010\u0010{\u001a\u00020\u001f2\u0006\u0010|\u001a\u00020}H\u0016J \u0010~\u001a\u00020\u001f2\u000e\u0010\u007f\u001a\n\u0012\u0005\u0012\u00030\u0081\u00010\u0080\u00012\u0006\u0010m\u001a\u00020\u0018H\u0016J\u0011\u0010\u0082\u0001\u001a\u00020\u001f2\u0006\u0010#\u001a\u00020$H\u0016J\u0013\u0010\u0083\u0001\u001a\u00020\u001f2\b\u0010\u0084\u0001\u001a\u00030\u0085\u0001H\u0016J\u0011\u0010\u0086\u0001\u001a\u00020\u001f2\u0006\u0010B\u001a\u00020CH\u0016J\u001d\u0010\u0087\u0001\u001a\u00020\u001f2\b\u0010\u0088\u0001\u001a\u00030\u0089\u00012\b\u0010\u008a\u0001\u001a\u00030\u0089\u0001H\u0016J\u001b\u0010\u008b\u0001\u001a\u00020\u001f2\u0006\u0010,\u001a\u00020-2\b\u0010\u008c\u0001\u001a\u00030\u008d\u0001H\u0016J\u0011\u0010\u008e\u0001\u001a\u00020\u001f2\u0006\u0010u\u001a\u00020vH\u0002J\u0013\u0010\u008f\u0001\u001a\u00020\u001f2\b\u0010\u0090\u0001\u001a\u00030\u0091\u0001H\u0016J\u0013\u0010\u0092\u0001\u001a\u00020\u001f2\b\u0010\u0093\u0001\u001a\u00030\u0094\u0001H\u0016J\u0012\u0010\u0095\u0001\u001a\u00020\u001f2\u0007\u0010\u0096\u0001\u001a\u00020\u0018H\u0016J\u0013\u0010\u0097\u0001\u001a\u00020\u001f2\b\u0010\u0098\u0001\u001a\u00030\u0099\u0001H\u0016J\u001a\u0010\u009a\u0001\u001a\u00020\u001f2\u000f\u0010\u009b\u0001\u001a\n\u0012\u0005\u0012\u00030\u009c\u00010\u0080\u0001H\u0016R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u009d\u0001"}, d2 = {"Lcom/noisefit/hybrid/handler/NFHUpdateDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "context", "Landroid/content/Context;", "bitwiseUtils", "Lcom/noisefit/hybrid/utils/BitwiseUtils;", "dataConverter", "Lcom/noisefit/hybrid/dataconversions/DataConverter;", "bitwiseHelperUtils", "Lcom/noisefit/hybrid/utils/BitwiseHelperUtils;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "onlineWatchFacesVision", "Lcom/noisefit/hybrid/utils/OnlineWatchFacesVision;", "bluetoothsdkExp", "Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp;", "visionCommands", "Lcom/noisefit/hybrid/base/VisionCommands;", "(Landroid/content/Context;Lcom/noisefit/hybrid/utils/BitwiseUtils;Lcom/noisefit/hybrid/dataconversions/DataConverter;Lcom/noisefit/hybrid/utils/BitwiseHelperUtils;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;Lcom/noisefit/hybrid/utils/OnlineWatchFacesVision;Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp;Lcom/noisefit/hybrid/base/VisionCommands;)V", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "customizeWatchFaceCallBack", "Lcn/appscomm/bluetoothsdk/interfaces/ResultCallBack;", "mobileNumber", "", "resultCallBack", "resultCallBackNew", "testUpdateDeviceDataCallback", "Lcom/noisefit_commans/interfaces/device_data/IUpdateDeviceDataCallback;", "updateCallBack", "addReminder", "", "reminder", "Lcom/noisefit_commans/models/ReminderList$Reminder;", "addWorldClock", "data", "Lcom/noisefit_commans/models/WorldClocksPushData;", "attachCallbacks", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "deleteAlarm", "alarm", "Lcom/noisefit_commans/models/AlarmsList;", "deleteReminders", "reminders", "Lcom/noisefit_commans/models/ReminderList;", "findDevice", "Lcom/noisefit_commans/models/SwitchSetting;", "fromHexToRgba", "", "hexString", "fromRgbToRgba", "r", "g", "b", "onDeviceSMSReply", "p1", "", "", "([Ljava/lang/Object;)V", "onDeviceSMSReplyDefault", "objects", "onSwitchSettingUpdated", "status", "", "(Z[Ljava/lang/Object;)V", "sendAppNotification", "appNotification", "Lcom/noisefit_commans/models/AppNotification;", "set8002CallbackNull", "setAutoSleep", "autoSleep", "Lcom/noisefit_commans/models/AutoSleep;", "setDevice", "device", "setDeviceDateTime", "calender", "Ljava/util/Calendar;", "units", "Lcom/noisefit_commans/models/TimeFormat;", "setDeviceUnits", "Lcom/noisefit_commans/models/DeviceUnits;", "setDrinkWaterReminder", "sedentaryData", "Lcom/noisefit_commans/models/SedentaryData;", "setFactoryReset", "setHandWashing", "handWashing", "Lcom/noisefit_commans/models/HandWashing;", "setHeartRateAlert", "heartRateAlert", "Lcom/noisefit_commans/models/HeartRateAlert;", "setHeartRateInterval", "heartRateInterval", "Lcom/noisefit_commans/models/HeartRateInterval;", "setIncomingCallInfo", "incomingCall", "Lcom/noisefit_commans/models/IncomingCall;", "setRestartDevice", "setScreenAwakeInterval", "interval", "setSedentaryData", "setStressData", "setSwitchSetting", "switchSetting", "setTemperatureUnit", "unit", "setUserInfo", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "userGoals", "Lcom/noisefit_commans/models/UserGoals;", "userName", "setWatchFace", "watchFace", "Lcom/noisefit_commans/models/WatchFace;", "setWatchFaceCustom", "Lcom/noisefit_commans/models/CustomWatchFace;", "setWatchFaceCustomHybrid", "Lcom/noisefit_commans/models/WatchFacesCustomHybrid;", "setWatchPassword", "watchPassword", "Lcom/noisefit_commans/models/WatchPassword;", "setWeatherData", "weatherDataList", "", "Lcom/noisefit_commans/models/WeatherData;", "setWorldClock", "setWristLiftGesture", "wristLiftGesture", "Lcom/noisefit_commans/models/WristLiftGesture;", "startCameraMode", "updateAPGSData", "data1", "Landroid/net/Uri;", "data2", "updateAlarm", "alarmAction", "Lcom/noisefit_commans/models/AlarmAction;", "updateCloudImage", "updateCustomReply", "customReplyData", "Lcom/noisefit_commans/models/CustomReplyData;", "updateDND", "doNotDisturb", "Lcom/noisefit_commans/models/DoNotDisturb;", "updateFirmware", "fileUri", "updateLanguage", "language", "Lcom/noisefit_commans/models/Language;", "visionUpdateFirmware", "visionOtaFiles", "Lcom/noisefit_commans/models/VisionOtaFiles;", "noisefit_hybrid_debug"})
public final class NFHUpdateDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions {
    private final android.content.Context context = null;
    private final com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils = null;
    private final com.noisefit.hybrid.dataconversions.DataConverter dataConverter = null;
    private final com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils = null;
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private final com.noisefit.hybrid.utils.OnlineWatchFacesVision onlineWatchFacesVision = null;
    private final com.noisefit.hybrid.utils.BluetoothSDK_Exp bluetoothsdkExp = null;
    private final com.noisefit.hybrid.base.VisionCommands visionCommands = null;
    private com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback testUpdateDeviceDataCallback;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private java.lang.String mobileNumber;
    private final cn.appscomm.bluetoothsdk.interfaces.ResultCallBack customizeWatchFaceCallBack = null;
    private cn.appscomm.bluetoothsdk.interfaces.ResultCallBack updateCallBack;
    private cn.appscomm.bluetoothsdk.interfaces.ResultCallBack resultCallBack;
    private cn.appscomm.bluetoothsdk.interfaces.ResultCallBack resultCallBackNew;
    
    @javax.inject.Inject
    public NFHUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.dataconversions.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.OnlineWatchFacesVision onlineWatchFacesVision, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BluetoothSDK_Exp bluetoothsdkExp, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.VisionCommands visionCommands) {
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
    com.noisefit_commans.models.ColorFitDevice device) {
    }
    
    private final void set8002CallbackNull() {
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
    
    private final int fromHexToRgba(java.lang.String hexString) {
        return 0;
    }
    
    private final int fromRgbToRgba(int r, int g, int b) {
        return 0;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    @java.lang.Override
    public void setWatchFaceCustom(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.CustomWatchFace watchFace) {
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
    public void deleteAlarm(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.AlarmsList alarm) {
    }
    
    @java.lang.Override
    public void deleteReminders(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ReminderList reminders) {
    }
    
    @java.lang.Override
    public void addReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ReminderList.Reminder reminder) {
    }
    
    @java.lang.Override
    public void setWorldClock(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WorldClocksPushData data) {
    }
    
    private final void addWorldClock(com.noisefit_commans.models.WorldClocksPushData data) {
    }
    
    @java.lang.Override
    public void updateFirmware(@org.jetbrains.annotations.NotNull
    java.lang.String fileUri) {
    }
    
    @java.lang.Override
    public void setStressData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void updateLanguage(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.Language language) {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void findDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting findDevice) {
    }
    
    @java.lang.Override
    public void setWatchFace(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    @java.lang.Override
    public void setWatchFaceCustomHybrid(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchFacesCustomHybrid watchFace) {
    }
    
    @java.lang.Override
    public void setWatchPassword(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.WatchPassword watchPassword) {
    }
    
    private final void updateCloudImage(com.noisefit_commans.models.WatchFace watchFace) {
    }
    
    @java.lang.Override
    public void visionUpdateFirmware(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.VisionOtaFiles> visionOtaFiles) {
    }
    
    @java.lang.Override
    public void startCameraMode(boolean status) {
    }
    
    @java.lang.Override
    public void updateCustomReply(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.CustomReplyData customReplyData) {
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
    public void setTemperatureUnit(@org.jetbrains.annotations.NotNull
    java.lang.String unit) {
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
    
    private final void onDeviceSMSReplyDefault(java.lang.Object[] objects) {
    }
    
    private final void onDeviceSMSReply(java.lang.Object[] p1) {
    }
    
    @java.lang.Override
    public void setScreenAwakeInterval(int interval) {
    }
    
    private final void onSwitchSettingUpdated(boolean status, java.lang.Object[] p1) {
    }
    
    @java.lang.Override
    public void setDrinkWaterReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
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
}