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
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.MoEngageLunaAppEvents
import com.oreo.data.model.OActivityListModal
import dagger.hilt.android.AndroidEntryPoint

const val DELETE_WORKOUT_REQUEST_KEY = "DELETE_WORKOUT_REQUEST_KEY"

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
        viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_workout_list_page_visit)
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
                context.showShortToast(getString(R.string.text_please_connect_your_ring_to_add_a_workout))
            }
        }
        binding.lytTodayEmpty.view1.setOnClickListener {
            if (viewModel.isDeviceConnected()) {
                navigate(R.id.addWorkoutFragment)
            } else {
                context.showShortToast(getString(R.string.text_please_connect_your_ring_to_add_a_workout))
            }
        }
        /* binding.lytEmptyView.btnAddWorkout.setOnClickListener {
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
         }*/

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
        LOGS.d("onActivitySelected activity=$activity")
        //viewModel.sessionManager.logMoEngageAppEvent(MoEngageLunaAppEvents.luna_homepage_workout_item_click)

        uiController.logAppEvent(
            MoEngageLunaAppEvents.workout_clicked,
            hashMapOf("source" to "activity",
                "description" to "old_workout_check",
                "workout_name" to "${activity.activityType}")
        )

        if(activity.getDisplayVersionType()==2){
            navigate(R.id.oWorkoutDetailsFragmentV2, Bundle().apply {
                putString("workoutId", activity.id ?: "")
                putInt("position", position)
            })
        }else{
            navigate(R.id.oWorkoutDetailsFragment, Bundle().apply {
                putString("workoutName", activity.getTranslatedActivityName())
                putString("workoutId", activity.id ?: "")
                putInt("position", position)
            })
        }
    }


}