package com.noisefit_ryeex_sdk.utils;

import java.lang.System;

@kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\u0018\u0000 \u00032\u00020\u0001:\u0003\u0003\u0004\u0005B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0006"}, d2 = {"Lcom/noisefit_ryeex_sdk/utils/ZipUtil;", "", "()V", "Companion", "ZipCompress", "ZipDecompress", "noisefit_ryeex_sdk_debug"})
public final class ZipUtil {
    @org.jetbrains.annotations.NotNull
    public static final com.noisefit_ryeex_sdk.utils.ZipUtil.Companion Companion = null;
    private static final int buf = 4096;
    
    public ZipUtil() {
        super();
    }
    
    /**
     * unzip
     */
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u0000 \n2\u00020\u0001:\u0001\nB\u0005\u00a2\u0006\u0002\u0010\u0002J$\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\tH\u0007J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\t\u00a8\u0006\u000b"}, d2 = {"Lcom/noisefit_ryeex_sdk/utils/ZipUtil$ZipDecompress;", "", "()V", "unzip", "", "srcFile", "Ljava/io/File;", "destDir", "retainZipAsFolder", "", "Companion", "noisefit_ryeex_sdk_debug"})
    public static final class ZipDecompress {
        @org.jetbrains.annotations.NotNull
        public static final com.noisefit_ryeex_sdk.utils.ZipUtil.ZipDecompress.Companion Companion = null;
        private static final int buf = 4096;
        
        public ZipDecompress() {
            super();
        }
        
        /**
         * 解压文件到文件所在目录
         *
         * @param srcFile           被解压的文件
         * @param retainZipAsFolder 是否把压缩文件作为一层目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File srcFile, boolean retainZipAsFolder) throws java.io.IOException {
        }
        
        /**
         * 解压文件,默认不把压缩文件作为一层目录
         *
         * @param srcFile 被解压的文件
         * @param destDir 目标目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        @kotlin.jvm.JvmOverloads
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File srcFile) throws java.io.IOException {
        }
        
        /**
         * 解压文件,默认不把压缩文件作为一层目录
         *
         * @param srcFile 被解压的文件
         * @param destDir 目标目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        @kotlin.jvm.JvmOverloads
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File srcFile, @org.jetbrains.annotations.NotNull
        java.io.File destDir) throws java.io.IOException {
        }
        
        /**
         * 解压文件,默认不把压缩文件作为一层目录
         *
         * @param srcFile 被解压的文件
         * @param destDir 目标目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        @kotlin.jvm.JvmOverloads
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File srcFile, @org.jetbrains.annotations.NotNull
        java.io.File destDir, boolean retainZipAsFolder) throws java.io.IOException {
        }
        
        @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/noisefit_ryeex_sdk/utils/ZipUtil$ZipDecompress$Companion;", "", "()V", "buf", "", "noisefit_ryeex_sdk_debug"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
        }
    }
    
    /**
     * Zip
     */
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\'\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0007\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00060\b\"\u00020\u0006\u00a2\u0006\u0002\u0010\tJ \u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000eH\u0002J \u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000eH\u0002\u00a8\u0006\u0011"}, d2 = {"Lcom/noisefit_ryeex_sdk/utils/ZipUtil$ZipCompress;", "", "()V", "zip", "", "destZipFile", "Ljava/io/File;", "fileOrDirs", "", "(Ljava/io/File;[Ljava/io/File;)V", "zipDir", "dir", "baseDir", "zos", "Ljava/util/zip/ZipOutputStream;", "zipFile", "srcFile", "noisefit_ryeex_sdk_debug"})
    public static final class ZipCompress {
        
        public ZipCompress() {
            super();
        }
        
        /**
         * 将多个(1~n)个文件或者目录压缩至目标文件
         *
         * @param destZipFile 目标压缩文件
         * @param fileOrDirs  多个被压缩的文件或目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        public final void zip(@org.jetbrains.annotations.NotNull
        java.io.File destZipFile, @org.jetbrains.annotations.NotNull
        java.io.File... fileOrDirs) throws java.io.IOException {
        }
        
        /**
         * 压缩目录
         *
         * @param dir     被压缩的目录
         * @param baseDir 被压缩的文件的根目录(用于记录压缩文件的层次结构)
         * @param zos     压缩文件流
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        private final void zipDir(java.io.File dir, java.io.File baseDir, java.util.zip.ZipOutputStream zos) throws java.io.IOException {
        }
        
        /**
         * 压缩文件
         *
         * @param srcFile 被压缩的文件
         * @param baseDir 被压缩的文件的根目录(用于记录压缩文件层次结构)
         * @param zos     压缩文件流
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        private final void zipFile(java.io.File srcFile, java.io.File baseDir, java.util.zip.ZipOutputStream zos) throws java.io.IOException {
        }
    }
    
    @kotlin.Metadata(mv = {1, 7, 1}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bJ\u001e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000bJ\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u000bJ\'\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\b2\u0012\u0010\u000e\u001a\n\u0012\u0006\b\u0001\u0012\u00020\b0\u000f\"\u00020\b\u00a2\u0006\u0002\u0010\u0010R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/noisefit_ryeex_sdk/utils/ZipUtil$Companion;", "", "()V", "buf", "", "unzip", "", "zipFile", "Ljava/io/File;", "destDir", "retainZipAsFolder", "", "zip", "destZipFile", "fileOrDirs", "", "(Ljava/io/File;[Ljava/io/File;)V", "noisefit_ryeex_sdk_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * 解压文件到文件所在目录,默认不把压缩文件作为一层目录
         *
         * @param zipFile 被解压的文件
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File zipFile) throws java.io.IOException {
        }
        
        /**
         * 解压文件到文件所在目录
         *
         * @param zipFile           被解压的文件
         * @param retainZipAsFolder 是否把压缩文件作为一层目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File zipFile, boolean retainZipAsFolder) throws java.io.IOException {
        }
        
        /**
         * 解压文件,默认不把压缩文件作为一层目录
         *
         * @param zipFile 被解压的文件
         * @param destDir 目标目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File zipFile, @org.jetbrains.annotations.NotNull
        java.io.File destDir) throws java.io.IOException {
        }
        
        /**
         * 解压文件
         *
         * @param zipFile           被解压的文件
         * @param destDir           目标目录
         * @param retainZipAsFolder 是否把压缩文件作为一层目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        public final void unzip(@org.jetbrains.annotations.NotNull
        java.io.File zipFile, @org.jetbrains.annotations.NotNull
        java.io.File destDir, boolean retainZipAsFolder) throws java.io.IOException {
        }
        
        /**
         * 将多个(1~n)个文件或者目录压缩至目标文件
         *
         * @param destZipFile 目标压缩文件
         * @param fileOrDirs  多个被压缩的文件或目录
         * @throws IOException
         */
        @kotlin.jvm.Throws(exceptionClasses = {java.io.IOException.class})
        public final void zip(@org.jetbrains.annotations.NotNull
        java.io.File destZipFile, @org.jetbrains.annotations.NotNull
        java.io.File... fileOrDirs) throws java.io.IOException {
        }
    }
}