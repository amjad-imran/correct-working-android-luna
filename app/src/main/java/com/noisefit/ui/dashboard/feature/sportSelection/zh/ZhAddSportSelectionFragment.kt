package com.noisefit.ui.dashboard.feature.sportSelection.zh

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.databinding.FragmentAddSportSelectionBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.feature.sportSelection.ADD_SPORT_REQUEST_KEY
import com.noisefit_commans.models.Widget
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ZhAddSportSelectionFragment :
    BaseFragment<FragmentAddSportSelectionBinding>(FragmentAddSportSelectionBinding::inflate),
    ZhAddSportSelectionAdapter.SportInteractionListener {


    private val sportSelectionAdapter by lazy { ZhAddSportSelectionAdapter(this) }
    private val viewModel: ZhSportSelectionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            viewModel.setAllSports(
                ArrayList(
                    ZhAddSportSelectionFragmentArgs.fromBundle(
                        it
                    ).widgetList.toList()
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
                bundleOf(
                    "allSports" to viewModel.getAllSportList()
                )
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

        viewModel.allSportModeList.observe(this) {
            sportSelectionAdapter.setDataSet(it)
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

    private fun showMaxDialog(alertMessage: String, title: String) {
        uiController.onApiErrorReceived(
            ErrorResponse(
                UIComponentType.InfoAlertDialog(
                    title,
                    alertMessage,
                    getString(R.string.text_close)
                )
            )
        )
        
    }

    override fun onSportClick(
        contact: Widget,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    ) {
        if (viewModel.sportSelectedCount.value == 1 && !isChecked) {
            checkBox.isChecked = true
            showMaxDialog(
                getString(R.string.text_atleast_one_sport_required),
                getString(R.string.text_alert)
            )


        } else if (viewModel.sportSelectedCount.value!! >= viewModel.maxCount && isChecked) {
            checkBox.isChecked = false
            showMaxDialog(
                getString(R.string.text_max_sport_message, viewModel.maxCount),
                getString(R.string.text_max_sport_reached)
            )
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