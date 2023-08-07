package com.noisefit.ui.common.location

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.luna.R
import com.noisefit.luna.databinding.FragmentLocationBottomSheetBinding
import com.noisefit.ui.friends.location.search.SearchStateType
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.disable
import com.noisefit_commans.ui.enable
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.AndroidEntryPoint


const val SAVE_LOCATION_REQUEST_KEY = "SAVE_LOCATION_REQUEST_KEY"


@AndroidEntryPoint
class LocationBottomSheet :
    BaseBottomSheetWithTransparent<FragmentLocationBottomSheetBinding>(
        FragmentLocationBottomSheetBinding::inflate
    ) {

    private val viewModel: LocationBottomSheetViewModel by viewModels()

    private val args: LocationBottomSheetArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        updateState()
        updateCity()

    }

    private fun updateState() {

        if (viewModel.userLocation.state.isNullOrEmpty()) {
            binding.etEnterCity.disable()
        } else {
            binding.etEnterCity.enable()
            binding.etEnterState.text = viewModel.userLocation.state
        }

    }

    private fun updateCity() {
        viewModel.userLocation.city?.let {
            binding.etEnterCity.text = it
        }

    }


    override fun initListener() {


        binding.etEnterCity.setOnClickListener {

            navigate(
                LocationBottomSheetDirections.actionLocationBottomSheetToSearchStateFragment(
                    SearchStateType.City,
                    viewModel.userLocation.city
                ).apply {
                    this.id = viewModel.userLocation.stateId!!
                }
            )
        }


        binding.etEnterState.setOnClickListener {

            navigate(
                LocationBottomSheetDirections.actionLocationBottomSheetToSearchStateFragment(
                    SearchStateType.State,
                    viewModel.userLocation.state
                ).apply {
                    this.id = viewModel.userLocation.stateId ?: -1
                }
            )
        }

        binding.btnSave.setOnClickListener {

            val selectedCity = binding.etEnterCity.text.toString()
            val selectedState = binding.etEnterState.text.toString()

            if (selectedCity.isEmpty()) {
                context.showShortToast(getString(R.string.text_enter_your_city))
                return@setOnClickListener
            }

            if (selectedState.isEmpty()) {
                context.showShortToast(getString(R.string.text_enter_your_state))
                return@setOnClickListener
            }

            if (args.shouldUpdate) {
                viewModel.updateUserLocation()
            } else {
                navigateUpSafe()
                requireActivity().supportFragmentManager.setFragmentResult(
                    SAVE_LOCATION_REQUEST_KEY,
                    bundleOf(
                        "save" to false,
                        "city" to viewModel.userLocation.city,
                        "cityId" to viewModel.userLocation.cityId,
                        "stateId" to viewModel.userLocation.stateId
                    )
                )
            }
            viewModel.sessionManager.tempUserLocation = null
        }

        viewModel.getLoading().observe(this) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }

        viewModel.getMessages().observe(this) {
            it.getContent()?.let { message ->
                context.showShortToast(message)
            }
        }

        binding.btnCancel.setOnClickListener {
            viewModel.sessionManager.tempUserLocation = null
            navigateUpSafe()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun subscribeObservers() {

        viewModel.userLocationUpdateResult.observe(this) {
            it?.let {
                viewModel.sessionManager.tempUserLocation = null
                navigateUpSafe()
                requireActivity().supportFragmentManager.setFragmentResult(
                    SAVE_LOCATION_REQUEST_KEY,
                    bundleOf("save" to true)
                )
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
                isCancelable = false
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

}