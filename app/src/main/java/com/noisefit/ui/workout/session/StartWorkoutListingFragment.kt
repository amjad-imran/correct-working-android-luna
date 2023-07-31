package com.noisefit.ui.workout.session

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit.databinding.FragmentStartWorkoutListingBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.workout.ActivityOptionsViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit.util.ImageUtil
import com.noisefit_commans.interfaces.QueryAction
import com.noisefit_commans.interfaces.QueryCallback
import com.noisefit_commans.interfaces.device_data.UpdateDeviceAction
import com.noisefit_commans.interfaces.device_data.UpdateDeviceDataCallback
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.SportsModeList
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StartWorkoutListingFragment :
    BaseFragment<FragmentStartWorkoutListingBinding>(FragmentStartWorkoutListingBinding::inflate) {

    @Inject
    lateinit var imageUtil: ImageUtil

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: ActivityOptionsViewModel by viewModels()

    private lateinit var mAdapter: WorkoutListingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.tvTitle.text = getString(R.string.start_session)
        setUpRecycler()
        viewModel.getActivities()
    }

    private fun setUpRecycler() {
        mAdapter = WorkoutListingAdapter(object : CardClickListener {
            override fun onCardClicked(summary: SportsModeList.SportsMode) {

                navigate(
                    StartWorkoutListingFragmentDirections.actionStartWorkoutListingFragmentToNavigationActivityStart(
                        summary
                    )
                )
            }
        })
        with(binding.rvWorkouts) {
            layoutManager = LinearLayoutManager(context)
            this.adapter = mAdapter
        }
    }

    override fun initListener() {

        binding.toolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mAdapter.setDataSet(viewModel.getFilteredItems(s.toString()))

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    override fun subscribeObservers() {
        viewModel.fetchRecentActivities.observe(viewLifecycleOwner) { status ->
            if (status) {
                sessionManager.sendQueryAction(QueryAction.GetSportModeInfo)
            }
        }

        sessionManager.deviceQueryCallback.observe(viewLifecycleOwner) {
            when (it) {
                is QueryCallback.SportModeInfoObtained -> {
                    if (it.sportsModeList == null) {

                        addDefaultActivity()
                    } else {
                        if (it.sportsModeList!!.sportsModes.isNullOrEmpty()) {

                            addDefaultActivity()
                        } else {

                            it.sportsModeList?.sportsModes?.let { modes ->
                                // viewModel.formatDataSet(modes)
                                viewModel.masterDataSet.clear()
                                viewModel.masterDataSet.addAll(viewModel.formatDataSet(modes))
                                mAdapter.setDataSet(viewModel.masterDataSet)
                            }
                        }
                    }
                    LOGS.i("SportsModeSelection", it.sportsModeList?.sportsModes.toString())
                }
                else -> {}
            }
        }

        sessionManager.updateDeviceCallback.observe(viewLifecycleOwner) {
            it.getContent()?.let { value ->
                when (value) {
                    is UpdateDeviceDataCallback.SportModeDataUpdated -> {
                        if (value.success) {
                            sessionManager.sendQueryAction(QueryAction.GetSportModeInfo)
                        }
                    }
                    else -> {}
                }
            }
        }

    }

    private fun addDefaultActivity() {
        sessionManager.connectedDevice.value?.deviceType?.let {
            when {
                it.equals(DeviceType.COLORFIT_PRO_2_OXY.deviceType, true) -> {
                    sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.SetSportModeInfo(
                            SportsModeList().apply {
                                this.sportsModes = AppConstants.sportsModeListOxy
                            }
                        ))
                }
                it.equals(DeviceType.COLORFIT_PRO_2.deviceType, true) -> {
                    sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.SetSportModeInfo(
                            SportsModeList().apply {
                                this.sportsModes = AppConstants.sportsModeListPro2
                            }
                        ))
                }
                it.equals(DeviceType.COLORFIT_2.deviceType, true) ->
                    sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.SetSportModeInfo(
                            SportsModeList().apply {
                                this.sportsModes = AppConstants.sportsModeListPro2
                            }
                        ))
                else -> {
                    sessionManager.sendUpdateQueryAction(
                        UpdateDeviceAction.SetSportModeInfo(
                            SportsModeList().apply {
                                this.sportsModes = AppConstants.sportsModeList
                            }
                        ))
                }
            }
        }
        /*sessionManager.sendUpdateQueryAction(
            UpdateDeviceAction.SetSportModeInfo(
                SportsModeList().apply {
                    this.sportsModes = arrayListOf(
                        SportsModeList.SportsMode(
                            index = 14,
                            name = "workout",
                            type = 8,
                            value = true
                        )
                    )
                }
            ))*/
    }


}