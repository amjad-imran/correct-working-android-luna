package com.noisefit_ryeex_sdk.dataConversion

import android.content.Context
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.SportsModeList
import com.ryeex.watch.protocol.pb.entity.PBSport
import java.io.File

class RyeexConst {

    companion object {
        const val OUTDOOR_RUN = "Outdoor_Run"
        const val INDOOR_RUN = "Indoor_Run"
        const val OUTDOOR_WALK = "Outdoor_Walk"
        const val INDOOR_WALK = "Indoor_Walk"
        const val OUTDOOR_CYCLE = "Outdoor_Cycle"
        const val INDOOR_CYCLE = "Indoor_Cycle"
        const val FREE = "Free_Training"
        const val INDOOR_SWIM = "Swimming"
        const val YOGA = "Yoga"
        const val SOCCER = "Football"
        const val STRENGTH_TRAINING = "Strength_Training"
        const val BASKETBALL = "Basketball"
        const val TABLE_TENNIS = "Table_Tennis"
        const val BADMINTON = "Badminton"
        const val ELLIPTICAL = "Elliptical"
        const val CRICKET = "Cricket"
        const val MOUNTAINEERING = "Mountaineering"
        const val TRAIL_RUNNING = "Trail_Running"
        const val SKIING = "Skiing"
        const val ROWER = "Rower"
        const val SPINNING = "Spinning"
        const val AIR_WALKER = "Air_Walker"
        const val HIKING = "Hiking"
        const val TENNIS = "Tennis"
        const val FOLK_DANCE = "Folk_Dance"
        const val DANCE = "Dance"
        const val COOL_DOWN = "Cooldown"
        const val CROSS_TRAINING = "Cross_Training"
        const val PILATES = "Pilates"
        const val CROSS_FIT = "Cross_Fit"
        const val FUNCTIONAL_TRAINING = "Functional_Training"
        const val PHYSICAL_TRAINING = "Physical_Training"
        const val MIXED_CARDIO = "Mixed_Cardio"
        const val LATIN_DANCE = "Latin_Dance"
        const val STREET_DANCE = "Street_Dance"
        const val KICKBOXING = "Kickboxing"
        const val BARRE = "Ballet"
        const val AUSTRALIAN_FOOTBALL = "Australian_Football"
        const val BASEBALL = "Baseball"
        const val BOWLING = "Bowling"
        const val RACQUETBALL = "Racquetball"
        const val CURLING = "Curling"
        const val HUNTING = "Hunting"
        const val SNOWBOARDING = "Snowboarding"
        const val FISHING = "Fishing"
        const val DISC_SPORTS = "Disc_Sports"
        const val RUGBY = "Rugby"
        const val GOLF = "Golf"
        const val DOWNHILL_SKIING = "Downhill_Skiing"
        const val CORE_TRAINING = "Core_Training"
        const val SKATING = "Skating"
        const val FITNESS_GAMING = "Fitness_Gaming"
        const val AEROBICS = "Aerobics"
        const val GROUP_TRAINING = "Group_Training"
        const val KENDO = "Kendo"
        const val FENCING = "Fencing"
        const val SOFTBALL = "Softball"
        const val STAIRS = "Stairs"
        const val AMERICAN_FOOTBALL = "American_Football"
        const val VOLLEYBALL = "Volleyball"
        const val ROLLING = "Rolling"
        const val PICKLEBALL = "Pickleball"
        const val HOCKEY = "Hockey"
        const val BOXING = "Boxing"
        const val TAEKWONDO = "Taekwondo"
        const val KARATE = "Karate"
        const val FLEXIBILITY = "Flexibility"
        const val HANDBALL = "Handball"
        const val HAND_CYCLING = "Hand_Cycling"
        const val MIND_BODY = "Mind&Body"
        const val WRESTLING = "Wrestling"
        const val STEP_TRAINING = "Step_Training"
        const val TAI_CHI = "Tai_Chi"
        const val GYMNASTICS = "Gymnastics"
        const val TRACK_FIELD = "Track_Field"
        const val JUMP_ROPE = "Jump_Rope"
        const val MARTIAL_ARTS = "Martial_Arts"
        const val PLAY = "Leisure"
        const val SNOW_SPORTS = "Snow_Sports"
        const val LACROSSE = "Lacrosse"
        const val SINGLE_BAR = "Single_Bar"
        const val PARALLEL_BARS = "Parallel_Bars"
        const val ROLLER_SKATING = "Roller_Skating"
        const val HULA_HOOP = "Hula_Hoop"
        const val DARTS = "Darts"
        const val ARCHERY = "Archery"
        const val HORSE_RIDING = "Horse_Riding"
        const val SHUTTLECOCK = "Shuttlecock"
        const val ICE_HOCKEY = "Ice_Hockey"
        const val SIT_UP = "Sit_Up"
        const val WAIST_TRAINING = "Waist_Training"
        const val PARKOUR = "Parkour"
        const val CLIMB = "Climb"
        const val KITE = "Kite"
        const val TUG_OF_WAR = "Tug_Of_War"
        const val BILLIARDS = "Billiards"
        const val DRIFTING = "Drifting"
        const val DRAGON_BOAT = "Dragon_Boat"
        const val MOTORBOAT = "Motorboat"
        const val RACING_CAR = "Racing_Car"
        const val JUDO = "Judo"
        const val HIIT = "HIIT"
        const val PLANK = "Plank"
        const val PULLUP = "Pull-up"
        const val PUSHUPS = "Push-ups"
        const val SHOOTING = "Shooting"
        const val HIGH_JUMP = "High_Jump"
        const val BALANCE_CAR = "Balance_Car"
        const val LONG_JUMP = "Long_Jump"
        const val MARATHON = "Marathon"
        const val TREADMILL = "Treadmill"
        const val TRAMPOLINE = "Trampoline"
        const val BUNGEE_JUMPING = "Bungee_Jumping"
        const val SKATEBOARD = "Skateboard"
        const val BOATING = "Boating"
        const val KABADDI = "Kabaddi"
        const val SQUAT = "Squat"
        const val BURPEES = "Burpees"


        const val KEY_APP_MESSENGER = "app.messenger"
        const val KEY_APP_VIBER = "app.viber"
        const val KEY_APP_SKYPE = "app.skype"
        const val KEY_APP_GMAIL = "app.gmail"
        const val KEY_APP_GOOGLE_MAP = "app.googlemaps"
        const val KEY_APP_YOUTUBE = "app.youtube"
        const val KEY_APP_TELEGRAM = "app.telegram"
        const val KEY_APP_WX = "app.wx"
        const val KEY_APP_FACEBOOK = "app.facebook"
        const val KEY_APP_WHATSAPP = "app.whatsapp"
        const val KEY_APP_TWITTER = "app.twitter"
        const val KEY_APP_LINE = "app.line"
        const val KEY_APP_INSTAGRAM = "app.instagram"
        const val KEY_APP_OUTLOOK = "app.outlook"
        const val KEY_APP_CALENDAR = "app.calendar"
        const val KEY_APP_OTHERS = "app.others"

        const val WATCH_APP_ACTIVITY = "Activity"
        const val WATCH_APP_HEART_RATE = "Heart Rate"
        const val WATCH_APP_BLOOD_OXYGEN = "SpO2"
        const val WATCH_APP_SLEEP = "Sleep"
        const val WATCH_APP_SPORTS = "Workout"
        const val WATCH_APP_SPORTS_RECORD = "Workout Records"
        const val WATCH_APP_BREATHE = "Breath"
        const val WATCH_APP_ALARM_CLOCK = "Alarm"
        const val WATCH_APP_WEATHER = "Weather"
        const val WATCH_APP_WC_PAY = "WeChat Pay"
        const val WATCH_APP_SECOND = "Stopwatch"
        const val WATCH_APP_TIMER = "Timer"
        const val WATCH_APP_MUSIC_CONTROL = "Music Control"
        const val WATCH_APP_CAMERA = "Camera Remote"
        const val WATCH_APP_FIND_PHONE = "Find My Phone"
        const val WATCH_APP_SETTING = "Settings"
        const val WATCH_APP_ALIPAY = "Alipay"
        const val WATCH_APP_PHONE = "Phone Call"
        const val WATCH_APP_CONTACTS = "Favorite Contacts"
        const val WATCH_APP_FLASHLIGHT = "Flashlight"
        const val WATCH_APP_CALCULATOR = "Calculator"
        const val WATCH_APP_PRESSURE = "Pressure"
        const val WATCH_APP_TEMP = "Body Temperature"


        fun getAllSportList(): ArrayList<SportsModeList.SportsMode> {
            val sportTypes = PBSport.Type.values()
            val sportsModeList = ArrayList<SportsModeList.SportsMode>()
            for (i in sportTypes.indices) {
                if (sportTypes[i] == PBSport.Type.INDOOR_SWIM) {
                    continue
                }
                val sportMode = DataConverter.parseSportMode(sportTypes[i].number)
                sportMode.index = i
                sportsModeList.add(sportMode)
            }
            return sportsModeList
        }

        fun getPreviewSize(noiseFitDevice: ColorFitDevice?): FloatArray {
            return when (noiseFitDevice?.deviceType) {
                DeviceType.COLORFIT_MIGHTY.deviceType -> floatArrayOf(186f, 214f)
                DeviceType.NOISEFIT_NOVA.deviceType -> floatArrayOf(300f, 300f)
                else -> floatArrayOf(0f, 0f)
            }
        }

        private fun getCacheDir(context: Context): File {
            return context.cacheDir
        }

        fun getWatchFaceCacheDir(context: Context): String {
            return "${getCacheDir(context).absolutePath}/watch_faces"
        }
    }
}