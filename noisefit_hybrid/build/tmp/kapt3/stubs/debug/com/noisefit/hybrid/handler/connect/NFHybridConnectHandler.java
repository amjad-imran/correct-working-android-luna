package com.noisefit.hybrid.handler.connect;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0013\u0018\u0000 \"2\u00020\u0001:\u0001\"B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001b\u0010\u000f\u001a\u00020\u0010\"\u0004\b\u0000\u0010\u00112\u0006\u0010\u0012\u001a\u0002H\u0011H\u0016\u00a2\u0006\u0002\u0010\u0013J\u001b\u0010\u0014\u001a\u00020\u0010\"\u0004\b\u0000\u0010\u00112\u0006\u0010\u0012\u001a\u0002H\u0011H\u0016\u00a2\u0006\u0002\u0010\u0013J\b\u0010\u0015\u001a\u00020\u0010H\u0016J\u0010\u0010\u0016\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\fH\u0016J\b\u0010\u0017\u001a\u00020\u0010H\u0002J\u0010\u0010\u0018\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\fH\u0016J\b\u0010\u0019\u001a\u00020\u0010H\u0002J\b\u0010\u001a\u001a\u00020\u0010H\u0016J\b\u0010\u001b\u001a\u00020\nH\u0016J\u0010\u0010\u001c\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0016J\b\u0010\u001d\u001a\u00020\u0010H\u0016J\u0006\u0010\u001e\u001a\u00020\u0010J\u0018\u0010\u001f\u001a\u00020\u00102\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010 \u001a\u00020\nH\u0016J\b\u0010!\u001a\u00020\u0010H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/noisefit/hybrid/handler/connect/NFHybridConnectHandler;", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "applicationHandler", "Lcom/noisefit/hybrid/base/NFHybridApplicationHandler;", "(Lcom/noisefit/hybrid/base/NFHybridApplicationHandler;)V", "TAG", "", "baseConnectionCallbacks", "Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "isReconnect", "", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "resultCallBack", "Lcn/appscomm/bluetoothsdk/interfaces/ResultCallBack;", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "checkWatchBindStatus", "connect", "connectSuccess", "disconnect", "disconnectSuccess", "init", "isConnected", "isDevicePaired", "onConnectedQRBinding", "onQRCodeBind", "reconnect", "type", "restoreDevice", "Companion", "noisefit_hybrid_debug"})
public final class NFHybridConnectHandler extends com.noisefit_commans.interfaces.connection.ConnectionDataActions {
    private final com.noisefit.hybrid.base.NFHybridApplicationHandler applicationHandler = null;
    private com.noisefit_commans.interfaces.connection.ConnectionCallbacks baseConnectionCallbacks;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private boolean isReconnect = false;
    private final java.lang.String TAG = "NFHybridConnectHandler";
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit.hybrid.handler.connect.NFHybridConnectHandler.Companion Companion = null;
    private static boolean qrCodeBind = false;
    private final cn.appscomm.bluetoothsdk.interfaces.ResultCallBack resultCallBack = null;
    
    @javax.inject.Inject
    public NFHybridConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.hybrid.base.NFHybridApplicationHandler applicationHandler) {
        super();
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
    
    @java.lang.Override
    public void checkWatchBindStatus() {
    }
    
    @java.lang.Override
    public void onConnectedQRBinding() {
    }
    
    private final void connectSuccess() {
    }
    
    private final void disconnectSuccess() {
    }
    
    @java.lang.Override
    public void disconnect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    private final void restoreDevice() {
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
    
    public final void onQRCodeBind() {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/noisefit/hybrid/handler/connect/NFHybridConnectHandler$Companion;", "", "()V", "qrCodeBind", "", "noisefit_hybrid_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}