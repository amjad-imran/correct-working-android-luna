package com.noisefit_ryeex_sdk.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001b\u0010\u0011\u001a\u00020\u0012\"\u0004\b\u0000\u0010\u00132\u0006\u0010\u0014\u001a\u0002H\u0013H\u0016\u00a2\u0006\u0002\u0010\u0015J\b\u0010\u0016\u001a\u0004\u0018\u00010\u0010J\b\u0010\u0017\u001a\u00020\u0012H\u0016J\b\u0010\u0018\u001a\u00020\u0012H\u0016J\u0010\u0010\u0019\u001a\u00020\u00122\b\u0010\u001a\u001a\u0004\u0018\u00010\u0010J\b\u0010\u001b\u001a\u00020\u0012H\u0016R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/noisefit_ryeex_sdk/base/RyeexApplicationHandler;", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "baseInitializeCallbacks", "Lcom/noisefit_commans/interfaces/base/BaseInitializeCallbacks;", "connectionCallbacks", "Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "getConnectionCallbacks", "()Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "setConnectionCallbacks", "(Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;)V", "isInitSDK", "", "watchDevice", "Lcom/ryeex/watch/adapter/device/WatchDevice;", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "getWatchDevice", "initSdk", "removeCallback", "setWatchDevice", "bindingDevice", "unInitSdk", "noisefit_ryeex_sdk_debug"})
public final class RyeexApplicationHandler extends com.noisefit_commans.interfaces.base.BaseInitializeInterface {
    private final android.content.Context context = null;
    private boolean isInitSDK = false;
    private com.noisefit_commans.interfaces.base.BaseInitializeCallbacks baseInitializeCallbacks;
    private com.ryeex.watch.adapter.device.WatchDevice watchDevice;
    @org.jetbrains.annotations.Nullable
    private com.noisefit_commans.interfaces.connection.ConnectionCallbacks connectionCallbacks;
    
    @javax.inject.Inject
    public RyeexApplicationHandler(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.noisefit_commans.interfaces.connection.ConnectionCallbacks getConnectionCallbacks() {
        return null;
    }
    
    public final void setConnectionCallbacks(@org.jetbrains.annotations.Nullable
    com.noisefit_commans.interfaces.connection.ConnectionCallbacks p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.ryeex.watch.adapter.device.WatchDevice getWatchDevice() {
        return null;
    }
    
    @java.lang.Override
    public void removeCallback() {
    }
    
    public final void setWatchDevice(@org.jetbrains.annotations.Nullable
    com.ryeex.watch.adapter.device.WatchDevice bindingDevice) {
    }
    
    @java.lang.Override
    public void initSdk() {
    }
    
    @java.lang.Override
    public void unInitSdk() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
}