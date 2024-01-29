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
import com.noisefit.data.googleFit.GoogleFitDataObservers
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentGoogleFitBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.web.WebViewActivity
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "GoogleFitFragment"
private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1980

@AndroidEntryPoint
class GoogleFitFragment :
    BaseFragment<FragmentGoogleFitBinding>(FragmentGoogleFitBinding::inflate) {

    @Inject
    lateinit var googleFitDataObservers: GoogleFitDataObservers

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

//        binding.bRequestData.setOnClickListener {
//            readGoals()
////            readWorkoutData()
//            readWorkoutFromSession()
//            insertWeightHeight(requireContext(), DataType.TYPE_WEIGHT, 170f);//weight in kg
//            //insertWeightHeight(requireContext(), DataType.TYPE_HEIGHT, 1.75f);//height in meter
//            getHeightWeight()
//        }
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


    private fun provideDataSource(streamName: String, dataType: DataType): DataSource {
        return DataSource.Builder()
            .setAppPackageName(requireContext().packageName)
            .setDataType(dataType)
            .setStreamName(" - $streamName")
            .setType(DataSource.TYPE_RAW)
            .build()
    }


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




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                try {
                    localDataStore.setGoogleFitStatus(true)
                    setGoogleFitSwitchState(true)
                    googleFitDataObservers.saveUserWeightAndHeight()
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


//    private val goalsReadRequest: GoalsReadRequest by lazy {
//        GoalsReadRequest.Builder()
//            .addDataType(DataType.TYPE_HEART_POINTS)
//            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
//            .addDataType(DataType.TYPE_DISTANCE_DELTA)
//
//            .build()
//    }

//    private fun readGoals() {
//        Fitness.getGoalsClient(requireContext(), googleSignInAccount)
//            .readCurrentGoals(goalsReadRequest)
//            .addOnSuccessListener { goals ->
//                // There should be at most one heart points goal currently.
//                goals.forEach {
//                    // What is the value of the goal
//                    val goalValue = it.metricObjective
//                    LOGS.i(TAG, "Goal value: $goalValue")
//
//                    // How is the goal measured?
//                    LOGS.i(TAG, "Objective: ${it.objective}")
//
//                    LOGS.i(TAG, "Objective: ${it.objectiveType}")
//
//                    // How often does the goal repeat?
//                    LOGS.i(TAG, "Recurrence: ${it.recurrence}")
//                }
//            }
//    }

//    private val Goal.objective: String
//        get() = when (objectiveType) {
//            OBJECTIVE_TYPE_DURATION ->
//                "Duration (s): ${durationObjective.getDuration(TimeUnit.SECONDS)}"
//
//            OBJECTIVE_TYPE_FREQUENCY ->
//                "Frequency : ${frequencyObjective.frequency}"
//
//            OBJECTIVE_TYPE_METRIC ->
//                "Metric : ${metricObjective.dataTypeName} - ${metricObjective.value}"
//
//            else -> "Unknown objective"
//        }

//    private val Goal.recurrenceDetails: String
//        get() = recurrence?.let {
//            val period = when (it.unit) {
//                Goal.Recurrence.UNIT_DAY -> "days"
//                Goal.Recurrence.UNIT_WEEK -> "weeks"
//                Goal.Recurrence.UNIT_MONTH -> "months"
//                else -> "Unknown"
//            }
//            "Every ${recurrence!!.count} $period"
//        } ?: "Does not repeat"


    override fun subscribeObservers() {

    }
}

