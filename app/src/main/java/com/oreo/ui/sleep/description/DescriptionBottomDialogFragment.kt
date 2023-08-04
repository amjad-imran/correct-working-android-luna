package com.oreo.ui.sleep.description

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.databinding.FragmentDescriptionBottomDialogBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.Contributors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DescriptionBottomDialogFragment :
    BaseBottomSheetWithTransparent<FragmentDescriptionBottomDialogBinding>(
        FragmentDescriptionBottomDialogBinding::inflate
    ) {
    private val descriptionSliderAdapter by lazy {
        DescriptionSliderAdaptor()
    }

    private var isLast: Boolean = false
    private var isFirst: Boolean = false
    private var pos: Int = -1
    private var contributorList: ArrayList<Contributors>? = null
    private var contriType: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            pos = DescriptionBottomDialogFragmentArgs.fromBundle(it).position
            contributorList =
                java.util.ArrayList(DescriptionBottomDialogFragmentArgs.fromBundle(it).contributorsList!!.toList())
            contriType = DescriptionBottomDialogFragmentArgs.fromBundle(it).contriType
        }
        setViewpager()

    }

    private fun setViewpager() {
        binding.vpImageSlider.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            adapter = descriptionSliderAdapter

        }
        TabLayoutMediator(
            binding.tabLayout,
            binding.vpImageSlider
        ) { _, _ -> }.attach()
        contributorList?.let { descriptionSliderAdapter.setDataSet(it) }
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
                isLast = position == contributorList?.size!! - 1
                isFirst = position == 0

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        if (pos == 0) {
            isFirst = true
        }
        binding.vpImageSlider.setCurrentItem(pos, false)


    }

    private fun getItem(i: Int): Int {
        return binding.vpImageSlider.currentItem + i
    }

    override fun initListener() {
        binding.ivNext.setOnClickListener {
            if (isLast) {
                dismiss()
                return@setOnClickListener
            }
            binding.vpImageSlider.setCurrentItem(getItem(+1), true)


        }
        binding.ivPrevious.setOnClickListener {
            if (isFirst) {
                dismiss()
                return@setOnClickListener
            }
            binding.vpImageSlider.setCurrentItem(getItem(-1), true)
        }
    }

    override fun subscribeObservers() {
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
                isCancelable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }


}