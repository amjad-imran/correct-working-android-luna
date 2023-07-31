package com.noisefit.ui.trophies

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.HandlerCompat
import androidx.fragment.app.activityViewModels
import com.noisefit.R
import com.noisefit_commans.data.model.trophies.DailyItem
import com.noisefit.databinding.FragmentTrophiesDetailsBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseFragment
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.Presets
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class TrophiesDetailsFragment :
    BaseFragment<FragmentTrophiesDetailsBinding>(FragmentTrophiesDetailsBinding::inflate) {
    var trophyData: DailyItem? = null
    var type: TrophiesType? = null
    private val viewModel: TrophiesViewModel by activityViewModels()
    var shareBitmap: Bitmap? = null
    var shareUri: Uri? = null
    private var isAlreadyShared = false

    @Inject
    lateinit var sessionManager: SessionManager

    override fun initListener() {

    }

    override fun subscribeObservers() {
        binding.backBtn.setOnClickListener {
            navigateUpSafe()
        }
        viewModel.badgeCollected.observe(viewLifecycleOwner) {
            it.getContent()?.let {

                if (type == TrophiesType.STEPS) {
                    trophyData?.isStepsCollect = 1
                } else {
                    trophyData?.isDistanceCollect = 1
                }
                binding.konfettiView.start(Presets.festive())
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateUpSafe()
                    if (nullableBinding != null) {
                        loadTrophyData()
                    }
                }, 1000)

            }

        }
        viewModel.getLoading().observe(viewLifecycleOwner) {
            if (it) {
                binding.progressBar.root.visible()
            } else {
                binding.progressBar.root.gone()
            }
        }
        viewModel.getApiErrors().observe(viewLifecycleOwner) {
            it?.getContent()?.let { response ->
                uiController.onApiErrorReceived(response)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, callback)
        initializeViews()

    }

    private fun initializeViews() {


        trophyData = arguments?.let {
            TrophyCollectBottomSheetArgs.fromBundle(it).trophyData
        }
        type = arguments?.let {
            TrophyCollectBottomSheetArgs.fromBundle(it).type
        }
        binding.textViewTitle.text = trophyData?.badgeType?.uppercase()

        binding.imageViewShare.setOnClickListener {
            shareImageSocial()
        }
        binding.btnCollect.setOnClickListener {
            trophyData?.let {
                if (type == TrophiesType.STEPS) {
                    viewModel.markBadgeCollected(it.stepsUserBadgeId)


                } else {
                    viewModel.markBadgeCollected(it.distanceUserBadgeId)

                }
            }
        }
        hideViews()
        loadTrophyData()
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (type == TrophiesType.STEPS) {
                    if (trophyData?.isStepsCollect == 1) {
                        navigateUpSafe()
                    }
                } else {
                    if (trophyData?.isDistanceCollect == 1) {
                        navigateUpSafe()
                    }
                }
            }
        }

    private fun hideViews() {
        if (type == TrophiesType.STEPS) {
            if (trophyData?.isStepsCollect == 0) {
                binding.backBtn.visibility = View.GONE
                binding.imageViewShare.visibility = View.GONE
                binding.btnCollect.visibility = View.VISIBLE
            } else {
                binding.backBtn.visibility = View.VISIBLE
                binding.imageViewShare.visibility = View.VISIBLE
                binding.btnCollect.visibility = View.GONE
            }
        } else {
            if (trophyData?.isDistanceCollect == 0) {
                binding.backBtn.visibility = View.GONE
                binding.imageViewShare.visibility = View.GONE
                binding.btnCollect.visibility = View.VISIBLE
            } else {
                binding.backBtn.visibility = View.VISIBLE
                binding.imageViewShare.visibility = View.VISIBLE
                binding.btnCollect.visibility = View.GONE
            }
        }
    }

    private fun shareImageSocial() {
        if (!isAlreadyShared) {
            isAlreadyShared = true
        }
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
        val view = binding.layoutShare
        executorService.execute {
            mainThreadHandler.post {
                binding.progressBar.root.visible()
            }
            if (shareBitmap == null) {
                shareBitmap = ImageUtil.getBitmapFromView(view)
            }
            shareBitmap?.let {
                if (shareUri == null) {
                    shareUri = ImageUtil.getTempImageUri(it, requireContext())
                }
                mainThreadHandler.post {
                    binding.progressBar.root.gone()

                    ShareUtil.shareOthers(requireContext(), shareUri)
                }


            }
            mainThreadHandler.post {
                binding.progressBar.root.gone()
            }
        }
    }

    private fun loadTrophyData() {
        trophyData?.let {
            if (it.badgeType.equals("daily", true)) {
                if (type == TrophiesType.STEPS) {
                    binding.imageView23.setImageResource(R.drawable.ic_steps_milestones)
                    binding.materialTextView2.text = getString(R.string.text_its_a_badge)
                    binding.materialTextView.text =
                        "You have just completed ${it.steps} Steps of Milestone, please collect your Trophy"
                } else {
                    binding.imageView23.setImageResource(R.drawable.ic_distance_milestones)
                    val distance = if (viewModel.getUnitValue() == Units.METRIC) {
                        it.titleForKm
                    } else {
                        it.titleForMile
                    }
                    binding.materialTextView2.text = getString(R.string.text_its_a_badge)
                    binding.materialTextView.text =
                        "You have just completed $distance of Milestone, please collect your Trophy"
                }
            } else if (it.badgeType.equals("lifetime", true)) {
                if (type == TrophiesType.STEPS) {
                    binding.imageView23.setImageResource(R.drawable.ic_steps_lifetime)
                    binding.materialTextView2.text = getString(R.string.text_its_a_badge)
                    binding.materialTextView.text =
                        "You have just completed ${it.steps} Steps of Badge, please collect your Trophy"
                } else {
                    binding.imageView23.setImageResource(R.drawable.ic_distance_lifettime)

                    val distance = if (viewModel.getUnitValue() == Units.METRIC) {
                        it.titleForKm
                    } else {
                        it.titleForMile
                    }
                    binding.materialTextView2.text = getString(R.string.text_its_a_badge)
                    binding.materialTextView.text =
                        "You have just completed $distance of Badge, please collect your Trophy"
                }
            }


        }
    }


}