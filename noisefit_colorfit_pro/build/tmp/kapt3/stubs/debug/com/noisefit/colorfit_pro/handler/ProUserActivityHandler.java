package com.noisefit.colorfit_pro.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u009c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\"\u001a\u00020#H\u0016J\u001b\u0010$\u001a\u00020#\"\u0004\b\u0000\u0010%2\u0006\u0010&\u001a\u0002H%H\u0016\u00a2\u0006\u0002\u0010\'J\u001b\u0010(\u001a\u00020#\"\u0004\b\u0000\u0010%2\u0006\u0010&\u001a\u0002H%H\u0016\u00a2\u0006\u0002\u0010\'J\u0016\u0010)\u001a\b\u0012\u0004\u0012\u00020*0\u00102\u0006\u0010+\u001a\u00020\u0011H\u0002J\b\u0010,\u001a\u00020#H\u0016J\b\u0010-\u001a\u00020#H\u0016J\b\u0010.\u001a\u00020#H\u0016J\u0010\u0010/\u001a\u00020#2\u0006\u00100\u001a\u000201H\u0016J\b\u00102\u001a\u00020#H\u0016J\u0010\u00103\u001a\u00020#2\u0006\u00104\u001a\u000205H\u0016J\u0010\u00106\u001a\u00020#2\u0006\u00104\u001a\u000205H\u0016J\b\u00107\u001a\u00020#H\u0016J\u0016\u00108\u001a\u00020#2\f\u00109\u001a\b\u0012\u0004\u0012\u00020;0:H\u0002J\u0010\u0010<\u001a\u00020#2\u0006\u0010\u0015\u001a\u00020\u0016H\u0016J\u0010\u0010=\u001a\u00020#2\u0006\u0010>\u001a\u00020\nH\u0016J\u0010\u0010?\u001a\u00020#2\u0006\u0010@\u001a\u00020AH\u0016J\u0010\u0010B\u001a\u00020#2\u0006\u00104\u001a\u000205H\u0016J\u0018\u0010C\u001a\u00020#2\u0006\u00104\u001a\u0002052\u0006\u0010D\u001a\u00020AH\u0016J\u0010\u0010E\u001a\u00020#2\u0006\u0010\u0015\u001a\u00020\u0016H\u0016R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010 \u001a\u0004\u0018\u00010!X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006F"}, d2 = {"Lcom/noisefit/colorfit_pro/handler/ProUserActivityHandler;", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "dataConverter", "Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;", "navPlusApplicationHandler", "Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "(Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;)V", "bloodOxygenChangeListener", "Lcom/crrepa/ble/conn/listener/CRPBloodOxygenChangeListener;", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "crpStepInfo", "Lcom/crrepa/ble/conn/bean/CRPStepInfo;", "heartRateChangListener", "Lcom/crrepa/ble/conn/listener/CRPHeartRateChangeListener;", "heartRateList", "Ljava/util/ArrayList;", "", "lastCRPStepInfo", "sleepChangeListener", "Lcom/crrepa/ble/conn/listener/CRPSleepChangeListener;", "sportsModeRequest", "Lcom/noisefit_commans/models/SportsModeRequest;", "sportsModeResponse", "Lcom/noisefit_commans/models/SportsModeResponse;", "sportsModeThreshold", "stepChangeListener", "Lcom/crrepa/ble/conn/listener/CRPStepChangeListener;", "stepsCategoryChangeListener", "Lcom/crrepa/ble/conn/listener/CRPStepsCategoryChangeListener;", "stressChangeListener", "Lcom/crrepa/ble/conn/listener/CRPStressListener;", "userActivityDataCallbacks", "Lcom/noisefit_commans/interfaces/data/IUserActivityDataCallback;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "getBloodOxygenData", "Lcom/noisefit_commans/models/BloodOxygenBreakup;", "p0", "getBloodOxygenLevel", "getBloodPressure", "getBodyTemperatureData", "getHeartHistory", "calendar", "Ljava/util/Calendar;", "getHeartRate", "getSleepData", "date", "", "getStepsData", "getStressCount", "onSportActivitiesObtained", "list", "", "Lcom/crrepa/ble/conn/bean/CRPMovementHeartRateInfo;", "refresh", "setDevice", "device", "setSpo2MeasurementData", "status", "", "syncSportsActivity", "syncUserActivity", "isRefresh", "updateSportsMode", "noisefit_colorfit_pro_debug"})
public final class ProUserActivityHandler extends com.noisefit_commans.interfaces.data.UserActivityDataActions {
    private com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter;
    private com.noisefit.colorfit_pro.base.ProApplicationHandler navPlusApplicationHandler;
    private com.crrepa.ble.conn.bean.CRPStepInfo lastCRPStepInfo;
    private com.noisefit_commans.interfaces.data.IUserActivityDataCallback userActivityDataCallbacks;
    private com.noisefit_commans.models.SportsModeRequest sportsModeRequest;
    private com.crrepa.ble.conn.bean.CRPStepInfo crpStepInfo;
    private com.crrepa.ble.conn.bean.CRPStepInfo sportsModeThreshold;
    private com.noisefit_commans.models.SportsModeResponse sportsModeResponse;
    private final java.util.ArrayList<java.lang.Integer> heartRateList = null;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    private com.crrepa.ble.conn.listener.CRPStepsCategoryChangeListener stepsCategoryChangeListener;
    private com.crrepa.ble.conn.listener.CRPStepChangeListener stepChangeListener;
    private com.crrepa.ble.conn.listener.CRPSleepChangeListener sleepChangeListener;
    private com.crrepa.ble.conn.listener.CRPStressListener stressChangeListener;
    private com.crrepa.ble.conn.listener.CRPHeartRateChangeListener heartRateChangListener;
    private com.crrepa.ble.conn.listener.CRPBloodOxygenChangeListener bloodOxygenChangeListener;
    
    @javax.inject.Inject
    public ProUserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler navPlusApplicationHandler) {
        super();
    }
    
    @java.lang.Override
    public void setDevice(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice device) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void getBodyTemperatureData() {
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
    public void attachCallbacks() {
    }
    
    private final void onSportActivitiesObtained(java.util.List<? extends com.crrepa.ble.conn.bean.CRPMovementHeartRateInfo> list) {
    }
    
    @java.lang.Override
    public void setSpo2MeasurementData(boolean status) {
    }
    
    private final java.util.ArrayList<com.noisefit_commans.models.BloodOxygenBreakup> getBloodOxygenData(int p0) {
        return null;
    }
    
    @java.lang.Override
    public void updateSportsMode(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SportsModeRequest sportsModeRequest) {
    }
    
    @java.lang.Override
    public void refresh(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.SportsModeRequest sportsModeRequest) {
    }
    
    @java.lang.Override
    public void getHeartHistory(@org.jetbrains.annotations.NotNull
    java.util.Calendar calendar) {
    }
    
    @java.lang.Override
    public void syncSportsActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
}