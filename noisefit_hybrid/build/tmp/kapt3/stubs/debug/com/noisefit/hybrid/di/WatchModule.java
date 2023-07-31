package com.noisefit.hybrid.di;

import java.lang.System;

@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0092\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0004H\u0007J \u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0007J\b\u0010\u0010\u001a\u00020\u000bH\u0007J\b\u0010\u0011\u001a\u00020\u0012H\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0007JB\u0010\u0017\u001a\u00020\u00182\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0019\u001a\u00020\t2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u000e\u001a\u00020\u000f2\b\b\u0001\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0012H\u0007J \u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\"\u001a\u00020#H\u0007J\b\u0010$\u001a\u00020\rH\u0007J\u0010\u0010%\u001a\u00020\u00162\u0006\u0010!\u001a\u00020\u0004H\u0007JJ\u0010&\u001a\u00020\'2\b\b\u0001\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0019\u001a\u00020\t2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010(\u001a\u00020)2\u0006\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u000e\u001a\u00020\u000fH\u0007J\b\u0010*\u001a\u00020)H\u0007J\u0010\u0010+\u001a\u00020,2\u0006\u0010-\u001a\u00020\u0018H\u0007J\u0010\u0010.\u001a\u00020/2\u0006\u00100\u001a\u00020\'H\u0007J\u0010\u00101\u001a\u0002022\u0006\u00103\u001a\u00020 H\u0007J\u0010\u00104\u001a\u00020\u000f2\u0006\u0010\n\u001a\u00020\u000bH\u0007J\u0018\u00105\u001a\u00020#2\u0006\u0010\u0019\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\u000fH\u0007\u00a8\u00066"}, d2 = {"Lcom/noisefit/hybrid/di/WatchModule;", "", "()V", "provideApplicationHandler", "Lcom/noisefit/hybrid/base/NFHybridApplicationHandler;", "provideBaseInitializeInterface", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "handler", "provideBitwiseHelperUtils", "Lcom/noisefit/hybrid/utils/BitwiseHelperUtils;", "bitwiseUtils", "Lcom/noisefit/hybrid/utils/BitwiseUtils;", "dataConverter", "Lcom/noisefit/hybrid/dataconversions/DataConverter;", "visionCommands", "Lcom/noisefit/hybrid/base/VisionCommands;", "provideBitwiseUtils", "provideBluetoothSDKExp", "Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp;", "provideConnectionDataActions", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "nfhConnectHandler", "Lcom/noisefit/hybrid/handler/connect/NFHybridConnectHandler;", "provideNFHQueryDeviceUnitsHandler", "Lcom/noisefit/hybrid/handler/NFHQueryDeviceUnitsHandler;", "bitwiseHelperUtils", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "appContext", "Landroid/content/Context;", "bluetoothsdkExp", "provideNFHUserActivityDataHandler", "Lcom/noisefit/hybrid/handler/NFHUserActivityHandler;", "applicationHandler", "visionHelperMethods", "Lcom/noisefit/hybrid/utils/VisionHelperMethods;", "provideNFHybridDataConverter", "provideNavPlusConnectHandler", "provideNfhUpdateDeviceUnitsHandler", "Lcom/noisefit/hybrid/handler/NFHUpdateDeviceUnitsHandler;", "onlineWatchFacesVision", "Lcom/noisefit/hybrid/utils/OnlineWatchFacesVision;", "provideOnlineWatchFacesVision", "provideQueryDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "nfhQueryDeviceUnitsHandler", "provideUpdateDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "nfhDeviceUnitsHandler", "provideUserActivityDataActions", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "nfhUserActivityDataHandler", "provideVisionCommand", "provideVisionHelperMethods", "noisefit_hybrid_debug"})
@dagger.Module
public final class WatchModule {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit.hybrid.di.WatchModule INSTANCE = null;
    
    private WatchModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NFHybridApplicationHandler")
    public final com.noisefit_commans.interfaces.base.BaseInitializeInterface provideBaseInitializeInterface(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.NFHybridApplicationHandler handler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.base.VisionCommands provideVisionCommand(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.utils.VisionHelperMethods provideVisionHelperMethods(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.VisionCommands visionCommands) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.utils.BitwiseUtils provideBitwiseUtils() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.utils.OnlineWatchFacesVision provideOnlineWatchFacesVision() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.utils.BluetoothSDK_Exp provideBluetoothSDKExp() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.dataconversions.DataConverter provideNFHybridDataConverter() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.utils.BitwiseHelperUtils provideBitwiseHelperUtils(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.dataconversions.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.VisionCommands visionCommands) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.base.NFHybridApplicationHandler provideApplicationHandler() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.handler.NFHQueryDeviceUnitsHandler provideNFHQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.dataconversions.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.VisionCommands visionCommands, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BluetoothSDK_Exp bluetoothsdkExp) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.handler.connect.NFHybridConnectHandler provideNavPlusConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.NFHybridApplicationHandler applicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NFHConnectionDataActions")
    public final com.noisefit_commans.interfaces.connection.ConnectionDataActions provideConnectionDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.handler.connect.NFHybridConnectHandler nfhConnectHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NFHQueryDeviceDataActions")
    public final com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions provideQueryDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.handler.NFHQueryDeviceUnitsHandler nfhQueryDeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NFHUpdateDevice")
    public final com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions provideUpdateDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.handler.NFHUpdateDeviceUnitsHandler nfhDeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.handler.NFHUpdateDeviceUnitsHandler provideNfhUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseUtils bitwiseUtils, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.dataconversions.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BitwiseHelperUtils bitwiseHelperUtils, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.OnlineWatchFacesVision onlineWatchFacesVision, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.BluetoothSDK_Exp bluetoothsdkExp, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.VisionCommands visionCommands) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.hybrid.handler.NFHUserActivityHandler provideNFHUserActivityDataHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.NFHybridApplicationHandler applicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.dataconversions.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.utils.VisionHelperMethods visionHelperMethods) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NFHUserActivityDataActions")
    public final com.noisefit_commans.interfaces.data.UserActivityDataActions provideUserActivityDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.handler.NFHUserActivityHandler nfhUserActivityDataHandler) {
        return null;
    }
}