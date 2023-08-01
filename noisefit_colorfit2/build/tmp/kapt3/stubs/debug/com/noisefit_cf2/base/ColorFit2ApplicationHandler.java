package com.noisefit_cf2.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001b\u0010\u0007\u001a\u00020\b\"\u0004\b\u0000\u0010\t2\u0006\u0010\n\u001a\u0002H\tH\u0016\u00a2\u0006\u0002\u0010\u000bJ\b\u0010\f\u001a\u00020\bH\u0016J\b\u0010\r\u001a\u00020\bH\u0016J\b\u0010\u000e\u001a\u00020\bH\u0016R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/noisefit_cf2/base/ColorFit2ApplicationHandler;", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "watchDataStore", "Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;", "(Lcom/noisefit_commans/data/local/abstraction/WatchDataStore;)V", "sdkInitStatus", "", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "initSdk", "removeCallback", "unInitSdk", "noisefit_colorfit2_debug"})
public final class ColorFit2ApplicationHandler extends com.noisefit_commans.interfaces.base.BaseInitializeInterface {
    private com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore;
    private boolean sdkInitStatus = false;
    
    @javax.inject.Inject
    public ColorFit2ApplicationHandler(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.data.local.abstraction.WatchDataStore watchDataStore) {
        super();
    }
    
    @java.lang.Override
    public void initSdk() {
    }
    
    @java.lang.Override
    public void removeCallback() {
    }
    
    @java.lang.Override
    public void unInitSdk() {
    }
    
    @java.lang.Override
    public <T extends java.lang.Object>void callbackListener(T callback) {
    }
}