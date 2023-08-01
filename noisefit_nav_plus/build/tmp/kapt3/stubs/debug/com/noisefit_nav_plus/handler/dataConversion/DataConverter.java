package com.noisefit_nav_plus.handler.dataConversion;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u00e0\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0018\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0013\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001a\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u0016\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u00162\u0006\u0010\u0018\u001a\u00020\u0014H\u0002J\u0006\u0010\u0019\u001a\u00020\u001aJ\u0014\u0010\u001b\u001a\u00020\u001c2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001eJ\u001a\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u001e2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020#0\u0016J\u000e\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'J\u000e\u0010(\u001a\u00020\u00102\u0006\u0010)\u001a\u00020\u0010J\u0014\u0010*\u001a\u00020+2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020,0\u001eJ\u000e\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\u0014J\u0018\u00100\u001a\b\u0012\u0004\u0012\u000202012\b\u00103\u001a\u0004\u0018\u00010\u0010H\u0002J\u000e\u00104\u001a\u00020\u00142\u0006\u00105\u001a\u00020.J\u0010\u00106\u001a\u0002072\b\u00108\u001a\u0004\u0018\u000109J\u000e\u0010:\u001a\u00020;2\u0006\u0010<\u001a\u00020=J\u001e\u0010>\u001a\n ?*\u0004\u0018\u00010\u00100\u00102\f\u0010@\u001a\b\u0012\u0004\u0012\u00020A0\u001eH\u0002J\u001c\u0010B\u001a\b\u0012\u0004\u0012\u00020C0\u001e2\u000e\u0010D\u001a\n\u0012\u0004\u0012\u00020F\u0018\u00010EJ\u000e\u0010G\u001a\u00020H2\u0006\u0010I\u001a\u00020JJ\u0018\u0010K\u001a\b\u0012\u0004\u0012\u00020A0\u001e2\b\u00103\u001a\u0004\u0018\u00010\u0010H\u0002J\u000e\u0010L\u001a\u00020\u001a2\u0006\u0010M\u001a\u00020NJ\u000e\u0010O\u001a\u00020H2\u0006\u0010P\u001a\u00020QJ\u0018\u0010R\u001a\u00020S2\u0006\u0010\u001d\u001a\u00020T2\b\u0010U\u001a\u0004\u0018\u00010VR\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000e\u00a8\u0006W"}, d2 = {"Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;", "", "context", "Landroid/content/Context;", "gson", "Lcom/google/gson/Gson;", "(Landroid/content/Context;Lcom/google/gson/Gson;)V", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "getGson", "()Lcom/google/gson/Gson;", "setGson", "(Lcom/google/gson/Gson;)V", "binary", "", "bytes", "", "radix", "", "binaryStrToBooleanArray", "", "", "my_byte", "dummyHandWashData", "Lcom/noisefit_commans/models/HandWashing;", "formatAlarmData", "Lcom/noisefit_commans/models/AlarmsList;", "p1", "Ljava/util/ArrayList;", "Lcom/zjw/zhbraceletsdk/bean/AlarmInfo;", "formatContactList", "Lcom/noisefit_commans/models/Contact;", "contactBeanList", "Lcom/zjw/zhbraceletsdk/bean/ContactsBean;", "formatMenstrualData", "Lcom/zjw/zhbraceletsdk/bean/MenstrualCycleBean;", "menstrualData", "Lcom/noisefit_commans/models/MenstrualData;", "formatNotificationMessage", "message", "formatReminderData", "Lcom/noisefit_commans/models/ReminderList;", "Lcom/zjw/zhbraceletsdk/bean/EventReminder;", "getBooleanFromInt", "", "cycle", "getGpsMapsData", "Ljava/util/LinkedList;", "Lcom/noisefit_commans/models/GPSDataResponse;", "mapsData", "getIntFromBooleanArray", "bArray", "getSleepData", "Lcom/noisefit_commans/models/SleepData;", "mSleepInfo", "Lcom/zjw/zhbraceletsdk/bean/SleepInfo;", "getStepsData", "Lcom/noisefit_commans/models/StepsData;", "mMotionInfo", "Lcom/zjw/zhbraceletsdk/bean/MotionInfo;", "listToJson", "kotlin.jvm.PlatformType", "value", "", "parseBodyTemperature", "Lcom/noisefit_commans/models/BodyTemperatureBreakup;", "p0", "", "Lcom/zjw/zhbraceletsdk/bean/MeasureTempInfo;", "parseDrinkWater", "Lcom/noisefit_commans/models/SedentaryData;", "drinkInfo", "Lcom/zjw/zhbraceletsdk/bean/DrinkInfo;", "parseGpsMapsData", "parseHandWashData", "handWashingInfo", "Lcom/zjw/zhbraceletsdk/bean/HandWashingInfo;", "parseSedentaryData", "sitInfo", "Lcom/zjw/zhbraceletsdk/bean/SitInfo;", "parseSportsDataGPS", "Lcom/noisefit_commans/models/SportsModeListGPS;", "Lcom/zjw/zhbraceletsdk/bean/SportModleInfo;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "noisefit_nav_plus_debug"})
public final class DataConverter {
    @org.jetbrains.annotations.NotNull
    private android.content.Context context;
    @org.jetbrains.annotations.NotNull
    private com.google.gson.Gson gson;
    
