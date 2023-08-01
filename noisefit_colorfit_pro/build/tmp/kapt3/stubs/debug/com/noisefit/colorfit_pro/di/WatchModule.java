package com.noisefit.colorfit_pro.di;

import java.lang.System;

@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0012\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000eH\u0007J\u0012\u0010\u000f\u001a\u00020\u00062\b\b\u0001\u0010\r\u001a\u00020\u000eH\u0007J\u0010\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0014H\u0007J*\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0016\u001a\u00020\f2\b\b\u0001\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u0019H\u0007J2\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0016\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u000e2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\u0019H\u0007J\u0018\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u0016\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u001bH\u0007J\u0010\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\u001dH\u0007\u00a8\u0006$"}, d2 = {"Lcom/noisefit/colorfit_pro/di/WatchModule;", "", "()V", "provideBaseInitializeInterface", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "proApplicationHandler", "Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "provideConnectionDataActions", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "proConnectHandler", "Lcom/noisefit/colorfit_pro/handler/connect/ProConnectHandler;", "provideDataConverter", "Lcom/noisefit/colorfit_pro/dataConversion/DataConverter;", "appContext", "Landroid/content/Context;", "provideProApplicationHandler", "provideProConnectHandler", "provideProDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/QueryDeviceDataActions;", "proQueryDeviceUnitsHandler", "Lcom/noisefit/colorfit_pro/handler/ProQueryDeviceUnitsHandler;", "provideProQueryDeviceUnitsHandler", "dataConverter", "context", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "provideProUpdateDeviceUnitsHandler", "Lcom/noisefit/colorfit_pro/handler/ProUpdateDeviceUnitsHandler;", "provideProUserActivityHandler", "Lcom/noisefit/colorfit_pro/handler/ProUserActivityHandler;", "provideUpdateDeviceDataActions", "Lcom/noisefit_commans/interfaces/device_data/UpdateDeviceDataActions;", "proUpdateDeviceUnitsHandler", "provideUserActivityDataActions", "Lcom/noisefit_commans/interfaces/data/UserActivityDataActions;", "proUserActivityHandler", "noisefit_colorfit_pro_debug"})
@dagger.Module
public final class WatchModule {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit.colorfit_pro.di.WatchModule INSTANCE = null;
    
    private WatchModule() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.colorfit_pro.dataConversion.DataConverter provideDataConverter(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.colorfit_pro.base.ProApplicationHandler provideProApplicationHandler(@org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ProApplicationHandler")
    public final com.noisefit_commans.interfaces.base.BaseInitializeInterface provideBaseInitializeInterface(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.colorfit_pro.handler.connect.ProConnectHandler provideProConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.colorfit_pro.handler.ProQueryDeviceUnitsHandler provideProQueryDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ProActivityDataActions")
    public final com.noisefit_commans.interfaces.data.UserActivityDataActions provideUserActivityDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.handler.ProUserActivityHandler proUserActivityHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.colorfit_pro.handler.ProUserActivityHandler provideProUserActivityHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ProConnectionDataActions")
    public final com.noisefit_commans.interfaces.connection.ConnectionDataActions provideConnectionDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.handler.connect.ProConnectHandler proConnectHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ProQueryDeviceDataActions")
    public final com.noisefit_commans.interfaces.device_data.QueryDeviceDataActions provideProDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.handler.ProQueryDeviceUnitsHandler proQueryDeviceUnitsHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    public final com.noisefit.colorfit_pro.handler.ProUpdateDeviceUnitsHandler provideProUpdateDeviceUnitsHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.dataConversion.DataConverter dataConverter, @org.jetbrains.annotations.NotNull
    @dagger.hilt.android.qualifiers.ApplicationContext
    android.content.Context appContext, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.handler.connect.ProConnectHandler proConnectHandler, @org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    @dagger.Provides
    @javax.inject.Singleton
    @javax.inject.Named(value = "ProUpdateDevice")
    public final com.noisefit_commans.interfaces.device_data.UpdateDeviceDataActions provideUpdateDeviceDataActions(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.handler.ProUpdateDeviceUnitsHandler proUpdateDeviceUnitsHandler) {
        return null;
    }
}