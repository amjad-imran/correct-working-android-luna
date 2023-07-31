package com.noisefit.colorfit_pro.handler.connect;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u0000 02\u00020\u0001:\u00010B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0015\u001a\u00020\u0016H\u0002J\u001b\u0010\u0017\u001a\u00020\u0016\"\u0004\b\u0000\u0010\u00182\u0006\u0010\u0019\u001a\u0002H\u0018H\u0016\u00a2\u0006\u0002\u0010\u001aJ\u001b\u0010\u001b\u001a\u00020\u0016\"\u0004\b\u0000\u0010\u00182\u0006\u0010\u0019\u001a\u0002H\u0018H\u0016\u00a2\u0006\u0002\u0010\u001aJ\b\u0010\u001c\u001a\u00020\u000fH\u0002J\b\u0010\u001d\u001a\u00020\u0016H\u0002J\b\u0010\u001e\u001a\u00020\u0016H\u0002J\b\u0010\u001f\u001a\u00020\u0016H\u0002J\u0010\u0010 \u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\u0014H\u0016J\b\u0010!\u001a\u00020\u0016H\u0003J\u0012\u0010\"\u001a\u00020\u00162\b\u0010#\u001a\u0004\u0018\u00010$H\u0002J\u0010\u0010\"\u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\u0014H\u0016J\b\u0010%\u001a\u00020\u0016H\u0016J\u000f\u0010&\u001a\u0004\u0018\u00010\u0006H\u0016\u00a2\u0006\u0002\u0010\'J\b\u0010(\u001a\u00020\u0016H\u0002J\b\u0010)\u001a\u00020\u000fH\u0002J\b\u0010*\u001a\u00020\u000fH\u0016J\b\u0010+\u001a\u00020\u000fH\u0002J\u0010\u0010,\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u0014H\u0016J\u0018\u0010-\u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010.\u001a\u00020\u000fH\u0016J\u0010\u0010/\u001a\u00020\u00162\u0006\u0010\u0010\u001a\u00020\u000fH\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\n \t*\u0004\u0018\u00010\b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u00061"}, d2 = {"Lcom/noisefit/colorfit_pro/handler/connect/ProConnectHandler;", "Lcom/noisefit_commans/interfaces/connection/ConnectionDataActions;", "proApplicationHandler", "Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "(Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;)V", "RECONNECTION_DELAY", "", "TAG", "", "kotlin.jvm.PlatformType", "baseConnectionCallbacks", "Lcom/noisefit_commans/interfaces/connection/ConnectionCallbacks;", "bleConnectionStateListener", "Lcom/crrepa/ble/conn/listener/CRPBleConnectionStateListener;", "connected", "", "connecting", "isDisconnectClicked", "isReconnect", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "btBindStatus", "", "callbackListener", "T", "callback", "(Ljava/lang/Object;)V", "callbackListenerNew", "canConnect", "checkForDisconnectState", "clearConnectState", "closeGatt", "connect", "delayConnect", "disconnect", "bleDevice", "Lcom/crrepa/ble/conn/CRPBleDevice;", "disconnectFromService", "getConnectionTimerDelay", "()Ljava/lang/Long;", "handleConnected", "isBluetoothEnable", "isConnected", "isConnecting", "isDevicePaired", "reconnect", "type", "setConnecting", "Companion", "noisefit_colorfit_pro_debug"})
public final class ProConnectHandler extends com.noisefit_commans.interfaces.connection.ConnectionDataActions {
    private final com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler = null;
    private final java.lang.String TAG = null;
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit.colorfit_pro.handler.connect.ProConnectHandler.Companion Companion = null;
    @org.jetbrains.annotations.Nullable
    private static com.crrepa.ble.conn.CRPBleConnection bleConnection;
    private com.noisefit_commans.interfaces.connection.ConnectionCallbacks baseConnectionCallbacks;
    private com.noisefit_commans.models.ColorFitDevice noiseFitDevice;
    private final long RECONNECTION_DELAY = 3000L;
    private boolean connecting = false;
    private boolean connected = false;
    private boolean isReconnect = false;
    private boolean isDisconnectClicked = false;
    private final com.crrepa.ble.conn.listener.CRPBleConnectionStateListener bleConnectionStateListener = null;
    
    @javax.inject.Inject
    public ProConnectHandler(@org.jetbrains.annotations.NotNull
    com.noisefit.colorfit_pro.base.ProApplicationHandler proApplicationHandler) {
        super();
    }
    
    @java.lang.Override
    public void disconnect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    @org.jetbrains.annotations.Nullable
    @java.lang.Override
    public java.lang.Long getConnectionTimerDelay() {
        return null;
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
    
    private final boolean isBluetoothEnable() {
        return false;
    }
    
    private final void closeGatt() {
    }
    
    @android.annotation.SuppressLint(value = {"CheckResult"})
    private final void delayConnect() {
    }
    
    @java.lang.Override
    public boolean isConnected() {
        return false;
    }
    
    private final void disconnect(com.crrepa.ble.conn.CRPBleDevice bleDevice) {
    }
    
    private final void btBindStatus() {
    }
    
    private final void checkForDisconnectState() {
    }
    
    @java.lang.Override
    public void connect(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
    }
    
    private final boolean canConnect() {
        return false;
    }
    
    private final void handleConnected() {
    }
    
    private final void clearConnectState() {
    }
    
    private final boolean isConnecting() {
        return false;
    }
    
    @java.lang.Override
    public void disconnectFromService() {
    }
    
    private final void setConnecting(boolean connecting) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListenerNew(T callback) {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/noisefit/colorfit_pro/handler/connect/ProConnectHandler$Companion;", "", "()V", "bleConnection", "Lcom/crrepa/ble/conn/CRPBleConnection;", "getBleConnection", "()Lcom/crrepa/ble/conn/CRPBleConnection;", "setBleConnection", "(Lcom/crrepa/ble/conn/CRPBleConnection;)V", "noisefit_colorfit_pro_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable
        public final com.crrepa.ble.conn.CRPBleConnection getBleConnection() {
            return null;
        }
        
        public final void setBleConnection(@org.jetbrains.annotations.Nullable
        com.crrepa.ble.conn.CRPBleConnection p0) {
        }
    }
}