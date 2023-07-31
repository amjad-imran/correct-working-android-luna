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
import com.noisefit_ryeex_sdk.dataConversion.RyeexConst
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

    fun getImageFromActivity(activityName: String?): Int {
        return when (activityName?.lowercase()?.trim()) {
            "walking", "outdoor_walking", "outdoor_walk", "indoor_walk", "on_foot", RyeexConst.OUTDOOR_WALK.lowercase(), RyeexConst.INDOOR_WALK.lowercase() -> R.drawable.ic_act_walking
            "cross_country" -> R.drawable.ic_act_cross_country
            "dance", "ballroom_dancing", "folk_dance", "modern_dance", "dancing", RyeexConst.DANCE.lowercase() -> R.drawable.ic_act_dance
            "national_dance", RyeexConst.FOLK_DANCE.lowercase() -> R.drawable.ic_act_national_dance
            "latin_dance", RyeexConst.LATIN_DANCE.lowercase() -> R.drawable.ic_act_latin_dance
            "jazz_dance" -> R.drawable.ic_act_jazz_dance
            "tennis", "table_tennis", RyeexConst.TENNIS.lowercase() -> R.drawable.ic_act_tennis
            "yoga", "mind_and_body", RyeexConst.YOGA.lowercase() -> R.drawable.ic_act_yoga
            "basketball", RyeexConst.BASKETBALL.lowercase() -> R.drawable.ic_act_basketball
            "gymnastics", "swimming", "diving", "group_gymnastics", RyeexConst.INDOOR_SWIM.lowercase() -> R.drawable.ic_act_gymnastic
            "hiking", RyeexConst.HIKING.lowercase() -> R.drawable.ic_hiking
            "climbing", "climbing_machine", "climb_the_stairs", "stairs", "stair_climbing", "stair_climber", RyeexConst.STAIRS.lowercase() -> R.drawable.ic_act_climbing_machine
            "football", "beach_soccer", "american_football", RyeexConst.AMERICAN_FOOTBALL.lowercase(), RyeexConst.SOCCER.lowercase(), RyeexConst.AUSTRALIAN_FOOTBALL.lowercase() -> R.drawable.ic_act_foot_ball
            "badminton", RyeexConst.BADMINTON.lowercase() -> R.drawable.ic_act_badminton
            "skipping", "rope_skipping", "battle_rope", "jump_rope", RyeexConst.JUMP_ROPE.lowercase() -> R.drawable.ic_act_rope_skipping
            "rowing_machine", "rowing" -> R.drawable.ic_act_rowing_machine
            "rower", RyeexConst.ROWER.lowercase() -> R.drawable.ic_act_rowing_machine
            "cricket", RyeexConst.CRICKET.lowercase() -> R.drawable.ic_act_cricket
            "pool_swimming", "fin_swimming", "synchronized_swimming" -> R.drawable.ic_act_gymnastic
            "open_water_swimming", "open_water_swim", "open_water", "water_sports", "other_water_sports" -> R.drawable.ic_act_gymnastic
            "elliptical", "elliptical_machine", "elliptical_training", RyeexConst.ELLIPTICAL.lowercase() -> R.drawable.ic_elliptical
            "treadmill", "indoor_running", "indoor_run", RyeexConst.INDOOR_RUN.lowercase() -> R.drawable.ic_act_treadmill
            "workout", "athletics", "abs", "strength_training", "strength","strength training", "warm_up_exercise", "free_workout", "free_exercise", "cross_training_crossfit", "push_ups", "pull_ups", "cross_training", RyeexConst.STRENGTH_TRAINING, RyeexConst.CROSS_TRAINING, RyeexConst.CROSS_FIT -> R.drawable.ic_act_strength_training
            "spinning", "indoor_cycle", RyeexConst.INDOOR_CYCLE.lowercase(), RyeexConst.SPINNING.lowercase() -> R.drawable.ic_act_indoor_cycling
            "indoor_cycling","cycling" -> R.drawable.ic_act_indoor_cycling
            "biking", "bicycling", "outdoor_cycling","outdoor_cycling__", RyeexConst.OUTDOOR_CYCLE.lowercase() -> R.drawable.ic_act_outdoor_cycling
            "outdoor_running", "running", "outdoor_run", RyeexConst.OUTDOOR_RUN.lowercase() -> R.drawable.ic_act_outdoor_running
            "trail_run", RyeexConst.TRAIL_RUNNING.lowercase() -> R.drawable.ic_act_trai_running
            "judo", RyeexConst.JUDO.lowercase() -> R.drawable.ic_act_judo
            "karate", RyeexConst.KARATE.lowercase() -> R.drawable.ic_act_karate
            "water_skiing", "skiing", "cross_country_skiing", "double_board_skiing", "double_board_skating", RyeexConst.SKIING.lowercase() -> R.drawable.ic_act_skiing
            "alpine_skiing", RyeexConst.DOWNHILL_SKIING.lowercase() -> R.drawable.ic_act_alpine_skiing
            "kabaddi", RyeexConst.KABADDI.lowercase() -> R.drawable.ic_act_kabaddi
            "kayaking", "kayak_rafting", "rafting" -> R.drawable.ic_act_kaya_king
            "hunting", RyeexConst.HUNTING.lowercase() -> R.drawable.ic_act_hunting
            "fishing", RyeexConst.FISHING.lowercase() -> R.drawable.ic_act_fishing
            "skateboarding", RyeexConst.SKATEBOARD.lowercase() -> R.drawable.ic_act_skateboarding
            "outdoor_skating", RyeexConst.SKATING.lowercase() -> R.drawable.ic_act_outdoor_skating
            "indoor_skating" -> R.drawable.ic_act_indoor_skating
            "roller_skating", RyeexConst.ROLLER_SKATING.lowercase() -> R.drawable.ic_act_roller_skating
            "fencing", RyeexConst.FENCING.lowercase() -> R.drawable.ic_act_fencing
            "boxing", "cardio_boxing", RyeexConst.BOXING.lowercase() -> R.drawable.ic_act_boxing
            "kickboxing", RyeexConst.KICKBOXING.lowercase() -> R.drawable.ic_act_kick_boxing
            "tai_chi", RyeexConst.TAI_CHI.lowercase() -> R.drawable.ic_act_tai_chi
            "bmx" -> R.drawable.ic_act_bmx
            "curling", RyeexConst.CURLING.lowercase() -> R.drawable.ic_act_curling
            "archery", RyeexConst.ARCHERY.lowercase() -> R.drawable.ic_act_archery
            "equestrian", "equestrian_sports" -> R.drawable.ic_act_equestrian
            "ballet", RyeexConst.BARRE.lowercase() -> R.drawable.ic_act_ballet
            "square_dance", "square_dancing" -> R.drawable.ic_act_square_dance
            "zumba" -> R.drawable.ic_act_zumba
            "mixed_aerobics", "aerobics", "aerobics_gyms", RyeexConst.MIXED_CARDIO.lowercase(), RyeexConst.AEROBICS.lowercase() -> R.drawable.ic_act_mixed_aerobics
            "stretching" -> R.drawable.ic_act_stretching
            "indoor_fitness", "fitness_gaming", "fitness", RyeexConst.FITNESS_GAMING.lowercase() -> R.drawable.ic_act_indoor_fitness
            "flexibility_training", "flexibility", RyeexConst.FLEXIBILITY.lowercase() -> R.drawable.ic_act_flexibility_training
            "stepper" -> R.drawable.ic_act_stepper
            "step_training", RyeexConst.STEP_TRAINING.lowercase() -> R.drawable.ic_act_step_training
            "freestyle", RyeexConst.FREE.lowercase() -> R.drawable.ic_act_freestyle
            "core_training", RyeexConst.CORE_TRAINING.lowercase() -> R.drawable.ic_act_core_training
            "sailing" -> R.drawable.ic_act_sailing
            "baseball", "pickle_ball", "solid_ball", "dodge_ball", RyeexConst.BASEBALL.lowercase() -> R.drawable.ic_act_baseball
            "bowling", RyeexConst.BOWLING.lowercase() -> R.drawable.ic_act_bowling
            "squash", RyeexConst.RACQUETBALL.lowercase() -> R.drawable.ic_act_squash
            "softball", RyeexConst.SOFTBALL.lowercase() -> R.drawable.ic_act_softball
            "volleyball", "beach_volleyball", RyeexConst.VOLLEYBALL.lowercase() -> R.drawable.ic_act_volley_ball
            "handball", RyeexConst.HANDBALL.lowercase() -> R.drawable.ic_act_handball
            "pingpong", RyeexConst.TABLE_TENNIS.lowercase() -> R.drawable.ic_act_ping_pong
            "belly_dance" -> R.drawable.ic_act_belly_dance
            "street_dance", RyeexConst.STREET_DANCE.lowercase() -> R.drawable.ic_act_street_dance
            "wrestling", RyeexConst.WRESTLING.lowercase() -> R.drawable.ic_act_wrestling
            "muay_thai" -> R.drawable.ic_act_muay_thai
            "taekwondo", RyeexConst.TAEKWONDO.lowercase() -> R.drawable.ic_act_taekwondo
            "martial_arts", RyeexConst.MARTIAL_ARTS.lowercase() -> R.drawable.ic_act_martial_arts
            "free_sparring" -> R.drawable.ic_act_free_sparring
            "pilates", RyeexConst.PILATES.lowercase() -> R.drawable.ic_act_pilates
            "functional_training", RyeexConst.FUNCTIONAL_TRAINING.lowercase() -> R.drawable.ic_act_functional_training
            "sit_ups", RyeexConst.SIT_UP.lowercase() -> R.drawable.ic_act_sit_ups
            "dumbbell_training", "dumbbells", "upper_limb_training", "lower_limb_training", "waist_and_abdomen_training", "back_training", "dumbbell" -> R.drawable.ic_act_dumbbell_training
            "barbell_training" -> R.drawable.ic_act_barbell_training
            "weightlifting" -> R.drawable.ic_act_weight_lifting
            "hiit", RyeexConst.HIIT.lowercase() -> R.drawable.ic_act_hiit
            "deadlift" -> R.drawable.ic_act_deadlift
            "darts", RyeexConst.DARTS.lowercase() -> R.drawable.ic_act_darts
            "frisbee", RyeexConst.DISC_SPORTS.lowercase() -> R.drawable.ic_act_frisbee
            "kite_flying", RyeexConst.KITE.lowercase() -> R.drawable.ic_act_kite_flying
            "tug_of_war", RyeexConst.TUG_OF_WAR.lowercase() -> R.drawable.ic_act_tug_of_war
            "shuttlecock", RyeexConst.SHUTTLECOCK.lowercase() -> R.drawable.ic_act_shuttle_cock
            "paddle_board" -> R.drawable.ic_act_paddle_boards
            "rock_climbing", RyeexConst.CLIMB.lowercase() -> R.drawable.ic_act_rock_climbing
            "physical_training", RyeexConst.PHYSICAL_TRAINING.lowercase() -> R.drawable.ic_act_physical_training
            "wall_ball" -> R.drawable.ic_act_wall_ball
            "bobby_jump", "long_jump", RyeexConst.LONG_JUMP.lowercase() -> R.drawable.ic_act_bobby_jump
            "rugby", RyeexConst.RUGBY.lowercase() -> R.drawable.ic_act_rugby
            "hockey", "ice_hockey", RyeexConst.HOCKEY.lowercase() -> R.drawable.ic_act_hockey
            "billiards", RyeexConst.BILLIARDS.lowercase() -> R.drawable.ic_act_billiards
            "snow_sports", "snowmobile", "snow_car", "snowboarding", RyeexConst.SNOW_SPORTS.lowercase(), RyeexConst.SNOWBOARDING.lowercase() -> R.drawable.ic_act_snow_boarding
            "puck", RyeexConst.ICE_HOCKEY.lowercase() -> R.drawable.ic_act_puck
            "hula_hoop", RyeexConst.HULA_HOOP.lowercase() -> R.drawable.ic_act_hulla_hoop
            "mountain_cycling" -> R.drawable.ic_act_mountain_cycling
            "paddleboard_surfing", "paddleboards" -> R.drawable.ic_act_paddle_boards
            "burpee","burpees" -> R.drawable.ic_burpee
            "squat" -> R.drawable.ic_squat
            "boating" ->R.drawable.ic_boating
            "bungee_jumping"->R.drawable.ic_bungee_jumping
            "trampoline"-> R.drawable.ic_trampoline
            "marathon" ->R.drawable.ic_marathon
            "balance_car"->R.drawable.ic_balance_car
            "high_jump"->R.drawable.ic_high_jump
            "shooting"->R.drawable.ic_shooting
            "push-ups"->R.drawable.ic_push_ups
            "pull-up"->R.drawable.ic_pull_up
            "plank"->R.drawable.ic_plank
            "racing_car"->R.drawable.ic_racing_car
            "motorboat"->R.drawable.ic_motorboat
            "dragon_boat"->R.drawable.ic_dragon_boat
            "drifting"->R.drawable.ic_drifting
            "parkour"->R.drawable.ic_parkour
            "waist_training"->R.drawable.ic_waist_training
            "golf"->R.drawable.ic_golf
            "parallel_bars"->R.drawable.ic_parallel_bars
            "single_bar"->R.drawable.ic_single_bar
            "lacrosse"->R.drawable.ic_lacrosse
            "leisure"->R.drawable.ic_leisure
            "track_field"->R.drawable.ic_track_field
            "mind&body"->R.drawable.ic_mind_and_body
            "hand_cycling"->R.drawable.ic_hand_cycling
            "pickleball"->R.drawable.ic_pickleball
            "rolling"->R.drawable.ic_rolling

            "mountaineering"->R.drawable.ic_mountaineering
            "air_walker"->R.drawable.ic_air_walker
            "cooldown"->R.drawable.ic_cooldown
            "horse_riding"->R.drawable.ic_horse_riding
            "kendo"->R.drawable.ic_kendo

            //rafting,boating,athletic,auto racing
            else -> R.drawable.ic_act_walking
        }
    }
