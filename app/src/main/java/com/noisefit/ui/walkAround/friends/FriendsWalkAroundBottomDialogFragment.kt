package com.noisefit.ui.walkAround.friends

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit_commans.data.model.FriendsWalkAround
import com.noisefit.luna.databinding.FragmentFriendsWalkAroundBottomDialogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendsWalkAroundBottomDialogFragment :
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
        localDataStore.setFriendsWalkAround(true)
    }

    private fun getWalkAroundDataList(): List<FriendsWalkAround> {
        val friendList = ArrayList<FriendsWalkAround>()
        friendList.add(
            FriendsWalkAround(
                "Daily Fitness Motivation",
                "Fitness, an everyday thing. Our curated feeds will keep you motivated and on track.",
                R.drawable.bg_f_walk_1
            )
        )
        friendList.add(
            FriendsWalkAround(
                "Flaunt your creativity",
                "Elevate the look of your feed by creating posts with our easy-to-use post maker.",
                R.drawable.bg_f_walk_2
            )
        )
        friendList.add(
            FriendsWalkAround(
                "Noise-makers, Assemble!",
                "Assemble your fitness Avengers by adding your fellow Noise-makers to your friend list.",
                R.drawable.bg_f_walk_3
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
                isDraggable = true
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }
}