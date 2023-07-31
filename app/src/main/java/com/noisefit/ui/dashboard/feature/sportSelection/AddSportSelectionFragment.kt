package com.noisefit.ui.dashboard.feature.sportSelection

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentAddSportSelectionBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.models.SportsModeList
import dagger.hilt.android.AndroidEntryPoint


const val ADD_SPORT_REQUEST_KEY = "ADD_SPORT_REQUEST_KEY"

@AndroidEntryPoint
class AddSportSelectionFragment :
    BaseFragment<FragmentAddSportSelectionBinding>(FragmentAddSportSelectionBinding::inflate),
    AddSportSelectionAdapter.SportInteractionListener {


    private val sportSelectionAdapter by lazy { AddSportSelectionAdapter(this) }
    private val viewModel: SportSelectionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            viewModel.setAllSports(
                ArrayList(
                    AddSportSelectionFragmentArgs.fromBundle(
                        it
                    ).sportList.toList()
                )
            )

        }
    }

    private fun setUpRecyclerView() {
        binding.rv.apply {

            adapter = sportSelectionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }


        //   binding.rv.addItemDecoration(MarginItemDecoration(16))
    }

    override fun initListener() {
        setUpRecyclerView()

        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_add_sport_mode)
            tvDesc.text = getString(R.string.text_you_can_add_sports, viewModel.maxCount)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.btnAllow.setOnClickListener {
            setFragmentResult(
                ADD_SPORT_REQUEST_KEY,
                bundleOf("sports" to viewModel.getSelectedSport())
            )
            navigateUpSafe()
        }
        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sportSelectionAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun updateCount(count: Int) {
        val dataCount = "($count/${viewModel.maxCount})"
        binding.tvSportsCount.text = dataCount
    }

    override fun subscribeObservers() {
        viewModel.noiseFitSearchQuery.observe(this) {
            val searchQuery = it
            sportSelectionAdapter.filter.filter(searchQuery)
        }

        viewModel.sportsLiveData.observe(this) {
            if (it.isEmpty()) {

//                binding.layoutNoContact.root.visible()
            } else {
//                binding.layoutNoContact.root.gone()
                sportSelectionAdapter.setDataSet(it)
            }

        }

        viewModel.sportSelectedCount.observe(this) {
            if (it != null) {
                updateCount(it)
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

    private fun showMaxContactDialog() {
        val alertMessage = getString(R.string.text_max_sport_message, viewModel.maxCount)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                getString(
                    R.string.text_max_sport_reached
                )
            )
            .setCancelable(false)
            .setMessage(alertMessage)
            .setNegativeButton(resources.getString(R.string.text_close)) { dialog, which ->
                dialog.dismiss()

            }

            .show()

    }

    override fun onSportClick(
        contact: SportsModeList.SportsMode,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    ) {
        if (viewModel.sportSelectedCount.value!! >= viewModel.maxCount && isChecked) {
            checkBox.isChecked = false
            showMaxContactDialog()
        } else {
            if (isChecked) {
                viewModel.updateSelectedSport(contact)
                viewModel.addSelectedCount()
            } else {
                viewModel.removeSelectedSport(contact)
                viewModel.subSelectedCount()
            }

            sportSelectionAdapter.updateContactList(position, isChecked)

        }
    }

}