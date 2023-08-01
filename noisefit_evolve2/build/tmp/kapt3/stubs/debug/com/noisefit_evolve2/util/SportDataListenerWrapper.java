package com.noisefit_evolve2.util;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u001b2\u00020\u0001:\u0003\u001a\u001b\u001cB\u000f\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0004J\u0012\u0010\b\u001a\u0004\u0018\u00010\u00072\u0006\u0010\t\u001a\u00020\nH\u0002J\b\u0010\u000b\u001a\u00020\fH\u0016J\u0018\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0016J\u0010\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0016J\u0010\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0016H\u0016J\b\u0010\u0017\u001a\u00020\fH\u0016J\u0010\u0010\u0018\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u0019H\u0016R\u0010\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/noisefit_evolve2/util/SportDataListenerWrapper;", "Lcom/touchgui/sdk/TGSimpleSportDataListener;", "callback", "Lcom/noisefit_evolve2/util/SportDataListenerWrapper$Callback;", "(Lcom/noisefit_evolve2/util/SportDataListenerWrapper$Callback;)V", "records", "", "Lcom/noisefit_evolve2/util/SportDataListenerWrapper$SportRecordMerge;", "getSportRecord", "date", "Ljava/util/Date;", "onCompleted", "", "onError", "code", "", "message", "", "onGpsData", "data", "Lcom/touchgui/sdk/bean/TGSyncGps;", "onSportRecord", "Lcom/touchgui/sdk/bean/TGSportRecord;", "onStart", "onSwimData", "Lcom/touchgui/sdk/bean/TGSyncSwim;", "Callback", "Companion", "SportRecordMerge", "noisefit_evolve2_debug"})
public final class SportDataListenerWrapper extends com.touchgui.sdk.TGSimpleSportDataListener {
    private final com.noisefit_evolve2.util.SportDataListenerWrapper.Callback callback = null;
    private final java.util.List<com.noisefit_evolve2.util.SportDataListenerWrapper.SportRecordMerge> records = null;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_evolve2.util.SportDataListenerWrapper.Companion Companion = null;
    
    public SportDataListenerWrapper(@org.jetbrains.annotations.Nullable
    com.noisefit_evolve2.util.SportDataListenerWrapper.Callback callback) {
        super();
    }
    
    @java.lang.Override
    public void onStart() {
    }
    
    @java.lang.Override
    public void onSportRecord(@org.jetbrains.annotations.NotNull
    com.touchgui.sdk.bean.TGSportRecord data) {
    }
    
    @java.lang.Override
    public void onGpsData(@org.jetbrains.annotations.NotNull
    com.touchgui.sdk.bean.TGSyncGps data) {
    }
    
    @java.lang.Override
    public void onSwimData(@org.jetbrains.annotations.NotNull
    com.touchgui.sdk.bean.TGSyncSwim data) {
    }
    
    @java.lang.Override
    public void onCompleted() {
    }
    
    @java.lang.Override
    public void onError(int code, @org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    private final com.noisefit_evolve2.util.SportDataListenerWrapper.SportRecordMerge getSportRecord(java.util.Date date) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u0004R\u001c\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013\u00a8\u0006\u0014"}, d2 = {"Lcom/noisefit_evolve2/util/SportDataListenerWrapper$SportRecordMerge;", "", "record", "Lcom/touchgui/sdk/bean/TGSportRecord;", "(Lcom/touchgui/sdk/bean/TGSportRecord;)V", "gpsData", "Lcom/touchgui/sdk/bean/TGSyncGps;", "getGpsData", "()Lcom/touchgui/sdk/bean/TGSyncGps;", "setGpsData", "(Lcom/touchgui/sdk/bean/TGSyncGps;)V", "getRecord", "()Lcom/touchgui/sdk/bean/TGSportRecord;", "setRecord", "swimData", "Lcom/touchgui/sdk/bean/TGSyncSwim;", "getSwimData", "()Lcom/touchgui/sdk/bean/TGSyncSwim;", "setSwimData", "(Lcom/touchgui/sdk/bean/TGSyncSwim;)V", "noisefit_evolve2_debug"})
    public static final class SportRecordMerge {
        @org.jetbrains.annotations.NotNull
        private com.touchgui.sdk.bean.TGSportRecord record;
        @org.jetbrains.annotations.Nullable
        private com.touchgui.sdk.bean.TGSyncGps gpsData;
        @org.jetbrains.annotations.Nullable
        private com.touchgui.sdk.bean.TGSyncSwim swimData;
        
        public SportRecordMerge(@org.jetbrains.annotations.NotNull
        com.touchgui.sdk.bean.TGSportRecord record) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.touchgui.sdk.bean.TGSportRecord getRecord() {
            return null;
        }
        
        public final void setRecord(@org.jetbrains.annotations.NotNull
        com.touchgui.sdk.bean.TGSportRecord p0) {
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.touchgui.sdk.bean.TGSyncGps getGpsData() {
            return null;
        }
        
        public final void setGpsData(@org.jetbrains.annotations.Nullable
        com.touchgui.sdk.bean.TGSyncGps p0) {
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.touchgui.sdk.bean.TGSyncSwim getSwimData() {
            return null;
        }
        
        public final void setSwimData(@org.jetbrains.annotations.Nullable
        com.touchgui.sdk.bean.TGSyncSwim p0) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H&J\u0018\u0010\b\u001a\u00020\u00032\u000e\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\nH&\u00a8\u0006\f"}, d2 = {"Lcom/noisefit_evolve2/util/SportDataListenerWrapper$Callback;", "", "onError", "", "code", "", "message", "", "onSyncSportData", "records", "", "Lcom/noisefit_evolve2/util/SportDataListenerWrapper$SportRecordMerge;", "noisefit_evolve2_debug"})
    public static abstract interface Callback {
        
        public abstract void onSyncSportData(@org.jetbrains.annotations.Nullable
        java.util.List<com.noisefit_evolve2.util.SportDataListenerWrapper.SportRecordMerge> records);
        
        public abstract void onError(int code, @org.jetbrains.annotations.NotNull
        java.lang.String message);
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u00a8\u0006\u0007"}, d2 = {"Lcom/noisefit_evolve2/util/SportDataListenerWrapper$Companion;", "", "()V", "formatDate", "", "date", "Ljava/util/Date;", "noisefit_evolve2_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        private final java.lang.String formatDate(java.util.Date date) {
            return null;
        }
    }
}