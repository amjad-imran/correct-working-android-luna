package com.noisefit.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.noisefit.luna.R
import java.io.*
import javax.inject.Inject


class ImageUtil
@Inject
constructor() {

    fun saveFileToPictures(context: Context, file: File): Uri? {
        try {
            val imageOutStream: OutputStream
            //val bitmap = getBitmapFromUri(inputImageUri) ?: return null
            val bitmap = getBitmapFromFile(file) ?: return null

            val filename = "${System.currentTimeMillis()}.jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                var outputUri: Uri? = null
                context.contentResolver.run {
                    outputUri =
                        context.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                        )
                            ?: return null
                    imageOutStream = openOutputStream(outputUri!!) ?: return null
                }
                imageOutStream.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

                return outputUri
            } else {//8
                val imagePath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
                val image = File(imagePath, filename)
                imageOutStream = FileOutputStream(image)
                imageOutStream.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

                return Uri.fromFile(image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    fun getBitmapFromFile(file: File): Bitmap? {
        val filePath: String = file.path
        return BitmapFactory.decodeFile(filePath)
    }

    fun getBitmapFromUri(uri: Uri): Bitmap? {
        val fileTOConvert = File(uri.path)
        return if (fileTOConvert.exists()) {
            return BitmapFactory.decodeFile(uri.path)
        } else {
            null
        }
    }

    fun deleteTempImage(tempImageFile: File): Boolean {
        return tempImageFile.delete()
    }
    /*
    * missing icons
    * outdoor_walking,cross_country,outdoor_walk,indoor_walk
    * jazz_dance,latin_dance,national_dance
    * swimming,hiking,rowing,rower,pool_swimming,open_water_swimming,elliptical,elliptical_training
    * indoor_run,indoor_running,workout,warm_up_exercise,free_workout,free_exercise
    * spinning
    * */

    fun getAppIconImageMighty(functionId: Int): Int {
        return when (functionId) {
            2 -> R.drawable.ic_mighty_app_sport
            20 -> R.drawable.ic_mighty_app_history
            19 -> R.drawable.ic_mighty_app_activity
            3 -> R.drawable.ic_mighty_app_heart_rate
            4 -> R.drawable.ic_mighty_app_spo2
            18 -> R.drawable.ic_mighty_app_sleep

            21 -> R.drawable.ic_mighty_app_breath
            29 -> R.drawable.ic_mighty_app_call
            30 -> R.drawable.ic_mighty_app_contact

            8 -> R.drawable.ic_mighty_app_alarm
            5 -> R.drawable.ic_mighty_app_weather
            9 -> R.drawable.ic_mighty_app_timer

            25 -> R.drawable.ic_mighty_app_stopwatch

            23 -> R.drawable.ic_mighty_app_music
            24 -> R.drawable.ic_mighty_app_camera
            12 -> R.drawable.ic_mighty_app_find_phone

            33 -> R.drawable.ic_mighty_app_flashlight
            7 -> R.drawable.ic_mighty_app_settings

            else -> R.drawable.ic_sports_walk
        }
    }

    fun getImageForApp(functionId: Int): Int {
        return when (functionId) {
            0 -> R.drawable.ic_sports_walk
//            1 -> R.drawable.ic_workouts_history_sort
            2, 32 -> R.drawable.ic_activity
            3 -> R.drawable.ic_heart_rate
            4 -> R.drawable.ic_sleep
            5 -> R.drawable.ic_act_spo2
            6 -> R.drawable.ic_alarm_clock
            7 -> R.drawable.ic_my_reminder
//            8 -> R.drawable.ic_stopwatch_sort
//            9 -> R.drawable.ic_timer_sort
            10 -> R.drawable.ic_music
            11 -> R.drawable.ic_weather
//            12 -> R.drawable.ic_breathing_sort
//            13 -> R.drawable.ic_notification_sort
//            14 -> R.drawable.ic_find_phone_sort
//            15 -> R.drawable.ic_setting_sort
//            16 -> R.drawable.ic_femal_sort
            17 -> R.drawable.ic_stress
            18 -> R.drawable.ic_world_clock
            19 -> R.drawable.ic_stock
//            20 -> "Air pressure"
//            21 -> "Compass"
//            22 -> "ECG"
//            23 -> "Temperature"
            24 -> R.drawable.ic_call_accept
            25 -> R.drawable.ic_contact
//            26 -> "Frequent contacts"
            27 -> R.drawable.ic_camera_shutter
            else -> R.drawable.ic_sports_walk
        }
    }


    fun getColorFromActivity(activityName: String?): ActivityColor {
        return when (activityName?.lowercase()) {
            "running", "basketball", "hiking", "indoor_cycle", "spinning", "badminton", "open_water_swimming", "outdoor_run", "outdoor_running", "cross_country" -> {
                ActivityColor.ORANGE
            }
            "walking", "outdoor_walking", "indoor_walk", "outdoor_walk", "yoga", "football", "rowing", "rowing_machine", "cricket", "elliptical", "elliptical_training", "elliptical_machine",
            "treadmill", "indoor_running", "indoor_run", "bicycling", "indoor_cycling", "outdoor_cycling", "biking" -> {
                ActivityColor.GREEN
            }

            "dance", "jazz_dance", "latin_dance", "national_dance", "tennis", "swimming", "climbing", "skipping", "rower", "pool_swimming", "workout", "warm_up_exercise", "free_workout", "free_exercise",
            "strength_training", "strength" -> {
                ActivityColor.BLUE
            }
            else -> ActivityColor.BLUE
        }
    }

    enum class ActivityColor {
        ORANGE, GREEN, BLUE
    }

    fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var outputBitmap = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = outputBitmap.width
            val height = outputBitmap.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            outputBitmap = Bitmap.createScaledBitmap(outputBitmap, finalWidth, finalHeight, true)
            outputBitmap
        } else {
            outputBitmap
        }
    }

    fun getBitmapFromAsset(context: Context, filePath: String?): Bitmap? {
        val assetManager = context.assets
        val istr: InputStream
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open(filePath!!)
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            // handle exception
            e.printStackTrace()
        }
        return bitmap
    }

    fun getThumbBitmap(photoFile: File, height: Int, width: Int): Bitmap? {
        val bitmap = getBitmapFromFile(photoFile) ?: return null
        return resize(bitmap, width, height)
    }

    fun getGradientList(level: Int): IntArray {
        val color0 = 0xFFFF1446.toInt()
        val color33 = 0xFFFF5A00.toInt()
        val color66 = 0xFFFFC800.toInt()
        val color100 = 0xFF00DC8C.toInt()

        return when (level) {
            in 0 until 33 -> {
                intArrayOf(color0, color33).reversedArray()
            }
            in 34 until 80 -> {
                intArrayOf(color0, color33, color66).reversedArray()
            }
            in 80 until 101 -> {
                intArrayOf(color0, color33, color66, color100).reversedArray()
            }
            else -> {
                intArrayOf(color0, color33).reversedArray()
            }
        }


        //ArgbEvaluator().evaluate(0.75f, 0x00ff00, 0xff0000)
    }

    fun getBOGradientList(level: Int): IntArray {
        val color0 = 0xFF264957.toInt()
        val color33 = 0xFF426d6b.toInt()
        val color66 = 0xFF569f99.toInt()
        val color100 = 0xFF6cd0c6.toInt()

        return when (level) {
            in 0 until 33 -> {
                intArrayOf(color0, color33).reversedArray()
            }
            in 34 until 80 -> {
                intArrayOf(color0, color33, color66).reversedArray()
            }
            in 80 until 101 -> {
                intArrayOf(color0, color33, color66, color100).reversedArray()
            }
            else -> {
                intArrayOf(color0, color33).reversedArray()
            }
        }


        //ArgbEvaluator().evaluate(0.75f, 0x00ff00, 0xff0000)
    }


}