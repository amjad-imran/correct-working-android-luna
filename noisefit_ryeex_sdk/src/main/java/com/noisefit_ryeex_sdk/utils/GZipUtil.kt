package com.noisefit_ryeex_sdk.utils

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * gzip工具
 *
 * @author lijiewen
 * @date on 2021/10/27
 */
object GZipUtil {
    /**
     * 压缩
     *
     * @param file
     * @param gzipFile
     * @throws Exception
     */
    @Throws(Exception::class)
    fun compressFile(file: String?, gzipFile: String?) {
        val fis = FileInputStream(file)
        val fos = FileOutputStream(gzipFile)
        val output = GZIPOutputStream(fos)
        val buffer = ByteArray(1024)
        var len: Int
        while (fis.read(buffer).also { len = it } != -1) {
            output.write(buffer, 0, len)
        }
        output.close()
        fos.close()
        fis.close()
    }

    /**
     * 解压
     *
     * @param gzipFile
     * @param newFile
     * @throws Exception
     */
    @Throws(Exception::class)
    fun decompressFile(gzipFile: String?, newFile: String?) {
        val fis = FileInputStream(gzipFile)
        val gis = GZIPInputStream(fis)
        val fos = FileOutputStream(newFile)
        val buffer = ByteArray(1024)
        var len: Int
        while (gis.read(buffer).also { len = it } != -1) {
            fos.write(buffer, 0, len)
        }
        fos.close()
        gis.close()
        fis.close()
    }
}