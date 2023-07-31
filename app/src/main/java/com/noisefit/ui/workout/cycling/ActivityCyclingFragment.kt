package com.noisefit.ui.workout.cycling

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
import com.github.mikephil.charting.data.CombinedData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.R
import com.noisefit.databinding.FragmentActivityCyclingBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit.ui.feeds.create.CREATE_POST_KEY
import com.noisefit.ui.feeds.create.PostContent
import com.noisefit.util.ImageUtil
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit.util.graph.OCombineChartUtils
import com.noisefit_commans.models.SportsModeResponse
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.GoogleMapsUtil
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ActivityCyclingFragment :
    BaseFragment<FragmentActivityCyclingBinding>(FragmentActivityCyclingBinding::inflate),
    OnMapsSdkInitializedCallback {
    private val viewModel: ActivityCyclingViewModel by viewModels()

    private val args: ActivityCyclingFragmentArgs by navArgs()

    private val adapter: ActivityCyclingAdapter by lazy {
        ActivityCyclingAdapter()
    }
    private val adapterHeartRate: ActivityCyclingHeartZoneAdapter by lazy {
        ActivityCyclingHeartZoneAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST, this)

    }

    private fun setRecycler() {
        binding.rvActivityDetails.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvActivityDetails.adapter = adapter

        binding.recyclerViewHeartZone.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHeartZone.adapter = adapterHeartRate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycler()

        if (viewModel.activities.value == null) viewModel.fetchDetailsDataFromServer(args.activity.id)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initUi(activity: SportsModeResponse) {

        binding.scrollView.visible()
        viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.ACTIVITY_DETAIL_PAGE_VISIT + activity.getFormattedActivityName())
        val distanceWithUnit = viewModel.getDistance(activity)
        binding.tvHeaderDistance.text = distanceWithUnit.first
        binding.tvHeaderUnit.text = distanceWithUnit.second
        binding.tvToolbarTitle.text = activity.getFormattedActivityName()
        binding.tvDayTime.text = DateFormats.formatActivityTime7(activity.time)


        val activityName = activity.type ?: activity.activityType
        binding.ivActivityType.setImageResource(ImageUtil().getImageFromActivity(activityName))

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
            binding.separator5.root.gone()
        } else {
            binding.separator5.root.visible()
            binding.tvHeartRateZoneTitle.visible()
            binding.recyclerViewHeartZone.visible()
            adapterHeartRate.setDataSet(heartZoneList)
        }
        binding.tvTime.text = DateFormats.formatActivityTime(activity.time)

        if (activity.temp != null &&
            activity.uvi != null &&
            activity.humidity != null
        ) {
            binding.tvTempValue.text = viewModel.getBodyTempWithUnit(activity.temp!!.toFloat())
            val humidity = "${activity.humidity}%"
            binding.tvHumidityValue.text = humidity
            val uvi = "${activity.uvi}"
            binding.tvUvIndexValue.text = uvi
        } else {
            binding.tvUvIndexValue.text = "_"
            binding.tvHumidityValue.text = "_"
            binding.tvTempValue.text = "_"
        }

        var placeName = ""
        if(!activity.start.isNullOrEmpty()){
            placeName += activity.start
        }
        if(!activity.end.isNullOrEmpty()){
            if(placeName != activity.end){
                if(placeName.isNotEmpty()){
                    placeName += " - "
                }
                placeName += activity.end
            }
        }


        if(placeName.isNotEmpty()){
         //   binding.pinImv.visible()
            binding.tvBetween.text = placeName
        }

        if (!activity.gpsCoordinate.isNullOrEmpty()) {
            setUpUpdatedMaps(activity.gpsCoordinate!!)
        } else {
//            val mapFragment: SupportMapFragment =
//                childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//            mapFragment.view?.visibility = View.GONE
//            binding.tvRouteTitle.visibility = View.GONE
//            binding.separator34.root.visibility = View.GONE
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
                LOGS.d("START_TIME_SD ${activity.heartRateData!!.size}")
                binding.lytHrGraph.tvHrStartTime.text =
                    DateFormats.formatActivityTime3(activity.time)
                if (activity.duration != null && activity.time != null) {
                    val endTime =
                        DateFormats.addMinutes(activity.time!!, activity.duration!!.toInt())

                    endTime?.let { time ->
                        binding.lytHrGraph.tvHrEndTime.text = time
                    }

                }


                val chart = binding.lytHrGraph.lineChart

                OCombineChartUtils.setChart(chart, activity.heartRateAvg?.toFloat() ?: 0f)

                val combinedData = CombinedData()


                combinedData.setData(
                    OCombineChartUtils.generateLineData(
                        values.first,
                        chart,
                        values.second
                    )
                )
                combinedData.setData(
                    OCombineChartUtils.generateCandleData(
                        values.third,
                        R.color.heart_candle_bg
                    )
                )
                chart.data = combinedData


                chart.invalidate()


                binding.separator4.root.visible()
                binding.lytHrGraph.root.visible()

                val max = it.maxOf { it2 -> it2 }
                val min = it.minOf { it2 -> it2 }
                binding.lytHrGraph.tvMinValue.text = "$min"
                binding.lytHrGraph.tvMaxValue.text = "$max"
                binding.lytHrGraph.tvAvgValue.text = activity.heartRateAvg.toString()


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
            map.setPadding(     16,      16,      20,     20);
            if (!success) {
                LOGS.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
            LOGS.e("Can't find style. Error: $e")
        }

        map.uiSettings.apply {
            isCompassEnabled = true
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
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
//            mapFragment.view?.visibility = View.GONE
//            binding.mapsPgBr.gone()
//            binding.tvRouteTitle.gone()
//            binding.separator3.root.gone()
//            binding.imageTransparent.gone()
            return
        }
        mapFragment.view?.visibility = View.VISIBLE
        binding.imageTransparent.visible()
        binding.mapsPgBr.visible()
        binding.tvRouteTitle.visible()
        binding.separator34.root.visible()
        mapFragment.getMapAsync { googleMap ->
            setMap(googleMap)
            scope.launch {
                GoogleMapsUtil().plotLocationCyclingGoogleMaps(
                    requireActivity(), googleMap, gpsCoordinateList
                ).collect {
                    withContext(Dispatchers.Main) {

                        try {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    it, 40
                                )
                            )
                        } catch (ise: IllegalStateException) {
                            googleMap.setOnMapLoadedCallback(GoogleMap.OnMapLoadedCallback {
                                googleMap.animateCamera(
                                    CameraUpdateFactory.newLatLngBounds(it, 40)
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


    override fun initListener() {

        binding.viewShare.setOnClickListener {
            if (viewModel.activities.value == null) return@setOnClickListener

            viewModel.sessionManager.logInsiderAppEvent(
                InsiderAppEvents.WORKOUT_SHARE_CLICK,
                HashMap<String, Any>().apply {
                    this["workout_type"] =
                        viewModel.activities.value?.getFormattedActivityName().toString()
                })


            viewModel.activities.value?.let {
                navigate(
                    ActivityCyclingFragmentDirections.actionActivityCyclingFragmentToActivityShareFragment(
                        it
                    )
                )
            }
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
                ActivityCyclingFragmentDirections.actionNavigationActivityToActivityFeedbackFragment()
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