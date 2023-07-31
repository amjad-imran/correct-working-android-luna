package com.noisefit_nav_plus.handler.connect;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0016\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0017\u001a\u00020\u0018H\u0016J\b\u0010\u0019\u001a\u00020\u0018H\u0002J\b\u0010\u001a\u001a\u00020\u0018H\u0002J\u001b\u0010\u001b\u001a\u00020\u0018\"\u0004\b\u0000\u0010\u001c2\u0006\u0010\u001d\u001a\u0002H\u001cH\u0016\u00a2\u0006\u0002\u0010\u001eJ\u001b\u0010\u001f\u001a\u00020\u0018\"\u0004\b\u0000\u0010\u001c2\u0006\u0010\u001d\u001a\u0002H\u001cH\u0016\u00a2\u0006\u0002\u0010\u001eJ\u0010\u0010 \u001a\u00020\u00182\u0006\u0010\u0015\u001a\u00020\u0016H\u0016J\u0010\u0010!\u001a\u00020\u00182\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\b\u0010\"\u001a\u00020\u0018H\u0002J\u0010\u0010#\u001a\u00020\u00182\u0006\u0010\u0015\u001a\u00020\u0016H\u0016J\b\u0010$\u001a\u00020\u0018H\u0002J\b\u0010%\u001a\u00020\u0018H\u0016J\b\u0010&\u001a\u00020\u0018H\u0002J\b\u0010\'\u001a\u00020\u0018H\u0002J\b\u0010(\u001a\u00020\rH\u0016J\u0010\u0010)\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u0016H\u0016J\b\u0010*\u001a\u00020\u0018H\u0016J\u0018\u0010+\u001a\u00020\u00182\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010,\u001a\u00020\rH\u0016J\b\u0010-\u001a\u00020\u0018H\u0016R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/noisefit_nav_plus/handler/connect/NavPlusConnectHandler;", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "navPlusApplicationHandler", "Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "(Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;)V", "TAG", "", "baseConnectionCallbacks", "Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "bindDeviceConnectDeviceCount", "", "bindDeviceConnectDeviceTotalTimes", "isDisconnect", "", "isReconnect", "mBleService", "Lcom/zjw/zhbraceletsdk/service/ZhBraceletService;", "mConnectorListener", "Lcom/zjw/zhbraceletsdk/linstener/ConnectorListener;", "getNavPlusApplicationHandler", "()Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "attachCallbacks", "", "attachListener", "bindFailed", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "connect", "connectDevice", "connectSuccess", "disconnect", "disconnectSuccess", "init", "initBindListener", "initDeviceUnbindListener", "isConnected", "isDevicePaired", "onConnectedQRBinding", "reconnect", "type", "removeCallbacks", "noisefit_nav_plus_debug"})
public final class NavPlusConnectHandler extends com.noisefit_commans.interfaces.connection.ConnectionDataActions {
    @org.jetbrains.annotations.NotNull
    private final com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler = null;
    private final java.lang.String TAG = "NavPlusConnectHandler";
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private com.zjw.zhbraceletsdk.service.ZhBraceletService mBleService;
    private boolean isReconnect = false;
    private boolean isDisconnect = false;
    private com.noisefit_commans.interfaces.connection.ConnectionCallbacks baseConnectionCallbacks;
    private final int bindDeviceConnectDeviceTotalTimes = 4;
    private int bindDeviceConnectDeviceCount = 0;
    private final com.zjw.zhbraceletsdk.linstener.ConnectorListener mConnectorListener = null;
    
    @javax.inject.Inject
    public NavPlusConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_nav_plus.base.NavPlusApplicationHandler navPlusApplicationHandler) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_nav_plus.base.NavPlusApplicationHandler getNavPlusApplicationHandler() {
        return null;
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public void init() {
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    private final void attachListener() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    private final void connectDevice(com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    @java.lang.Override
    public void connect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    private final void bindFailed() {
    }
    
    private final void initBindListener() {
    }
    
    private final void initDeviceUnbindListener() {
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
    
    private final void connectSuccess() {
    }
    
    private final void disconnectSuccess() {
    }
    
    @java.lang.Override
    public boolean isConnected() {
        return false;
    }
    
    @java.lang.Override
    public void onConnectedQRBinding() {
    }
}