package com.noisefit.hybrid.dataconversions;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0092\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0015\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\u0010\u0013\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0002J\u0016\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u0018\u0010\f\u001a\u00020\u000b2\u000e\u0010\r\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\bH\u0002J\u001d\u0010\u000e\u001a\u00020\u000f2\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u0010\u0012J\u001d\u0010\u0013\u001a\u00020\u000f2\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u0010\u0012J\u001d\u0010\u0014\u001a\u00020\u00152\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u0010\u0016J\u001d\u0010\u0017\u001a\u00020\u00152\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u0010\u0016J\u001d\u0010\u0018\u001a\u00020\u00152\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u0010\u0016J\u000e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\n\u001a\u00020\u000bJ\u0010\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J\u000e\u0010\u001f\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020\u000bJ\u001d\u0010!\u001a\u00020\"2\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u0010#J\u001d\u0010$\u001a\u00020%2\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u0010&J\u0018\u0010\'\u001a\u0014\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0(J\u0010\u0010)\u001a\u00020\u000b2\b\u0010*\u001a\u0004\u0018\u00010\u0004J\u001a\u0010+\u001a\u00020\t2\u0006\u0010,\u001a\u00020\u001c2\b\u0010-\u001a\u0004\u0018\u00010\u001cH\u0002J\u0010\u0010+\u001a\u00020\t2\u0006\u0010\u001d\u001a\u00020\u001eH\u0002J(\u0010.\u001a\n /*\u0004\u0018\u00010\u00040\u00042\u0016\u00100\u001a\u0012\u0012\u0004\u0012\u00020201j\b\u0012\u0004\u0012\u000202`3H\u0002J(\u00104\u001a\u0012\u0012\u0004\u0012\u00020201j\b\u0012\u0004\u0012\u000202`32\u000e\u00105\u001a\n\u0012\u0004\u0012\u000206\u0018\u00010\bH\u0002J\u001d\u00107\u001a\u0002082\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u0011\u00a2\u0006\u0002\u00109J1\u0010:\u001a\u00020;2\u0010\u0010\u0010\u001a\f\u0012\u0006\b\u0001\u0012\u00020\u0001\u0018\u00010\u00112\u0012\u0010<\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002060\b0\b\u00a2\u0006\u0002\u0010=J\u000e\u0010>\u001a\u00020\u000b2\u0006\u0010\n\u001a\u00020\u000b\u00a8\u0006?"}, d2 = {"Lcom/noisefit/hybrid/dataconversions/DataConverter;", "", "()V", "binary", "", "bytes", "", "binstrToBooleanArray", "", "", "my_byte", "", "booleanArrayToBinStr", "list", "formatAlarmData", "Lcom/noisefit_commans/models/AlarmsList;", "p1", "", "([Ljava/lang/Object;)Lcom/noisefit_commans/models/AlarmsList;", "formatNavAlarmData", "formatNavReminderData", "Lcom/noisefit_commans/models/ReminderList;", "([Ljava/lang/Object;)Lcom/noisefit_commans/models/ReminderList;", "formatReminderData", "formatVisionReminderData", "getArrayFromInt", "", "getCalenderTime", "Ljava/util/Calendar;", "timeStamp", "", "getParseDays", "data", "getSleepData", "Lcom/noisefit_commans/models/SleepData;", "([Ljava/lang/Object;)Lcom/noisefit_commans/models/SleepData;", "getStepsData", "Lcom/noisefit_commans/models/StepsData;", "([Ljava/lang/Object;)Lcom/noisefit_commans/models/StepsData;", "getTimeZoneOffset", "Lkotlin/Triple;", "getWeatherCode", "weatherType", "isSelectedDate", "selectedDate", "stepDate", "listToJson", "kotlin.jvm.PlatformType", "value", "Ljava/util/ArrayList;", "", "Lkotlin/collections/ArrayList;", "parseGpsMapsData", "mapsData", "Lcom/noisefit_commans/models/GPSDataResponse;", "parseSportsData", "Lcom/noisefit_commans/models/SportsModeRequestList;", "([Ljava/lang/Object;)Lcom/noisefit_commans/models/SportsModeRequestList;", "parseSportsDataGPS", "Lcom/noisefit_commans/models/SportsModeListGPS;", "p2", "([Ljava/lang/Object;Ljava/util/List;)Lcom/noisefit_commans/models/SportsModeListGPS;", "toParseDay", "noisefit_hybrid_debug"})
public final class DataConverter {
    
    @javax.inject.Inject
    public DataConverter() {
        super();
    }
    
    public final int getParseDays(int data) {
        return 0;
    }
    
    private final int booleanArrayToBinStr(java.util.List<java.lang.Boolean> list) {
        return 0;
    }
    
    public final int getWeatherCode(@org.jetbrains.annotations.Nullable
    java.lang.String weatherType) {
        return 0;
    }
    
    public final int toParseDay(int my_byte) {
        return 0;
    }
    
    private final java.util.List<java.lang.Boolean> binstrToBooleanArray(int my_byte) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.AlarmsList formatAlarmData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.AlarmsList formatNavAlarmData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.ReminderList formatReminderData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.ReminderList formatNavReminderData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.ReminderList formatVisionReminderData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.StepsData getStepsData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    private final java.util.Calendar getCalenderTime(long timeStamp) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeRequestList parseSportsData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    private final java.util.ArrayList<double[]> parseGpsMapsData(java.util.List<com.noisefit_commans.models.GPSDataResponse> mapsData) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeListGPS parseSportsDataGPS(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1, @org.jetbrains.annotations.NotNull
    java.util.List<? extends java.util.List<com.noisefit_commans.models.GPSDataResponse>> p2) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SleepData getSleepData(@org.jetbrains.annotations.Nullable
    java.lang.Object[] p1) {
        return null;
    }
    
    private final boolean isSelectedDate(java.util.Calendar selectedDate, java.util.Calendar stepDate) {
        return false;
    }
    
    private final boolean isSelectedDate(long timeStamp) {
        return false;
    }
    
    private final java.lang.String listToJson(java.util.ArrayList<double[]> value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final int[] getArrayFromInt(int my_byte) {
        return null;
    }
    
    private final java.lang.String binary(byte[] bytes) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlin.Triple<java.lang.Integer, java.lang.Integer, java.lang.Integer> getTimeZoneOffset() {
        return null;
    }
}