package com.oreo.ui.recordworkout

import android.Manifest
import android.animation.Animator
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRecordWorkoutBinding
import com.noisefit.ui.common.bottomSheet.WORKOUT_STOP_KEY
import com.noisefit.ui.onboarding.pairing.find.FindDeviceListFragment
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.model.OWorkoutListModal
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.location.LocationService
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.AppLogs
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.ui.sleep.nap.BOTTOM_NAP_RESULT
import com.oreo.ui.workout.add.SELECT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordWorkoutFragment :
    BaseFragment<FragmentRecordWorkoutBinding>(FragmentRecordWorkoutBinding::inflate),
    OnCompleteListener<LocationSettingsResponse> {

    val navArgs: RecordWorkoutFragmentArgs by navArgs()
    val viewModel: RecordWorkoutViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navArgs.workout.let {
            viewModel.workout = it
            binding.tvWorkoutTitle.text = it.getFormattedActivityName()
            binding.ivWorkoutImage.loadImage(binding.ivWorkoutImage.context, it.iconUrl)
            viewModel.sportStartTime =
                viewModel.ringDataStore.getOngoingRecordWorkout()?.first ?: 0L
        }
        binding.btnEndWorkout.isEnabled = false

        navArgs.onGoingWorkout?.let {
            viewModel.workoutDuration = it.duration.toLong()
            if (it.sportStatus == 1 || it.sportStatus == 3) {
                startWorkoutUi()
            } else if (it.sportStatus == 2) {
                pauseWorkout()
                viewModel.updateTimer()
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)

    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCrossClicked()
            }
        }


    private fun startWorkoutUi() {
        viewModel.currentWorkoutState = 1
        binding.btnStartWorkout.gone()
        binding.btnPauseResume.apply {
            this.text = getString(R.string.pause)
            this.visible()
        }
        binding.btnEndWorkout.isEnabled = true
        viewModel.starTimer()

        viewModel.saveOngoingRecordWorkout()
    }

    private fun pauseWorkout() {
        viewModel.currentWorkoutState = 2
        binding.btnStartWorkout.gone()
        binding.btnPauseResume.visible()
        binding.btnPauseResume.text = getString(R.string.resume)
        viewModel.pauseTimer()
        binding.btnEndWorkout.isEnabled = true
    }

    private fun resumeWorkout() {
        viewModel.currentWorkoutState = 3
        binding.btnStartWorkout.gone()
        binding.btnPauseResume.visible()
        binding.btnPauseResume.text = getString(R.string.pause)
        viewModel.resumeTimer()
        binding.btnEndWorkout.isEnabled = true
    }

    private fun stopWorkout() {
        viewModel.deleteOngoingRecordWorkout()
        viewModel.currentWorkoutState = 4
        viewModel.stopTimer()

        if (viewModel.markedDeleted) {
            navigateUpSafe()
        } else {
            binding.progressBar.root.visible()
            viewModel.sessionManager.lastOngoingWorkoutTimestamp = viewModel.sportStartTime
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopTimer()
    }

    private fun onCrossClicked() {
        if (viewModel.currentWorkoutState == 0) {
            navigateUpSafe()
        } else {

            if (viewModel.sessionManager.connectStateRing.value !is ConnectState.ConnectSuccess) {
                return
            }

            if (viewModel.workoutDuration < 60) {

                setFragmentResultListener(
                    END_WORKOUT_KEY_SHORT
                ) { _, bundle ->
                    val end = bundle.getBoolean("end")

                    if (end) {
                        binding.progressBar.root.visible()
                        val sportId = viewModel.workout?.ringId ?: -1

                        viewModel.sessionManager.sendUpdateQueryAction(
                            UpdateDeviceAction.UpdateOngoingWorkout(
                                sportId,
                                viewModel.getCurrentTimeStamp(),
                                4
                            )
                        )
                        viewModel.markedDeleted = true
                        viewModel.markForDelete(viewModel.sportStartTime)
                    }
                }
                navigate(R.id.bottomSheetEndWorkoutShort)
                return
            }





            setFragmentResultListener(
                END_WORKOUT_KEY
            ) { _, bundle ->
                val allow = bundle.getBoolean("allow")
                val delete = bundle.getBoolean("delete")

                if (allow || delete) {
                    binding.progressBar.root.visible()
                    val sportId = viewModel.workout?.ringId ?: -1

                    viewModel.sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.UpdateOngoingWorkout(
                            sportId,
                            viewModel.getCurrentTimeStamp(),
                            4
                        )
                    )

                    if (delete) {
                        viewModel.markedDeleted = true
                        viewModel.markForDelete(viewModel.sportStartTime)
                    }
                }
            }
            navigate(R.id.bottomSheetEndWorkout)
        }
    }

    fun sendStartWorkoutCommand() {
        binding.progressBar.root.visible()
        viewModel.sportStartTime = viewModel.getCurrentTimeStamp()
        val sportId = viewModel.workout?.ringId ?: -1

        AppLogs.sendAppLogs("Record workout command sent -sportsId - $sportId  timestamp - ${viewModel.sportStartTime}")

        viewModel.sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.StartWorkout(
                sportId,
                viewModel.sportStartTime,
                viewModel.requireGps()
            )
        )
    }

    override fun initListener() {

        binding.ivWorkoutImage.setOnClickListener {
            viewModel.sessionManager.sendUpdateQueryAction(UpdateDeviceAction.CheckOngoingWorkout())
        }

        binding.btnStartWorkout.setOnClickListener {

            if (!viewModel.isDeviceConnected()) {
                return@setOnClickListener
            }
            if (viewModel.requireGps()) {
                if (!hasGpsPermission()) {
                    showPermDetailsDialog()
                    return@setOnClickListener
                } else {
                    if (!isGpsTurnedOn()) {
                        return@setOnClickListener
                    }
                    LOGS.d("LOCATION_PERM Has all required permisison")
                }
            }

            binding.btnStartWorkout.gone()

            //startWorkoutAnim()

            binding.lottieAnim.gone()
            binding.btnStartWorkout.visible()
            sendStartWorkoutCommand()

        }

        binding.btnPauseResume.setOnClickListener {
            binding.progressBar.root.visible()

            if (viewModel.currentWorkoutState == 1 || viewModel.currentWorkoutState == 3) {
                val sportId = viewModel.workout?.ringId ?: -1

                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateOngoingWorkout(
                        sportId,
                        viewModel.getCurrentTimeStamp(),
                        2
                    )
                )
            } else if (viewModel.currentWorkoutState == 2) {
                val sportId = viewModel.workout?.ringId ?: -1

                viewModel.sessionManager.sendUpdateQueryAction(
                    UpdateDeviceAction.UpdateOngoingWorkout(
                        sportId,
                        viewModel.getCurrentTimeStamp(),
                        3
                    )
                )
            }
        }

        binding.btnEndWorkout.setOnClickListener {

            if (!viewModel.isDeviceConnected()) {
                return@setOnClickListener
            }

            onCrossClicked()
        }

        binding.ivCross.setOnClickListener {
            onCrossClicked()
        }

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    setConnectingState()
                }

                is ConnectState.Connecting -> {
                    setConnectingState()
                }

                is ConnectState.ConnectSuccess -> {
                    setStateConnected()
                }

                is ConnectState.UnPaired -> {
                    navigateUpSafe()
                }

                else -> {}
            }
        }

    }

    private fun showPermDetailsDialog() {
        setFragmentResultListener(LOCATION_PERM_REQUEST) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                this@RecordWorkoutFragment.showLocationPermissionDialog()
            } else {
                binding.btnStartWorkout.gone()
                viewModel.workout?.isGpsRequired = 0

                binding.lottieAnim.gone()
                binding.btnStartWorkout.visible()
                sendStartWorkoutCommand()

                //this@RecordWorkoutFragment.startWorkoutAnim()
            }
        }
        navigate(R.id.bottomSheetLocationPermissionRequest)
    }

    fun isGpsTurnedOn(): Boolean {
        if (!ApplicationUtils.isLocationProviderEnabled(requireContext())) {
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 5000
            val builder: LocationSettingsRequest.Builder =
                LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val task: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(requireActivity())
                    .checkLocationSettings(builder.build())
            task.addOnCompleteListener(this)
            return false
        } else {
            return true
        }
    }

    private fun hasGpsPermission(): Boolean {
        val permissionAccessFineLocationApproved =
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED)

        val backgroundLocationPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            } else {
                true
            }

        return permissionAccessFineLocationApproved && backgroundLocationPermissionApproved
    }

    private fun showLocationPermissionDialog() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

    }

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        var openSettings = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) && permissions.getOrDefault(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    false
                ) -> {
                    LOGS.d("LOCATION_PERM LOCATION GRANTED")
                }

                else -> {
                    openSettings = true
                }
            }
        } else {
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) -> {
                    LOGS.d("LOCATION_PERM LOCATION GRANTED")
                }

                else -> {
                    openSettings = true
                }
            }
        }
        if (openSettings) {
            tryCatch {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }


    private fun setStateConnected() {
        binding.lytRingConnecting.root.gone()
        binding.imageConnecting.gone()
        binding.groupRingStatus.visible()
        binding.ivCross.visible()
        val batteryPercentage = viewModel.watchDataStore.getBatteryPercentRing()
        binding.batteryStatus.progress = batteryPercentage


        if (batteryPercentage <= 20) {
            binding.oreoStatus.loadImage(
                requireContext(),
                R.drawable.ic_ring_default_silver_new
            )
            binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.color_error))
        } else {
            binding.oreoStatus.setBackgroundResource(R.drawable.back_modal_new_round)
            binding.batteryStatus.setIndicatorColor(resources.getColor(R.color.white))
        }
    }

    private fun setConnectingState() {
        binding.lytRingConnecting.root.visible()
        binding.imageConnecting.visible()
        binding.groupRingStatus.gone()
        binding.ivCross.invisible()
    }

    override fun subscribeObservers() {

        LocationService.locationBroadCast.observe(this) {
            it.getContent()?.let {
                if (viewModel.shouldCheckWeather()) {
                    viewModel.getWeatherDetails(it.first, it.second)
                }
                if (viewModel.shouldCheckCity()) {
                    viewModel.getAddress(it.first, it.second)
                }
            }
        }

        viewModel.showWorkoutStoppedByRingDialog.observe(viewLifecycleOwner) {
            it.getContent()?.let { error ->

                setFragmentResultListener(
                    WORKOUT_STOP_KEY,
                ) { _, bundle ->

                    val allow = bundle.getBoolean("allow")
                    val delete = bundle.getBoolean("delete")

                    if (allow || delete) {

                        if (delete) {
                            viewModel.markedDeleted = true
                            viewModel.markForDelete(viewModel.sportStartTime)
                        }

                        stopWorkout()
                        viewModel.sessionManager.sendUserActivityAction(
                            UserActivityAction.SyncAutoSportsActivity()
                        )

                    }
                }
                viewModel.stopTimer()
                var showSave = true
                if (viewModel.workoutDuration < 60L) {
                    showSave = false
                }

                navigate(
                    R.id.workoutStopRingBottomSheet,
                    bundleOf(
                        "showSave" to showSave,
                        "message" to viewModel.getStoppedByRingMessage(error, showSave)
                    )
                )
            }
        }

        viewModel.sessionManager.showWorkoutDetails.observe(viewLifecycleOwner) {
            it.getContent()?.let { workoutId ->

                if (workoutId != null) {
                    if (viewModel.currentWorkoutState == 4) {
                        if (workoutId.equals("none")) {
                            navigateUpSafe()
                            return@observe
                        }
                        navigate(
                            RecordWorkoutFragmentDirections.actionRecordWorkoutFragmentToOWorkoutDetailsFragment(
                                workoutId,
                                -1
                            )
                        )
                    }
                }
            }
        }

        viewModel.displayTimer.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvTimer.text = "00:00"
            } else {
                binding.tvTimer.text = it
            }
        }


        viewModel.sessionManager.updateDeviceCallback.observe(this) {
            it.getContent()?.let {

                when (it) {
                    /*is UpdateDeviceDataCallback.WorkoutEndFromRingState -> {
                        AppLogs.sendAppLogs("Workout failed from ring Reason: ${it.errorMessage}")
                        stopWorkout()
                    }*/

                    is UpdateDeviceDataCallback.WorkoutStartState -> {
                        if (it.success) {
                            startWorkoutUi()
                        } else {
                            //context.showShortToast("Workout started : ${it.success}")
                        }
                        binding.progressBar.root.gone()
                    }

                    /*is UpdateDeviceDataCallback.OngoingWorkoutData -> {
                        setWorkoutData(it.data)
                    }*/

                    is UpdateDeviceDataCallback.WorkoutStopped -> {
                        binding.progressBar.root.gone()
                        if (it.success) {
                            stopWorkout()
                        } else {
                            //context.showShortToast("Workout Stopped : ${it.success}")
                        }
                    }

                    is UpdateDeviceDataCallback.WorkoutStoppedByRing -> {
                        binding.progressBar.root.gone()

                        viewModel.showWorkoutStoppedByRingDialog.postValue(Event(it.error))

                    }

                    is UpdateDeviceDataCallback.WorkoutPaused -> {
                        if (it.success) {
                            pauseWorkout()
                        } else {
                            //context.showShortToast("Workout Paused : ${it.success}")
                        }
                        binding.progressBar.root.gone()
                    }

                    is UpdateDeviceDataCallback.WorkoutResumed -> {
                        if (it.success) {
                            resumeWorkout()
                        } else {
                            //context.showShortToast("Workout Resumed : ${it.success}")
                        }
                        binding.progressBar.root.gone()
                    }


                    else -> {}
                }

            }

        }

    }

    override fun onComplete(task: Task<LocationSettingsResponse>) {
        try {
            task.getResult(ApiException::class.java)
            //startScan()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                    try {
                        exception.status.resolution?.intentSender?.let {
                            startIntentSenderForResult(
                                it,
                                REQUEST_CHECK_SETTINGS,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        }
                    } catch (exp: Exception) {
                        //CASE : For handling Fragment not attached to Activity
                    }

                } catch (sendEx: IntentSender.SendIntentException) {
                    LOGS.d("Failed to show dialog")
                } catch (classCast: ClassCastException) {
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                }
            }
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 42

    }

}