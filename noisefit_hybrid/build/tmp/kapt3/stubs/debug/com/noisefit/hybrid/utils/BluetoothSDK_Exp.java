package com.noisefit.hybrid.utils;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001:\u0002\u000e\u000fB\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\u0010\t\u001a\u0004\u0018\u00010\nJ\u0010\u0010\u000b\u001a\u00020\u00042\b\u0010\t\u001a\u0004\u0018\u00010\fJ\u0010\u0010\r\u001a\u00020\u00042\b\u0010\t\u001a\u0004\u0018\u00010\n\u00a8\u0006\u0010"}, d2 = {"Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp;", "", "()V", "editCustomizeReply", "", "index", "", "text", "", "callback", "Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp$BoolCallback;", "getCustomizeReply", "Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp$CustomReplyCallback;", "jumpOutTakePhoto", "BoolCallback", "CustomReplyCallback", "noisefit_hybrid_debug"})
public final class BluetoothSDK_Exp {
    
    @javax.inject.Inject
    public BluetoothSDK_Exp() {
        super();
    }
    
    public final void jumpOutTakePhoto(@org.jetbrains.annotations.Nullable
    com.noisefit.hybrid.utils.BluetoothSDK_Exp.BoolCallback callback) {
    }
    
    public final void editCustomizeReply(int index, @org.jetbrains.annotations.Nullable
    java.lang.String text, @org.jetbrains.annotations.Nullable
    com.noisefit.hybrid.utils.BluetoothSDK_Exp.BoolCallback callback) {
    }
    
    public final void getCustomizeReply(@org.jetbrains.annotations.Nullable
    com.noisefit.hybrid.utils.BluetoothSDK_Exp.CustomReplyCallback callback) {
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\u0004H&\u00a8\u0006\b"}, d2 = {"Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp$BoolCallback;", "", "()V", "onFail", "", "code", "", "onSuccess", "noisefit_hybrid_debug"})
    public static abstract class BoolCallback {
        
        public BoolCallback() {
            super();
        }
        
        public abstract void onSuccess();
        
        public abstract void onFail(int code);
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H&J\u0018\u0010\u0007\u001a\u00020\u00042\u000e\u0010\b\u001a\n\u0012\u0004\u0012\u00020\n\u0018\u00010\tH&\u00a8\u0006\u000b"}, d2 = {"Lcom/noisefit/hybrid/utils/BluetoothSDK_Exp$CustomReplyCallback;", "", "()V", "onFail", "", "code", "", "onSuccess", "customizeReplyList", "", "Lcn/appscomm/bluetooth/mode/Customize;", "noisefit_hybrid_debug"})
    public static abstract class CustomReplyCallback {
        
        public CustomReplyCallback() {
            super();
        }
        
        public abstract void onSuccess(@org.jetbrains.annotations.Nullable
        java.util.List<? extends cn.appscomm.bluetooth.mode.Customize> customizeReplyList);
        
        public abstract void onFail(int code);
    }
}