package com.noisefit_nav_plus.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u001b\u0010\t\u001a\u00020\n\"\u0004\b\u0000\u0010\u000b2\u0006\u0010\f\u001a\u0002H\u000bH\u0016\u00a2\u0006\u0002\u0010\rJ\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fJ\b\u0010\u0010\u001a\u00020\nH\u0016J\u0010\u0010\u0011\u001a\u00020\n2\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013J\b\u0010\u0014\u001a\u00020\nH\u0016J\b\u0010\u0015\u001a\u00020\nH\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/noisefit_nav_plus/base/NavPlusApplicationHandler;", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "()V", "TAG", "", "baseInitializeCallbacks", "Lcom/noisefit_commans/interfaces/base/BaseInitializeCallbacks;", "isInitialized", "", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "getZhBraceletService", "Lcom/zjw/zhbraceletsdk/service/ZhBraceletService;", "initSdk", "openBleService", "listener", "Lcom/zjw/zhbraceletsdk/linstener/ZHInitStatusListener;", "removeCallback", "unInitSdk", "noisefit_nav_plus_debug"})
public final class NavPlusApplicationHandler extends com.noisefit_commans.interfaces.base.BaseInitializeInterface {
    private final java.lang.String TAG = "NavPlusApplicationHandler";
    private com.noisefit_commans.interfaces.base.BaseInitializeCallbacks baseInitializeCallbacks;
    private boolean isInitialized = false;
    
    @javax.inject.Inject
    public NavPlusApplicationHandler() {
        super();
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
    
    @java.lang.Override
    public void removeCallback() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.zjw.zhbraceletsdk.service.ZhBraceletService getZhBraceletService() {
        return null;
    }
    
    public final void openBleService(@org.jetbrains.annotations.Nullable
    com.zjw.zhbraceletsdk.linstener.ZHInitStatusListener listener) {
    }
    
    @java.lang.Override
    public void initSdk() {
    }
    
    @java.lang.Override
    public void unInitSdk() {
    }
}