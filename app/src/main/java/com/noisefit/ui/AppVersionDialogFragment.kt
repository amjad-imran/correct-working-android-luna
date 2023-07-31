package com.noisefit.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.noisefit.R
import com.noisefit_commans.data.response.VersionCheckResponse
import com.noisefit.databinding.FragmentAppVersionDialogBinding
import com.noisefit_commans.ui.loadImage
import com.noisefit_commans.ui.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppVersionDialogFragment : DialogFragment() {

    private var listener: OnInteractionListener? = null
    lateinit var binding: FragmentAppVersionDialogBinding
    private var versionCheckResponse: VersionCheckResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            versionCheckResponse = bundle.getParcelable(VersionData)
        }
    }

    fun setOnInteractionListener(onInteractionListener: OnInteractionListener) {
        this.listener = onInteractionListener
    }

    interface OnInteractionListener {
        fun onAppExit()
        fun onAppUpdate()
        fun onAppContinue(version: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppVersionDialogBinding.inflate(inflater, container, false)
        /*
        * this code is used to set background transparent
        * inset is used for set margin for DialogFragment view
        * */
        val back = ColorDrawable(Color.TRANSPARENT)
        val inset = InsetDrawable(back, 24, 0, 24, 0)
        dialog?.window?.setBackgroundDrawable(inset)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (versionCheckResponse?.maintenanceMode == true) {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }else{
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        handleAppVersion(versionCheckResponse!!)
    }

    private fun handleAppVersion(versionCheckResponse: VersionCheckResponse) {
        if (versionCheckResponse.maintenanceMode == true) {
            loadAppUnderMaintenanceView()
            return
        }
        var isForceUpdate = false
        binding.imageview.loadImage(requireContext(), R.drawable.ic_update_app)
        binding.tvToolbarText.text = getString(R.string.text_update_app)
        binding.titleTv.text = getString(R.string.text_update_app)
        binding.subTitleFirstTv.text = getString(R.string.text_new_version_of_the_app_is_available)

        var description = versionCheckResponse.description
        if (description.isNullOrEmpty()) {
            description = getString(R.string.text_update_now_to_enjoy_new_features)
        }
        binding.bUpdate.visible()
        binding.bUpdate.setOnClickListener {
            listener?.onAppUpdate()
        }

        binding.subTitleSecondTv.text = description
        if (versionCheckResponse.upgradeType == "force_upgrade") {
            isForceUpdate = true

        }

        binding.clearBtn.setOnClickListener {
            if (isForceUpdate) {
                listener?.onAppExit()
            } else {
                listener?.onAppContinue(versionCheckResponse.currentVersion ?: 0)
            }

        }

    }


    private fun loadAppUnderMaintenanceView() {
        binding.imageview.loadImage(requireContext(), R.drawable.ic_maintenance)
        binding.tvToolbarText.text = getString(R.string.text_app_under_maintenance)
        binding.titleTv.text = getString(R.string.text_app_under_maintenance)
        binding.subTitleFirstTv.text =
            getString(R.string.text_noisefit_is_currently_under_maintenance)
        binding.subTitleSecondTv.text = getString(R.string.text_please_check_back_again_later)
        binding.clearBtn.setOnClickListener {
            listener?.onAppExit()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.setCancelable(false)
    }

    companion object {
        private const val VersionData = "VERSION_DATA"

        @JvmStatic
        fun newInstance(versionCheckResponse: VersionCheckResponse) =
            AppVersionDialogFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(VersionData, versionCheckResponse)
                }
            }
    }

}