package com.noisefit_evolve2.di;

import java.lang.System;

@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\u001a\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0007J\u0012\u0010\u0011\u001a\u00020\u00062\b\b\u0001\u0010\u0012\u001a\u00020\u000eH\u0007J\u0010\u0010\u0013\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J*\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\f2\b\b\u0001\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u0017\u001a\u00020\u0018H\u0007J*\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u0016\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0017\u001a\u00020\u0018H\u0007J\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0016\u001a\u00020\f2\u0006\u0010\u001d\u001a\u00020\u0006H\u0007J\u0010\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u0015H\u0007J\u0010\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\u001aH\u0007J\u0010\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\u001cH\u0007\u00a8\u0006\'"}, d2 = {"Lcom/noisefit_evolve2/di/WatchModule;", "", "()V", "provideBaseInitializeInterface", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "evolve2ApplicationHandler", "Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;", "provideConnectionDataActions", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "evolve2ConnectHandler", "Lcom/noisefit_evolve2/handler/connect/Evolve2ConnectHandler;", "provideDataConverter", "Lcom/noisefit_evolve2/dataConversion/DataConverter;", "appContext", "Landroid/content/Context;", "gson", "Lcom/google/gson/Gson;", "provideEvolve2ApplicationHandler", "context", "provideEvolve2ConnectHandler", "provideEvolve2QueryDeviceUnitsHandler", "Lcom/noisefit_evolve2/handler/Evolve2QueryDeviceUnitsHandler;", "dataConverter", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "provideEvolve2UpdateDeviceUnitsHandler", "Lcom/noisefit_evolve2/handler/Evolve2UpdateDeviceUnitsHandler;", "provideEvolve2UserActivityHandler", "Lcom/noisefit_evolve2/handler/Evolve2UserActivityHandler;", "evolveApplicationHandler", "provideEvolveQueryDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "evolve2QueryDeviceUnitsHandler", "provideEvolveUpdateDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "evolveUpdateDeviceUnitsHandler", "provideEvolveUserActivityDataActions", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "evolve2UserActivityHandler", "noisefit_evolve2_debug"})
@dagger.Module
public final class WatchModule {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_evolve2.di.WatchModule INSTANCE = null;
    
    private WatchModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_evolve2.dataConversion.DataConverter provideDataConverter(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.google.gson.Gson gson) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_evolve2.handler.Evolve2UpdateDeviceUnitsHandler provideEvolve2UpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_evolve2.base.Evolve2ApplicationHandler provideEvolve2ApplicationHandler(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_evolve2.handler.Evolve2QueryDeviceUnitsHandler provideEvolve2QueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_evolve2.handler.Evolve2UserActivityHandler provideEvolve2UserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolveApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit_evolve2.handler.connect.Evolve2ConnectHandler provideEvolve2ConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "EvolveUpdateDevice")
    public final com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions provideEvolveUpdateDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.handler.Evolve2UpdateDeviceUnitsHandler evolveUpdateDeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "evolveConnectionDataActions")
    public final com.noisefit_commans.interfaces.connection.ConnectionDataActions provideConnectionDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.handler.connect.Evolve2ConnectHandler evolve2ConnectHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "evolveApplicationHandler")
    public final com.noisefit_commans.interfaces.base.BaseInitializeInterface provideBaseInitializeInterface(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "EvolveUserActivityDataActions")
    public final com.noisefit_commans.interfaces.data.UserActivityDataActions provideEvolveUserActivityDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.handler.Evolve2UserActivityHandler evolve2UserActivityHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "EvolveQueryDeviceDataActions")
    public final com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions provideEvolveQueryDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.handler.Evolve2QueryDeviceUnitsHandler evolve2QueryDeviceUnitsHandler) {
        return null;
    }
}