package com.noisefit.ui.trophies

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.HandlerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.noisefit.R
import com.noisefit_commans.data.model.trophies.DailyItem
import com.noisefit.databinding.DialogTrophyCollectBinding
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.share.ShareUtil
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.ImageUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject


@AndroidEntryPoint
class TrophyCollectDialog : DialogFragment() {

    lateinit var binding: DialogTrophyCollectBinding
    var shareBitmap: Bitmap? = null
    var shareUri: Uri? = null

    var trophyData: DailyItem? = null
    var type: TrophiesType? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val viewModel: TrophiesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTrophyCollectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        trophyData = arguments?.let {
            TrophyCollectBottomSheetArgs.fromBundle(it).trophyData
        }
        type = arguments?.let {
            TrophyCollectBottomSheetArgs.fromBundle(it).type
        }
        setObservers()
        loadTrophyData()

        binding.ivClose.setOnClickListener {
            findNavController().navigateUp()
            viewModel.getNextTrophy()
        }

        binding.ivFacebook.setOnClickListener {

            shareImageSocial(1)
        }
        binding.ivInsta.setOnClickListener {

            shareImageSocial(2)
        }
        binding.ivWhatsapp.setOnClickListener {

            shareImageSocial(3)
        }
        binding.ivOthers.setOnClickListener {

            shareImageSocial(0)
        }
        binding.bCollect.setOnClickListener {
            if (binding.bCollect.alpha == 1f) {
                trophyData?.let {
                    if (type == TrophiesType.STEPS) {
                        viewModel.markBadgeCollected(it.stepsUserBadgeId)


                    } else {
                        viewModel.markBadgeCollected(it.distanceUserBadgeId)

                    }
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.badgeCollected.observe(this) {
            it.getContent()?.let {

                if (type == TrophiesType.STEPS) {
                    trophyData?.isStepsCollect = 1
                } else {
                    trophyData?.isDistanceCollect = 1
                }
                loadTrophyData()
            }

        }
    }

    private fun loadTrophyData() {
        trophyData?.let {

            if (it.badgeType.equals("daily", true)) {
                if (type == TrophiesType.STEPS) {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_steps_milestones)
                    if (it.isStepsCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.bCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.bCollect.text = getString(R.string.text_collected)
                        binding.bCollect.alpha = 0.6f
                        showShareOptions()
                    }
                    binding.badgeLayout.textView49.text = getString(R.string.text_is_a_milestone)
                    binding.badgeLayout.textView50.text =
                        "You have just completed ${it.steps} Steps of Milestone, please collect your Trophy"
                } else {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_distance_milestones)
                    if (it.isDistanceCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.bCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.bCollect.text = getString(R.string.text_collected)
                        binding.bCollect.alpha = 0.6f
                        showShareOptions()
                    }
                    val distance = if (viewModel.getUnitValue() == Units.METRIC) {
                        it.titleForKm
                    } else {
                        it.titleForMile
                    }

                    binding.badgeLayout.textView49.text = getString(R.string.text_is_a_milestone)
                    binding.badgeLayout.textView50.text =
                        "You have just completed $distance of Milestone, please collect your Trophy"
                }
            } else if (it.badgeType.equals("lifetime", true)) {
                if (type == TrophiesType.STEPS) {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_steps_lifetime)
                    if (it.isStepsCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.bCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.bCollect.text = getString(R.string.text_collected)
                        binding.bCollect.alpha = 0.6f
                        showShareOptions()
                    }
                    binding.badgeLayout.textView49.text = getString(R.string.text_its_a_badge)
                    binding.badgeLayout.textView50.text =
                        "You have just completed ${it.steps} Steps of Badge, please collect your Trophy"
                } else {
                    binding.badgeLayout.imageView23.setImageResource(R.drawable.ic_distance_lifettime)
                    if (it.isDistanceCollect == 0) {
                        binding.textHeader.text = getString(R.string.text_new_tropy)
                        binding.bCollect.text = getString(R.string.text_collect)
                    } else {
                        binding.textHeader.text = getString(R.string.text_share)
                        binding.bCollect.text = getString(R.string.text_collected)
                        binding.bCollect.alpha = 0.6f
                        showShareOptions()
                    }
                    val distance = if (viewModel.getUnitValue() == Units.METRIC) {
                        it.titleForKm
                    } else {
                        it.titleForMile
                    }
                    binding.badgeLayout.textView49.text = getString(R.string.text_its_a_badge)
                    binding.badgeLayout.textView50.text =
                        "You have just completed $distance of Badge, please collect your Trophy"
                }
            }


        }

    }

    private fun showShareOptions() {
        binding.view11.visible()
        binding.textView31.visible()
        binding.ivFacebook.visible()
        binding.ivInsta.visible()
        binding.ivWhatsapp.visible()
        binding.ivOthers.visible()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCancelable(false)
    }

    /**
     * @param shareOn 0->Default 1->Facebook 2->Insta 3->Whatsapp
     */
    fun shareImageSocial(shareOn: Int) {
        val executorService: ExecutorService = Executors.newFixedThreadPool(1)
        val mainThreadHandler: Handler = HandlerCompat.createAsync(Looper.getMainLooper())

        val view = binding.badgeLayout.root

        executorService.execute {
            mainThreadHandler.post {
                binding.progressBar.root.visible()
            }
            val bitmap = ImageUtil.getBitmapFromView(view)
            bitmap?.let {
                if (shareUri == null) {
                    shareUri = ImageUtil.getTempImageUri(it, requireContext())
                }
                mainThreadHandler.post {
                    binding.progressBar.root.gone()

                    when (shareOn) {
                        0 -> {
                            ShareUtil.shareOthers(requireContext(), shareUri)
                        }
                        1 -> {
                            ShareUtil.shareOnFacebook(requireContext(), shareUri)
                        }
                        2 -> {
                            ShareUtil.shareOnInsta(requireContext(), shareUri)
                        }
                        3 -> {
                            ShareUtil.shareOnWhatsapp(requireContext(), shareUri)
                        }
                    }
                }
            }
            mainThreadHandler.post {
                binding.progressBar.root.gone()
            }
        }
    }

}