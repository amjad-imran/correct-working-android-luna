package com.noisefit.ui.onboarding.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentJoinNoisefitBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.onboarding.onboardProfile.ProfileSetupActivity
import com.noisefit.ui.onboarding.pairing.DeviceSetupActivity
import com.noisefit.ui.onboarding.pairing.PairDeviceActivity
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


const val GOOGLE_SIGN_IN: Int = 1341

const val FB_FIRST_NAME = "first_name"
const val FB_LAST_NAME = "last_name"
const val FB_PICTURE = "picture"
const val FB_ID = "id"
const val FB_EMAIL = "email"
const val FB_PUBLIC_PROFILE = "public_profile"

@AndroidEntryPoint
class JoinNoisefitFragment :
    BaseFragment<FragmentJoinNoisefitBinding>(FragmentJoinNoisefitBinding::inflate) {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient


    private val authViewModel: AuthViewModel by activityViewModels()

    private var callbackManager: CallbackManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.LAND_ON_START_FITNESS_PAGE_VISIT)
        authViewModel.resetData()
    }

    override fun initListener() {
        binding.vGoogle.setOnClickListener {
            binding.progressBar.root.visible()
            authViewModel.loginMethod = "google"
            googleSignIn()
        }
        binding.vFacebook.setOnClickListener {
            authViewModel.loginMethod = "facebook"
            facebookLogin()
            binding.facebookHideBtn.performClick()
        }
        binding.vEmail.setOnClickListener {
            authViewModel.loginMethod = "email"
            navigate(
                JoinNoisefitFragmentDirections.actionJoinNoisefitFragmentToEmailFragment(
                    EmailMode.LOGIN
                )
            )
        }

    }

    override fun subscribeObservers() {

        authViewModel.authSuccess.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                if (value) {
                    if (authViewModel.isDevicePaired()) {
                        if (authViewModel.isProfileSetupComplete()) {
                            startActivity(DeviceSetupActivity.getStartIntent(requireContext(),
                                setupDevice = true))
                            activity?.finish()
                        } else {
                            startActivity(ProfileSetupActivity.getStartIntent(requireContext()))
                            activity?.finish()
                        }
                    } else {
                        startActivity(PairDeviceActivity.getStartIntent(requireContext()))
                    }

                    activity?.finish()
                }
            }
        }

        authViewModel.verifyMobile.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                if (value) {
                    navigate(R.id.otpNumberFragment)
                }
            }
        }

        authViewModel.verifyEmail.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                if (value) {
                    navigate(
                        JoinNoisefitFragmentDirections.actionJoinNoisefitFragmentToEmailFragment(
                            EmailMode.VERIFY
                        )
                    )
                }
            }
        }

        authViewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        authViewModel.createUser.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                if (value) {
                    authViewModel.createInternationalUser()
                }
            }
        }

    }

    private fun facebookLogin() {
        callbackManager = CallbackManager.Factory.create()
        binding.facebookHideBtn.setPermissions(listOf(FB_PUBLIC_PROFILE, FB_EMAIL))
        binding.facebookHideBtn.fragment = this
        binding.facebookHideBtn.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val request = GraphRequest.newMeRequest(
                        loginResult.accessToken
                    ) { response, _ ->
                        if (response != null) {
                            authViewModel.handleSocialLogin(
                                loginResult.accessToken.token,
                                AuthMode.facebook,
                                null
                            )
                            disconnectFromFacebook()
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString(
                        "fields",
                        "$FB_ID, $FB_EMAIL, $FB_FIRST_NAME, $FB_LAST_NAME, $FB_PICTURE"
                    )
                    request.parameters = parameters
                    request.executeAsync()

                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException) {
                    error.printStackTrace()
                    uiController.onDisplayError(getString(R.string.text_something_went_wrong))
                }
            })
    }

    private fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return  // already logged out
        }
        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
            { LoginManager.getInstance().logOut() }
        ).executeAsync()
    }

    private fun googleSignIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            nullableBinding?.progressBar?.root?.gone()
            try {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } catch (exp: Exception) {
                authViewModel.sessionManager.logCustomCrashlyticsEvents(
                    "JoinNoisefitFragment",
                    " Failure delivering result-Google",
                    exp
                )
            }

        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            authViewModel.handleSocialLogin(
                account.idToken ?: "",
                AuthMode.google,
                account.photoUrl.toString()
            )
            googleLogout()
        } catch (e: ApiException) {
            if (e.statusCode == 12501) { //Back Pressed on the dialog
                return
            }
            uiController.onDisplayError(getString(R.string.text_something_went_wrong))
            LOGS.d("signInResult:failed code=" + e.statusCode)
        }
    }

    private fun googleLogout() {
        googleSignInClient.signOut()
    }
}