package com.noisefit_cf2.handler.connect;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0010\t\n\u0002\b\u000b\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0018\u001a\u00020\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u0019H\u0002J\u001b\u0010\u001b\u001a\u00020\u0019\"\u0004\b\u0000\u0010\u001c2\u0006\u0010\u001d\u001a\u0002H\u001cH\u0016\u00a2\u0006\u0002\u0010\u001eJ\u001b\u0010\u001f\u001a\u00020\u0019\"\u0004\b\u0000\u0010\u001c2\u0006\u0010\u001d\u001a\u0002H\u001cH\u0016\u00a2\u0006\u0002\u0010\u001eJ\u0010\u0010 \u001a\u00020\u00192\u0006\u0010\u0014\u001a\u00020\u0015H\u0016J\u0010\u0010!\u001a\u00020\u00192\u0006\u0010\u0014\u001a\u00020\u0015H\u0016J\r\u0010\"\u001a\u00020#H\u0016\u00a2\u0006\u0002\u0010$J\b\u0010%\u001a\u00020\u0019H\u0016J\b\u0010&\u001a\u00020\u0013H\u0016J\u0010\u0010\'\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0016J\b\u0010(\u001a\u00020\u0019H\u0002J\u0018\u0010)\u001a\u00020\u00192\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010*\u001a\u00020\u0013H\u0016J\b\u0010+\u001a\u00020\u0019H\u0016J\b\u0010,\u001a\u00020\u0019H\u0002J\u0010\u0010-\u001a\u00020\u00192\u0006\u0010.\u001a\u00020/H\u0016J\b\u00100\u001a\u00020\u0019H\u0002R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00061"}, d2 = {"Lcom/noisefit_cf2/handler/connect/CF2ConnectHandler;", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "applicationHandler", "Lcom/noisefit_cf2/base/ColorFit2ApplicationHandler;", "(Lcom/noisefit_cf2/base/ColorFit2ApplicationHandler;)V", "getApplicationHandler", "()Lcom/noisefit_cf2/base/ColorFit2ApplicationHandler;", "baseConnectionCallbacks", "Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "bindCallBack", "Lcom/ido/ble/callback/BindCallBack$ICallBack;", "bindDeviceConnectDeviceCount", "", "bindDeviceConnectDeviceTotalTimes", "connectCallBack", "Lcom/ido/ble/callback/ConnectCallBack$ICallBack;", "dfuStateListener", "Lcom/ido/ble/dfu/BleDFUState$IListener;", "isReconnect", "", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "unbindCallBack", "Lcom/ido/ble/callback/UnbindCallBack$ICallBack;", "attachCallbacks", "", "bind", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "connect", "disconnect", "getConnectionTimerDelay", "", "()Ljava/lang/Long;", "init", "isConnected", "isDevicePaired", "onDeviceConnected", "reconnect", "type", "removeCallbacks", "setSportModePro3", "startDfuUpdate", "fileUri", "", "unbind", "noisefit_colorfit2_debug"})
public final class CF2ConnectHandler extends com.noisefit_commans.interfaces.connection.ConnectionDataActions {
    @org.jetbrains.annotations.NotNull
    private final com.noisefit_cf2.base.ColorFit2ApplicationHandler applicationHandler = null;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private boolean isReconnect = false;
    private com.noisefit_commans.interfaces.connection.ConnectionCallbacks baseConnectionCallbacks;
    private int bindDeviceConnectDeviceCount = 0;
    private int bindDeviceConnectDeviceTotalTimes = 4;
    private final com.ido.ble.callback.UnbindCallBack.ICallBack unbindCallBack = null;
    private final com.ido.ble.callback.ConnectCallBack.ICallBack connectCallBack = null;
    private final com.ido.ble.dfu.BleDFUState.IListener dfuStateListener = null;
    private final com.ido.ble.callback.BindCallBack.ICallBack bindCallBack = null;
    
    @javax.inject.Inject
    public CF2ConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_cf2.base.ColorFit2ApplicationHandler applicationHandler) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_cf2.base.ColorFit2ApplicationHandler getApplicationHandler() {
        return null;
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void connect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    @org.jetbrains.annotations.NotNull
    @java.lang.Override
    public java.lang.Long getConnectionTimerDelay() {
        return null;
    }
    
    @java.lang.Override
    public void disconnect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    @java.lang.Override
    public void reconnect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice, boolean type) {
    }
    
    @java.lang.Override
    public boolean isDevicePaired(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
        return false;
    }
    
    @java.lang.Override
    public boolean isConnected() {
        return false;
    }
    
    private final void bind() {
    }
    
    private final void unbind() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    private final void onDeviceConnected() {
    }
    
    @java.lang.Override
    public void startDfuUpdate(@org.jetbrains.annotations.NotNull
    java.lang.String fileUri) {
    }
    
    private final void setSportModePro3() {
    }
}