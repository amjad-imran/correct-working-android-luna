package com.oreo.ui.findmyring

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.noisefit.data.model.RingLocationData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingLocationBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.location.LocationService2
import com.noisefit_commans.location.LocationUtils2
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RingLocationFragment :
    BaseFragment<FragmentRingLocationBinding>(FragmentRingLocationBinding::inflate),
    OnMapsSdkInitializedCallback {

    private var googleMap: GoogleMap? = null
    private val viewModel: RingLocationViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST, this)

        setUpMaps()

        //Check for permissions
        if (viewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            viewModel.setLoading(true)
            LocationUtils2.startLocationService(false)
        } else {
            viewModel.getRingLastLocation()
        }

    }

    override fun initListener() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.lytMapControls.ivCurrentLocation.setOnClickListener {

        }
        binding.lytMapControls.ivMapType.setOnClickListener {

        }

        binding.lytDirections.setOnClickListener {
            val lat = viewModel.ringLocationData.value?.latitude
            val long = viewModel.ringLocationData.value?.longitude

            if (lat != null && long != null) {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=$lat,$long")
                    )
                    startActivity(intent)
                } catch (exp: Exception) {
                    //activity not found
                }
            }
        }
    }

    override fun subscribeObservers() {
        LocationService2.locationBroadCastFindMyRing2.observe(this) {
            it.getContent()?.let {
                viewModel.updateRingLocation(it)
                viewModel.setLoading(false)
            }
        }

        viewModel.ringLocationData.observe(this) {
            setLocationData(it)
            setBottomSheetData(it)
        }

        viewModel.sessionManager.connectStateRing.observe(this) { connectedState ->
            when (connectedState) {
                is ConnectState.ConnectFailed -> {
                    binding.lytLocationData.tvConnectionState.text = "Not connected"
                }

                is ConnectState.Connecting -> {
                    binding.lytLocationData.tvConnectionState.text = "Not connected"
                }

                is ConnectState.ConnectSuccess -> {
                    binding.lytLocationData.tvConnectionState.text = "Connected"
                }

                is ConnectState.UnPaired -> {
                    this@RingLocationFragment.navigateUpSafe()
                }

                else -> {}
            }
        }

        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun setBottomSheetData(ringLocationData: RingLocationData?) {
        if (ringLocationData == null) {
            binding.lytLocationData.root.gone()
            binding.lytDirections.gone()
        } else {
            binding.lytLocationData.root.visible()
            binding.lytDirections.visible()
        }

        binding.lytLocationData.apply {
            tvName.text = "${viewModel.getUserName()}Luna Ring"

            ivRing.loadImage(
                ivRing.context, viewModel.getRingImage()
            )

            tvBatteryPercentage.text =
                if (ringLocationData?.battery_percentage == null) "-" else "${ringLocationData.battery_percentage}%"

            viewModel.getAddress(ringLocationData?.latitude, ringLocationData?.longitude) {
                tvAddress.text = it
            }

            val lastSync = ringLocationData?.last_sync
            if (lastSync.isNullOrEmpty().not()) {
                val startTimeStamp = DateFormats.convertDateTimeToTimeStamp(
                    lastSync!!,
                    DateFormats.dateTimeFormat5()
                )

                tvLastSyncedAt.text = "Last Synced ${DateFormats.getRelativeTime(startTimeStamp)}"
            } else {
                tvLastSyncedAt.text = ""
            }
        }
    }

    private fun setLocationData(ringLocationData: RingLocationData?) {

        if (ringLocationData?.latitude == null || ringLocationData.longitude == null) return

        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            googleMap.clear()
            val currentLoc = LatLng(ringLocationData.latitude, ringLocationData.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(currentLoc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_ring_marker))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15f))
        }
    }

    private fun setUpMaps() {
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            setMap(googleMap)
            this.googleMap = googleMap
        }
    }

    private fun setMap(map: GoogleMap) {
        try {
            val success: Boolean = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.google_maps_workout
                )
            )
            if (!success) {
                LOGS.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            LOGS.e("Can't find style. Error: $e")
        }
        map.isBuildingsEnabled = false

        map.uiSettings.apply {
            isCompassEnabled = false
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
            isZoomGesturesEnabled = true
            isScrollGesturesEnabledDuringRotateOrZoom = false
            isScrollGesturesEnabled = true
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> LOGS.d(
                "MapsDemo", "The latest version of the renderer is used."
            )

            MapsInitializer.Renderer.LEGACY -> LOGS.d(
                "MapsDemo", "The legacy version of the renderer is used."
            )
        }
    }

}