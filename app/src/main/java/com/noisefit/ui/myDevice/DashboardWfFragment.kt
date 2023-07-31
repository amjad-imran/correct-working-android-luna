package com.noisefit.ui.myDevice

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.databinding.FragmentWfDashboardBinding
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.loadImageCacheWithProgress
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardWfFragment :
    BaseFragment<FragmentWfDashboardBinding>(FragmentWfDashboardBinding::inflate) {


    private var dashboardWfListener: DashboardWfListener? = null
    private var wfList: ArrayList<Watchface2>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (!wfList.isNullOrEmpty()) {
            when (wfList!!.size) {
                1 -> {
                    loadImage(binding.ivWatchFace1, wfList!![0].imageUrl)
                }

                2 -> {
                    loadImage(binding.ivWatchFace1, wfList!![0].imageUrl)
                    loadImage(binding.ivWatchFace2, wfList!![1].imageUrl)

                }

                3 -> {
                    loadImage(binding.ivWatchFace1, wfList!![0].imageUrl)
                    loadImage(binding.ivWatchFace2, wfList!![1].imageUrl)
                    loadImage(binding.ivWatchFace3, wfList!![2].imageUrl)
                }
            }
        }
    }

    private fun loadImage(imageView: ImageView, url: String) {
        imageView.loadImageCacheWithProgress(requireActivity(), url)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initListener() {

//        binding.ivWatchFace1.setOnTouchListener { _, _ ->
//            dashboardWfListener?.onStopScroll()
//            return@setOnTouchListener true
//        }
//        binding.ivWatchFace2.setOnTouchListener { _, _ ->
//            dashboardWfListener?.onStopScroll()
//            return@setOnTouchListener true
//        }
//        binding.ivWatchFace3.setOnTouchListener { _, _ ->
//
//            dashboardWfListener?.onStopScroll()
//            return@setOnTouchListener true
//        }
        binding.ivWatchFace1.setOnClickListener {
            if (wfList.isNullOrEmpty() || wfList!!.getOrNull(0) == null) {
                return@setOnClickListener
            }
            dashboardWfListener?.onWatchfaceClicked(wfList!![0])
        }
        binding.ivWatchFace2.setOnClickListener {
            if (wfList.isNullOrEmpty() || wfList!!.getOrNull(1) == null) {
                return@setOnClickListener
            }
            dashboardWfListener?.onWatchfaceClicked(wfList!![1])
        }
        binding.ivWatchFace3.setOnClickListener {
            if (wfList.isNullOrEmpty() || wfList!!.getOrNull(2) == null) {
                return@setOnClickListener
            }
            dashboardWfListener?.onWatchfaceClicked(wfList!![2])
        }
    }

    override fun subscribeObservers() {

    }

    companion object {
        @JvmStatic
        fun newInstance(listWatchFace: ArrayList<Watchface2>) =
            DashboardWfFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("wf", listWatchFace)

                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            wfList = it.getParcelableArrayList<Watchface2>("wf")?.let { it1 -> ArrayList(it1) }
        }
    }


    fun setDashboardWfListener(dashboardWfListener: DashboardWfListener) {
        this.dashboardWfListener = dashboardWfListener
    }
}

interface DashboardWfListener {
    fun onWatchfaceClicked(watchface2: Watchface2)
    fun onStopScroll()
}