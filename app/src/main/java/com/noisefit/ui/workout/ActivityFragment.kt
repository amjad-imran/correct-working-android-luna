package com.noisefit.ui.workout

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentActivityBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit.ui.workout.adapter.RecentActivityActions
import com.noisefit.ui.workout.adapter.RecentWorkoutListingAdapter
import com.noisefit.util.ApplicationUtils
import com.noisefit.watch.SDKWatchType
import com.noisefit_commans.interfaces.data.UserActivityAction
import com.noisefit_commans.interfaces.data.UserActivityCallback
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActivityFragment : BaseFragment<FragmentActivityBinding>(FragmentActivityBinding::inflate),
    RecentActivityActions {



    private val viewModel: ActivityViewModel by viewModels()

    private val adapter: RecentWorkoutListingAdapter by lazy {
        RecentWorkoutListingAdapter(this,  viewModel.dataUnitConverter)
    }

    private fun askPermission() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionResultListener.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            return
        }


        val permissionAccessCoarseLocationApproved =
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        if (permissionAccessCoarseLocationApproved) {
            val backgroundLocationPermissionApproved = (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            if (backgroundLocationPermissionApproved) {
                // App can access location both in the foreground and in the background.
                // Start your service that doesn't have a foreground service type
                // defined.


                handlePermission()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    showDialog(false)
                } else {
                    requestPermissions(
                        Array(1) { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                        409
                    )
                }
            }


//            }
        } else {

            // App doesn't have access to the device's location at all. Make full request
            // for permission.LO
            permissionResultListener.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }
    }

    private fun showDialog(redirectToSettings: Boolean) {
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.AreYouSureDialog(
                    getString(
                        R.string.text_noisefit_needs_location_access
                    ),
                    getString(
                        R.string.text_to_track_your_real_time_run_noisefit_needs_location
                    ),
                    false,
                    getString(R.string.text_yes),
                    object : BinaryActionCallback {
                        override fun yes() {
                            if (redirectToSettings) {
                                ShareUtil.openAppPermissionSettings(context)
                            } else {
                                requestPermissions(
                                    Array(1) { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                                    409
                                )
                            }
                        }


                        override fun no() {

                        }

                    }

                )
            )
        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (requestCode == 409 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                showDialog(true)

            }
        }

    }

    private fun handlePermission() {
        viewModel.watchDataStore.setAskForPermission(false)
        if (viewModel.askPermissionForPro) {
            navigate(R.id.startWorkoutListingFragment)
        }

    }

    private val permissionResultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true && it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                handlePermission()
            } else {
                uiController.onApiErrorReceived(
                    ErrorResponse(
                        UIComponentType.AreYouSureDialog(
                            getString(R.string.text_permission_required),
                            getString(R.string.text_to_track_your_real_time_run_noisefit_needs_location),
                            false,
                            getString(R.string.text_allow),
                            object : BinaryActionCallback {
                                override fun yes() {
                                    activity?.let { act ->
                                        ApplicationUtils.openAppSettings(act)
                                    }
                                }


                                override fun no() {

                                }

                            }

                        )
                    )
                )

            }
        } else {
            if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true && it[Manifest.permission.ACCESS_FINE_LOCATION] == true && it[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true) {
                handlePermission()
            } else {

                uiController.onApiErrorReceived(
                    ErrorResponse(
                        UIComponentType.AreYouSureDialog(
                            getString(R.string.text_permission_required),
                            getString(R.string.text_to_track_your_real_time_run_noisefit_needs_location),
                            false,
                            getString(R.string.text_allow),
                            object : BinaryActionCallback {
                                override fun yes() {
                                    activity?.let { act ->
                                        ApplicationUtils.openAppSettings(act)
                                    }
                                }

                                override fun no() {
                                }
                            }
                        )
                    )
                )


            }
        }


    }

    private fun requestPermission(permissionName: String, permissionRequestCode: Int) {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(permissionName),
                permissionRequestCode
            )
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()
        binding.layoutWorkoutPermission.divider.gone()
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WORKOUT_LIST_PAGE_VISIT)
        if (viewModel.activities.value == null) {
            viewModel.getActivities()
        }

        LOGS.d("askPermission ${ viewModel.watchDataStore.getAskForPermission()}")
        if ( viewModel.watchDataStore.getAskForPermission()) {
            askPermission()
            LOGS.d("askPermission")
        }

        /*if (viewModel.isUserLogined) {*/
        binding.header.textViewTitle.text = getString(R.string.text_activity)

        /*} else {
            binding.tvToolbarText.text = getString(R.string.text_activity)
        }*/

    }

    private fun setRecycler() {
        binding.rvActivity.layoutManager = LinearLayoutManager(context)
        binding.rvActivity.adapter = adapter
        adapter.connectedDevice =  viewModel.sessionManager.connectedDevice.value?.deviceType ?: ""
        adapter.unitsSystem = viewModel.getUnitSystem()
    }

    private fun syncFullAppData() {
        scope.launch {
            viewModel.sessionManager.forceSyncDataWithServer = true
            val status = ApplicationUtils.startSyncScheduler(requireContext())
        }
    }

    override fun initListener() {

        binding.tvStartSession.setOnClickListener {
            viewModel.askPermissionForPro = true
            askPermission()
        }

        binding.header.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.srActivity.setOnRefreshListener {

            val watchSdk =  viewModel.watchesSDK.getWatchType()
            if (watchSdk != null) {
                if (watchSdk == SDKWatchType.SDK_CF_PRO) {
                    syncFullAppData()
                } else {
                    viewModel.sessionManager.sendUserActivityAction(UserActivityAction.SyncSportsActivity(""))
                }

            }
            Handler(Looper.getMainLooper()).postDelayed(dismissRunnable, 10000)

        }
        binding.rvActivity.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.isActivitiesLoading || viewModel.isLastPage) {
                        return
                    }
                    nullableBinding?.let {
                        val visibleItemCount = it.rvActivity.layoutManager?.childCount ?: 0
                        val totalItemCount = it.rvActivity.layoutManager?.itemCount ?: 0
                        val firstVisibleItemPosition =
                            (it.rvActivity.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            viewModel.fetchActivityFromServer()
                        }
                    }
                }
            }

        })

        binding.layoutWorkoutPermission.tvAlertMessage.setOnClickListener {
//            showLocationPermissionDialog()
            uiController.onApiErrorReceived(ErrorResponse(
                UIComponentType.AreYouSureDialog(
                    "Noisefit requires background location permission for",
                    getString(R.string.text_background_permission_message),
                    false,
                    getString(R.string.text_continue),
                    object : BinaryActionCallback {
                        override fun yes() {
                            askPermission()
                        }

                        override fun no() {

                        }
                    }

                )))
        }


    }


    override fun onResume() {
        super.onResume()
        if (checkBackgroundLocationPermission()) {
            binding.layoutWorkoutPermission.root.gone()
            binding.layoutBackPermission.gone()
        } else {
            binding.layoutWorkoutPermission.apply {
                tvAlertMessage.text =
                    getString(R.string.text_background_location_permission_required)
                this.ivCloseAlert.invisible()
                this.ivAlertImage.visible()
                root.visible()
            }
            binding.layoutBackPermission.visible()
        }
    }

    private fun checkBackgroundLocationPermission(): Boolean {
        val permissionAccessCoarseLocationApproved =
            (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
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

        if (permissionAccessCoarseLocationApproved && backgroundLocationPermissionApproved) {
            return true
        }

        return false

    }


    private val dismissRunnable = Runnable {
        try {
            if (binding.srActivity.isRefreshing) {
                binding.srActivity.isRefreshing = false
            }
        } catch (exp: Exception) {
        }
    }

    override fun subscribeObservers() {
        viewModel.activities.observe(this) {
            adapter.setDataSet(it)
            if (it.isEmpty()) {
                if (viewModel.isUserLogined()) {
                    binding.tvNoActivities.text = getText(R.string.text_no_activities)
                } else {
                    binding.tvNoActivities.text = getText(R.string.text_no_activities_login)
                }
                binding.tvNoActivities.visible()
                binding.header.textViewTitle.visible()
                binding.header.textViewTitle.text = getString(R.string.text_activity)
            } else {
                binding.tvNoActivities.gone()
                /* binding.tvToolbarText.text = DateFormats.formatDateTime(
                     viewModel.activitiesDates.first(),
                     DateFormats.dateFormat2,
                     DateFormats.dateFormat4
                 )*/
                binding.header.textViewTitle.visible()
            }
        }

        viewModel.sessionStartAvailable.observe(this) {
            if (it) {
                binding.tvStartSession.visible()
            } else {
                binding.tvStartSession.gone()
            }
        }

        viewModel.sessionManager.userActivityCallback.observe(this) { event ->
            event.peekContent()?.let {
                /*if (it is UserActivityCallback.SportsModeDataObtainedGPS) {
                    LOGS.d("ActivityFragment", "Size : ${it.sportsModeResponse.activities}")
                    LOGS.d("ActivityFragment", "Size : ${it.sportsModeResponse.activities?.size}")
                    if (!it.sportsModeResponse.activities.isNullOrEmpty()) {
                        viewModel.addActivity(it.sportsModeResponse.activities)

                        it.sportsModeResponse.activities?.forEach { act ->
                            uiController.logAppEvent(AppEvents.ACTIVITY_SYNC,
                                java.util.HashMap<String, Any?>().apply {
                                    this["Activity Name"] = act.activityType
                                }
                            )
                        }
                    }
                }else if(it is UserActivityCallback.SportsModeDataObtained){
                    LOGS.d("ActivityFragment SportsModeDataObtained", "Size : ${it.sportsModeRequestList.activities}")
                    LOGS.d("ActivityFragment SportsModeDataObtained", "Size : ${it.sportsModeRequestList.activities?.size}")
                    if (!it.sportsModeRequestList.activities.isNullOrEmpty()) {
                        viewModel.addActivity(it.sportsModeRequestList.activities)

                        it.sportsModeRequestList.activities?.forEach { act ->
                            uiController.logAppEvent(AppEvents.ACTIVITY_SYNC,
                                java.util.HashMap<String, Any?>().apply {
                                    this["Activity Name"] = act.activityType
                                }
                            )
                        }

                    }
                } else */if (it is UserActivityCallback.SportsModeDataSyncSuccess) {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.getActivities()
                }, 2000)
            }
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                if (!binding.srActivity.isRefreshing) {
                    binding.srActivity.isRefreshing = true
                }
            } else {
                binding.srActivity.isRefreshing = false
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    override fun onActivitySelected(activity: SportsModeResponse) {

        if (activity.activityType?.lowercase() == "outdoor_cycling__") {
            navigate(
                ActivityFragmentDirections.actionNavigationActivityToActivityCyclingFragment(
                    activity
                )
            )
        } else {
            navigate(
                ActivityFragmentDirections.actionNavigationActivityToActivityDetailsFragment(
                    activity
                )
            )
        }


    }
}