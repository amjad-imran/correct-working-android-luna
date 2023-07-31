package com.noisefit_evolve2.handler.connect;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0016\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0013\u001a\u00020\u0014H\u0016J\b\u0010\u0015\u001a\u00020\u0014H\u0002J\u001b\u0010\u0016\u001a\u00020\u0014\"\u0004\b\u0000\u0010\u00172\u0006\u0010\u0018\u001a\u0002H\u0017H\u0016\u00a2\u0006\u0002\u0010\u0019J\u001b\u0010\u001a\u001a\u00020\u0014\"\u0004\b\u0000\u0010\u00172\u0006\u0010\u0018\u001a\u0002H\u0017H\u0016\u00a2\u0006\u0002\u0010\u0019J\u0010\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u0011\u001a\u00020\u0012H\u0016J\u0010\u0010\u001c\u001a\u00020\u00142\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010\u001d\u001a\u00020\u0014H\u0002J\u0010\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u0011\u001a\u00020\u0012H\u0016J\b\u0010\u001f\u001a\u00020\u0014H\u0002J\b\u0010 \u001a\u00020\fH\u0016J\u0010\u0010!\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016J\b\u0010\"\u001a\u00020\fH\u0002J\u0018\u0010#\u001a\u00020\u00142\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010$\u001a\u00020\fH\u0016J\u000e\u0010%\u001a\u00020\f2\u0006\u0010&\u001a\u00020\u0006J\b\u0010\'\u001a\u00020\u0014H\u0016J\u0010\u0010(\u001a\u00020\u00142\u0006\u0010)\u001a\u00020\u0006H\u0016R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/noisefit_evolve2/handler/connect/Evolve2ConnectHandler;", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "evolve2ApplicationHandler", "Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;", "(Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;)V", "TAG", "", "baseConnectionCallbacks", "Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "getEvolve2ApplicationHandler", "()Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;", "isReconnect", "", "mClient", "Lcom/touchgui/sdk/TGClient;", "mConnectorListener", "Lcom/touchgui/sdk/TGConnectionListener;", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "attachCallbacks", "", "bindFailed", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "connect", "connectDevice", "connectSuccess", "disconnect", "disconnectSuccess", "isConnected", "isDevicePaired", "isReconnecting", "reconnect", "type", "removeBond", "address", "removeCallbacks", "startDfuUpdate", "fileUri", "noisefit_evolve2_debug"})
public final class Evolve2ConnectHandler extends com.noisefit_commans.interfaces.connection.ConnectionDataActions {
    @org.jetbrains.annotations.NotNull
    private final com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler = null;
    private com.noisefit_commans.interfaces.connection.ConnectionCallbacks baseConnectionCallbacks;
    private final java.lang.String TAG = null;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private com.touchgui.sdk.TGClient mClient;
    private boolean isReconnect = false;
    private final com.touchgui.sdk.TGConnectionListener mConnectorListener = null;
    
    @javax.inject.Inject
    public Evolve2ConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_evolve2.base.Evolve2ApplicationHandler evolve2ApplicationHandler) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.noisefit_evolve2.base.Evolve2ApplicationHandler getEvolve2ApplicationHandler() {
        return null;
    }
    
    @java.lang.Override
    public void attachCallbacks() {
    }
    
    @java.lang.Override
    public void removeCallbacks() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @java.lang.Override
    public void connect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    private final void connectDevice(com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
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
    
    private final void bindFailed() {
    }
    
    private final void disconnectSuccess() {
    }
    
    public final boolean removeBond(@org.jetbrains.annotations.NotNull
    java.lang.String address) {
        return false;
    }
    
    @java.lang.Override
    public boolean isConnected() {
        return false;
    }
    
    private final boolean isReconnecting() {
        return false;
    }
    
    @java.lang.Override
    public void startDfuUpdate(@org.jetbrains.annotations.NotNull
    java.lang.String fileUri) {
    }
}