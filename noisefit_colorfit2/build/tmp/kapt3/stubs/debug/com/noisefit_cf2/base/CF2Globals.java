package com.noisefit_cf2.base;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0007\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001c\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001c\u0010\t\u001a\u0004\u0018\u00010\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001e\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u0010\n\u0002\u0010\u0014\u001a\u0004\b\u000f\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u001e\u0010\u0015\u001a\u0004\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u0010\n\u0002\u0010\u0014\u001a\u0004\b\u0015\u0010\u0011\"\u0004\b\u0016\u0010\u0013\u00a8\u0006\u0017"}, d2 = {"Lcom/noisefit_cf2/base/CF2Globals;", "", "()V", "basicInfo", "Lcom/ido/ble/protocol/model/BasicInfo;", "getBasicInfo", "()Lcom/ido/ble/protocol/model/BasicInfo;", "setBasicInfo", "(Lcom/ido/ble/protocol/model/BasicInfo;)V", "firmwareUrl", "", "getFirmwareUrl", "()Ljava/lang/String;", "setFirmwareUrl", "(Ljava/lang/String;)V", "isCloudDialSupport", "", "()Ljava/lang/Boolean;", "setCloudDialSupport", "(Ljava/lang/Boolean;)V", "Ljava/lang/Boolean;", "isCustomDialSupport", "setCustomDialSupport", "noisefit_colorfit2_debug"})
public final class CF2Globals {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_cf2.base.CF2Globals INSTANCE = null;
    @org.jetbrains.annotations.Nullable
    private static com.ido.ble.protocol.model.BasicInfo basicInfo;
    @org.jetbrains.annotations.Nullable
    private static java.lang.String firmwareUrl;
    @org.jetbrains.annotations.Nullable
    private static java.lang.Boolean isCustomDialSupport = false;
    @org.jetbrains.annotations.Nullable
    private static java.lang.Boolean isCloudDialSupport = false;
    
    private CF2Globals() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.ido.ble.protocol.model.BasicInfo getBasicInfo() {
        return null;
    }
    
    public final void setBasicInfo(@org.jetbrains.annotations.Nullable
    com.ido.ble.protocol.model.BasicInfo p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getFirmwareUrl() {
        return null;
    }
    
    public final void setFirmwareUrl(@org.jetbrains.annotations.Nullable
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean isCustomDialSupport() {
        return null;
    }
    
    public final void setCustomDialSupport(@org.jetbrains.annotations.Nullable
    java.lang.Boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Boolean isCloudDialSupport() {
        return null;
    }
    
    public final void setCloudDialSupport(@org.jetbrains.annotations.Nullable
    java.lang.Boolean p0) {
    }
}