package com.oreo.ui.chatGpt.summary

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.noisefit.luna.databinding.AiShareTemplate1Binding
import com.noisefit.luna.databinding.FragmentShareSummaryBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.dpToPixel
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.utils.CommonConstants.FILE_PROVIDER
import com.noisefit_commans.utils.ImageUtil
import com.oreo.data.model.AiDailySummaryModel
import com.oreo.data.model.DataMetrics
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt


@AndroidEntryPoint
class ShareSummaryFragment :
    BaseFragment<FragmentShareSummaryBinding>(FragmentShareSummaryBinding::inflate) {

    private val args: ShareSummaryFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI(args.data, false)

    }

    private fun initUI(data: AiDailySummaryModel, privateMode: Boolean) {
        binding.lytTemplate.tvTitle.text = data.title
        binding.lytTemplate.tvSubtext.text = data.subTitle

        binding.ivBgImage.loadImage(binding.ivBgImage.context, data.bgImage)

        binding.lytTemplate.ivBack.loadImage(binding.lytTemplate.ivBack.context, data.bgImage)
        if (privateMode) {
            setRecycler(ArrayList())
        } else {
            setRecycler(data.metrics)
        }
    }

    private fun setRecycler(metrics: List<DataMetrics>?) {
        binding.lytTemplate.rvDataMetrics.layoutManager = LinearLayoutManager(requireContext())
        binding.lytTemplate.rvDataMetrics.adapter = DataMetricsAdapter().apply {
            this.setDataSet(metrics ?: ArrayList())
        }
    }

    override fun initListener() {
        binding.switchPrivate.setOnCheckedChangeListener { buttonView, isChecked ->
            initUI(args.data, isChecked)
        }

        binding.tvShare.setOnClickListener {
            context?.let {
                val bitmap = ImageUtil.getBitmapFromView(binding.lytTemplate.root)
                bitmap ?: return@setOnClickListener

                val file = saveBitmapToFile(it, bitmap)
                shareImage(it, file)
            }
        }
    }

    override fun subscribeObservers() {

    }

    private fun createImageFromLayout(view: View): Bitmap {
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }

    //Move to BG thread
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.getExternalFilesDir(null), "ai_shared.png")
        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private fun shareImage(context: Context, imageFile: File) {
        val uri = FileProvider.getUriForFile(
            context, FILE_PROVIDER,
            imageFile
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("image/png")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, "Share Daily Health Digest"))
    }

}