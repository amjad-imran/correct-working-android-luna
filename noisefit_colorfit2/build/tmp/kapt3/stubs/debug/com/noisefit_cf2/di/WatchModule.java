package com.noisefit_cf2.di;

import java.lang.System;

@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0004H\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0007J\b\u0010\u0017\u001a\u00020\u000fH\u0007J\u0010\u0010\u0018\u001a\u00020\u00162\u0006\u0010\u0019\u001a\u00020\u0004H\u0007J\u0010\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000bH\u0007J\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u0012H\u0007J\u0010\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\rH\u0007\u00a8\u0006#"}, d2 = {"Lcom/noisefit_cf2/di/WatchModule;", "", "()V", "provideApplicationHandler", "Lcom/noisefit_cf2/base/ColorFit2ApplicationHandler;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "provideBaseInitializeInterface", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "handler", "provideCF2QueryDeviceUnitsHandler", "Lcom/noisefit_cf2/handler/CF2QueryDeviceUnitsHandler;", "provideCF2UserActivityDataHandler", "Lcom/noisefit_cf2/handler/CF2UserActivityDataHandler;", "dataConverter", "Lcom/noisefit_cf2/dataconversions/Colorfit2DataConverter;", "colorFit2ApplicationHandler", "provideCf2UpdateDeviceUnitsHandler", "Lcom/noisefit_cf2/handler/CF2UpdateDeviceUnitsHandler;", "provideConnectionDataActions", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "cF2ConnectHandler", "Lcom/noisefit_cf2/handler/connect/CF2ConnectHandler;", "provideDataConverter", "provideNavPlusConnectHandler", "applicationHandler", "provideQueryDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "cf2QueryDeviceUnitsHandler", "provideUpdateDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "cf2DeviceUnitsHandler", "provideUserActivityDataActions", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "cF2UserActivityDataHandler", "noisefit_colorfit2_debug"})
@dagger.Module
public final class WatchModule {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_cf2.di.WatchModule INSTANCE = null;
    
    private WatchModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_cf2.dataconversions.Colorfit2DataConverter provideDataConverter() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ColorFit2ApplicationHandler")
    public final com.noisefit_commans.interfaces.base.BaseInitializeInterface provideBaseInitializeInterface(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.base.ColorFit2ApplicationHandler handler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_cf2.base.ColorFit2ApplicationHandler provideApplicationHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_cf2.handler.CF2QueryDeviceUnitsHandler provideCF2QueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_cf2.handler.connect.CF2ConnectHandler provideNavPlusConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.base.ColorFit2ApplicationHandler applicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_cf2.handler.CF2UserActivityDataHandler provideCF2UserActivityDataHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.dataconversions.Colorfit2DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_cf2.base.ColorFit2ApplicationHandler colorFit2ApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ColorFit2ConnectionDataActions")
    public final com.noisefit_commans.interfaces.connection.ConnectionDataActions provideConnectionDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.handler.connect.CF2ConnectHandler cF2ConnectHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "CF2QueryDeviceDataActions")
    public final com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions provideQueryDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.handler.CF2QueryDeviceUnitsHandler cf2QueryDeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "Cf2UpdateDevice")
    public final com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions provideUpdateDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.handler.CF2UpdateDeviceUnitsHandler cf2DeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_cf2.handler.CF2UpdateDeviceUnitsHandler provideCf2UpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "CF2UserActivityDataActions")
    public final com.noisefit_commans.interfaces.data.UserActivityDataActions provideUserActivityDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.handler.CF2UserActivityDataHandler cF2UserActivityDataHandler) {
        return null;
    }
}