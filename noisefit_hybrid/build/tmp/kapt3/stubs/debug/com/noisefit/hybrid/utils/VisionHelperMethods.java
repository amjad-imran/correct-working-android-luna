package com.noisefit.hybrid.utils;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\t\u001a\u001a\u0012\u0016\u0012\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f\u0012\u0004\u0012\u00020\b0\u000b0\nJ\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\nJ$\u0010\u0010\u001a\u0018\u0012\u0014\u0012\u0012\u0012\u0004\u0012\u00020\u00120\u0011j\b\u0012\u0004\u0012\u00020\u0012`\u00130\n2\u0006\u0010\u0014\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/noisefit/hybrid/utils/VisionHelperMethods;", "", "bitwiseHelperUtils", "Lcom/noisefit/hybrid/utils/BitwiseHelperUtils;", "visionCommands", "Lcom/noisefit/hybrid/base/VisionCommands;", "(Lcom/noisefit/hybrid/utils/BitwiseHelperUtils;Lcom/noisefit/hybrid/base/VisionCommands;)V", "sleepCount", "", "getHeartRateObservable", "Lio/reactivex/rxjava3/core/Observable;", "Lkotlin/Pair;", "", "Lcom/noisefit_commans/models/HeartRate;", "getSleepDataObservable", "Lcom/noisefit_commans/models/SleepData;", "getStressDataObservable", "Ljava/util/ArrayList;", "Lcom/noisefit_commans/models/BloodOxygenStressData;", "Lkotlin/collections/ArrayList;", "bloodOxygenCount", "noisefit_hybrid_debug"})
public final class VisionHelperMethods {
    private final com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils = null;
    private final com.noisefit.hybrid.base.VisionCommands visionCommands = null;
    private int sleepCount = 0;
    
    @javax.inject.Inject
    public VisionHelperMethods(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.VisionCommands visionCommands) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final io.reactivex.rxjava3.core.Observable<kotlin.Pair<java.util.List<com.noisefit_commans.models.HeartRate>, java.lang.Integer>> getHeartRateObservable() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final io.reactivex.rxjava3.core.Observable<java.util.ArrayList<com.noisefit_commans.models.BloodOxygenStressData>> getStressDataObservable(int bloodOxygenCount) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final io.reactivex.rxjava3.core.Observable<com.noisefit_commans.models.SleepData> getSleepDataObservable() {
        return null;
    }
}