package com.noisefit.ui.trophies

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.R
import com.noisefit_commans.data.model.trophies.DailyItem
import com.noisefit.databinding.BottomSheetTrophyCollectBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseBottomSheet
import com.noisefit_commans.models.Units
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrophyCollectBottomSheet :
    BaseBottomSheet<BottomSheetTrophyCollectBinding>(BottomSheetTrophyCollectBinding::inflate) {

    var trophyData: DailyItem? = null
    var type: TrophiesType? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: TrophiesViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {


        trophyData = arguments?.let {
            TrophyCollectBottomSheetArgs.fromBundle(it).trophyData
        }
        type = arguments?.let {
            TrophyCollectBottomSheetArgs.fromBundle(it).type
        }
        setObservers()
        loadTrophyData()

        binding.btnCollect.setOnClickListener {
            if (binding.btnCollect.alpha == 1f) {
                trophyData?.let {
                    if (type == TrophiesType.STEPS) {
                        viewModel.markBadgeCollected(it.stepsUserBadgeId)


                    } else {
                        viewModel.markBadgeCollected(it.distanceUserBadgeId)

                    }
                }
            }
        }

    }

    private fun setObservers() {
        viewModel.badgeCollected.observe(viewLifecycleOwner) {
            it.getContent()?.let {

                if (type == TrophiesType.STEPS) {
                    trophyData?.isStepsCollect = 1
                } else {
                    trophyData?.isDistanceCollect = 1
                }
                loadTrophyData()
            }

        }
    }

    private fun loadTrophyData() {
        trophyData?.let {

            if (it.badgeType.equals("daily", true)) {
                if (type == TrophiesType.STEPS) {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_steps_milestones)
                    if (it.isStepsCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.btnCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.btnCollect.text = getString(R.string.text_collected)
                        binding.btnCollect.alpha = 0.6f

                    }
                    binding.badgeLayout.textView49.text = getString(R.string.text_is_a_milestone)
                    binding.badgeLayout.textView50.text =
                        "You have just completed ${it.steps} Steps of Milestone, please collect your Trophy"
                } else {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_distance_milestones)
                    if (it.isDistanceCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.btnCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.btnCollect.text = getString(R.string.text_collected)
                        binding.btnCollect.alpha = 0.6f

                    }
                    val distance = if (viewModel.getUnitValue() == Units.METRIC) {
                        it.titleForKm
                    } else {
                        it.titleForMile
                    }

                    binding.badgeLayout.textView49.text = getString(R.string.text_is_a_milestone)
                    binding.badgeLayout.textView50.text =
                        "You have just completed $distance of Milestone, please collect your Trophy"
                }
            } else if (it.badgeType.equals("lifetime", true)) {
                if (type == TrophiesType.STEPS) {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_steps_lifetime)
                    if (it.isStepsCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.btnCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.btnCollect.text = getString(R.string.text_collected)
                        binding.btnCollect.alpha = 0.6f

                    }
                    binding.badgeLayout.textView49.text = getString(R.string.text_its_a_badge)
                    binding.badgeLayout.textView50.text =
                        "You have just completed ${it.steps} Steps of Badge, please collect your Trophy"
                } else {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_distance_lifettime)
                    if (it.isDistanceCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.btnCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.btnCollect.text = getString(R.string.text_collected)
                        binding.btnCollect.alpha = 0.6f

                    }
                    val distance = if (viewModel.getUnitValue() == Units.METRIC) {
                        it.titleForKm
                    } else {
                        it.titleForMile
                    }
                    binding.badgeLayout.textView49.text = getString(R.string.text_its_a_badge)
                    binding.badgeLayout.textView50.text =
                        "You have just completed $distance of Badge, please collect your Trophy"
                }
            }


        }

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
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog

    }
}