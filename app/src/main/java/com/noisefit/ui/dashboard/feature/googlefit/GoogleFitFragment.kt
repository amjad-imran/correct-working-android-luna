package com.noisefit.ui.dashboard.feature.googlefit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.gson.Gson
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentGoogleFitBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.*
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1980

@AndroidEntryPoint
class GoogleFitFragment :
    BaseFragment<FragmentGoogleFitBinding>(FragmentGoogleFitBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var fitnessOptions: FitnessOptions

    @Inject
    lateinit var googleSignInAccount: GoogleSignInAccount

    @Inject
    lateinit var googleSignInOptions: GoogleSignInOptions

    @Inject
    lateinit var sessionManager: SessionManager

    private val TAG = "GoogleFitFragment"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGoogleFitSwitchState(localDataStore.isEnableGoogleFit())

        requestPermission()

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    2233
                )
            }
        }
    }

    override fun initListener() {
        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_google_fit)
            tvTitle.text = getString(R.string.text_google_fit)
            tvTitleDisc.text = getString(R.string.text_learn_more)
            tvTitleDisc.setTextColor(requireActivity().resources.getColor(com.noisefit_commans.R.color.text_accent_color))

        }
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_google_fit)
            tvDesc.gone()
            //  tvDesc.text = getString(R.string.text_allow_your_workouts_and_health_data)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }


        binding.lytGoogleFitDisclosure.tvPrivacyPolicy.setOnClickListener {
            activity?.let {
                startActivity(
                    WebViewActivity.getStartIntent(
                        it,
                        getString(R.string.text_privacy_policy),
                        AppConstants.URL_PRIVACY_POLICY
                    )
                )
            }
        }
        binding.lytGoogleFitDisclosure.tvGoogleFitLearnMore.setOnClickListener {
            activity?.let {
                startActivity(
                    WebViewActivity.getStartIntent(
                        it,
                        getString(R.string.text_google_fit),
                        AppConstants.URL_GOOGLE_FIT
                    )
                )
            }
        }
        binding.bConnect.setOnClickListener {
            if (binding.bConnect.text.equals(getString(R.string.text_disconnect))) {
                logOutFit()

            } else {
                fitSignIn()

            }
        }


        binding.lytFeatureTile.llSwitch.invisible()

        binding.bRequestData.setOnClickListener {
            //readStepsData()
        readWorkoutData()
        }
    }

//    private val activityRecognitionPermissionResult = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) {
//        if (it) {
//            if (localDataStore.isEnableGoogleFit()) {
//                logOutFit()
//            } else {
//                fitSignIn()
//            }
//        } else {
//            context.showShortToast("Permission Required")
//        }
//    }

    private fun logOutFit() {


//        if (oAuthPermissionsApproved()) {
        context?.let { context ->

            Fitness.getConfigClient(
                context,
                googleSignInAccount
            )
                .disableFit()
                .addOnSuccessListener {
                    setGoogleFitSwitchState(false)
                    uiController.onDisplayError(getString(R.string.text_google_fit_disable))
                    localDataStore.setGoogleFitStatus(false)
                    GoogleSignIn.getClient(context, googleSignInOptions).signOut()

                    sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.GOOGLE_FIT_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = false
                        })
                }
                .addOnFailureListener { e ->
                    uiController.onDisplayError(getString(R.string.text_google_fit_disable))
                    localDataStore.setGoogleFitStatus(false)
                    setGoogleFitSwitchState(false)
                    GoogleSignIn.getClient(context, googleSignInOptions).signOut()
                    sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.GOOGLE_FIT_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = false
                        })

                    e.printStackTrace()
                }
        }

