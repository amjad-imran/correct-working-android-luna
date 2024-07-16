package com.oreo.ui.sleep2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentODropDownBinding
import com.noisefit.luna.databinding.LayoutSleepDurationDropDownBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.ODropDownDataModel
import com.oreo.ui.internal.ODropDownAdapter
import com.oreo.ui.internal.OHMInternalViewModel
import dagger.hilt.android.AndroidEntryPoint

const val SLEEP_DROP_DOWN_ITEM = "SLEEP_DROP_DOWN_ITEM"

@AndroidEntryPoint
class ODropDownFragment :
    BaseFragment<FragmentODropDownBinding>(FragmentODropDownBinding::inflate) {
    private val viewModel: OHMInternalViewModel by viewModels()
    var alert: AlertDialog? = null
    val mDDAdapter by lazy {
        ODropDownAdapter(object : ODropDownAdapter.ODDItemClickListener {
            override fun onItemClick(resultData: ODropDownDataModel, position: Int) {
                if (alert != null)
                    alert?.dismiss()
                LOGS.d("Clicked Item ${resultData.title}")
                setFragmentResult(
                    SLEEP_DROP_DOWN_ITEM,
                    bundleOf("itemName" to resultData.title)
                )
                navigateUpSafe()

            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sleepDurationDropDown()

    }

    override fun initListener() {

    }

    override fun subscribeObservers() {
    }

    private fun sleepDurationDropDown() {
        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
        val layoutAlertBinding: LayoutSleepDurationDropDownBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.layout_sleep_duration_drop_down,
            null,
            false
        )

        with(layoutAlertBinding.lytContentView.rvDropdown) {
            adapter = mDDAdapter
        }
        defaultView(layoutAlertBinding)


        layoutAlertBinding.lytContentView.lytTab1.root.setOnClickListener {
            updateView(layoutAlertBinding, 0)
        }
        layoutAlertBinding.lytContentView.lytTab2.root.setOnClickListener {
            updateView(layoutAlertBinding, 1)
        }
        layoutAlertBinding.lytContentView.lytTab3.root.setOnClickListener {
            updateView(layoutAlertBinding, 2)
        }
        builder.setView(layoutAlertBinding.root)
        builder.setCancelable(false)
        alert = builder.create()
        alert?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alert?.show()
    }

    private fun defaultView(
        layoutAlertBinding: LayoutSleepDurationDropDownBinding
    ) {
        mDDAdapter.setData(viewModel.fetchDropDownData().first)
        layoutAlertBinding.lytContentView.lytTab1.tvTitle.text = getString(R.string.text_sleep)
        layoutAlertBinding.lytContentView.lytTab1.tvTitle.setTextColor(
            ContextCompat.getColor(
                layoutAlertBinding.lytTopView.ivDropDown.context,
                R.color.white
            )
        )
        layoutAlertBinding.lytContentView.lytTab2.tvTitle.text = getString(R.string.text_activity)
        layoutAlertBinding.lytContentView.lytTab2.tvTitle.setTextColor(
            ContextCompat.getColor(
                layoutAlertBinding.lytTopView.ivDropDown.context,
                R.color.white_40
            )
        )
        layoutAlertBinding.lytContentView.lytTab3.tvTitle.text = getString(R.string.text_readiness)
        layoutAlertBinding.lytContentView.lytTab3.tvTitle.setTextColor(
            ContextCompat.getColor(
                layoutAlertBinding.lytTopView.ivDropDown.context,
                R.color.white_40
            )
        )
        layoutAlertBinding.lytContentView.lytTab1.divider1.setBackgroundColor(
            ContextCompat.getColor(
                layoutAlertBinding.lytTopView.ivDropDown.context,
                R.color.white
            )
        )
        layoutAlertBinding.lytContentView.lytTab2.divider1.setBackgroundColor(
            ContextCompat.getColor(
                layoutAlertBinding.lytTopView.ivDropDown.context,
                R.color.daytime_inactive_un_selected_color
            )
        )
        layoutAlertBinding.lytContentView.lytTab3.divider1.setBackgroundColor(
            ContextCompat.getColor(
                layoutAlertBinding.lytTopView.ivDropDown.context,
                R.color.daytime_inactive_un_selected_color
            )
        )

    }

    private fun updateView(layoutAlertBinding: LayoutSleepDurationDropDownBinding, type: Int) {

        when (type) {
            0 -> {
                mDDAdapter.setData(viewModel.fetchDropDownData().first)
                layoutAlertBinding.lytContentView.lytTab1.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.white
                    )
                )
                layoutAlertBinding.lytContentView.lytTab2.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )
                layoutAlertBinding.lytContentView.lytTab3.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )

                layoutAlertBinding.lytContentView.lytTab1.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_80
                    )
                )
                layoutAlertBinding.lytContentView.lytTab2.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.daytime_inactive_un_selected_color
                    )
                )
                layoutAlertBinding.lytContentView.lytTab3.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.daytime_inactive_un_selected_color
                    )
                )

            }

            1 -> {
                mDDAdapter.setData(viewModel.fetchDropDownData().second)
                layoutAlertBinding.lytContentView.lytTab1.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_40
                    )
                )
                layoutAlertBinding.lytContentView.lytTab2.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.white
                    )
                )
                layoutAlertBinding.lytContentView.lytTab3.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )
                layoutAlertBinding.lytContentView.lytTab1.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.daytime_inactive_un_selected_color
                    )
                )
                layoutAlertBinding.lytContentView.lytTab2.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_80
                    )
                )
                layoutAlertBinding.lytContentView.lytTab3.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.daytime_inactive_un_selected_color
                    )
                )
            }

            else -> {
                mDDAdapter.setData(viewModel.fetchDropDownData().third)
                layoutAlertBinding.lytContentView.lytTab1.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_40
                    )
                )
                layoutAlertBinding.lytContentView.lytTab2.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )
                layoutAlertBinding.lytContentView.lytTab3.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.white
                    )
                )
                layoutAlertBinding.lytContentView.lytTab1.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.daytime_inactive_un_selected_color
                    )
                )
                layoutAlertBinding.lytContentView.lytTab2.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        R.color.daytime_inactive_un_selected_color
                    )
                )
                layoutAlertBinding.lytContentView.lytTab3.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        layoutAlertBinding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_80
                    )
                )
            }
        }


    }


}