//
//    fun getImageForApp(functionId:Int): Int {
//       return when (functionId) {
//            0->R.drawable.ic_workout_sort
//            1->R.drawable.ic_workouts_history_sort
//            2->R.drawable.ic_activity_sort
//            3->R.drawable.ic_heart_rate_sort
//            4->R.drawable.ic_sleep_sort
//            5->R.drawable.ic_blood_oxygen_sort
//            6->R.drawable.ic_alarm_sort
//            7->R.drawable.ic_event_reminder_sort
//            8->R.drawable.ic_stopwatch_sort
//            9->R.drawable.ic_timer_sort
//            10->R.drawable.ic_music_sort
//            11->R.drawable.ic_weather_sort
//            12->R.drawable.ic_breathing_sort
//            13->R.drawable.ic_notification_sort
//            14->R.drawable.ic_find_phone_sort
//            15->R.drawable.ic_setting_sort
//            16->R.drawable.ic_femal_sort
//            /*17->"Stress"
//            18->"World clock"
//            19->"Stocks"
//            20->"Air pressure"
//            21->"Compass"
//            22->"ECG"
//            23->"Temperature"
//            24->"Phone"
//            25->"Contacts"
//            26->"Frequent contacts"
//            27->"Remote camera"*/
//            else -> R.drawable.ic_sports_workout
//        }
//    }

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