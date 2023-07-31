package com.noisefit.hybrid.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010\u0011\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010\u000f\u001a\u00020\u0010H\u0016J\u001b\u0010\u0011\u001a\u00020\u0010\"\u0004\b\u0000\u0010\u00122\u0006\u0010\u0013\u001a\u0002H\u0012H\u0016\u00a2\u0006\u0002\u0010\u0014J\u001b\u0010\u0015\u001a\u00020\u0010\"\u0004\b\u0000\u0010\u00122\u0006\u0010\u0013\u001a\u0002H\u0012H\u0016\u00a2\u0006\u0002\u0010\u0014J\b\u0010\u0016\u001a\u00020\u0010H\u0016J\b\u0010\u0017\u001a\u00020\u0010H\u0016J\b\u0010\u0018\u001a\u00020\u0010H\u0016J\u0010\u0010\u0019\u001a\u00020\u00102\u0006\u0010\u001a\u001a\u00020\u001bH\u0016J\b\u0010\u001c\u001a\u00020\u0010H\u0016J\b\u0010\u001d\u001a\u00020\u0010H\u0002J\u0010\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u001fH\u0016J\b\u0010 \u001a\u00020\u0010H\u0002J\u0010\u0010!\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u001fH\u0016J\u0010\u0010\"\u001a\u00020\u00102\u0006\u0010#\u001a\u00020$H\u0002J\b\u0010%\u001a\u00020\u0010H\u0016J\b\u0010&\u001a\u00020\u0010H\u0002J\b\u0010\'\u001a\u00020\u0010H\u0016J\b\u0010(\u001a\u00020\u0010H\u0002J\u001f\u0010)\u001a\u00020\u00102\u0010\u0010*\u001a\f\u0012\u0006\b\u0001\u0012\u00020,\u0018\u00010+H\u0002\u00a2\u0006\u0002\u0010-J\u001f\u0010.\u001a\u00020\u00102\u0010\u0010*\u001a\f\u0012\u0006\b\u0001\u0012\u00020,\u0018\u00010+H\u0002\u00a2\u0006\u0002\u0010-J\u001f\u0010/\u001a\u00020\u00102\u0010\u0010*\u001a\f\u0012\u0006\b\u0001\u0012\u00020,\u0018\u00010+H\u0002\u00a2\u0006\u0002\u0010-J3\u00100\u001a\u00020\u00102\u0010\u0010*\u001a\f\u0012\u0006\b\u0001\u0012\u00020,\u0018\u00010+2\u0012\u00101\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002030202H\u0002\u00a2\u0006\u0002\u00104J\u001f\u00105\u001a\u00020\u00102\u0010\u0010*\u001a\f\u0012\u0006\b\u0001\u0012\u00020,\u0018\u00010+H\u0002\u00a2\u0006\u0002\u0010-J\b\u00106\u001a\u00020\u0010H\u0002J\u0010\u00107\u001a\u00020\u00102\u0006\u0010\t\u001a\u00020\nH\u0016J\u0010\u00108\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u001fH\u0016J\u0018\u00109\u001a\u00020\u00102\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010:\u001a\u00020;H\u0016R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006<"}, d2 = {"Lcom/noisefit/hybrid/handler/NFHUserActivityHandler;", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "nfhApplicationHandler", "Lcom/noisefit/hybrid/base/NFHybridApplicationHandler;", "dataConverter", "Lcom/noisefit/hybrid/dataconversions/DataConverter;", "visionHelperMethods", "Lcom/noisefit/hybrid/utils/VisionHelperMethods;", "(Lcom/noisefit/hybrid/base/NFHybridApplicationHandler;Lcom/noisefit/hybrid/dataconversions/DataConverter;Lcom/noisefit/hybrid/utils/VisionHelperMethods;)V", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "resultCallBack", "Lcn/appscomm/bluetoothsdk/interfaces/ResultCallBack;", "userActivityDataCallbacks", "Lcom/noisefit_commans/interfaces/data/IUserActivityDataCallback;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "getBloodOxygenLevel", "getBloodPressure", "getBodyTemperatureData", "getHeartHistory", "calendar", "Ljava/util/Calendar;", "getHeartRate", "getSleepData", "date", "", "getSleepDataForVision", "getStepsData", "getStressAndBloodData", "bloodOxygenCount", "", "getStressCount", "getVisionHeartRate", "init", "onDeleteSportsData", "onHeartHistoryObtained", "p1", "", "", "([Ljava/lang/Object;)V", "onSleepDataObtained", "onSportsDataObtained", "onSportsDataObtainedGPS", "p2", "", "Lcom/noisefit_commans/models/GPSDataResponse;", "([Ljava/lang/Object;Ljava/util/List;)V", "onStepsDataObtained", "sendStartSyncStatus", "setDevice", "syncSportsActivity", "syncUserActivity", "isRefresh", "", "noisefit_hybrid_debug"})
public final class NFHUserActivityHandler extends com.noisefit_commans.interfaces.data.UserActivityDataActions {
    private com.noisefit.hybrid.base.NFHybridApplicationHandler nfhApplicationHandler;
    private final com.noisefit.hybrid.dataconversions.DataConverter dataConverter = null;
    private final com.noisefit.hybrid.utils.VisionHelperMethods visionHelperMethods = null;
    private com.noisefit_commans.interfaces.data.IUserActivityDataCallback userActivityDataCallbacks;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private cn.appscomm.bluetoothsdk.interfaces.ResultCallBack resultCallBack;
    
    @javax.inject.Inject
    public NFHUserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.NFHybridApplicationHandler nfhApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.dataconversions.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.VisionHelperMethods visionHelperMethods) {
        super();
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    private final void sendStartSyncStatus() {
    }
    
    @java.lang.Override
    public void syncUserActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date, boolean isRefresh) {
    }
    
    @java.lang.Override
    public void getBodyTemperatureData() {
    }
    
    @java.lang.Override
    public void getStepsData(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    @java.lang.Override
    public void getSleepData(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    private final void getSleepDataForVision() {
    }
    
    @java.lang.Override
    public void getStressCount() {
    }
    
    private final void getStressAndBloodData(int bloodOxygenCount) {
    }
    
    private final void getSleepData() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public void getHeartRate() {
    }
    
    private final void getVisionHeartRate() {
    }
    
    @java.lang.Override
    public void getBloodOxygenLevel() {
    }
    
    @java.lang.Override
    public void getBloodPressure() {
    }
    
    private final void onSportsDataObtained(java.lang.Object[] p1) {
    }
    
    private final void onSportsDataObtainedGPS(java.lang.Object[] p1, java.util.List<? extends java.util.List<com.noisefit_commans.models.GPSDataResponse>> p2) {
    }
    
    private final void onStepsDataObtained(java.lang.Object[] p1) {
    }
    
    @java.lang.Override
    public void syncSportsActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    private final void onHeartHistoryObtained(java.lang.Object[] p1) {
    }
    
    @java.lang.Override
    public void getHeartHistory(@org.jetbrains.annotations.NotNull
    java.util.Calendar calendar) {
    }
    
    private final void onSleepDataObtained(java.lang.Object[] p1) {
    }
    
    private final void onDeleteSportsData() {
    }
}