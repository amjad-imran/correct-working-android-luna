package com.oreo.ui.sleep2

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentODropDownBinding
import com.noisefit_commans.ui.BaseFragment
import com.oreo.ui.internal.ODropDownAdapter
import com.oreo.ui.sleep2.internal.SleepInternalLaunchState
import dagger.hilt.android.AndroidEntryPoint

const val SLEEP_DROP_DOWN_ITEM = "SLEEP_DROP_DOWN_ITEM"

@AndroidEntryPoint
class ODropDownFragment :
    BaseFragment<FragmentODropDownBinding>(FragmentODropDownBinding::inflate) {
    private val viewModel: ODropDownViewModel by viewModels()
    private val args: ODropDownFragmentArgs by navArgs()
    var alert: AlertDialog? = null
    val mDDAdapter by lazy {
        ODropDownAdapter(object : ODropDownAdapter.ODDItemClickListener {
            override fun onItemClick(resultData: SleepInternalLaunchState, position: Int) {
                if (alert != null)
                    alert?.dismiss()
                setFragmentResult(
                    SLEEP_DROP_DOWN_ITEM,
                    bundleOf("itemName" to resultData)
                )
                navigateUpSafe()

            }
        })
    }

    companion object {
        fun getStartData(launchMode: SleepInternalLaunchState): Pair<Int, Bundle?> {
            return Pair(R.id.dropDownFragment, Bundle().apply {
                putSerializable("launchMode", launchMode)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedLaunchMode = args.launchMode
        initUi()


    }

    private fun initUi() {
        binding.lytTopView.tvType.text = viewModel.getTitle().first
        binding.lytTopView.ivType.setImageResource(viewModel.getTitle().second)

        with(binding.lytContentView.rvDropdown) {
            adapter = mDDAdapter
        }
        defaultView()
    }

    override fun initListener() {

        binding.lytContentView.lytTab1.root.setOnClickListener {
            updateView( 0)
        }
        binding.lytContentView.lytTab2.root.setOnClickListener {
            updateView( 1)
        }
        binding.lytContentView.lytTab3.root.setOnClickListener {
            updateView( 2)
        }
        binding.main.setOnClickListener {
            navigateUpSafe()
        }


    }

    override fun subscribeObservers() {
    }



    private fun defaultView(

    ) {
        mDDAdapter.setData(viewModel.fetchDropDownData().first, viewModel.selectedLaunchMode)
        binding.lytContentView.lytTab1.tvTitle.text = getString(R.string.text_sleep)
        binding.lytContentView.lytTab1.tvTitle.setTextColor(
            ContextCompat.getColor(
                binding.lytTopView.ivDropDown.context,
                R.color.white
            )
        )
        binding.lytContentView.lytTab2.tvTitle.text = getString(R.string.text_activity)
        binding.lytContentView.lytTab2.tvTitle.setTextColor(
            ContextCompat.getColor(
                binding.lytTopView.ivDropDown.context,
                R.color.white_40
            )
        )
        binding.lytContentView.lytTab3.tvTitle.text = getString(R.string.text_readiness)
        binding.lytContentView.lytTab3.tvTitle.setTextColor(
            ContextCompat.getColor(
                binding.lytTopView.ivDropDown.context,
                R.color.white_40
            )
        )
        binding.lytContentView.lytTab1.divider1.setBackgroundColor(
            ContextCompat.getColor(
                binding.lytTopView.ivDropDown.context,
                R.color.white
            )
        )
        binding.lytContentView.lytTab2.divider1.setBackgroundColor(
            ContextCompat.getColor(
                binding.lytTopView.ivDropDown.context,
                R.color.color_dd_separator
            )
        )
        binding.lytContentView.lytTab3.divider1.setBackgroundColor(
            ContextCompat.getColor(
                binding.lytTopView.ivDropDown.context,
                R.color.color_dd_separator
            )
        )

    }

    private fun updateView( type: Int) {

        when (type) {
            0 -> {
                mDDAdapter.setData(viewModel.fetchDropDownData().first, viewModel.selectedLaunchMode)
                binding.lytContentView.lytTab1.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.white
                    )
                )
                binding.lytContentView.lytTab2.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )
                binding.lytContentView.lytTab3.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )

                binding.lytContentView.lytTab1.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_80
                    )
                )
                binding.lytContentView.lytTab2.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.color_dd_separator
                    )
                )
                binding.lytContentView.lytTab3.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.color_dd_separator
                    )
                )

            }

            1 -> {
                mDDAdapter.setData(
                    viewModel.fetchDropDownData().first,
                    viewModel.selectedLaunchMode
                )
                binding.lytContentView.lytTab1.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_40
                    )
                )
                binding.lytContentView.lytTab2.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.white
                    )
                )
                binding.lytContentView.lytTab3.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )
                binding.lytContentView.lytTab1.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.color_dd_separator
                    )
                )
                binding.lytContentView.lytTab2.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_80
                    )
                )
                binding.lytContentView.lytTab3.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.color_dd_separator
                    )
                )
            }

            else -> {
                mDDAdapter.setData(
                    viewModel.fetchDropDownData().first,
                    viewModel.selectedLaunchMode
                )
                binding.lytContentView.lytTab1.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_40
                    )
                )
                binding.lytContentView.lytTab2.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.white_40
                    )
                )
                binding.lytContentView.lytTab3.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.white
                    )
                )
                binding.lytContentView.lytTab1.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.color_dd_separator
                    )
                )
                binding.lytContentView.lytTab2.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        R.color.color_dd_separator
                    )
                )
                binding.lytContentView.lytTab3.divider1.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.lytTopView.ivDropDown.context,
                        com.noisefit_commans.R.color.white_80
                    )
                )
            }
        }


    }


}