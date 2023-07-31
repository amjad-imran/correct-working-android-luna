package com.noisefit_commans.utils.imagepicker


import android.content.Context
import java.io.File
import java.util.*

object Util {
    var fileNamePrefix = "noisefit_"

    fun createFile(context: Context, fileType: String): File? {
        return try {
            val filename = fileNamePrefix + UUID.randomUUID().toString() + "." + fileType
            val fileDir: File = context.filesDir
            val file = File(fileDir, filename)
            file.createNewFile()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun getCaptureImageOutputUri(context: Context): File? {
        val getImage: File? = context.externalCacheDir
        if (getImage != null) {
            return File(getImage.path, "watchface.png")
        }
        return null
    }

}
