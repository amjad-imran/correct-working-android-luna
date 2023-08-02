package com.noisefit.ui.onboarding.pairing

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.noisefit.luna.R
import com.noisefit.data.local.AppStaticData
import com.noisefit_commans.data.model.User
import com.noisefit.luna.databinding.ActivityDeviceSetupBinding
import com.noisefit.oreo.OreoMainActivity
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit.session.SessionManager
import com.noisefit.ui.common.BaseActivity
import com.noisefit_commans.ui.showShortToast
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.utils.LOW_VIBRATION
import com.noisefit_commans.utils.VibrationUtils
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.CustomReplyData
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.DeviceUnits
import com.noisefit_commans.models.Gender
import com.noisefit_commans.models.SportsModeList
import com.noisefit_commans.models.UnitSystem
import com.noisefit_commans.models.UserGoals
import com.noisefit_commans.models.UserInfo
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject


const val OPEN_PROFILE = "OPEN_PROFILE"

@AndroidEntryPoint
class DeviceSetupActivity : BaseActivity<ActivityDeviceSetupBinding>() {
    private var setupStarted = false

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var watchesSDK: WatchesSDK


    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private var user: User? = null
    private val viewModel: SetupProfileViewModel by viewModels()
    private val deviceSetupViewModel: DeviceSetupViewModel by viewModels()


