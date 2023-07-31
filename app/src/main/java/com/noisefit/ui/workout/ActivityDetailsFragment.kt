package com.noisefit.ui.workout

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentActivityDetailsBinding
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.feeds.create.CREATE_POST_KEY
import com.noisefit.ui.feeds.create.PostContent
import com.noisefit.ui.workout.adapter.ActivityDetailAdapter
import com.noisefit.ui.workout.adapter.ActivityDetailsHeartZoneAdapter
import com.noisefit_commans.utils.GoogleMapsUtil
import com.noisefit.util.ImageUtil
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.util.graph.HeartChartUtils
import com.noisefit_commans.models.GPSDataResponse
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.ui.displayToast
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class ActivityDetailsFragment :
    BaseFragment<FragmentActivityDetailsBinding>(FragmentActivityDetailsBinding::inflate),
    OnMapsSdkInitializedCallback {

    private val viewModel: ActivityDetailsViewModel by viewModels()

    private val args: ActivityDetailsFragmentArgs by navArgs()

    private val adapter: ActivityDetailAdapter by lazy {
        ActivityDetailAdapter()
    }
    private val adapterHeartRate: ActivityDetailsHeartZoneAdapter by lazy {
        ActivityDetailsHeartZoneAdapter()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()

        if (viewModel.activities.value == null) viewModel.fetchDetailsDataFromServer(args.activity.id)
    }

    private fun setRecycler() {
        binding.rvActivityDetails.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvActivityDetails.adapter = adapter

        binding.recyclerViewHeartZone.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHeartZone.adapter = adapterHeartRate


    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initUi(activity: SportsModeResponse) {
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACTIVITY_DETAIL_PAGE_VISIT + activity.getFormattedActivityName())
        binding.textViewTitle.text = activity.getFormattedActivityName()
        binding.tvTitle.text = activity.getFormattedActivityName()
        binding.tvDayTime.text = DateFormats.formatActivityTime4(activity.time)

        val activityName = activity.type ?: activity.activityType

        if (viewModel.generateDetailsData(activity, viewModel.unit).isNotEmpty()) {
            adapter.setDataSet(viewModel.generateDetailsData(activity, viewModel.unit))
            binding.rvActivityDetails.visibility = View.VISIBLE
            binding.separator2.root.visibility = View.VISIBLE
        } else {
            binding.rvActivityDetails.visibility = View.GONE
            binding.separator2.root.visibility = View.GONE
        }


        val heartZoneList = viewModel.getHeartRateData(activity)
        if (heartZoneList.isNullOrEmpty()) {
            binding.tvHeartRateZoneTitle.gone()
            binding.recyclerViewHeartZone.gone()
        } else {
            binding.tvHeartRateZoneTitle.visible()
            binding.recyclerViewHeartZone.visible()
            adapterHeartRate.setDataSet(heartZoneList)
        }
        binding.tvTime.text =
            DateFormats.formatActivityTime(activity.time) + "-" + DateFormats.formatActivityTime5(
                DateFormats.getDateFromString(activity.time, activity.duration)
            )

        if (!activity.gpsData.isNullOrEmpty()) {
            setUpMaps(activity.gpsData!!)
        } else if (!activity.gpsCoordinate.isNullOrEmpty()) {
            setUpUpdatedMaps(activity.gpsCoordinate!!)
        } else {
            val mapFragment: SupportMapFragment =
                childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.view?.visibility = View.GONE
            binding.tvRouteTitle.visibility = View.GONE
            binding.separator3.root.visibility = View.GONE
        }


        binding.imageTransparent.setOnTouchListener { v: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Disallow ScrollView to intercept touch events.
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                    // Disable touch on transparent view
                    return@setOnTouchListener false
                }

                MotionEvent.ACTION_UP -> {
                    // Allow ScrollView to intercept touch events.
                    binding.scrollView.requestDisallowInterceptTouchEvent(false)
                    return@setOnTouchListener true
                }

                else -> return@setOnTouchListener true
            }
        }


