package com.oreo.ui.info.troubleshoot

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.freshchat.consumer.sdk.Freshchat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetTroubleshootBinding
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.share.ShareUtil.SUPPORT_URL
import com.oreo.ui.device.FIND_RING_LOCATION_PERM_REQUEST
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TroubleShootBottomSheetFragment :
    BaseBottomSheetWithTransparent<BottomSheetTroubleshootBinding>(
        BottomSheetTroubleshootBinding::inflate
    ), OnCompleteListener<LocationSettingsResponse> {

    private val args: TroubleShootBottomSheetFragmentArgs by navArgs()

    private val descriptionSliderAdapter by lazy {
        TroubleshootAdapter(object : TroubleShootAction {
            override fun onClicked(action: TroubleShootActionType) {
                handleActionClick(action)
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val showLocation = args.showLastLocation

        setViewpager(showLocation)

    }

    private fun handleActionClick(action: TroubleShootActionType) {
        when (action) {
            TroubleShootActionType.LAST_LOCATION -> {
                if (hasGpsPermission().not()) {
                    showPermDetailsDialog()
                } else {
                    if (!isGpsTurnedOn()) {
                        return
                    }
                    navigate(R.id.ringLocationFragment)
                }
            }

            TroubleShootActionType.BLUETOOTH -> {
                tryCatch {
                    val settingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    startActivity(settingsIntent)
                }
            }

            TroubleShootActionType.CONTACT_US -> {
                context?.let {
                    Freshchat.showConversations(requireContext())
                    //ShareUtil.openExternalUrl(it, SUPPORT_URL)
                }
            }
        }
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

    private fun showPermDetailsDialog() {
        setFragmentResultListener(FIND_RING_LOCATION_PERM_REQUEST) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            if (allow) {
                this@TroubleShootBottomSheetFragment.showLocationPermissionDialog()
            }
        }
        navigate(R.id.bottomSheetLocationPermissionFindMyRing)
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

    private val locationPermissionRequest = registerForActivityResult(
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

    private fun setViewpager(showLocation: Boolean) {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            adapter = descriptionSliderAdapter

        }
        TabLayoutMediator(
            binding.tabLayout,
            binding.vpImageSlider
        ) { _, _ -> }.attach()

        descriptionSliderAdapter.setDataSet(generateDataSet(), showLocation)

        binding.vpImageSlider?.post {
            binding?.vpImageSlider?.requestLayout()
            binding?.vpImageSlider?.requestTransform()
        }

        binding.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                LOGS.d("onPageScrolled $position")
                if (position == 0) {
                    binding.ivPrevious.invisible()
                    binding.ivNext.visible()
                } else if (position == (descriptionSliderAdapter.itemCount - 1)) {
                    binding.ivPrevious.visible()
                    binding.ivNext.invisible()
                } else {
                    binding.ivPrevious.visible()
                    binding.ivNext.visible()
                }

                binding.vpImageSlider?.post {
                    binding?.vpImageSlider?.requestLayout()
                    binding?.vpImageSlider?.requestTransform()
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

    }

    private fun generateDataSet(): java.util.ArrayList<TroubleShootData> {
        return arrayListOf(
            TroubleShootData(
                title = getString(R.string.text_steps_to_follow),
                message = getString(R.string.text_try_keeping_your_ring_on_the_charger),
                image = R.drawable.image_ts_1,
                ctaText = getString(R.string.text_check_last_synced_location),
                action = TroubleShootActionType.LAST_LOCATION
            ),
            TroubleShootData(
                title = getString(R.string.text_steps_to_follow),
                message = getString(R.string.text_ensure_your_ring_and_your),
                image = R.drawable.image_ts_2,
                ctaText = getString(R.string.text_check_last_synced_location),
                action = TroubleShootActionType.LAST_LOCATION
            ),
            TroubleShootData(
                title = getString(R.string.text_steps_to_follow),
                message = getString(R.string.text_try_turning_on_off_the_bluetooth_until_the_ring_gets_connected),
                image = R.drawable.image_ts_3,
                ctaText = getString(R.string.text_turn_on_bluetooth),
                action = TroubleShootActionType.BLUETOOTH
            ),
            TroubleShootData(
                title = getString(R.string.text_still_not_connecting),
                message = getString(R.string.text_reach_out_to_us_by_tapping),
                image = R.drawable.image_ts_4,
                ctaText = getString(R.string.text_contact_us),
                action = TroubleShootActionType.CONTACT_US
            )
        )
    }

    private fun getItem(i: Int): Int {
        return binding.vpImageSlider.currentItem + i
    }

    override fun initListener() {
        binding.ivNext.setOnClickListener {
            binding.vpImageSlider.setCurrentItem(getItem(+1), true)
        }
        binding.ivPrevious.setOnClickListener {
            binding.vpImageSlider.setCurrentItem(getItem(-1), true)
        }
    }

    override fun subscribeObservers() {
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
                isCancelable = true
            }
            dialog.window?.setDimAmount(0.9f)
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
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
                                44,
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


}