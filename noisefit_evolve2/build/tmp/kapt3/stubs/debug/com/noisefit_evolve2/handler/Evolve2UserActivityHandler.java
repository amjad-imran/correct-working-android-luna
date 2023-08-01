package com.noisefit_evolve2.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0014\u001a\u00020\u0015H\u0016J\u001b\u0010\u0016\u001a\u00020\u0015\"\u0004\b\u0000\u0010\u00172\u0006\u0010\u0018\u001a\u0002H\u0017H\u0016\u00a2\u0006\u0002\u0010\u0019J\u001b\u0010\u001a\u001a\u00020\u0015\"\u0004\b\u0000\u0010\u00172\u0006\u0010\u0018\u001a\u0002H\u0017H\u0016\u00a2\u0006\u0002\u0010\u0019J\b\u0010\u001b\u001a\u00020\u0015H\u0016J\b\u0010\u001c\u001a\u00020\u0015H\u0016J\b\u0010\u001d\u001a\u00020\u0015H\u0016J\u0010\u0010\u001e\u001a\u00020\u00152\u0006\u0010\u001f\u001a\u00020 H\u0016J\b\u0010!\u001a\u00020\u0015H\u0016J\u0010\u0010\"\u001a\u00020\u00152\u0006\u0010#\u001a\u00020$H\u0016J\u0010\u0010%\u001a\u00020\u00152\u0006\u0010#\u001a\u00020$H\u0016J\b\u0010&\u001a\u00020\u0015H\u0016J\b\u0010\'\u001a\u00020\u0015H\u0016J\u0010\u0010(\u001a\u00020\u00152\u0006\u0010)\u001a\u00020*H\u0002J\b\u0010+\u001a\u00020\u0015H\u0016J\u0010\u0010,\u001a\u00020\u00152\u0006\u0010\u0007\u001a\u00020\bH\u0016J\u0010\u0010-\u001a\u00020\u00152\u0006\u0010#\u001a\u00020$H\u0016J\u0018\u0010.\u001a\u00020\u00152\u0006\u0010#\u001a\u00020$2\u0006\u0010/\u001a\u00020\fH\u0016R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00060"}, d2 = {"Lcom/noisefit_evolve2/handler/Evolve2UserActivityHandler;", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "dataConverter", "Lcom/noisefit_evolve2/dataConversion/DataConverter;", "evolve2ApplicationHandler", "Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;", "(Lcom/noisefit_evolve2/dataConversion/DataConverter;Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;)V", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "healthDataListener", "Lcom/touchgui/sdk/TGHealthDataManager$OnHealthDataListener;", "isSyncProtoSportSyncing", "", "isSyncProtoSportToday", "mClient", "Lcom/touchgui/sdk/TGClient;", "sportDataListener", "Lcom/touchgui/sdk/TGWorkoutDataCallback;", "userActivityDataCallbacks", "Lcom/noisefit_commans/interfaces/data/IUserActivityDataCallback;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "getBloodOxygenLevel", "getBloodPressure", "getBodyTemperatureData", "getHeartHistory", "calendar", "Ljava/util/Calendar;", "getHeartRate", "getSleepData", "date", "", "getStepsData", "getStressCount", "init", "onHeartHistoryObtained", "mWoHeartInfo", "Lcom/touchgui/sdk/bean/TGHeartRateData;", "removeCallbacks", "setDevice", "syncSportsActivity", "syncUserActivity", "isRefresh", "noisefit_evolve2_debug"})
public final class Evolve2UserActivityHandler extends com.noisefit_commans.interfaces.data.UserActivityDataActions {
    private com.noisefit_evolve2.dataConversion.DataConverter dataConverter;
    private com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private com.noisefit_commans.interfaces.data.IUserActivityDataCallback userActivityDataCallbacks;
    private com.touchgui.sdk.TGClient mClient;
    private boolean isSyncProtoSportToday = false;
    private boolean isSyncProtoSportSyncing = false;
    private com.touchgui.sdk.TGWorkoutDataCallback sportDataListener;
    private final com.touchgui.sdk.TGHealthDataManager.OnHealthDataListener healthDataListener = null;
    
    @javax.inject.Inject
    public Evolve2UserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler) {
        super();
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void getBodyTemperatureData() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void syncUserActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date, boolean isRefresh) {
    }
    
    @java.lang.Override
    public void getStepsData(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    @java.lang.Override
    public void getSleepData(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    @java.lang.Override
    public void getStressCount() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    @java.lang.Override
    public void getHeartRate() {
    }
    
    @java.lang.Override
    public void getBloodOxygenLevel() {
    }
    
    @java.lang.Override
    public void getBloodPressure() {
    }
    
    @java.lang.Override
    public void syncSportsActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public void getHeartHistory(@org.jetbrains.annotations.NotNull
    java.util.Calendar calendar) {
    }
    
    private final void onHeartHistoryObtained(com.touchgui.sdk.bean.TGHeartRateData mWoHeartInfo) {
    }
}