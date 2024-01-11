package com.oreo.ui.activity.all

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentOActivityListBinding
import com.noisefit.oreo.OreoMainViewModel
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.FirebaseLunaAppEvents
import com.oreo.data.model.OActivityListModal
import com.oreo.ui.workout.details.DELETE_WORKOUT_REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OActivityListFragment :
    BaseFragment<FragmentOActivityListBinding>(FragmentOActivityListBinding::inflate),
    OActivityListInteraction {

    private val adapter: OActivityListAdapter by lazy {
        OActivityListAdapter(this, viewModel.dataUnitConverter)
    }

    private val mainViewModel: OreoMainViewModel by activityViewModels()
    private val viewModel: OActivityListViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessionManager.logFirebaseEvent(FirebaseLunaAppEvents.LUNA_WORKOUT_LIST_PAGE_VISIT)
        setRecycler()
//        if (viewModel.activities.value.isNullOrEmpty()) {
        viewModel.fetchActivityFromServer()
//        }

    }


    override fun initListener() {
        binding.lytTodayEmpty.btnAddWorkout.setOnClickListener {
            if (viewModel.isDeviceConnected()) {
                navigate(R.id.addWorkoutFragment)
            } else {
                context.showShortToast("Please connect your ring to add a workout")
            }
        }
        binding.lytTodayEmpty.view1.setOnClickListener {
            if (viewModel.isDeviceConnected()) {
                navigate(R.id.addWorkoutFragment)
            } else {
                context.showShortToast("Please connect your ring to add a workout")
            }
        }
        binding.lytEmptyView.btnAddWorkout.setOnClickListener {
            if (viewModel.isDeviceConnected()) {
                navigate(R.id.addWorkoutFragment)
            } else {
                context.showShortToast("Please connect your ring to add a workout")
            }
        }
        binding.lytEmptyView.view1.setOnClickListener {
            if (viewModel.isDeviceConnected()) {
                navigate(R.id.addWorkoutFragment)
            } else {
                context.showShortToast("Please connect your ring to add a workout")
            }
        }

        setFragmentResultListener(DELETE_WORKOUT_REQUEST_KEY) { _, bundle ->
            val allow = bundle.getBoolean("allow")
            val position = bundle.getInt("position")
            if (allow) {
                if (position != -1) {
                    adapter.removeItem(position)
                    if (adapter.itemCount == 0 || adapter.itemCount == 1) {
                        binding.rv.gone()
                    }
                    handleTodayEmptyView()
                }
            }
        }
        binding.lytToolbar.apply {
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
            tvTitle.text = getString(R.string.text_all_workouts)

        }

//        binding.srActivity.setOnRefreshListener {
//
//            Handler(Looper.getMainLooper()).postDelayed(dismissRunnable, 10000)
//
//        }

        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.isActivitiesLoading || viewModel.isLastPage) {
                        return
                    }
                    nullableBinding?.let {
                        val visibleItemCount = it.rv.layoutManager?.childCount ?: 0
                        val totalItemCount = it.rv.layoutManager?.itemCount ?: 0
                        val firstVisibleItemPosition =
                            (it.rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            // viewModel.fetchActivityFromServer()
                        }
                    }
                }
            }

        })
    }

    private fun setRecycler() {
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = adapter
        adapter.connectedDevice =
            viewModel.sessionManager.connectedDeviceRing.value?.deviceType ?: ""
        adapter.unitsSystem = viewModel.localDataStore.getUnit()
    }

    override fun subscribeObservers() {
        viewModel.activities.observe(this) {

            adapter.setDataSet(it)
            binding.lytEmptyView.root.gone()
            binding.rv.visible()
            handleTodayEmptyView()
        }

        viewModel.emptyActivities.observe(this) {
            it?.getContent()?.let {
                if (it) {
                    binding.lytEmptyView.root.visible()
                    binding.rv.gone()

                } else {
                    binding.lytEmptyView.root.gone()
                    binding.rv.visible()
                }
            }
            handleTodayEmptyView()
        }


        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }


        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar1.root.visible()
            } else {
                binding.progressBar1.root.gone()
            }
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }

    private fun handleTodayEmptyView() {
        if (adapter.isShowTodayEmptyView()) {
            binding.lytTodayEmpty.root.visible()
        } else
            binding.lytTodayEmpty.root.gone()
    }

//    private val dismissRunnable = Runnable {
//        try {
//            if (binding.srActivity.isRefreshing) {
//                binding.srActivity.isRefreshing = false
//            }
//        } catch (exp: Exception) {
//        }
//    }


    override fun onActivitySelected(activity: OActivityListModal, position: Int) {
        navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
            putString("workoutId", activity.id)
            putInt("position", position)
            putString("workoutName", activity.getFormattedActivityName())
        })
    }


}