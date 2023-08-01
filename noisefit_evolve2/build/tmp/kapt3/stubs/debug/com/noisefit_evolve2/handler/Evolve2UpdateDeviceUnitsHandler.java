package com.noisefit_evolve2.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u009c\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 u2\u00020\u0001:\u0001uB\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0016J\u001b\u0010\u001a\u001a\u00020\u0017\"\u0004\b\u0000\u0010\u001b2\u0006\u0010\u000b\u001a\u0002H\u001bH\u0016\u00a2\u0006\u0002\u0010\u001cJ\u001b\u0010\u001d\u001a\u00020\u0017\"\u0004\b\u0000\u0010\u001b2\u0006\u0010\u000b\u001a\u0002H\u001bH\u0016\u00a2\u0006\u0002\u0010\u001cJ\u0010\u0010\u001e\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020 H\u0016J\u0010\u0010!\u001a\u00020\u00172\u0006\u0010\"\u001a\u00020#H\u0016J\u0010\u0010$\u001a\u00020\u00172\u0006\u0010$\u001a\u00020%H\u0016J\b\u0010&\u001a\u00020\u0017H\u0002J\u0010\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020*H\u0002J\b\u0010+\u001a\u00020\u0017H\u0016J\u0010\u0010,\u001a\u00020\u00172\u0006\u0010-\u001a\u00020.H\u0016J\u0016\u0010/\u001a\u00020\u00172\f\u00100\u001a\b\u0012\u0004\u0012\u00020201H\u0016J\u0010\u00103\u001a\u00020\u00172\u0006\u00104\u001a\u00020\u000eH\u0016J\u0018\u00105\u001a\u00020\u00172\u0006\u00106\u001a\u0002072\u0006\u00108\u001a\u000209H\u0016J\u0010\u0010:\u001a\u00020\u00172\u0006\u00108\u001a\u00020;H\u0016J\u0010\u0010<\u001a\u00020\u00172\u0006\u00108\u001a\u000209H\u0002J\u0010\u0010=\u001a\u00020\u00172\u0006\u0010>\u001a\u00020?H\u0016J\u0010\u0010@\u001a\u00020\u00172\u0006\u0010A\u001a\u00020BH\u0016J\u0010\u0010C\u001a\u00020\u00172\u0006\u0010D\u001a\u00020EH\u0016J\u0010\u0010F\u001a\u00020\u00172\u0006\u0010G\u001a\u00020HH\u0016J\u0010\u0010I\u001a\u00020\u00172\u0006\u0010J\u001a\u00020KH\u0016J\u0010\u0010L\u001a\u00020\u00172\u0006\u0010M\u001a\u00020%H\u0016J\u0010\u0010N\u001a\u00020\u00172\u0006\u0010A\u001a\u00020BH\u0016J\u0010\u0010O\u001a\u00020\u00172\u0006\u0010P\u001a\u00020*H\u0016J\"\u0010Q\u001a\u00020\u00172\u0006\u0010R\u001a\u00020S2\u0006\u0010T\u001a\u00020U2\b\u0010V\u001a\u0004\u0018\u00010*H\u0016J\u0010\u0010W\u001a\u00020\u00172\u0006\u0010>\u001a\u00020XH\u0016J\u001e\u0010Y\u001a\u00020\u00172\f\u0010Z\u001a\b\u0012\u0004\u0012\u00020[012\u0006\u0010P\u001a\u00020*H\u0016J\u0010\u0010\\\u001a\u00020\u00172\u0006\u0010]\u001a\u00020%H\u0016J\u0010\u0010^\u001a\u00020\u00172\u0006\u0010_\u001a\u00020`H\u0016J\u0010\u0010a\u001a\u00020\u00172\u0006\u0010b\u001a\u00020cH\u0016J\u0018\u0010d\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020 2\u0006\u0010e\u001a\u00020fH\u0016J\u0010\u0010g\u001a\u00020\u00172\u0006\u0010h\u001a\u00020iH\u0016J\u0010\u0010j\u001a\u00020\u00172\u0006\u0010k\u001a\u00020lH\u0016J\u0010\u0010m\u001a\u00020\u00172\u0006\u0010n\u001a\u00020*H\u0016J\u0010\u0010o\u001a\u00020\u00172\u0006\u0010p\u001a\u00020qH\u0016J\u0010\u0010r\u001a\u00020\u00172\u0006\u0010s\u001a\u00020tH\u0016R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006v"}, d2 = {"Lcom/noisefit_evolve2/handler/Evolve2UpdateDeviceUnitsHandler;", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "dataConverter", "Lcom/noisefit_evolve2/dataConversion/DataConverter;", "context", "Landroid/content/Context;", "evolve2ApplicationHandler", "Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_evolve2/dataConversion/DataConverter;Landroid/content/Context;Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "callback", "Lcom/touchgui/sdk/TGDialManager$OnSyncDialListener;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "mClient", "Lcom/touchgui/sdk/TGClient;", "syncDialProcessListener", "testUpdateDeviceDataCallback", "Lcom/noisefit_commans/interfaces/device_data/IUpdateDeviceDataCallback;", "updateDeviceDataCallbacks", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataCallbacks;", "addReminder", "", "reminder", "Lcom/noisefit_commans/models/ReminderList$Reminder;", "callbackListener", "T", "(Ljava/lang/Object;)V", "callbackListenerNew", "deleteAlarm", "alarm", "Lcom/noisefit_commans/models/AlarmsList;", "deleteReminders", "reminderList", "Lcom/noisefit_commans/models/ReminderList;", "findDevice", "Lcom/noisefit_commans/models/SwitchSetting;", "findPhoneListener", "getWeatherType", "", "weatherType", "", "init", "sendAppNotification", "appNotification", "Lcom/noisefit_commans/models/AppNotification;", "setContactList", "contactList", "", "Lcom/noisefit_commans/models/Contact;", "setDevice", "device", "setDeviceDateTime", "calender", "Ljava/util/Calendar;", "units", "Lcom/noisefit_commans/models/TimeFormat;", "setDeviceUnits", "Lcom/noisefit_commans/models/DeviceUnits;", "setDeviceUnitsNew", "setDiyWatchFaceCustom", "watchFace", "Lcom/noisefit_commans/models/DiyCustomWatchFace;", "setDrinkWaterReminder", "sedentaryData", "Lcom/noisefit_commans/models/SedentaryData;", "setHeartRateAlert", "heartRateAlert", "Lcom/noisefit_commans/models/HeartRateAlert;", "setHeartRateInterval", "heartRateInterval", "Lcom/noisefit_commans/models/HeartRateInterval;", "setIncomingCallInfo", "incomingCall", "Lcom/noisefit_commans/models/IncomingCall;", "setMusicSwitch", "musicSwitch", "setSedentaryData", "setTemperatureUnit", "unit", "setUserInfo", "userInfo", "Lcom/noisefit_commans/models/UserInfo;", "userGoals", "Lcom/noisefit_commans/models/UserGoals;", "userName", "setWatchFace", "Lcom/noisefit_commans/models/WatchFace;", "setWeatherData", "weatherDataList", "Lcom/noisefit_commans/models/WeatherData;", "setWeatherSwitch", "switchSetting", "setWristLiftGesture", "wristLiftGesture", "Lcom/noisefit_commans/models/WristLiftGesture;", "startCameraMode", "status", "", "updateAlarm", "alarmAction", "Lcom/noisefit_commans/models/AlarmAction;", "updateCustomReply", "customReplyData", "Lcom/noisefit_commans/models/CustomReplyData;", "updateDND", "doNotDisturb", "Lcom/noisefit_commans/models/DoNotDisturb;", "updateFirmware", "fileUri", "updateLanguage", "language", "Lcom/noisefit_commans/models/Language;", "updateMenstrualData", "menstrualData", "Lcom/noisefit_commans/models/MenstrualData;", "Companion", "noisefit_evolve2_debug"})
public final class Evolve2UpdateDeviceUnitsHandler extends com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions {
    private com.noisefit_evolve2.dataConversion.DataConverter dataConverter;
    private android.content.Context context;
    private com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler;
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_evolve2.handler.Evolve2UpdateDeviceUnitsHandler.Companion Companion = null;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String mobileNumber;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String lastMapsNotification;
    private com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallbacks updateDeviceDataCallbacks;
    private com.touchgui.sdk.TGClient mClient;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private com.noisefit_commans.interfaces.device_data.IUpdateDeviceDataCallback testUpdateDeviceDataCallback;
    private final com.touchgui.sdk.TGDialManager.OnSyncDialListener syncDialProcessListener = null;
    private final com.touchgui.sdk.TGDialManager.OnSyncDialListener callback = null;
    
    @javax.inject.Inject
    public Evolve2UpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
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
    public void setMusicSwitch(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting musicSwitch) {
    }
    
    @java.lang.Override
    public void setDeviceUnits(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DeviceUnits units) {
    }
    
    private final void setDeviceUnitsNew(com.noisefit_commans.models.TimeFormat units) {
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
    
    private final void findPhoneListener() {
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
    
    @java.lang.Override
    public void updateFirmware(@org.jetbrains.annotations.NotNull
    java.lang.String fileUri) {
    }
    
    @java.lang.Override
    public void setContactList(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.Contact> contactList) {
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
    public void setWeatherSwitch(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SwitchSetting switchSetting) {
    }
    
    @java.lang.Override
    public void setWeatherData(@org.jetbrains.annotations.NotNull
    java.util.List<com.noisefit_commans.models.WeatherData> weatherDataList, @org.jetbrains.annotations.NotNull
    java.lang.String unit) {
    }
    
    private final int getWeatherType(java.lang.String weatherType) {
        return 0;
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
    public void setHeartRateAlert(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.HeartRateAlert heartRateAlert) {
    }
    
    @java.lang.Override
    public void setDrinkWaterReminder(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SedentaryData sedentaryData) {
    }
    
    @java.lang.Override
    public void updateCustomReply(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.CustomReplyData customReplyData) {
    }
    
    @java.lang.Override
    public void setTemperatureUnit(@org.jetbrains.annotations.NotNull
    java.lang.String unit) {
    }
    
    @java.lang.Override
    public void setDiyWatchFaceCustom(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DiyCustomWatchFace watchFace) {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001c\u0010\t\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\b\u00a8\u0006\f"}, d2 = {"Lcom/noisefit_evolve2/handler/Evolve2UpdateDeviceUnitsHandler$Companion;", "", "()V", "lastMapsNotification", "", "getLastMapsNotification", "()Ljava/lang/String;", "setLastMapsNotification", "(Ljava/lang/String;)V", "mobileNumber", "getMobileNumber", "setMobileNumber", "noisefit_evolve2_debug"})
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
        
        @org.jetbrains.annotations.Nullable
        public final java.lang.String getLastMapsNotification() {
            return null;
        }
        
        public final void setLastMapsNotification(@org.jetbrains.annotations.Nullable
        java.lang.String p0) {
        }
    }
}