    companion object {
        fun getStartIntent(context: Context, openProfile: Boolean = false): Intent {
            return Intent(context, DeviceSetupActivity::class.java).apply {
                this.putExtra(OPEN_PROFILE, openProfile)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.localDataStore.setDeviceSetupPendingStatus(true)
        deviceSetupViewModel.getDeviceType()

        binding.vPlayer.repeatCount = 0
        binding.vPlayer.setAnimation(deviceSetupViewModel.getAnimation(0))
        binding.vPlayer.playAnimation()
        deviceSetupViewModel.currentAnimation = 0


        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_device_setup_start)
        setupStarted = false

        viewModel.ringDataStore.getRingDevice()?.let {
            initUi(it)
            deviceSetupViewModel.updateUserDevice(it, true)
        }


        saveDefaultUserValue()


        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                showShortToast(getString(R.string.text_no_device_connected))
                sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_device_setup_failed,
                    HashMap<String, Any>().apply {
                        this["status"] = "failed"
                    })

                startMainActivity()
            }

        }, 5000)

    }

    private fun startMainActivity() {
        startActivity(OreoMainActivity.getStartIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })


    }

    private fun initUi(colorFitDevice: ColorFitDevice) {
        /* Glide.with(this)
             .load(colorFitDevice.url)
             .error(R.drawable.watch_default)
             .into(binding.rowWatchSelection.root.findViewById(R.id.ivWatchImage))
         binding.rowWatchSelection.root.findViewById<TextView>(R.id.tvWatchMacAddress).text =
             "MAC ${colorFitDevice.address}"

         binding.rowWatchSelection.root.findViewById<TextView>(R.id.tvWatchName).text =
             colorFitDevice.bluetoothName

         binding.bGetStarted.isEnabled = false*/
    }


    override fun logAppEvent(eventName: String, data: HashMap<String, Any?>) {

    }

    override fun initListener() {

        binding.vPlayer.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (deviceSetupViewModel.currentAnimation == 0) {

                    val connectState =
                        sessionManager.connectStateRing.value


                    if (connectState is ConnectState.ConnectSuccess) {
                        binding.vPlayer.repeatCount = 0
                        binding.vPlayer.setAnimation(deviceSetupViewModel.getAnimation(1))
                        binding.vPlayer.playAnimation()
                        deviceSetupViewModel.currentAnimation = 1
                    } else {
                        binding.vPlayer.repeatCount = 0
                        binding.vPlayer.setAnimation(deviceSetupViewModel.getAnimation(0))
                        binding.vPlayer.playAnimation()
                        deviceSetupViewModel.currentAnimation = 0
                    }
                } else if (deviceSetupViewModel.currentAnimation == 1) {
                    binding.vPlayer.repeatCount = 0
                    binding.vPlayer.setAnimation(deviceSetupViewModel.getAnimation(2))
                    binding.vPlayer.playAnimation()
                    deviceSetupViewModel.currentAnimation = 2
                    vibrationUtils.vibrate(LOW_VIBRATION)
                } else if (deviceSetupViewModel.currentAnimation == 2) {
                    viewModel.localDataStore.setDeviceSetupPendingStatus(false)
                    startMainActivity()
                }

            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })


        /*binding.bGetStarted.setOnClickListener {
            startActivity(MainActivity.getStartIntent(this).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this.putExtra(
                    MainActivity.OPEN_PROFILE,
                    intent.getBooleanExtra(MainActivity.OPEN_PROFILE, false)
                )
            })
        }*/

    }

    override fun observeSubscriber() {
        sessionManager.connectState.observe(this) { connectedState ->
            LOGS.d("CONNECT_STATE", "PairingSuccessFragment > $connectedState")
            when (connectedState) {
                is ConnectState.ConnectSuccess -> {
                    if (!setupStarted) {
                        setupStarted = true
                        initDefaultValue()
                    }

                }

                else -> {}
            }
        }

        sessionManager.connectStateRing.observe(this) { connectedState ->
            LOGS.d("CONNECT_STATE", "PairingSuccessFragment > $connectedState")
            when (connectedState) {
                is ConnectState.ConnectSuccess -> {
                    if (!setupStarted) {
                        setupStarted = true
                        initDefaultValue()
                    }

                }

                else -> {}
            }
        }

    }

    private fun initDefaultValue() {

        Handler(Looper.getMainLooper()).postDelayed({
            updateDeviceDateTime()
        }, 1000)
        Handler(Looper.getMainLooper()).postDelayed({
            sendDefaultQuickReply()
        }, 2000)
        Handler(Looper.getMainLooper()).postDelayed({
            sendDefaultSportsMode()
        }, 3000)
        Handler(Looper.getMainLooper()).postDelayed({
            setUserInfo()
        }, 4000)
        Handler(Looper.getMainLooper()).postDelayed({
            setUnit()
        }, 5000)
        Handler(Looper.getMainLooper()).postDelayed({
            sessionManager.sendUpdateQueryAction(UpdateDeviceAction.SetBrightnessLevel(3))
//            setWeather()
        }, 6000)
        /*Handler(Looper.getMainLooper()).postDelayed({
            localDataStore.setDeviceSetupPendingStatus(false)
            vibrationUtils.vibrate(LOW_VIBRATION)
             *//*binding.textView.text = "Device setup successful"
             binding.bGetStarted.isEnabled = true*//*

        }, 7000)*/

        sessionManager.logInsiderAppEvent(InsiderAppEvents.PairingEvents.wn_pair_device_setup_complete,
            HashMap<String, Any>().apply {
                this["status"] = "complete"
            })
    }

    private fun setUnit() {
        getUserGoal1().let {
            val unit = DeviceUnits(
                unitSystem = it.unitSystem
            )
            sessionManager.sendUpdateQueryAction(
                UpdateDeviceAction.SetDeviceUnits(unit)
            )
        }
    }

    private fun updateDeviceDateTime() {

        val deviceUnits = DateFormats.getDefaultUnitFormats(this)
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetDeviceDateTime(
                Calendar.getInstance(),
                deviceUnits
            )
        )
        deviceUnits.timeFormat?.let { viewModel.localDataStore.setTimeFormat(it) }
    }

    private fun sendDefaultQuickReply() {
        val customReply = CustomReplyData(
            customReplies = AppStaticData.getDefaultCustomReply()
        )
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.UpdateCustomReply(
                customReply
            )
        )
    }

    private fun sendDefaultSportsMode() {


        sessionManager.connectedDevice.value?.deviceType?.let {
            if (it.equals(DeviceType.COLORFIT_PRO_2_OXY.deviceType, true)) {
                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetSportModeInfo(
                        SportsModeList().apply {
                            this.sportsModes = AppConstants.sportsModeListOxy
                        }
                    ))
            } else if (it.equals(DeviceType.COLORFIT_PRO_2.deviceType, true)) {
                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetSportModeInfo(
                        SportsModeList().apply {
                            this.sportsModes = AppConstants.sportsModeListPro2
                        }
                    ))
            } else if (it.equals(DeviceType.COLORFIT_2.deviceType, true)) {
                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetSportModeInfo(
                        SportsModeList().apply {
                            this.sportsModes = AppConstants.sportsModeListPro2
                        }
                    ))
            } else {

                sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.SetSportModeInfo(
                        SportsModeList().apply {
                            this.sportsModes = AppConstants.sportsModeList
                        }
                    ))
            }
        }

    }


