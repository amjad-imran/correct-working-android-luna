package com.noisefit.ui.dashboard.feature.googlefit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGoogleFitSwitchState(localDataStore.isEnableGoogleFit())

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

    fun readWorkoutData() {
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_WORKOUT_EXERCISE)
            .setTimeRange(1696918645, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(
            requireActivity(),
            googleSignInAccount
        )
            .readData(readRequest)
            .addOnSuccessListener { dataReadResponse: DataReadResponse? ->
                LOGS.d("GoogleFitTestFragment", "DataSET ${Gson().toJson(dataReadResponse)}")
            }
            .addOnFailureListener { e: Exception? ->
                LOGS.w("GoogleFitTestFragment", "Failure ${e?.message}")
                e?.printStackTrace()
            }
    }


    override fun subscribeObservers() {

    }
}

