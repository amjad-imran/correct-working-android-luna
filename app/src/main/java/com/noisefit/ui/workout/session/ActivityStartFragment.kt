package com.noisefit.ui.workout.session

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentActivityStartBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.util.ImageUtil
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.models.LocationDataModel
import com.noisefit_commans.models.SportsModeRequest
import com.noisefit_commans.utils.DistanceUtil
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.LocationClientClass
import com.noisefit_nav_plus.handler.NavPlusQueryDeviceUnitsHandler
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ActivityStartFragment :
    BaseFragment<FragmentActivityStartBinding>(FragmentActivityStartBinding::inflate) {

    private  val  TAG = "ActivityStartFragment"
    @Inject
    lateinit var localDataStore: DataStoredInterface

    @Inject
    lateinit var sessionManager: SessionManager
    private var locationClientClass: LocationClientClass? = null
    private val viewModel: ActivityStartViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.ongoing)
                    showAlertDialog()
                else
                    navigateUpSafe()
            }
        })
    }

    private fun enableLocation() {
        if (!viewModel.enableGps(viewModel.mode?.name)) {
            return
        }

        LOGS.d(TAG,"ENABLE LOCATION")
        locationClientClass = LocationClientClass()
        NoisefitApplication.context?.let {
            locationClientClass?.initialize(it)
            locationClientClass?.requestLocationUpdates(it)
            LocalBroadcastManager
                .getInstance(it)
                .registerReceiver(
                    locationReceiver,
                    IntentFilter(NavPlusQueryDeviceUnitsHandler.LOCATION_BROADCAST_RECEIVER)
                )
        }
    }

    private fun disableLocation() {
        if (!viewModel.enableGps(viewModel.mode?.name)) {
            return
        }
        LOGS.d(TAG,"ENABLE LOCATION")
        NoisefitApplication.context?.let {
            locationClientClass?.removeLocationUpdates(it)
            LocalBroadcastManager
                .getInstance(it)
                .unregisterReceiver(locationReceiver)
        }
    }

    private var locationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val locationArrayList =
                intent.getParcelableArrayListExtra<LocationDataModel>(NavPlusQueryDeviceUnitsHandler.LAT_LONG)

            if (locationArrayList.isNullOrEmpty()) {
                return
            }

            if (viewModel.sportsModeRequest?.status == "pause" || viewModel.sportsModeRequest?.status == "stop") {
                return
            }
            val finalLocationDataModel = localDataStore.saveAndGetLocation(locationArrayList)
            viewModel.distanceCovered = DistanceUtil.getCalculatedDistance(finalLocationDataModel)

            viewModel.sportsModeRequest?.distance = viewModel.distanceCovered
            LOGS.d("$TAG ${viewModel.distanceCovered}")
            viewModel.sportsModeRequest?.let {
                sessionManager.sendUserActivityAction(
                    UserActivityAction.Refresh(
                        it
                    )
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.mode = ActivityStartFragmentArgs.fromBundle(it).mode
            binding.toolbar.tvTitle.text =
                viewModel.mode?.name?.replace("_", " ")?.uppercase() ?: ""
            binding.ivActivityType.setImageResource(ImageUtil().getImageFromActivity(viewModel.mode?.name))
        }
        binding.bStart.visible()
        binding.ongoingLayout.gone()
    }

    override fun initListener() {
        binding.bStart.setOnClickListener {


            binding.progressBar.root.visible()
            viewModel.sportsModeRequest = getInitialStateRequest()
            viewModel.sportsModeRequest?.let {
                sessionManager.sendUserActivityAction(
                    UserActivityAction.UpdateSportsMode(
                        it
                    )
                )
            }

            enableLocation()
        }
        binding.bStop.setOnClickListener {
            disableLocation()

            stopActivity()
        }
        binding.bPause.setOnClickListener {

            binding.progressBar.root.visible()
            viewModel.sportsModeRequest?.status = "pause"
            viewModel.sportsModeRequest?.let {
                sessionManager.sendUserActivityAction(
                    UserActivityAction.UpdateSportsMode(
                        it
                    )
                )
            }

        }
        binding.bResume.setOnClickListener {
            binding.progressBar.root.visible()
            viewModel.sportsModeRequest?.status = "resume"
            viewModel.sportsModeRequest?.let {
                sessionManager.sendUserActivityAction(
                    UserActivityAction.UpdateSportsMode(
                        it
                    )
                )
            }

        }

        binding.toolbar.backBtn.setOnClickListener {
            if (viewModel.ongoing)
                showAlertDialog()
            else
                navigateUpSafe()
        }
    }

    private fun stopActivity() {
        binding.progressBar.root.visible()
        viewModel.sportsModeRequest?.status = "stop"
        viewModel.sportsModeRequest?.let {
            sessionManager.sendUserActivityAction(UserActivityAction.UpdateSportsMode(it))
        }
    }


    override fun subscribeObservers() {

        sessionManager.userActivityCallback.observe(viewLifecycleOwner) { event ->
            event.getContent()?.let {
                if (it is UserActivityCallback.SportsModeStatusChange) {
                    binding.progressBar.root.gone()
                    handleButtonsVisibilityByStatus(it.syncDataStatus.status)
                    LOGS.d("SportsModeStatusChange2  " + it.syncDataStatus.status)
                } else if (it is UserActivityCallback.SportsModeDataObtained) {
                    LOGS.d("SportsModeStatusChange3  " + it.sportsModeRequestList.activities)
                    if (it.sportsModeRequestList.activities?.size!! > 0) {
                        viewModel.sportsModeResponse = it.sportsModeRequestList.activities!!.get(0)
                        viewModel.sportsModeResponse?.calories.let {
                            binding.tvCalorie.text = it.toString() + " kcal"
                            if (viewModel.sportsModeRequest != null) {
                                viewModel.sportsModeRequest?.calories = it?.toInt()!!
                            }
                        }
                        viewModel.sportsModeResponse?.heartRateCurrent.let {
                            binding.tvHr.text = it.toString() + " bpm"
                        }
                        //LOGS.d("sportsModeResponse.distance ${sportsModeResponse.distance}")
                    }
                }
            }

        }

        sessionManager.sportsModeRequest.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.sportsModeRequest?.let {

                }
                viewModel.sportsModeRequest?.duration = it.duration
                displayTime(it.duration)
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {

        }
    }

    private fun displayTime(duration: Int) {
        if (duration == 0) {
            binding.tvTimer.text = "00:00:00"
        } else {
            val hours: Int = duration / 3600
            val minutes: Int = (duration % 3600) / 60
            val seconds: Int = (duration % 3600) % 60
            var time = ""
            if (hours < 10) {
                time += "0$hours"
            } else {
                time += "$hours"
            }
            if (minutes < 10) {
                time += ":0$minutes"
            } else {
                time += ":$minutes"
            }
            if (seconds < 10) {
                time += ":0$seconds"
            } else {
                time += ":$seconds"
            }
            binding.tvTimer.text = time
        }
    }

    private fun handleButtonsVisibilityByStatus(status: String?) {
        viewModel.activityStatus = status
        when (status) {
            "start" -> {
                localDataStore.clearLocation()
                viewModel.ongoing = true
                binding.bStart.gone()
                binding.ongoingLayout.visible()
                binding.bPause.visible()
                binding.bStop.visible()
                binding.bResume.gone()
            }
            "pause" -> {
                binding.bStart.gone()
                binding.ongoingLayout.visible()
                binding.bPause.gone()
                binding.bStop.visible()
                binding.bResume.visible()
            }
            "resume" -> {
                binding.bStart.gone()
                binding.ongoingLayout.visible()
                binding.bPause.visible()
                binding.bStop.visible()
                binding.bResume.gone()
            }
            "stop" -> {
                viewModel.ongoing = false
                resetValues()
                localDataStore.clearLocation()
            }
        }
    }

    private fun getInitialStateRequest(): SportsModeRequest {
        val now = Calendar.getInstance()
        val sportsModeRequest = SportsModeRequest()
        sportsModeRequest.activityType = viewModel.mode?.name
        sportsModeRequest.status = "start"
        sportsModeRequest.day = now.get(Calendar.DAY_OF_MONTH)
        sportsModeRequest.hour = now.get(Calendar.HOUR_OF_DAY)
        sportsModeRequest.minute = now.get(Calendar.MINUTE)
        sportsModeRequest.month = now.get(Calendar.MONTH) + 1
        sportsModeRequest.year = now.get(Calendar.YEAR)
        sessionManager.setSportsModeRequest(sportsModeRequest)
        return sportsModeRequest
    }

    private fun resetValues() {
        binding.bStart.visible()
        binding.ongoingLayout.gone()
        binding.bPause.gone()
        binding.bStop.gone()
        binding.bResume.gone()
        binding.tvCalorie.text = "0kcal"
        binding.tvHr.text = "0bpm"
        binding.tvTimer.text = "00:00:00"
        viewModel.sportsModeRequest = getInitialStateRequest()
    }

    private fun showAlertDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Warning")
            .setCancelable(false)
            .setMessage("You want to stop your current activity data?")
            .setNegativeButton(resources.getString(R.string.text_no)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.text_yes)) { dialog, which ->
                dialog.dismiss()
                stopActivity()
            }
            .show()

    }

}