package com.noisefit_nav_plus.di;

import java.lang.System;

@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\u001a\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0007J\b\u0010\u0011\u001a\u00020\u0006H\u0007J\u0010\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J2\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\f2\b\b\u0001\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0017\u001a\u00020\u0018H\u0007J2\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u0015\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u0018H\u0007J\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0015\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u0014H\u0007J\u0010\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u001aH\u0007J\u0010\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020\u001cH\u0007\u00a8\u0006&"}, d2 = {"Lcom/noisefit_nav_plus/di/WatchModule;", "", "()V", "provideBaseInitializeInterface", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "navPlusApplicationHandler", "Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "provideConnectionDataActions", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "navPlusConnectHandler", "Lcom/noisefit_nav_plus/handler/connect/NavPlusConnectHandler;", "provideDataConverter", "Lcom/noisefit_nav_plus/handler/dataConversion/DataConverter;", "appContext", "Landroid/content/Context;", "gson", "Lcom/google/gson/Gson;", "provideNavPlusApplicationHandler", "provideNavPlusConnectHandler", "provideNavPlusQueryDeviceUnitsHandler", "Lcom/noisefit_nav_plus/handler/NavPlusQueryDeviceUnitsHandler;", "dataConverter", "context", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "provideNavPlusUpdateDeviceUnitsHandler", "Lcom/noisefit_nav_plus/handler/NavPlusUpdateDeviceUnitsHandler;", "provideNavPlusUserActivityHandler", "Lcom/noisefit_nav_plus/handler/NavPlusUserActivityHandler;", "provideQueryDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "navPlusQueryDeviceUnitsHandler", "provideUpdateDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "navPlusUpdateDeviceUnitsHandler", "provideUserActivityDataActions", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "navPlusUserActivityHandler", "noisefit_nav_plus_debug"})
@dagger.Module
public final class WatchModule {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_nav_plus.di.WatchModule INSTANCE = null;
    
    private WatchModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_nav_plus.handler.dataConversion.DataConverter provideDataConverter(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_nav_plus.handler.NavPlusUpdateDeviceUnitsHandler provideNavPlusUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson, @org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_nav_plus.base.NavPlusApplicationHandler provideNavPlusApplicationHandler() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_nav_plus.handler.NavPlusQueryDeviceUnitsHandler provideNavPlusQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_nav_plus.handler.NavPlusUserActivityHandler provideNavPlusUserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_nav_plus.handler.connect.NavPlusConnectHandler provideNavPlusConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NavPlusUpdateDevice")
    public final com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions provideUpdateDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.NavPlusUpdateDeviceUnitsHandler navPlusUpdateDeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NavPlusConnectionDataActions")
    public final com.noisefit_commans.interfaces.connection.ConnectionDataActions provideConnectionDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.connect.NavPlusConnectHandler navPlusConnectHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NavPlusApplicationHandler")
    public final com.noisefit_commans.interfaces.base.BaseInitializeInterface provideBaseInitializeInterface(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NavPlusUserActivityDataActions")
    public final com.noisefit_commans.interfaces.data.UserActivityDataActions provideUserActivityDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.NavPlusUserActivityHandler navPlusUserActivityHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "NavPlusQueryDeviceDataActions")
    public final com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions provideQueryDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.handler.NavPlusQueryDeviceUnitsHandler navPlusQueryDeviceUnitsHandler) {
        return null;
    }
}