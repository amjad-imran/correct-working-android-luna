package com.noisefit_commans.utils

import android.R.attr
import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import com.noisefit_commans.NoisefitApplication
import java.io.*
import android.R.attr.bitmap

import android.graphics.PorterDuff

import android.graphics.PorterDuffXfermode

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


object ImageUtil {
    fun getBitmapFromAsset(rawRes: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inputStream: InputStream? =
                NoisefitApplication.context?.resources?.openRawResource(rawRes)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            return null
        }
        return bitmap
    }

    fun getBytesFromAsset(rawRes: Int): ByteArray? {
        var buffer: ByteArray? = null

        return try {
            val inputStream: InputStream =
                NoisefitApplication.context?.resources?.openRawResource(rawRes) ?: return null
            val size: Int = inputStream.available()
            buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            return buffer
        } catch (e: IOException) {
            null
        }
    }

    fun getBitmapFromUri(uri: Uri): Bitmap? {
        try {

            val inputStream: InputStream =
                NoisefitApplication.context?.contentResolver?.openInputStream(uri) ?: return null
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getBitmapFromView(view: View): Bitmap? {
        return try {
            val bitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (exp: Exception) {
            null
        }

    }

    fun saveMediaToStorage(context: Context, bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    fun getTempImageUri(bitmap: Bitmap, context: Context): Uri? {
        val file = File(context.externalCacheDir, "share.png") //Get Access to a local file.
        file.delete() // Delete the File, just in Case, that there was still another File
        file.createNewFile()
        val fileOutputStream = file.outputStream()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream)
        val bytearray = byteArrayOutputStream.toByteArray()
        fileOutputStream.write(bytearray)
        fileOutputStream.flush()
        fileOutputStream.close()
        byteArrayOutputStream.close()
        return FileProvider.getUriForFile(
            context, AppLogs.FILE_PROVIDER, file
        )
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap? {
        val output =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(),
            paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }


    fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun changeBitmapSize(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val scaleWidth = width.toFloat() / bitmapWidth
        val scaleHeight = height.toFloat() / bitmapHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(
            bitmap, 0, 0,
            bitmapWidth, bitmapHeight, matrix, false
        )
    }


}