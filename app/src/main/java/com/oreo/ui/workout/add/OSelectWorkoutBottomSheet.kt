package com.oreo.ui.workout.add

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.noisefit.databinding.FragmentOSelectWorkoutBottomSheetBinding
import com.noisefit.ui.common.bottomSheet.ALERT_REQUEST_KEY
import com.noisefit.ui.common.bottomSheet.AlertTextBottomSheetArgs
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.oreo.data.model.OWorkoutListModal
import dagger.hilt.android.AndroidEntryPoint

const val SELECT_REQUEST_KEY = "SELECT_REQUEST_KEY"
@AndroidEntryPoint
class OSelectWorkoutBottomSheet :
    BaseBottomSheetWithTransparent<FragmentOSelectWorkoutBottomSheetBinding>(
        FragmentOSelectWorkoutBottomSheetBinding::inflate
    ) {


    private var workoutList = ArrayList<OWorkoutListModal>()

    private val selectWorkoutAdapter by lazy {
        OSelectWorkoutAdapter(object :OSelectWorkoutAdapter.OSelectWorkoutInteraction{
            override fun onWorkoutSelected(oWorkoutListModal: OWorkoutListModal) {
                setFragmentResult(
                    SELECT_REQUEST_KEY,
                    bundleOf("workout" to oWorkoutListModal)
                )
                navigateUpSafe()

            }

        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            workoutList.addAll(OSelectWorkoutBottomSheetArgs.fromBundle(it).workoutList)
        }

        setRecycler()

    }

    override fun initListener() {


        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                selectWorkoutAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}

        })


    }

    private fun setRecycler() {
        with(binding.rvSelectWorkout) {
            adapter = selectWorkoutAdapter
        }
        selectWorkoutAdapter.setData(workoutList)

    }

    override fun subscribeObservers() {

    }


}