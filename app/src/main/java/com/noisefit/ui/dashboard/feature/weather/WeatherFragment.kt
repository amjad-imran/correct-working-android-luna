package com.noisefit.ui.dashboard.feature.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.AppStaticData
import com.noisefit.luna.databinding.FragmentWeatherBinding
import com.noisefit.ui.common.*
import com.noisefit.ui.common.bottomSheet.VALUE_REQUEST_KEY
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.models.SwitchSetting
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.*
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WeatherFragment : BaseFragment<FragmentWeatherBinding>(FragmentWeatherBinding::inflate),
    LocationListener, OnCompleteListener<LocationSettingsResponse> {


    private var locationManager: LocationManager? = null
    private var provider: String? = null
    private val viewModel: WeatherViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lytFeatureTile.apply {
            imvIcon.loadImage(requireContext(), R.drawable.ic_weather)
            tvTitle.text = getString(R.string.text_weather_settings)
            tvTitleDisc.visible()
            tvTitleDisc.text = getString(R.string.text_turn_on_toggle_for_current_location)
        }
        binding.lytToolbarWithDetails.tvTitle.text = getString(R.string.text_weather_settings)
        binding.lytToolbarWithDetails.tvDesc.text =
            getString(R.string.text_allow_your_watch_to_display_weather)

        initLocationManager()
    }

    private fun initLocationManager() {
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val criteria = Criteria()
        provider = locationManager?.getBestProvider(criteria, false)
    }

    private fun hideTempUnitSelection() {
        binding.apply {
            include4.root.gone()
            btnTempUnit.gone()
            tvTempUnitText.gone()
            viewBackUnit.gone()
        }
    }

    private fun showTempUnitSelection() {
        binding.apply {
            include4.root.visible()
            btnTempUnit.visible()
            tvTempUnitText.visible()
            viewBackUnit.visible()
        }
    }

    override fun subscribeObservers() {
        setFragmentResultListener(VALUE_REQUEST_KEY) { key, bundle ->
            val position = bundle.getInt("selectedPosition")
            val selectedValue = bundle.getString("selectedValue")
            LOGS.i("$position | $selectedValue")
            binding.btnTempUnit.text = selectedValue
            if (position == 0) {
                viewModel.setUnit(Units.METRIC)
            } else {
                viewModel.setUnit(Units.IMPERIAL)
            }

            viewModel.getSessionManager()
                .sendUpdateQueryAction(UpdateDeviceAction.SetTemperatureUnit(viewModel.temperatureUnit.value!!.name))
            viewModel.getWeatherData()
        }

        viewModel.location.observe(this) {
            it.getContent().let { location ->
                if (location != null) {
                    var locationString = "N/A"
                    if (!location.addressString.isNullOrEmpty()) {
                        locationString = location.addressString
                    }
                    binding.lytFeatureTile.tvTitleDisc.text = locationString
                    viewModel.updateLocation(location.address, locationString)
                    scope.launch {
                        context?.let { it1 ->
                            viewModel.getSessionManager().isWorkSchedulerScheduled = true
                            ApplicationUtils.startWeatherScheduler(it1)
                        }
                    }
                } else {
                    binding.lytFeatureTile.tvTitleDisc.text = "Unable to fetch your location"
                }
                viewModel.setLoading(false)

            }
        }



        viewModel.weatherData.observe(this) {
            viewModel.getSessionManager().sendUpdateQueryAction(
                UpdateDeviceAction.SetWeatherSwitch(
                    SwitchSetting(
                        status = true
                    )
                )
            )
            viewModel.getSessionManager().sendUpdateQueryAction(
                UpdateDeviceAction.SetWeatherData(
                    it.first,
                    viewModel.temperatureUnit.value!!.name
                )
            )
            viewModel.getSessionManager().sendUpdateQueryAction(
                UpdateDeviceAction.SetWeatherDataHourly(
                    it.first,
                    it.second,
                    viewModel.temperatureUnit.value!!.name
                )
            )

            scope.launch {
                context?.let { it1 ->
                    viewModel.getSessionManager().isWorkSchedulerScheduled = true
                    ApplicationUtils.startWeatherScheduler(it1)
                }
            }
        }

        viewModel.weatherEnabled.observe(this) {
            it.getContent()?.let { value ->
                binding.lytFeatureTile.llSwitch.isChecked = value

                if (value) {
                    handleWeatherToggle(value)
                    if (viewModel.showUnitSelection) {
                        showTempUnitSelection()
                    }
                } else {
                    hideTempUnitSelection()
                }
            }

        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }



        viewModel.temperatureUnit.observe(this) {
            it?.let { unit ->
                binding.btnTempUnit.text = viewModel.getTemperatureUnit()
            }
        }

    }

    @SuppressLint("MissingPermission")
    override fun initListener() {


        binding.progressBar.tvLoadingText.text = getString(R.string.text_fecthing_weather_updates)
        viewModel.setUnitSelectionVisibility()

        binding.btnTempUnit.setOnClickListener {
            navigate(
                WeatherFragmentDirections.actionWeatherFragmentToValueSelectorBottomSheet(
                    viewModel.getTemperatureUnit(),
                    AppStaticData.getTemperatureValues(),
                    getString(R.string.text_temperature_unit)
                )
            )
        }

        binding.lytToolbarWithDetails.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.lytFeatureTile.llSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) {
                return@setOnCheckedChangeListener
            }

            viewModel.setWeatherEnabled(isChecked)
            //handleWeatherToggle(isChecked)

            if (!isChecked) {
                handleWeatherToggle(isChecked)
            }
            if (isChecked)
                viewModel.getSessionManager().logInsiderAppEvent(
                    InsiderAppEvents.WEATHER_SETTING_CLICK,
                HashMap<String, Any>().apply
                 { this["is_enabled"]=true })
            else
                viewModel.getSessionManager().logInsiderAppEvent(
                    InsiderAppEvents.WEATHER_SETTING_CLICK,
                    HashMap<String, Any>().apply
                    { this["is_enabled"] = false })
        }
    }

    private fun handleWeatherToggle(status: Boolean) {

        if (status) {
            checkLocationPermission(permissionGranted = {
                fetchLocation()
            })
            if (viewModel.showUnitSelection) {
                showTempUnitSelection()
            }
        } else {
            viewModel.getSessionManager().sendUpdateQueryAction(
                UpdateDeviceAction.SetWeatherSwitch(
                    SwitchSetting(status = false)
                )
            )
            hideTempUnitSelection()

            binding.lytFeatureTile.tvTitleDisc.text =
                getString(R.string.text_turn_on_toggle_for_current_location)
            context?.let { ApplicationUtils.stopWeatherScheduler(it) }
            viewModel.getSessionManager().isWorkSchedulerScheduled = false
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {

        if (viewModel.lat != null && viewModel.long != null) {
            viewModel.getAddress()
            return
        }
        if (activity == null) return

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
            return
        }

        viewModel.setLoading(true)
        try {
            if (locationManager!!.allProviders.contains(LocationManager.GPS_PROVIDER)) {
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
            }
        } catch (exp: Exception) {
            //Handle getAllProviders exception
        }

        try {
            if (locationManager!!.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    this
                )
            }
        } catch (exp: Exception) {
            //Handle getAllProviders exception
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()

        locationManager?.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        viewModel.setLoading(false)
        viewModel.lat = location.latitude
        viewModel.long = location.longitude
        LOGS.d("${viewModel.lat} ${viewModel.long}")
        locationManager?.removeUpdates(this)
        viewModel.getAddress()
    }


    override fun onComplete(task: Task<LocationSettingsResponse>) {
        if (!isAdded) {
            return
        }
        try {
            task.getResult(ApiException::class.java)
            fetchLocation()
        } catch (exception: ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                    if (isAdded) {
                        startIntentSenderForResult(
                            exception.status.resolution?.intentSender,
                            REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null
                        )
                    }


                    //TODO migrate
//                    val resolvable = exception as ResolvableApiException
//                    resolvable.startResolutionForResult(
//                        requireActivity(),
//                        REQUEST_CHECK_SETTINGS
//                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    LOGS.d("Failed to show dialog")
                } catch (classCast: ClassCastException) {
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK || ApplicationUtils.isLocationProviderEnabled(requireContext())) {
                fetchLocation()
            } else {
                context.showShortToast("Location Permission required")
                navigateUpSafe()
            }
        }
    }


    private fun checkLocationPermission(
        permissionGranted: () -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.getSessionManager().logInsiderAppEvent(
                InsiderAppEvents.WEATHER_SETTING_LOCATION_PERMISSION_CLICK,
                HashMap<String, Any>().apply
                { this["is_enabled"] = true })
            permissionGranted.invoke()
        } else {
            viewModel.getSessionManager().logInsiderAppEvent(
                InsiderAppEvents.WEATHER_SETTING_CLICK,
                HashMap<String, Any>().apply
                { this["is_enabled"] = false })
            permissionResultListener.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private val permissionResultListener = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true && it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fetchLocation()
        } else {
            uiController.onApiErrorReceived(
                ErrorResponse(
                    UIComponentType.AreYouSureDialog(
                        getString(R.string.text_permission_required),
                        getString(R.string.text_permission_denial_location_weather),
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

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}


    companion object {
        private const val REQUEST_CHECK_SETTINGS = 42
    }

}