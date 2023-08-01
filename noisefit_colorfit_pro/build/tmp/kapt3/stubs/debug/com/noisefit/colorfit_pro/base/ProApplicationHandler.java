package com.noisefit.colorfit_pro.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001b\u0010\r\u001a\u00020\u000e\"\u0004\b\u0000\u0010\u000f2\u0006\u0010\u0010\u001a\u0002H\u000fH\u0016\u00a2\u0006\u0002\u0010\u0011J\u0006\u0010\u0012\u001a\u00020\fJ\u0012\u0010\u0013\u001a\u0004\u0018\u00010\n2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015J\b\u0010\u0016\u001a\u0004\u0018\u00010\nJ\b\u0010\u0017\u001a\u00020\u000eH\u0016J\b\u0010\u0018\u001a\u00020\u000eH\u0016J\u0006\u0010\u0019\u001a\u00020\u000eJ\b\u0010\u001a\u001a\u00020\u000eH\u0016R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/noisefit/colorfit_pro/base/ProApplicationHandler;", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "appContext", "Landroid/content/Context;", "(Landroid/content/Context;)V", "getAppContext", "()Landroid/content/Context;", "baseInitializeCallbacks", "Lcom/noisefit_commans/interfaces/base/BaseInitializeCallbacks;", "bleDevice", "Lcom/crrepa/ble/conn/CRPBleDevice;", "crpBleClient", "Lcom/crrepa/ble/CRPBleClient;", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "getBleClient", "getBleDevice", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "getBleDeviceNullable", "initSdk", "removeCallback", "reset", "unInitSdk", "noisefit_colorfit_pro_debug"})
public final class ProApplicationHandler extends com.noisefit_commans.interfaces.base.BaseInitializeInterface {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context appContext = null;
    private com.crrepa.ble.CRPBleClient crpBleClient;
    private com.noisefit_commans.interfaces.base.BaseInitializeCallbacks baseInitializeCallbacks;
    private com.crrepa.ble.conn.CRPBleDevice bleDevice;
    
    @javax.inject.Inject
    public ProApplicationHandler(@org.jetbrains.annotations.NotNull
    android.content.Context appContext) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final android.content.Context getAppContext() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.crrepa.ble.conn.CRPBleDevice getBleDevice(@org.jetbrains.annotations.Nullable
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice) {
        return null;
    }
    
    @java.lang.Override
    public void removeCallback() {
    }
    
    public final void reset() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.crrepa.ble.conn.CRPBleDevice getBleDeviceNullable() {
        return null;
    }
    
    @java.lang.Override
    public void initSdk() {
    }
    
    @java.lang.Override
    public void unInitSdk() {
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.crrepa.ble.CRPBleClient getBleClient() {
        return null;
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
}