package com.noisefit.ui.npl.summary

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.noisefit_commans.data.response.LiveMatch
import com.noisefit_commans.data.response.PrizeInfo

enum class IplDashBoardEnum() {
    LIVE_SCORE,
    PREDICTION,
    LOST_STATE
}

class IplDashboardAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val data: Pair<PrizeInfo?, LiveMatch?>,
    private val listOfFragment: ArrayList<String>
) : FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int {
        return listOfFragment.size
    }

    override fun createFragment(position: Int): Fragment {
        when (listOfFragment[position]) {
            IplDashBoardEnum.LIVE_SCORE.name -> return LiveScoreFragment.newInstance(data.second!!)
            IplDashBoardEnum.PREDICTION.name -> return PredictionFragment.newInstance(data.first!!)
            IplDashBoardEnum.LOST_STATE.name -> return NplLostFragment.newInstance()
        }
        return LiveScoreFragment()
    }
}