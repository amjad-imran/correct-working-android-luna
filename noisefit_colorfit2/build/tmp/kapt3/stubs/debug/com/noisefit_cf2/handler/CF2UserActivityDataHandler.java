package com.noisefit_cf2.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u0000 <2\u00020\u0001:\u0001<B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0011\u001a\u00020\u0012H\u0016J\u001b\u0010\u0013\u001a\u00020\u0012\"\u0004\b\u0000\u0010\u00142\u0006\u0010\u0015\u001a\u0002H\u0014H\u0016\u00a2\u0006\u0002\u0010\u0016J\u001b\u0010\u0017\u001a\u00020\u0012\"\u0004\b\u0000\u0010\u00142\u0006\u0010\u0015\u001a\u0002H\u0014H\u0016\u00a2\u0006\u0002\u0010\u0016J\b\u0010\u0018\u001a\u00020\u0012H\u0016J\u0017\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0002\u00a2\u0006\u0002\u0010\u001dJ\u0017\u0010\u001e\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0002\u00a2\u0006\u0002\u0010\u001dJ\u0017\u0010\u001f\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0002\u00a2\u0006\u0002\u0010\u001dJ\b\u0010 \u001a\u00020\u0012H\u0016J\b\u0010!\u001a\u00020\u0012H\u0016J\b\u0010\"\u001a\u00020\u0012H\u0016J\u0010\u0010#\u001a\u00020\u00122\u0006\u0010$\u001a\u00020%H\u0016J\b\u0010&\u001a\u00020\u0012H\u0016J\u0010\u0010\'\u001a\u00020\u00122\u0006\u0010(\u001a\u00020\u001aH\u0016J\u0010\u0010)\u001a\u00020\u00122\u0006\u0010(\u001a\u00020\u001aH\u0016J\b\u0010*\u001a\u00020\u0012H\u0016J\b\u0010+\u001a\u00020\u0012H\u0016J\b\u0010,\u001a\u00020\u0012H\u0016J\u0018\u0010-\u001a\u00020\u00122\u0006\u0010.\u001a\u00020\u001c2\u0006\u0010/\u001a\u00020\u001cH\u0016J\u0010\u00100\u001a\u00020\u00122\u0006\u00101\u001a\u000202H\u0016J\b\u00103\u001a\u00020\u0012H\u0016J\u0010\u00104\u001a\u00020\u00122\u0006\u0010\t\u001a\u00020\nH\u0016J\b\u00105\u001a\u00020\u0012H\u0002J\b\u00106\u001a\u00020\u0012H\u0002J\u0010\u00107\u001a\u00020\u00122\u0006\u0010(\u001a\u00020\u001aH\u0016J\u0018\u00108\u001a\u00020\u00122\u0006\u0010(\u001a\u00020\u001a2\u0006\u00109\u001a\u00020:H\u0016J\u0010\u0010;\u001a\u00020\u00122\u0006\u00101\u001a\u000202H\u0016R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006="}, d2 = {"Lcom/noisefit_cf2/handler/CF2UserActivityDataHandler;", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "dataConverter", "Lcom/noisefit_cf2/dataconversions/Colorfit2DataConverter;", "colorFit2ApplicationHandler", "Lcom/noisefit_cf2/base/ColorFit2ApplicationHandler;", "(Lcom/noisefit_cf2/dataconversions/Colorfit2DataConverter;Lcom/noisefit_cf2/base/ColorFit2ApplicationHandler;)V", "appExchangeDataCallBack", "Lcom/ido/ble/callback/AppExchangeDataCallBack$ICallBack;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "iSyncDataListener", "Lcom/ido/ble/business/sync/ISyncDataListener;", "isSyncProgressListener", "Lcom/ido/ble/business/sync/ISyncProgressListener;", "userActivityDataCallbacks", "Lcom/noisefit_commans/interfaces/data/IUserActivityDataCallback;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "disableEnableBluetooth", "getActivityTypeString", "", "type", "", "(Ljava/lang/Integer;)Ljava/lang/String;", "getActivityTypeStringV3", "getActivityTypeSwimming", "getBloodOxygenLevel", "getBloodPressure", "getBodyTemperatureData", "getHeartHistory", "calendar", "Ljava/util/Calendar;", "getHeartRate", "getSleepData", "date", "getStepsData", "getStressCount", "init", "openGmailApp", "pushGPSData", "gpsSignal", "distance", "refresh", "sportsModeRequest", "Lcom/noisefit_commans/models/SportsModeRequest;", "removeCallbacks", "setDevice", "startSyncHealth", "syncData", "syncSportsActivity", "syncUserActivity", "isRefresh", "", "updateSportsMode", "Companion", "noisefit_colorfit2_debug"})
public final class CF2UserActivityDataHandler extends com.noisefit_commans.interfaces.data.UserActivityDataActions {
    private com.noisefit_cf2.dataconversions.Colorfit2DataConverter dataConverter;
    private com.noisefit_cf2.base.ColorFit2ApplicationHandler colorFit2ApplicationHandler;
    private com.noisefit_commans.interfaces.data.IUserActivityDataCallback userActivityDataCallbacks;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_cf2.handler.CF2UserActivityDataHandler.Companion Companion = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String TAG = "CF2UserActivity";
    private final com.ido.ble.business.sync.ISyncProgressListener isSyncProgressListener = null;
    private final com.ido.ble.business.sync.ISyncDataListener iSyncDataListener = null;
    private final com.ido.ble.callback.AppExchangeDataCallBack.ICallBack appExchangeDataCallBack = null;
    
    @javax.inject.Inject
    public CF2UserActivityDataHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.dataconversions.Colorfit2DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_cf2.base.ColorFit2ApplicationHandler colorFit2ApplicationHandler) {
        super();
    }
    
    @java.lang.Override
    public void openGmailApp() {
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice colorFitDevice) {
    }
    
    @java.lang.Override
    public void getBodyTemperatureData() {
    }
    
    private final void syncData() {
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
    public void init() {
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
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void syncUserActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date, boolean isRefresh) {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    @java.lang.Override
    public void updateSportsMode(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SportsModeRequest sportsModeRequest) {
    }
    
    @java.lang.Override
    public void getHeartHistory(@org.jetbrains.annotations.NotNull
    java.util.Calendar calendar) {
    }
    
    @java.lang.Override
    public void refresh(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SportsModeRequest sportsModeRequest) {
    }
    
    private final java.lang.String getActivityTypeString(java.lang.Integer type) {
        return null;
    }
    
    private final java.lang.String getActivityTypeStringV3(java.lang.Integer type) {
        return null;
    }
    
    private final java.lang.String getActivityTypeSwimming(java.lang.Integer type) {
        return null;
    }
    
    @java.lang.Override
    public void syncSportsActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    private final void startSyncHealth() {
    }
    
    @java.lang.Override
    public void pushGPSData(int gpsSignal, int distance) {
    }
    
    @java.lang.Override
    public void disableEnableBluetooth() {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/noisefit_cf2/handler/CF2UserActivityDataHandler$Companion;", "", "()V", "TAG", "", "noisefit_colorfit2_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}