    @javax.inject.Inject
    public DataConverter(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson) {
        super();
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
    public final java.util.ArrayList<com.noisefit_commans.models.BodyTemperatureBreakup> parseBodyTemperature(@org.jetbrains.annotations.Nullable
    java.util.List<com.zjw.zhbraceletsdk.bean.MeasureTempInfo> p0) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.AlarmsList formatAlarmData(@org.jetbrains.annotations.NotNull
    java.util.ArrayList<com.zjw.zhbraceletsdk.bean.AlarmInfo> p1) {
        return null;
    }
    
    private final java.util.List<java.lang.Boolean> binaryStrToBooleanArray(int my_byte) {
        return null;
    }
    
    private final java.lang.String binary(byte[] bytes, int radix) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.StepsData getStepsData(@org.jetbrains.annotations.NotNull
    com.zjw.zhbraceletsdk.bean.MotionInfo mMotionInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SleepData getSleepData(@org.jetbrains.annotations.Nullable
    com.zjw.zhbraceletsdk.bean.SleepInfo mSleepInfo) {
        return null;
    }
    
    private final java.util.ArrayList<double[]> parseGpsMapsData(java.lang.String mapsData) {
        return null;
    }
    
    private final java.util.LinkedList<com.noisefit_commans.models.GPSDataResponse> getGpsMapsData(java.lang.String mapsData) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeListGPS parseSportsDataGPS(@org.jetbrains.annotations.NotNull
    com.zjw.zhbraceletsdk.bean.SportModleInfo p1, @org.jetbrains.annotations.Nullable
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
        return null;
    }
    
    private final java.lang.String listToJson(java.util.ArrayList<double[]> value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.ReminderList formatReminderData(@org.jetbrains.annotations.NotNull
    java.util.ArrayList<com.zjw.zhbraceletsdk.bean.EventReminder> p1) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.ArrayList<com.noisefit_commans.models.Contact> formatContactList(@org.jetbrains.annotations.NotNull
    java.util.List<? extends com.zjw.zhbraceletsdk.bean.ContactsBean> contactBeanList) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.zjw.zhbraceletsdk.bean.MenstrualCycleBean formatMenstrualData(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.MenstrualData menstrualData) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SedentaryData parseSedentaryData(@org.jetbrains.annotations.NotNull
    com.zjw.zhbraceletsdk.bean.SitInfo sitInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SedentaryData parseDrinkWater(@org.jetbrains.annotations.NotNull
    com.zjw.zhbraceletsdk.bean.DrinkInfo drinkInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.HandWashing parseHandWashData(@org.jetbrains.annotations.NotNull
    com.zjw.zhbraceletsdk.bean.HandWashingInfo handWashingInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.HandWashing dummyHandWashData() {
        return null;
    }
    
    public final int getIntFromBooleanArray(@org.jetbrains.annotations.NotNull
    boolean[] bArray) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final boolean[] getBooleanFromInt(int cycle) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String formatNotificationMessage(@org.jetbrains.annotations.NotNull
    java.lang.String message) {
        return null;
    }
}