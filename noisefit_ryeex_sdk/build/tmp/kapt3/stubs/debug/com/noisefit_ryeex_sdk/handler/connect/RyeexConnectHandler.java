package com.noisefit_ryeex_sdk.handler.connect;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\u001b\u0010\u0012\u001a\u00020\u0011\"\u0004\b\u0000\u0010\u00132\u0006\u0010\u0014\u001a\u0002H\u0013H\u0016\u00a2\u0006\u0002\u0010\u0015J\u001b\u0010\u0016\u001a\u00020\u0011\"\u0004\b\u0000\u0010\u00132\u0006\u0010\u0014\u001a\u0002H\u0013H\u0016\u00a2\u0006\u0002\u0010\u0015J\u0010\u0010\u0017\u001a\u00020\u00112\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\b\u0010\u0018\u001a\u00020\u0011H\u0003J\u0010\u0010\u0019\u001a\u00020\u00112\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\b\u0010\u001a\u001a\u00020\u0011H\u0002J\b\u0010\u001b\u001a\u00020\fH\u0016J\u0010\u0010\u001c\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\u0010\u0010\u001d\u001a\u00020\u00112\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u0018\u0010\u001e\u001a\u00020\u00112\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u001f\u001a\u00020\fH\u0016J\u0012\u0010 \u001a\u00020\u00112\b\u0010!\u001a\u0004\u0018\u00010\"H\u0002R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/noisefit_ryeex_sdk/handler/connect/RyeexConnectHandler;", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "ryeexApplicationHandler", "Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "baseConnectionCallbacks", "Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "btHelper", "Lcom/ryeex/watch/bt/BTHelper;", "isDisconnect", "", "isReconnect", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "bindFailed", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "connect", "connectSuccess", "disconnect", "disconnectSuccess", "isConnected", "isDevicePaired", "reConnectDevice", "reconnect", "type", "setConnectListener", "watchDevice", "Lcom/ryeex/watch/adapter/device/WatchDevice;", "noisefit_ryeex_sdk_debug"})
public final class RyeexConnectHandler extends com.noisefit_commans.interfaces.connection.ConnectionDataActions {
    private final com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler = null;
    private final com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore = null;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private boolean isReconnect = false;
    private boolean isDisconnect = false;
    private com.noisefit_commans.interfaces.connection.ConnectionCallbacks baseConnectionCallbacks;
    private com.ryeex.watch.bt.BTHelper btHelper;
    
    @javax.inject.Inject
    public RyeexConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_ryeex_sdk.base.RyeexApplicationHandler ryeexApplicationHandler, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @java.lang.Override
    public void connect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    @java.lang.Override
    public void disconnect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    @java.lang.Override
    public void reconnect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice, boolean type) {
    }
    
    private final void reConnectDevice(com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    private final void setConnectListener(com.ryeex.watch.adapter.device.WatchDevice watchDevice) {
    }
    
    @java.lang.Override
    public boolean isDevicePaired(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
        return false;
    }
    
    @android.annotation.SuppressLint(value = {"MissingPermission"})
    private final void connectSuccess() {
    }
    
    private final void disconnectSuccess() {
    }
    
    @java.lang.Override
    public boolean isConnected() {
        return false;
    }
    
    private final void bindFailed() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
}