package com.noisefit_evolve2.dataConversion;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u009a\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0013\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u001a\u0010\u0013\u001a\u00020\u00102\b\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0016\u001a\u00020\u0012H\u0002J\u0016\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00190\u00182\u0006\u0010\u001a\u001a\u00020\u0012H\u0002J\u0016\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00190\u00182\u0006\u0010\u001a\u001a\u00020\u0012H\u0002J\u0016\u0010\u001c\u001a\u00020\u00122\u000e\u0010\u001d\u001a\n\u0012\u0004\u0012\u00020\u0019\u0018\u00010\u0018J\u0014\u0010\u001e\u001a\u00020\u001f2\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\"0!J\u0010\u0010#\u001a\u00020\u00102\u0006\u0010$\u001a\u00020%H\u0002J\u0010\u0010&\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u0006\u0010\'\u001a\u00020\u0012J\u000e\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020+J\u000e\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020/J\u000e\u00100\u001a\u0002012\u0006\u00102\u001a\u000203J \u00104\u001a\b\u0012\u0004\u0012\u0002050!2\u0010\u00106\u001a\f\u0012\u0006\b\u0001\u0012\u000208\u0018\u000107H\u0002J\u000e\u00109\u001a\u0002012\u0006\u00102\u001a\u00020:J\u0014\u0010;\u001a\u00020<2\f\u0010=\u001a\b\u0012\u0004\u0012\u00020>07R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000e\u00a8\u0006?"}, d2 = {"Lcom/noisefit_evolve2/dataConversion/DataConverter;", "", "context", "Landroid/content/Context;", "gson", "Lcom/google/gson/Gson;", "(Landroid/content/Context;Lcom/google/gson/Gson;)V", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "getGson", "()Lcom/google/gson/Gson;", "setGson", "(Lcom/google/gson/Gson;)V", "appendPrefixToTime", "", "time", "", "binary", "bytes", "", "radix", "binstrToBooleanArrayAlarm", "", "", "my_byte", "binstrToBooleanArrayReminder", "booleanArrayToBinaryString", "booleanList", "formatAlarmData", "Lcom/noisefit_commans/models/AlarmsList;", "p1", "Ljava/util/ArrayList;", "Lcom/touchgui/sdk/bean/TGAlarm;", "getCalorieFormat", "value", "", "getFormattedTime", "getOffset", "getSleepData", "Lcom/noisefit_commans/models/SleepData;", "mSleepInfo", "Lcom/touchgui/sdk/bean/TGSleepData;", "getStepsData", "Lcom/noisefit_commans/models/StepsData;", "mMotionInfo", "Lcom/touchgui/sdk/bean/TGStepData;", "parseDrinkWater", "Lcom/noisefit_commans/models/SedentaryData;", "longSit", "Lcom/touchgui/sdk/bean/TGRemindDrinking;", "parseGpsMapsData", "", "tgSyncGps", "", "Lcom/touchgui/sdk/bean/TGWorkoutRecord$Gps;", "parseSedentaryData", "Lcom/touchgui/sdk/bean/TGSedentaryConfig;", "parseSportsDataGPS", "Lcom/noisefit_commans/models/SportsModeListGPS;", "sportList", "Lcom/touchgui/sdk/bean/TGWorkoutRecord;", "noisefit_evolve2_debug"})
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
    public final com.noisefit_commans.models.AlarmsList formatAlarmData(@org.jetbrains.annotations.NotNull
    java.util.ArrayList<com.touchgui.sdk.bean.TGAlarm> p1) {
        return null;
    }
    
    public final int booleanArrayToBinaryString(@org.jetbrains.annotations.Nullable
    java.util.List<java.lang.Boolean> booleanList) {
        return 0;
    }
    
    private final java.util.List<java.lang.Boolean> binstrToBooleanArrayAlarm(int my_byte) {
        return null;
    }
    
    private final java.util.List<java.lang.Boolean> binstrToBooleanArrayReminder(int my_byte) {
        return null;
    }
    
    private final java.lang.String binary(byte[] bytes, int radix) {
        return null;
    }
    
    private final java.lang.String getCalorieFormat(float value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.StepsData getStepsData(@org.jetbrains.annotations.NotNull
    com.touchgui.sdk.bean.TGStepData mMotionInfo) {
        return null;
    }
    
    private final java.lang.String getFormattedTime(int time) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SleepData getSleepData(@org.jetbrains.annotations.NotNull
    com.touchgui.sdk.bean.TGSleepData mSleepInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String appendPrefixToTime(int time) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeListGPS parseSportsDataGPS(@org.jetbrains.annotations.NotNull
    java.util.List<com.touchgui.sdk.bean.TGWorkoutRecord> sportList) {
        return null;
    }
    
    private final java.util.ArrayList<double[]> parseGpsMapsData(java.util.List<? extends com.touchgui.sdk.bean.TGWorkoutRecord.Gps> tgSyncGps) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SedentaryData parseSedentaryData(@org.jetbrains.annotations.NotNull
    com.touchgui.sdk.bean.TGSedentaryConfig longSit) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SedentaryData parseDrinkWater(@org.jetbrains.annotations.NotNull
    com.touchgui.sdk.bean.TGRemindDrinking longSit) {
        return null;
    }
    
    public final int getOffset() {
        return 0;
    }
}