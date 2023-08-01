package com.noisefit.colorfit_pro.utils;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0016\u0018\u00002\u00020\u0001:\u0001\u0015B\u000f\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\r\u001a\u00020\u000e2\u000e\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u0010H\u0016J\u0012\u0010\u0012\u001a\u00020\u000e2\b\u0010\u0013\u001a\u0004\u0018\u00010\bH\u0016J\b\u0010\u0014\u001a\u00020\u000eH\u0002R\u001c\u0010\u0005\u001a\u0010\u0012\u0004\u0012\u00020\u0007\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/noisefit/colorfit_pro/utils/SportDataListenerWrapper;", "Lcom/crrepa/ble/conn/listener/CRPTrainingChangeListener;", "callback", "Lcom/noisefit/colorfit_pro/utils/SportDataListenerWrapper$Callback;", "(Lcom/noisefit/colorfit_pro/utils/SportDataListenerWrapper$Callback;)V", "activityDataHm", "Ljava/util/LinkedHashMap;", "", "Lcom/crrepa/ble/conn/bean/CRPTrainingInfo;", "count", "", "trainingList", "Ljava/util/ArrayList;", "onHistoryTrainingChange", "", "list", "", "Lcom/crrepa/ble/conn/bean/CRPHistoryTrainingInfo;", "onTrainingChange", "crpTrainingInfo", "queryTrainingDetails", "Callback", "noisefit_colorfit_pro_debug"})
public class SportDataListenerWrapper implements com.crrepa.ble.conn.listener.CRPTrainingChangeListener {
    private final com.noisefit.colorfit_pro.utils.SportDataListenerWrapper.Callback callback = null;
    private final java.util.LinkedHashMap<java.lang.Long, com.crrepa.ble.conn.bean.CRPTrainingInfo> activityDataHm = null;
    private final java.util.ArrayList<java.lang.Integer> trainingList = null;
    private int count = 0;
    
    public SportDataListenerWrapper(@org.jetbrains.annotations.Nullable
    com.noisefit.colorfit_pro.utils.SportDataListenerWrapper.Callback callback) {
        super();
    }
    
    @java.lang.Override
    public void onHistoryTrainingChange(@org.jetbrains.annotations.Nullable
    java.util.List<com.crrepa.ble.conn.bean.CRPHistoryTrainingInfo> list) {
    }
    
    private final void queryTrainingDetails() {
    }
    
    @java.lang.Override
    public void onTrainingChange(@org.jetbrains.annotations.Nullable
    com.crrepa.ble.conn.bean.CRPTrainingInfo crpTrainingInfo) {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H&\u00a8\u0006\u0007"}, d2 = {"Lcom/noisefit/colorfit_pro/utils/SportDataListenerWrapper$Callback;", "", "onSyncSportData", "", "records", "", "Lcom/crrepa/ble/conn/bean/CRPTrainingInfo;", "noisefit_colorfit_pro_debug"})
    public static abstract interface Callback {
        
        public abstract void onSyncSportData(@org.jetbrains.annotations.NotNull
        java.util.List<? extends com.crrepa.ble.conn.bean.CRPTrainingInfo> records);
    }
}