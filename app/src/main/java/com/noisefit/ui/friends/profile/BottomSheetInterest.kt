package com.noisefit.ui.friends.profile

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.noisefit.R
import com.noisefit.databinding.BottomSheetInterestBinding
import com.noisefit.ui.friends.reactions.ReactionsBottomSheetArgs
import com.noisefit_commans.data.model.Interest
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.displayToast
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

const val INTEREST_UPDATE_KEY = "INTEREST_UPDATE_KEY"

@AndroidEntryPoint
class BottomSheetInterest :
    BaseBottomSheetWithTransparent<BottomSheetInterestBinding>(BottomSheetInterestBinding::inflate) {

    private val viewModel: InterestsViewModel by viewModels()
    private val sharedViewModel: InterestSharedViewModel by activityViewModels()
    private val args: BottomSheetInterestArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, com.noisefit_commans.R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.lastSelectedInterest =sharedViewModel.selectedInterests
        viewModel.prepareSelectedInterestData()
        updateSelectedCount(0)
        viewModel.getInterestList()


    }

    fun updateSelectedCount(selectedCount: Int) {
        binding.tvSelectedCount.text = "Selected  ($selectedCount/5)"
    }




    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            viewModel.localDataStore.setInterestCancelled()
            navigateUpSafe()
        }

        binding.cgInterests.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.size > 5) {
                val chip: Chip = binding.cgInterests.findViewById(checkedIds.last())
                chip.isChecked = false
            } else {
                updateSelectedCount(checkedIds.size)
            }
        }

        binding.btnSave.setOnClickListener {


            val ids: List<Int> = binding.cgInterests.checkedChipIds
            val selectedInterest = ArrayList<Interest>()
            for (id in ids) {
                val chip: Chip = binding.cgInterests.findViewById(id)
                selectedInterest.add(
                    Interest(
                        name = chip.text.toString(),
                        id = chip.tag as Int
                    )
                )
            }

            if (selectedInterest.size < 5) {
                context.showShortToast("Select 5 interests")
                return@setOnClickListener
            }

            if (args.shouldUpdate) {
                viewModel.updateInterest(selectedInterest)
            } else {
                setFragmentResult(
                    INTEREST_UPDATE_KEY,
                    bundleOf(
                        "selectedValues" to selectedInterest
                    )
                )
                navigateUpSafe()
            }
        }

    }

    override fun subscribeObservers() {

        viewModel.interests.observe(this) {
            setChips(it)
        }
        viewModel.interestUpdated.observe(this) {
            it.getContent()?.let {
                setFragmentResult(
                    INTEREST_UPDATE_KEY,
                    bundleOf(
                        "updated" to true
                    )
                )
                navigateUpSafe()
            }
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
    }

    fun setChips(chips: List<Interest>) {

        binding.cgInterests.removeAllViews()
        val interests = viewModel.selectedInterests

        chips.forEach {
            val mChip: Chip =
                layoutInflater.inflate(R.layout.item_chip_feedback, null, false) as Chip
            mChip.text = it.name
            mChip.tag = it.id

            if (interests.contains((it.id ?: -1).toInt())) {
                mChip.isChecked = true
            }
            val paddingDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10F, resources.displayMetrics
            )
            mChip.setPadding(paddingDp.toInt(), 0, paddingDp.toInt(), 0)
            binding.cgInterests.addView(mChip)
        }

        updateSelectedCount(binding.cgInterests.checkedChipIds.size)
    }
}