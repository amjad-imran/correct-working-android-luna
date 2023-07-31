package com.noisefit.ui.reward.walkthrough

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.R
import com.noisefit_commans.data.model.FriendsWalkAround
import com.noisefit.databinding.FragmentFriendsWalkAroundBottomDialogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit.ui.walkAround.friends.FriendsWalkAroundSliderAdapter
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CoinsWalkAroundBottomDialogFragment :
    BaseBottomSheetWithTransparent<FragmentFriendsWalkAroundBottomDialogBinding>(
        FragmentFriendsWalkAroundBottomDialogBinding::inflate
    ) {

    private val friendsWalkAroundSliderAdapter by lazy {
        FriendsWalkAroundSliderAdapter()
    }

    private var listCount = 0

    @Inject
    lateinit var localDataStore: DataStoredInterface

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPrevious.text = getString(R.string.text_back)
        setViewpager()
    }


    private fun getItem(i: Int): Int {
        return binding.vpImageSlider.currentItem + i
    }

    private fun setViewpager() {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            adapter = friendsWalkAroundSliderAdapter

        }
        TabLayoutMediator(
            binding.tabLayout,
            binding.vpImageSlider
        ) { _, _ -> }.attach()

        val walkAroundList = getWalkAroundDataList()
        listCount = walkAroundList.size

        friendsWalkAroundSliderAdapter.setDataSet(walkAroundList)

        binding.vpImageSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                LOGS.d("onPageScrolled $position")
                if (position == 0) {
                    binding.btnPrevious.invisible()
                } else {
                    binding.btnPrevious.visible()
                }

                if (position == listCount - 1) {
                    binding.btnNext.text = getString(R.string.text_done)
                    binding.btnSkip.gone()
                    binding.arrow.gone()
                } else {
                    binding.btnNext.text = getString(R.string.text_next)
                    binding.btnSkip.visible()
                    binding.arrow.visible()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }


    override fun initListener() {
        binding.btnSkip.setOnClickListener {
            setWalkAround()
        }

        binding.arrow.setOnClickListener {
            setWalkAround()
        }
        binding.btnNext.setOnClickListener {
            if (binding.btnNext.text.equals(getString(R.string.text_done))) {
                setWalkAround()
                return@setOnClickListener
            }
            binding.vpImageSlider.setCurrentItem(getItem(+1), true)
        }

        binding.btnPrevious.setOnClickListener {
            binding.vpImageSlider.setCurrentItem(getItem(-1), true)
        }
    }

    override fun subscribeObservers() {

    }


    private fun setWalkAround() {
        dismiss()
        localDataStore.setCoinsWalkAround(true)
    }

    private fun getWalkAroundDataList(): List<FriendsWalkAround> {
        val friendList = ArrayList<FriendsWalkAround>()
        friendList.add(
            FriendsWalkAround(
                "Rewards",
                "Finish simple tasks & meet milestones to earn Noise Coins",
                R.drawable.bg_c_walk_1
            )
        )
        friendList.add(
            FriendsWalkAround(
                "Step Streak",
                "Complete your step goal daily to maintain your streak, and earn coins daily",
                R.drawable.bg_c_walk_2
            )
        )
        friendList.add(
            FriendsWalkAround(
                "Level up for daily reward multipliers",
                "The longer your streak, the more you earn daily",
                R.drawable.bg_c_walk_3
            )
        )
        friendList.add(
            FriendsWalkAround(
                "Your profile, front and center",
                "Change your settings, and check your progress",
                R.drawable.bg_c_walk_4
            )
        )
        return friendList
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = false
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}