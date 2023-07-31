package com.noisefit.ui.onboarding.onboardProfile.endgame

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.noisefit.luna.R
import com.noisefit_commans.data.model.Interest
import com.noisefit.luna.databinding.FragmentEndGameBinding
import com.noisefit.ui.onboarding.onboardProfile.SetupProfileViewModel
import com.noisefit_commans.ui.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EndGameFragment : BaseFragment<FragmentEndGameBinding>(FragmentEndGameBinding::inflate) {

    private val viewModel: SetupProfileViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUi()
        binding.lytOnBoardProgress.apply {
            pgBr.progress = 24
            tvCount.text = getString(R.string.text_2)
        }

        viewModel.getInterestList()

    }

    private fun setUpUi() {
        updateSelectedCount(0)
    }

    fun updateSelectedCount(selectedCount: Int) {
        if (selectedCount == 5) {
            binding.btnContinue.enable()
        } else {
            binding.btnContinue.disable()
        }
        binding.tvSelectedCount.text = "Selected  ($selectedCount/5)"
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

    override fun initListener() {

        binding.cgInterests.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.size > 5) {
                val chip: Chip = binding.cgInterests.findViewById(checkedIds.last())
                chip.isChecked = false
            } else {
                updateSelectedCount(checkedIds.size)
            }
        }


        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        binding.btnContinue.setOnClickListener {


            val ids: List<Int> = binding.cgInterests.checkedChipIds
            val selectedInterest = ArrayList<Int>()
            for (id in ids) {
                val chip: Chip = binding.cgInterests.findViewById(id)
                selectedInterest.add(chip.tag.toString().toInt())
            }

            if (selectedInterest.size < 5) {
                context.showShortToast("Select 5 interests to continue")
                return@setOnClickListener
            }
            viewModel.updateInterest(selectedInterest)
            viewModel.saveUserInfoLocally()

            navigate(R.id.onBoardDobFragment)
        }


    }

    override fun subscribeObservers() {

        viewModel.interests.observe(this) {
            setChips(it)
        }

        viewModel.getApiErrors().observe(this) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
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
}