//    private fun setWeather() {
//        if (watchesSDK.getWatchType() == SDKWatchType.SDK_RYEEX) {
//            localDataStore.updateWeatherSettings(true)
//            sessionManager.sendUpdateQueryAction(
//                UpdateDeviceAction.SetWeatherSwitch(
//                    SwitchSetting(status = true)
//                )
//            )
//            AppUtil.startWeatherScheduler(this)
//        }
//    }

    private fun setUserInfo() {
        val userGoals = getUserGoal1()
        val userInfo = getUserInfo()
        sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetUserInfo(userInfo, userGoals, user?.firstName)
        )

//        val unit = DeviceUnits(
//            unitSystem = userGoals.unitSystem,
//            timeFormat = TimeFormats.HOURS_12.type
//        )
//        sessionManager.sendUpdateQueryAction(
//            UpdateDeviceAction.SetDeviceUnits(unit)
//        )
    }

    private fun saveDefaultUserValue() {
        user = viewModel.localDataStore.getUser()

        user?.apply {
            userGoals = getUserGoal1()
            userInfo = getUserInfo()
            viewModel.localDataStore.saveUserInfo(this)
        }


    }

    private fun getUserInfo(): UserInfo {
        val user = viewModel.localDataStore.getUser()
        var gender = user?.userInfo?.gender
        if (gender.isNullOrEmpty()) {
            gender = Gender.MALE.type
        }
        var weight = user?.userInfo?.weight
        if (weight == null || weight == 0) {
            weight = viewModel.getDefaultWeightInKg()
        }


        var height = user?.userInfo?.height
        if (height == null || height == 0) {
            height = viewModel.getDefaultHeightInCm()
        }

        var age = user?.userInfo?.age
        if (age == null || age == 0) {
            age = 18
        }

        var dob = user?.userInfo?.dob
        if (dob.isNullOrEmpty()) {
            dob = viewModel.getDob()
        }

        val stepLength = 66
        return UserInfo(weight, height, age, dob, gender, stepLength)

    }

    private fun getUserGoal1(): UserGoals {
        val user = viewModel.localDataStore.getUser()
        var stepGoal = user?.userGoals?.stepGoal

        if (stepGoal == null || stepGoal == 0) {
            stepGoal = viewModel.getStepsDefaultGoal()
        }

        var caloriesGoal = user?.userGoals?.caloriesGoal
        if (caloriesGoal == null || caloriesGoal == 0) {
            caloriesGoal = viewModel.getCaloriesGoal()
        }
        var distanceGoal = user?.userGoals?.distanceGoal

        if (distanceGoal == null || distanceGoal == 0) {
            distanceGoal = viewModel.getDistanceGoal()
        }

        var unitSystem = user?.userGoals?.unitSystem
        if (unitSystem.isNullOrEmpty()) {
            unitSystem = UnitSystem.METRIC.type
        }

        val sleepGoal = 8

        return UserGoals(stepGoal, caloriesGoal, distanceGoal, sleepGoal, unitSystem = unitSystem)
    }

    override fun getViewBinding() = ActivityDeviceSetupBinding.inflate(layoutInflater)

    override fun setLoadingView(): DefaultLoaderBinding? = null
}

enum class DeviceSetupStatus {
    PENDING, COMPLETED
}