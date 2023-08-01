package com.noisefit_nav_plus.handler;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010!\u001a\u00020\"H\u0016J\u001b\u0010#\u001a\u00020\"\"\u0004\b\u0000\u0010$2\u0006\u0010%\u001a\u0002H$H\u0016\u00a2\u0006\u0002\u0010&J\u001b\u0010\'\u001a\u00020\"\"\u0004\b\u0000\u0010$2\u0006\u0010%\u001a\u0002H$H\u0016\u00a2\u0006\u0002\u0010&J\b\u0010(\u001a\u00020\"H\u0016J\b\u0010)\u001a\u00020\"H\u0016J\b\u0010*\u001a\u00020\"H\u0016J\u0010\u0010+\u001a\u00020\"2\u0006\u0010,\u001a\u00020-H\u0016J\b\u0010.\u001a\u00020\"H\u0016J\u0010\u0010/\u001a\u00020\"2\u0006\u00100\u001a\u00020\bH\u0016J\u0010\u00101\u001a\u00020\"2\u0006\u00100\u001a\u00020\bH\u0016J\b\u00102\u001a\u00020\"H\u0016J\b\u00103\u001a\u00020\"H\u0016J\b\u00104\u001a\u00020\"H\u0002J\b\u00105\u001a\u00020\"H\u0002J\u0010\u00106\u001a\u00020\"2\u0006\u00107\u001a\u000208H\u0002J\b\u00109\u001a\u00020\"H\u0016J\u0010\u0010:\u001a\u00020\"2\u0006\u0010\t\u001a\u00020\nH\u0016J\b\u0010;\u001a\u00020\"H\u0002J\u0010\u0010<\u001a\u00020\"2\u0006\u00100\u001a\u00020\bH\u0016J\u0018\u0010=\u001a\u00020\"2\u0006\u00100\u001a\u00020\b2\u0006\u0010>\u001a\u00020\fH\u0016R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001bR\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001f\u001a\u0004\u0018\u00010 X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006?"}, d2 = {"Lcom/noisefit_nav_plus/handler/NavPlusUserActivityHandler;", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "dataConverter", "Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;", "navPlusApplicationHandler", "Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "(Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;)V", "TAG", "", "colorFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "isSyncProtoSportSyncing", "", "isSyncProtoSportToday", "mBleService", "Lcom/zjw/zhbraceletsdk/service/ZhBraceletService;", "getMBleService", "()Lcom/zjw/zhbraceletsdk/service/ZhBraceletService;", "setMBleService", "(Lcom/zjw/zhbraceletsdk/service/ZhBraceletService;)V", "mPerformerListener", "Lcom/zjw/zhbraceletsdk/linstener/SimplePerformerListener;", "mSyncProtoHistoryListener", "Lcom/zjw/zhbraceletsdk/linstener/SyncProtoHistoryListener;", "getNavPlusApplicationHandler", "()Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "setNavPlusApplicationHandler", "(Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;)V", "sportModleInfoList", "Ljava/util/ArrayList;", "Lcom/zjw/zhbraceletsdk/bean/SportModleInfo;", "userActivityDataCallbacks", "Lcom/noisefit_commans/interfaces/data/IUserActivityDataCallback;", "attachCallbacks", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "getBloodOxygenLevel", "getBloodPressure", "getBodyTemperatureData", "getHeartHistory", "calendar", "Ljava/util/Calendar;", "getHeartRate", "getSleepData", "date", "getStepsData", "getStressCount", "init", "initBloodOxygen", "initStressData", "onHeartHistoryObtained", "mWoHeartInfo", "Lcom/zjw/zhbraceletsdk/bean/WoHeartInfo;", "removeCallbacks", "setDevice", "syncData", "syncSportsActivity", "syncUserActivity", "isRefresh", "noisefit_nav_plus_debug"})
public final class NavPlusUserActivityHandler extends com.noisefit_commans.interfaces.data.UserActivityDataActions {
    private com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter;
    @org.jetbrains.annotations.NotNull
    private com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler;
    private final java.lang.String TAG = null;
    private com.noisefit_commans.interfaces.data.IUserActivityDataCallback userActivityDataCallbacks;
    private boolean isSyncProtoSportToday = false;
    private boolean isSyncProtoSportSyncing = false;
    private java.util.ArrayList<com.zjw.zhbraceletsdk.bean.SportModleInfo> sportModleInfoList;
    private com.noisefit_commans.models.ColorFitDevice colorFitDevice;
    @org.jetbrains.annotations.Nullable
    private com.zjw.zhbraceletsdk.service.ZhBraceletService mBleService;
    private final com.zjw.zhbraceletsdk.linstener.SyncProtoHistoryListener mSyncProtoHistoryListener = null;
    private final com.zjw.zhbraceletsdk.linstener.SimplePerformerListener mPerformerListener = null;
    
    @javax.inject.Inject
    public NavPlusUserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_nav_plus.base.NavPlusApplicationHandler getNavPlusApplicationHandler() {
        return null;
    }
    
    public final void setNavPlusApplicationHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler p0) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.zjw.zhbraceletsdk.service.ZhBraceletService getMBleService() {
        return null;
    }
    
    public final void setMBleService(@org.jetbrains.annotations.Nullable
    com.zjw.zhbraceletsdk.service.ZhBraceletService p0) {
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void syncUserActivity(@org.jetbrains.annotations.NotNull
    java.lang.String date, boolean isRefresh) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void getBodyTemperatureData() {
    }
    
    @java.lang.Override
    public void getStepsData(@org.jetbrains.annotations.NotNull
    java.lang.String date) {
    }
    
    private final void syncData() {
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
    
    private final void initBloodOxygen() {
    }
    
    private final void initStressData() {
    }
    
    private final void onHeartHistoryObtained(com.zjw.zhbraceletsdk.bean.WoHeartInfo mWoHeartInfo) {
    }
}