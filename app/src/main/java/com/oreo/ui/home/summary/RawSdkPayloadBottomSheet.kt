package com.oreo.ui.home.summary

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.noisefit.luna.databinding.BottomSheetSdkRawPayloadBinding
import com.noisefit_commans.common.copyToClipBoard
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.ui.BaseBottomSheetWithTransparent
import com.noisefit_commans.ui.showShortToast
import com.noisefit_commans.utils.share.ShareUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class RawSdkPayloadBottomSheet :
    BaseBottomSheetWithTransparent<BottomSheetSdkRawPayloadBinding>(
        BottomSheetSdkRawPayloadBinding::inflate
    ) {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_PAYLOAD_TYPE = "arg_payload_type"
        private const val PAGE_SIZE = 3500

        const val TYPE_CONTINUOUS_HEART_RATE = "continuous_heart_rate"
        const val TYPE_CONTINUOUS_PRESSURE = "continuous_pressure"
        const val TYPE_SLEEP_RRI = "sleep_rri"
        const val TYPE_SLEEP_HRV = "sleep_hrv"
        const val TYPE_CONTINUOUS_RRI = "continuous_rri"
        const val TYPE_SPORT_HEART_RATE_AFTER = "sport_heart_rate_after"
        const val TYPE_DEV_SPORT = "dev_sport"

        fun newInstance(title: String, payloadType: String): RawSdkPayloadBottomSheet {
            return RawSdkPayloadBottomSheet().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_PAYLOAD_TYPE to payloadType
                )
            }
        }
    }

    @Inject
    lateinit var watchDataStore: WatchDataStore

    private val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    private var loadSnapshotJob: Job? = null
    private var currentPageIndex = 0
    private var fullSnapshotText = "No payload captured yet."
    private var snapshotPages: List<String> = listOf(fullSnapshotText)
    private var hasPayload = false

    override fun initListener() {
        binding.tvClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.tvRefreshSnapshot.setOnClickListener {
            loadSnapshot()
        }
        binding.tvPrevious.setOnClickListener {
            if (currentPageIndex > 0) {
                currentPageIndex--
                bindCurrentPage()
            }
        }
        binding.tvNext.setOnClickListener {
            if (currentPageIndex < snapshotPages.lastIndex) {
                currentPageIndex++
                bindCurrentPage()
            }
        }
        binding.tvCopyFull.setOnClickListener {
            if (!hasPayload) {
                context.showShortToast("No payload captured yet.")
                return@setOnClickListener
            }
            fullSnapshotText.copyToClipBoard("Copied raw payload")
        }
        binding.tvShareFull.setOnClickListener {
            if (!hasPayload) {
                context.showShortToast("No payload captured yet.")
                return@setOnClickListener
            }
            ShareUtil.shareText(requireContext(), fullSnapshotText)
        }
    }

    override fun subscribeObservers() = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = requireArguments().getString(ARG_TITLE).orEmpty()
        loadSnapshot()
    }

    override fun onDestroyView() {
        loadSnapshotJob?.cancel()
        loadSnapshotJob = null
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dia ->
            val dialog = dia as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isHideable = true
                isDraggable = true
            }
            bottomSheet.setBackgroundResource(android.R.color.transparent)
        }
        return bottomSheetDialog
    }

    private fun loadSnapshot() {
        loadSnapshotJob?.cancel()
        loadSnapshotJob = viewLifecycleOwner.lifecycleScope.launch {
            binding.progressLoading.isVisible = true
            val payloadType = requireArguments().getString(ARG_PAYLOAD_TYPE).orEmpty()
            val snapshot = withContext(Dispatchers.Default) {
                buildSnapshot(payloadType)
            }

            hasPayload = snapshot.hasPayload
            fullSnapshotText = snapshot.formattedText
            snapshotPages = snapshot.pages
            currentPageIndex = 0

            binding.tvSnapshotStatus.text = snapshot.statusText
            binding.tvPayloadSize.text = snapshot.sizeText
            bindCurrentPage()
            binding.progressLoading.isVisible = false
        }
    }

    private fun bindCurrentPage() {
        val currentPage = snapshotPages.getOrElse(currentPageIndex) { "No payload captured yet." }
        binding.tvRawContent.text = currentPage
        binding.tvPageIndicator.text = "Page ${currentPageIndex + 1} / ${snapshotPages.size}"
        binding.tvPrevious.isEnabled = currentPageIndex > 0
        binding.tvPrevious.alpha = if (binding.tvPrevious.isEnabled) 1f else 0.45f
        binding.tvNext.isEnabled = currentPageIndex < snapshotPages.lastIndex
        binding.tvNext.alpha = if (binding.tvNext.isEnabled) 1f else 0.45f
        binding.scrollRawContent.scrollTo(0, 0)
    }

    private fun buildSnapshot(payloadType: String): RawSnapshot {
        val rawPayload = when (payloadType) {
            TYPE_CONTINUOUS_HEART_RATE -> watchDataStore.testGetRawContinuousHeartRateJson()
            TYPE_CONTINUOUS_PRESSURE -> watchDataStore.testGetRawContinuousPressureJson()
            TYPE_SLEEP_RRI -> watchDataStore.testGetRawSleepRriJson()
            TYPE_SLEEP_HRV -> watchDataStore.testGetRawSleepHrvJson()
            TYPE_CONTINUOUS_RRI -> watchDataStore.testGetRawContinuousRriJson()
            TYPE_SPORT_HEART_RATE_AFTER -> watchDataStore.testGetRawSportHeartRateAfterJson()
            TYPE_DEV_SPORT -> watchDataStore.testGetRawDevSportJson()
            else -> null
        }

        if (rawPayload.isNullOrBlank()) {
            val emptyText = "No payload captured yet."
            return RawSnapshot(
                hasPayload = false,
                formattedText = emptyText,
                pages = listOf(emptyText),
                statusText = "Latest captured payload",
                sizeText = "0 chars"
            )
        }

        val formatted = try {
            Gson().newBuilder().setPrettyPrinting().create()
                .toJson(JsonParser.parseString(rawPayload))
        } catch (_: Exception) {
            rawPayload
        }

        return RawSnapshot(
            hasPayload = true,
            formattedText = formatted,
            pages = splitIntoPages(formatted, PAGE_SIZE),
            statusText = "Snapshot refreshed at ${timeFormat.format(Date())}",
            sizeText = "${rawPayload.length} raw chars | ${formatted.length} display chars"
        )
    }

    private fun splitIntoPages(text: String, pageSize: Int): List<String> {
        if (text.length <= pageSize) {
            return listOf(text)
        }

        val pages = ArrayList<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + pageSize, text.length)
            var splitIndex = text.lastIndexOf('\n', end - 1)
            if (splitIndex < start + (pageSize / 2)) {
                splitIndex = end
            } else {
                splitIndex += 1
            }
            pages.add(text.substring(start, splitIndex))
            start = splitIndex
        }
        return pages
    }

    private data class RawSnapshot(
        val hasPayload: Boolean,
        val formattedText: String,
        val pages: List<String>,
        val statusText: String,
        val sizeText: String
    )
}