//        setCalorieGraph(activity)
        setHrGraph(activity)
    }


    private fun setHrGraph(activity: SportsModeResponse) {


        if (activity.heartRateData == null || activity.heartRateData!!.isEmpty()) {
            return
        }

        binding.lytHrGraph.layoutParent.visibility = View.VISIBLE
        val values = viewModel.handleHrData(activity)
        activity.heartRateData?.let {
            if (it.isNotEmpty()) {
                LOGS.d("START_TIME ${activity.time}")
                binding.lytHrGraph.tvHrStartTime.text =
                    DateFormats.formatActivityTime3(activity.time)
                if (activity.duration != null && activity.time != null) {
                    val endTime =
                        DateFormats.addMinutes(activity.time!!, activity.duration!!.toInt())
                    LOGS.d("START_TIME_ $endTime")
                    endTime?.let { time ->
                        binding.lytHrGraph.tvHrEndTime.text = time
                    }

                }

                HeartChartUtils.setDayHrChart(
                    binding.lytHrGraph.lineChart, true
                )
                HeartChartUtils.setDayHrChartData(
                    values, binding.lytHrGraph.lineChart, true
                )
                binding.lytHrGraph.containerMpChart.visible()


                val max = it.maxOf { it2 -> it2 }
                val min = it.minOf { it2 -> it2 }
                binding.lytHrGraph.tvHeartAvgValue.text = "$min"
                binding.lytHrGraph.tvHeartMaxValue.text = "$max"


            }
        }
    }

    private fun setMap(map: GoogleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success: Boolean = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.google_night_mode
                )
            )
            if (!success) {
                LOGS.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            LOGS.e("Can't find style. Error: $e")
        }

        map.uiSettings.apply {
            isCompassEnabled = true
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabledDuringRotateOrZoom = true
            isScrollGesturesEnabled = true
        }
    }

    private fun setUpUpdatedMaps(gpsData: String) {
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment


        val type = object : TypeToken<ArrayList<DoubleArray>>() {}.type
        val gpsCoordinateList = Gson().fromJson<ArrayList<DoubleArray>>(gpsData, type)
        if (gpsCoordinateList.size <= 2) {
            mapFragment.view?.visibility = View.GONE
            binding.mapsPgBr.gone()
            binding.tvRouteTitle.gone()
            binding.separator3.root.gone()
            binding.imageTransparent.gone()
            return
        }
        mapFragment.view?.visibility = View.VISIBLE
        binding.imageTransparent.visible()
        binding.mapsPgBr.visible()
        binding.tvRouteTitle.visible()
        binding.separator3.root.visible()
        mapFragment.getMapAsync { googleMap ->
            setMap(googleMap)
            scope.launch {
                GoogleMapsUtil().plotLocationModalGoogleMaps(
                    requireActivity(), googleMap, gpsCoordinateList
                ).collect {
                    withContext(Dispatchers.Main) {

                        try {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    it, 0
                                )
                            )
                        } catch (ise: IllegalStateException) {
                            googleMap.setOnMapLoadedCallback(OnMapLoadedCallback {
                                googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(it, 0)
                                )
                            })
                        }
                        nullableBinding?.mapsPgBr?.gone()
                        nullableBinding?.map?.visible()
                    }
                }
            }
        }

    }

    private fun setUpMaps(gpsData: String) {
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        val type = object : TypeToken<List<GPSDataResponse>>() {}.type
        val gpsCoordinateList = Gson().fromJson<List<GPSDataResponse>>(gpsData, type)

        if (gpsCoordinateList.size <= 2) {
            mapFragment.view?.visibility = View.GONE
            binding.mapsPgBr.gone()
            binding.tvRouteTitle.gone()
            binding.separator3.root.gone()
            binding.imageTransparent.gone()
            return
        }

        mapFragment.view?.visibility = View.VISIBLE
        binding.imageTransparent.visible()
        binding.mapsPgBr.visible()
        binding.tvRouteTitle.visible()
        binding.separator3.root.visible()
        mapFragment.getMapAsync { googleMap ->
            setMap(googleMap)
            scope.launch {
                GoogleMapsUtil().plotLocationModalGoogleMaps(
                    requireActivity(), googleMap, gpsCoordinateList
                ).collect {
                    withContext(Dispatchers.Main) {
                        try {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    it, 0
                                )
                            )
                        } catch (ise: IllegalStateException) {
                            googleMap.setOnMapLoadedCallback(OnMapLoadedCallback {
                                googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(it, 0)
                                )
                            })
                        }
                        binding.mapsPgBr.gone()
                        binding.map.visible()
                    }
                }
            }


        }

    }


    override fun initListener() {

        binding.viewShare.setOnClickListener {
            if (viewModel.activities.value == null) return@setOnClickListener
            if (viewModel.isActivityDataZero()) {
                requireActivity().displayToast(R.string.text_you_cannot_share_without_any_activity_progress)
                return@setOnClickListener
            }


            viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.WORKOUT_SHARE_CLICK,
                HashMap<String, Any>().apply {
                    this["workout_type"] =
                        viewModel.activities.value?.getFormattedActivityName().toString()
                })

            setFragmentResultListener(CREATE_POST_KEY) { _, bundle ->
                val updated = bundle.getBoolean("updated")
                if (updated) {
                    //mainViewModel.navigateTo(BottomNavOption.COMMUNITY)
                }
            }

            navigate(
                ActivityDetailsFragmentDirections.actionActivityDetailsFragmentToCreatePostFragment()
                    .apply {
                        this.shareContent = PostContent.WORKOUTS
                        this.workout = viewModel.activities.value
                    })
        }

        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnFeedback.setOnClickListener {
            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.WORKOUT_FEEDBACK_CLICK,
                HashMap<String, Any>().apply
                {
                    this["workout_type"] =
                        viewModel.activities.value?.getFormattedActivityName().toString()
                })
            navigate(
                ActivityDetailsFragmentDirections.actionNavigationActivityToActivityFeedbackFragment()
            )
        }

    }


    override fun subscribeObservers() {
        viewModel.activities.observe(viewLifecycleOwner) {
            binding.scrollView.visible()
            initUi(it)
        }
        viewModel.getLoading().observe(viewLifecycleOwner) {
            uiController.displayProgressBar(it, "")
        }

        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
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