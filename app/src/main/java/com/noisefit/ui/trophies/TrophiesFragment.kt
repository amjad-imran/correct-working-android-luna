package com.noisefit.ui.trophies

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.R
import com.noisefit.databinding.FragmentTrophiesBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrophiesFragment : BaseFragment<FragmentTrophiesBinding>(FragmentTrophiesBinding::inflate) {
    private lateinit var pagerAdapter: TrophiesPagerAdapter

    private val viewModel: TrophiesViewModel by activityViewModels()
    var defaultSelected = 0
    private val args: TrophiesFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultSelected =
            if (args.selectedDefault?.equals(TrophiesType.DISTANCE.name, true) == true) {
                1
            } else {
                0
            }

        binding.layoutSteps.tvTabTitle.text = getString(R.string.text_steps)
        binding.layoutDistance.tvTabTitle.text = getString(R.string.text_distance)

        setPagerAdapter(defaultSelected)
        val mobileNumber = arguments?.let {
            TrophiesFragmentArgs.fromBundle(it).userMobile
        }

        /**
         * Case : When opened from Buddies
         */
        if (mobileNumber != null && !mobileNumber.equals("null", true)) {
            viewModel.buddyMobileNumber = mobileNumber
            viewModel.getBuddyTrophiesData(mobileNumber)
            viewModel.getUserMobileNumber()?.let {
//                if (it.equals(mobileNumber, true)) {
                binding.tvRemoveBuddy.gone()
//                } else {
//                    binding.tvRemoveBuddy.visible()
//                }
            }
        } else {
            viewModel.buddyMobileNumber = null
            viewModel.getTrophiesData(true)
            binding.tvRemoveBuddy.gone()
        }

    }


    private fun setTabBackground(position: Int) {
        val intervals = arrayOf(binding.layoutSteps, binding.layoutDistance)
        intervals.forEachIndexed { index, binding ->
            if (index == position) {
                binding.tvTabTitle.setTextColor(binding.tvTabTitle.context.getColor(R.color.accent_color_purple))
                binding.vBottom.visible()
            } else {
                binding.tvTabTitle.setTextColor(Color.parseColor("#a3ffffff"))
                binding.vBottom.invisible()
            }
        }
    }


    override fun initListener() {
        binding.layoutToolbar.tvTitle.text = getString(R.string.my_badges_title)
        binding.layoutToolbar.backBtn.setOnClickListener {
            navigateUpSafe()
        }


    }

    override fun subscribeObservers() {
        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }


        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }

        viewModel.collectTrophy.observe(this) {
            it.getContent()?.let { data ->
                navigate(
                    TrophiesFragmentDirections.actionTrophiesFragmentToTrophyDetailsFragment(
                        data.first,
                        data.second
                    )
                )
            }
        }

        viewModel.buddyRemoved.observe(this) {
            it.getContent()?.let {
                navigateUpSafe()
            }
        }

    }

    private fun setPagerAdapter(defaultSelected: Int) {
        pagerAdapter = TrophiesPagerAdapter(childFragmentManager, lifecycle)
        binding.vpTrophies.isUserInputEnabled = false
        binding.vpTrophies.adapter = pagerAdapter

        if (defaultSelected == 0) {
            setTabBackground(0)
            Handler(Looper.getMainLooper()).postDelayed({
                if (view != null) {
                    binding.vpTrophies.setCurrentItem(0, true)
                }
            }, 500)
        } else {
            setTabBackground(1)
            Handler(Looper.getMainLooper()).postDelayed({
                if (view != null) {
                    binding.vpTrophies.setCurrentItem(1, true)
                }
            }, 500)
        }



        binding.layoutSteps.root.setOnClickListener {
            setTabBackground(0)
            binding.vpTrophies.currentItem = 0
        }
        binding.layoutDistance.root.setOnClickListener {
            setTabBackground(1)
            binding.vpTrophies.currentItem = 1
        }

    }


}

enum class TrophiesType {
    STEPS, DISTANCE
}