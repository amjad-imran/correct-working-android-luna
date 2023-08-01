package com.noisefit.hybrid.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001b\u0010\t\u001a\u00020\n\"\u0004\b\u0000\u0010\u000b2\u0006\u0010\f\u001a\u0002H\u000bH\u0016\u00a2\u0006\u0002\u0010\rJ\b\u0010\u000e\u001a\u00020\nH\u0016J\b\u0010\u000f\u001a\u00020\nH\u0016J\b\u0010\u0010\u001a\u00020\nH\u0016R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b\u00a8\u0006\u0011"}, d2 = {"Lcom/noisefit/hybrid/base/NFHybridApplicationHandler;", "Lcom/noisefit_commans/interfaces/base/BaseInitializeInterface;", "()V", "sdkInitStatus", "", "getSdkInitStatus", "()Z", "setSdkInitStatus", "(Z)V", "callbackListener", "", "T", "callback", "(Ljava/lang/Object;)V", "initSdk", "removeCallback", "unInitSdk", "noisefit_hybrid_debug"})
public final class NFHybridApplicationHandler extends com.noisefit_commans.interfaces.base.BaseInitializeInterface {
    private boolean sdkInitStatus = false;
    
    public NFHybridApplicationHandler() {
        super();
    }
    
    public final boolean getSdkInitStatus() {
        return false;
    }
    
    public final void setSdkInitStatus(boolean p0) {
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