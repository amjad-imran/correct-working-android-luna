package com.oreo.ui.workout.details

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOWorkoutDetailsV2Binding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.models.LocationDataNetwork
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.paintText
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.GoogleMapsUtil
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.VibrationUtils
import com.oreo.data.model.ChartModel
import com.oreo.data.model.GraphDummyModel
import com.oreo.data.model.OWDActivityHRZoneData
import com.oreo.data.model.OWorkoutDetailsResponseModel
import com.oreo.data.model.SleepChartModel
import com.oreo.data.model.WorkoutTypes
import com.oreo.data.model.health.Nudges
import com.oreo.ui.activity.all.DELETE_WORKOUT_REQUEST_KEY
import com.oreo.ui.custom.OnHeartRateChartClickAction
import com.oreo.ui.readiness.OreoReadinessBannerFragment
import com.oreo.ui.sleep.banner.OreoSleepBannerAdapter
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val maxLatLngPadding = 180

@AndroidEntryPoint
class OWorkoutDetailsFragmentV2 :
    BaseFragment<FragmentOWorkoutDetailsV2Binding>(FragmentOWorkoutDetailsV2Binding::inflate),
    OnMapsSdkInitializedCallback {

    private val viewModel: OWorkoutDetailsViewModelV2 by viewModels()
    private val args: OWorkoutDetailsFragmentV2Args by navArgs()
    private val mainViewModel: OreoMainViewModel by activityViewModels()

    @Inject
    lateinit var vibrationUtils: VibrationUtils

    private var googleMap: GoogleMap? = null


    private val workoutDetailsAdapter: OWorkoutDetailslAdapterV2 by lazy {
        OWorkoutDetailslAdapterV2()
    }
    private val hrZoneAdapter: OWorkoutHRZoneAdapter by lazy {
        OWorkoutHRZoneAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultUiValue()
        setRecycler()
        viewModel.getWorkoutDetails(args.workoutId)
        viewModel.position = args.position

    }

    private fun setRecycler() {
        with(binding.lytTop.rvActivityDetails) {
            adapter = workoutDetailsAdapter
        }
        with(binding.lytHeartRate.ryvHrZone) {
            adapter = hrZoneAdapter
        }
    }


    override fun initListener() {

        binding.lytTop.lytToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }

        binding.tvEdit.setOnClickListener {
            val id = viewModel.workoutDetailsResponse.value?.id
            viewModel.deleteWorkout(id!!)
        }

        binding.svMain.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (Math.abs(scrollY - oldScrollY) > 0) {
                binding.lytHeartRate.heartRateChart.resetIfInteracting()
            }
        }

    }

    private fun setDefaultUiValue() {
        binding.lytTop.lytActivityItem.tvDurationTitle.text = getString(R.string.text_duration)
        binding.lytArrow.ivArrowImage.setImageResource(R.drawable.ic_arrow_down)
    }

    override fun subscribeObservers() {

        viewModel.workoutDetailsResponse.observe(this) {
            if (it != null) {
                updateUi(it)
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        viewModel.workoutDeletedResponse.observe(this) {
            it?.getContent()?.let { response ->
                mainViewModel.reloadTodaysData()

                setFragmentResult(
                    DELETE_WORKOUT_REQUEST_KEY,
                    bundleOf("allow" to true, "position" to viewModel.position)

                )
                navigateUpSafe()
            }
        }
        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(it: OWorkoutDetailsResponseModel) {
        LOGS.d("OWorkoutDetailsFragmentV2 detail=$it")

//        binding.lytIntensity.tvIntensityType.text = it.intensity

        if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3) && !it.type.equals(
                "auto", true
            ) && !it.type.equals(
                "apple", true
            ) && !it.type.equals(
                "google", true
            )
        ) {
            binding.tvEdit.visible()
        }


        binding.lytTop.lytActivityItem.tvWorkoutTime.text =
            DateFormats.getActivityDisplayDates(it.startTime, it.endTime)

        val title = StringBuilder()
        if (DateFormats.getTodaysDateString(10).equals(it.date)) {
            title.append("Today, ")
        }
        title.append(DateFormats.getOrdinalDateToday(it.date, DateFormats.dateFormat3))

        binding.lytTop.rvActivityDetails.visible()
        binding.lytTop.lytActivityItem.root.visible()
        binding.lytTop.lytToolbar.tvTitle.text = title.toString()
        binding.lytTop.lytActivityItem.tvActivityName.text = it.getFormattedActivityName()

        if (it.durationSeconds == null || it.durationSeconds == 0L) {
            binding.lytTop.lytActivityItem.tvDurationValue.text =
                ApplicationUtils.getActivityDurationFormat2(it.duration)

            binding.lytTop.lytActivityItem.tvDurationUnit.text = "00"
        } else {
            val (hour, minute, seconds) = ApplicationUtils.getFormattedDuration(it.durationSeconds)
            binding.lytTop.lytActivityItem.tvDurationValue.text =
                String.format("%02d:%02d", hour, minute)
            binding.lytTop.lytActivityItem.tvDurationUnit.text = String.format("%02d", seconds)
        }

        //binding.lytTop.lytActivityItem.tvDurationValue.paintText()

        val topValue = viewModel.getDistance(it)
        binding.lytTop.lytActivityItem.tvDistanceTitle.text = topValue.third
        binding.lytTop.lytActivityItem.tvDistanceValue.text = topValue.first
        binding.lytTop.lytActivityItem.tvDistanceUnit.text = topValue.second

        //binding.lytTop.lytActivityItem.tvDistanceValue.paintText()

        setNudgesViewPager(it.nudges)

        binding.lytTop.lytActivityItem.ivWorkoutImage.loadImage(
            requireContext(), it.iconUrl
        )

        val workoutDetailList = viewModel.prepareDataForActivity(it)
        if (workoutDetailList.size > 4) {
            binding.lytArrow.root.visible()
            workoutDetailsAdapter.setDataSet(workoutDetailList.take(4))
            binding.lytArrow.ivArrow.setOnClickListener {
                if (!viewModel.workoutDetailsExpanded) {
                    viewModel.workoutDetailsExpanded = true
                    binding.lytArrow.ivArrowImage.rotation = 180f
                    workoutDetailsAdapter.setDataSet(workoutDetailList)
                } else {
                    viewModel.workoutDetailsExpanded = false
                    workoutDetailsAdapter.setDataSet(workoutDetailList.take(4))
                    binding.lytArrow.ivArrowImage.rotation = 0f

                }
            }
        } else {
            binding.lytArrow.root.gone()
            workoutDetailsAdapter.setDataSet(workoutDetailList)
        }

        //val dummyHrValues = arrayListOf(10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200)

        if (it.type.equals(WorkoutTypes.USERWORKOUT.name, true)) {
            if (!it.hrArray.isNullOrEmpty()) {
                binding.llExpand.visible()
                viewModel.avgValue = if (it.hrAvg == null || it.hrAvg == 0 || it.hrAvg == 255) {
                    "-"
                } else {
                    it.hrAvg.toString()
                }
                setAvgHr()
                val maxHr = if (it.hrMax == null || it.hrMax == 0 || it.hrMax == 255) {
                    "-"
                } else {
                    it.hrMax.toString()
                }
                binding.lytHeartRate.tvMaxHR.text =
                    "${getString(R.string.text_max_hr)} $maxHr ${getString(R.string.text_bpm_small)}"

                hrZoneAdapter.setDataSet(
                    viewModel.generateHrZones(it.hrArray)
                )
                hrZoneAdapter.setListener(object :
                    OWorkoutHRZoneAdapter.OWorkoutHRZoneInteractionListener {
                    override fun onClick(
                        selectedPosition: Int, isHighlighted: Boolean, data: OWDActivityHRZoneData
                    ) {
                        hrZoneAdapter.updateData(selectedPosition, isHighlighted)
                        if (isHighlighted) {
                            val (indexes, color) = viewModel.getIndexList(data.zone)
                            binding.lytHeartRate.heartRateChart.updateHighlight(
                                indexes, color
                            )
                        } else {
                            binding.lytHeartRate.heartRateChart.removeHighlights()
                        }
                    }

                })
            } else {
                return
            }
        } else {
            return
        }


        val sleepChart = SleepChartModel()
        val chartList = ArrayList<ChartModel>()


        it.hrArray.forEachIndexed { index, data ->
            val chartModel = ChartModel()

            var value = data
            if (value == 255) {
                value = 0
            }

            chartModel.value = value
            chartModel.index = ""
            chartList.add(chartModel)
        }


        sleepChart.list = chartList

        binding.lytHeartRate.heartRateChart.apply {
            setVibrationUtil(vibrationUtils)
            updateGraphColor(
                Color.parseColor("#ff7f96"),
                Color.parseColor("#844B60"),
                Color.parseColor("#99ff718b"),
                Color.parseColor("#0Dff718b")
            )
            val lowValueIndex = updateDataWithMax(
                sleepChart, 5, false, false, GraphDummyModel(
                    false, 40, 100
                ), it.hrAvg, "${it.date} ${it.startTime}", "${it.date} ${it.endTime}"
            )

            setInteractiveMode(true)

            setClickListener(object : OnHeartRateChartClickAction {
                override fun onValueSelected(value: Int, isInteracting: Boolean, time: String?) {
                    if (isInteracting) {
                        binding.lytHeartRate.tvAverageTitle.text = time ?: ""
                        binding.lytHeartRate.tvAverageValue.text = if (value > 0) "$value" else "-"

                    } else {
                        setAvgHr()
                    }
                }

            })

        }

        if (it.location.isNullOrEmpty()) {
            binding.lytTop.vMapOverlay.gone()
            binding.lytTop.vMapGradientTop.gone()
            binding.lytTop.vMapGradientBottom.gone()
        } else {
            binding.lytTop.vMapOverlay.visible()
            binding.lytTop.vMapGradientTop.visible()
            binding.lytTop.vMapGradientBottom.visible()

            if (it.weather?.temp != null) {
                binding.lytTop.apply {
                    tvTemp.text = "${it.weather.temp}°C"
                    groupTemp.visible()
                }
                binding.lytTop.ivWeatherImage.setImageResource(viewModel.getWeatherImage(it.weather.status))
            } else {
                binding.lytTop.groupTemp.gone()
            }
            setUpMaps(it.location)
        }
    }

    private fun setNudgesViewPager(data: List<Nudges>?) {

        if (data.isNullOrEmpty()) {
            binding.lytTop.lytCues.root.gone()
            return
        } else {
            binding.lytTop.lytCues.root.visible()
        }
        val fragments = ArrayList<WorkoutNudgeFragment>()
        data.forEach {
            fragments.add(WorkoutNudgeFragment.newInstance(it))
        }
        val sleepBannerAdapter = OreoSleepBannerAdapter(childFragmentManager, lifecycle, fragments)
        binding.lytTop.lytCues.vpBannerSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(40))
            })
            adapter = sleepBannerAdapter
        }

        TabLayoutMediator(
            binding.lytTop.lytCues.tabLayout, binding.lytTop.lytCues.vpBannerSlider
        ) { _, _ -> }.attach()

        if (fragments.size > 1) {
            binding.lytTop.lytCues.tabLayout.visible()
        } else {
            binding.lytTop.lytCues.tabLayout.invisible()
        }
    }


    private fun setAvgHr() {
        binding.lytHeartRate.tvAverageTitle.text = getString(R.string.text_resting_hr)
        binding.lytHeartRate.tvAverageValue.text = viewModel.avgValue
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

        map.uiSettings.apply {
            isCompassEnabled = false
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = false
            isZoomGesturesEnabled = false
            isScrollGesturesEnabledDuringRotateOrZoom = false
            isScrollGesturesEnabled = false
        }
    }

    private fun setUpMaps(locationData: List<LocationDataNetwork>) {
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        if (locationData.size <= 2) {
            mapFragment.view?.gone()
            return
        }
        mapFragment.view?.visible()

        mapFragment.getMapAsync { googleMap ->
            setMap(googleMap)
            this.googleMap = googleMap
            scope.launch {
                GoogleMapsUtil().plotLocationModalGoogleMaps(
                    requireActivity(), googleMap, locationData
                ).collect {
                    withContext(Dispatchers.Main) {

                        //googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(it, maxLatLngPadding)
                        )

                        nullableBinding?.lytTop?.map?.visible()
                    }
                }
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