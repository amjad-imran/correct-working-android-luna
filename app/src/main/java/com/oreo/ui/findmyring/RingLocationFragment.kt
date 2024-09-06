package com.oreo.ui.findmyring

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.noisefit.data.model.RingLocationData
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentRingLocationBinding
import com.noisefit_commans.interfaces.connection.ConnectState
import com.noisefit_commans.location.LocationService2
import com.noisefit_commans.location.LocationUtils2
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt


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

        if (viewModel.sessionManager.connectStateRing.value is ConnectState.ConnectSuccess) {
            LocationUtils2.startLocationService(false)
            viewModel.setLoading(true)
        } else {
            viewModel.getRingLastLocation()
        }
    }


    override fun initListener() {

        binding.lytLocationData.root.setOnClickListener {
            if (viewModel.isDataHidden) {
                bottomSheetToggle(false)
            } else {
                bottomSheetToggle(true)
            }
            viewModel.isDataHidden = viewModel.isDataHidden.not()
        }

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

    private fun bottomSheetToggle(hideData: Boolean) {
        if (viewModel.isDataHidden == hideData) return

        if (hideData) {
            binding.lytLocationData.apply {
                animateViewOut(this.root)
            }
        } else {
            binding.lytLocationData.apply {
                animateViewIn(this.root)
            }
        }

    }

    private fun animateViewOut(view: View) {
        val translateHeight = view.height.toFloat() - 98f.dpToPixel()
        ObjectAnimator.ofFloat(view, "translationY", 0f, translateHeight).apply {
            duration = 400
            start()
        }
    }

    private fun animateViewIn(view: View) {
        val translateHeight = view.height.toFloat() - 98f.dpToPixel()
        ObjectAnimator.ofFloat(view, "translationY", translateHeight, 0f)
            .apply {
                duration = 400
                start()
            }
    }

    private fun setBottomSheetData(ringLocationData: RingLocationData?) {
        if (ringLocationData == null) {
            binding.lytLocationData.root.gone()
            binding.lytDirections.gone()
            return
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

            tvAddress.text = ringLocationData?.address

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


            Handler(Looper.getMainLooper()).postDelayed({
                bottomSheetToggle(true)
                viewModel.isDataHidden = true
            }, 200)
        }
    }

    private fun setLocationData(ringLocationData: RingLocationData?) {

        if (ringLocationData?.latitude == null || ringLocationData.longitude == null) return

        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            googleMap.clear()
            loadMarker(googleMap, ringLocationData.latitude, ringLocationData.longitude)
        }
    }

    private fun loadMarker(googleMap: GoogleMap, latitude: Double, longitude: Double) {

        val currentLoc = LatLng(latitude, longitude)

        val ringImage = viewModel.getRingImage()

        val markerView: View = layoutInflater.inflate(
            R.layout.layout_custom_marker,
            null,
            false
        )
        val imageView = markerView.findViewById<ImageView>(R.id.ivRingImage)

        googleMap.addMarker(
            MarkerOptions()
                .position(currentLoc)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_ring_marker))
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15f))

        Glide.with(imageView.context)
            .asBitmap()
            .load(ringImage)
            .into(object : CustomTarget<Bitmap?>(
                56f.dpToPixel().roundToInt(),
                56f.dpToPixel().roundToInt()
            ) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    googleMap.clear()
                    imageView.setImageBitmap(resource)

                    val bitmap = viewModel.getBitmapFromLayout(requireActivity(), markerView)

                    bitmap?.let {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(currentLoc)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(bitmap)
                                )
                        )
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun setUpMaps() {
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            setMap(googleMap)
            this.googleMap = googleMap

            googleMap.setOnMapClickListener {
                bottomSheetToggle(true)
                viewModel.isDataHidden = true
            }

            googleMap.setOnCameraMoveStartedListener {
                bottomSheetToggle(true)
                viewModel.isDataHidden = true
            }
        }
    }

    private fun setMap(map: GoogleMap) {

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