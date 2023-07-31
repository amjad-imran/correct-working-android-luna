package com.noisefit_ryeex_sdk.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\r\u001a\u00020\u000eH\u0016J\u001b\u0010\u000f\u001a\u00020\u000e\"\u0004\b\u0000\u0010\u00102\u0006\u0010\u0011\u001a\u0002H\u0010H\u0016\u00a2\u0006\u0002\u0010\u0012J\u001b\u0010\u0013\u001a\u00020\u000e\"\u0004\b\u0000\u0010\u00102\u0006\u0010\u0011\u001a\u0002H\u0010H\u0016\u00a2\u0006\u0002\u0010\u0012J\b\u0010\u0014\u001a\u00020\u000eH\u0016J\b\u0010\u0015\u001a\u00020\u000eH\u0016J\b\u0010\u0016\u001a\u00020\u000eH\u0016J\b\u0010\u0017\u001a\u00020\u000eH\u0016J\u0010\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\u0010\u0010\u001b\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\b\u0010\u001c\u001a\u00020\u000eH\u0016J\b\u0010\u001d\u001a\u00020\u000eH\u0016J\u0010\u0010\u001e\u001a\u00020\u000e2\u0006\u0010\u001f\u001a\u00020\bH\u0016J\b\u0010 \u001a\u00020\u000eH\u0002J\u0010\u0010!\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\b\u0010\"\u001a\u00020\u000eH\u0002J\u0018\u0010\"\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010#\u001a\u00020$H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/noisefit_ryeex_sdk/handler/RyeexUserActivityHandler;", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "dataConverter", "Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;", "ryeexApplicationHandler", "Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;", "(Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;)V", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "userActivityDataCallbacks", "Lcom/noisefit_commans/interfaces/data/IUserActivityDataCallback;", "watchDevice", "Lcom/ryeex/watch/adapter/device/WatchDevice;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "getBloodOxygenLevel", "getBloodPressure", "getBodyTemperatureData", "getHeartRate", "getSleepData", "date", "", "getStepsData", "getStressCount", "init", "setDevice", "colorFitDevice", "syncRealTimeActivity", "syncSportsActivity", "syncUserActivity", "isRefresh", "", "noisefit_ryeex_sdk_debug"})
public final class RyeexUserActivityHandler extends com.noisefit_commans.interfaces.data.UserActivityDataActions {
    private com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter;
    private com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler;
    private com.noisefit_commans.interfaces.data.IUserActivityDataCallback userActivityDataCallbacks;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private com.ryeex.watch.adapter.device.WatchDevice watchDevice;
    
    @javax.inject.Inject
    public RyeexUserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler) {
        super();
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void syncUserActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date, boolean isRefresh) {
    }
    
    @java.lang.Override
    public void syncSportsActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    private final void syncUserActivity() {
    }
    
    private final void syncRealTimeActivity() {
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
    public void getHeartRate() {
    }
    
    @java.lang.Override
    public void getBloodOxygenLevel() {
    }
    
    @java.lang.Override
    public void getBloodPressure() {
    }
    
    @java.lang.Override
    public void getStressCount() {
    }
    
    @java.lang.Override
    public void getBodyTemperatureData() {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
}