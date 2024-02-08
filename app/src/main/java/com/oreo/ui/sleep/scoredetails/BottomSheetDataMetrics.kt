package com.oreo.ui.sleep.scoredetails

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.navigation.fragment.navArgs
import com.noisefit.luna.databinding.BottomSheetDataMetricsBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetDataMetrics : BaseBottomSheetWithTransparent<BottomSheetDataMetricsBinding>(
    BottomSheetDataMetricsBinding::inflate
) {
    val args: BottomSheetDataMetricsArgs by navArgs()
    private var type: String? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvData.text = Html.fromHtml(args.infoData)
        if (arguments?.containsKey("type") == true) {
            type = args.type
        }



        handleView()

    }

    private fun handleView() {
        when (type) {
            ViewItemClickType.BODY_TEMPERATURE.name -> {
                binding.groupMain.visible()
            }

            else -> {

            }
        }
    }

    override fun initListener() {


    }

    override fun subscribeObservers() {

    }
}