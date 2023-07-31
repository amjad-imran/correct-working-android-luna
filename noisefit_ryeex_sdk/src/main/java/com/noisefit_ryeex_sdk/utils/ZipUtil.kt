package com.noisefit_ryeex_sdk.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipUtil {

    companion object {
        private const val buf = 4096

        /**
         * 解压文件到文件所在目录,默认不把压缩文件作为一层目录
         *
         * @param zipFile 被解压的文件
         * @throws IOException
         */
        @Throws(IOException::class)
        fun unzip(zipFile: File) {
            ZipDecompress().unzip(zipFile)
        }

        /**
         * 解压文件到文件所在目录
         *
         * @param zipFile           被解压的文件
         * @param retainZipAsFolder 是否把压缩文件作为一层目录
         * @throws IOException
         */
        @Throws(IOException::class)
        fun unzip(zipFile: File, retainZipAsFolder: Boolean) {
            ZipDecompress().unzip(zipFile, retainZipAsFolder)
        }

        /**
         * 解压文件,默认不把压缩文件作为一层目录
         *
         * @param zipFile 被解压的文件
         * @param destDir 目标目录
         * @throws IOException
         */
        @Throws(IOException::class)
        fun unzip(zipFile: File, destDir: File) {
            ZipDecompress().unzip(zipFile, destDir)
        }

        /**
         * 解压文件
         *
         * @param zipFile           被解压的文件
         * @param destDir           目标目录
         * @param retainZipAsFolder 是否把压缩文件作为一层目录
         * @throws IOException
         */
        @Throws(IOException::class)
        fun unzip(zipFile: File, destDir: File, retainZipAsFolder: Boolean) {
            ZipDecompress().unzip(zipFile, destDir, retainZipAsFolder)
        }

        /**
         * 将多个(1~n)个文件或者目录压缩至目标文件
         *
         * @param destZipFile 目标压缩文件
         * @param fileOrDirs  多个被压缩的文件或目录
         * @throws IOException
         */
        @Throws(IOException::class)
        fun zip(destZipFile: File, vararg fileOrDirs: File) {
            ZipCompress().zip(destZipFile, *fileOrDirs)
        }
    }

    /**
     * unzip
     */
    class ZipDecompress {
        /**
         * 解压文件到文件所在目录
         *
         * @param srcFile           被解压的文件
         * @param retainZipAsFolder 是否把压缩文件作为一层目录
         * @throws IOException
         */
        @Throws(IOException::class)
        fun unzip(srcFile: File, retainZipAsFolder: Boolean) {
            unzip(srcFile, srcFile.parentFile, retainZipAsFolder)
        }
        /**
         * 解压文件
         *
         * @param srcFile           被解压的文件
         * @param destDir           目标目录
         * @param retainZipAsFolder 是否把压缩文件作为一层目录
         * @throws IOException
         */
        /**
         * 解压文件到文件所在目录,默认不把压缩文件作为一层目录
         *
         * @param srcFile 被解压的文件
         * @throws IOException
         */
        /**
         * 解压文件,默认不把压缩文件作为一层目录
         *
         * @param srcFile 被解压的文件
         * @param destDir 目标目录
         * @throws IOException
         */
        @JvmOverloads
        @Throws(IOException::class)
        fun unzip(srcFile: File, destDir: File = srcFile.parentFile, retainZipAsFolder: Boolean = false) {
            var destDir = destDir
            if (retainZipAsFolder) {
                val fileName = srcFile.name
                val extIndex = fileName.lastIndexOf('.')
                val folderName: String
                folderName = if (extIndex != -1) {
                    fileName.substring(0, extIndex)
                } else {
                    fileName
                }
                destDir = File(destDir, folderName)
            }
            if (!destDir.exists()) {
                if (!destDir.mkdirs()) { // 创建目录失败
                    throw IOException(String.format("create dir:%s fail", destDir.path))
                }
            }
            try {
                val zipFile = ZipFile(srcFile)
                val entries = zipFile.entries()
                var len: Int
                val data = ByteArray(buf)
                while (entries.hasMoreElements()) {
                    val zipEntry = entries.nextElement()
                    val outFile = File(destDir, zipEntry.name)
                    if (zipEntry.isDirectory) { // 解压目录
                        outFile.mkdirs()
                    } else { // 解压文件
                        if (!outFile.parentFile.exists()) {
                            outFile.parentFile.mkdirs()
                        }
                        if (!outFile.exists()) {
                            outFile.createNewFile()
                        }
                        var bis: BufferedInputStream? = null
                        var bos: BufferedOutputStream? = null
                        try {
                            bis = BufferedInputStream(zipFile.getInputStream(zipEntry))
                            bos = BufferedOutputStream(FileOutputStream(outFile))
                            while (bis.read(data).also { len = it } != -1) {
                                bos.write(data, 0, len)
                            }
                            bos.flush()
                        } finally {
                            bos?.close()
                            bis?.close()
                        }
                    }
                }
            } catch (e: IOException) {
                throw e
            }
        }

        companion object {
            private const val buf = 4096
        }
    }

    /**
     * Zip
     */
    class ZipCompress {
        /**
         * 将多个(1~n)个文件或者目录压缩至目标文件
         *
         * @param destZipFile 目标压缩文件
         * @param fileOrDirs  多个被压缩的文件或目录
         * @throws IOException
         */
        @Throws(IOException::class)
        fun zip(destZipFile: File, vararg fileOrDirs: File) {
            var zos: ZipOutputStream? = null
            try {
                zos = ZipOutputStream(FileOutputStream(destZipFile))
                for (fileOrDir in fileOrDirs) {
                    if (fileOrDir.isDirectory) {
                        zipDir(fileOrDir, fileOrDir, zos)
                    } else {
                        zipFile(fileOrDir, fileOrDir.parentFile, zos)
                    }
                }
            } finally {
                zos?.close()
            }
        }

        /**
         * 压缩目录
         *
         * @param dir     被压缩的目录
         * @param baseDir 被压缩的文件的根目录(用于记录压缩文件的层次结构)
         * @param zos     压缩文件流
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun zipDir(dir: File, baseDir: File, zos: ZipOutputStream) {
            val subFiles = dir.listFiles()
            for (subFile in subFiles) {
                if (subFile.isDirectory) {
                    zipDir(subFile, baseDir, zos)
                } else {
                    zipFile(subFile, baseDir, zos)
                }
            }
        }

        /**
         * 压缩文件
         *
         * @param srcFile 被压缩的文件
         * @param baseDir 被压缩的文件的根目录(用于记录压缩文件层次结构)
         * @param zos     压缩文件流
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun zipFile(srcFile: File, baseDir: File, zos: ZipOutputStream) {
            val `in`: InputStream = FileInputStream(srcFile)
            val entry = srcFile.path.substring(baseDir.path.length + 1)
            val zipEntry = ZipEntry(entry)
            zos.putNextEntry(zipEntry)
            val data = ByteArray(buf)
            var len: Int
            while (`in`.read(data).also { len = it } != -1) {
                zos.write(data, 0, len)
            }
            zos.flush()
            `in`.close()
        }


    }
}