//        }
    }

    private fun fitSignIn() {
        //if (!oAuthPermissionsApproved()) {
        GoogleSignIn.requestPermissions(
            this,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            googleSignInAccount, fitnessOptions
        )
//        } else {
//
//            localDataStore.setGoogleFitStatus(true)`
//            displayToast(getString(R.string.text_google_fit_enable))
//        }
    }

    private fun setGoogleFitSwitchState(isChecked: Boolean) {
        if (!isChecked) {
            nullableBinding?.lytGoogleFitDisclosure?.apply {
                tvConnection.visible()
                tvConnectionText.visible()
            }
            //   binding.bConnect.setBackgroundDrawable()
            nullableBinding?.bConnect?.text = getString(R.string.text_connect)
        } else {
            nullableBinding?.lytGoogleFitDisclosure?.apply {
                tvConnection.gone()
                tvConnectionText.gone()
            }
            nullableBinding?.bConnect?.text = getString(R.string.text_disconnect)
        }

    }

    private fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(requireActivity()) != null && oAuthPermissionsApproved()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                try {
                    localDataStore.setGoogleFitStatus(true)
                    setGoogleFitSwitchState(true)
                    sessionManager.logInsiderAppEvent(
                        InsiderAppEvents.GOOGLE_FIT_CLICK,
                        HashMap<String, Any>().apply {
                            this["is_enabled"] = true
                        })

                    uiController.onDisplayError(getString(R.string.text_google_fit_enable))
                } catch (e: Exception) {
                    //Null pointers on view destroyed
                }
            }

            else -> {
                try {
                    oAuthErrorMsg(requestCode, resultCode)
                } catch (e: Exception) {
                    //Null pointers on view destroyed
                }
            }
        }
    }

    private fun oAuthPermissionsApproved(): Boolean {
        return GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)
    }

    private fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        localDataStore.setGoogleFitStatus(false)
        setGoogleFitSwitchState(false)
        uiController.onDisplayError(getString(R.string.text_something_went_wrong))
        val message = """
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        LOGS.e(message)
    }

    fun readStepsData(){
        val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())

        val datasource = DataSource.Builder()
            .setAppPackageName("com.google.android.gms")
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setType(DataSource.TYPE_DERIVED)
            .setStreamName("estimated_steps")
            .build()

        val request = DataReadRequest.Builder()
            .aggregate(datasource)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        Fitness.getHistoryClient(requireActivity(), GoogleSignIn.getAccountForExtension(requireActivity(), fitnessOptions))
            .readData(request)
            .addOnSuccessListener { response ->
                val totalSteps = response.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .sumBy { it.getValue(Field.FIELD_STEPS).asInt() }
                LOGS.d(TAG,"Steps $totalSteps")

            }


    }

    fun readWorkoutData() {


        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_WORKOUT_EXERCISE)
            /*.aggregate(DataType.TYPE_DISTANCE_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED)
            .aggregate(DataType.TYPE_HEART_RATE_BPM)
            .read(DataType.TYPE_WORKOUT_EXERCISE)*/
            /*.read(DataType.AGGREGATE_MOVE_MINUTES)
            .read(DataType.TYPE_MOVE_MINUTES)*/
            /*.enableServerQueries()
            .bucketByActivitySegment(1, TimeUnit.MINUTES)*/
            .setTimeRange(1696918645, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(
            requireActivity(),
            googleSignInAccount
        )
            .readData(readRequest)
            .addOnSuccessListener { dataReadResponse: DataReadResponse? ->

                //LOGS.d(TAG, "DataSET ${Gson().toJson(dataReadResponse)}")
                if(dataReadResponse==null) return@addOnSuccessListener

             /*   Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.toString());
                Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.getStatus());
                Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA));
                Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.getBuckets().get(0));
                Log.d("TAG_F", "onSuccess: 1 " + dataReadResponse.getBuckets().get(0).getDataSets().size);*/


                for (bucket in dataReadResponse.buckets){
                    for (data in bucket.dataSets){
                        for (point in data.dataPoints) {
                            LOGS.d(TAG,"Found Point ${data.dataPoints}")
                            when (point.dataType) {
                                //DataType.TYPE_WORKOUT_EXERCISE ->LOGS.d(TAG,""+point.getValue(Field.FIELD_ACTIVITY).asFloat())
                                DataType.AGGREGATE_DISTANCE_DELTA   -> LOGS.d(TAG,""+point.getValue(Field.FIELD_DISTANCE).asFloat())
                                DataType.TYPE_HEART_RATE_BPM        -> LOGS.d(TAG,""+  point.getValue(
                                Field.FIELD_BPM).asFloat()               )
                                DataType.TYPE_CALORIES_EXPENDED     -> LOGS.d(  TAG,""+point.getValue(Field.FIELD_CALORIES).asFloat()          )
                                DataType.TYPE_WORKOUT_EXERCISE      -> LOGS.d( TAG,""+"[${point.getValue(Field.FIELD_EXERCISE).asString()}]   ")
                                DataType.TYPE_MOVE_MINUTES          -> LOGS.d(TAG,""+ "Move Minutes          $point     ")
                                DataType.AGGREGATE_MOVE_MINUTES     -> LOGS.d(TAG,""+ "Moving Mins  Count    $point     ")
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e: Exception? ->
                LOGS.w(TAG, "Failure ${e?.message}")
                e?.printStackTrace()
            }
    }


    override fun subscribeObservers() {

    }
}

