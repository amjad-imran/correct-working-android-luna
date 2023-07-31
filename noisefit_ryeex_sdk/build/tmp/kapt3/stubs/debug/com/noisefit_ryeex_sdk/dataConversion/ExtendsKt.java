package com.noisefit_ryeex_sdk.dataConversion;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 2, d1 = {"\u0000x\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0015\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u001a\u001c\u0010\u0006\u001a\u00020\u00042\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\u0010\t\u001a\u0004\u0018\u00010\nH\u0002\u001a(\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002\u001a\u0012\u0010\u0013\u001a\u00020\u0001*\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u0004\u001a\n\u0010\u0015\u001a\u00020\n*\u00020\u0004\u001a.\u0010\u0016\u001a\u00020\u0017*\u00020\u00182\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0019\u001a\u00020\n2\u0006\u0010\u001a\u001a\u00020\nH\u0002\u001a$\u0010\u001b\u001a\u00020\u001c*\u00020\u00042\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u001c2\u0006\u0010\u001e\u001a\u00020\nH\u0002\u001a.\u0010\u001f\u001a\u00020\u0017*\u00020 2\u0006\u0010!\u001a\u00020\f2\u0006\u0010\"\u001a\u00020#2\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020&0%\u001a\n\u0010\'\u001a\u00020\u0001*\u00020\u0001\u001a$\u0010(\u001a\u00020\u0001*\u00020\u00012\u0006\u0010)\u001a\u00020\u00012\u0006\u0010*\u001a\u00020+2\u0006\u0010\u0005\u001a\u00020+H\u0002\u001a4\u0010,\u001a\u00020\u0017*\u00020-2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010.\u001a\u00020\u00042\u0006\u0010/\u001a\u00020\u00182\u0006\u00100\u001a\u00020\u001c\u001a\n\u00101\u001a\u00020\u0017*\u00020\b\u001a\u0014\u00102\u001a\u00020\u0017*\u00020\u00012\u0006\u00103\u001a\u00020\nH\u0002\u001a\u001c\u00104\u001a\u00020\u0001*\u00020\u00012\u0006\u0010\u0003\u001a\u00020+2\u0006\u0010\u0005\u001a\u00020+H\u0002\u001a+\u00105\u001a\u00020\u0017*\u0002062\u0006\u00107\u001a\u0002062\u0006\u0010.\u001a\u00020\u00042\b\u00108\u001a\u0004\u0018\u00010\u0004H\u0002\u00a2\u0006\u0002\u00109\u00a8\u0006:"}, d2 = {"changeBitmapSize", "Landroid/graphics/Bitmap;", "bitmap", "width", "", "height", "convertPosition", "noiseFitDevice", "Lcom/noisefit_commans/models/ColorFitDevice;", "style", "", "generateJieliResource", "", "context", "Landroid/content/Context;", "input", "output", "size", "", "changeColor", "color", "colorToHex", "convertImage", "", "Lcom/noisefit_commans/models/DiyCustomWatchFace;", "previewFolder", "resFolder", "copyToFile", "Ljava/io/File;", "dir", "ext", "deleteWatchface", "Lcom/ryeex/watch/adapter/device/WatchDevice;", "isDynamic", "deviceSurface", "Lcom/ryeex/watch/adapter/model/entity/DeviceSurfaceInfo;", "callback", "Lcom/ryeex/ble/connector/callback/AsyncBleCallback;", "Lcom/ryeex/ble/connector/error/BleError;", "oval", "overlay", "foreground", "with", "", "prepareTarResource", "Lcom/ryeex/watch/adapter/model/entity/DeviceSurfaceInfo$Surface;", "newId", "customWatchFace", "tempDir", "removeBond", "saveToStorage", "path", "scale", "updateConfig", "Lorg/json/JSONObject;", "jsonObject", "fontColor", "(Lorg/json/JSONObject;Lorg/json/JSONObject;ILjava/lang/Integer;)V", "noisefit_ryeex_sdk_debug"})
public final class ExtendsKt {
    
    public static final void deleteWatchface(@org.jetbrains.annotations.NotNull
    com.ryeex.watch.adapter.device.WatchDevice $this$deleteWatchface, boolean isDynamic, @org.jetbrains.annotations.NotNull
    com.ryeex.watch.adapter.model.entity.DeviceSurfaceInfo deviceSurface, @org.jetbrains.annotations.NotNull
    com.ryeex.ble.connector.callback.AsyncBleCallback<java.lang.Integer, com.ryeex.ble.connector.error.BleError> callback) {
    }
    
    public static final void prepareTarResource(@org.jetbrains.annotations.NotNull
    com.ryeex.watch.adapter.model.entity.DeviceSurfaceInfo.Surface $this$prepareTarResource, @org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.Nullable
    com.noisefit_commans.models.ColorFitDevice noiseFitDevice, int newId, @org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.DiyCustomWatchFace customWatchFace, @org.jetbrains.annotations.NotNull
    java.io.File tempDir) {
    }
    
    private static final int convertPosition(com.noisefit_commans.models.ColorFitDevice noiseFitDevice, java.lang.String style) {
        return 0;
    }
    
    private static final java.io.File copyToFile(int $this$copyToFile, android.content.Context context, java.io.File dir, java.lang.String ext) {
        return null;
    }
    
    private static final void updateConfig(org.json.JSONObject $this$updateConfig, org.json.JSONObject jsonObject, int newId, java.lang.Integer fontColor) {
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String colorToHex(int $this$colorToHex) {
        return null;
    }
    
    private static final void convertImage(com.noisefit_commans.models.DiyCustomWatchFace $this$convertImage, android.content.Context context, com.noisefit_commans.models.ColorFitDevice noiseFitDevice, java.lang.String previewFolder, java.lang.String resFolder) {
    }
    
    @org.jetbrains.annotations.NotNull
    public static final android.graphics.Bitmap changeBitmapSize(@org.jetbrains.annotations.NotNull
    android.graphics.Bitmap bitmap, int width, int height) {
        return null;
    }
    
    private static final boolean generateJieliResource(android.content.Context context, java.lang.String input, java.lang.String output, int[] size) {
        return false;
    }
    
    private static final android.graphics.Bitmap scale(android.graphics.Bitmap $this$scale, float width, float height) {
        return null;
    }
    
    private static final android.graphics.Bitmap overlay(android.graphics.Bitmap $this$overlay, android.graphics.Bitmap foreground, float with, float height) {
        return null;
    }
    
    private static final void saveToStorage(android.graphics.Bitmap $this$saveToStorage, java.lang.String path) {
    }
    
    @org.jetbrains.annotations.NotNull
    public static final android.graphics.Bitmap changeColor(@org.jetbrains.annotations.NotNull
    android.graphics.Bitmap $this$changeColor, int color) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final android.graphics.Bitmap oval(@org.jetbrains.annotations.NotNull
    android.graphics.Bitmap $this$oval) {
        return null;
    }
    
    public static final void removeBond(@org.jetbrains.annotations.NotNull
    com.noisefit_commans.models.ColorFitDevice $this$removeBond) {
    }
}