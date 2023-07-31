package com.noisefit.ui.dashboard.feature.healthMonitor

import android.os.Bundle
import android.view.View
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentHealthMonitorBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.models.DeviceType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HealthMonitorFragment :
    BaseFragment<FragmentHealthMonitorBinding>(FragmentHealthMonitorBinding::inflate) {

    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.features = localDataStore.getDeviceFeatures()?.apply {

            val user = localDataStore.getUser()
            if (user != null) {
                user.userInfo?.gender?.let {
                    if (!it.equals("female", true)) {
                        this.menstrualData = 0
                    }
                }
            }
        }
        binding.lifecycleOwner = this
    }

    override fun initListener() {
        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_health_monitor)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.llWalkReminder.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.WALK_REMINDER_PAGE_VISIT)
            navigate(R.id.walkReminderFragment)
        }

        binding.llIdleAlert.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.IDLE_ALERT_PAGE_VISIT)
            navigate(R.id.idleReminderFragment)
        }
        binding.llStress.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.STRESS_PAGE_VIEWED)
            navigate(R.id.stressSettingsFragment)
        }
        binding.llMeal.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MEAL_REMINDER_PAGE_VISIT)
            navigate(R.id.mealReminderFragment)
        }
        binding.llMedicine.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.MEDICINE_REMINDER_PAGE_VISIT)
            navigate(R.id.medicineReminderFragment)
        }
        binding.llSpo2.setOnClickListener {
            //  logInsiderEvent(InsiderAppEvents.MEDICINE_REMINDER_PAGE_VISIT)
            navigate(R.id.autoSpo2Fragment)
        }
        binding.llDrink.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.DRINK_WATER_PAGE_VISIT)
            navigate(R.id.drinkReminderFragment)
        }
        binding.llCycleTracker.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.FEMALE_TRACKER_PAGE_VISIT)
            navigate(R.id.femaleHealthFragment)
        }
        binding.llSpo2Measurement.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.SPO2_PAGE_VISIT)
            navigate(R.id.spo2MeasurementFragment)
        }

        binding.llRapidEye.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.RAPID_EYE_PAGE_VISIT)
            navigate(R.id.rapidEyeMomentFragment)
        }
        binding.llHandWash.setOnClickListener {
            logInsiderEvent(InsiderAppEvents.HAND_WASH_PAGE_VISIT)
            navigate(R.id.handWashReminderFragment)
        }
        binding.llSleepReminder.setOnClickListener {

            navigate(R.id.sleepReminderFragment)
        }
        binding.llHeartRate.setOnClickListener {

            logInsiderEvent(InsiderAppEvents.HEART_RATE_PAGE_VISIT)
            localDataStore.getConnectedDevice()?.let {
                if (it.deviceType.equals(DeviceType.NOISEFIT_ACTIVE.deviceType, true)
                    || it.deviceType.equals(DeviceType.NOISEFIT_AGILE.deviceType, true)
                ) {
                    navigate(R.id.realHeartRateSettingFragment)
                } else if (it.deviceType.equals(DeviceType.COLORFIT_NAV.deviceType, true)
                    || it.deviceType.equals(DeviceType.COLORFIT_VISION.deviceType, true)
                    || it.deviceType.equals(DeviceType.NOISEFIT_HYBRID.deviceType, true)
                    || it.deviceType.equals(DeviceType.NOISE_EVOLVE_2.deviceType, true)
                    || it.deviceType.equals(DeviceType.COLORFIT_PULSE_2.deviceType, true)
                    || it.deviceType.equals(DeviceType.COLORFIT_PULSE_2_BUZZ.deviceType, true)
                    || it.deviceType.equals(DeviceType.NOISE_EVOLVE_2_PLAY.deviceType, true)
                ) {
                    navigate(R.id.heartRateSettingsFragment)
                } else if (it.deviceType.equals(DeviceType.COLORFIT_PRO_3.deviceType, true)
                    || it.deviceType.equals(DeviceType.COLORFIT_PRO_2.deviceType, true)
                    || it.deviceType.equals(DeviceType.NOISEFIT_ENDURE.deviceType, true)
                    || it.deviceType.equals(DeviceType.COLORFIT_PRO_2_OXY.deviceType, true)
                ) {
                    navigate(R.id.onlyHeartRateFragment)
                } else if (it.deviceType.equals(
                        DeviceType.FORCE.deviceType,
                        true
                    ) || it.deviceType.equals(DeviceType.ICON_3.deviceType, true)
                ) {
                    navigate(R.id.heartRateWithIntervalFragment)
                } else {
                    navigate(R.id.alertHeartRateFragment)
                }
            }

        }
    }
    fun logInsiderEvent(eventName:String){
        sessionManager.logInsiderAppEvent(eventName)

    }

    override fun subscribeObservers() {

    }
}