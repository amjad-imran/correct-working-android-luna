package com.noisefit.ui.dashboard.healthOverview.edit

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.R
import com.noisefit_commans.data.model.HealthOverViewList
import com.noisefit.luna.databinding.FragmentEditDashboardBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.showShortToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditDashboardFragment :
    BaseFragment<FragmentEditDashboardBinding>(FragmentEditDashboardBinding::inflate),
    EditDashBoardAdapter.EditDashBoardInteractionListener {

    private val viewModel: EditDashboardViewModel by viewModels()
    private val editDashBoardAdapter by lazy {
        EditDashBoardAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytToolbarWithDetails.apply {
            tvTitle.text = getString(R.string.text_edit_health_overview)
            tvDesc.text = getString(R.string.text_edit_dashboard_about)
            backBtn.setOnClickListener {
                navigateUpSafe()
            }
        }
        setAdapter()
    }

    private fun setAdapter() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = editDashBoardAdapter
        }
        editDashBoardAdapter.setDataSet(viewModel.getDashBoardList())
    }

    override fun initListener() {

        binding.btnCancel.setOnClickListener {
            navigateUpSafe()
        }

        binding.btnAllow.setOnClickListener {
            if (viewModel.isMinItemsSelected()) {
                viewModel.updateDashboardList()
                navigateUpSafe()
            } else {
                context.showShortToast(getString(R.string.text_select_minimum_data_points))
            }
        }
    }

    override fun subscribeObservers() {

    }

    override fun onEditDashboardClick(
        healthOverViewList: HealthOverViewList,
        isChecked: Boolean,
        position: Int,
        checkBox: CheckBox
    ) {
        viewModel.updateEditHealthOverview(healthOverViewList.id, isChecked)
        editDashBoardAdapter.updateDashboard(isChecked, position)
    }


}