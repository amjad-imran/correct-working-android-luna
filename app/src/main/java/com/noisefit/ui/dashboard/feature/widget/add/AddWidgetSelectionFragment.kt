package com.noisefit.ui.dashboard.feature.widget.add

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.UIComponentType
import com.noisefit.luna.databinding.FragmentWidgetSortingBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit.ui.dashboard.feature.sportSelection.ADD_SPORT_REQUEST_KEY
import com.noisefit.ui.dashboard.feature.widget.WidgetSelectionViewModel
import com.noisefit_commans.models.Widget
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddWidgetSelectionFragment :
    BaseFragment<FragmentWidgetSortingBinding>(FragmentWidgetSortingBinding::inflate),
    AddWidgetSelectionAdapter.WidgetInteractionListener {


    private val addWidgetSelectionAdapter by lazy { AddWidgetSelectionAdapter(this) }
    private val viewModel: WidgetSelectionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            viewModel.setWidgetList(
                ArrayList(
                    AddWidgetSelectionFragmentArgs.fromBundle(
                        it
                    ).widgetList.toList()
                )
            )

        }
    }

    private fun setUpRecyclerView() {
        binding.rv.apply {

            adapter = addWidgetSelectionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        //   binding.rv.addItemDecoration(MarginItemDecoration(16))
    }

    override fun initListener() {
        setUpRecyclerView()

        binding.lytToolbar.apply {
            tvTitle.text = getString(R.string.text_add_smart_widget)
            tvDesc.text = getString(R.string.text_you_have_to_select_at_least_3_widgets)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }

        binding.btnAllow.setOnClickListener {
            val widgetList = viewModel.getSelectedWidgetSort()
            if (widgetList.size < 3) {
                uiController.onDisplayError(getString(R.string.text_you_have_to_select_at_least_3_widgets))
                return@setOnClickListener
            }
            setFragmentResult(
                ADD_SPORT_REQUEST_KEY,
                bundleOf(
                    "allSports" to viewModel.getAllWidgetSortList()
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
                addWidgetSelectionAdapter.filter.filter(s)
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
            addWidgetSelectionAdapter.filter.filter(searchQuery)
        }

        viewModel.allWidgetSortList.observe(this) {
            addWidgetSelectionAdapter.setDataSet(it)
        }
//        viewModel.count.observe(this) {
//            if (it != null) {
//                updateCount(it)
//            }
//        }

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


    override fun onWidgetClick(
        contact: Widget,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    ) {
        if (viewModel.count.value == 3 && !isChecked) {
            checkBox.isChecked = true
            showMaxDialog(
                getString(R.string.text_atleast_one_widget_required),
                getString(R.string.text_alert)
            )


        } else if (viewModel.count.value!! >= viewModel.maxCount && isChecked) {
            checkBox.isChecked = false
            showMaxDialog(
                getString(R.string.text_max_widget_message, viewModel.maxCount),
                getString(R.string.text_max_widget_reached)
            )
        } else {
            if (isChecked) {
                viewModel.updateSelectedWidgetSort(contact)
                viewModel.addSelectedCount()
            } else {
                viewModel.removeSelectedWidgetSort(contact)
                viewModel.subSelectedCount()
            }
            addWidgetSelectionAdapter.updateContactList(position, isChecked)

        }
    }

}