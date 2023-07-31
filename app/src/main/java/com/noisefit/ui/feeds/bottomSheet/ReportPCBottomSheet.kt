package com.noisefit.ui.feeds.bottomSheet

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.R
import com.noisefit.data.model.ReportAbuseData
import com.noisefit.databinding.FragmentReportPCBottomSheetBinding
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.InsiderAppEvents
import dagger.hilt.android.AndroidEntryPoint

const val REPORT_PC_REQUEST_KEY = "REPORT_PC_REQUEST_KEY"

@AndroidEntryPoint
class ReportPCBottomSheet : BaseBottomSheetWithTransparent<FragmentReportPCBottomSheetBinding>(
    FragmentReportPCBottomSheetBinding::inflate
) {

    private val args: ReportPCBottomSheetArgs by navArgs()
    private val viewModel: FeedBottomSheetViewModel by viewModels()
    private val reportPCAdapter by lazy {
        ReportPCAdapter(object : OnRepostAbuseItemClickListener {
            override fun onItemClick(data: ReportAbuseData) {
                if (viewModel.comeFrom.equals("feed"))
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.FEEDS_REPORTPOST_ + data.title + "_CLICK")
                else if (viewModel.comeFrom.equals("ut"))
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.UT_REPORTPOST_ + data.title + "_CLICK")
                else
                    viewModel.sessionManager.logInsiderAppEvent(InsiderAppEvents.UT_REPORTCOMMENT_ + data.title + "_CLICK")
                viewModel.reportAbusePOrC(data.title)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            viewModel.postId = args.postId
            viewModel.commentId = args.commentId
            viewModel.comeFrom = args.comeFrom
        }

        if (viewModel.commentId != -1L) {
            viewModel.isOpenFromComment = true
        }
        setAdapter()
    }

    private fun setAdapter() {
        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportPCAdapter
        }

    }

    override fun initListener() {
        binding.editContainer.setOnClickListener {
            binding.editContainer.gone()
            viewModel.getReportAbuseList()
        }
    }

    override fun subscribeObservers() {
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
        viewModel.getApiErrors().observe(this) {
            it.getContent()?.let { res ->
                context.showShortToast(getString(R.string.text_something_went_wrong))
            }
        }

        viewModel.serverStatus.observe(this) {
            it?.let {
                if (it) {
                    context.showShortToast("Reported successfully!!")
                    dismiss()
                    if (viewModel.isOpenFromComment) {

                        setFragmentResult(
                            REPORT_PC_REQUEST_KEY,
                            bundleOf(
                                "report" to true
                            )
                        )
                    } else {
                        requireActivity().supportFragmentManager.setFragmentResult(
                            REPORT_PC_REQUEST_KEY,
                            bundleOf(
                                "report" to true
                            )
                        )
                    }
                }
            }
        }
        viewModel.reportAbuseList.observe(this) {
            it?.let {

                reportPCAdapter.setDataSet(it)
            }
        }
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val bottomSheetDialog =
//            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
//        bottomSheetDialog.setOnShowListener { dia ->
//            val dialog = dia as BottomSheetDialog
//            val bottomSheet =
//                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//            BottomSheetBehavior.from<FrameLayout?>(bottomSheet!!).apply {
//                state = BottomSheetBehavior.STATE_EXPANDED
//                skipCollapsed = true
//                isHideable = true
//                isDraggable = true
//                isCancelable = false
//            }
//            bottomSheet.setBackgroundResource(android.R.color.transparent)
//        }
//        return bottomSheetDialog
//    }

}