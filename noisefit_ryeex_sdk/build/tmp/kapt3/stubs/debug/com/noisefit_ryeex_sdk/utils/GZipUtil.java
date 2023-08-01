package com.noisefit_ryeex_sdk.utils;

import java.lang.System;

/**
 * gzip工具
 *
 * @author lijiewen
 * @date on 2021/10/27
 */
@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\u0006J\u001a\u0010\b\u001a\u00020\u00042\b\u0010\u0007\u001a\u0004\u0018\u00010\u00062\b\u0010\t\u001a\u0004\u0018\u00010\u0006\u00a8\u0006\n"}, d2 = {"Lcom/noisefit_ryeex_sdk/utils/GZipUtil;", "", "()V", "compressFile", "", "file", "", "gzipFile", "decompressFile", "newFile", "noisefit_ryeex_sdk_debug"})
public final class GZipUtil {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_ryeex_sdk.utils.GZipUtil INSTANCE = null;
    
    private GZipUtil() {
        super();
    }
    
    /**
     * 压缩
     *
     * @param file
     * @param gzipFile
     * @throws Exception
     */
    @kotlin.jvm.Throws(exceptionClasses = {java.lang.Exception.class})
    public final void compressFile(@org.jetbrains.annotations.Nullable
    java.lang.String file, @org.jetbrains.annotations.Nullable
    java.lang.String gzipFile) throws java.lang.Exception {
    }
    
    /**
     * 解压
     *
     * @param gzipFile
     * @param newFile
     * @throws Exception
     */
    @kotlin.jvm.Throws(exceptionClasses = {java.lang.Exception.class})
    public final void decompressFile(@org.jetbrains.annotations.Nullable
    java.lang.String gzipFile, @org.jetbrains.annotations.Nullable
    java.lang.String newFile) throws java.lang.Exception {
    }
}