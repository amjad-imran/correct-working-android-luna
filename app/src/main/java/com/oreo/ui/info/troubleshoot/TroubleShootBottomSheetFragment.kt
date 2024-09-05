package com.oreo.ui.info.troubleshoot

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.noisefit.luna.R
import com.noisefit.luna.databinding.BottomSheetTroubleshootBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.invisible
import com.noisefit_commans.ui.tryCatch
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.utils.share.ShareUtil.SUPPORT_URL
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TroubleShootBottomSheetFragment :
    BaseBottomSheetWithTransparent<BottomSheetTroubleshootBinding>(
        BottomSheetTroubleshootBinding::inflate
    ) {

    private val args: TroubleShootBottomSheetFragmentArgs by navArgs()

    private val descriptionSliderAdapter by lazy {
        TroubleshootAdapter(object : TroubleShootAction {
            override fun onClicked(action: TroubleShootActionType) {
                handleActionClick(action)
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val showLocation = args.showLastLocation

        setViewpager(showLocation)

    }

    private fun handleActionClick(action: TroubleShootActionType) {
        when (action) {
            TroubleShootActionType.LAST_LOCATION -> {
                navigate(R.id.ringLocationFragment)
            }

            TroubleShootActionType.BLUETOOTH -> {
                tryCatch {
                    val settingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    startActivity(settingsIntent)
                }
            }

            TroubleShootActionType.CONTACT_US -> {
                context?.let {
                    ShareUtil.openExternalUrl(it, SUPPORT_URL)
                }
            }
        }
    }

    private fun setViewpager(showLocation: Boolean) {
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

        descriptionSliderAdapter.setDataSet(generateDataSet(),showLocation)

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
                    binding.ivPrevious.invisible()
                    binding.ivNext.visible()
                } else if (position == (descriptionSliderAdapter.itemCount - 1)) {
                    binding.ivPrevious.visible()
                    binding.ivNext.invisible()
                } else {
                    binding.ivPrevious.visible()
                    binding.ivNext.visible()
                }


            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

    }

    private fun generateDataSet(): java.util.ArrayList<TroubleShootData> {
        return arrayListOf(
            TroubleShootData(
                title = "Steps to follow",
                message = "Try reconnecting your ring for few minutes until it’s connected",
                image = R.drawable.image_ts_1,
                ctaText = "Check last synced location",
                action = TroubleShootActionType.LAST_LOCATION
            ),
            TroubleShootData(
                title = "Steps to follow",
                message = "Ensure your ring and your phone is within 1 metre distance",
                image = R.drawable.image_ts_2,
                ctaText = "Check last synced location",
                action = TroubleShootActionType.LAST_LOCATION
            ),
            TroubleShootData(
                title = "Steps to follow",
                message = "Try turning off the bluetooth and reconnect again to the ring",
                image = R.drawable.image_ts_3,
                ctaText = "Turn on bluetooth",
                action = TroubleShootActionType.BLUETOOTH
            ),
            TroubleShootData(
                title = "Still not connecting?",
                message = "Reach out to us by tapping the button below",
                image = R.drawable.image_ts_4,
                ctaText = "Contact us",
                action = TroubleShootActionType.CONTACT_US
            )
        )
    }

    private fun getItem(i: Int): Int {
        return binding.vpImageSlider.currentItem + i
    }

    override fun initListener() {
        binding.ivNext.setOnClickListener {
            /*if (isLast) {
                dismiss()
                return@setOnClickListener
            }*/
            binding.vpImageSlider.setCurrentItem(getItem(+1), true)


        }
        binding.ivPrevious.setOnClickListener {
            /*if (isFirst) {
                dismiss()
                return@setOnClickListener
            }*/
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