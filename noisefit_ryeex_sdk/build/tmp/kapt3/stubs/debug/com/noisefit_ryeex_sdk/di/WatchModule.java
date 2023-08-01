package com.noisefit_ryeex_sdk.di;

import java.lang.System;

@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\"\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\u0007J\u0018\u0010\u0013\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0007J\u0012\u0010\u0014\u001a\u00020\u00062\b\b\u0001\u0010\u0015\u001a\u00020\u000eH\u0007J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0007J*\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u001b\u001a\u00020\f2\b\b\u0001\u0010\u0015\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0012H\u0007J\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0007J*\u0010 \u001a\u00020\u001f2\u0006\u0010\u001b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0007J\u0010\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$H\u0007J\u0018\u0010%\u001a\u00020$2\u0006\u0010\u001b\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00a8\u0006&"}, d2 = {"Lcom/noisefit_ryeex_sdk/di/WatchModule;", "", "()V", "provideBaseInitializeInterface", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "ryeexApplicationHandler", "Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;", "provideConnectionDataActions", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "ryeexConnectHandler", "Lcom/noisefit_ryeex_sdk/handler/connect/RyeexConnectHandler;", "provideDataConverter", "Lcom/noisefit_ryeex_sdk/dataConversion/DataConverter;", "appContext", "Landroid/content/Context;", "gson", "Lcom/google/gson/Gson;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "provideRyeeexConnectHandler", "provideRyeexApplicationHandler", "context", "provideRyeexQueryDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "ryeexQueryDeviceUnitsHandler", "Lcom/noisefit_ryeex_sdk/handler/RyeexQueryDeviceUnitsHandler;", "provideRyeexQueryDeviceUnitsHandler", "dataConverter", "provideRyeexUpdateDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "ryeexUpdateDeviceUnitsHandler", "Lcom/noisefit_ryeex_sdk/handler/RyeexUpdateDeviceUnitsHandler;", "provideRyeexUpdateDeviceUnitsHandler", "provideRyeexUserActivityDataActions", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "ryeexUserActivityHandler", "Lcom/noisefit_ryeex_sdk/handler/RyeexUserActivityHandler;", "provideRyeexUserActivityHandler", "noisefit_ryeex_sdk_debug"})
@dagger.Module
public final class WatchModule {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_ryeex_sdk.di.WatchModule INSTANCE = null;
    
    private WatchModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_ryeex_sdk.dataConversion.DataConverter provideDataConverter(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_ryeex_sdk.handler.RyeexUpdateDeviceUnitsHandler provideRyeexUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_ryeex_sdk.base.RyeexApplicationHandler provideRyeexApplicationHandler(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_ryeex_sdk.handler.RyeexQueryDeviceUnitsHandler provideRyeexQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_ryeex_sdk.handler.RyeexUserActivityHandler provideRyeexUserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_ryeex_sdk.handler.connect.RyeexConnectHandler provideRyeeexConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "RyeexUpdateDevice")
    public final com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions provideRyeexUpdateDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.handler.RyeexUpdateDeviceUnitsHandler ryeexUpdateDeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ryeexConnectionDataActions")
    public final com.noisefit_commans.interfaces.connection.ConnectionDataActions provideConnectionDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.handler.connect.RyeexConnectHandler ryeexConnectHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ryeexApplicationHandler")
    public final com.noisefit_commans.interfaces.base.BaseInitializeInterface provideBaseInitializeInterface(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "RyeexUserActivityDataActions")
    public final com.noisefit_commans.interfaces.data.UserActivityDataActions provideRyeexUserActivityDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.handler.RyeexUserActivityHandler ryeexUserActivityHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "RyeexQueryDeviceDataActions")
    public final com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions provideRyeexQueryDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.handler.RyeexQueryDeviceUnitsHandler ryeexQueryDeviceUnitsHandler) {
        return null;
    }
}