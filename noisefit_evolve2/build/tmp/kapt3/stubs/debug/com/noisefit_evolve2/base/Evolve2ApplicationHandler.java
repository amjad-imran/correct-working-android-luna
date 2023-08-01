package com.noisefit_evolve2.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001b\u0010\t\u001a\u00020\n\"\u0004\b\u0000\u0010\u000b2\u0006\u0010\f\u001a\u0002H\u000bH\u0016\u00a2\u0006\u0002\u0010\rJ\b\u0010\u000e\u001a\u00020\u000fH\u0002J\b\u0010\u0010\u001a\u0004\u0018\u00010\bJ\b\u0010\u0011\u001a\u00020\nH\u0016J\b\u0010\u0012\u001a\u0004\u0018\u00010\bJ\b\u0010\u0013\u001a\u00020\nH\u0016J\b\u0010\u0014\u001a\u00020\nH\u0016R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/noisefit_evolve2/base/Evolve2ApplicationHandler;", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "baseInitializeCallbacks", "Lcom/noisefit_commans/interfaces/base/BaseInitializeCallbacks;", "mClient", "Lcom/touchgui/sdk/TGClient;", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "getServiceIntent", "Landroid/content/Intent;", "getTGBleClient", "initSdk", "openBleService", "removeCallback", "unInitSdk", "noisefit_evolve2_debug"})
public final class Evolve2ApplicationHandler extends com.noisefit_commans.interfaces.base.BaseInitializeInterface {
    private final android.content.Context context = null;
    private com.noisefit_commans.interfaces.base.BaseInitializeCallbacks baseInitializeCallbacks;
    private com.touchgui.sdk.TGClient mClient;
    
    @javax.inject.Inject
    public Evolve2ApplicationHandler(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public void removeCallback() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.touchgui.sdk.TGClient getTGBleClient() {
        return null;
    }
    
    private final android.content.Intent getServiceIntent() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.touchgui.sdk.TGClient openBleService() {
        return null;
    }
    
    @java.lang.Override
    public void initSdk() {
    }
    
    @java.lang.Override
    public void unInitSdk() {
    }
}