package com.noisefit_ryeex_sdk.dataConversion;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u008c\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0013\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 :2\u00020\u0001:\u0001:B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018J\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u0018H\u0002J\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001d2\u0006\u0010\u001f\u001a\u00020 J\u001c\u0010!\u001a\b\u0012\u0004\u0012\u00020\"0\u001d2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020%0$H\u0002J\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020\'0$2\u0006\u0010\u001f\u001a\u00020(J\u000e\u0010)\u001a\u00020*2\u0006\u0010\u001f\u001a\u00020+J\u000e\u0010,\u001a\u00020-2\u0006\u0010\u001f\u001a\u00020.J\u0014\u0010/\u001a\u0002002\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u001802J\u0016\u00103\u001a\u0002042\u0006\u00105\u001a\u00020\u00162\u0006\u00106\u001a\u000207J\u000e\u00108\u001a\u00020\u00182\u0006\u00109\u001a\u00020\u0018R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014\u00a8\u0006;"}, d2 = {"Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;", "", "context", "Landroid/content/Context;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "gson", "Lcom/google/gson/Gson;", "(Landroid/content/Context;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;Lcom/google/gson/Gson;)V", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "getGson", "()Lcom/google/gson/Gson;", "setGson", "(Lcom/google/gson/Gson;)V", "getWatchDataStore", "()Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "setWatchDataStore", "(Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "getAppName", "", "id", "", "isOutdoorSport", "", "sportId", "parseBloodOxygenData", "Ljava/util/ArrayList;", "Lcom/noisefit_commans/models/BloodOxygenBreakup;", "bean", "Lcom/ryeex/watch/adapter/model/entity/LibHealthBloodOxygenDomain;", "parseGpsMapsData", "", "mapsData", "", "Lcom/noisefit_commans/models/LocationDataModel;", "parseHeartRateData", "Lcom/noisefit_commans/models/HeartRate;", "Lcom/ryeex/watch/adapter/model/entity/LibHealthHeartRateDomain;", "parseSleepData", "Lcom/noisefit_commans/models/SleepData;", "Lcom/ryeex/watch/adapter/model/entity/LibHealthSleepDomain;", "parseSportsData", "Lcom/noisefit_commans/models/SportsModeListGPS;", "Lcom/ryeex/watch/adapter/model/entity/LibSportDomain;", "parseSportsModeInfo", "Lcom/noisefit_commans/models/SportsModeList;", "functionInfo", "", "parseStepsData", "Lcom/noisefit_commans/models/StepsData;", "dateKey", "tempStepDataWrapper", "Lcom/noisefit_ryeex_sdk/utils/TempStepDataWrapper;", "parseWeatherType", "code", "Companion", "noisefit_ryeex_sdk_debug"})
public final class DataConverter {
    @org.jetbrains.annotations.NotNull
    private android.content.Context context;
    @org.jetbrains.annotations.NotNull
    private com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore;
    @org.jetbrains.annotations.NotNull
    private com.google.gson.Gson gson;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_ryeex_sdk.dataConversion.DataConverter.Companion Companion = null;
    
    @javax.inject.Inject
    public DataConverter(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore, @org.jetbrains.annotations.NotNull
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
    public final com.noisefit_commans.data.local.abstraction.WatchDataStore getWatchDataStore() {
        return null;
    }
    
    public final void setWatchDataStore(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.google.gson.Gson getGson() {
        return null;
    }
    
    public final void setGson(@org.jetbrains.annotations.NotNull
    com.google.gson.Gson p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.StepsData parseStepsData(@org.jetbrains.annotations.NotNull
    java.lang.String dateKey, @org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.utils.TempStepDataWrapper tempStepDataWrapper) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeListGPS parseSportsData(@org.jetbrains.annotations.NotNull
    com.ryeex.watch.adapter.model.entity.LibSportDomain bean) {
        return null;
    }
    
    private final java.util.ArrayList<double[]> parseGpsMapsData(java.util.List<com.noisefit_commans.models.LocationDataModel> mapsData) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<com.noisefit_commans.models.HeartRate> parseHeartRateData(@org.jetbrains.annotations.NotNull
    com.ryeex.watch.adapter.model.entity.LibHealthHeartRateDomain bean) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.ArrayList<com.noisefit_commans.models.BloodOxygenBreakup> parseBloodOxygenData(@org.jetbrains.annotations.NotNull
    com.ryeex.watch.adapter.model.entity.LibHealthBloodOxygenDomain bean) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SleepData parseSleepData(@org.jetbrains.annotations.NotNull
    com.ryeex.watch.adapter.model.entity.LibHealthSleepDomain bean) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_commans.models.SportsModeList parseSportsModeInfo(@org.jetbrains.annotations.NotNull
    java.util.List<java.lang.Integer> functionInfo) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getAppName(int id) {
        return null;
    }
    
    public final int parseWeatherType(int code) {
        return 0;
    }
    
    private final boolean isOutdoorSport(int sportId) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\t"}, d2 = {"Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter$Companion;", "", "()V", "getSportName", "", "sportId", "", "parseSportMode", "Lcom/noisefit_commans/models/SportsModeList$SportsMode;", "noisefit_ryeex_sdk_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.noisefit_commans.models.SportsModeList.SportsMode parseSportMode(int sportId) {
            return null;
        }
        
        private final java.lang.String getSportName(int sportId) {
            return null;
        }
    }
}