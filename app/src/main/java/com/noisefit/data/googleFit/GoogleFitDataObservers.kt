package com.noisefit.data.googleFit

import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessActivities
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.SessionInsertRequest
import com.google.gson.Gson
import com.noisefit_commans.models.*
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val NOISE_ACTIVITY = "noise_activity"
private const val SLEEP_SESSION_NAME = "Noise - Sleep Data"

class GoogleFitDataObservers
@Inject constructor(
    private val context: Context,
    private val googleSignInAccount: GoogleSignInAccount
) {

    fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, ACTIVITY_RECOGNITION
            ) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    private fun getSleepType(sleepType: String): Int? {
        when (sleepType.lowercase()) {
            "awake" -> {
                return SleepStages.AWAKE
            }
            "light" -> {
                return SleepStages.SLEEP_LIGHT
            }
            "deep" -> {
                return SleepStages.SLEEP_DEEP
            }
            "rem" -> {
                return SleepStages.SLEEP_REM
            }
            else -> {
                return null
            }
        }
    }

    @SuppressLint("CheckResult")
    fun insertSleepData(
        sleepData: SleepDataGoogleFit,
        success: () -> Unit,
        failed: () -> Unit
    ) {


//        LOGS.d("Sleep DATA ${Gson().toJson(sleepData)}")
        val session = Session.Builder()
            .setName(SLEEP_SESSION_NAME)
            .setIdentifier(context.packageName)
            .setDescription("$NOISE_ACTIVITY sleep data")
            .setStartTime(sleepData.startTime, TimeUnit.MILLISECONDS)
            .setEndTime(sleepData.endTime, TimeUnit.MILLISECONDS)
            .setActivity(FitnessActivities.SLEEP)
            .build()

        val dataSource = provideDataSource("sleep", DataType.TYPE_SLEEP_SEGMENT)

        val dataPoints1 = ArrayList<DataPoint>()
        sleepData.sleepArray?.forEach { sleepDataBreakup ->

            getSleepType(sleepDataBreakup.sleepType)?.let { sleepType ->
                dataPoints1.add(
                    DataPoint.builder(dataSource)
                        .setTimeInterval(
                            sleepDataBreakup.startTime,
                            sleepDataBreakup.endTime,
                            TimeUnit.MILLISECONDS
                        )
                        .setField(Field.FIELD_SLEEP_SEGMENT_TYPE, sleepType)
                        .build()
                )
            }

        }

        val dataSet = DataSet.builder(dataSource)
            .addAll(dataPoints1)
            .build()

        val sessionInsertRequest = SessionInsertRequest.Builder()
            .setSession(session)
            .addDataSet(dataSet)
            .build()

        Fitness.getSessionsClient(
            context,
            googleSignInAccount
        ).insertSession(sessionInsertRequest).addOnSuccessListener {
            success.invoke()
        }.addOnFailureListener {
            failed.invoke()
            it.printStackTrace()
        }

    }


    @SuppressLint("CheckResult")
    fun insertHeartData(
        heartDataList: List<HeartRate>,
        success: () -> Unit,
        failed: () -> Unit
    ) {

        val dataSource = provideDataSource("Heart data", DataType.TYPE_HEART_RATE_BPM)
        val dataPointList = ArrayList<DataPoint>()

        heartDataList.forEach { heartRate ->

            if (heartRate.date == null || heartRate.time == null) {
                return@forEach
            }
            val time =
                DateFormats.convertDateTimeToTimeStamp(heartRate.date!!, heartRate.time!!)

            dataPointList.add(
                DataPoint.builder(dataSource)
                    .setField(Field.FIELD_BPM, heartRate.averageHeartRate.toFloat())
                    .setTimestamp(time, TimeUnit.MILLISECONDS)
                    .build()
            )
        }


        //TODO: Add min and max heart rate
        val dataSet = DataSet.builder(dataSource)
            .addAll(dataPointList)
            .build()


        Fitness.getHistoryClient(
            context,
            googleSignInAccount
        ).insertData(dataSet).addOnCompleteListener {
            if (it.isSuccessful) {
                success.invoke()

            } else {
                failed.invoke()
                it.exception
            }
        }


    }

    private fun provideDataSource(streamName: String, dataType: DataType): DataSource {
        return DataSource.Builder()
            .setAppPackageName(context.packageName)
            .setDataType(dataType)
            .setStreamName("$NOISE_ACTIVITY - $streamName")
            .setType(DataSource.TYPE_RAW)
            .build()
    }

    @SuppressLint("CheckResult")
    fun insertStepData(
        stepsData: StepDataGoogleFit,
        success: () -> Unit,
        failed: () -> Unit
    ) {

        val dataSource = provideDataSource("step count", DataType.TYPE_STEP_COUNT_DELTA)

        val dataPoint =
            DataPoint.builder(dataSource)
                .setField(Field.FIELD_STEPS, stepsData.steps)
                .setTimeInterval(stepsData.startTime, stepsData.endTime, TimeUnit.MILLISECONDS)
                .build()

        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()


        Fitness.getHistoryClient(
            context,
            googleSignInAccount
        ).insertData(dataSet).addOnCompleteListener {
            if (it.isSuccessful) {
                success.invoke()
                //emit(GoogleFitResponse(isSuccess))

            } else {
                failed.invoke()
                it.exception
            }
        }


    }


    private fun getActivityType(type: String): String {
        when (type) {

            "skateboarding" -> {
                return FitnessActivities.SKATEBOARDING
            }
            "fencing" -> {
                return FitnessActivities.FENCING
            }
            "boxing" -> {
                return FitnessActivities.BOXING
            }
            "curling" -> {
                return FitnessActivities.CURLING
            }
            "indoor_skating" -> {
                return FitnessActivities.SKATING_INDOOR
            }
            "archery" -> {
                return FitnessActivities.ARCHERY
            }
            "outdoor_hiking" -> {
                return FitnessActivities.HIKING
            }
            "zumba" -> {
                return FitnessActivities.ZUMBA
            }
            "mixed_aerobics" -> {
                return FitnessActivities.AEROBICS
            }
            "strength_training" -> {
                return FitnessActivities.STRENGTH_TRAINING
            }
            "elliptical_machine" -> {
                return FitnessActivities.ELLIPTICAL
            }
            "yoga" -> {
                return FitnessActivities.YOGA
            }
            "climbing_machine" -> {
                return FitnessActivities.STAIR_CLIMBING
            }
            "gymnastics" -> {
                return FitnessActivities.GYMNASTICS
            }
            "sailing" -> {
                return FitnessActivities.SAILING
            }
            "roller_skating" -> {
                return FitnessActivities.SKATING
            }
            "baseball" -> {
                return FitnessActivities.BASEBALL
            }
            "squash" -> {
                return FitnessActivities.SQUASH
            }
            "softball" -> {
                return FitnessActivities.SOFTBALL
            }
            "volleyball" -> {
                return FitnessActivities.VOLLEYBALL
            }
            "handball" -> {
                return FitnessActivities.HANDBALL
            }
            "dance", "ballroom_dancing", "street_dance", "belly_dance" -> {
                return FitnessActivities.DANCING
            }
            "martial_arts" -> {
                return FitnessActivities.MARTIAL_ARTS
            }
            "rope_skipping" -> {
                return FitnessActivities.JUMP_ROPE
            }
            "outdoor_running", "running" -> {
                return FitnessActivities.RUNNING
            }
            "bicycling" -> {
                return FitnessActivities.BIKING
            }
            "climbing" -> {
                return FitnessActivities.ROCK_CLIMBING
            }
            "treadmill" -> {
                return FitnessActivities.TREADMILL
            }
            "workout" -> {
                return FitnessActivities.UNKNOWN
            }
            "basketball" -> {
                return FitnessActivities.BASKETBALL
            }
            "football" -> {
                return FitnessActivities.FOOTBALL_SOCCER
            }
            "tennis" -> {
                return FitnessActivities.TENNIS
            }

            "badminton" -> {
                return FitnessActivities.BADMINTON
            }
            "swimming" -> {
                return FitnessActivities.SWIMMING
            }
            "outdoor_walking" -> {
                return FitnessActivities.WALKING
            }
            "spinning" -> {
                return FitnessActivities.BIKING_SPINNING
            }
            "hiking" -> {
                return FitnessActivities.HIKING
            }
            "outdoor_cycling" -> {
                return FitnessActivities.BIKING
            }

            "indoor_walking" -> {
                return FitnessActivities.WALKING
            }
            "indoor_running" -> {
                return FitnessActivities.RUNNING
            }
            "indoor_cycling","cycling" -> {
                return FitnessActivities.BIKING
            }
            "elliptical" -> {
                return FitnessActivities.ELLIPTICAL
            }
            "rower", "rowing_machine" -> {
                return FitnessActivities.ROWING
            }
            "cricket" -> {
                return FitnessActivities.CRICKET
            }
            "pool_swimming" -> {
                return FitnessActivities.SWIMMING_POOL
            }
            "open_water_swimming" -> {
                return FitnessActivities.SWIMMING_OPEN_WATER
            }
            else -> {
                return FitnessActivities.OTHER
            }

        }
    }


    fun insertActivityData(
        sportsData: SportsDataGoogleFit
    ) {


        val distanceDataSet = try {
            val distanceDataSource = provideDataSource("Distance", DataType.TYPE_DISTANCE_DELTA)
            val distancePoint = DataPoint.builder(distanceDataSource)
                .setTimeInterval(sportsData.startTime, sportsData.endTime, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_DISTANCE, sportsData.distance)
                .build()

            DataSet.builder(distanceDataSource)
                .addAll(listOf(distancePoint))
                .build()
        } catch (exp: Exception) {
            null
        }


        val stepsDataSet = try {
            val stepsDataSource = provideDataSource("Steps", DataType.TYPE_STEP_COUNT_DELTA)

            val stepsPoint = DataPoint.builder(stepsDataSource)
                .setTimeInterval(sportsData.startTime, sportsData.endTime, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_STEPS, sportsData.steps)
                .build()

            DataSet.builder(stepsDataSource)
                .addAll(listOf(stepsPoint))
                .build()
        } catch (exp: Exception) {
            null
        }


        val caloriesDataSet = try {
            val caloriesDataSource = provideDataSource("Calories", DataType.TYPE_CALORIES_EXPENDED)

            val caloriesPoint = DataPoint.builder(caloriesDataSource)
                .setTimeInterval(sportsData.startTime, sportsData.endTime, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_CALORIES, sportsData.calories)
                .build()

            DataSet.builder(caloriesDataSource)
                .addAll(listOf(caloriesPoint))
                .build()
        } catch (exp: Exception) {
            null
        }


        val heartsDataSet = try {
            val heartsDataSource = provideDataSource("Heart Rate", DataType.TYPE_HEART_RATE_BPM)

            val heartsPoint = DataPoint.builder(heartsDataSource)
                .setTimeInterval(sportsData.startTime, sportsData.endTime, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_BPM, sportsData.heartRate)
                .build()

            DataSet.builder(heartsDataSource)
                .addAll(listOf(heartsPoint))
                .build()
        } catch (exp: Exception) {
            null
        }


        /*
    val locationPointList = ArrayList<DataPoint>()

    sportsData.locationList.forEach { locationDataModel ->
        val locationPoint = DataPoint.builder(locationDataSource)
            .setTimeInterval(
                locationDataModel.timeStamp,
                locationDataModel.timeStamp,
                TimeUnit.MILLISECONDS
            )
            .setField(Field.FIELD_LATITUDE, locationDataModel.latitude.toFloat())
            .setField(Field.FIELD_LONGITUDE, locationDataModel.longitude.toFloat())
            .setField(Field.FIELD_ACCURACY, locationDataModel.accuracy.toFloat())
            .setField(Field.FIELD_ALTITUDE, locationDataModel.altitude.toFloat())

            .build()
        locationPointList.add(locationPoint)
    }

     */

        val session = try {
            val activity = getActivityType(sportsData.type)
            Session.Builder()
                .setName(activity)
                .setIdentifier("GoNoise ${System.currentTimeMillis()}")
                .setDescription("Running Session Details")
                .setActivity(activity)
                .setStartTime(sportsData.startTime, TimeUnit.MILLISECONDS)
                .setEndTime(sportsData.endTime, TimeUnit.MILLISECONDS)
                .build()
        }catch (exp  :Exception){
            null
        }

        if(session==null){
            LOGS.e("Failed to insert activities in google fit")
            return
        }
        val sessionBuilder = SessionInsertRequest.Builder()
            .setSession(session)


        distanceDataSet?.let {
            sessionBuilder.addDataSet(it)
        }
        stepsDataSet?.let {
            sessionBuilder.addDataSet(it)
        }
        caloriesDataSet?.let {
            sessionBuilder.addDataSet(it)
        }
        heartsDataSet?.let {
            sessionBuilder.addDataSet(it)
        }

        val sessionInsertRequest = sessionBuilder.build()


        val success = Fitness.getSessionsClient(
            context,
            googleSignInAccount
        ).insertSession(sessionInsertRequest).isSuccessful
        if (success) {

        } else {

        }

    }


}