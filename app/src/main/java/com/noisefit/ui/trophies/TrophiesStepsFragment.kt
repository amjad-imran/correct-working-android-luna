package com.noisefit.ui.trophies

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.noisefit_commans.data.model.trophies.DailyItem
import com.noisefit.luna.databinding.FragmentTrophiesStepsBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit.ui.trophies.adapter.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrophiesStepsFragment :
    BaseFragment<FragmentTrophiesStepsBinding>(FragmentTrophiesStepsBinding::inflate) {
    private val mileStoneAdapter by lazy {
        TrophiesStepsMilestonesAdapter(object : StepsMilestonesAction {
            override fun onTrophyClicked(item: DailyItem) {
                if (viewModel.buddyMobileNumber.isNullOrEmpty()) {
                    navigate(
                        TrophiesFragmentDirections.actionTrophiesFragmentToTrophyDetailsFragment(
                            item,
                            TrophiesType.STEPS
                        )
                    )
                }
            }
        })
    }
    private val goalStreakAdapter by lazy {
        TrophiesStepsGoalsAdapter()
    }
    private val lifetimeAdapter by lazy {
        TrophiesStepsLifetimeAdapter(object : StepsLifetimeAction {
            override fun onTrophyClicked(item: DailyItem) {
                if (viewModel.buddyMobileNumber.isNullOrEmpty()) {
                    navigate(
                        TrophiesFragmentDirections.actionTrophiesFragmentToTrophyDetailsFragment(
                            item,
                            TrophiesType.STEPS
                        )
                    )
                }
            }
        })
    }

    private val viewModel: TrophiesViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycler()
    }


    private fun setRecycler() {
        binding.rvMilestones.layoutManager = GridLayoutManager(context, 3)
        binding.rvMilestones.adapter = mileStoneAdapter

        binding.rvGoalSteaks.layoutManager = GridLayoutManager(context, 3)
        binding.rvGoalSteaks.adapter = goalStreakAdapter

        binding.rvLifetime.layoutManager = GridLayoutManager(context, 3)
        binding.rvLifetime.adapter = lifetimeAdapter
    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
        viewModel.steps.observe(this) {
            mileStoneAdapter.setDataSet(it.daily ?: ArrayList(), TrophiesType.STEPS)
            goalStreakAdapter.setDataSet(it.streaks ?: ArrayList(), TrophiesType.STEPS)
            lifetimeAdapter.setDataSet(it.lifetime ?: ArrayList(), TrophiesType.STEPS)


        }
    }
}
